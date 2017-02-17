package benblamey.saesneg.phaseB.old;

import benblamey.saesneg.model.Event;
import benblamey.saesneg.model.UserContext;
import benblamey.saesneg.model.annotations.PersonAnnotation;
import benblamey.saesneg.model.datums.Datum;
import benblamey.saesneg.phaseA.image.ImageFeatureExtractor;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashSet;

@Deprecated
public class EventSimilarity_OLD {

    private final UserContext _user;
    private final ImageFeatureExtractor _mi = new ImageFeatureExtractor();

    public EventSimilarity_OLD(UserContext user) {
        _user = user;
    }

    public double CompareEvent(Event i, Event j) throws UnknownHostException, IOException, IOException {

        Datum i_obj = i.getDatums().get(0);
        Datum j_obj = j.getDatums().get(0);

        double edgeWeight = 0;

//        {
//            // Location in Common.
//            HashSet<String> locations_i = i_obj.getLocations();
//            HashSet<String> locations_j = j_obj.getLocations();
//
//            int totalBeforeMerge = locations_i.size()+ locations_j.size();
//
//            HashSet<String> merged = new HashSet<String>();
//            merged.addAll(locations_j);
//            merged.addAll(locations_i);
//
//            if (totalBeforeMerge > merged.size()) {
//                edgeWeight+=1;
//            }
//        }
        {
            int peopleAti = i_obj.getAnnotations().People.size();
            int peopleAtj = j_obj.getAnnotations().People.size();

            HashSet<String> peopleAtEither = new HashSet<String>();

            for (PersonAnnotation pae : i_obj.getAnnotations().People) {
                peopleAtEither.add(pae.getFacebookID());
            }

            for (PersonAnnotation pae : j_obj.getAnnotations().People) {
                peopleAtEither.add(pae.getFacebookID());
            }

            if (peopleAtEither.size() + 2 < peopleAti + peopleAtj) {
                edgeWeight += 1;
            }
        }

//        {
//            // Photo-specific similarity
//            if (i_obj instanceof MinedPhoto) {
//                MinedPhoto i_photo = (MinedPhoto)i_obj;
//                if (j_obj instanceof MinedPhoto) {
//                    MinedPhoto j_photo = (MinedPhoto)j_obj;    
//                    
//                    // Same album.
//                    MinedAlbum i_album = i_photo.getAlbum();
//                    MinedAlbum j_album = j_photo.getAlbum();
//                    if (i_album != null && j_album != null) {
//                        if (i_album.album.getId().equals(j_album.album.getId())) {
//                            //edgeWeight += 1;
//                        }
//                    }
//                    
//                    Map<String, Float> similarity = _mi.GetSimilarity(i_photo._photo.getSource(),
//                            j_photo._photo.getSource());
//
//                    int sims  = 0;
//                    if (similarity.get(Main_Images.ColorLayout) < 200)
//                    {
//                        sims++;
//                    }
//                    
//                    if (similarity.get(Main_Images.EdgeHistogram) < 200)
//                    {
//                        sims++;
//                    }
//                    
//                    if (similarity.get(Main_Images.ScalableColor) < 200)
//                    {
//                        sims++;
//                    }
//                    
//                    if (sims > 2 ) {
//                        edgeWeight += 1;
//                    }
//                          
//        
//                    
//                }
//            }
//        }
        //i_obj.
        // i.getObjects().get(0);
        return edgeWeight;

    }
}
