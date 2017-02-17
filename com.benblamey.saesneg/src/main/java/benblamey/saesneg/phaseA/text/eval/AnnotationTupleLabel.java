package benblamey.saesneg.phaseA.text.eval;

/**
 * The different possibilities for correctness of a GATE annotation.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public enum AnnotationTupleLabel {

    /**
     * Gold annotation is missing a response (different to the wrong response).
     */
    GoldMissingResponse,
    /**
     * Responses but no gold annotation.
     */
    NoGold,
    /**
     * Gold annotation has multiple responses. Hopefully this is an edge case.
     * Consider excluding these results.
     */
    GoldMultipleResponse,
    /**
     * Tuple is excluded because the gold annotation has some issue associated
     * with it. This is usually because it is a valid place, but is absent from
     * OSM, or our version of OSM.
     */
    GoldExclude

}
