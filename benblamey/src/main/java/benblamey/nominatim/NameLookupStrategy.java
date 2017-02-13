package benblamey.nominatim;

/**
 * Various strategies for matching the search query against the tokens
 * associated with a place.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public enum NameLookupStrategy {

    /**
     * The search query is compared against all name tokens for a candidate
     * place.
     */
    ANY,
    /**
     * The search query is compared against the first token only.
     */
    FIRST,

}
