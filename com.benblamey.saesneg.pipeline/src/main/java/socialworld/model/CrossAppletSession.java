package socialworld.model;

import com.benblamey.core.MongoClientInstance;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.WriteConcern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Represents the Facebook User ID (i.e. identity of the user) associated with the ID of the session cookie
 * common to the applets.
 *
 * Share cookie IDs between applets.
 * see: Context.xml
 * see: http://stackoverflow.com/questions/9436736/sharing-session-data-between-contexts-in-tomcat
 *
 * @author Ben
 */
public class CrossAppletSession {

	private final String _sessionID;

	public CrossAppletSession(HttpServletRequest request) {
		HttpSession session = request.getSession(true); // Get a new session, creating one if it doens't already exist.
		// I think "Session" in this situation is not shared - it is only the identifier.
		// As far as the browser is concerned, the session is shared.

		_sessionID = session.getId();

		DBCollection users = openDB();
		BasicDBObject sessionQuery = getQuery();
		DBObject user = users.findOne(sessionQuery);
		if (user == null) {
				users.insert(sessionQuery);
		}
	}

	private static final String CROSS_APPLET_SESSION_ID = "CROSS_APPLET_SESSION_ID";
	private static final String FACEBOOK_USER_ID = "FACEBOOK_USER_ID";
	public static final String REDIRECT_AFTER_LOGIN_TEMP = "REDIRECT_AFTER_LOGIN_TEMP";

	private BasicDBObject getQuery() {
		return new BasicDBObject(CROSS_APPLET_SESSION_ID,_sessionID);
	}


	public String getFacebookIDOrNull() {
		DBCollection c = openDB();
		BasicDBObject query = getQuery();
		DBObject record = c.findOne(query);
		if (record == null) {
			return null;
		} else {
			String result = (String)record.get(FACEBOOK_USER_ID);
			return result;
		}
	}

	public void SetFacebookID(String facebookID) {

		assert facebookID != null;
		assert !facebookID.isEmpty();

		DBCollection c = openDB();
		BasicDBObject query = getQuery();
		c.update(query, new BasicDBObject("$set",
				new BasicDBObject(FACEBOOK_USER_ID,facebookID)));
	}

	public void RemoveFacebookID() {

		System.out.println("Removing cas for session: " + _sessionID);
		DBCollection c = openDB();
		BasicDBObject fb_user_query = new BasicDBObject(CROSS_APPLET_SESSION_ID,_sessionID);
		c.remove(fb_user_query,WriteConcern.JOURNAL_SAFE);
	}

	public String getRedirectAfterLoginTemp() {

		System.out.println("Getting redirect value for session: " + _sessionID);

		DBCollection c = openDB();
		BasicDBObject fb_user_query = getQuery();
		DBObject record = c.findOne(fb_user_query);
		if (record == null) {
			return null;
		} else {
			String result = (String)record.get(REDIRECT_AFTER_LOGIN_TEMP);
			return result;
		}
	}

	/**
	 * Never allow this to be set to anything specified by the user - danger of phishing.
	 * This session variable is copied over during login.
	 * @param url
	 */
	public void setRedirectAfterLoginTemp(String url) {

		System.out.println("Setting redirect to " + url + " for session: " + _sessionID);

		DBCollection c = openDB();
		BasicDBObject query = getQuery();
		c.update(query, new BasicDBObject("$set",
				new BasicDBObject(REDIRECT_AFTER_LOGIN_TEMP,url)));
	}

	private DBCollection openDB() {
		MongoClient mongoClient = MongoClientInstance.getClientLocal(); // We always use the local database for cross-applet-sessions.
		DB db = mongoClient.getDB("SocialWorld");
		return db.getCollection("cross_applet_session_data");
	}

	public boolean isSignedIn() {
		return getFacebookIDOrNull() != null;
	}


}
