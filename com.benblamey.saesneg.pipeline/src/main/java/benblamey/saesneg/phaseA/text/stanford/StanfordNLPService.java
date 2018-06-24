package com.benblamey.saesneg.phaseA.text.stanford;

import com.benblamey.eventparser.SocialEventAnnotator;
import com.benblamey.eventparser.SocialEventAnnotatorOptions;
import com.benblamey.eventparser.SocialEventExpression;
import com.benblamey.saesneg.model.annotations.DataKind;
import com.benblamey.saesneg.model.annotations.TemporalAnnotation;
import com.benblamey.saesneg.model.annotations.socialevents.SocialEventAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.pipeline.WordsToSentencesAnnotator;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.TimeAnnotator;
import edu.stanford.nlp.time.distributed.TimePDF;
import edu.stanford.nlp.util.CoreMap;
import gate.Document;
import gate.DocumentContent;
import gate.Factory;
import gate.FeatureMap;
import gate.util.InvalidOffsetException;
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

public class StanfordNLPService {

    public static final String TIMEX_ANNOTATION = "timex";
    public static final String TOKEN_ANNOTATION = "token";
    private static final String NER_ANNOTATION = "named_entity";
    public static final String SOCIALEVENT_ANNOTATION = "socialevent";
    public static final String COREMAP_FEATURE = "coremap";

    /*
	 * Name of the GATE destination Annotation Set for the Stanford annotations.
     */
    public static final String GATE_ANNOTATION_SET = "stanford";

    private static AnnotationPipeline _pipeline;

    /**
     * Create a pipeline.
     */
    public static synchronized AnnotationPipeline getPipeline() {
        if (_pipeline == null) {

            ///////////////////////////////////////////
            //  All properties should be Strings!!!  //
            ///////////////////////////////////////////
            Properties props = new Properties();

			// See: http://www-nlp.stanford.edu/software/corenlp.shtml
            // --General--
            props.put("annotators",
                    "tokenize"
                    + ", ssplit"
                    + ", pos" // ner requires pos
                   + ", lemma" // new requires lemma
                    + ", ner"
                    //+ ", sutime"
            //", parse"
            //", dcoref"
            );

            // ---TOKENIZE---
            // "separates words only when whitespace is encountered. "
            props.put("tokenize.whitespace", "true");


            // ---SSPLIT---
            // always = "a newline is always a sentence break (but there still may be multiple sentences per line)."
            props.put("ssplit.newlineIsSentenceBreak", "always");
//
            // SUTIME
            props.put("sutime.rules",
                  "edu/stanford/nlp/models/sutime/distributed.defs.txt,"
                + "edu/stanford/nlp/models/sutime/distributed.defs.g.txt,"
                + "edu/stanford/nlp/models/sutime/defs.sutime.txt,"
                + "edu/stanford/nlp/models/sutime/english.sutime.txt,"
                + "edu/stanford/nlp/models/sutime/english.holidays.sutime.txt");

            props.put("sutime.verbose", "true");


          // props.put("sutime.binders", "0");





             // ---POS---
            // ner requires pos
             String uri = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";
             props.put("pos.model", uri);
            // --NER--

            String uri2 = "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz";
             props.put("ner.model", uri2);


            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

            // Add our custom event annotator.
			{
				// Inside the Stanford code, it actually accepts a resource name of a file name.
				// Generate a resource name from the class name.
				String resourceName = SocialEventAnnotator.class.getPackage().getName();
				resourceName = resourceName.replace(".", "/");
				resourceName += "/eventrules.txt";

				SocialEventAnnotatorOptions options = new SocialEventAnnotatorOptions();
				options.grammarFilename = resourceName;

				// "C:/work/data/event_rules.txt";
				pipeline.addAnnotator(new SocialEventAnnotator("benevents", options));
			}




            pipeline.addAnnotator(new TimeAnnotator("sutime", props));




            // AnnotationPipeline pipeline = new AnnotationPipeline();
            // if (tokenize) {
            //
            // // include EOL when tokenizing
            // props.put(WhitespaceTokenizerAnnotator.EOL_PROPERTY, "true");
            //
            // pipeline.addAnnotator(new WhitespaceTokenizerAnnotator(props));
            //
            // final boolean endOfLineIsEndOfSentence = true;
            //
            String end_of_sentence_regex;
             if (true) {
             end_of_sentence_regex = "(\\.|[!?]+)[\\r\\n]*";
             } else {
             end_of_sentence_regex = "\\.|[!?]+";
             }
            //
             pipeline.addAnnotator(new WordsToSentencesAnnotator(true, end_of_sentence_regex)); // true to debug
            // }

            _pipeline = pipeline;
        }

        return _pipeline;
    }

    /**
     * Run a GATE document through the Stanford NLP pipeline.
     */
    public static void annotate(Document doc, PrintStream log) throws InvalidOffsetException {

        DocumentContent content = doc.getContent();

        Annotation annotations = new Annotation(content.toString());
        //annotation.set(CoreAnnotations.DocDateAnnotation.class, date);
        getPipeline().annotate(annotations);

        for (Class clazz : annotations.keySet()) {
            log.println(clazz.toString());
        }
        // OUTPUT:
        // class edu.stanford.nlp.ling.CoreAnnotations$TextAnnotation
        // class edu.stanford.nlp.ling.CoreAnnotations$DocDateAnnotation
        // class edu.stanford.nlp.ling.CoreAnnotations$TokensAnnotation
        // class edu.stanford.nlp.ling.CoreAnnotations$SentencesAnnotation
        // class edu.stanford.nlp.ling.CoreAnnotations$NumerizedTokensAnnotation
        // class stanford.events.EventAnnotations$SocialEventAnnotations


        // Copy annotations from the Stanford pipeline to GATE.
        copyTokenAnnotations(doc, annotations, log);
        copyTempexAnnotations(doc, annotations, log);
        copySocialEventAnnotations(doc, annotations, log);
    }

    private static void copyTokenAnnotations(Document doc, Annotation annotations, PrintStream log) throws InvalidOffsetException {

        List<CoreLabel> tokens = annotations.get(CoreAnnotations.TokensAnnotation.class);
        for (CoreLabel token : tokens) {

            String text = token.get(CoreAnnotations.TextAnnotation.class);

            Long start = new Long(token.beginPosition());
            Long end = new Long(token.endPosition());

            {
                FeatureMap newFeatureMap = Factory.newFeatureMap();
                doc.getAnnotations(GATE_ANNOTATION_SET).add(start, end, TOKEN_ANNOTATION, newFeatureMap);
            }

//				for (Class tokenAnnotationKey : token.keySet()) {
//					log.println(tokenAnnotationKey.toString());
//				}
//				class edu.stanford.nlp.dcoref.CorefCoreAnnotations$CorefClusterIdAnnotation
//				class edu.stanford.nlp.dcoref.CorefCoreAnnotations$CorefClusterIdAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$AfterAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$BeforeAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$BeginIndexAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$CharacterOffsetBeginAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$CharacterOffsetEndAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$EndIndexAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$IndexAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$LemmaAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$NamedEntityTagAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$NormalizedNamedEntityTagAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$NumericCompositeTypeAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$NumericCompositeValueAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$NumericTypeAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$NumericValueAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$OriginalTextAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$ParagraphAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$PartOfSpeechAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$SpeakerAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$TextAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$TokenBeginAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$TokenEndAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$UtteranceAnnotation
//				class edu.stanford.nlp.ling.CoreAnnotations$ValueAnnotation
//				class edu.stanford.nlp.ling.tokensregex.types.Tags$TagsAnnotation
//				class edu.stanford.nlp.time.TimeAnnotations$TimexAnnotation
            {
                String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                String ner_normalized = token.get(CoreAnnotations.NormalizedNamedEntityTagAnnotation.class);

                if (ner != null) {
                    // see: C:\work\data\stanford-ner-sample.txt
                    log.println(text + " - " + ner + " - " + ner_normalized);

//						ner_normalized.equals("O")) { // This seems to be the label for non-entities.
                    FeatureMap nerFeatures = Factory.newFeatureMap();
                    nerFeatures.put("entity", ner);
                    nerFeatures.put("entity_normalized", ner_normalized);
                    doc.getAnnotations(GATE_ANNOTATION_SET).add(start, end, NER_ANNOTATION, nerFeatures);
                }
            }

        }

    }

    private static void copySocialEventAnnotations(Document doc, Annotation annotations, PrintStream log) throws InvalidOffsetException {
        List<CoreMap> socialevents = annotations.get(SocialEventAnnotator.SocialEventAnnotations.class);

        if (socialevents == null) {
            log.println(StanfordNLPService.class.getName() + ": no socialevent annotations found, skipping.");
            return;
        }

        for (CoreMap socialeventannotation : socialevents) {

            Integer start = socialeventannotation.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
            Integer end = socialeventannotation.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

            long startForDebug = start - 20;
            long finForDebug = end + 20;

            if (startForDebug < 0) {
                startForDebug = 0;
            }
            if (finForDebug > doc.getContent().size()) {
                finForDebug = doc.getContent().size();
            }

            log.println("Found event annotation: \"" + socialeventannotation.get(CoreAnnotations.TextAnnotation.class).toString() + "\" context = " + doc.getContent().getContent(startForDebug, finForDebug).toString().replace("\n", "{NL}"));

            FeatureMap newFeatureMap = ((SocialEventAnnotation) (socialeventannotation.get(SocialEventExpression.Annotation.class).value.get())).getFeaturesForGATE();

            newFeatureMap.put(COREMAP_FEATURE, socialeventannotation);

            // Add the annotation to the GATE document.
            doc.getAnnotations(GATE_ANNOTATION_SET).add(new Long((Integer) start), new Long(end), SOCIALEVENT_ANNOTATION, newFeatureMap);
        }
    }

    private static void copyTempexAnnotations(Document doc, Annotation annotations, PrintStream log) throws InvalidOffsetException {
        List<CoreMap> timexes = annotations.get(TimeAnnotations.TimexAnnotations.class);
        DocumentContent content = doc.getContent();

        if (timexes != null) {
            for (CoreMap timexannotation : timexes) {

                TemporalAnnotation tempex = new TemporalAnnotation();

                tempex.setText(timexannotation.get(edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation.class));
                tempex.setTimex(timexannotation.get(edu.stanford.nlp.time.TimeAnnotations.TimexAnnotation.class));
                tempex.setDensity(timexannotation.get(TimePDF.TimePDFAnnotation.class));
                tempex.SourceDataKind = DataKind.Text;
                tempex.isDefinitive = false;


                Integer start = timexannotation.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
                Integer end = timexannotation.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

                tempex.setOriginalText( content.getContent(new Long(start),new Long(end)).toString());

                FeatureMap newFeatureMap = Factory.newFeatureMap();
                newFeatureMap.put(COREMAP_FEATURE, tempex);

                //newFeatureMap.put("foo", new Mention());
                // Add the annotation to the GATE document.
                doc.getAnnotations(GATE_ANNOTATION_SET).add(new Long(start), new Long(end), TIMEX_ANNOTATION, newFeatureMap);
            }
        } else {
            log.println(StanfordNLPService.class.getName() + ": no tempex annotations found, skipping.");
        }
    }


}
