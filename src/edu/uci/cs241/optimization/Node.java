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

    public Node(String name) {
        super(name);
        live = new ArrayList<Interval>();
    }

    public String toString() {
        return super.getId()+":"+live.toString();
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
