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
    private HashMap<String, Integer> regMap;   // [Intermediate] = RegNo.
    private HashMap<String, Node> nodeMap;
    private Function func;
    private List<Instruction> phis;

    public RegisterAllocator(Function func) {
        this.ig = new SimpleGraph<Node, String>();
        this.liveRanges = new ArrayList<Set<Node>>();
        this.regMap = new HashMap<String, Integer>();
        this.func = func;
        this.nodeMap = new HashMap<String, Node>();
        this.du = func.getDu();
        this.phis = new LinkedList<Instruction>();
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
        this.regMap = new HashMap<String, Integer>();
        this.nodeMap = new HashMap<String, Node>();
    }


    public void initializeGraph(){
        // add all used intermediate in the graph
        for(Map.Entry<Integer, DefUseChain.Item> inter : this.du.intermediates.entrySet()){
            int frequency = inter.getValue().uses.size();
            if(frequency == 0) continue;
            Node n = new Node(String.valueOf(inter.getKey()));
            n.cost = frequency;
            nodeMap.put(n.getId(), n);
            this.ig.addVertex(n);
        }
        // add non-local reference in the graph
        for(Map.Entry<String, DefUseChain.Item> inter : this.du.variables.entrySet()){
            int frequency = inter.getValue().uses.size();
            //if(frequency == 0) continue;
            Node n = new Node(inter.getKey());
            n.cost = frequency;
            nodeMap.put(n.getId(), n);
            this.ig.addVertex(n);
        }
    }

    /*
    Based on Franz's paper: Linear Scan Register Allocation on SSA Form
     */
    public void buildLiveRanges() throws Exception {
        // Initialize the Graph first
        initializeGraph();
        // Build the reverse ordering for BuildIntervals
        boolean explored[] = new boolean[100];
        ArrayList<BasicBlock> ro = buildOrdering(func.entry, explored);
        // Build intervals
        for(BasicBlock b : ro) {
            // Avoid null / empty blocks
            if(b.ins == null || b.ins.isEmpty()) {
                continue;
            }
            /** liveSet = union of all live in b's successors **/
            HashSet<String> liveSet = new HashSet<String>();
            for(BasicBlock successor : b.getSuccessors()) {
                // Add all vars that were alive in the successors
                // (they were used so must be defined in a successor block)
                liveSet.addAll(successor.live);
                //This is to add all the corresponding input phis from successor blocks.
                liveSet.addAll(successor.getRelevantPhiInputs(b));
            }
            /** traverse instructions in reverse order **/
            for(int i = b.ins.size() - 1; i >= 0; i--) {
                // Due to our structure, all of these are inputs.
                Instruction in = b.ins.get(i);
                // Skip PHI
                if(in.operator == InstructionType.PHI) continue;

                // For output
                    // If output, this def check has to come first to avoid over lapping with the end use vars
                    // If it is contained in nodeMap, then it must have been used already.
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

                // For Inputs
                for(String input : in.getInputs()) {
                    liveSet.add(input);
                }
            }

            /** removal of phis **/
            for(Instruction i : b.phis) {
                liveSet.remove(String.valueOf(i.id));
            }

            /** if b is loop header **/
            if(b.type == BasicBlockType.WHILE) {
                BasicBlock loopEnd = b.loop_end;
                //traverse the WHILE BODY to get Defs
                List<String> to_add = new LinkedList<String>();
                int start = b.getStart();
                int end = loopEnd.getEnd();
                ListIterator<Instruction> it = func.ir.ins.listIterator(start);
                int i = start;
                while(i++ <= end){
                    Instruction in = it.next();
                    // if it is in the node map, then it is a valid def
                    if(nodeMap.containsKey(String.valueOf(in.id))) {
                        // it is a valid def
                        to_add.add(String.valueOf(in.id));
                    }
                }
                for(String live : liveSet){
                    for(String def : to_add) {
                        ig.addEdge(live + "--" + def, nodeMap.get(live), nodeMap.get(def));
                    }
                }

            }
            b.live = liveSet;
        }
    }

    public void mergePhis(){
        if(this.ig == null || this.ig.numOfVertices() <= 1) return;
        for(Instruction in : phis){
            List<Node> groups = new ArrayList<Node>(10);
            if(nodeMap.containsKey(String.valueOf(in.id)))
                groups.add(nodeMap.get(String.valueOf(in.id)));
            // build the group
            for (int i = 0, len = in.operands.size(); i < len; i++) {
                Operand operand = in.operands.get(i);
                if(operand.type != OperandType.INST && !nodeMap.containsKey(operand.getValue())) continue;
                Node potential = nodeMap.get(operand.getValue());
                boolean interfere = false;
                for(int j  = 0, jlen = groups.size(); j < jlen; j++){
                    Node existing = groups.get(j);
                    if(this.ig.areAdjacent(potential, existing)) {
                        interfere = true;
                        break;
                    }
                }
                if(!interfere) groups.add(potential);
            }
            // add the group indicator in node
            for(Node node : groups){
                List<Node> g_indicator = new LinkedList<Node>(groups);
                g_indicator.remove(node);
                node.group = g_indicator;
            }
        }
    }

    private ArrayList<BasicBlock> buildOrdering (BasicBlock b, boolean[] explored) {
        ArrayList<BasicBlock> a = new ArrayList<BasicBlock>();
        if(b == null || explored[b.id]) {
            return a;
        }
        explored[b.id] = true;
        // Record all the phis
        this.phis.addAll(b.phis);
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
        ig.removeVertex(removed);
        // Deal with the 'group' of merged nodes as one.
        for(Node n : removed.group) {
            if(!ig.containsVertex(n)) {
                continue;
                // may have been removed already.
            }
            for(Node a : ig.adjacentVertices(n)) {
                adj.add(a);
            }
            ig.removeVertex(n);    // this takes care of edges
        }

        allocateRegisters();

        // Grab neighbor colors to make sure we don't use them
        LinkedHashSet<Integer> adjRegs = new LinkedHashSet<Integer>();
        for(Node n : adj) {
            adjRegs.add(regMap.get(n.getId()));
        }
        // Attempt to assign a color / register
        int reg = 1;
        while(true) {
            // if already in use or special reg
            if(adjRegs.contains(reg) || (reg >= 28 && reg <= 30)) {
                reg++;
            } else {
                // assign it to this
                regMap.put(removed.getId(), reg);
                for(Node n : removed.group) {
                    regMap.put(n.getId(), reg);
                }
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
            if(regMap.containsKey(String.valueOf(i.id))) {
                i.regno = regMap.get(String.valueOf(i.id));
            }
            // Change the operand intermediates to regnos
            for(Operand o : i.operands) {
                if(o.type == OperandType.INST  || o.type == OperandType.VARIABLE) {
                    if(regMap.containsKey(o.getValue())) {
                        o.regno = regMap.get(o.getValue());
                        o.type = OperandType.REG;
                    } else {
                        //throw new Exception("Error in register allocation: " +
                        //        "trying to replace intermediate ["+o.line+"] with reg but was never allocated.");
                    }
                }
            }
        }
    }

    public SimpleGraph<Node, String> getIG() {
        return ig;
    }

    public String getRegMapinString(){
        StringBuilder builder = new StringBuilder();
        for(Map.Entry<String, Integer> entry : regMap.entrySet()){
            builder.append(entry.getKey()).append(" = R");
            builder.append(entry.getValue()).append("\n");
        }
        return builder.toString();
    }


}
