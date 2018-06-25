package com.benblamey.saesneg.phaseB.strategies;

import com.benblamey.saesneg.experiments.PhaseBOptions;
import com.benblamey.saesneg.model.datums.Datum;
import com.benblamey.saesneg.model.datums.DatumAlbum;
import com.benblamey.saesneg.model.datums.DatumPhoto;
import com.benblamey.saesneg.phaseB.DatumPairSimilarity;
import com.benblamey.saesneg.phaseB.DatumSimilarityEvidence;
import com.benblamey.saesneg.phaseB.FestibusFeatures;

public class UserHelpStrategy extends Strategy {

    @Override
    public void addEvidenceToPair(DatumPairSimilarity pair, PhaseBOptions options) {

        Datum left = pair.getLeft();
        Datum right = pair.getRight();

        if (left instanceof DatumPhoto && right instanceof DatumPhoto) {

            DatumPhoto leftPhoto = (DatumPhoto) left;
            DatumPhoto rightPhoto = (DatumPhoto) right;

            DatumAlbum leftAlbum = leftPhoto.getAlbum();
            DatumAlbum rightAlbum = rightPhoto.getAlbum();

            // In all these cases, assume a value of 0.
            if (leftAlbum == null) {
                return;
            } else if (rightAlbum == null) {
                return;
            } else if (leftAlbum.isStructural()) {
                return;
            } else if (rightAlbum.isStructural()) {
                return;
            }

            // Photos have the same album.
            pair.addEvidence(new DatumSimilarityEvidence(
                    FestibusFeatures.User_PhotosInSameAlbum,
                    leftAlbum.equals(rightAlbum) ? 1.0 : -1.0,
                    "User - photos in same album (or not)"
            ));

        }

    }

}
