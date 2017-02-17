package benblamey.saesneg.model.datums;

import benblamey.core.DateUtil;
import benblamey.core.WebCache404;
import benblamey.saesneg.FacebookClientHelper;
import benblamey.saesneg.evaluation.DatumWebProperty;
import benblamey.saesneg.experiments.ExperimentOptions;
import benblamey.saesneg.model.UserContext;
import benblamey.saesneg.model.annotations.DataKind;
import benblamey.saesneg.model.annotations.DatumsInUserStructureAnnotation;
import benblamey.saesneg.model.annotations.ImageContentAnnotation;
import benblamey.saesneg.model.annotations.LocationAnnotation;
import benblamey.saesneg.model.annotations.PersonAnnotation;
import benblamey.saesneg.model.annotations.PhotoUploadTimeDensity;
import benblamey.saesneg.model.annotations.SingleDayTimeDensity;
import benblamey.saesneg.model.annotations.TemporalAnnotation;
import benblamey.saesneg.phaseA.image.ImageFeatureExtractor;
import benblamey.saesneg.phaseA.text.gatesubdocument.GateSubDocumentWriter;
import com.benblamey.core.StringUtils;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import com.restfb.exception.FacebookException;
import com.restfb.types.Album;
import com.restfb.types.CategorizedFacebookType;
import com.restfb.types.Comment;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.Photo;
import com.restfb.types.Photo.Tag;
import com.restfb.types.Place;
import gate.util.InvalidOffsetException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.joda.time.DateTime;

public class DatumPhoto extends Datum {

    private DatumAlbum _album;
    private String _allText;
    public Photo _photo;
    private List<NamedFacebookType> _tags;

    // private MinedPhoto() { // Serialization ctor
    // }
    public DatumPhoto(UserContext user, Photo data) {
        super(user, data);

        if (data instanceof Photo) {
            _photo = (Photo) data;
        } else {
            throw new IllegalArgumentException("data is not an Photo.");
        }

        // FQL is now deprecated.
        //searchForAlbum();

        _tags = FacebookClientHelper.getAllFromConnection(_user.fch._facebookClient.fetchConnection(data.getId() + "/tags", NamedFacebookType.class));
    }

    @Override
    public String getWebViewTitle() {
        String title = "";
        if (!StringUtils.IsNullOrEmpty(this._photo.getName())) {
            title = this._photo.getName();
        }
        if (_album != null) {
            title += StringUtils.IsNullOrEmpty(title) ? "" : ", album:";
            title += _album._album.getName();
        }
        if (StringUtils.IsNullOrEmpty(title)) {
            title = "Photo ID: " + _photo.getId();
        }
        return title;
    }

    @Override
    protected void appendTextToGATE(GateSubDocumentWriter doc) throws InvalidOffsetException {

        if (_photo.getName() != null) {
            doc.appendLine(this._photo.getName(), "photo_name");
        }

        for (Comment c : _photo.getComments()) {
            doc.appendLine(c.getMessage(), "photo_comment");
        }

    }

    @Override
    public Set<DatumWebProperty> getWebViewMetadata() {
        Set<DatumWebProperty> metadata = super.getWebViewMetadata();

        metadata.add(new DatumWebProperty() {
            {
                Key = "Type";
                FriendlyName = "Type";
                Value = "Photo";
            }
        });
        metadata.add(new DatumWebProperty() {
            {
                Key = "AlbumName";
                FriendlyName = "Album Name";
                Value = (_album != null) ? _album._album.getName() : "";
            }
        });
        metadata.add(new DatumWebProperty() {
            {
                Key = "AlbumDescription";
                FriendlyName = "Album Desc.";
                Value = (_album != null) ? _album._album.getDescription() : "";
            }
        });
        metadata.add(new DatumWebProperty() {
            {
                Key = "AlbumLocation";
                FriendlyName = "Album Loc.";
                Value = (_album != null) ? _album._album.getLocation() : "";
            }
        });
        metadata.add(new DatumWebProperty() {
            {
                Key = "PhotoName";
                FriendlyName = "Photo Name";
                Value = _photo.getName();
            }
        });
        metadata.add(new DatumWebProperty() {
            {
                Key = "Date";
                FriendlyName = "Date";
                Value = DateUtil.ToPrettyDate(new DateTime(_photo.getCreatedTime()));
            }
        });

        // We don't include Location - its a JSON object.
        String names = "";
        for (Tag t : _photo.getTags()) {
            names += Datum.anonName(t.getName()) + ", ";
        }
        final String names2 = names;
        metadata.add(new DatumWebProperty() {
            {
                Key = "People";
                FriendlyName = "People";
                Value = names2;
            }
        });

        return metadata;
    }

    private static Map<String, String> albumIDs = new HashMap<String, String>();

    private void searchForAlbum() {
        try {
            // First, search for the Album that contains the photo.
            List<String> albumIDresult = super._user.fch._facebookClient
                    .executeFqlQuery("SELECT aid FROM photo WHERE object_id = \""
                            + this.getNetworkID() + "\"", String.class);
            if (albumIDresult.size() == 0) {
                System.err.append("no albumIDresults");
                return;
            }
            BasicDBObject albumIDResultParsed = (BasicDBObject) JSON.parse(albumIDresult.get(0));
            String albumID = (String) albumIDResultParsed.get("aid");

            // If we got duff data, give up.
            if (albumID.equals("0")) {
                System.err.println("Counldn't resolve ID of album " + this.getNetworkID());
                return;
            }

            // To retrieve the album from the Graph API, we need to convert the album-ID
            // into an Object ID (THEY ARE NOT THE SAME!)
            // See if we have it cached.
            String albumObjectID = albumIDs.get(albumID);

            if (albumObjectID == null) {
                String query = "SELECT object_id FROM album WHERE aid=\"" + albumID + "\"";
                System.out.println(query);

                List<String> albumGraphIDResult = super._user.fch._facebookClient.executeFqlQuery(query, String.class);
                if (albumGraphIDResult.size() == 0) {
                    System.err.println("Counldn't resolve ID of photo " + this.getNetworkID());
                    return;
                }

                BasicDBObject albumGraphIDresultParsed = (BasicDBObject) JSON.parse(albumGraphIDResult.get(0));
                albumObjectID = (albumGraphIDresultParsed.get("object_id")).toString();
                albumIDs.put(albumID, albumObjectID); // Store the cached result.
            }

            Long albumObjectIDLong = Long.parseLong(albumObjectID);

            // Now we know which album we are fetching, see if we have fetched the album already.
            DatumAlbum album = (DatumAlbum) _user.getLifeStory().datums.getObjectWithNetworkID(albumObjectIDLong);
            if (album == null) {
                // If not, fetch it.
                Album rawAlbum = super._user.fch._facebookClient.fetchObject(albumObjectIDLong.toString(), Album.class);
                DatumAlbum datumAlbum = new DatumAlbum(_user, rawAlbum);
                _user.getLifeStory().datums.add(datumAlbum);
            }

            _album = album;

        } catch (FacebookException e) {
            System.err.println("Ignoring FB exception when fetching album info: " + e.toString());
        }
    }

    public DatumAlbum getAlbum() {
        return this._album;
    }

    @Override
    public String getImageThumbnailURL() {
        List<Photo.Image> images = _photo.getImages();
        if (!images.isEmpty()) {
            return images.get(0).getSource();
        }
        return null;
    }

    @Override
    public String getFullImageURL() {
        return _photo.getSource();
    }

    @Override
    public String getWebViewClass() {
        return "facebookphotodatum";
    }

    @Override
    public DateTime getContentAddedDateTime() {
        return new DateTime(_photo.getCreatedTime());
    }

    @Override
    public void processMetadataFields(ExperimentOptions opt) throws IOException,
            URISyntaxException {
        Place place = this._photo.getPlace();
        if (place != null && place.getLocation() != null && place.getLocation().getLatitude() != null) {
            this.getAnnotations().Locations.add(new LocationAnnotation(place.getLocation(), "location", DataKind.Metadata));
        }

        if (this._photo.getFrom() != null) {
            CategorizedFacebookType from = this._photo.getFrom();
            PersonAnnotation owner = PersonAnnotation.From(from, "photo_from");
            this.getAnnotations().People.add(owner);
        }
        for (Tag tag : this._photo.getTags()) {
            PersonAnnotation tagPerson = PersonAnnotation.From(tag, "photo_tag");
            this.getAnnotations().People.add(tagPerson);
        }

        if (this._album != null) {
            this.getAnnotations().UserStructureAnnotations.add(new DatumsInUserStructureAnnotation(_album));
        }

        // Create Temporal Constraints.
        {
            TemporalAnnotation temporalAnno = new TemporalAnnotation();
            temporalAnno.SourceDataKind = DataKind.Metadata;
            temporalAnno.setDensity(new PhotoUploadTimeDensity(_photo.getCreatedTime()));
            temporalAnno.isDefinitive = true;
            this.getAnnotations().DateTimesAnnotations.add(temporalAnno);
        }

        if (this._album == null) {
            // System.out.println("Photo with no album.");
        } else if (_album._album.getName().equals("Mobile Uploads")) {
            SingleDayTimeDensity exactDate = new SingleDayTimeDensity(new DateTime(_photo.getCreatedTime()));

            TemporalAnnotation te2 = new TemporalAnnotation();
            te2.setDensity(exactDate);
            te2.SourceDataKind = DataKind.Metadata;
            this.getAnnotations().DateTimesAnnotations.add(te2);
        }

    }

    @Override
    public void processImageContent() {
        super.processImageContent();

        
        if (ImageFeatureExtractor.isJPEG(_photo.getSource())) {
            
            String localFilePath = "C:\\work\\docs\\Dropbox\\PHD_DATA\\images\\" + this._ID + ".jpg";
            
            if (!WebCache404.is404(_photo.getSource())) {
                File destFile = new File(localFilePath);
                if (!destFile.exists())
                {
                    // Download the file.
                    InputStream in = null;

                    try {
                        URL url2 = new URL(_photo.getSource());
                        in = new BufferedInputStream(url2.openStream());
                    } catch (FileNotFoundException ex) {
                        System.out.println("Skipping photo image not found -- " + _photo.getSource());
                        WebCache404.put404(_photo.getSource());
                    } catch (IOException ex) {
                        System.out.println("Skipping photo image not found/403 -- " + _photo.getSource());
                        WebCache404.put404(_photo.getSource());
                    }

                    if (in != null) {
                        try {
                            // copy over the stream.
                            FileOutputStream fos = new FileOutputStream(localFilePath);
                            while (true) {
                                int b = in.read();
                                if (b < 0) {
                                    break;
                                }
                                fos.write(b);
                            }
                            fos.close();
                            in.close();

                        } catch (MalformedURLException ex) {
                            throw new RuntimeException(ex);
                        } catch (FileNotFoundException ex) {
                            throw new RuntimeException(ex);
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                }
            }

            try {
                ImageFeatureExtractor extractor = new ImageFeatureExtractor();
                ImageContentAnnotation anno = extractor.createImageAnnotation(_photo.getSource());
                anno.Note = _photo.getSource();
                this.getAnnotations().ImageContentAnnotations.add(anno);
            } catch (RuntimeException e) {
                System.out.println("Skipping photo image not found -- " + _photo.getSource());
                WebCache404.put404(_photo.getSource());
            }
            
        }
    }

}
