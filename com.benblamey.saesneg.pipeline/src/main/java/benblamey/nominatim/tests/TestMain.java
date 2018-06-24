package com.benblamey.nominatim.tests;

import com.benblamey.nominatim.NameLookupStrategy;
import com.benblamey.nominatim.OpenStreetMapElementKind;
import com.benblamey.nominatim.OpenStreetMapSearch;
import com.benblamey.nominatim.OpenStreetMapSearchAlgorithmOptions;
import com.benblamey.nominatim.OpenStreetMapSearchResult;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Tests for OSM.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class TestMain {

    public void run() throws SQLException {

        String text
                = "cardiff" //"New Zealand "
                //				  "Clapham "
                //				+ "The usual clear liquids rule applies. (Comments and complaints about this rule on the back of a postcard to: Flat 2907, Floor 36, Devon Mansions Block 59, Tooley Street) "
                //				+ "Chelsea's Birthday Festivities " + "The plans for tomorrow have finally been finalised. " + "1pm - The Loft, Clapham High Street for brunch. It's tasty and not too expensive "
                //				+ "http://www.viewlondon.co.uk/clubs/the-loft-review-27197.html "
                //				+ "After that I reckon we'll head to a pub on the High Street for a little bit before heading to mine to change for those who need to. "
                //				+ "6pm - Zebrano on Greek Street in Soho. Apparently it's Halloween themed and has a good happy hour "
                //				+ "http://www.viewlondon.co.uk/clubs/zebrano-at-the-establishment-review-32099.html "
                //				+ "About 9ish - Thirst bar also on Greek Street. We have area (apparently VIP) booked so all should be good and we can have a bit of a boogie. "
                //				+ "http://www.viewlondon.co.uk/pubsandbars/thirst-review-14734.html"
                ;

        StringTokenizer stringTokenizer = new StringTokenizer(text);

        List<String> tokens = new ArrayList<String>();
        while (stringTokenizer.hasMoreTokens()) {
            String token = stringTokenizer.nextToken();
            tokens.add(token);
            // tokens.add(" " + token);
        }

        OpenStreetMapSearchAlgorithmOptions options = new OpenStreetMapSearchAlgorithmOptions();
        options.nameLookup = NameLookupStrategy.ANY;

        OpenStreetMapSearch search = new OpenStreetMapSearch(options);

        for (String token : tokens) {

            int id = search.getSearchStringID(token);
            System.out.println("Token:  " + token + " id = " + id);

            Collection<OpenStreetMapSearchResult> results = search.searchForString(id);
            printResults(token, results);
        }

//		for (int i = 0; i <= tokens.size() - 2; i++) {
//			String largeToken = " " +tokens.get(i) + " " + tokens.get(i + 1);
//			int id = search.getSearchStringID(largeToken);
//			System.out.println(largeToken + " id: " + id);
//			Collection<OpenStreetMapSearchResult> searchForString = search.searchForString(id);
//			printResults(largeToken, searchForString);
//		}
    }

    public static void main(String[] args) throws SQLException {
        TestMain f = new TestMain();
        f.run();
        //f.lookupNewZealandByOSMId();
    }

    private void lookupNewZealandByOSMId() throws SQLException {
        OpenStreetMapSearchAlgorithmOptions options = new OpenStreetMapSearchAlgorithmOptions();
        options.nameLookup = NameLookupStrategy.ANY;

        OpenStreetMapSearch search = new OpenStreetMapSearch(options);

        OpenStreetMapSearchResult search_for_osm_id = search.search_for_osm_id((long) 556706, OpenStreetMapElementKind.DontKnow);

        "".toCharArray();
    }

    private void printResults(String largeToken, Collection<OpenStreetMapSearchResult> searchForString) {

        if (searchForString.size() > 0) {
            System.out.println("Search: " + largeToken);
            for (OpenStreetMapSearchResult result : searchForString) {
                System.out.println("\t" + result.toString());
            }
        } else {
            System.out.println("No results!");
        }
    }

}
