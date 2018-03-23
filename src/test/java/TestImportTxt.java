import org.junit.Before;
import org.junit.Test;

public class TestImportTxt {

    @Before
    public void setUp() throws Exception {
        DBManager.getInstance().clearDB();
        DBManager.getInstance().executeScript("C:\\Users\\User\\Desktop\\AbbrResolver\\src\\main\\java\\resourses\\create-sqlite.sql");
    }

    @Test
    public void simpleTest() throws Exception {
        ImportTxt importTxt = new ImportTxt();
        importTxt.doImport("C:\\Users\\User\\Desktop\\AbbrResolver\\src\\test\\java\\importTest.txt");
    }
}
