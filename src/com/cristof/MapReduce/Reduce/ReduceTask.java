package com.cristof.MapReduce.Reduce;

import java.util.ArrayList;

import com.cristof.MapReduce.Map.MapWorker.MapResult;

public class ReduceTask {
	
	public ArrayList<MapResult> mapResults;
	
	public ReduceTask(ArrayList<MapResult> mapResults){
		this.mapResults = mapResults;
	}

}
