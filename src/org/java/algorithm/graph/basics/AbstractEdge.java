package org.java.algorithm.graph.basics;

public abstract class AbstractEdge {
	
	protected final String id;
	protected boolean visited;

	public AbstractEdge(String id){
		this.id = id;
		this.setVisited(false);
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
