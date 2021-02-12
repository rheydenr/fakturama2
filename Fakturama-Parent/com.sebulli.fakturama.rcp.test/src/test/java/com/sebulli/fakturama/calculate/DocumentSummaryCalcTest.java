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
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sebulli.fakturama.Activator;
import com.sebulli.fakturama.dao.DocumentReceiverDAO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.dto.Price;
import com.sebulli.fakturama.dto.PriceBuilder;
import com.sebulli.fakturama.dto.VatSummarySet;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.model.VAT;

public class DocumentSummaryCalcTest {

    private static final boolean DEBUG_PRICES = false;

    private IEclipseContext ctx;
    
    @Mock
	private DocumentReceiverDAO documentReceiverDao;


    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ctx = EclipseContextFactory.getServiceContext(Activator.getContext());
        Mockito.when(documentReceiverDao.isSETEnabled(Mockito.any(Document.class))).thenReturn(Boolean.FALSE);
        ctx.set(DocumentReceiverDAO.class, documentReceiverDao);
        ContextInjectionFactory.setDefault(ctx);
    }
    
    /**
     * Test simple document with one item:<br>
     * <ul><li>use net price
     * <li>1 item with 10% VAT at 10 EUR (net)
     * <li>no shipping costs or discounts
     * </ul>
     * 
     */
    @Test
    public void testOneItemInADocument() {
    	DocumentItem documentItem = createDocumentItem(1, Double.valueOf(1.0), Double.valueOf(10.0), Double.valueOf(0.1));
    	Invoice invoice = FakturamaModelPackage.MODELFACTORY.createInvoice();
    	invoice.addToItems(documentItem);
    	invoice.setNetGross(DocumentSummary.ROUND_NOTSPECIFIED); // this is the default
    	
		VatSummarySet globalVatSummarySet = ContextInjectionFactory.make(VatSummarySet.class, ctx);

    	DocumentSummaryCalculator calc = ContextInjectionFactory.make(DocumentSummaryCalculator.class, ctx);
    	DocumentSummary summary = calc.calculate(invoice, globalVatSummarySet);
    	Assert.assertEquals(1.0, summary.getTotalQuantity(), 0.0);
        Assert.assertEquals(10.0, summary.getTotalNet().getNumber().doubleValue(), 0);
        Assert.assertEquals(11.0, summary.getTotalGross().getNumber().doubleValue(), 0);
        Assert.assertEquals(1.0, summary.getTotalVat().getNumber().doubleValue(), 0);
        
//        globalVatSummarySet.g
       
//        Assert.assertEquals(0.1, documentVatSummaryItems.getIndex(0));
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

//        printPrice(testPrice);
    }
    
    /**
     * <p>Test a simple {@link DocumentItem}.</p>
     * <p>
     * <table border=1>
     * <tr><th>quantity</th><th>VAT</th><th>price</th></tr>
     * <tr><td>1</td><td>10 %</td><td>10 EUR</td></tr>
     * </table>
     * </p>
     */
    @Test
    public void testSimpleDocumentItem() {
        DocumentItem documentItem = createDocumentItem(1, Double.valueOf(1.0), Double.valueOf(10.0), Double.valueOf(0.1));
        
        Price testPrice = new PriceBuilder().withDocumentItem(documentItem).build();
        Assert.assertEquals(10.0, testPrice.getTotalNet().getNumber().doubleValue(), 0);
        Assert.assertEquals(11.0, testPrice.getTotalGross().getNumber().doubleValue(), 0);
        Assert.assertEquals(1.0, testPrice.getTotalVat().getNumber().doubleValue(), 0);
    }

	private DocumentItem createDocumentItem(int id, Double quantity, Double price, Double taxValue) {
		DocumentItem documentItem = FakturamaModelPackage.MODELFACTORY.createDocumentItem();
        VAT itemVat = FakturamaModelPackage.MODELFACTORY.createVAT();
        itemVat.setId(id);
        itemVat.setTaxValue(taxValue);
        
        documentItem.setId(1);
        documentItem.setPrice(price);
        documentItem.setQuantity(quantity);
        documentItem.setItemVat(itemVat);
		return documentItem;
	}

    @SuppressWarnings("unused")
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
