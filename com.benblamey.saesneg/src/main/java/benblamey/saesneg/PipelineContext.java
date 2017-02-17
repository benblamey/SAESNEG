package benblamey.saesneg;

import com.benblamey.core.SystemArchitecture;
import com.benblamey.core.SystemInfo;
import com.mongodb.BasicDBObject;
import java.util.Arrays;

/**
 * Static, infrastructure options, which are fixed for a given situation (as
 * opposed to options which may be altered for experimentation).
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public abstract class PipelineContext {

    public boolean USE_MYSQL_CACHE;
    public boolean LAST_YEAR_ONLY;
    public boolean FORCE_DAEMON_FETCHER;
    public boolean DAEMON_SEND_EMAIL_REPORT;
    public boolean DAEMON_SEND_EMAIL_INVITE;
    public boolean DAEMON_ADD_LIFE_STORY_INFO;
    public String DB;

    private static PipelineContext _current = new PipelineContext() {
        {
            switch (SystemInfo.detectServer()) {

                case BENBLAMEY_OVH:
                    this.LAST_YEAR_ONLY = true;
                    this.USE_MYSQL_CACHE = false;
                    this.FORCE_DAEMON_FETCHER = false;
                    this.DAEMON_SEND_EMAIL_REPORT = true;
                    this.DAEMON_SEND_EMAIL_INVITE = true;
                    this.DAEMON_ADD_LIFE_STORY_INFO = true;
                    this.DB = "SocialWorld";
                    break;
                
                case BENBLAMEY_OVH_STAGING:
                    this.LAST_YEAR_ONLY = true;
                    this.USE_MYSQL_CACHE = false;
                    this.FORCE_DAEMON_FETCHER = true;
                    this.DAEMON_SEND_EMAIL_REPORT = false;
                    this.DAEMON_SEND_EMAIL_INVITE = false;
                    this.DAEMON_ADD_LIFE_STORY_INFO = true;
                    this.DB = "SocialWorld";
                break;
                
                case LOCAL_MACHINE:
                    // CONFIG FOR **WINDOWS**
                    this.LAST_YEAR_ONLY = true;
                    this.USE_MYSQL_CACHE = false;
                    this.FORCE_DAEMON_FETCHER = true;
                    this.DAEMON_SEND_EMAIL_REPORT = false;
                    this.DAEMON_SEND_EMAIL_INVITE = false;
                    this.DAEMON_ADD_LIFE_STORY_INFO = false;
                    this.DB = "SocialWorld";
                break;
            }
        }

        @Override
        public BasicDBObject getUserQuery() {

            if (SystemArchitecture.IsLinuxSystem()) {
                // No filter on users for production system.
                return new BasicDBObject();
            } else {
                // For Dev system,
                BasicDBObject append = new BasicDBObject()
                        
                //.append("LIFE_STORY_INFOS.SUCCESS", true) // Select only users for whom we have a valid life story.
                .append("GROUND_TRUTH_EVENTS", new BasicDBObject() { // Where the user has created ground truth events.
                    {
                        append("$exists", true);// Where the user has created ground truth events.
                    }
                })
                
                .append("FACEBOOK_USER_ID", new BasicDBObject() {
                    {
                        append("$nin", Arrays.asList( 
                                    "836555706",  // Exclude "Muhamed Mustafa". Uncertainty re: which LS to load. lots of arabic text.
                                    "100005149806497" // Exclude "Felix Smith". Garbage event ground truth.
                                ) );
                    }
                })
                    
//                .append("FACEBOOK_USER_ID", new BasicDBObject() {
//                    {
//                        append("$ne", ); 
//                    }
//                })
                

                ;
                System.out.println("Note: ***User Filter is Active***!" + append.toString());
                return append;
            }
        }
    };

    public abstract BasicDBObject getUserQuery();

    public static PipelineContext getCurrentContext() {
        return _current;
    }

    /**
     * Set this flag to true in order to apply filter to old lifestories
     * containing pre-2012 datums. This is so that exported GATE documents
     * precisely match those created for ground-truth purposes.
     *
     */
    public boolean GATE_EXPORT_SINCE_2012_FILTER;

    public String getDataOutputDir() {
        return "C:\\work\\data\\output\\";
    }

}
