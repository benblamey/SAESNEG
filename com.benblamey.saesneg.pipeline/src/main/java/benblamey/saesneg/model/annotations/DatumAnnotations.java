package com.benblamey.saesneg.model.annotations;

import java.util.ArrayList;
import java.util.List;

public class DatumAnnotations {

    public final List<LocationAnnotation> Locations = new ArrayList<>();
    public final List<PersonAnnotation> People = new ArrayList<>();
    public final List<benblamey.saesneg.model.annotations.socialevents.SocialEventAnnotation> SocialEventAnnotation = new ArrayList<>();
    public final List<DatumsInUserStructureAnnotation> UserStructureAnnotations = new ArrayList<>();
    public final List<TemporalAnnotation> DateTimesAnnotations = new ArrayList<>();
    public final List<ImageContentAnnotation> ImageContentAnnotations = new ArrayList<>();

    public List<Annotation> getAllAnnotations() {
        // no yield return in java.
        List<Annotation> all = new ArrayList<Annotation>();
        all.addAll(Locations);
        all.addAll(People);
        all.addAll(SocialEventAnnotation);
        all.addAll(UserStructureAnnotations);
        all.addAll(DateTimesAnnotations);
        all.addAll(ImageContentAnnotations);

        return all;
    }

}
