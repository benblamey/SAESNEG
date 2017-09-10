package com.benblamey.saesneg.review;

import com.benblamey.core.GATE.GateUtils2;
import gate.Document;
import gate.util.GateException;
import java.io.FileNotFoundException;

public class CheckDocsTheSameMain {

    /**
     * @param args
     * @throws GateException
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws GateException, FileNotFoundException {

        String folder = "C:/work/data/output/PARTICIPANT_1_FULL_NAME/";

        Document gen = GateUtils2.loadGATEDoc(folder + "gate_PARTICIPANT_1_FACEBOOK_ID_Participant1_1385216922_gen_post.xml");
        Document gold = GateUtils2.loadGATEDoc(folder + "gate_PARTICIPANT_1_FACEBOOK_ID_Participant1_1385216922_gold.xml");

        GateUtils2.quickCheckDocContentIdentical(gen, gold);

        GateUtils2.exportAnnotationSetToCSV(folder + "gate_PARTICIPANT_1_FACEBOOK_ID_Participant1_1385216922_gen_post.xml.csv", gen.getAnnotations("SAESNEG_LOCATION"));
        GateUtils2.exportAnnotationSetToCSV(folder + "gate_PARTICIPANT_1_FACEBOOK_ID_Participant1_1385216922_gold.xml.csv", gold.getAnnotations("gold"));

    }

}
