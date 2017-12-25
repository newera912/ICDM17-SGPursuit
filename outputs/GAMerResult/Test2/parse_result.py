import numpy as np

def main():
    output=open("../../result/VaryingNumOfClusters-GAMer.txt","w")
    for clusterNum in [10,12,14,15,20,25]:
        file_name = 'VaryingNumOfClusters_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_' + str(clusterNum) + '_TrueSGSize_30_FeasNum_100_trueFeasNum_10_sigmas1_0.0316.txt'
        data = dict()
        with open(file_name) as f:
            for each_line in f.readlines():
                items = [float(each_item) for each_item in each_line.rstrip().split(" ")]
                for i in range(len(items)):
                    if i not in data:
                        data[i] = []
                    data[i].append(items[i])
        print(str(clusterNum)+" "+str(np.mean(data[0]))+" "+str(np.mean(data[1]))+" "+str(np.mean(data[2]))+" "+str(np.mean(data[3]))+" "+str(np.mean(data[4]))+" "+str(np.mean(data[5])))
        output.write(str(clusterNum)+" "+str(np.mean(data[0]))+" "+str(np.mean(data[1]))+" "+str(np.mean(data[2]))+" "+str(np.mean(data[3]))+" "+str(np.mean(data[4]))+" "+str(np.mean(data[5]))+"\n")
    

def get_single_file_result(num_):
    #            Test2_APDM_Dense_subgraph_in_0.35_out_0.1_Cluster_10_      TrueSGSize_30_FeaNum_100_trueFeasNum_10
    file_name = 'Test2_APDM_Dense_subgraph_in_0.35_out_0.1_Cluster_' + str(
        num_) + '_TrueSGSize_30_FeaNum_100_trueFeasNum_10.txt'
    data = dict()
    with open(file_name) as f:
        for each_line in f.readlines():
            items = [float(each_item) for each_item in each_line.rstrip().split(" ")]
            for i in range(len(items)):
                if i not in data:
                    data[i] = []
                data[i].append(items[i])
    print('node_fm: ', np.mean(data[2]), 'feature_fm: ', np.mean(data[5]))


if __name__ == '__main__':
    main()
#     get_single_file_result(10)
#     get_single_file_result(12)
#     get_single_file_result(14)
#     get_single_file_result(15)
#     get_single_file_result(20)
#     get_single_file_result(25)
