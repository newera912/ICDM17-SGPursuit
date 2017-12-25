import math
import matplotlib.pyplot as plt
import numpy as np


def sigmoid(x):
    a = []
    for item in x:
        a.append(1 / (1 + math.exp(-item)))
    return a


def test1():
    x = np.arange(-10., 10., 0.2)
    sig = sigmoid(x)
    x = [1, 5, 10, 15, 20, 25, 30]
    sig = [1.0, 1.0, 1.0, 0.967741935483871, 1.0, 0.8823529411764706, 0.34285714285714286]

    print sig
    plt.plot(x, sig)
    plt.show()


mu = [0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0]
pre = []
rec = []
fmeasure = []
for eachLine in open('result.txt').readlines():
    eachLine = eachLine.rstrip()
    pre.append(eachLine.split(' ')[0])
    rec.append(eachLine.split(' ')[1])
    fmeasure.append(eachLine.split(' ')[2])
print mu
print pre
print rec
print fmeasure
plt.plot(mu, rec, '-gD', linewidth='4')
plt.ylim([0.0, 1.1])
plt.xlabel('mu')
plt.ylabel('precision')
plt.show()
