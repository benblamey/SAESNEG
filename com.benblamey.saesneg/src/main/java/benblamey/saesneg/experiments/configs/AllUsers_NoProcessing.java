package com.benblamey.saesneg.experiments.configs;

import com.benblamey.saesneg.PipelineContext;
import com.benblamey.saesneg.experiments.Experiment;
import com.benblamey.saesneg.experiments.ExperimentOptions;
import com.benblamey.saesneg.experiments.ExperimentSet;

public class AllUsers_NoProcessing {

    public static void main(String[] args) throws Exception {

        ExperimentSet set = new ExperimentSet();
        set.name = "AllUsers_NoProcessing";

        set.addExperiment(new Experiment(set) {
            {
                Name = "default";
                Options = new ExperimentOptions() {
                    {
                        UserQuery = PipelineContext.getCurrentContext().getUserQuery();

                        // Skip both phases.
                        // Phase A options.
                        runPhaseA = false; // Skip Phase A completely.

                        // Skip phase B.
                        PhaseBOptions = null;
                    }
                };

            }
        });

        set.run();

    }

}
