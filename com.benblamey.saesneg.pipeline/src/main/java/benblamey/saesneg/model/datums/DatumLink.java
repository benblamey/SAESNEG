package com.benblamey.saesneg.model.datums;

import com.benblamey.core.DateUtil;
import com.benblamey.saesneg.evaluation.DatumWebProperty;
import com.benblamey.saesneg.experiments.ExperimentOptions;
import com.benblamey.saesneg.model.UserContext;
import com.benblamey.saesneg.phaseA.text.gatesubdocument.GateSubDocumentWriter;
import com.restfb.types.Link;
import gate.util.InvalidOffsetException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Set;
import org.joda.time.DateTime;

@Deprecated
public class DatumLink extends Datum {

    public Link _link;

    DatumLink() { // Serialization ctor.
    }

    public DatumLink(UserContext _user, Link data) {
        super(_user, data);
        _link = data;
    }

    @Override
    public String getWebViewTitle() {
        return _link.getName();
    }

    @Override
    public Set<DatumWebProperty> getWebViewMetadata() {
        Set<DatumWebProperty> metadata = super.getWebViewMetadata();

        metadata.add(new DatumWebProperty() {
            {
                Key = "Type";
                FriendlyName = "Type";
                Value = "Link";
            }
        });

        metadata.add(new DatumWebProperty() {
            {
                Key = "Link";
                FriendlyName = "Link";
                Value = _link.getLink();
            }
        });

        metadata.add(new DatumWebProperty() {
            {
                Key = "Description";
                FriendlyName = "Desc.";
                Value = _link.getDescription();
            }
        });

        metadata.add(new DatumWebProperty() {
            {
                Key = "Status"; // For consistency with status message.
                FriendlyName = "Status";
                Value = _link.getMessage();
            }
        });

        metadata.add(new DatumWebProperty() {
            {
                Key = "Date";
                FriendlyName = "Date";
                Value = DateUtil.ToPrettyDate(
                        new DateTime(_link.getCreatedTime()));
            }
        });

        return metadata;
    }

    @Override
    public String getWebViewClass() {
        return "facebooklinkdatum";
    }

    @Override
    public DateTime getContentAddedDateTime() {
        return new DateTime(_link.getCreatedTime());
    }

    @Override
    public String getImageThumbnailURL() {
        return _link.getPicture();
    }

    @Override
    public String getFullImageURL() {
        return _link.getPicture();
    }

    @Override
    protected void appendTextToGATE(GateSubDocumentWriter doc) throws InvalidOffsetException {
        doc.appendLine(this._link.getName(), "name");
        doc.appendLine(this._link.getMessage(), "message");
        doc.appendLine(this._link.getDescription(), "description");

    }

    @Override
    public void processMetadataFields(ExperimentOptions opt) throws IOException,
            URISyntaxException {
        // No location fields.
    }
}
