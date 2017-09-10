package com.benblamey.nominatim;

/**
 * Options for the OSM search.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class OpenStreetMapSearchAlgorithmOptions {

    /**
     * Strategy for matching the search query against the tokens associated with
     * a place.
     */
    public NameLookupStrategy nameLookup = NameLookupStrategy.ANY;

}
