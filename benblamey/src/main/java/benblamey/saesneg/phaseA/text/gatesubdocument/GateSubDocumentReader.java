package benblamey.saesneg.phaseA.text.gatesubdocument;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.util.InvalidOffsetException;
import java.util.Iterator;

public class GateSubDocumentReader extends GateSubDocument {

    public GateSubDocumentReader(Document parentDocument, String network_id, Long object_id) throws InvalidOffsetException {
        super(parentDocument, network_id, object_id);

        FeatureMap constraints = Factory.newFeatureMap();
        constraints.put(GateSubDocument.NETWORK_ID, network_id);
        constraints.put(GateSubDocument.OBJECT_ID, object_id);

        //_original_osn_annotations.add(_startOffset, endOffset, "datum", annoParams);
        AnnotationSet annotationSet = _original_osn_annotations.get("datum", constraints);
        Iterator<Annotation> iterator = annotationSet.iterator();
        if (iterator.hasNext()) {
            Annotation anno = annotationSet.iterator().next();
            this._startOffset = anno.getStartNode().getOffset();
            this._endOffset = anno.getEndNode().getOffset();
        } else {
            // There is no text associated with this datum, so the annotation was never created.
            this._startOffset = 0L;
            this._endOffset = 0L;
        }
    }

}
