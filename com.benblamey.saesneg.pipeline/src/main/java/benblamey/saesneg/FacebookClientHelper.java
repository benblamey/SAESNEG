package com.benblamey.saesneg;

import com.benblamey.core.DateUtil;
import com.benblamey.core.facebook.CachedDefaultWebRequestor;
import com.benblamey.core.facebook.ErrorMapper;
import com.benblamey.saesneg.model.UserContext;
import com.benblamey.saesneg.model.datums.Datum;
import com.benblamey.saesneg.model.datums.DatumCollection;
import com.benblamey.saesneg.model.datums.DatumEvent;
import com.benblamey.saesneg.model.datums.DatumPhoto;
import com.benblamey.core.logging.Logger;
import com.benblamey.core.logging.PlainTextLogger;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.DefaultJsonMapper;
import com.restfb.DefaultWebRequestor;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.types.FacebookType;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * Various helper methods for using the FacebookClient class.
 *
 * @author Ben
 * @param <T>
 */
public class FacebookClientHelper<T> {

  private static Logger _logger = new PlainTextLogger(System.out);


  static private abstract class MinedFacebookObjectFactory {

        abstract Datum CreateDatum(UserContext uc, FacebookType obj);
        String userConnection;
        Class<? extends FacebookType> FacebookType;
        String fieldsToGet;
    }
    static final List<MinedFacebookObjectFactory> thingsToGet = new ArrayList<MinedFacebookObjectFactory>() {
        {
            // accounts
            // achievements
            // activities
            // albums - we check photos/uploaded instead.
            // applications/developer
            // apprequests
            // books

            // CHECKINs is deprecated for versions >=v2.0
//            add(new MinedFacebookObjectFactory() {
//                @Override
//                public Datum CreateDatum(UserContext uc, FacebookType obj) {
//                    return new DatumCheckin(uc, (com.restfb.types.Checkin) obj);
//                }
//
//                {
//                    userConnection = "checkins";
//                    FacebookType = com.restfb.types.Checkin.class;
//                }
//            });

            add(new MinedFacebookObjectFactory() { // The events this user is attending.
                @Override
                public Datum CreateDatum(UserContext uc, FacebookType obj) {
                    return new DatumEvent(uc, (com.restfb.types.Event) obj);
                }

                {
                    userConnection = "events";
                    FacebookType = com.restfb.types.Event.class;
                }
            });
            // family
            // feed -- the users wall - only the last 25 - not much point.
            // friendlists
            // friendrequests
            // friends
            // games
            // groups
            // home -- the users news feed - only the last 25 - not much point.
            // inbox
            // interests
            // likes

            // Decided to exclude these, as they are mostly non-events, yet considered important by Participant 1.
//            add(new MinedFacebookObjectFactory() { // Might be a link to an event website.
//                @Override
//                public MinedFacebookObject CreateDatum(UserContext uc, FacebookType obj) {
//                    return new MinedLink(uc, (com.restfb.types.Link) obj);
//                }
//
//                {
//                    userConnection = "links";
//                    FacebookType = com.restfb.types.Link.class;
//                }
//            });
            // locations - get everything else anyway.
            // movies
            // music
            // mutualfriends
            // notes
            // notifications
            // outbox
            // payment_transactions
            // payments
            // permissions
            add(new MinedFacebookObjectFactory() { // Photos the user is tagged in.
                @Override
                public Datum CreateDatum(UserContext uc, FacebookType obj) {
                    return new DatumPhoto(uc, (com.restfb.types.Photo) obj);
                }

                {
                    userConnection = "photos";
                    FacebookType = com.restfb.types.Photo.class;
                    fieldsToGet = "id,album,created_time,from,images,link,name,page_story_id,updated_time,event,place";
                }
            });
            add(new MinedFacebookObjectFactory() { // Photos the user is tagged in.
                @Override
                public Datum CreateDatum(UserContext uc, FacebookType obj) {
                    return new DatumPhoto(uc, (com.restfb.types.Photo) obj);
                }

                {
                    userConnection = "photos/uploaded";
                    FacebookType = com.restfb.types.Photo.class;
                }
            });
            // picture - the profile pic.
            // pokes
            // posts - includes trivia like "became friends". We get the important stuff anyway.
            // questions
            // scores
            // sharedposts
//            add(new MinedFacebookObjectFactory() {
//                @Override
//                public Datum CreateDatum(UserContext uc, FacebookType obj) {
//                    return new DatumStatusMessage(uc, (com.restfb.types.StatusMessage) obj);
//                }
//
//                {
//                    userConnection = "statuses";
//                    FacebookType = com.restfb.types.StatusMessage.class;
//                }
//            });
            // subscribedto
            // subscribers
            // tagged
            // television
            // updates - inbox - trivia - "soandso commented on your cat picture"
            // videos.
        }
    };

    public final FacebookClient _facebookClient;
    private final UserContext _userContext;



    public FacebookClientHelper(UserContext uc) {
        _userContext = uc;
        _facebookClient = new DefaultFacebookClient(
                _userContext.FacebookAccessKey,
                PipelineContext.getCurrentContext().USE_MYSQL_CACHE
                        ? new CachedDefaultWebRequestor()
                        : new DefaultWebRequestor(), new DefaultJsonMapper(new ErrorMapper())
                        //,FacebookConfig.FacebookAPIVersion
        );
    }

    public void getObjects(DatumCollection data, String person) {

        for (MinedFacebookObjectFactory thing : thingsToGet) {
            _logger.debug("Fetching " + thing.userConnection + "...");

            String path = person + "/" + thing.userConnection;

            if (PipelineContext.getCurrentContext().LAST_YEAR_ONLY) {
                System.out.println("Note: fetching last year only.");
            }

            List<FacebookType> objects = getConnectionWithDateRange(path,
                    thing.FacebookType,
                    PipelineContext.getCurrentContext().LAST_YEAR_ONLY
                            ? DateTime.now().minusMonths(6) : null,
                    DateTime.now().secondOfDay().setCopy(0),
                    thing.fieldsToGet); // Always use midnight so that requests get cached.
            System.out.println("Fetched " + objects.size()
                    + " objects from connection " + person + "/" + thing.userConnection);

            for (FacebookType nfd : objects) {
                if (!data.containsObjectWithNetworkID(Datum.getNetworkIDForFB(nfd))) {
                    Datum createFromData = thing.CreateDatum(_userContext, nfd);
                    data.add(createFromData);
                }
            }
        }
    }

    public List<FacebookType> getConnectionWithDateRange(String path, Class<? extends FacebookType> type,
            DateTime since, DateTime until, String fields) {
        ArrayList<FacebookType> results = new ArrayList<>();

        ArrayList<Parameter> p = new ArrayList<>();

        if (since != null) {
            p.add(Parameter.with("since", DateUtil.DateTimeToUnixTime(since))); // Facebook is expecting a unix timestamp.
        }
        if (until != null) {
            p.add(Parameter.with("until", DateUtil.DateTimeToUnixTime(until)));
        }
        if (fields != null)
        {
            p.add(Parameter.with("fields", fields));
        }

        p.add(Parameter.with("since", DateUtil.DateTimeToUnixTime(DateTime.now().minusMonths(12*6)))); // Facebook is expecting a unix timestamp.

        Connection<? extends FacebookType> fetchConnection = _facebookClient.fetchConnection(path, type,
            p.toArray(new Parameter[p.size()]));
        results.addAll(fetchConnection.getData());

        System.out.println("Have:" + results.size() + " "+ path +" after  first page.");

        while (fetchConnection.getNextPageUrl() != null) {
            fetchConnection = _facebookClient.fetchConnectionPage(fetchConnection.getNextPageUrl(), type);
            results.addAll(fetchConnection.getData());
            System.out.println("Now have:" + results.size());
        }

        return results;
    }

    public List<T> getConnectionWithPagination(String path, Class<T> type) {
        Connection<T> fetchConnection = _facebookClient.fetchConnection(path, type);
        return getAllFromConnection(fetchConnection);
    }

    public static <T> List<T> getAllFromConnection(Connection<T> fetchConnection) {
        List<T> all = new ArrayList<>();
        for (List<T> items : fetchConnection) {
            _logger.debug("\tFetching items..");
            all.addAll(items);
        }
        return all;
    }
}
