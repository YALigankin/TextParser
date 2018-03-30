import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.Assert.*;

public class TestImportTxt {

    @Before
    public void setUp() throws Exception {

        changeDbUrl("jdbc:sqlite:dictionary/abbreviationTest.db");

        DBManager.getInstance().dropAllTables();
        DBManager.getInstance().executeScript(this.getClass().getResourceAsStream("/createDb-sqlite.sql"));
    }

    @After
    public void tearDown() throws Exception {

        DBManager.getInstance().close();

        changeDbUrl("jdbc:sqlite:dictionary/abbreviation.db");

        assertTrue(FileUtils.deleteQuietly(new File(("dictionary/abbreviationTest.db"))));
    }

    @Ignore
    @Test
    public void simpleTest() throws Exception {
        ImportTxt importTxt = new ImportTxt();
        importTxt.doImport(this.getClass().getResourceAsStream("/abbrDic.txt"), "importTest.txt");

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
