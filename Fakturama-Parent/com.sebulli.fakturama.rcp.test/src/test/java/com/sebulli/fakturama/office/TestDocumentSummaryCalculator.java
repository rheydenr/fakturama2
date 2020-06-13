/*
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2020 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.office;

import java.util.ArrayList;
import java.util.List;

import javax.money.CurrencyUnit;
import javax.money.Monetary;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.sebulli.fakturama.calculate.DocumentSummaryCalculator;
import com.sebulli.fakturama.dao.DocumentReceiverDAO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.model.ItemType;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.model.ShippingVatType;
import com.sebulli.fakturama.model.VAT;

/**
 * Test class for {@link DocumentSummaryCalculator}
 */
@RunWith(MockitoJUnitRunner.class)
public class TestDocumentSummaryCalculator {

    @Mock
    protected DocumentReceiverDAO documentsReceiverDao;

    @Test
    public void testSimpleDocumentWithOneItem() {

        List<DocumentItem> mockItems = createMockItemList(1);
        Invoice dummyInvoice = FakturamaModelPackage.MODELFACTORY.createInvoice();
        dummyInvoice.setShippingValue(Double.valueOf(5.0));
        dummyInvoice.setShippingAutoVat(ShippingVatType.SHIPPINGVATGROSS); // shipping value is gross
        dummyInvoice.setItems(mockItems);
        
        Mockito.when(documentsReceiverDao.isSETEnabled(dummyInvoice)).thenReturn(Boolean.FALSE);

        IEclipseContext context = EclipseContextFactory.create();
        context.set(DocumentReceiverDAO.class, documentsReceiverDao);
        CurrencyUnit currency = Monetary.getCurrency("EUR");
        context.set(DocumentSummaryCalculator.CURRENCY_CODE, currency);

        DocumentSummaryCalculator calculator = ContextInjectionFactory.make(DocumentSummaryCalculator.class, context);

        DocumentSummary summary = calculator.calculate(dummyInvoice);
        System.out.println(summary);
        Assert.assertEquals("document items net sum is wrong.", Double.valueOf(10), summary.getItemsNet().getNumber().doubleValue(), Double.valueOf(0));
        Assert.assertEquals("document items gross sum is wrong.", Double.valueOf(11), summary.getItemsGross().getNumber().doubleValue(), Double.valueOf(0));
        Assert.assertEquals("document's shipping gross is wrong.", Double.valueOf(5), summary.getShippingGross().getNumber().doubleValue(), Double.valueOf(0));
        Assert.assertEquals("document's shipping net is wrong.", Double.valueOf(4.55), summary.getShippingNet().getNumber().doubleValue(), Double.valueOf(0));
    }
    
    private Shipping createMockShipping() {
        Shipping shipping = FakturamaModelPackage.MODELFACTORY.createShipping();
        shipping.setAutoVat(ShippingVatType.SHIPPINGVATGROSS);
        shipping.setDescription("Dummy Shipping");
        shipping.setShippingValue(Double.valueOf(5.0));
        shipping.setShippingVat(createMockVAT());
        return shipping;
    }

    private List<DocumentItem> createMockItemList(int countOfItems) {
        VAT dummyVat = createMockVAT();
        List<DocumentItem> retList = new ArrayList<>();
        for (int i = 0; i < countOfItems; i++) {
            DocumentItem dummyItem = FakturamaModelPackage.MODELFACTORY.createDocumentItem();
            dummyItem.setPosNr(i + 1);
            dummyItem.setItemNumber(String.format("ITEM%03d", i));
            dummyItem.setItemType(ItemType.POSITION);
            dummyItem.setPrice(Double.valueOf(10));
            dummyItem.setItemVat(dummyVat);
            
            retList.add(dummyItem);
        }
        return retList;
    }

    private VAT createMockVAT() {
        VAT dummyVat = FakturamaModelPackage.MODELFACTORY.createVAT();
        dummyVat.setDescription("Dummy VAT");
        dummyVat.setTaxValue(Double.valueOf(0.1));
        return dummyVat;
    }
}
