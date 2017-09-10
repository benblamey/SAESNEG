package com.benblamey.saesneg.phaseA.text.eval;

import com.benblamey.core.GATE.GateUtils2;
import com.benblamey.saesneg.phaseA.text.nerpaper.OSMExperimentMain;
import gate.Annotation;
import gate.Document;
import gate.util.InvalidOffsetException;
import java.util.ArrayList;
import java.util.List;

public class GATEAnnotationEvaluator {

    private Document _keyDoc;
    private String _keyAnnotationSet;
    private Document _responseDoc;
    private String _responseAnnotationSet;
    private String _annotation;

    public GATEAnnotationEvaluator(Document keyDoc, String keyAnnotationSet, Document responseDoc, String responseAnnotationSet, String annotation) {
        this._keyDoc = keyDoc;
        this._keyAnnotationSet = keyAnnotationSet;
        this._responseDoc = responseDoc;
        this._responseAnnotationSet = responseAnnotationSet;
        this._annotation = annotation;
    }

    public void run() throws InvalidOffsetException {
        // Check document consistency.

        Document a = this._keyDoc;
        Document b = this._responseDoc;

        GateUtils2.quickCheckDocContentIdentical(a, b);

        List<Annotation> goldLocations = GateUtils2.getSortedAnnotations(_keyDoc.getAnnotations(this._keyAnnotationSet).get(this._annotation));
        List<Annotation> respLocations = GateUtils2.getSortedAnnotations(_responseDoc.getAnnotations(this._responseAnnotationSet).get(this._annotation));

        // Organize the annotations into tuples.
        List<GATEAnnotationTuple> tuples = toTuples(goldLocations, respLocations);

        labelTuples(tuples);
    }

    private void labelTuples(List<GATEAnnotationTuple> tuples) {

        for (GATEAnnotationTuple tuple : tuples) {

            AnnotationTupleLabel label;

            if (tuple.gold != null) {
                long goldID = OSMExperimentMain.getOSMIDFromAnnotation(tuple.gold);

                if (goldID < 0) {
                    label = AnnotationTupleLabel.GoldExclude;
                } else if (tuple.resps.size() == 0) {
                    label = AnnotationTupleLabel.GoldMissingResponse;
                } else if (tuple.resps.size() == 1) {
                    Annotation response = tuple.resps.get(0);
                    long respID = OSMExperimentMain.getOSMIDFromAnnotation(response);

                    throw new RuntimeException("not implemented");

                } else {
                    label = AnnotationTupleLabel.GoldMultipleResponse;
                }
            } else {
                // No gold
                label = AnnotationTupleLabel.NoGold;
            }

            tuple.label = label;
        }

    }

    private List<GATEAnnotationTuple> toTuples(List<Annotation> goldLocations, List<Annotation> respLocations) {
        List<GATEAnnotationTuple> tuples = new ArrayList<GATEAnnotationTuple>();

        int goldIndex = 0;
        int respIndex = 0;
        while ((goldIndex < goldLocations.size()) || (respIndex < respLocations.size())) {

            // Get the annotations and tuples if we can.
            Annotation gold = null, resp = null;
            if (goldIndex < goldLocations.size()) {
                gold = goldLocations.get(goldIndex);
            }
            if (respIndex < respLocations.size()) {
                resp = respLocations.get(respIndex);
            }
            GATEAnnotationTuple tuple = null;
            if (tuples.size() > 0) {
                tuple = tuples.get(tuples.size() - 1);
            }

            // Pick which annotation we are going to process next.
            Annotation next;
            if (gold == null) {
                next = resp;
            } else if (resp == null) {
                next = gold;
            } else {
                long goldStart = gold.getStartNode().getOffset();
                long respStart = resp.getStartNode().getOffset();
                next = (goldStart < respStart) ? gold : resp;
            }

            // Now decide whether we are beginning a tuple or appending to an existing one.
            boolean addTuple; // True to start a new tuple, false to add to the existing one.

            if (tuple == null) {
                // We don't have a tuple at the moment, we need to add a new one.
                addTuple = true;
            } else {
                Long tupleEnd = tuple.getEnd();
                Long nextStart = next.getStartNode().getOffset();

                // Begin a new tuple if we are not touching.
                addTuple = (nextStart > tupleEnd);
            }

            if (addTuple) {
                tuple = new GATEAnnotationTuple();
                tuples.add(tuple);
            }

            if (next == gold) {
                if (tuple.gold != null) {
                    throw new RuntimeException("We can't have more than one gold annotation in a tuple.");
                }
                tuple.gold = next;
                goldIndex++;
            } else {
                tuple.resps.add(next);
                respIndex++;
            }
        }

        return tuples;
    }

}
