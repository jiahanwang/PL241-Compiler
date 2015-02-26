package org.java.algorithm.graph.basics;

import java.util.Collection;

public interface DirectedGraph<V, E> extends Graph<V, E>{
	
	V destination(E edge);
	
	V source(E edge);
	
	
	int inDegree(V vertex);
	
	int outDegree(V vertex);
	
	
	Collection<V> incomingVertices(V vertex);
	
	Collection<V> outgoingVertices(V vertex);
	
	
	Collection<E> incomingEdges(V vertex);
	
	Collection<E> outgoingEdges(V vertex);
	
	
	boolean revert(E edge);
	
	boolean revertAll();
	
}
