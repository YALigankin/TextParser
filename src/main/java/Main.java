import beans.Descriptor;
import beans.Sentence;
import grammeme.MorfologyParameters;
import jmorfsdk.JMorfSdk;
import morphologicalstructures.OmoForm;

import java.util.Arrays;
import java.util.List;

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
        for (Sentence sentence : sentences) {
            sentence.print();
        }
        /*JMorfSdk jMorfSdk = JMorfSdkLoad.loadFullLibrary();
        System.out.println(text);
        System.out.println(abbrResolver.resolveAbbrs(text, descriptors));
        StringBuilder sb;
        for (String sentence : sentences) {
            String[] words = sentence.split(" ");     //TODO ����� ������� ������
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

        String d1 = getAcronymTrueForm(jMorfSdk, "�����������", "����", null);
        String d2 = getAcronymTrueForm(jMorfSdk, "��������", "����", null);
        String d3 = getAcronymTrueForm(jMorfSdk, "��������", "������", "�");

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
