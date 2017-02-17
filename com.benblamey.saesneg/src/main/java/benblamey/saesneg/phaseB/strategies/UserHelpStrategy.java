package benblamey.saesneg.phaseB.strategies;

import benblamey.saesneg.experiments.PhaseBOptions;
import benblamey.saesneg.model.datums.Datum;
import benblamey.saesneg.model.datums.DatumAlbum;
import benblamey.saesneg.model.datums.DatumPhoto;
import benblamey.saesneg.phaseB.DatumPairSimilarity;
import benblamey.saesneg.phaseB.DatumSimilarityEvidence;
import benblamey.saesneg.phaseB.FestibusFeatures;

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
