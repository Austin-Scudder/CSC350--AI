import numpy as np
import json
import os
import sys
import miniBrain
from sklearn.neighbors import KNeighborsClassifier
from sklearn.model_selection import train_test_split
from sklearn.model_selection import cross_val_score
import pandas

def get_file_label(filename):
    return filename[filename.index('_', 9) - 1]

def main():
    train_files = []
    test_files = []
    all_files = []
    basepath = 'res/'
    file_data = []
    file_name = []
    pandas.set_option('display.expand_frame_repr', False)
    with os.scandir(basepath) as entries:
        for entry in entries:
            if entry.is_file():
                file_data.append(pandas.read_json(entry))
                file_name.append(get_file_label(entry.name))
                all_files.append((pandas.read_json(entry), get_file_label(entry.name)))
                #print(entry.name)
                #print(get_file_label(entry.name))
    data_train, data_test, name_train, name_test = train_test_split(file_data, file_name, test_size=0.2)
    model = KNeighborsClassifier(n_neighbors = 2)
    model.fit(data_train, name_train)
    #for name in file_name:
    #    print(name)
    print(model.predict(data_test))
    #print(cross_val_score(model, data_train, name_train, cv=5))
    #print(nbrs.kneighbors_graph(data_train).toArray()
    #print(indicies)
                #if entry.name[-6] == "9":
                #    train_files.append(pandas.read_json(entry))
                #else:
                #    test_files.append(pandas.read_json(entry))
    #nbrs = NearestNeighbors(algorithm='ball_tree').fit(test_files)
    #distances, indicies = nbrs.kneighbors(test_files)
    #print(distances)
    #print(indicies)
    
if __name__ == "__main__":
    main()