import grammeme.MorfologyParameters;
import jmorfsdk.JMorfSdk;
import jmorfsdk.load.JMorfSdkLoad;
import morphologicalstructures.OmoForm;
import org.junit.Ignore;
import org.junit.Test;
import storagestructures.OmoFormList;

import static junit.framework.TestCase.assertTrue;

public class TestMorfLibrary {

    @Ignore
    @Test
    public void testSimpleSentence() throws Exception {

        JMorfSdk jMorfSdk = JMorfSdkLoad.loadInAnalysisMode();

        String sentence = "тест проверяет программу";
        String[] words = sentence.split(" ");

        OmoFormList omoForms1 = jMorfSdk.getAllCharacteristicsOfForm(words[0]);
        OmoFormList omoForms2 = jMorfSdk.getAllCharacteristicsOfForm(words[1]);
        OmoFormList omoForms3 = jMorfSdk.getAllCharacteristicsOfForm(words[2]);

        hasMorfCharacteristic(omoForms1, MorfologyParameters.TypeOfSpeech.class, MorfologyParameters.TypeOfSpeech.NOUN);

        hasMorfCharacteristic(omoForms2, MorfologyParameters.TypeOfSpeech.class, MorfologyParameters.TypeOfSpeech.VERB);

        hasMorfCharacteristic(omoForms3, MorfologyParameters.TypeOfSpeech.class, MorfologyParameters.TypeOfSpeech.NOUN);

        jMorfSdk.finish();
    }

    private boolean hasMorfCharacteristic(OmoFormList omoForms, Class morfCharacteristic, long expValue) {
        for (OmoForm omoForm : omoForms) {
            if (expValue == omoForm.getTheMorfCharacteristics(morfCharacteristic)) {
                return true;
            }
        }
        return false;
    }
}
