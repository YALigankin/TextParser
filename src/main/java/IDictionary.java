import beans.Item;

import java.util.List;

public interface IDictionary {

    void addItem(Item item) throws Exception;

    List<String> findAbbrLongForms(String abbr) throws Exception;
}
