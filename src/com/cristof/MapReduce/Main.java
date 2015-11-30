package com.cristof.MapReduce;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import javax.sql.PooledConnection;

import com.cristof.MapReduce.Map.MapResultFinishedCallback;
import com.cristof.MapReduce.Map.MapWorker;
import com.cristof.MapReduce.Map.MapWorker.MapResult;
import com.cristof.MapReduce.Map.PartialText;
import com.cristof.MapReduce.Reduce.ReducePool;
import com.cristof.MapReduce.Reduce.ReduceTask;
import com.cristof.MapReduce.Reduce.ReduceWorker;

public class Main  {

	static int documentID;
	static int numberOfThreads ;
	static String inputFilePath;
	static String outputFilePath;
	static MapResultFinishedCallback mapResultCallback;
	static MapWorker[] mapWorkers;
	static ReduceWorker[] reduceWorkers;
	static WorkPool mapPool;
	static ReducePool reducePool;
	static HashMap<Integer, List<MapResult>> mapResults;
	
	public static void main(String[] args){
	
		if(args.length < 3){
			System.out.println("Not enough arguments");
			return;
		}
		
		numberOfThreads = Integer.parseInt(args[0]);
		
		/*
		 * TODO 
		 * Delete if not for debug
		 */
		numberOfThreads = 1;
		
		inputFilePath = args[1];
		outputFilePath = args[2];
		mapWorkers = new MapWorker[numberOfThreads];
		reduceWorkers = new ReduceWorker[numberOfThreads];
		mapResults = new HashMap<Integer, List<MapResult>>();
		
		/*
		 * Read from file the fragmentSize and number of Files + filenames and sizes
		 */
		
		
		
		File inFile = new File(inputFilePath);
		try {
			
			Scanner sc = new Scanner(inFile);
			int fragmentSize = sc.nextInt();
			System.out.println("Fragment size is " + fragmentSize);
			
			final int numberOfDocuments = sc.nextInt();
			System.out.println("Number of documents is "+ numberOfDocuments);
			
			
			//this object is responsible with adding the 
			//result from MapWorker to the list of Results
			mapResultCallback = new MapResultFinishedCallback() {
				
				@Override
				public void mapResultReady(MapResult result , int workerID , int fragmentID) {
					//Put in a hasTable the results
					synchronized (mapResults) {
						
						ArrayList<MapResult> resultsForID = (ArrayList<MapResult>)mapResults.get(fragmentID);
						if(resultsForID == null){
							ArrayList<MapResult> value = new ArrayList<MapWorker.MapResult>();
							value.add(result);
							mapResults.put(fragmentID, value);
						}else{
							resultsForID.add(result);
							mapResults.put(fragmentID, resultsForID);
						}
					}
				}
			};
			

			/*
			 * I instantiate here because the mapPoolCallback will be null if I put it above
			 */
			mapPool = new WorkPool(numberOfThreads); 
			reducePool = new ReducePool(numberOfThreads);
			
			//create the mapWorkers
			for(int i = 0 ; i < numberOfThreads ; i++){
				//documentID for each Worker is 0
				mapWorkers[i] = new MapWorker(mapPool, mapResultCallback, i);
			}
			
			//open the documents and split the work
			while(sc.hasNext()){
				Document firstDocument = new Document(sc.next());
				
				//split into fragments
				ArrayList<PartialText> fragments = firstDocument.splitFile(fragmentSize,documentID);
				for(int i = 0 ; i < fragments.size() ; i++){
					PartialText fragment = fragments.get(i);
					mapPool.putWork(fragment);				
				}
				
				documentID++; //process the next document
				
			}
			
			for(int i = 0 ; i < numberOfThreads ; i++){
				mapWorkers[i].start();
			}
				
			while(!mapPool.ready);
			
			for(int i = 0 ; i < numberOfDocuments ; i++){
				ArrayList<MapResult> results =(ArrayList<MapResult>) mapResults.get(i);
				ReduceTask newTask = new ReduceTask(results);
				reducePool.putWork(newTask);
			}
			
			for(int i = 0 ; i < numberOfThreads ; i++){
				reduceWorkers[i] = new ReduceWorker(reducePool);
			}
			
			for(int i = 0 ; i < numberOfThreads ; i++){
				reduceWorkers[i].start();
			}
			
			for(int i = 0 ; i < numberOfThreads ; i++){
				reduceWorkers[i].join();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
		
	private static class Document{
		
		private String name ;
		private int fragmentSize;
		
		public Document(String name){
			this.name = name ;
			fragmentSize = 0;
		}
		
		public ArrayList<PartialText> splitFile(int fragmentSize , int IDtoHave){ 
			
			/*
			 * DELETE THIS WHEN COMPLETED DEBUGGING
			 */
			String folderPath = "/home/cristof/Downloads/Tema2APD/Test-Debug/";
			name = folderPath + name;
			
			File file = new File(name);
			long fileSize = file.length();
			/*
			 * Don't care if it is int or float
			 */
			int numberOfFragments = (int) fileSize / fragmentSize;
			
			ArrayList<PartialText> fragments = new ArrayList<PartialText>(numberOfFragments);
			long lastPosition = 0 ;
			
			for(int i = 0 ; i < numberOfFragments ; i++){
				PartialText fragment = new PartialText(name , lastPosition, lastPosition + fragmentSize - 1 , IDtoHave);
				lastPosition += fragmentSize;
				fragments.add(fragment);
			}
			
			PartialText lastFragment = new PartialText(name , lastPosition , fileSize - 1, IDtoHave);
			fragments.add(lastFragment);
			
			
			return fragments;
			
		}
		
	}


	
}
