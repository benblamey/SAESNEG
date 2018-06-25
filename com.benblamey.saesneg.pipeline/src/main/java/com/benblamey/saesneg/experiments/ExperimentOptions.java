package com.benblamey.saesneg.experiments;

import com.benblamey.nominatim.OpenStreetMapSearchAlgorithmOptions;
import com.benblamey.saesneg.phaseA.text.ProcessTextOptions;
import com.mongodb.DBObject;

/**
 * The options for an Experiment run on a set of users.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class ExperimentOptions {

    /**
     * The set of users to run the experiment on.
     */
    public transient DBObject UserQuery;

    public LifeStorySelectionStrategy lifeStorySelectStrategy = LifeStorySelectionStrategy.UseGroundTruthMatching; //Load the ground truth life story by default.

    /**
     * The options for Phase A text processing.
     */
    public ProcessTextOptions _textOptions;


    /**
     * Whether to geocode metadata.
     */
    public boolean geocodeMetadata = true;

    /**
     * The options for OpenStreetMap search. Set null to disable.
     */
    public OpenStreetMapSearchAlgorithmOptions _gisTextOptions;

    public PhaseBOptions PhaseBOptions;

    public boolean runPhaseA = true;

    public boolean runPhaseAMetadata = true;

}
