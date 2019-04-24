"""

"""

import numpy as np
import json
import os
import sys
import miniBrain

file1 = ""
file2 = ""
file1Reduced = [
    [0, 0, 0, 0], 
    [0, 0, 0, 0],
    [0, 0, 0, 0],
    [0, 0, 0, 0]
]
file2Reduced = [
    [0, 0, 0, 0], 
    [0, 0, 0, 0],
    [0, 0, 0, 0],
    [0, 0, 0, 0]
]
workingList = []



"""
    32x32:
    11111111222222223333333344444444
    11111111222222223333333344444444
    11111111222222223333333344444444
    11111111222222223333333344444444
    11111111222222223333333344444444
    11111111222222223333333344444444
    11111111222222223333333344444444
    11111111222222223333333344444444
    55555555666666667777777788888888
    55555555666666667777777788888888
    55555555666666667777777788888888
    55555555666666667777777788888888
    55555555666666667777777788888888
    55555555666666667777777788888888
    55555555666666667777777788888888
    55555555666666667777777788888888
    AAAAAAAABBBBBBBBCCCCCCCCDDDDDDDD
    AAAAAAAABBBBBBBBCCCCCCCCDDDDDDDD
    AAAAAAAABBBBBBBBCCCCCCCCDDDDDDDD
    AAAAAAAABBBBBBBBCCCCCCCCDDDDDDDD
    AAAAAAAABBBBBBBBCCCCCCCCDDDDDDDD
    AAAAAAAABBBBBBBBCCCCCCCCDDDDDDDD
    AAAAAAAABBBBBBBBCCCCCCCCDDDDDDDD
    AAAAAAAABBBBBBBBCCCCCCCCDDDDDDDD
    EEEEEEEEFFFFFFFFGGGGGGGGHHHHHHHH
    EEEEEEEEFFFFFFFFGGGGGGGGHHHHHHHH
    EEEEEEEEFFFFFFFFGGGGGGGGHHHHHHHH
    EEEEEEEEFFFFFFFFGGGGGGGGHHHHHHHH
    EEEEEEEEFFFFFFFFGGGGGGGGHHHHHHHH
    EEEEEEEEFFFFFFFFGGGGGGGGHHHHHHHH
    EEEEEEEEFFFFFFFFGGGGGGGGHHHHHHHH
    EEEEEEEEFFFFFFFFGGGGGGGGHHHHHHHH
    
    turns into a 4x4 image:
    1234
    5678
    ABCD
    EFGH
"""

def main():
    print("Displaying image: {0}.".format(file1))
    miniBrain.print_image(miniBrain.read_data(file1), 200)
    reduce_image(miniBrain.read_data(file1), file1Reduced)
    print_image2(file1Reduced)
    #print("Displaying image: {0}.".format(file2))
    #miniBrain.print_image(miniBrain.read_data(file2), 200)
    
def reduce_image(img, reducedList):
    for x in range(0, 4):
        for row in img[(8 * x):(8 * (x + 1))]:
            total = 0
            count = 0
            for y in range(0, 4):
                for pixel in row[(8 * y):(8 * (y + 1))]:
                    total = total + pixel
                    count = count + 1
                reducedList[x][y] = (total / count)
                
def print_image2(img):
    totalString = ""
    for row in img:
        for pixel in row:
            totalString += (str(int(pixel)) + " | ").strip()
            #print(str(pixel) + " | ")
        totalString += "\n"
        #print()  # Newline at end of the row
    print(totalString)
                
if __name__ == "__main__":
    file1 = sys.argv[1]
    file2 = sys.argv[2]
    main()