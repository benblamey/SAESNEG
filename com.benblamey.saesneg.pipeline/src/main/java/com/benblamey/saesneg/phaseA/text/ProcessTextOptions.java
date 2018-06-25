package com.benblamey.saesneg.phaseA.text;

/**
 * Per-experiment options.
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class ProcessTextOptions {

    public static ProcessTextOptions Default = new ProcessTextOptions() {
        {

        }
    };

    public boolean RUN_LEGACY_OSM_NOMINATIM_SEARCH = false;

    public boolean BOOTSTRAP_GOLD_LABELLING = false;

    public boolean RUN_GEONAMES_LOOKUP = false;

    public boolean USE_CACHED_GATE_DOC = false;

    public boolean LOOKUP_2_GRAMS = false;

}
