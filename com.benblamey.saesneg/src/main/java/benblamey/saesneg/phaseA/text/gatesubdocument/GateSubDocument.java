package com.benblamey.saesneg.phaseA.text.gatesubdocument;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.FeatureMap;
import gate.Node;
import gate.util.InvalidOffsetException;

public class GateSubDocument {

    public static final String ORIGINAL_OSN_AS = "Key"; // Magic name of AS that the Document Reset PR will leave intact.

    /**
     * Name of feature which identifies the network of the annotation, e.g.
     * "FB".
     */
    protected static final String NETWORK_ID = "network_id";

    /**
     * Name of the feature which identifies the network-specific ID of the
     * datum, e.g. "2342344".
     */
    protected static final String OBJECT_ID = "object_id";

    protected Long _startOffset;
    protected Long _endOffset;

    public final Document ParentDocument;
    protected final AnnotationSet _original_osn_annotations;
    protected final String _network_id;
    protected final Long _object_id;

    protected GateSubDocument(Document parentDocument, String network_id, Long object_id) throws InvalidOffsetException {

        ParentDocument = parentDocument;
        _network_id = network_id;
        _object_id = object_id;
        _original_osn_annotations = ParentDocument.getAnnotations(ORIGINAL_OSN_AS);

        _startOffset = parentDocument.getContent().size();
        _endOffset = _startOffset;
    }

    // Methods that don't change the text.
    public AnnotationSet getSentences() {
        // Get the default annotation set, then get all "Sentence" annotations.
        return this.ParentDocument.getAnnotations().getContained(_startOffset, _endOffset).get("Sentence");
    }

    public AnnotationSet getTokens() {
        AnnotationSet defaultAS = ParentDocument.getAnnotations("");
        return defaultAS.getContained(_startOffset, _endOffset).get("Token");
    }

    public void annotateToken(Node startNode, Node endNode, String annotationSet,
            String annotationName, FeatureMap features) {
        this.ParentDocument.getAnnotations(annotationSet).add(startNode, endNode, annotationName, features);
    }

    public AnnotationSet getIntersectingAnnotations(String annotationSet) {
        return this.ParentDocument
                .getAnnotations(annotationSet)
                .get(this._startOffset, this._endOffset);
    }

    public AnnotationSet getTokensForSentence(Annotation sentence) {
        AnnotationSet defaultAS = ParentDocument.getAnnotations("");
        return defaultAS.getContained(
                sentence.getStartNode().getOffset(),
                sentence.getEndNode().getOffset())
                .get("Token");
    }

    public String getText() throws InvalidOffsetException {
        return this.ParentDocument.getContent().getContent(_startOffset, _endOffset).toString();
    }

}
