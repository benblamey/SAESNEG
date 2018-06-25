package com.benblamey.saesneg.geonames;

import com.benblamey.saesneg.model.annotations.LocationAnnotation;

/**
 * A place found inside GeoNames.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
@Deprecated
public class GeoNamesPlace extends LocationAnnotation {

    public Double lat;
    public Double lon;
    public String originalText;
    public int geonames_id;

    @Override
    public double getLat() {
        return lat;
    }

    @Override
    public double getLon() {
        return lon;
    }

    @Override
    public String getOriginalText() {
        return originalText;
    }

}
