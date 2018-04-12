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

        //�������� � ������ �����
        String text1 = "� ����. �������� ������ ���� ���� �����������. ������ ��������� � ��������. �����������.";
        String expText1 = "� ������� �������� ������ ���� ���� �����������. ������ ��������� � ��������������� �����������.";
        assertEquals(expText1, splitTest(jMorfSdk, text1));

        //������������ � ��c�������� �������, ������� �.�. �����������
        String text2 = "����� ����� ������ ���� ���������� ��������. ����� ������� ���� ������ ����� ����������.";
        String expText2 = "����� ����� ������ ���� ��������� ���������������� ��������� ���������� ��������. ����� ������� ����� ��������� ���������������� ��������� ������ ����� ����������.";
        assertEquals(expText2, splitTest(jMorfSdk, text2));

        //������������ � ����������� �������, ������� �.�. �����������
        String text3 = "��� ���������� �� ����� ����������� ����� ����� ���.";
        String expText3 = "��� ���������� ��������������� ��������������� ����� ����������� ����� ����� ����������������� ����� �����������������.";
        assertEquals(expText3, splitTest(jMorfSdk, text3));

        //�������� � ����������� �����
        String text4 = "������ ���� ���-�� ����� ����� ������.";
        String expText4 = "������ ���� �������� ����� ����� ������.";
        assertEquals(expText4, splitTest(jMorfSdk, text4));

        //�������� � ����������� �����
        String text5 = "����� ������ ��-�� �������� ��������� ������� ���������.";
        String expText5 = "����� ������ ��������� �������� ��������� ������� ���������.";
        assertEquals(expText5, splitTest(jMorfSdk, text5));

        //���. + �������� � ����������� �����
        String text6 = "������ ��������� ����� �� ��-�� �����������.";
        String expText6 = "������ ��������� ����� �� ������������ �����������.";
        assertEquals(expText6, splitTest(jMorfSdk, text6));

        //��. + ����. + �������� � ����������� ����� (���. �����)
        String text7 = "������ ����� ���������� � ���-�� �����.";
        String expText7 = "������ ����� ���������� � ������������� �����.";
        assertEquals(expText7, splitTest(jMorfSdk, text7));

        //��. + ����. + �������� � ����������� ����� (����. �����)
        String text8 = "������ ����������� ������� ��� ���-���.";
        String expText8 = "������ ����������� ������� ��� ������������.";
        assertEquals(expText8, splitTest(jMorfSdk, text8));

        //������������ � ����������� �������, ������� �.�. �����������
        String text9 = "������ ���� ���� ���������� ��.";
        String expText9 = "������ ���� ���� ���������� ���������� ���������.";
        assertEquals(expText9, splitTest(jMorfSdk, text9));

        //TODO ������������ ������ - ��� ������: ������������� �������
        /*String text10 = "��� �� ������������� ��.";
        String expText10 = "��� �� ������������� ��������� ������� ���������� ���������";
        assertEquals(expText10, splitTest(jMorfSdk, text10));*/

        //���������� ������: ������� �����������
        String text11 = "�������� ��������� ������� ���. ����������.";
        String expText11 = "�������� ��������� ������� ����������� ����������.";
        assertEquals(expText11, splitTest(jMorfSdk, text11));

        //������������ ���������� ��� �����
        /*String text12 = "����������� ������� �������� ��������� ���������� �������� ���� ��� (����� �������� ������� ���� � 4,42 ���. ��), ����� (1,82 ���. ��), ������� (1,77 ���. ��) � ������ (1,28 ���. ��).";
        String expText12 = "����������� ������� �������� ��������� ���������� �������� ���� ��� (����� �������� ������� ���� � 4,42 ���������� ��������), ����� (1,82 ���������� ��������), ������� (1,77 ���������� ��������) � ������ (1,28 ���������� ��������).";
        assertEquals(expText12, splitTest(jMorfSdk, text12));*/

        //���������� ����� ���������� (���.) ����� �����
        String text13 = "����� �������� 234 � ���������.";
        String expText13 = "����� �������� 234 ����� ���������.";
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
