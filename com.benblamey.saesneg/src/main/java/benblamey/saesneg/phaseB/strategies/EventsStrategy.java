package benblamey.saesneg.phaseB.strategies;

import benblamey.saesneg.experiments.PhaseBOptions;
import benblamey.saesneg.model.LifeStory;
import benblamey.saesneg.model.annotations.socialevents.BirthdaySocialEventAnnotation;
import benblamey.saesneg.model.annotations.socialevents.SocialEventAnnotation;
import benblamey.saesneg.model.annotations.socialevents.WeddingSocialEventAnnotation;
import benblamey.saesneg.model.datums.Datum;
import benblamey.saesneg.phaseB.DatumPairSimilarity;
import benblamey.saesneg.phaseB.DatumSimilarityEvidence;
import benblamey.saesneg.phaseB.FestibusFeatures;
import com.benblamey.core.ListUtils;
import java.util.HashSet;
import java.util.List;

public class EventsStrategy extends Strategy {

    private final LifeStory _ls;

    public EventsStrategy(LifeStory ls) {
        _ls = ls;
    }

    @Override
    public void addEvidenceToPair(DatumPairSimilarity pair, PhaseBOptions options) {

        Datum left = pair.getLeft();
        Datum right = pair.getRight();

        List<SocialEventAnnotation> leftEvents = left.getAnnotations().SocialEventAnnotation;
        List<SocialEventAnnotation> rightEvents = right.getAnnotations().SocialEventAnnotation;

        if ((leftEvents.isEmpty()) || (rightEvents.isEmpty())) {
            // Comparison not possible if one datum has no annotations.
            return;
        }

        HashSet<Class<?>> leftEventTypes = new HashSet<>();
        for (SocialEventAnnotation e : leftEvents) {
            leftEventTypes.add(e.getClass());
        }

        HashSet<Class<?>> rightEventTypes = new HashSet<>();
        for (SocialEventAnnotation e : rightEvents) {
            rightEventTypes.add(e.getClass());
        }

        if ((leftEventTypes.size() != 1) || (rightEventTypes.size() != 1)) {
            // Comparison not possible if datums are of different types (e.g. wedding + birthday).
            return;
        }

        if (!ListUtils.first(leftEventTypes).equals(ListUtils.first(rightEventTypes))) {
            pair.addEvidence(new DatumSimilarityEvidence(FestibusFeatures.Events, -1, "events are different sorts"));
            return;
        }

        Class<?> annotationType = ListUtils.first(leftEventTypes);

        // Events being the same kind gives us a score of 0.5, this may be increased or decreased depending on whether the details (if known) match.
        double value = 0.5;
        String note = "";

        // To keep things simple, we compare metadata to produce a single value representing the similarity of metadata. +0.5 for a partial match, +1.0 for a full match.
        if (annotationType.equals(BirthdaySocialEventAnnotation.class)) {
            BirthdaySocialEventAnnotation leftBirthday = BirthdaySocialEventAnnotation
                    .merge(ListUtils
                            .<SocialEventAnnotation, BirthdaySocialEventAnnotation>
                            cast(leftEvents));
            BirthdaySocialEventAnnotation rightBirthday = BirthdaySocialEventAnnotation
                    .merge(ListUtils
                            .<SocialEventAnnotation, BirthdaySocialEventAnnotation>
                            cast(leftEvents));

            String ownerL = leftBirthday.getOwner();
            String ownerR = rightBirthday.getOwner();
            if (ownerL != null && ownerR != null && ownerL.equals(ownerR)) {
                // Names not matching doesn't really tell us very much -- could be nicknames for example.
                value += 0.25;
            }

            int leftAge = leftBirthday.getAge();
            int rightAge = rightBirthday.getAge();
            if (leftAge > 0 && rightAge > 0) {
                // If the age of both birthdays is known, and the ages are different -- this is strong negative evidence for event commonality.
                value = (leftAge == rightAge) && (leftAge > 0) ? value + 0.25 : -1.0;
            }

        } else if (annotationType.equals(WeddingSocialEventAnnotation.class)) {
            WeddingSocialEventAnnotation leftWedding = WeddingSocialEventAnnotation
                    .merge(ListUtils
                            .<SocialEventAnnotation, WeddingSocialEventAnnotation>
                            cast(leftEvents));
            WeddingSocialEventAnnotation rightWedding = WeddingSocialEventAnnotation
                    .merge(ListUtils
                            .<SocialEventAnnotation, WeddingSocialEventAnnotation>
                            cast(leftEvents));

            HashSet<String> leftPeople = new HashSet<>();
            leftPeople.add(leftWedding.getPartnerA());
            leftPeople.add(leftWedding.getPartnerB());
            leftPeople.remove(null); // Remove any null-people

            HashSet<String> rightPeople = new HashSet<>();
            rightPeople.add(rightWedding.getPartnerA());
            rightPeople.add(rightWedding.getPartnerB());
            rightPeople.remove(null); // Remove any null-people

            List<String> peopleGettingMarried = com.benblamey.core.ListUtils.intersection(leftPeople, rightPeople);

            value += ((double) peopleGettingMarried.size()) / 4.0; // In the best case, +0.5
        } else {
            throw new RuntimeException("event annotation type not known.");
        }

        if (value < -1 || value > +1) {
            throw new RuntimeException("value out of range");
        }
        pair.addEvidence(new DatumSimilarityEvidence(FestibusFeatures.Events, value, "events are same sort, metadata compared."));

    }

}
