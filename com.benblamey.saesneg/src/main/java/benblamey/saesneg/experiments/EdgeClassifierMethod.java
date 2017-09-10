package com.benblamey.saesneg.experiments;

/**
 * Methods for computing edge similarity class.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public enum EdgeClassifierMethod {

    /**
     * Using Support Vector Machine.
     */
    SVM,
    /**
     * By using a threshold on hand-crafted feature values.
     */
    HandCraftedScores,
}
