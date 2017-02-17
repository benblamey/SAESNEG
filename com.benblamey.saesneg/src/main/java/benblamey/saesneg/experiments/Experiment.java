package benblamey.saesneg.experiments;

import benblamey.core.DateUtil;
import benblamey.core.GATE.GateUtils2;
import benblamey.saesneg.ExperimentUserContext;
import benblamey.saesneg.PipelineContext;
import benblamey.saesneg.Users;
import benblamey.saesneg.model.Event;
import benblamey.saesneg.model.LifeStory;
import benblamey.saesneg.model.UserContext;
import benblamey.saesneg.model.annotations.Annotation;
import benblamey.saesneg.model.annotations.LocationAnnotation;
import benblamey.saesneg.model.datums.Datum;
import benblamey.saesneg.phaseA.text.stanford.StanfordNLPService;
import benblamey.saesneg.phaseB.ClusteringStrategies;
import benblamey.saesneg.phaseB.DatumPairSimilarity;
import benblamey.saesneg.phaseB.DatumSimilarityEvidence;
import benblamey.saesneg.phaseB.FestibusFeatures;
import benblamey.saesneg.phaseB.SVMEdgeClassifier;
import benblamey.saesneg.phaseB.strategies.SpatialStrategy;
import benblamey.saesneg.review.PairwiseClusteringEvaluation;
import benblamey.saesneg.serialization.LifeStoryInfo;
import benblamey.saesneg.serialization.LifeStoryJsonSerializer;
import benblamey.saesneg.serialization.LifeStoryXMLSerializer;
import com.benblamey.core.FileUtil;
import com.benblamey.core.ProcessUtilities;
import com.benblamey.core.classifier.svm.LibSVM;
import com.benblamey.core.classifier.svm.SvmFile;
import com.benblamey.core.pajek.Edge;
import com.benblamey.core.pajek.Network;
import com.benblamey.core.pajek.Vertex;
import com.mongodb.DBObject;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang.NotImplementedException;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import socialworld.model.SocialWorldUser;

/**
 * A single experiment, to run the pipeline repeatedly on a set of users, with a
 * fixed set of options.
 *
 * @author Ben Blamey blamey.ben@gmail.com
 */
public class Experiment {

    public final IExperimentContainer ParentSet;
    public final PairwiseClusteringEvaluation clusteringEvaluation = new PairwiseClusteringEvaluation(this);

    public String Name = "unnamed";
    public ExperimentOptions Options;

    public final ExperimentResults Results = new ExperimentResults();
    public PrintStream LogFile;

    public Experiment(IExperimentContainer parentSet) {
        ParentSet = parentSet;
    }

    public String getallUsersLibSVMCasesPath() {
        return PipelineContext.getCurrentContext().getDataOutputDir() + "\\all_users_svm.dat";
    }

    public String getOutputDirectory() {
        String dirName = ParentSet.getOutputDirectoryWithTrailingSlash() + Name + "\\";
        new File(dirName).mkdirs();
        return dirName;
    }

    public List<ExperimentUserContext> run() throws Exception {

        DateTime start = DateTime.now();


        String outputFile = getOutputDirectory() + "log.txt";
        System.out.println("Writing clustering eval to: " + outputFile);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(outputFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        LogFile = new PrintStream(fos, true);

        List<ExperimentUserContext> eucs = runCore();

        DateTime finish = DateTime.now();

        Duration duration = new Duration(start, finish);

        System.out.println("Experiment took: " + duration.toString());

        LogFile.close();

        return eucs;
    }

    private List<ExperimentUserContext> runCore() throws NumberFormatException, RuntimeException, IOException, InterruptedException, Exception {
        final List<ExperimentUserContext> eucs = new ArrayList<>();
        System.out.println("Step 1 -- Initialize the User Contexts.");
        step1_init(eucs);

        System.out.println("====== There are " + eucs.size() + " users. =====");
        if (eucs.isEmpty()) {
            return null;
        }

        // Load the gold data.
        step1_initGold(eucs);

        {
            final String datumTypes = this.getOutputDirectory() + "DatumTypesForPython.csv";
            // Print out basic information about the gold datums for analysis in Python.
            PrintWriter out = new PrintWriter(datumTypes);
            for (ExperimentUserContext euc : eucs) {
                for (Datum d : euc.userContext.getLifeStory().goldDatums) {
                    out.println(d.getNetworkID() + "," + d.getClass().getName());
                }
            }
            out.close();
            System.out.println("Printed info to:" + datumTypes);
        }

        System.out.println("Step 2 -- Run Phase A on all users.");
        if (Options.runPhaseA) {
            StanfordNLPService.getPipeline();
            GateUtils2.setGATELogFile(getOutputDirectory() + "gate_log.txt");

            for (ExperimentUserContext euc : eucs) {
                System.out.println("Phase A Processing: " + euc.userContext.getName());
                euc.runPhaseA();
            }

            exportPhaseAAnnotations(eucs);
        } else {
            System.out.println("(skipped phase A).");
            return eucs;
        }

        // Assert that any location annotations are supported.
        for (ExperimentUserContext euc : eucs) {
            for (Datum d : euc.userContext.getLifeStory().datums) {
                for (Annotation a : d.getAnnotations().getAllAnnotations()) {
                    if (a == null) {
                        throw new RuntimeException("null annotation.");
                    }
                    
                    if (a instanceof LocationAnnotation) {
                        LocationAnnotation la = (LocationAnnotation)a;
                        if (la.Level < 1 || la.Level > SpatialStrategy.osmAdminLevelToMinDist.size()) {
                            System.err.println(la.toString());
                            throw new RuntimeException("location out of range.");    
                        }
                    }
                }
            }
        }

        if (this.Options.PhaseBOptions != null) {
            System.out.println("Step 3 -- Run Phase B (Festibus Strategies on Pairs)");
            step3_runFestibus(eucs);
            exportFestibusPairFeatures(eucs);
        
            //if (eucs.size() > 1) {
                System.out.println("Step 4 -- Perform Clustering (including training & testing) based on Festibusk output features.");
                step4_cluster(eucs);
            //} else {
              //  System.out.println("Skipping cluatering -- only one user");
            //}
            
        }

        return eucs;
    }

    private void step1_init(List<ExperimentUserContext> experimentUserContexts) throws NumberFormatException {
        for (DBObject userObj : Users.getUsers(Options.UserQuery)) {
            UserContext uc = UserContext.FromSocialWorldUser(userObj, null);
            uc.socialWorldUser = new SocialWorldUser(userObj);
            System.out.println("checking user: " + uc.socialWorldUser.getPrettyName());

            selectLifeStory(uc, Options.lifeStorySelectStrategy);

            if (uc.getLifeStory() == null) {
                System.out.println("Skipping user, no life story found.");
                continue;
            }

            System.out.println("Selected: " + uc.lifeStoryInfo.filename);

            if (uc.getLifeStory().datums.size() == 0) {
                System.out.println("Skipping user, no datums in life story.");
                continue;
            }

            // Read ground truth.
            uc.getLifeStory().EventsGolden = LifeStoryJsonSerializer.getGroundTruthEvents(uc.getLifeStory(), uc.socialWorldUser);
            for (Event e : uc.getLifeStory().EventsGolden) {
                for (Datum d : e.getDatums()) {
                    if (!uc.getLifeStory().datums.containsObjectWithNetworkID(d.getNetworkID())) {
                        System.err.println("missing datum!" + d.getNetworkID());
                    }
                }
            }

            // Remove empty events.
            for (int i = uc.getLifeStory().EventsGolden.size() - 1; i >= 0; i--) {
                Event e = uc.getLifeStory().EventsGolden.get(i);
                //   System.out.println("#datums in event = " + e.getDatums().size());
                if (e.getDatums().isEmpty()) {
                    uc.getLifeStory().EventsGolden.remove(i);
                }
            }

            if (uc.getLifeStory().EventsGolden.isEmpty()) {
                System.out.println("Skipping user " + uc.getFileSystemSafeName() + " -- no events left after removing empty ones.");
                continue;
            }

            experimentUserContexts.add(new ExperimentUserContext(uc, this));
        }
    }

    public static void selectLifeStory(UserContext uc, LifeStorySelectionStrategy strategy) {
        if (uc.ID.equals("PARTICIPANT_1_FACEBOOK_ID")) // Participant 1's data is a funny case, because she was involved with testing. for her, manually choose the lifestory to load.
        {
            String latestLifeStory = "PARTICIPANT_1_FACEBOOK_ID_Participant1_1383841998.xml"; // Correct life story for Participant 1.
            // Create a fake lifestory info.
            uc.lifeStoryInfo = LifeStoryInfo.guessFromFilename(latestLifeStory);
        } else {
            switch (strategy) {
                case LatestOnDisk: {
                    File dir = new File(LifeStoryXMLSerializer.getXMLDirectoryWithTrailingSlash());
                    TreeSet<String> s = new TreeSet<>();
                    for (File f : dir.listFiles()) {
                        String[] split = (f.getName()).split("_");
                        if (split.length != 3) {
                            continue;
                        }
                        String fbID = split[0];
                        if (fbID.equals(uc.socialWorldUser.getValue(SocialWorldUser.FACEBOOK_USER_ID))) {
                            s.add(f.getName());
                        }
                    }
                    if (s.isEmpty()) {
                        System.out.println("Skipping user, no life stories on disk.");
                        return;
                    }
                    String latestLifeStory = s.last();

                    // Create a fake lifestory info.
                    uc.lifeStoryInfo = LifeStoryInfo.guessFromFilename(latestLifeStory);
                }
                break;
                case UseGroundTruthMatching: {
                    String matchingLifeStory = LifeStoryJsonSerializer.getLifeStoryFileNameMatchingGroundTruth(uc.socialWorldUser);
                    if (matchingLifeStory != null) {
                        for (LifeStoryInfo info : LifeStoryInfo.getLifeStoryInfos(uc.socialWorldUser)) {
                            if (info.filename == null) {
                                continue;
                            }
                            if (info.filename.equals(matchingLifeStory)) {
                                uc.lifeStoryInfo = info;
                                break;
                            }
                        }
                    }

                    if (uc.lifeStoryInfo == null) {
                        System.out.println("LS for ground truth is absent - falling back to a best-guess");
                    // Ground truth created before data was recorded.
                        // the users facebook would be updated when they logged in to create the ground truth.
                        Integer lastLoginTimeUnix = (Integer) uc.socialWorldUser.getValue(SocialWorldUser.FACEBOOK_OAUTH_TOKEN_UPDATED_UNIX);
                        DateTime lastLoginTime = DateUtil.DateTimeFromUnixTime(lastLoginTimeUnix);
                        DateTime cutoff = lastLoginTime.plusMinutes(100); // They would not have waited more than one hour for a life story before creating ground truth.

                        List<LifeStoryInfo> infosBeforeCutoff = new ArrayList<>();
                        for (LifeStoryInfo info : LifeStoryInfo.getLifeStoryInfos(uc.socialWorldUser)) {
                            if (info.created.isBefore(cutoff)) {
                                infosBeforeCutoff.add(info);
                            }
                        }
                        infosBeforeCutoff.sort((LifeStoryInfo o1, LifeStoryInfo o2) -> o1.created.compareTo(o2.created)); // sorts in place.
                        uc.lifeStoryInfo = infosBeforeCutoff.get(infosBeforeCutoff.size() - 1);
                    }
                }
                break;
            }
        }

        LifeStory lifeStory = LifeStoryXMLSerializer.DeserializeLifeStory(uc.lifeStoryInfo.filename, uc);
        uc.setDefaultLifeStory(lifeStory);
    }

    private void step3_runFestibus(List<ExperimentUserContext> experimentUserContexts) {
        for (ExperimentUserContext euc : experimentUserContexts) {

            LifeStory lifeStory = euc.userContext.getLifeStory();

            List<DatumPairSimilarity> goldenPairs = euc.userContext.getLifeStory().goldenPairs;

            // Now run FESTIBUS strategies on those pairs, to produce the evidence/features.
            ClusteringStrategies strategies = new ClusteringStrategies(lifeStory, euc.userContext, this.Options.PhaseBOptions);
            strategies.runFestibus(goldenPairs);

            for (DatumPairSimilarity pair : goldenPairs) {
                // Check that the features are unique, and that the SVM values are in range.
                HashSet<FestibusFeatures> featuresCheck = new HashSet<>();
                for (DatumSimilarityEvidence e : pair.getEvidence()) {
                    boolean wasAdded = featuresCheck.add(e.getFeatureID());
                    if (!wasAdded) {
                        throw new RuntimeException("Duplicate feature: " + e.getFeatureID());
                    }
                    if (e.getSVMFeatureValue() < -1.0 || e.getSVMFeatureValue() > +1.0) {
                        throw new RuntimeException("Pair evidence out of range " + e.getSVMFeatureValue() + " " + e.toString());
                    }
                }
            }
        }
    }

    private void step4_cluster(List<ExperimentUserContext> experimentUserContexts) throws RuntimeException, InterruptedException, IOException {
        switch (Options.PhaseBOptions.LibSVMGoldAction) {
            case CrossRun_Individual:

                if (false) { // 
                    int numFolds = 5;

                    for (int fold = 0; fold < numFolds; fold++) {
                        System.out.println("Running fold" + fold);

                        List<ExperimentUserContext> trainingCases = new ArrayList<>();
                        List<ExperimentUserContext> testCases = new ArrayList<>();
                        for (int i = 0; i < experimentUserContexts.size(); i++) {
                            if (i % numFolds == fold) {
                                // The user is a *test* for this fold.
                                testCases.add(experimentUserContexts.get(i));
                            } else {
                                // The user is a *train* for this fold.
                                trainingCases.add(experimentUserContexts.get(i));
                            }
                        }
                        final String label = "fold_" + fold;

                        step4_cluster_pairwiseSVM(label, trainingCases, testCases);

                        // FOR THE TIME BEING, QUIT AFTER ONE FOLD!
                        //break;

                    }
                }
                
                
                {
                    // Try folding accross users -- so that training cases are striped.
                    
                    List<DatumPairSimilarity> pairsForAllUsers = new ArrayList<>();                        
                    for (ExperimentUserContext euc : experimentUserContexts) {
                        pairsForAllUsers.addAll(euc.userContext.getLifeStory().goldenPairs);
                    }
                    
                    int numFolds = 10;

                    for (int fold = 0; fold < numFolds; fold++) {

                        System.out.println("Running fold" + fold);

                        List<DatumPairSimilarity> trainingCases = new ArrayList<>();
                        List<DatumPairSimilarity> testCases = new ArrayList<>();
                        
                        for (int i = 0; i < pairsForAllUsers.size(); i++) {
                            if (i % numFolds == fold) {
                                // The user is a *test* for this fold.
                                testCases.add(pairsForAllUsers.get(i));
                            } else {
                                // The user is a *train* for this fold.
                                trainingCases.add(pairsForAllUsers.get(i));
                            }
                        }
                        final String label = "fold_" + fold;
                        step4_cluster_pairwiseSVM_byPair(label, trainingCases, testCases);
                    }
                }
                
                
                if (false)
                {
                    // Cheating -- jusst to see what happens!
                    List<ExperimentUserContext> trainingCases = new ArrayList<>();
                    List<ExperimentUserContext> testCases = new ArrayList<>();
                    trainingCases.addAll(experimentUserContexts);
                    testCases.addAll(experimentUserContexts);
                    step4_cluster_pairwiseSVM("cheating_", trainingCases, testCases);
                }
                
                

                // Pairs have now been assigned result probabilities, we can export the results.
                for (ExperimentUserContext euc : experimentUserContexts) {

                    step4_outputToPajek(euc);

                    Map<String, DatumPairSimilarity> pairsByDatums = new HashMap<>();
                    for (DatumPairSimilarity pair : euc.userContext.getLifeStory().goldenPairs) {
                        pairsByDatums.put(DatumPairSimilarity.getCanonicalLabel(pair), pair);
                    }

                    // Export for coefficient clustering with external utility.
                    step4_cluster_doClustering(euc, pairsByDatums);

                    euc.exportGenClusters();

                    // Run basic stats on clustering output (e.g. pairwise accuracy, number of event clusters, etc.)
                    clusteringEvaluation.evaluatePairwise(euc.userContext);

                    // Evaluate
                    euc.evaluationOnEvents();
                }

                break;
            default:
                throw new NotImplementedException();
        }
    }

    private void step4_cluster_doClustering(ExperimentUserContext euc, Map<String, DatumPairSimilarity> pairsByDatums) throws RuntimeException, IOException {
        String matrixFile = euc.userContext.getOutputDirectoryWithTrailingSlash() + "clustering.matrix";
        FileWriter fw = new FileWriter(matrixFile);
        BufferedWriter correlationMatrixBW = new BufferedWriter(fw);
        List<Datum> datums = euc.userContext.getLifeStory().goldDatums;
        correlationMatrixBW.append(datums.size() + "\n"); // Header is the no of vertices.
        for (int i = 0; i < datums.size(); i++) { // row
            Datum left = datums.get(i);
            for (int j = i + 1; j < datums.size(); j++) { // column
                Datum right = datums.get(j);

                if (left == right) {
                    throw new RuntimeException("left and right are supposed to be different");
                }

                // The datums arn't necessarily sorted, yet each pair is sorted, we might need to swap left and right and generate the right key
                // this method handles the sorting.
                String key = DatumPairSimilarity.getCanonicalLabel(left, right);

                DatumPairSimilarity pair = pairsByDatums.get(key);
                if (pair == null) {
                    throw new RuntimeException("pair not found");
                }

                // Note that weights and relative frequencies are **SWAPPED** -- edges with relativily LESS frequency recieve relatively MORE weight, 
                // so that classes are not penalized in clustering. Hence, expected sum of edge weights for perfect clustering = 0.
		// See Ch4 for table of frequencies.
                Double edgeWeight = 
                    pair.classificationResult.Probabilities.get(DatumPairSimilarity.SVM_CLASS_SAME_EVENT)       * 35298  // The relative frequency of INTER edges.
                   -pair.classificationResult.Probabilities.get(DatumPairSimilarity.SVM_CLASS_DIFFERENT_EVENT)  *  4239; // The relative frequency of INTER edges.
                
                if (pair.classificationResult.Probabilities.keySet().size() != 2 ) {
                    throw new RuntimeException("confused! unexpected pair class: " + pair.classificationResult);
                }
                correlationMatrixBW.append(edgeWeight + "\n");
            }
        }

        correlationMatrixBW.close();

        String melsnerOutputFile = euc.userContext.getOutputDirectoryWithTrailingSlash() + "clustering.output";

        //The clustering utility needs to be run from inside Cygwin, we do this by invoking bash.exe
        // Need to run a command something like this:
        // C:\cygwin64\bin\bash.exe -c
        //  "/cygdrive/c/work/code/3rd_Ben/correlation-distr/bin64/chainedSolvers.exe log vote boem /cygdrive/c/work/data/MelsnerTest.m >/cygdrive/c/work/data/melsner2.out"
        String[] melsnerCmd = {
            "c:/work/code/3rd_Ben/correlation-distr/bin64/chainedSolvers.exe", "log", "vote", "boem", matrixFile, //" >" + melsnerOutputFile
        };

        String bashOutput = ProcessUtilities.runAndReturnOutput(melsnerCmd);
        FileUtil.writeFile(bashOutput, melsnerOutputFile);
        System.out.println("Written output to: " + melsnerOutputFile);

        // Now read the output and convert to a computed clustering.
        List<Event> events = euc.userContext.getLifeStory().EventsComputed;
        if (!events.isEmpty()) {
            throw new RuntimeException("we already have events?!");
        }
        //BufferedReader reader = new BufferedReader(bashOutput);
        //String line = null;
        int datumIndex = 0;
        for (String line : bashOutput.split("[\r\n]+")) {

            if (line.equals("k-clustered as 0 clusters")) {
                // Put each datum in a different cluster.
                for (Datum d : euc.userContext.getLifeStory().goldDatums) {
                    Event e = new Event();
                    e.getDatums().add(d);
                    euc.userContext.getLifeStory().EventsComputed.add(e);
                }
                // Then we're done.
                break;
            }

            try {
                int i = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                // Skip this line.
                continue;
            }

            //while ((line = reader.readLine()) != null) {
            int eventIndex = Integer.parseInt(line); // 1-based.
            if (eventIndex < 1) {
                throw new RuntimeException("supposed to be one based.");
            }
            if (eventIndex > events.size()) {// eventIndex 1-based.
                events.add(new Event());
            }
            events.get(eventIndex - 1) // 1-based.
                    .getDatums().add(datums.get(datumIndex));
            datumIndex++;
        }
        System.out.println("Found " + events.size() + " events.");
//                            if (datumIndex == 0) {
//                                throw new RuntimeException("Empty output from Melnser clustering.");
//                            }
//                            if (datumIndex != datums.size()) {
//                                throw new RuntimeException("did we add all the datums?!");
//                            }
    }

    private void step4_outputToPajek(ExperimentUserContext euc) throws RuntimeException, IOException {
        // For the mean time, export them to PAJEK.
        String netFile = euc.userContext.getOutputDirectoryWithTrailingSlash() + "datums_svm_edges.net";

        // Export a network for each test user.
        Network net = new Network();

        // Vertices first.
        Map<Datum, Vertex> datumsToVertices = new HashMap<>();
        for (Datum d : euc.userContext.getLifeStory().goldDatums) {
            Vertex v = new Vertex(d.getNetworkString(), d);
            datumsToVertices.put(d, v);
            net.getVertices().add(v);
        }
        // Then edges.
        for (DatumPairSimilarity pair : euc.userContext.getLifeStory().goldenPairs) {
            Vertex leftVertex = datumsToVertices.get(pair.getLeft());
            Vertex rightVertex = datumsToVertices.get(pair.getRight());

            double pajekEdgeWeight;

            if (pair.classificationResult.mostLikelyClass == DatumPairSimilarity.SVM_CLASS_SAME_EVENT) {
                pajekEdgeWeight = 0.5 + (pair.classificationResult.probOfMostLikelyClass / 2);
            } else if (pair.classificationResult.mostLikelyClass == DatumPairSimilarity.SVM_CLASS_DIFFERENT_EVENT) {
                pajekEdgeWeight = (pair.classificationResult.probOfMostLikelyClass / 10);
            } else {
                throw new RuntimeException("confused! unexpected pair class: " + pair.classificationResult);
            }

            Edge e = new Edge(leftVertex, rightVertex, pajekEdgeWeight);
            net.getEdges().add(e);
        }

        {
            // Export to PAJEK.
            FileWriter fw = new FileWriter(netFile);
            BufferedWriter bw = new BufferedWriter(fw);
            net.WriteToNetFile(bw);
            bw.close();
            System.out.println("Exported net file to: " + netFile);
        }

        {
            // Additionally, export the gold event clusters as an addtional partition.
            // We need to build map of datums to their respective events.
            Map<Datum, Integer> eventIDsForDatums = new HashMap<>();
            int eventID = 1; // According to PAJEK example, colours start at 1.
            for (Event e : euc.userContext.getLifeStory().EventsGolden) {
                for (Datum d : e.getDatums()) {
                    eventIDsForDatums.put(d, eventID);
                }
                eventID++;
            }

            String cluFile = euc.userContext.getOutputDirectoryWithTrailingSlash() + "datums_gold_events.clu";
            // Write partition file.
            FileWriter fw = new FileWriter(cluFile);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.append("*Vertices " + net.getVertices().size() + "\n");
            for (Vertex v : net.getVertices()) {
                // We just print the colours of the vertices on each line. Index of vertices is implicit from line ordering.
                bw.append(eventIDsForDatums.get((Datum) v.getData()).toString() + "\n");
            }
            bw.close();
            System.out.println("Exported gold clusters to: " + cluFile);
        }
    }

    
    private void step4_cluster_pairwiseSVM_byPair(String label, List<DatumPairSimilarity> trainingCases, List<DatumPairSimilarity> testCases) throws IOException {
        String trainingFileName = this.getOutputDirectory() + label + ".training";

        // Run the training for this fold.
        SvmFile trainingFile = new SvmFile(trainingFileName, FestibusFeatures.values().length);
        
        for (DatumPairSimilarity pair : trainingCases) {
            pair.addCaseToSvmFile(trainingFile,
                    pair.goldIsInSameEvent
                            ? DatumPairSimilarity.SVM_CLASS_SAME_EVENT
                            : DatumPairSimilarity.SVM_CLASS_DIFFERENT_EVENT);
        }
        
        trainingFile.close();
        String modelFileName = this.getOutputDirectory() + label + ".model";
        String trainCmd[] = LibSVM.generateSVMTrainCmd(trainingFileName, modelFileName);
        String trainingOutput = ProcessUtilities.runAndReturnOutput(trainCmd);
        String trainingLogFileName = this.getOutputDirectory() + label + ".training.log";
        FileUtil.writeFile(trainingOutput, trainingLogFileName);

        // Now execute the class to run the "test", i.e. classify edges from the other users.
        SVMEdgeClassifier svmClassifier = new SVMEdgeClassifier(modelFileName);

        String testFileName = this.getOutputDirectory() + label + ".test";
        svmClassifier.computePairSimilarity(testCases, testFileName); // Run the SVM.

        for (DatumPairSimilarity pair : testCases) {
            if (pair.classificationResult == null || pair.classificationResult.mostLikelyClass == null
                    || pair.classificationResult.probOfMostLikelyClass == null) {
                throw new RuntimeException("classification result is missing");
            }
        }
    }
    
    private void step4_cluster_pairwiseSVM(String label, List<ExperimentUserContext> trainingCases, List<ExperimentUserContext> testCases) throws RuntimeException, IOException {
        String trainingFileName = this.getOutputDirectory() + label + ".training";

        // Run the training for this fold.
        SvmFile trainingFile = new SvmFile(trainingFileName, FestibusFeatures.values().length);
        for (ExperimentUserContext euc : trainingCases) {
            euc.addPairsToSvmFile(trainingFile);
        }
        trainingFile.close();
        String modelFileName = this.getOutputDirectory() + label + ".model";
        String trainCmd[] = LibSVM.generateSVMTrainCmd(trainingFileName, modelFileName);
        String trainingOutput = ProcessUtilities.runAndReturnOutput(trainCmd);
        String trainingLogFileName = this.getOutputDirectory() + label + ".training.log";
        FileUtil.writeFile(trainingOutput, trainingLogFileName);

        // Now execute the class to run the "test", i.e. classify edges from the other users.
        SVMEdgeClassifier svmClassifier = new SVMEdgeClassifier(modelFileName);
        List<DatumPairSimilarity> allTestPairs = new ArrayList<>();
        for (ExperimentUserContext euc : testCases) {
            for (DatumPairSimilarity pair : euc.userContext.getLifeStory().goldenPairs) {
                allTestPairs.add(pair);
            }
        }
        String testFileName = this.getOutputDirectory() + label + ".test";
        svmClassifier.computePairSimilarity(allTestPairs, testFileName); // Run the SVM.

        for (ExperimentUserContext euc : testCases) {
            for (DatumPairSimilarity pair : euc.userContext.getLifeStory().goldenPairs) {
                if (pair.classificationResult == null || pair.classificationResult.mostLikelyClass == null
                        || pair.classificationResult.probOfMostLikelyClass == null) {
                    throw new RuntimeException("classification result is missing");
                }
            }
        }
    }

    //        for (ExperimentUserContext euc : experimentUserContexts) {
//			LifeStory ls = euc.userContext.getLifeStory();
//			
//	
//			// Grab datums used by ground truth.
//			List<Datum> gtruthDatums = new ArrayList<Datum>();
//			for (Event e : euc.userContext.getLifeStory().EventsGolden) {
//				gtruthDatums.addAll(e.getDatums());
//			}
//			
//			ls.DatumSimilarityCalculator = new ClusteringStrategies(ls, Options.PhaseBOptions);
//	
//			if (Options.PhaseBOptions.LibSVMGoldAction != null) {
//				//euc.goldClusteringSVM(ls, gtruthDatums);
//			}
//	
//			if (Options.PhaseBOptions.EdgeClassifier != null) {
//		
//			} else {
//				LogFile.append("skipping clustering - no edge classifier set.");
//			}
//		}
//        
//        if (Options.PhaseBOptions != null) {
//	        // Any SVM-related actions:
//        	LogFile.println("To cross-validate:");
//        	LogFile.println(LibSVM.generateSVMCrossValidateCmd(getallUsersLibSVMCasesPath()));
//        	LogFile.println("To train model:");
//        	
//        	LogFile.println(LibSVM.generateSVMTrainCmd(getallUsersLibSVMCasesPath(), ParentSet.getSvmModelPath() ));
//	        
//	        if (Options.PhaseBOptions.LibSVMGoldAction == LibSVMGoldActions.ExportAllUserCases_AndTrain) {
//	        	String output = ProcessUtilities.runAndReturnOutput(LibSVM.generateSVMTrainCmd(getallUsersLibSVMCasesPath(), ParentSet.getSvmModelPath()));
//	        	LogFile.println(output);
//	        }
//        }
    private void openLifeStory(UserContext uc) {

        LifeStoryInfo chosenLifeStoryInfo = null;

        /* Two strategies for life story selection:
         - Use the latest life story always - recommended for most things - maximizes Ground truth data which "exists" in the life stories (38 missing vs. 104)
         - Use the life story which matches the gold gate doc -this is the only strategy suitable for gold labelling text eval. */
        if (Options.lifeStorySelectStrategy == LifeStorySelectionStrategy.MatchGoldGATEDoc) {
            // We want to select the life story that matches the gold GATE doc if we can. 
            DateTime goldGateDocLifeStoryCreated = Experiment.getGoldGateDocLifeStoryCreated(uc);
            if (goldGateDocLifeStoryCreated != null) {

                LifeStoryInfo goldInfo = LifeStoryInfo.getLifeStoryWithCreatedTimestamp(goldGateDocLifeStoryCreated, uc.socialWorldUser);
                if (goldInfo != null) {
                    chosenLifeStoryInfo = goldInfo;
                    LogFile.println("Using life story matching gold info");
                } else {
                    throw new IllegalArgumentException("LS for gold gate doc does not exist?!");
                }
            } else {
                LogFile.println("no gold gate doc found for LS selection.");
            }
        }

        if (chosenLifeStoryInfo == null) {
            chosenLifeStoryInfo = LifeStoryInfo.getLatestGoodLifeStory(uc.socialWorldUser);
            LogFile.println("Using latest life story");
        }

        if (chosenLifeStoryInfo != null) {
            LifeStory lifeStory = LifeStoryXMLSerializer.DeserializeLifeStory(chosenLifeStoryInfo.filename, uc);
            lifeStory.setCreated(chosenLifeStoryInfo.created);
            uc.setDefaultLifeStory(lifeStory);
        } else {
            System.out.println("Skipping user - no 'good' life story.");
        }

        uc.lifeStoryInfo = chosenLifeStoryInfo;
    }

    /**
     * Works out which life story to load for the gold gate doc.
     *
     * @retusrn
     */
    public static DateTime getGoldGateDocLifeStoryCreated(UserContext user) {

        File userOutputDir = new File(user.getOutputDirectoryWithTrailingSlash());
        // gate_PARTICIPANT_1_FACEBOOK_ID_Participant1_1385216922_gen.xml
        Pattern gateFileNamePattern = Pattern.compile("gate_([0-9]+_[^_]+)_([0-9]+)_([a-z]+)\\.xml");

        for (File f : userOutputDir.listFiles()) {
            // File name only (no path)
            String fileName = f.getName();

            Matcher matcher = gateFileNamePattern.matcher(fileName);
            if (matcher.matches()) {

                String prettyName = matcher.group(1);
                if (!prettyName.equals(user.getFileSystemSafeName())) {
                    throw new IllegalArgumentException("gate file in wrong dir?!" + fileName);
                }

                int lifeStoryCreatedTimestamp = Integer.parseInt(matcher.group(2));
                String kind = matcher.group(3);

                if (kind.equals("gold")) {
                    return DateUtil.DateTimeFromUnixTime(lifeStoryCreatedTimestamp);
                }
            }
        }
        return null;

    }

    public void close() throws IOException {
        LogFile.close();
    }

    private String toCygwinPath(String windowsFilePath) {
        windowsFilePath = windowsFilePath.replace("C:", "/cygdrive/c");
        windowsFilePath = windowsFilePath.replace("\\", "/");
        return windowsFilePath;
    }

    private String escpaeQuotes(String string) {
        return string.replace("\"", "\\\"");
    }

    private void exportPhaseAAnnotations(List<ExperimentUserContext> experimentUserContexts) {
        String outputFile = this.getOutputDirectory() + "PhaseA_Annos.csv";
        BufferedWriter writer = FileUtil.getWriter(outputFile);
        // Match fields in adjacent function.
        FileUtil.appendCSVLine(writer, new Object[]{"userID", "datumID", "kind", "dataKind", "dataType", "originalText", "Note"});
        for (ExperimentUserContext euc : experimentUserContexts) {
            // We only analyze the gold datums.
            for (Datum d : euc.userContext.getLifeStory().goldDatums) {
                exportAnnosToCSV(d.getAnnotations().DateTimesAnnotations, "DateTime", euc, writer, d);
                exportAnnosToCSV(d.getAnnotations().ImageContentAnnotations, "ImageContent", euc, writer, d);
                exportAnnosToCSV(d.getAnnotations().Locations, "Location", euc, writer, d);
                exportAnnosToCSV(d.getAnnotations().People, "People", euc, writer, d);
                exportAnnosToCSV(d.getAnnotations().SocialEventAnnotation, "SocialEvent", euc, writer, d);
                exportAnnosToCSV(d.getAnnotations().UserStructureAnnotations, "UserStructure", euc, writer, d);
                //exportAnnosToCSV(d.getAnnotations(), "UserStructure", euc, writer, d);
            }
        }
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Exported Phase A annos to:" + outputFile);
    }

    private void exportAnnosToCSV(List<? extends Annotation> annotations, String kind, ExperimentUserContext euc, BufferedWriter writer, Datum d) {
        if (kind == null) {
            throw new RuntimeException("kind cannot be null.");
        }

        for (Annotation a : annotations) {
            // Match fields in adjacent function.
            if (a.SourceDataKind == null) {
                throw new RuntimeException("SourceDataKind cannot be null.");
            }

            FileUtil.appendCSVLine(writer, new Object[]{euc.userContext.ID, d.getNetworkID(), kind, a.SourceDataKind, d.getClass().getName(), a.getOriginalText(), a.Note});
        }
    }

    private void exportFestibusPairFeatures(List<ExperimentUserContext> eucs) {
        BufferedWriter writer = FileUtil.getWriter(this.getOutputDirectory() + "PhaseB_Pair_Annos.csv");
        // Match fields in adjacent function.
        FileUtil.appendCSVLine(writer, new Object[]{"userID", "isIntra", "leftDatumID", "rightDatumID", "leftDataType", "rightDataType", "featureID", "svmValue", "message"});
        for (ExperimentUserContext euc : eucs) {
            // We only analyze the gold pairs.
            for (DatumPairSimilarity pair : euc.userContext.getLifeStory().goldenPairs) {
                for (DatumSimilarityEvidence ev : pair.getEvidence()) {
                    FileUtil.appendCSVLine(writer, new Object[]{
                        euc.userContext.ID,
                        pair.goldIsInSameEvent,
                        pair.getLeft().getNetworkID(),
                        pair.getRight().getNetworkID(),
                        pair.getLeft().getClass().getName(),
                        pair.getRight().getClass().getName(),
                        ev.getFeatureID(),
                        ev.getSVMFeatureValue(),
                        ev.getMessage()
                    }
                    );
                }
            }
        }

        try {
            writer.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void step1_initGold(List<ExperimentUserContext> eucs) {
        for (ExperimentUserContext euc : eucs) {

            LifeStory lifeStory = euc.userContext.getLifeStory();

            // Fetch the **golden events** from the MongoDB database.
            lifeStory.EventsGolden = LifeStoryJsonSerializer.getGroundTruthEvents(lifeStory, euc.userContext.socialWorldUser);

            lifeStory.goldDatums = new ArrayList<Datum>();

            // Now create the set of **golden pairs**, create list of **golden datums** at the same time.
            List<DatumPairSimilarity> goldenPairs = new ArrayList<DatumPairSimilarity>();
            for (Event leftEvent : lifeStory.EventsGolden) {
                for (Datum left : leftEvent.getDatums()) {
                    lifeStory.goldDatums.add(left);// Note we only add the left datums.
                    for (Event rightEvent : lifeStory.EventsGolden) {
                        for (Datum right : rightEvent.getDatums()) {
                            if (left == right) {
                                continue;
                            }
                            if (left.getNetworkID() > right.getNetworkID()) {
                                continue;
                            }

                            DatumPairSimilarity pair = new DatumPairSimilarity(left, right);
                            pair.goldIsInSameEvent = (leftEvent == rightEvent);
                            goldenPairs.add(pair);
                        }
                    }
                }
            }
            lifeStory.goldenPairs = goldenPairs;
        }
    }

    

}
