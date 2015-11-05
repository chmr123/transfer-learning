package tf.autoodc.iterative;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import com.opencsv.CSVReader;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class Documents {
	
	Stemmer stemmer = new Stemmer();
	public LinkedHashMap<String, ArrayList<String>> getTokenAndPOSTag() {
		LinkedHashMap<String, ArrayList<String>> token_type = new LinkedHashMap<String, ArrayList<String>>();
		return token_type;
	}

	public String getPOSTag(String text,MaxentTagger tagger) {

		String tagged = tagger.tagString(text);
		return tagged;
	}

	public Map<String, String[]> getTextFromFile(String[] files, String training, String testing) throws IOException {
		Map<String, String[]> alltext = new LinkedHashMap<String, String[]>();
		for (String filename : files) {
			//System.out.println("Working on dataset " + filename);
			CSVReader reader = new CSVReader(new FileReader(filename));
			
			String[] nextLine;
			
			while ((nextLine = reader.readNext()) != null) {
				String line = nextLine[0].toLowerCase() + " " + nextLine[1].toLowerCase();
				String[] split = line.split("\\s+");
				String line_after_stemmed = "";
				for(String s : split){
					line_after_stemmed = line_after_stemmed + stem(s) + " ";
				}
				String[] category_file = new String[2];
				category_file[0] = nextLine[2];
				if(filename.equals(training))
					category_file[1] = "train";
				else if(filename.equals(testing))
					category_file[1] = "test";
				else{
					System.out.println("Invalid file name");
					System.exit(0);
				}
				alltext.put(line_after_stemmed, category_file);
			}
			reader.close();
		}
		
		return alltext;
	}
	
	private String stem(String term){
		char[] chars = term.toCharArray();
		for (char c : chars)
			stemmer.add(c);
		stemmer.stem();
		String stemmed = stemmer.toString();
		return stemmed;
	}
}
