package com.benblamey.evaluation.web;

import com.benblamey.saesneg.model.Event;
import com.benblamey.saesneg.model.LifeStory;
import java.util.Comparator;
import java.util.List;

public class GroundTruthLifeStoryViewModel {

    private LifeStory _lifeStory;

    public GroundTruthLifeStoryViewModel(LifeStory lifeStory) {
        _lifeStory = lifeStory;

        // Remove any empty events.
        for (int i = _lifeStory.EventsGolden.size() - 1; i >= 0; i--) {
            if (_lifeStory.EventsGolden.get(i).getDatums().size() == 0) {
                _lifeStory.EventsGolden.remove(i);
            }
        }

        java.util.Collections.sort(_lifeStory.EventsGolden, new Comparator<Event>() {

            public int compare(Event o1, Event o2) {

                // Both normal events.
                // Sort by event ID to retain original UI ordering.
                String o1name = o1.getUserEditableName();
                String o2name = o2.getUserEditableName();

                if (o1name == null) {
                    o1name = "";
                }
                if (o2name == null) {
                    o2name = "";
                }

                return o1name.compareTo(o2name);

            }
        ;

    }

    );


	}

	public List<Event> getEvents() {
        return this._lifeStory.EventsGolden;
    }

}
