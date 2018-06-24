package com.benblamey.eventparser;

import edu.stanford.nlp.ie.NumberNormalizer;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.util.CoreMap;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SocialEventExpressionExtractorImpl {

    protected static final Logger logger = Logger.getLogger(SocialEventExpressionExtractorImpl.class.getName());
    private final SocialEventAnnotatorOptions _options;

    SocialEventExpressionPatterns _eventPatterns;
    CoreMapExpressionExtractor expressionExtractor;

    public SocialEventExpressionExtractorImpl(SocialEventAnnotatorOptions options) {

        _options = options;

        if (_options.verboseLogging) {
            logger.setLevel(Level.FINE);
        } else {
            logger.setLevel(Level.SEVERE);
        }
        NumberNormalizer.setVerbose(_options.verboseLogging);

        if (_options.grammarFilename != null) {
            _eventPatterns = new SocialEventExpressionPatternsImpl(_options);
        } else {
            throw new RuntimeException("No grammar file.");
        }

        this.expressionExtractor = _eventPatterns.createExtractor();
        this.expressionExtractor.setLogger(logger);
    }

    public List<CoreMap> extractEventExpressionCoreMaps(CoreMap annotation) {
        List<SocialEventExpression> eventExpressions = extractEventExpressions(annotation);
        List<CoreMap> coreMaps = toCoreMaps(annotation, eventExpressions);
        return coreMaps;
    }

    private List<SocialEventExpression> extractEventExpressions(CoreMap annotation) {

        List<CoreMap> mergedNumbers = NumberNormalizer.findAndMergeNumbers(annotation);
        annotation.set(CoreAnnotations.NumerizedTokensAnnotation.class, mergedNumbers);

        List<? extends MatchedExpression> matchedExpressions = expressionExtractor.extractExpressions(annotation);

        List<SocialEventExpression> eventExpressions = new ArrayList<SocialEventExpression>(matchedExpressions.size());
        for (MatchedExpression expr : matchedExpressions) {
            if (expr instanceof SocialEventExpression) {
                eventExpressions.add((SocialEventExpression) expr);
            } else {
                eventExpressions.add(new SocialEventExpression(expr));
            }
        }

        return eventExpressions;
    }

    private List<CoreMap> toCoreMaps(CoreMap annotation, List<SocialEventExpression> timeExpressions) {
        if (timeExpressions == null) {
            return null;
        }
        List<CoreMap> coreMaps = new ArrayList<CoreMap>(timeExpressions.size());

        for (SocialEventExpression te : timeExpressions) {
            CoreMap cm = te.getAnnotation();

            coreMaps.add(cm);
        }

        return coreMaps;
    }

}
