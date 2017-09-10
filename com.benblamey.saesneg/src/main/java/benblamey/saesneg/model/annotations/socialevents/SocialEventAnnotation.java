package com.benblamey.saesneg.model.annotations.socialevents;

import com.benblamey.saesneg.model.annotations.Annotation;
import gate.FeatureMap;

public abstract class SocialEventAnnotation extends Annotation {

    public abstract FeatureMap getFeaturesForGATE();

}
