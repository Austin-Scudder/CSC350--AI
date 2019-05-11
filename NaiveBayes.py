#Put the code for the naive bayes classifier here
#Authors: Matthew Jagiela, Austin Scudder, Nicholas Molina
from sklearn.naive_bayes import GaussianNB
import numpy as np
import json
import os
import sklearn.model_selection as ms
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


model = GaussianNB() #Our model is going to be Naive Bayes Gaussian
x_info, y_labels, z_filenames = info_get()
y_labels = np.array(y_labels)
x_info = np.array(x_info)

kfold = ms.KFold(n_splits=10, shuffle=True) #Kfold validation

for train_index, test_index in kfold.split(y_labels):
    x_train = np.array(x_info[train_index])
    x_test = np.array(x_info[test_index])
    y_train = np.array(y_labels[train_index])
    y_test = np.array(y_labels[test_index])

model.fit(x_info, y_labels.flatten())

index = 0
predictions = []
for element, data in zip(model.predict(x_test), x_test): #Print each predicition and the actual number
    predictions.append(element)
    if(element != y_test[index]): #for every right answer
        print("Predicted : {} Actual {}" .format(element, y_test[index])) #Print the result of every answer compared to what it actually is
        print("Filename:" + str(get_filename(data, x_info, z_filenames)))
    index += 1 #Search index increase
print("Accuracy {} " .format(model.score(x_test, y_test))) #This model has a score associated with it... Print the score.
#Generate the confusion matrix and print it:
cm = confusion_matrix(y_test, predictions)
print(cm)
print(classification_report(y_test, predictions))
print(matthews_corrcoef(y_test, predictions))