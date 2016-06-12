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
import benblamey.saesneg.phaseB.strategies.EventsStrategy;
import benblamey.saesneg.phaseB.strategies.FriendsStrategy;
import benblamey.saesneg.phaseB.strategies.KindStrategy;
import benblamey.saesneg.phaseB.strategies.SceneStrategy;
import benblamey.saesneg.phaseB.strategies.SpatialStrategy;
import benblamey.saesneg.phaseB.strategies.Strategy;
import benblamey.saesneg.phaseB.strategies.TemporalStrategy;
import benblamey.saesneg.phaseB.strategies.UserHelpStrategy;
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
