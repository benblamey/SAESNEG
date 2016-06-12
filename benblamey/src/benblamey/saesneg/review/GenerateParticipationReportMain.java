package benblamey.saesneg.review;

import benblamey.core.MongoClientInstance;
import benblamey.saesneg.PipelineContext;
import benblamey.saesneg.model.PartialEvent;
import benblamey.saesneg.serialization.LifeStoryJsonSerializer;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import socialworld.model.SocialWorldUser;

/**
 * Generates a report about participation and state of ground truth.
 *
 * @author Ben Blamey blamey.ben@gmail.com
 *
 */
public class GenerateParticipationReportMain {

    public static void main(String[] args) throws Exception {
        generateReport();
    }

    /**
     * Write out diary as a HTML page.
     */
    public static void generateReport() throws Exception {
        VelocityEngine ve = VelocityUtils.getVelocityEngine();

        VelocityContext context = new VelocityContext();

        Mongo mongoClient = MongoClientInstance.getClientRemote();
        DB db = mongoClient.getDB("SocialWorld");
        DBCollection users = db.getCollection("users");

        DBCursor usersWithLifeStories = users.find(
                new BasicDBObject() {
                    {
                        // Match a Field Without Specifying Array Index
                        // If you do not know the index position of the subdocument, concatenate the name of the field that contains the array, with a dot (.) and the name of the field in the subdocument.
                        //   http://docs.mongodb.org/manual/tutorial/query-documents/
                        append("LIFE_STORY_INFOS.SUCCESS", true);
                    }
                }
        );

        DBCursor finishedEditingGroundTruth = users.find(
                new BasicDBObject() {
                    {
                        append("FINISHED_EDITING_GROUND_TRUTH", true);
                    }
                }
        );

        Map<String, List<PartialEvent>> eventsForPeople = new HashMap<String, List<PartialEvent>>();

        while (usersWithLifeStories.hasNext()) {
            DBObject user = usersWithLifeStories.next();

            ArrayList<PartialEvent> events = new ArrayList<>();
            eventsForPeople.put(SocialWorldUser.getName(user), events);

            BasicDBObject groundTruth = (BasicDBObject) user.get(LifeStoryJsonSerializer.GROUND_TRUTH_EVENTS);
            if (groundTruth != null) {
                BasicDBList groundTruthList = (BasicDBList) groundTruth.get("events");
                if (groundTruthList != null) {
                    for (Object groundTruthEventObj : groundTruthList) {
                        PartialEvent e = new PartialEvent((BasicDBObject) groundTruthEventObj);
                        events.add(e);

                    }
                }
            }

        }

        context.put("totalusers", users.count());
        context.put("withls", usersWithLifeStories.size());
        context.put("fingt", finishedEditingGroundTruth.size());
        context.put("eventsForPeople", eventsForPeople);

        SimpleDateFormat logTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
        context.put("date", logTimestamp.format(new Date()));

        mongoClient.close();

        Template t = ve.getTemplate("benblamey/saesneg/review/report.vm");
        FileWriter writer = new FileWriter(PipelineContext.getCurrentContext().getDataOutputDir() + "report.htm");
        t.merge(context, writer);
        writer.close();
    }

}
