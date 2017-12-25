function [indexOrder] = FocusCO(ApdmFile ,outFile, similar_nodes, gamma,disRatio)
   
addpath('matlab_src/PGDM')
addpath('matlab_src/io')
addpath('matlab_src')
%ApdmFile = '/home/baojian/git/S2GraphMP/data/DenseGraph/DenseSubgraph_APDM/Test1/Test1_APDM_DenseSubgraph_in_0.35_out_0.1_Cluster_10_TrueSGSize_30_FeaNum_20_trueFeasNum_10_case_1.txt';
%outFile = '/home/baojian/git/S2GraphMP/data/DenseGraph/DenseSubgraph_APDM/Test1/Test1_APDM_DenseSubgraph_in_0.35_out_0.1_Cluster_10_TrueSGSize_30_FeaNum_20_trueFeasNum_10_case_1.wEdges';
%similar_nodes = [83, 63, 62, 68, 76, 74, 75, 84, 69, 61, 79, 64, 78, 67, 82, 85, 88, 72, 80, 89, 65, 71, 77, 90, 70, 86, 87, 66, 73, 81]
%gamma = 1.0
%disRatio = 2.0

[X,edges,label,A]=APDM_Reader(ApdmFile);
% open and load files
  
similar_pairs = [];

for node=[1:2*length(similar_nodes)-1]
    ii = randi([1,length(similar_nodes)]);
    jj = randi([1,length(similar_nodes)]);
    if ii ~= jj
        similar_pairs = [similar_pairs; similar_nodes(ii), similar_nodes(jj)];
    end
end
varargin={};
% pull out some useful variables
num_vertices = size(A,1);
% parse remaining arguments
p = inputParser;
defaultGamma = gamma;
defaultDissimilarSamples = disRatio*size(similar_pairs,2);
default_topk_features = size(X,2);   
default_dml = 'sparse';
default_file_out = outFile;
default_reweight_type = 'sparse';

addOptional(p, 'gamma', defaultGamma,@isnumeric);
addOptional(p, 'size_D', defaultDissimilarSamples,@isnumeric);
addOptional(p, 'top_k_features', default_topk_features, @isnumeric);   
addOptional(p, 'dml_datatype', default_dml, @(x) strcmp(x, 'sparse') || strcmp(x, 'dense'));   
addOptional(p, 'file_output', default_file_out, @isstr);   
addOptional(p, 'reweight_type', default_reweight_type, @isstr);   

parse(p, varargin{:});

gamma = p.Results.gamma;
num_dissimilar_pairs = p.Results.size_D;
top_k_features = p.Results.top_k_features;
dml_datatype = p.Results.dml_datatype;
dm_file_out = p.Results.file_output;
reweight_type = p.Results.reweight_type;

fprintf('FocusCO Distance Metric Learning\n-------------------------------------\n')
fprintf('Gamma: %f\n', gamma)
fprintf('# Dissimilar pairs: %d\n', num_dissimilar_pairs)
fprintf('Distance Metric Learning Data Type: %s\n', dml_datatype)   
fprintf('# Features to consider (if dml_datatype == sparse): %d\n', top_k_features)
fprintf('Type of graph reweighting (sparse or dense similarity): %s\n', reweight_type)
fprintf('Graph Output File: %s\n\n', dm_file_out)

% use dense or sparse DML?
[ DM, S, D ] = distance_metric_learning_manual(X, similar_pairs, num_dissimilar_pairs, num_vertices , gamma, top_k_features, dml_datatype);

fprintf('Distance metric:\n');
arrDM=diag(DM).';
[order,indexOrder]=sort(arrDM,'descend');
arr=find(arrDM);

indexOrder=indexOrder(1:length(arr))
if strcmp(reweight_type, 'sparse')
    WeightedA = reweigh_sparse(A, X, DM);
else       
    WeightedA = reweigh(A, X, DM);
end

if ~strcmp(dm_file_out, '')
    savesparse(dm_file_out, WeightedA);
end
%exit    



