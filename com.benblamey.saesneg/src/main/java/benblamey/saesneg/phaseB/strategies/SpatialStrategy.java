package com.benblamey.saesneg.phaseB.strategies;

import com.benblamey.saesneg.experiments.PhaseBOptions;
import com.benblamey.saesneg.model.annotations.LocationAnnotation;
import com.benblamey.saesneg.model.datums.Datum;
import com.benblamey.saesneg.phaseB.DatumPairSimilarity;
import com.benblamey.saesneg.phaseB.DatumSimilarityEvidence;
import com.benblamey.saesneg.phaseB.FestibusFeatures;
import com.benblamey.core.GIS.Haversine;
import java.util.ArrayList;
import java.util.List;

public class SpatialStrategy extends Strategy {

    public static final List<Double> osmAdminLevelToMinDist = new ArrayList<Double>() { {
        add(200.0); //1
        add(200.0); //2
        add(200.0); //3
        add(200.0); //4
        add(50.0); // 5
        add(30.0);//6
        add(5.0); //7
        add(5.0); //8
        add(3.0); //9
        add(3.0); //10
        add(1.0); //11
        add(1.0); //12
        add(1.0);//13
        add(1.0);//14
        add(1.0);//15
    } };

    // 'London' in one of the Scottish islands has an admin_level of 15.
    // http://www.openstreetmap.org/node/1338311783#map=7/59.135/-3.049


    @Override
    public void addEvidenceToPair(DatumPairSimilarity pair, PhaseBOptions options) {

        Datum left = pair.getLeft();
        Datum right = pair.getRight();

        List<LocationAnnotation> leftLocations = left.getAnnotations().Locations;
        List<LocationAnnotation> rightLocations = right.getAnnotations().Locations;

        double minDistInMiles = Double.POSITIVE_INFINITY;
        String messageForEvidence = "";

        for (LocationAnnotation leftLoc : leftLocations) {
            if (leftLoc == null) {
                leftLoc.toString();
            }

            for (LocationAnnotation rightLoc : rightLocations) {

                if (rightLoc == null) {
                    rightLoc.toString();
                }

                double foo = leftLoc.getLat()
                        + leftLoc.getLon()
                        + rightLoc.getLat()
                        + rightLoc.getLon();

                double distInMiles = Haversine.distInMiles(leftLoc.getLat(), leftLoc.getLon(), rightLoc.getLat(), rightLoc.getLon());

                double minDistLeft =  osmAdminLevelToMinDist.get(leftLoc.Level-1);
                double minDistRight =  osmAdminLevelToMinDist.get(rightLoc.Level-1);

                // Apply thresholding -- so that sim(UK,UK)!=0 miles.
                distInMiles = Math.max(distInMiles, Math.min(minDistLeft, minDistRight));

                String message = "comparison: " +
                        leftLoc.toString() + " <> " +
                        rightLoc.toString() + " " +
                        String.format("%.0f", distInMiles)
                        + "miles apart.";
                //System.out.println(message);
                if (distInMiles < minDistInMiles ) {
                    minDistInMiles = distInMiles;
                    messageForEvidence = message;
                }
            }
        }

        if (minDistInMiles < 1) {
            minDistInMiles = 1;
        }

        if (minDistInMiles != Double.POSITIVE_INFINITY) {
            pair.addEvidence(new DatumSimilarityEvidence(FestibusFeatures.Spatial_SameLocation, 1.0/minDistInMiles, messageForEvidence));
        }
    }



//
//                boolean areEqual = false;
//
//                if (leftLoc instanceof GeoNamesPlace
//                        && rightLoc instanceof GeoNamesPlace
//                        && ((GeoNamesPlace) leftLoc).geonames_id == ((GeoNamesPlace) rightLoc).geonames_id) {
//                    pair.addEvidence(new DatumSimilarityEvidence(
//                            FestibusFeatures.Spatial_SameLocation,
//                            1,
//                            leftLoc.getOriginalText() + "(geonames)"));
//                    areEqual = true;
//                }
//
//                if (leftLoc instanceof OpenStreetMapSearchResult && rightLoc instanceof OpenStreetMapSearchResult) {
//
//                    OpenStreetMapSearchResult leftOSM = (OpenStreetMapSearchResult) leftLoc;
//                    OpenStreetMapSearchResult rightOSM = (OpenStreetMapSearchResult) rightLoc;
//
//                    if (leftOSM.osm_id == rightOSM.osm_id
//                            && leftOSM.osm_type == rightOSM.osm_type
//                            && leftOSM.osm_type != OpenStreetMapElementKind.DontKnow) {
//                        pair.addEvidence(new DatumSimilarityEvidence(
//                                FestibusFeatures.Spatial_SameLocation,
//                                1,
//                                leftLoc.getOriginalText() + "(osm)"));
//                    } else {
//
//                        double distInMiles = Haversine.distInMiles(leftOSM.lat, leftOSM.lon, rightOSM.lat, rightOSM.lon);
//
//                        System.out.println("Distance between " + leftOSM.name + " and " + rightOSM.name + " is " + distInMiles + " miles.");
//                    }
//                }
//
//                if (areEqual) {
//                    if (leftLoc.getOriginalText() != null) {
//                        if (locations.contains(leftLoc.getOriginalText().toLowerCase())) {
//                            continue;
//                        } else {
//
//                            locations.add(leftLoc.getOriginalText().toLowerCase());
//                        }
//                    }
//                }





}
