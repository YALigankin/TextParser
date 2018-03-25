import beans.Descriptor;
import beans.DescriptorType;
import beans.Sentence;
import grammeme.MorfologyParameters;
import jmorfsdk.JMorfSdk;
import jmorfsdk.load.JMorfSdkLoad;
import morphologicalstructures.OmoForm;
import storagestructures.OmoFormList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Main {

    public static void main(String[] args) throws Exception {

        String text = "� ����. �������� ������ ���� ���� �����������. ������ ��������� � ��������. �����������.";
        //String text = "��� ���������, ������� ������ ������������ 1 �����������.";
        //String text = "� 1991 ���� ���� ��������. ����� ����� ��� ���������. ������ ��������� ��������� ����������. Ÿ ����� ������ �.�. ��� ���������� �� ����� ����������� ����� ����� ���. ����� ������������� ���.";
        //String text = "������ ����������� (�� ���� ������ ������ ���� ������ �����������...�� �� ���-�� ����!). ������ �����������.";
        //String text = "�� ��������� �������� 23 � 24 �������� 1922 �. (��� ����������������� �.�. ��������) ����������� ������ ������� �.�. ���������� ������ �����������.";

        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();
        TextManager textManager = new TextManager(patternFinder, abbrResolver);

        List<Sentence> sentences = textManager.splitText(text);

        JMorfSdk jMorfSdk = JMorfSdkLoad.loadFullLibrary();

        List<String> result = new ArrayList<>(sentences.size());

        for (Sentence sentence : sentences) {
            List<Descriptor> descriptors = sentence.getDescriptors();
            String copy = new String(sentence.getContent());
            for (int i = 0; i < descriptors.size(); i++) {
                Descriptor curDescriptor = descriptors.get(i);
                if (Objects.equals(DescriptorType.SHORT_WORD, curDescriptor.getType())) {
                    String mainWord = getMainWord(jMorfSdk, descriptors, i);
                    String trueForm = mainWord != null ? getAcronymTrueForm(jMorfSdk, curDescriptor.getDesc(), mainWord, "") : curDescriptor.getDesc();
                    copy = copy.replaceAll(curDescriptor.getValue(), trueForm);
                }
            }
            result.add(copy);
        }

        jMorfSdk.finish();

        System.out.println();
        for (String s : result) {
            System.out.println(s);
        }
        /*String d1 = getAcronymTrueForm(jMorfSdk, "�����������", "����", null);
        String d2 = getAcronymTrueForm(jMorfSdk, "��������", "����", null);
        String d3 = getAcronymTrueForm(jMorfSdk, "��������", "������", "�");*/
    }

    private static String getMainWord(JMorfSdk jMorfSdk, List<Descriptor> descriptors, int acronymIndex) throws Exception {

        OmoFormList omoForms = jMorfSdk.getAllCharacteristicsOfForm(descriptors.get(acronymIndex).getDesc());
        if (omoForms.isEmpty()) {
            return null;
        }
        byte acronymTypeOfSpeech = omoForms.get(0).getTypeOfSpeech();

        int wordIndex = acronymIndex;

        if (Arrays.asList(MorfologyParameters.TypeOfSpeech.ADJECTIVEFULL, MorfologyParameters.TypeOfSpeech.ADJECTIVESHORT, MorfologyParameters.TypeOfSpeech.NOUNPRONOUN,
                MorfologyParameters.TypeOfSpeech.PARTICIPLE, MorfologyParameters.TypeOfSpeech.PARTICIPLEFULL, MorfologyParameters.TypeOfSpeech.NUMERAL).contains(acronymTypeOfSpeech)) {
            //������� ����� �������
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
            //������� ����� ������
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


    private static String getAcronymTrueForm(JMorfSdk jMorfSdk, String acronymFull, String mainWord, String preposition) throws Exception {
        OmoForm acronymOmoForm = jMorfSdk.getAllCharacteristicsOfForm(acronymFull).get(0);
        byte acronymTypeOfSpeech = acronymOmoForm.getTypeOfSpeech();

        OmoForm mainWordOmoForm = jMorfSdk.getAllCharacteristicsOfForm(mainWord.toLowerCase()).get(0);
        byte mainWordTypeOfSpeech = mainWordOmoForm.getTypeOfSpeech();
        long mainWordCase = mainWordOmoForm.getTheMorfCharacteristics(MorfologyParameters.Case.IDENTIFIER);
        long mainWordNumbers = mainWordOmoForm.getTheMorfCharacteristics(MorfologyParameters.Numbers.IDENTIFIER);
        long mainWordGender = mainWordOmoForm.getTheMorfCharacteristics(MorfologyParameters.Gender.IDENTIFIER);

        if (Arrays.asList(MorfologyParameters.TypeOfSpeech.ADJECTIVEFULL, MorfologyParameters.TypeOfSpeech.ADJECTIVESHORT, MorfologyParameters.TypeOfSpeech.NOUNPRONOUN,
                MorfologyParameters.TypeOfSpeech.PARTICIPLE, MorfologyParameters.TypeOfSpeech.PARTICIPLEFULL, MorfologyParameters.TypeOfSpeech.NUMERAL).contains(acronymTypeOfSpeech)) {
            //model 1: ����. (����., �������., ���������, ������.) + ���. (����.)
            List<String> matchList = jMorfSdk.getDerivativeForm(acronymFull, mainWordCase);
            matchList.removeIf(s -> {
                try {
                    return jMorfSdk.getAllCharacteristicsOfForm(s).get(0).getTheMorfCharacteristics(MorfologyParameters.Numbers.IDENTIFIER) != mainWordNumbers;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            });
            //������� - ������� ��� (��������������� �����������, ��������������� �����)
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
            //model 2:  ����. (�����.) + ����. (���.)
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
                // TODO ������ ���������� ?
            }
        } else if (acronymTypeOfSpeech == MorfologyParameters.TypeOfSpeech.NOUN && mainWordTypeOfSpeech == MorfologyParameters.TypeOfSpeech.NOUN) {
            //model 3:  ���. (�����.) + ����. (���.)
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
                //������� - ������� ��� (��������������� �����������, ��������������� �����)
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
        if (Arrays.asList("���", "�", "��", "��", "�", "�����", "��", "�����", "�����", "���", "������").contains(preposition)) {
            return MorfologyParameters.Case.GENITIVE;  //GENITIVE1 GENITIVE2 ???
        } else if (Arrays.asList("�", "��").contains(preposition)) {
            return MorfologyParameters.Case.DATIVE;
        } else if (Arrays.asList("�", "��", "��", "���", "�����").contains(preposition)) {
            return MorfologyParameters.Case.ACCUSATIVE;  //ACCUSATIVE2 ???
        } else if (Arrays.asList("��", "���", "���", "�����", "�").contains(preposition)) {
            return MorfologyParameters.Case.ABLTIVE;
        } else if (Arrays.asList("�", "��", "�", "��", "���", "���").contains(preposition)) {
            return MorfologyParameters.Case.PREPOSITIONA;   //PREPOSITIONA1 PREPOSITIONA2 ???
        } else {
            return MorfologyParameters.Case.NOMINATIVE;   //default
        }
    }
}
