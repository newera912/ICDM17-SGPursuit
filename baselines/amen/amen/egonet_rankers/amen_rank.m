function [ bestSubSet, bestScore,bestWeights ] = amen_rank( A, X_Total,radius, varargin )
%AMEN_RANK Rank egonets by their amen circle normality score
%   Detailed explanation goes here

    % parameters
    parser = inputParser;
    addOptional(parser,'min_degree', 30);
    addOptional(parser,'max_degree', 100);    
    addOptional(parser,'norm', 'L1'); 
    addOptional(parser,'node_filter', []);     

    varargin{:};
    parse(parser, varargin{:});    

    min_degree = parser.Results.min_degree
    max_degree = parser.Results.max_degree
    p_norm = parser.Results.norm
    node_filter = parser.Results.node_filter;    

    degrees = sum(A,2);
    M = nnz(A)/2;
    
    if isempty(node_filter)
        node_filter = (degrees >= min_degree & degrees <= max_degree);
    end
    
    % select egos to permute
    egos = find(node_filter)

    ego_scores = zeros(1,numel(egos));
    
    X_Total_Transpose = X_Total.';
    bestScore=0;
    
    for i=1:numel(egos)    
        ego = egos(i);                        
        [neighbors, ~, ~] = find(A(:,ego)); 
        All_neighbors=[neighbors'];
        %Add neighbor nodes with the r as the radius%
        curr_neighbors=neighbors';
        if radius>1       
        for r=1:radius
            temp=[];
            for node = [curr_neighbors']
                [NodeNeighbors, ~, ~] = find(A(:,node));
                All_neighbors=union(All_neighbors,NodeNeighbors);
                temp=union(temp,NodeNeighbors);
                temp'
            end
            curr_neighbors=setdiff(temp,All_neighbors);
            curr_neighbors';
        end            
        end
        All_neighbors';
        
        egonet_community = [ego All_neighbors];
               
        % use only features that are non-zero inside the community
%         [ci, ~, ~] = find(X_Total_Transpose(:,egonet_community));        
        X = [];         
        [weights, weighted_score] = amen_learn_weights( A, X, egonet_community, degrees,  M, p_norm, @amen_objective, X_Total_Transpose );         
        ego_scores(i) = weighted_score;
        if bestScore<abs(weighted_score)
            bestScore=abs(weighted_score);
            bestWeights=weights;
            bestSubSet=egonet_community;
        end
    end
    
    % sort all egos by score
    [scores, ind] = sort(ego_scores);
    scores = scores;
    ranking = egos(ind);
end

