import java.io.*;
import java.io.IOException;

public class Main {

    private static void read(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (FileNotFoundException e) {
            // do something, when the file could not be found on the system
            System.out.println("The file doesn't exist.");
        } catch (IOException e) {
            // do something, when the file is not readable
            System.out.println("The file could not be read.");
        }
    }

    public static void main(String[] args) {
        int maxFiles = 11;
        String fileName;
        for (int i = 1; i < maxFiles; i++) {
            String index = Integer.toString(i);
            fileName = "code/src/0" + index + ".txt";
            read(fileName);
        }
        fileName = "code/src/prioridades.txt";
        read(fileName);

        fileName = "code/src/quantum.txt";
        read(fileName);
    }
}
