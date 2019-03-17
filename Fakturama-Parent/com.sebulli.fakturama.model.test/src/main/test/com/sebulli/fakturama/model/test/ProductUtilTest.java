/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2019 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.model.test;

import static org.junit.Assert.assertEquals;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.util.ProductUtil;

/**
 *
 */
public class ProductUtilTest {
    
	Double quantityOfOne = Double.valueOf(1.0);
    
    @Mock
    protected IEclipsePreferences preferences;
    protected IEclipseContext context;
    
	private ProductUtil productUtil;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
        this.productUtil = ContextInjectionFactory.make(ProductUtil.class, context);
	}

	@Test
	public void testGetPriceByQuantity_withoutScales_success() {
		Mockito.when(preferences.getInt(Mockito.eq(Constants.PREFERENCES_PRODUCT_SCALED_PRICES), Mockito.anyInt())).thenReturn(Integer.valueOf(1));
        productUtil.setEclipsePrefs(preferences);
        Product product = new Product();
		product.setPrice1(Double.valueOf(1.0));
        Double result = productUtil.getPriceByQuantity(product, quantityOfOne);
        assertEquals(quantityOfOne, result);
	}
	
	@Test
	public void testGetPriceByQuantity_withoutScales_scaledPrice() {
		Mockito.when(preferences.getInt(Mockito.eq(Constants.PREFERENCES_PRODUCT_SCALED_PRICES), Mockito.anyInt())).thenReturn(Integer.valueOf(1));
        productUtil.setEclipsePrefs(preferences);
		Product product = new Product();
		product.setPrice1(Double.valueOf(10.0));
		product.setPrice2(Double.valueOf(8.0));
		product.setBlock1(Integer.valueOf(1));
		product.setBlock2(Integer.valueOf(10));
		Double result = productUtil.getPriceByQuantity(product, quantityOfOne);
		assertEquals(Double.valueOf(10.0), result);
	}
	
	@Test
	public void testGetPriceByQuantity_withScales_scaledPrice() {
		Mockito.when(preferences.getInt(Mockito.eq(Constants.PREFERENCES_PRODUCT_SCALED_PRICES), Mockito.anyInt())).thenReturn(Integer.valueOf(2));
        productUtil.setEclipsePrefs(preferences);
		Product product = new Product();
		product.setPrice1(Double.valueOf(10.0));
		product.setPrice2(Double.valueOf(8.0));
		product.setBlock1(Integer.valueOf(1));
		product.setBlock2(Integer.valueOf(10));
		Double result = productUtil.getPriceByQuantity(product, Double.valueOf(10.0));
		assertEquals(Double.valueOf(8.0), result);
	}
}
