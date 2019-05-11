"""A very simple 30-second Keras example.

Author: Christian A. Duncan (based off of the example given on Keras site)
Course: CSC350: Intelligent Systems
Term: Spring 2019
"""

from keras.models import Sequential
from keras.layers.core import Dense, Dropout, Activation
import sklearn.model_selection as ms
from keras.utils import np_utils
import numpy as np
import json
import os
import csv
from sklearn.metrics import confusion_matrix
from sklearn.metrics import classification_report
from sklearn.metrics import matthews_corrcoef


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
        input_data = [p/255 for p in input_data ]
        x_data.append(input_data)
    return x_data, y_data

def actual_get():
    files = []
    basepath = 'DataSetAI/'
    with os.scandir(basepath) as entries:
        for entry in entries:
            if entry.is_file() and entry.name[-4] == 'j':
                files.append(basepath + entry.name)
    x_data = []
    y_data = []
    for file in files:
        y_data.append([int(file[-8])])
        input_data = np.array(read_data(file)).flatten()
        input_data = [p / 255 for p in input_data]
        x_data.append(input_data)
    return x_data, y_data



def greatest_numpy(nump):
    #print(nump)
    y = 0.0
    count = 0
    for x in nump:
        print(nump[count])
        if int(round(nump[count])) > y:
            # print("y = {}".format(y))
            # print(count)
            y = count
        count += 1
    # print("y return = {}".format(y))
    return y


def run_test(x_info, y_labels):

    x_train = np.array(x_info)
    y_train = np.array(y_labels)

    n_classes = 10
    print("Shape before one-hot encoding: ", y_train.shape)
    y_train = np_utils.to_categorical(y_train, n_classes)

    # Create the model
    model = Sequential()
    model.add(Dense(units=512, input_dim=1024))  # First (hidden) layer
    model.add(Activation('relu'))
    model.add(Dropout(0.2))
    model.add(Dense(units=256))  # Second (hidden) layer
    model.add(Activation('relu'))
    model.add(Dropout(0.2))
    model.add(Dense(units=10))  # Third, final (output) layer
    model.add(Activation('softmax'))

    model.compile(loss='categorical_crossentropy', metrics=['accuracy'], optimizer='adam')

    # kfold = ms.KFold(n_splits=10, shuffle=True)
    # Train the model, iterating on the data in batches of 32 samples (try batch_size=1)

    model.fit(x_train, y_train, epochs=17, batch_size=32, verbose=1)

    # Evaluate the model from a sample test data set



    # Make a few predictions
    print("These are the predictions: ")
    x_test, y_test = actual_get()
    x_test = np.array(x_test)

    x_pred = model.predict(x_test)
    x_pred = np.array(x_pred)
    predictions = []
    labels = []
    with open('answers.csv', mode='w') as answers:
        answers = csv.writer(answers, delimiter=',', quotechar='"', quoting=csv.QUOTE_MINIMAL)
        for i in range(len(x_pred)):
            pred = greatest_numpy(x_pred[i])
            predictions.append(pred)
            lab = y_test[i]
            labels.append(lab)
            # print("2. This is the prediction {} this is the actual{}".format(pred, lab[i]))
            answers.writerow([lab, pred])


    model_json = model.to_json()
    with open("modelBig.json", "w") as json_file:
        json_file.write(model_json)
    # serialize weights to HDF5
    model.save_weights("modelBig.h5")
    print("Saved model to disk")
    print("#####")
    print("#####")
    print("#####")
    print("#####")
    print(y_test)
    print(confusion_matrix(y_test, predictions))
    print(classification_report(y_test, predictions))
    print(matthews_corrcoef(y_test, predictions))


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