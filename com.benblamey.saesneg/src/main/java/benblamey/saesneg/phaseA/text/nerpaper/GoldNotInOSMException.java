package benblamey.saesneg.phaseA.text.nerpaper;

import benblamey.nominatim.OpenStreetMapSearchResult;

public class GoldNotInOSMException extends Exception {

    public OpenStreetMapSearchResult OSMResult;

    public GoldNotInOSMException(OpenStreetMapSearchResult right) {
        this.OSMResult = right;
    }

}
