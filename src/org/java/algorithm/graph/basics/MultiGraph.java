package org.java.algorithm.graph.basics;


/**
 * A multigraph. A multigraph is a non-simple undirected graph in which no loops
 * are permitted, but multiple edges between any two vertices are. If you're
 * unsure about multigraphs, see: <a
 * href="http://mathworld.wolfram.com/Multigraph.html">
 * http://mathworld.wolfram.com/Multigraph.html</a>.
 */

public class MultiGraph<V, E> extends AbstractUndirectedGraph<V, E>{
	
	public MultiGraph(){
		super(true, false);
	}

}
