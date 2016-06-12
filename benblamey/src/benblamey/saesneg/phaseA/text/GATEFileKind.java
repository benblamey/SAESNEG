package benblamey.saesneg.phaseA.text;

public enum GATEFileKind {

    Generated,
    Gold,
    PostProcessingAnnotations,
    /**
     * Like 'PostProcessingAnnotations' but include the OSM search results (XML
     * file is too large with these, binary ser. needed.).
     */
    BinaryCache,
}
