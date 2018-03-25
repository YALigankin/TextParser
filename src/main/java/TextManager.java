import beans.Descriptor;
import beans.DescriptorType;
import beans.Sentence;

import java.util.*;

public class TextManager {

    //Unicodes https://www.fileformat.info/info/unicode/category/Po/list.htm
    private static final Set<Character> SPACE_CHAR = new HashSet<>(Arrays.asList('\u0020', '\n', '\r', '\t'));   // LF, CR, HT
    private static final Set<Character> PUNCTUATION_CHAR = new HashSet<>(Arrays.asList('\u002C', '\u003B', '\u003A', '\u0021'/*, '\u002E', '\u003F', '\u2026'*/));     // , ; :  (! . ? ...)
    private static final Set<Character> SENTENCE_END = new HashSet<>(Arrays.asList('\u002E', '\u0021', '\u003F', '\u2026'));   // . ! ? ...
    private static final Set<Character> RUSSIAN_LEX = new HashSet<>();
    private static final Set<Character> FOREIGN_LEX = new HashSet<>();

    //List, чтобы при проверке парности использовать индекс (+ список короткий)
    private static final List<Character> OPEN_BRACKET = Arrays.asList('\u0028', '\u005B', '\u007B');
    private static final List<Character> CLOSE_BRACKET = Arrays.asList('\u0029', '\u005D', '\u007D');
    private static final List<Character> OPEN_QUOTE = Arrays.asList('\u201C', '\u00AB');
    private static final List<Character> CLOSE_QUOTE = Arrays.asList('\u201D', '\u00BB');

    static {
        for (char i = 'ј'; i <= '€'; i++) {
            RUSSIAN_LEX.add(i);
        }
        for (char i = 'A'; i <= 'z'; i++) {
            FOREIGN_LEX.add(i);
        }
    }

    private final PatternFinder patternFinder;
    private final AbbrResolver abbrResolver;

    public TextManager(PatternFinder patternFinder, AbbrResolver abbrResolver) {
        this.patternFinder = patternFinder;
        this.abbrResolver = abbrResolver;
    }

    public List<Sentence> splitText(String text) throws Exception {

        //поиск всех дескрипторов, чтобы потом правильно определить границы предложений
        List<Descriptor> descriptors = patternFinder.getDescriptors(text);

        //подгрузка значений сокращений из Ѕƒ, если не найдет, то это не сокращение
        abbrResolver.fillAbbrDescriptions(descriptors);

        return splitText(text, descriptors);
    }

    private List<Sentence> splitText(String text, List<Descriptor> descriptors) {

        List<Sentence> sentences = new ArrayList<>();

        //sentence attributes
        int sentenceIndex = 1;
        Sentence sentence = new Sentence();
        int sentenceStartPos = 0;
        int sentenceEndPos = text.length() - 1;
        boolean foundEnd = false;

        //word attributes
        char ch;
        int wordStartPos = 0;
        boolean hasRussianLex = false;
        boolean hasDigit = false;
        boolean hasForeignLex = false;

        //descriptor
        int desciptorIndex = descriptors.isEmpty() ? -1 : 0;
        Descriptor curDescriptor = descriptors.isEmpty() ? null : descriptors.get(desciptorIndex);

        //vars
        Stack<Character> pairsStack = new Stack<>();
        int searchPos = -1;

        for (int i = 0; i < text.length(); i++) {
            ch = text.charAt(i);
            if (foundEnd) {
                if (SPACE_CHAR.contains(ch)) {
                    continue;
                } else if (Character.isUpperCase(ch) && pairsStack.isEmpty() && !hasIntersection(i, descriptors)) {
                    if (wordStartPos != i) {
                        sentence.addDescriptor(new Descriptor(getDescriptorType(hasRussianLex, hasDigit, hasForeignLex), wordStartPos, sentenceEndPos - wordStartPos, text.substring(wordStartPos, sentenceEndPos)));

                        hasRussianLex = false;
                        hasDigit = false;
                        hasForeignLex = false;
                    }
                    wordStartPos = i;

                    sentence.setIndexInText(sentenceIndex++);
                    sentence.setStartPos(sentenceStartPos);
                    sentence.setLength(sentenceEndPos - sentenceStartPos);
                    sentence.setContent(text.substring(sentenceStartPos, sentenceEndPos));
                    sentences.add(sentence);

                    sentence = new Sentence();
                    foundEnd = false;
                    sentenceStartPos = i;
                    sentenceEndPos = text.length();
                } else {
                    foundEnd = false;
                }
            } else if (SPACE_CHAR.contains(ch)) {
                if (wordStartPos != i) {
                    sentence.addDescriptor(new Descriptor(getDescriptorType(hasRussianLex, hasDigit, hasForeignLex), wordStartPos, i - wordStartPos, text.substring(wordStartPos, i)));

                    hasRussianLex = false;
                    hasDigit = false;
                    hasForeignLex = false;
                }
                wordStartPos = i + 1;
            } else if (curDescriptor != null && i == curDescriptor.getStartPos()) {    //натолкнулись на ранее найденный дескриптор
                sentence.addDescriptor(curDescriptor);
                i += curDescriptor.getLength() - 1;
                wordStartPos = i + 1;
                desciptorIndex++;
                curDescriptor = desciptorIndex >= descriptors.size() ? null : descriptors.get(desciptorIndex);
            } else if (RUSSIAN_LEX.contains(ch)) {
                hasRussianLex = true;
            } else if (Character.isDigit(ch)) {
                hasDigit = true;
            } else if (FOREIGN_LEX.contains(ch)) {
                hasForeignLex = true;
            } else if (OPEN_BRACKET.contains(ch)) {
                if (wordStartPos != i) {
                    sentence.addDescriptor(new Descriptor(getDescriptorType(hasRussianLex, hasDigit, hasForeignLex), wordStartPos, i - wordStartPos, text.substring(wordStartPos, i)));
                }
                sentence.addDescriptor(new Descriptor(DescriptorType.OPEN_BRACKET, i, 1, Character.toString(ch)));
                wordStartPos = i + 1;
                pairsStack.add(ch);
            } else if (OPEN_QUOTE.contains(ch)) {
                if (wordStartPos != i) {
                    sentence.addDescriptor(new Descriptor(getDescriptorType(hasRussianLex, hasDigit, hasForeignLex), wordStartPos, i - wordStartPos, text.substring(wordStartPos, i)));
                }
                sentence.addDescriptor(new Descriptor(DescriptorType.OPEN_QUOTE, i, 1, Character.toString(ch)));
                wordStartPos = i + 1;
                pairsStack.add(ch);
            } else if ((searchPos = CLOSE_BRACKET.indexOf(ch)) >= 0) {
                if (wordStartPos != i) {
                    sentence.addDescriptor(new Descriptor(getDescriptorType(hasRussianLex, hasDigit, hasForeignLex), wordStartPos, i - wordStartPos, text.substring(wordStartPos, i)));
                }
                sentence.addDescriptor(new Descriptor(DescriptorType.CLOSE_BRACKET, i, 1, Character.toString(ch)));
                wordStartPos = i + 1;
                //rem from stack
                if (!pairsStack.isEmpty() && OPEN_BRACKET.indexOf(pairsStack.peek()) == searchPos) {
                    pairsStack.pop();
                    foundEnd = false;
                }
            } else if ((searchPos = CLOSE_QUOTE.indexOf(ch)) >= 0) {
                if (wordStartPos != i) {
                    sentence.addDescriptor(new Descriptor(getDescriptorType(hasRussianLex, hasDigit, hasForeignLex), wordStartPos, i - wordStartPos, text.substring(wordStartPos, i)));
                }
                sentence.addDescriptor(new Descriptor(DescriptorType.CLOSE_QUOTE, i, 1, Character.toString(ch)));
                wordStartPos = i + 1;
                //rem from stack
                if (!pairsStack.isEmpty() && OPEN_QUOTE.indexOf(pairsStack.peek()) == searchPos) {
                    pairsStack.pop();
                    foundEnd = false;
                }
            } else if (PUNCTUATION_CHAR.contains(ch)) {
                if (wordStartPos != i) {
                    sentence.addDescriptor(new Descriptor(getDescriptorType(hasRussianLex, hasDigit, hasForeignLex), wordStartPos, i - wordStartPos, text.substring(wordStartPos, i)));
                }
                sentence.addDescriptor(new Descriptor(DescriptorType.PUNCTUATION_CHAR, i, 1, Character.toString(ch)));
                wordStartPos = i + 1;
            } else if (SENTENCE_END.contains(ch)) {
                sentenceEndPos = i;
                foundEnd = true;
            }
        }

        sentence.addDescriptor(new Descriptor(getDescriptorType(hasRussianLex, hasDigit, hasForeignLex), wordStartPos, (text.length() - 1) - wordStartPos, text.substring(wordStartPos, (text.length() - 1))));

        sentence.setIndexInText(sentenceIndex++);
        sentence.setStartPos(sentenceStartPos);
        sentence.setLength(sentenceEndPos - sentenceStartPos);
        sentence.setContent(text.substring(sentenceStartPos, sentenceEndPos));
        sentences.add(sentence);

        return sentences;
    }

    private int getDescriptorType(boolean hasRussianLex, boolean hasDigit, boolean hasForeignLex) {
        if (hasRussianLex && !hasDigit && !hasForeignLex) {
            return DescriptorType.RUSSIAN_LEX;
        } else if (!hasRussianLex && hasDigit && !hasForeignLex) {
            return DescriptorType.NUM_SEQ;
        } else if (!hasRussianLex && !hasDigit && hasForeignLex) {
            return DescriptorType.FOREIGN_LEX;
        } else {
            return DescriptorType.OTHER_LEX;
        }
    }

    private boolean hasIntersection(int pos, List<Descriptor> descriptors) {
        if (descriptors.isEmpty() || pos < descriptors.get(0).getStartPos()) {
            return false;
        } else {
            for (Descriptor descriptor : descriptors) {
                if (descriptor.getStartPos() > pos) {
                    return false;
                } else if (pos > descriptor.getStartPos() && pos <= (descriptor.getStartPos() + descriptor.getLength())) {    //точка - участник дескриптора
                    return true;

                    //TODO mayStayInEnd

                    //TODO проверка на пересечение символа, предшествующего SentenceEndSeq, с графематическими дескрипторами,  найденными  на  предыдущих  этапах.
                    // ≈сли  символ, предшествующий SentenceEndSeq, пересекаетс€ хот€ бы с одним дескриптором, который не может быть концом предложени€, то найденна€ последовательность
                    //SentenceEndSeq не €вл€етс€ концом предложени€, переходим к шагу 2
                }
            }
        }
        return false;
    }

}
