package com.benblamey.nominatim.tests;

import com.benblamey.core.GATE.GateUtils2;
import com.benblamey.saesneg.phaseA.text.gatesubdocument.GateSubDocumentReader;
import gate.AnnotationSet;
import gate.Document;
import gate.util.GateException;

public class Participant1BonfireNightFunTest {

    /**
     * @param args
     * @throws GateException
     */
    public static void main(String[] args) throws GateException {

        Document loadGATEDoc = GateUtils2.loadGATEDoc("C:/work/data/output/Participant 1/gate_PARTICIPANT_1_FACEBOOK_ID_Participant1_1385216922_gold.xml");

        GateSubDocumentReader subdoc = new GateSubDocumentReader(loadGATEDoc, "fb", 315891781886276L);

        AnnotationSet sentences = subdoc.getSentences();

        "".toCharArray();

    }

}
