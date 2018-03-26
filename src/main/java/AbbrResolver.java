import beans.Descriptor;
import beans.DescriptorType;
import beans.Sentence;
import grammeme.MorfologyParameters;
import jmorfsdk.JMorfSdk;
import morphologicalstructures.OmoForm;
import storagestructures.OmoFormList;

import java.util.*;

public class AbbrResolver {

    public void fillAbbrDescriptions(List<Descriptor> descriptors) throws Exception {
        DBManager dbManager = DBManager.getInstance();
        Iterator<Descriptor> iter = descriptors.iterator();
        List<String> notFounded = new ArrayList<>();
        while (iter.hasNext()) {
            Descriptor curDescriptor = iter.next();
            if (curDescriptor.getType().equals(DescriptorType.SHORT_WORD)) {
                List<String> longForms = dbManager.findAbbrLongForms(curDescriptor.getValue());    //TODO затратный поиск (нужно запрашивать пачкой)
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

    public String resolveAcronyms(JMorfSdk jMorfSdk, Sentence sentence) throws Exception {
        List<Descriptor> descriptors = sentence.getDescriptors();
        String copy = new String(sentence.getContent());
        for (int i = 0; i < descriptors.size(); i++) {
            Descriptor curDescriptor = descriptors.get(i);
            if (Objects.equals(DescriptorType.SHORT_WORD, curDescriptor.getType())) {

                String[] acronymWords = curDescriptor.getDesc().split(" ");
                boolean[] capitalizeWords = new boolean[acronymWords.length];

                int mainWordAcronymIndex = 0;

                //prepare - toLowerCase + find main word
                if (acronymWords.length > 1) {
                    for (int j = 0; j < acronymWords.length; j++) {
                        String word = acronymWords[j];
                        if (Character.isUpperCase(word.charAt(0))) {
                            acronymWords[j] = Utils.uncapitalize(word);
                            capitalizeWords[j] = true;
                        }
                    }
                    mainWordAcronymIndex = getMainWordAcronymIndex(jMorfSdk, acronymWords);
                }

                String collacationMainWord = getMainWord(jMorfSdk, descriptors, i, acronymWords[mainWordAcronymIndex]);

                String trueForm;
                if (collacationMainWord != null) {
                    trueForm = getTrueAcronymForm(jMorfSdk, acronymWords[mainWordAcronymIndex], collacationMainWord, "");
                    if (capitalizeWords[mainWordAcronymIndex]) {
                        trueForm = curDescriptor.getDesc().replaceAll(Utils.capitalize(acronymWords[mainWordAcronymIndex]), Utils.capitalize(trueForm));
                    } else {
                        trueForm = curDescriptor.getDesc().replaceAll(acronymWords[mainWordAcronymIndex], trueForm);
                    }
                } else {
                    trueForm = curDescriptor.getDesc();
                }

                copy = copy.replaceAll(curDescriptor.getValue(), trueForm);
            }
        }
        return copy;
    }

    private int getMainWordAcronymIndex(JMorfSdk jMorfSdk, String[] acronymWords) throws Exception {
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
        return 0;
    }

    private String getMainWord(JMorfSdk jMorfSdk, List<Descriptor> descriptors, int acronymIndex, String acronym) throws Exception {

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


    private String getTrueAcronymForm(JMorfSdk jMorfSdk, String acronymFull, String mainWord, String preposition) throws Exception {
        OmoForm acronymOmoForm = jMorfSdk.getAllCharacteristicsOfForm(acronymFull).get(0);
        byte acronymTypeOfSpeech = acronymOmoForm.getTypeOfSpeech();

        OmoForm mainWordOmoForm = jMorfSdk.getAllCharacteristicsOfForm(mainWord.toLowerCase()).get(0);
        byte mainWordTypeOfSpeech = mainWordOmoForm.getTypeOfSpeech();
        long mainWordCase = mainWordOmoForm.getTheMorfCharacteristics(MorfologyParameters.Case.IDENTIFIER);
        long mainWordNumbers = mainWordOmoForm.getTheMorfCharacteristics(MorfologyParameters.Numbers.IDENTIFIER);
        long mainWordGender = mainWordOmoForm.getTheMorfCharacteristics(MorfologyParameters.Gender.IDENTIFIER);

        if (Arrays.asList(MorfologyParameters.TypeOfSpeech.ADJECTIVEFULL, MorfologyParameters.TypeOfSpeech.ADJECTIVESHORT, MorfologyParameters.TypeOfSpeech.NOUNPRONOUN,
                MorfologyParameters.TypeOfSpeech.PARTICIPLE, MorfologyParameters.TypeOfSpeech.PARTICIPLEFULL, MorfologyParameters.TypeOfSpeech.NUMERAL).contains(acronymTypeOfSpeech)) {
            //model 1: сокр. (прил., местоим., причастие, числит.) + сущ. (глав.)
            List<String> matchList = jMorfSdk.getDerivativeForm(acronymFull, mainWordCase);
            removeIf(jMorfSdk, matchList, MorfologyParameters.Numbers.class, mainWordNumbers);
            //removeIf(jMorfSdk, matchList, MorfologyParameters.Gender.class, mainWordGender);       //мужской - средний род (противоположном направлении, противоположном ключе)
            return matchList.get(0);
        } else if (acronymTypeOfSpeech == MorfologyParameters.TypeOfSpeech.NOUN && mainWordTypeOfSpeech == MorfologyParameters.TypeOfSpeech.VERB) {
            //model 2:  глаг. (главн.) + сокр. (сущ.)
            if (preposition != null && !preposition.isEmpty()) {
                long prepositionCase = getCaseByPreposition(preposition);
                List<String> matchList = jMorfSdk.getDerivativeForm(acronymFull, prepositionCase);
                removeIf(jMorfSdk, matchList, MorfologyParameters.Numbers.class, mainWordNumbers);
                return matchList.get(0);
            } else {
                // TODO глагол переходный ?
            }
        } else if (acronymTypeOfSpeech == MorfologyParameters.TypeOfSpeech.NOUN && mainWordTypeOfSpeech == MorfologyParameters.TypeOfSpeech.NOUN) {
            //model 3:  сущ. (главн.) + сокр. (сущ.)
            if (preposition != null && !preposition.isEmpty()) {
                long prepositionCase = getCaseByPreposition(preposition);
                List<String> matchList = jMorfSdk.getDerivativeForm(acronymFull, prepositionCase);
                removeIf(jMorfSdk, matchList, MorfologyParameters.Numbers.class, mainWordNumbers);
                return matchList.get(0);
            } else {
                List<String> matchList = jMorfSdk.getDerivativeForm(acronymFull, MorfologyParameters.Case.GENITIVE);     //GENITIVE1 GENITIVE2 ???
                removeIf(jMorfSdk, matchList, MorfologyParameters.Numbers.class, mainWordNumbers);
                //removeIf(jMorfSdk, matchList, MorfologyParameters.Gender.class, mainWordGender);    //мужской - средний род (противоположном направлении, противоположном ключе)
                return matchList.get(0);
            }
        }
        return acronymFull;
    }

    private void removeIf(JMorfSdk jMorfSdk, List<String> matchList, Class morfologyParameterClass, long param) {
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
