import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Utils {

    public static String readFile(InputStream is) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String curLine;
            while ((curLine = reader.readLine()) != null) {
                sb.append(curLine);
            }
        }
        return sb.toString();
    }

    public static String readFile(String filePath) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String curLine;
            while ((curLine = reader.readLine()) != null) {
                sb.append(curLine);
            }
        }
        return sb.toString();
    }

    public static Integer parseInt(String str) {
        Integer temp;
        try {
            temp = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            temp = null;
        }
        return temp;
    }
}
