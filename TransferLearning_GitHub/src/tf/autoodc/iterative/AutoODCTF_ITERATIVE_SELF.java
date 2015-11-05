package tf.autoodc.iterative;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class AutoODCTF_ITERATIVE_SELF {

	public static void main(String[] args) throws IOException, InterruptedException {
		String category = "";
		String training = "";
		String testing = "";
		for(int i = 0; i < args.length; i++){
			if(args[i].equals("-c")){
				category = args[i+1];
			}
			if(args[i].equals("-train")){
				training = args[i+1];
			}
			if(args[i].equals("-test")){
				testing = args[i+1];
			}
		}
		
		System.out.println("Removing temp files...");
		File folder = new File(".");
		for (File f : folder.listFiles()) {
		    if (f.getName().endsWith(".data")) {
		        f.delete(); // may fail mysteriously - returns boolean you may want to check
		    }
		}
		//MaxentTagger tagger = new MaxentTagger("taggers/english-left3words-distsim.tagger");
		Documents doc = new Documents();
		Instances ins = new Instances();
		String[] files = new String[2];
		files[0] = training;
		files[1] = testing;
		Map<String,String[]> alltext = doc.getTextFromFile(files,training,testing);
		Map<String,String[]> alltextOriginal = doc.getTextFromFile(files,training,testing);
		
		ArrayList<String> dictionary = ins.dictionary(alltext);
		//ArrayList<String> dictionary = ins.dictionaryWithPosTag(alltext,tagger);
	
		String[] categories = new String[1];
		categories[0] = category;
		
		int initialTestDataSize = ins.getTestDataSize(alltext);//the inistial size of testing data
		Map<String, Integer> finalPredictionLabels = new HashMap<String, Integer>();//A map to store the final predicted labels for evaluation purpose
		//Build a model on each defect category
		int positive = 0;
		int negative = 0;
		for(String c : categories){
			//These two lines generate the initial training and testing sets for the current defect category
			ins.generateTrainingFile(dictionary, alltext, c);
			ins.generateTestingFile(dictionary, alltext, c);
			
			//define a counter for iteration number
			int iterationNum = 1;
			while(ins.getTestDataSize(alltext) != 0){
				System.out.println("Iteration " + iterationNum + "...(" + initialTestDataSize + " iterations left)");
				svm_iteration(c);//Iterative learning
				
				//Create a map to store the probabilities the SVM classifier output on the test data in each iteration
				Map<String, Double> predictedProbabilityMap = ins.getPredictedProbability(alltext,c);
				//Create a map to store the labels the SVM classifier predicted on the test data in each iteration
				Map<String, Integer> predictedLabelsMap = ins.getPredictedLabels(alltext,c);
				//The selected instance with highest probability
				String selectedKey = ins.selectInstanceHighest(predictedProbabilityMap);
				//The predicted label of the selected instance above
				int selectedLabel = predictedLabelsMap.get(selectedKey);
				if(selectedLabel == 1) positive++;
				if(selectedLabel == -1) negative++;
				//Add the selected instance to the final prediction
				finalPredictionLabels.put(selectedKey, selectedLabel);
				/*Update "alltext" map after converting the selected testing instance (with highest probability) to training instance, 
				  whereThe label of selected instance is changed to "train" from "test".
				*/
				alltext = ins.updateAllText(alltext, selectedKey, dictionary, c);
				//Generate training and testing files again
				ins.generateTrainingFile(dictionary, alltext, c);
				ins.generateTestingFile(dictionary, alltext, c);
				
				//Update iteration number and testing data size
				iterationNum++;
				initialTestDataSize--;
				System.out.println("     Label+ : " + positive + " Label- : " + negative);
				evaluate(alltextOriginal, finalPredictionLabels,c);
			}
			
			
		}
	}
	
	private static void svm_iteration(String category) throws IOException, InterruptedException{
		//Train a prediction model
		ProcessBuilder pr1 = new ProcessBuilder("./svm-train", "-q","-c","10000","-b","1","training."+ category,category+".model");
        pr1.directory(new File("."));
		//pr1.directory(new File("C:\\Users\\install\\Desktop\\TransferLearning\\autoodc"));
		//pr1.directory(new File("/users5/csegrad/mingruic/transferlearning"));
		Process p = pr1.start();
		p.waitFor();
	
		//Test a prediction model
		ProcessBuilder pr2 = new ProcessBuilder("./svm-predict","-q","-b","1","testing."+ category,category+".model",category+".probability");
		pr2.directory(new File("."));
        //pr2.directory(new File("C:\\Users\\install\\Desktop\\TransferLearning\\autoodc"));
		//pr2.directory(new File("/users5/csegrad/mingruic/transferlearning"));
		
		p = pr2.start();
		p.waitFor();	
		
		//Removing temp training and testing data files
		File folder = new File(".");
		for (File f : folder.listFiles()) {
		    if (f.getName().endsWith("."+category)) {
		        f.delete(); // may fail mysteriously - returns boolean you may want to check
		    }
		}
	}
	
	private static void evaluate(Map<String, String[]> alltextOriginal, Map<String, Integer> finalPrediction, String category){
		Map<String, Integer> groundTruth = new HashMap<String, Integer>();
		for(String key : alltextOriginal.keySet()){
			if(alltextOriginal.get(key)[1].equals("test")){
				String c = alltextOriginal.get(key)[0];
				int label = c.equals(category)? 1 : -1;
				groundTruth.put(key, label);
			}
		}
		
		double tp = 0;
		double fp = 0;
		double fn = 0;
		
		for(String key : finalPrediction.keySet()){
			int truelabel = groundTruth.get(key);
			int predictedlabel = finalPrediction.get(key);
			if(truelabel == 1 && predictedlabel == 1) tp++;
			if(truelabel == -1 && predictedlabel == 1) fp++;
			if(truelabel == 1 && predictedlabel == -1) fn++;
		}
		
		System.out.println("     " + tp + " " + fp + " " + fn);
		double recall = tp / (tp + fn);
		double precision = tp / (tp + fp);
		double f1 = 2*recall*precision/(recall + precision);
		
		System.out.printf("     Recall: %.3f, Precision %.3f, F-score %.3f\n", recall, precision, f1);
	}

}