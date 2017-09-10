package com.benblamey.saesneg.phaseB.strategies;

import com.benblamey.saesneg.experiments.PhaseBOptions;
import com.benblamey.saesneg.model.annotations.ImageContentAnnotation;
import com.benblamey.saesneg.model.datums.Datum;
import com.benblamey.saesneg.phaseB.DatumPairSimilarity;
import com.benblamey.saesneg.phaseB.DatumSimilarityEvidence;
import com.benblamey.saesneg.phaseB.FestibusFeatures;

public class SceneStrategy extends Strategy {

    @Override
    public void addEvidenceToPair(DatumPairSimilarity pair, PhaseBOptions options) {

        Datum left = pair.getLeft();
        Datum right = pair.getRight();

        if (left.getAnnotations().ImageContentAnnotations.isEmpty() || right.getAnnotations().ImageContentAnnotations.isEmpty()) {
            return;
        }

        ImageContentAnnotation leftAnno = left.getAnnotations().ImageContentAnnotations.get(0);
        ImageContentAnnotation rightAnno = right.getAnnotations().ImageContentAnnotations.get(0);

        // Distances are in range [0,+inf]
        Float scColor = leftAnno.getScalableColor().getDistance(rightAnno.getScalableColor());
        Float colorLayout = leftAnno.getColorLayout().getDistance(rightAnno.getColorLayout());
	Float edgeHistogram = leftAnno.getEdgeHistogram().getDistance(rightAnno.getEdgeHistogram());

//        // Hand-crafted rules.
//        if (colorLayout == 0 || scColor == 0) {
//            pair.addEvidence(new DatumSimilarityEvidence(
//                    FestibusFeatures.Scene_identicalPhoto,
//                    1,
//                    "Photos are likely identical."));
//        }

        pair.addEvidence(new DatumSimilarityEvidence(
                FestibusFeatures.Scene_ColorLayout,
                1.0/(1.0 + scColor) // distance of zero means descriptors are similar, images are similar. larger descriptors mean less similar.
        ));

        pair.addEvidence(new DatumSimilarityEvidence(
                FestibusFeatures.Scene_scColor,
                1.0/(1.0 + scColor) // distance of zero means descriptors are similar, images are similar. larger descriptors mean less similar.
        ));

        pair.addEvidence(new DatumSimilarityEvidence(
                FestibusFeatures.Scene_edgeHistogram,
                1.0/(1.0 + edgeHistogram) // distance of zero means descriptors are similar, images are similar. larger descriptors mean less similar.
        ));
    }

}
