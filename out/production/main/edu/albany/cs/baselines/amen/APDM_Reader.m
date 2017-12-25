function [data,edges,label,A] = APDM_Reader(fileName)
fid=fopen(fileName);
tline = fgetl(fid);
data=[];
label=[];
edges={};
numNodes=0;
numFess=0;
while ischar(tline)
    %numNodes = 100    
    %numOfFeatures = 10
    if strncmp(tline,'#',1)        
        tline = fgetl(fid);
        continue;
    elseif strstartswith(tline,'numNodes')
        str=strsplit('=',tline,'omit');
        numNodes=str2num(str{2});
        tline = fgetl(fid);
    elseif strstartswith(tline,'numOfFeatures')
        str=strsplit('=',tline,'omit');
        numOfFeatures=str2num(str{2}); 
        tline = fgetl(fid);
    
    elseif strstartswith(tline,'NodeID')        
        while(true)
            tline = fgetl(fid);
            if ~strstartswith(tline,'END')
                str=strsplit(' ',tline,'omit');
                temp=sprintf('%s ',str{:});
                newRow=sscanf(temp,'%f');
                newRow=newRow.';
                data=[data; newRow(2:end)];
            else
                break
            end            
        end
    elseif strstartswith(tline,'EndPoint0')
         A=sparse(length(data),length(data));
         index=1;
         while(true)
            tline = fgetl(fid);
            if ~strstartswith(tline,'END')
                str=strsplit(' ',tline,'omit');
                edges{index}(1)=str2num(str{1})+1;
                edges{index}(2)=str2num(str{2})+1;
                A(edges{index}(1),edges{index}(2))=1;
                A(edges{index}(2),edges{index}(1))=1;
                index=index+1;
                
            else
                break
            end            
         end
    elseif strstartswith(tline,'SECTION4')
         fgetl(fid);
         index=1;
         while(true)
            tline = fgetl(fid);
            if ~strstartswith(tline,'END')
                str=strsplit(' ',tline,'omit');
                label=[label str2num(str{1})+1];
                label=[label str2num(str{2})+1];
                index=index+1;
                
            else
                break
            end            
        end
    else
        tline = fgetl(fid);
        continue
    %disp(tline)
    
    end
end
label=unique(label);
A=sparse(A);

fclose(fid);