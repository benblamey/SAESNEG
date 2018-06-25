package com.benblamey.saesneg.experiments.configs;

import com.benblamey.nominatim.OpenStreetMapSearchAlgorithmOptions;
import com.benblamey.saesneg.PipelineContext;
import com.benblamey.saesneg.experiments.Experiment;
import com.benblamey.saesneg.experiments.ExperimentOptions;
import com.benblamey.saesneg.experiments.ExperimentSet;
import com.benblamey.saesneg.experiments.LibSVMGoldActions;
import com.benblamey.saesneg.experiments.LifeStorySelectionStrategy;
import com.benblamey.saesneg.experiments.PhaseBOptions;
import com.benblamey.saesneg.phaseA.text.ProcessTextOptions;

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
