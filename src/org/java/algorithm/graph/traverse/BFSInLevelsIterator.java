package org.java.algorithm.graph.traverse;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import org.java.algorithm.graph.basics.Graph;

public class BFSInLevelsIterator<V, E> extends AbstractGraphIterator<V, E> {

	public BFSInLevelsIterator(Graph<V, E> graph, V src, Comparator<? super V> c) {
		super(graph, src, null, c);
	}
	
	public BFSInLevelsIterator(Graph<V, E> graph, V src) {
		super(graph, src, null, null);
	}

	public boolean hasNext() {
		return !isEmpty();
	}

	public V next() {
		throw new UnsupportedOperationException();
	}
	
	public List<V> nextLevel() {
		if (!hasNext()){
            throw new NoSuchElementException();
		}
		List<V> level = new LinkedList<V>();
		while(!isEmpty()){
			V vertex = pollFirst();
			// visit the node
			setMark(vertex, true);
			List<V> adjacentVertices = new LinkedList<V>(getGraph().adjacentVertices(vertex));
			if(getComparator() != null){
				Collections.sort(adjacentVertices, getComparator());
			}
			for(V adjacentVertex : adjacentVertices){
				if(getMark(adjacentVertex) == false && !contains(adjacentVertex)){
					level.add(adjacentVertex);
				}
			}
		}
		addLevel(level);
		return level;
	}
	
}
