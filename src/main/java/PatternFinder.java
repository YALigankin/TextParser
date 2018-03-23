import beans.Descriptor;
import beans.DescriptorType;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternFinder {

    private static final Map<Integer, List<Pattern>> PATTERNS = new HashMap<>();
    private static final Set<Integer> DESCRIPTORS_WITH_DOT_IN_END = new HashSet<>(Arrays.asList(DescriptorType.FIO, DescriptorType.SHORT_WORD));

    static {
        PATTERNS.put(DescriptorType.FIO, Arrays.asList(
                Pattern.compile("[А-Я][А-Яа-я]+ [А-Я]\\.[A-Я]\\."),
                Pattern.compile("[А-Я]\\.[А-Я]\\. ?[А-Я][А-Яа-я]+"))
        );
        PATTERNS.put(DescriptorType.SHORT_WORD, Arrays.asList(Pattern.compile("[A-ЯЁA-Z]{2,}"),
                Pattern.compile("[а-я]+\\."),         //Сокр. усечение
                Pattern.compile("[а-я.]+-[а-я]+"))    //Сокр.стяжение
        );
        PATTERNS.put(DescriptorType.EMAIL, Collections.singletonList(Pattern.compile("[a-zA-Z1-9\\-\\._]+@[a-z1-9]+(.[a-z1-9]+){1,}")));
        PATTERNS.put(DescriptorType.NUM_SEQ, Collections.singletonList(Pattern.compile("\\d+[.,]\\d+")));
    }

    public List<Descriptor> getDescriptors(String text) {
        List<Descriptor> result = new ArrayList<>();
        for (Map.Entry<Integer, List<Pattern>> patternEntry : PATTERNS.entrySet()) {
            int descriptorType = patternEntry.getKey();
            boolean mayStayInEnd = DESCRIPTORS_WITH_DOT_IN_END.contains(descriptorType);
            for (Pattern pattern : patternEntry.getValue()) {
                Matcher m = pattern.matcher(text);
                while (m.find()) {
                    Descriptor curDescriptor = new Descriptor(descriptorType, m.start(), (m.end() - m.start()), text.substring(m.start(), m.end()));
                    curDescriptor.setMayStayInEnd(mayStayInEnd);
                    result.add(curDescriptor);
                }
            }
        }

        result.sort(Comparator.comparing(Descriptor::getStartPos));

        //поиск пересечения дескрипторов - занял Иванов В.И. При оформлении
        Descriptor prevDescriptor = null;
        Iterator<Descriptor> iter = result.iterator();
        while (iter.hasNext()) {
            Descriptor curDescriptor = iter.next();
            if (prevDescriptor != null && curDescriptor.getStartPos() < (prevDescriptor.getStartPos() + prevDescriptor.getLength())) {
                iter.remove();
            } else {
                prevDescriptor = curDescriptor;
            }
        }

        return result;
    }

}
