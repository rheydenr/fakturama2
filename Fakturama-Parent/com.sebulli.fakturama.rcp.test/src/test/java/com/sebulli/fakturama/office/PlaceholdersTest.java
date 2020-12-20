package com.sebulli.fakturama.office;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class PlaceholdersTest {

	@Test
	public void testInterpretParameters_placeholderHasNoParams_Success() {
		Placeholders p = new Placeholders();
		Assertions.assertEquals("placeholder has no parameter!", "BOO", p.interpretParameters("DINGSDA", "BOO"));
	}
	
	@Test
	public void testInterpretParameters_placeholderHasPreParam_Success() {
		Placeholders p = new Placeholders();
		Assertions.assertEquals("placeholder was not correctly substituted with param!", "FooBar", p.interpretParameters("DINGSDA$PRE:Foo", "Bar"));
	}
	
	@Test
	public void testInterpretParameters_placeholderHasFirstParam_Success() {
		Placeholders p = new Placeholders();
		Assertions.assertEquals("1. placeholder was not correctly substituted with param!", "Foo", p.interpretParameters("DINGSDA$FIRST:3", "FooBar"));
		Assertions.assertEquals("2. placeholder was not correctly substituted with param!", "FooBar", p.interpretParameters("DINGSDA$FIRST:8", "FooBar"));
		Assertions.assertEquals("3. placeholder was not correctly substituted with param!", "", p.interpretParameters("DINGSDA$FIRST:0", "FooBar"));
		Assertions.assertEquals("4. placeholder was not correctly substituted with param!", "FooBar", p.interpretParameters("DINGSDA$FIRST:nodeal", "FooBar"));
	}

	@Test
	public void testInterpretParameters_placeholderHasLastParam_Success() {
		Placeholders p = new Placeholders();
		Assertions.assertEquals("1. placeholder was not correctly substituted with param!", "Bar", p.interpretParameters("DINGSDA$LAST:3", "FooBar"));
		Assertions.assertEquals("2. placeholder was not correctly substituted with param!", "FooBar", p.interpretParameters("DINGSDA$LAST:8", "FooBar"));
		Assertions.assertEquals("3. placeholder was not correctly substituted with param!", "", p.interpretParameters("DINGSDA$LAST:0", "FooBar"));
		Assertions.assertEquals("4. placeholder was not correctly substituted with param!", "FooBar", p.interpretParameters("DINGSDA$LAST:nodeal", "FooBar"));
	}
	
	@Test
	public void testInterpretParameters_placeholderHasRangeParam_Success() {
		Placeholders p = new Placeholders();
		Assertions.assertEquals("1. placeholder was not correctly substituted with param!", "ooB", p.interpretParameters("DINGSDA$RANGE:2,4", "FooBar"));
		Assertions.assertEquals("2. placeholder was not correctly substituted with param!", "ooBar", p.interpretParameters("DINGSDA$RANGE:2,8", "FooBar"));
		Assertions.assertEquals("3. placeholder was not correctly substituted with param!", "", p.interpretParameters("DINGSDA$RANGE:2,0", "FooBar"));
		Assertions.assertEquals("4. placeholder was not correctly substituted with param!", "ooBar", p.interpretParameters("DINGSDA$RANGE:2,nodeal", "FooBar"));
	}
	
	@Test
	public void testInterpretParameters_placeholderHasExRangeParam_Success() {
		Placeholders p = new Placeholders();
		Assertions.assertEquals("1. placeholder was not correctly substituted with param!", "Far", p.interpretParameters("DINGSDA$EXRANGE:2,4", "FooBar"));
		Assertions.assertEquals("2. placeholder was not correctly substituted with param!", "F", p.interpretParameters("DINGSDA$EXRANGE:2,8", "FooBar"));
		Assertions.assertEquals("3. placeholder was not correctly substituted with param!", "FoBar", p.interpretParameters("DINGSDA$EXRANGE:2,2", "FooBar"));
		Assertions.assertEquals("4. placeholder was not correctly substituted with param!", "", p.interpretParameters("DINGSDA$EXRANGE:0,6", "FooBar"));
		Assertions.assertEquals("5. placeholder was not correctly substituted with param!", "F", p.interpretParameters("DINGSDA$EXRANGE:2,nodeal", "FooBar"));
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
