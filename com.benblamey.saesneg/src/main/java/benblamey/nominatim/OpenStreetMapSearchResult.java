package com.benblamey.nominatim;

import com.benblamey.saesneg.model.annotations.LocationAnnotation;
import com.mongodb.util.JSON;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents a result or place found during an OSM search.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class OpenStreetMapSearchResult extends LocationAnnotation {

    // Order as in placex table.
    public int placeID;
    public OpenStreetMapElementKind osm_type;
    public Long osm_id;

    public String osm_class;
    public String osm_sub_class;
    public String calculated_country_code;
    public Integer rank_search; // Lower is "bigger".
    public String isin;
    public String name;
    public Integer admin_level;
    public Integer rank_address;
    public Double lon;
    public Double lat;

    public String source;
    public Integer partition;
    public String tokenDebug;

    public OpenStreetMapSearchResult() {
    }

    @Deprecated
    public OpenStreetMapSearchResult(JSONObject result2) throws JSONException {

        lat = Double.parseDouble(result2.getString("lat"));
        lon = Double.parseDouble(result2.getString("lon"));
        osm_id = Long.parseLong(result2.getString("place_id"));
        name = result2.getString("display_name");
    }

    public OpenStreetMapSearchResult(Map<Object, Object> fm) {

        placeID = (Integer) fm.get("placeID");
        osm_type = OpenStreetMapSearch.PLACE_KIND_BY_LETTER.get((String) fm.get("osm_type"));

        Object osmID = fm.get("osm_id");
        osm_id = osmID instanceof String ? Long.parseLong((String) osmID) : (Long) osmID;

        osm_class = (String) fm.get("osm_class");
        osm_sub_class = (String) fm.get("osm_sub_class");

        name = (String) fm.get("name");
        Level = admin_level = (Integer) fm.get("admin_level");
        isin = (String) fm.get("isin");
        rank_address = (Integer) fm.get("rank_address");
        rank_search = (Integer) fm.get("rank_search");
        calculated_country_code = (String) fm.get("calculated_country_code");

        lat = (Double) fm.get("lat");
        lon = (Double) fm.get("lon");

        source = (String) fm.get("source");
        partition = (Integer) fm.get("partition");
        tokenDebug = (String) fm.get("tokenDebug");
    }

    /**
     * Converts to a Map<>, (to allow conversion to a FeatureMap and thus
     * storage inside a GATE document)
     *
     * @return
     */
    public Map<String, Object> toMap() {
        Map<String, Object> newFeatureMap = new HashMap<>();
        newFeatureMap.put("placeID", placeID);
        newFeatureMap.put("osm_type", OpenStreetMapSearch.PLACE_KIND_BY_ENUM.get(osm_type));

        newFeatureMap.put("osm_id", osm_id);

        newFeatureMap.put("osm_class", osm_class);
        newFeatureMap.put("osm_sub_class", osm_sub_class);

        newFeatureMap.put("name", name);
        newFeatureMap.put("admin_level", admin_level);
        newFeatureMap.put("isin", isin);
        newFeatureMap.put("rank_address", rank_address);
        newFeatureMap.put("rank_search", rank_search);
        newFeatureMap.put("calculated_country_code", calculated_country_code);

        newFeatureMap.put("lat", lat);
        newFeatureMap.put("lon", lon);

        newFeatureMap.put("source", source);
        newFeatureMap.put("partition", partition);
        newFeatureMap.put("tokenDebug", tokenDebug);

        return newFeatureMap;
    }

    public void readValuesFromPlacexResult(ResultSet result_placex) throws SQLException {
        // Ordering by column ordering in placex table.

        placeID = result_placex.getInt("place_id");
        osm_type = OpenStreetMapSearch.PLACE_KIND_BY_LETTER.get(result_placex.getString("osm_type"));
        if (osm_type == null) {
            throw new RuntimeException("Kind not read from database.");
        }

        osm_id = result_placex.getLong("osm_id");

        osm_class = result_placex.getString("class");
        osm_sub_class = result_placex.getString("type");

        name = (String) ((HashMap) result_placex.getObject("name")).get("name");
        admin_level = result_placex.getInt("admin_level");
        isin = result_placex.getString("isin");
        rank_address = result_placex.getInt("rank_address");
        rank_search = result_placex.getInt("rank_search");
        calculated_country_code = result_placex.getString("calculated_country_code");

        lon = result_placex.getDouble("lon");
        lat = result_placex.getDouble("lat");

		//name = result_placex.;
        //Object foo = result_placex.getObject(columnIndex)
        // result.partition -- from before I joined the tables.
        //placex_name = result_placex.getString("name");
        //rank_search = result_placex.getInt("rank_search"); // Lower numbers are "more important" places.
        // result.source -- already filled in.
        // result.tokenDebug -- only used for debug.
    }

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
        return name;
    }

    @Override
    public String toString() {
        return JSON.serialize(this.toMap());
        //return "osm_id= " + osm_id + " place_id= "+ placeID + " isin: " + isin +  " name: " + name  + " source: " + source + " " + tokenDebug;
    }

}
