package com.cristof.MapReduce.Map;

import java.io.File;

public class PartialText {

	public String fileName;
	public long start;
	public long stop; 

	
	public PartialText(String fileName , long start , long stop){
		this.fileName = fileName;
		this.start = start;
		this.stop = stop;
	}
	
	
}
