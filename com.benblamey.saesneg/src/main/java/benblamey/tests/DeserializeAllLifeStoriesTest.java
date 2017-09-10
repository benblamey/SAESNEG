package com.benblamey.tests;

import com.benblamey.saesneg.Users;
import com.benblamey.saesneg.model.LifeStory;
import com.benblamey.saesneg.model.UserContext;
import com.benblamey.saesneg.serialization.LifeStoryInfo;
import com.benblamey.saesneg.serialization.LifeStoryXMLSerializer;
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
