package com.sebulli.fakturama.calculate;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.spi.MoneyUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.sebulli.fakturama.Activator;
import com.sebulli.fakturama.dto.Price;
import com.sebulli.fakturama.dto.PriceBuilder;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.VAT;

public class DocumentSummaryCalcTest {

    private static final boolean DEBUG_PRICES = false;

    private IEclipseContext ctx;

    @Before
    public void setUp() throws Exception {
//        MockitoAnnotations.initMocks(this);
        ctx = EclipseContextFactory.getServiceContext(Activator.getContext());
        ContextInjectionFactory.setDefault(ctx);
    }

    @Test
    public void testSimplePrice() {
        Price testPrice = new PriceBuilder()
                .withUnitPrice(Money.of(MoneyUtils.getBigDecimal(10), "EUR"))
                .withGrossPrices(false).withQuantity(1.0)
                .withVatPercent(0.1).build();

        Assert.assertEquals(10.0, testPrice.getTotalNet().getNumber().doubleValue(), 0);
        Assert.assertEquals(11.0, testPrice.getTotalGross().getNumber().doubleValue(), 0);
        Assert.assertEquals(1.0, testPrice.getTotalVat().getNumber().doubleValue(), 0);

        printPrice(testPrice);
    }
    
    @Test
    public void testSimpleDocumentItem() {
        DocumentItem documentItem = FakturamaModelPackage.MODELFACTORY.createDocumentItem();
        VAT itemVat = FakturamaModelPackage.MODELFACTORY.createVAT();
        itemVat.setId(1);
        itemVat.setTaxValue(Double.valueOf(0.1));
        
        documentItem.setId(1);
        documentItem.setPrice(Double.valueOf(10.0));
        documentItem.setQuantity(Double.valueOf(1.0));
        documentItem.setItemVat(itemVat);
        
        Price testPrice = new PriceBuilder().withDocumentItem(documentItem).build();
        Assert.assertEquals(10.0, testPrice.getTotalNet().getNumber().doubleValue(), 0);
        Assert.assertEquals(11.0, testPrice.getTotalGross().getNumber().doubleValue(), 0);
        Assert.assertEquals(1.0, testPrice.getTotalVat().getNumber().doubleValue(), 0);
    }

    private void printPrice(Price testPrice) {
        if (DEBUG_PRICES) {
            int longestLabel = 30;
            System.out.println("Calculated price:");
            System.out.println(StringUtils.repeat("=", longestLabel + 6));
            System.out.println(StringUtils.rightPad("UnitNet:", longestLabel)+ testPrice.getUnitNet());
            System.out.println(StringUtils.rightPad("UnitNetDiscounted:", longestLabel) + testPrice.getUnitNetDiscounted());
            System.out.println(StringUtils.rightPad("UnitNetDiscountedRounded:", longestLabel)+ testPrice.getUnitNetDiscountedRounded());

            System.out.println(StringUtils.rightPad("UnitGross:", longestLabel) + testPrice.getUnitGross());
            System.out.println(StringUtils.rightPad("UnitGrossRounded:", longestLabel) + testPrice.getUnitGrossRounded());
            System.out.println(StringUtils.rightPad("UnitGrossDiscounted:", longestLabel) + testPrice.getUnitGrossDiscounted());
            System.out.println(StringUtils.rightPad("UnitGrossDiscountedRounded:", longestLabel) + testPrice.getUnitGrossDiscountedRounded());

            System.out.println(StringUtils.rightPad("TotalAllowance:", longestLabel) + testPrice.getTotalAllowance());

            System.out.println(StringUtils.rightPad("TotalNet:", longestLabel) + testPrice.getTotalNet());
            System.out.println(StringUtils.rightPad("TotalNetRounded:", longestLabel) + testPrice.getTotalNetRounded());

            System.out.println(StringUtils.rightPad("VatPercent:", longestLabel) + testPrice.getVatPercent());
            System.out.println(StringUtils.rightPad("TotalVat:", longestLabel) + testPrice.getTotalVat());
            System.out.println(StringUtils.rightPad("TotalVatRounded:", longestLabel) + testPrice.getTotalVatRounded());

            System.out.println(StringUtils.rightPad("SalesEqTaxPercent:", longestLabel) + testPrice.getSalesEqTaxPercent());
            System.out.println(StringUtils.rightPad("TotalSalesEqTax:", longestLabel) + testPrice.getTotalSalesEqTax());
            System.out.println(StringUtils.rightPad("TotalSalesEqTaxRounded:", longestLabel) + testPrice.getTotalSalesEqTaxRounded());

            System.out.println(StringUtils.rightPad("TotalGross:", longestLabel) + testPrice.getTotalGross());
            System.out.println(StringUtils.rightPad("TotalGrossRounded:", longestLabel) + testPrice.getTotalGrossRounded());
        }
    }

}
