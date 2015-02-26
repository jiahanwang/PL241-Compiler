package org.java.algorithm.graph.basics;

import java.util.*;

public abstract class AbstractUndirectedGraph<V, E>
			extends AbstractGraph<V, E>
			implements UndirectedGraph<V, E> {
	
	// implemented in adjacent list by default
	private Map<V, Set<E>>  vertices;
	private Map<E, Pair<V>> edges;
	private boolean allowingMultiEdges = false;
	private boolean allowingLoops = false;
	
	public AbstractUndirectedGraph(boolean multiEdges, boolean loops){
		super();
		vertices = new HashMap<V, Set<E>>();
		edges = new HashMap<E, Pair<V>>();
		allowingMultiEdges = multiEdges;
		allowingLoops = loops;
	}

	public boolean addEdge(E edge, V vertex1, V vertex2){
		if(edge == null || vertex1 == null || vertex2 == null) 
    		throw new IllegalArgumentException("Edges cannot contain null values");
		if(edges.containsKey(edge)){
			return false;
		}
		if(!this.allowingLoops){
			if(vertex1.equals(vertex2)){
				return false;
			}
		}
		if(!this.allowingMultiEdges){
			if(areAdjacent(vertex1, vertex2)){
				return false;
			}
		}
		addVertex(vertex1);
		vertices.get(vertex1).add(edge);
		addVertex(vertex2);
		vertices.get(vertex2).add(edge);
		edges.put(edge, new Pair<V>(vertex1, vertex2));
		return true;
	};

	public boolean addVertex(V vertex) {
		if(vertex == null) 
    		throw new NullPointerException("Vertices cannot contain null values");
		if(vertices.containsKey(vertex)){
			// this vertex already exists
			return false;
		}else{
			vertices.put(vertex, new HashSet<E>());
			return true;
		}
	}

	 
	public boolean removeEdge(E edge) {
		if(!edges.containsKey(edge)){
			// this edge doesn't exist (including null)
			return false;
		}else{
			Pair<V> pair = edges.get(edge);
			if(vertices.containsKey(pair.getSource()))
				vertices.get(pair.getSource()).remove(edge);
			if(vertices.containsKey(pair.getDestination()))
				vertices.get(pair.getDestination()).remove(edge);
			edges.remove(edge);
			return true;
		}
	}

	 
	public boolean removeVertex(V vertex) {
		if(!vertices.containsKey(vertex)){
			// this vertex doesn't exist(including null)
			return false;
		}else{
			Set<E> incidentEdges = new HashSet<E>(vertices.get(vertex));
			vertices.remove(vertex);
			// remove all the incident edges
			for(E edge : incidentEdges){
				removeEdge(edge);
			}
			return true;
		}
	}
	
	 
	public int numOfVertices() {
		return vertices.size();
	}

	 
	public int numOfEdges() {
		return edges.size();
	}

	 
	public boolean containsVertex(V vertex) {
		return vertices.containsKey(vertex);
	}

	 
	public boolean containsEdge(E edge) {
		return edges.containsKey(edge);
	}

	 
	public Collection<V> getVertices() {
		return Collections.unmodifiableCollection(vertices.keySet());
	}

	 
	public Collection<E> getEdges() {
		return Collections.unmodifiableCollection(edges.keySet());
	}

	 
	public Collection<V> adjacentVertices(V vertex) {
		if(!vertices.containsKey(vertex)){
			return null;
		}
		Set<V> verticesSet =  new HashSet<V>();
		for(E edge : vertices.get(vertex)){
			V v  = opposite(edge, vertex);
			if(v != null){
				verticesSet.add(v);
			}
		}
		return Collections.unmodifiableCollection(verticesSet);
	}

	 
	public Collection<E> incidentEdges(V vertex) {
		if(!vertices.containsKey(vertex)){
			return null;
		}else{
			return Collections.unmodifiableCollection(vertices.get(vertex));
		}
	}
	/**
	 * Test if two vertices are adjacent
	 * @param vertex1 one vertex
	 * @param vertex2 another vertex
	 * @return <code>false</code> if two vertices are not adjacent or any of the two vertices doesn't exist in the graph; <code>true</code> if two vertices are adjacent
	 * 
	 * */
	 
	public boolean areAdjacent(V vertex1, V vertex2) {
		if(! vertices.containsKey(vertex1) || ! vertices.containsKey(vertex2)){
			return false;
		}else{
			// must create a new Set, to prevent contaminating the original one
			Set<E> adjacentList = new HashSet<E>(vertices.get(vertex1));
			adjacentList.retainAll(vertices.get(vertex2));
			if(adjacentList.size() == 0){
				return false;
			}else{
				return true;
			}
		}
	}

	 
	public boolean areIncident(V vertex, E edge) {
		if(!vertices.containsKey(vertex) || !edges.containsKey(edge)){
			return false;
		}else{
			return edges.get(edge).contains(vertex);
		}
	}

	 
	public Collection<V> endVertices(E edge) {
		if(!edges.containsKey(edge)){
			return null;
		}
		Collection<V> verticesList = new LinkedList<V>();
		verticesList.add(edges.get(edge).getSource());
		verticesList.add(edges.get(edge).getDestination());
		return Collections.unmodifiableCollection(verticesList);
		
	}

	 
	public V opposite(E edge, V vertex) {
		if(!vertices.containsKey(vertex) || !edges.containsKey(edge)){
			return null;
		}
		Pair<V> pair = edges.get(edge); 
		if(vertex.equals(pair.getSource())){
			return pair.getDestination();
		}else 
			if (vertex.equals(pair.getDestination())){
				return pair.getSource();
			}else{
				// this edge is not incident to this vertex
				vertices.get(vertex).remove(edge);
				return null;
			}
	}

	 
	public int degree(V vertex) {
		if(!vertices.containsKey(vertex)){
			// return -1 when this vertex doesn't exist
			return -1;
		}else{
			return vertices.get(vertex).size();
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(V vertex: vertices.keySet()){
			builder.append(vertex).append(": ").append(vertices.get(vertex)).append("\n");
		}
		return builder.toString();
	}

}
