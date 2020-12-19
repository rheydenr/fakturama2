package com.sebulli.fakturama.common.i18n;


import java.util.Locale;
import java.util.Optional;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.ibm.icu.util.ULocale;
import com.sebulli.fakturama.common.Activator;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.misc.Constants;

//@RunWith(PowerMockRunner.class)
@RunWith(MockitoJUnitRunner.class)
//@PrepareForTest(Activator.class)
public class LocaleUtilTest {
	
	private ILocaleService localeService;
	
	@Mock
	private IPreferenceStore mockPrefs;
	
	@Before
	public void setUp() {
//		MockitoAnnotations.initMocks(this);
		IEclipseContext mockContext = EclipseContextFactory.create("mockContext");
		localeService = ContextInjectionFactory.make(LocaleUtil.class, mockContext);
	}

	@Test
	public void testFindLocaleByDisplayCountry() {
		Optional<ULocale> localeByDisplayCountry = localeService.findLocaleByDisplayCountry("Italien");
		Assert.assertTrue(localeByDisplayCountry.isPresent());
		Assert.assertEquals(Locale.ITALY, localeByDisplayCountry.get());
	}
	
	@Test
	public void testFindCodeByDisplayCountry() {
		String localeByDisplayCountry = localeService.findCodeByDisplayCountry("Italien", "de");
		Assert.assertEquals("IT", localeByDisplayCountry);
	}
	
	@Test
	public void testGetCurrencyLocale() {
		ULocale currencyLocale = localeService.getCurrencyLocale();
		Assert.assertEquals(Locale.GERMANY, currencyLocale);
	}

	@Test
	@Ignore("can't be executed because of OSGi class loading quirks")
	public void testFindByCode() {
		Mockito.when(Activator.getPreferenceStore()).thenReturn(mockPrefs);
		Mockito.when(mockPrefs.getString(Constants.PREFERENCE_CURRENCY_LOCALE)).thenReturn("de_DE");
		Optional<ULocale> localeByCode = localeService.findByCode("de");
		Assert.assertTrue(localeByCode.isPresent());
		Assert.assertEquals(Locale.GERMANY, localeByCode.get());
	}
}
