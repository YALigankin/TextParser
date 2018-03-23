import beans.Descriptor;
import beans.Sentence;
import grammeme.MorfologyParameters;
import jmorfsdk.JMorfSdk;
import morphologicalstructures.OmoForm;

import java.util.Arrays;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        String text = "У кажд. человека должны быть свои инструменты. Отдача последует в противоп. направлении.";
        //String text = "Это программа, которая сейчас обрабатывает 1 предложение.";
        //String text = "В 1991 году СССР распался. Глава ЦКПСС был упразднен. Вскоре появилась должность президента. Её занял Ельцин Б.Н. При оформлении ИП вашей организации будет выдан ИНН. Форма собственности ЗАО.";
        //String text = "Первое предложение (по идее следом должно быть второе предложение...Но не тут-то было!). Третье предложение.";
        //String text = "На заседании комиссии 23 и 24 сентября 1922 г. (под председательством В.В. Молотова) принимается проект Сталина И.В. Грузинский проект отклоняется.";

        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();

        TextManager textManager = new TextManager(patternFinder, abbrResolver);

        List<Sentence> sentences = textManager.splitText(text);
        for (Sentence sentence : sentences) {
            sentence.print();
        }
        /*JMorfSdk jMorfSdk = JMorfSdkLoad.loadFullLibrary();
        System.out.println(text);
        System.out.println(abbrResolver.resolveAbbrs(text, descriptors));
        StringBuilder sb;
        for (String sentence : sentences) {
            String[] words = sentence.split(" ");     //TODO самый простой случай
            sb = new StringBuilder();
            for (int i = 0; i < words.length; i++) {
                Descriptor foundDesc = getAbbr(words[i], descriptors);
                if (foundDesc != null) {
                    String ddd = getAcronymTrueForm(jMorfSdk, foundDesc.getDesc(), words[i + 1], "");   //model 1
                    words[i] = ddd;
                }
                sb.append(words[i]).append(" ");
            }
            System.out.println(sb.toString());
        }

        String d1 = getAcronymTrueForm(jMorfSdk, "обсуждаемый", "тема", null);
        String d2 = getAcronymTrueForm(jMorfSdk, "общество", "член", null);
        String d3 = getAcronymTrueForm(jMorfSdk, "институт", "дорога", "в");

        jMorfSdk.finish();*/
    }

    private static String getAcronymTrueForm(JMorfSdk jMorfSdk, String acronymFull, String mainWord, String preposition) throws Exception {
        OmoForm acronymOmoForm = jMorfSdk.getAllCharacteristicsOfForm(acronymFull).get(0);
        byte acronymTypeOfSpeech = acronymOmoForm.getTypeOfSpeech();

        OmoForm mainWordOmoForm = jMorfSdk.getAllCharacteristicsOfForm(mainWord).get(0);
        byte mainWordTypeOfSpeech = mainWordOmoForm.getTypeOfSpeech();
        long mainWordCase = mainWordOmoForm.getTheMorfCharacteristics(MorfologyParameters.Case.IDENTIFIER);
        long mainWordNumbers = mainWordOmoForm.getTheMorfCharacteristics(MorfologyParameters.Numbers.IDENTIFIER);
        long mainWordGender = mainWordOmoForm.getTheMorfCharacteristics(MorfologyParameters.Gender.IDENTIFIER);

        if (Arrays.asList(MorfologyParameters.TypeOfSpeech.ADJECTIVEFULL, MorfologyParameters.TypeOfSpeech.ADJECTIVESHORT, MorfologyParameters.TypeOfSpeech.NOUNPRONOUN,
                MorfologyParameters.TypeOfSpeech.PARTICIPLE, MorfologyParameters.TypeOfSpeech.PARTICIPLEFULL, MorfologyParameters.TypeOfSpeech.NUMERAL).contains(acronymTypeOfSpeech)) {
            //model 1: сокр. (прил., местоим., причастие, числит.) + сущ. (глав.)
            List<String> matchList = jMorfSdk.getDerivativeForm(acronymFull, mainWordCase);
            matchList.removeIf(s -> {
                try {
                    return jMorfSdk.getAllCharacteristicsOfForm(s).get(0).getTheMorfCharacteristics(MorfologyParameters.Numbers.IDENTIFIER) != mainWordNumbers;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            });
            //мужской - средний род (противоположном направлении, противоположном ключе)
            /*matchList.removeIf(s -> {
                try {
                    return jMorfSdk.getAllCharacteristicsOfForm(s).get(0).getTheMorfCharacteristics(MorfologyParameters.Gender.IDENTIFIER) != mainWordGender;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            });*/
            return matchList.get(0);
        } else if (acronymTypeOfSpeech == MorfologyParameters.TypeOfSpeech.NOUN && mainWordTypeOfSpeech == MorfologyParameters.TypeOfSpeech.VERB) {
            //model 2:  глаг. (главн.) + сокр. (сущ.)
            if (preposition != null && !preposition.isEmpty()) {
                long prepositionCase = getCaseByPreposition(preposition);
                List<String> matchList = jMorfSdk.getDerivativeForm(acronymFull, mainWordCase);
                matchList.removeIf(s -> {
                    try {
                        return jMorfSdk.getAllCharacteristicsOfForm(s).get(0).getTheMorfCharacteristics(MorfologyParameters.Numbers.IDENTIFIER) != mainWordNumbers;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                });
                return matchList.get(0);
            } else {
                // TODO глагол переходный ?
            }
        } else if (acronymTypeOfSpeech == MorfologyParameters.TypeOfSpeech.NOUN && mainWordTypeOfSpeech == MorfologyParameters.TypeOfSpeech.NOUN) {
            //model 3:  сущ. (главн.) + сокр. (сущ.)
            if (preposition != null && !preposition.isEmpty()) {
                long prepositionCase = getCaseByPreposition(preposition);
                List<String> matchList = jMorfSdk.getDerivativeForm(acronymFull, mainWordCase);
                matchList.removeIf(s -> {
                    try {
                        return jMorfSdk.getAllCharacteristicsOfForm(s).get(0).getTheMorfCharacteristics(MorfologyParameters.Numbers.IDENTIFIER) != mainWordNumbers;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                });
                return matchList.get(0);
            } else {
                long caseAcronym = MorfologyParameters.Case.ACCUSATIVE;
                List<String> matchList = jMorfSdk.getDerivativeForm(acronymFull, caseAcronym);
                matchList.removeIf(s -> {
                    try {
                        return jMorfSdk.getAllCharacteristicsOfForm(s).get(0).getTheMorfCharacteristics(MorfologyParameters.Numbers.IDENTIFIER) != mainWordNumbers;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                });
                //мужской - средний род (противоположном направлении, противоположном ключе)
                /*matchList.removeIf(s -> {
                    try {
                        return jMorfSdk.getAllCharacteristicsOfForm(s).get(0).getTheMorfCharacteristics(MorfologyParameters.Gender.IDENTIFIER) != mainWordGender;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return false;
                });*/
                return matchList.get(0);
            }
        }
        return acronymFull;
    }

    private static long getCaseByPreposition(String preposition) {
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
