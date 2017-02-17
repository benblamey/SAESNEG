package benblamey.saesneg.experiments.configs;

import benblamey.saesneg.experiments.Experiment;
import benblamey.saesneg.experiments.ExperimentOptions;
import benblamey.saesneg.experiments.ExperimentSet;
import benblamey.saesneg.experiments.LifeStorySelectionStrategy;
import benblamey.saesneg.phaseA.text.ProcessTextOptions;

public class TomcatExperimentSet {

    private static Experiment _exp;

    public static ExperimentOptions ExperimentOptions = new ExperimentOptions() {
        {
            UserQuery = null;// This gets set to the user from the request.

            lifeStorySelectStrategy = LifeStorySelectionStrategy.UseGroundTruthMatching;

            // Phase A options.
            _gisTextOptions = null; // Don't run OSM search.
            _textOptions = new ProcessTextOptions() {
                {
                    // Skip text processing.
                    //USE_CACHED_GATE_DOC = true;
                }
            };
            
            runPhaseAMetadata = true;
            
            PhaseBOptions = null;
//            new PhaseBOptions() {
//                {
//                    LibSVMGoldAction = LibSVMGoldAction.ExportAllUserCases;
//                }
//            };
        }
    };

    public static synchronized Experiment getExperiment() {
        


        if (_exp == null) {
            
                            ExperimentSet set = new ExperimentSet();
        set.name = "Thesis_Experiment_Set";
            
            _exp = new Experiment(set) {
                {
                    Name = "default";
                    Options = ExperimentOptions;
                }
            };

        }
        return _exp;
    }

}
