package com.benblamey.saesneg.review;

import com.benblamey.saesneg.model.Event;
import com.benblamey.saesneg.model.UserContext;
import java.io.FileWriter;
import java.util.List;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;

// Depreciated by Tomcat integration.
@Deprecated
public class HTMLDiary {

    /**
     * Write out diary as a HTML page.
     */
    public static void WriteToHTML(UserContext user, String name, List<Event> events) throws Exception {
        VelocityEngine ve = VelocityUtils.getVelocityEngine();

        VelocityContext context = new VelocityContext();
        context.put("events", events);
        context.put("context", user);

        Template t = ve.getTemplate("benblamey/experiments/pipeline/export/diary.vm");
        FileWriter writer = new FileWriter(user.getOutputDirectoryWithTrailingSlash() + name + ".htm");
        t.merge(context, writer);
        writer.close();
    }

}
