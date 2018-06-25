package com.benblamey.saesneg.model.datums;

import com.benblamey.core.DateUtil;
import com.benblamey.saesneg.evaluation.DatumWebProperty;
import com.benblamey.saesneg.experiments.ExperimentOptions;
import com.benblamey.saesneg.model.UserContext;
import com.benblamey.saesneg.model.annotations.DataKind;
import com.benblamey.saesneg.model.annotations.LocationAnnotation;
import com.benblamey.saesneg.model.annotations.PersonAnnotation;
import com.benblamey.saesneg.model.annotations.SingleDayTimeDensity;
import com.benblamey.saesneg.model.annotations.TemporalAnnotation;
import com.benblamey.saesneg.phaseA.text.gatesubdocument.GateSubDocumentWriter;
import com.restfb.types.Checkin;
import com.restfb.types.Comment;
import com.restfb.types.Location;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.Place;
import gate.util.InvalidOffsetException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;
import org.joda.time.DateTime;

public class DatumCheckin extends Datum {

    private final Checkin _checkin;

    public DatumCheckin(UserContext uc, Checkin checkin) {
        super(uc, checkin);
        _checkin = checkin;
    }

    @Override
    public String getWebViewClass() {
        return "facebookcheckindatum";
    }

    private static String placeToFriendlyString(Place place) {
        if (place != null) {
            Location location = place.getLocation();
            if (location != null) {
                return location.getStreet() + "," + location.getCity() + "," + location.getState()
                        + "," + location.getCountry() + "," + location.getZip();
            } else {
                return place.getLocationAsString();
            }
        }
        return null;
    }

    @Override
    public String getWebViewTitle() {
        String location = placeToFriendlyString(_checkin.getPlace());
        return "Checkin: " + location;
    }

    @Override
    public Set<DatumWebProperty> getWebViewMetadata() {
        Set<DatumWebProperty> metadata = super.getWebViewMetadata();

        metadata.add(new DatumWebProperty() {
            {
                Key = "Type";
                FriendlyName = "Type";
                Value = "Checkin";
            }
        });

        final Place place = _checkin.getPlace();
        if (place != null) {
            metadata.add(new DatumWebProperty() {
                {
                    Key = "Place";
                    FriendlyName = "Place";
                    Value = placeToFriendlyString(place);
                }
            });
        }

        final NamedFacebookType application = _checkin.getApplication();
        if (application != null) {
            metadata.add(new DatumWebProperty() {
                {
                    Key = "App";
                    FriendlyName = "Application";
                    Value = application.getName();
                }
            });
        }

        metadata.add(new DatumWebProperty() {
            {
                Key = "Status";
                FriendlyName = "Status";
                Value = _checkin.getMessage();
            }
        });

        metadata.add(new DatumWebProperty() {
            {
                Key = "Date";
                FriendlyName = "Date";
                Value = DateUtil.ToPrettyDate(
                        new DateTime(_checkin.getCreatedTime()));
            }
        });

        // We don't include Location - it ends up being JSON.
        //_checkin.
        String names = "";
        for (NamedFacebookType t : _checkin.getTags()) {
            names += t.getName() + ", ";
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

    @Override
    public DateTime getContentAddedDateTime() {
        return new DateTime(_checkin.getCreatedTime());
    }

    @Override
    protected void appendTextToGATE(GateSubDocumentWriter doc) throws InvalidOffsetException {

        String message = this._checkin.getMessage();
        doc.appendLine(message, "message");

        for (Comment comment : this._checkin.getComments()) {
            doc.appendLine(comment.getMessage(), "comment");
        }

    }

    @Override
    public void processPeopleTextFields() {

        String message = _checkin.getMessage();
        if (message != null) {
            for (String friendName : this._user.getLifeStory().FriendIDByName.keySet()) {
                if (message.contains(friendName)) {
                    {
                        PersonAnnotation personAtEvent = PersonAnnotation.fromFreeText(friendName,  this._user.getLifeStory().FriendIDByName, "status_mention");
                        this.getAnnotations().People.add(personAtEvent);
                    }
                }
            }
        }
    }

    @Override
    public void processMetadataFields(ExperimentOptions opt) throws IOException,
            URISyntaxException {

        Location l = this._checkin.getPlace().getLocation();
        if (l != null) {
            this.getAnnotations().Locations.add(new LocationAnnotation(this._checkin.getPlace().getLocation(), "place > location", DataKind.Metadata));
        }

        PersonAnnotation personAtEvent = PersonAnnotation.From(_checkin.getFrom(), "status_from");
        getAnnotations().People.add(personAtEvent);

        for (NamedFacebookType tag : _checkin.getTags()) {
            PersonAnnotation tagPerson = PersonAnnotation.From(tag, "photo_tag");
            getAnnotations().People.add(tagPerson);
        }

        {
            TemporalAnnotation temporalAnno = new TemporalAnnotation();
            temporalAnno.setDensity(new SingleDayTimeDensity(_checkin.getCreatedTime()));
            temporalAnno.SourceDataKind = DataKind.Metadata;
            temporalAnno.isDefinitive = true;
            this.getAnnotations().DateTimesAnnotations.add(temporalAnno);
        }

    }

}
