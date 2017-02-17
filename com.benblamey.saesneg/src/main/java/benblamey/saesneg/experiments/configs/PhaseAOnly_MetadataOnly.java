package benblamey.saesneg.experiments.configs;

import benblamey.nominatim.OpenStreetMapSearchAlgorithmOptions;
import benblamey.saesneg.PipelineContext;
import benblamey.saesneg.experiments.Experiment;
import benblamey.saesneg.experiments.ExperimentOptions;
import benblamey.saesneg.experiments.ExperimentSet;

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
