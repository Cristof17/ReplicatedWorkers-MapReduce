package com.cristof.MapReduce.Reduce;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.cristof.MapReduce.Map.PartialText;
import com.cristof.MapReduce.Map.MapWorker.MapResult;

public class ReduceWorker extends Thread{
	
	private ReducePool pool;

	public ReduceWorker(ReducePool pool){
		this.pool = pool;
	}

	@Override
	public void run() {
		
		System.out.println("Thread-ul reduce " + this.getName() + " a pornit...");
		ReduceTask task ;
		while (true) {
			task = pool.getWork();
			if (task == null)
				break;
			MapResult master = combine(task.mapResults);
			float rank = process(master);
			System.out.println("Rank from " + master.filename + " = " + rank);
		}
	}
	
	
	public float process(MapResult mapResult){

		System.out.println("Processing " + mapResult.filename + " " + mapResult.maxLength);;
		
		float rank = 0;
		//find which is the last key to iteratate to 
		Set<Integer> keySet = mapResult.hash.keySet();
		Integer[] keys = new Integer[keySet.size()];
		//keySet.toArray() returns Object[] and I need Integer[]
		for(int j = 0 ; j < keySet.size() ; j++){
			Integer intKey = (Integer)keySet.toArray()[j];
			keys[j] = intKey;
		}
		//get the hass for the MapResult from the combine phase
		HashMap <Integer, Integer> values = mapResult.hash;
		for (int i = 0 ; i < keys.length ; i++){
			Integer key = keys[i];
			Integer value = values.get(key);
			Integer numberOfWords = mapResult.numberOfWords;
			rank += ( Fibonacci(key + 1) * value )/(float)numberOfWords;
		}
		
		return rank;
	}
	
	public int Fibonacci(int n){
	        if (n <= 1) return n;
	        else return Fibonacci(n-1) + Fibonacci(n-2);
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
			Integer[] keys = new Integer[keySet.size()];
			//keySet.toArray() returns Object[] and I need Integer[]
			for(int j = 0 ; j < keySet.size() ; j++){
				Integer intKey = (Integer)(keySet.toArray()[j]);
				keys[j] = intKey;
			}
					
			for(Integer key : keys){
				HashMap<Integer, Integer> hash = aux.hash;
				//iterate and get values into master
				Integer value = hash.get(key);
				//check the master value not to be null for key j
				Integer masterValue = master.hash.get(key);
				if(masterValue == null)
					//inititalize it if null
					masterValue = new Integer(0);
				//add our value to the masterValue
				masterValue += value;
				//put masterValue in the master Hash
				master.hash.put(key, masterValue);
			}
		}

		
		//Combine Lists
		int maxSize = 0;
		int[] whichMapResult = new int[maps.size()]; //position/positions in the MapResults Array that has/have the maxSize
		int currPos = 0;
		for(int i = 0 ; i < maps.size() ; i++){
			MapResult aux = maps.get(i);
			if(aux.maxLength >= maxSize ){
				maxSize = aux.maxLength;
				whichMapResult[currPos] = i;
				++currPos;
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
		
		//select only the unique entries in the maxWords List
		HashSet<String> uniqueValues = new HashSet<>(globalMaxWords);
		globalMaxWords = new ArrayList<>(maxSize);
		for(String uniqueWord : uniqueValues){
			globalMaxWords.add(uniqueWord);
		}
		
		master.maxWords = globalMaxWords;
		master.maxLength = maxSize;
		
		
		//set master numberOfWords
		int masterNumberOfWords = 0;
		
		for(MapResult result : maps){
			masterNumberOfWords += result.numberOfWords;
		}
		master.numberOfWords = masterNumberOfWords;
		
		return master; 
	}
	
	
}
