package org.java.algorithm.graph.basics;


/**
 * A simple graph. A simple graph is an undirected graph for which at most one
 * edge connects any two vertices, and loops are not permitted. 
 * 
 */

public class SimpleGraph<V,E> extends AbstractUndirectedGraph<V,E>{
	
	public SimpleGraph(){
		super(false, false);
	}
	
}
