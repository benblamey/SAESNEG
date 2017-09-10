package com.benblamey.saesneg.model;

import com.benblamey.saesneg.model.datums.Datum;
import com.benblamey.saesneg.model.datums.DatumAlbum;
import com.benblamey.saesneg.model.datums.DatumCollection;
import com.benblamey.saesneg.model.datums.DatumEvent;
import com.benblamey.saesneg.phaseB.ClusteringStrategies;
import com.benblamey.saesneg.phaseB.DatumPairSimilarity;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.User;
import gate.Document;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlRootElement;
import org.joda.time.DateTime;

/*
 * The life story of a user (a collection of events comprised of datums)
 * Includes annotations and extracted WWWW.
 * Intended as a Data Access Object rather than a repository for business logic nor a working context.
 * Intended to be serialised.
 */
@XmlRootElement
public class LifeStory {

    public static final String USER_CONTEXT_METADATA_KEY_DIARY_ENTRIES = "USER_CONTEXT_KEY_DIARY_ENTRIES";
    public static final String USER_CONTEXT_METADATA_KEY_TFIDF = "USER_CONTEXT_KEY_TFIDF";
    public List<Object> Locations;

    public List<NamedFacebookType> Friends;
    public transient HashMap<String, String> FriendIDByName = new HashMap<String, String>();
    public transient HashMap<String, String> FriendNameByID = new HashMap<String, String>();

    public List<Object> Groups;

    public transient HashSet<Document> GateDocs = new HashSet<Document>();

    /*
     * Left behind to stop deserialisation breaking - not used for anything.
     */
    @Deprecated
    private final transient Map<String, Object> _metadata = new HashMap<>();

    public List<Event> EventsGolden;
    public transient List<Event> EventsComputed = new ArrayList<Event>();
    public transient List<DatumPairSimilarity> goldenPairs;

    /**
     * Be careful! the key here is the album_id not the graph API object_id.
     */
    public final DatumCollection datums = new DatumCollection();

    /**
     * Be careful! the key here is the album_id not the graph API object_id.
     * (Left behind so that we can deserialize the albums. Albums are
     * immediately moved out of this field on loading.
     */
    @Deprecated
    private HashMap<String, DatumAlbum> albums = new HashMap<>();

    private transient DateTime _created;

    public User _profile;

    // Transient (i.e. non-serialised) fields.
    public transient ClusteringStrategies DatumSimilarityCalculator;
    public transient List<Datum> goldDatums;

    /**
     * Gets the subset of datums that are suitable for clustering.
     *
     * @return
     */
    public List<Datum> getCandidateDatumsForClustering() {
        List<Datum> candDatums = new ArrayList<Datum>();

        for (Datum d : datums) {
            // Secondary datums, such as albums, are not used in clustering.
//    		if (!d.isPrimary()) {
//    			continue;
//    		}

            if (d instanceof DatumEvent) {

                // We exclude events that occur in the future (i.e. after the cutoff).
                DatumEvent e = (DatumEvent) d;
                // For an event, the content added time is actually the event time.
                if (e.getContentAddedDateTime().isAfter(_created)) {
                    continue;
                }
            }

            candDatums.add(d);
        }

        return candDatums;
    }

    /**
     * Get all the datums that are not in an event.
     *
     * @return
     */
    public DatumCollection getNextOrphanDatums() {
        DatumCollection orphanDatums = new DatumCollection();
        // Create a list of primary datums that are not in events.
        for (Datum mfo : getCandidateDatumsForClustering()) {
            if (mfo.isPrimary()) {
                orphanDatums.add(mfo);
            }
        }
        for (Event e : EventsGolden) {
            orphanDatums.removeAll(e.getDatums());
        }
        Collections.sort(orphanDatums, new Comparator<Datum>() {

            @Override
            public int compare(Datum arg0, Datum arg1) {

				// Requirements:
                // rationale: recall will be better for more recent data,
                // avoid sorting by type
                // albums photos still together
                // Most recent data first.
                // Recent needs to be lower.
                if (arg0.getContentAddedDateTime().isAfter(arg1.getContentAddedDateTime())) {
                    return -1;
                } else if (arg0.getContentAddedDateTime().isAfter(arg1.getContentAddedDateTime())) {
                    return +1;
                }

                return 0;
            }
        });
        // Truncate the set of orphans to a size that is manageable for the end user.
        int MAX_ORPHANS = 50;
        while (orphanDatums.size() > MAX_ORPHANS) {
            orphanDatums.remove(MAX_ORPHANS);
        }
        return orphanDatums;

    }

    public void initFriends() {
        if (Friends != null) {
            FriendIDByName = new HashMap<>();
            FriendNameByID = new HashMap<>();
            for (NamedFacebookType friend : Friends) {
                FriendIDByName.put(friend.getName(), friend.getId());
                FriendNameByID.put(friend.getId(), friend.getName());
            }
        }
    }

    public static void RemoveDuplicateDatums(ArrayList<Event> events) {
        HashSet<Long> IDs = new HashSet<>();
        for (Event e : events) {
            for (int i = e.getDatums().size() - 1; i >= 0; i--) {
                Datum obj = e.getDatums().get(i);
                Long ID = obj.getNetworkID();
                if (IDs.contains(ID)) {
                    e.getDatums().remove(i);
                } else {
                    IDs.add(ID);
                }
            }
        }
    }

    public void afterDeserializationFix(UserContext user) {

        // Move the albums to the datums (they were originally fetched separately.)
        if (albums != null) { // Newer lifestories have this field as null.
            for (DatumAlbum album : albums.values()) {
                album.afterDeserializationFix();
                datums.add(album);
            }
            albums.clear();
        }
        // The datums are stored in a tree map, so ordering isn't an issue.

        for (Datum d : this.datums) {
            d.postDeserializationFix(user);
        }

        EventsComputed = new ArrayList<>();

        this.datums.afterDeserlializationFix();


        // Build the friend indexes.
        initFriends();
    }

    public void setCreated(DateTime created) {
        _created = created;
    }

    public DateTime getCreated() {
        return _created;
    }

}
