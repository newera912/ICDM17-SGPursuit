% add amen & subfolders to path
addpath(genpath('amen'))

% load matrix
%load('arlei_dblp.mat')
% score egonets
[X,edges,label,A]=APDM_Reader('F:/workspace/git/S2GraphMP/data/DenseGraph/Dense_APDM/cluster100/cluster-10/APDM_r_5_Cluster_10_in_0.25_out_0.01_case_1.txt');
[amen_ranking, amen_scores] = amen_rank(A, X, 'norm', 'L2', 'min_degree', 1, 'max_degree', inf);

% plot results
histogram(amen_scores)
title('AMEN L2 Scores')

