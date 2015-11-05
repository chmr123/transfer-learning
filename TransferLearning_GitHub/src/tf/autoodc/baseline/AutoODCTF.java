package tf.autoodc.baseline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class AutoODCTF {

	public static void main(String[] args) throws IOException {
		String category = "";
		for(int i = 0; i < args.length; i++){
			if(args[i].equals("-c")){
				category = args[i+1]; 
			}
		}
		if(category.length() == 0){
			System.out.println("No category specified");
			System.exit(0);
		}
		// TODO Auto-generated method stub
		MaxentTagger tagger = new MaxentTagger("taggers/english-left3words-distsim.tagger");
		Documents doc = new Documents();
		Instances ins = new Instances();
		String[] files = {"filezilla.csv","prismstream.csv"};
		Map<String,String[]> alltext = doc.getTextFromFile(files);
		ArrayList<String> dictionary = ins.dictionary(alltext);
		//ArrayList<String> dictionary = ins.dictionaryWithPosTag(alltext,tagger);
		ins.generateTrainingFile(dictionary, alltext, files[0], category);
		ins.generateTestingFile(dictionary, alltext, files[1], category);
		//System.out.println("Work finished.");
	}

}
