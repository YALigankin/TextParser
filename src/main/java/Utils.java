import java.util.Collection;
import java.util.Objects;

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

    public static String concat(String separator, Collection<?> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        if (list.size() == 1) {
            return Objects.toString(list.iterator().next(), null);
        }
        StringBuilder sb = new StringBuilder(list.size() * 16);
        for (Object o : list) {
            String s = Objects.toString(o, null);
            if (!s.isEmpty()) {
                sb.append(s).append(separator);
            }
        }
        if (sb.length() == 0) {
            return "";
        }
        if (separator.length() > 0) {
            sb.setLength(sb.length() - separator.length());
        }
        return sb.toString();
    }
}
