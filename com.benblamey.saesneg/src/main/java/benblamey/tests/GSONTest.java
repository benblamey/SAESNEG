/**
 *
 */
package com.benblamey.tests;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Field;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

/**
 * @author Ben Blamey ben@benblamey.com
 *
 */
public class GSONTest {

    @Test
    public void test() throws IllegalArgumentException, IllegalAccessException {
        GSONTestClass foo = new GSONTestClass() {
            {
                SomeInt = 6;
            }
        };

        GsonBuilder builder = new GsonBuilder();
        builder.setExclusionStrategies(new ExclusionStrategy() {

            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return false;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        });

        Gson create = builder.create();

        String json = create.toJson(foo);

        Class<?> clazz = foo.getClass();
        Field[] f = clazz.getFields();
        System.out.println(f[0].get(foo));

        System.out.println(json);
        assertNotNull(json);
    }

    public void test2() {

        GSONTestClass foo = new GSONTestClass() {
            {
                SomeInt = 6;
            }
        };

        //simple.
    }
}
