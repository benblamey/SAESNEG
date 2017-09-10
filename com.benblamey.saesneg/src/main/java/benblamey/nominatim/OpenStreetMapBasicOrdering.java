package com.benblamey.nominatim;

import java.util.Comparator;

/**
 * Compares @see OpenStreetMapSearchResult by rank.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class OpenStreetMapBasicOrdering implements Comparator<OpenStreetMapSearchResult> {

    public static OpenStreetMapBasicOrdering Instance = new OpenStreetMapBasicOrdering();

    @Override
    public int compare(OpenStreetMapSearchResult o1, OpenStreetMapSearchResult o2) {

        // Order by increasing admin_level -- things like national states appear towards the front of the list.
        int result =  Integer.compare(o1.admin_level, o2.admin_level);



//        // Don't use the actual position, this is supposed to be a trivial baseline.
//        if (result == 0) {
//            return 1;
//        }
        return result;
    }

}
