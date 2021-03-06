import beans.Item;

import java.io.*;
import java.nio.charset.Charset;

public class ImportDb extends Importer{

    public void doImport(InputStream is, String filePath) throws Exception {
        DBManager dbManager = DBManager.getInstance();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("windows-1251")))) {
            System.out.println("����� ������ ����� '" + filePath + "'.");
            Item item;
            while ((item = readItem(reader)) != null) {
                dbManager.addItem(item);
            }
            System.out.println("������ ��������.");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found!", e);
        }
    }
}
