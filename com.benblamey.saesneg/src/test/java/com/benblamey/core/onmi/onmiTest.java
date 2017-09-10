/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.benblamey.core.onmi;

import com.benblamey.core.JavaResources;
import com.benblamey.core.onmi.OnmiResult;
import com.benblamey.core.onmi.onmi;
import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author ben-laptop
 */
public class onmiTest {

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of getDump method, of class onmiResult.
     */
    @Test
    public void testGetDump() throws InterruptedException, IOException {


        File f1 = new File(this.getClass().getResource("onmiData1").toString());

        OnmiResult result = onmi.run(f1, f1);

        //System.err.println();

        Assert.fail(result.getDump());
        //final String test1 = "NMI<Max>:\tnan\nOther measures:\n  lfkNMI:\t0\n  NMI<Sum>:\tnan\n";
        //OnmiResult result = new OnmiResult(test1);
//        Assert.assertEquals(result.Output.get(_NMI_MAX), "INFINITY");
//        Assert.assertEquals(result.Output.get(_NMI_Sum), Double.POSITIVE_INFINITY);
//        Assert.assertEquals(result.Output.get(_LFK_NMI), 0.0);
    }

    public static void main(String[] args) throws InterruptedException, IOException {

        {
            File f1 = new File(JavaResources.getAbsoluteResourceFilePath(onmiTest.class, "onmiDataA-1"));
            File f2 = new File(JavaResources.getAbsoluteResourceFilePath(onmiTest.class, "onmiDataB-1"));
            System.out.println(f1.getAbsolutePath());
            System.out.println(f2.getAbsolutePath());
            OnmiResult result = onmi.run(f1, f2);
            System.out.println(result.getDump());

            // 1 vs. 1, yields:
            // NMI<Max>:	nan
            // Other measures:
            //  lfkNMI:	0
            //  NMI<Sum>:	nan
        }


        {
            File f1 = new File(JavaResources.getAbsoluteResourceFilePath(onmiTest.class, "onmiDataC-1 2"));
            File f2 = new File(JavaResources.getAbsoluteResourceFilePath(onmiTest.class, "onmiDataD-1 2"));
            System.out.println(f1.getAbsolutePath());
            System.out.println(f2.getAbsolutePath());
            OnmiResult result = onmi.run(f1, f2);
            System.out.println(result.getDump());

            // 1 vs. 1, yields:
            // NMI<Max>:	nan
            // Other measures:
            //  lfkNMI:	0
            //  NMI<Sum>:	nan
        }

        {
            File f1 = new File(JavaResources.getAbsoluteResourceFilePath(onmiTest.class, "onmiDataE-12 34"));
            File f2 = new File(JavaResources.getAbsoluteResourceFilePath(onmiTest.class, "onmiDataF-12 34"));
            System.out.println(f1.getAbsolutePath());
            System.out.println(f2.getAbsolutePath());
            OnmiResult result = onmi.run(f1, f2);
            System.out.println(result.getDump());

            // 1 vs. 1, yields:
            // NMI<Max>:	nan
            // Other measures:
            //  lfkNMI:	0
            //  NMI<Sum>:	nan
        }
    }

    private static final String _NMI_MAX = "NMIMax";
    private static final String _NMI_Sum = "NMISum";
    private static final String _LFK_NMI = "lfkNMI";

}
