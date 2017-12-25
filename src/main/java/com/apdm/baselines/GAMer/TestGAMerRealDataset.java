package com.apdm.baselines.GAMer;

import i9.graph.gamer.graph.Graph;
import i9.graph.gamer.graph.Node;
import i9.graph.gamer.graphReader.GraphReader;
import i9.graph.gamer.main.ICDMMain;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.StatUtils;

import com.apdm.base.Matrix;

public class TestGAMerRealDataset {

	// gamer, copam, cocain, seqsubgraph and seqsubspace
	private String methodType = "gamer";


	private void testRealFiles(String graphMLPFile) {
		System.out.println(graphMLPFile.substring(0,
				graphMLPFile.lastIndexOf("."))
				+ ".properties");
		log(graphMLPFile);
		ICDMMain gamer = new ICDMMain();
		// minimum number of nodes returned clique.
		String propFile = graphMLPFile.substring(0,
				graphMLPFile.lastIndexOf("."))
				+ ".properties";

		/** Load GraphML file **/
		Graph myGraph = GraphReader.loadGraphFromGraphMLFile(graphMLPFile);
		int numFeas = myGraph.getNumberOfAtts();
		int N = myGraph.getNodes().size();

		System.out.println("No. Nodes=" + N + " No. Features=" + numFeas);
		log("No. Nodes=" + N + " No. Features=" + numFeas);
		/** GAMer parameters */
		double gamma_min = 0.5D; // return clique gamm(s)>=gamma_min
		int s_min = 2; // minimum feature
		int n_min = 4; // minimum cluster size
		// the maximal extent of a cluster in the attribute space
		double w_max = 10.0;
		// Q(C)=density(O)^a . |O|^b . |S|^c
		double param_a = 1.0D; // density
		double param_b = 1.0D; // node number
		double param_c = 1.0D; // feature number
		double r_dim = 0.1D;
		double r_obj = 0.1D;
		FileWriter output = null;
		FileWriter outProp = null;
		try {

			outProp = new FileWriter(propFile, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/** Write the property file */
		try {
			outProp.write("filename = " + graphMLPFile + "\n \n");
			outProp.write("n_min = " + n_min + "\n");
			outProp.write("gamma_min = " + gamma_min + "\n");
			outProp.write("s_min = " + s_min + "\n");
			outProp.write("w_max = " + w_max + "\n\n");

			outProp.write("param_a = " + param_a + "\n");
			outProp.write("param_b = " + param_b + "\n");
			outProp.write("param_c = " + param_c + "\n");
			outProp.write("r_obj = " + r_obj + "\n");
			outProp.write("r_dim = " + r_dim + "\n");

			outProp.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println(propFile);
		int offset = propFile.lastIndexOf(".");
		String resultFile = propFile.substring(0, offset) + "_" + methodType
				+ ".found";
		System.out.println(resultFile);
		String[] fargs = new String[2];
		fargs[0] = methodType;
		fargs[1] = propFile;
		gamer.main(fargs);

		ArrayList<ArrayList<Integer>> clusterRes = new ArrayList<ArrayList<Integer>>();
		try {
			for (String eachLine : Files.readAllLines(Paths.get(resultFile))) {
				System.out.println(eachLine);
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

				/** Calculate cluster PRF **/
				ArrayList<Integer> tempRes = new ArrayList<Integer>();
				for (int i = numFeas + 1; i < terms.length; i++) {
					tempRes.add(Integer.valueOf(terms[i]));
				}
				clusterRes.add(tempRes);
			}// readfile
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// file read
		if (clusterRes.size() > 0) {
			System.out.println(clusterRes.get(0));
		} else {
			System.out.println("No result!!!!!");
			return;
		}

		/*************************************************************
		 * \
		 * 
		 * Read GraphML file, generate adj Matrix \*
		 ***********************************************************/


		int nodeIndex = 0;
		HashMap<Integer, Integer> originId2NodeID = new HashMap<Integer, Integer>(); // index
																						// dictionary
		double[][] adjMatrix = new double[N][N];
		double[][] data = new double[N][numFeas];
		/** 1. Indexing the nodes */
		for (Iterator<Node> it = myGraph.getNodes().iterator(); it.hasNext();) {
			int originID = ((Node) it.next()).getID();
			originId2NodeID.put(originID, nodeIndex);
			nodeIndex++;
		}

		nodeIndex = 0;

		/** 2. Generate Adjacency matrix **/
		for (Iterator<Node> it = myGraph.getNodes().iterator(); it.hasNext();) {
			Node node = (Node) it.next();
			HashSet<Node> neighNodes = node.getNeighbors();
			int origId = node.getID();
			int currNodeID = originId2NodeID.get(node.getID());
			/** Get adjcent List, generate Edges */
			int neighbNum = neighNodes.size();
			if (neighbNum > 0) {
				int neighbOrignId;
				int nNodeId;
				for (Iterator<Node> nit = neighNodes.iterator(); nit.hasNext();) {
					neighbOrignId = originId2NodeID.get(((Node) nit.next())
							.getID());
					adjMatrix[currNodeID][neighbOrignId] = 1.0;
					adjMatrix[neighbOrignId][currNodeID] = 1.0;
				}
			}// if

			/** 3.Generate Attribute Matrix */
			int attIdx = 0;
			NormalDistribution norm = new NormalDistribution(200, 50);
			for (double d : node.getAttributes()) {
				if (Double.isNaN(d))
					d = norm.sample();
				data[currNodeID][attIdx] = d;
				attIdx++;
			}
			System.out.println(attIdx);
			// System.out.println(nodeIndex + " " +
			// node.getID()+" "+neighNodes);//+" "+ArrayUtils.toString(node.getAttributes()));
			// System.out.println();

			nodeIndex++;
		}// for

		/** Calculate Normalized cut for top 1 result ***/
		double nCut = 0.0D;
		double[] I = Matrix.identityVector(N);
		double tempNCut = 100000.0D;
		Set<Integer> tempRes = new HashSet<Integer>();
		for (int j = 0; j < clusterRes.size(); j++) {
			Set<Integer> topRes = new HashSet<Integer>();
			for (int i : clusterRes.get(j)) {
				topRes.add(originId2NodeID.get(i));
			}

			double[] a1 = indicateVector(topRes, N); // top 1 results indicatr
														// vector
			double[] a1C = Matrix.VecSubstract(I, a1);
			double aW1_a = Matrix
					.dot(Matrix.VecMultiplyMat(a1, adjMatrix), a1C);
			double aw1 = Matrix.dot(Matrix.VecMultiplyMat(a1, adjMatrix), I);
			nCut = aW1_a / aw1;
			System.out.println("Top" + j + " " + aW1_a + " " + aw1
					+ " Normalzie Cut: " + nCut + " " + topRes.toString());
			if (nCut < tempNCut) {
				tempNCut = nCut;
				tempRes = topRes;
			}

			if (j == 0) {
				System.out.println(aW1_a + " " + aw1 + " Normalzie Cut: "
						+ nCut + " " + topRes.toString());
				log(aW1_a + " " + aw1 + " Normalzie Cut: " + nCut + " "
						+ topRes.toString());
			}

			if (j == clusterRes.size() - 1) {
				System.out.println(aW1_a + " " + aw1
						+ " Smallest Normalzie Cut: " + tempNCut + " "
						+ tempRes.toString());
				log(aW1_a + " " + aw1 + " Smallest Normalzie Cut: " + tempNCut
						+ " " + tempRes.toString());
			}

		}
	}

	private void testRealFilesOn3Measure(String graphMLPFile, String dataset,
			int nmin, int smin, double gamma, double w) {
		System.out.println(graphMLPFile.substring(0,
				graphMLPFile.lastIndexOf("."))
				+ ".properties");
		log(graphMLPFile);
		ICDMMain gamer = new ICDMMain();
		// minimum number of nodes returned clique.
		String propFile = graphMLPFile.substring(0,
				graphMLPFile.lastIndexOf("."))
				+ ".properties";

		/** Load GraphML file **/
		Graph myGraph = GraphReader.loadGraphFromGraphMLFile(graphMLPFile);
		int numFeas = myGraph.getNumberOfAtts();
		int N = myGraph.getNodes().size();

		// System.out.println("No. Nodes=" + N + " No. Features=" + numFeas);
		log("No. Nodes=" + N + " No. Features=" + numFeas);
		/** GAMer parameters */
		double gamma_min = gamma; // return clique gamm(s)>=gamma_min
		int s_min = smin; // minimum feature
		int n_min = nmin; // minimum cluster size
		// the maximal extent of a cluster in the attribute space
		double w_max = w;
		// Q(C)=density(O)^a . |O|^b . |S|^c
		double param_a = 1.0D; // density
		double param_b = 1.0D; // node number
		double param_c = 1.0D; // feature number
		double r_dim = 0.1D;
		double r_obj = 0.1D;
		FileWriter output = null;
		FileWriter outProp = null;
		try {

			outProp = new FileWriter(propFile, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		/** Write the property file */
		try {
			outProp.write("filename = " + graphMLPFile + "\n \n");
			outProp.write("n_min = " + n_min + "\n");
			outProp.write("gamma_min = " + gamma_min + "\n");
			outProp.write("s_min = " + s_min + "\n");
			outProp.write("w_max = " + w_max + "\n\n");

			outProp.write("param_a = " + param_a + "\n");
			outProp.write("param_b = " + param_b + "\n");
			outProp.write("param_c = " + param_c + "\n");
			outProp.write("r_obj = " + r_obj + "\n");
			outProp.write("r_dim = " + r_dim + "\n");

			outProp.close();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// System.out.println(propFile);
		int offset = propFile.lastIndexOf(".");
		String resultFile = propFile.substring(0, offset) + "_" + methodType
				+ ".found";
		// System.out.println(resultFile);
		String[] fargs = new String[2];
		fargs[0] = methodType;
		fargs[1] = propFile;
		gamer.main(fargs);

		ArrayList<SubSpace> clusterRes = new ArrayList<SubSpace>();
		try {
			for (String eachLine : Files.readAllLines(Paths.get(resultFile))) {
				// System.out.println(eachLine);
				if (eachLine.trim().split(" ").length < 2
						|| eachLine.indexOf(":") > 0)
					continue;
				String[] terms = eachLine.split(" ");

				Set<Integer> attRes = new HashSet<>();
				for (int i = 0; i < numFeas; i++) {
					int temp = Integer.valueOf(terms[i]);
					if (temp != 0) {
						attRes.add(i);
					}
				}

				/** Calculate cluster PRF **/
				Set<Integer> nodeRes = new HashSet<>();
				for (int i = numFeas + 1; i < terms.length; i++) {
					nodeRes.add(Integer.valueOf(terms[i]));
				}
				clusterRes.add(new SubSpace(nodeRes, attRes));
			}// readfile
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}// file read
		if (clusterRes.size() > 0) {
			System.out.println("Have result!!!");
		} else {
			System.out.println("No result!!!!!");
			return;
		}
		if (clusterRes.size() < 10) {
			return;
		}
		/*************************************************************
		 * \
		 * 
		 * Read GraphML file, generate adj Matrix \*
		 ***********************************************************/



		int nodeIndex = 0;
		HashMap<Integer, Integer> originId2NodeID = new HashMap<Integer, Integer>(); // index
																						// dictionary
		double[][] adjMatrix = new double[N][N];
		double[][] data = new double[N][numFeas];
		/** 1. Indexing the nodes */
		for (Iterator<Node> it = myGraph.getNodes().iterator(); it.hasNext();) {
			int originID = ((Node) it.next()).getID();
			originId2NodeID.put(originID, nodeIndex);
			nodeIndex++;
		}

		nodeIndex = 0;

		/** 2. Generate Adjacency matrix **/
		for (Iterator<Node> it = myGraph.getNodes().iterator(); it.hasNext();) {
			Node node = (Node) it.next();
			HashSet<Node> neighNodes = node.getNeighbors();
			int origId = node.getID();
			int currNodeID = originId2NodeID.get(node.getID());
			/** Get adjcent List, generate Edges */
			int neighbNum = neighNodes.size();
			if (neighbNum > 0) {
				int neighbOrignId;
				int nNodeId;
				for (Iterator<Node> nit = neighNodes.iterator(); nit.hasNext();) {
					neighbOrignId = originId2NodeID.get(((Node) nit.next())
							.getID());
					adjMatrix[currNodeID][neighbOrignId] = 1.0;
					adjMatrix[neighbOrignId][currNodeID] = 1.0;
				}
			}// if

			/** 3.Generate Attribute Matrix */
			int attIdx = 0;
			// NormalDistribution norm = new NormalDistribution(50, 10);
			for (double d : node.getAttributes()) {
//				if (Double.isNaN(d))
//					d = norm.sample();
				data[currNodeID][attIdx] = d;
				attIdx++;
			}
			// System.out.println(nodeIndex + " " +
			// node.getID()+" "+neighNodes);//+" "+ArrayUtils.toString(node.getAttributes()));
			// System.out.println();

			nodeIndex++;
		}// for

		/********************************************************
		 * Calculate : 1. Node density: x^TAx/1^Tx 2. Attribute coherence: Sum
		 * of Euclideant 3. Cluster size
		 ********************************************************/

		List<Integer> c_num = (List<Integer>) Arrays.asList(5);// , 10, 15, 20);
		for (int t : c_num) {
			double avg_density = 0.0D;
			double avg_clusterSize = 0.0D;
			double avg_euclidean = 0.0D;

			double temp_density = 0.0D;
			double temp_clusterSize = 0.0D;
			double temp_euclidean_dist = 0.0D;
			int TOPK = t;
			int top = TOPK;
			ArrayList<Integer> tempRes = new ArrayList<Integer>();
			for (SubSpace cluster : clusterRes) {
				if (top < 1) {
					break;
				}
				top--;
				Set<Integer> topRes = new HashSet<Integer>();
				for (int i : cluster.x) {
					topRes.add(originId2NodeID.get(i));
				}

				double[] xx = indicateVector(topRes, N); // top 1 results
															// indicator
															// vector

				double density = (1.0 * Matrix.dot(
						Matrix.VecMultiplyMat(xx, adjMatrix), xx))
						/ StatUtils.sum(xx);

				temp_density += density;

				temp_clusterSize += cluster.x.size();

				Set<ArrayList<Integer>> pairs = new HashSet<ArrayList<Integer>>();
				for (int i = 0; i < cluster.x.size() - 1; i++) {
					for (int j = i + 1; j < cluster.x.size(); j++) {
						ArrayList<Integer> temp = new ArrayList<Integer>();
						temp.add(originId2NodeID.get(cluster.x.get(i)));
						temp.add(originId2NodeID.get(cluster.x.get(j)));
						pairs.add(temp);
					}
				}
				double tempSum = 0.0D;
				for (ArrayList<Integer> pair : pairs) {
					// System.out.println(ArrayUtils.toString(data[pair.get(0)]));
					// System.out.println(ArrayUtils.toString(data[pair.get(1)]));
					double dist = EuclideanDist(data[pair.get(0)],
							data[pair.get(1)], cluster.y);
					tempSum += dist;

				}
				temp_euclidean_dist += tempSum / pairs.size();

			}

			if (TOPK > clusterRes.size()) {
				TOPK = clusterRes.size();
			}
			// 1. average density
			avg_density = temp_density / TOPK;
			// 2. average cluster size
			avg_clusterSize = temp_clusterSize / TOPK;
			// 3. average euclidean distance
			avg_euclidean = temp_euclidean_dist / TOPK;
			log(dataset + " " + n_min + " " + s_min + " " + gamma + " " + w
					+ " " + avg_density + " " + avg_clusterSize + " "
					+ avg_euclidean + "\n", dataset);
			System.out.println("Top" + t + "/" + TOPK + " "
					+ round(avg_density, 2) + " " + round(avg_clusterSize, 2)
					+ " " + round(avg_euclidean, 2));
		}
	}

	private double EuclideanDist(double[] ds1, double[] ds2,
			ArrayList<Integer> y) {
		// TODO Auto-generated method stub
		double eDist = 0.0D;
		for (int i : y) {
//			 System.out.println(ds1[i] + "-" + ds2[i] + " " + Math.abs(ds1[i] - ds2[i]));
			eDist += Math.pow(Math.abs(ds1[i] - ds2[i]), 2);
		}
		return Math.sqrt(eDist);
	}

	public double[] indicateVector(Set<Integer> a, int n) {
		double[] x = new double[n];
		for (int i : a) {
			x[i] = 1.0;
		}

		return x;
	}

	public static void log(String line, String dataset) {
		FileWriter fstream;
		// System.out.println(line);
		try {
			fstream = new FileWriter("data/DenseGraph/Log/log_real_" + dataset
					+ ".txt", true);
			fstream.write(line);
			fstream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void log(String line) {
		FileWriter fstream;
		// System.out.println(line);
		try {
			fstream = new FileWriter("data/DenseGraph/Log/log_real.txt", true);
			fstream.write(line + "\r\n");
			fstream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	private void testRealdatasets() {
		ExecutorService pool = Executors.newFixedThreadPool(50);
		String root = "data/DenseGraph/Realdata/";
		String outroot = "outputs/GAMerResult/";

		// "dblp", "dfb", "genes", "imdb","arxivSmall", "patents", "arxivLarge"
		for (String dataset : new String[] { "dblp", "dfb", "genes", "imdb",
				"arxivSmall" }) {
			for (int n_min : new int[] { 2, 3, 4, 5, 10, 15, 20 }) {
				for (int s_min : new int[] { 2, 3, 4, 5, 10, 15, 20, 25,
						30 }) {
					for (double gamma : new double[] { 0.3, 0.4, 0.5, 0.6, 0.7,
							0.8, 0.9 }) {
						for (double w : new double[] { 0.01, 0.1, 1.0, 5.0,
								10.0, 50.0, 100.0 }) {
							String fileName = root + dataset + ".graphml";
							pool.execute(new Thread() {
								public void run() {
							System.out.println(fileName);

							testRealFilesOn3Measure(fileName, dataset, n_min,
									s_min, gamma, w);
								}
							});
						}// w
					}// gamma
				}// smin
			}// nmin
		}// dataset
		pool.shutdown();
	}
	public static double round(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();

		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	/**
	 * Subspace result class, two sets: 1. x: subset of nodes and 2. y: subset
	 * of attributes
	 */
	public static class SubSpace {
		public ArrayList<Integer> x = new ArrayList<Integer>();
		public ArrayList<Integer> y = new ArrayList<Integer>();

		public SubSpace(Set<Integer> x_star, Set<Integer> y_star) {
			for (Integer xi : x_star) {
				x.add(xi);
			}
			for (Integer yi : y_star) {
				y.add(yi);
			}
		}
	}

	public static void main(String[] args) {
		new TestGAMerRealDataset().testRealdatasets();
	}

}
