package com.benblamey.tests;

import com.benblamey.saesneg.Fetcher;
import com.benblamey.saesneg.Users;
import com.benblamey.saesneg.model.LifeStory;
import com.benblamey.saesneg.model.UserContext;
import com.benblamey.saesneg.serialization.LifeStoryXMLSerializer;
import com.benblamey.core.logging.PlainTextLogger;
import com.mongodb.DBObject;
import java.io.IOException;
import org.joda.time.DateTime;
import org.junit.Test;

public class FetcherTest {

    @Test
    public void test() throws IOException {

        DBObject firstUser = Users.getDefaultUsers().iterator().next();

        UserContext uc = UserContext.FromSocialWorldUser(firstUser, new LifeStory());

        Fetcher.fetch(new PlainTextLogger(System.out), uc);

        // Serialize to XML.
        String fileName = LifeStoryXMLSerializer.SerializeToXML(uc.getLifeStory(), uc, new DateTime());

        System.out.println(fileName);
    }

}
