package com.benblamey.saesneg.phaseA.text.eval;

import gate.Annotation;
import java.util.ArrayList;

/**
 * Represents a pair of annotations from different annotations for the purposes
 * of their comparison.
 *
 * The tuple is asymetric, there will be no more than one gold annotation, but
 * there may be multiple 'response' annotations.
 *
 * Remember that annotations might come from different documents (copies of the
 * same document).
 *
 * @author Ben
 */
public class GATEAnnotationTuple {

    public Annotation gold;
    public final ArrayList<Annotation> resps = new ArrayList<>();
    public AnnotationTupleLabel label;

    public long getEnd() {
        long end = -1;

        if (gold != null) {
            end = Math.max(end, gold.getEndNode().getOffset());
        }

        for (Annotation anno : resps) {
            end = Math.max(end, anno.getEndNode().getOffset());
        }

        return end;
    }

}
