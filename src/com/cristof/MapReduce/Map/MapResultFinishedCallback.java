package com.cristof.MapReduce.Map;

import com.cristof.MapReduce.Map.MapWorker.MapResult;

public interface MapResultFinishedCallback {

	public void mapResultReady(MapResult result, int ID);
	
}
