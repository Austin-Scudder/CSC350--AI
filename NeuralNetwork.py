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
    # train_data = tf.keras.utils.normalize(train_data)
    return x_data, y_data

def run_test(x_train, y_train, x_test, y_test):
    # Create the model
    model = Sequential()
    model.add(Dense(units=1000, input_dim=1024))  # First (hidden) layer
    model.add(Activation('sigmoid'))
    model.add(Dense(units=10))  # Second (hidden) layer
    model.add(Activation('sigmoid'))
    model.add(Dense(units=1))  # Third, final (output) layer
    model.add(Activation('sigmoid'))

    model.compile(loss='mean_squared_error',
                  optimizer='sgd',
                  metrics=['accuracy'])

    # Train the model, iterating on the data in batches of 32 samples (try batch_size=1)
    model.fit(x_train, y_train, epochs=20, batch_size=32)

    # x_test = np.random.randint(2, size=(1000, 2))   # Random input data
    # y_test = np.array([[x and y, x ^ y] for (x, y) in x_test]) # The results (Carry,Add)

    # Evaluate the model from a sample test data set
    score = model.evaluate(x_test, y_test)
    print()
    print("Score was {}.".format(score))
    print("Labels were {}.".format(model.metrics_names))

    # Make a few predictions
    y_pred = model.predict(x_test)
    print("Result of {} is {} should be {}.".format(x_test, y_pred, y_test))


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

x_data, y_data = info_get()
x_data = np.array(x_data)
y_data = np.array(y_data)

# x_train = np.random.randint(2, size=(10000,2))   # Random input data
print(x_data[12])
print(y_data[12])
# plt.imshow(x_train[12], cmap=plt.cm.binary)
# plt.show()

run_test(x_data[:-1], y_data[:-1], x_data[-1:], y_data[-1:])


"""
# this is the main line to run
if __name__ == "__main__":
    main()

"""