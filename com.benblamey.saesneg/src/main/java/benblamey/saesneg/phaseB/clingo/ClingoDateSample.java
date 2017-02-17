/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package benblamey.saesneg.phaseB.clingo;

import java.util.Iterator;
import org.joda.time.DateTime;

/**
 *
 * @author ben
 */
public class ClingoDateSample implements Iterable<DateTime> {

    DateTime _start = new DateTime(2013, 3, 1, 0, 0);
    DateTime _now = DateTime.now();

    @Override
    public Iterator<DateTime> iterator() {
        return new foo();
    }

    class foo implements Iterator<DateTime> {

        int _days = 0;

        @Override
        public boolean hasNext() {
            return next().isBefore(_now);
        }

        @Override
        public DateTime next() {
            DateTime result = _start.plusDays(_days);
            _days += 14;
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("Not supported.");
        }
    }

    public static String getDateTimeId(DateTime dt) {
        long millis = dt.getMillis();
        long id = (millis / 1000);
        return Long.toString(id);
    }
}
