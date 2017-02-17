package benblamey.saesneg.phaseA.text;

import benblamey.core.GATE.GateUtils2;
import benblamey.saesneg.phaseA.text.nerpaper.GoldLabelling;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.util.GateException;
import java.io.File;
import java.io.FileNotFoundException;

public class GateToolsMain {

    public static void main(String[] args) throws GateException, FileNotFoundException {

        Gate.init();

        // ALWAYS work on "gen"!!!
        String file = "C:/work/data/output/Matthew Gibson/"
                + "gate_671948696_MatthewGibson_1382369147_gen.xml";

        if (!file.contains("_gen.xml")) {
            throw new RuntimeException("are you sure you want to change the gold?");
        }

        String uri = "file:/" + file.replace("\\", "/");
        System.out.println("Loading URI: " + uri);

        FeatureMap params = Factory.newFeatureMap();
        params.put("sourceUrl", uri);
        // params.put("preserveOriginalContent", new Boolean(true));
        // params.put("collectRepositioningInfo", new Boolean(true));

        Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", params);

        System.out.println("Loaded document with size: " + doc.getContent().size());

        String text = doc.getContent().toString().toLowerCase();

        int addedCount = 0;
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

                    AnnotationSet gold_locations = doc.getAnnotations("gold").get("location"); // Get
                    // all
                    // the
                    // gold
                    // locations.

                    // Don't add any annotations on top of existing annotations.
                    AnnotationSet annotationSet = gold_locations.get((long) x, (long) (x + token.length()));
                    if (annotationSet.size() == 0) {
                        // The gold_locations is immutable.
                        doc.getAnnotations("gold").add((long) x, (long) (x + token.length()), "location", features);
                        addedCount++;

                        Long start = (long) x - 20L;
                        Long end = (long) x + 20L;
                        if (start < 0) {
                            start = 0L;
                        }
                        if (end > doc.getContent().size()) {
                            end = doc.getContent().size() - 1;
                        }

                        String sample = doc.getContent().getContent(start, end).toString();
                        sample = sample.replace("\n", "<nl>");

                        System.out.println(token + " : " + sample);
                    }
                }

                x++;
            }
        }

        GateUtils2.exportGATEtoXML(doc, file);

        System.out.println("Added " + addedCount + " annotations.");
        System.out.println("Saved File.");
    }

    // if (doc.getContent().size() > 0) {
    // Run ANNIE
    // Load ANNIE plugin
    File gateHome = Gate.getGateHome();
	// File pluginsHome = new File(gateHome, "plugins");
    // Gate.getCreoleRegister().registerDirectories(new File(pluginsHome,
    // "ANNIE").toURL());

	// create a serial analyser controller to run ANNIE with
    // SerialAnalyserController annieController = (SerialAnalyserController)
    // Factory.createResource("gate.creole.SerialAnalyserController",
    // Factory.newFeatureMap(), Factory.newFeatureMap(), "ANNIE_"
    // + Gate.genSym());
    // load each PR as defined in ANNIEConstants
    // for (int i = 0; i < ANNIEConstants.PR_NAMES.length; i++) {
    // FeatureMap params2 = Factory.newFeatureMap(); // use default parameters
    // ProcessingResource pr = (ProcessingResource)
    // Factory.createResource(ANNIEConstants.PR_NAMES[i], params2);
    //
    // // add the PR to the pipeline controller
    // annieController.add(pr);
    // } // for each ANNIE PR
    // Add the document to a corpus
    // Corpus corpus = (Corpus)
    // Factory.createResource("gate.corpora.CorpusImpl");
    // corpus.add(doc);
    // tell the pipeline about the corpus and run it
    // annieController.setCorpus(corpus);
    // annieController.execute();
}
