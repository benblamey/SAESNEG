package com.benblamey.saesneg.model.datums;

import com.benblamey.saesneg.experiments.ExperimentOptions;
import com.benblamey.saesneg.model.UserContext;
import com.benblamey.saesneg.phaseA.text.gatesubdocument.GateSubDocumentWriter;
import com.restfb.types.Album;
import com.restfb.types.FacebookType;
import com.restfb.types.NamedFacebookType;
import com.restfb.types.Photo;
import gate.util.InvalidOffsetException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.joda.time.DateTime;

public class DatumAlbum extends Datum {

    public Album _album;
    private List<FacebookType> _photos;

    public DatumAlbum(UserContext uc, NamedFacebookType data) {
        super(uc, data);

        if (data instanceof Album) {
            _album = (Album) data;
        } else {
            throw new IllegalArgumentException("data is not an Album.");
        }

        _photos = uc.fch.getConnectionWithPagination(_album.getId() + "/photos", Photo.class);

    }

    private DatumAlbum() {
        super();
    } // Serialization Ctor.

    public boolean isStructural() {
        return _album.getName().equals("Profile Pictures") || _album.getName().equals("Mobile Uploads");
    }

    @Override
    public boolean isPrimary() {
        // Don't show albums to the user.
        return false;
    }

    ;

    @Override
    public void processMetadataFields(ExperimentOptions opt) throws IOException, URISyntaxException {
        super.searchLocationMetadataField(_album.getLocation(), "location", opt);

    }

    @Override
    public String getWebViewClass() {
        return "facebookalbumdatum";
    }

    @Override
    protected void appendTextToGATE(GateSubDocumentWriter doc) throws InvalidOffsetException {
        if (_album != null) {
            doc.appendLine(this._album.getName(), "album_name");
            doc.appendLine(this._album.getDescription(), "album_description");
        }

    }

    @Override
    public DateTime getContentAddedDateTime() {
        return new DateTime(_album.getCreatedTime());

    }

    public void afterDeserializationFix() {
        // In old serialized versions of this class, DatumAlbum didn't descend from Datum.
        super._data = _album;
    }

}
