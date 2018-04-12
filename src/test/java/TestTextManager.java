import beans.Sentence;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.charset.Charset;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestTextManager {

    @Test
    public void testSplitText1() throws Exception {

        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();

        TextManager textManager = new TextManager(patternFinder, abbrResolver);

        String text = IOUtils.toString(this.getClass().getResourceAsStream("/text1.txt"), Charset.forName("windows-1251"));

        List<Sentence> sentences = textManager.splitText(text);
        assertEquals(9, sentences.size());

        assertEquals(10, sentences.get(0).getDescriptors().size());
        assertEquals(25, sentences.get(1).getDescriptors().size());
        assertEquals(48, sentences.get(2).getDescriptors().size());
        assertEquals(9, sentences.get(3).getDescriptors().size());
        assertEquals(30, sentences.get(4).getDescriptors().size());
        assertEquals(7, sentences.get(5).getDescriptors().size());
        assertEquals(39, sentences.get(6).getDescriptors().size());
        assertEquals(7, sentences.get(7).getDescriptors().size());
        assertEquals(17, sentences.get(8).getDescriptors().size());
    }

    @Test
    //ќбщеприн€тые сокращени€: км
    public void testSplitText2() throws Exception {

        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();

        TextManager textManager = new TextManager(patternFinder, abbrResolver);

        String text = IOUtils.toString(this.getClass().getResourceAsStream("/text2.txt"), Charset.forName("windows-1251"));

        List<Sentence> sentences = textManager.splitText(text);
        assertEquals(14, sentences.size());
    }

    @Ignore
    @Test
    //пр€ма€ речь не обрабытываетс€
    public void testSplitText3() throws Exception {

        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();

        TextManager textManager = new TextManager(patternFinder, abbrResolver);

        String text = IOUtils.toString(this.getClass().getResourceAsStream("/text3.txt"), Charset.forName("windows-1251"));

        List<Sentence> sentences = textManager.splitText(text);
        assertEquals(14, sentences.size());
    }

    @Ignore
    @Test
    //пр€ма€ речь не обрабатываетс€
    public void testSplitText4() throws Exception {

        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();

        TextManager textManager = new TextManager(patternFinder, abbrResolver);

        String text = IOUtils.toString(this.getClass().getResourceAsStream("/text4.txt"), Charset.forName("windows-1251"));

        List<Sentence> sentences = textManager.splitText(text);
        assertEquals(14, sentences.size());
    }

    @Test
    public void testSplitText5() throws Exception {

        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();

        TextManager textManager = new TextManager(patternFinder, abbrResolver);

        String text = IOUtils.toString(this.getClass().getResourceAsStream("/text5.txt"), Charset.forName("windows-1251"));

        List<Sentence> sentences = textManager.splitText(text);
        assertEquals(7, sentences.size());
    }

    @Ignore
    @Test
    //пр€ма€ речь не обрабатываетс€
    public void testSplitText6() throws Exception {

        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();

        TextManager textManager = new TextManager(patternFinder, abbrResolver);

        String text = IOUtils.toString(this.getClass().getResourceAsStream("/text6.txt"), Charset.forName("windows-1251"));

        List<Sentence> sentences = textManager.splitText(text);
        assertEquals(14, sentences.size());
    }
}
