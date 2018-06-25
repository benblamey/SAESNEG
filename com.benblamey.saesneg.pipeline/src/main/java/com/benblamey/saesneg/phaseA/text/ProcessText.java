package com.benblamey.saesneg.phaseA.text;

import com.benblamey.core.GATE.GateUtils2;
import com.benblamey.nominatim.OpenStreetMapSearch;
import com.benblamey.nominatim.OpenStreetMapSearchAlgorithmOptions;
import com.benblamey.nominatim.OpenStreetMapSearchResult;
import com.benblamey.saesneg.model.LifeStory;
import com.benblamey.saesneg.model.datums.Datum;
import com.benblamey.saesneg.phaseA.text.stanford.StanfordNLPService;
import gate.Annotation;
import gate.Corpus;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.SimpleAnnotationSet;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.SerialAnalyserController;
import gate.util.GateException;
import gate.util.InvalidOffsetException;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

public class ProcessText {

    public static Document processDocument(Document doc, ProcessTextOptions textOptions, OpenStreetMapSearchAlgorithmOptions gisOptions, PrintStream log) throws GateException, FileNotFoundException, MalformedURLException, SQLException {

        if (doc.getContent().size() > 0) {
            // Run ANNIE
            runANNIE(doc);

            // Now run Stanford NLP and annotate the document.
            StanfordNLPService.annotate(doc, log);
        }

        return doc;
    }

    public static void runOSMGazetteer(Document doc, OpenStreetMapSearchAlgorithmOptions osmOptions, ProcessTextOptions textOptions) throws SQLException, InvalidOffsetException {

        if (osmOptions != null) {

            com.benblamey.nominatim.OpenStreetMapSearch mapSearch = new OpenStreetMapSearch(osmOptions);

            List<Annotation> sortedSentences = GateUtils2.getSortedAnnotations(doc.getAnnotations().get("Sentence"));

            int count = 0;
            for (Annotation sentence : sortedSentences) {
                if (count % 20 == 0) {
                    System.out.println("SENTENCE " + count + " of " + sortedSentences.size());
                }

                System.out.println("SENTENCE: " + doc.getContent().getContent(sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset()));

                SimpleAnnotationSet tokensAS = GateUtils2.getTokensForSentence(doc, sentence);
                List<Annotation> tokens = GateUtils2.getSortedAnnotations(tokensAS);

                // For each token, search OSM and the cache the results as annotations.
                for (int i = 0; i < tokens.size(); i++) {
                    // Search for word-1-grams.

                    Annotation token = tokens.get(i);
                    String tokenText = (String) token.getFeatures().get("string");

                    int id = mapSearch.getSearchStringID(tokenText);
                    Collection<OpenStreetMapSearchResult> osmResults = mapSearch.searchForString(id);

                    for (OpenStreetMapSearchResult r : osmResults) {
                        // Add the token.
                        FeatureMap newFeatureMap = GateUtils2.toFeatureMap(r.toMap());
                        doc.getAnnotations(Datum.OPEN_STREET_MAP_BEN_AS).add(token.getStartNode().getOffset(),
                                token.getEndNode().getOffset(),
                                Datum.LOCATION_GATE_ANNOTATION,
                                newFeatureMap);
                    }

//					if (textOptions.RUN_GEONAMES_LOOKUP) {
//						lookupGeonames(token, tokenText);
//					}
//
//					if (textOptions.RUN_LEGACY_OSM_NOMINATIM_SEARCH) {
//						legacyOSMSearch(token, tokenText);
//					}
                }

                count++;
            }

            mapSearch.close();

        }

    }

//	private void lookupGeonames(Annotation a, String token) {
//		GeoNamesPlace location = GeonamesLookup.get(token);
//		if (location != null) {
//
//			// Store the location in the GATE document for debugging purposes.
//			FeatureMap newFeatureMap = Factory.newFeatureMap();
//			newFeatureMap.put("geonameid", location.geonames_id);
//			this._gateSubDocument.annotateToken(a.getStartNode(), a.getEndNode(), "geonames", LOCATION_GATE_ANNOTATION, newFeatureMap);
//
//			// And add it to the list of associated locations for further
//			// processing.
//			//this._locations.add(location);
//		}
//	}
//
//	private void legacyOSMSearch(Annotation a, String token) {
//		List<OpenStreetMapSearchResult> osmResults = LegacyNominatimOpenStreetMapSearch.search(token);
//
//		for (OpenStreetMapSearchResult osmLocation : osmResults) {
//			this._gateSubDocument.annotateToken(a.getStartNode(), a.getEndNode(), "OpenStreetMap", LOCATION_GATE_ANNOTATION, GateUtils2.toFeatureMap(osmLocation.toMap()));
//
//			// And add it to the list of associated locations for further
//			// processing.
//			//this._locations.add(osmLocation);
//
//		}
//	}
    public static Document createGATEdoc(LifeStory ls) throws ResourceInstantiationException, InvalidOffsetException {

        GateUtils2.initGate();

        FeatureMap params = Factory.newFeatureMap();
        params.put("encoding", "UTF-8");
        // params.put("sourceUrl", user.getProfile().getId());
        // params.put("preserveOriginalContent", new Boolean(true));
        // params.put("collectRepositioningInfo", new Boolean(true));

        Document doc = (Document) Factory.createResource("gate.corpora.DocumentImpl", params);

        // doc.edit(0L, 0L, new
        // DocumentContentImpl("This is some inserted text."));
        // First, append all the text to a GATE document.
        for (Datum mfo : ls.datums) {
            mfo.appendTextToGateDocument(doc);
        }
        return doc;
    }

    private static void runANNIE(Document doc) throws GateException, MalformedURLException, ResourceInstantiationException, ExecutionException {
        // We run ANNIE inside a segment processing pipeline, so text for each datum is processed separately.
        SerialAnalyserController annieController = GateUtils2.getSegmentProcessingPipeline();

        // Add the document to a corpus
        Corpus corpus = (Corpus) Factory.createResource("gate.corpora.CorpusImpl");
        corpus.add(doc);

        // tell the pipeline about the corpus and run it
        annieController.setCorpus(corpus);
        annieController.execute();
    }

}
