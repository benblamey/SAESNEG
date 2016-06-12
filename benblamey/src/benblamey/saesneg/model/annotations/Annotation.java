package benblamey.saesneg.model.annotations;

public class Annotation {

    public String Note;
    public DataKind SourceDataKind;
    String _originalText;

    public String getOriginalText() {
        return _originalText;
    }

    public void setOriginalText(String text) {
        _originalText = text;
    }

    public void addNote(String string) {
        Note += string + "\n";
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + ", from:" + SourceDataKind + " text: "+_originalText;
    }

    
}
