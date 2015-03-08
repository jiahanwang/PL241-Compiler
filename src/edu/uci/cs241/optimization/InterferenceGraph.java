package edu.uci.cs241.optimization;

import edu.uci.cs241.ir.DefUseChain;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan on 3/8/2015.
 */
public class InterferenceGraph {

    List<Node> nodes;

    public InterferenceGraph(DefUseChain du) {
        nodes = new ArrayList<Node>();
        // Build IG from DU


    }

    public void removeNode(String name) {
        for(Node n : nodes) {
            if(n.name.equals(name)) {
                for(Node e : n.edges) {
                    n.removeEdge(e);
                }
                this.nodes.remove(n);
                return;
            }
        }
    }





    public class Node {
        String name;
        List<Node> edges;

        public Node(String name) {
            this.name = name;
            edges = new ArrayList<Node>();
        }

        public void addEdge(Node v) {
            this.edges.add(v);
            v.edges.add(this);
        }

        public void removeEdge(Node v) {
            this.edges.remove(v);
            v.edges.remove(this);
        }

        public int getEdges() {
            return edges.size();
        }

    }
}
