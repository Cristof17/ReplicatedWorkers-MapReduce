package com.cristof.MapReduce.Reduce;

import com.cristof.MapReduce.Map.MapWorker.MapResult;

public class ReduceResult implements Comparable<ReduceResult> {
	
	public MapResult master;
	public int rank;

	
	public ReduceResult(MapResult master , int rank){
		
		this.master = master ;
		this.rank = rank;
	}


	@Override
	public int compareTo(ReduceResult aux) {
		if(aux.rank == this.rank)
			return 0;
		else 
			if(this.rank < aux.rank)
				return -1;
			else
				return 1;
	}

}
