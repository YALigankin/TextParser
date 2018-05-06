import beans.Descriptor;
import beans.DescriptorType;
import beans.Sentence;
import grammeme.MorfologyParameters;
import jmorfsdk.JMorfSdk;
import morphologicalstructures.OmoForm;
import storagestructures.OmoFormList;

import java.util.*;

public class AbbrResolver {

    private static final List<Byte> VERB_TYPES = Arrays.asList(MorfologyParameters.TypeOfSpeech.VERB, MorfologyParameters.TypeOfSpeech.INFINITIVE);

    private static final Set<String> GENITIVE_PREPOSITIONS = new HashSet<>(Arrays.asList("без", "у", "до", "от", "с", "около", "из", "возле", "после", "для", "вокруг"));
    private static final Set<String> DATIVE_PREPOSITIONS = new HashSet<>(Arrays.asList("к", "по"));
    private static final Set<String> ACCUSATIVE_PREPOSITIONS = new HashSet<>(Arrays.asList("в", "за", "на", "про", "через"));
    private static final Set<String> ABLTIVE_PREPOSITIONS = new HashSet<>(Arrays.asList("за", "над", "под", "перед", "с"));
    private static final Set<String> PREPOSITIONA_PREPOSITIONS = new HashSet<>(Arrays.asList("в", "на", "о", "об", "обо", "при"));

    private JMorfSdk jMorfSdk;

    public void setJMorfSdk(JMorfSdk jMorfSdk) {
        this.jMorfSdk = jMorfSdk;
    }

    public void fillAbbrDescriptions(DBManager dictionary, List<Descriptor> descriptors) throws Exception {
        for (Descriptor curDescriptor : descriptors) {
            List<String> longForms = dictionary.findAbbrLongForms(curDescriptor.getValue());    //TODO затратный поиск (нужно запрашивать пачкой) + cокращения могут повторяться в разных предложениях

                if (longForms.isEmpty() && curDescriptor.getValue().contains("-")) {
                    for (String s : getPossibleInfinitives( curDescriptor.getValue())) {
                    longForms = dictionary.findAbbrLongForms(s);
                        if (!longForms.isEmpty()) {
                        break;
                        }
                    }
                }

            if (!longForms.isEmpty()) {
                curDescriptor.setDesc(longForms.get(0));           //TODO пока берется первое попавшееся значение аббревиатуры
            }
        }
    }

    public String resolveAcronyms(Sentence sentence) throws Exception {
        if (jMorfSdk == null) {
            throw new RuntimeException("JMorfSdk is not setted!");
        }
        List<Descriptor> descriptors = sentence.getDescriptors();
        String copy = sentence.getContent();
        for (int i = 0; i < descriptors.size(); i++) {
            Descriptor curDescriptor = descriptors.get(i);
            if (Objects.equals(curDescriptor.getType(), DescriptorType.SHORT_WORD)) {

                String[] acronymWords = curDescriptor.getDesc().split(" ");
                boolean[] capitalizeWords = new boolean[acronymWords.length];

                //save acronym case
                for (int j = 0; j < acronymWords.length; j++) {
                    String word = acronymWords[j];
                    if (Character.isUpperCase(word.charAt(0))) {
                        acronymWords[j] = Utils.uncapitalize(word);
                        capitalizeWords[j] = true;
                    }
                }

                int mainWordAcronymIndex = getMainWordAcronymIndex(acronymWords);

                Integer collacationMainWordIndex = getMainWordIndex(descriptors, i, acronymWords[mainWordAcronymIndex]);
                if (collacationMainWordIndex != null) {
                    String collMainWord = descriptors.get(collacationMainWordIndex).getValue();
                    Integer prepositionIndex = getPrepositionIndex(descriptors, i, collacationMainWordIndex);
                    String prepositionWord = prepositionIndex != null ? descriptors.get(prepositionIndex).getValue() : "";
                    acronymWords[mainWordAcronymIndex] = getTrueAcronymForm(acronymWords[mainWordAcronymIndex], collMainWord, prepositionWord);
                }

                if (acronymWords.length > 1) {
                    adaptAcronymWords(acronymWords, mainWordAcronymIndex);
                }

                //restore acronym case
                for (int j = 0; j < acronymWords.length; j++) {
                    if (capitalizeWords[j]) {
                        acronymWords[j] = Utils.capitalize(acronymWords[j]);
                    }
                }

                copy = copy.replace(curDescriptor.getValue(), Utils.concat(" ", Arrays.asList(acronymWords)));      //TODO replaceAll заменить
            }
        }
        return copy;
    }

    /**
     * Определяет главное слово в полной форме сокращения
     */
    private int getMainWordAcronymIndex(String[] acronymWords) throws Exception {
        if (acronymWords.length > 1) {
            OmoFormList omoForms;
            OmoForm omoForm;
            for (int i = 0; i < acronymWords.length; i++) {
                if (!(omoForms = jMorfSdk.getAllCharacteristicsOfForm(acronymWords[i])).isEmpty()) {
                    omoForm = omoForms.get(0);
                    if (omoForm.getTypeOfSpeech() == MorfologyParameters.TypeOfSpeech.NOUN
                            && omoForm.getTheMorfCharacteristics(MorfologyParameters.Case.class) == MorfologyParameters.Case.NOMINATIVE) {
                        return i;
                    }
                }
            }
        }
        return 0;
    }

    /**
     * Ищет главное слово в предложении для сокращения
     */
    private Integer getMainWordIndex(List<Descriptor> descriptors, int acronymIndex, String acronym) throws Exception {

        OmoFormList omoForms = jMorfSdk.getAllCharacteristicsOfForm(acronym);
        if (omoForms.isEmpty()) {
            return null;
        }
        byte acronymTypeOfSpeech = omoForms.get(0).getTypeOfSpeech();

        int wordIndex = acronymIndex;

        if (Arrays.asList(MorfologyParameters.TypeOfSpeech.ADJECTIVEFULL, MorfologyParameters.TypeOfSpeech.ADJECTIVESHORT, MorfologyParameters.TypeOfSpeech.NOUNPRONOUN,
                MorfologyParameters.TypeOfSpeech.PARTICIPLE, MorfologyParameters.TypeOfSpeech.PARTICIPLEFULL, MorfologyParameters.TypeOfSpeech.NUMERAL).contains(acronymTypeOfSpeech)) {
            //главное слово впереди
            while (++wordIndex < descriptors.size()) {
                Descriptor curDescriptor = descriptors.get(wordIndex);
                if (Objects.equals(curDescriptor.getType(), DescriptorType.RUSSIAN_LEX) && !Character.isUpperCase(curDescriptor.getValue().charAt(0))) {
                    OmoForm wordOmoForm = jMorfSdk.getAllCharacteristicsOfForm(curDescriptor.getValue()).get(0);
                    if (wordOmoForm.getTypeOfSpeech() == MorfologyParameters.TypeOfSpeech.NOUN) {
                        return wordIndex;
                    }
                }
            }
        } else if (acronymTypeOfSpeech == MorfologyParameters.TypeOfSpeech.NOUN) {
            //главное слово позади
            while (--wordIndex >= 0) {
                Descriptor curDescriptor = descriptors.get(wordIndex);
                if (Objects.equals(curDescriptor.getType(), DescriptorType.RUSSIAN_LEX) && !Character.isUpperCase(curDescriptor.getValue().charAt(0))) {
                    OmoForm wordOmoForm = jMorfSdk.getAllCharacteristicsOfForm(curDescriptor.getValue()).get(0);
                    if (wordOmoForm.getTypeOfSpeech() == MorfologyParameters.TypeOfSpeech.NOUN
                            || VERB_TYPES.contains(wordOmoForm.getTypeOfSpeech())) {
                        return wordIndex;
                    }
                } else if (Objects.equals(curDescriptor.getType(), DescriptorType.NUM_SEQ) && acronymIndex - wordIndex < 3) {
                    OmoFormList omoFormList = jMorfSdk.getAllCharacteristicsOfForm(curDescriptor.getValue());
                    if (!omoFormList.isEmpty() && omoFormList.get(0).getTypeOfSpeech() == MorfologyParameters.TypeOfSpeech.NUMERAL) {
                        return wordIndex;
                    }
                }
            }
        }
        return null;
    }

    private Integer getPrepositionIndex(List<Descriptor> descriptors, int acronymIndex, int mainWordIndex) throws Exception {
        int delta = acronymIndex - mainWordIndex;
        int startIndex = delta > 0 ? mainWordIndex : acronymIndex;
        int endIndex = startIndex + Math.abs(delta);
        for (int wordIndex = startIndex + 1; wordIndex < endIndex; wordIndex++) {
            Descriptor curDescriptor = descriptors.get(wordIndex);
            if (Objects.equals(curDescriptor.getType(), DescriptorType.RUSSIAN_LEX) && !Character.isUpperCase(curDescriptor.getValue().charAt(0))) {
                OmoForm wordOmoForm = jMorfSdk.getAllCharacteristicsOfForm(curDescriptor.getValue()).get(0);
                if (wordOmoForm.getTypeOfSpeech() == MorfologyParameters.TypeOfSpeech.PRETEXT) {
                    return wordIndex;
                }
            }
        }
        return null;
    }

    /**
     * Согласует сокращение с главным словом в предложении
     */
    private String getTrueAcronymForm(String acronymMainWord, String collacationMainWord, String preposition) throws Exception {
        OmoForm acronymOmoForm = jMorfSdk.getAllCharacteristicsOfForm(acronymMainWord).get(0);
        byte acronymTypeOfSpeech = acronymOmoForm.getTypeOfSpeech();

        OmoForm mainWordOmoForm = jMorfSdk.getAllCharacteristicsOfForm(collacationMainWord.toLowerCase()).get(0);
        byte mainWordTypeOfSpeech = mainWordOmoForm.getTypeOfSpeech();
        long mainWordCase = mainWordOmoForm.getTheMorfCharacteristics(MorfologyParameters.Case.IDENTIFIER);
        long mainWordNumbers = mainWordOmoForm.getTheMorfCharacteristics(MorfologyParameters.Numbers.IDENTIFIER);
        long mainWordGender = mainWordOmoForm.getTheMorfCharacteristics(MorfologyParameters.Gender.IDENTIFIER);

        if (Arrays.asList(MorfologyParameters.TypeOfSpeech.ADJECTIVEFULL, MorfologyParameters.TypeOfSpeech.ADJECTIVESHORT, MorfologyParameters.TypeOfSpeech.NOUNPRONOUN,
                MorfologyParameters.TypeOfSpeech.PARTICIPLE, MorfologyParameters.TypeOfSpeech.PARTICIPLEFULL, MorfologyParameters.TypeOfSpeech.NUMERAL).contains(acronymTypeOfSpeech)) {
            //model 1: сокр. (прил., местоим., причастие, числит.) + сущ. (глав.)
            List<String> matchList = jMorfSdk.getDerivativeForm(acronymMainWord, mainWordCase);
            removeIf(matchList, MorfologyParameters.Numbers.class, mainWordNumbers);
            //removeIf(jMorfSdk, matchList, MorfologyParameters.Gender.class, mainWordGender);       //мужской - средний род (противоположном направлении, противоположном ключе)
            return matchList.get(0);
        } else if (acronymTypeOfSpeech == MorfologyParameters.TypeOfSpeech.NOUN && VERB_TYPES.contains(mainWordTypeOfSpeech)) {
            //model 2:  глаг. (главн.) + сокр. (сущ.)
            if (preposition != null && !preposition.isEmpty()) {
                long prepositionCase = getCaseByPreposition(preposition);
                List<String> matchList = jMorfSdk.getDerivativeForm(acronymMainWord, prepositionCase);
                removeIf(matchList, MorfologyParameters.Numbers.class, mainWordNumbers);
                return matchList.get(0);
            } else {
                long transitivity = mainWordOmoForm.getTheMorfCharacteristics(MorfologyParameters.Transitivity.class);
                if (transitivity == MorfologyParameters.Transitivity.TRAN) {
                    List<String> matchList = jMorfSdk.getDerivativeForm(acronymMainWord, MorfologyParameters.Case.ACCUSATIVE);
                    removeIf(matchList, MorfologyParameters.Numbers.class, mainWordNumbers);
                    return matchList.get(0);
                } else if (transitivity == MorfologyParameters.Transitivity.INTR) {
                    //TODO глагол непереходный. Что делать?
                }
            }
        } else if (acronymTypeOfSpeech == MorfologyParameters.TypeOfSpeech.NOUN && mainWordTypeOfSpeech == MorfologyParameters.TypeOfSpeech.NOUN) {
            //model 3:  сущ. (главн.) + сокр. (сущ.)
            if (preposition != null && !preposition.isEmpty()) {
                long prepositionCase = getCaseByPreposition(preposition);
                List<String> matchList = jMorfSdk.getDerivativeForm(acronymMainWord, prepositionCase);
                removeIf(matchList, MorfologyParameters.Numbers.class, mainWordNumbers);
                return matchList.get(0);
            } else {
                List<String> matchList = jMorfSdk.getDerivativeForm(acronymMainWord, MorfologyParameters.Case.GENITIVE);     //GENITIVE1 GENITIVE2 ???
                removeIf(matchList, MorfologyParameters.Numbers.class, mainWordNumbers);
                //removeIf(jMorfSdk, matchList, MorfologyParameters.Gender.class, mainWordGender);    //мужской - средний род (противоположном направлении, противоположном ключе)
                return matchList.get(0);
            }
        } else if (acronymTypeOfSpeech == MorfologyParameters.TypeOfSpeech.NOUN && mainWordTypeOfSpeech == MorfologyParameters.TypeOfSpeech.NUMERAL) {
            Integer count = Utils.parseInt(collacationMainWord);
            if (count != null) {
                int tailTen = count % 10;
                int tailHundred = count % 100;
                if (tailTen == 1 && tailHundred != 11) {
                    return acronymMainWord;
                } else if ((tailTen == 2 && tailHundred != 12) || (tailTen == 3 && tailHundred != 13) || (tailTen == 4 && tailHundred != 14)) {
                    List<String> matchList = jMorfSdk.getDerivativeForm(acronymMainWord, MorfologyParameters.Case.ACCUSATIVE);
                    removeIf(matchList, MorfologyParameters.Numbers.class, MorfologyParameters.Numbers.SINGULAR);
                    return matchList.get(0);
                } else {
                    List<String> matchList = jMorfSdk.getDerivativeForm(acronymMainWord, MorfologyParameters.Case.ACCUSATIVE);
                    removeIf(matchList, MorfologyParameters.Numbers.class, MorfologyParameters.Numbers.PLURAL);
                    return matchList.get(0);
                }
            }
        }
        return acronymMainWord;
    }

    private void adaptAcronymWords(String[] acronymWords, int mainWordAcronymIndex) throws Exception {

        OmoForm acronymOmoForm = jMorfSdk.getAllCharacteristicsOfForm(acronymWords[mainWordAcronymIndex]).get(0);
        long acronymCase = acronymOmoForm.getTheMorfCharacteristics(MorfologyParameters.Case.IDENTIFIER);

        if (acronymCase != MorfologyParameters.Case.NOMINATIVE) {
            long lastNounNumbers = acronymOmoForm.getTheMorfCharacteristics(MorfologyParameters.Numbers.IDENTIFIER);
            long lastNounGender = acronymOmoForm.getTheMorfCharacteristics(MorfologyParameters.Gender.IDENTIFIER);
            for (int i = acronymWords.length - 1; i >= 0; i--) {
                String curWord = acronymWords[i];
                OmoForm wordOmoForm = jMorfSdk.getAllCharacteristicsOfForm(curWord).get(0);
                if (wordOmoForm.getTypeOfSpeech() == MorfologyParameters.TypeOfSpeech.NOUN) {
                    lastNounNumbers = wordOmoForm.getTheMorfCharacteristics(MorfologyParameters.Numbers.IDENTIFIER);
                    lastNounGender = wordOmoForm.getTheMorfCharacteristics(MorfologyParameters.Gender.IDENTIFIER);
                }
                if (i != mainWordAcronymIndex) {
                    String initialForm = jMorfSdk.getAllCharacteristicsOfForm(curWord).get(0).getInitialFormString();
                    List<String> matchList = jMorfSdk.getDerivativeForm(initialForm, acronymCase);
                    removeIf(matchList, MorfologyParameters.Numbers.class, lastNounNumbers);
                    removeIf(matchList, MorfologyParameters.Gender.class, lastNounGender);       //мужской - средний род (противоположном направлении, противоположном ключе)
                    if (matchList.size() > 0) {
                        acronymWords[i] = matchList.get(0);
                    }
                }
            }
        }
    }

    private void removeIf(List<String> matchList, Class morfologyParameterClass, long param) {
        if (param != 0L) {
            matchList.removeIf(s -> {
                try {
                    OmoFormList omoForms = jMorfSdk.getAllCharacteristicsOfForm(s);
                    return omoForms.isEmpty() || omoForms.get(0).getTheMorfCharacteristics(morfologyParameterClass) != param;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            });
        }
    }

    private long getCaseByPreposition(String preposition) {
        if (GENITIVE_PREPOSITIONS.contains(preposition)) {
            return MorfologyParameters.Case.GENITIVE;  //GENITIVE1 GENITIVE2 ???
        } else if (DATIVE_PREPOSITIONS.contains(preposition)) {
            return MorfologyParameters.Case.DATIVE;
        } else if (ACCUSATIVE_PREPOSITIONS.contains(preposition)) {
            return MorfologyParameters.Case.ACCUSATIVE;  //ACCUSATIVE2 ???
        } else if (ABLTIVE_PREPOSITIONS.contains(preposition)) {
            return MorfologyParameters.Case.ABLTIVE;
        } else if (PREPOSITIONA_PREPOSITIONS.contains(preposition)) {
            return MorfologyParameters.Case.PREPOSITIONA;   //PREPOSITIONA1 PREPOSITIONA2 ???
        } else {
            return MorfologyParameters.Case.NOMINATIVE;   //default
        }
    }

    private List<String> getPossibleInfinitives(String str) {
        char lastChar = str.charAt(str.length() - 1);
        char prevLastChar = str.charAt(str.length() - 2);
        if (lastChar == 'а') {
            return Arrays.asList(
                    str.substring(0, str.length() - 1) + 'о',
                    str.substring(0, str.length() - 1)
            );
        } else if (lastChar == 'ы') {
            return Collections.singletonList(str.substring(0, str.length() - 1) + 'а');
        } else if (lastChar == 'и') {
            return Arrays.asList(
                    str.substring(0, str.length() - 1) + 'я',
                    str.substring(0, str.length() - 1) + 'ь'
            );
        } else if (lastChar == 'е') {
            return Arrays.asList(
                    str.substring(0, str.length() - 1) + 'а',
                    str.substring(0, str.length() - 1) + 'я',
                    str.substring(0, str.length() - 1) + 'о',
                    str.substring(0, str.length() - 1) + 'е',
                    str.substring(0, str.length() - 1) + 'ь'
            );
        } else if (lastChar == 'я') {
            return Collections.singletonList(str.substring(0, str.length() - 1) + 'ь');
        } else if (lastChar == 'ю') {
            return Arrays.asList(
                    str.substring(0, str.length() - 1) + 'е',
                    str.substring(0, str.length() - 1) + 'я',
                    str.substring(0, str.length() - 1) + 'ь'
            );
        } else if (lastChar == 'у') {
            return Arrays.asList(
                    str.substring(0, str.length() - 1) + 'а',
                    str.substring(0, str.length() - 1) + 'о',
                    str.substring(0, str.length() - 1)
            );
        } else if (lastChar == 'й') {
            if (prevLastChar == 'о') {
                return Collections.singletonList(str.substring(0, str.length() - 2) + 'а');
            } else if (prevLastChar == 'е') {
                return Collections.singletonList(str.substring(0, str.length() - 2) + 'я');
            }
        } else if (lastChar == 'м') {
            if (prevLastChar == 'о') {
                return Arrays.asList(
                        str.substring(0, str.length() - 2) + 'о',
                        str.substring(0, str.length() - 2)
                );
            } else if (prevLastChar == 'e') {
                return Collections.singletonList(str.substring(0, str.length() - 2) + 'e');
            } else if (prevLastChar == 'ё') {
                return Collections.singletonList(str.substring(0, str.length() - 1) + 'ь');
            }
        }
        return Collections.emptyList();
    }
}
