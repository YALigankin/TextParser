import beans.Descriptor;
import beans.DescriptorType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AbbrResolver {

    public void fillAbbrDescriptions(List<Descriptor> descriptors) throws Exception {
        DBManager dbManager = DBManager.getInstance();
        Iterator<Descriptor> iter = descriptors.iterator();
        List<String> notFounded = new ArrayList<>();
        while (iter.hasNext()) {
            Descriptor curDescriptor = iter.next();
            if (curDescriptor.getType().equals(DescriptorType.SHORT_WORD)) {
                List<String> longForms = dbManager.findAbbrLongForms(curDescriptor.getValue());    //TODO затратный поиск (нужно запрашивать пачкой)
                if (!longForms.isEmpty()) {
                    curDescriptor.setDesc(longForms.get(0));           //TODO пока берется первое попавшееся значение аббревиатуры
                } else {
                    notFounded.add(curDescriptor.getValue());
                    iter.remove();    //не смогли найти - Regex неправильно определил - удаляем из списка
                }
            }
        }
        System.out.println("NotFouded:");
        for (String s : notFounded) {
            System.out.println(s);
        }
    }
}
