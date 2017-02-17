package benblamey.saesneg.serialization;

import benblamey.core.DateUtil;
import benblamey.saesneg.model.LifeStory;
import benblamey.saesneg.model.UserContext;
import benblamey.saesneg.model.datums.DatumAlbum;
import benblamey.saesneg.model.datums.DatumCheckin;
import benblamey.saesneg.model.datums.DatumCollection;
import benblamey.saesneg.model.datums.DatumEvent;
import benblamey.saesneg.model.datums.DatumLink;
import benblamey.saesneg.model.datums.DatumPhoto;
import benblamey.saesneg.model.datums.DatumStatusMessage;
import com.benblamey.core.SystemArchitecture;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;
import com.thoughtworks.xstream.security.AnyTypePermission;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.DateTime;

public class LifeStoryXMLSerializer {

    public static String getXMLDirectoryWithTrailingSlash() {
        String prefix;
        if (SystemArchitecture.IsLinuxSystem()) {
            prefix = "/usr/benblamey/lifestory/";
        } else {
            prefix = "C:/work/data/output/lifestory/";
        }
        return prefix;
    }

    public static String SerializeToXML(LifeStory user, UserContext uc, DateTime timestamp) throws IOException {

        String filePath = GenerateNewXMLFilePath(uc, timestamp);

        {
            FileOutputStream f = new FileOutputStream(filePath);
            OutputStreamWriter osw = new OutputStreamWriter(f, "UTF-8");
            BufferedWriter bw = new BufferedWriter(osw);

            XStream xstream = new XStream(new StaxDriver());
            xstream.toXML(user, bw);
            bw.close();
        }

        // a/b/c.txt --> c.txt
        String fileName = FilenameUtils.getName(filePath);
        return fileName;
    }

    private static String GenerateNewXMLFilePath(UserContext uc, DateTime timestamp) {
        String prefix = getXMLDirectoryWithTrailingSlash();

        String timestampStr = Integer.toString(DateUtil.DateTimeToUnixTime(timestamp));
        String path = prefix + uc.getFileSystemSafeName()
                + "_" + timestampStr
                + ".xml";
        return path;
    }
    
    // "Keep a configured XStream instance for multiple usage. Creation and initialization is quite expensive compared to the overhead of XStream when calling marshall or unmarshal."
    // http://xstream.codehaus.org/faq.html
    private static XStream xstream;
    
    static {
        
       xstream = new XStream(new StaxDriver());
        
                // Need to configure XStream so that it copes with the various refactorings.
        xstream.alias("benblamey.experiments.pipeline.model.MinedObjectCollection", DatumCollection.class);

        xstream.alias("benblamey.experiments.pipeline.model.MinedAlbum", DatumAlbum.class);
        xstream.aliasField("album", DatumAlbum.class, "_album");

        xstream.alias("benblamey.experiments.pipeline.model.MinedEvent", DatumEvent.class);
        xstream.alias("benblamey.experiments.pipeline.model.MinedCheckin", DatumCheckin.class);
        xstream.alias("benblamey.experiments.pipeline.model.MinedPhoto", DatumPhoto.class);
        xstream.alias("benblamey.experiments.pipeline.model.MinedLink", DatumLink.class);
        xstream.alias("benblamey.experiments.pipeline.model.MinedStatusMessage", DatumStatusMessage.class);

        xstream.aliasPackage("benblamey.experiments.pipeline", "benblamey.saesneg");

        xstream.aliasField("facebookObjects", LifeStory.class, "datums");
        
        // Turn off security.
        xstream.addPermission(AnyTypePermission.ANY);
        
        //xstream.allowTypesByWildcard("benblamey.**");

        //Converter lookupConverterForType = xstream.getConverterLookup().lookupConverterForType(DatumPhoto.class);
        //System.out.println(lookupConverterForType.toString());
        
        //DefaultConverterLookup.lookupConverterForType(DatumPhoto.class);
    }
    

    public static LifeStory DeserializeLifeStory(String fileName, UserContext user) {
        if (fileName == null) {
            throw new RuntimeException("fileName is null");
        }
        String path = getXMLDirectoryWithTrailingSlash() + fileName;

        
        
        System.out.println("Deserializing life story: " + path);
        LifeStory story = (LifeStory) xstream.fromXML(new File(path));

        story.afterDeserializationFix(user);

        return story;
    }
}
