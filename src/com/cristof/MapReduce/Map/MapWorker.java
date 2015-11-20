package com.cristof.MapReduce.Map;

import com.cristof.MapReduce.WorkPool;

public class MapWorker extends Thread {
	WorkPool wp;
	int maxSize;

	public MapWorker(WorkPool workpool) {
		this.wp = workpool;
		this.maxSize = 20 ;
	}

	/**
	 * Procesarea unei solutii partiale. Aceasta poate implica generarea unor
	 * noi solutii partiale care se adauga in workpool folosind putWork().
	 * Daca s-a ajuns la o solutie finala, aceasta va fi afisata.
	 */
	void processPartialText(PartialText ps) {

		// if(wp.getWork() == null){
		// 	return;

		// }else{

			if(ps.toString().length() > maxSize){

				String stanga = ps.toString().substring(0, 20);
				String dreapta = ps.toString().substring(20,ps.toString().length());

				//PartialText solutionStanga = new PartialText();
			//	PartialText solutionDreapta = new PartialText();

				//wp.putWork();
				//wp.putWork(solutionDreapta);

			}else{

				String contentOfPartialText = ps.toString();
				
				// System.out.println("Partial Solution has " + contentOfPartialText);

				int lastIndex = 0;
				int count = 0;

				//count the number of appearances of a string in java

				while(lastIndex != -1){

	    			lastIndex = contentOfPartialText.indexOf("mare",lastIndex);
					if(lastIndex != -1){
		        		count ++;
	       				lastIndex += contentOfPartialText.length();
	       			}
	       		}

	       		wp.incrementOccurencesWith(count);
	       		System.out.println("Incrementing occurences with " + count);
			}
		// }
	}
	
	public void run() {
		System.out.println("Thread-ul worker " + this.getName() + " a pornit...");
		while (true) {

			PartialText ps = wp.getWork();

			if (ps == null)
				break;
			
			processPartialText(ps);
		}
		System.out.println("Thread-ul worker " + this.getName() + " s-a terminat...");
	}

	
}