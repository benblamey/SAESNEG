package benblamey.saesneg.phaseA.text.gatesubdocument;

import benblamey.core.GATE.GateUtils2;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.corpora.DocumentContentImpl;
import gate.util.GateException;
import java.io.FileNotFoundException;

public class GateSubDocumentWriterTest {

    public static void main(String[] args) throws FileNotFoundException, GateException {
        // Run GATE / Stanford
        Gate.init();

        System.out.println("...GATE initialised");

        FeatureMap params = Factory.newFeatureMap();

        Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", params);

        doc.edit(0L, 0L, new DocumentContentImpl("This is some inserted text."));

        GateSubDocumentWriter subDoc = new GateSubDocumentWriter(doc, "fb", 1234L);
        subDoc.appendLine("foo", "some_field");
        subDoc.appendLine("more1", "some_field");
        subDoc.finalizeText();

        GateSubDocumentWriter subDoc2 = new GateSubDocumentWriter(doc, "fb", 5678L);
        subDoc2.appendLine("wibble", "some_field");
        subDoc2.appendLine("more1", "some_field");
        subDoc2.finalizeText();

        String fileName = "C:/work/data/gate_test.xml";

        GateUtils2.exportGATEtoXML(doc, fileName);
    }
}
