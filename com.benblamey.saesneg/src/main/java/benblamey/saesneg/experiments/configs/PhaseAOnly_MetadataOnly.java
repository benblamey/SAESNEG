package com.benblamey.saesneg.experiments.configs;

import com.benblamey.nominatim.OpenStreetMapSearchAlgorithmOptions;
import com.benblamey.saesneg.PipelineContext;
import com.benblamey.saesneg.experiments.Experiment;
import com.benblamey.saesneg.experiments.ExperimentOptions;
import com.benblamey.saesneg.experiments.ExperimentSet;

public class PhaseAOnly_MetadataOnly {

    public static void main(String[] args) throws Exception {

        ExperimentSet set = new ExperimentSet();
        set.name = "PhaseAOnly_MetadataOnly";

        set.addExperiment(new Experiment(set) {
            {
                Name = "PhaseAOnly_MetadataOnly";
                Options = new ExperimentOptions() {
                    {
                        UserQuery = PipelineContext.getCurrentContext().getUserQuery(); // Don't change here

                        // Phase A options.
                        _gisTextOptions = new OpenStreetMapSearchAlgorithmOptions();

                        _textOptions = null; // Don't run text processing.

                        PhaseBOptions = null; // Don't run Phase B.
                    }
                };

            }
        });

        set.run();

    }

}
