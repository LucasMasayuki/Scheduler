import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TxtHelper {
    public static List<Integer> readIntegerFiles(String fileName) {
        try {
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            List<Integer> txt = new ArrayList<>();;

            while ((line = reader.readLine()) != null) {
                txt.add(Integer.parseInt(line));
            }

            return txt;

        } catch (FileNotFoundException e) {
            System.out.println("The file doesn't exist.");
        } catch (IOException e) {
            System.out.println("The file could not be read.");
        }

        return null;
    }

    public static List<String> readStringFiles(String fileName) {
        try {
            String line;
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            List<String> txt = new ArrayList<>();;

            while ((line = reader.readLine()) != null) {
                txt.add(line);
            }

            return txt;

        } catch (FileNotFoundException e) {
            System.out.println("The file doesn't exist.");
        } catch (IOException e) {
            System.out.println("The file could not be read.");
        }

        return null;
    }

    // Separate Text by enter and return array
//    public List<Object> separateTextByEnter(String txt, List<Object> array) {
//        String text = "";
//
//        for (int i = 0; i < txt.length(); i++) {
//            // If enter add text to array
//            if ("\n".equals(txt.charAt(i))) {
//                array.add(text);
//            } else {
//                text +=  txt.charAt(i);
//            }
//        }
//
//        return array;
//    }
}
