package com.benblamey.saesneg.phaseA.text.nerpaper;

import com.benblamey.core.MongoClientInstance;
import com.benblamey.nominatim.OpenStreetMapElementKind;
import com.benblamey.nominatim.OpenStreetMapSearch;
import com.benblamey.nominatim.OpenStreetMapSearchResult;
import com.benblamey.core.GIS.Haversine;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import java.sql.SQLException;
import java.util.Scanner;

public class SamePlaceCalculator {

    private OpenStreetMapSearch _search;
    private DBCollection _goldMongoCollection;

    public SamePlaceCalculator() {
        _goldMongoCollection = MongoClientInstance.getClientLocal().getDB("OSM_Gold").getCollection("osm_places");
    }

    /**
     *
     * @param goldID - will probably just be an OSM ID.
     * @return
     * @throws SQLException
     * @throws GoldNotInOSMException
     */
    public boolean areOSMplacesTheSame(final OpenStreetMapSearchResult candidate, final Long goldID) throws SQLException, GoldNotInOSMException {
        // If the ID is the same, we're done (we assume the types match).
        if (candidate.osm_id.equals(goldID)) {
            return true;
        }

        Boolean areEqual = null;

        // See if we have cached the human judgement.
        BasicDBObject goldLabelEqualityCacheObject = new BasicDBObject() {
            {
                put("CANDIDATE_OSM_ID", candidate.osm_id);
                put("CANDIDATE_OSM_TYPE", OpenStreetMapSearch.PLACE_KIND_BY_ENUM.get(candidate.osm_type));
                put("GOLD_OSM_ID", goldID);
            }
        };
        DBObject findOne = _goldMongoCollection.findOne(goldLabelEqualityCacheObject);
        if (findOne != null) {
            return (Boolean) findOne.get(OSMExperimentMain.ARE_EQUAL);
        }

        // Look up the gold element in OSM.
        OpenStreetMapSearchResult gold = _search.search_for_osm_id(goldID, OpenStreetMapElementKind.DontKnow);
        OpenStreetMapElementKind gold_osm_type = null;

        // If the lookup is successful, we may have enough information to automatically make a judgement.
        if (gold != null) {
            goldLabelEqualityCacheObject.put("GOLD_OSM_TYPE", gold.osm_type);
            gold_osm_type = gold.osm_type;

            Double leftLat = (Double) candidate.lat;
            Double leftLong = (Double) candidate.lon;

            Double rightLat = (Double) gold.lat;
            Double rightLong = (Double) gold.lon;

            double distInMiles = Haversine.distInMiles(leftLat, leftLong, rightLat, rightLong);

            String leftName = candidate.name;
            String rightName = gold.name;

            System.out.print("Comparing: " + leftName + " (" + candidate.osm_id + ") and " + rightName + " (" + gold.osm_id + ") - ");

            String leftType = candidate.osm_sub_class;
            String rightType = gold.osm_sub_class;

            Double farAwayMiles;

            if (leftType.equals("hamlet") || rightType.equals("hamlet")) {
                farAwayMiles = 15.0;
            } else if (leftType.equals("village") || rightType.equals("village")) {
                farAwayMiles = 15.0;
            } else if (leftType.equals("suburb") || rightType.equals("suburb")) {
                farAwayMiles = 30.0;
            } else if (leftType.equals("town") || rightType.equals("town")) {
                farAwayMiles = 30.0;
            } else {
                farAwayMiles = 200.0;
            }

            //			if (findParentRelations.size() == 1) {
            //				Map<String, Object> parent = findParentRelations.get(0);
            //
            //				String relationship = (String)parent.get("RELATIONSHIP");
            //				if (relationship.equals("admin_centre") && gold.name.equals(candidate.name)) {
            //					System.out.println("admin_centre and name the same.");
            //					areEqual = true;
            //				}
            //			}
            if (distInMiles > farAwayMiles) {
                System.out.println(" " + distInMiles + " miles apart, assuming different.");
                areEqual = false;
            }
        }

        // If we have made a judgement automatically, return it.
        if (areEqual != null) {
            return areEqual;
        }

        Scanner sc = new Scanner(System.in);

        // Otherwise, we need to request human judgement, so we should resolve any type ambiguity.
        while (gold_osm_type == null) {
            System.out.println("What type is this element (n/w/r)?");
            System.out.println("\thttp://www.openstreetmap.org/???/" + goldID);
            String next = sc.next();

            if (next.equalsIgnoreCase("n")) {
                gold_osm_type = OpenStreetMapElementKind.Node;
            }
            if (next.equalsIgnoreCase("w")) {
                gold_osm_type = OpenStreetMapElementKind.Way;
            }
            if (next.equalsIgnoreCase("r")) {
                gold_osm_type = OpenStreetMapElementKind.Relation;
            }

            if (gold_osm_type == null) {
                System.out.println("Please enter n/w/r.");
            }
        }

        sc.close();

        goldLabelEqualityCacheObject.put("GOLD_OSM_TYPE", OpenStreetMapSearch.PLACE_KIND_BY_ENUM.get(gold_osm_type));

        while (areEqual == null) {
            System.out.println("Are these the same place (y/n)?");
            System.out.println("\tCandidate:  http://www.openstreetmap.org/" + OSMExperimentMain.kindToURLString(candidate.osm_type) + "/" + candidate.osm_id);
            System.out.println("\tGold:  http://www.openstreetmap.org/" + OSMExperimentMain.kindToURLString(gold_osm_type) + "/" + goldID);

            String next = sc.next();

            if (next.equalsIgnoreCase("y")) {
                areEqual = true;
            }
            if (next.equalsIgnoreCase("n")) {
                areEqual = false;
            }

            if (areEqual == null) {
                System.out.println("Response not understood. Please answer y or n.");
            }
        }

        // Record the human judgement.
        Object put = goldLabelEqualityCacheObject.put(OSMExperimentMain.ARE_EQUAL, areEqual);
        _goldMongoCollection.insert(goldLabelEqualityCacheObject);

        return areEqual;
    }

}
