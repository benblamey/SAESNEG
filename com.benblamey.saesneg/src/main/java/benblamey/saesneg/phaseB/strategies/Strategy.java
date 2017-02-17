package benblamey.saesneg.phaseB.strategies;

import benblamey.saesneg.experiments.PhaseBOptions;
import benblamey.saesneg.phaseB.DatumPairSimilarity;
import java.util.HashSet;

public abstract class Strategy {

    public abstract void addEvidenceToPair(DatumPairSimilarity pair, PhaseBOptions b);
    
    public static HashSet<Class<?>> getStrategiesAllExcept(Class<?> classToExclude) {
        HashSet<Class<?>> strategiesDefault = getStrategiesDefault();
        strategiesDefault.remove(classToExclude);
        return strategiesDefault;
    }
    
    public static HashSet<Class<?>> getStrategiesOnly(Class<?> classToExclude) {
        return new HashSet<Class<?>>() {
                        {
                            add(classToExclude);
                        }
        };
    }
    
    public static HashSet<Class<?>> getStrategiesDefault() {
        return new HashSet<Class<?>>() {
                        {
                            add(FriendsStrategy.class);
                            add(EventsStrategy.class);
                            add(SpatialStrategy.class);
                            add(TemporalStrategy.class);
                            add(UserHelpStrategy.class);
                            add(SceneStrategy.class);
                            add(KindStrategy.class);
                        }
                    };
    }

    public static HashSet<Class<?>> getStrategiesNone() {
        return new HashSet<Class<?>>();
    }
    
    
    
}
