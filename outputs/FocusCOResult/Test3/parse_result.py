import numpy as np

def main():
    output=open("../../result/VaryingClusterSizes-FocusCO_0.9.txt","w")
    for clusterSize in [100,150,200,300,400]:
        file_name = 'VaryingClusterSizes_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_clusterSizeLower_30_clusterSizeUpper_' + str(clusterSize) + '_FeasNum_100_trueFeasNum_10_sigmas1_0.0316_0.9.txt'
        data = dict()
        with open(file_name) as f:
            for each_line in f.readlines():
                items = [float(each_item) for each_item in each_line.rstrip().split(" ")]
                for i in range(len(items)):
                    if i not in data:
                        data[i] = []
                    data[i].append(items[i])
        print(str(clusterSize)+" "+str(np.mean(data[0]))+" "+str(np.mean(data[1]))+" "+str(np.mean(data[2]))+" "+str(np.mean(data[3]))+" "+str(np.mean(data[4]))+" "+str(np.mean(data[5])))
        output.write(str(clusterSize)+" "+str(np.mean(data[0]))+" "+str(np.mean(data[1]))+" "+str(np.mean(data[2]))+" "+str(np.mean(data[3]))+" "+str(np.mean(data[4]))+" "+str(np.mean(data[5]))+"\n")
    

def get_single_file_result(num_):
    file_name = 'Test3_APDM_Dense_subgraph_in_0.35_out_0.1_numClusters_10_clusterSizeLower_30_clusterSizeUpper_' \
                + str(num_) + '_FeasNum_100_trueFeasNum_10.txt'
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
#     get_single_file_result(100)
#     get_single_file_result(150)
#     get_single_file_result(200)
#     get_single_file_result(300)
#     get_single_file_result(400)
