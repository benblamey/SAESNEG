package benblamey.saesneg;

import benblamey.core.DateUtil;
import benblamey.core.GATE.GateUtils2;
import benblamey.saesneg.experiments.Experiment;
import benblamey.saesneg.model.Event;
import benblamey.saesneg.model.LifeStory;
import benblamey.saesneg.model.UserContext;
import benblamey.saesneg.model.datums.Datum;
import benblamey.saesneg.phaseA.metadata.ProcessMetadata;
import benblamey.saesneg.phaseA.text.GATEFileKind;
import benblamey.saesneg.phaseA.text.ProcessText;
import benblamey.saesneg.phaseB.DatumPairSimilarity;
import benblamey.saesneg.phaseB.eval.EventMatcher;
import benblamey.saesneg.phaseB.eval.FileDiffEventMatcherOutput;
import benblamey.saesneg.serialization.LifeStoryJsonSerializer;
import com.benblamey.core.BinarySerializer;
import com.benblamey.core.StreamUtils;
import com.benblamey.core.classifier.svm.SvmFile;
import com.benblamey.core.onmi.OnmiResult;
import com.benblamey.core.onmi.onmi;
import gate.AnnotationSet;
import gate.Document;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

/**
 * Execution of the pipeline for a single user.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class ExperimentUserContext {

    private final Experiment _parent;
    public final UserContext userContext;

    public ExperimentUserContext(UserContext uc, Experiment parent) {

        this.userContext = uc;
        this._parent = parent;

        if (userContext.getLifeStory() != null) {
            // Copy the profile across from the old context to the new.
            if (userContext.getProfile() == null) {
                userContext.setProfile(userContext.getLifeStory().datums.get(0)._user.getProfile());
            }
        }
    }

    public void runPhaseA() throws Exception {

        LifeStory ls = userContext.getLifeStory();

        /////////////////////////////////////////////////////////////////////////////////////////////////
        // Process TEXT
        ProcessMetadata.processDatums(ls.datums);

        Document doc;
        if (this._parent.Options._textOptions != null) {
            if (this._parent.Options._textOptions.USE_CACHED_GATE_DOC) {
                //doc = GateUtils2.loadGATEDoc(Pipeline.getGATEfilename(_user, _lifeStoryInfo, GATEFileKind.Generated));
                System.out.println("Reading cached GATE doc from binary file...");

                // Use the binary file containing the OSM gazetteer search results.
                doc = (Document) BinarySerializer.readObjectFromFile(getGATEfilename(userContext, GATEFileKind.BinaryCache));
                System.out.print("initializing datums...");

                // Link the datums to the GATE document.
                for (Datum mfo : ls.datums) {
                    mfo.loadGateDocument(doc);
                }
                System.out.print("initializing datums...");
                System.out.println("Reading cached GATE doc from binary file done.");
            } else {
                // Run GATE / Stanford
                doc = ProcessText.createGATEdoc(ls);

                System.out.println(doc.getContent().toString());
                
                ProcessText.processDocument(doc, _parent.Options._textOptions, _parent.Options._gisTextOptions, this._parent.LogFile);

                // Export to XML (before we add the OSM annotations -- it makes the file too big)
                // NEVER OVERWRITE THE GOLD LABELLED GATE FILES.
                GateUtils2.exportGATEtoXML(doc, getGATEfilename(userContext, GATEFileKind.Generated));

                // Now run the OSM Gazetteer.
                ProcessText.runOSMGazetteer(doc, _parent.Options._gisTextOptions, _parent.Options._textOptions);

                doc.getFeatures().put("lifestory_date", userContext.lifeStoryInfo.created.toString());
                doc.getFeatures().put("lifestory_filename", userContext.lifeStoryInfo.filename);

                // And serialize in binary form.
                // NEVER OVERWRITE THE GOLD LABELLED GATE FILES.
                
                //BinarySerializer.writeToFile(getGATEfilename(userContext, GATEFileKind.BinaryCache), doc);
            }

            // This is expensive, so we avoid doing it for each datum.
            AnnotationSet osmLocationAnnotations = doc.getAnnotations(Datum.OPEN_STREET_MAP_BEN_AS).get(Datum.LOCATION_GATE_ANNOTATION);

            // Now ANNIE has run, do further text processing. (this includes custom taggers that
            // rely on tokenization.)
            int i = 0;
            for (Datum mfo : ls.datums) {
                if (0 == i % 100) {
                    System.out.println("postANNIETextProcessing - " + i + " of " + ls.datums.size());
                }
                mfo.postANNIETextProcessing(_parent.Options._textOptions, _parent.Options._gisTextOptions, osmLocationAnnotations);
                i++;
            }

            // Remove OSM gazetteer annotations (they make the XML file too long for GATE).
            doc.removeAnnotationSet(Datum.OPEN_STREET_MAP_BEN_AS);

            // Export again (post processing can add annotations) -- separate file because of the caching.
            {
                String fileName = getGATEfilename(userContext, GATEFileKind.PostProcessingAnnotations);
                GateUtils2.exportGATEtoXML(doc, fileName);
            }
        } else {
            System.out.println("Skipping text processing.");
        }

        /////////////////////////////////////////////////////////////////////////////////////////////////
        // Process METADATA
        if (this._parent.Options.runPhaseAMetadata) {
            for (Datum d : ls.datums) {
                d.processMetadataFields(this._parent.Options);
            }
        } else {
            System.out.println("Skipping Phase A / Metadata");
        }

        /////////////////////////////////////////////////////////////////////////////////////////////////
        // Process IMAGE CONTENT
        for (Datum d : ls.datums) {
            d.processImageContent();
        }

        System.out.println("There were " + Integer.toString(userContext.getLifeStory().datums.size()) + " entries");

    }

    public static String getGATEfilename(UserContext user, GATEFileKind kind) {
        String snippet;

        switch (kind) {
            case Gold:
                snippet = "gold.xml";
                break;
            case Generated:
                snippet = "gen.xml";
                break;
            case PostProcessingAnnotations:
                snippet = "gen_post.xml";
                break;
            case BinaryCache:
                snippet = "gen_post.bin";
                break;
            default:
                throw new IllegalArgumentException("duff enum");
        }
        String fileName = user.getOutputDirectoryWithTrailingSlash() + "gate_" + user.getFileSystemSafeName() + "_" + DateUtil.DateTimeToUnixTime(user.lifeStoryInfo.created) + "_" + snippet;
        return fileName;
    }

    //////////////////// EVALUATION ///////////////////////
    public void evaluationOnEvents() throws IOException, InterruptedException {

//		if (_parent.Options.PhaseBOptions.scope == DatumsScope.AllDatums) {
//			_parent.LogFile.append("Skipping eval because ran on all datums.");
//			return;
//		}
        if (_parent.Options.PhaseBOptions.computePairwiseAccuracy) {
            this._parent.clusteringEvaluation.evaluatePairwise(userContext);
        }

        computeClusterAccuracyOnmi();
    
        if (_parent.Options.PhaseBOptions.exportForDiffTool) {
            String comp = this._parent.getOutputDirectory() + this.userContext.getName() + "_comp.txt";
            String gt = this._parent.getOutputDirectory() + this.userContext.getName() + "_gtruth.txt";
            final String difftoo = "C:\\Program Files\\SourceGear\\Common\\DiffMerge\\sgdm.exe";
            this._parent.LogFile.println(difftoo + " \"" + comp + "\" \"" + gt + "\"");

            // Export for viewing in DiffMerge.
            FileDiffEventMatcherOutput output = new FileDiffEventMatcherOutput(comp, gt);

            EventMatcher.run(output, this.userContext.getLifeStory().EventsComputed, this.userContext.getLifeStory().EventsGolden);

            output.close();
        }
    }

//	/**
//	 * Run SVM training or x-validation for gold-truth clustering.
//	 * 
//	 * @param ls
//	 * @param gtruthDatums
//	 * @throws IOException
//	 */
//	public void goldClusteringSVM(LifeStory ls, List<Datum> gtruthDatums) throws IOException {
//		String svmFilePath;
//		boolean append = false;
//
//		switch (this._parent.Options.PhaseBOptions.LibSVMGoldAction) {
//			case ExportAllUserCases:
//			case ExportAllUserCases_AndTrain:
//				svmFilePath = this._parent.getallUsersLibSVMCasesPath();
//				append = true;
//				break;
//			case CrossValidate_Individual:
//				svmFilePath = this._parent.getOutputDirectory() + "/" + userContext.getName() + "_svm.dat";
//				this._parent.LogFile.println("Creating SVM file: " + svmFilePath);
//				append = false;
//				break;
//			case None:
//				svmFilePath = null;
//				append = false;
//				break;
//			default:
//				throw new RuntimeException();
//		}
//
//		// SVM Training.
//		if (svmFilePath != null) {
//
//			SvmFile svmFile = new SvmFile(svmFilePath, FestibusFeatures.values().length, append);
//
//			ls.DatumSimilarityCalculator.runFestibus(ls.goldenPairs);
//
//			int casesInner = 0;
//			int casesOuter = 0;
//
//			// For each pair of datums...
//
//			for (DatumPairSimilarity pairSim : computeAllSimilarities) {
//
//				// Compute the class (i.e. does the edge lie inside a single event?).
//				boolean isInSameGTEvent = false;
//				for (Event a : ls.EventsGolden) {
//					if (a.getDatums().contains(pairSim.getLeft())) {
//						isInSameGTEvent = a.getDatums().contains(pairSim.getRight());
//						break;
//					}
//				}
//				if (isInSameGTEvent) {
//					casesInner++;
//				} else {
//					casesOuter++;
//				}
//				int classLabel = isInSameGTEvent ? DatumPairSimilarity.SVM_CLASS_SAME_EVENT : 
//					DatumPairSimilarity.SVM_CLASS_DIFFERENT_EVENT;
//
//				pairSim.addCaseToSvmFile(svmFile, classLabel);
//			}
//
//			this._parent.LogFile.println("Total number of edges: " + (casesInner + casesOuter));
//			this._parent.LogFile.println("\t" + "Edges inside events: " + casesInner);
//			this._parent.LogFile.println("\t" + "Edges outside events: " + casesOuter);
//
//			svmFile.close();
//
//			if (this._parent.Options.PhaseBOptions.LibSVMGoldAction == LibSVMGoldActions.CrossValidate_Individual) {
//				this._parent.LogFile.println("To x-validate individuals:");
//				this._parent.LogFile.println(LibSVM.generateSVMCrossValidateCmd(svmFilePath));
//			}
//		}
//	}
    private void computeClusterAccuracyOnmi() throws IOException, InterruptedException {
        String onmi_comp = this._parent.getOutputDirectory() + this.userContext.getName() + "_onmi_comp.txt";
        printOnmiFile(this.userContext.getLifeStory().EventsComputed, onmi_comp);
        String onmi_gt = this._parent.getOutputDirectory() + this.userContext.getName() + "_onmi_gtruth.txt";
        printOnmiFile(this.userContext.getLifeStory().EventsGolden, onmi_gt);

        OnmiResult result = onmi.run(onmi_comp, onmi_gt);
        this._parent.LogFile.println("onmi output:");
        this._parent.LogFile.println(result.getDump());
        this._parent.Results.OnmiResults.put(this.userContext.ID,result);
    }

    private void printOnmiFile(Collection<Event> events, String filePath) throws IOException {
        BufferedWriter output = new BufferedWriter(new FileWriter(filePath));
        // Print out a list of IDs on each line - one line for each event.
        for (Event e : events) {
            for (Datum d : e.getDatums()) {
                output.append(d.getNetworkID() + " ");
            }
            output.append("\n");
        }
        output.close();
    }

    ///////////////////////// EXPORT ///////////////////////////////////////////////
    public void exportGenClusters() {
        String filePath = getClusterOutputFilename(userContext.getOutputDirectoryWithTrailingSlash());
        String eventsJson = LifeStoryJsonSerializer.eventsToJson(this.userContext.getLifeStory().EventsComputed);
        StreamUtils.writeStringToFile(eventsJson, filePath);
        this._parent.LogFile.println("Generated Clusters exported to: " + filePath);

    }

    public static String getClusterOutputFilename(String userOutputFolder) {
        String filenameSnippet = "gold"; // used to be switched on different scopes (i.e. gold gt only vs. all)
        String filePath = userOutputFolder + "gen_" + filenameSnippet + "_clusters.json";
        return filePath;
    }

    public void addPairsToSvmFile(SvmFile svmFile) throws IOException {
        for (DatumPairSimilarity pair : this.userContext.getLifeStory().goldenPairs) {
            pair.addCaseToSvmFile(svmFile,
                    pair.goldIsInSameEvent
                            ? DatumPairSimilarity.SVM_CLASS_SAME_EVENT
                            : DatumPairSimilarity.SVM_CLASS_DIFFERENT_EVENT);
        }
    }
}
