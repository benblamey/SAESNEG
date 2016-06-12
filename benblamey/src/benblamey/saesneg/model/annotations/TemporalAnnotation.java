package benblamey.saesneg.model.annotations;

import edu.stanford.nlp.time.Timex;
import edu.stanford.nlp.time.distributed.TimeDensityFunction;

public class TemporalAnnotation extends Annotation {

    String _text;
    Timex _timex;
    TimeDensityFunction _density;

    public void setDensity(TimeDensityFunction _density) {
        this._density = _density;
    }

    /**
     * The distributed, probability-density based representation of a temporal
     * expression.
     *
     * @return
     */
    public TimeDensityFunction getDensity() {
        return _density;
    }

    public void setText(String _text) {
        this._text = _text;
    }

    public String getText() {
        return _text;
    }

    /**
     * The Stanford NLP interval-based representation of time, following the
     * convention of a TIMEX3 XML representation.
     *
     * @param _timex
     */
    public void setTimex(Timex _timex) {
        this._timex = _timex;
    }

    public Timex getTimex() {
        return _timex;
    }
    
    public boolean isDefinitive;

    @Override
    public String toString() {
        return ((_timex != null)  ? _timex.toString() : "") + " " + ((this._density != null) ? this._density.toString() : "");
    }
    
    
}
