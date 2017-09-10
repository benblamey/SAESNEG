package com.benblamey.saesneg.experiments.configs;

import com.benblamey.saesneg.experiments.Experiment;
import com.benblamey.saesneg.experiments.ExperimentOptions;
import com.benblamey.saesneg.experiments.ExperimentSet;
import com.benblamey.saesneg.experiments.PhaseBOptions;
import com.benblamey.saesneg.phaseA.text.ProcessTextOptions;
import com.mongodb.BasicDBObject;

public class PhaseBOnly_Train_ExperimentSet {

    public static void main(String[] args) throws Exception {

        ExperimentSet set = new ExperimentSet();
        set.name = "FullPipelineNoOSMExperimentSet";

        set.addExperiment(new Experiment(set) {
            {
                Name = "default";
                Options = new ExperimentOptions() {
                    {
                        UserQuery = new BasicDBObject() {
                            {
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
                                LibSVMGoldAction = LibSVMGoldAction.ExportAllUserCases;
                            }
                        };
                    }
                };

            }
        });

        set.run();

    }
}
