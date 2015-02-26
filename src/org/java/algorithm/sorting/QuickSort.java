package org.java.algorithm.sorting;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;

public class QuickSort {

	public static <T> Collection<T> quickSort(Collection<T> values){
		return quickSort(values, 0, values.size() - 1, null);
	}
	
	public static <T> Collection<T> quickSort(Collection<T> values, Comparator<? super T> c){
		return quickSort(values, 0, values.size() - 1, c);
	}
	
	public static <T> Collection<T> quickSort(Collection<T> values, int fromIndex, int toIndex){
		return quickSort(values, fromIndex, toIndex, null);
	}
	
	
	/**
	 * All quickSort of Collection call this method.
	 * This method convert collection to array and then call call quickSort of array. 
	 * It also checks the eligibility of all parameters.
	 * 
	 * */
	@SuppressWarnings("unchecked")
	public static <T> Collection<T> quickSort(Collection<T> values, int fromIndex, int toIndex, Comparator<? super T> c){
		if(values.size() <= 1)
			return values;
		if(fromIndex > toIndex || fromIndex < 0 || toIndex > values.size() - 1){
			throw new IllegalArgumentException("fromIndex or toIndex is wrong");
		}
		if(c == null){
			c = new Comparator<T>(){
				public int compare(T a, T b){
					return ((Comparable)a).compareTo(b);
				}
			};
		}
		T[] valuesArray = (T[])values.toArray();
		quickSortInternal(valuesArray, fromIndex, toIndex, c);
		return Arrays.asList(valuesArray);
	}
	
	
	public static <T> void quickSort(T[] values){
		quickSort(values, 0, values.length - 1, null);
	}
	
	public static <T> void quickSort(T[] values, Comparator<? super T> c){
		quickSort(values, 0, values.length - 1, c);
	}
	
	public static <T> void quickSort(T[] values, int fromIndex, int toIndex){
		quickSort(values, fromIndex, toIndex, null);
	}
	
	/**
	 * All quickSort of array call this method eventually
	 * This method serves the wrapper method of the recursive quickSort to check all the eligibility of all parameters
	 * 
	 * */
	public static <T> void quickSort(T[] values, int fromIndex, int toIndex, Comparator<? super T> c){
		if(values.length <= 1) return;
		if(fromIndex > toIndex || fromIndex < 0 || toIndex > values.length - 1){
			throw new IllegalArgumentException("fromIndex or toIndex is wrong");
		}
		if(c == null){
			c = new Comparator<T>(){
				public int compare(T a, T b){
					return ((Comparable)a).compareTo(b);
				}
			};
		}
		quickSortInternal(values, fromIndex, toIndex, c);
	}
	
	/**
	 * The internal recursive quick sort method
	 * 
	 * */
	private static <T> void quickSortInternal(T[] values, int fromIndex, int toIndex, Comparator<? super T> c){
		if(!(fromIndex < toIndex)) return;
		// choose a pivot from fromIndex to toIndex
		int pivotIndex = median3(values, fromIndex, toIndex, c);
		// get the new position of the pivot
		int pivotNewIndex = partition(values, fromIndex, toIndex, pivotIndex, c);
		// sort two halves
		quickSortInternal(values, fromIndex, pivotNewIndex - 1, c);
		quickSortInternal(values, pivotNewIndex + 1, toIndex, c);
	}
	
	/**
	 * Use <a href="http://www.cs.fsu.edu/~breno/COP-4530/slides/21-anim.pdf">Three median</a> to choose the pivot. During choosing, the elements will be moved and the pivot will be always put
	 * in the middle. The method still return the middle position of the input array.  
	 * 
	 * */
	private static <T> int median3(T[] values, int fromIndex, int toIndex, Comparator<T> c){
		int centerIndex =  fromIndex + (toIndex - fromIndex) / 2;
		if(c.compare(values[fromIndex], values[centerIndex]) > 0 )
			swap(values, fromIndex, centerIndex);
		if(c.compare(values[fromIndex], values[toIndex]) > 0 )
			swap(values, fromIndex, toIndex);
		if(c.compare(values[centerIndex], values[toIndex]) > 0 )
			swap(values, centerIndex, toIndex);
		// always put the pivot in the center
		return centerIndex;
	}
	
	/**
	 * Use <a href="http://en.wikipedia.org/wiki/Quicksort#In-place_version">In place</a> partition method
	 * 
	 * */
	private static <T> int partition(T[] values, int fromIndex, int toIndex, int pivotIndex, Comparator<T> c){
		T pivotValue = values[pivotIndex];
		swap(values, pivotIndex, toIndex);
		int i = fromIndex, storeIndex = fromIndex;
		for(; i <= toIndex - 1; i++){
			if(c.compare(values[i], pivotValue) <= 0){
				swap(values, i, storeIndex);
				storeIndex ++;
			}
		}
		swap(values, toIndex, storeIndex);
		return storeIndex;
	}
	
	private static <T> void swap(T[] values, int fromIndex, int toIndex){
		T temp = values[fromIndex];
		values[fromIndex] = values[toIndex];
		values[toIndex] = temp;
	}

}
