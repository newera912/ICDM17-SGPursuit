import matplotlib.pyplot as plt
import numpy as np
from matplotlib.pyplot import xlabel, ylabel




xlabels=["Number of Attributes","Number of Clusters","Range of Cluster Size"]
SetTitles=["Effectiveness of Increasing Attribute Space Size","Effectiveness of Incoherent Clusters","Effectiveness of Different Cluster Size"]
#for m,settings in enumerate(["VaryingNumOfAttributes","VaryingNumOfClusters","VaryingClusterSizes"]):#5,15,20]:
for m,settings in enumerate(["VaryingNumOfAttributes","VaryingNumOfClusters","VaryingClusterSizes"]):#5,15,20]:
    points = [
    (0, 10),
    (10, 20),
    (20, 40),
    (60, 100),
    ]
    
    x = map(lambda x: x[0], points)
    y = map(lambda x: x[1], points)
    plt.rc('grid', linestyle="-",linewidth=0.2, color='gray')
    plt.scatter(x, y)
    plt.grid(True)
    
    plt.show()
    plt.close()
    method_num=3
    if m==2:
        method_num=2
    xpre=[[] for i in range(method_num)]
    xrec=[[] for i in range(method_num)]
    xf=[[] for i in range(method_num)]
    ypre=[[] for i in range(method_num)]
    yrec=[[] for i in range(method_num)]
    yf=[[] for i in range(method_num)]
    x_label=[]
    #"0.5","1.0","2.0","3.0","5.0"]:#"0.5","1.0","2.0","3.0","5.0","10.0"]:
        
    methods=["SG-Pursuit","FocusCO","GAMer"]
    for i,method in enumerate(methods):
        if i==2 and m==2:
            continue
        fileName="./"+settings+"-"+method+".txt"
        xp=[]
        xr=[]
        xf1=[]
        
        yp=[]
        yr=[]
        yf1=[]
        x_label=[]
        with open(fileName,"r") as f:
            for line in f.readlines():
                terms=line.strip().split(" ")
                if m==2:
                    x_label.append("[30,"+str(terms[0])+"]") 
                else:        
                    x_label.append(str(terms[0]))               
                xp.append(float(terms[1]))                
                xr.append(float(terms[2]))
                xf1.append(float(terms[3]))
                
                yp.append(float(terms[4]))
                yr.append(float(terms[5])) 
                yf1.append(float(terms[6]))               
               
        xpre[i]=xp
        xrec[i]=xr
        ypre[i]=yp
        yrec[i]=yr
        xf[i]=xf1
        yf[i]=yf1
    
    if m<2:    
        print "0,1"
        x=[i for i in range(len(x_label))]   
        fig=plt.figure(1,figsize=(9,6))
        plt.subplot(231)
        plt.rc('grid', linestyle="-",linewidth=0.2, color='gray')
        #x=[i for i in range(len(mu))] 
        print xpre
        print xrec
         
        plt.plot(x, xpre[0],'r*--',linewidth='1.2', markersize=9,label=methods[0]) 
        plt.plot(x, xpre[1],'bo-.',linewidth='1.2', markersize=6,label=methods[1])
        plt.plot(x, xpre[2],'g^:',linewidth='1.2', markersize=6,label=methods[2]) 
       
        plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,ncol=1, mode="expand", borderaxespad=0.)
        plt.xticks(x,x_label,fontsize=8)  
        plt.yticks(fontsize=8)    
        plt.ylim([0.0,1.1])
        plt.xlabel(xlabels[m],fontsize=8)
        plt.ylabel('Precision of Nodes',fontsize=8)
        plt.title(SetTitles[m],fontsize=8)
        leg=plt.legend(loc='best',fontsize=8)  
        leg.get_frame().set_alpha(0.5)      
        plt.grid()
       
        #plt.plot(X, Avg, yerr=Min_Max, fmt='o',label='Average With Min. & Max values')
        
        
        plt.subplot(234)    
        plt.plot(x,  ypre[0] , 'r*--',linewidth='1.2', markersize=9,label=methods[0]) 
        plt.plot(x,  ypre[1] , 'bo-.',linewidth='1.2', markersize=6,label=methods[1]) 
        plt.plot(x,  ypre[2],   'g^:',linewidth='1.2', markersize=6,label=methods[2])  
           
        plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,ncol=1, mode="expand", borderaxespad=0.)
        plt.xticks(x,x_label,fontsize=8)  
        plt.yticks(fontsize=8)        
        plt.ylim([0.0,1.1])
        plt.ylabel('Precision of Attributes',fontsize=8)
        plt.xlabel(xlabels[m],fontsize=8)
        #plt.xlim([0.2,0.0])
        leg=plt.legend(loc='best',fontsize=8)  
        leg.get_frame().set_alpha(0.5) 
        #plt.title(SetTitles[m],fontsize=8)
        plt.grid()
        plt.rc('grid', linestyle="-",linewidth=0.2, color='gray')
        
        plt.subplot(232)     
        plt.plot(x,  xrec[0] ,'r*--',linewidth='1.2', markersize=9,label=methods[0]) 
        plt.plot(x,  xrec[1] , 'bo-.',linewidth='1.2', markersize=6,label=methods[1])  
        plt.plot(x,  xrec[2],   'g^:',linewidth='1.2', markersize=6,label=methods[2])  
        
        plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,ncol=1, mode="expand", borderaxespad=0.)
        plt.xticks(x,x_label,fontsize=8)  
        plt.yticks(fontsize=8)   
         
         
        plt.ylim([0.0,1.1])
        plt.xlabel(xlabels[m],fontsize=8) 
        plt.ylabel('Recall of Nodes',fontsize=8)  
        leg=plt.legend(loc='best',fontsize=8)  
        leg.get_frame().set_alpha(0.5) 
        plt.title(SetTitles[m],fontsize=8)
        plt.grid()
        plt.rc('grid', linestyle="-",linewidth=0.2, color='gray')
        
        plt.subplot(235)     
        plt.plot(x,  yrec[0] , 'r*--',linewidth='1.2', markersize=9,label=methods[0]) 
        plt.plot(x,  yrec[1] , 'bo-.',linewidth='1.2', markersize=6,label=methods[1])    
        plt.plot(x,  yrec[2],   'g^:',linewidth='1.2', markersize=6,label=methods[2])    
        
        plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,ncol=1, mode="expand", borderaxespad=0.)
        plt.xticks(x,x_label,fontsize=8)  
        plt.yticks(fontsize=8)   
         
         
        plt.ylim([0.0,1.1])
        plt.xlabel(xlabels[m],fontsize=8)
        plt.ylabel('Recall of Attributes',fontsize=8)
        #plt.xlim([0.2,0.0])
        leg=plt.legend(loc='best',fontsize=8)  
        leg.get_frame().set_alpha(0.5) 
        
        plt.grid()
        plt.rc('grid', linestyle="-",linewidth=0.2, color='gray')
        
        plt.subplot(233)
        plt.plot(x,  xf[0] ,'r*--',linewidth='1.2', markersize=9,label=methods[0]) 
        plt.plot(x,  xf[1] , 'bo-.',linewidth='1.2', markersize=6,label=methods[1])    
        plt.plot(x,  xf[2],   'g^:',linewidth='1.2', markersize=6,label=methods[2])   
        
        plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,ncol=1, mode="expand", borderaxespad=0.)
        plt.xticks(x,x_label,fontsize=8)  
        plt.yticks(fontsize=8)   
         
        plt.title(SetTitles[m],fontsize=8)
        plt.ylim([0.0,1.1])
        plt.xlabel(xlabels[m],fontsize=8) 
        plt.ylabel('F-measure of Nodes',fontsize=8)   
        leg=plt.legend(loc='best',fontsize=8)  
        leg.get_frame().set_alpha(0.5)        
        plt.grid()
        plt.rc('grid', linestyle="-",linewidth=0.2, color='gray')
         
        plt.subplot(236)      
        plt.plot(x,  yf[0] , 'r*--',linewidth='1.2', markersize=9,label=methods[0]) 
        plt.plot(x,  yf[1] , 'bo-.',linewidth='1.2', markersize=6,label=methods[1])    
        plt.plot(x,  yf[2],  'g^:',linewidth='1.2', markersize=6,label=methods[2])   
        
        plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,ncol=1, mode="expand", borderaxespad=0.)
        plt.xticks(x,x_label,fontsize=8)  
        plt.yticks(fontsize=8)        
        plt.ylim([0.0,1.1])
        plt.xlabel(xlabels[m],fontsize=8)  
        plt.ylabel('F-measure of Attributes',fontsize=8)  
        leg=plt.legend(loc='best',fontsize=8)  
        leg.get_frame().set_alpha(0.5) 
        
        plt.grid()
        plt.rc('grid', linestyle="-",linewidth=0.2, color='gray')
        
        #plt.tight_layout(pad=0.4, w_pad=0.5, h_pad=1.0)
        plt.show()
        fig.savefig('./'+settings+'.png',dpi=300)
        plt.close()
    else:
        print "3"
        x=[i for i in range(len(x_label))]   
        fig=plt.figure(2,figsize=(9,6))
        plt.subplot(231)
        plt.rc('grid', linestyle="-",linewidth=0.2, color='gray')
        #x=[i for i in range(len(mu))] 
        print xpre
        print xrec
         
        plt.plot(x, xpre[0],'r*--',linewidth='1.2', markersize=9,label=methods[0]) 
        plt.plot(x, xpre[1],'bo-.',linewidth='1.2', markersize=6,label=methods[1])
       
       
        plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,ncol=1, mode="expand", borderaxespad=0.)
        plt.xticks(x,x_label,fontsize=8)  
        plt.yticks(fontsize=8)    
        plt.ylim([0.0,1.1])
        plt.xlabel(xlabels[m],fontsize=8)
        plt.ylabel('Precision of Nodes',fontsize=8)
        plt.title(SetTitles[m],fontsize=8)
        leg=plt.legend(loc='best',fontsize=8)  
        leg.get_frame().set_alpha(0.5)   
              
        plt.grid(True)
        
        #plt.plot(X, Avg, yerr=Min_Max, fmt='o',label='Average With Min. & Max values'
        
        
        plt.subplot(234)    
        plt.plot(x,  ypre[0] , 'r*--',linewidth='1.2', markersize=9,label=methods[0]) 
        plt.plot(x,  ypre[1] , 'bo-.',linewidth='1.2', markersize=6,label=methods[1]) 
      
           
        plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,ncol=1, mode="expand", borderaxespad=0.)
        plt.xticks(x,x_label,fontsize=8)   
        plt.yticks(fontsize=8)       
        plt.ylim([0.0,1.1])
        plt.xlabel(xlabels[m],fontsize=8)
        plt.ylabel('Precision of Attributes',fontsize=8)
        #plt.xlim([0.2,0.0])
        leg=plt.legend(loc='best',fontsize=8)  
        leg.get_frame().set_alpha(0.5) 
        #plt.title(SetTitles[m],fontsize=8)
        plt.grid()
        plt.rc('grid', linestyle="-",linewidth=0.2, color='gray')
        
        plt.subplot(232)     
        plt.plot(x,  xrec[0] ,'r*--',linewidth='1.2', markersize=9,label=methods[0]) 
        plt.plot(x,  xrec[1] , 'bo-.',linewidth='1.2', markersize=6,label=methods[1])  
          
        
        plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,ncol=1, mode="expand", borderaxespad=0.)
        plt.xticks(x,x_label,fontsize=8)  
        plt.yticks(fontsize=8)   
         
         
        plt.ylim([0.0,1.1])
        plt.xlabel(xlabels[m],fontsize=8)
        plt.ylabel('Recall of Nodes',fontsize=8)    
        leg=plt.legend(loc='best',fontsize=8)  
        leg.get_frame().set_alpha(0.5) 
        plt.title(SetTitles[m],fontsize=8)
        plt.grid()
        plt.rc('grid', linestyle="-",linewidth=0.2, color='gray')
        
        plt.subplot(235)     
        plt.plot(x,  yrec[0] , 'r*--',linewidth='1.2', markersize=9,label=methods[0]) 
        plt.plot(x,  yrec[1] , 'bo-.',linewidth='1.2', markersize=6,label=methods[1])    
           
        
        plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,ncol=1, mode="expand", borderaxespad=0.)
        plt.xticks(x,x_label,fontsize=8)  
        plt.yticks(fontsize=8)   
         
         
        plt.ylim([0.0,1.1])
        plt.xlabel(xlabels[m],fontsize=8)
        plt.ylabel('Recall of Attributes',fontsize=8)  
        #plt.xlim([0.2,0.0])
        leg=plt.legend(loc='best',fontsize=8)  
        leg.get_frame().set_alpha(0.5) 
        #plt.title('y Recall',fontsize=8)
        plt.grid()
        plt.rc('grid', linestyle="-",linewidth=0.2, color='gray')
        
        plt.subplot(233)
        plt.plot(x,  xf[0] ,'r*--',linewidth='1.2', markersize=9,label=methods[0]) 
        plt.plot(x,  xf[1] , 'bo-.',linewidth='1.2', markersize=6,label=methods[1])    
   
        
        plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,ncol=1, mode="expand", borderaxespad=0.)
        plt.xticks(x,x_label,fontsize=8)  
        plt.yticks(fontsize=8)   
         
         
        plt.ylim([0.0,1.1])
        plt.xlabel(xlabels[m],fontsize=8) 
        plt.ylabel('F-measure of Nodes',fontsize=8)     
        leg=plt.legend(loc='best',fontsize=8)  
        leg.get_frame().set_alpha(0.5) 
        plt.title(SetTitles[m],fontsize=8)
        plt.grid()
        plt.rc('grid', linestyle="-",linewidth=0.2, color='gray')
         
        plt.subplot(236)      
        plt.plot(x,  yf[0] , 'r*--',linewidth='1.2', markersize=9,label=methods[0]) 
        plt.plot(x,  yf[1] , 'bo-.',linewidth='1.2', markersize=6,label=methods[1])    
         
        
        plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,ncol=1, mode="expand", borderaxespad=0.)
        plt.xticks(x,x_label,fontsize=8)  
        plt.yticks(fontsize=8)        
        plt.ylim([0.0,1.1])
        plt.xlabel(xlabels[m],fontsize=8) 
        plt.ylabel('F-measure of Attributes',fontsize=8)   
        leg=plt.legend(loc='best',fontsize=8)  
        leg.get_frame().set_alpha(0.5) 
        #plt.title('y F-score',fontsize=8)
        plt.rc('grid', linestyle="-",linewidth=0.2, color='gray')
        plt.grid()
        
        
        #plt.tight_layout(pad=0.4, w_pad=0.5, h_pad=1.0)
        plt.show()
        fig.savefig('./'+settings+'.png',dpi=300)
        plt.close()
   
        
        