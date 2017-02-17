/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package benblamey.core.onmi;

import com.benblamey.core.onmi.OnmiResult;
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
public class onmiResultTest {
    
    public onmiResultTest() {
    }
    
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
    public void testGetDump() {
        final String test1 = "NMI<Max>:\tnan\nOther measures:\n  lfkNMI:\t0\n  NMI<Sum>:\tnan\n";
        OnmiResult result = new OnmiResult(test1);
        Assert.assertEquals((Double)Double.NaN, (Double)result.Output.get(_NMI_MAX));
        Assert.assertEquals((Double)Double.NaN, (Double)result.Output.get(_NMI_Sum));
        Assert.assertEquals((Double)0.0, (Double)result.Output.get(_LFK_NMI));
    }
    
    @Test
    public void testGetDump2() {
        final String test1 = "NMI<Max>:\t1.0\nOther measures:\n  lfkNMI:\t2.0\n  NMI<Sum>:\t3.0\n";
        OnmiResult result = new OnmiResult(test1);
        Assert.assertEquals(result.Output.get(_NMI_MAX), (Double)1.0);
        Assert.assertEquals(result.Output.get(_LFK_NMI), (Double)2.0);
        Assert.assertEquals(result.Output.get(_NMI_Sum), (Double)3.0);
    }
    
    private static final String _NMI_MAX = "NMIMax";
    private static final String _NMI_Sum = "NMISum";
    private static final String _LFK_NMI = "lfkNMI";
    
}
