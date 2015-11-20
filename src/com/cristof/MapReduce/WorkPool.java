package com.cristof.MapReduce;

import java.util.LinkedList;

import com.cristof.MapReduce.Map.PartialText;

/**
 * Clasa ce implementeaza un "work pool" conform modelului "replicated workers".
 * Task-urile introduse in work pool sunt obiecte de tipul PartialText.
 *
 */
public class WorkPool {
	int nThreads; // nr total de thread-uri worker
	int nWaiting = 0; // nr de thread-uri worker care sunt blocate asteptand un task
	public boolean ready = false; // daca s-a terminat complet rezolvarea problemei 
	
	LinkedList<PartialText> tasks = new LinkedList<PartialText>();


	int occurences = 0 ;
	/**
	 * Constructor pentru clasa WorkPool.
	 * @param nThreads - numarul de thread-uri worker
	 */
	public WorkPool(int nThreads) {
		this.nThreads = nThreads;
		this.occurences = 0 ;
	}

	public synchronized void incrementOccurencesWith(int toAdd){
		this.occurences += toAdd ;
	}

	public synchronized int getOccurences(){
		return this.occurences;
	}

	/**
	 * Functie care incearca obtinera unui task din workpool.
	 * Daca nu sunt task-uri disponibile, functia se blocheaza pana cand 
	 * poate fi furnizat un task sau pana cand rezolvarea problemei este complet
	 * terminata
	 * @return Un task de rezolvat, sau null daca rezolvarea problemei s-a terminat 
	 */
	public synchronized PartialText getWork() {
		if (tasks.size() == 0) { // workpool gol
			nWaiting++;
			/* condtitie de terminare:
			 * nu mai exista nici un task in workpool si nici un worker nu e activ 
			 */
			if (nWaiting == nThreads) {
				ready = true;
				/* problema s-a terminat, anunt toti ceilalti workeri */
				notifyAll();
				return null;
			} else {
				while (!ready && tasks.size() == 0) {
					try {
						this.wait();
					} catch(Exception e) {e.printStackTrace();}
				}
				
				if (ready)
				    /* s-a terminat prelucrarea */
				    return null;

				nWaiting--;
				
			}
		}
		return tasks.remove();

	}


	/**
	 * Functie care introduce un task in workpool.
	 * @param sp - task-ul care trebuie introdus 
	 */
	synchronized void putWork(PartialText sp) {
		System.out.println("WorkPool - adaugare task: " + sp);
		tasks.add(sp);
		/* anuntam unul dintre workerii care asteptau */
		this.notify();

	}


}



