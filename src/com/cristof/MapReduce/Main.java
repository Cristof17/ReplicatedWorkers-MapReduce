package com.cristof.MapReduce;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

import com.cristof.MapReduce.Map.MapWorker;
import com.cristof.MapReduce.Map.PartialText;

public class Main {

	static int numberOfThreads ;
	static String inputFilePath;
	static String outputFilePath;
	
	
	public static void main(String[] args){
	
		if(args.length < 3){
			System.out.println("Not enough arguments");
			return;
		}
		
		numberOfThreads = Integer.parseInt(args[0]);
		inputFilePath = args[1];
		outputFilePath = args[2];
		
		/*
		 * Read from file the fragmentSize and number of Files + filenames and sizes
		 */
		
		File inFile = new File(inputFilePath);
		try {
			
			Scanner sc = new Scanner(inFile);
			int fragmentSize = sc.nextInt();
			System.out.println("Fragment size is " + fragmentSize);
			
			int numberOfDocuments = sc.nextInt();
			System.out.println("Number of documents is "+ numberOfDocuments);
			
			Document firstDocument = new Document(sc.next());
			ArrayList<PartialText> fragments = firstDocument.splitFile(fragmentSize);
			
			MapWorker worker = new MapWorker(new WorkPool(4));
			for(PartialText fragment : fragments){
				worker.processPartialText(fragment);
			}
			
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
		WorkPool workPool  = new WorkPool(4);

	}
	
	
	private static class Document{
		
		private String name ;
		private int fragmentSize;
		
		public Document(String name){
			this.name = name ;
			fragmentSize = 0;
		}
		
		public ArrayList<PartialText> splitFile(int fragmentSize){ 
			
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
				PartialText fragment = new PartialText(name , lastPosition, lastPosition + fragmentSize - 1);
				lastPosition += fragmentSize;
				fragments.add(fragment);
			}
			
			PartialText lastFragment = new PartialText(name , lastPosition , fileSize - 1);
			fragments.add(lastFragment);
			
			
			return fragments;
			
		}
		
	}
	

	
}
