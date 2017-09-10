package com.benblamey.saesneg.model;

import com.benblamey.saesneg.model.datums.Datum;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.xml.bind.annotation.XmlIDREF;

public class Event {

    @XmlIDREF
    List<Datum> _objects = new ArrayList<>();
    public String note = "";
    private String _userEditableName;

    public Event(BasicDBObject obj, LifeStory ls) { // Construct from JSON.
        _userEditableName = obj.getString("userEditableName");
        note = obj.getString("note");
        BasicDBList datumIDs = (com.mongodb.BasicDBList) obj.get("datums");
        if (datumIDs != null) {
            for (Object datumIDObj : datumIDs) {
                String datumID = ((BasicDBObject) datumIDObj).getString("id");

                // Trim the old global-ID prefix.
                if (datumID.startsWith("fb_")) {
                    datumID = datumID.substring(3);
                }

                Datum datum = ls.datums.getObjectWithNetworkID(Long.parseLong(datumID));
                if (datum != null) {
                    _objects.add(datum);
                } else {
                    System.out.println("MISSING GROUND TRUTH DATUM! - " + datumID);
                }

            }
        }

    }

    public Event() {
    } // Ctor for JAXP.

    public Event(Datum obj) {
        _objects.add(obj);
    }

    public Event(Collection<Datum> objs) {
        _objects.addAll(objs);
    }

    @Deprecated
    public String getNote() {
        return note;
    }

    public List<Datum> getDatums() {
        return _objects;
    }

    public List<Datum> getPrimaryDatums() {
        return _objects;
    }

    public String getUserEditableName() {
        return _userEditableName;
    }

    public void setUserEditableName(String userEditableName) {
        _userEditableName = userEditableName;
    }
//    /**
//     * Get the start-time of the event as indicated by the raw content (e.g.
//     * photo upload time).
//     */
//    public DateTime getRawStartTime() {
//
//        DateTime rawStartDate = new DateTime(3000, 1, 1, 1, 1, 1);
//
//        for (MinedFacebookObject o : _objects) {
//            DateTime dateTime = o.getDateTime();
//            if ((dateTime != null) && (dateTime.isBefore(rawStartDate))) {
//                rawStartDate = dateTime;
//            }
//        }
//
//        return rawStartDate;
//    }
//    /**
//     * Get the start-time of the event as indicated by the raw content (e.g.
//     * photo upload time).
//     */
//    public DateTime getEarliestKnownTime() throws Exception {
//
//        DateTime knownStart = null;
//
//        for (MinedFacebookObject o : _objects) {
//
//            DateTime dateTime = o.getTrustedEventTimeOrNull();
//
//            if ((dateTime != null)
//                    && ((knownStart == null) || (dateTime.isBefore(knownStart)))) {
//                knownStart = dateTime;
//            }
//        }
//
//        return knownStart;
//    }
//    /**
//     * Get the start-time of the event as indicated by the raw content (e.g.
//     * photo upload time).
//     */
//    public DateTime getLatestKnownTime() throws Exception {
//
//        DateTime knownEnd = null;
//
//        for (MinedFacebookObject o : _objects) {
//
//            DateTime dateTime = o.getTrustedEventTimeOrNull();
//
//            if ((dateTime != null)
//                    && ((knownEnd == null) || (dateTime.isAfter(knownEnd)))) {
//                knownEnd = dateTime;
//            }
//        }
//
//        return knownEnd;
//    }
//    public HashSet<String> GetPeopleInEvent() {
//        HashSet<String> peopleAtEvent = new HashSet<>();
//
//        for (MinedFacebookObject o : getObjects()) {
//            if (o instanceof MinedPhoto) {
//                MinedPhoto op = (MinedPhoto) o;
//                CategorizedFacebookType from = op._photo.getFrom();
//                peopleAtEvent.add(from.getId());
//                for (Photo.Tag t : op._photo.getTags()) {
//                    peopleAtEvent.add(t.getId());
//                }
//            } else if (o._message != null) {
//                peopleAtEvent.add(o._message.getFrom().getId());
////                        for(Tag t: o._message.getTags()) {
////                            peopleAtEvent.add(t.getId());
////                        }
//            } else {
//                Assert.fail("not implemented");
//            }
//        }
//
//        return peopleAtEvent;
//    }
}
