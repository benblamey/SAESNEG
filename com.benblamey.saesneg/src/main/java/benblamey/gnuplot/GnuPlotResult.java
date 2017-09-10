package com.benblamey.gnuplot;

public class GnuPlotResult {

    public String filePath;

    public String fileName;

    public String command;

    public String title;

    public static final GnuPlotResult Empty = new GnuPlotResult() {
        {
            fileName = "missing.png";
        }
    };

    public double totalMass;

}
