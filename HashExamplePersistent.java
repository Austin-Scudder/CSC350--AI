import java.util.HashMap;
import java.util.Scanner;
import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.ObjectOutputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/****
 * Author: Christian Duncan
 * CSC350: Intelligent Systems
 * Spring 2019
 *
 * Demonstrates how to use a hashmap AND save/read it from 
 * a file using Java's Serializable feature.
 ****/
public class HashExamplePersistent {
    public static class Record implements Serializable {
        static final long serialVersionUID = 1L;  // Used to verify it is same version of Record (in case it changes!)
        double alpha;  // The fitness score of this state
        
        public Record() { alpha = 1.0; }
    }

    /**
     * Read in a HashMap from the given file name.
     * @return The HashMap that was read (or a newly created blank hash map if file does not exist)
     **/
    @SuppressWarnings("unchecked")
    public static HashMap<String, Record> readMap(String mapFileName) {
        ObjectInputStream ois = null;
        try {
            // Open up the Object file for reading and read in the HashMap
            ois = new ObjectInputStream(new FileInputStream(mapFileName));
            Object obj = ois.readObject();
            ois.close();
            return (HashMap<String,Record>) obj; // Typecast the Object read to a HashMap
        } catch (FileNotFoundException e) {
            System.out.println("Map file " + mapFileName + " was not found.  Using new map.");
            System.out.println("  Message: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Aborting!  Error processing file. " + e.getMessage());
            System.exit(1);
        } catch (ClassNotFoundException e) {
            System.out.println("Aborting!  Error processing file. Does not appear to save a Hash Map. " + e.getMessage());
            System.exit(1);
        }
        
        // Just return a blank map...
        return new HashMap<String,Record>();
    }

    /**
     * Write a HashMap to a file with the given file name.
     **/
    public static void saveMap(HashMap<String, Record> map, String mapFileName) {
        ObjectOutputStream oos = null;
        try {
            // Open up the Object file for writing and write the HashMap
            oos = new ObjectOutputStream(new FileOutputStream(mapFileName));
            oos.writeObject(map);
            oos.close();
        } catch (IOException e) {
            System.out.println("Aborting!  Error saving map to file. " + e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        HashMap<String,Record> map = null;
        String mapFileName = null;
        if (args.length > 0) {
            // Read in a map file
            mapFileName = args[0];
            map = readMap(mapFileName);
        } else {
            // Create map from scratch
            map = new HashMap<String,Record>();
        }
        Scanner in = new Scanner(System.in);
        
        // How many lines are there
        int count = in.nextInt();
        in.nextLine(); // Sucks up that first line

        for (; count > 0; count--) {
            String state = in.nextLine();  // Read in the line
            
            // See if the state has already been visited
            Record r = map.get(state);
            if (r == null) {
                // A new state
                r = new Record();
                map.put(state, r);
                System.out.println("Line: " + state + " Score = " + r.alpha);
            } else {
                // Print and update the record
                r.alpha *= 0.7;
                System.out.println("Line: " + state + " Score = " + r.alpha);
            }
        }

        // Save the map to the file
        saveMap(map, mapFileName);
    }
}
