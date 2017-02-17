package benblamey.saesneg.review;

import benblamey.saesneg.Users;
import benblamey.saesneg.experiments.configs.UserIDs;
import benblamey.saesneg.model.LifeStory;
import benblamey.saesneg.model.datums.Datum;
import benblamey.saesneg.serialization.LifeStoryInfo;
import benblamey.saesneg.serialization.LifeStoryXMLSerializer;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import java.io.FileNotFoundException;
import socialworld.model.SocialWorldUser;

public class TestLoadOldParticipant3 {

    public static void main(String[] args) throws FileNotFoundException {

        for (DBObject userObj : Users.getUsers(new BasicDBObject(SocialWorldUser.FACEBOOK_USER_ID, UserIDs.PARTICIPANT_3_FACEBOOK_USER_ID))) {
            // Create the user context, and populate a new lifestory.

            SocialWorldUser user = new SocialWorldUser(userObj);

            System.out.println(user.getPrettyName());

            for (LifeStoryInfo info : LifeStoryInfo.getLifeStoryInfos(user)) {
                System.out.println(info.getSummary());

                if (info.success) {
                    LifeStory deserializeLifeStory = LifeStoryXMLSerializer.DeserializeLifeStory(info.filename, null); // to fix
                    System.out.println("datums in LS: " + deserializeLifeStory.datums.size());
                    for (Datum d : deserializeLifeStory.datums) {
                        System.out.println(d.getNetworkID());
                    }
                }
            }

        }

    }
}
