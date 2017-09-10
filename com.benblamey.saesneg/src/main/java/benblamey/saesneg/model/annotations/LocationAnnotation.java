package com.benblamey.saesneg.model.annotations;

import com.restfb.types.Location;

public class LocationAnnotation extends Annotation {

    private double _latitude;
    private double _longitude;

    public LocationAnnotation() {
    }

    public LocationAnnotation(Location fbLocation, String originatingField, DataKind sourceDataKind) {
        _latitude = fbLocation.getLatitude();
        _longitude = fbLocation.getLongitude();
        Level = 11; // Use n'hood level for Facebook places, things like streets.

        this.Note += fbLocation.toString() + "\n";
        this.Note += "From FB Location. originating field: " + originatingField;
        this.SourceDataKind = sourceDataKind;
        this.setOriginalText(fbLocation.getCity() + "," + fbLocation.getCountry());
    }

    public double getLat() {
        return _latitude;
    }

    public void setLat(double lat) {
        _latitude = lat;
    }

    public double getLon() {
        return _longitude;
    }

    public void setLon(double lon) {
        _longitude = lon;
    }

    public int Level;

    @Override
    public String toString() {
        return super.toString() + " lat:" + String.format("%.2f",_latitude) + " lon:" + String.format("%.2f",_longitude);
    }



}
