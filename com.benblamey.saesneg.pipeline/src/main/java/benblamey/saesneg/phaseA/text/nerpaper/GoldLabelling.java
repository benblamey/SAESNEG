package com.benblamey.saesneg.phaseA.text.nerpaper;

import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.util.InvalidOffsetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class GoldLabelling {

    public static void doGoldLocationLabelling(Document doc) throws InvalidOffsetException {

        String text = doc.getContent().toString().toLowerCase();

        for (String token : GoldLabelling.s_caseInsensitiveSearchTokens.keySet()) {
            token = token.toLowerCase();

            int x = 0;

            while (true) {
                x = text.indexOf(token, x);
                if (x < 0) {
                    break;
                }

                // Label the annotation.
                Long osm_id = GoldLabelling.s_caseInsensitiveSearchTokens.get(token.toLowerCase());

                if (osm_id != null) {
                    FeatureMap features = Factory.newFeatureMap();
                    features.put("osm_id", osm_id);
                    features.put("bootstrapped", true);

                    AnnotationSet gold_locations = doc.getAnnotations("gold").get("location"); // Get all the gold locations.

                    // Don't add any annotations on top of existing annotations.
                    AnnotationSet annotationSet = gold_locations.get((long) x, (long) (x + token.length()));
                    if (annotationSet.size() == 0) {
                        // The gold_locations is immutable.
                        doc.getAnnotations("gold").add((long) x, (long) (x + token.length()), "location", features);
                    }
                }

                x++;
            }
        }

    }

    public static Map<String, Long> s_caseInsensitiveSearchTokens = new HashMap<String, Long>() {
        {
            put("azerbaijan", 364110L);
            put("covent garden", 2290086954L);
            put("cardiff", 738885245L);
            put("woodstock", 175671L);
            put("peak district", 2176657L);
            put("vietnam", 49915L);
            put("wales", 58437L);

            put("newport", 335184L);
            put("newports", 335184L);
            put("swansea", 87944L);

            put("cwmbran", 69287057L);
            put("tenby", 21413029L);
            put("glastonbury", 29673839L);
            put("london", 65606L);
            put("porthcawl", 27199791L);
            put("chepstow", 27199864L);
            put("caldicot", 8997358L);
            put("bristol", 57539L);
            //put("uk ",62149L);
            put("europe", 25871341L);
            put("ibiza", 536255L);
            put("wimbledon", 21329597L);
            put("london", 65606l);
            put("southampton", 127864L);
            put("essex", 180904L);
            put("aber", 21598107L);
            put("tunisia", 424317315L);

            put("canada", 1428125L);
            put("norway", 1059668L);
            put("iceland", 299133L);
            put("paris", 7444L);

            put("majorca", 374056L);
            put("windsor", 275803060L);

            // put("cardiff", 738885245L); // node
            // put("london", 6560665606L); // boundary
            // put("windsor", 275803060L); // boundary
            // put("clapham", 27554267L); // boundary
            // put("leckwith", 1225072442L);
            // put("wales", 58437L);
            // put("cymru", 58437L);
            // put("brixton", 21521237L);
            // put("uk", 62149L);
            // put("rickmansworth", 21139877L);
            // put("watford", 2683176L);
        }
    };

    // 1-grams commonly mistagged as locations.
    public static HashSet<String> s_nonLocations = new HashSet<String>() {
        {
            add("hope");

            add("derby"); // sports
            add("rugby");
            add("lions");

            // building names
            add("kerridge");
            add("piece");

            add("valley");
            add("st");
            add("bring");
            add("hill");
            add("bring");
            add("bank");
            add("old");
            add("sale");
            add("mill");
            add("camp");
            add("hall");
            add("march");
            add("cooling");
            add("parc");
            add("valley");
            add("star");
            add("gang");
            add("will");
            add("shell");

            add("flat");
            add("village"); // Portmeirion nickname apparently.
            add("trinity");
            add("college");
            add("nye");

            add("martin");
            add("hannah");
            add("halliwell"); // Spice Girl and Bolton area.
        }
    };

}
