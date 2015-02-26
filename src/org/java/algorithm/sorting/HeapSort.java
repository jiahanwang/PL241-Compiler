package org.java.algorithm.sorting;

public class HeapSort {
	/**
	 * @see {@link http://en.wikipedia.org/wiki/Heapsort#Pseudocode}
	 * 
	 * */
	public static void heapSort(int[] values){
		if(values == null || values.length == 1) return;
		heapify(values);
		int end = values.length - 1;
		while(end > 0){
			swap(values, 0, end);
			end--;
			shiftDown(values, 0, end);
		}
		
	}
	
	public static void heapify(int[] values){
		int len = values.length;
		int start = len / 2 ;
		while( start >= 0 ){
			shiftDown(values, start, len - 1);
			start --;
		}
	}

	private static void shiftDown(int[] values, int start, int end){
		int root = start;
		while( root * 2 + 1 <= end ){
			int child = root * 2 + 1;
			int swap = root;
			if( values[swap] < values[child] )
				swap = child;
			if( child + 1 <= end && values[swap] < values[child + 1])
				swap = child + 1;
			if(swap != root){
				swap(values, root, swap);
				root = swap;
			}else
				return;
		}
	}

	private static void swap(int[] values, int left, int right){
		int temp = values[left];
		values[left] = values[right];
		values[right] = temp;
	}
	
	public static void main(String[] args){
		int[] a = new int[]{ 3, 5, 2, 7, 1, 2, 0, 4};
		heapify(a);
		for(int i = 0; i < a.length; i++){
			System.out.println(a[i]);
		}
		
		heapSort(a);
		for(int i = 0; i < a.length; i++){
			System.out.println(a[i]);
		}
	}
	
}
