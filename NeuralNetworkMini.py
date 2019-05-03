"""A very simple 30-second Keras example. =)

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


def run_test(x_info, y_labels):
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
    x_train = []
    x_test = []
    # Train the model, iterating on the data in batches of 32 samples (try batch_size=1)
    for train_index, test_index in kfold.split(y_labels):
        x_train = np.array(x_info[train_index])
        x_test = np.array(x_info[test_index])
        y_train = np.array(y_labels[train_index])
        y_test = np.array(y_labels[test_index])

    model.fit(x_train, y_train, epochs=150, batch_size=32, verbose=1)

    # Evaluate the model from a sample test data set

    score = model.evaluate(x_test, y_test)
    print()
    print("Score was {}.".format(score))
    print("Labels were {}.".format(model.metrics_names))

    # Make a few predictions
    print("These are the predictions: ")
    x_pred = model.predict(x_test)
    x_pred = np.array([round(p[0], 0) for p in x_pred])
    for i in range(len(x_pred)):
        print("This is the prediction {} this is the actual{}".format(x_pred[i], y_test[i]))

    model_json = model.to_json()
    with open("model.json", "w") as json_file:
        json_file.write(model_json)
    # serialize weights to HDF5
    model.save_weights("model.h5")
    print("Saved model to disk")
    cm = confusion_matrix(x_pred, y_test)
    print(cm)

"""
# Get the data from the files



# load json and create model
json_file = open('model.json', 'r')
loaded_model_json = json_file.read()
json_file.close()
loaded_model = model_from_json(loaded_model_json)
# load weights into new model
loaded_model.load_weights("model.h5")
print("Loaded model from disk")

# evaluate loaded model on test data
loaded_model.compile(loss='mean_squared_error',
                  optimizer='sgd',
                  metrics=['accuracy'])
score = loaded_model.evaluate(X, Y, verbose=0)
print("%s: %.2f%%" % (loaded_model.metrics_names[1], score[1] * 100))
"""

x_data, y_data = info_get()
x_data = np.array(x_data)
y_data = np.array(y_data)
kfold = ms.KFold(n_splits=15, shuffle=True)




run_test(x_data, y_data)
# confusion_matrix(y_true, y_pred)