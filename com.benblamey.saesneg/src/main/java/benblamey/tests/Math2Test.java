/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.benblamey.tests;

import com.benblamey.core.Math2;
import com.mysql.jdbc.NotImplemented;
import java.util.ArrayList;

/**
 *
 * @author Ben
 */
public class Math2Test {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws NotImplemented {

        final int mod = 12;

        test(2, Math2.distUndermod(11, 1, mod));
        test(2, Math2.distUndermod(1, 11, mod));
        test(3, Math2.distUndermod(3, 6, mod));
        test(3, Math2.distUndermod(6, 3, mod));

        ArrayList<Double> values = new ArrayList<Double>();
        values.add(2.0);
        values.add(4.0);
        values.add(4.0);
        values.add(4.0);
        values.add(5.0);
        values.add(5.0);
        values.add(7.0);
        values.add(9.0);

        test(2, Math2.standardDeviationUnderModulo(values, 5, 20));

    }

    private static void test(int i, double distanceUnderModulo) {
        System.out.println("Expected: " + i + " got " + distanceUnderModulo);

    }
}
