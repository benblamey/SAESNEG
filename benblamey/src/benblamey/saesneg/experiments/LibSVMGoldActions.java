package benblamey.saesneg.experiments;

public enum LibSVMGoldActions {

    /**
     * Run cross-validation separately on users (x-validated within libsvm)
     */
    CrossValidate_Individual_Internal,
    /**
     * Do cross validation -- but actually run full pipeline on others.
     */
    CrossRun_Individual,
    /**
     * Export the edge cases aggregated for all users, for later training or
     * X-validation.
     */
    ExportAllUserCases,
    /**
     * Export the edge cases aggregated for all users and execute training.
     */
    ExportAllUserCases_AndTrain,
    None,

}
