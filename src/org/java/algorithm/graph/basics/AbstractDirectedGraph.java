package org.java.algorithm.graph.basics;

import java.util.*;

/**
 * 
 * @author Jiahan Wang
 * */
public abstract class AbstractDirectedGraph<V, E> 
			extends AbstractGraph<V, E>
			implements DirectedGraph<V, E> {
	
    protected static final int INCOMING = 0;
    protected static final int OUTGOING = 1;
    
    // implemented in adjacent list by default
    private Map<V, Map<E, E>[]>  vertices;
	private Map<E, Pair<V>> edges;
	private boolean allowingMultiEdges = false;
	private boolean allowingLoops = false;
	
	AbstractDirectedGraph(boolean multiEdges, boolean loops){
		super();
		vertices = new HashMap<V, Map<E, E>[]>();
		edges = new HashMap<E, Pair<V>>();
		allowingMultiEdges = multiEdges;
		allowingLoops = loops;
	}
	
	public boolean addEdge(E edge, V src, V des){
		if(edge == null || src == null || des == null){
    		throw new IllegalArgumentException("Edges cannot contain null values");
		}
		if(edges.containsKey(edge)){
			return false;
		}
		if(!this.allowingLoops){
			if(src.equals(des)){
				return false;
			}
		}
		if(!this.allowingMultiEdges){
			if(isAdjacent(des, src)){
				return false;
			}
		}
		addVertex(src);
		vertices.get(src)[OUTGOING].put(edge, edge);
		addVertex(des);
		vertices.get(des)[INCOMING].put(edge, edge);
		edges.put(edge, new Pair<V>(src, des));
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public boolean addVertex(V vertex) {
		if(vertex == null) 
    		throw new NullPointerException("Vertices cannot contain null value");
		if(vertices.containsKey(vertex)){
			// this vertex already exists
			return false;
		}else{
			vertices.put(vertex, new HashMap[]{new HashMap<E, E>(), new HashMap<E, E>()});
			return true;
		}
	}

	public boolean removeEdge(E edge) {
		if(!edges.containsKey(edge)){
			// this edge doesn't exist (including null)
			return false;
		}else{
			Pair<V> pair = edges.get(edge);
			if(vertices.containsKey(pair.getSource())){
				vertices.get(pair.getSource())[OUTGOING].remove(edge);
			}
			if(vertices.containsKey(pair.getDestination())){
				vertices.get(pair.getDestination())[INCOMING].remove(edge);
			}
			edges.remove(edge);
			return true;
		}
	}

	public boolean removeVertex(V vertex) {
		if(!vertices.containsKey(vertex)){
			// this vertex doesn't exist(including null)
			return false;
		}else{
			Set<E> incidentEdges = new HashSet<E>(vertices.get(vertex)[OUTGOING].keySet());
			incidentEdges.addAll(vertices.get(vertex)[INCOMING].keySet());
			// remove all the incident edges
			for(E edge : incidentEdges){
				removeEdge(edge);
			}
			vertices.remove(vertex);
			return true;
		}
	}

	public int numOfVertices() {
		return vertices.size();
	}

	public int numOfEdges() {
		return edges.size();
	}

	public Collection<V> getVertices() {
		return Collections.unmodifiableCollection(vertices.keySet());
	}

	public Collection<E> getEdges() {
		return Collections.unmodifiableCollection(edges.keySet());
	}

	public boolean containsVertex(V vertex) {
		return vertices.containsKey(vertex);
	}

	public boolean containsEdge(E edge) {
		return edges.containsKey(edge);
	}
	
	/**
	 * Get the adjacent vertices(neighbors) of a vertex. In a directed graph, a vertex <i>s</i> is adjacent
	 * to vertex <i>u</i> only if there is edge <i>(u, s)</i> outgoing from <i>u</i> to <i>s</i>. 
	 * 
	 * @param vertex the vertex you want to get adjacent vertices of
	 * @return an unmodifiable Collection view of adjacent vertices
	 */
	public Collection<V> adjacentVertices(V vertex) {
		return outgoingVertices(vertex);
	}

	/**
	 * Get the incident edges of a vertex. In a directed graph, a edge <i>e</i> is incident with 
	 * a vertex <i>u</i> either it starts from <i>u</i> or it ends at <i>u</i>.
	 * 
	 * @param vertex the vertex you want to get incident edges of
	 * @return an unmodifiable Collection view of adjacent edges
	 */
	public Collection<E> incidentEdges(V vertex) {
		if(!vertices.containsKey(vertex)){
			return null;
		}else{
			Set<E> incidentSet = new HashSet<E>(vertices.get(vertex)[OUTGOING].keySet());
			incidentSet.addAll(vertices.get(vertex)[INCOMING].keySet());
			return Collections.unmodifiableCollection(incidentSet);
		}
	}
	
	/**
	 * Test if vertex <i>s</i> is adjacent to vertex <i>u</i>. In a directed graph, a vertex <i>s</i> is adjacent
	 * to vertex <i>u</i> only if there is edge <i>(u, s)</i> outgoing from <i>u</i> to <i>s</i>.
	 * 
	 * @param s one vertex
	 * @param u another vertex
	 * @return <code>false</code> if two vertices are not adjacent or any vertex doesn't exist in the graph; <code>true</code> if two vertices are adjacent
	 * @see #areAdjacent(V, V)
	 */
	public boolean isAdjacent(V s, V u) {
		if(! vertices.containsKey(s) || ! vertices.containsKey(u)){
			return false;
		}else{
			// must create a new Set, to prevent contaminating the original one
			Set<E> adjacentList = new HashSet<E>(vertices.get(u)[OUTGOING].keySet());
			adjacentList.retainAll(vertices.get(s)[INCOMING].keySet());
			if(adjacentList.size() == 0){
				return false;
			}else{
				return true;
			}
		}
	}
	
	/**
	 * Test if two vertices are adjacent, which means if there is an edge outgoing from <i>s</i> to <i>u</i> and another 
	 * edge outgoing from <i>u</i> to <i>s</i> then they are adjacent.
	 * 
	 * @param s one vertex
	 * @param t another vertex
	 * @return <code>false</code> if two vertices are not adjacent or any vertex doesn't exist in the graph; <code>true</code> if two vertices are adjacent
	 * @see #isAdjacent(V, V)
	 */
	public boolean areAdjacent(V s, V u) {
		if(! vertices.containsKey(s) || ! vertices.containsKey(u)){
			return false;
		}
		return isAdjacent(s, u) && isAdjacent(u,s);
	}

	public boolean areIncident(V vertex, E edge) {
		if(!vertices.containsKey(vertex) || !edges.containsKey(edge)){
			return false;
		}else{
			return edges.get(edge).contains(vertex);
		}
	}

	public V destination(E edge) {
		if(!edges.containsKey(edge)){
			return null;
		}
		return edges.get(edge).getDestination();
	}

	public V source(E edge) {
		if(!edges.containsKey(edge)){
			return null;
		}
		return edges.get(edge).getSource();
	}

	public int inDegree(V vertex) {
		if(!vertices.containsKey(vertex)){
			// return -1 when this vertex doesn't exist
			return -1;
		}else{
			return vertices.get(vertex)[INCOMING].size();
		}
	}

	public int outDegree(V vertex) {
		if(!vertices.containsKey(vertex)){
			// return -1 when this vertex doesn't exist
			return -1;
		}else{
			return vertices.get(vertex)[OUTGOING].size();
		}
	}

	public Collection<V> incomingVertices(V vertex) {
		if(!vertices.containsKey(vertex)){
			return null;
		}
		Set<V> verticesSet =  new HashSet<V>();
		for(E edge : vertices.get(vertex)[INCOMING].keySet()){
			V v  = source(edge);
			if(v != null){
				verticesSet.add(v);
			}
		}
		return Collections.unmodifiableCollection(verticesSet);
	}

	public Collection<V> outgoingVertices(V vertex) {
		if(!vertices.containsKey(vertex)){
			return null;
		}
		Set<V> verticesSet =  new HashSet<V>();
		for(E edge : vertices.get(vertex)[OUTGOING].keySet()){
			V v  = destination(edge);
			if(v != null){
				verticesSet.add(v);
			}
		}
		return Collections.unmodifiableCollection(verticesSet);
	}

	public Collection<E> incomingEdges(V vertex) {
		if(!vertices.containsKey(vertex)){
			return null;
		}
		return Collections.unmodifiableCollection(vertices.get(vertex)[INCOMING].keySet());
	}

	public Collection<E> outgoingEdges(V vertex) {
		if(!vertices.containsKey(vertex)){
			return null;
		}
		return Collections.unmodifiableCollection(vertices.get(vertex)[OUTGOING].keySet());
	}

	public boolean revert(E edge) {
		if(!edges.containsKey(edge)){
			return false;
		}
		Pair<V> pair = edges.get(edge);
		Map<E, E>[] adjacentList = vertices.get(pair.getSource());
		adjacentList[OUTGOING].remove(edge);
		adjacentList[INCOMING].put(edge, edge);
		adjacentList = vertices.get(pair.getDestination());
		adjacentList[INCOMING].remove(edge);
		adjacentList[OUTGOING].put(edge, edge);
		return true;
	}

	public boolean revertAll() {
		boolean successful = true;
		for(E edge : edges.keySet()){
			successful  = successful && revert(edge);
		}
		return successful;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for(V vertex: vertices.keySet()){
			builder.append(vertex).append(": in").append(vertices.get(vertex)[INCOMING].keySet())
								  .append(" out").append(vertices.get(vertex)[OUTGOING].keySet()).append("\n");
		}
		return builder.toString();
	}

}
