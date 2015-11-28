package com.cristof.MapReduce.Map;

import java.beans.DesignMode;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
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
			
			int numberOfChars =(int) (ps.stop - ps.start + 1); 
			byte[] destination_buffer =  new byte[10000 ];
						
			try {
				
				RandomAccessFile raf = new  RandomAccessFile(new File(ps.fileName),"rw");
				int charRead = 0;
				StringBuilder word = new StringBuilder();
				raf.seek(ps.start);
				while(charRead <= numberOfChars){
					while(true){
						byte caracter = raf.readByte();
						if(delimitators.contains(new Character((char)caracter).toString())){
							charRead = 0;
							break;
						}else{
							word.append((char)caracter);
							charRead ++;
						}
					}
					
					if(word.toString()!= ""){
						System.out.println(word.toString());
					}
					
					processWord(word.toString());
					
					word = new StringBuilder();
					
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
//				e.printStackTrace();
			}
			
			
			
		 }
	
	private void processWord(String string) {
		// TODO Auto-generated method stub
		
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