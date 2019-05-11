#Nearest Neighbor Classifier
#Authors: Matthew Jagiela, Austin Scudder, Nicholas Molina

import numpy as np
import json
import os
from sklearn.neighbors import KNeighborsClassifier
from sklearn.metrics import confusion_matrix
import sklearn.model_selection as ms
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
    z_data = []
    for file in files:
        y_data.append([int(file[-8])])
        z_data.append([file])
        input_data = np.array(read_data(file)).flatten()
        input_data = [p/255 for p in input_data]
        x_data.append(input_data)
    return x_data, y_data, z_data

def get_filename(data, x_info, z_filenames):
    return z_filenames[x_info.tolist().index(data.tolist())]
    
model = KNeighborsClassifier(n_neighbors=2) #This represents KNN using 2 neighbors. This gives us the best accuracy
x_info, y_labels, z_filenames = info_get()
y_labels = np.array(y_labels)
x_info = np.array(x_info)

kfold = ms.KFold(n_splits=10, shuffle=True) #KFold Validation

for train_index, test_index in kfold.split(y_labels):
    x_train = np.array(x_info[train_index])
    x_test = np.array(x_info[test_index])
    y_train = np.array(y_labels[train_index])
    y_test = np.array(y_labels[test_index])

model.fit(x_info, y_labels.flatten())

index = 0
correct = 0
predictions = []
for element, data in zip(model.predict(x_test), x_test): #For every prediction
    predictions.append(element) #add predicition to the array of all predicitions
    if(element == y_test[index]): #for every right answer
        correct += 1 #Total number of correct answers
    else:
        print("Predicted : {} Actual {}" .format(element, y_test[index])) #Print the result of every answer compared to what it actually is
        print("Filename:" + str(get_filename(data, x_info, z_filenames)))
    index += 1 #Search index increase
print("Accuracy {} ".format(correct / index)) #The total accuracy of the model is....
cm = confusion_matrix(y_test, predictions) #generate the confusion matrix
print (cm) #Print the confusion matrix
print(classification_report(y_test, predictions))
print(matthews_corrcoef(y_test, predictions))