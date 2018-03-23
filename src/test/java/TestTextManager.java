import beans.Sentence;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestTextManager {

    @Test
    public void testSplitText1() throws Exception {

        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();

        TextManager textManager = new TextManager(patternFinder, abbrResolver);

        String text = Utils.readFile("C:\\Users\\User\\Desktop\\AbbrResolver\\src\\test\\java\\text1.txt");

        List<Sentence> sentences = textManager.splitText(text);
        assertEquals(9, sentences.size());
    }

    @Test
    public void testSplitText2() throws Exception {

        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();

        TextManager textManager = new TextManager(patternFinder, abbrResolver);

        String text = Utils.readFile("C:\\Users\\User\\Desktop\\AbbrResolver\\src\\test\\java\\text2.txt");

        List<Sentence> sentences = textManager.splitText(text);
        assertEquals(14, sentences.size());
    }

    @Ignore
    @Test
    //прямая речь не обрабытывается
    public void testSplitText3() throws Exception {

        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();

        TextManager textManager = new TextManager(patternFinder, abbrResolver);

        String text = Utils.readFile("C:\\Users\\User\\Desktop\\AbbrResolver\\src\\test\\java\\text3.txt");

        List<Sentence> sentences = textManager.splitText(text);
        assertEquals(14, sentences.size());
    }

    @Ignore
    @Test
    //прямая речь не обрабатывается
    public void testSplitText4() throws Exception {

        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();

        TextManager textManager = new TextManager(patternFinder, abbrResolver);

        String text = Utils.readFile("C:\\Users\\User\\Desktop\\AbbrResolver\\src\\test\\java\\text4.txt");

        List<Sentence> sentences = textManager.splitText(text);
        assertEquals(14, sentences.size());
    }

    @Ignore
    @Test
    public void testSplitText5() throws Exception {

        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();

        TextManager textManager = new TextManager(patternFinder, abbrResolver);

        String text = Utils.readFile("C:\\Users\\User\\Desktop\\AbbrResolver\\src\\test\\java\\text5.txt");

        List<Sentence> sentences = textManager.splitText(text);
        assertEquals(14, sentences.size());
    }

    @Ignore
    @Test
    //прямая речь не обрабатывается
    public void testSplitText6() throws Exception {

        PatternFinder patternFinder = new PatternFinder();
        AbbrResolver abbrResolver = new AbbrResolver();

        TextManager textManager = new TextManager(patternFinder, abbrResolver);

        String text = Utils.readFile("C:\\Users\\User\\Desktop\\AbbrResolver\\src\\test\\java\\text6.txt");

        List<Sentence> sentences = textManager.splitText(text);
        assertEquals(14, sentences.size());
    }
}
