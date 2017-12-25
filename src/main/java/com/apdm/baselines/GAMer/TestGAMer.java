package com.apdm.baselines.GAMer;

import i9.graph.gamer.main.ICDMMain;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import com.apdm.GraphMLProcess.APDMToGraphML;
import com.apdm.base.APDMInputFormat;
import com.apdm.base.Utils;
import com.apdm.scoreFuncs.Stat;
import com.google.common.collect.Sets;

public class TestGAMer {

    // gamer, copam, cocain, seqsubgraph and seqsubspace
	private String methodType = "gamer";

    private void testSingleFile(String fileName, String outFile) {

        APDMInputFormat apdm = new APDMInputFormat(fileName);
        ICDMMain gamer = new ICDMMain();
        // minimum number of nodes returned clique.

        double gamma_min = 0.35D; // return clique gamm(s)>=gamma_min
		int s_min = (int) Math.ceil(apdm.data.trueFeas.length * 0.5); // minimum
																		// feature
        // the maximal extent of a cluster in the attribute space
        double w_max = 0.1D;
        // Q(C)=density(O)^a . |O|^b . |S|^c
        double param_a = 1.0D; // density
        double param_b = 1.0D; // node number
        double param_c = 1.0D; // feature number
        double r_dim = 0.5D;
        double r_obj = 0.5D;

		double attbestPre = 0.0D;
		double attbestRec = 0.0D;
		double attbestFScore = 0.0D;
		double bestPre = 0.0D;
		double bestRec = 0.0D;
		double bestFScore = 0.0D;

		for (double s_ratio : new double[] { 0.1}) {
			int n_min = (int) Math.ceil(apdm.data.trueSubGraphNodes.length
					* s_ratio); // minimum
			System.out.print(s_ratio+" ");																		// feature
        String outputFile = fileName.substring(0, fileName.lastIndexOf("."));
        String propFile = APDMToGraphML.APDM2GraphML(apdm, outputFile, n_min, gamma_min, s_min,
                w_max, param_a, param_b, param_c, r_dim, r_obj);
        int offset = propFile.lastIndexOf(".");
        String resultFile = propFile.substring(0, offset) + "_" + methodType + ".found";
        String[] fargs = new String[2];
        fargs[0] = methodType;
        fargs[1] = propFile;
        gamer.main(fargs);
        int numFeas = apdm.data.numFeas;


               	
			try {
				for (String eachLine : Files
						.readAllLines(Paths.get(resultFile))) {
					if (eachLine.trim().split(" ").length < 2
							|| eachLine.indexOf(":") > 0)
						continue;
					String[] terms = eachLine.split(" ");
					/** Calcualte feature PRF */
					Set<Integer> attRes = new HashSet<>();
					for (int i = 0; i < numFeas; i++) {
						int temp = Integer.valueOf(terms[i]);
						if (temp != 0) {
							attRes.add(i);
					}
				}
					int intersect = Sets.intersection(attRes,
							Stat.Array2Set(apdm.data.trueFeas)).size();
					double[] attPRF = Stat.PRF(intersect, attRes.size(),
							apdm.data.trueFeas.length);
					/** Calculate cluster PRF **/
					Set<Integer> clusterRes = new HashSet<>();
					for (int i = numFeas + 1; i < terms.length; i++) {
						clusterRes.add(Integer.valueOf(terms[i]));
					}
					int clusIntersec = Sets.intersection(clusterRes,
							Stat.Array2Set(apdm.data.trueSubGraphNodes)).size();
					double[] clustPRF = Stat.PRF(clusIntersec,
							clusterRes.size(),
							apdm.data.trueSubGraphNodes.length);
					if (bestFScore < clustPRF[2]) {
						bestPre = clustPRF[0];
						bestRec = clustPRF[1];
						bestFScore = clustPRF[2];
						attbestPre = attPRF[0];
						attbestRec = attPRF[1];
						attbestFScore = attPRF[2];
					}
				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}// file read
		}
		System.out.println();
            try {
                FileWriter output = new FileWriter(outFile, true);
                output.write(bestPre + " " + bestRec + " " + bestFScore + " " + attbestPre + " " + attbestRec + " "
                        + attbestFScore + "\n");
                output.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            System.out.println(methodType + " " + fileName + "\n" + bestPre + " " + bestRec + " "
                    + bestFScore + " " + attbestPre + " " + attbestRec + " " + attbestFScore);

    }

    private void test1VaryNumOfAtt() {
		String root = "data/DenseGraph/DenseSubgraph_APDM/VaryingNumOfAttributes/";
        String outroot = "outputs/GAMerResult/Test1/";
        long startTime = System.nanoTime();
		for (int FeaNum : new int[] { 20, 40, 80, 100 }) {
			String outfile = outroot
					+ "VaryingNumOfAttributes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_TrueSGSize_30_FeasNum_"
					+ FeaNum + "_trueFeasNum_10_sigmas1_0.0316.txt";
			for (int i = 0; i < 50; i++) {
				if(i<4 && FeaNum==20){
					continue;
				}
				String fileName = "VaryingNumOfAttributes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_TrueSGSize_30_FeasNum_"
						+ FeaNum
						+ "_trueFeasNum_10_sigmas1_0.0316_case_"
						+ i
						+ ".txt";
				System.out.println(fileName);
                testSingleFile(root + fileName, outfile);
            }
        }
        double runTime = (System.nanoTime() - startTime) / 1e9;
        System.out.println("Running Time :" + Utils.roundW(runTime) + " sec....");
    }

    private void test2VaryNumOfCluster() {
		String root = "data/DenseGraph/DenseSubgraph_APDM/VaryingNumOfClusters/";
        String outroot = "outputs/GAMerResult/Test2/";
        long startTime = System.nanoTime();

        for (int numClusters : new int[]{20, 25}) {
			String outfile = outroot
					+ "VaryingNumOfClusters_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_"
					+ numClusters
					+ "_TrueSGSize_30_FeasNum_100_trueFeasNum_10_sigmas1_0.0316.txt";

			for (int i = 0; i < 50; i++) {
//				if(i<17 && numClusters==14)
//					continue;
				String fileName = "VaryingNumOfClusters_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_"
						+ numClusters
						+ "_TrueSGSize_30_FeasNum_100_trueFeasNum_10_sigmas1_0.0316_case_"
						+ i + ".txt";
				System.out.println(fileName);
                testSingleFile(root + fileName, outfile);
            }
        }
        double runTime = (System.nanoTime() - startTime) / 1e9;
        System.out.println("Running Time :" + Utils.roundW(runTime) + " sec....");
    }

    private void test3VaryClusterSize() {
		String root ="data/DenseGraph/DenseSubgraph_APDM/VaryingClusterSizes/";
		String outroot = "outputs/FocusCOResult/Test3/";
        long startTime = System.nanoTime();
			for (int clusterSize_up : new int[] { 100,150,200,300,400}) {// 100,150,200,300,400
                    String outfile = outroot +
 "VaryingClusterSizes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_clusterSizeLower_30_clusterSizeUpper_"
							+ clusterSize_up
						+ "_FeasNum_100_trueFeasNum_10_sigmas1_0.0316_0.9.txt";
				for (int i = 0; i < 2; i++) {
					// if (i < 40 && clusterSize_up == 100) {
					// continue;
					// }
                        //Test3_APDM_DenseSubgraph_in_0.35_out_0.1_numClusters_10_clusterLow_30_clusterUp_100_FeasNum_100_trueFeasNum_10_case_1
						String fileName = "VaryingClusterSizes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_clusterSizeLower_30_clusterSizeUpper_"
								+ clusterSize_up
								+ "_FeasNum_100_trueFeasNum_10_sigmas1_0.0316_case_"
								+ i + ".txt";
                        if (!new File(Paths.get(root, fileName).toString()).exists()) {
                            System.err.println(fileName);
                            continue;
                        }
                        System.out.println(fileName);
                        testSingleFile(root + fileName, outfile);
                    }
                }
        
        double runTime = (System.nanoTime() - startTime) / 1e9;
        System.out.println("Running Time :" + runTime + " sec....");
    }
    
    public static void main(String[] args) {
		//new TestGAMer().test1VaryNumOfAtt();
		//new TestGAMer().test2VaryNumOfCluster();
		new TestGAMer().test3VaryClusterSize(); 
    }

}
