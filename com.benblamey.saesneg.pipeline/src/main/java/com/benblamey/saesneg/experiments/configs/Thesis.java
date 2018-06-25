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
import com.benblamey.saesneg.phaseB.strategies.EventsStrategy;
import com.benblamey.saesneg.phaseB.strategies.FriendsStrategy;
import com.benblamey.saesneg.phaseB.strategies.KindStrategy;
import com.benblamey.saesneg.phaseB.strategies.SceneStrategy;
import com.benblamey.saesneg.phaseB.strategies.SpatialStrategy;
import com.benblamey.saesneg.phaseB.strategies.Strategy;
import com.benblamey.saesneg.phaseB.strategies.TemporalStrategy;
import com.benblamey.saesneg.phaseB.strategies.UserHelpStrategy;
import java.util.HashSet;

public class Thesis extends ExperimentSet {

    static class AllThesisExpOptions extends ExperimentOptions {

        public AllThesisExpOptions(HashSet<Class<?>> strategies) {
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
                    Strategies = strategies;
                }
            };
        }

    }

    public static void main(String[] args) throws Exception {
        ExperimentSet set = new ExperimentSet();
        set.name = "Thesis";

        set.addExperiment(new Experiment(set) { { Options= new AllThesisExpOptions( Strategy.getStrategiesDefault() ) {  { PhaseBOptions.UseDistributedTemporalSimilarity = true; }  }; Name="Full"; } });
        set.addExperiment(new Experiment(set) { { Options= new AllThesisExpOptions( Strategy.getStrategiesDefault() ) {  { PhaseBOptions.UseDistributedTemporalSimilarity = false; }  }; Name="Full_ClassicTempex"; } });

        set.addExperiment(new Experiment(set) { { Options= new AllThesisExpOptions( Strategy.getStrategiesNone() ); Name="None"; } });
        set.addExperiment(new Experiment(set) { { Options= new AllThesisExpOptions( Strategy.getStrategiesOnly(FriendsStrategy.class) ); Name="FriendsStrat_Only";  } });
        set.addExperiment(new Experiment(set) { { Options= new AllThesisExpOptions( Strategy.getStrategiesOnly(EventsStrategy.class) ); Name="EventsStrat_Only"; } });
        set.addExperiment(new Experiment(set) { { Options= new AllThesisExpOptions( Strategy.getStrategiesOnly(SpatialStrategy.class) ); Name="SpatialStrat_Only"; } });
        set.addExperiment(new Experiment(set) { { Options= new AllThesisExpOptions( Strategy.getStrategiesOnly(TemporalStrategy.class) ); Name="TemporalStrat_Only"; } });
        set.addExperiment(new Experiment(set) { { Options= new AllThesisExpOptions( Strategy.getStrategiesOnly(TemporalStrategy.class) ) {  { PhaseBOptions.UseDistributedTemporalSimilarity = false; }  } ; Name="OldSkoolTempex";  } });
        set.addExperiment(new Experiment(set) { { Options= new AllThesisExpOptions( Strategy.getStrategiesOnly(UserHelpStrategy.class) ); Name="UserHelpStrat_Only"; } });
        set.addExperiment(new Experiment(set) { { Options= new AllThesisExpOptions( Strategy.getStrategiesOnly(SceneStrategy.class) ); Name="SceneStrat_Only"; } });
        set.addExperiment(new Experiment(set) { { Options= new AllThesisExpOptions( Strategy.getStrategiesOnly(KindStrategy.class) ); Name="KindStrat_Only"; } });



        set.run();
    }

}
