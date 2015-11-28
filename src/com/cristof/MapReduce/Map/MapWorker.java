package com.cristof.MapReduce.Map;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

import com.cristof.MapReduce.WorkPool;

public class MapWorker extends Thread implements ProcessWordInterface{
	
	public WorkPool wp;
	public int maxSize;
	public MapResult result;
	public MapResultFinishedCallback callback;


	public static String delimitators = new String(";: /?~\\.,><~`[]{}()!@#$%^&-_+'=*\"\t\r\n");
	
	public MapWorker(WorkPool workpool,
			MapResultFinishedCallback callback) {
		this.wp = workpool;
		this.maxSize = 20 ;
		this.callback = callback;
	}

	/**
	 * Procesarea unei solutii partiale. Aceasta poate implica generarea unor
	 * noi solutii partiale care se adauga in workpool folosind putWork().
	 * 	Daca s-a ajuns la o solutie finala, aceasta va fi afisata.
	 */
	public void processPartialText(PartialText ps) {
			
			int numberOfChars =(int) (ps.stop - ps.start + 1); 
			byte[] destination_buffer =  new byte[10000 ];
			try {
				RandomAccessFile raf = new  RandomAccessFile(new File(ps.fileName),"r");
				int charRead = 0;
				StringBuilder word = new StringBuilder();
				//for checking the letter before the first letter
				if(ps.start == 0)
					raf.seek(ps.start);
				else
					raf.seek(ps.start-1);
				//read the fragment
				while(charRead <= numberOfChars){
					//read a word
					while(true){
						byte caracter = raf.readByte();
						char caraterDebug = (char)caracter;
						//check special situations
						/*
						 * BEGINING
						 */
						//check the current character
						if(ps.start > 0 &&
							! delimitators.contains(new Character((char)caracter).toString())){
							//landed in the middle of a word
							//get the rest of partial word
							while(! delimitators.contains(new Character((char)caracter).toString())){
								caracter = raf.readByte();
								caraterDebug = (char)caracter;
								charRead++;
							}
							//go to the next word
							break;
						}
						//check the previous character
						long previousFilePointer = raf.getFilePointer();
						//if is not the beginning of the word
						if(ps.start > 0)
							raf.seek(raf.getFilePointer() - 1);//one char behind
						//check the previous caracter 
						if(ps.start > 0 &&
								! delimitators.contains(new Character((char)caracter).toString())){
								//landed in the middle of a word
								
								//get the rest of partial word
								while(! delimitators.contains(new Character((char)caracter).toString())){
									caracter = raf.readByte();
									charRead ++ ;
								}
								//go to the next word
								break;
							}				
						if(delimitators.contains(new Character((char)caracter).toString())){
							break;
						}else{
							word.append((char)caracter);
							charRead ++;
						}
					}
					if(word.toString()!= ""){
						System.out.println(word.toString());
					}
					processWord(word.toString(),ps.fileName);
					word = new StringBuilder();
				}
			} catch (IOException e){}
			callback.mapResultReady(result);
		 }

	public void run() {
		System.out.println("Thread-ul worker " + this.getName() + " a pornit...");
		PartialText ps ;
		while (true) {

			ps = wp.getWork();

			if (ps == null)
				break;
			
			processPartialText(ps);
		}
		System.out.println("Thread-ul worker " + this.getName() + " a executat partea de la " + ps.start + "-" + ps.stop);
	}
	
	public static class MapResult{
		
		private HashMap<Integer,Integer> hash ;
		private String filename;
		private ArrayList<String> maxWords;
		private int maxLength;
		
		public MapResult(String filename){
			this.filename = filename;
		}
		
		//count the word to the hash and check if it is of size maxSize
		public void putWord(String word){
			
			if(hash == null && maxWords == null){
				hash = new HashMap<Integer,Integer>();
				maxWords = new ArrayList<String>();
			}
			
			Integer previous = hash.get(word.length());
			if(previous == null || previous == 0){
				previous = new Integer(0);
				hash.put(word.length(), new Integer(0));
			}
			
			hash.put(word.length(),previous++);
			
			
			//reset the maxSize and the list of MaxSize words
			if(word.length() > maxLength){
				maxLength = word.length();
				maxWords = new ArrayList<>();
			}
			
			if(word.length() == maxLength){
				maxWords.add(word);
			}
		}
	}

	@Override
	public void processWord(String word, String filename) {
		if(result == null)
			result = new MapResult(filename);
		
		result.putWord(word);
		
	}
	
}