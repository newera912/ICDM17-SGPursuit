% add amen & subfolders to path
addpath(genpath('amen'))

% load matrix
%load('arlei_dblp.mat')
% score egonets

root_path = '../../dataset/CrimesOfChicago/graph/';
resultFile='./result_amen_BURGLARY_diff_para_new.txt';
files = dir(strcat(root_path,'/*.txt'));
for file = files'
    
    filepath=strcat(root_path,file.name);
    str=strsplit(file.name,'_');
    keyword=str{2};
    if strcmp(keyword,'BATTERY')
        continue
    end
    
    run_time = 0;
    [data_matrix,true_subgraph,true_feature,adj]=crimes_chicago_reader(filepath);
    best_fm = -1.0;
    result = [];
    fileID=fopen(resultFile,'a+');
    for radius = [1 2 3 4 5]
        %addpath(genpath('amen'))
        %load('test_case.mat')
        tic
        [bestSubSet, bestScore,bestWeights] = amen_rank(adj,true_subgraph,radius, data_matrix,'norm', 'L2', 'min_degree', 1, 'max_degree', inf);
        run_time = run_time + toc;
        disp('-------------------------------------------------------------------\n')
        inter=length(intersect(bestSubSet,true_subgraph));
        pre_node=inter/length(bestSubSet);
        rec_node=inter/length(true_subgraph);
        fm_node = 0;
        if (pre_node + rec_node) ~= 0
            fm_node = 2*(pre_node*rec_node) / (pre_node + rec_node);
        else
            fm_node = 0.0;
        end
        [a,b] = sort(bestWeights,2,'descend');
        b = b(1:length(true_feature));
        inter=length(intersect(b,true_feature));
        pre=inter/length(b);
        rec=inter/length(true_feature);
        if (pre + rec) ~= 0
            fm = 2*(pre*rec) / (pre + rec);
        else
            fm = 0.0;
        end
        if fm_node > best_fm
            best_fm = fm_node;
            result = [pre_node,rec_node,fm_node,pre,rec,fm];
        end
    end
    pre = result(1);
    rec = result(2);
    fm = result(3);
    fprintf('nodes precision and recall %f %f %f \n',pre,rec,fm);
    fprintf(fileID,'%s %1.6f %1.6f %1.6f ',keyword,pre,rec,fm);
    pre = result(4);
    rec = result(5);
    fm = result(6);
    run_time = run_time*(46357/length(true_subgraph));
    fprintf('feature precision and recall %f %f %f \n',pre,rec,fm);
    fprintf(fileID,'%1.6f %1.6f %1.6f ',pre,rec,fm);
    fprintf(fileID,'%1.6f\n',run_time);
    fclose(fileID);
    disp('-------------------------------------------------------------------\n')
end
