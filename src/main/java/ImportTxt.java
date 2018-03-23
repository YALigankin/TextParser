import beans.Item;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ImportTxt {

    private static final String DELIMITER = ";";

    public void doImport(String filePath) throws Exception {
        DBManager dbManager = DBManager.getInstance();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
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

    private Item readItem(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line != null && !line.isEmpty()) {
            String[] pieces = line.split(DELIMITER);
            if (pieces.length > 0) {
                Item Item = new Item();
                Item.setWord(pieces[0]);
                Item.setDefinition(pieces.length > 1 ? pieces[1] : null);
                Item.setDescription(pieces.length > 2 ? pieces[2] : null);
                Item.setType(pieces.length > 3 ? Utils.parseInt(pieces[3]) : Integer.valueOf(99));
                return Item;
            }
        }
        return null;
    }
}
