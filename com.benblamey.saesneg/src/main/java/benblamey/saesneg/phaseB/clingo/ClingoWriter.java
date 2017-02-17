//package benblamey.saesneg.phaseB.clingo;
//
//import java.io.BufferedWriter;
//import java.io.FileWriter;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashSet;
//
//import org.joda.time.DateTime;
//
//import benblamey.saesneg.model.UserContext;
//import benblamey.saesneg.model.annotations.TemporalAnnotation;
//import benblamey.saesneg.model.datums.Datum;
//import clingowrapper.ParseException;
//import edu.stanford.nlp.time.distributed.TimeDensityFunction;
//
//public class ClingoWriter {
//
//    private static final int SCALING_FACTOR = 100;
//    private static final double ZERO = 1.0 / (SCALING_FACTOR * 10);
//
//    public void WriteClingo(UserContext user) throws IOException, ParseException, ParseException {
//
//        ClingoDateSample sample = new ClingoDateSample();
//        BufferedWriter out = new BufferedWriter(new FileWriter(
//                user.getOutputDirectoryWithTrailingSlash() + "events_1.gen.clingo"));
//
//        try {
//
//            HashSet<Datum> mfos = new HashSet<Datum>();
//
//            // 284 datums for Participant 1 - ASP fails.
//            // Instead, filter only those with tempexes.
//            for (Datum mfo : user.getLifeStory().datums) {
//
//                boolean add = true;
//
//                for (TemporalAnnotation t : mfo.getAnnotations().DateTimesAnnotations) {
//                    CanExpressTimeAsFunction density = t.getDensity();
//                    if (density == null) {
//                        add = false;
//                        continue;
//                    }
//                    TimeDensityFunction GettimeDensityFunction = density.getTimeDensityFunction();
//                    if (GettimeDensityFunction == null) {
//                        add = false;
//                        continue;
//                    }
//                }
//
//                if (add) {
//                    mfos.add(mfo);
//                }
//
//                if (mfos.size() > 10) {
//                    break;
//                }
//
//            }
//
//            out.append("#const n_datums = " + mfos.size() + ".");
//            out.append("\n");
//            out.append("\n");
//
//            for (Datum mfo : mfos) {
//                out.append("datum(" + mfo.getNetworkID().toString() + ").");
//                out.append("\n");
//            }
//
//            out.append("\n");
//
//            for (DateTime time : sample) {
//                out.append("time(" + ClingoDateSample.getDateTimeId(time) + ").");
//                out.append("\n");
//            }
//
//            for (Datum mfo : mfos) {
//                ArrayList<Double> densities = new ArrayList<Double>();
//                double sum = 0;
//                for (DateTime dt : sample) {
//                    double dens = 1;
//
//                    //mfo.getTimes();
//                    for (TemporalAnnotation t : mfo.getAnnotations().DateTimesAnnotations) {
//                        CanExpressTimeAsFunction density = t.getDensity();
//                        if (density != null) {
//                            TimeDensityFunction GettimeDensityFunction = density.getTimeDensityFunction();
//                            if (GettimeDensityFunction != null) {
//                                try {
//                                    double GetDensity = GettimeDensityFunction.getDensity(dt);
//                                    dens *= GetDensity;
//                                    if (dens < ZERO) {
//                                        break;
//                                    }
//                                } catch (UnsupportedOperationException e) {
//                                }
//                            }
//                        }
//                    }
//
//                    densities.add(dens);
//                    sum += dens;
//                }
//
//                int i = 0;
//                for (DateTime dt : sample) {
//                    double dens = (double) densities.get(i) / sum;
//                    out.append("datum_at_time(" + mfo.getNetworkID() + ","
//                            + ClingoDateSample.getDateTimeId(dt) + ","
//                            + ScaleNumber(dens) + ").");
//                    out.append("\n");
//                    // dens
//                    // dt
//                    // mfo
//                    i++;
//                }
//            }
//
//        } finally {
//            out.close();
//        }
//
//        Process p = Runtime.getRuntime().exec(
//                new String[]{
//                    "cmd",
//                    "/c",
//                    "C:\\work\\code\\Ben\\ben_phd_java\\BenTimeline2\\src\\clingo\\concat_clingo.bat"
//                });
//
//        // ClingoOutput clingoResult = ClingoWrapper.RunClingo(user.getOutputDirectoryWithTrailingSlash() + "events.clingo");
//    }
//
//    private static String ScaleNumber(double number) {
//        return Integer.toString((int) (number * SCALING_FACTOR));
//    }
//}
