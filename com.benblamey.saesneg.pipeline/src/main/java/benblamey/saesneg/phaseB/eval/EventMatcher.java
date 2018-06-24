package com.benblamey.saesneg.phaseB.eval;

import com.benblamey.saesneg.model.Event;
import com.benblamey.core.ListUtils;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EventMatcher {

    public static void run(IEventMatcherOutput eventPairs, Collection<Event> eventsComputed, Collection<Event> eventsComputed2) throws IOException {

        List<JaccardEventSimilarity> sims = new ArrayList<>();

        for (Event a : eventsComputed) {
            for (Event b : eventsComputed2) {
                sims.add(new JaccardEventSimilarity(a, b));
            }
        }

        // Sort the pairs according to similarity.
        Collections.sort(sims, new Comparator<JaccardEventSimilarity>() {

            @Override
            public int compare(JaccardEventSimilarity o1, JaccardEventSimilarity o2) {
                return -Double.compare(o1.Jaccard, o2.Jaccard);
            }

        });

        List<Event> aToWrite = new ArrayList<>(eventsComputed);
        List<Event> bToWrite = new ArrayList<>(eventsComputed2);

        for (JaccardEventSimilarity jacc : sims) {
            if (aToWrite.contains(jacc.A) && bToWrite.contains(jacc.B)) {
                aToWrite.remove(jacc.A);
                bToWrite.remove(jacc.B);
                eventPairs.newEventPair(jacc.A, jacc.B);
            }
        }

        eventPairs.writeGap();

        for (Event a : aToWrite) {
            eventPairs.newEventPair(a, null);
        }

        for (Event b : bToWrite) {
            eventPairs.newEventPair(null, b);
        }
    }

    static class JaccardEventSimilarity {

        public Event A;
        public Event B;
        public Double Jaccard;

        public JaccardEventSimilarity(Event a, Event b) {
            A = a;
            B = b;
            Jaccard = (double) ListUtils.intersection(a.getDatums(), b.getDatums()).size()
                    / (double) ListUtils.union(a.getDatums(), b.getDatums()).size();
        }
    }

}
