package socialworld.model;

import com.benblamey.core.DateUtil;
import com.benblamey.core.MongoClientInstance;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.restfb.DefaultFacebookClient;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.User;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Information about the user as stored in the Mongo database. Please use constants (either below or in calling code) wherever possible as keys.
 *
 * @author Ben
 *
 */
public class SocialWorldUser {

	// Big Five on Facebook data only
	public static final String SWT_FB_BIG5_EXTRAVERSION = "SWT_FB_BIG5_EXTRAVERSION";
	public static final String SWT_FB_BIG5_EMOTIONALSTABILITY = "SWT_FB_BIG5_EMOTIONALSTABILITY";
	public static final String SWT_FB_BIG5_AGREEABLENESS = "SWT_FB_BIG5_AGREEABLENESS";
	public static final String SWT_FB_BIG5_CONSCIENTIOUSNESS = "SWT_FB_BIG5_CONSCIENTIOUSNESS";
	public static final String SWT_FB_BIG5_OPENNESSTOEXPERIENCE = "SWT_FB_BIG5_OPENNESSTOEXPERIENCE";

	// Number of Facebook friends
	public static final String SWT_FB_NUMBEROFFRIENDS = "SWT_FB_NUMBEROFFRIENDS";

	// Significant words
	public static final String SWT_FB_SIGNIFICANTWORDS = "SWT_FB_SIGNIFICANTWORDS";

	public static final String FACEBOOK_USER_ID = "FACEBOOK_USER_ID";
	public static final String HAS_DONE_ETHICS = "HAS_DONE_ETHICS";
        public static final String FACEBOOK_NAME = "FACEBOOK_NAME";
	public static final String FACEBOOK_BIRTHDAY = "FACEBOOK_BIRTHDAY";
	public static final String FACEBOOK_EMAIL = "FACEBOOK_EMAIL";
	public static final String FACEBOOK_OAUTH_TOKEN = "FACEBOOK_OAUTH_TOKEN";

	public static final String TWITTER_OAUTH_TOKEN = "TWITTER_OAUTH_TOKEN";
	public static final String TWITTER_OAUTH_TOKEN_SECRET = "TWITTER_OAUTH_TOKEN_SECRET";

	public static final String LINKEDIN_OAUTH_TOKEN = "LINKEDIN_OAUTH_TOKEN";
	public static final String LINKEDIN_OAUTH_TOKEN_SECRET = "LINKEDIN_OAUTH_TOKEN_SECRET";

	public static final String QUESTIONAIRE_RESPONSE_PREFIX = "PERSONALITY_RESPONSEID_";
	public static final String REGISTRATION_DATE = "REGISTRATION_DATE";
	public static final String FACEBOOK_OAUTH_TOKEN_UPDATED_UNIX = "FACEBOOK_OAUTH_TOKEN_UPDATED_UNIX";
	public static final String FACEBOOK_OAUTH_TOKEN_EXPIRES_UNIX = "FACEBOOK_OAUTH_TOKEN_EXPIRES_UNIX";
	public static final String FINISHED_EDITING_GROUND_TRUTH = "FINISHED_EDITING_GROUND_TRUTH";

        public static String getName(DBObject obj) {
            if (obj.containsField(FACEBOOK_NAME)) {
                return (String)obj.get(FACEBOOK_NAME);
            } else {
                return (obj.get("FACEBOOK_FIRST_NAME") + " " + obj.get("FACEBOOK_LAST_NAME"));
            }
        }

    	public String getPrettyName() {
		return getName(this._obj);
	}

        public String getName() {
            return getName(this._obj);
        }


	private String _facebookUserID;
	public DBObject _obj;
        private boolean _readonly;

        public static SocialWorldUser fromCrossAppletSession(CrossAppletSession cas) {
		String facebookID = cas.getFacebookIDOrNull();
		if (facebookID == null) {
			return null;
		} else {
			return new SocialWorldUser(facebookID, true); // Allow add, we trust the CAS.
		}
	}

	public SocialWorldUser(DBObject obj) {
            this((String) obj.get(FACEBOOK_USER_ID), false); // Don't allow add.
	}

	public SocialWorldUser(String facebookUserID) {
            this(facebookUserID, true);
	}

        public SocialWorldUser(DBObject obj, boolean readonly) {
		if (readonly) {
                        _readonly = true;
			_obj = obj;
		} else {
			throw new RuntimeException("This ctor for readonly only.");
		}

	}

	// Main ctor.
	public SocialWorldUser(String facebookUserID, boolean allowAdd) throws NoSuchUserException {

		if (facebookUserID == null) {
			throw new IllegalArgumentException("facebookUserID cannot be nuil");
		}

		_facebookUserID = facebookUserID;

		DBCollection users = getDB();
		BasicDBObject fb_user_query = new BasicDBObject(FACEBOOK_USER_ID, facebookUserID);
		_obj = users.findOne(fb_user_query);
		if (_obj == null) {
			// We are creating a new user.
			if (allowAdd) {
				// Add the user, including a registration timestamp.
				DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
				Calendar cal = Calendar.getInstance();
				String dateString = dateFormat.format(cal.getTime());
				fb_user_query.append(REGISTRATION_DATE, dateString);

				users.insert(fb_user_query);
			} else {
				throw new NoSuchUserException();
			}
		} else {

			// We are loading an existing user.

			// Work around an issue whereby some fields are mysteriously empty - repair the data by fetching the missing values.
			try {
				if (isFBTokenValid()) {
					DefaultFacebookClient client = new DefaultFacebookClient((String) this.getValue(FACEBOOK_OAUTH_TOKEN));

					User fbProfile = (User) client.fetchObject("me", User.class);

					if (this.getValue(FACEBOOK_EMAIL) == null) {
						this.setValue(FACEBOOK_EMAIL, fbProfile.getEmail());
					}

					if (this.getValue(FACEBOOK_BIRTHDAY) == null) {
						this.setValue(FACEBOOK_BIRTHDAY, fbProfile.getBirthday());
					}
				}
			} catch (FacebookOAuthException e) {
				// Never mind. we tried.
                            throw new RuntimeException(e);
			}
		}
	}

	public boolean isFBTokenValid() {

		Object tokenExpiresUnixObj = this.getValue(FACEBOOK_OAUTH_TOKEN_EXPIRES_UNIX);
		Long tokenExpiresUnix;

		if (tokenExpiresUnixObj instanceof Long) {
			tokenExpiresUnix = (Long) tokenExpiresUnixObj;
		} else if (tokenExpiresUnixObj instanceof Integer) {
			tokenExpiresUnix = (long) (Integer) tokenExpiresUnixObj;
		} else {
			// Too old to tell - if we're trying something, try it.
			return true;
		}

		return (this.getValue(FACEBOOK_OAUTH_TOKEN) != null) && DateUtil.DateTimeFromUnixTime(tokenExpiresUnix).isAfterNow();
	}

	public Object getValue(String key) {
		if (_obj != null) {
		 	return _obj.get(key);
		} else {
			BasicDBObject fb_user_query = new BasicDBObject(FACEBOOK_USER_ID, _facebookUserID);

			DBCollection users = getDB();
			DBObject user = users.findOne(fb_user_query);
			Object result = user.get(key);
			return result;
		}
	}

	public void setValue(String key, Object value) {
		if (_readonly) {
			throw new RuntimeException("readonly!");
		}

		BasicDBObject fb_user_query = new BasicDBObject(FACEBOOK_USER_ID, _facebookUserID);

		DBCollection users = getDB();
                throw new RuntimeException("DB write currently disabled.");
		//users.update(fb_user_query, new BasicDBObject("$set", new BasicDBObject(key, value)));
	}

	public void unsetValue(String key) {
		if (_readonly) {
			throw new RuntimeException("readonly!");
		}

		BasicDBObject fb_user_query = new BasicDBObject(FACEBOOK_USER_ID, _facebookUserID);

		DBCollection users = getDB();
                throw new RuntimeException("DB write currently disabled.");
		//..users.update(fb_user_query, new BasicDBObject("$unset", new BasicDBObject(key, 1)));
	}

	public void appendToArray(String key, BasicDBObject obj) {
		if (_readonly) {
			throw new RuntimeException("readonly!");
		}

		BasicDBObject fb_user_query = new BasicDBObject(FACEBOOK_USER_ID, _facebookUserID);

		DBCollection users = getDB();
                throw new RuntimeException("DB write currently disabled.");
		//users.update(fb_user_query, new BasicDBObject("$push", new BasicDBObject(key, obj)));
	}

	public static DBCollection getDB() {
		MongoClient mongoClient = MongoClientInstance.getClientLocal();
		DB db = mongoClient.getDB("SocialWorld");
		DBCollection users = db.getCollection("users");
		return users;
	}



}
