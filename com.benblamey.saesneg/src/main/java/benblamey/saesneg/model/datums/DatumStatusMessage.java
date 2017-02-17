package benblamey.saesneg.model.datums;

import benblamey.core.DateUtil;
import benblamey.saesneg.evaluation.DatumWebProperty;
import benblamey.saesneg.experiments.ExperimentOptions;
import benblamey.saesneg.model.UserContext;
import benblamey.saesneg.model.annotations.DataKind;
import benblamey.saesneg.model.annotations.PersonAnnotation;
import benblamey.saesneg.model.annotations.SingleDayTimeDensity;
import benblamey.saesneg.model.annotations.TemporalAnnotation;
import benblamey.saesneg.phaseA.text.gatesubdocument.GateSubDocumentWriter;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.StatusMessage;
import gate.util.InvalidOffsetException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;
import org.joda.time.DateTime;

public class DatumStatusMessage extends Datum {

    public StatusMessage _message;

    DatumStatusMessage() { // Serialization ctor.
    }

    public DatumStatusMessage(UserContext _user, StatusMessage data) {
        super(_user, data);
        _message = data;
    }

    @Override
    public String getWebViewTitle() {
        return _message.getMessage();
    }

    @Override
    public Set<DatumWebProperty> getWebViewMetadata() {
        Set<DatumWebProperty> metadata = super.getWebViewMetadata();

        metadata.add(new DatumWebProperty() {
            {
                Key = "Type";
                FriendlyName = "Type";
                Value = "Status Message";
            }
        });

        metadata.add(new DatumWebProperty() {
            {
                Key = "Status";
                FriendlyName = "Status";
                Value = _message.getMessage();
            }
        });

        metadata.add(new DatumWebProperty() {
            {
                Key = "Date";
                FriendlyName = "Date";
                Value = DateUtil.ToPrettyDate(
                        new DateTime(_message.getUpdatedTime()));
            }
        });

        return metadata;
    }

    @Override
    public String getWebViewClass() {
        return "facebookstatusdatum";
    }

    @Override
    public void processPeopleTextFields() {
        String message = this._message.getMessage();

        if (message != null) {
            for (String friendName : this._user.getLifeStory().FriendIDByName.keySet()) {
                if (message.contains(friendName)) {
                    {
                        PersonAnnotation personAtEvent = PersonAnnotation.fromFreeText(friendName, this._user.getLifeStory().FriendIDByName, "status_mention");
                        this.getAnnotations().People.add(personAtEvent);
                    }
                }
            }
        }
    }

    @Override
    public DateTime getContentAddedDateTime() {
        return new DateTime(_message.getUpdatedTime());
    }

    @Override
    protected void appendTextToGATE(GateSubDocumentWriter doc) throws InvalidOffsetException {
        doc.appendLine(this._message.getMessage(), "message");
    }

    @Override
    public void processMetadataFields(ExperimentOptions opt) throws IOException,
            URISyntaxException {

        {
            NamedFacebookType from = this._message.getFrom();
            PersonAnnotation personAtEvent = PersonAnnotation.From(from, "status_from");
            this.getAnnotations().People.add(personAtEvent);
        }

        {
            TemporalAnnotation temporalAnno = new TemporalAnnotation();
            temporalAnno.SourceDataKind= DataKind.Metadata;
            temporalAnno.setDensity(new SingleDayTimeDensity(_message.getUpdatedTime()));
            temporalAnno.isDefinitive = false;
            this.getAnnotations().DateTimesAnnotations.add(temporalAnno);
        }

    }
}
