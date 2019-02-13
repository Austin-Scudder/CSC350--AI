import java.util.HashMap;
import java.util.Scanner;

/****
 * Author: Christian Duncan
 * CSC350: Intelligent Systems
 * Spring 2019
 *
 * Demonstrates how to use a hashmap (the basics).
 ****/
public class HashExample {
    public static class Record {
        double alpha;  // The fitness score of this state
        
        public Record() { alpha = 1.0; }
    }

    public static void main(String[] args) {
        HashMap<String,Record> map = new HashMap<String,Record>();
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
    }
}
