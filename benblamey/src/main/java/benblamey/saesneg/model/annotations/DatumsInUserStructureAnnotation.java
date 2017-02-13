package benblamey.saesneg.model.annotations;

import benblamey.saesneg.model.datums.DatumAlbum;

public class DatumsInUserStructureAnnotation extends Annotation {

    public DatumsInUserStructureAnnotation(DatumAlbum album) {
        _userStructureID = "fb_album" + album.getNetworkID();
        this.SourceDataKind = SourceDataKind.Metadata;
    }

    private String _userStructureID;

    public String getUserStructureID() {
        return _userStructureID;
    }

}
