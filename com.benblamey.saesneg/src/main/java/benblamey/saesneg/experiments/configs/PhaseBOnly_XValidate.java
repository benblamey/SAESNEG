package benblamey.saesneg.experiments.configs;

import benblamey.saesneg.experiments.Experiment;
import benblamey.saesneg.experiments.ExperimentOptions;
import benblamey.saesneg.experiments.ExperimentSet;
import benblamey.saesneg.experiments.LibSVMGoldActions;
import benblamey.saesneg.experiments.PhaseBOptions;
import benblamey.saesneg.phaseA.text.ProcessTextOptions;
import com.mongodb.BasicDBObject;
import socialworld.model.SocialWorldUser;

public class PhaseBOnly_XValidate {

    public static void main(String[] args) throws Exception {

        ExperimentSet set = new ExperimentSet();
        set.name = "PhaseBOnly_XValidate";

        set.addExperiment(new Experiment(set) {
            {
                Name = "train";
                Options = new ExperimentOptions() {
                    {
                        UserQuery = new BasicDBObject() {
                            {
                                append(SocialWorldUser.FACEBOOK_EMAIL, new BasicDBObject("$ne", "PARTICIPANT_1_EMAIL"));

                                append("LIFE_STORY_INFOS.SUCCESS", true); // Select only users for whom we have a valid life story.
                                append("GROUND_TRUTH_EVENTS", new BasicDBObject("$exists", true));  // Where the user has created ground truth events.
                            }
                        };

                        // Phase A options.
                        _gisTextOptions = null; // Don't run OSM search.
                        _textOptions = new ProcessTextOptions() {
                            {
                                // Skip text processing.
                                USE_CACHED_GATE_DOC = true;
                            }
                        };
                        PhaseBOptions = new PhaseBOptions() {
                            {
                                LibSVMGoldAction = LibSVMGoldActions.ExportAllUserCases_AndTrain;
                            }
                        };
                    }
                };

            }
        });

        set.addExperiment(new Experiment(set) {
            {
                Name = "testonParticipant1";
                Options = new ExperimentOptions() {
                    {
                        UserQuery = new BasicDBObject() {
                            {
                                // We test on Participant 1.
                                append(SocialWorldUser.FACEBOOK_USER_ID, UserIDs.PARTICIPANT_1_FACEBOOK_ID);

                                append("LIFE_STORY_INFOS.SUCCESS", true); // Select only users for whom we have a valid life story.
                                append("GROUND_TRUTH_EVENTS", new BasicDBObject() { // Where the user has created ground truth events.
                                    {
                                        append("$exists", true);
                                    }
                                });
                            }
                        };

                        // Phase A options.
                        _gisTextOptions = null; // Don't run OSM search.
                        _textOptions = new ProcessTextOptions() {
                            {
                                // Skip text processing.
                                USE_CACHED_GATE_DOC = true;
                            }
                        };

                        PhaseBOptions = new PhaseBOptions() {
                            {
                                LibSVMGoldAction = null;

                                exportForDiffTool = true;
                                computePairwiseAccuracy = true;
                            }
                        };
                    }
                };

            }
        });

        set.run();

    }

}
