package benblamey.saesneg.experiments.configs;

import benblamey.nominatim.OpenStreetMapSearchAlgorithmOptions;
import benblamey.saesneg.PipelineContext;
import benblamey.saesneg.experiments.Experiment;
import benblamey.saesneg.experiments.ExperimentOptions;
import benblamey.saesneg.experiments.ExperimentSet;
import benblamey.saesneg.experiments.LifeStorySelectionStrategy;
import benblamey.saesneg.phaseA.text.ProcessTextOptions;

public class PhaseAOnlyCachedExperimentSet {

    public static void main(String[] args) throws Exception {

        ExperimentSet set = new ExperimentSet();
        set.name = "FullPipelineNoOSMExperimentSet";

        set.addExperiment(new Experiment(set) {
            {
                Name = "PhaseAOnlyExperimentSet_CACHED";
                Options = new ExperimentOptions() {
                    {
                        lifeStorySelectStrategy = LifeStorySelectionStrategy.UseGroundTruthMatching;

                        UserQuery = PipelineContext.getCurrentContext().getUserQuery();

                        // Phase A options.
                        _gisTextOptions = new OpenStreetMapSearchAlgorithmOptions();

                        _textOptions = new ProcessTextOptions() {
                            {
                                // Load GATE doc from cache.
                                USE_CACHED_GATE_DOC = true;
                            }
                        };
                        PhaseBOptions = null; // Don't run Phase B.
                    }
                };

            }
        });

        set.run();

    }

}
