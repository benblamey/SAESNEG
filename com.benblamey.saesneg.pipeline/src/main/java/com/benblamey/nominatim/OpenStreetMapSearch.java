package com.benblamey.nominatim;

import com.benblamey.core.SQLUtils;
import com.benblamey.core.StringUtils;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * OSM Search. Tables need to be prepared first by running @see
 * {@link BuildNewTablesMain}
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class OpenStreetMapSearch {

    private PreparedStatement _selectFrom_search_name_x;
    private PreparedStatement _selectFrom_placex;
    private PreparedStatement _selectFrom_word;
    //private PreparedStatement _selectFrom_wordBackwards;
    private Connection _con;
    private PreparedStatement _selectFrom_location_area_large_x;
    private PreparedStatement _selectFrom_location_area_country; // schema the same as '_selectFrom_location_area_large_x'
    private OpenStreetMapSearchAlgorithmOptions _options;
    private PreparedStatement _selectPlaceData;
    private PreparedStatement _findParentRelation;

    public static final Map<String, OpenStreetMapElementKind> PLACE_KIND_BY_LETTER = new HashMap<String, OpenStreetMapElementKind>() {
        {
            put("N", OpenStreetMapElementKind.Node);
            put("W", OpenStreetMapElementKind.Way);
            put("R", OpenStreetMapElementKind.Relation);
            put("P", OpenStreetMapElementKind.PostCode);
        }
    };

    public static final Map<OpenStreetMapElementKind, String> PLACE_KIND_BY_ENUM = new HashMap<OpenStreetMapElementKind, String>() {
        {
            put(OpenStreetMapElementKind.Node, "N");
            put(OpenStreetMapElementKind.Way, "W");
            put(OpenStreetMapElementKind.Relation, "R");
            put(OpenStreetMapElementKind.PostCode, "P");
        }
    };

    static {
        org.postgresql.Driver d = new org.postgresql.Driver();
        try {
            DriverManager.registerDriver(d);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public OpenStreetMapSearch(OpenStreetMapSearchAlgorithmOptions options) throws SQLException {
        this._options = options;

        _con = DriverManager.getConnection(
                OpenStreetMapConfig.JDBC_POSTGRESQL_NOMINATIM,
                OpenStreetMapConfig.JDBC_POSTGRESQL_NOMINATIM_USER,
                OpenStreetMapConfig.JDBC_POSTGRESQL_NOMINATIM_PASSWORD);

        //_selectFrom_wordBackwards = _con.prepareStatement("SELECT word_token FROM word WHERE word_id = ? LIMIT 1");
        _selectFrom_word = _con.prepareStatement("SELECT word_id FROM word WHERE word_token = ? LIMIT 1");

        _findParentRelation = _con.prepareStatement("SELECT * FROM planet_osm_rels WHERE parts && ARRAY[?]");

        String whereClause;
        switch (_options.nameLookup) {
            case ANY:
                whereClause = " WHERE name_vector && ARRAY[?]"; // This is the one that uses the index.
                break;
            case FIRST:
                whereClause = " WHERE ? = name_vector[1] ";
                break;
            default:
                throw new RuntimeException("unrecognized enum.");
        }
        String query = "SELECT DISTINCT ON (place_id) place_id, name_vector FROM search_name_all " + whereClause;
        _selectFrom_search_name_x = _con.prepareStatement(query);

        String whereClause2;
        switch (_options.nameLookup) {
            case ANY:
                whereClause2 = " WHERE keywords && ARRAY[?]";
                break;
            case FIRST:
                whereClause2 = " WHERE ? = keywords[1] ";
                break;
            default:
                throw new RuntimeException("unrecognized enum.");
        }
        String query2 = "SELECT DISTINCT ON (place_id) place_id, keywords FROM location_area_large_all " + whereClause2;
        _selectFrom_location_area_large_x = _con.prepareStatement(query2);
        String query3 = "SELECT DISTINCT ON (place_id) place_id, keywords FROM location_area_country " + whereClause2;
        _selectFrom_location_area_country = _con.prepareStatement(query3);

        // osm_id, name, rank_search, calculated_country_code, isin
        _selectFrom_placex = _con.prepareStatement("SELECT place_id, osm_type, osm_id, class, type, name, admin_level, isin, rank_address, rank_search, calculated_country_code, ST_X(centroid) as lon, ST_Y(centroid) as lat FROM placex WHERE place_id = ? LIMIT 1");

        // We don't specify the osm_type here - it is not known for our old test data.
        _selectPlaceData = _con.prepareStatement("SELECT *, ST_X(centroid) as lon, ST_Y(centroid) as lat FROM placex WHERE osm_id = ? AND placex.osm_type != 'P'");
    }

    public void close() throws SQLException {
        _con.close();
    }

    /**
     * Converts a search string into an ID number (we can cache these values
     * inside java if we wish).
     */
    public int getSearchStringID(String s) throws SQLException {
        s = toStandardFormWithLeadingSpace(s);

        if (StringUtils.IsNullOrEmpty(s)) {
            return -1;
        }

        _selectFrom_word.setString(1, s);

        ResultSet result_word = _selectFrom_word.executeQuery();

        int word_id;
        if (!result_word.next()) {
            word_id = -1;
        } else {
            word_id = result_word.getInt("word_id");
        }
        result_word.close();
        return word_id;
    }

    /**
     * Search for different places that match the search string represented by
     * the ID.
     */
    public List<OpenStreetMapSearchResult> searchForString(int stringID) throws SQLException {
        ArrayList<OpenStreetMapSearchResult> results = new ArrayList<>();

        if (stringID < 0) {
            return results;
        }

        search_name(stringID, results);
        location_area_large(stringID, results);
        location_area_country(stringID, results);

        // Sort by rank.
        Collections.sort(results, new Comparator<OpenStreetMapSearchResult>() {
            @Override
            public int compare(OpenStreetMapSearchResult o1, OpenStreetMapSearchResult o2) {
                boolean o1gb = "gb".equals(o1.calculated_country_code);
                boolean o2gb = "gb".equals(o2.calculated_country_code);
                if (o1gb && !o2gb) {
                    return -1;
                } else if (!o1gb && o2gb) {
                    return +1;
                }
                return Integer.compare(o1.rank_search, o2.rank_search);
            }
        });

        return results;
    }

    /**
     * Search the "search_name" tables for places matching the query string.
     *
     * @param stringID
     * @param results
     * @throws SQLException
     */
    private void search_name(int stringID, List<OpenStreetMapSearchResult> results) throws SQLException {
        _selectFrom_search_name_x.setInt(1, stringID);

        ResultSet result_search_name_x = _selectFrom_search_name_x.executeQuery();

        while (result_search_name_x.next()) {
            int place_id = result_search_name_x.getInt("place_id");
            String source = "search_name_x";
            OpenStreetMapSearchResult result = getResult(place_id, source);
            results.add(result);
        }

        result_search_name_x.close();
    }


    private static HashMap<Integer, OpenStreetMapSearchResult> RESULT_CACHE = new HashMap<Integer,OpenStreetMapSearchResult>();

    private OpenStreetMapSearchResult getResult(int place_id, String source) throws SQLException {

        OpenStreetMapSearchResult result = RESULT_CACHE.get(place_id);

        if (result == null) {
            result = new OpenStreetMapSearchResult();

            result.placeID = place_id;

            // Record the source.
            result.source = source;

            // Fill in the rest of the other details from other tables.
            _selectFrom_placex.setInt(1, result.placeID); // Unlike osm_id, place ID is globally unique, the kind is not needed.
            ResultSet result_placex = _selectFrom_placex.executeQuery();
            result_placex.next();
            result.readValuesFromPlacexResult(result_placex);
            result_placex.close();

            RESULT_CACHE.put(place_id, result);
        }

        return result;
    }

    /**
     * Search the "location_area_large" tables for places matching the query
     * string.
     *
     * @param stringID
     * @param results
     * @throws SQLException
     */
    private void location_area_large(int stringID, List<OpenStreetMapSearchResult> results) throws SQLException {
        _selectFrom_location_area_large_x.setInt(1, stringID);
        ResultSet result_location_area_large_x = _selectFrom_location_area_large_x.executeQuery();

        while (result_location_area_large_x.next()) {
            int placeID = result_location_area_large_x.getInt("place_id");
            String source = "location_area_large_x";

            // Fill in the rest of the other details from other tables.
            results.add(getResult(placeID, source));
        }
        result_location_area_large_x.close();
    }

    /**
     * Search the "location_area_country" tables for places matching the query
     * string.
     *
     * @param stringID
     * @param results
     * @throws SQLException
     */
    private void location_area_country(int stringID, List<OpenStreetMapSearchResult> results) throws SQLException {
        _selectFrom_location_area_country.setInt(1, stringID);
        ResultSet result_location_area_country = _selectFrom_location_area_country.executeQuery();

        while (result_location_area_country.next()) {
            OpenStreetMapSearchResult result = getResult(result_location_area_country.getInt("place_id"), "location_area_country");
            results.add(result);
        }
        result_location_area_country.close();
    }


    // Not really in use.
    /**
     * Get a debug-style dump of all the information we have in the database
     * about a place.
     *
     * @param osmID
     * @param kind
     * @return
     * @throws SQLException
     */
    public OpenStreetMapSearchResult search_for_osm_id(Long osmID, OpenStreetMapElementKind kind) throws SQLException {

        if (kind == OpenStreetMapElementKind.PostCode) {
            throw new IllegalArgumentException("postcode not supported.");
        }

        _selectPlaceData.setLong(1, osmID);
        ResultSet resultSet = _selectPlaceData.executeQuery();
        //List<Map<String, Object>> allResultData = SQLUtils.getAllResultData(resultSet);
        //Map<String, Object> filteredResultData = null;
        OpenStreetMapSearchResult result = null;

        boolean throwIfDifferent = (kind == OpenStreetMapElementKind.DontKnow);

        while (resultSet.next()) {

            //for (Map<String, Object> result : allResultData) {
            OpenStreetMapElementKind resultKind = PLACE_KIND_BY_LETTER.get(resultSet.getString("osm_type"));

            if (resultKind == OpenStreetMapElementKind.PostCode) {
                // We ignore postcodes.
                continue;
            } else if (kind == OpenStreetMapElementKind.DontKnow) {
                // Assume the kind of the first match.
                kind = resultKind;
            } else if (throwIfDifferent && kind != resultKind) {
                throw new IllegalStateException("Multiple matches for osm_id = " + osmID);
            } else if (!throwIfDifferent && kind != resultKind) {
                // Filter out matches of the wrong kind.
                continue;
            }

            result = new OpenStreetMapSearchResult();
            result.readValuesFromPlacexResult(resultSet);
        }

        resultSet.close();

        return result;
    }

    /**
     * Search for relations which have this element as a member.
     *
     * @param osmID
     * @param kind
     * @return
     * @throws SQLException
     */
    public List<Map<String, Object>> findParentRelations(Long osmID, OpenStreetMapElementKind kind) throws SQLException {
        _findParentRelation.setLong(1, osmID);
        ResultSet resultSet = _findParentRelation.executeQuery();

        String wayID = "w" + osmID.toString();
        String nodeID = "n" + osmID.toString();
        String relationID = "r" + osmID.toString();

        List<Map<String, Object>> results = new ArrayList<>();

        while (resultSet.next()) {
            // We know the relation contains the member, but we arn't sure if the element type is correct.

            String relationship = null;

            Array membersSQLArray = resultSet.getArray("members");

            List<String> members = Arrays.asList((String[]) membersSQLArray.getArray());

//				if (array2.contains(null)) {
//					System.out.println("argh");
//				}
//
//				for (Object obj : array2) {
//					if (!(obj instanceof String)) {
//						"".toString();
//					}
//				}
//
//						String[] array3 = array2.toArray(new String[0]);
//				List<String> members = Arrays.asList(array3);
            if (kind == kind.DontKnow || kind == kind.Way) {
                int indexOf = members.indexOf(wayID);
                if (indexOf >= 0) {
                    relationship = (String) members.get(indexOf + 1);
                }
            }

            if (kind == kind.DontKnow || kind == kind.Node) {
                int indexOf = members.indexOf(nodeID);
                if (indexOf >= 0) {
                    relationship = (String) members.get(indexOf + 1);
                }
            }

            if (kind == kind.DontKnow || kind == kind.Relation) {
                int indexOf = members.indexOf(relationID);
                if (indexOf >= 0) {
                    relationship = (String) members.get(indexOf + 1);
                }
            }

            if (relationship != null) {

                Map<String, Object> resultRow = SQLUtils.getResultRow(resultSet);
                resultRow.put("RELATIONSHIP", relationship);
                results.add(resultRow);
            }
        }

        return results;
    }

//	private void location_area_large_debug_keywords(ResultSet result_location_area_large_x, OpenStreetMapSearchResult result) throws SQLException {
//	ResultSet nameVector = result_location_area_large_x.getArray("keywords").getResultSet();
//	result.tokenDebug = "";
//
//	while (nameVector.next()) {
//		// The first column in the result is just the index in the array.
//		int wordID = nameVector.getInt(2);
//		_selectFrom_wordBackwards.setInt(1, wordID);
//		ResultSet result_lookupWord = _selectFrom_wordBackwards.executeQuery();
//		if (result_lookupWord.next()) {
//			result.tokenDebug += " \"" + wordID + ":" + result_lookupWord.getString("word_token") + "\"";
//		}
//	}
//	nameVector.close();
//}
//	private void search_name_token_debug(ResultSet result_search_name_x, OpenStreetMapSearchResult result) throws SQLException {
//		ResultSet nameVector = result_search_name_x.getArray("name_vector").getResultSet();
//		result.tokenDebug = "";
//		while (nameVector.next()) {
//			// The first column in the result is just the index in the array.
//			int wordID = nameVector.getInt(2);
//			_selectFrom_wordBackwards.setInt(1, wordID);
//			ResultSet result_lookupWord = _selectFrom_wordBackwards.executeQuery();
//			if (result_lookupWord.next()) {
//				result.tokenDebug += " \"" + wordID + ":" + result_lookupWord.getString("word_token") + "\"";
//			}
//			result_lookupWord.close();
//		}
//		nameVector.close();
//	}
    // Flag to enable support for unicode character classes.
    static final Pattern NON_LETTER_NOR_SPACE_PATTERN = Pattern.compile("[^\\p{L}\\s]+", Pattern.UNICODE_CHARACTER_CLASS);

    /**
     * Convert a query string to standard form. Based on:
     * Nominatim/module/nominatim.c gettokenstring()
     *
     * @see
     * <a href="https://trac.openstreetmap.org/browser/subversion/applications/utils/nominatim/module/nominatim.c">https://trac.openstreetmap.org/browser/subversion/applications/utils/nominatim/module/nominatim.c</a>
     * @param s The query string to normalize.
     * @return The normalized query string.
     */
    public static String toStandardFormWithLeadingSpace(String s) {
        s = s.toLowerCase();

        // Replace chars that are neither whitespace nor letters with empty apace.
        s = NON_LETTER_NOR_SPACE_PATTERN.matcher(s).replaceAll(" ");

        // Remove leading and trailing whitespace.
        s = s.trim();

        s = s.replaceAll("\\s\\s+", " ");

        // 'and' in various languages
        s = s.replace(" and ", " ");
        s = s.replace(" und ", " ");
        s = s.replace(" en ", " ");
        s = s.replace(" et ", " ");
        s = s.replace(" y ", " ");

        // 'the' (and similar)
        s = s.replace(" the ", " ");
        s = s.replace(" der ", " ");
        s = s.replace(" den ", " ");
        s = s.replace(" die ", " ");
        s = s.replace(" das ", " ");
        s = s.replace(" la ", " ");
        s = s.replace(" le ", " ");
        s = s.replace(" el ", " ");
        s = s.replace(" il ", " ");

        // german
        s = s.replace("ae", "a");
        s = s.replace("oe", "o");
        s = s.replace("ue", "u");
        s = s.replace("sss", "ss");
        s = s.replace("ih", "i");
        s = s.replace("eh", "e");

        // russian
        s = s.replace("ie", "i");
        s = s.replace("yi", "i");

        // Remove leading and trailing whitespace.
        s = s.trim();

        // Leading space represents an exact string match (see email from
        // Nominatim developer).
        s = " " + s;

        return s;
    }

}
