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

        List<String> list1 = memoryDictionary.findAbbrLongForms("�");
        assertEquals(8, list1.size());

        List<String> list2 = memoryDictionary.findAbbrLongForms("�");
        assertEquals(0, list2.size());

        List<String> list3 = memoryDictionary.findAbbrLongForms("���. ");
        assertEquals(3, list3.size());

        List<String> list4 = memoryDictionary.findAbbrLongForms("���. �.");
        assertEquals(2, list4.size());
        assertEquals("��������� ��������", list4.get(0));
    }
}
