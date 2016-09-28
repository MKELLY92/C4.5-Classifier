//Matthew Kelly - 12393001
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;

@SuppressWarnings("rawtypes")
public class Classifier {

	private static ArrayList<Integer> numberAttributes;
	private static ArrayList<Sample> samples;
	private static ArrayList<Sample> trainingData;
	private static ArrayList<Sample> testData;
	private static ArrayList<String> classNames;
	private static HashMap<String, int[]> classResults;
	private static Node decisionTree;

	public static void main(String[] args) throws IOException, CloneNotSupportedException {

		samples = parseCSVFile("owls15.csv");

		for (int i = 0; i < 10; i++) {
			trainingData = new ArrayList<Sample>();
			testData = new ArrayList<Sample>();
			decisionTree = null;

			// run classifier test 10 times

			Collections.shuffle(samples);
			for (int x = 0; x < samples.size() / 2; x++) {
				testData.add(samples.get(x));
			}
			trainingData.addAll(samples);
			trainingData.removeAll(testData);
			trainingData.trimToSize();
			System.out.println("RUN #" + i);

			decisionTree = trainClassifier(trainingData, new ArrayList<Integer>(numberAttributes));
			runClassificationTest(testData);
		}
	}

	// read from CSV file containing all sample data
	private static ArrayList<Sample> parseCSVFile(String path) throws IOException, CloneNotSupportedException {

		samples = new ArrayList<Sample>();
		numberAttributes = new ArrayList<Integer>();
		String line;
		FileReader fileRead = new FileReader(path);
		BufferedReader buffRead = new BufferedReader(fileRead);
		classNames = new ArrayList<String>();
		boolean firstPass = true;

		// take each line and create a new Sample object
		while ((line = buffRead.readLine()) != null) {

			System.out.println(line);

			ArrayList<String> sampleString = new ArrayList<String>();
			for (String s : line.split(",")) {
				sampleString.add(s);
			}

			// class value will be last entry line
			String classValue = (String) sampleString.remove(sampleString.size() - 1);
			ArrayList attrValues = sampleString;

			Sample sample = new Sample(attrValues, classValue);

			int a = 0;
			while (a < sample.getNumberOfAttributes() && firstPass) {
				numberAttributes.add(a);
				a++;
			}

			firstPass = false;
			// add new classes to the known classes
			if (!classNames.contains(sample.getClassValue())) {
				classNames.add(sample.getClassValue());
			}

			samples.add(new Sample(sample));
		}

		buffRead.close();
		fileRead.close();
		return samples;
	}

	// create the decision tree for classification
	private static Node trainClassifier(ArrayList<Sample> trainingSet, ArrayList<Integer> remAttr) {

		Node attrNode = null;

		HashMap<String, Integer> breakdown = getDataBreakdown(trainingSet);
		if (breakdown.size() == 1 || remAttr.size() == 0) {

			int tempMax = 0;
			String mostCommon = "";
			for (Entry<String, Integer> e : breakdown.entrySet()) {
				if (e.getValue() > tempMax) {
					tempMax = e.getValue();
					mostCommon = e.getKey();
				}
			}

			attrNode = new Node(-1, mostCommon);
			return attrNode;
		}

		else {

			double iG = 0.0;
			double tIG = 0.0;
			int bestAttr = -1;

			// find the attribute that gives the biggest information gain
			for (int i : remAttr) {
				tIG = calculateInformationGain(i, trainingSet);
				if ((tIG = calculateInformationGain(i, trainingSet)) >= iG) {
					iG = tIG;
					bestAttr = i;
				}
			}

			// find the best threshold to split the data for this attribute
			double threshold = findBestThreshold(bestAttr, trainingSet);

			attrNode = new Node(bestAttr, threshold);

			ArrayList<Sample> greaterThan = new ArrayList<Sample>();
			ArrayList<Sample> lessThan = new ArrayList<Sample>();

			for (Sample s : trainingSet) {
				if (Double.parseDouble(s.getAttribueValue(bestAttr)) > threshold) {
					greaterThan.add(new Sample(s));
				} else {
					lessThan.add(new Sample(s));
				}
			}
			remAttr.remove((Object) bestAttr);

			//add left node on decision tree
			attrNode.childrenNodes.add(trainClassifier(lessThan, new ArrayList<Integer>(remAttr)));
			//add right node on decision tree
			attrNode.childrenNodes.add(trainClassifier(greaterThan, new ArrayList<Integer>(remAttr)));

		}
		return attrNode;
	}

	private static void runClassificationTest(ArrayList<Sample> testSet)
			throws IOException, CloneNotSupportedException {

		classResults = new HashMap<String, int[]>();
		FileWriter fw = new FileWriter("results.txt", true);

		for (Object str : classNames.toArray()) {
			int[] results = { 0, 0 };
			classResults.put((String) str, results);
			// Correct/incorrect classification
		}

		String c;
		int numberCorrect = 0;

		// break up results of different simulations
		fw.write("*******************************************************************");
		fw.write("PREDICTED \t ACTUAL");
		
		for (Sample sam : testSet) {

			c = classifySample(sam);


			fw.write("\n" + c + " \t " + sam.getClassValue());
			fw.write("\r");

			//System.out.println(c);
			int res[] = classResults.get(sam.getClassValue());

			if (c.equals(sam.getClassValue())) {
				res[0]++;
				numberCorrect++;
			} else {
				res[1]++;
			}
			//update correct/incorrect classifications for this 
			classResults.put(sam.getClassValue(), res);
		}

		
		fw.write("\nNumber of correct classifications : " + numberCorrect);
		fw.write("\nNumber of incorrect classifications : " + (testSet.size() - numberCorrect));
		fw.write("\nClassification Accuracy : " + (double) numberCorrect / testSet.size() * 100 + "%");
		fw.close();
	}

	private static String classifySample(Sample sample) {

		Node decisionNode = decisionTree;
		while (!(decisionNode.data instanceof String) && (decisionNode.childrenNodes.size() > 0)) { // go
																									// through
																									// tree
			// until
			// we reach leaf nodes
			if ((Double.parseDouble(sample.getAttribueValue(decisionNode.id)) < (double) decisionNode.data)) {
				decisionNode = (Node) decisionNode.childrenNodes.get(0);
			} else {
				decisionNode = (Node) decisionNode.childrenNodes.get(1);
			}
		}
		return (String) decisionNode.data;
	}

	private static double calcEntropyOverThreshold(int attrID, double threshold, ArrayList<Sample> dataSet) {

		double ent = calculateEntropy(dataSet);

		ArrayList<Sample> g = new ArrayList<Sample>();
		ArrayList<Sample> l = new ArrayList<Sample>();

		for (Sample s : dataSet) {
			if (Double.parseDouble(s.getAttribueValue(attrID)) > threshold) {
				g.add(s);
			} else {
				l.add(s);
			}
		}

		ent -= ((double) l.size() / dataSet.size()) * calculateEntropy(l);
		ent -= ((double) g.size() / dataSet.size()) * calculateEntropy(g);

		return ent;
	}

	private static double calculateInformationGain(int attrID, ArrayList<Sample> dataSet) {

		double threshold = findBestThreshold(attrID, dataSet);
		return calcEntropyOverThreshold(attrID, threshold, dataSet);
	}

	private static double calculateEntropy(ArrayList<Sample> dataSet) {

		int classSizes[] = new int[classNames.size()];
		double entropy = 0.0;
		for (Sample s : dataSet) {

			if (classNames.contains(s.getClassValue())) {
				classSizes[classNames.indexOf(s.getClassValue())]++;
			}
		}

		for (int i : classSizes) {
			if (dataSet.size() != 0 && i != 0) {
				double y = (double) i / dataSet.size();

				entropy -= y * (Math.log(y) * Math.log(2.0));
			}
		}
		return entropy;
	}

	private static double findBestThreshold(int attrId, ArrayList<Sample> data) {

		double bestThreshold = 0.0;
		double bestInformationGain = 0.0;

		ArrayList<Double> attrValues = new ArrayList<Double>();
		for (Sample s : data) {
			attrValues.add(Double.parseDouble(s.getAttribueValue(attrId)));
		}

		Collections.sort(attrValues);
		double threshold = 0.0;
		double tempInformationGain = 0.0;
		Double[] attrArr = new Double[attrValues.size()];
		attrArr = attrValues.toArray(attrArr);

		for (int i = 1; i < attrArr.length; i++) {
			threshold = (double) ((attrArr[i - 1] + attrArr[i]) / 2);
			tempInformationGain = calcEntropyOverThreshold(attrId, threshold, data);

			if (tempInformationGain > bestInformationGain) {
				bestInformationGain = tempInformationGain;
				bestThreshold = threshold;
			}
		}
		return bestThreshold;
	}

	private static HashMap<String, Integer> getDataBreakdown(ArrayList<Sample> dataSet) {

		HashMap<String, Integer> bd = new HashMap<String, Integer>();
		for (Sample s : dataSet) {
			if (!bd.containsKey(s.getClassValue())) {
				bd.put(s.getClassValue(), 1);
			} else {
				int temp = bd.get(s.getClassValue()) + 1;
				bd.put(s.getClassValue(), temp);
			}
		}
		return bd;
	}

	// structure to hold the decision tree
	public static class Node<T> {

		private ArrayList<Node> childrenNodes;
		private int id;
		private Object data; // can be string or double

		public Node(int id, Object data) {
			this.id = id;
			this.data = data;
			childrenNodes = new ArrayList<Node>();
		}
	}
}