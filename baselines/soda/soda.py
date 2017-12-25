__author__ = 'fengchen'
from cvxopt import matrix, solvers
import itertools
import logging
import random
 
 
from collections import defaultdict
 
def read_APDM_data(path):
    data=[]
    nodes=[]
    adjList = defaultdict(list)
    trueNodes=[]
    trueFea=[]
    lines = open(path).readlines()
    n = -1
    for idx, line in enumerate(lines):
        if line.strip().startswith('truth_features'):
            trueFea=map(int,str(line.strip().split('=')[1]).split())
            n = idx + 5
            break
 
    for idx in range(n, len(lines)):
        # print lines[idx]
        line = lines[idx]
        if line.find('END') >= 0:
            n = idx + 4
            break
        else:
            items = line.split(' ')
            nodes.append(int(items[0]))
            data.append(map(float,items[1:]))
 
    for idx in range(n, len(lines)):
        line = lines[idx]
        if line.find('END') >= 0:
            n = idx + 4
            break
        else:
            vertices = line.split(' ')
            edge=[0]*2
            edge[0] = int(vertices[0])
            edge[1] = int(vertices[1])
 
            if edge[1] not in adjList[edge[0]]:
                adjList[edge[0]].append(edge[1])
            if edge[0] not in adjList[edge[1]]:
                adjList[edge[1]].append(edge[0])
 
    true_subgraph = []
    for idx in range(n, len(lines)):
        line = lines[idx]
        if line.find('END') >= 0:
            break
        else:
            items = line.split(' ')
            true_subgraph.append(int(items[0]))
            true_subgraph.append(int(items[1]))
    true_subgraph = sorted(list(set(true_subgraph)))
    return nodes,dict(adjList),data, true_subgraph,trueFea
 
 
"""
edges: {node_id: {neighbor_id}, ...}
W: n by p matrix
p: total number of attributes
C: tradeoff parameter
 
"""
def outlierScore(neighbors, W, p, edges, C, s):
    """ Returns outlier score"""
    pairs = list(itertools.combinations(neighbors, 2))
    Hm = []
    Lm = []
    w = []
    zeta = []
    b = []
    c = []
 
    for i in range(p):
        w.append([])
    for rg in range(len(pairs)):
        zeta.append([])
 
    j = 0
    for p in pairs:  #Equation 3.9-3.12
        n1 = min(p)
        n2 = max(p)
 
        if n2 in edges[n1]:  #Equation 3.9 and 3.10 (alternative lines for each)
            Hm.append(-1.0)
            Hm.append(0.0)
            Lm.append(0.0)
            Lm.append(0.0)
            for i in range(len(w)):
                w[i].append(1.0 * abs(W[n1][i] - W[n2][i]))
                w[i].append(0.0)
            for i in range(len(zeta)):
                if (i == j):
                    zeta[i].append(-1.0)
                    zeta[i].append(-1.0)
                else:
                    zeta[i].append(0.0)
                    zeta[i].append(0.0)
 
        else:  #Equation 3.11 and 3.12 (alternative lines for each)
            Hm.append(0.0)
            Hm.append(0.0)
            Lm.append(1.0)
            Lm.append(0.0)
            for i in range(len(w)):
                w[i].append(-1.0 * abs(W[n1][i] - W[n2][i]))
                w[i].append(0.0)
            for i in range(len(zeta)):
                if (i == j):
                    zeta[i].append(-1.0)
                    zeta[i].append(-1.0)
                else:
                    zeta[i].append(0.0)
                    zeta[i].append(0.0)
 
        b.append(0.0)
        b.append(0.0)
 
        j += 1
    for i in range(len(w)):  #Equation 3.13
        Hm.append(0.0)
        Hm.append(0.0)
        Lm.append(0.0)
        Lm.append(0.0)
        for i in range(len(zeta)):
            zeta[i].append(0.0)
            zeta[i].append(0.0)
        for j in range(len(w)):
            if (i == j):
                w[j].append(1.0)
                w[j].append(-1.0)
            else:
                w[j].append(0.0)
                w[j].append(0.0)
        b.append(1.0)
        b.append(0.0)
 
    #Equation 3.14
    Hm.append(0.0)
    Hm.append(0.0)
    Lm.append(0.0)
    Lm.append(0.0)
    for i in range(len(zeta)):
        zeta[i].append(0.0)
        zeta[i].append(0.0)
    for i in range(len(w)):
        w[i].append(1.0)
        w[i].append(-1.0)
    b.append(1.0)
    b.append(-1.0)
 
    # Now populating c, the order of coefficients in c is Hm, Lm, w, zeta
    c.append(1.0)
    c.append(-1.0)
    for i in w:
        c.append(0.0)
    for i in zeta:
        c.append(1.0 * C / len(pairs))
 
    tempList = []
    tempList.append(Hm)
    tempList.append(Lm)
 
    for i in w:
        tempList.append(i)
    for i in zeta:
        tempList.append(i)
 
    A = matrix(tempList)
    b = matrix(b)
    c = matrix(c)
    # Now feeding the input to simplex algo
 
    # print A
    # print b
    # print c
 
    try:
        sol = solvers.lp(c, A, b, solver='glpk')
        # print sol['x']
    except Exception as e:
        print "error"
        return 0, None
    if sol['x'] == None:
        return None, None
    else:
        w1 = []
        for i in range(len(w)):
           w1.append([i, sol['x'][i+2]])
        w1 = sorted(w1, key=lambda x:x[1])
        print "w1:", w1
        return sol['x'][0] - sol['x'][1], [w1[i][0] for i in range(s)]
 
def get_neighbors(edges, S):
    neighbors = set()
    for i in S:
        for j in edges[i]:
            neighbors.add(j)
    return neighbors
 
 
"""
V: list of node ids that start from 0.
edges:
edges: {node_id: {neighbor_id}, ...}
W: n by p feature matrix
radius: the radius each neighborhood
s: number of true attributes
C: a tradeoff parameter (100 by default)
"""
def soda_scan(V, edges, W, radius, s, C):
    p = len(W[0])
    S_data = []
    for i in V:
        S = set()
        S.add(i)
        for step in range(radius):
            S = S.union(get_neighbors(edges, S))
        S_data.append(S)
    scores = []
    for S in S_data:
        print S
        if len(get_neighbors(edges, S)) < 100:
            [fvalue, feature_set] = outlierScore(get_neighbors(edges, S), W, p, edges, C, s)
            if fvalue != None:
                scores.append([fvalue, S, feature_set])
                print fvalue, S
#    sorted(scores, key=lambda x:x[0], reverse=True)
    print scores
    [fvalue, S, feature_set] = max(scores, key=lambda x:x[0])
    return fvalue, S, feature_set
 
def stat_calc(true_nodes, true_features, S, R):
    node_prec = len(true_nodes.intersection(S)) / (len(true_nodes) * 1.0)
    node_recall = len(true_nodes.intersection(S)) / (len(S) * 1.0)
    feature_prec = len(true_features.intersection(R)) / (len(true_features) * 1.0)
    feature_recall = len(true_features.intersection(R)) / (len(R) * 1.0)
    return node_prec, node_recall, feature_prec, feature_recall
 

 
def unittest4():
    V,edges,W,true_nodes, true_features = read_APDM_data('data/test1.txt')
    print V
    # print dict(edges)
    print W
    print true_nodes
    print true_features
    s = len(true_features)
    C = 10
    [fvalue, S, R] = soda_scan(V, edges, W, 1, s, C)
    # print S, R
    # [node_prec, node_recall, feature_prec, feature_recall] = stat_calc(true_nodes, true_features, S, R)
    # print node_prec, node_recall, feature_prec, feature_recall

if __name__ == '__main__':
 

    unittest4()