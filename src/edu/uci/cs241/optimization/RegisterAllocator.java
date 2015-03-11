package edu.uci.cs241.optimization;

import edu.uci.cs241.ir.*;
import edu.uci.cs241.ir.types.BasicBlockType;
import edu.uci.cs241.ir.types.OperandType;
import org.java.algorithm.graph.basics.Node;
import org.java.algorithm.graph.basics.SimpleGraph;

import java.util.*;

/**
 * Created by Ivan on 3/8/2015.
 */
public class RegisterAllocator {

    private SimpleGraph<Node, String> ig;
    private List<List<Node>> liveRanges;
    private DefUseChain du;
    private HashMap<Integer, Integer> regMap;   // [Intermediate] = RegNo.
    private HashMap<String, Node> nodeMap;
    private Function func;

    public RegisterAllocator(Function func) {
        this.ig = new SimpleGraph<Node, String>();
        this.liveRanges = new ArrayList<List<Node>>();
        this.regMap = new HashMap<Integer, Integer>();
        this.func = func;
    }

    public void applyRA() throws Exception {
        buildLiveRanges();
        buildIG();
        allocateRegisters();
        replaceInstructions();
        reset();
    }

    public void reset() {
        ig = new SimpleGraph<Node, String>();
        liveRanges = new ArrayList<List<Node>>();
        regMap = new HashMap<Integer, Integer>();
    }
    /*
    Based on Franz's paper: Linear Scan Register Allocation on SSA Form
     */
    public void buildLiveRanges() throws Exception {
        // Intialize the list of lists
        for(int i = 0; i < func.ir.ins.size(); i++) {
            liveRanges.add(new ArrayList<Node>());
        }
        // Build the reverse ordering for BuildIntervals
        ArrayList<BasicBlock> ro = buildOrdering(func.entry);
        for(BasicBlock b : ro) {
            // liveSet = union of all live in b's successors
            HashSet<String> liveSet = new HashSet<String>();
            for(BasicBlock d : b.dom) {
                liveSet.addAll(d.live);
            }
            //TODO: Phi traversal
            int blockStartID = b.ins.get(0).id;
            int blockEndID = b.ins.get(b.ins.size()-1).id;

            // For each opd in live set
            // These have to be created already otherwise they would not be live now.
            for(String s : liveSet) {
                Node n = nodeMap.get(s);
                int start = Integer.parseInt(n.getId());
                if(start >= blockStartID && start <= blockEndID) {
                    n.addRange(start, blockEndID);
                } else {
                    n.addRange(blockStartID, blockEndID);
                }
            }

            // traverse instructions in reverse order
            for(int i = b.ins.size(); i > 0; i--) {
                // Due to our structure, all of these are inputs.
                Instruction in = b.ins.get(i);
                for(Operand o : in.operands) {
                    //If operand is constant, skip
                    if(o.type  == OperandType.CONST) {
                        continue;
                    }
                    // Since this var is alive
                    liveSet.add(o.getValue());
                    Node var = new Node(o.getValue());
                    // This is for the case that the var ends within the same block
                    int temp = 0;
                    try {
                        temp = Integer.parseInt(o.getValue());
                        if(temp > blockStartID && temp < blockEndID) {
                            // leave temp to be set
                        } else {
                            temp = blockStartID;
                            // else its out of range of this block
                        }
                    } catch(Exception e) {
                        // Its probably a variable.
                        var.addRange(0, in.id);
                        liveSet.remove(o.getValue());
                    }
                    var.addRange(temp, in.id);
                }
            }

            // if b is loop header
            if(b.type == BasicBlockType.WHILE) {
//                BasicBlock loopEnd = b.loopEnd;
            }
            b.live = liveSet;
        }


//        Node n = new Node(i.toString());
//        n.cost = length;
//        for(int j = start; j <= last; j++) {
//            liveRanges.get(j).add(n);
//        }
//        ig.addVertex(n);
    }

    private ArrayList<BasicBlock> buildOrdering (BasicBlock b) {
        ArrayList<BasicBlock> a = new ArrayList<BasicBlock>();
        if(b == null) {
            return a;
        }
        // Reverse Pre-order notation
        a.addAll(buildOrdering(b.right));
        a.addAll(buildOrdering(b.left));
        a.add(b);
        return a;
    }

    public void printLiveRanges() {
        if(liveRanges.size() < 1) {
            return;
        }
        for(int i = 0; i < liveRanges.size(); i++) {
            System.out.print(i + ": ");
            for(Node n : liveRanges.get(i)) {
                System.out.print(n.toString()+", ");
            }
            System.out.println("");
        }
    }

    public SimpleGraph<Node, String> buildIG() {
        for(int i = 0; i < liveRanges.size(); i++) {
            if(liveRanges.get(i).size() < 2) {
                continue;
                // there is no interference on this line.
            }
            for(Node n : liveRanges.get(i)) {
                for(Node n2 : liveRanges.get(i)) {
                    if(n.equals(n2)) {
                        continue;       // dont link to itself
                    }
                    if(ig.containsEdge(n.getId()+" -- "+n2.getId()) || ig.containsEdge(n2.getId()+" -- "+n.getId())){
                        continue;       // already has this edge, dont add again.
                    }
                    // Special case for def beginning and use endings
                    if(n.end == n2.start || n2.end == n.start){
//                        continue;       // there is no overlap for this def use
                    }
                    ig.addEdge(n.getId()+" -- "+n2.getId(), n, n2);
                }
            }
        }
        return ig;
    }

    int NUM_REG = 8;
    public void allocateRegisters() {
        if(ig == null || ig.getVertices().size() < 1) {
            return;
        }
        Node removed = getVertexToRemove(NUM_REG);
        // Save all the edges
        ArrayList<Node> adj = new ArrayList<Node>();
        for(Node n : ig.adjacentVertices(removed)) {
            adj.add(n);
        }
        // Remove this vertex and attempt to recursively color regs
        ig.removeVertex(removed);    // this takes care of edges
        allocateRegisters();

        // Grab neighbor colors to make sure we don't use them
        LinkedHashSet<Integer> adjRegs = new LinkedHashSet<Integer>();
        for(Node n : adj) {
            adjRegs.add(regMap.get(n));
        }
        // Attempt to assign a color / register
        int reg = 1;
        while(true) {
            // if already in use or special reg
            if(adjRegs.contains(reg) || (reg >= 27 && reg <= 30)) {
                reg++;
            } else {
                // assign it to this
                regMap.put(Integer.valueOf(removed.getId()), reg);
                break;
            }
        }
    }

    public Node getVertexToRemove(int num) {
        // Get node with n degrees or less or grab least cost.
        int min = 9999;
        int max = -1;
        Node minNode = null, maxNode = null;
        for(Node n : ig.getVertices()) {
            if(ig.adjacentVertices(n).size() > max && ig.adjacentVertices(n).size() < num) {
                maxNode = n;
            }
            if(n.cost < min) {
                minNode = n;
                min = n.cost;
            }
        }
        return (maxNode != null) ? maxNode : minNode;
    }

    public void replaceInstructions() throws Exception {
        for(Instruction i : func.ir.ins) {
            // Store regno into the instruction
            if(regMap.containsKey(i.id)) {
                i.regno = regMap.get(i.id);
            }
            // Change the operand intermediates to regnos
            for(Operand o : i.operands) {
                if(o.type == OperandType.INST) {
                    if(regMap.containsKey(o.line)) {
                        o.type = OperandType.REG;
                        o.regno = regMap.get(o.line);
                    } else {
                        throw new Exception("Error in register allocation: " +
                                "trying to replace intermediate ["+o.line+"] with reg but was never allocated.");
                    }
                }
            }
        }
    }

    public SimpleGraph<Node, String> getIG() {
        return ig;
    }

    public void printRegMap() {
        System.out.println(regMap.toString());
    }


}
