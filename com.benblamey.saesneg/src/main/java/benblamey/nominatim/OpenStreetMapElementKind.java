package com.benblamey.nominatim;

/**
 * Kinds of elements in Nominatim - @see
 * http://wiki.openstreetmap.org/wiki/Element
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public enum OpenStreetMapElementKind {
    Node,
    Way,
    Relation,
    /*
	 * Generated automatically by the Nominatim import process.
     */
    PostCode,
    DontKnow,

}
