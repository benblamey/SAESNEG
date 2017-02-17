package benblamey.saesneg;

import benblamey.core.MongoClientInstance;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.util.Iterator;
import socialworld.model.SocialWorldUser;

public class Users {

    private Users() {
    }

    /**
     * Get the set of users for the current platform. This is sometimes set to
     * just a single user for debugging purposes.
     *
     * @return
     */
    public static Iterable<BasicDBObject> getDefaultUsers() {
        Iterable<BasicDBObject> users = new Iterable<BasicDBObject>() {
            @Override
            public Iterator<BasicDBObject> iterator() {
                return new UserIterator(PipelineContext.getCurrentContext().getUserQuery());
            }
        };
        return users;
    }

    /**
     * Manually specify the query to select the users.
     *
     * @param query
     * @return
     */
    public static Iterable<BasicDBObject> getUsers(final DBObject query) {
        Iterable<BasicDBObject> users = new Iterable<BasicDBObject>() {
            @Override
            public Iterator<BasicDBObject> iterator() {
                return new UserIterator(query);
            }
        };
        return users;
    }

    public static Iterable<BasicDBObject> getAllUsers() {
        Iterable<BasicDBObject> users = new Iterable<BasicDBObject>() {
            @Override
            public Iterator<BasicDBObject> iterator() {
                return new UserIterator(new BasicDBObject()); // Empty query so that we get all the users.
            }
        };
        return users;
    }

    private static class UserIterator implements Iterator<BasicDBObject> {

        DBCursor _cursor;

        UserIterator(DBObject userQuery) {

            Mongo mongoClient = MongoClientInstance.getClientLocal();

            DB db = mongoClient.getDB(PipelineContext.getCurrentContext().DB);
            DBCollection users = db.getCollection("users");

            _cursor = users
                    .find(userQuery)
                    .sort(new BasicDBObject(SocialWorldUser.FACEBOOK_OAUTH_TOKEN_UPDATED_UNIX, 1)); // 1 = Sort in ascending order. The first people to log in are at the front of the queue for analysis - FIFO.

        }

        @Override
        public boolean hasNext() {
            return _cursor.hasNext();
        }

        @Override
        public BasicDBObject next() {
            BasicDBObject obj = (BasicDBObject) _cursor.next();

            return obj;
//            String id = (String) obj.get(UserContext.FACEBOOK_USER_ID);
//
//            UserContext user = new UserContext(obj.get(UserContext.FACEBOOK_FIRST_NAME) + " " + obj.get(UserContext.FACEBOOK_LAST_NAME));
//            user.FacebookAccessKey = (String) obj.get(UserContext.FACEBOOK_OAUTH_TOKEN);
//            return user;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }

        @Override
        protected void finalize() throws Throwable {
            _cursor.close();
            super.finalize();
        }
    }
}
