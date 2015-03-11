package edu.uci.cs241.optimization;

import edu.uci.cs241.ir.*;
import edu.uci.cs241.ir.types.BasicBlockType;
import edu.uci.cs241.ir.types.InstructionType;
import edu.uci.cs241.ir.types.OperandType;
import org.java.algorithm.graph.basics.SimpleGraph;

import java.util.*;

/**
 * Created by Ivan on 3/8/2015.
 */
public class RegisterAllocator {

    private SimpleGraph<Node, String> ig;
    private ArrayList<Set<Node>> liveRanges;
    private DefUseChain du;
    private HashMap<Integer, Integer> regMap;   // [Intermediate] = RegNo.
    private HashMap<String, Node> nodeMap;
    private Function func;

    public RegisterAllocator(Function func) {
        this.ig = new SimpleGraph<Node, String>();
        this.liveRanges = new ArrayList<Set<Node>>();
        this.regMap = new HashMap<Integer, Integer>();
        this.func = func;
        this.nodeMap = new HashMap<String, Node>();
        this.du = func.getDu();
    }

    public void applyRA() throws Exception {
        buildLiveRanges();
        buildIG();
        allocateRegisters();
        replaceInstructions();
        reset();
    }

    public void reset() {
        this.ig = new SimpleGraph<Node, String>();
        this.liveRanges = new ArrayList<Set<Node>>();
        this.regMap = new HashMap<Integer, Integer>();
        this.nodeMap = new HashMap<String, Node>();
    }
    /*
    Based on Franz's paper: Linear Scan Register Allocation on SSA Form
     */
    public void buildLiveRanges() throws Exception {

        // Build the reverse ordering for BuildIntervals
        boolean explored[] = new boolean[100];
        ArrayList<BasicBlock> ro = buildOrdering(func.entry, explored);
        for(BasicBlock b : ro) {
            // Avoid null / empty blocks
            if(b.ins == null || b.ins.isEmpty()) {
                continue;
            }
            // liveSet = union of all live in b's successors
            HashSet<String> liveSet = new HashSet<String>();
            for(BasicBlock d : b.getSuccessors()) {
                // Add all vars that were alive in the successors
                // (they were used so must be defined in a successor block)
                liveSet.addAll(d.live);
                //This is to add all the corresponding input phis from successor blocks.
                for (Instruction i : d.phis) {
                    // Get left or right phi depending on the block it came from.
                    Operand o = (d == b.left) ? i.operands.get(0) : i.operands.get(1);
                    if (o.type != OperandType.CONST) {
                        liveSet.add(o.getValue());
                    }
                }
            }
            // traverse instructions in reverse order
            for(int i = b.ins.size() - 1; i >= 0; i--) {
                // Due to our structure, all of these are inputs.
                Instruction in = b.ins.get(i);

                if(in.operator == InstructionType.BRA || in.operator == InstructionType.RETURN
//                        || in.operator == InstructionType.BNE || in.operator == InstructionType.BEQ
//                        || in.operator == InstructionType.BLE || in.operator == InstructionType.BLT
//                        || in.operator == InstructionType.BGE || in.operator == InstructionType.BGT
                        || in.operator ==  InstructionType.WLN || in.operator == InstructionType.LOADPARAM) {
                    continue;
                }

                if(in.operator == InstructionType.PHI) {
                    // Special case; create nodes and skip
                    for(Operand o : in.operands) {
                        Node n = new Node(o.getValue());
                        nodeMap.put(o.getValue(), n);
                    }
                    continue;
                }

                // If output, this def check has to come first to avoid over
                // lapping with the end use vars
                // If it is contained in nodeMap, then it must hvave been used already.
                if(nodeMap.containsKey(String.valueOf(in.id))) {
                    // add interference with liveset
                    Node n = nodeMap.get(String.valueOf(in.id));
                    for (String s : liveSet) {
                        Node n2 = nodeMap.get(s);
                        if (n.equals(n2)) {
                            continue;
                        }
                        ig.addEdge(n2.getId() + "--" + n.getId(), n, n2);
                    }
                    liveSet.remove(n.getId());
                }

                // For Inputs / USE
                // create the node, insert into live range
                for(Operand o : in.operands) {
                    //If operand is constant, skip
                    if(o.type  == OperandType.CONST
                            || o.type == OperandType.ARR_ADDRESS
                            || o.type == OperandType.BASE_ADDRESS
                            || o.type == OperandType.MEM_ADDRESS
                            || o.type == OperandType.JUMP_ADDRESS) {
                        continue;
                    }
                    // Since this var is alive, add to set
                    Node var = new Node(o.getValue());
                    nodeMap.put(o.getValue(), var);
                    liveSet.add(o.getValue());
                    ig.addVertex(var);
                }
            }

            // removal of phis
            for(Instruction i : b.phis) {
                    liveSet.remove(String.valueOf(i.id));
            }

            // if b is loop header
            if(b.type == BasicBlockType.WHILE) {
                BasicBlock loopEnd = b.loop_end;
                //traverse the WHILE BODY

            }
            b.live = liveSet;
        }
    }

    private ArrayList<BasicBlock> buildOrdering (BasicBlock b, boolean[] explored) {
        ArrayList<BasicBlock> a = new ArrayList<BasicBlock>();
        if(b == null || explored[b.id]) {
            return a;
        }
        explored[b.id] = true;
        // Reverse Pre-order notation
        a.addAll(buildOrdering(b.right, explored));
        a.addAll(buildOrdering(b.left, explored));
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

    public void printNodeMap() {
        System.out.println(nodeMap.toString());
    }

    public SimpleGraph<Node, String> buildIG() {
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
