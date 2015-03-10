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

    public SimpleGraph<Node, String> ig;
    private List<List<Node>> liveRanges;
    private DefUseChain du;
    public HashMap<Node, Integer> regMap;

    public RegisterAllocator() {
        ig = new SimpleGraph<Node, String>();
        liveRanges = new ArrayList<List<Node>>();
    }

    public void buildLiveRanges(Function func) throws Exception {
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
    public void allocateRegisters(SimpleGraph<Node, String> g) {


        for(Node n : g.getVertices()) {

        }

        Node remove = null;
        // Save all the edges
        List<Node> adj = (List<Node>) g.adjacentVertices(remove);
        // Remove this vertex and attempt to recursively color regs
        ig.removeVertex(remove);    // this takes care of edges
        allocateRegisters(g);

        // Grab neighbor colors to make sure we don't use them
        LinkedHashSet<Integer> adjRegs = new LinkedHashSet<Integer>();
        for(Node n : adj) {
            adjRegs.add(regMap.get(n));
        }
        // Attempt to assign a color / register
        int reg = 1;
        while(true) {
            if(regMap.containsKey(reg)) {
                reg++;
            } else {
                // assign it to this

            }
        }




    }


}
