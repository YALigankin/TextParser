import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TestImportTxt {

    @Before
    public void setUp() throws Exception {

        File testDbFile = new File("dictionary/abbreviationTest.db");
        assertTrue(testDbFile.createNewFile());

        changeDbUrl("jdbc:sqlite:dictionary/abbreviationTest.db");
        DBManager.getInstance().close();

        DBManager.getInstance().dropAllTables();
        DBManager.getInstance().executeScript(this.getClass().getResourceAsStream("/createDb-sqlite.sql"));
    }

    @After
    public void tearDown() throws Exception {

        changeDbUrl("jdbc:sqlite:dictionary/abbreviation.db");
        DBManager.getInstance().close();

        assertTrue(FileUtils.deleteQuietly(new File(("dictionary/abbreviationTest.db"))));
    }

    @Test
    public void simpleTest() throws Exception {
        ImportTxt importTxt = new ImportTxt();
        importTxt.doImport(this.getClass().getResourceAsStream("/importTest.txt"), "importTest.txt");

        try (Statement stmt = DBManager.getConnection().createStatement()){
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM shortForm")) {
                while (rs.next()) {
                    assertEquals(5, rs.getInt(1));
                }
            }
        }

        try (Statement stmt = DBManager.getConnection().createStatement()){
            try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM longForm")) {
                while (rs.next()) {
                    assertEquals(5, rs.getInt(1));
                }
            }
        }

    }

    private void changeDbUrl(String value) throws Exception {
        Properties dbconfig = new Properties();
        URL configUrl = this.getClass().getResource("/dbconfig.properties");
        try (InputStream is = configUrl.openStream()) {
            dbconfig.load(is);
        }
        dbconfig.setProperty("db_url", value);
        try (OutputStream out = new FileOutputStream(configUrl.getPath())) {
            dbconfig.store(out, "");
        }
    }
}
