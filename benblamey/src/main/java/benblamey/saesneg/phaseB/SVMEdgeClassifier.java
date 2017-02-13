package benblamey.saesneg.phaseB;

import com.benblamey.core.classifier.svm.LibSVM;
import com.benblamey.core.classifier.svm.SvmClassificationProbResult;
import com.benblamey.core.classifier.svm.SvmFile;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang.NullArgumentException;

public class SVMEdgeClassifier implements IEdgeClassifier {

    private String _model;

    public SVMEdgeClassifier(String model) {
        if (model == null) {
            throw new NullArgumentException("model");
        }
        _model = model;
    }


    @Override
    public void computePairSimilarity(List<DatumPairSimilarity> similarities, String outputFile) {

        if (similarities.isEmpty()) {
            System.out.println("Asking to compute similarities for empty set of edges!");
        }

        SvmFile file = createSvmFileForPairs(similarities, outputFile);

        List<SvmClassificationProbResult> predictWithProbs = LibSVM.predictWithProbs(file.getPath(), _model, outputFile+".output");

        //createTempFile.delete();
        for (int i = 0; i < predictWithProbs.size(); i++) {
            SvmClassificationProbResult result = predictWithProbs.get(i);
            similarities.get(i).classificationResult = result;
        }
    }

    private static SvmFile createSvmFileForPairs(List<DatumPairSimilarity> similarities, String filename) {

        SvmFile file;
        try {
            file = new SvmFile(filename, FestibusFeatures.values().length);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (DatumPairSimilarity pair : similarities) {
            // We include the (ground truth) case label here because LibSVM will automatically analyze the results.
            pair.addCaseToSvmFile(file, pair.goldIsInSameEvent ? DatumPairSimilarity.SVM_CLASS_SAME_EVENT : DatumPairSimilarity.SVM_CLASS_DIFFERENT_EVENT);
        }

        try {
            file.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }


}
