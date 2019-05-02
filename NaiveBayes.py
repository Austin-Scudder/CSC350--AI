#Put the code for the naive bayes classifier here
from sklearn.naive_bayes import GaussianNB
import numpy as np
import json
import os
import sklearn.model_selection as ms
from sklearn.metrics import confusion_matrix


def read_data(file):
    try:
        with open(file, 'r') as inf:
            bitmap = json.load(inf)
        return bitmap
    except FileNotFoundError as err:
        print("File Not Found: {0}.".format(err))


def file_get():
    files = []
    basepath = 'DataSetAI/'
    with os.scandir(basepath) as entries:
        for entry in entries:
            if entry.is_file() and entry.name[-4] == 'j':
                files.append(basepath + entry.name)
    return files


# This gets the data from the files and places it into the labels y and the actual 2D array in x
def info_get():
    files = file_get()
    x_data = []
    y_data = []
    for file in files:
        y_data.append([int(file[-8])])
        input_data = np.array(read_data(file)).flatten()
        input_data = [p/255 for p in input_data]
        x_data.append(input_data)
    return x_data, y_data


model = GaussianNB()
x_info, y_labels = info_get()
y_labels = np.array(y_labels)
x_info = np.array(x_info)

kfold = ms.KFold(n_splits=10, shuffle=True)

for train_index, test_index in kfold.split(y_labels):
    x_train = np.array(x_info[train_index])
    x_test = np.array(x_info[test_index])
    y_train = np.array(y_labels[train_index])
    y_test = np.array(y_labels[test_index])

model.fit(x_info, y_labels.flatten())
GaussianNB(priors=None, var_smoothing=1024)

index = 0
predictions = []
for element in model.predict(x_test):
    predictions.append(element)
    print("Predicted : {} Actual {}" .format(element, y_test[index]))
    index += 1
print("Accuracy {} " .format(model.score(x_test, y_test)))

cm = confusion_matrix(y_test, predictions)
print(cm)


