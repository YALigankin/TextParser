import beans.Sentence;
import jmorfsdk.JMorfSdk;
import jmorfsdk.load.JMorfSdkLoad;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        //String text = "У кажд. человека должны быть свои инструменты. Отдача последует в противоп. направлении.";
        String text = "Автор книги назвал СССР пережитком прошлого. После распада СССР многое резко поменялось. При оформлении ИП вашей организации будет выдан ИНН.";

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
        /*String d1 = getAcronymTrueForm(jMorfSdk, "обсуждаемый", "тема", null);
        String d2 = getAcronymTrueForm(jMorfSdk, "общество", "член", null);
        String d3 = getAcronymTrueForm(jMorfSdk, "институт", "дорога", "в");*/
    }
}
