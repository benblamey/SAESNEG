package com.benblamey.core;

import com.benblamey.core.json.JavaToJson;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class JavaToJsonTest {

    @Test
    public void test() {
        assertEquals("6", JavaToJson.toJSON(6));
    }

    @Test
    public void test2() {

        Object foo2 = new Object() {
            private int foo;

            {
                foo = 5;
            }
        };

        assertEquals("{\n\"foo\":5\n}\n", JavaToJson.toJSON(foo2));
    }


    @Test
    public void test3() {
        Object foo3 = new Object() {
            private Double foo;
            {
                foo = 5.0;
            }
        };
        assertEquals("{\n\"foo\":5.0\n}\n", JavaToJson.toJSON(foo3));
    }

    @Test
    public void test3a() {
        Object foo3 = new Object() {
            private Double foo1;
            private Double foo2;
            private Double foo3;
            {
                foo1 = Double.NEGATIVE_INFINITY;
                foo2 = Double.POSITIVE_INFINITY;
                foo3 = Double.NaN;
            }
        };
        assertEquals("{\n\"foo1\":\"-INF\",\n\"foo2\":\"+INF\",\n\"foo3\":\"NAN\"\n}\n", JavaToJson.toJSON(foo3));
    }


    @Test
    public void test3b() {
        Object foo3 = new Object() {
            private double foo1;
            private double foo2;
            private double foo3;
            {
                foo1 = Double.NEGATIVE_INFINITY;
                foo2 = Double.POSITIVE_INFINITY;
                foo3 = Double.NaN;
            }
        };
        assertEquals("{\n\"foo1\":\"-INF\",\n\"foo2\":\"+INF\",\n\"foo3\":\"NAN\"\n}\n", JavaToJson.toJSON(foo3));
    }


    @Test
    public void test4() {
        Object foo4 = new Object() {
            private Object foo;
            {
                foo = null;
            }
        };
        assertEquals("{\n\"foo\":null\n}\n", JavaToJson.toJSON(foo4));
    }

    @Test
    public void test4a() {
        Object foo4 = new Object() {
            private Double foo;
            {
                foo = null;
            }
        };
        assertEquals("{\n\"foo\":null\n}\n", JavaToJson.toJSON(foo4));
    }


}
