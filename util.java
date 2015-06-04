import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Util {

	/*
	 * Increment the count. C(f,e) += increment
	 * But it seems more complicated in Java, so we provide this for you.
	 */
	public static void incrementCount(HashMap<String, HashMap<String, Double>> tt, String eWord, String fWord, double increment){
		HashMap<String, Double> countTable;
		if (tt.containsKey(eWord)){
			countTable = tt.get(eWord);
		}else {
			countTable = new HashMap<String, Double>();
			tt.put(eWord, countTable);
		}

		double oldCount = countTable.containsKey(fWord)? countTable.get(fWord): 0;
		countTable.put(fWord, oldCount + increment);
	}

	public static ArrayList<ArrayList<String>> readCorpus(String fileName) throws IOException{
		return readCorpus(fileName, -1);
	}

	/*
	 * Read the parallel sentences in an ArrayList
	 * You can specify the number of sentences that you want if you don't want to read the whole file
	 */
	public static ArrayList<ArrayList<String>> readCorpus(String fileName, int numSentences) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		ArrayList<ArrayList<String>> sentences = new ArrayList<ArrayList<String>>();
		String thisLine;
		while ((thisLine = br.readLine()) != null) {
            String[] tokenized = thisLine.toLowerCase().split(" ");
            ArrayList<String> tokenizedList = new ArrayList<String>(); 
            for (String token : tokenized) {
            	tokenizedList.add(token);
            }
            sentences.add(tokenizedList);
            if (numSentences > -1 && sentences.size() == numSentences) break;
         }  	
		br.close();
		return sentences;
		
	}
}
