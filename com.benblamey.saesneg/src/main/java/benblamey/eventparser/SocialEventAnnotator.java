package benblamey.eventparser;

import edu.stanford.nlp.ie.regexp.NumberSequenceClassifier;
import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.ErasureUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An annotator for the Stanford CoreNLP toolkit for detecting Social Events
 * (such as birthdays).
 *
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class SocialEventAnnotator implements Annotator {

    /**
     * The CoreMap key for storing a SocialEvent annotation
     */
    public static class SocialEventAnnotation implements CoreAnnotation<SocialEventAnnotation> {

        public Class<SocialEventAnnotation> getType() {
            return SocialEventAnnotation.class;
        }
    }

    /**
     * The CoreMap key for storing all SocialEvent annotations in a document.
     */
    public static class SocialEventAnnotations implements CoreAnnotation<List<CoreMap>> {

        public Class<List<CoreMap>> getType() {
            return ErasureUtils.<Class<List<CoreMap>>>uncheckedCast(List.class);
        }
    }

    public static final String EVENT = "event";
    public static final Requirement EVENT_REQUIREMENT = new Requirement(EVENT);

    private final SocialEventExpressionExtractorImpl _eventExtractor;

    public SocialEventAnnotator(String name, SocialEventAnnotatorOptions options) {
        _eventExtractor = new SocialEventExpressionExtractorImpl(options);
    }

    @Override
    public void annotate(Annotation annotation) {

        List<CoreMap> allTimeExpressions; // initialized below = null;
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        if (sentences != null) {
            allTimeExpressions = new ArrayList<CoreMap>();
            List<CoreMap> allNumerics = new ArrayList<CoreMap>();

            System.out.println("Starting sentences...");
            for (CoreMap sentence : sentences) {
                // make sure that token character offsets align with the actual
                // sentence text
                // They may not align due to token normalizations, such as "("
                // to "-LRB-".
                CoreMap alignedSentence = NumberSequenceClassifier.alignSentence(sentence);
                // uncomment the next line for verbose dumping of tokens....
                // System.err.println("SENTENCE: " + ((ArrayCoreMap)
                // sentence).toShorterString());
                List<CoreMap> socialEventExpressions = _eventExtractor.extractEventExpressionCoreMaps(alignedSentence);
                if (socialEventExpressions != null) {
                    allTimeExpressions.addAll(socialEventExpressions);
                    sentence.set(SocialEventAnnotations.class, socialEventExpressions);
                    for (CoreMap eventExpression : socialEventExpressions) {
                        eventExpression.set(CoreAnnotations.SentenceIndexAnnotation.class, sentence.get(CoreAnnotations.SentenceIndexAnnotation.class));
                    }
                }
                List<CoreMap> numbers = alignedSentence.get(CoreAnnotations.NumerizedTokensAnnotation.class);
                if (numbers != null) {
                    sentence.set(CoreAnnotations.NumerizedTokensAnnotation.class, numbers);
                    allNumerics.addAll(numbers);
                }
            }
            System.out.println("...ending sentences");

            annotation.set(CoreAnnotations.NumerizedTokensAnnotation.class, allNumerics);
        } else {
            allTimeExpressions = annotateSingleSentence(annotation);
        }

        System.out.println("Adding a list of " + allTimeExpressions.size() + " expressions to SocialEventAnnotations annotation.");

        annotation.set(SocialEventAnnotations.class, allTimeExpressions);

    }

    private List<CoreMap> annotateSingleSentence(CoreMap sentence) {
        CoreMap annotationCopy = NumberSequenceClassifier.alignSentence(sentence);

        return this._eventExtractor.extractEventExpressionCoreMaps(annotationCopy);
    }

    @Override
    public Set<Requirement> requires() {
        return Collections.singleton(TOKENIZE_REQUIREMENT);
    }

    @Override
    public Set<Requirement> requirementsSatisfied() {
        return Collections.singleton(EVENT_REQUIREMENT);
    }

}
