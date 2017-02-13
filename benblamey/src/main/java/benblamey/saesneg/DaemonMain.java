package benblamey.saesneg;

import benblamey.core.DateUtil;
import benblamey.core.Email;
import benblamey.saesneg.model.LifeStory;
import benblamey.saesneg.model.UserContext;
import benblamey.saesneg.serialization.LifeStoryInfo;
import benblamey.saesneg.serialization.LifeStoryXMLSerializer;
import com.benblamey.core.ExceptionHandler;
import com.benblamey.core.SystemInfo;
import com.benblamey.core.logging.Logger;
import com.benblamey.core.logging.LoggerLevel;
import com.benblamey.core.logging.PlainTextLogger;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.mail.MessagingException;
import org.joda.time.DateTime;
import socialworld.model.SocialWorldUser;

/**
 * The main class for the fetcher daemon service running on the remote server.
 *
 * @author Ben Blamey ben@benblamey.com
 */
public class DaemonMain {

    private static int CURRENT_VERSION = 3; // Increment this number to re-run fetching.
    private static final int DAYS_BEFORE_ERROR_RETRY = 1;
    private static final String LIFE_STORY_INFO_SOURCE = "FETCHER_DAEMON";

    public static Logger _logger = new PlainTextLogger(System.out);

    public static void main(String[] args) throws InterruptedException, MessagingException, IOException {

        _logger.debug = false;

        //DateTime readBuildTimestamp = readBuildTimestamp();
        String nameOfUser = null;
        DBObject userForDebug = null;
        boolean FORCE_DAEMON_FETCHER = PipelineContext.getCurrentContext().FORCE_DAEMON_FETCHER;

        boolean run = false;

        try {
            _logger.debug("Daemon started at " + DateTime.now().toString());
            
            // If there is a command line argument, assume it is a name.
            final Iterable<BasicDBObject> users;
            if (args.length == 1) {
                System.out.println("Fetching for: " + args[0]);
                users = Users.getUsers(new com.mongodb.BasicDBObject("FACEBOOK_NAME", args[0]));
                _logger.debug = true;
                FORCE_DAEMON_FETCHER = true; // Try even if we failed recently.
            }
            else
            {
                users = Users.getDefaultUsers();
            }

            for (DBObject user : users) {
                userForDebug = user;

                nameOfUser = SocialWorldUser.getName(user);

                _logger.debug("Checking " + nameOfUser);

                String currentFbToken = (String) user.get(SocialWorldUser.FACEBOOK_OAUTH_TOKEN);
                // If the user does not have a FB token for some reason, skip them.
                if (currentFbToken == null) {
                    _logger.debug("Skipping user - no FB token");
                    continue;
                }

                Object tokenExpiresUnixObj = user.get(SocialWorldUser.FACEBOOK_OAUTH_TOKEN_EXPIRES_UNIX);
                Long tokenExpiresUnix;
                if (tokenExpiresUnixObj instanceof Long) {
                    tokenExpiresUnix = (Long) tokenExpiresUnixObj;
                } else if (tokenExpiresUnixObj instanceof Integer) {
                    tokenExpiresUnix = (long) (Integer) tokenExpiresUnixObj;
                } else if (tokenExpiresUnixObj == null) {
                    // The last time this user logged in was too long ago for us to access Facebook.
                    _logger.info("Skipping user - don't know when token expires.");
                    continue;
                } else {
                    throw new RuntimeException("what else could it be?");
                }

                DateTime tokenExpires = DateUtil.DateTimeFromUnixTime(tokenExpiresUnix);

                if (tokenExpires.isBeforeNow()) {
                    // Token has expired, will not be possible to fetch anything.
                    _logger.info("Skipping user - token has expired.");
                    continue;
                }

                // Getting this far means we should be able to fetch Facebook data if we wish.
                // The question now is whether we should bother, or try someone else.
                List<LifeStoryInfo> lifeStoryInfos = LifeStoryInfo.getLifeStoryInfos(user);

                if (FORCE_DAEMON_FETCHER) {
                    // Force a run.
                    _logger.info("Running - forced.");  
                    run = true;
                } else if (lifeStoryInfos.size() == 0) {
                    // We haven't fetched any data for this user.
                    _logger.info("Running - first.");
                    run = true;
                } else {

                    // We have previously fetched data for this user.
                    // Find the most recent fetched result.
                    LifeStoryInfo latestInfo = null;
                    for (LifeStoryInfo info : lifeStoryInfos) {
                        latestInfo = ((latestInfo == null) || info.created.isAfter(latestInfo.created)) ? info : latestInfo;
                    }

                    _logger.info("Latest info is: " + latestInfo.getSummary());

                    if (latestInfo.version < CURRENT_VERSION) {
                        // If new code has been uploaded since the lifestory was created (and we're not currently working on it), 
                        // and the code is newer than the latest lifestory, run again.
                        // This is regardless of whether it succeeded last time or not.
                        _logger.info("Running - new code.");
                        run = true;
                    } else if (!latestInfo.success && latestInfo.created.plusDays(DAYS_BEFORE_ERROR_RETRY).isBeforeNow()) {
                        // If the latest fetch attempt failed, ***more than 24 hours ago***, fetch again (we don't want to "get stuck" on problematic data).
                        // This is regardless of code versions.
                        _logger.info("Running - failed last time, retrying after delay.");
                        run = true;
                    }
                }

                if (run) {
                    run(user);
                }
                    
                if (run) {
                    break;
                }
            }

        } catch (Exception e) {
            // Send an email report.
            String subject = "Daemon Run Exception for user " + ((nameOfUser == null) ? "[null]" : nameOfUser);
            String body = "User data:\n\n";
            if (userForDebug != null) {
                body += userForDebug.toString();
            } else {
                body += "[null]";
            }
            body += "\n\n\n";
            body += "Exception:";

            // As a last resort - catch all the exceptions here
            // - lots of "unlikely" exceptions are thrown as RuntimeExceptions - these are caught here. 
            // - we want super-detailed trace  - don't rely on Java's implementation.
            body += ExceptionHandler.getDetailedExceptionSummary(e);

            if (PipelineContext.getCurrentContext().DAEMON_SEND_EMAIL_REPORT) {
                Email.send(Email.BEN_EMAIL_ADDRESS, subject, body, "noreply");
                _logger.log(LoggerLevel.ERROR, "Exception report sent via email:");
            }
            _logger.log(LoggerLevel.ERROR, subject);
            _logger.log(LoggerLevel.ERROR, body);
        } finally {
            _logger.info("Daemon finished at " + DateTime.now().toString() + (run ? "" : " No work to do."));
        }

    }

    private static DateTime readBuildTimestamp() throws IOException {
        // Note that the DaemonMain class is "touched" by the cg_sync_geekisp_acad.sh script.
        File daemonClass = new File(DaemonMain.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        return new DateTime(daemonClass.lastModified());
    }

    private static void run(DBObject userObj) throws MessagingException, IOException {

        SocialWorldUser socialWorldUser = new SocialWorldUser(userObj);

        String userPrettyName = socialWorldUser.getValue("FACEBOOK_FIRST_NAME")
                + " " + socialWorldUser.getValue("FACEBOOK_LAST_NAME");

        _logger.info("Starting user: " + userPrettyName);

        LifeStoryInfo info = new LifeStoryInfo();
        info.created = DateTime.now();
        //info.filename = 
        info.source = LIFE_STORY_INFO_SOURCE;
        info.note = "";
        info.version = CURRENT_VERSION;
        info.success = false; // We need to record failed attempts, so that processing can skip this user.

        try {
            UserContext user = UserContext.FromSocialWorldUser(userObj, new LifeStory());

            Fetcher.fetch(_logger, user);

            // Serialize to XML.
            _logger.info("Serialize to XML.");
            info.filename = LifeStoryXMLSerializer.SerializeToXML(user.getLifeStory(), user, info.created);

            // Mark as a success.
            info.success = true;
        } finally {
            if (PipelineContext.getCurrentContext().DAEMON_ADD_LIFE_STORY_INFO) {
                // We also need to record (even failed attempts, so that processing can skip this user).
                socialWorldUser.appendToArray(LifeStoryInfo.LIFE_STORY_INFOS, info.ToDBObject());
            } else {
                System.out.println("Daemon complete - not saving in MongoDB");
                System.out.println(info.ToDBObject().toString());
            }
        }

        if (PipelineContext.getCurrentContext().DAEMON_SEND_EMAIL_REPORT) {
            // Send a success "report" (exceptions are handled in the calling method)
            String body = info.getSummary();
            body += "\n\n";
            body += "User: " + userPrettyName;
            body += "\n\n";
            body += "Facebook ID: " + socialWorldUser.getValue(SocialWorldUser.FACEBOOK_USER_ID);
            body += "\n\n";
            body += info.note;
            body += "\n\n";
            body += userObj.toString();
            String subject = "Daemon Run Result Success";
            Email.send(Email.BEN_EMAIL_ADDRESS, subject, body, "noreply");
            Email.send("blamey.ben@gmail.com", subject, body, "noreply");
        }

        // Send an invite?
        boolean sendInvite = false;

        // We don't want to pester people with lots of emails as the daemon runs at intervals.
        // Instead, we send them an invite in particular cases:
        if (PipelineContext.getCurrentContext().DAEMON_SEND_EMAIL_INVITE
                && (info.success)) {
            List<LifeStoryInfo> lifeStoryInfos = LifeStoryInfo.getLifeStoryInfos(socialWorldUser);

            if (lifeStoryInfos.size() == 1) {
                // If this is the first time we have fetched the lifestory, send an invite.
                sendInvite = true;
            } else {
                // This is not the first time we have fetched the life story.

                // Compute whether all previous attempts failed.
                boolean allPrevFailed = true;
                for (int i = 0; i < lifeStoryInfos.size() - 1; i++) {
                    if (lifeStoryInfos.get(i).success) {
                        allPrevFailed = false;
                        break;
                    }
                }

                sendInvite = allPrevFailed;
            }
        }

        // Email might be empty (e.g. user signed up with a mobile phone).
        String userEmail = (String) socialWorldUser.getValue(SocialWorldUser.FACEBOOK_EMAIL);
        if (sendInvite && userEmail != null && !userEmail.isEmpty()) {
            String body = "Hi,\n";
            body += "\n";
            body += "You recently signed up to our academic study, we are now ready for you to participate by creating your life story: \n";
            body += "\n";
            body += "http://"+SystemInfo.getWebDomainName()+":"+SystemInfo.getPublicHTTPPort()+"/benblamey.evaluation/\n";
            body += "\n";
            body += "There is a video explaining what to do. Hit the reply button if you have any queries.";
            body += "\n";
            body += "Thanks,\n";
            body += "Ben Blamey\n";
            body += "\n";
            body += "PhD Candidate\n";
            body += "Cardiff Metropolitan University, Llandaff Campus, Western Avenue, Cardiff, CF5 2YB\n";
            body += "ben@benblamey.com\n";
            String subject = "Create your life story!";
            
            Email.send(userEmail, subject, body, "Ben Blamey");
            _logger.log(LoggerLevel.INFORMATION, "Sent invite email to: " + userEmail);
        } else {
            _logger.log(LoggerLevel.INFORMATION, "Didn't send invite email to: " + userPrettyName);
        }
    }
}
