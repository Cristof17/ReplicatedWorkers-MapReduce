import java.util.ArrayList;
import java.util.HashMap;


public class MapResult {

	public HashMap<Integer,Integer> hash ;
	public String filename;
	public ArrayList<String> maxWords;
	public int maxLength;
	public int numberOfWords;
	public int fragmentID;
	
	public MapResult(String filename,int fragmentID){
		this.filename = filename;
		this.numberOfWords = 0;
		this.fragmentID = fragmentID;
	}
	
	//count the word to the hash and check if it is of size maxSize
	public synchronized void putWord(String word){
		
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
			
			maxLength = word.length();
			maxWords = new ArrayList<>();
			maxWords.add(word);
		}else if(word.length() == maxLength){
			maxWords.add(word);
		}
	}
	
}
