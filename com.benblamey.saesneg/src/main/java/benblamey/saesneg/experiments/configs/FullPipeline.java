package benblamey.saesneg.experiments.configs;

import benblamey.nominatim.OpenStreetMapSearchAlgorithmOptions;
import benblamey.saesneg.PipelineContext;
import benblamey.saesneg.experiments.Experiment;
import benblamey.saesneg.experiments.ExperimentOptions;
import benblamey.saesneg.experiments.ExperimentSet;
import benblamey.saesneg.experiments.LibSVMGoldActions;
import benblamey.saesneg.experiments.LifeStorySelectionStrategy;
import benblamey.saesneg.experiments.PhaseBOptions;
import benblamey.saesneg.phaseA.text.ProcessTextOptions;

public class FullPipeline {

    public static void main(String[] args) throws Exception {

        ExperimentSet set = new ExperimentSet();
        set.name = "FullPipeline";

        set.addExperiment(new Experiment(set) {
            {
                Name = "Full";

                Options = new ExperimentOptions() {
                    {
                        UserQuery = PipelineContext.getCurrentContext().getUserQuery(); // Don't change here

                        lifeStorySelectStrategy = LifeStorySelectionStrategy.UseGroundTruthMatching;

                        // Phase A options.
                        _gisTextOptions = new OpenStreetMapSearchAlgorithmOptions();

                        _textOptions = new ProcessTextOptions() {};
                        
                        runPhaseAMetadata = true;
                        
                        geocodeMetadata = true;
                        
                        PhaseBOptions = new PhaseBOptions() {
                            {
                                LibSVMGoldAction = LibSVMGoldActions.CrossRun_Individual;
                            }
                        };
                        
                        
                    }
                };

            }
        });

        set.run();

    }
}
