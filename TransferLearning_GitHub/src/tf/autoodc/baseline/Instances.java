package tf.autoodc.baseline;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class Instances {
	Documents doc = new Documents();

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

	public void generateTrainingFile(ArrayList<String> dictionary, Map<String, String[]> alltext, String filename,
			String category) throws IOException {
		//System.out.println("Generating training file on dataset " + filename + "...");
		for (String line : alltext.keySet()) {
			int[] vector = new int[dictionary.size()];
			String flag;
			if (alltext.get(line)[0].equals(category)) {
				flag = "+1";
			} else {
				flag = "-1";
			}
			if (alltext.get(line)[1].equals(filename)) {
				List<String> termlist = Arrays.asList(line.toLowerCase().split("\\s+"));
				//System.out.println(termlist);
				for (int i = 0; i < vector.length; i++) {				
					if (termlist.contains(dictionary.get(i))) {
						vector[i] = 1;
					}
				}
				writeInstanceToFile(vector, flag, filename, "train",category);
			}
			
		}
	}

	public void generateTestingFile(ArrayList<String> dictionary, Map<String, String[]> alltext, String filename,
			String category) throws IOException {
		//System.out.println("Generating testing file on dataset " + filename + "...");
		for (String line : alltext.keySet()) {
			int[] vector = new int[dictionary.size()];
			String flag;
			if (alltext.get(line)[0].equals(category)) {
				flag = "+1";
			} else {
				flag = "-1";
			}
			if (alltext.get(line)[1].equals(filename)) {
				List<String> termlist = Arrays.asList(line.split("\\s+"));
				for (int i = 0; i < vector.length; i++) {
					if (termlist.contains(dictionary.get(i))) {
						vector[i] = 1;
					}
				}
				writeInstanceToFile(vector, flag, filename, "test",category);
			}
			
		}
	}

	private void writeInstanceToFile(int[] vector, String flag, String filename, String type, String category) throws IOException {
		FileWriter fw = null;
		if (type.equals("train"))
			fw = new FileWriter(filename.replace(".csv", "") + "_" + category, true);
		if (type.equals("test"))
			fw = new FileWriter(filename.replace(".csv", "") + "_" + category, true);
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
