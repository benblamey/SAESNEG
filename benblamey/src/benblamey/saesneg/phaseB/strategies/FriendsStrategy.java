package benblamey.saesneg.phaseB.strategies;

import benblamey.saesneg.experiments.PhaseBOptions;
import benblamey.saesneg.model.LifeStory;
import benblamey.saesneg.model.UserContext;
import benblamey.saesneg.model.annotations.PersonAnnotation;
import benblamey.saesneg.model.datums.Datum;
import benblamey.saesneg.phaseB.DatumPairSimilarity;
import benblamey.saesneg.phaseB.DatumSimilarityEvidence;
import benblamey.saesneg.phaseB.FestibusFeatures;
import com.benblamey.core.ListUtils;
import java.util.HashSet;
import java.util.List;

public class FriendsStrategy extends Strategy {

    private String _theUserID;
    
    public FriendsStrategy(LifeStory ls, UserContext user) {
        _theUserID = user.ID;
    }

    @Override
    public void addEvidenceToPair(DatumPairSimilarity pair, PhaseBOptions options) {
        Datum left = pair.getLeft();
        Datum right = pair.getRight();
        pair.addEvidence(createFriendsEvidence(left, right));
    }

    private DatumSimilarityEvidence createFriendsEvidence(Datum left, Datum right) {

        List<PersonAnnotation> peopleAtLeft = left.getAnnotations().People;
        List<PersonAnnotation> peopleAtRight = left.getAnnotations().People;

        HashSet<String> facebookIDsLeft = new HashSet<>();
        HashSet<String> facebookIDsRight = new HashSet<>();
        
        for (PersonAnnotation pae : peopleAtLeft) {
            facebookIDsLeft.add(pae.getFacebookID());
        }

        for (PersonAnnotation pae : peopleAtRight) {
            facebookIDsRight.add(pae.getFacebookID());
        }

        List<String> intersection = ListUtils.intersection(facebookIDsLeft, facebookIDsRight);
        
        // Ensure that the owner is always in the intersection.
        if (!intersection.contains(_theUserID)) {
            intersection.add(_theUserID);
        }
                
        return new DatumSimilarityEvidence(
                FestibusFeatures.Friends_InCommon,
                1.0 - 1.0/intersection.size(),
                "");
    }

}
