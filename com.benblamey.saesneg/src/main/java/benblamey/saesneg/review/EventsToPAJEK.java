package benblamey.saesneg.review;

import benblamey.saesneg.model.Event;
import benblamey.saesneg.model.UserContext;
import benblamey.saesneg.model.datums.Datum;
import benblamey.saesneg.phaseB.old.EventSimilarity_OLD;
import com.benblamey.core.pajek.Edge;
import com.benblamey.core.pajek.Network;
import com.benblamey.core.pajek.Vertex;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.List;

public class EventsToPAJEK {

    public static void WriteToNETFile(UserContext user) throws UnsupportedEncodingException, UnsupportedEncodingException, FileNotFoundException, IOException, IOException {

        List<Event> events = user.getLifeStory().EventsGolden;

        Network n = new Network();
        EventSimilarity_OLD eventSimilarity = new EventSimilarity_OLD(user);
        for (int i = 0; i < events.size(); i++) {
            Event event_i = events.get(i);
            Datum obj_i = event_i.getDatums().get(0);
            n.getVertices().add(new Vertex(obj_i.getNetworkID().toString()));
        }
        for (int i = 0; i < events.size(); i++) {
            Event event_i = events.get(i);
            for (int j = 0; j < i; j++) {
                Event event_j = events.get(j);
                double compareEvent = eventSimilarity.CompareEvent(event_i, event_j);
                if (compareEvent > 0) {
                    Edge edge = new Edge(n.getVertices().get(i), n.getVertices().get(j), compareEvent);
                    n.getEdges().add(edge);
                }
            }
        }
        Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(user.getOutputDirectoryWithTrailingSlash() + "people.net"), "ASCII"));
        n.WriteToNetFile(out);
        out.close();
    }
}
