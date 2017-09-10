package com.benblamey.saesneg.model.datums;

import com.benblamey.core.DateUtil;
import com.benblamey.saesneg.evaluation.DatumWebProperty;
import com.benblamey.saesneg.experiments.ExperimentOptions;
import com.benblamey.saesneg.model.UserContext;
import com.benblamey.saesneg.model.annotations.DataKind;
import com.benblamey.saesneg.model.annotations.PersonAnnotation;
import com.benblamey.saesneg.model.annotations.SingleDayTimeDensity;
import com.benblamey.saesneg.model.annotations.TemporalAnnotation;
import com.benblamey.saesneg.phaseA.text.gatesubdocument.GateSubDocumentWriter;
import com.restfb.exception.FacebookOAuthException;
import com.restfb.types.Event;
import com.restfb.types.NamedFacebookType;
import gate.util.InvalidOffsetException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import org.joda.time.DateTime;

public class DatumEvent extends Datum {

    com.restfb.types.Event _event;

    private transient List<NamedFacebookType> _attending;
    private transient List<NamedFacebookType> _declined;
    private transient List<NamedFacebookType> _maybe;
    private transient List<NamedFacebookType> _invited;

    public DatumEvent(UserContext user, Event data) {
        super(user, data);

        // The info we get when paginating through events is quite sparse.
        // We get the object directly to get more info.
        _event = (com.restfb.types.Event) _user.fch._facebookClient.fetchObject(data.getId(), Event.class);
        super._data = _event;
        downloadAttendees();
    }

    @Override
    public void postDeserializationFix(UserContext user) {
        super.postDeserializationFix(user);
        downloadAttendees();
    }

    private void downloadAttendees() {
        // Fetch information about attendees.
        try {
            //_attending = FacebookClientHelper.getAllFromConnection(_user.fch._facebookClient.fetchConnection(_event.getId() + "/attending", NamedFacebookType.class));
            //_declined = FacebookClientHelper.getAllFromConnection(_user.fch._facebookClient.fetchConnection(_event.getId() + "/declined", NamedFacebookType.class));
            //_invited = FacebookClientHelper.getAllFromConnection(_user.fch._facebookClient.fetchConnection(_event.getId() + "/invited", NamedFacebookType.class));
            //_maybe = FacebookClientHelper.getAllFromConnection(_user.fch._facebookClient.fetchConnection(_event.getId() + "/maybe", NamedFacebookType.class));
        } catch (FacebookOAuthException e) {
            System.out.println("Token now invalid -- cannot download event attendees.");
        }
    }

    private DatumEvent() {
    } //Serialization Ctor.

    @Override
    public String getWebViewTitle() {
        // People need a reminder - its too much like metadata for mortals.
        return "Event: " + _event.getName();
    }

    @Override
    public Set<DatumWebProperty> getWebViewMetadata() {
        Set<DatumWebProperty> metadata = super.getWebViewMetadata();

        if (_event.getOwner() != null) {
            metadata.add(new DatumWebProperty() {
                {
                    Key = "Organizer";
                    FriendlyName = "Organizer";
                    Value = Datum.anonName(_event.getOwner().getName());
                }
            });
        }

        metadata.add(new DatumWebProperty() {
            {
                Key = "RSVP";
                FriendlyName = "RSVP";
                Value = _event.getRsvpStatus();
            }
        });

        metadata.add(new DatumWebProperty() {
            {
                Key = "Date";
                FriendlyName = "Date";
                Value = DateUtil.ToPrettyDate(
                        new DateTime(_event.getStartTime()));
            }
        });

        metadata.add(new DatumWebProperty() {
            {
                Key = "Location";
                FriendlyName = "Location";
                Value = _event.getLocation();
            }
        });

        metadata.add(new DatumWebProperty() {
            {
                Key = "Description";
                FriendlyName = "Desc.";
                Value = _event.getDescription();
            }
        });

        metadata.add(new DatumWebProperty() {
            {
                Key = "Type";
                FriendlyName = "Type";
                Value = "Event";
            }
        });

        return metadata;
    }

    @Override
    public String getWebViewClass() {
        return "facebookeventdatum";
    }

    @Override
    public DateTime getContentAddedDateTime() {
        //System.err.println("Lazy: using event time instead of event created time.");
        return new DateTime(this._event.getStartTime());
    }

    @Override
    protected void appendTextToGATE(GateSubDocumentWriter doc) throws InvalidOffsetException {

        doc.appendLine(this._event.getName(), "name");
        doc.appendLine(this._event.getDescription(), "description");

    }

    @Override
    public void processMetadataFields(ExperimentOptions opt) throws IOException,
            URISyntaxException {

        super.searchLocationMetadataField(this._event.getLocation(), "location", opt);

        {
            Event.Owner owner = _event.getOwner();
            if (owner != null) {
                PersonAnnotation fromTag;
                fromTag = PersonAnnotation.From(owner, "event_owner");
                this.getAnnotations().People.add(fromTag);
            }
        }

        if (this._attending != null) {
            for (NamedFacebookType attendee : this._attending) {
                PersonAnnotation fromTag2 = PersonAnnotation.From(attendee, "event/attending");
                this.getAnnotations().People.add(fromTag2);
            }
        } else {
            // We have not downloaded event attendence information.
        }

        {
            TemporalAnnotation temporalAnno = new TemporalAnnotation();
            temporalAnno.setDensity(new SingleDayTimeDensity(_event.getStartTime()));
            temporalAnno.SourceDataKind = DataKind.Metadata;
            this.getAnnotations().DateTimesAnnotations.add(temporalAnno);
        }

    }

}
