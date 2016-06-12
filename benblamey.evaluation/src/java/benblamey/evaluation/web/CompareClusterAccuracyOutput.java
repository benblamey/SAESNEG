package benblamey.evaluation.web;

import com.benblamey.core.ListUtils;
import benblamey.saesneg.model.Event;
import benblamey.saesneg.model.datums.Datum;
import benblamey.saesneg.phaseB.eval.IEventMatcherOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * An alternative means of inspecting event clustering output thru means of the
 * web interface.
 *
 * @author ben-laptop
 */
public class CompareClusterAccuracyOutput implements IEventMatcherOutput {

    public final List<CompareClusterEventPairViewModel> viewModels = new ArrayList<>();

    @Override
    public void newEventPair(Event groundTruth, Event computed) throws IOException {
        CompareClusterEventPairViewModel vm = new CompareClusterEventPairViewModel();
        if ((groundTruth != null) && (computed != null)) {
            List<Datum> union = ListUtils.union(groundTruth.getDatums(), computed.getDatums());
            vm.label = CompareClusterEventPairLabel.Pair;
            for (Datum d : union) {
                CompareClusterDatumAccuracy computeLabel = computeLabel(groundTruth, computed, d);
                vm.put(d, computeLabel);
            }
        } else if (groundTruth != null) {
            vm.label = CompareClusterEventPairLabel.GroundTruthOnly;
            for (Datum d : groundTruth.getDatums()) {
                vm.put(d, CompareClusterDatumAccuracy.Unmatched);
            }
        } else if (computed != null) {
            for (Datum d : computed.getDatums()) {
                vm.put(d, CompareClusterDatumAccuracy.Unmatched);
            }
            vm.label = CompareClusterEventPairLabel.ComputedOnly;
        }
        viewModels.add(vm);
    }

    private CompareClusterDatumAccuracy computeLabel(Event groundTruth, Event computed, Datum d) {
        if (groundTruth.getDatums().contains(d)) {
            if (computed.getDatums().contains(d)) {
                return CompareClusterDatumAccuracy.Correct;
            } else {
                // Not contained in computed.
                return CompareClusterDatumAccuracy.Missing;
            }
        } else {
            // Not contained in ground truth - shouldn't be in computed.
            return CompareClusterDatumAccuracy.Extra;
        }
    }

    @Override
    public void writeGap() throws IOException {
        // Nothing to do.
    }

}
