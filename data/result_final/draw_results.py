import matplotlib as plt
import numpy as np

root_path = 'F:/workspace/git/SG-Pursuit/data/result_final/'


def load_result(file_path_):
    result = dict()
    with open(root_path + file_path_) as f:
        for each_line in f.readlines():
            items_ = each_line.rstrip().split(' ')
            if items_[0] not in result:
                result[items_[0]] = dict()
            entries = [float(items_[1]), float(items_[2]), float(items_[3])]
            entries = entries + [float(items_[4]), float(items_[5]), float(items_[6])]
            if len(items_)>8:
                entries = entries + [float(items_[10])]
            else:
                entries = entries + [float(items_[7])]                
            for i in range(7):
                if i not in result[items_[0]]:
                    result[items_[0]][i] = []
                result[items_[0]][i].append(entries[i])
            #print result[items_[0]][i]
    for type_ in result:        
        for item_ in result[type_]:
            sum_ = np.sum(result[type_][item_])
            min_ = np.min(result[type_][item_])
            max_ = np.max(result[type_][item_])
            size_ = len(result[type_][item_]) - 2.0
            result[type_][item_] = (sum_ - min_ - max_) / size_
    return result


def draw_table():
    print('type, [node_pre, node_rec, node_fm, fea_pre, fea_rec, fea_fm, run_time]')
    print('------------------------ SODA -------------------------')
    result = load_result('final_result_SODA_New.txt')
    for type_ in result:
        print(type_, ["{:0.6f}".format(result[type_][i]) for i in range(len(result[type_]))])
    print('------------------------ AMEN -------------------------')
    result = load_result('final_result_AMEN_New.txt')
    for type_ in result:
        print(type_, ["{:0.6f}".format(result[type_][i]) for i in range(len(result[type_]))])
    print('------------------------ Ours -------------------------')
    result = load_result('final_result_S2GraphMP.txt')
    for type_ in result:
        print(type_, ["{:0.6f}".format(result[type_][i]) for i in range(len(result[type_]))])


if __name__ == '__main__':
    draw_table()
