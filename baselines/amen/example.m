% add amen & subfolders to path
addpath(genpath('amen'))

% load matrix
%load('arlei_dblp.mat')
% score egonets
radius=1
tic
[X,edges,label,A]=APDM_Reader('APDM-GridData-100-precen-0.1-noise_0.0-0.txt');
[bestSubSet, bestScore,bestWeights] = amen_rank(A, X,radius,'norm', 'L2', 'min_degree', 1, 'max_degree', inf);
toc
bestSubSet
bestScore
bestWeights
inter=length(intersect(bestSubSet,label))
pre=inter/length(bestSubSet)
rec=inter/length(label)
[~,a]=find(bestWeights)
trueFeature=[1,2,3,4,5]
inter=length(intersect(a,trueFeature))
pre=inter/length(a)
rec=inter/length(trueFeature)
% plot results
%histogram(amen_scores)
%title('AMEN L2 Scores')

