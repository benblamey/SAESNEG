package com.benblamey.saesneg.model.annotations.socialevents;

import gate.Factory;
import gate.FeatureMap;
import java.util.List;

public class WeddingSocialEventAnnotation extends SocialEventAnnotation {

    private String _partnerA = null;
    private String _partnerB = null;

    public WeddingSocialEventAnnotation() {
        log();
    }

    public WeddingSocialEventAnnotation(String who1) {
        _partnerA = who1;
        log();
    }

    public WeddingSocialEventAnnotation(String who1, String who2) {
        _partnerA = who1;
        _partnerB = who2;
        log();
    }


    private void log() {
        System.out.println("Created " + WeddingSocialEventAnnotation.class.getName()
                + ", partnerA = " + _partnerA
                + ", partnerB = " + _partnerB);

    }

    @Override
    public String toString() {
        return WeddingSocialEventAnnotation.class.getName() + ", partnerA = " + _partnerA
                + ", partnerB = " + _partnerB;
    }

    public String getPartnerA() {
        return _partnerA;
    }

    public String getPartnerB() {
        return _partnerA;
    }

    @Override
    public FeatureMap getFeaturesForGATE() {
        FeatureMap newFeatureMap = Factory.newFeatureMap();

        if (_partnerA != null) {
            newFeatureMap.put("partnerA", _partnerA);
        }

        if (_partnerB != null) {
            newFeatureMap.put("partnerB", _partnerB);
        }

        return newFeatureMap;
    }


    public static WeddingSocialEventAnnotation merge(List<WeddingSocialEventAnnotation> weddings) {
        WeddingSocialEventAnnotation mergedWedding = null;
        for (WeddingSocialEventAnnotation wedding : weddings) {
            if (mergedWedding == null) {
                mergedWedding = wedding;
            }
            if (mergedWedding._partnerB == null && wedding._partnerB != null) {
                // Annotations with two partners replace those with one.
                mergedWedding = wedding;
            }
        }
        return mergedWedding;
    }


}
