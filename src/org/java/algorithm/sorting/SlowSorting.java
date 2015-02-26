package org.java.algorithm.sorting;

import java.util.Comparator;

public class SlowSorting {
	

	public static <T> void insertionSort(T[] values, Comparator<? super T> c){
		if(values == null || values.length <= 1) return;
		if(c == null){
			c = new Comparator<T>(){
				public int compare(T a, T b){
					return ((Comparable)a).compareTo(b);
				}
			};
		}
		int len = values.length;
		T temp = null;
		for(int i = 1; i < len; i++){
			temp = values[i];
			int j = i;
			// has to be less than (<) to keep this sorting stable
			while(j > 0 && c.compare(temp, values[j - 1]) < 0){
				values[j] = values[--j];
			}
			values[j] = temp;
		}
	}
	
	public static <T> void selectionSort(T[] values, Comparator<? super T> c){
		if(values == null || values.length <= 1) return;
		if(c == null){
			c = new Comparator<T>(){
				public int compare(T a, T b){
					return ((Comparable)a).compareTo(b);
				}
			};
		}
		int len = values.length;
		for(int i = 0; i < len; i++){
			int minIndex = i;
			for(int j = i + 1;  j < len; j++){
				if(c.compare(values[j], values[minIndex]) < 0)
					minIndex = j;
			}
			swap(values, i, minIndex);
		}
	}

	private static <T> void swap(T[] values, int fromIndex, int toIndex){
		if(fromIndex == toIndex) return;
		T temp = values[fromIndex];
		values[fromIndex] = values[toIndex];
		values[toIndex] = temp;
	}
	
	
	/*public static void main(String[] args){
		Integer[] array = new Integer[]{2, 4, 1, 6, 2, 3, 6, 1, 22};
		selectionSort(array, null);
		for(int i : array){
			System.out.println(i);
		}
	}*/

}
