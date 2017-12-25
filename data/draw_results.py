import matplotlib as plt
import numpy as np

root_path = '/home/baojian/git/S2GraphMP/data/result_final/'


def get_all_records(file_paths):
    all_records = []
    for each_file_path in file_paths:
        with open(each_file_path) as f:
            for each_line in f.readlines():
                all_records.append(each_line)
    return all_records


def load_result(records):
    result = dict()
    for each_line in records:
        items_ = each_line.rstrip().split(' ')
        if items_[0] not in result:
            result[items_[0]] = dict()
        entries = [float(items_[1]), float(items_[2]), float(items_[3])]
        entries = entries + [float(items_[4]), float(items_[5]), float(items_[6])]
        entries = entries + [float(items_[10]) * 463.57]
        for i in range(7):
            if i not in result[items_[0]]:
                result[items_[0]][i] = []
            result[items_[0]][i].append(entries[i])
    for type_ in result:
        for item_ in result[type_]:
            sum_ = np.sum(result[type_][item_])
            min_ = np.min(result[type_][item_])
            max_ = np.max(result[type_][item_])
            size_ = len(result[type_][item_]) - 2.0
            result[type_][item_] = (sum_ - min_ - max_) / size_
    return result


def load_result_(records):
    result = dict()
    for each_line in records:
        items_ = each_line.rstrip().split(' ')
        if items_[0] not in result:
            result[items_[0]] = dict()
        entries = [float(items_[1]), float(items_[2]), float(items_[3])]
        entries = entries + [float(items_[4]), float(items_[5]), float(items_[6])]
        entries = entries + [float(items_[7])]
        for i in range(7):
            if i not in result[items_[0]]:
                result[items_[0]][i] = []
            result[items_[0]][i].append(entries[i])
    for type_ in result:
        for item_ in result[type_]:
            sum_ = np.sum(result[type_][item_])
            min_ = np.min(result[type_][item_])
            max_ = np.max(result[type_][item_])
            size_ = len(result[type_][item_]) - 2.0
            result[type_][item_] = (sum_ - min_ - max_) / size_
    return result


def draw_table_1():
    print('type, [node_pre, node_rec, node_fm, fea_pre, fea_rec, fea_fm, run_time]')
    print('------------------------ SODA -------------------------')
    paths_ = ['/home/baojian/git/S2GraphMP/data/soda/final_result_SODA_crimes.txt']
    result = load_result(get_all_records(paths_))
    for type_ in result:
        print(type_, ["{:0.3f}".format(result[type_][i]) for i in range(len(result[type_]))])
    print('------------------------ AMEN -------------------------')
    paths_ = ['/home/baojian/git/S2GraphMP/data/amen/result_amen_BATTERY_diff_para.txt',
              '/home/baojian/git/S2GraphMP/data/amen/result_amen_BURGLARY_diff_para.txt']
    result = load_result_(get_all_records(paths_))
    for type_ in result:
        print(type_, ["{:0.3f}".format(result[type_][i]) for i in range(len(result[type_]))])
    print('------------------------ Ours -------------------------')
    paths_ = ['/home/baojian/git/S2GraphMP/outputs/CrimesOfChicago/final_result_S2GraphMP_fix.txt']
    result = load_result(get_all_records(paths_))
    for type_ in result:
        print(type_, ["{:0.3f}".format(result[type_][i]) for i in range(len(result[type_]))])


def draw_table_2():
    result = dict()
    with open('/home/baojian/git/S2GraphMP/outputs/Yelp/result_s2GraphMP_yelp.txt') as f:
        for each_line in f.readlines():
            items_ = each_line.rstrip().split(' ')
            if items_[0] not in result:
                result[items_[0]] = dict()
            entries = [float(items_[1]), float(items_[2]), float(items_[3])]
            entries = entries + [float(items_[4]), float(items_[5]), float(items_[6])]
            entries = entries + [float(items_[10])]
            for i in range(7):
                if i not in result[items_[0]]:
                    result[items_[0]][i] = []
                result[items_[0]][i].append(entries[i])
    for type_ in result:
        for item_ in result[type_]:
            sum_ = np.sum(result[type_][item_])
            min_ = np.min(result[type_][item_])
            max_ = np.max(result[type_][item_])
            size_ = len(result[type_][item_]) - 2.0
            result[type_][item_] = (sum_ - min_ - max_) / size_
    import time
    for type_ in result:
        print(type_, ["{:0.3f}".format(result[type_][i]) for i in range(len(result[type_]))])


def get_keywords_freq():
    keywords = dict()
    with open('/home/baojian/git/S2GraphMP/outputs/Yelp/result_s2GraphMP_yelp.txt') as f:
        for each_line in f.readlines():
            items_ = each_line.rstrip().split(' ,,,,, ')
            for item_ in items_[1].split(' '):
                if item_ not in keywords:
                    keywords[item_] = 0
                keywords[item_] += 1
    for item_ in sorted([(keywords[item], item) for item in keywords], reverse=True):
        print item_


if __name__ == '__main__':
    draw_table_1()
    draw_table_2()
    get_keywords_freq()
