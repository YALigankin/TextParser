import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestImportMemoryDictionary {

    @Test
    public void test() throws Exception {

        MemoryDictionary.getInstance().close();

        Importer importer = new ImportMemory();
        importer.doImport(this.getClass().getResourceAsStream("/importMemory.txt"), "importMemory.txt");

        MemoryDictionary memoryDictionary = MemoryDictionary.getInstance();

        List<String> list1 = memoryDictionary.findAbbrLongForms("т");
        assertEquals(8, list1.size());

        List<String> list2 = memoryDictionary.findAbbrLongForms("м");
        assertEquals(0, list2.size());

        List<String> list3 = memoryDictionary.findAbbrLongForms("тит. ");
        assertEquals(3, list3.size());

        List<String> list4 = memoryDictionary.findAbbrLongForms("тит. с.");
        assertEquals(2, list4.size());
        assertEquals("титульная страница", list4.get(0));
    }
}
