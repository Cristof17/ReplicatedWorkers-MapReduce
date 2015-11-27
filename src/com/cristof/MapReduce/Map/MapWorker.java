package com.cristof.MapReduce.Map;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.StringTokenizer;

import com.cristof.MapReduce.WorkPool;

public class MapWorker extends Thread {
	WorkPool wp;
	int maxSize;


	public static String delimitators = new String(";: /?~\\.,><~`[]{}()!@#$%^&-_+'=*\"\t\r\n");
	
	public MapWorker(WorkPool workpool) {
		this.wp = workpool;
		this.maxSize = 20 ;
	}

	/**
	 * Procesarea unei solutii partiale. Aceasta poate implica generarea unor
	 * noi solutii partiale care se adauga in workpool folosind putWork().
	 * 	Daca s-a ajuns la o solutie finala, aceasta va fi afisata.
	 */
	public void processPartialText(PartialText ps) {

//		 if(wp.getWork() == null){
//		 	return;
//		 	
		if(2 > 4){
		
		
		}else{
			//TODO 
			//de facut ce trebuie sa faca un worker
			
			int numberOfChars =(int) (ps.stop - ps.start + 1); 
			char[] destination_buffer = new char[numberOfChars];
			
			
			try {
				
				File sourceFile = new File(ps.fileName);		
				BufferedReader fileBufferedReader = new BufferedReader(new FileReader(sourceFile));
				fileBufferedReader.mark((int) ps.start);
				fileBufferedReader.read(destination_buffer, (int)ps.start , numberOfChars);
				
				
				//DEBUG
				System.out.println("------INITIAL------");
				System.out.println(destination_buffer);
				System.out.println("------END INITIAL------");
				
			
				String fragmentText = new String(destination_buffer);
				StringBuilder fragmentBuider = new StringBuilder();
				
				
				StringTokenizer st = new StringTokenizer(fragmentText, delimitators); 
				
				//check the beginning of the word 
				if(! delimitators.contains(new Character(destination_buffer[0]).toString()) && ps.start > 0){
					//jump over the first word 
					//eliminate the first word
					st.nextElement();
					
				}
								
				
				if(! delimitators.contains(new Character(destination_buffer[destination_buffer.length -1]).toString())){
					//process the last word as well
					int offset = 0;
					char outputFromOffset[] = {'A'}; //stores the next Byte 
					while(true){
						//read one character, store in the one char buffer and then check if it is a delimiter
						Character nextChar = new Character(outputFromOffset[0]);
						fileBufferedReader.mark(destination_buffer.length + offset - 1);
						fileBufferedReader.read(outputFromOffset);
						if(!delimitators.contains(nextChar.toString())){
							//still in the middle of the word
							//concatenate current byte to the fragment buffer
							StringBuilder sb = new StringBuilder();
							sb.append(destination_buffer);
							sb.append(outputFromOffset);
							destination_buffer = sb.toString().toCharArray();
							offset++;
						}else{
							//it is a delimiter
							break;
						}
					}
					
				}
				//start processing from the second string
		
				
				//DEBUG
				System.out.println("------AFTER LIMITS------");
				System.out.println(destination_buffer);
				System.out.println("------END LIMITS------");
				
				fileBufferedReader.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
		 }
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

	
}