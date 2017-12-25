import numpy as np


def main():
    output=open("../../result/VaryingNumOfAttributes-FocusCO_0.9.txt","w")
    for attNum in [20,40,80,100]:
        file_name = 'VaryingNumOfAttributes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_TrueSGSize_30_FeasNum_'+str(attNum)+'_trueFeasNum_10_sigmas1_0.0316_0.9.txt'
        data = dict()
        with open(file_name) as f:
            for each_line in f.readlines():
                items = [float(each_item) for each_item in each_line.rstrip().split(" ")]
                for i in range(len(items)):
                    if i not in data:
                        data[i] = []
                    data[i].append(items[i])
        print(str(attNum)+" "+str(np.mean(data[0]))+" "+str(np.mean(data[1]))+" "+str(np.mean(data[2]))+" "+str(np.mean(data[3]))+" "+str(np.mean(data[4]))+" "+str(np.mean(data[5])))
        output.write(str(attNum)+" "+str(np.mean(data[0]))+" "+str(np.mean(data[1]))+" "+str(np.mean(data[2]))+" "+str(np.mean(data[3]))+" "+str(np.mean(data[4]))+" "+str(np.mean(data[5]))+"\n")
    

def get_single_file_result(num_):
    file_name = 'VaryingNumOfAttributes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_TrueSGSize_30_FeasNum_'+str(num_)+'_trueFeasNum_10_sigmas1_0.0316.txt'
    data = dict()
    with open(file_name) as f:
        for each_line in f.readlines():
            items = [float(each_item) for each_item in each_line.rstrip().split(" ")]
            for i in range(len(items)):
                if i not in data:
                    data[i] = []
                data[i].append(items[i])
    print('node_fm: ',np.mean(data[2]),'feature_fm: ',np.mean(data[5]))

if __name__ == '__main__':
    main()
#     num_ = 20
#     get_single_file_result(20)
#     get_single_file_result(40)
#     get_single_file_result(80)
#     get_single_file_result(100)
