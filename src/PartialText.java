

import java.io.File;

public class PartialText {

	public String fileName;
	public long start;
	public long stop; 
	public int fragmentID;
	//what document it came from. Fragments with the same fragmentID are parts
	//of the same document
	
	public PartialText(String fileName , long start , long stop , int fragmentID){
		this.fileName = fileName;
		this.start = start;
		this.stop = stop;
		this.fragmentID = fragmentID;
	}
	
	
}
