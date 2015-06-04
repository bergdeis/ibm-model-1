import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;


public class SentenceAligner {
	
	private IBMModel1 model;
	private ArrayList<ArrayList<String>> eSentences;
	private ArrayList<ArrayList<String>> fSentences;
	private ArrayList<ArrayList<String>> badESentences;
	private HashMap<ArrayList<String>, ArrayList<String>> correctAlignment;
	private HashMap<ArrayList<String>, ArrayList<String>> predictedAlignment;

	public SentenceAligner(String eSentFile, String badESentFile, String fSentFile, int maxIterations) throws IOException{
		correctAlignment = new HashMap<ArrayList<String>, ArrayList<String>>();
		eSentences = Util.readCorpus(eSentFile, 100);
		fSentences = Util.readCorpus(fSentFile, 100);
		badESentences = Util.readCorpus(badESentFile, 300);
		for (int i=0; i<eSentences.size(); i++){
			correctAlignment.put(fSentences.get(i), eSentences.get(i));
		}
	}
	
	public void alignSentences(int maxIterations){
		predictedAlignment = new HashMap<ArrayList<String>, ArrayList<String>>();
		IBMModel1 model = new IBMModel1();
		model.train(eSentences, fSentences, maxIterations);
		eSentences.addAll(badESentences);
		
		for (ArrayList<String> fSentence : fSentences){
			double bestAlignment = Double.NEGATIVE_INFINITY;
			ArrayList<String> bestSentence = null;
			for(ArrayList<String> eSentence : eSentences){
				if(Math.abs(fSentence.size() - eSentence.size()) < 5){
					double translationalProbability = model.computeTranslationLogProbability(eSentence, fSentence);
					if (translationalProbability > bestAlignment){
						bestAlignment = translationalProbability;
						bestSentence = eSentence;
					}
				}
			}
			this.predictedAlignment.put(fSentence, bestSentence);
		}
	}	
	
	public double analysis(){
		double count = 0.0;
		for (ArrayList<String> fSentence : correctAlignment.keySet()){
			ArrayList<String> correctE= correctAlignment.get(fSentence);
			if(predictedAlignment.get(fSentence).equals(correctE)){
				count += 1;
			}
		}
		return count / correctAlignment.keySet().size();
	}
	
	public static void main(String[] args) throws IOException{
		SentenceAligner SA = new SentenceAligner("en-sentences.txt", "en-bad-sentences.txt", "de-sentences.txt", 10000);
		SA.alignSentences(10000);
		System.out.println("correct alignment accuracy = " + SA.analysis() * 100 + "%");
	}
		
}
