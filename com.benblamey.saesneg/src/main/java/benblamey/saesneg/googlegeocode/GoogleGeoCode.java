package com.benblamey.saesneg.googlegeocode;

import com.benblamey.core.MySQLWebCache;
import com.benblamey.saesneg.model.annotations.LocationAnnotation;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.util.JSON;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class GoogleGeoCode {

    public static LocationAnnotation geoCode(String address) throws IOException, URISyntaxException {

        if (address == null || address.equals("null")) {
            return null;
        }

        // Note missing "https:" is added in the line below.
        String url = "//maps.googleapis.com/maps/api/geocode/json?"
                //+ "region=.co.uk"
                + "address=" + address
                + "&key=" + GOOGLE_ACCESS_KEY;

        // URI Encode as neccessary.
        URI uri = new URI("https", url, null);

        //String url = "http://www.google.com";
        String rawResults = MySQLWebCache.get(uri.toASCIIString(), "application/json");

        System.out.println(rawResults);

        // Json is always UTF-8
        BasicDBObject parsedResults = (BasicDBObject) JSON.parse(rawResults);

        BasicDBList results = (BasicDBList) parsedResults.get("results");

        if (results.size() > 1 || results.size() == 0) {
            // If its ambiguous, ignore the results -- query was probably something vague like "my house".
            return null;
        }

        BasicDBObject result = (BasicDBObject) results.get(0);

        BasicDBObject geometry = (BasicDBObject) result.get("geometry");
        BasicDBObject location = (BasicDBObject) geometry.get("location");
        Double lat = (Double) location.get("lat");
        Double lng = (Double) location.get("lng");

        LocationAnnotation locAnn = new LocationAnnotation();
        locAnn.setLat(lat);
        locAnn.setLat(lng);
        locAnn.Level = 11; // Use OSM n'hood level for street addresses.

        locAnn.setOriginalText(address);
        locAnn.addNote("Google Address: " + (String) result.get("formatted_address"));

        return locAnn;

        /*
		{
			   "results" : [
			      {
			         "address_components" : [
			            {
			               "long_name" : "1600",
			               "short_name" : "1600",
			               "types" : [ "street_number" ]
			            },
			            {
			               "long_name" : "Amphitheatre Parkway",
			               "short_name" : "Amphitheatre Pkwy",
			               "types" : [ "route" ]
			            },
			            {
			               "long_name" : "Mountain View",
			               "short_name" : "Mountain View",
			               "types" : [ "locality", "political" ]
			            },
			            {
			               "long_name" : "Santa Clara County",
			               "short_name" : "Santa Clara County",
			               "types" : [ "administrative_area_level_2", "political" ]
			            },
			            {
			               "long_name" : "California",
			               "short_name" : "CA",
			               "types" : [ "administrative_area_level_1", "political" ]
			            },
			            {
			               "long_name" : "United States",
			               "short_name" : "US",
			               "types" : [ "country", "political" ]
			            },
			            {
			               "long_name" : "94043",
			               "short_name" : "94043",
			               "types" : [ "postal_code" ]
			            }
			         ],
			         "formatted_address" : "1600 Amphitheatre Parkway, Mountain View, CA 94043, USA",
			         "geometry" : {
			            "location" : {
			               "lat" : 37.4222953,
			               "lng" : -122.0840671
			            },
			            "location_type" : "ROOFTOP",
			            "viewport" : {
			               "northeast" : {
			                  "lat" : 37.42364428029151,
			                  "lng" : -122.0827181197085
			               },
			               "southwest" : {
			                  "lat" : 37.42094631970851,
			                  "lng" : -122.0854160802915
			               }
			            }
			         },
			         "types" : [ "street_address" ]
			      }
			   ],
			   "status" : "OK"
			}
         */
    }

    // https://maps.googleapis.com/maps/api/geocode/json?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&key=????????????????
    private static final String GOOGLE_ACCESS_KEY = "???????????????";
}
