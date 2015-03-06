package org.java.algorithm.graph.basics;

public class Pair<T>{
	
	private T source;
	private T destination;
	
	public Pair(T source, T destination) {
    	if(source == null || destination == null) 
    		throw new IllegalArgumentException("Pair cannot contain null values");
        setSource(source);
        setDestination(destination);
    }
	
	public boolean contains(T t) {
		if(source == t || destination == t || source.equals(t)|| destination.equals(t)){
			return true;
		}else{
			return false;
		}
	}
	
	public T getSource() {
		return source;
	}

	public void setSource(T source) {
		this.source = source;
	}

	public T getDestination() {
		return destination;
	}

	public void setDestination(T destination) {
		this.destination = destination;
	}

}