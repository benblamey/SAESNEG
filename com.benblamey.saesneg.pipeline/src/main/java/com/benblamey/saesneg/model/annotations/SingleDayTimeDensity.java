package com.benblamey.saesneg.model.annotations;

import com.benblamey.core.DateUtil;
import edu.stanford.nlp.time.distributed.TimeDensityFunction;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

public class SingleDayTimeDensity extends TimeDensityFunction {

    DateTime _time;

    public SingleDayTimeDensity(DateTime jodaTime) {
        _time = jodaTime;
    }

    public SingleDayTimeDensity(Date time) {

        _time = new DateTime(time);
    }

    @Override
    public double getDensity(DateTime time) {
        LocalDate firstDate = _time.toLocalDate();
        LocalDate secondDate = time.toLocalDate();
        return firstDate.equals(secondDate) ? 1 : 0;
    }

    @Override
    public String getGNUPlot(String millTimeSecondsExpr) {
        int start = DateUtil.ToMilleniumTime(_time.secondOfDay().setCopy(0));
        int end = start + 24 * 60 * 60;
        return "(x > " + start + " && x < " + end + ")";
    }

}
