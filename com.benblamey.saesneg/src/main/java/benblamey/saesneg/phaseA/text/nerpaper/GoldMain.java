package benblamey.saesneg.phaseA.text.nerpaper;

import benblamey.core.GATE.GateUtils2;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.util.GateException;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public class GoldMain {

    private static Map<String, Long> s_tokens = new HashMap<String, Long>() {
        {

            put("cardiff", 738885245L); // node
            put("london", 6560665606L); // boundary
            put("windsor", 275803060L); // boundary
            put("clapham", 27554267L); // boundary
            put("leckwith", 1225072442L);
            put("wales", 58437L);
            put("cymru", 58437L);
            put("brixton", 21521237L);
            put("uk", 62149L);
            put("rickmansworth", 21139877L);
            put("watford", 2683176L);

        }
    };

    public static void main(String[] args) throws GateException, FileNotFoundException {
        String fileName = "C:/work/data/output/PARTICIPANT_1_FULL_NAME/gate_ner_gen.xml";

        Gate.init();

        // LifeStory ls = user.getDefaultLifeStory();
        System.out.println("...GATE initialised");

        FeatureMap params = Factory.newFeatureMap();
        params.put("sourceUrl", fileName);
        // params.put("preserveOriginalContent", new Boolean(true));
        // params.put("collectRepositioningInfo", new Boolean(true));

        Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", params);

        AnnotationSet tokens = doc.getAnnotations("").get("Token");

        for (int i = 0; i < tokens.size(); i++) {
            Annotation annotation = tokens.get(i);
            String token = (String) annotation.getFeatures().get("string");

            Long osm_id = s_tokens.get(token.toLowerCase());

            if (osm_id != null) {
                FeatureMap features = Factory.newFeatureMap();
                features.put("osm_id", osm_id);
                features.put("bootstrapped", true);
                doc.getAnnotations("gold").add(annotation.getStartNode(), annotation.getEndNode(), "location", features);
            }
        }

        //doc.getContent().toString().c
        GateUtils2.exportGATEtoXML(doc, "C:/work/data/output/PARTICIPANT_1_FULL_NAME/gate_gold.xml");
    }

}
