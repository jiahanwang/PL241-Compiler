package org.java.algorithm.graph.traverse;

import org.java.algorithm.graph.basics.*;

import java.util.*;


public class Traverse {
	
	private enum TraversalKind{
		BFS, DFS;
	}
	
	private static <V, E> List<V> traverse(Graph<V, E> graph, V src, V des, Comparator<? super V > comparator, TraversalKind indicator){
		if(graph == null || src == null){
			throw new NullPointerException("Graph or source vertex is null");
		}
		if(!graph.containsVertex(src)){
			throw new IllegalArgumentException("Source vertex doesn't exist in graph");
		}
		if(des != null && !graph.containsVertex(des)){
			throw new IllegalArgumentException("Destination vertex doesn't exist in graph");
		}
		// need extra space to store marks for traversal, boolean for instance 
		Map<V, Boolean> traversalMarks = new HashMap<V, Boolean>();
		for(V vertex : graph.getVertices()){
			traversalMarks.put(vertex, false);
		}
		// declare the list to be returned
		List<V> traversalList = new LinkedList<V>();
		// declare extra memory to control the traversal
		LinkedList<V> memory = new LinkedList<V>();
		memory.addLast(src);
		// if DFS, reverse the comparator
		if(comparator != null && indicator == TraversalKind.DFS){
			// If the specified comparator is null, it returns a comparator that imposes the reverse of the natural ordering 
			comparator  = Collections.reverseOrder(comparator);
		}
		while(!memory.isEmpty()){
			V vertex;
			if(indicator == TraversalKind.BFS){
				vertex = memory.pollFirst();
			}else{
				vertex = memory.pollLast();
			}
			traversalMarks.put(vertex,true);
			traversalList.add(vertex);
			// encounter the destination, return
			if(des!= null && vertex.equals(des)){
				return traversalList;
			}
			List<V> adjacentVertices = new LinkedList<V>(graph.adjacentVertices(vertex));
			if(comparator != null){
				Collections.sort(adjacentVertices, comparator);
			}
			for(V adjacentVertex: adjacentVertices){
				if(traversalMarks.get(adjacentVertex) == false && !memory.contains(adjacentVertex)){
					memory.addLast(adjacentVertex);
				}
			}
		}
		return traversalList;
	}
	
	public static <V, E> List<V> bfs(Graph<V, E> graph, V src, V des, Comparator<? super V > comparator){
		return traverse(graph, src, des, comparator, TraversalKind.BFS);
	}
	
	public static <V, E> List<V> bfs(Graph<V, E> graph, V source, Comparator<? super V > c){
		return bfs(graph, source, null, c);
	}
	
	public static <V, E> List<V> bfs(Graph<V, E> graph, V source, V destination){
		return bfs(graph, source, destination, null);
	}
	
	public static <V, E> List<V> bfs(Graph<V, E> graph, V source){
		return bfs(graph, source, null, null);
	}
	
	public static <V, E> List<V> dfs(Graph<V, E> graph, V src, V des, Comparator<? super V > comparator){
		return traverse(graph, src, des, comparator, TraversalKind.DFS);
	}
	
	public static <V, E> List<V> dfs(Graph<V, E> graph, V source, Comparator<? super V > c){
		return dfs(graph, source, null, c);
	}
	
	public static <V, E> List<V> dfs(Graph<V, E> graph, V source, V destination){
		return dfs(graph, source, destination, null);
	}
	
	public static <V, E> List<V> dfs(Graph<V, E> graph, V source){
		return dfs(graph, source, null, null);
	}
	
	public static <V, E> List<List<V>> bfsInLevels(Graph<V, E> graph, V src, Comparator<? super V > comparator){
		if(graph == null || src == null){
			throw new NullPointerException("Graph or source vertex is null");
		}
		if(!graph.containsVertex(src)){
			throw new IllegalArgumentException("Source vertex doesn't exist in graph");
		}
		// need extra space to store marks for traversal, boolean for instance 
		Map<V, Boolean> traversalMarks = new HashMap<V, Boolean>();
		for(V vertex : graph.getVertices()){
			traversalMarks.put(vertex, false);
		}
		// declare the list to be returned
		List<List<V>> traversalList = new LinkedList<List<V>>();
		// add the first level
		traversalList.add(new LinkedList<V>());
		// visit the root
		traversalMarks.put(src, true);
		traversalList.get(0).add(src);
		for(int i = 0; !traversalList.get(i).isEmpty(); i++){
			traversalList.add(new LinkedList<V>());
			// sort the vertices according to comparator, if comparator is null, then natural ordering is used
			Collections.sort(traversalList.get(i), comparator);
			for(V vertex : traversalList.get(i)){
				List<V> adjacentVertices = new LinkedList<V>(graph.adjacentVertices(vertex));
				Collections.sort(adjacentVertices, comparator);
				for(V adjacentVertex: adjacentVertices){
					if(traversalMarks.get(adjacentVertex) == false){
						traversalList.get(i + 1).add(adjacentVertex);
						// visit the vertex
						traversalMarks.put(adjacentVertex, true);
					}
				}
			}
		}
		traversalList.remove(traversalList.size() - 1);
		return traversalList;
	}

	public static <V, E> List<List<V>> bfsInLevels(Graph<V, E> graph, V src){
		return bfsInLevels(graph, src, null);
	}
	
	
	/**
	 * Get the topological order of a directed acyclic graph (DAG)
	 * 
	 * @param graph the DAG you want get topological order
	 * @param comparator  how you want choose vertices when traversing. 
	 * {@code null} indicates natural ordering is used, which is smaller one will be chosen first
	 * this comparator is used when traversal needs to choose from vertices.
	 * @return A list of topological order
	 * @see #topologicalOrder(DirectedGraph)
	 * */
	public static <V, E> List<V> topologicalOrder(DirectedGraph<V, E> graph, Comparator<? super V > comparator){
		if(graph == null){
			throw new NullPointerException("Graph or source vertex is null");
		}
		// need extra space to store marks for traversal, boolean for instance 
		Map<V, Boolean> traversalMarks = new HashMap<V, Boolean>();
		// declare the list to be returned
		LinkedList<V> traversalList = new LinkedList<V>();
		// declare the list to hold potential roots
		List<V> rootList = new LinkedList<V>();
		for(V vertex : graph.getVertices()){
			traversalMarks.put(vertex, false);
			if(graph.inDegree(vertex) == 0){
				rootList.add(vertex);
			}
		}
		Collections.sort(rootList, comparator);
		for(V root : rootList){
			topological_recursive(graph, root, comparator, traversalMarks, traversalList);
		}
		return traversalList;
	} 
	
	/**
	 * Get the topological order of a directed acyclic graph (DAG). Natural ordering is used when traversal needs to choose from vertices.
	 * 
	 * @param graph the DAG you want get topological order
	 * @return A list of topological order
	 * @see #topologicalOrder(DirectedGraph, Comparator)
	 * */
	public static <V, E> List<V> topologicalOrder(DirectedGraph<V, E> graph){
		return topologicalOrder(graph, null);
	} 
	
	private static <V, E> void topological_recursive(Graph<V, E> graph, V root, Comparator<? super V > comparator, Map<V, Boolean> traversalMarks, LinkedList<V> traversalList){
		if(traversalMarks.get(root) == true){
			return;
		}else{
			traversalMarks.put(root, true);
		}
		List<V> adjacentVertices = new LinkedList<V>(graph.adjacentVertices(root));
		if(comparator != null){
			Collections.sort(adjacentVertices, comparator);
		}
		for(V vertex : adjacentVertices){
			if(traversalMarks.get(vertex) == false){
				topological_recursive(graph, vertex, comparator, traversalMarks, traversalList);
			}
		}
		traversalList.addFirst(root);
	}
		
}
