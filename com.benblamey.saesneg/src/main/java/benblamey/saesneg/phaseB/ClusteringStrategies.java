package benblamey.saesneg.phaseB;

import benblamey.saesneg.experiments.PhaseBOptions;
import benblamey.saesneg.model.LifeStory;
import benblamey.saesneg.model.UserContext;
import benblamey.saesneg.phaseB.strategies.EventsStrategy;
import benblamey.saesneg.phaseB.strategies.FriendsStrategy;
import benblamey.saesneg.phaseB.strategies.KindStrategy;
import benblamey.saesneg.phaseB.strategies.SceneStrategy;
import benblamey.saesneg.phaseB.strategies.SpatialStrategy;
import benblamey.saesneg.phaseB.strategies.Strategy;
import benblamey.saesneg.phaseB.strategies.TemporalStrategy;
import benblamey.saesneg.phaseB.strategies.UserHelpStrategy;
import java.util.ArrayList;
import java.util.List;

public class ClusteringStrategies {

    //private static Map<String,DatumPairSimilarity> _pair_cache = new HashMap<>();
    private final ArrayList<Strategy> _festibus;
    private final PhaseBOptions _options;

    public ClusteringStrategies(final LifeStory ls, UserContext user, PhaseBOptions options) {
        if (options == null) {
            throw new RuntimeException("argument null: options");
        }
        this._options = options;

        ArrayList<Strategy> festibus = getStrategies(ls, user);
        _festibus = festibus;
    }

    private ArrayList<Strategy> getStrategies(final LifeStory ls, UserContext user) {
        ArrayList<Strategy> festibus = new ArrayList<Strategy>();
        if (_options.Strategies.contains(FriendsStrategy.class)) {
            festibus.add(new FriendsStrategy(ls, user)); // Friends    
        }
        if (_options.Strategies.contains(EventsStrategy.class)) {
            festibus.add(new EventsStrategy(ls));// Events
        }
        if (_options.Strategies.contains(SpatialStrategy.class)) {
            festibus.add(new SpatialStrategy());	// Spatial
        }
        if (_options.Strategies.contains(TemporalStrategy.class)) {
            festibus.add(new TemporalStrategy()); // Temporal
        }
        // Inference
        // Boundary
        if (_options.Strategies.contains(UserHelpStrategy.class)) {
            festibus.add(new UserHelpStrategy()); // User
        }
        if (_options.Strategies.contains(SceneStrategy.class)) {
            festibus.add(new SceneStrategy());
        }
        if (_options.Strategies.contains(KindStrategy.class)) {
            festibus.add(new KindStrategy()); // ??
        }    
        return festibus;
    }

    public void runFestibus(List<DatumPairSimilarity> pairs) {
        for (DatumPairSimilarity pair : pairs) {
            for (Strategy strategy : _festibus) {
                strategy.addEvidenceToPair(pair, _options);
            }
        }
    }

}
