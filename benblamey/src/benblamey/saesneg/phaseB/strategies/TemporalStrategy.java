package benblamey.saesneg.phaseB.strategies;

import benblamey.saesneg.experiments.PhaseBOptions;
import benblamey.saesneg.model.annotations.TemporalAnnotation;
import benblamey.saesneg.model.datums.Datum;
import benblamey.saesneg.phaseB.DatumPairSimilarity;
import benblamey.saesneg.phaseB.DatumSimilarityEvidence;
import benblamey.saesneg.phaseB.FestibusFeatures;
import edu.stanford.nlp.time.distributed.AnnualUniformDistribution;
import edu.stanford.nlp.time.distributed.IntersectTimeExpression;
import edu.stanford.nlp.time.distributed.SumTimeExpression;
import edu.stanford.nlp.time.distributed.TimeDensityFunction;
import java.util.ArrayList;
import java.util.List;
import org.joda.time.Days;

public class TemporalStrategy extends Strategy {

    @Override
    public void addEvidenceToPair(DatumPairSimilarity pair, PhaseBOptions b) {
        Datum left = pair.getLeft();
        Datum right = pair.getRight();

        // Feature is shared with Distributed Temporal Feature.
        if (b.UseDistributedTemporalSimilarity) {
            List<TemporalAnnotation> leftTimes;
                    
            TimeDensityFunction timeOfLeftEvent = getCombinedTimeForEvent(left);
            TimeDensityFunction timeOfRightEvent = getCombinedTimeForEvent(right);
            Double score = TimeDensityFunction.getSimilarity(timeOfLeftEvent, timeOfRightEvent);
            
            String note = "intersection between two probability distributions";
            
            if (Double.isNaN(score) || Double.isInfinite(score)) {
                note = "Answer was " + Double.toString(score) + " -- using 0.0 instead.";
                score = 0.0;
            }

            // This needs to be normalied!!!
            pair.addEvidence(new DatumSimilarityEvidence(
                    FestibusFeatures.Temporal,
                    score,
                    note));

        } else {
            int daysBetween = Days.daysBetween(left.getContentAddedDateTime(),
                    right.getContentAddedDateTime()).getDays();

            // We want the magnitude.
            if (daysBetween < 0) {
                daysBetween = -daysBetween;
            }
            
            double score = 1.0 / (1.0 + daysBetween);

            pair.addEvidence(new DatumSimilarityEvidence(
                    FestibusFeatures.Temporal,
                    score,
                    "content uploaded at similar time - days=" + daysBetween));
        }
    }

    private TimeDensityFunction getCombinedTimeForEvent(Datum datum) {
        
        List<TimeDensityFunction> terms = new ArrayList<TimeDensityFunction>();
        for (TemporalAnnotation timeAnno : datum.getAnnotations().DateTimesAnnotations) {
            TimeDensityFunction term = timeAnno.getDensity();
            
            if (term != null) {
                if (!timeAnno.isDefinitive) {
                    // Assume 50% likelihood that temporal information matches the event.
                    term = new SumTimeExpression(new AnnualUniformDistribution(1), term);
                }
                // else: Definitive temporal information -- the event must match the tempex.
                terms.add(term);
            } else {
                System.out.println("term: " + timeAnno + " is null.");
            }
            
        }
        
        if (terms.isEmpty()){
            // Return a uniform distribution if there is no other temporal information.
            return new AnnualUniformDistribution(1);
        } else {
            return new IntersectTimeExpression(terms);
        }

    }

}
