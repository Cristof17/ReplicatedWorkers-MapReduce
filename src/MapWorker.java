

import java.beans.DesignMode;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;

import javax.print.attribute.standard.Chromaticity;

public class MapWorker extends Thread{
	
	public WorkPool wp;
	public int maxSize;
	public MapResult result;
	public MapResultFinishedCallback callback;
	public int ID;


	public static String delimitators = new String("[;:/?~\\\\.,><~`\\[\\]{}()!@#$%\\^&\\-_+'=*\"| 	\r\n]+");
//	public static String delimitators = new String(";: /?~\\.,><~`[]{}()!@#$%^&-_+'=*\"\t\r\n");
	
	public MapWorker(WorkPool workpool,
			MapResultFinishedCallback callback, int ID) {
		this.wp = workpool;
		this.maxSize = 0 ;
		this.callback = callback;
		this.ID = ID; // worker ID 
	}
	
	
	public void run() {
		
		PartialText ps ;
		while (true) {
			ps = wp.getWork();
			if (ps == null){
				boolean ready = wp.ready;
				break;
			}
			
			processPartialText(ps);
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
			byte[] destination_buffer;
			byte caracter;
			char debugChar;
			
			//TODO
			//Just for debug
			try {
			RandomAccessFile raf = new  RandomAccessFile(new File(ps.fileName),"r");
			destination_buffer = new byte[2 * numberOfChars];
				
			int offsetBegin = 0;	
			int offsetEnd=0;
			int charRead = 0;
			//for checking the letter before the first letter
			if(ps.start == 0)
				raf.seek(ps.start);
			else
				raf.seek(ps.start);
		
			/*
			 * BEGINING
			 */
			if(ps.start >0){
				
				//check the current character to be a delimiter
				caracter = raf.readByte();
				debugChar = (char)caracter;
				if(delimitators.contains(new Character((char)caracter).toString())){}
				else{
					raf.seek(ps.start - 1); //check the previous value
					caracter = raf.readByte();
					//if half of the word, delete it
					if(delimitators.contains(new Character((char)caracter).toString())){
						raf.seek(ps.start);
					}else{
						//it's not ok (middle of the word)
						while(!delimitators.contains(new Character((char)caracter).toString())){
							caracter = raf.readByte(); //read blindly until the first delimitator is met
							debugChar = (char)caracter;
							++offsetBegin;
						}
					}
				}
			}
			
			raf.seek(ps.stop);
			caracter = raf.readByte();
			while(!delimitators.contains(new Character((char)caracter).toString())){
					debugChar = (char)caracter;
					offsetEnd ++;
					caracter = raf.readByte();
			}
			try{
							
			int numberOfBytes;
			int currentPosition = 0; //beginning of the buffer
			if(ps.stop + offsetEnd + 1 > raf.length()){
				numberOfBytes = (int)(raf.length() - ps.start - offsetBegin + 1);
				raf.seek(ps.start + offsetBegin);
				raf.read(destination_buffer, currentPosition , numberOfBytes);
			}else
				numberOfBytes =(int)( ps.stop + offsetEnd - ps.start - offsetBegin + 1 );
				raf.seek(ps.start + offsetBegin);
				raf.read(destination_buffer, currentPosition , numberOfBytes );
				
			}catch (IndexOutOfBoundsException e){
				System.out.println("Start = " + ps.start
						+ " beginOffset ="+ offsetBegin
						+ " Stop = "+ps.stop
						+ " endOffset =" +offsetEnd
						+ " Current = " + raf.getFilePointer()
						+ " End = " + raf.length());
			}
			String[] pieces = new String(destination_buffer).split(delimitators);
		
			
			for(int i = 0 ; i < pieces.length ; i ++){
				processWord(pieces[i],ps.fileName, ps.fragmentID); //local Method
				System.out.println(pieces[i] + " ");
			}
			
			raf.close();
			
//				int charRead = 0;
////				StringBuilder word = new StringBuilder();
//				//for checking the letter before the first letter
//				if(ps.start == 0)
//					raf.seek(ps.start);
//				else
//					raf.seek(ps.start);
//			
//				/*
//				 * BEGINING
//				 */
//				if(ps.start >0){
//					
//					//check the current character to be a delimiter
//					caracter = raf.readByte();
//					debugChar = (char)caracter;
//					if(delimitators.contains(new Character((char)caracter).toString())){}
//					else{
//						raf.seek(ps.start - 1); //check the previous value
//						caracter = raf.readByte();
//						//if half of the word, delete it
//						if(delimitators.contains(new Character((char)caracter).toString())){
//							raf.seek(ps.start);
//						}else{
//							//it's not ok (middle of the word)
//							while(!delimitators.contains(new Character((char)caracter).toString())){
//								caracter = raf.readByte(); //read blindly until the first delimitator is met
//								debugChar = (char)caracter;
//								++charRead;
//							}
//						}
//					}
//				}
//				
//				//read after the middle word (numberOfChars - charRead) means the remaining characters
//				charRead = raf.read(destination_buffer, (int)ps.start + charRead,(int)(ps.stop - ps.start)  +1);
////				word.append(destination_buffer.toString());
//				caracter =  destination_buffer[charRead -1];
//				debugChar = (char) caracter;
//				while(!delimitators.contains(new Character((char)caracter).toString())){
//					caracter = raf.readByte();
//					debugChar = (char)caracter;
//					destination_buffer[charRead] = caracter;
//					++charRead;
//				}
//				
//				String[] pieces = new String(destination_buffer).split(delimitators);
//								
//				for(int i = 0 ; i < pieces.length ; i ++){
//					processWord(pieces[i],ps.fileName, ps.fragmentID); //local Method
//					System.out.println(pieces[i] + " ");
//				}
			
			
			
//				word = new StringBuilder();
//				//read the fragment
//				while(charRead <= numberOfChars){
//
//					//read a word
//					while(true){
//						++charRead;
//						caracter = raf.readByte();
//						debugChar = (char)caracter;
//						if(delimitators.contains(new Character((char)caracter).toString())){
//							/*
//							 * If the charRead == numberOfChars after processing this word
//							 * and the last character read is a delimiter 
//							 * than the next word, FROM THE NEXT FRAGMENT
//							 * will be read.
//							 * 
//							 * When going to the next fragment, then the last word
//							 * from the previous fragment will be read the second time
//							 * because my algorithm checks to see if the previous character
//							 * is a delimiter
//							 */
//							if(charRead == numberOfChars)
//								++charRead;
//							break;
//						}else{
//							word.append((char)caracter);
//						}
//					}
//					if(word.length() == 18){
//						System.out.println("Found the word of size 18 ");
//					}
//					
//					processWord(word.toString(),ps.fileName, ps.fragmentID); //local Method
//					word = new StringBuilder();
//				}
				
//				raf.close();
				
			} catch (IOException e){}
			callback.mapResultReady(result, this.ID, ps.fragmentID);
		 }
	
	public synchronized void processWord(String word, String filename,int fragmentID) {
		if(result == null)
			result = new MapResult(filename, fragmentID);
		
		result.putWord(word); //local Method
		result.numberOfWords = result.numberOfWords+1 ;
		
	}
	}