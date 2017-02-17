package benblamey.saesneg.experiments.configs;

import benblamey.saesneg.ExperimentUserContext;
import benblamey.saesneg.evaluation.DatumWebProperty;
import benblamey.saesneg.experiments.Experiment;
import benblamey.saesneg.experiments.ExperimentOptions;
import benblamey.saesneg.experiments.ExperimentSet;
import benblamey.saesneg.model.Event;
import benblamey.saesneg.model.datums.Datum;
import benblamey.saesneg.model.datums.DatumCheckin;
import benblamey.saesneg.model.datums.DatumEvent;
import benblamey.saesneg.model.datums.DatumPhoto;
import benblamey.saesneg.model.datums.DatumStatusMessage;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;

public class Participant1_NoProcessing_Thesis {

    public static void main(String[] args) throws Exception {

        createParticipant1Appendix();
        
        //getLocalImagePath("10100980662791070");

    }

    private static void createParticipant1Appendix() throws Exception, FileNotFoundException, UnsupportedEncodingException {
        ExperimentSet set = new ExperimentSet();
        set.name = "AllUsers_NoProcessing_ThesisExamples";

        set.addExperiment(new Experiment(set) {
            {
                Name = "default";
                Options = new ExperimentOptions() {
                    {
                        UserQuery = new BasicDBObject().append("FACEBOOK_FIRST_NAME", "PARTICIPANT_1_FIRST_NAME").append("FACEBOOK_LAST_NAME", "PARTICIPANT_1_LAST_NAME");

                        // Skip both phases.
                        // Phase A options.
                        runPhaseA = false; // Skip Phase A completely.

                        // Skip phase B.
                        PhaseBOptions = null;
                    }
                };

            }
        });

        List<ExperimentUserContext> users = set.exps.get(0).run();

        ExperimentUserContext participant1 = users.get(0);

        PrintWriter writer = new PrintWriter("C:\\work\\docs\\LATEX\\thesis\\images\\participant_1_photos\\participant_1_events.html", "UTF-8");
        //writer.println("\\begin{verbatim}");
        writer.print("<HTML>");
        writer.print("<HEAD>");
        writer.print("<LINK rel=\"stylesheet\" type=\"text/css\" href=\"participant_1_events.css\">");
        writer.print("</HEAD>");
        writer.print("<BODY>");
        int i = 0;
        for (Event event : participant1.userContext.getLifeStory().EventsGolden) {
            if (event.getDatums().isEmpty()) continue;
            if (event.getUserEditableName().equals("Events I didn't attend")) continue; // dummy event, exclude.
            
            writer.println("<div class=\"event\"/>");
            writer.println("<h2>Event "+i+"</h2>");
            writer.println("<!-- "+event.getUserEditableName()+" -->");
            for (Datum d : event.getDatums()) {
                writer.println("<div class=\"datum\">");
                writer.println("<!-- "+d.getNetworkID()+" -->");
                writer.println("<h3>"+ getFriendlyClassName(d) + "</h3>"); // \": \" + htmlEntities(d.getWebViewTitle()) + \"
                writer.println("<p class=\"datum\">");
                
                if (d instanceof DatumPhoto) {
                    String imagePath = getLocalImagePath(d.getNetworkString());
                    if (imagePath != null) {
                        writer.println("<img src=\""+imagePath+"\" />");
                    }
                }

                for (DatumWebProperty prop : d.getWebViewMetadata()) {
                    if (prop.Key.equals("OSN")) {
                        continue;
                    }
                    //if (prop.Key.equals("Date")) continue;
                    if (prop.Key.equals("Type")) {
                        continue;
                    }
                    if (prop.Value == null || prop.Value.equals("")) {
                        continue;
                    }
                    writer.println(prop.FriendlyName + ": " + cleanup(prop.Value) + "<br>");
                }
                writer.println("</p>");
                writer.println("</div>");
            }
            writer.println("</div>");
            writer.println("<hr>");
            i++;
        }
        writer.print("</BODY>");
        writer.print("<HTML>");
        writer.close();
    }

    private static String getFriendlyClassName(Datum d) {
        return names.get(d.getClass());
    }

    private static final Map<Class, String> names = new HashMap<Class, String>() {
        {
            put(DatumPhoto.class, "Photo");
            put(DatumStatusMessage.class, "Status Message");
            put(DatumEvent.class, "Event");
            put(DatumCheckin.class, "Check-In");
        }
    };

    private static String cleanup(String value) {
        if (value == null) {
            return "";
        }
        String newValue = value;
        newValue = shorten(value);
        newValue = newValue.split("[\\n\\r]+")[0]; // Of the first line.
        if (!value.equals(newValue)) {
            newValue = newValue + "...";
        }
        
        newValue = htmlEntities(newValue);
        
        
        //newValue = StringEscapeUtils.escapeHtml(newValue);
        
        return newValue;
    }

    private static String htmlEntities(String newValue) {
        // http://www.w3schools.com/html/html_entities.asp
        newValue = newValue.replaceAll("&", "&amp;"); // Do this one first!
        newValue = newValue.replaceAll("à", "a&#768;");
        newValue = newValue.replaceAll("á", "a&#769;");
        return newValue;
    }

    private static String shorten(String value) {
        final int length = 110;

        if (value.length() > length) {
            value = value.substring(0, length); // Only the first few chars.
        }
        return value;
    }

    public static String getLocalImagePath(String networkID) throws IOException {

        final String dir = "C:\\work\\docs\\LATEX\\thesis\\images\\participant1_photos\\";
        
        String pathIfExists = dir + networkID + ".jpg";
        String pathIfMissing = dir + networkID + ".MISSING";
        //String pathIfExists = dir + networkID + ".jpg";

        if (new File(pathIfMissing).exists()) {
            // Photo is marked as undownloadable -- skip.
            return null;
        }
        
        if (!new File(pathIfExists).exists()) {
            // File does not exist, try to download it.
            String url = "https://graph.facebook.com/v2.5/" + networkID + "?access_token=" + PARTICIPANT_1_ACCESS_TOKEN + "&debug=all&fields=source&format=json&method=get&pretty=0&suppress_http_code=1";
            BasicDBObject result = (BasicDBObject)getJSON(url);
            String sourceUrl = result.getString("source");
            
            if (sourceUrl == null) {
                // The source URL could not be found, makr the file as undownloadable and skip.
                FileUtils.touch(new File(pathIfMissing));
                return null;
            }
            
            getImage(sourceUrl, pathIfExists);
        }
        
        if (new File(pathIfExists).exists()) {
            return networkID+".jpg";
        } else {
            return null;
        }
        
    }

    public static Object getJSON(String urlstr) throws IOException {

        StringBuffer buff = new StringBuffer();
        URL url = new URL(urlstr);
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openConnection().getInputStream()));
        int c;
        while ((c = br.read()) != -1) {
            buff.append((char) c);
        }
        br.close();
        String content = buff.toString();

        Object parse = JSON.parse(content);

        return parse;
    }
    
    public static void getImage(String urlstr, String filename) throws IOException {
        URL url = new URL(urlstr);
        InputStream br = url.openConnection().getInputStream();
        OutputStream bw = new FileOutputStream(filename);
        int c;
        while ((c = br.read()) != -1) {
            bw.write(c);
        }
        br.close();
        bw.close();
    }

    private static final String PARTICIPANT_1_ACCESS_TOKEN = "???????/";

}
