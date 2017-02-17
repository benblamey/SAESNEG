package benblamey.saesneg.experiments;

import com.benblamey.core.onmi.OnmiResult;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds the overall results for running classification on a single user.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class ExperimentResults {

    /**
     * Onmi, Mutual-Information based evaluation results, per user.
     */
    public final Map<String, OnmiResult> OnmiResults = new HashMap<>();
    
    public final Map<String, Object> PairwiseResults = new HashMap<>();

}
