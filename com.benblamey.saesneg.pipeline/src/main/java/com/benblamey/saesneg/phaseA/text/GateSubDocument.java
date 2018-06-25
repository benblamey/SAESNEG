package com.benblamey.saesneg.phaseA.text;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.Node;
import gate.corpora.DocumentContentImpl;
import gate.util.InvalidOffsetException;

public class GateSubDocument {

    public static final String ORIGINAL_OSN_AS = "Key"; // Magic name of AS that the Document Reset PR will leave intact.

    //private String _text;
    private Long _startOffset;
    private Long _endOffset;
    public Document _parentDocument;
    private boolean _isReadOnly;

    //private String network_id;
    //private String object_id;
    private final AnnotationSet _original_osn_annotations;

    private final String network_id;
    private final Long object_id;

    private boolean _was_added;

    public GateSubDocument(Document parentDocument, String network_id, Long object_id) throws InvalidOffsetException {

        _parentDocument = parentDocument;
        this.network_id = network_id;
        this.object_id = object_id;
        _original_osn_annotations = _parentDocument.getAnnotations(ORIGINAL_OSN_AS);

        _startOffset = parentDocument.getContent().size();
        _endOffset = _startOffset;
    }

    public void appendLine(String text, String source_field) throws InvalidOffsetException {
        if (_isReadOnly) {
            throw new RuntimeException("field is readonly: _text");
        }

        if (text == null || text.isEmpty()) {
            return;
        }

        Long start = _endOffset;
        Long end = _endOffset;

        if (!text.endsWith("\n")) {
            text += "\n";
        }

        //System.out.println("Appending at start/end " + start + " " + end);
        _parentDocument.edit(start, end, new DocumentContentImpl(text));

        int length = text.length();
        _endOffset += length;

        end = (start + length) - 1; // We don't include the trailing newline within the annotation.

        // Now we create an annotation to describe the origin of the text.
        this._parentDocument.getAnnotations(ORIGINAL_OSN_AS)
                .add(start, end, source_field, Factory.newFeatureMap());

        _was_added = true;
    }

    public void finalizeText() throws InvalidOffsetException {

        if (_was_added) {
            // Note that annotations are "wet" i.e. appending on the end extends the annotation.
            // We leave the trailing \n outside, so any subsequent text doesn't end up inside the annotation.
            Long endOffset = _endOffset - 1;

			// (Adding the datum annotation as a final step was the only simple solution to avoid adding extraneous data to document.)
            //System.out.println("Creating initial annotation with start/end of " + _startOffset + " " + endOffset);
            FeatureMap annoParams = Factory.newFeatureMap();
            annoParams.put("network_id", network_id);
            annoParams.put("object_id", object_id);
            _original_osn_annotations.add(_startOffset, endOffset, "datum", annoParams);

            _isReadOnly = true;
        }
    }

    // Methods that don't change the text.
    public AnnotationSet getSentences() {
        // Get the default annotation set, then get all "Sentence" annotations.
        return this._parentDocument.getAnnotations().getContained(_startOffset, _endOffset).get("Sentence");
    }

    public AnnotationSet getTokens() {
        AnnotationSet defaultAS = _parentDocument.getAnnotations("");
        return defaultAS.getContained(_startOffset, _endOffset).get("Token");
    }

    public void annotateToken(Node startNode, Node endNode, String annotationSet,
            String annotationName, FeatureMap features) {
        this._parentDocument.getAnnotations(annotationSet).add(startNode, endNode, annotationName, features);
    }

    public AnnotationSet getIntersectingAnnotations(String annotationSet) {
        return this._parentDocument
                .getAnnotations(annotationSet)
                .get(this._startOffset, this._endOffset);
    }

    public AnnotationSet getTokensForSentence(Annotation sentence) {
        AnnotationSet defaultAS = _parentDocument.getAnnotations("");
        return defaultAS.getContained(
                sentence.getStartNode().getOffset(),
                sentence.getEndNode().getOffset())
                .get("Token");
    }

}
