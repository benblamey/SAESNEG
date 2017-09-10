package com.benblamey.saesneg.experiments;

public enum LifeStorySelectionStrategy {

    @Deprecated
    LatestOnDisk, // Load the most recent file on disk, ignoring the info. Used as a temporary measure while JSON was missing.

    UseGroundTruthMatching, // Use the life story which matches the gold gate doc -this is the only strategy suitable for gold labelling text eval. */

    MatchGoldGATEDoc // Load the version of the lifestory which matches the gold GATE doc.

            /* Two strategies for life story selection:
		- Use the latest life story always - recommended for most things - maximizes Ground truth data which "exists" in the life stories (38 missing vs. 104 missing for GOLD Gate Doc)
		- */

}
