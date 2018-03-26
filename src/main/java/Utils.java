public class Utils {

    public static String capitalize(String str) {
        return String.valueOf(Character.toUpperCase(str.charAt(0))) + str.substring(1);
    }

    public static String uncapitalize(String str) {
        return String.valueOf(Character.toLowerCase(str.charAt(0))) + str.substring(1);
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
