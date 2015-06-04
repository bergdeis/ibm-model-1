import java.util.List;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;


public class IBMModel1 {

	// P(f|e) 
	private HashMap<String, HashMap<String, Double>> tt; 
	
	public IBMModel1(){
		
	}

	public IBMModel1(String modelFileName) throws FileNotFoundException{
		tt = new HashMap<String, HashMap<String, Double>>();
		File model = new File(modelFileName);
		Scanner file = new Scanner(model);
		while(file.hasNext()){
			Util.incrementCount(tt, file.next(), file.next(), file.nextDouble());
		}
		file.close();
	}
	
	/*
	 * Initialize the translation with the co-occurrence counts
	 */
	public void initTranslationTable(ArrayList<ArrayList<String>> eSentences, ArrayList<ArrayList<String>> fSentences){
		assert(eSentences.size() == fSentences.size());
		int numSentences = eSentences.size();
		tt = new HashMap<String, HashMap<String,Double>>();
		for (int j = 0; j < numSentences; j++) {
			ArrayList<String> eSentence = eSentences.get(j);
			ArrayList<String> fSentence = fSentences.get(j);
			for (String eWord : eSentence) {
				for (String fWord : fSentence){
					Util.incrementCount(tt, eWord, fWord, 1);
				}
			}
		}
		normalizeTranslationTable(tt);
	}
	

	
	public void train(ArrayList<ArrayList<String>> eSentences, ArrayList<ArrayList<String>> fSentences, int maxIterations){
		initTranslationTable(eSentences, fSentences);
		assert(eSentences.size() == fSentences.size());
		int numSentences = eSentences.size();
        HashMap<String, HashMap<String, Double>> newTT;
        double prevDataLogLikelihood = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < maxIterations; i++) {
			double totalDataLogLikelihood = 0.0;
			newTT = new HashMap<String, HashMap<String, Double>>();
			for (int j = 0; j < numSentences; j++) {
				ArrayList<String> eSentence = eSentences.get(j);
				ArrayList<String> fSentence = fSentences.get(j);
				HashMap<String, Double> newCounts = new HashMap<String, Double>();
				for (String fWord : fSentence){
					double sTotal = 0.0;
					for (String eWord : eSentence){
						sTotal += tt.get(eWord).get(fWord);
					}
					newCounts.put(fWord, sTotal); 
				}
				for (String fWord : fSentence){
					for (String eWord : eSentence){
						Util.incrementCount(newTT, eWord, fWord, tt.get(eWord).get(fWord) / newCounts.get(fWord));
					}
				}
				for (String fWord: fSentence){
					totalDataLogLikelihood += Math.log(newCounts.get(fWord));
				}
				totalDataLogLikelihood += Math.log(1.0 / Math.pow(fSentence.size(), eSentence.size()));
			}
			System.out.println("iteration "+ i + " loglikelihood per sentence = " + totalDataLogLikelihood / numSentences);
			normalizeTranslationTable(newTT);
			tt = newTT;
			if ( i > 0 && (totalDataLogLikelihood - prevDataLogLikelihood) / numSentences < 0.01){
				break;
			}
			prevDataLogLikelihood = totalDataLogLikelihood;
		}

	}

	/*
	 * Normalize the translation table C(F,E) into a translation probability table P(F|E)
	 */
	public void normalizeTranslationTable(HashMap<String, HashMap<String, Double>> translationTable){
		for (String eWord : translationTable.keySet()){
			HashMap<String, Double> countTable = translationTable.get(eWord);
			double totalCount = 0.0;
			for (String fWord : countTable.keySet()){
				totalCount += countTable.get(fWord);
			}
			for (String fWord : countTable.keySet()){
				double unnormalizedCount = countTable.get(fWord);
				countTable.put(fWord, unnormalizedCount / totalCount);
				//System.out.println("P("+fWord+"|"+eWord+") = "+ unnormalizedCount/totalCount);
			}
		}
	}

	public double computeTranslationLogProbability(ArrayList<String> eSentenceHypothesis, ArrayList<String> fSentence) {
		List<String> eStopwords = Arrays.asList("i", "me", "my", "myself", "we", "our", "ours", "ourselves", "you", "your", "yours", "yourself", "yourselves", "he", "him", "his", "himself", "she", "her", "hers", "herself", "it", "its", "itself", "they", "them", "their", "theirs", "themselves", "what", "which", "who", "whom", "this", "that", "these", "those", "am", "is", "are", "was", "were", "be", "been", "being", "have", "has", "had", "having", "do", "does", "did", "doing", "a", "an", "the", "and", "but", "if", "or", "because", "as", "until", "while", "of", "at", "by", "for", "with", "about", "against", "between", "into", "through", "during", "before", "after", "above", "below", "to", "from", "up", "down", "in", "out", "on", "off", "over", "under", "again", "further", "then", "once", "here", "there", "when", "where", "why", "how", "all", "any", "both", "each", "few", "more", "most", "other", "some", "such", "no", "nor", "not", "only", "own", "same", "so", "than", "too", "very", "s", "t", "can", "will", "just", "don", "should", "now");
		List<String> gStopwords = Arrays.asList("aber", "alle", "allem", "allen", "aller", "alles", "als", "also", "am", "an", "ander", "andere", "anderem", "anderen", "anderer", "anderes", "anderm", "andern", "anderr", "anders", "auch", "auf", "aus", "bei", "bin", "bis", "bist", "da", "damit", "dann", "der", "den", "des", "dem", "die", "das", "derselbe", "derselben", "denselben", "desselben", "demselben", "dieselbe", "dieselben", "dasselbe", "dazu", "dein", "deine", "deinem", "deinen", "deiner", "deines", "denn", "derer", "dessen", "dich", "dir", "du", "dies", "diese", "diesem", "diesen", "dieser", "dieses", "doch", "dort", "durch", "ein", "eine", "einem", "einen", "einer", "eines", "einig", "einige", "einigem", "einigen", "einiger", "einiges", "einmal", "er", "ihn", "ihm", "es", "etwas", "euer", "eure", "eurem", "euren", "eurer", "eures", "gegen", "gewesen", "hab", "habe", "haben", "hat", "hatte", "hatten", "hier", "hin", "hinter", "ich", "mich", "mir", "ihr", "ihre", "ihrem", "ihren", "ihrer", "ihres", "euch", "im", "in", "indem", "ins", "ist", "jede", "jedem", "jeden", "jeder", "jedes", "jene", "jenem", "jenen", "jener", "jenes", "jetzt", "kann", "kein", "keine", "keinem", "keinen", "keiner", "keines", "machen", "man", "manche", "manchem", "manchen", "mancher", "manches", "mein", "meine", "meinem", "meinen", "meiner", "meines", "mit", "muss", "musste", "nach", "nicht", "nichts", "noch", "nun", "nur", "ob", "oder", "ohne", "sehr", "sein", "seine", "seinem", "seinen", "seiner", "seines", "selbst", "sich", "sie", "ihnen", "sind", "so", "solche", "solchem", "solchen", "solcher", "solches", "soll", "sollte", "sondern", "sonst", "um", "und", "uns", "unse", "unsem", "unsen", "unser", "unses", "unter", "viel", "vom", "von", "vor", "war", "waren", "warst", "was", "weg", "weil", "weiter", "welche", "welchem", "welchen", "welcher", "welches", "wenn", "werde", "werden", "wie", "wieder", "will", "wir", "wird", "wirst", "wo", "wollen", "wollte", "zu", "zum", "zur", "zwar", "zwischen");
		double score = 0.0;
		/*Got 6% with this
		 * for (int i=0; i<eSentenceHypothesis.size() && i<fSentence.size(); i++){
			String eWord = eSentenceHypothesis.get(i);
			String fWord = fSentence.get(i);
			if (tt.containsKey(eWord) && tt.get(eWord).containsKey(fWord)){
				score += tt.get(eWord).get(fWord);
			}
		}*/
		
		for (String eWord : eSentenceHypothesis){
			//if (eWord.length() > 3){
			if(!eStopwords.contains(eWord)){
				double bestScore = 0.0;
				for (String fWord : fSentence){
					if (!gStopwords.contains(fWord) && tt.containsKey(eWord) && tt.get(eWord).containsKey(fWord) && tt.get(eWord).get(fWord) > bestScore){
						bestScore = tt.get(eWord).get(fWord);
					}
				}
				score += Math.log(bestScore);
			}
		}
		return score / (Math.pow((1 + fSentence.size()), eSentenceHypothesis.size()));
	}
	
	public void save(String fileName) throws FileNotFoundException{
		File output = new File(fileName);
		PrintWriter file = new PrintWriter(output);
		for (String eWord : tt.keySet()){
			for (String fWord : tt.get(eWord).keySet()){
				file.println(eWord + " " + fWord + " " + tt.get(eWord).get(fWord));
			}
		}
		file.close();
	}
	
	
	public static void main(String[] argv) throws IOException{
		IBMModel1 model = new IBMModel1();
		model.train(Util.readCorpus("english.txt"), Util.readCorpus("german.txt"), 10000);
		model.save("output.txt");
		IBMModel1 newModel = new IBMModel1("output.txt");
		ArrayList<String> eSentenceHypothesis = new ArrayList<>(Arrays.asList("a", "book"));
		ArrayList<String> fSentence = new ArrayList<>(Arrays.asList("ein", "buch"));
		double translationProbability = newModel.computeTranslationLogProbability(eSentenceHypothesis, fSentence);
		System.out.println("translationProbability = " + translationProbability);
		
	} 
}
 
