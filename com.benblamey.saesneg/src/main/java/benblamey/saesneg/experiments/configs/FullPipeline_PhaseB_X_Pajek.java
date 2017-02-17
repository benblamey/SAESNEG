package benblamey.saesneg.experiments.configs;

import benblamey.saesneg.PipelineContext;
import benblamey.saesneg.experiments.Experiment;
import benblamey.saesneg.experiments.ExperimentOptions;
import benblamey.saesneg.experiments.ExperimentSet;
import benblamey.saesneg.experiments.LibSVMGoldActions;
import benblamey.saesneg.experiments.LifeStorySelectionStrategy;
import benblamey.saesneg.experiments.PhaseBOptions;

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
