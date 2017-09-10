package com.benblamey.saesneg.phaseB.eval;

import com.benblamey.saesneg.model.Event;
import java.io.IOException;

public interface IEventMatcherOutput {

    public void newEventPair(Event groundTruth, Event computed) throws IOException;

    public void writeGap() throws IOException;

}
