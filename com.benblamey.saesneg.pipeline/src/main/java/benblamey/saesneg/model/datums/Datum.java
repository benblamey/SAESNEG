package com.benblamey.saesneg.model.datums;

import com.benblamey.core.GATE.GateUtils2;
//import com.benblamey.core.googlegeocode.GoogleGeoCode;
import com.benblamey.eventparser.SocialEventExpression;
import com.benblamey.gnuplot.GnuPlot;
import com.benblamey.gnuplot.GnuPlotResult;
import com.benblamey.nominatim.OpenStreetMapBasicOrdering;
import com.benblamey.nominatim.OpenStreetMapElementKind;
import com.benblamey.nominatim.OpenStreetMapSearch;
import com.benblamey.nominatim.OpenStreetMapSearchAlgorithmOptions;
import com.benblamey.nominatim.OpenStreetMapSearchResult;
import com.benblamey.saesneg.PipelineContext;
import com.benblamey.saesneg.evaluation.DatumWebProperty;
import com.benblamey.saesneg.experiments.ExperimentOptions;
import com.benblamey.saesneg.model.UserContext;
import com.benblamey.saesneg.model.annotations.DataKind;
import com.benblamey.saesneg.model.annotations.DatumAnnotations;
import com.benblamey.saesneg.model.annotations.LocationAnnotation;
import com.benblamey.saesneg.model.annotations.TemporalAnnotation;
import com.benblamey.saesneg.model.annotations.socialevents.SocialEventAnnotation;
import com.benblamey.saesneg.phaseA.text.ProcessTextOptions;
import com.benblamey.saesneg.phaseA.text.gatesubdocument.GateSubDocument;
import com.benblamey.saesneg.phaseA.text.gatesubdocument.GateSubDocumentReader;
import com.benblamey.saesneg.phaseA.text.gatesubdocument.GateSubDocumentWriter;
import com.benblamey.saesneg.phaseA.text.nerpaper.GoldLabelling;
import com.benblamey.saesneg.phaseA.text.stanford.StanfordNLPService;
import com.restfb.types.Comment;
import com.restfb.types.FacebookType;
import com.restfb.types.NamedFacebookType;
import edu.stanford.nlp.time.distributed.TimeDensityFunction;
import edu.stanford.nlp.util.CoreMap;
import gate.Annotation;
import gate.AnnotationSet;
import gate.Document;
import gate.DocumentContent;
import gate.Factory;
import gate.FeatureMap;
import gate.SimpleAnnotationSet;
import gate.util.InvalidOffsetException;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import org.joda.time.DateTime;

public abstract class Datum {

    public static final String LOCATION_GATE_ANNOTATION = "location";
    public static final String GOLD_AS = "gold";
    public static final String OPEN_STREET_MAP_BEN_AS = "OpenStreetMap_BEN";

    static String anonName(String name) {
        String[] names = name.split(" ");
        String anon = new Character((char)(names[0].substring(0,1).charAt(0) + 1)).toString();
        if (names.length > 1)
            anon += names[names.length-1].substring(0,1);
        anon = anon.toUpperCase();
        return anon;
    }

    @XmlID
    @XmlAttribute
    String _ID;
    FacebookType _data;

    // Ideally this field should be transient, but we keep it in order to rescue data that was lost originally.
    @XmlIDREF
    public UserContext _user; // Note that this ends up being a different instance - this is the one that holds the copy of the profile.

    public List<Comment> Comments;
    public List<NamedFacebookType> Likes;

    // Old fields that still exist in serialized data -- we need to leave these in so it doesn't break.
    @Deprecated
    private transient String _note = null;
    @Deprecated
    private transient Object _locations = null;
    @Deprecated
    private transient Object _metadata = null;

    //private transient Map<String, Object> _metadata = new HashMap<>();
    // Experiment Annotations.
    public transient List<String> associatedUserUris;
    public transient List<String> matchingTFIDFWords = new ArrayList<>();

    // Annotations.
    //private transient ArrayList<LocationAnnotation> _locations = new ArrayList<LocationAnnotation>();
    // HashSet<String> NamedPerson
    //private transient ArrayList<TemporalExpression> _tempexes = new ArrayList<TemporalExpression>();
    //private transient ArrayList<SocialEventAnnotation> _socialEvents = new ArrayList<>();
    private transient DatumAnnotations _annotations = new DatumAnnotations();
    protected transient GateSubDocument _gateSubDocument = null;

    protected Datum() {
        //_socialEvents = new ArrayList<>();
    } // Serialization Ctor.

    protected Datum(UserContext user, FacebookType data) {
        if (data == null) {
            throw new IllegalArgumentException();
        }

        _user = user;
        _data = data;
        _ID = data.getId();

        Comments = _user.fch.getConnectionWithPagination(_ID + "/comments", com.restfb.types.Comment.class);
        // Likes field is deprecated in API 2.4 and higher.
        //Likes = _user.fch.getConnectionWithPagination(_ID + "/likes", com.restfb.types.NamedFacebookType.class);
    }

    public Long getNetworkID() {
        return Long.parseLong(_data.getId());
    }

    public String getNetworkString() {
        return _data.getId();
    }

    /**
     * Some of the fields are not initialized properly for types like Album (to
     * cut a long story short). i.e. they get deserialized as null.
     */
    private Object readResolve() throws ObjectStreamException {
        matchingTFIDFWords = new ArrayList<>();
        _annotations = new DatumAnnotations();
        return this;
    }

    public static Long getNetworkIDForFB(FacebookType arg) {
        return Long.parseLong(arg.getId());
    }

    /**
     * Will be used a css class: all lowercase, descriptive in global state.
     * Suggest prefix 'datum'.
     *
     * @return
     */
    public abstract String getWebViewClass();

    public String getWebViewTitle() {
        return "Implement getWebViewTitle!!";
    }

    public DatumAnnotations getAnnotations() {
        return _annotations;
    }

    public Set<DatumWebProperty> getWebViewMetadata() {

        // Order the properties by key so that they are consistently presented
        // in the web UI.
        Set<DatumWebProperty> map = new TreeSet<DatumWebProperty>(new Comparator<DatumWebProperty>() {
            {
            }

            @Override
            public int compare(DatumWebProperty o1, DatumWebProperty o2) {
                return o1.Key.compareTo(o2.Key);
            }
        });

        map.add(new DatumWebProperty() {
            {
                Key = "OSN";
                FriendlyName = "Social Network";
                Value = "Facebook";
            }
        });

        return map;
    }

    public void appendTextToGateDocument(Document gateDocument) throws InvalidOffsetException {

        // If wanting to export canonical GATE documents - enable this flag.
        if (PipelineContext.getCurrentContext().GATE_EXPORT_SINCE_2012_FILTER) {
            DateTime start2012 = new DateTime(2012, 1, 1, 0, 0, 0);
            if (this.getContentAddedDateTime().isBefore(start2012)) {
                return;
            }
        }

        // System.out.println("AppendTextToGateDocument " +
        // this.getNetworkID());
        GateSubDocumentWriter gateSubDocument = new GateSubDocumentWriter(gateDocument, "fb", this.getNetworkID());

        appendTextToGATE(gateSubDocument);

        // It is important to do this to include the datum annotation.
        gateSubDocument.finalizeText();

        _gateSubDocument = gateSubDocument;
    }

    public String getText() throws InvalidOffsetException {
        if (_gateSubDocument == null) {
            return null;
        }
        return _gateSubDocument.getText();
    }

    public void loadGateDocument(Document gateDocument) throws InvalidOffsetException {
        // Load the GATE document.
        _gateSubDocument = new GateSubDocumentReader(gateDocument, "fb", this.getNetworkID());
    }

    protected abstract void appendTextToGATE(GateSubDocumentWriter gateSubDocument) throws InvalidOffsetException;

    /**
     * Get the content time - don't trust this in data mining
     */
    public abstract DateTime getContentAddedDateTime();

    public String getImageThumbnailURL() {
        return null;
    }

    public String getFullImageURL() {
        return null;
    }

    public String getLocalImageURL() {
        return "file://C:/work/docs/Dropbox/PHD_DATA/images/" + this._ID + ".jpg";
    }

    public Iterable<GnuPlotResult> getTimeConstraintPlotPaths() throws IOException, InterruptedException {
        ArrayList<GnuPlotResult> plots = new ArrayList<>();
        for (TemporalAnnotation tempexAnno : this.getAnnotations().DateTimesAnnotations) {
            String outputDir = "C:\\work\\code\\Ben\\ben_phd_java\\benblamey.evaluation\\web\\gnuplots\\";
            //this._user.getOutputDirectoryWithTrailingSlash("gnuplots")
            GnuPlot plot = new GnuPlot(outputDir);

            TimeDensityFunction density = tempexAnno.getDensity();
            if (density != null) {
                GnuPlotResult exportGraph = plot.exportGraph(density.getGNUPlot("x") + " title '" + plot.escapeTitle(tempexAnno.getText()) + "'");
                exportGraph.totalMass = density.getTotalMass();
                plots.add(exportGraph);
            } else {
                System.err.println("Skipping tempex without density.");
                plots.add( GnuPlotResult.Empty );
            }
        }
        return plots;
    }

    /**
     * Reads information out of the annotated text, and converts them into datum
     * annotations. Intended to be a simple metadata-mapping, not performing
     * extensive text-lookup operations.
     *
     * @param textOptions
     * @param osmOptions
     * @param osmLocationAnnotations -- this object is expensive to build (so we
     * avoid doing it each time)
     * @throws SQLException
     * @throws InvalidOffsetException
     */
    public void postANNIETextProcessing(ProcessTextOptions textOptions, OpenStreetMapSearchAlgorithmOptions osmOptions, AnnotationSet osmLocationAnnotations) throws SQLException, InvalidOffsetException {

        //////////////////////////////////////////////////////////////////////
        // Social Events
        if (osmOptions != null) {
            annotateTextLocations(osmLocationAnnotations, textOptions);
        }

        //////////////////////////////////////////////////////////////////////
        // Social Events
        extractStanfordSocialEvents();

        //////////////////////////////////////////////////////////////////////
        // Timexes.
        AnnotationSet intersectingAnnotations = this._gateSubDocument.getIntersectingAnnotations(StanfordNLPService.GATE_ANNOTATION_SET);

        DocumentContent content = this._gateSubDocument.ParentDocument.getContent();
        for (Annotation an : intersectingAnnotations.get(StanfordNLPService.TIMEX_ANNOTATION)) {
            TemporalAnnotation ta = (TemporalAnnotation) an.getFeatures().get(StanfordNLPService.COREMAP_FEATURE);
            ta.SourceDataKind = DataKind.Text;
            ta.isDefinitive = false; // Temporal annotations found in text are not necessarily definitive.
            ta.setOriginalText(content.getContent(
                    an.getStartNode().getOffset(),
                    an.getEndNode().getOffset()).toString());
            ta.toString();
            getAnnotations().DateTimesAnnotations.add(ta);
        }
    }

    private void annotateTextLocations(AnnotationSet osmLocationAnnotations, ProcessTextOptions textOptions) throws InvalidOffsetException {
        AnnotationSet stanfordLocs = this._gateSubDocument.ParentDocument
                .getAnnotations("stanford")
                .get("named_entity");
        AnnotationSet saesnegOutputLocationAnnotations = this._gateSubDocument.ParentDocument.getAnnotations("SAESNEG_LOCATION");
        for (Annotation sentence : GateUtils2.getSortedAnnotations(this._gateSubDocument.getSentences())) {
            System.out.println("SENTENCE: " + this._gateSubDocument.ParentDocument.getContent().getContent(sentence.getStartNode().getOffset(), sentence.getEndNode().getOffset()));

            SimpleAnnotationSet tokensAS = this._gateSubDocument.getTokensForSentence(sentence);
            List<Annotation> tokens = GateUtils2.getSortedAnnotations(tokensAS);

            for (int i = 0; i < tokens.size(); i++) {
                Annotation token = tokens.get(i);
                AnnotationSet stanfordLocationsAS = stanfordLocs.get(token.getStartNode().getOffset(), token.getEndNode().getOffset());
                Annotation stanfordLocation = null;
                for (Annotation stanfordLocationIter : stanfordLocationsAS) {
                    // There is only ever one location annotation.
                    stanfordLocation = stanfordLocationIter;
                    break;
                }

                String tokenText = (String) token.getFeatures().get("string");
                String trimmedTokenText = OpenStreetMapSearch.toStandardFormWithLeadingSpace(tokenText).trim();
                if (trimmedTokenText.length() == 0) {
                    continue;
                }

                Object stanfordEntityType = stanfordLocation.getFeatures().get("entity");
                String pos = (String) token.getFeatures().get("category");

                // Example features:
                // features={category=JJ, kind=word, orth=lowercase, length=6,
                // string=lovely}; start=NodeImpl:
                // id=3844; offset=7022; end=NodeImpl: id=3845; offset=7028
                boolean addedOSM = false;

                // Process word-1-grams.
                List<OpenStreetMapSearchResult> osmResults = readOSMResultsFromDoc(token, osmLocationAnnotations);
                Collections.sort(osmResults, new OpenStreetMapBasicOrdering());

                for (OpenStreetMapSearchResult r : osmResults) {

                    if (r.name == null) {
                        continue;
                    }

                    // See here for ranks: http://wiki.openstreetmap.org/wiki/Nominatim/Development_overview
// Indented back for use in thesis.
boolean passesFilter =
    stanfordEntityType.equals("LOCATION")
    || (
        trimmedTokenText.toLowerCase().equals(r.name.toLowerCase())
        && (r.osm_type != OpenStreetMapElementKind.PostCode)
        && (r.osm_sub_class.equals("state"))
       )
    || (
        (stanfordEntityType.equals("ORGANIZATION")
                || stanfordEntityType.equals("PERSON"))
            && (r.admin_level <= 10)
       )
    || (r.admin_level <= 6);

//(r.rank_search <= 10)
                            //&& tokenText.equals(r.name)) // important places if exact match (including accents etc.)
                            // || (stanfordEntityType.equals("ORGANIZATION") // Anything that is a location
                            //|| ((r.rank_search <= 7) && tokenText.length() > 2)


                    if (passesFilter) {
                        // We only export the first match - prevents confusing the anno diff tool.
                        if (!addedOSM) {
                            FeatureMap newFeatureMap = GateUtils2.toFeatureMap(r.toMap());
                            saesnegOutputLocationAnnotations.add(token.getStartNode().getOffset(), token.getEndNode().getOffset(), LOCATION_GATE_ANNOTATION, newFeatureMap);
                            addedOSM = true;
                        }
                        // Add the annotation to the datum.
                        r.setOriginalText(tokenText);
                        r.SourceDataKind = DataKind.Text;
                        this.getAnnotations().Locations.add(r);
                    }
                }

                if (textOptions.BOOTSTRAP_GOLD_LABELLING) {
                    bootstrapGoldLabelling(token, tokenText, pos, osmResults);
                }

                if (!addedOSM && tokenText.contains("Cardiff")) {
                    "".toString();
                }

            }
        }
    }

    public abstract void processMetadataFields(ExperimentOptions opt) throws IOException, URISyntaxException;

    private List<OpenStreetMapSearchResult> readOSMResultsFromDoc(Annotation token, AnnotationSet osmLocationAnnotations) {

        List<OpenStreetMapSearchResult> results = new ArrayList<OpenStreetMapSearchResult>();

        // 1-grams only.
        // This includes OSM results that 'partially overlap' i.e. if the token has extra punctuation etc. that was removed for search.
        for (Annotation osmLocationAnnotation : osmLocationAnnotations.get(token.getStartNode().getOffset(), token.getEndNode().getOffset())) {
            OpenStreetMapSearchResult osmResult = new OpenStreetMapSearchResult(osmLocationAnnotation.getFeatures());
            results.add(osmResult);
        }

        return results;
    }

    private void bootstrapGoldLabelling(Annotation a, String token, String pos, Collection<OpenStreetMapSearchResult> searchForString) {
        if (!GoldLabelling.s_nonLocations.contains(token.toLowerCase()) && (token.length() > 1)) {
            if (token.equals("UK")) {
                FeatureMap newFeatureMap = Factory.newFeatureMap();
                newFeatureMap.put("osm_id", 62149L);
                this._gateSubDocument.annotateToken(a.getStartNode(), a.getEndNode(), GOLD_AS, LOCATION_GATE_ANNOTATION, newFeatureMap);
            } else if (token.equals("US") || token.equals("USA")) {
                FeatureMap newFeatureMap = Factory.newFeatureMap();
                newFeatureMap.put("osm_id", 148838L);
                this._gateSubDocument.annotateToken(a.getStartNode(), a.getEndNode(), GOLD_AS, LOCATION_GATE_ANNOTATION, newFeatureMap);
            }

            boolean addedGold = false;
            for (OpenStreetMapSearchResult r : searchForString) {

                // Bootstrap gold labelling.
                if (r.calculated_country_code.equals("gb") && !addedGold && pos.equals("NNP")) { // We only add proper nouns when bootstrapping the gold labelling.

                    FeatureMap newFeatureMap = GateUtils2.toFeatureMap(r.toMap());
                    this._gateSubDocument.annotateToken(a.getStartNode(), a.getEndNode(), GOLD_AS, LOCATION_GATE_ANNOTATION, newFeatureMap);
                    addedGold = true;
                }
            }
        }
    }

    private void extractStanfordSocialEvents() throws InvalidOffsetException {
        AnnotationSet intersectingAnnotations = this._gateSubDocument.getIntersectingAnnotations(StanfordNLPService.GATE_ANNOTATION_SET);

        DocumentContent content = this._gateSubDocument.ParentDocument.getContent();

        // Handle the Social Event annotations.
        for (Annotation an : intersectingAnnotations.get(StanfordNLPService.SOCIALEVENT_ANNOTATION)) {
            CoreMap cm = (CoreMap) an.getFeatures().get(StanfordNLPService.COREMAP_FEATURE);
            SocialEventExpression eventExpression = cm.get(benblamey.eventparser.SocialEventExpression.Annotation.class);
            SocialEventAnnotation se = (SocialEventAnnotation) eventExpression.getValue().get();
            se.SourceDataKind = DataKind.Text;
            se.setOriginalText(content.getContent(
                    an.getStartNode().getOffset(),
                    an.getEndNode().getOffset()).toString());
            getAnnotations().SocialEventAnnotation.add(se);
        }
    }

    /**
     * Secondary datums are special datums like Albums, that need to be
     * considered for merging, but that the user doesn't think of as datums. And
     * that we exclude from merging accuracy calculations.
     */
    public boolean isPrimary() {
        // True by default.
        return true;
    }

    public void searchLocationMetadataField(String location, String originatingField, ExperimentOptions opt) throws IOException, URISyntaxException {
        if (opt.geocodeMetadata)
        {
            System.out.println("Searching " + location + " from " + this._data.getClass().getName() + " field: " + originatingField);
            LocationAnnotation geoCode = GoogleGeoCode.geoCode(location);
            if (geoCode != null) {
                geoCode.SourceDataKind = DataKind.Metadata;
                this.getAnnotations().Locations.add(geoCode);
            }
        }
    }

    public void processImageContent() {
        // Nothing to do by default.
    }

    public void processPeopleTextFields() {
        // Nothing to do by default.
    }

    public void postDeserializationFix(UserContext user) {
        // Ideally this field should be transient, but we keep it in order to rescue data that was lost originally -- it needs to be re-initialized.
        // Except if it is missing completely and needs to be re-created.
        if (this._user == null) {
            this._user = user;
        } else {
            this._user.postDeserializationFix();
        }

        _annotations = new DatumAnnotations();
    }



}
