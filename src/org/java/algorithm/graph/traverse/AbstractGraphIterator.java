package org.java.algorithm.graph.traverse;

import java.util.*;

import org.java.algorithm.graph.basics.Graph;

public abstract class AbstractGraphIterator<V, E> implements Iterator<V>{
	
	private Map<V, Boolean> traversalMarks;
	private LinkedList<V> traversalMemory;
	private final Graph<V, E> graph;
	private V destination;
	private Comparator<? super V> comparator;
	private boolean desEncountered = false;
	
	public AbstractGraphIterator(Graph<V, E> graph, V src, V des, Comparator<? super V> c){
		if(graph ==  null || src == null){
			throw new NullPointerException("Graph or source vertex is null");
		}
		if(!graph.containsVertex(src)){
			throw new IllegalArgumentException("Source vertex doesn't exist in graph");
		}
		if(des != null && !graph.containsVertex(des)){
			throw new IllegalArgumentException("Destination vertex doesn't exist in graph");
		}
		this.traversalMemory = new LinkedList<V>();
		this.traversalMemory.add(src);
		this.graph = graph;
		this.destination = des;
		this.comparator = c;
		this.traversalMarks = new HashMap<V, Boolean>();
		for(V vertex : graph.getVertices()){
			traversalMarks.put(vertex, false);
		}
	}
	
	protected boolean getMark(V vertex){
		return traversalMarks.get(vertex);
	}
	
	protected void setMark(V vertex, boolean mark){
		traversalMarks.put(vertex, mark);
	}
	
	protected Graph<V, E> getGraph() {
		return graph;
	}

	protected V getDestination() {
		return destination;
	}

	protected void setDestination(V destination) {
		this.destination = destination;
	}

	protected Comparator<? super V> getComparator() {
		return comparator;
	}

	protected void setComparator(Comparator<? super V> comparator) {
		this.comparator = comparator;
	}
	
	protected boolean getDesEncountered() {
		return desEncountered;
	}

	protected void setDesEncountered(boolean desEncountered) {
		this.desEncountered = desEncountered;
	}
	
	protected V pollFirst(){
		return traversalMemory.pollFirst();
	}
	
	protected V pollLast(){
		return traversalMemory.pollLast();
		
	}
	
	protected boolean addLast(V vertex){
		return traversalMemory.add(vertex);
	}
	
	protected boolean addLevel(Collection<V> vertices){
		return traversalMemory.addAll(vertices);
	}
	
	protected boolean isEmpty(){
		return traversalMemory.isEmpty();
	}
	
	protected boolean contains(V vertex){
		return this.traversalMemory.contains(vertex);
	}
	
	public void remove(){
        throw new UnsupportedOperationException();
    }
}
