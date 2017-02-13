package benblamey.saesneg.phaseA.image;

import at.lux.imageanalysis.ColorLayoutImpl;
import at.lux.imageanalysis.EdgeHistogramImplementation;
import at.lux.imageanalysis.ScalableColorImpl;
import at.lux.imageanalysis.VisualDescriptor;
import benblamey.saesneg.model.annotations.DataKind;
import benblamey.saesneg.model.annotations.ImageContentAnnotation;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class ImageFeatureExtractor {

    public static String EdgeHistogram = "EdgeHistogram";
    public static String ScalableColor = "ScalableColor";
    public static String ColorLayout = "ColorLayout";

    public static ImageFeatureExtractor Instance = new ImageFeatureExtractor();
    
    private ImageMetadataCache _cache = new ImageMetadataCache();


//    if (isJPEG(urlA) && isJPEG(urlB)) {
//        return distances;
//    }
    public static boolean isJPEG(String url) {
        url = url.toLowerCase();
        return url.endsWith(".jpeg") || url.endsWith(".jpg")
                || // https://fbcdn-sphotos-g-a.akamaihd.net/hphotos-ak-ash3/163775_10150143159126217_5955414_n.jpg?lvh=1
                url.contains(".jpg?") || url.contains(".jpeg?");
    }
    
    public ImageContentAnnotation createImageAnnotation(String url) {
        
        ImageContentAnnotation anno = new ImageContentAnnotation();
        anno.SourceDataKind = DataKind.Image;
        
        ReusableImageStream s = new ReusableImageStream(url); // Doesn't actually read anything immediately.
        
        try {
            anno.setEdgeHistogram(getEdgeHistogram(url, s));
            anno.setScalableColor(getScalableColor(url, s));
            anno.setColorLayout(getColorLayout(url, s));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        
        return anno;
    }

    private VisualDescriptor getEdgeHistogram(String urlA, ReusableImageStream ris) throws IOException{
        String descriptor = (String) _cache.get(urlA, EdgeHistogram);
        EdgeHistogramImplementation ehi;
        if (descriptor == null) {
            BufferedImage bufferedImage = ImageIO.read(ris.getInputStream());
            ehi = new EdgeHistogramImplementation(bufferedImage);
            _cache.put(urlA, EdgeHistogram, ehi.getStringRepresentation());
        } else {
            ehi = new EdgeHistogramImplementation(descriptor);
        }
        return ehi;
    }

    private VisualDescriptor getScalableColor(String urlA, ReusableImageStream ris) throws IOException{
        String descriptor = (String) _cache.get(urlA, ScalableColor);
        ScalableColorImpl ehi;
        if (descriptor == null) {
            BufferedImage bufferedImage2 = ImageIO.read(ris.getInputStream());
            ehi = new ScalableColorImpl(bufferedImage2);
            _cache.put(urlA, ScalableColor, ehi.getStringRepresentation());
        } else {
            ehi = new ScalableColorImpl(descriptor);
        }
        return ehi;
    }

    private VisualDescriptor getColorLayout(String urlA, ReusableImageStream ris) throws IOException{
        String edgeHistogramDescriptor = (String) _cache.get(urlA, ColorLayout);
        ColorLayoutImpl ehi;
        if (edgeHistogramDescriptor == null) {
            BufferedImage bufferedImage2 = ImageIO.read(ris.getInputStream());
            ehi = new ColorLayoutImpl(bufferedImage2);
            _cache.put(urlA, ColorLayout, ehi.getStringRepresentation());
        } else {
            ehi = new ColorLayoutImpl(edgeHistogramDescriptor);
        }
        return ehi;
    }

//  
//  public float computeSimilarity(String file1, String file2) {
//      ArrayList<VisualDescriptor> get1 = _features.get(file1);
//      ArrayList<VisualDescriptor> get2 = _features.get(file2);
//
//      float low = 0;
//      for (int i = 0; i < get1.size(); i++) {
//          low = low + get1.get(i).getDistance(get2.get(i));
//      }
//
//      low = low / get1.size();
//
//      return low;
//  }
//    private void timeExtraction(String key, String fileName) throws IOException {
//
//    	
//        long time;
//        int countRuns = 5;
//
//        ArrayList<VisualDescriptor> metadata = new ArrayList<VisualDescriptor>();
//
//
//        EdgeHistogramImplementation e2 = null;
//        time = System.currentTimeMillis();
//        for (int i = 0; i < countRuns; i++) {
//            e2 = new EdgeHistogramImplementation(img);
//            metadata.add(e2);
//
//        }
//        System.out.println("EdgeHistogram took " + ((float) (System.currentTimeMillis() - time) / (float) countRuns) + " ms each");
//
//        ScalableColorImpl sc = null;
//        time = System.currentTimeMillis();
//        for (int i = 0; i < countRuns; i++) {
//            sc = new ScalableColorImpl(img);
//            metadata.add(sc);
//        }
//        System.out.println("ScalableColor took " + ((float) (System.currentTimeMillis() - time) / (float) countRuns) + " ms each");
//
//        ColorLayoutImpl cl = null;
//        time = System.currentTimeMillis();
//        for (int i = 0; i < countRuns; i++) {
//            cl = new ColorLayoutImpl(img);
//            metadata.add(cl);
//
//            ColorLayoutImpl cl2 = new ColorLayoutImpl(
//                    cl.getStringRepresentation());
//
//        }
//        System.out.println("ColorLayout took " + ((float) (System.currentTimeMillis() - time) / (float) countRuns) + " ms each");
//
//        //java.lang.ArrayIndexOutOfBoundsException
////        ColorStructureImplementation dc = null;
////        time = System.currentTimeMillis();
////        for (int i = 0; i < countRuns; i++) {
////            dc = new ColorStructureImplementation(img);
////            metadata.add(dc);
////        }
////        System.out.println("DominantColor took " + ((float) (System.currentTimeMillis() - time) / (float) countRuns) + " ms each");
////        
//        _features.put(fileName, metadata);
//
//        //Serializer.writeToFile(fileName + ".ser", metadata);
//    }


}
