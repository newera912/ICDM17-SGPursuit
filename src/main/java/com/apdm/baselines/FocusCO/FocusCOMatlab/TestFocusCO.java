package com.apdm.baselines.FocusCO.FocusCOMatlab;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
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

public class TestFocusCO {

	// TODO Change your workspace root path
	public String root = "/home/apdm01/workspace/git/SG-Pursuit/";

	public double egNodeRatio = 0.9;

	private void TestSingleFile(String fileName, String outroot,
			String outFile, double gamma, double disRatio) {
        try {

			String matlabPath = outroot
					+ "src/main/java/edu/albany/cs/baselines/FocusCO/FocusCOMatlab/";
			String dataFile = outroot + fileName;
            APDMInputFormat apdm = new APDMInputFormat(dataFile);
            Object[] fargs = new Object[5];
            fargs[0] = dataFile; /** input File */
            fargs[1] = Utils.getFileName(dataFile) + ".wEdges";/** output File */

			fargs[2] = getPartArray(apdm.data.trueSubGraphNodes, egNodeRatio);
			/** User input Sample nodes */
            fargs[3] = gamma;/** gamma ,default 1.0 */
            /** Dissimilar Samples Ratio default 2.0*|Sample nodes|*
             * defaultDissimilarSamples = disRatio*size(similar_pairs,2); */
            fargs[4] = disRatio;
            System.out.println("data file name : " + fargs[0]);
			// System.out.println("wEdges file name: " + fargs[1]);
			// System.out.println("sample nodes: " + Arrays.toString((int[])
			// fargs[2]));
			System.out.println("gamma: " + fargs[3] + " example Node= "
					+ egNodeRatio + "% of |s|");
            MatlabProxyFactory factory = new MatlabProxyFactory(new MatlabProxyFactoryOptions.Builder()
                    .setUsePreviouslyControlledSession(true).setHidden(true).setMatlabLocation(null).build());
            MatlabProxy proxy = factory.getProxy();
            proxy.eval("cd(\'" + matlabPath + "\')");
            Object[] matlabReturn = proxy.returningFeval("FocusCO", 1, fargs);
			// System.out.println(ArrayUtils.toString(matlabReturn[0]));
            Object resultObj = matlabReturn[0];
            double[] topAttIndex = ((double[]) resultObj);
            proxy.disconnect();
            /** Calculate attribut Pre, Rec and F-score **/
            int k = apdm.data.trueFeas.length;
            Set<Integer> topAttSet = getTopKValue(topAttIndex, k);
            int attInterSection = Sets.intersection(topAttSet,
                    Utils.intArray2Set(apdm.data.trueFeas)).size();
            double[] attPRF = Stat.PRF(attInterSection, topAttSet.size(), k);
            CommunityClusterer communityClusterer = new CommunityClusterer(0);
            try {
                communityClusterer.processOneFile((String) fargs[1], root,
                        false, "Focus", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
            double bestPre = 0.0D;/** Calcualte best true cluster pre rec **/
            double bestRec = 0.0D;
            double bestFScore = 0.0D;
            for (Set<Integer> res : communityClusterer.clusters) {
                Set<Integer> trueNodesSet = Utils.intArray2Set(apdm.data.trueSubGraphNodes);
                int clusInterSection = Sets.intersection(getJavaIndex(res), trueNodesSet).size();
                double[] tempPRF = Stat.PRF(clusInterSection, res.size(), apdm.data.trueSubGraphNodes.length);
                if (tempPRF[2] > bestFScore) {
                    bestPre = tempPRF[0];
                    bestRec = tempPRF[1];
                    bestFScore = tempPRF[2];
                }
            }
            FileWriter output = null;
            try {
                output = new FileWriter(outFile, true);
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            try {
                output.write(Utils.join(" ", bestPre, bestRec, bestFScore, attPRF[0], attPRF[1], attPRF[2]));
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
			System.out.println(Utils.join(" ", bestPre, bestRec, bestFScore,
					attPRF[0], attPRF[1], attPRF[2]));
        } catch (MatlabInvocationException | MatlabConnectionException e) {
            e.printStackTrace();
        }

    }


    private Set<Integer> getTopKValue(double[] x, int k) {
        Set<Integer> mySet = new HashSet<Integer>();
        for (int i = 0; i < k && i < x.length; i++) {
            mySet.add((int) x[i] - 1); // matlab to java index
        }
        return mySet;
    }

    private int[] getPartArray(int[] array, double ratio) {
        int k = (int) Math.ceil(ratio * array.length);
        if (k < 2) {
            k = 2;
        }
        if (k > array.length) {
            k = array.length;
        }
        int[] randomIdx = new Random().ints(0, array.length).distinct().limit(k).toArray();
        int[] subArray = null;
        for (int i : randomIdx) {
            subArray = ArrayUtils.add(subArray, array[i] + 1);
        }
        return subArray;
    }

    private Set<Integer> getJavaIndex(Set<Integer> S) {
        Set<Integer> matlab2Java = new HashSet<>();
        for (Integer i : S) {
            matlab2Java.add(i - 1);
        }

        return matlab2Java;
    }


    private void focusCOTest1VaryNumOfAtt() {
		String data_root = root
				+ "data/DenseGraph/DenseSubgraph_APDM/VaryingNumOfAttributes/";
		String outroot = root + "outputs/FocusCOResult/Test1/";
        long startTime = System.nanoTime();
        for (double gamma : new double[]{1.0}) {
				for (int FeaNum : new int[] { 20, 40, 80, 100 }) {
				double disRatio = 2.0; // disSimilarRatio=|F|, paper 3.2.1
											// section
					String outfile = outroot
						+ "VaryingNumOfAttributes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_TrueSGSize_30_FeasNum_"
						+ FeaNum + "_trueFeasNum_10_sigmas1_0.0316_0.9.txt";
				for (int i = 0; i < 50; i++) {
						String fileName = "VaryingNumOfAttributes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_TrueSGSize_30_FeasNum_"
								+ FeaNum
								+ "_trueFeasNum_10_sigmas1_0.0316_case_"
								+ i
								+ ".txt";
                        if (!new File(Paths.get(data_root, fileName).toString()).exists()) {
                            System.err.println(fileName);
                            continue;
                        }
                        TestSingleFile(fileName, data_root, outfile, gamma, disRatio);
                    }
                }
        }//gamma
        double runTime = (System.nanoTime() - startTime) / 1e9;
        System.out.println("Running Time :" + runTime + " sec....");
    }

    private void focusCOTest2VaryNumOfCluster() {
		String data_root = root
				+ "data/DenseGraph/DenseSubgraph_APDM/VaryingNumOfClusters/";
		String outroot = root + "outputs/FocusCOResult/Test2/";
        long startTime = System.nanoTime();
        for (double gamma : new double[]{1.0}) {
                for (int numClusters : new int[]{10, 12, 14, 15, 20, 25}) {
				double disRatio = 2.0;
					String outfile = outroot
						+ "VaryingNumOfClusters_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_"
							+ numClusters
						+ "_TrueSGSize_30_FeasNum_100_trueFeasNum_10_sigmas1_0.0316_0.9.txt";
				for (int i = 0; i < 50; i++) {
						String fileName = "VaryingNumOfClusters_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_"
								+ numClusters
								+ "_TrueSGSize_30_FeasNum_100_trueFeasNum_10_sigmas1_0.0316_case_"
								+ i + ".txt";
                        if (!new File(Paths.get(data_root, fileName).toString()).exists()) {
                            System.err.println(Paths.get(data_root, fileName).toString());
                            continue;
                        }
                        TestSingleFile(fileName, data_root, outfile, gamma, disRatio);
                    }
                }
        }// gamma
        double runTime = (System.nanoTime() - startTime) / 1e9;
        System.out.println("Running Time :" + runTime + " sec....");
    }

    private void focusCOTest3VaryClusterSize() {
		String data_root = root
				+ "data/DenseGraph/DenseSubgraph_APDM/VaryingClusterSizes/";
		String outroot = root + "outputs/FocusCOResult/Test3/";
        long startTime = System.nanoTime();
        for (double gamma : new double[]{1.0}) {
			for (int clusterSize_up : new int[] { 100, 150, 200, 300, 400 }) {
				double disRatio = 2.0;
                    String outfile = outroot +
 "VaryingClusterSizes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_clusterSizeLower_30_clusterSizeUpper_"
							+ clusterSize_up
						+ "_FeasNum_100_trueFeasNum_10_sigmas1_0.0316_0.9.txt";
				for (int i = 0; i < 50; i++) {
					String fileName = "VaryingClusterSizes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_clusterSizeLower_30_clusterSizeUpper_"
								+ clusterSize_up
								+ "_FeasNum_100_trueFeasNum_10_sigmas1_0.0316_case_"
								+ i + ".txt";
					if (!new File(Paths.get(data_root, fileName).toString())
							.exists()) {
						System.err.println(fileName);
						continue;
                    }
					TestSingleFile(fileName, data_root, outfile, gamma,
							disRatio);
				}
			}
        }//gamma
        double runTime = (System.nanoTime() - startTime) / 1e9;
        System.out.println("Running Time :" + runTime + " sec....");
    }


    private void focusCOTest5VaryingFixedClusters() {
		String root = "/home/apdm01/workspace/git/SG-Pursuit/data/DenseGraph/DenseSubgraph_APDM/setting5/";
		String outroot = "/home/apdm01/workspace/git/SG-Pursuit/outputs/FocusCOResult/setting5/";
        long startTime = System.nanoTime();
        for (double gamma : new double[]{1.0}) {
                for (int clusterSize : new int[]{30, 50, 80, 100, 150, 200,250, 300}) {// 30, 50, 80, 100, 150, 200
				double disRatio = 2.0;
				String outfile = outroot
                            + "VaryingFixedClustersSize_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_TrueSGSize_"
                            + clusterSize + "_FeasNum_100_trueFeasNum_10_sigmas1_0.0316.txt";
                    for (int i = 0; i < 50; i++) {
                        String fileName = "VaryingFixedClustersSize_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_TrueSGSize_"
                                + clusterSize + "_FeasNum_100_trueFeasNum_10_sigmas1_0.0316_case_" + i + ".txt";
                        if (!new File(Paths.get(root, fileName).toString()).exists()) {
                            System.err.println(fileName);
                            continue;
                        }
                        TestSingleFile(fileName, root, outfile, gamma, disRatio);
                    }
                }
        }// gamma
        double runTime = (System.nanoTime() - startTime) / 1e9;
        System.out.println("Running Time :" + runTime + " sec....");
    }

    public static void main(String[] args) {
		// new TestFocusCO().focusCOTest1VaryNumOfAtt(); // test1
		// new TestFocusCO().focusCOTest2VaryNumOfCluster(); // test2
		new TestFocusCO().focusCOTest3VaryClusterSize(); // test3
        // focusCOTest5VaryingFixedClusters();
    }

}
