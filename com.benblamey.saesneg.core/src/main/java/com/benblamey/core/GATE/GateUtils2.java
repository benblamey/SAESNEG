package com.benblamey.core.GATE;

import com.benblamey.core.StringUtil;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.DocumentContent;
import gate.Factory;
import gate.FeatureMap;
import gate.Gate;
import gate.ProcessingResource;
import gate.SimpleAnnotationSet;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;
import gate.util.InvalidOffsetException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;

/**
 * Various utility methods for <a href="https://gate.ac.uk/">GATE</a>.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class GateUtils2 {

    private static boolean s_gateInitialized;
    private static SerialAnalyserController _annieController;
    private static SerialAnalyserController _segmentProcessingPR;

    static {
        initGate();
    }

    public static void initGate() {
        if (!s_gateInitialized) {
            s_gateInitialized = true;
            try {

                Properties props = System.getProperties();
                props.setProperty("gate.home", "C:\\work\\code\\3rd_Ben\\gate-8.0-build4825-ALL\\");
                try {
                    Gate.init();
                } catch (GateException ex) {
                    throw new RuntimeException(ex);
                }

                Gate.init();

                // Load ANNIE plugin
                File gateHome = Gate.getGateHome();
                File pluginsHome = new File(gateHome, "plugins");
                Gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "ANNIE").toURL());
                Gate.getCreoleRegister().registerDirectories(new File(pluginsHome, "Alignment").toURL()); // Load alignment plugin (for segment processing PR)

                System.out.println("...GATE initialised");

            } catch (GateException e) {
                throw new RuntimeException(e);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

        }
    }

    public static void setGATELogFile(String filename) {
        FileAppender fa = new FileAppender();
        fa.setName("FileLogger");
        fa.setFile(filename);
        fa.setLayout(new PatternLayout("%d %-5p [%c{1}] %m%n"));
        fa.setThreshold(Level.INFO);
        fa.setAppend(true);
        fa.activateOptions();

        // add appender to any Logger (here is root)
        Logger.getRootLogger().addAppender(fa);
    }

    /**
     * Convert a Map to a FeatureMap
     *
     * @param map
     * @return
     */
    public static FeatureMap toFeatureMap(Map<?, ?> map) {
        FeatureMap newFeatureMap = Factory.newFeatureMap();
        for (Entry<?, ?> entry : map.entrySet()) {
            newFeatureMap.put(entry.getKey(), entry.getValue());
        }
        return newFeatureMap;
    }

    /**
     * Get annotations, sorted according to their position within the document.
     *
     * @param tokensAS
     * @return
     */
    public static List<Annotation> getSortedAnnotations(SimpleAnnotationSet tokensAS) {
        List<Annotation> tokens = Arrays.asList(tokensAS.toArray(new Annotation[0]));
        Collections.sort(tokens, new Comparator<Annotation>() {
            @Override
            public int compare(Annotation o1, Annotation o2) {
                return Long.compare(o1.getStartNode().getOffset(), o2.getStartNode().getOffset());
            }
        });
        return tokens;
    }

    /**
     * Performs a quick check to see if the content of two GATE documents is
     * identical. Checks some characters through the document. Does not look at
     * annotations at all.
     */
    public static void quickCheckDocContentIdentical(Document a, Document b) throws InvalidOffsetException {
        DocumentContent keyContent = a.getContent();
        DocumentContent responseContent = b.getContent();

        if (!keyContent.size().equals(responseContent.size())) {
            System.out.println("key doc has length " + keyContent.size() + " chars.");
            System.out.println("resp doc has length " + keyContent.size() + " chars.");

            throw new RuntimeException("Documents are different lengths");
        }

        // Check various characters in the text.
        long interval = keyContent.size() / 200L;
        for (long i = 0; i < keyContent.size() - 2; i += interval) {
            String keyContentLetter = keyContent.getContent(i, i + 1).toString();
            String respContentLetter = keyContent.getContent(i, i + 1).toString();

            if (!keyContentLetter.equals(respContentLetter)) {
                throw new RuntimeException("character at position " + i + " don't match between documents.");
            }
        }

        // Check the final character.
        {
            String keyContentLetter = keyContent.getContent(keyContent.size() - 1, keyContent.size()).toString();
            String respContentLetter = keyContent.getContent(keyContent.size() - 1, keyContent.size()).toString();

            if (!keyContentLetter.equals(respContentLetter)) {
                throw new RuntimeException("character at position " + (keyContent.size() - 1) + " don't match between documents.");
            }

        }
    }

    public static void exportGATEtoXML(Document doc, String fileName) throws FileNotFoundException {
        PrintWriter out;
        System.out.println("Exported GATE XML to: " + fileName);
        out = new PrintWriter(fileName);
        out.print(doc.toXml());
        out.close();
    }

    /**
     * Writes out a annotation table with the same columns as the table in the
     * GATE GUI: type, set, start, end, id, features.
     *
     * @param filename
     * @param annotations
     * @throws FileNotFoundException
     * @throws InvalidOffsetException
     */
    public static void exportAnnotationSetToCSV(String filename, AnnotationSet annotations) throws FileNotFoundException, InvalidOffsetException {

        PrintWriter out = new PrintWriter(filename);
        ArrayList<Annotation> annotationsList = toSortedAnnotationSet(annotations);

        for (Annotation annotation : annotationsList) {

            out.println(StringUtil.ToCommaList(
                    new String[]{annotation.getType(),
                        // annotations.getName(),
                        annotation.getStartNode().getOffset().toString(), annotation.getEndNode().getOffset().toString(),
                        // JSON.serialize(annotation.getFeatures())
                        annotations.getDocument().getContent().getContent(annotation.getStartNode().getOffset(), annotation.getEndNode().getOffset()).toString()}
            ));
        }

        out.close();

    }

    public static void main(String args[]) throws GateException, MalformedURLException {
        getANNIEController();
    }

    private static ArrayList<Annotation> toSortedAnnotationSet(AnnotationSet annotations) {
        ArrayList<Annotation> annotationsList = new ArrayList<Annotation>(annotations);

        Collections.sort(annotationsList, new Comparator<Annotation>() {

            @Override
            public int compare(Annotation o1, Annotation o2) {
                return o1.getStartNode().getOffset().compareTo(o2.getStartNode().getOffset());
            }

        });
        return annotationsList;
    }

    public static Document loadGATEDoc(String filename) throws GateException {

        String uri = "file:/" + filename.replace("\\", "/");
        System.out.println("Loading URI: " + uri);

        FeatureMap params = Factory.newFeatureMap();
        params.put("sourceUrl", uri);
        // params.put("preserveOriginalContent", new Boolean(true));
        // params.put("collectRepositioningInfo", new Boolean(true));

        Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", params);

        System.out.println("Loaded document with size: " + doc.getContent().size());

        return doc;
    }

    public static SerialAnalyserController getANNIEController() throws GateException, MalformedURLException, ResourceInstantiationException {

        if (_annieController == null) {

              // need Tools plugin for the Morphological analyser
//  Gate.getCreoleRegister().registerDirectories(
//    new File(Gate.getPluginsHome(), "ANNIE").toURL();
//  );
//
            // need Tools plugin for the Morphological analyser
            Gate.getCreoleRegister().registerDirectories(new File("C:\\work\\code\\3rd_Ben\\gate-8.0-build4825-ALL\\plugins\\ANNIE").toURI().toURL());

//            // load the ANNIE application from the saved state in plugins/ANNIE
//            File pluginsHome = Gate.getPluginsHome();
//            File anniePlugin = new File(pluginsHome, "ANNIE");
//            File annieGapp = new File(anniePlugin, "ANNIE_with_defaults.gapp");
//            try {
//                PersistenceManager.loadObjectFromFile(annieGapp);
////                System.out.println("ANNIE has the following PRs:");
////
////                ProcessingResource docResetPR = null;
////                for (ProcessingResource pr: _annieController.getPRs()) {
////                    String prName = pr.getName();
////                    System.out.println("\t" + prName);
////                    if (prName.equals("Document Reset PR")) {
////                        docResetPR = pr;
////                    }
////                }
////                _annieController.getPRs().remove(docResetPR);
//            } catch (PersistenceException ex) {
//                throw new RuntimeException(ex);
//            } catch (IOException ex) {
//                throw new RuntimeException(ex);
//            }
            // create a serial analyser controller to run ANNIE with
            _annieController = (SerialAnalyserController) Factory.createResource("gate.creole.SerialAnalyserController", Factory.newFeatureMap(), Factory.newFeatureMap(), "ANNIE_" + Gate.genSym());

            // load each PR as defined in ANNIEConstants
            for (String prName : Arrays.asList(
                    //Document Reset PR
                    "gate.creole.tokeniser.DefaultTokeniser",
                    "gate.creole.splitter.SentenceSplitter"
            //	"ANNIE Sentence Splitter",
            //	"ANNIE POS Tagger"
            //"ANNIE NE Transducer"
            //"ANNIE OrthoMatcher"
            )) {
                FeatureMap params2 = Factory.newFeatureMap(); // use default parameters
                ProcessingResource pr = (ProcessingResource) Factory.createResource(prName, params2);

                // add the PR to the pipeline controller
                _annieController.add(pr);
            } // for each ANNIE PR

        }

        return _annieController;
    }

    /**
     * Gets a segment processing pipeline which processes each datum separately
     * through the ANNIE pipeline.
     *
     * @return
     * @throws MalformedURLException
     * @throws GateException
     */
    public static SerialAnalyserController getSegmentProcessingPipeline() throws MalformedURLException, GateException {

        if (_segmentProcessingPR == null) {

            // create a serial analyser controller to run ANNIE with
            _segmentProcessingPR = (SerialAnalyserController) Factory.createResource("gate.creole.SerialAnalyserController", Factory.newFeatureMap(), Factory.newFeatureMap(), "SEGMENT_" + Gate.genSym());

            FeatureMap params2 = Factory.newFeatureMap(); // use default parameters

            // Name is not the class name, it is the name inside the creole.xml file.
            ProcessingResource pr = (ProcessingResource) Factory.createResource("gate.composite.impl.SegmentProcessingPR", params2);
            pr.setParameterValue("analyser", getANNIEController());
            pr.setParameterValue("inputASName", "Key");
            pr.setParameterValue("segmentAnnotationType", "datum");

            _segmentProcessingPR.add(pr);
        }

        return _segmentProcessingPR;

    }

    public static AnnotationSet getTokensForSentence(Document doc, Annotation sentence) {
        AnnotationSet defaultAS = doc.getAnnotations("");
        return defaultAS.getContained(
                sentence.getStartNode().getOffset(),
                sentence.getEndNode().getOffset())
                .get("Token");
    }

}
