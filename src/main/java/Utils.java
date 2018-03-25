public class Utils {

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
