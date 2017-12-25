from __future__ import print_function
import itertools
from collections import defaultdict
from cvxopt import matrix, solvers
import os
import sys
import time
import networkx as nx

__author__ = 'fengchen'

"""
edges: {node_id: {neighbor_id}, ...}
data_matrix: n by p matrix
p: total number of attributes
C: tradeoff parameter

"""


def outlier_score(neighbors, data_matrix, edges, C, s):
    p = len(data_matrix[0])
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
    for p in pairs:  # Equation 3.9-3.12
        n1 = min(p)
        n2 = max(p)
        if n2 in edges[n1]:  # Equation 3.9 and 3.10 (alternative lines for each)
            Hm.append(-1.0)
            Hm.append(0.0)
            Lm.append(0.0)
            Lm.append(0.0)
            for i in range(len(w)):
                w[i].append(1.0 * abs(data_matrix[n1][i] - data_matrix[n2][i]))
                w[i].append(0.0)
            for i in range(len(zeta)):
                if i == j:
                    zeta[i].append(-1.0)
                    zeta[i].append(-1.0)
                else:
                    zeta[i].append(0.0)
                    zeta[i].append(0.0)
        else:  # Equation 3.11 and 3.12 (alternative lines for each)
            Hm.append(0.0)
            Hm.append(0.0)
            Lm.append(1.0)
            Lm.append(0.0)
            for i in range(len(w)):
                w[i].append(-1.0 * abs(data_matrix[n1][i] - data_matrix[n2][i]))
                w[i].append(0.0)
            for i in range(len(zeta)):
                if i == j:
                    zeta[i].append(-1.0)
                    zeta[i].append(-1.0)
                else:
                    zeta[i].append(0.0)
                    zeta[i].append(0.0)
        b.append(0.0)
        b.append(0.0)
        j += 1
    for i in range(len(w)):  # Equation 3.13
        Hm.append(0.0)
        Hm.append(0.0)
        Lm.append(0.0)
        Lm.append(0.0)
        for i in range(len(zeta)):
            zeta[i].append(0.0)
            zeta[i].append(0.0)
        for j in range(len(w)):
            if i == j:
                w[j].append(1.0)
                w[j].append(-1.0)
            else:
                w[j].append(0.0)
                w[j].append(0.0)
        b.append(1.0)
        b.append(0.0)
    # Equation 3.14
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
    for _ in w:
        c.append(0.0)
    for _ in zeta:
        c.append(1.0 * C / len(pairs))

    temp_list = [Hm, Lm]
    for i in w:
        temp_list.append(i)
    for i in zeta:
        temp_list.append(i)
    A = matrix(temp_list)
    b = matrix(b)
    c = matrix(c)
    # Now feeding the input to simplex algo
    try:
        sol = solvers.lp(c, A, b, solver='glpk')
        # print sol['x']
    except ImportError:
        print("there is glpk package, please install it.")
        sys.exit(0)
    if sol['x'] is None:
        return None, None
    else:
        w1 = []
        for i in range(len(w)):
            w1.append([i, sol['x'][i + 2]])
        w1 = sorted(w1, key=lambda x: x[1])
        return sol['x'][0] - sol['x'][1], [w1[i][0] for i in range(s)]


def get_neighbors(edges, subset):
    neighbors = set()
    for i in subset:
        for j in edges[i]:
            neighbors.add(j)
    return neighbors


def load_crimes_of_chicago(file_path_):
    nodes = []
    adj_list = defaultdict(list)
    data_matrix = []
    true_subgraph = []
    true_features = []
    index = 1
    g = nx.Graph()
    with open(file_path_) as f:
        for each_line in f.readlines():
            if index == 1:
                n = int(each_line.rstrip().split(' ')[0])
                p = int(each_line.rstrip().split(' ')[1])
                for i in range(n):
                    nodes.append(i)
                    data_matrix.append([])
                index += 1
            elif 2 <= index <= (n + 1):
                for each_entry in each_line.rstrip().split(' '):
                    data_matrix[index - 2].append(float(each_entry))
                index += 1
            elif (index >= (n + 2)) and (index <= (n + 2)):
                num_edges = int(each_line.rstrip())
                index += 1
            elif (index >= (n + 3)) and (index <= (n + 3 + num_edges - 1)):
                endpoint0 = each_line.rstrip().split(' ')[0]
                endpoint1 = each_line.rstrip().split(' ')[1]
                edge = [0] * 2
                edge[0] = int(endpoint0)
                edge[1] = int(endpoint1)
                g.add_edge(edge[0], edge[1], weight=1.0)
                if edge[1] not in adj_list[edge[0]]:
                    adj_list[edge[0]].append(edge[1])
                if edge[0] not in adj_list[edge[1]]:
                    adj_list[edge[1]].append(edge[0])
                index += 1
            elif (index >= (n + num_edges + 3)) and (index <= (n + num_edges + 3)):
                for each_node in each_line.rstrip().split(' '):
                    true_subgraph.append(int(each_node))
                index += 1
            elif (index >= (n + num_edges + 4)) and (index <= (n + num_edges + 4)):
                for each_feature in each_line.rstrip().split(' '):
                    true_features.append(int(each_feature))
                index += 1
    print('number of nodes: {}'.format(n))
    print('number of features: {}'.format(p))
    print('true nodes size: {}'.format(len(true_subgraph)))
    print('true feature size: {}'.format(len(true_features)))
    return nodes, dict(adj_list), data_matrix, true_subgraph, true_features, g


"""
V: list of node ids that start from 0.
edges:
edges: {node_id: {neighbor_id}, ...}
W: n by p feature matrix [[] []]
radius: the radius each neighborhood
s: number of true attributes
C: a trade-off parameter (100 by default)
"""


def soda_scan(edges, data_matrix, radius, true_feature_size, C, true_nodes):
    set_query_nodes = []
    for i in true_nodes:
        nodes = set()
        nodes.add(i)
        for step in range(radius):
            nodes = nodes.union(get_neighbors(edges, nodes))
        set_query_nodes.append(nodes)

    scores = []
    for nodes in set_query_nodes:
        if len(get_neighbors(edges, nodes)) < 200:
            [func_value, feature_set] = \
                outlier_score(get_neighbors(edges, nodes), data_matrix, edges, C, true_feature_size)
            if func_value is not None:
                scores.append([func_value, nodes, feature_set])
                print(func_value, nodes)
    if scores:
        [func_value, nodes, feature_set] = max(scores, key=lambda x: x[0])
        return func_value, nodes, feature_set
    else:
        return 0.0, set(), []


def stat_calc(true_nodes, true_features, result_nodes, result_features):
    true_features = set(true_features)
    true_nodes = set(true_nodes)
    if len(result_nodes) == 0:
        node_prec = 0.0
        node_recall = 0.0
    else:
        node_prec = len(true_nodes.intersection(result_nodes)) / (len(true_nodes) * 1.0)
        node_recall = len(true_nodes.intersection(result_nodes)) / (len(result_nodes) * 1.0)
    if (node_prec + node_recall) == 0.0:
        node_fm = 0.0
    else:
        node_fm = 2.0 * (node_prec * node_recall) / (node_prec + node_recall)
    if len(result_features) == 0:
        feature_prec = 0.0
        feature_recall = 0.0
    else:
        feature_prec = len(true_features.intersection(result_features)) / (len(true_features) * 1.0)
        feature_recall = len(true_features.intersection(result_features)) / (len(result_features) * 1.0)
    if (feature_prec + feature_recall) == 0.0:
        feature_fm = 0.0
    else:
        feature_fm = 2.0 * (feature_prec * feature_recall) / (feature_prec + feature_recall)
    return node_prec, node_recall, node_fm, feature_prec, feature_recall, feature_fm


def get_data_file_list(output_path_):
    file_list = []
    keywords = []
    for file_path in sorted(os.listdir(output_path_)):
        file_list.append(output_path_ + file_path)
        keywords.append(file_path.split('_')[1])
    return file_list, keywords


def test_on_crimes_of_chicago():
    file_path = '/home/apdm02/Dropbox/expriments/CrimesOfChicago/graph/'
    file_list, keywords = get_data_file_list(file_path)
    index = 0
    for each_file in file_list:
        start_time = time.time()
        fp = open('final_result_SODA_crimes_diff_para.txt', 'a')
        vertices, edges, data_matrix, true_nodes, true_features, g = \
            load_crimes_of_chicago(each_file)
        type_ = keywords[index]
        result = []
        best_fm = -1.0
        for radius in [1, 2, 3, 4, 5]:
            C = 100.0
            true_feature_size = len(true_features)
            [func_value, result_nodes, result_features] = \
                soda_scan(edges, data_matrix, radius, true_feature_size, C, true_nodes)
            [node_prec, node_recall, node_fm, feature_prec, feature_recall, feature_fm] = \
                stat_calc(true_nodes, true_features, result_nodes, result_features)
            if not result:
                result = [type_, str(node_prec), str(node_recall), str(node_fm)]
                result += [str(feature_prec), str(feature_recall), str(feature_fm)]
                best_fm = node_fm
            elif node_fm > best_fm:
                result = [type_, str(node_prec), str(node_recall), str(node_fm)]
                result += [str(feature_prec), str(feature_recall), str(feature_fm)]
                best_fm = node_fm
            print('-----------------------------------------------------')
            print('function value: ', func_value)
            print('radius: ', radius)
            print(node_prec, node_recall, feature_prec, feature_recall)
            print('-----------------------------------------------------')
        run_time = time.time() - start_time
        run_time *= (len(vertices) * 1.0 / len(true_nodes))
        result.append(str(run_time))
        index += 1
        fp.write(' '.join(result) + '\n')
        fp.close()
        print('finish ...')
        time.sleep(2)


if __name__ == '__main__':
    test_on_crimes_of_chicago()
