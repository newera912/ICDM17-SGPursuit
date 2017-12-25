package com.apdm.baselines.FocusCO.FocusCOMatlab;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabInvocationException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import matlabcontrol.MatlabProxyFactoryOptions;

import org.apache.commons.lang3.ArrayUtils;

import com.apdm.base.APDMInputFormat;
import com.apdm.base.Utils;
import com.apdm.scoreFuncs.Stat;
import com.google.common.collect.Sets;

import edu.stonybrook.focused.main.CommunityClusterer;

//import nl.tudelft.simulation.dsol.interpreter.operations.NEW;

public class TestFocusCOBackUp {



	public static void TestSingleFile(String fileName, String root,
			String outFile, double gamma, double disRatio, String matlabfolder) {

		MatlabProxyFactoryOptions options = new MatlabProxyFactoryOptions.Builder()
				.setUsePreviouslyControlledSession(true).setHidden(true)
				.setMatlabLocation(null).build();
		MatlabProxyFactory factory = new MatlabProxyFactory(options);
		MatlabProxy proxy = null;

		FileWriter output = null;
		try {
			output = new FileWriter(outFile, true);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
			proxy = factory.getProxy();
		} catch (MatlabConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Object[] fargs = new Object[5];
		Object[] matlabReturn = null;
		double[] topAttIndex = null;
		String matlabPath = "cd(\'" + matlabfolder + "\')";
		String dataFile = root + fileName;
		APDMInputFormat apdm = new APDMInputFormat(dataFile);

		/** input File */
		fargs[0] = new String(dataFile);
		/** output File */
		fargs[1] = new String(Utils.getFileName(dataFile) + ".wEdges");
		/** User input Sample nodes */
		fargs[2] = getPartArray(apdm.data.trueSubGraphNodes, disRatio);
		/** gamma ,default 1.0 */
		fargs[3] = new Double(gamma);
		/**
		 * Dissimilar Samples Ratio default 2.0*|Sample nodes|
		 * defaultDissimilarSamples = disRatio*size(similar_pairs,2);
		 */
		fargs[4] = new Double(disRatio);
		long startTime = System.nanoTime();

		try {
			proxy.eval(matlabPath);
			matlabReturn = proxy.returningFeval("FocusCO", 1, fargs);
			// System.out.println(ArrayUtils.toString(matlabReturn[0]));
			Object resultObj = matlabReturn[0];
			topAttIndex = ((double[]) resultObj);
			proxy.disconnect();

		} catch (MatlabInvocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// long MatlabEndTime = System.nanoTime();
		// /double matlab = (MatlabEndTime - startTime) / 1e9;

		/** Calculate attribut Pre, Rec and F-score **/
		int k = apdm.data.trueFeas.length;
		Set<Integer> topAttSet = getTopKValue(topAttIndex, k);
		int attInterSection = Sets.intersection(topAttSet,
				Utils.intArray2Set(apdm.data.trueFeas)).size();
		double[] attPRF = Stat.PRF(attInterSection, topAttSet.size(), k);


		CommunityClusterer communityClusterer = new CommunityClusterer(0);
		try {
			communityClusterer.processOneFile((String) fargs[1], root, false,
					"Focus", false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int[] truenodes = apdm.data.trueSubGraphNodes;
		Arrays.sort(truenodes);
		// System.out.println("GraphSize:" + apdm.data.numNodes);
		// System.out.println("True :" + truenodes.length + " "
		// + ArrayUtils.toString(truenodes));
		/** Calcualte best true cluster pre rec **/
		double bestPre = 0.0D;
		double bestRec = 0.0D;
		double bestFScore = 0.0D;
		Set<Integer> BestRes = null;
		for (Set<Integer> res : communityClusterer.clusters) {
			// System.out.println(res.size() + " "
			// + res.stream().sorted().collect(Collectors.toList()));
			int clusInterSection = Sets.intersection(getJavaIndex(res),
					Utils.intArray2Set(apdm.data.trueSubGraphNodes)).size();
			double[] tempPRF = Stat.PRF(clusInterSection, res.size(),
					apdm.data.trueSubGraphNodes.length);
			if (tempPRF[2] > bestFScore) {
				bestPre = tempPRF[0];
				bestRec = tempPRF[1];
				bestFScore = tempPRF[2];
				BestRes = res;
			}
		}
		// System.out.println("Best Cluster: " + BestRes.size() + " "
		// + BestRes.stream().sorted().collect(Collectors.toList()));
		// System.out.println(communityClusterer.clusters.toString());
		// System.out.println("Matlab: " + matlab + " Java: "
		// + (System.nanoTime() - MatlabEndTime) / 1e9);
		try {
			output.write(Utils.roundW(bestPre) + " " + Utils.roundW(bestRec)
					+ " " + Utils.roundW(bestFScore) + " "
					+ Utils.roundW(attPRF[0]) + " " + Utils.roundW(attPRF[1])
					+ " " + Utils.roundW(attPRF[2]) + "\n");

			output.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(Utils.roundW(bestPre)
				+ " " + Utils.roundW(bestRec)
				+ " " + Utils.roundW(bestFScore) + " "
				+ Utils.roundW(attPRF[0]) + " " + Utils.roundW(attPRF[1]) + " "
				+ Utils.roundW(attPRF[2]) + " " + fileName + " " + gamma + " "
				+ disRatio);

	}


	public static Set<Integer> getTopKValue(double[] x, int k) {
		Set<Integer> mySet = new HashSet<Integer>();
		for (int i = 0; i < k && i < x.length; i++) {
			mySet.add((int) x[i] - 1); // matlab to java index
		}
		return mySet;
	}

	public static int[] getPartArray(int[] array, double ratio) {
		int k = (int) Math.ceil(ratio * array.length);
		if (k < 2) {
			k = 2;
		}
		int[] randomIdx = new Random().ints(0, array.length).distinct()
				.limit(k).toArray();
		int[] subArray = null;
		for (int i : randomIdx) {
			subArray = ArrayUtils.add(subArray, array[i] + 1);
		}
		return subArray;
	}

	public static Set<Integer> getJavaIndex(Set<Integer> S) {
		Set<Integer> matlab2Java = new HashSet<Integer>();
		for (Integer i : S) {
			matlab2Java.add(i - 1);
		}

		return matlab2Java;
	}



	public static void FocusCOTest1VaryNumOfAtt() throws NumberFormatException, IOException {
		// VaryingNumOfAttributes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_TrueSGSize_30_FeasNum_20_trueFeasNum_5_sigmas1_0.0316_case_2.txt
		String matlabPath = "F:/workspace/git/S2GraphMP/src/main/java/edu/albany/cs/baselines/FocusCO/FocusCOMatlab/";
		String root = "F:/workspace/git/S2GraphMP/data/DenseGraph/DenseSubgraph_APDM/final/";
		String outroot = "outputs/FocusCOResult/Test1/";
		
		double p_in = 0.35;
		double p_out = 0.10;
		int numClusters = 10;
		int clusterSize = 30;
		int numTrueFeat = 10;
		long startTime = System.nanoTime();
		for (double gamma : new double[] { 1.0 }) {
			for (double disRatio : new double[] { 2.0 }) {
				for (int FeaNum : new int[] { 20, 40, 80, 100 }) {//

					String outfile = outroot
							+ "VaryingNumOfAttributes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_"
							+ numClusters + "_TrueSGSize_" + clusterSize
							+ "_FeasNum_" + FeaNum + "_trueFeasNum_"
							+ numTrueFeat + "_sigmas1_0.0316-0.2.txt";

					for (int i = 0; i < 50; i++) {
						String fileName = "VaryingNumOfAttributes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_"
								+ numClusters
								+ "_TrueSGSize_"
								+ clusterSize
								+ "_FeasNum_"
								+ FeaNum + "_trueFeasNum_" + numTrueFeat
								+ "_sigmas1_0.0316_case_"
								+ i
								+ ".txt";
						if (!new File(Paths.get(root, fileName).toString())
								.exists()) {
							System.err.println(fileName);
							continue;
						}
						TestSingleFile(fileName, root, outfile, gamma,
								disRatio, matlabPath);

						// System.out.println("Running Time :" +
						// Utils.roundW(runTime)
						// + " sec....");
					}

				}
				
			}//disRatio
		}//gamma
		double runTime = (System.nanoTime() - startTime) / 1e9;
		System.out.println("Running Time :" + Utils.roundW(runTime)
				+ " sec....");
	}

	public static void FocusCOTest2VaryNumOfCluster() throws NumberFormatException, IOException {
		// VaryingNumOfClusters_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_TrueSGSize_30_FeasNum_100_trueFeasNum_10_sigmas1_0.0316_case_0.txt
		String matlabPath = "F:/workspace/git/S2GraphMP/src/main/java/edu/albany/cs/baselines/FocusCO/FocusCOMatlab/";
		String root = "F:/workspace/git/S2GraphMP/data/DenseGraph/DenseSubgraph_APDM/final/";
		String outroot = "outputs/FocusCOResult/Test2/";
		
		double p_in = 0.35;
		double p_out = 0.10;
		int FeaNum = 100;
		int clusterSize = 30;
		int numTrueFeat = 10;
		long startTime = System.nanoTime();
		for (double gamma : new double[] { 1.0 }) {
			for (double disRatio : new double[] { 2.0 }) {
				for (int numClusters : new int[] { 10, 12, 14, 15, 20, 25 }) {
					String outfile = outroot
							+ "VaryingNumOfClusters_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_"
							+ numClusters + "_TrueSGSize_" + clusterSize
							+ "_FeasNum_" + FeaNum + "_trueFeasNum_"
							+ numTrueFeat + "_sigmas1_0.0316-0.2.txt";

					for (int i = 0; i < 50; i++) {
						String fileName = "VaryingNumOfClusters_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_"
								+ numClusters
								+ "_TrueSGSize_"
								+ clusterSize
								+ "_FeasNum_"
								+ FeaNum + "_trueFeasNum_" + numTrueFeat
								+ "_sigmas1_0.0316_case_"
								+ i
								+ ".txt";
						if (!new File(Paths.get(root, fileName).toString())
								.exists()) {
							System.err.println(fileName);
							continue;
						}
						TestSingleFile(fileName, root, outfile, gamma,
								disRatio, matlabPath);

						// System.out.println("Running Time :" +
						// Utils.roundW(runTime)
						// + " sec....");
					}

				}

			}// disRatio
		}// gamma
		double runTime = (System.nanoTime() - startTime) / 1e9;
		System.out.println("Running Time :" + Utils.roundW(runTime)
				+ " sec....");
	}

	public static void FocusCOTest3VaryClusterSize()
			throws NumberFormatException, IOException {
		// VaryingClusterSizes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_clusterSizeLower_30_clusterSizeUpper_100_FeasNum_100_trueFeasNum_10_sigmas1_0.0316_case_0.txt
		String matlabPath = "F:/workspace/git/S2GraphMP/src/main/java/edu/albany/cs/baselines/FocusCO/FocusCOMatlab/";
		String root = "F:/workspace/git/S2GraphMP/data/DenseGraph/DenseSubgraph_APDM/final/";
		String outroot = "outputs/FocusCOResult/Test3/";

		double p_in = 0.35;
		double p_out = 0.10;
		int FeaNum = 100;
		int clusterSize = 30;
		int numTrueFeat = 10;
		int numClusters = 10;
		int clusterSize_lower = 30;
		long startTime = System.nanoTime();
		for (double gamma : new double[] { 1.0 }) {
			for (double disRatio : new double[] { 2.0 }) {
				for (int clusterSize_up : new int[] { 400 }) {// 100, 150, 200,
																// 300,
					String outfile = outroot
							+ "VaryingClusterSizes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_"
							+ numClusters + "_clusterSizeLower_"
							+ clusterSize_lower + "_clusterSizeUpper_"
							+ clusterSize_up + "_FeasNum_" + FeaNum
							+ "_trueFeasNum_" + numTrueFeat
							+ "_sigmas1_0.0316-0.2.txt";

					for (int i = 0; i < 50; i++) {
						String fileName = "VaryingClusterSizes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_"
								+ numClusters
								+ "_clusterSizeLower_"
								+ clusterSize_lower
								+ "_clusterSizeUpper_"
								+ clusterSize_up
								+ "_FeasNum_"
								+ FeaNum + "_trueFeasNum_" + numTrueFeat
								+ "_sigmas1_0.0316_case_" + i + ".txt";
						if (!new File(Paths.get(root, fileName).toString())
								.exists()) {
							System.err.println(fileName);
							continue;
						}
						TestSingleFile(fileName, root, outfile, gamma,
								disRatio, matlabPath);

						// System.out.println("Running Time :" +
						// Utils.roundW(runTime)
						// + " sec....");
					}

				}
				
			}//disRatio
		}//gamma
		double runTime = (System.nanoTime() - startTime) / 1e9;
		System.out.println("Running Time :" + Utils.roundW(runTime)
				+ " sec....");
	}

	// public static void FocusCOTest4VarySigma() throws NumberFormatException,
	// IOException {
	// //
	// VaryingClusterSizes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_clusterSizeLower_30_clusterSizeUpper_100_FeasNum_100_trueFeasNum_10_sigmas1_0.0316_case_0.txt
	// String matlabPath =
	// "F:/workspace/git/S2GraphMP/src/main/java/edu/albany/cs/baselines/FocusCO/FocusCOMatlab/";
	// String root =
	// "F:/workspace/git/S2GraphMP/data/DenseGraph/DenseSubgraph_APDM/setting4/";
	// String outroot = "outputs/FocusCOResult/setting4-2/";
	//
	// long startTime = System.nanoTime();
	// for (double gamma : new double[] { 0.01 }) {
	// for (double disRatio : new double[] { 1.0 }) {
	// for (String sigma : new String[] { "0.00100" }) {//
	// ,"0.00224","0.00316","0.00707","0.01000","0.03162"
	// // }) {
	// String outfile = outroot
	// +
	// "debugVaryingSigma1_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_TrueSGSize_30_FeasNum_100_trueFeasNum_10_sigmas1_"
	// + sigma + ".txt";
	//
	// for (int i = 400; i < 401; i++) {
	// String fileName =
	// "VaryingSigma1_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_TrueSGSize_30_FeasNum_100_trueFeasNum_10_sigmas1_"
	// + sigma + "_case_" + i + ".txt";
	//
	// if (!new File(Paths.get(root, fileName).toString())
	// .exists()) {
	// System.err.println(fileName);
	// continue;
	// }
	// TestSingleFile(fileName, root, outfile, gamma,
	// disRatio, matlabPath);
	//
	// // System.out.println("Running Time :" +
	// // Utils.roundW(runTime)
	// // + " sec....");
	// }
	//
	// }
	//
	// }// disRatio
	// }// gamma
	// double runTime = (System.nanoTime() - startTime) / 1e9;
	// System.out.println("Running Time :" + Utils.roundW(runTime)
	// + " sec....");
	// }
	public static void FocusCOTest5VaryingFixedClusters() throws NumberFormatException,
	 IOException {
		// VaryingClusterSizes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_clusterSizeLower_30_clusterSizeUpper_100_FeasNum_100_trueFeasNum_10_sigmas1_0.0316_case_0.txt
		String matlabPath = "F:/workspace/git/S2GraphMP/src/main/java/edu/albany/cs/baselines/FocusCO/FocusCOMatlab/";
		String root = "F:/workspace/git/S2GraphMP/data/DenseGraph/DenseSubgraph_APDM/setting5/";
		String outroot = "outputs/FocusCOResult/setting5/";
	
	 long startTime = System.nanoTime();
		for (double gamma : new double[] { 1.0 }) {
			for (double disRatio : new double[] { 2.0 }) {
				for (int clusterSize : new int[] { 250, 300 }) {// 30, 50, 80,
																// 100, 150, 200
					String outfile = outroot
							+ "VaryingFixedClustersSize_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_TrueSGSize_"
							+ clusterSize
							+ "_FeasNum_100_trueFeasNum_10_sigmas1_0.0316.txt";

					for (int i = 1; i < 5; i++) {
						String fileName = "VaryingFixedClustersSize_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_TrueSGSize_"
								+ clusterSize
								+ "_FeasNum_100_trueFeasNum_10_sigmas1_0.0316_case_"
								+ i + ".txt";

						if (!new File(Paths.get(root, fileName).toString())
								.exists()) {
							System.err.println(fileName);
							continue;
						}
						TestSingleFile(fileName, root, outfile, gamma,
								disRatio, matlabPath);

						// System.out.println("Running Time :" +
						// Utils.roundW(runTime)
						// + " sec....");
					}

				}
	 
			}// disRatio
		}// gamma
	 double runTime = (System.nanoTime() - startTime) / 1e9;
	 System.out.println("Running Time :" + Utils.roundW(runTime)
	 + " sec....");
	 }

	public static void main(String[] args) throws NumberFormatException,
			IOException {
		FocusCOTest1VaryNumOfAtt(); // test1
		FocusCOTest2VaryNumOfCluster(); // test2
		FocusCOTest3VaryClusterSize(); // test3

		FocusCOTest5VaryingFixedClusters();
	}

}
