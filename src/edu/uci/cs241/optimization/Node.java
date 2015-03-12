package edu.uci.cs241.optimization;

import org.java.algorithm.graph.basics.AbstractVertex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ivan on 3/8/2015.
 */
public class Node extends AbstractVertex {

    public int cost;

    public List<Interval> live;

    public List<Node> group;

    public Node(String name) {
        super(name);
        this.live = new ArrayList<Interval>();
        this.group = new ArrayList<Node>();
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("").append(this.id).append("]\ncost: ").append(this.cost).append("\n");
        if(this.group.size() != 0) {
            builder.append("group: [");
            for (int i = 0, len = this.group.size(); i < len; i++) {
                builder.append(this.group.get(i).id + ",");
            }
            builder.deleteCharAt(builder.length() - 1);
            builder.append("]\n");
        }
        return builder.toString();
    }

    // Functions added from Franz's Paper "Linear Scan Register Allocation"
    public void addRange(int start, int end) {
        live.add(new Interval(start, end));
    }

    public class Interval {
        int start;
        int end;

        public Interval(int start, int end) {
            this.start = start;
            this.end = end;
        }

        public String toString() {
            return "("+start+","+end+")";
        }

    }



}
