import beans.Sentence;
import jmorfsdk.JMorfSdk;
import jmorfsdk.load.JMorfSdkLoad;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        //String text = "� ����. �������� ������ ���� ���� �����������. ������ ��������� � ��������. �����������.";
        String text = "����� ����� ������ ���� ���������� ��������. ����� ������� ���� ������ ����� ����������. ��� ���������� �� ����� ����������� ����� ����� ���.";

        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();
        TextManager textManager = new TextManager(patternFinder, abbrResolver);

        List<Sentence> sentences = textManager.splitText(text);

        JMorfSdk jMorfSdk = JMorfSdkLoad.loadFullLibrary();

        List<String> result = new ArrayList<>(sentences.size());

        for (Sentence sentence : sentences) {
            result.add(abbrResolver.resolveAcronyms(jMorfSdk, sentence));
        }

        System.out.println();
        for (String s : result) {
            System.out.println(s);
        }

        jMorfSdk.finish();
        /*String d1 = getAcronymTrueForm(jMorfSdk, "�����������", "����", null);
        String d2 = getAcronymTrueForm(jMorfSdk, "��������", "����", null);
        String d3 = getAcronymTrueForm(jMorfSdk, "��������", "������", "�");*/
    }
}
