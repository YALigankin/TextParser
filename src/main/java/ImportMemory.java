import beans.Item;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

public class ImportMemory extends Importer {

    @Override
    public void doImport(InputStream is, String filePath) throws Exception {

        MemoryDictionary memoryDictionary = MemoryDictionary.getInstance();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("windows-1251")))) {
            System.out.println("Начат импорт файла '" + filePath + "'.");
            Item item;
            while ((item = readItem(reader)) != null) {
                memoryDictionary.addItem(item);
            }
            System.out.println("Импорт завершен.");
        } catch (FileNotFoundException e) {
            throw new RuntimeException("File not found!", e);
        }
    }
}
