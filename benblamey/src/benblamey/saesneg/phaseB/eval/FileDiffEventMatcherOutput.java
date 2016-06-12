package benblamey.saesneg.phaseB.eval;

import benblamey.saesneg.model.Event;
import benblamey.saesneg.model.datums.Datum;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class FileDiffEventMatcherOutput implements IEventMatcherOutput {

    private BufferedWriter _aWriter;
    private BufferedWriter _bWriter;

    public FileDiffEventMatcherOutput(String aFile, String bFile) throws IOException {
        _aWriter = new BufferedWriter(new FileWriter(aFile));
        _bWriter = new BufferedWriter(new FileWriter(bFile));
    }

    public void close() throws IOException {
        _aWriter.close();
        _bWriter.close();
    }

    @Override
    public void newEventPair(Event a, Event b) throws IOException {
        print(a, _aWriter);
        print(b, _bWriter);
    }

    @Override
    public void writeGap() throws IOException {
        _aWriter.append("\n");
        _aWriter.append("--- Finished all matches. ---\n");
        _aWriter.append("\n");

        _bWriter.append("\n");
        _bWriter.append("--- Finished all matches. ---\n");
        _bWriter.append("\n");
    }

    private static void print(Event e, BufferedWriter bWriter) throws IOException {

        bWriter.append("============================================\n");
        if (e != null) {
            for (Datum d : e.getDatums()) {
                bWriter.append("\tDatum: " + d.getNetworkID() + "\n");
            }
        } else {
            bWriter.append("\tNo match.\n");
        }
        bWriter.append("--------------------------------------------\n");
    }

}
