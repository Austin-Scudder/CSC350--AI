import numpy as np
import json
import os
import sys
from sklearn.neighbors import KNeighborsClassifier
from sklearn.model_selection import train_test_split
from sklearn.model_selection import cross_val_score
import pandas
import itertools

def get_file_label(filename):
    return filename[filename.index('_', 9) - 1]

def main():
    train_files = []
    test_files = []
    all_files = []
    basepath = 'DataSet/'
    file_data = []
    file_name = []
    pandas.set_option('display.expand_frame_repr', False)
    with os.scandir(basepath) as entries:
        for entry in entries:
            if entry.is_file():
                with open(entry, 'r') as inf:
                    bitmap = json.load(inf)
                file_data.append(bitmap)
                file_name.append(get_file_label(entry.name))
                all_files.append((bitmap, get_file_label(entry.name)))
    file_data = np.array(file_data).reshape(-1, 1024)
    model = KNeighborsClassifier(n_neighbors = 5)
    results = cross_val_score(model, file_data, file_name, cv=6)
    print("Accuracy: %0.2f (+/- %0.2f)" % (results.mean(), results.std() * 2))
    
    
if __name__ == "__main__":
    main()