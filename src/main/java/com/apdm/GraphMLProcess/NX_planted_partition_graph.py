import networkx as nx
import os

# G = nx.planted_partition_graph(3,20,.8,.01)
# for node in G.nodes():
#     print node
# for edge in G.edges():
#     print edge[0],edge[1],len(G.edges())
# partition = G.graph['partition']
# for p in partition:
#     print str(list(p))
    
N=300
m=[20]
cluster_node_num=30
p_in=0.35
p_out=0.1
for N in [300, 500, 1000, 1200, 1500, 1700,2000,2500, 3000, 3500, 4000, 4500, 5000, 5500, 6000]: #, 2500, 3000, 3500, 4000, 4500, 5000, 5500, 6000
    for mm in m:
        mm=N/cluster_node_num
        for i in range(0,1):
            root="F:/workspace/git/S2GraphMP/data/DenseGraph/raw/rawData"+str(N)
            adpmRoot="F:/workspace/git/S2GraphMP/data/DenseGraph/Dense_APDM/cluster"+str(N)
            if not os.path.exists(root):
                os.makedirs(root)
            if not os.path.exists(adpmRoot):
                os.makedirs(adpmRoot)
            root="F:/workspace/git/S2GraphMP/data/DenseGraph/raw/rawGAMer"
            rawFile=open(root+"/"+str(N)+"_Cluster_"+str(mm)+"_in_"+str(p_in)+"_out_"+str(p_out)+"_15_case_"+str(i)+".txt","w")
            G = nx.planted_partition_graph(mm,cluster_node_num,p_in,p_out)
            print "Edge Num: ",len(G.edges())
            print "Node Num: ",len(G.nodes())
            for edge in G.edges():
                rawFile.write(str(edge[0])+" "+str(edge[1])+"\n")
            partition = G.graph['partition']
            for p in partition:            
                rawFile.write(" ".join(map(str, list(p)))+"\n")
            rawFile.close()
        
# nx.draw(G)
# plt.draw()
# plt.show()