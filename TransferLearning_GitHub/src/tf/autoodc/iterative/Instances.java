package tf.autoodc.iterative;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class Instances {
	Documents doc = new Documents();
	Map<String, Integer> predicted_labels = new LinkedHashMap<String, Integer>();
	Map<String, Double> predicted_probability = new LinkedHashMap<String, Double>();

	public ArrayList<String> dictionary(Map<String, String[]> alltext) {
		Set<String> tokenset = new HashSet<String>();
		for (String line : alltext.keySet()) {
			//String postags = doc.getPOSTag(line);
			String[] split = line.split("\\s+");
			for (String s : split) {
				//String token = parsePostag(s);
				tokenset.add(s);
				// System.out.println(s + ", " + token);
			}
		}
		ArrayList<String> dictionary = new ArrayList<String>(tokenset);
		return dictionary;
	}
	
	public ArrayList<String> dictionaryWithPosTag(Map<String, String[]> alltext,MaxentTagger tagger) {
		Set<String> tokenset = new HashSet<String>();
		for (String line : alltext.keySet()) {
			String postags = doc.getPOSTag(line,tagger);
			String[] split = postags.split("\\s+");
			for (String s : split) {
			
				String token[] = parsePostag(s);
				if(!token[1].equals("NN"))
					tokenset.add(token[0]);
			}
		}
		ArrayList<String> dictionary = new ArrayList<String>(tokenset);
		return dictionary;
	}

	public void generateTrainingFile(ArrayList<String> dictionary, Map<String, String[]> alltext, String category) throws IOException {
		//System.out.println("Generating training file on dataset " + filename + "...");
		for (String line : alltext.keySet()) {
			int[] vector = new int[dictionary.size()];
			String flag;
			if (alltext.get(line)[0].equals(category)) {
				flag = "+1";
			} else {
				flag = "-1";
			}
			if (alltext.get(line)[1].equals("train")) {
				List<String> termlist = Arrays.asList(line.toLowerCase().split("\\s+"));
				//System.out.println(termlist);
				for (int i = 0; i < vector.length; i++) {				
					if (termlist.contains(dictionary.get(i))) {
						vector[i] = 1;
					}
				}
				writeInstanceToFile(vector, flag, "train",category);
			}
			
		}
	}

	public void generateTestingFile(ArrayList<String> dictionary, Map<String, String[]> alltext, String category) throws IOException {
		//System.out.println("Generating testing file on dataset " + filename + "...");
		for (String line : alltext.keySet()) {
			int[] vector = new int[dictionary.size()];
			String flag;
			if (alltext.get(line)[0].equals(category)) {
				flag = "+1";
			} else {
				flag = "-1";
			}
			if (alltext.get(line)[1].equals("test")) {
				List<String> termlist = Arrays.asList(line.split("\\s+"));
				for (int i = 0; i < vector.length; i++) {
					if (termlist.contains(dictionary.get(i))) {
						vector[i] = 1;
					}
				}
				writeInstanceToFile(vector, flag, "test",category);
			}
			
		}
	}

	
	public Map<String, Double> getPredictedProbability(Map<String, String[]> alltext) throws IOException{
		ArrayList<String> test_instances = new ArrayList<String>();
		for(String key : alltext.keySet()){
			if(alltext.get(key)[1].equals("test")){
				test_instances.add(key);
			}
		}
		Map<String, Double> predictedProbability = new LinkedHashMap<String, Double>();
		File output = new File("probability");
		BufferedReader br = new BufferedReader(new FileReader(output));
		String line;
		int index = 0;
		while((line = br.readLine()) != null){
			if(line.contains("label")) {
				continue;
			}else{
				String[] split = line.split(" ");
				double p1 = Double.parseDouble(split[1]);
				double p2 = Double.parseDouble(split[2]);
				double selectedProbability = p1 > p2 ? p1 : p2;
				String key = test_instances.get(index);
				predictedProbability.put(key, selectedProbability);
				index++;
			}
		}
		br.close();
		return predictedProbability;
	}
	
	public Map<String, Integer> getPredictedLabels(Map<String, String[]> alltext) throws NumberFormatException, IOException{
		ArrayList<String> test_instances = new ArrayList<String>();
		for(String key : alltext.keySet()){
			if(alltext.get(key)[1].equals("test")){
				test_instances.add(key);
			}
		}
		Map<String, Integer> predictedLables = new LinkedHashMap<String, Integer>();
		File output = new File("probability");
		BufferedReader br = new BufferedReader(new FileReader(output));
		String line;
		int index = 0;
		while((line = br.readLine()) != null){
			if(line.contains("label")) {
				continue;
			}else{
				String[] split = line.split(" ");
				int label = Integer.parseInt(split[0]);
				String key = test_instances.get(index);
				predictedLables.put(key, label);
				index++;
			}
		}
		br.close();
		return predictedLables;
	}
	
	public Map<String, String[]> updateAllText(Map<String, String[]> alltext, String selectedKey,ArrayList<String> dictionary, String category) throws IOException{
		Map<String, String[]> alltextUpdated = alltext;
		for(String key : alltextUpdated.keySet()){
			if(key.equals(selectedKey)){
				String[] value = alltextUpdated.get(key);
				value[1] = "train";
				alltextUpdated.put(key, value);
			}
		}
		return alltextUpdated;
	}
	
	public String selectInstanceHighest(Map<String, Double> test_instances){
		double maxProbability = Collections.max(test_instances.values());
		for(Entry<String, Double> entry : test_instances.entrySet()){
			if(entry.getValue() == maxProbability){
				return entry.getKey();
			}
		}
		return null;
	}
	
	public int getTestDataSize(Map<String, String[]> alltext){
		int size = 0;
		for(String key : alltext.keySet()){
			if(alltext.get(key)[1].equals("test")){
				size++;
			}
		}
		return size;
	}
	
	private void writeInstanceToFile(int[] vector, String flag, String type, String category) throws IOException {
		FileWriter fw = null;
		if (type.equals("train"))
			fw = new FileWriter("training" + "_" + category+".data", true);
		if (type.equals("test"))
			fw = new FileWriter("testing" + "_" + category+".data", true);
		fw.write(flag + " ");

		for (int i = 1; i <= vector.length; i++) {
			if(vector[i-1] != 0){
				fw.write(i + ":" + vector[i-1] + " ");
			}		
		}
		
		fw.write("\n");
		fw.flush();
		fw.close();

	}

	private String[] parsePostag(String term_tag) {
		String term = term_tag.substring(0, term_tag.lastIndexOf("_"));
		String tag = term_tag.substring(term_tag.lastIndexOf("_")+1, term_tag.length());
		String[] array = new String[2];
		array[0] = term;
		array[1] = tag;
		return array;
	}
}
