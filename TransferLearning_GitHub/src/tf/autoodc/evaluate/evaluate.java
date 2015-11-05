package tf.autoodc.evaluate;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class evaluate {

	public static void main(String[] args) throws IOException {
		File testfile = new File(args[0]);
		File output = new File("output");
		BufferedReader br1 = new BufferedReader(new FileReader(output));
		BufferedReader br2 = new BufferedReader(new FileReader(testfile));
		
		String line;
		ArrayList<Integer> predictedLabels = new ArrayList<Integer>();
		ArrayList<Integer> trueLabels = new ArrayList<Integer>();
		while((line = br1.readLine()) != null){
			if(line.contains("label")) continue;
			int predict = Integer.parseInt(line.substring(0, line.indexOf(" ")));	
			predictedLabels.add(predict);
		}
		
		br1.close();
		
		while((line = br2.readLine()) != null){
			String label = line.substring(0, line.indexOf(" "));
			int truelabel = Integer.parseInt(label);
			
			trueLabels.add(truelabel);
		}
		
		br2.close();
		
		double tp = 0.0;
		double fp = 0.0;
		double fn = 0.0;
		
		for(int i = 0; i < trueLabels.size(); i++){
			if(predictedLabels.get(i) == 1 && trueLabels.get(i) == 1) tp++;
			if(predictedLabels.get(i) == -1 && trueLabels.get(i) == 1) fn++;
			if(predictedLabels.get(i) == 1 && trueLabels.get(i) == -1) fp++;
		}
		System.out.println(tp + " " + fp + " " + fn);
		double recall = tp / (tp + fn);
		double precision = tp / (tp + fp);
		double f1 = 2*recall*precision/(recall + precision);
		
		System.out.printf("Recall: %.3f, Precision %.3f, F-score %.3f\n", recall, precision, f1);

	}

}
