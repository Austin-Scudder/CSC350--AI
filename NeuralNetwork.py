"""A very simple 30-second Keras example.

Author: Christian A. Duncan (based off of the example given on Keras site)
Course: CSC350: Intelligent Systems
Term: Spring 2019
"""

from keras.models import Sequential
from keras.layers import Dense, Activation
from sklearn.metrics import confusion_matrix
import sklearn.model_selection as ms
import numpy as np
import json
import os


def read_data(file):
    try:
        with open(file, 'r') as inf:
            bitmap = json.load(inf)
        return bitmap
    except FileNotFoundError as err:
        print("File Not Found: {0}.".format(err))


def file_get():
    files = []
    basepath = 'res/'
    with os.scandir(basepath) as entries:
        for entry in entries:
            if entry.is_file():
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
        input_data = [p/255 for p in input_data ]
        x_data.append(input_data)
    return x_data, y_data


def run_test(x_data, y_labels):
    # Create the model
    model = Sequential()
    model.add(Dense(units=512, input_dim=1024))  # First (hidden) layer
    model.add(Activation('sigmoid'))
    model.add(Dense(units=256))  # Second (hidden) layer
    model.add(Activation('sigmoid'))
    model.add(Dense(units=1))  # Third, final (output) layer
    model.add(Activation('sigmoid'))

    model.compile(loss='mean_squared_error',
                  optimizer='sgd',
                  metrics=['accuracy'])

    kfold = ms.KFold(n_splits=10, shuffle=True)
    # Train the model, iterating on the data in batches of 32 samples (try batch_size=1)
    for train_index, test_index in kfold.split(y_data):
        x_train = y_data[train_index]
        x_test = y_data[test_index]
    model.fit(x_train, x_test, epochs=150, batch_size=32, verbose=1)

    # Evaluate the model from a sample test data set
    x_data = np.array(x_data)
    score = model.evaluate(x_train, x_test)
    print()
    print("Score was {}.".format(score))
    print("Labels were {}.".format(model.metrics_names))
    """
    score = model.predict(x_train, len(x_train))
    for i in range(len(x_train)):
        print(score[0][i])
    """
    # Make a few predictions
    print("These are the predictions: ")
    x_pred = model.predict(x_train)
    x_pred = np.array([round(p[0], 0) for p in x_pred])
    print(x_pred)

    print("test")
    print(x_test)

    cm = confusion_matrix(x_test, x_pred)
    print(cm)

# Get the data from the files
x_data, y_data = info_get()
x_data = np.array(x_data)
y_data = np.array(y_data)

for train_index, test_index in kfold.split(x_data):
    x_train = x_data[train_index]
    x_test = x_data[test_index]
    print("Train={} Test={}".format(x_train, x_test))
    run_test(x_train, x_test)

# confusion_matrix(y_true, y_pred)
