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
import sys


def read_data(file):
    try:
        with open(file, 'r') as inf:
            bitmap = json.load(inf)
        return bitmap
    except FileNotFoundError as err:
        print("File Not Found: {0}.".format(err))


def print_image(img, threshold):
    for row in img:
        for pixel in row:
            print('.' if pixel > threshold else 'X', end='')
        print()  # Newline at end of the row


def file_get():
    files = []
    basepath = '/Users/AustinS/PycharmProjects/CSC350--AI/res'
    with os.scandir(basepath) as entries:
        for entry in entries:
            if entry.is_file():
                files.append('/Users/AustinS/PycharmProjects/CSC350--AI/res/'+entry.name)
    return files


def main():
    file_list = file_get()
    img = ""
    for file_name in file_list:
        img = read_data(file_name)
        # print("Displaying image: {0}.".format(file_name))
        # print_image(img, 200)  # Different thresholds will change what shows up as X and what as a .
    return img


model = Sequential()
model.add(Dense(units=1024, input_dim=2)) # First (hidden) layer
model.add(Activation('sigmoid'))
model.add(Dense(units=4))  # First (hidden) layer
model.add(Activation('sigmoid'))
model.add(Dense(units=2))   # Second, final (output) layer
model.add(Activation('sigmoid'))

model.compile(loss='mean_squared_error',
              optimizer='sgd',
              metrics=['accuracy'])

x_train = np.random.randint(2, size=(10000,2))   # Random input data
y_train = np.array([[x and y, x^y] for (x,y) in x_train]) # The results (Carry,Add)

# Train the model, iterating on the data in batches of 32 samples (try batch_size=1)
model.fit(x_train, y_train, epochs=10, batch_size=8)

x_test = np.random.randint(2, size=(1000,2))   # Random input data
y_test = np.array([[x and y, x^y] for (x,y) in x_test]) # The results (Carry,Add)

# Evaluate the model from a sample test data set
score = model.evaluate(x_test, y_test)
print()
print("Score was {}.".format(score))
print("Labels were {}.".format(model.metrics_names))

# Make a few predictions
x_input = np.array([[0,0], [0,1], [1,0], [1,1]])
y_output = model.predict(x_input)
print("Result of {} is {}.".format(x_input, y_output))