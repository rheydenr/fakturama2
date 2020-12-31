package com.sebulli.fakturama.office;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PlaceholdersTest {

    @Test
    public void testInterpretParameters_placeholderHasNoParams_Success() {
        Placeholders p = new Placeholders();
        Assertions.assertEquals("BOO", p.interpretParameters("DINGSDA", "BOO"), "placeholder has no parameter!");
    }

    @Test
    public void testInterpretParameters_placeholderHasPreParam_Success() {
        Placeholders p = new Placeholders();
        Assertions.assertEquals("FooBar", p.interpretParameters("DINGSDA$PRE:Foo", "Bar"), "placeholder was not correctly substituted with param!");
    }

    @Test
    public void testInterpretParameters_placeholderHasFirstParam_Success() {
        Placeholders p = new Placeholders();
        Assertions.assertEquals("Foo", p.interpretParameters("DINGSDA$FIRST:3", "FooBar"), "1. placeholder was not correctly substituted with param!");
        Assertions.assertEquals("FooBar", p.interpretParameters("DINGSDA$FIRST:8", "FooBar"), "2. placeholder was not correctly substituted with param!");
        Assertions.assertEquals("", p.interpretParameters("DINGSDA$FIRST:0", "FooBar"), "3. placeholder was not correctly substituted with param!");
        Assertions.assertEquals("FooBar", p.interpretParameters("DINGSDA$FIRST:nodeal", "FooBar"), "4. placeholder was not correctly substituted with param!");
    }

    @Test
    public void testInterpretParameters_placeholderHasLastParam_Success() {
        Placeholders p = new Placeholders();
        Assertions.assertEquals("Bar", p.interpretParameters("DINGSDA$LAST:3", "FooBar"), "1. placeholder was not correctly substituted with param!");
        Assertions.assertEquals("FooBar", p.interpretParameters("DINGSDA$LAST:8", "FooBar"), "2. placeholder was not correctly substituted with param!");
        Assertions.assertEquals("", p.interpretParameters("DINGSDA$LAST:0", "FooBar"), "3. placeholder was not correctly substituted with param!");
        Assertions.assertEquals("FooBar", p.interpretParameters("DINGSDA$LAST:nodeal", "FooBar"), "4. placeholder was not correctly substituted with param!");
    }

    @Test
    public void testInterpretParameters_placeholderHasRangeParam_Success() {
        Placeholders p = new Placeholders();
        Assertions.assertEquals("ooB", p.interpretParameters("DINGSDA$RANGE:2,4", "FooBar"), "1. placeholder was not correctly substituted with param!");
        Assertions.assertEquals("ooBar", p.interpretParameters("DINGSDA$RANGE:2,8", "FooBar"), "2. placeholder was not correctly substituted with param!");
        Assertions.assertEquals("", p.interpretParameters("DINGSDA$RANGE:2,0", "FooBar"), "3. placeholder was not correctly substituted with param!");
        Assertions.assertEquals("ooBar", p.interpretParameters("DINGSDA$RANGE:2,nodeal", "FooBar"), "4. placeholder was not correctly substituted with param!");
    }

    @Test
    public void testInterpretParameters_placeholderHasExRangeParam_Success() {
        Placeholders p = new Placeholders();
        Assertions.assertEquals("Far", p.interpretParameters("DINGSDA$EXRANGE:2,4", "FooBar"), "1. placeholder was not correctly substituted with param!");
        Assertions.assertEquals("F", p.interpretParameters("DINGSDA$EXRANGE:2,8", "FooBar"), "2. placeholder was not correctly substituted with param!");
        Assertions.assertEquals("FoBar", p.interpretParameters("DINGSDA$EXRANGE:2,2", "FooBar"), "3. placeholder was not correctly substituted with param!");
        Assertions.assertEquals("", p.interpretParameters("DINGSDA$EXRANGE:0,6", "FooBar"), "4. placeholder was not correctly substituted with param!");
        Assertions.assertEquals("F", p.interpretParameters("DINGSDA$EXRANGE:2,nodeal", "FooBar"), "5. placeholder was not correctly substituted with param!");
    }

    //	@Test
    //	public void testExtractParam() {
    //		fail("Not yet implemented");
    //	}
    //
    //	@Test
    //	public void testIsPlaceholder() {
    //		fail("Not yet implemented");
    //	}

}
