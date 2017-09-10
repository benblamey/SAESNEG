package com.benblamey.evaluation.web;

import com.benblamey.saesneg.model.datums.Datum;
import java.util.ArrayList;
import java.util.List;

public class CompareClusterEventPairViewModel {

    public CompareClusterEventPairLabel label;

    // The view prefers two lists, with indices. Makes building the table easier.
    public final List<Datum> _datums = new ArrayList<>();
    public final List<CompareClusterDatumAccuracy> _labels = new ArrayList<>();

    public void put(Datum d, CompareClusterDatumAccuracy unmatched) {

        _datums.add(d);
        _labels.add(unmatched);

    }

}
