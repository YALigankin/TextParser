import beans.Sentence;
import jmorfsdk.JMorfSdk;
import jmorfsdk.load.JMorfSdkLoad;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;

public class TestAbbrResolver {


    @Test
    public void testSentences() throws Exception {

        JMorfSdk jMorfSdk = JMorfSdkLoad.loadFullLibrary();

        Importer importer = new ImportMemory();
        importer.doImport(this.getClass().getResourceAsStream("/abbrDic.txt"), "abbrDic.txt");

        //усечение в нужной форме
        String text1 = "У кажд. человека должны быть свои инструменты. Отдача последует в противоп. направлении.";
        String expText1 = "У каждого человека должны быть свои инструменты. Отдача последует в противоположном направлении.";
        assertEquals(expText1, splitTest(jMorfSdk, text1));

        //аббревиатура с неcколькими словами, которые д.б. согласованы
        String text2 = "Автор книги назвал СССР пережитком прошлого. После распада СССР многое резко поменялось.";
        String expText2 = "Автор книги назвал Союз Советских Социалистических Республик пережитком прошлого. После распада Союза Советских Социалистических Республик многое резко поменялось.";
        assertEquals(expText2, splitTest(jMorfSdk, text2));

        //аббревиатура с несколькими словами, которые д.б. согласованы
        String text3 = "При оформлении ИП вашей организации будет выдан ИНН.";
        String expText3 = "При оформлении индивидуального предпринимателя вашей организации будет выдан идентификационный номер налогоплательщика.";
        assertEquals(expText3, splitTest(jMorfSdk, text3));

        //стяжение в производной форме
        String text4 = "Каждый член общ-ва имеет право голоса.";
        String expText4 = "Каждый член общества имеет право голоса.";
        assertEquals(expText4, splitTest(jMorfSdk, text4));

        //стяжение в производной форме
        String text5 = "Новый ректор ин-та пообещал студентам хорошие стипендии.";
        String expText5 = "Новый ректор института пообещал студентам хорошие стипендии.";
        assertEquals(expText5, splitTest(jMorfSdk, text5));

        //сущ. + стяжение в производной форме
        String text6 = "Решено построить завод по пр-ву автомобилей.";
        String expText6 = "Решено построить завод по производству автомобилей.";
        assertEquals(expText6, splitTest(jMorfSdk, text6));

        //гл. + пред. + стяжение в производной форме (дат. падеж)
        String text7 = "Теперь нужно переходить к стр-ву крыши.";
        String expText7 = "Теперь нужно переходить к строительству крыши.";
        assertEquals(expText7, splitTest(jMorfSdk, text7));

        //гл. + пред. + стяжение в производной форме (твор. падеж)
        String text8 = "Угроза уничтожения нависла над гос-вом.";
        String expText8 = "Угроза уничтожения нависла над государством.";
        assertEquals(expText8, splitTest(jMorfSdk, text8));

        //аббревиатура с несколькими словами, которые д.б. согласованы
        String text9 = "Данную роль дали Президенту РФ.";
        String expText9 = "Данную роль дали Президенту Российской Федерации.";
        assertEquals(expText9, splitTest(jMorfSdk, text9));

        //TODO непереходный глагол - что делать: соответствует кодексу
        /*String text10 = "Это не соответствует ТК.";
        String expText10 = "Это не соответствует Трудовому кодексу Российской Федерации";
        assertEquals(expText10, splitTest(jMorfSdk, text10));*/

        //переходный глагол: оценить диссертацию
        String text11 = "Комиссии предстоит оценить дис. выпускника.";
        String expText11 = "Комиссии предстоит оценить диссертацию выпускника.";
        assertEquals(expText11, splitTest(jMorfSdk, text11));

        //общепринятое сокращение без точки
        /*String text12 = "Крупнейшими водными каналами изучаемой территории являются реки Дон (объем годового притока воды – 4,42 куб. км), Хопер (1,82 куб. км), Воронеж (1,77 куб. км) и Ворона (1,28 куб. км).";
        String expText12 = "Крупнейшими водными каналами изучаемой территории являются реки Дон (объем годового притока воды – 4,42 кубический километр), Хопер (1,82 кубический километр), Воронеж (1,77 кубический километр) и Ворона (1,28 кубический километр).";
        assertEquals(expText12, splitTest(jMorfSdk, text12));*/

        //правильная форма сокращения (сущ.) после числа
        String text13 = "Школа выделила 234 м проволоки.";
        String expText13 = "Школа выделила 234 Метра проволоки.";
        assertEquals(expText13, splitTest(jMorfSdk, text13));

        jMorfSdk.finish();

    }

    private String splitTest(JMorfSdk jMorfSdk, String text) throws Exception {

        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();
        abbrResolver.setJMorfSdk(jMorfSdk);

        Iterator<Sentence> iter = new TextManager(patternFinder, abbrResolver, MemoryDictionary.getInstance()).splitText(text).iterator();

        StringBuilder sb = new StringBuilder();
        while (iter.hasNext()) {
            Sentence sentence = iter.next();
            sb.append(abbrResolver.resolveAcronyms(sentence)).append(".");
            if (iter.hasNext()) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }
}
