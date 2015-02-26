package org.java.algorithm.graph.basics;

public abstract class AbstractVertex {
	
	protected final String id;
	protected boolean visited;
	
	AbstractVertex(String id){
		this.id = id;
		this.visited = false;
	}

	public String getId() {
		return id;
	}
	
	public boolean isVisited() {
		return visited;
	}

	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
}
