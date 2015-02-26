package org.java.algorithm.graph.basics;

import java.util.Collection;

public interface UndirectedGraph<V, E> extends Graph<V, E>{
	
	int degree(V vertex);
	
	Collection<V> endVertices(E edge);
	
	V opposite(E edge, V vertex);

}
