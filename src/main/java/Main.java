import beans.Sentence;
import jmorfsdk.JMorfSdk;
import jmorfsdk.load.JMorfSdkLoad;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        //String text = "У кажд. человека должны быть свои инструменты. Отдача последует в противоп. направлении.";
        String text = "Автор книги назвал СССР пережитком прошлого. После распада СССР многое резко поменялось. При оформлении ИП вашей организации будет выдан ИНН.";

        JMorfSdk jMorfSdk = JMorfSdkLoad.loadFullLibrary();
        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();
        abbrResolver.setJMorfSdk(jMorfSdk);

        TextManager textManager = new TextManager(patternFinder, abbrResolver);

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
