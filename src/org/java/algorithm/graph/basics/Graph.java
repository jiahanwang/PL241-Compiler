package org.java.algorithm.graph.basics;

import java.util.Collection;

public interface Graph<V, E> {
	
	boolean addEdge(E edge, V src, V des);
	
	boolean addVertex(V vertex);

	
	boolean removeEdge(E edge);
	
	boolean removeAllEdges(Collection<? extends E> edges);

	boolean removeVertex(V vertex);
	
	boolean removeAllVertices(Collection<? extends V> vertices);
	
	
	int numOfVertices();
	
	int numOfEdges();
	
	
	Collection<V> getVertices();
	
	Collection<E> getEdges();
	
	
	boolean containsVertex(V vertex);
	
	boolean containsEdge(E edge);
	
	
	Collection<V> adjacentVertices(V vertex);
	
	Collection<E> incidentEdges(V vertex);
	
	
	boolean areAdjacent(V vertex1, V vertex2);
	
	boolean areIncident(V vertex, E edge);
	

}
