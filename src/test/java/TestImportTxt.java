import org.junit.Before;
import org.junit.Test;

public class TestImportTxt {

    @Before
    public void setUp() throws Exception {
        DBManager.getInstance().dropAllTables();
        DBManager.getInstance().executeScript(this.getClass().getResourceAsStream("/createDb-sqlite.sql"));
    }

    @Test
    public void simpleTest() throws Exception {
        ImportTxt importTxt = new ImportTxt();
        importTxt.doImport(this.getClass().getResourceAsStream("/importTest.txt"), "importTest.txt");
    }
}
