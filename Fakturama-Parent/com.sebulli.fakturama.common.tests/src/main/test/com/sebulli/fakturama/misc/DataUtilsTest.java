/**
 * 
 */
package com.sebulli.fakturama.misc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.money.MonetaryAmount;

import org.apache.commons.lang3.StringUtils;
import org.javamoney.moneta.Money;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.ibm.icu.util.ULocale;
import com.sebulli.fakturama.common.Activator;
import com.sebulli.fakturama.i18n.ILocaleService;

/**
 *
 */
public class DataUtilsTest {
	private static final double EPSILON = 0.00000001;

	@Mock
	private ILocaleService localeService;
	
	private DataUtils dataUtils;


	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		Mockito.when(localeService.getDefaultLocale()).thenReturn(ULocale.GERMANY);
		Mockito.when(localeService.getCurrencyLocale()).thenReturn(ULocale.GERMANY);
		Dictionary<String, Object> dict = new Hashtable<>();
		dict.put(org.osgi.framework.Constants.SERVICE_RANKING, Integer.MAX_VALUE);
		Activator.getContext().registerService(ILocaleService.class, localeService, dict);
		dataUtils = DataUtils.getInstance();
	}

	/**
	 * Test method for {@link com.sebulli.fakturama.misc.DataUtils#getDefaultCurrencyUnit()}.
	 */
	@Test
	public void testGetDefaultCurrencyUnit() {
		Assert.assertTrue("EUR".contentEquals(dataUtils.getDefaultCurrencyUnit().toString()));
	}

	/**
	 * Test method for {@link com.sebulli.fakturama.misc.DataUtils#DoublesAreEqual(java.lang.Double, java.lang.Double)}.
	 */
	@Test
	public void testDoublesAreEqual() {
		Double testValue = Double.valueOf(10);
		Assert.assertTrue(dataUtils.DoublesAreEqual(testValue, Double.valueOf(10.000000001)));
		Assert.assertFalse(dataUtils.DoublesAreEqual(testValue, Double.valueOf(10.0001)));

		testValue *= -1;
		Assert.assertTrue(dataUtils.DoublesAreEqual(testValue, Double.valueOf(-10.000000001)));
		Assert.assertFalse(dataUtils.DoublesAreEqual(testValue, Double.valueOf(-10.0001)));
	}

	/**
	 * Test method for {@link com.sebulli.fakturama.misc.DataUtils#StringToDouble(java.lang.String)}.
	 */
	@Test
	public void testStringToDouble() {
		Double testValue = Double.valueOf(10);
		Assert.assertEquals(testValue, dataUtils.StringToDouble("10"));
		Assert.assertEquals(testValue, dataUtils.StringToDouble("+10"));
		Assert.assertEquals(testValue, dataUtils.StringToDouble("10,0"));
		Assert.assertEquals(testValue, dataUtils.StringToDouble("10.0"));
		
		Assert.assertEquals(Double.valueOf(0.1), dataUtils.StringToDouble("10%"));
		Assert.assertEquals(Double.valueOf(0.1), dataUtils.StringToDouble("10,0 %"));
		Assert.assertEquals(Double.valueOf(0.1), dataUtils.StringToDouble("10.0 %"));
		Assert.assertEquals(Double.valueOf(0.1), dataUtils.StringToDouble("10,0 %"));
		
		Assert.assertEquals(Double.valueOf(0.1), dataUtils.StringToDouble("10;0 %"));
		// a little bit crazy... Should be fixed in future.
		Assert.assertEquals(Double.valueOf(0.1), dataUtils.StringToDouble("a10#0 %"));
	}

	/**
	 * Test method for {@link com.sebulli.fakturama.misc.DataUtils#round(java.lang.Double)}.
	 */
	@Test
	public void testRound() {
		Double testValue = Double.valueOf(10.12645);
		Assert.assertEquals(Double.valueOf(10.13), dataUtils.round(testValue)); // scale 2 is the default
		Assert.assertEquals(Double.valueOf(10.126), dataUtils.round(testValue, 3));
		Assert.assertEquals(Double.valueOf(10.1265), dataUtils.round(testValue, 4));
		Assert.assertEquals(Double.valueOf(10.12645), dataUtils.round(testValue, 5));
		
		testValue *= -1;
		Assert.assertEquals(Double.valueOf(-10.13), dataUtils.round(testValue)); // scale 2 is the default
		Assert.assertEquals(Double.valueOf(-10.126), dataUtils.round(testValue, 3));
		Assert.assertEquals(Double.valueOf(-10.1265), dataUtils.round(testValue, 4));
		Assert.assertEquals(Double.valueOf(-10.12645), dataUtils.round(testValue, 5));
	}

	/**
	 * Test method for {@link com.sebulli.fakturama.misc.DataUtils#CalculateGrossFromNet(javax.money.MonetaryAmount, java.lang.Double)}.
	 */
	@Test
	public void testCalculateGrossFromNetMonetaryAmountDouble() {
		MonetaryAmount netValue = Money.of(BigDecimal.valueOf(100), "EUR");
		MonetaryAmount grossValue = Money.of(BigDecimal.valueOf(110), "EUR");
		
		Assert.assertTrue(grossValue.isEqualTo(dataUtils.CalculateGrossFromNet(netValue, Double.valueOf(0.1))));
	}

	/**
	 * Test method for {@link com.sebulli.fakturama.misc.DataUtils#CalculateGrossFromNet(java.lang.Double, java.lang.Double)}.
	 */
	@Test
	public void testCalculateGrossFromNetDoubleDouble() {
		Assert.assertEquals(Double.valueOf(110), dataUtils.CalculateGrossFromNet(Double.valueOf(100), Double.valueOf(0.1)), EPSILON);
	}

	/**
	 * Test method for {@link com.sebulli.fakturama.misc.DataUtils#calculateNetFromGross(java.lang.String, java.lang.Double)}.
	 */
	@Test
	public void testCalculateNetFromGrossStringDouble() {
		Double netValue = Double.valueOf(100);
		MonetaryAmount testValue = dataUtils.calculateNetFromGross("110 EUR", Double.valueOf(0.1));
		Assert.assertEquals(netValue, testValue.getNumber().doubleValue(), EPSILON);
	}

	/**
	 * Test method for {@link com.sebulli.fakturama.misc.DataUtils#calculateNetFromGross(java.lang.Double, java.lang.Double)}.
	 */
	@Test
	public void testCalculateNetFromGrossDoubleDouble() {
		Double netValue = Double.valueOf(100);
		MonetaryAmount testValue = dataUtils.calculateNetFromGross(Double.valueOf(110), Double.valueOf(0.1));
		Assert.assertEquals(netValue, testValue.getNumber().doubleValue(), EPSILON);
	}

	/**
	 * Test method for {@link com.sebulli.fakturama.misc.DataUtils#calculateNetFromGross(java.lang.String, java.lang.Double, javax.money.MonetaryAmount)}.
	 */
	@Test
	public void testCalculateNetFromGrossStringDoubleMonetaryAmount() {
		Money amount = Money.of(Double.valueOf(100), "EUR");
		MonetaryAmount testValue = dataUtils.calculateNetFromGross("110", Double.valueOf(0.1));
		Assert.assertEquals(amount, dataUtils.getDefaultRounding().apply(testValue));
		
		amount = Money.of(Double.valueOf(100.15), "EUR");
		testValue = dataUtils.calculateNetFromGross("110,17", Double.valueOf(0.1));
		Assert.assertEquals(amount, dataUtils.getDefaultRounding().apply(testValue));
	}

	/**
	 * Test method for {@link com.sebulli.fakturama.misc.DataUtils#calculateNetFromGrossAsDouble(java.lang.Double, java.lang.Double)}.
	 */
	@Test
	public void testCalculateNetFromGrossAsDouble() {
		Assert.assertEquals(Double.valueOf(100.0), dataUtils.calculateNetFromGrossAsDouble(110.0, 0.1), EPSILON);
	}

	/**
	 * Test method for {@link com.sebulli.fakturama.misc.DataUtils#addToDate(java.util.Date, int)}.
	 */
	@Test
	public void testAddToDate() {
		LocalDateTime testValue = LocalDateTime.of(2019, 2, 10, 0, 0);
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(2019, 1, 1);
		LocalDateTime retval = dataUtils.addToDate(calendar.getTime(), 9);
		Assert.assertTrue(String.format("returned value [%s] is not equal to expected value [%s]", retval, testValue.format(DateTimeFormatter.ISO_DATE)), testValue.isEqual(retval));
	}

	/**
	 * Test method for {@link com.sebulli.fakturama.misc.DataUtils#removeCR(java.lang.String)}.
	 */
	@Test
	public void testRemoveCR() {
		String aStringWithManyLinebreaks = StringUtils.join(new String[] {"a", "String", "With", "Many", "Linebreaks"}, System.lineSeparator());
		Assert.assertTrue("aStringWithManyLinebreaks".contentEquals(dataUtils.removeCR(aStringWithManyLinebreaks)));
	}

	/**
	 * Test method for {@link com.sebulli.fakturama.misc.DataUtils#makeOSLineFeeds(java.lang.String)}.
	 */
	@Test
	public void testMakeOSLineFeeds() {
		String stringWithLinefeed = "stringWith\nLinefeed";
		String stringWithOSLinefeed = "stringWith"+System.lineSeparator()+"Linefeed";
		Assert.assertTrue(stringWithOSLinefeed.contentEquals(dataUtils.makeOSLineFeeds(stringWithLinefeed)));
	}

	/**
	 * Test method for {@link com.sebulli.fakturama.misc.DataUtils#MultiLineStringsAreEqual(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testMultiLineStringsAreEqual() {
		DataUtils dataUtils = DataUtils.getInstance();
		String myFirstTestString = "A long line"+System.lineSeparator()+"with a break";
		String mySecondTestString = "A long line\rwith a break";
		Assert.assertTrue(dataUtils.MultiLineStringsAreEqual(myFirstTestString, mySecondTestString));
	}

	/**
	 * Test method for {@link com.sebulli.fakturama.misc.DataUtils#convertCRLF2LF(java.lang.String)}.
	 */
	@Test
	public void testConvertCRLF2LF() {
		DataUtils dataUtils = DataUtils.getInstance();
		String myTestString = "A long line\r\nwith a break";
		Assert.assertEquals("A long line\nwith a break", dataUtils.convertCRLF2LF(myTestString));
	}

	/**
	 * Test method for {@link com.sebulli.fakturama.misc.DataUtils#getSingleLine(java.lang.String)}.
	 */
	@Test
	public void testGetSingleLine() {
		DataUtils dataUtils = DataUtils.getInstance();
		String myTestString = "A long line" + System.lineSeparator() + "with a break";
		Assert.assertEquals("A long line", dataUtils.getSingleLine(myTestString));
	}

	/**
	 * Test method for {@link com.sebulli.fakturama.misc.DataUtils#replaceAllAccentedChars(java.lang.String)}.
	 */
	@Test
	public void testReplaceAllAccentedChars() {
		DataUtils dataUtils = DataUtils.getInstance();
		Assert.assertEquals("eee", dataUtils.replaceAllAccentedChars("eee"));
		Assert.assertEquals("eee", dataUtils.replaceAllAccentedChars("éèê"));
	}
}
