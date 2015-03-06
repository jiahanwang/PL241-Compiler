package org.java.algorithm.graph.traverse;

import org.java.algorithm.graph.basics.Graph;

import java.util.*;


public class BFSIterator<V, E> extends AbstractGraphIterator<V, E>{
	
	public BFSIterator(Graph<V, E> graph, V src, V des, Comparator<? super V> c){
		super(graph, src, des, c);
	}
	
	public BFSIterator(Graph<V, E> graph, V src, Comparator<? super V> c){
		this(graph, src, null, c);
	}
	
	public BFSIterator(Graph<V, E> graph, V src, V des){
		this(graph, src, des, null);
	}
	
	public BFSIterator(Graph<V, E> graph, V src){
		this(graph, src, null, null);
	}
	
	public boolean hasNext() {
		return !getDesEncountered() && !isEmpty();
	}

	public V next() {
		if (!hasNext()){
            throw new NoSuchElementException();
		}
		V vertex = pollFirst();
		setMark(vertex, true);
		if(getDestination() != null && vertex.equals(getDestination())){
			setDesEncountered(true);
			return vertex;
		}
		List<V> adjacentVertices = new LinkedList<V>(getGraph().adjacentVertices(vertex));
		if(getComparator() != null){
			Collections.sort(adjacentVertices, getComparator());
		}
		for(V adjacentVertex : adjacentVertices){
			if(getMark(adjacentVertex) == false && !contains(adjacentVertex)){
				addLast(adjacentVertex);
			}
		}
		return vertex;
	}
	
}
