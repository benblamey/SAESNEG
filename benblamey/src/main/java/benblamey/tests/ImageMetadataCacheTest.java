package benblamey.tests;

import benblamey.core.DateUtil;
import benblamey.saesneg.phaseA.image.ImageMetadataCache;
import java.io.IOException;
import junit.framework.Assert;
import org.joda.time.DateTime;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

public class ImageMetadataCacheTest {

    @Test
    public void test() {

        ImageMetadataCache cache = new ImageMetadataCache();

        final String imageID = "<some_id>";
        final String key = "my_property";
        final String value = "some value";

        cache.put(imageID, key, value);

        Object object = cache.get(imageID, key);

        Assert.assertEquals(value, object);

    }

    @Test
    public void test2() {

        ImageMetadataCache cache = new ImageMetadataCache();

        final String imageID = "test2" + DateUtil.DateTimeToUnixTime(DateTime.now()) + "https://scontent-b.xx.fbcdn.net/hphotos-ash4/t1/s720x720/1381342_10152030278384673_1926632838_n.jpg";
        final String key = "my_property";
        final String value = "some value";

        cache.put(imageID, key, value);

        Object object = cache.get(imageID, key);

        Assert.assertEquals(value, object);

    }

    @Test
    public void test3() throws IOException {

        ImageMetadataCache cache = new ImageMetadataCache();

        String url = "https://scontent-b.xx.fbcdn.net/hphotos-ash4/t1/s720x720/1381342_10152030278384673_1926632838_n.jpg";

        final String imageID = url;

        final String key = "my_property";

//		ReusableImageStream s = new ReusableImageStream(url);
//		BufferedImage bufferedImage = ImageIO.read(s.getInputStream());
//		 EdgeHistogramImplementation edgeHistogramImplementation = new EdgeHistogramImplementation(bufferedImage);
//		 
//		 final String value = edgeHistogramImplementation.getStringRepresentation();
//		 
//		 Assert.assertNotNull(value);
//		 
//		
//		cache.put(imageID, key, value);
        Object object = cache.get(imageID, key);

        System.out.println((String) object);

        assertNotNull(object);

        //Assert.assertEquals(value, object);
    }

}
