

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main  {

	static int documentID;
	static int numberOfThreads ;
	static String inputFilePath;
	static String outputFilePath;
	static MapResultFinishedCallback mapResultCallback;
	static ReduceResultFinishedCallback reduceResultCallback;
	static MapWorker[] mapWorkers;
	static ReduceWorker[] reduceWorkers;
	static WorkPool mapPool;
	static ReducePool reducePool;
	static HashMap<Integer, List<MapResult>> mapResults;
	static HashMap<Integer,Integer> ranks;
	static ReduceResult[] reduceResults;
	static ArrayList<String> inputFiles;
	
	public static void main(String[] args){
	
		if(args.length < 3){
			System.out.println("Not enough arguments");
			return;
		}
		
		numberOfThreads = Integer.parseInt(args[0]);
				
		inputFilePath = args[1];
		outputFilePath = args[2];
		mapWorkers = new MapWorker[numberOfThreads];
		reduceWorkers = new ReduceWorker[numberOfThreads];
		mapResults = new HashMap<Integer, List<MapResult>>();
		ranks = new HashMap<Integer,Integer>();
		
		
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
						
						if(result != null){
							ArrayList<MapResult> resultsForID = (ArrayList<MapResult>)mapResults.get(fragmentID);
							if(resultsForID == null){
								ArrayList<MapResult> value = new ArrayList<MapResult>();
								value.add(result);
								mapResults.put(fragmentID, value);
							}else{
								resultsForID.add(result);
								mapResults.put(fragmentID, resultsForID);
							}
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
				String fileName = sc.next();
				inputFiles = new ArrayList<String>();
				inputFiles.add(fileName);
				Document firstDocument = new Document(fileName);
				
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

			
			for(int i = 0 ; i < numberOfThreads ; i++){
				mapWorkers[i].join();
			}
			
			for(int i = 0 ; i < numberOfDocuments ; i++){
				ArrayList<MapResult> results =(ArrayList<MapResult>) mapResults.get(i);
				ReduceTask newTask = new ReduceTask(results);
				reducePool.putWork(newTask);
			}
			
			reduceResults = new ReduceResult[numberOfDocuments];
			synchronized (reduceResults) {
				reduceResultCallback = new ReduceResultFinishedCallback() {
					
					@Override
					public void reduceResultReady(ReduceResult result) {
						int position = result.master.fragmentID;
						reduceResults[position] = result;
					}
				};				
			}
			
			
			for(int i = 0 ; i < numberOfThreads ; i++){
				reduceWorkers[i] = new ReduceWorker(reducePool,outputFilePath,reduceResultCallback);
			}
			
			for(int i = 0 ; i < numberOfThreads ; i++){
				reduceWorkers[i].start();
			}
			
			for(int i = 0 ; i < numberOfThreads ; i++){
				reduceWorkers[i].join();
			}
			

			
			Arrays.sort(reduceResults,Collections.reverseOrder());
						
			reduceResults = checkOrder(reduceResults, inputFiles);
			
			for(int i = 0 ; i < reduceResults.length ; i++){
				System.out.println(reduceResults[i].master.filename + " " + reduceResults[i].rank);
			}
			
			writeInFile(outputFilePath, reduceResults);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	
	private static ReduceResult[] checkOrder(ReduceResult[] original , ArrayList<String> input){
		
		ArrayList<ReduceResult> returnList = new ArrayList<ReduceResult>(original.length);
		boolean flag = true;
		
		  while ( flag ){
		            flag= false;    //set flag to false awaiting a possible swap
		            for(int j=0;  j < original.length -1;  j++ ){
		            	int posA = input.indexOf(original[j].master.filename);
		            	int posB = input.indexOf(original[j+1].master.filename);
		                   if (original[j].rank == original[j+1].rank
		                		   && (posB < posA)){
		                           ReduceResult aux = original[ j ];
		                           original[ j ] = original[ j+1 ];
		                           original[ j+1 ] = aux;
		                           flag = true; 
		                  }
		            }
		   } 
		
		for(int i = 0; i < original.length ; i++){
			returnList.add(original[i]);
		}
		
		return original;
	}
	
	private static void writeInFile(String filename, ReduceResult[] results){
		if(results == null)
			return;
		try
		{
			for(int i = 0; i < results.length ; i++){
				ReduceResult aux = results[i];
				String rankString = new String ("" + aux.rank);
				int length = rankString.length();
				rankString = rankString.substring(0, length-2) + "." + rankString.substring(length - 2, length);
			    FileWriter fw = new FileWriter(filename,true); //the true will append the new data
			    fw.write(aux.master.filename +";"
			    + rankString
			    +";["
			    +aux.master.maxLength
			    +","
			    +aux.master.maxWords.size()+
			    "]"
			    + System.getProperty("line.separator"));
			    fw.close();
			}
		}
		catch(IOException ioe)
		{
		    System.err.println("IOException: " + ioe.getMessage());
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
