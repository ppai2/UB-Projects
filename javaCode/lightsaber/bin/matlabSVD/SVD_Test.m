
collectionPath= '';
path = collectionPath+'termDocMatrix\tor';
path2 = collectionPath+'termDocMatrix\blog';
start = 0;
dend = 10434;


%%%%%%%%%%%%%%  TOR   %%%%%%%%%%%%%%%%%%
errCount = 0;
for i=start:dend

    filename = i+'.txt';
    try
    A = load([path+'\' num2str(i) '.txt']);
    
    [U,S,V] = svds(A,20);
    V = round(V.*1000)/1000;
    
    dlmwrite([path+'\' num2str(i) '.txt'],V,',');
    i
    catch err
        errCount = errCount+1;
        Error(errCount) = i;
        dlmwrite([collectionPath+'\SVD Matrix\torError.txt'],Error,',');

    end
end


%%%%%%%%%%%%%%  BLOG   %%%%%%%%%%%%%%%%%%
errCount2 = 0;
for i=start:dend

    filename = i+'.txt';
    try
    A = load([path2+'\' num2str(i) '.txt']);
    
    [U,S,V] = svds(A,20);
    V = round(V.*1000)/1000;
    
    dlmwrite([path2+'\' num2str(i) '.txt'],V,',');
    i
    catch err
        errCount2 = errCount2+1;
        Error(errCount2) = i;
        dlmwrite([collectionPath+'\SVD Matrix\blogError.txt'],Error,',');

    end
end