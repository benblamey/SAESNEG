package benblamey.saesneg.model.annotations.socialevents;

import benblamey.saesneg.model.annotations.Annotation;
import gate.FeatureMap;

public abstract class SocialEventAnnotation extends Annotation {

    public abstract FeatureMap getFeaturesForGATE();

}
