package benblamey.saesneg.phaseB;

import benblamey.saesneg.model.datums.Datum;
import com.benblamey.core.classifier.svm.SvmClassificationProbResult;
import com.benblamey.core.classifier.svm.SvmFile;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds the DatumSimilarityEvidence, and SVM output for a pair of datums.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class DatumPairSimilarity {

    public static final int SVM_CLASS_UNKNOWN = -1; // Ignored by LibSVM, so doesn't matter what is here.
    public static final int SVM_CLASS_DIFFERENT_EVENT = 1;
    public static final int SVM_CLASS_SAME_EVENT = 2;

    private Datum _left;
    private Datum _right;
    private List<DatumSimilarityEvidence> _evidence = new ArrayList<>();
    private List<FestibusFeatures> _evidenceFeatures = new ArrayList<>();

    public SvmClassificationProbResult classificationResult;
    public boolean goldIsInSameEvent;

    public DatumPairSimilarity(Datum left, Datum right) {

        if (Long.compare(left.getNetworkID(), right.getNetworkID()) < 0) {
            _left = left;
            _right = right;
        } else {
            _left = left;
            _right = right;
        }

    }
    
    /**
     * Generates a canonical label which can be used as a key for uniquqly identifying keys storing and pairs.
     */
    public static String getCanonicalLabel(Datum a, Datum b) {
        if (a == b) {
            throw new RuntimeException("a and b are the same -- cannot have a loopback pair");
        }
        Long left = a.getNetworkID();
        Long right  = b.getNetworkID();
        
        // Swap as necessary
        if (right > left)
        {
            Long t = left;
            left = right;
            right = t;
        }
        
        return left + "_" + right;
    }

    /**
     * Generates a canonical label which can be used as a key for uniquqly identifying keys storing and pairs.
     * @param pair
     * @return 
     */
    public static String getCanonicalLabel(DatumPairSimilarity pair) {
        return getCanonicalLabel(pair.getLeft(), pair.getRight());
    }

    public List<DatumSimilarityEvidence> getEvidence() {
        return _evidence;
    }

    public Datum getLeft() {
        return _left;
    }

    public Datum getRight() {
        return _right;
    }

    public void addEvidence(DatumSimilarityEvidence evidence) {
        _evidence.add(evidence);
    }

    public void addCaseToSvmFile(SvmFile svmFile, int classLabel) {
        // Create the set of feature values.
        double[] featureWeights = new double[FestibusFeatures.values().length];
        for (DatumSimilarityEvidence evid : this.getEvidence()) {
            double featureValue = evid.getSVMFeatureValue();

            if (Double.isNaN(featureValue) || Double.isInfinite(featureValue)) {
                throw new IllegalArgumentException(evid.getFeatureID() + " has NaN value");
            }

            featureWeights[evid.getFeatureID().ordinal()] = featureValue;
        }

        try {
            // Add the case to the file.
            svmFile.addCase(classLabel, featureWeights);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
