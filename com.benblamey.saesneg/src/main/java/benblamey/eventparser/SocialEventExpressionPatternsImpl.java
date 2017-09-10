package com.benblamey.eventparser;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;
import edu.stanford.nlp.ling.tokensregex.Env;
import edu.stanford.nlp.ling.tokensregex.TokenSequencePattern;
import edu.stanford.nlp.pipeline.CoreMapAttributeAggregator;
import edu.stanford.nlp.util.StringUtils;
import java.util.List;
import java.util.regex.Pattern;

public class SocialEventExpressionPatternsImpl implements SocialEventExpressionPatterns {

    Env env;
    SocialEventAnnotatorOptions options;

    public SocialEventExpressionPatternsImpl(SocialEventAnnotatorOptions options) {
        this.options = options;

        initEnv();
    }

    private void initEnv() {

        env = TokenSequencePattern.getNewEnv();
        env.setDefaultResultsAnnotationExtractor(SocialEventExpression.TimeExpressionConverter);
        env.setDefaultTokensAnnotationKey(CoreAnnotations.NumerizedTokensAnnotation.class);
        env.setDefaultResultAnnotationKey(SocialEventExpression.Annotation.class);
        env.setDefaultNestedResultsAnnotationKey(SocialEventExpression.ChildrenAnnotation.class);
        env.setDefaultTokensAggregators(CoreMapAttributeAggregator.DEFAULT_NUMERIC_TOKENS_AGGREGATORS);

        // Do case insensitive matching
        env.setDefaultStringPatternFlags(Pattern.CASE_INSENSITIVE);
    }

    @Override
    public CoreMapExpressionExtractor createExtractor() {
        List<String> filenames = StringUtils.split(options.grammarFilename, "\\s*[,;]\\s*");
        return CoreMapExpressionExtractor.createExtractorFromFiles(env, filenames);
    }

}
