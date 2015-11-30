package com.cristof.MapReduce.Map;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

import javax.print.attribute.standard.Chromaticity;

import com.cristof.MapReduce.WorkPool;

public class MapWorker extends Thread implements ProcessWordInterface{
	
	public WorkPool wp;
	public int maxSize;
	public MapResult result;
	public MapResultFinishedCallback callback;
	public int ID;


	public static String delimitators = new String(";: /?~\\.,><~`[]{}()!@#$%^&-_+'=*\"\t\r\n");
	
	public MapWorker(WorkPool workpool,
			MapResultFinishedCallback callback, int ID) {
		this.wp = workpool;
		this.maxSize = 0 ;
		this.callback = callback;
		this.ID = ID; // worker ID 
	}
	
	
	public void run() {
		
		System.out.println("Thread-ul worker " + this.getName() + " a pornit...");
		PartialText ps ;
		while (true) {
			ps = wp.getWork();
			if (ps == null){
				boolean ready = wp.ready;
				break;
			}
			processPartialText(ps);
			System.out.println("Thread-ul worker " + this.getName() + " a executat partea de la " + ps.start + "-" + ps.stop);
			
			this.maxSize = 0;
			this.result = null;
		}
	}

	/**
	 * Procesarea unei solutii partiale. Aceasta poate implica generarea unor
	 * noi solutii partiale care se adauga in workpool folosind putWork().
	 * 	Daca s-a ajuns la o solutie finala, aceasta va fi afisata.
	 */
	public synchronized void processPartialText(PartialText ps) {
			
			int numberOfChars =(int) (ps.stop - ps.start + 1); 
			byte[] destination_buffer =  new byte[10000 ];
			byte caracter;
			char debugChar;
			
			try {
				RandomAccessFile raf = new  RandomAccessFile(new File(ps.fileName),"r");
				int charRead = 0;
				StringBuilder word = new StringBuilder();
				//for checking the letter before the first letter
				if(ps.start == 0)
					raf.seek(ps.start);
				else
					raf.seek(ps.start);
				
				/*
				 * BEGINING
				 */
				if(ps.start >0){
					raf.seek(ps.start - 1); //check the previous value
					caracter = raf.readByte();
					//if half of the word, delete it
					if(delimitators.contains(new Character((char)caracter).toString())){
						raf.seek(ps.start);
						caracter = raf.readByte();
					}
					else{ //it's not ok (middle of the word)
						while(!delimitators.contains(new Character((char)caracter).toString())){
							caracter = raf.readByte(); //read blindly until the first delimitator is met
							debugChar = (char)caracter;
							++charRead;
						}
					}
				}
				//read the fragment
				while(charRead <= numberOfChars){

					//read a word
					while(true){
						++charRead;
						caracter = raf.readByte();
						debugChar = (char)caracter;
						if(delimitators.contains(new Character((char)caracter).toString())){
							break;
						}else{
							word.append((char)caracter);
						}
					}
					
									
					if(word.toString().length() != 0){
						//TODO
						//Remove if not for debug

						
							System.out.print(word.toString());
//							System.out.println();
					}
					
					processWord(word.toString(),ps.fileName); //local Method
					word = new StringBuilder();
				}
			} catch (IOException e){}
			callback.mapResultReady(result, this.ID, ps.fragmentID);
		 }
	
	@Override
	public synchronized void processWord(String word, String filename) {
		if(result == null)
			result = new MapResult(filename);
		
		result.putWord(word); //local Method
		result.numberOfWords = result.numberOfWords+1 ;
		
	}
	
	
	
	public static class MapResult{
		
		public HashMap<Integer,Integer> hash ;
		public String filename;
		public ArrayList<String> maxWords;
		public int maxLength;
		public int numberOfWords;
		
		public MapResult(String filename){
			this.filename = filename;
			this.numberOfWords = 0;
		}
		
		//count the word to the hash and check if it is of size maxSize
		public synchronized void putWord(String word){
			
			//TODO
			//Remove if not for debug
			
			if(word.toString() == "unqualified"){
				int a = 2;
				int v = 23;
				System.out.println("Reached unqualified word");
			}
			
			if(hash == null && maxWords == null){
				hash = new HashMap<Integer,Integer>();
				maxWords = new ArrayList<String>();
			}
			
			Integer previous = hash.get(word.length());
			if(previous == null || previous == 0){
				previous = new Integer(0);
				hash.put(word.length(), new Integer(0));
			}
			
			hash.put(word.length(), ++previous);
			
			
			//reset the maxSize and the list of MaxSize words
			if(word.length() > maxLength){
				if(word.toString().equals("characteristics")){
				
				//TODO
				//Remove if not for debug
					int a = 2;
					int b = 20;
					System.out.println("Word is characteristics");
				}	
				maxLength = word.length();
				maxWords = new ArrayList<>();
				maxWords.add(word);
			}else if(word.length() == maxLength){
				maxWords.add(word);
			}
			
			System.out.println("Word size for " + word.toString() + " is " + word.toString().length());
		}
	}
}