package com.apdm.GraphMLProcess;

import i9.graph.gamer.graph.Graph;
import i9.graph.gamer.graph.Node;
import i9.graph.gamer.graphReader.GraphReader;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.commons.math3.distribution.NormalDistribution;

import com.apdm.base.APDMInputFormat;
import com.apdm.base.Edge;

public class GraphMLToAPDM {

	public GraphMLToAPDM(String gmlFile, String apdmFile) {

		Graph myGraph = GraphReader.loadGraphFromGraphMLFile(gmlFile);
	    //i9.graph.gamer.base.Parameter.numberOfAtts = myGraph.getNumberOfAtts();
	    
	    int numOfNodes=myGraph.getNodes().size();
	    int numFestures=myGraph.getNumberOfAtts();
	    
	    HashMap<Integer, Integer> originId2NodeID= new HashMap<Integer, Integer>();
	    ArrayList<Edge> edges= new ArrayList<Edge>();
		String usedAlgorithm = "NULL";
		String dataSource = "GraphMLDataset";
		String[] attributeNames=new String[numFestures];
		for(int i=0;i<numFestures;i++){
			attributeNames[i]="att"+i;
		}
		//GenerateSingleGrid g = new GenerateSingleGrid(numOfNodes);
		
		double[][] data = new double[numFestures + 1][numOfNodes];
		
	    int nodeIndex=0;
	    /**Indexing the nodes */
	    for (Iterator<Node> it = myGraph.getNodes().iterator(); it.hasNext();)
	    {
	    	originId2NodeID.put(((Node)it.next()).getID(), nodeIndex);
	    	nodeIndex++;
		}
	    nodeIndex=0;
	    int edgeIndex=0;
	    double weight=1.0D;
	    for (Iterator<Node> it = myGraph.getNodes().iterator(); it.hasNext();)
	    {
	      Node node = (Node)it.next();
	      HashSet<Node> neighNodes=node.getNeighbors();
	      int origId=node.getID();
	      int currNodeID=originId2NodeID.get(node.getID());
	      /**Get adjcent List, generate Edges */
	      int neighbNum=neighNodes.size();
	      if(neighbNum>0){
	    	  int neighbOrignId;
	    	  int nNodeId;
		      for (Iterator<Node> nit = neighNodes.iterator(); nit.hasNext();) {
					neighbOrignId = originId2NodeID.get(((Node) nit.next())
							.getID());
						edges.add(new Edge(currNodeID, neighbOrignId, edgeIndex++, weight));
					}
		   }
	      	data[0][currNodeID]=origId;
			int attIdx = 1;

			for (double d : node.getAttributes()) {
				if (Double.isNaN(d))
					d = Double.NaN;
			   data[attIdx][currNodeID]=d;
			   attIdx++;
		   }
			// System.out.println(nodeIndex + " " +
			// node.getID()+" "+neighNodes);//+" "+ArrayUtils.toString(node.getAttributes()));
	    	//System.out.println();
	    	
	     
	    	nodeIndex++;
	    }
		// NormalDistribution norm = new NormalDistribution(0, 1);
		// for (int i = 1; i < data.length; i++) {
		// double nomrcol = normalizeCol(data, i);
		// for (int j = 0; j < data[0].length; j++) {
		// if (!Double.isNaN(data[i][j])) {
		// data[i][j] = data[i][j] / nomrcol;
		// } else {
		// data[i][j] = norm.sample();
		// }
		//
		// }
		// }

		NormalDistribution norm = new NormalDistribution(0, 1);
		for (int i = 1; i < data.length; i++) {
			double[] minmax = colMinMax(data, i);
			double min = minmax[0];
			double max = minmax[1];
			for (int j = 0; j < data[0].length; j++) {
				if (!Double.isNaN(data[i][j])) {
					data[i][j] = (data[i][j] - min) / (max - min);
				} else {
					data[i][j] = norm.sample();
				}

			}
		}
	    System.out.println("Generatign APDM file..... "+edges.size());
		APDMInputFormat.generateAPDMFile(usedAlgorithm, dataSource, edges,
				null, apdmFile, attributeNames, numFestures, "", data);
	    System.out.println("Done.....");
	}
	
	public GraphMLToAPDM(String gmlFile, String apdmFile, String method) {

		Graph myGraph = GraphReader.loadGraphFromGraphMLFile(gmlFile);
		// i9.graph.gamer.base.Parameter.numberOfAtts =
		// myGraph.getNumberOfAtts();

		int numOfNodes = myGraph.getNodes().size();
		int numFestures = myGraph.getNumberOfAtts();

		HashMap<Integer, Integer> originId2NodeID = new HashMap<Integer, Integer>();
		ArrayList<Edge> edges = new ArrayList<Edge>();
		String usedAlgorithm = "NULL";
		String dataSource = "GraphMLDataset";
		String[] attributeNames = new String[numFestures];
		for (int i = 0; i < numFestures; i++) {
			attributeNames[i] = "att" + i;
		}
		// GenerateSingleGrid g = new GenerateSingleGrid(numOfNodes);

		double[][] data = new double[numFestures + 1][numOfNodes];

		int nodeIndex = 0;
		/** Indexing the nodes */
		for (Iterator<Node> it = myGraph.getNodes().iterator(); it.hasNext();) {
			originId2NodeID.put(((Node) it.next()).getID(), nodeIndex);
			nodeIndex++;
		}
		nodeIndex = 0;
		int edgeIndex = 0;
		double weight = 1.0D;
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
					edges.add(new Edge(currNodeID, neighbOrignId, edgeIndex++,
							weight));
				}
			}
			data[0][currNodeID] = origId;
			int attIdx = 1;

			for (double d : node.getAttributes()) {
				if (Double.isNaN(d))
					d = 0.0;
				data[attIdx][currNodeID] = d;
				attIdx++;
			}
			// System.out.println(nodeIndex + " " +
			// node.getID()+" "+neighNodes);//+" "+ArrayUtils.toString(node.getAttributes()));
			// System.out.println();

			nodeIndex++;
		}
		NormalDistribution norm = new NormalDistribution(0, 1);
		for (int i = 1; i < data.length; i++) {
			double nomrcol = normalizeCol(data, i);
			for (int j = 0; j < data[0].length; j++) {
				if (!Double.isNaN(data[i][j])) {
					data[i][j] = data[i][j] / nomrcol;
				} else {
					data[i][j] = norm.sample();
				}

			}
		}

		// NormalDistribution norm = new NormalDistribution(0, 1);
		// for (int i = 1; i < data.length; i++) {
		// double[] minmax = colMinMax(data, i);
		// double min = minmax[0];
		// double max = minmax[1];
		// for (int j = 0; j < data[0].length; j++) {
		// if (!Double.isNaN(data[i][j])) {
		// data[i][j] = (data[i][j] - min) / (max - min);
		// } else {
		// data[i][j] = norm.sample();
		// }
		//
		// }
		// }
		System.out.println("Generatign APDM file..... " + edges.size());
		APDMInputFormat.generateAPDMFile(usedAlgorithm, dataSource, edges,
				null, apdmFile, attributeNames, numFestures, "", data);
		System.out.println("Done.....");
	}
	private double normalizeCol(double[][] data, int iCol) {
		double colSum = 0.0D;
		for (int i = 0; i < data.length; i++) {
			if (!Double.isNaN(data[i][iCol])) {
				colSum += data[i][iCol] * data[i][iCol];
			}
		}
		// TODO Auto-generated method stub
		return Math.sqrt(colSum);
	}

	private double[] colMinMax(double[][] data, int iCol) {
		double colMin = Double.POSITIVE_INFINITY;
		double colMax = Double.NEGATIVE_INFINITY;
		double[] minmax = new double[2];
		for (int i = 0; i < data.length; i++) {
			if (!Double.isNaN(data[i][iCol])) {
				if (data[i][iCol] < colMin) {
					colMin = data[i][iCol];
				} else if (data[i][iCol] > colMax) {
					colMax = data[i][iCol];
				}
			}
		}
		minmax[0] = colMin;
		minmax[1] = colMax;
		// TODO Auto-generated method stub
		return minmax;
	}


	public static void singleGraphML() {
		String root = "data/DenseGraph/Realdata/";
		String rootApdm = "data/DenseGraph/Realdata/apdm/";
		String gmlFile = "dfb.graphml";

		String apdmFile = rootApdm
				+ gmlFile.replace(".graphml", "_norm_APDM.txt");
		GraphMLToAPDM gml2Apdm = new GraphMLToAPDM(root + gmlFile, apdmFile,
				"arxiv");

	}

	public static void main(String args[]) throws FileNotFoundException{
		// bio arxiv99-03 dblp_top patent_91-95
		// String root = "data/DenseGraph/Realdata/";
		// String rootApdm = "data/DenseGraph/Realdata/apdm/";
		//
		// for (File rawFile : new File(root).listFiles()) {
		// String gmlFile = rawFile.getName();
		// if (!gmlFile.toLowerCase().endsWith(".graphml")) {
		// continue;
		// }
		//
		// String apdmFile = rootApdm
		// + gmlFile.substring(0, gmlFile.lastIndexOf("."))
		// + "_norm_APDM.txt";
		// GraphMLToAPDM gml2Apdm = new GraphMLToAPDM(root + gmlFile, apdmFile);
		// }
		
		singleGraphML();
	}
}
