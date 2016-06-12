package benblamey.eventparser;

import edu.stanford.nlp.ling.tokensregex.CoreMapExpressionExtractor;

public interface SocialEventExpressionPatterns {

    public CoreMapExpressionExtractor createExtractor();

}
