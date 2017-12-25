function [data_matrix,true_subgraph,true_feature,adj] = crimes_chicago_reader(fileName)
fid=fopen(fileName);
tline = fgetl(fid);
data_matrix=[];
true_subgraph = [];
true_feature = [];
numNodes=0;
numEdges=0;
index=1;
tic
while ischar(tline)
    %numNodes = 100    
    %numOfFeatures = 10
    if index == 1
        str=strsplit(tline,' ');
        numNodes=str2num(str{1});
        adj=sparse(numNodes,numNodes);
        index = index + 1;
        fprintf('number of nodes: %d\n',numNodes);
    elseif index >= 2 && index <= numNodes+1
        tline = fgetl(fid);
        str=strsplit(tline,' ');
        temp=sprintf('%s ',str{:});
        newRow=sscanf(temp,'%f');
        newRow=newRow.';
        if length(newRow) ~= 121
            fprintf('error !!! length of newRow: %d\n',length(newRow));
            pause;
        end
        data_matrix=[ data_matrix; newRow];
        index = index + 1;
    elseif index == (numNodes + 2)
        tline = fgetl(fid);
        numEdges=str2num(tline)
        fprintf('number of edges: %d\n',numEdges);
        index=index+1;
    elseif index >= (numNodes + 3) && index <= (numNodes + 3 + numEdges - 1)
        tline = fgetl(fid);
        str=strsplit(tline,' ');
        adj(str2num(str{1})+1,str2num(str{2})+1)=1;
        adj(str2num(str{2})+1,str2num(str{1})+1)=1;
        index=index+1;
    elseif index == (numNodes + numEdges + 3)
        tline = fgetl(fid);
        str=strsplit(tline,' ');
        temp=sprintf('%s ',str{:});
        newRow=sscanf(temp,'%d');
        true_subgraph=(newRow+1).';
        fprintf('number of true subgraph nodes: %d',length(true_subgraph));
        index=index + 1;
    elseif index == (numNodes + numEdges + 4)
        tline = fgetl(fid);
        str=strsplit(tline,' ');
        temp=sprintf('%s ',str{:});
        newRow=sscanf(temp,'%d');
        true_feature=(newRow+1).';
        fprintf('number of true feature nodes: %d',length(true_feature));
        index=index + 1;
    else
        tline = fgetl(fid);
        index=index + 1;
        continue
    %disp(tline)
    end
    if mod(index,10000) == 0
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