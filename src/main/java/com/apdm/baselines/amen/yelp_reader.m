function [data_matrix,true_subgraph,true_feature,adj,words_ids,words_indices] = yelp_reader(fileName)
fid=fopen(fileName);
tline = fgetl(fid)
flag=1;
data_matrix=[];
nodes_ids = [];
nodes_indices = [];
words_ids = [];
words_indices = [];
true_subgraph = [];
true_feature = [];
numEdges=0;
index=1;
tic
while ischar(tline) && flag>0
    %numNodes = 100    
    %numOfFeatures = 10
    flag=0;
    if index == 1
        flag=1;
        str=strsplit(tline,' ');
        n=str2num(str{1});
        p=str2num(str{2});
        m=str2num(str{5});
        adj=sparse(n,n);
        fprintf('number of nodes: %d, edges: %d\n',n,m);
    elseif index >= 2 && index <= n+1        
        flag=1;
        tline = fgetl(fid);
        str=strsplit(tline,' ');
        temp=sprintf('%s ',str{:});
        newRow=sscanf(temp,'%f');
        newRow=newRow.';
        data_matrix=[ data_matrix; newRow];
    elseif index >= n + 2 && index <= (2*n + 1)        
        flag=1;
        tline = fgetl(fid);
        str=strsplit(tline,' ');
        words_ids = [words_ids, {str{1}}];
        words_indices = [words_indices, str2num(str{2})];
    elseif index >= 2*n + 2 && index <= (2*n + p + 1)        
        flag=1;
        tline = fgetl(fid);
        str=strsplit(tline,' ');
        nodes_ids = [nodes_ids, {str{1}}];
        nodes_indices = [nodes_indices, str2num(str{2})];
    elseif index == (2*n + p + 2)
        flag=1;
        tline = fgetl(fid);
        str=strsplit(tline,' ');
        temp=sprintf('%s ',str{:});
        newRow=sscanf(temp,'%d');
        true_subgraph=(newRow+1).';
        fprintf('number of true subgraph nodes: %d',length(true_subgraph));
    elseif index == (2*n + p + 3)
        flag=1;
        tline = fgetl(fid);
        str=strsplit(tline,' ');
        temp=sprintf('%s ',str{:});
        newRow=sscanf(temp,'%d');
        true_feature=(newRow+1).';
        fprintf('number of true feature nodes: %d',length(true_feature));
    elseif index >= (2*n + p + 4) && index <= (2*n + p + 4 + m - 1)
        flag=1;
        tline = fgetl(fid);
        str=strsplit(tline,' ');
        adj(str2num(str{1})+1,str2num(str{2})+1)=1;
        adj(str2num(str{2})+1,str2num(str{1})+1)=1;
    end
    index=index + 1;
    if mod(index,1000) == 0
        disp(index);
    end
end
true_subgraph=unique(true_subgraph);
true_feature = unique(true_feature);
run_time = toc;
fprintf('finished in %f seconds.',run_time);
if 0
    save('./test_case.mat','adj');
    save('./test_case.mat','numNodes','-append');
    save('./test_case.mat','numEdges','-append');
    save('./test_case.mat','data_matrix','-append');
    save('./test_case.mat','true_subgraph','-append');
    save('./test_case.mat','true_feature','-append');
    save('./test_case.mat','run_time','-append');
end
fclose(fid);