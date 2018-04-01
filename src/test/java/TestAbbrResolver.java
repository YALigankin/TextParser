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

        String text1 = "� ����. �������� ������ ���� ���� �����������. ������ ��������� � ��������. �����������.";
        String expText1 = "� ������� �������� ������ ���� ���� �����������. ������ ��������� � ��������������� �����������.";
        assertEquals(expText1, splitTest(jMorfSdk, text1));

        String text2 = "����� ����� ������ ���� ���������� ��������. ����� ������� ���� ������ ����� ����������.";
        String expText2 = "����� ����� ������ ���� ��������� ���������������� ��������� ���������� ��������. ����� ������� ����� ��������� ���������������� ��������� ������ ����� ����������.";
        assertEquals(expText2, splitTest(jMorfSdk, text2));

        String text3 = "��� ���������� �� ����� ����������� ����� ����� ���.";
        String expText3 = "��� ���������� ��������������� ��������������� ����� ����������� ����� ����� ����������������� ����� �����������������.";
        assertEquals(expText3, splitTest(jMorfSdk, text3));

        jMorfSdk.finish();

    }

    private String splitTest(JMorfSdk jMorfSdk, String text) throws Exception {

        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();
        abbrResolver.setJMorfSdk(jMorfSdk);
        Iterator<Sentence> iter = new TextManager(patternFinder, abbrResolver).splitText(text).iterator();

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
