package benblamey.saesneg.model.annotations;

import at.lux.imageanalysis.VisualDescriptor;

public class ImageContentAnnotation extends Annotation {

    private VisualDescriptor _scalableColor;
    private VisualDescriptor _edgeHistorgram;
    private VisualDescriptor _colorLayout;

    public VisualDescriptor getScalableColor() {
        return _scalableColor;
    }

    public void setScalableColor(VisualDescriptor scalableColor) {
        _scalableColor = scalableColor;
    }

    public VisualDescriptor getEdgeHistogram() {
        return _edgeHistorgram;
    }

    public void setEdgeHistogram(VisualDescriptor edgeHistorgram) {
        _edgeHistorgram = edgeHistorgram;
    }

    public VisualDescriptor getColorLayout() {
        return _colorLayout;
    }

    public void setColorLayout(VisualDescriptor colorLayout) {
        _colorLayout = colorLayout;
    }

}
