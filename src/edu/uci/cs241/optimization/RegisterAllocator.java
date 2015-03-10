package edu.uci.cs241.optimization;

import edu.uci.cs241.ir.DefUseChain;
import edu.uci.cs241.ir.Function;
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
    private HashMap<Node, Integer> regMap;
    private Function func;

    public RegisterAllocator(Function func) {
        this.ig = new SimpleGraph<Node, String>();
        this.liveRanges = new ArrayList<List<Node>>();
        this.regMap = new HashMap<Node, Integer>();
        this.func = func;
    }

    public void reset() {
        ig = new SimpleGraph<Node, String>();
        liveRanges = new ArrayList<List<Node>>();
        regMap = new HashMap<Node, Integer>();
    }

    public void buildLiveRanges() throws Exception {
        // Intialize the list of lists
        for(int i = 0; i < func.ir.ins.size(); i++) {
            liveRanges.add(new ArrayList<Node>());
        }
        // Grab the information from the def-use chain
        du = func.getDu();
        for(Integer i : du.intermediates.keySet()) {
            int start = du.intermediates.get(i).def.id;
            int length = du.intermediates.get(i).uses.size();
            if(length < 1) {
                continue;
                // could not find last use. just forget about it.
            }
            int last = (du.intermediates.get(i).uses).get(length - 1).id;
            if(last == -1) {
                throw new Exception("Error building live ranges from def use chain");
            }
            Node n = new Node(i.toString());
            n.cost = length;
            for(int j = start; j <= last; j++) {
                liveRanges.get(j).add(n);
            }
        }
        // Parse vars
        for(String s : du.variables.keySet()) {
            if(du.variables.get(s).def == null) {
                continue;
            }
            int start = du.variables.get(s).def.id;
            if(start < 0) {
                start = 0;
            }
            int length = du.variables.get(s).uses.size();
            if(length < 1) {
                continue;
                // could not find last use. just forget about it.
            }
            int last = (du.variables.get(s).uses).get(length - 1).id;
            if(last == -1) {
                throw new Exception("Error building live ranges from def use chain");
            }
            Node n = new Node(s);
            n.cost = length;
            for(int j = start; j <= last; j++) {
                liveRanges.get(j).add(n);
            }
        }
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
                // no interference on this line.
            }
            for(Node n : liveRanges.get(i)) {
                for(Node n2 : liveRanges.get(i)) {
                    if(ig.containsEdge(n.getId()+" -- "+n2.getId()) || ig.containsEdge(n2.getId()+" -- "+n.getId())){
                        continue;
                        // already has this edge
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
            if(adjRegs.contains(reg) ||  (reg >= 27 && reg <= 30)) {
                reg++;
            } else {
                // assign it to this
                regMap.put(removed, reg);
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
            if(ig.incidentEdges(n).size() > max && ig.incidentEdges(n).size() < num) {
                maxNode = n;
            }
            if(n.cost < min) {
                minNode = n;
                min = n.cost;
            }
        }
        return (maxNode != null) ? maxNode : minNode;
    }

    public SimpleGraph<Node, String> getIG() {
        return ig;
    }

    public void printRegMap() {
        System.out.println(regMap.toString());
    }


}
