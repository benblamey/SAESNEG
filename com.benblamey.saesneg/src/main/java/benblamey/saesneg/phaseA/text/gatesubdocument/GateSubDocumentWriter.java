package benblamey.saesneg.phaseA.text.gatesubdocument;

import gate.Document;
import gate.Factory;
import gate.FeatureMap;
import gate.corpora.DocumentContentImpl;
import gate.util.InvalidOffsetException;

public class GateSubDocumentWriter extends GateSubDocument {

    private boolean _isReadOnly;
    private boolean _was_added;

    public GateSubDocumentWriter(Document parentDocument, String network_id, Long object_id) throws InvalidOffsetException {
        super(parentDocument, network_id, object_id);
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
        ParentDocument.edit(start, end, new DocumentContentImpl(text));

        int length = text.length();
        _endOffset += length;

        end = (start + length) - 1; // We don't include the trailing newline within the annotation.

        // Now we create an annotation to describe the origin of the text.
        this.ParentDocument.getAnnotations(ORIGINAL_OSN_AS)
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
            annoParams.put(NETWORK_ID, _network_id);
            annoParams.put(OBJECT_ID, _object_id);
            _original_osn_annotations.add(_startOffset, endOffset, "datum", annoParams);

            _isReadOnly = true;
        }
    }

}
