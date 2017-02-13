package benblamey.eventparser;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.ErasureUtils;
import edu.stanford.nlp.util.Function;
import edu.stanford.nlp.util.Interval;
import java.util.List;

public class SocialEventExpression extends MatchedExpression {

    public SocialEventExpression(Interval<Integer> charOffsets, Interval<Integer> tokenOffsets, SingleAnnotationExtractor extractFunc, double priority, double weight) {
        super(charOffsets, tokenOffsets, extractFunc, priority, weight);
        throw new RuntimeException("not implemented");
    }

    public SocialEventExpression(MatchedExpression in) {
        super(in);
    }

    /**
     * The CoreMap key for storing a TimeExpression annotation
     */
    public static class Annotation implements CoreAnnotation<SocialEventExpression> {

        public Class<SocialEventExpression> getType() {
            return SocialEventExpression.class;
        }
    }

    /**
     * The CoreMap key for storing a nested annotations
     */
    public static class ChildrenAnnotation implements CoreAnnotation<List<? extends CoreMap>> {

        public Class<List<? extends CoreMap>> getType() {
            return ErasureUtils.<Class<List<? extends CoreMap>>>uncheckedCast(List.class);
        }
    }

    protected static final Function<MatchedExpression, SocialEventExpression> TimeExpressionConverter = new Function<MatchedExpression, SocialEventExpression>() {
        public SocialEventExpression apply(MatchedExpression in) {
            if (in == null) {
                return null;
            }
            if (in instanceof SocialEventExpression) {
                return (SocialEventExpression) in;
            }
            return new SocialEventExpression(in);
        }
    };

}
