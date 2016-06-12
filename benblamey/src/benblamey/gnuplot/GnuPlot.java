package benblamey.gnuplot; 

//import benblamey.experiments.pipeline.UserContext;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.codec.digest.DigestUtils;
import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Seconds;

public class GnuPlot {

    private final String _outputDirectory;
    private static final String ValuePattern = "yyyy-M-d";
    private static final DateTime s_startMillenium = new DateTime(2000, 1, 1, 0, 0);

    public static void main(String[] args) throws IOException, InterruptedException {
        GnuPlot jp = new GnuPlot("C:\\work\\data\\gnuplot\\");
        // jp.plots.add("tm_mday(x)>3");
        jp.exportGraph("x > 383011200 && x < 383097600");
    }

    public GnuPlot(String outputDirectory) {
        if (null == outputDirectory) {
            throw new RuntimeException("outputDirectory cannot be null.");
        }
        _outputDirectory = outputDirectory;
    }
    
    // ************************************
    // NOTE:
    //    tm_mon is zero-indexed.
    // ************************************
    public GnuPlotResult drawDemoPlots() throws IOException, InterruptedException {
        return exportGraph(
                Arrays.asList(
                    "(x>60*60*24*365*12)*(x<60*60*24*(365*12+1)) title 'some day'",
                    "(tm_year(x)==2012)*(tm_mon(x)==4) title 'May2012'"
                )
        );
    }
    
    public GnuPlotResult exportGraph(String plot) throws IOException, InterruptedException {
        return exportGraph(Arrays.asList(plot));
    }
    
    public GnuPlotResult exportGraph(List<String> plots) throws IOException, InterruptedException {

        GnuPlotResult result = new GnuPlotResult();
        result.filePath = _outputDirectory;
        
        if (plots.isEmpty()) {
            result.command = null;
            result.fileName = "emptygraph.png";
            return result;
        }

        final String minorLabelPattern = "MMM";
        final String majorLabelPattern = "YY";

        DateTime timeCursorTemp = new DateTime(2008, 1, 1, 0, 0); // was 2011

        String xStart = timeCursorTemp.toString(ValuePattern);
        String xEnd = xStart;
        String xtics = null;

        while (timeCursorTemp.isBeforeNow()) {

            String label;
            if (timeCursorTemp.getMonthOfYear() == 6) {
                label = timeCursorTemp.toString(majorLabelPattern);
            } else {
                label = timeCursorTemp.toString(minorLabelPattern).substring(0, 1);
            }

            if (label != null) {
                if (xtics == null) {
                    xtics = "";
                } else {
                    xtics = xtics + ", ";
                }

                xtics = xtics + "'" + label + "' '" + timeCursorTemp.toString(ValuePattern)
                        + "'";
                xEnd = timeCursorTemp.toString(ValuePattern);
            }

            timeCursorTemp = timeCursorTemp.plusMonths(1);
        }

        final String DummyFileName = "DUMMYFILENAME.png";

        
        result.command =                
            "syear = 366*24*60*60;"
            +"min2(x,y) = x<y?x:y;"
            +"max2(x,y) = x>y?x:y;"
            +"dsyear(x,y) = min2 (   max2(syear(x),syear(y)) - min2(syear(x),syear(y)) , syear + min2(syear(x),syear(y)) - max2(syear(x),syear(y)) );"
            +"syear(x) = (tm_yday(x)-1)*24*60*60 + tm_hour(x)*60*60 + tm_min(x)*60 + tm_sec(x);"

                // remember to change samples also
            +"set term png size 2500,200;"// large size 800,600;" // was 1000x100
            //+ "set autoscale; "
            //+ "set grid;"
            //+ "set format y \"%0.f\";"
            + "set samples 2500;" // samples of x-axis.
            //+ "set xmtics;"
            // + "set xtics 0,365,10;"
            //  + "set xtics add (\"feb\" 32);"

            // + "set bmargin 0;"
            // + "set lmargin 0;"
            // + "set rmargin 0;"
            + "set tmargin 0;"
            + "set xdata time;"
            + "set timefmt \'%Y-%m-%d\';" // same as $pattern
            + "set xtics format \'%b %d\';"
            + "set xrange [\'" + xStart + "\':\'" + xEnd + "\'];"
            + "set xtics (" + xtics + ");"
            + "set output '" + DummyFileName + "';"
           // + "set yrange [0:1.1];"
            //  + "set xrange [0:"++"];"
            + "plot";

        for (int i = 0; i < plots.size(); i++) {
            result.command +=
                    (((i > 0) ? ", " : " ")
                    + plots.get(i)
                    + ((i == plots.size() - 1) ? ";" : ""));
        }

        // Apache commons codec lib.
        String argumentsHex = DigestUtils.shaHex(result.command);

        
        result.fileName = argumentsHex
                + ".png";
        
        System.out.println(result.fileName);
        
        // Otherwise, fill in the actual filename into the parameters.
        result.command = result.command.replace(DummyFileName, (_outputDirectory + "\\" + result.fileName).replace("\\", "//"));
        
        // If the same plot already exists, return that.
        if (!new File(_outputDirectory + "\\" + result.fileName).exists())  {
            runGnuPlot(result);
        }

        return result;
    }

    private void runGnuPlot(GnuPlotResult result) throws InterruptedException, IOException {
        String gnuplotPath;
        if (System.getProperty("os.name").startsWith("Windows")) {
            gnuplotPath = "C:\\Program Files (x86)\\gnuplot\\bin\\gnuplot.exe";
        } else {
            gnuplotPath = "gnuplot";
        }
        
        String[] s = {gnuplotPath,
            "-e",
            result.command
        };
        
        Runtime rt = Runtime.getRuntime();
        Process proc = rt.exec(s);
        
        InputStream stdin = proc.getErrorStream();
        InputStreamReader isr = new InputStreamReader(stdin);
        
        BufferedReader br = new BufferedReader(isr);
        
        String line;
        while ((line = br.readLine()) != null) {
            System.err.println("gnuplot:" + line);
        }
        
        int exitVal = proc.waitFor();
        if (exitVal != 0) {
            System.err.print("gnuplot Process exitValue: " + exitVal);
            
            // Delete the image so that we attempt to re-create it in the future.
            File png = new File(_outputDirectory + "\\" + result.fileName);
            png.delete();
        }
        
        
        proc.getInputStream().close();
        proc.getOutputStream().close();
        proc.getErrorStream().close();
    }
    
    public String escapeTitle(String title) {
        if (title == null) {
            return "null"; 
        } else {
            return title.replace("'", "?").replace("\"", "?").replace("`", "?");
        }
    }
    
    /**
     * Seconds since January 1st 2000 is used for gnuplot.
     * @return 
     */
    public static int toSecondsSinceY2K(Instant dateTime) {
        try {
            return Seconds.secondsBetween(s_startMillenium, dateTime).getSeconds();
        } catch (ArithmeticException e) {
            System.out.println("offending date: " + dateTime.toString());
            return 0;
        }
    }
    

}

        // + "sin(x)"
        //  + ", cos(x)"
        //+ ", tm_hour(x)"
        ///  + "tm_mday(x)>3"
        //   + ", (x>60*60*24*365*12)*(x<60*60*24*(365*12+1));";
        //                    + "set xdata time;"
        //                    + "set timefmt \"%Y-%m-%d-%H:%M:%S\";"
        //                    + "set xlabel \"Dates\";"
        //                    + "set ylabel \"Data transferred (bytes)\";"
        //                    + "plot \""+x+"\" using 1:2 title \"Total:"+tot+"\" with linespoints;"
