package benblamey.saesneg.phaseA.text.nerpaper;

import benblamey.core.GATE.GateUtils2;
import benblamey.nominatim.OpenStreetMapBasicOrdering;
import benblamey.nominatim.OpenStreetMapElementKind;
import benblamey.nominatim.OpenStreetMapSearch;
import benblamey.nominatim.OpenStreetMapSearchAlgorithmOptions;
import benblamey.nominatim.OpenStreetMapSearchResult;
import benblamey.saesneg.ExperimentUserContext;
import benblamey.saesneg.Users;
import benblamey.saesneg.model.UserContext;
import benblamey.saesneg.model.datums.Datum;
import benblamey.saesneg.phaseA.text.GATEFileKind;
import benblamey.saesneg.phaseA.text.stanford.StanfordNLPService;
import benblamey.saesneg.serialization.LifeStoryInfo;
import com.benblamey.core.classifier.naivebayes.Classifier;
import com.mongodb.DBObject;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.util.GateException;
import gate.util.InvalidOffsetException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import socialworld.model.SocialWorldUser;

public class OSMExperimentMain {

    private static final String OSM_ID = "osm_id";
    private static final String ENTITY_NORMALIZED = "entity_normalized";
    public static final String ARE_EQUAL = "ARE_EQUAL";
    private static final String CATEGORY_TRUE = "true";
    private static final String CATEGORY_FALSE = "false";

    public static void main(String[] args) throws GateException, IOException, SQLException, GoldNotInOSMException {
        OSMExperimentMain main = new OSMExperimentMain();
        main.run();
    }

    public OpenStreetMapSearch _search;
    private ArrayList<UserContext> _users = new ArrayList<UserContext>();
    private ArrayList<Document> _goldDocs = new ArrayList<Document>();
    private BufferedWriter writer;
    private Classifier _classifier;
    private ArrayList<Document> _genDocs = new ArrayList<Document>();
    private OpenStreetMapSearch _openStreetMapSearch;
    private SamePlaceCalculator calc = new SamePlaceCalculator();

    public OSMExperimentMain() throws SQLException, IOException {
        OpenStreetMapSearchAlgorithmOptions options = new OpenStreetMapSearchAlgorithmOptions();
        _search = new OpenStreetMapSearch(options);

        File csvFile = new File("C:/work/data/geo_features.csv");
        writer = new BufferedWriter(new FileWriter(csvFile));

        _openStreetMapSearch = new OpenStreetMapSearch(new OpenStreetMapSearchAlgorithmOptions());
    }

    private void run() throws GateException, IOException, SQLException, GoldNotInOSMException {

//		ArrayList<File> load2 = load2();
//		for (File goldFile : load2) {
//			fetchDetails(goldFile);
//		}
        _classifier = new Classifier(0.001);

        load();

        runStanfordNER();

        //train();
        writer.close();
//		
//		test();

    }

    private void runStanfordNER() throws InvalidOffsetException, FileNotFoundException, SQLException, GoldNotInOSMException {
        UserContext userContext = this._users.get(0);
        Document document = this._genDocs.get(0);
        Document gold = this._goldDocs.get(0);
        StanfordNLPService.annotate(document, System.out);
        String debugOutputFilename = userContext.getOutputDirectoryWithTrailingSlash() + userContext.getFileSystemSafeName() + "_stanford_gate.xml";
        GateUtils2.exportGATEtoXML(document, debugOutputFilename);

        List<Annotation> sortedAnnotations = GateUtils2.getSortedAnnotations(document.getAnnotations("stanford").get("named_entity"));

        // Filter to leave only the 'LOCATION' annotations.
        List<Annotation> locationNERannotations = new ArrayList<>();
        for (Annotation annotation : sortedAnnotations) {
            String entityCode = (String) annotation.getFeatures().get(ENTITY_NORMALIZED);
            if (entityCode == null || !entityCode.equals("LOCATION")) {
                continue;
            }
            locationNERannotations.add(annotation);
        }

        for (int i = 0; i < locationNERannotations.size(); i++) {

            Annotation locationAnnotation = locationNERannotations.get(i);
            Annotation nextLocationAnnotation = null;

            if (i + 1 < locationNERannotations.size()) {
                nextLocationAnnotation = locationNERannotations.get(i + 1);

                if (locationAnnotation.getEndNode().getOffset() + 1 == nextLocationAnnotation.getStartNode().getOffset()
                        && document.getContent().getContent(locationAnnotation.getEndNode().getOffset(), locationAnnotation.getEndNode().getOffset() + 1).equals(" ")) {

                    Long start = locationAnnotation.getStartNode().getOffset();
                    Long end = nextLocationAnnotation.getEndNode().getOffset();

                    String location2gram = document.getContent().getContent(start, end).toString();
                    System.out.println("Found 2-location-gram: " + location2gram);

                    Boolean foundMatch = searchForStringAndAnnotateWithTopResult(document, gold, start, end);
                    if (foundMatch) {
                        i++;
                        continue;
                    }
                }
            }

            Long start = locationAnnotation.getStartNode().getOffset();
            Long end = locationAnnotation.getEndNode().getOffset();
            searchForStringAndAnnotateWithTopResult(document, gold, start, end);

        }

        GateUtils2.exportGATEtoXML(document, debugOutputFilename);

        //Anno
    }

    /**
     * @return True if an annotation was added, otherwise false.
     */
    private Boolean searchForStringAndAnnotateWithTopResult(Document document, Document gold, Long start, Long end) throws InvalidOffsetException, SQLException, GoldNotInOSMException {

        if (start > end) {
            throw new RuntimeException("invalid offsets");
        }

        String queryString = document.getContent().getContent(start, end).toString();

        int searchStringID = this._openStreetMapSearch.getSearchStringID(queryString);
        List<OpenStreetMapSearchResult> searchForString = this._openStreetMapSearch.searchForString(searchStringID);

        Collections.sort(searchForString, OpenStreetMapBasicOrdering.Instance);

        if (searchForString.size() > 0) {

            OpenStreetMapSearchResult openStreetMapSearchResult = searchForString.get(0);
            Integer newAnnotationID = document.getAnnotations("baseline").add(start, end, "location",
                    GateUtils2.toFeatureMap(openStreetMapSearchResult.toMap()));
            Annotation baselineResultAnnotation = document.getAnnotations("baseline").get(newAnnotationID);
            //this.isLocationCorrect(annotation, openStreetMapSearchResult, gold);
            return true;
        }
        return false;
    }

    private void load() throws GateException, IOException {

        for (DBObject user : Users.getDefaultUsers()) {

            UserContext userContext = UserContext.FromSocialWorldUser(user, null);

            SocialWorldUser swu = new SocialWorldUser(user);
            LifeStoryInfo latestGoodLifeStory = LifeStoryInfo.getLatestGoodLifeStory(swu);
            if (latestGoodLifeStory == null) {
                continue;
            }
            userContext.lifeStoryInfo = latestGoodLifeStory;
            System.out.println("Processing " + userContext.getName());

            // LifeStory lifeStory = LifeStoryXMLSerializer.DeserializeLifeStory(latestGoodLifeStory.filename);
            // userContext.setDefaultLifeStory(lifeStory);
            String gateGoldfilename = ExperimentUserContext.getGATEfilename(userContext, GATEFileKind.Gold);

            File f = new File(gateGoldfilename);
            if (!f.exists()) {
                System.out.println("Skipping " + userContext.getName() + " - no gold GATE doc.");
                continue;
            }

            Document goldDoc = GateUtils2.loadGATEDoc(gateGoldfilename);

            _users.add(userContext);
            _goldDocs.add(goldDoc);
            Document genGATEdoc = GateUtils2.loadGATEDoc(ExperimentUserContext.getGATEfilename(userContext, GATEFileKind.Generated));
            _genDocs.add(genGATEdoc);

            GateUtils2.quickCheckDocContentIdentical(goldDoc, genGATEdoc);

        }

        System.out.println("Loaded " + _goldDocs.size() + " GOLD documents.");
    }

    private void train() throws SQLException, InvalidOffsetException, IOException {

        for (int i = 0; i < _users.size(); i++) {
            UserContext userContext = _users.get(i);
            Document goldDoc = _goldDocs.get(i);
            Document genDoc = this._genDocs.get(i);

            System.out.println("Training on " + userContext.getName());

            AnnotationSet locationAnnotations = genDoc.getAnnotations(Datum.OPEN_STREET_MAP_BEN_AS);

            int candProcessedCount = 0;
            for (Annotation locationAnnotation : locationAnnotations.get(Datum.LOCATION_GATE_ANNOTATION)) {
                if (candProcessedCount % 100 == 0) {
                    System.out.println("Processed " + candProcessedCount + " training cases.");
                }

                candProcessedCount++;

                OpenStreetMapSearchResult benLocationOSM = new OpenStreetMapSearchResult(locationAnnotation.getFeatures());

//				OpenStreetMapSearchResult leftData = _search.search_for_osm_id(benLocationOSM.osm_id, benLocationOSM.osm_type);
//				if (leftData == null) {
//					System.out.println("Cand. annotation not found in OSM " + benLocationOSM.toString() + " - skipping.");
//					continue;
//				}
                boolean isTrue;
                try {
                    isTrue = isLocationCorrect(locationAnnotation, benLocationOSM, goldDoc);
                } catch (GoldNotInOSMException e) {
                    System.out.println("Gold annotation not found in OSM " + e.OSMResult.toString() + " - skipping.");
                    continue;
                }

                //ArrayList<String> features = computeFeatures(isTrue, locationAnnotation, benLocationOSM);
                //_classifier.AddTrainingCase(isTrue ? CATEGORY_TRUE : CATEGORY_FALSE, features);
            }

        }

        this._classifier.printTrainingCaseSummaryToSTDOUT();
    }

    private boolean isLocationCorrect(Annotation ben_location, OpenStreetMapSearchResult benLocationOSM, Document gold) throws SQLException, GoldNotInOSMException {
        Long goldID = getTouchingGoldAnnotation(ben_location, gold);
        if (goldID == null) {
            return false;
        }

        return calc.areOSMplacesTheSame(benLocationOSM, goldID);
    }

    private Long getTouchingGoldAnnotation(Annotation candidate, Document gold) {
        AnnotationSet annotationSet = gold.getAnnotations(Datum.GOLD_AS).get(Datum.LOCATION_GATE_ANNOTATION).get(candidate.getStartNode().getOffset(), candidate.getEndNode().getOffset());
        if (annotationSet.size() == 0) {
            return null;
        } else if (annotationSet.size() == 1) {
            Annotation overlappingAnnotation = annotationSet.iterator().next();
            Long osm_id = getOSMIDFromAnnotation(overlappingAnnotation);
            return osm_id;
        } else {
            throw new RuntimeException("more than one annotation is touching?!");
        }
    }

    public static Long getOSMIDFromAnnotation(Annotation overlappingAnnotation) {
        Object osmID = overlappingAnnotation.getFeatures().get(OSM_ID);
        Long osm_id = osmID instanceof String ? Long.parseLong((String) osmID) : (Long) osmID;
        return osm_id;
    }

    public static String kindToURLString(OpenStreetMapElementKind osm_type) {
        String candidateKindString;
        switch (osm_type) {
            case Node:
                candidateKindString = "node";
                break;
            case Relation:
                candidateKindString = "relation";
                break;
            case Way:
                candidateKindString = "way";
                break;
            default:
                throw new RuntimeException("enum not recognized");
        }
        return candidateKindString;
    }

//	Document goldDoc = ProcessText.loadGATEDoc(goldFile.getAbsolutePath());
//	
//	AnnotationSet goldAnnotations = goldDoc.getAnnotations("gold").get("location");
//	
//	for (Annotation goldAnnotation : goldAnnotations) {
//		Object osm_idObj = goldAnnotation.getFeatures().get("osm_id");
//		Long osm_id;
//		try {
//			osm_id = osm_idObj instanceof Long ? (Long)osm_idObj : Long.parseLong((String)osm_idObj);
//		} catch (NumberFormatException e) {
//			System.out.println("EXCEPTION " + goldAnnotation.getStartNode().toString());
//			throw e;
//		}
//		
//		if (osm_id <= 0) {
//			System.out.println("WARNING - Missing Annotation - " + goldDoc.getContent().getContent(
//					goldAnnotation.getStartNode().getOffset(),
//					goldAnnotation.getEndNode().getOffset()));
//		}
//		
//		OpenStreetMapSearchResult search_for_osm_id = this._search.search_for_osm_id(osm_id, OpenStreetMapElementKind.DontKnow);
//		
//		if (search_for_osm_id == null) {
//			
//			System.out.println("Can't find gold annotation " + osm_id + " looking for parents.");
//			
//			List<Map<String, Object>> findParentRelations = this._search.findParentRelations(osm_id, OpenStreetMapElementKind.DontKnow);
//			
//			System.out.println("\tFound " + findParentRelations.size() + " parents.");
//			for (Map<String, Object> parent : findParentRelations) {
//				System.out.println("\t"+parent.get("RELATIONSHIP") + " id: " + parent.get("id"));
//			}
//		}
//	}
//
//}
//private ArrayList<File> load2() {
//	
//	Collection<File> allXMLFiles = FileUtils.listFiles(new File("C:/work/data/output"), new String[] {"xml"}, true); // Recursive.
//	ArrayList<File> goldXMLfiles = new ArrayList<>();
//	
////	File dir = ;
////	File[] listFiles = dir.listFiles(new FileFilter() {
////		
////		@Override
////		public boolean accept(File pathname) {
////			String filename = pathname.getName();
////			return 
////		}
////	});
//	
//	for (File filename : allXMLFiles) {
//		if (filename.getName().endsWith("gold.xml")) {
//			goldXMLfiles.add(filename);
//			//System.out.println(filename);
//		}
//	}
//	
//	return goldXMLfiles;
//}
//	private ArrayList<String> computeFeatures(Document doc, boolean isTrue, Annotation locationAnnotation, OpenStreetMapSearchResult osm) throws InvalidOffsetException, IOException {
//		
//		
//		
//		ArrayList<String> results = new ArrayList<>();
//
//		results.add("RANK_SEARCH_"+ osm.rank_search);
//		
//		//AnnotationSet annotationSet = gold.getAnnotations().get("Token");
//		
//		//annotationSet = annotationSet.get(locationAnnotation.getStartNode().getOffset(), locationAnnotation.getEndNode().getOffset());
//
//		
//		int numTokens = 0;
//		Iterator<Annotation> iterator = annotationSet.iterator();
//		String debugText = "";
//		while (iterator.hasNext()) {
//			Annotation next = iterator.next();
//			if (next.getStartNode().getOffset() >= locationAnnotation.getEndNode().getOffset()
//					|| next.getEndNode().getOffset() <= locationAnnotation.getStartNode().getOffset()) {
//				continue;
//			}
//			numTokens++;
//			debugText += "-" + next.getFeatures().get("string");
//		}
//		results.add("NUM_TOKENS_"+ numTokens);
//		
//		//System.out.println("candidate text: "  + debugText); 
//		//+ gold.getContent().getContent(ben_location.getStartNode().getOffset(), ben_location.getEndNode().getOffset()));
//		
//		
//		// For the time being, just look at the first token.
//		Annotation next = annotationSet.iterator().next();
//		FeatureMap features = next.getFeatures();
//
//		results.add("GATE_FIRST_TOKEN_CATEGORY_"+ features.get("category"));
//		results.add("GATE_FIRST_TOKEN_KIND_"+ features.get("kind"));
//		
//		results.add("GATE_FIRST_TOKEN_LENGTH_"+ features.get("length"));
//		
//		results.add("GATE_FIRST_TOKEN_ORTH_"+ features.get("orth"));
//		
//		this.writer.append(isTrue + ","  + (Integer)osm.rank_search + "," + features.get("category") 
//				+ "," + features.get("kind") + "," + features.get("length") + "," + features.get("orth")+ ","+ numTokens);
//		this.writer.newLine();
//		
//		
//		return results;
//	}
}
