/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package benblamey.saesneg.phaseA.text.stanford;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.tokensregex.MatchedExpression;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.time.TimeAnnotations;
import edu.stanford.nlp.time.Timex;
import static edu.stanford.nlp.time.distributed.DistributedMain.textToAnnotation;
import edu.stanford.nlp.time.distributed.TimeDensityFunction;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.HasInterval;
import edu.stanford.nlp.util.StringUtils;
import edu.stanford.nlp.util.ValuedInterval;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 *
 * @author Ben
 */
public class StanfordNLPTestMain {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        String in = "Summer '12 "
                + " aaaa \n\n Summer "
                + "aaaaaa \n\n Winter "
                + "aaaaaa \n\n Easter "
                + "aaaaaa \n\n Autumn "
                + "aaaaaa \n\n Summer 2015 "
                + "aaaaaa \n\n Thursday 25th July "
                + "aaaaaa \n\n  the beginning of next week "
                + "aaaaaa \n\n 21st April "
                + "aaaaaa \n\n the end of June "
                + "aaaaaa \n\n Xmas "
                + "aaaaaa \n\n '13 "
                + "aaaaaa \n\n Dec '11 "
                + "aaaaaa \n\n June "
                + "aaaaaa \n\n Sun 21st April "
                + "aaaaaa \n\n Thursday 14th March 2013 "
                + "aaaaaa \n\n foo '12 "
                + "aaaaaa \n\n fffSummer '11. ";

        AnnotationPipeline pipeline = StanfordNLPService.getPipeline();

        processText(pipeline, in, null);
    }

    public static void processText(AnnotationPipeline pipeline, String text, String date) throws IOException {

        text = text.replace("{", "(").replace("}", ")");

       // System.err.println("Processing line: " + text);
        Annotation annotation = textToAnnotation(pipeline, text, date);

        String text2 = annotation.get(CoreAnnotations.TextAnnotation.class);

//        List<CoreLabel> tokens = annotation.get(CoreAnnotations.TokensAnnotation.class);
//        for (CoreLabel token : tokens) {
//            Integer start = token.get(CoreAnnotations.TokenBeginAnnotation.class);
//            Integer end = token.get(CoreAnnotations.TokenEndAnnotation.class);
//            if (start != null || end != null) {
//                String tokenText = text.substring(start, end);
//                System.err.println("Token: " + token);
//            }
//        }
        List<CoreMap> timexes = annotation.get(
                TimeAnnotations.TimexAnnotations.class);
        for (CoreMap timexannotation : timexes) {
            Integer characterOffsetStart = timexannotation.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
            Integer characterOffsetEnd = timexannotation.get(CoreAnnotations.CharacterOffsetEndAnnotation.class);
        }

        // Sort the timex annotations according to character offsets.
        List<ValuedInterval<CoreMap, Integer>> timexList = new ArrayList<ValuedInterval<CoreMap, Integer>>(timexes.size());
        for (CoreMap timexAnn : timexes) {
            timexList.add(new ValuedInterval<CoreMap, Integer>(timexAnn,
                    MatchedExpression.COREMAP_TO_CHAR_OFFSETS_INTERVAL_FUNC.apply(timexAnn)));
        }
        Collections.sort(timexList, HasInterval.CONTAINS_FIRST_ENDPOINTS_COMPARATOR);

        {
            annotation.get(CoreAnnotations.SentencesAnnotation.class);
        }

        StringBuilder newText = new StringBuilder();

        int lastIndex = 0;

        for (ValuedInterval<CoreMap, Integer> vi : timexList) {
            int characterOffsetStart = vi.getValue().get(CoreAnnotations.CharacterOffsetBeginAnnotation.class);
            int characterOffsetEnd = vi.getValue().get(CoreAnnotations.CharacterOffsetEndAnnotation.class);

            if (characterOffsetStart > 0 && (characterOffsetStart > lastIndex)) {
                newText.append(text.substring(lastIndex, characterOffsetStart));
                System.out.println(text.substring(lastIndex, characterOffsetStart));
            }

            String token = vi.getValue().get(edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation.class);

            newText.append(text.substring(characterOffsetStart, characterOffsetEnd));
            System.out.println(text.substring(characterOffsetStart, characterOffsetEnd));

            Timex get = vi.getValue().get(edu.stanford.nlp.time.TimeAnnotations.TimexAnnotation.class);
            System.out.println(get.toString());
            TimeDensityFunction get1 = vi.getValue().get(edu.stanford.nlp.time.distributed.TimePDF.TimePDFAnnotation.class);
            System.out.print("\tDensity:");
            if (get1 == null) {
                System.out.println("null");
            } else {
                System.out.println(get1.toString());
            }

            String gnuPlot = get.getGNUPlot();

            System.out.println("\tplot: " + gnuPlot);
            "".toString();

            newText.append("{"); //  

            newText.append("}"); // 

            lastIndex = characterOffsetEnd;
        }

        newText.append(text.substring(lastIndex));

        //ProcessTextResult ptr = new ProcessTextResult();
        // ptr.textAreaText = StringEscapeUtils.escapeHtml4(text);
        String highlightedHtml = newText.toString(); // Has { } to indicate annotations at correct indices.

        // Now parsed, so can escape HTML entities in the incoming string.
        //highlightedHtml = StringEscapeUtils.escapeHtml4(highlightedHtml);
        //highlightedHtml = highlightedHtml.replace("{", "<span class=\"highlight\">");
        //highlightedHtml = highlightedHtml.replace("}", "</span>");
        highlightedHtml = highlightedHtml;

        // Insert tags for carriage returns in HTML.
        //highlightedHtml = highlightedHtml.replaceAll("\\r?\\n", "<br/>\n");
        //System.out.println(highlightedHtml); //    	List<Node> timexNodes = createTimexNodes(
    }

    private static void test1() {
        System.out.println(
                edu.stanford.nlp.process.WhitespaceTokenizer.class
                //.getName()
                .getProtectionDomain().getCodeSource().getLocation().getPath());

        String in = //"Summer 2001\n\n"+
                "Fred's birthday\n\n"
                + "Jen and Tim's Wedding\n\n"
                + "Wedding ceremony of Jen and Tim\n\n"
                + "Jen's Wedding";

        //IOUtils.slurpFile()
        String date = null;//"2013-04-23";// props.getProperty("date");

        AnnotationPipeline pipeline;

        String uri = "edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger";

        Properties props = StringUtils.argsToProperties(new String[0]);
        props.put(
                "pos.model",
                uri);
        //"C:\\work\\code\\3rd_Ben\\stanford_nlp/src/edu/stanford/nlp/models/pos-tagger/english-left3words/english-left3words-distsim.tagger");

        //pipeline = StanfordNLPService.getPipeline(props, true);
        //StringWriter stringWriter = new StringWriter();
        //PrintWriter pw = new PrintWriter(stringWriter);
        //  List<TemporalExpression> processText = StanfordNLPService.processText(pipeline, in, date);
    }

//	public static List<TemporalExpression> processText(AnnotationPipeline pipeline, String text, String date) {
//
//		ArrayList<TemporalExpression> times = new ArrayList<>();
//
//		text = text.replace("{", "(").replace("}", ")");
//		System.err.println("Processing line: " + text);
//
//		Annotation annotation = StanfordNLPService.textToAnnotation(pipeline, text, date);
//
//		List<CoreMap> timexes = annotation.get(TimeAnnotations.TimexAnnotations.class);
//		for (CoreMap timexannotation : timexes) {
//
//			TemporalExpression tempex = new TemporalExpression();
//
//			tempex.setText(timexannotation.get(edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation.class));
//			tempex.setTimex(timexannotation.get(edu.stanford.nlp.time.TimeAnnotations.TimexAnnotation.class));
//			tempex.setDensity(timexannotation.get(TimePDF.TimePDFAnnotation.class));
//
//			times.add(tempex);
//		}
//		return times;
//	}
}
