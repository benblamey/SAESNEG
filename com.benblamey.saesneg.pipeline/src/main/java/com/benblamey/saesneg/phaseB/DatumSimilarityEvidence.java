package com.benblamey.saesneg.phaseB;

public class DatumSimilarityEvidence {

    private FestibusFeatures _id;
    private double _value;
    private String _message;

    private DatumSimilarityEvidence() {
        throw new RuntimeException("not implemented");
    }

    public DatumSimilarityEvidence(FestibusFeatures id, double value) {
        this(id, value, "(empty)");
    }

    public DatumSimilarityEvidence(FestibusFeatures id, double value, String message) {
        if (value < -1 || value > +1) {
            throw new RuntimeException("svm value outside permitted range.");
        }
        _id = id;
        _value = value;
        _message = message;
    }

    public FestibusFeatures getFeatureID() {
        return _id;
    }

    public String getMessage() {
        return _message;
    }

    public double getSVMFeatureValue() {
        return _value;
    }

}
