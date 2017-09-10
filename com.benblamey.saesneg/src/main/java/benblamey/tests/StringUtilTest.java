package com.benblamey.tests;

import com.benblamey.core.StringUtils;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class StringUtilTest {

    @Test
    public void test0() {
        assertEquals(null, StringUtils.trimNonLetters(null));
    }

    @Test
    public void test0c() {
        assertEquals("", StringUtils.trimNonLetters(""));
    }

    @Test
    public void test0b() {
        assertEquals("F", StringUtils.trimNonLetters("F"));
    }

    @Test
    public void test0d() {
        assertEquals("", StringUtils.trimNonLetters("!"));
    }

    @Test
    public void test1() {
        assertEquals("Foo", StringUtils.trimNonLetters("Foo"));
    }

    @Test
    public void test2() {
        assertEquals("Foo", StringUtils.trimNonLetters("Foo "));
    }

    @Test
    public void test2e() {
        assertEquals("London", StringUtils.trimNonLetters("London-"));
    }

    @Test
    public void test2a() {
        assertEquals("Foo", StringUtils.trimNonLetters(" Foo"));
    }

    @Test
    public void test2b() {
        assertEquals("Foo", StringUtils.trimNonLetters(" Foo "));
    }

    @Test
    public void test2c() {
        assertEquals("Foo", StringUtils.trimNonLetters("Foo!"));
    }

    @Test
    public void test3() {
        assertEquals("Foo", StringUtils.trimNonLetters("!Foo"));
    }

    @Test
    public void test4() {
        assertEquals("Foo Bar", StringUtils.trimNonLetters("Foo Bar"));
    }

    @Test
    public void test5() {
        assertEquals("Foo!Bar", StringUtils.trimNonLetters("Foo!Bar"));
    }

    @Test
    public void test5c() {
        assertEquals("Foo!Bar", StringUtils.trimNonLetters(" Foo!Bar"));
    }

}
