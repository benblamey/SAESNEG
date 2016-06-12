package benblamey.tests;

import benblamey.saesneg.Users;
import benblamey.saesneg.model.LifeStory;
import benblamey.saesneg.model.UserContext;
import benblamey.saesneg.serialization.LifeStoryInfo;
import benblamey.saesneg.serialization.LifeStoryXMLSerializer;
import com.mongodb.DBObject;
import java.io.IOException;
import socialworld.model.SocialWorldUser;

public class DeserializeAllLifeStoriesTest {

    public static void main(String args[]) throws IOException {

        for (DBObject userObj : Users.getAllUsers()) {
            // Create the user context, and populate a new lifestory.

            UserContext user = UserContext.FromSocialWorldUser(userObj, null);
            System.out.println("Processing: " + user.getName());

            SocialWorldUser socialWorldUser = new SocialWorldUser(userObj);

            LifeStoryInfo lifeStoryInfo = LifeStoryInfo.getLatestGoodLifeStory(socialWorldUser);
            if (lifeStoryInfo != null) {
                LifeStory lifeStory = LifeStoryXMLSerializer.DeserializeLifeStory(lifeStoryInfo.filename, null);
                user.setDefaultLifeStory(lifeStory);
            } else {
                System.out.println("Skipping user - no 'good' life story.");
            }

        }

    }

}
