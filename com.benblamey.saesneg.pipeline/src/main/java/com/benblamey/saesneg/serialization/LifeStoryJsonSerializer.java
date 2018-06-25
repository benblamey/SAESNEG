package com.benblamey.saesneg.serialization;

import com.benblamey.saesneg.model.Event;
import com.benblamey.saesneg.model.LifeStory;
import com.benblamey.saesneg.model.datums.Datum;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import socialworld.model.SocialWorldUser;

public class LifeStoryJsonSerializer {

    public static final String GROUND_TRUTH_EVENTS = "GROUND_TRUTH_EVENTS";

    public static String getLifeStoryFileNameMatchingGroundTruth(SocialWorldUser user) {
        BasicDBObject groundTruth = (BasicDBObject) user.getValue(GROUND_TRUTH_EVENTS);
        if (groundTruth == null) {
            return null;
        }

        String filename = groundTruth.getString("lifeStoryFileName");
        if (filename == null) {
            System.out.println("lifestory file name missing from ground truth JSON");
            return null;
        }
        return filename;
    }

    public static List<Event> getGroundTruthEvents(LifeStory ls, SocialWorldUser user) {

        ArrayList<Event> events = new ArrayList<>();

        BasicDBObject groundTruth = (BasicDBObject) user.getValue(GROUND_TRUTH_EVENTS);
        if (groundTruth != null) {
            BasicDBList groundTruthList = (BasicDBList) groundTruth.get("events");
            if (groundTruthList != null) {
                for (Object groundTruthEventObj : groundTruthList) {
                    Event e = new Event((BasicDBObject) groundTruthEventObj, ls);
                    events.add(e);
                }
            }
        }

        LifeStory.RemoveDuplicateDatums(events);

        return events;
    }

    public static String eventsToJson(Collection<Event> eventsComputed) {

        BasicDBList jsonEvents = new BasicDBList();

        for (Event e : eventsComputed) {
            BasicDBList jsonEvent = new BasicDBList();
            for (Datum d : e.getDatums()) {
                jsonEvent.add(d.getNetworkID());
            }
            jsonEvents.add(jsonEvent);
        }

        return jsonEvents.toString();
    }

    public static BasicDBObject cleanUpEvents(BasicDBObject foo) {
        BasicDBList events = (BasicDBList) foo.get("events");

        HashSet<String> datumIDs = new HashSet<String>();

        for (int i = events.size() - 1; i >= 0; i--) {
            BasicDBObject event = (BasicDBObject) events.get(i);

            BasicDBList datums = (BasicDBList) event.get("datums");
            // We trim any "empty" events.
            if (datums.size() == 0) {
                events.remove(i);
                continue;
            }

            // Remove any duplicate datums.
            for (int j = datums.size() - 1; j >= 0; j--) {
                BasicDBObject datum = (BasicDBObject) datums.get(j);
                String id = (String) datum.get("id");
                if (datumIDs.contains(id)) {
                    datums.remove(j);
                }
            }
        }

        return foo;
    }
}
