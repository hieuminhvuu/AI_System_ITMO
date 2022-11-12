import random

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from sklearn.model_selection import train_test_split
from sklearn.metrics import accuracy_score, precision_recall_curve, precision_score, recall_score, roc_curve, auc, average_precision_score, RocCurveDisplay, PrecisionRecallDisplay, roc_auc_score

dict_num_name = {'1': 'Age',
                 '2': 'Sex',
                 '3': 'Graduated high_school type',
                 '4': 'Scholarship type',
                 '5': 'Additional Work',
                 '6': 'Regular activity?',
                 '7': 'Have partner?',
                 '8': 'Total salary available',
                 '9': 'Transportation to uni',
                 '10': 'Accommodation type in Cyprus',
                 '11': 'Mother education',
                 '12': 'Father Education',
                 '13': 'Number of sisters/brothers',
                 '14': 'Parental status',
                 '15': 'Mother occupation',
                 '16': 'Father occupation',
                 '17': 'Weekly study hour',
                 '18': 'Reading frequency non-scientific',
                 '19': 'Reading frequency scientific',
                 '20': 'Attendance to the seminars/conferences related to the department',
                 '21': 'Impact of your projects/activities on your success',
                 '22': 'Attendance to classes',
                 '23': ' Preparation to midterm exams 1',
                 '24': ' Preparation to midterm exams 2',
                 '25': 'Taking notes in classes',
                 '26': 'Listening in classes',
                 '27': 'Discussion improves my interest and success in the course',
                 '28': 'Flip-classroom',
                 '29': 'Cumulative grade point average in the last semester',
                 '30': 'Expected Cumulative grade point average in the graduation',
                 'COURSE ID':  'Course ID',
                 'GRADE': 'OUTPUT Grade'
                 }


def analyze(predict, expect):
    print("accuracy_score:", accuracy_score(expect, predict))
    print("precision_score:", precision_score(
        expect, predict, average="micro"))
    print("recall_score:", recall_score(expect, predict, average="micro"))


def draw_plt(predict_arr, expect_arr):
    y_true = np.array([0 if x == 'passed' else 1 for x in predict_arr])
    y_score = np.array([0 if x == 'passed' else 1 if x ==
                       'not passed' else '-1' for x in expect_arr])

    fpr, tpr, _ = roc_curve(y_true, y_score)
    roc_display = RocCurveDisplay(fpr=fpr, tpr=tpr).plot()
    precision, recall, _ = precision_recall_curve(y_true, y_score)
    pr_display = PrecisionRecallDisplay(
        precision=precision, recall=recall).plot()
    auc_roc = auc(fpr, tpr)
    auc_pr = average_precision_score(y_true, y_score)
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(12, 8))
    roc_display.plot(ax=ax1)
    pr_display.plot(ax=ax2)
    plt.show()


class TreeNode:
    def __init__(self, feature_names, eps=0.03, depth=10, min_leaf_size=1):
        self.children = {}
        self.decision = None
        self.feature_names = feature_names
        self.split_feature_name = None
        self.split_feature_index = None
        self.eps = eps
        self.depth = depth
        self.min_leaf_size = min_leaf_size

    def get_entropy(self, x):
        entropy = 0
        for x_value in set(x):
            p = x[x == x_value].shape[0] / x.shape[0]
            entropy -= p * np.log2(p)
        return entropy

    def information_gain(self, x, y):
        entropy = 0
        for x_value in set(x):
            sub_y = y[x == x_value]
            tmp_ent = self.get_entropy(sub_y)
            p = sub_y.shape[0]/y.shape[0]
            entropy += p*tmp_ent
        return self.get_entropy(y) - entropy

    def fit(self, X, y):
        self._built_tree(X, y, 0)

    def getMajorClass(self, y):
        sum = 0
        result = None
        for x in set(y):
            if np.sum(y == x) > sum:
                sum = np.sum(y == x)
                result = x
        return result

    def _built_tree(self, X, y, depth):
        if (len(set(y))) == 1:
            self.decision = y[0]
            return
        if len(X[0]) == 0:
            self.decision = self.getMajorClass(y)
            return
        if depth > self.depth:
            self.decision = self.getMajorClass(y)
            return
        if len(y) < self.min_leaf_size:
            self.decision = self.getMajorClass(y)
            return

        self.children = {}
        best_feature_index = 0
        max_gain = 0
        for feature_index in range(len(X[0])):
            gain = self.information_gain(X[:, feature_index], y)
            if max_gain < gain:
                max_gain = gain
                best_feature_index = feature_index

        if max_gain < self.eps:
            self.decision = self.getMajorClass(y)
            return

        self.split_feature_name = self.feature_names[best_feature_index]
        self.split_feature_index = cols.index(self.split_feature_name)

        for best_feature in set(X[:, best_feature_index]):
            index = X[:, best_feature_index] == best_feature
            sub_X = X[index]
            sub_X = np.delete(sub_X, best_feature_index, 1)
            sub_col = np.delete(self.feature_names, best_feature_index)
            # print(sub_X.shape)
            if len(X[index]) > 0:
                self.children[best_feature] = TreeNode(feature_names=sub_col)
                self.children[best_feature]._built_tree(
                    sub_X, y[index], depth+1)
            else:
                self.children[best_feature] = TreeNode(
                    feature_names=sub_col, depth=1)
                self.children[best_feature]._built_tree(
                    sub_X, y[index], depth+1)

    def predict(self, x):
        if self.decision is not None:
            return self.decision
        else:
            attr_val = x[self.split_feature_index]
            try:
                child = self.children[attr_val]
            except KeyError:
                return '?'
            return child.predict(x)

    def pretty_print(self, prefix=''):
        if self.split_feature_name is not None:
            for k, v in self.children.items():
                v.pretty_print(
                    f"{prefix}:When {self.split_feature_name} is {k}")
        else:
            print(f"{prefix}:{self.decision}")


if __name__ == '__main__':
    data = pd.read_csv("DATA.csv", index_col=0, sep=';')
    data['Decision'] = data['GRADE'].apply(
        lambda x: "not passed" if x <= 4 else "passed")
    X = data.iloc[:, :-2]
    Y = data.iloc[:, -1]
    X = X.sample(n=6, axis=1)
    cols = X.columns.tolist()
    cols_new = [dict_num_name.get(str(x)) for x in cols]
    X = X.values
    Y = Y.values
    X_train, X_test, Y_train, Y_test = train_test_split(
        X, Y, test_size=0.2, random_state=3)
    clf = TreeNode(feature_names=cols)
    clf.fit(X_train, Y_train)
    # clf.pretty_print()
    X_test_ndarray = pd.DataFrame(X_test, columns=cols)
    # print(X_test_ndarray)
    predict = X_test_ndarray.apply(lambda row: clf.predict(row), axis=1)

    print(predict)
    analyze(predict, Y_test)
    draw_plt(predict, Y_test)
