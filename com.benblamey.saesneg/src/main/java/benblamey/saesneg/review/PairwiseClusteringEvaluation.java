package com.benblamey.saesneg.review;

import com.benblamey.saesneg.experiments.Experiment;
import com.benblamey.saesneg.model.UserContext;
import com.benblamey.saesneg.phaseB.DatumPairSimilarity;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PairwiseClusteringEvaluation {

    private Experiment _experiment;

    public PairwiseClusteringEvaluation(Experiment experiment) {
        _experiment = experiment;
    }

    public void evaluatePairwise(UserContext user) throws IOException {

        int pairIntraCorrect = 0;
        int pairIntraIncorrect = 0;
        int pairInterCorrect = 0;
        int pairInterIncorrect = 0;


        for (DatumPairSimilarity pair : user.getLifeStory().goldenPairs) {

            if ((pair.classificationResult.mostLikelyClass != DatumPairSimilarity.SVM_CLASS_DIFFERENT_EVENT)
                    && (pair.classificationResult.mostLikelyClass != DatumPairSimilarity.SVM_CLASS_SAME_EVENT)) {
                throw new RuntimeException("classification result of edge is unknown");
            }

            if (pair.goldIsInSameEvent) { // INTRA
                switch (pair.classificationResult.mostLikelyClass) {
                    case DatumPairSimilarity.SVM_CLASS_SAME_EVENT:
                        pairIntraCorrect++;
                        break;
                    case DatumPairSimilarity.SVM_CLASS_DIFFERENT_EVENT:
                        pairIntraIncorrect++;
                        break;
                    default:
                        throw new RuntimeException("classification result invalid");
                }
            } else { // INTER
                switch (pair.classificationResult.mostLikelyClass) {
                    case DatumPairSimilarity.SVM_CLASS_SAME_EVENT:
                        pairInterIncorrect++;
                        break;
                    case DatumPairSimilarity.SVM_CLASS_DIFFERENT_EVENT:
                        pairInterCorrect++;
                        break;
                    default:
                        throw new RuntimeException("classification result invalid");
                }
            }
        }

        double accuracy = ((double) (pairInterCorrect + pairIntraCorrect)) / (pairInterCorrect + pairInterIncorrect + pairIntraCorrect + pairIntraIncorrect);

        Map<String, Integer> userPairwiseResults = new HashMap<>();
        userPairwiseResults.put("pairInterCorrect", pairInterCorrect);
        userPairwiseResults.put("pairInterIncorrect", pairInterIncorrect);
        userPairwiseResults.put("pairIntraCorrect", pairIntraCorrect);
        userPairwiseResults.put("pairIntraIncorrect", pairIntraIncorrect);

        _experiment.Results.PairwiseResults.put(user.ID,userPairwiseResults);

        _experiment.LogFile.println(user.getName());
        _experiment.LogFile.println("Clustering accuracy (pairwise correct) = " + accuracy);
        _experiment.LogFile.println("no. of datums = " + user.getLifeStory().goldDatums.size());
        _experiment.LogFile.println("no. of computed events = " + user.getLifeStory().EventsComputed.size());
        _experiment.LogFile.println("no. of ground truth events = " + user.getLifeStory().EventsGolden.size());

    }
}
