package com.benblamey.tests;

import com.benblamey.core.onmi.onmi;
import java.io.IOException;
import org.junit.Test;

public class OnmiTest {

    @Test
    public void test() throws InterruptedException, IOException {
        Object foo = onmi.run("C:/work/code/3rd_Ben/Overlapping-NMI/a.txt",
                "C:/work/code/3rd_Ben/Overlapping-NMI/b.txt");
        foo.toString();
    }

}
