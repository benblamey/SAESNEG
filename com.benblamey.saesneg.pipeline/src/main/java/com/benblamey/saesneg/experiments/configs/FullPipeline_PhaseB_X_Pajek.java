package com.benblamey.saesneg.experiments.configs;

import com.benblamey.saesneg.PipelineContext;
import com.benblamey.saesneg.experiments.Experiment;
import com.benblamey.saesneg.experiments.ExperimentOptions;
import com.benblamey.saesneg.experiments.ExperimentSet;
import com.benblamey.saesneg.experiments.LibSVMGoldActions;
import com.benblamey.saesneg.experiments.LifeStorySelectionStrategy;
import com.benblamey.saesneg.experiments.PhaseBOptions;

public class FullPipeline_PhaseB_X_Pajek {

    public static void main(String[] args) throws Exception {

        ExperimentSet set = new ExperimentSet();
        set.name = "FullPipeline_PhaseB_X_Pajek";

        set.addExperiment(new Experiment(set) {
            {
                Name = "default";

                Options = new ExperimentOptions() {
                    {
                        UserQuery = PipelineContext.getCurrentContext().getUserQuery(); // Don't change here

                        lifeStorySelectStrategy = LifeStorySelectionStrategy.UseGroundTruthMatching;

                        // Phase A options.
                        _gisTextOptions = null; // Don't run OSM search.
                        _textOptions = null;//ProcessTextOptions.Default; // Default text options, for now.
                        runPhaseAMetadata = false;
                        PhaseBOptions = new PhaseBOptions() {
                            {
                                LibSVMGoldAction = LibSVMGoldActions.CrossRun_Individual;
                                //EdgeClassifier = EdgeClassifierMethod.SVM;
                            }
                        };
                    }
                };

            }
        });

        set.run();

    }
}
