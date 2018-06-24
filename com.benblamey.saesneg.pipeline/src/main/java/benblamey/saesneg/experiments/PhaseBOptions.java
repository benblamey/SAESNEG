package com.benblamey.saesneg.experiments;

import com.benblamey.saesneg.phaseB.strategies.Strategy;
import java.util.HashSet;

/**
 * Various options for Phase B of the pipeline (i.e. datum event clustering).
 *
 * @author Ben Blamey ben@benblamey.com
 *

 */
public class PhaseBOptions {

//	/**
//	 * Which datums to include in the clustering.
//	 */
//	public DatumsScope scope = DatumsScope.DatumsUsedInGroundTruth;
    /////////////////////////////////////////////
    // Options for festibusk feature calculations.
    ///////////////////////////////////////////
    /**
     * if false -- use legacy "# days between"-based similarity. if true -- use
     * new distributed p.d.f based similarity.
     */
    public boolean UseDistributedTemporalSimilarity = true;

    /////////////////////////////////////////////
    // Clustering / Training options
    /////////////////////////////////////////////
    /**
     * What to do with gold datum event clusters.
     */
    public LibSVMGoldActions LibSVMGoldAction;

    /////////////////////////////////////////////
    // Options for the evaluation of clustering quality.
    /////////////////////////////////////////////

    /**
     * Export the event files for comparison of clustering in a difftool etc.
     */
    public boolean exportForDiffTool;

    /**
     * Whether to compute the pairwise accuracy. Perhaps a poor measure.
     */
    public boolean computePairwiseAccuracy;

    public HashSet<Class<?>> Strategies = Strategy.getStrategiesDefault();


}
