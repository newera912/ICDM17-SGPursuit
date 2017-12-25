#!/usr/bin/python

import sys
import numpy as np
import scipy.optimize as opt
import matplotlib.pyplot as plt
from matplotlib import cm
import time
import random

debug_level = 0


def log_exp(x):
    if x < -50.0:
        return 0.0
    elif x >= 50.0:
        return x
    else:
        return np.log(1.0 + np.exp(x))


def logistic_loss(x, theta, y):
    x_prod_theta = np.dot(x, theta)
    return - x_prod_theta * y + log_exp(x_prod_theta)


def func_d(theta, x, y, C):
    norm_theta = np.linalg.norm(theta)
    term1 = 0.5 * norm_theta * norm_theta
    term2 = C * logistic_loss(x, theta, y)
    return term1 + term2


def func_psi(theta, x, y, C):
    if y <= 0.0 or y >= 1.0:
        print 'error.'
        sys.exit(0)
    [theta0, theta1] = get_solution_thetas(theta, x, y, C, -100.0, 100.0)
    term1 = (1.0 - y) * func_d(theta0, x, 0.0, C)
    term2 = y * func_d(theta1, x, 1.0, C)
    return term1 + term2


def func_bisect(z, x, theta, y, C):
    para_l0 = np.dot(np.subtract(theta, np.multiply(y * C * z, x)), x)
    if para_l0 <= -800.0:
        term1 = 0.0
    elif para_l0 >= 50.0:
        term1 = 1.0
    else:
        term1 = np.exp(para_l0) / (1.0 + np.exp(para_l0))
    para_l1 = np.dot(np.add(theta, np.multiply((1 - y) * C * z, x)), x)
    if para_l1 <= -800.0:
        term2 = 0.0
    elif para_l1 >= 50.0:
        term2 = 1.0
    else:
        term2 = -1.0 + np.exp(para_l1) / (1.0 + np.exp(para_l1))
    return term1 - term2 - z


def get_solution_thetas(theta, x, y, C, a, b):
    z = opt.bisect(func_bisect, a, b, (x, theta, y, C))
    theta_0 = np.subtract(theta, np.multiply(y * C * z, x))
    theta_1 = np.subtract(np.multiply(C * z, x) + theta, np.multiply(y * C * z, x))
    return [theta_0, theta_1]


def convex_extension_func_d(theta, x, y, C):
    if y == 0.0 or y == 1.0:
        return func_d(theta, x, y, C)
    else:
        return func_psi(theta, x, y, C)


def test_l2_regularization():
    start_time = time.time()
    x = np.array([1.0])
    C = 16
    # set up a figure twice as wide as it is tall
    fig = plt.figure()
    ax = fig.add_subplot(1, 1, 1, projection='3d')
    # plot a 3D surface like in the example mplot3d/surface3d_demo
    theta = np.arange(-2.5, 2.5, 0.01)
    y = np.arange(1.0, 0.0, -0.01)
    X, Y = np.meshgrid(theta, y)
    func_vals = []
    for i in range(0, X.shape[0]):
        for j in range(0, X.shape[1]):
            theta_0 = X[i][j]
            y_0 = Y[i][j]
            x = 1.0
            C = 16.0
            func_vals.append(convex_extension_func_d(theta_0, x, y_0, C))
    print 'the running time: ' + str(time.time() - start_time)
    func_vals = np.array(func_vals).reshape((X.shape[0], X.shape[1]))
    surf = ax.plot_surface(Y, X, func_vals, rstride=1, cstride=1, cmap=cm.coolwarm, linewidth=0)
    print np.min(func_vals)
    ax.scatter(0.16635469, -1.3117574, 4.0292518261505537, c='r', marker='D', s=5)
    plt.gca().invert_xaxis()
    fig.colorbar(surf, shrink=0.5, aspect=10)
    plt.show()


def sub_gradient_convex_ext_func_d(theta, x, y, C):
    [theta_0, theta_1] = get_solution_thetas(theta, x, y, C, -100, 100)
    x_prod_theta0 = np.dot(x, theta_0)
    if x_prod_theta0 <= -800.0:
        term1 = 0.0
    elif x_prod_theta0 >= 50.0:
        term1 = 1.0
    else:
        term1 = np.exp(x_prod_theta0) / (1.0 + np.exp(x_prod_theta0))
    gradient_d0_theta = theta_0 + np.multiply(C * term1, x)
    x_prod_theta1 = np.dot(x, theta_1)
    if x_prod_theta1 <= -800.0:
        term2 = 0.0
    elif x_prod_theta1 >= 50.0:
        term2 = 1.0
    else:
        term2 = np.exp(x_prod_theta1) / (1.0 + np.exp(x_prod_theta1))
    gradient_d1_theta = theta_1 + np.multiply(C * (-1.0 + term2), x)
    v = gradient_d0_theta
    if debug_level > 0:
        print 'two of these should be equal: '
        print gradient_d0_theta, gradient_d1_theta
    if y == 0.0:
        return np.append(v, 1e10)
    elif y == 1.0:
        return np.append(v, -1e10)
    else:
        d1_val = func_d(theta_1, x, 1.0, C)
        d0_val = - func_d(theta_0, x, 0.0, C)
        w = np.dot(v, theta_0) - np.dot(v, theta_1) + d1_val + d0_val
        return np.append(v, w)


def gradient_descent(x, C):
    num_iteration = 0
    maximum_iteration = 10000
    eta = 0.001
    error_tol = 1e-10
    x_old = np.array([random.uniform(-10.0, 10.0), random.uniform(0.0, 1.0)])
    while 1:
        theta = x_old[0]
        y = x_old[1]
        sub_gradient = sub_gradient_convex_ext_func_d(theta, x, y, C)
        x_new = x_old - np.multiply(eta, sub_gradient)
        if x_new[1] > 1.0:
            x_new[1] = 1.0
        elif x_new[1] < 0.0:
            x_new[1] = 0.0
        gap = np.linalg.norm(np.subtract(x_new, x_old))
        func_value = convex_extension_func_d(theta, x, y, C)
        # print 'current gap: ' + str(gap) + " current func value: " + str(func_value)
        if error_tol > gap or num_iteration > maximum_iteration:
            break
        num_iteration += 1
        x_old = x_new
    return [func_value, x_new]


def test_objective_function():
    start_time = time.time()
    x = np.array([1.0])
    C = 16
    # set up a figure twice as wide as it is tall
    fig = plt.figure()
    ax = fig.add_subplot(1, 1, 1, projection='3d')
    # plot a 3D surface like in the example mplot3d/surface3d_demo
    theta = np.arange(-2.5, 2.5, 0.01)
    y = np.arange(1.0, 0.0, -0.01)
    X, Y = np.meshgrid(theta, y)
    func_vals = []
    maximum_entry = [1e10, 0.0, 0.0]
    for i in range(0, X.shape[0]):
        for j in range(0, X.shape[1]):
            theta_0 = X[i][j]
            y_0 = Y[i][j]
            x = 1.0
            C = 16.0
            obj = np.exp(np.dot(x, theta_0) * y_0)
            obj = - np.dot(x, theta_0) * y_0 + log_exp(np.dot(x, theta_0))
            val = obj / (1 + np.exp(np.dot(x, theta_0)))
            val = 0.5 * np.dot(theta_0, theta_0) + obj
            func_vals.append(val)
            if val < maximum_entry[0]:
                maximum_entry[0] = val
                maximum_entry[1] = y_0
                maximum_entry[2] = theta_0
    print 'the running time: ' + str(time.time() - start_time)
    func_vals = np.array(func_vals).reshape((X.shape[0], X.shape[1]))
    surf = ax.plot_surface(Y, X, func_vals, rstride=1, cstride=1, cmap=cm.coolwarm, linewidth=0)
    print np.min(func_vals)
    print maximum_entry
    ax.scatter(maximum_entry[1], maximum_entry[2], maximum_entry[0], c='r', marker='D', s=5)
    plt.gca().invert_xaxis()
    fig.colorbar(surf, shrink=0.5, aspect=10)
    plt.show()


if __name__ == '__main__':
    # test_objective_function()
    # test_l2_regularization()
    x = np.array([1.0])
    C = 16
    for i in range(0, 100):
        minimumVal = gradient_descent(x, C)
        print 'minimumVal: ' + str(minimumVal[0]) + ' [theta, y]: ' + str(minimumVal[1])
