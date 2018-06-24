package com.benblamey.saesneg.model.annotations;

import com.benblamey.core.DateUtil;
import edu.stanford.nlp.time.distributed.TimeDensityFunction;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

public class PhotoUploadTimeDensity extends TimeDensityFunction{

    private final static String gaussianFormat = " x < (%s - 60*60*24) ? (exp(-1.0204 * log(%s - x) + 18.5702)) : 0.0"; // 1/(%s * sqrt(2 * pi)) *
    private DateTime _time;

    public PhotoUploadTimeDensity(DateTime time) {
        _time = time;
    }

    public PhotoUploadTimeDensity(Date createdTime) {
        _time = new DateTime(createdTime);
    }

    public double getDensity(DateTime time) {
        int x = Seconds.secondsBetween(time, _time).getSeconds();

        if (x < 0) {
            return 0;
        } else if (x < 24 * 60 * 60) {
            x = 24 * 60 * 60;
        }

        return Math.exp(-1.0204 * Math.log(x) + 18.5702); // From tempex paper.
    }

    public String getGNUPlot(String millTimeSecondsExpr) {
        String uploadTime = Integer.toString(DateUtil.ToMilleniumTime(_time));
        String plot = String.format(gaussianFormat, uploadTime, uploadTime); // sd, mean, sd,
        // System.err.print(plot + "\n");
        return plot;
    }

}
