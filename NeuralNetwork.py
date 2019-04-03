"""A very simple 30-second Keras example.

Author: Christian A. Duncan (based off of the example given on Keras site)
Course: CSC350: Intelligent Systems
Term: Spring 2017

This one trains on the function "1-bit add with carry".
Examples are 
   ((0, 0),(0, 0),
    (0, 1),(0, 1),
    (1, 0),(0, 1),
    (1, 1),(1, 0))
   This requires two inputs and two outputs with 2 nodes in the hidden layer.
"""

from keras.models import Sequential
from keras.layers import Dense, Activation
import numpy as np
import json
import os
import matplotlib.pyplot as plt
import tensorflow as tf
import sys

# Make the test set vs the training set.


def read_data(file):
    try:
        with open(file, 'r') as inf:
            bitmap = json.load(inf)
        return bitmap
    except FileNotFoundError as err:
        print("File Not Found: {0}.".format(err))


def file_get():
    files = []
    basepath = '/Users/AustinS/PycharmProjects/CSC350--AI/res/'
    with os.scandir(basepath) as entries:
        for entry in entries:
            if entry.is_file():
                    files.append(basepath + entry.name)
    return files


def info_get():
    files = file_get()
    test_set = []
    train_set = []
    test_data = []
    train_data = []
    for file in files:
        if file[-6] == "9":
            test_set.append(file[-8])
            test_data.append(read_data(file))
        else:
            train_set.append(file[-8])
            train_data.append(read_data(file))
    return train_data, test_data, train_set, test_set


def main():
    file_list = file_get()
    img = ""
    for file_name in file_list:
        img = read_data(file_name)
        # print("Displaying image: {0}.".format(file_name))
        # print_image(img, 200)  # Different thresholds will change what shows up as X and what as a .
    return img  # edit the return to only giv ethe array




"""x_train = np.random.randint(2, size=(10000,2))   # Random input data
y_train = np.array([[x and y, x^y] for (x,y) in x_train]) # The results (Carry,Add)"""

x_train, x_test, y_train, y_test = np.array(info_get())

# x_train = np.random.randint(2, size=(10000,2))   # Random input data
print(len(x_train))
print(len(y_train))
print(x_train[12])
print(y_train[12])
# plt.imshow(x_train[12], cmap=plt.cm.binary)
# plt.show()
"""
model = Sequential()
model.add(Dense(units=1024, input_dim=2)) # First (hidden) layer
model.add(Activation('sigmoid'))
model.add(Dense(units=32)) # First (hidden) layer
model.add(Activation('sigmoid'))
model.add(Dense(units=2))   # Second, final (output) layer
model.add(Activation('sigmoid'))

model.compile(loss='mean_squared_error',
              optimizer='sgd',
              metrics=['accuracy'])


# Train the model, iterating on the data in batches of 32 samples (try batch_size=1)
model.fit(x_train, y_train, epochs=10, batch_size=2)

# x_test = np.random.randint(2, size=(1000, 2))   # Random input data
# y_test = np.array([[x and y, x ^ y] for (x, y) in x_test]) # The results (Carry,Add)

# Evaluate the model from a sample test data set
score = model.evaluate(x_test, y_test)
print()
print("Score was {}.".format(score))
print("Labels were {}.".format(model.metrics_names))

# Make a few predictions
x_input = np.array([[0,0], [0,1], [1,0], [1,1]])
y_output = model.predict(x_input)
print("Result of {} is {}.".format(x_input, y_output))






# this is the main line to run
if __name__ == "__main__":
    main()

"""