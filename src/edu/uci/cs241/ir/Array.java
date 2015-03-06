package edu.uci.cs241.ir;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by hanplusplus on 2/24/15.
 */
public class Array {

    public String name;

    public List<Integer> dimensions;

    public Array (String name){
        this.name = name;
        this.dimensions = new ArrayList<Integer>();
    }

    /*public boolean addDimension(int which, int value){
        if(dimensions.get(which - 1) == null){
            dimensions.add(which - 1, value);
            return true;
        }else {
            return false;
        }
    }*/

    // add dimensions can be only calles once
    public boolean addDimensions(List<Integer> dimensions){
        if(this.dimensions.size() == 0){
            this.dimensions.addAll(dimensions);
            return true;
        }else{
            return false;
        }
    }
}
