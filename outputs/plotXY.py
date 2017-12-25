import matplotlib.pyplot as plt
import numpy as np

def round(x):
    return str(np.round(x,3))
def fscore(p,r):
    if p==0.0 or r==0.0:
        return 0.001
    return 2.0*p*r/(r+p)
method_num=2
mus=[0.5,1.0,2.0,3.0,5.0,10.0]
for trueSub in [5,10,15,20]:#5,15,20]:
    print "--------",trueSub
    xpre=[[] for i in range(method_num)]
    xrec=[[] for i in range(method_num)]
    xf=[[] for i in range(method_num)]
    ypre=[[] for i in range(method_num)]
    yrec=[[] for i in range(method_num)]
    yf=[[] for i in range(method_num)]
    for mu in ["0.5","1.0","2.0","3.0","5.0","10.0"]:#"0.5","1.0","2.0","3.0","5.0"]:#"0.5","1.0","2.0","3.0","5.0","10.0"]:
        
        #k=[[] for i in range(method_num)]
        #s=[[] for i in range(method_num)]
        #f_val=[]
        methdos=["S2GraphMP2","lasso_truesub"]
        for i,method in enumerate(methods):
            fileName="./"+method+"_"+str(trueSub)+"_mu_"+mu+".txt"
            xp=[]
            xr=[]
            yp=[]
            yr=[]
            with open(fileName,"r") as f:
                for line in f.readlines():
                    terms=line.strip().split(" ")
                    #k.append(float(terms[0]))
                    #s.append(float(terms[1]))
                    xp.append(float(terms[2]))
                    xr.append(float(terms[3]))
                    yp.append(float(terms[4]))
                    yr.append(float(terms[5]))                
                    #f_val.append(float(terms[6]))
    #                 k.append(float(terms[4]))
    #                 s.append(float(terms[5]))
    #                 xpre.append(float(terms[0]))
    #                 xrec.append(float(terms[1]))
    #                 ypre.append(float(terms[2]))
    #                 yrec.append(float(terms[3]))
                    #f_val.append(float(terms[6]))
            xpre[i].append(np.mean(xp))
            xrec[i].append(np.mean(xr))
            ypre[i].append(np.mean(yp))
            yrec[i].append(np.mean(yr))
            xf[i].append(fscore(np.mean(xp), np.mean(xr)))
            yf[i].append(fscore(np.mean(yp), np.mean(yr)))
            
            #print str(mu)+" & "+round(np.mean(k))+" & "+round(np.mean(s))+" & "+round(np.mean(xpre))+" & "+round(np.std(xpre))+" & "+round(np.mean(xrec))+" & "+round(np.std(xrec))+"\\\\ \hline" #+" & "+round(np.mean(f_val))+"\\\\ \hline" #+" "+round(np.mean(ypre))+" std="+round(np.std(ypre))+" "+round(np.mean(yrec))+" std="+" "+round(np.std(yrec))
            print str(mu)+" & "+round(np.mean(k))+" & "+round(np.mean(s))+" & "+round(np.mean(ypre))+" & "+round(np.std(ypre))+" & "+round(np.mean(yrec))+" & "+round(np.std(yrec))+"\\\\ \hline" #+" & "+round(np.mean(f_val))+"\\\\ \hline" #+" "+round(np.mean(ypre))+" std="+round(np.std(ypre))+" "+round(np.mean(yrec))+" std="+" "+round(np.std(yrec))
            
        #print str(mu)+"\ \  \ \ "+round(np.mean(ypre))+" "+" std="+" "+round(np.std(ypre))+" "+round(np.mean(yrec))+" std="+" "+round(np.std(yrec)) +"\\\\"#+" "+round(np.mean(ypre))+" std="+round(np.std(ypre))+" "+round(np.mean(yrec))+" std="+" "+round(np.std(yrec))
    
    fig=plt.figure(1)
    plt.subplot(311)
    #x=[i for i in range(len(mu))] 
    #print xpre,x
     
    plt.plot(mus,  xpre[0] , 'r*-',linewidth='2.0', markersize=5,label='x pre S2GraphMP2') 
    plt.plot(mus,  xpre[1] , 'bo-',linewidth='2.0', markersize=5,label='x pre lasso_truesub')     
    plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,ncol=1, mode="expand", borderaxespad=0.)
    #plt.plot([0.2,0.1,0.0],[0.5,0.5,0.5])
     
     
    plt.ylim([0.0,1.1])
    plt.xlabel('mu')
    #plt.xlim([0.2,0.0])
    plt.legend(loc='best',fontsize=14)
    plt.title('mu='+str(mu)+' X Precision Plot Truesub= '+str(trueSub))
     
    plt.subplot(312)
     
    plt.plot(mus,  xrec[0] , 'r*-',linewidth='2.0', markersize=5,label='x rec S2GraphMP2') 
    plt.plot(mus,  xrec[1] , 'bo-',linewidth='2.0', markersize=5,label='x rec lasso_truesub')     
    plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,ncol=1, mode="expand", borderaxespad=0.)
    #plt.plot([0.2,0.1,0.0],[0.5,0.5,0.5])
     
     
    plt.ylim([0.0,1.1])
    plt.xlabel('mu')
    #plt.xlim([0.2,0.0])
    plt.legend(loc='best',fontsize=14)
    plt.title('mu='+str(mu)+' X Recall Plot Truesub= '+str(trueSub))
     
    plt.subplot(313)
     
    plt.plot(mus,  xf[0] , 'r*-',linewidth='2.0', markersize=5,label='x f-score S2GraphMP2') 
    plt.plot(mus,  xf[1] , 'bo-',linewidth='2.0', markersize=5,label='x f-score lasso_truesub')     
    plt.legend(bbox_to_anchor=(0., 1.02, 1., .102), loc=3,ncol=1, mode="expand", borderaxespad=0.)
    #plt.plot([0.2,0.1,0.0],[0.5,0.5,0.5])
     
     
    plt.ylim([0.0,1.1])
    plt.xlabel('mu')
    #plt.xlim([0.2,0.0])
    plt.legend(loc='best',fontsize=14)
    plt.title('mu='+str(mu)+' X F-score Plot Truesub= '+str(trueSub))
    
    plt.tight_layout(pad=0.4, w_pad=0.5, h_pad=1.0)
    #plt.show()
    fig.savefig('./trueSub-'+str(trueSub)+'.png')
    plt.close()