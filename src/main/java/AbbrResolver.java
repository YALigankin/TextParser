import beans.Descriptor;
import beans.DescriptorType;
import beans.Sentence;
import grammeme.MorfologyParameters;
import jmorfsdk.JMorfSdk;
import morphologicalstructures.OmoForm;
import storagestructures.OmoFormList;

import java.util.*;

public class AbbrResolver {

    private JMorfSdk jMorfSdk;

    public void setJMorfSdk(JMorfSdk jMorfSdk) {
        this.jMorfSdk = jMorfSdk;
    }

    public void fillAbbrDescriptions(List<Descriptor> descriptors) throws Exception {
        DBManager dbManager = DBManager.getInstance();
        Iterator<Descriptor> iter = descriptors.iterator();
        List<String> notFounded = new ArrayList<>();
        while (iter.hasNext()) {
            Descriptor curDescriptor = iter.next();
            if (curDescriptor.getType().equals(DescriptorType.SHORT_WORD)) {
                List<String> longForms = dbManager.findAbbrLongForms(curDescriptor.getValue());    //TODO затратный поиск (нужно запрашивать пачкой) + cокращения могут повторяться в разных предложениях
                if (!longForms.isEmpty()) {
                    curDescriptor.setDesc(longForms.get(0));           //TODO пока берется первое попавшееся значение аббревиатуры
                } else {
                    notFounded.add(curDescriptor.getValue());
                    iter.remove();    //не смогли найти - Regex неправильно определил - удаляем из списка
                }
            }
        }
        System.out.println("NotFouded:");
        for (String s : notFounded) {
            System.out.println(s);
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
            if (Objects.equals(DescriptorType.SHORT_WORD, curDescriptor.getType())) {

                String[] acronymWords = curDescriptor.getDesc().split(" ");
                boolean[] capitalizeWords = new boolean[acronymWords.length];

                //prepare - toLowerCase with save + find main word
                if (acronymWords.length > 1) {
                    for (int j = 0; j < acronymWords.length; j++) {
                        String word = acronymWords[j];
                        if (Character.isUpperCase(word.charAt(0))) {
                            acronymWords[j] = Utils.uncapitalize(word);
                            capitalizeWords[j] = true;
                        }
                    }
                }

                int mainWordAcronymIndex = getMainWordAcronymIndex(acronymWords);

                String collacationMainWord = getMainWord(descriptors, i, acronymWords[mainWordAcronymIndex]);

                if (collacationMainWord != null) {
                    acronymWords[mainWordAcronymIndex] = getTrueAcronymForm(acronymWords[mainWordAcronymIndex], collacationMainWord, "");
                }

                if (acronymWords.length > 1) {
                    adaptAcronymWords(acronymWords, mainWordAcronymIndex);
                }

                //restoreCase
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
                    if (Objects.equals(MorfologyParameters.TypeOfSpeech.NOUN, omoForm.getTypeOfSpeech())
                            && Objects.equals(MorfologyParameters.Case.NOMINATIVE, omoForm.getTheMorfCharacteristics(MorfologyParameters.Case.class))) {
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
    private String getMainWord(List<Descriptor> descriptors, int acronymIndex, String acronym) throws Exception {

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
                if (Objects.equals(DescriptorType.RUSSIAN_LEX, curDescriptor.getType())) {
                    String curWord = curDescriptor.getValue();
                    OmoForm wordOmoForm = jMorfSdk.getAllCharacteristicsOfForm(curWord.toLowerCase()).get(0);
                    if (Objects.equals(MorfologyParameters.TypeOfSpeech.NOUN, wordOmoForm.getTypeOfSpeech())) {
                        return curWord;
                    }
                }
            }
        } else if (acronymTypeOfSpeech == MorfologyParameters.TypeOfSpeech.NOUN) {
            //главное слово позади
            while (--wordIndex >= 0) {
                Descriptor curDescriptor = descriptors.get(wordIndex);
                if (Objects.equals(DescriptorType.RUSSIAN_LEX, curDescriptor.getType())) {
                    String curWord = curDescriptor.getValue();
                    OmoForm wordOmoForm = jMorfSdk.getAllCharacteristicsOfForm(curWord.toLowerCase()).get(0);
                    if (Objects.equals(MorfologyParameters.TypeOfSpeech.NOUN, wordOmoForm.getTypeOfSpeech())
                            || Objects.equals(MorfologyParameters.TypeOfSpeech.VERB, wordOmoForm.getTypeOfSpeech())) {
                        return curWord;
                    }
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
        } else if (acronymTypeOfSpeech == MorfologyParameters.TypeOfSpeech.NOUN && mainWordTypeOfSpeech == MorfologyParameters.TypeOfSpeech.VERB) {
            //model 2:  глаг. (главн.) + сокр. (сущ.)
            if (preposition != null && !preposition.isEmpty()) {
                long prepositionCase = getCaseByPreposition(preposition);
                List<String> matchList = jMorfSdk.getDerivativeForm(acronymMainWord, prepositionCase);
                removeIf(matchList, MorfologyParameters.Numbers.class, mainWordNumbers);
                return matchList.get(0);
            } else {
                // TODO глагол переходный ?
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
                    //removeIf(matchList, MorfologyParameters.Gender.class, lastNounGender);       //мужской - средний род (противоположном направлении, противоположном ключе)
                    if (matchList.size() > 0) {
                        acronymWords[i] = matchList.get(0);
                    }
                }
            }
        }
    }

    private void removeIf(List<String> matchList, Class morfologyParameterClass, long param) {
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

    private long getCaseByPreposition(String preposition) {
        if (Arrays.asList("без", "у", "до", "от", "с", "около", "из", "возле", "после", "для", "вокруг").contains(preposition)) {
            return MorfologyParameters.Case.GENITIVE;  //GENITIVE1 GENITIVE2 ???
        } else if (Arrays.asList("к", "по").contains(preposition)) {
            return MorfologyParameters.Case.DATIVE;
        } else if (Arrays.asList("в", "за", "на", "про", "через").contains(preposition)) {
            return MorfologyParameters.Case.ACCUSATIVE;  //ACCUSATIVE2 ???
        } else if (Arrays.asList("за", "над", "под", "перед", "с").contains(preposition)) {
            return MorfologyParameters.Case.ABLTIVE;
        } else if (Arrays.asList("в", "на", "о", "об", "обо", "при").contains(preposition)) {
            return MorfologyParameters.Case.PREPOSITIONA;   //PREPOSITIONA1 PREPOSITIONA2 ???
        } else {
            return MorfologyParameters.Case.NOMINATIVE;   //default
        }
    }
}
