package org.java.algorithm.graph.basics;

import java.util.Collection;

public abstract class AbstractGraph<V, E> implements Graph<V, E> {
	
	protected AbstractGraph() {
	}

	public boolean removeAllEdges(Collection<? extends E> edges) {
		boolean modified = false;
		for(E edge : edges){
			modified |= removeEdge(edge);
		}
		return modified;
	}

	public boolean removeAllVertices(Collection<? extends V> vertices) {
		boolean modified = false;
        for (V vertex : vertices) {
            modified |= removeVertex(vertex);
        }
        return modified;
	}

	@Override
	public Graph<V, E> clone(){
		throw new UnsupportedOperationException();
		
	}
	
}
