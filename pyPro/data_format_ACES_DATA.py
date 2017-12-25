__author__ = 'fengchen'

import pickle


datasets = ['U133A_combat_DMFS', 'U133A_combat_RFS']
networks = [
    'nwEdgesKEGG',
    'nwEdgesHPRD9',
    'nwEdgesI2D',
    'nwEdgesIPP'
]

for dataset in datasets[:]:
    for network in networks[:]:
        print "\n############################\n"

        print dataset, network
        f = open('../../../Dropbox/expriments/ACES_Data/data/{0}_{1}_data.pkl'.format(dataset, network),'rb')
        data = pickle.load(f)

        print '5 gene labels: ', data.geneLabels[:5] # print out 10 genel labels

        n = data.expressionData.shape[1]
        p = data.expressionData.shape[0]
        # The type of data.expressionData is ndarray
        print 'Number of patients: {0}; number of genes:{1}'.format(p, n)

        # The type of data.patientClassLabels is ndarray.
        print 'The class labels of five patients: ', data.patientClassLabels[:5]

        npos = 0
        for label in data.patientClassLabels:
            if label == True:
                npos += 1
        nneg = p - npos

        print 'Count of positive patients: {0}; Count of negative patients: {1}'.format(npos, nneg)

        # The type of data.patientLabels is ndarray
        print 'The labels of five patients', data.patientLabels[:5]

        f = open('/home/baojian/Dropbox/expriments/ACES Data/data/{0}_{1}_net.pkl'.format(dataset, network),'rb')
        net = pickle.load(f)

        list_edges = []
        for edge in net.edges:
            e = []
            for a in edge:
                e.append(a)
            list_edges.append(e)

        print 'Count of edges: {0}'.format(len(list_edges))




