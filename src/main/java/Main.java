import beans.Sentence;
import jmorfsdk.JMorfSdk;
import jmorfsdk.load.JMorfSdkLoad;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        //String text = "� ����. �������� ������ ���� ���� �����������. ������ ��������� � ��������. �����������.";
        String text = "����� ����� ������ ���� ���������� ��������. ����� ������� ���� ������ ����� ����������. ��� ���������� �� ����� ����������� ����� ����� ���.";

        JMorfSdk jMorfSdk = JMorfSdkLoad.loadFullLibrary();
        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();
        abbrResolver.setJMorfSdk(jMorfSdk);

        Importer importer = new ImportMemory();
        importer.doImport(Main.class.getResourceAsStream("/abbrDic.txt"), "abbrDic.txt");

        TextManager textManager = new TextManager(patternFinder, abbrResolver, MemoryDictionary.getInstance());

        List<Sentence> sentences = textManager.splitText(text);

        List<String> result = new ArrayList<>(sentences.size());

        for (Sentence sentence : sentences) {
            result.add(abbrResolver.resolveAcronyms(sentence));
        }

        System.out.println();
        for (String s : result) {
            System.out.println(s);
        }

        jMorfSdk.finish();
    }
}
