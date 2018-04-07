import beans.Item;
import org.apache.commons.collections4.trie.PatriciaTrie;

import java.util.*;

public class MemoryDictionary implements IDictionary {

    private static MemoryDictionary singleInstance;
    private static PatriciaTrie<List<String>> dictionary;

    private MemoryDictionary() {
        dictionary = new PatriciaTrie<>();
    }

    public synchronized static MemoryDictionary getInstance() {
        if (singleInstance == null) {
            singleInstance = new MemoryDictionary();
        }
        return singleInstance;
    }

    @Override
    public void addItem(Item item) throws Exception {
        dictionary.merge(item.getWord(), Collections.singletonList(item.getDefinition()), (strings, strings2) -> {
            List<String> newValue = new ArrayList<>(strings.size() + 1);
            newValue.addAll(strings);
            newValue.addAll(strings2);
            return newValue;
        });
    }

    @Override
    public List<String> findAbbrLongForms(String abbr) throws Exception {
        Map<String, List<String>> resultMap = dictionary.prefixMap(abbr);
        if (!resultMap.isEmpty()) {
            List<String> resultList = new ArrayList<>();
            for (List<String> strings : resultMap.values()) {
                resultList.addAll(strings);
            }
            return resultList;
        }
        return Collections.emptyList();
    }
}
