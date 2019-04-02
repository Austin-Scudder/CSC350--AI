"""
Author: Christian Duncan
Date: Spring 2019
Course: CSC350 (Intelligent Systems)

This sample code shows how one can read in our JSON image files in Python3.  It reads in the file and then outputs the two-dimensional array.
It applies a simple threshold test - if value is > threshold then it outputs a '.' otherwise it outputs an 'X'.  More refinement is probably better.
But this is for displaying images not processing or recognizing images."""

import sys
import json
import os
from pathlib import Path

""" 
Reads in the given JSON file as outlined in the README.txt file.
"""


def read_data(file):
    try:
        with open(file, 'r') as inf:
            bitmap = json.load(inf)
        return bitmap
    except FileNotFoundError as err:
        print("File Not Found: {0}.".format(err))


""" 
Prints out the given "image" (a 2-dimensional array).
This just replaces any values greater than a threshold with . and otherwise with an X.
"""


def print_image(img, threshold):
    for row in img:
        for pixel in row:
            print('.' if pixel > threshold else 'X', end='')
        print()  # Newline at end of the row


""" 
Main entry point.  Assumes all the arguments passed to it are file names.
For each argument, reads in the file and the prints it out.
"""


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
    for file_name in file_list:
        img = read_data(file_name)
        print("Displaying image: {0}.".format(file_name))
        print_image(img, 200)  # Different thresholds will change what shows up as X and what as a .


# This is just used to trigger the Python to run the main() method.  The if statement is used so that if this code
# were imported as a module then everything would load but main() would not be executed.
if __name__ == "__main__":
    main()
