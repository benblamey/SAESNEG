package com.benblamey.saesneg.phaseB;

import java.util.List;

public interface IEdgeClassifier {

    public abstract void computePairSimilarity(List<DatumPairSimilarity> similarities, String outputFile);

}
