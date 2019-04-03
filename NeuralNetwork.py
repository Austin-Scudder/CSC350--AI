"""A very simple 30-second Keras example.

Author: Christian A. Duncan (based off of the example given on Keras site)
Course: CSC350: Intelligent Systems
Term: Spring 2019
"""

from keras.models import Sequential
from keras.layers import Dense, Activation
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


def info_get():
    files = file_get()
    x_data = []
    y_data = []
    for file in files:
        y_data.append([int(file[-8])])
        input_data = np.array(read_data(file)).flatten()
        input_data = [ p/255 for p in input_data ]
        x_data.append(input_data)
    return x_data, y_data


def run_test(x_train, y_train, x_test, y_test):
    # Create the model
    model = Sequential()
    model.add(Dense(units=512, input_dim=1024))  # First (hidden) layer
    model.add(Activation('sigmoid'))
    model.add(Dense(units=100))  # Second (hidden) layer
    model.add(Activation('sigmoid'))
    model.add(Dense(units=1))  # Third, final (output) layer
    model.add(Activation('sigmoid'))

    model.compile(loss='mean_squared_error',
                  optimizer='sgd',
                  metrics=['accuracy'])

    # Train the model, iterating on the data in batches of 32 samples (try batch_size=1)
    model.fit(x_train, y_train, epochs=150, batch_size=32)

    # Evaluate the model from a sample test data set
    score = model.evaluate(x_train, y_train)
    print()
    print("Score was {}.".format(score))
    print("Labels were {}.".format(model.metrics_names))

    # Make a few predictions
    print("This is the prediction: ")
    y_pred = model.predict(x_test)
    pred = round(y_pred[0][0],0)
    print("Result of {} is {} should be {}.".format(x_test, pred, y_test))


x_data, y_data = info_get()
x_data = np.array(x_data)
y_data = np.array(y_data)


run_test(x_data[:-1], y_data[:-1], x_data[-1:], y_data[-1:])

