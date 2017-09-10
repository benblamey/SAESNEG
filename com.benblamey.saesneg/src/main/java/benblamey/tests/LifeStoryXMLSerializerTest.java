package com.benblamey.tests;

import com.benblamey.saesneg.serialization.LifeStoryXMLSerializer;
import org.junit.Test;

public class LifeStoryXMLSerializerTest {

    @Test
    public void testDeserializeLifeStory() {
        LifeStoryXMLSerializer.DeserializeLifeStory("PARTICIPANT_1_FACEBOOK_ID_Participant1_1374837846.xml", null);
    }

}
