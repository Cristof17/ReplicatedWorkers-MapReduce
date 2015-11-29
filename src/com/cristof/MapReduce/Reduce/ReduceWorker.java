package com.cristof.MapReduce.Reduce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import com.cristof.MapReduce.Map.MapWorker.MapResult;

public class ReduceWorker extends Thread{
	
	private ArrayList<MapResult> mapResults;
	private int documentID;

	
	
	public ReduceWorker(ArrayList<MapResult> mapResults, int documentID){
		this.mapResults = mapResults;
		this.documentID = documentID;
	}

	@Override
	public void run() {
		
	}
	
	
	public void processMapResult(MapResult mapResult){
		
	}
	
	public MapResult combine(ArrayList<MapResult> maps){
		//we bring all of them to the first MapResult
		//which will be the master
		MapResult master = maps.get(0);
		
		//Combine Hashes
		for(int i = 1 ; i < maps.size() ; i++){
			MapResult aux = maps.get(i);
			//I need to know the last key from aux to bring everything from valid keys in master
			//List contained up to lastKey in the master (to do an iteration)
			Set<Integer> keySet = aux.hash.keySet();
			int size = ((Integer[]) keySet.toArray()).length;
			//the last value from the key set needs to be the biggest Integer key
			//thus being the last one
			Integer lastKey = ((Integer[])keySet.toArray())[size -1 ];
					
			for(int j = 0 ; j < lastKey ; j++){
				HashMap<Integer, Integer> hash = aux.hash;
				//iterate and get values into master
				Integer value = hash.get(j);
				//check the master value not to be null for key j
				Integer masterValue = master.hash.get(j);
				if(masterValue == null)
					//inititalize it if null
					masterValue = new Integer(0);
				//add our value to the masterValue
				masterValue += value;
				//put masterValue in the master Hash
				master.hash.put(j, masterValue);
			}
		}
		//remove master and add it again to make sure changes 
		//are persistent
		maps.remove(0);
		maps.add(0,master);
		
		
		
		//Combine Lists
		int maxSize = 0;
		int[] whichMapResult = new int[maps.size()]; //position/positions in the MapResults Array that has/have the maxSize
		for(int i = 0 ; i < maps.size() ; i++){
			MapResult aux = maps.get(i);
			if(aux.maxLength > maxSize ){
				maxSize = aux.maxLength;
				whichMapResult[whichMapResult.length] = i;
			}
		}
		
		//combine the maxLength lists
		
		//create a new array because it is irrelevant if the 
		//master has maxLength the same as the one found by us
		//in the block of code above
		//we will replace the list of max words from the master
		ArrayList<String> globalMaxWords = new ArrayList<>();
		for(int i = 0 ; i < whichMapResult.length ; i++){
			MapResult aux = maps.get(whichMapResult[i]);
			ArrayList<String> auxMaxWords = aux.maxWords;
			for(String word : auxMaxWords){
				globalMaxWords.add(word);
			}
		}
		
		master.maxWords = globalMaxWords;
		master.maxLength = maxSize;
		
		return master; 
	}
	
	
}
