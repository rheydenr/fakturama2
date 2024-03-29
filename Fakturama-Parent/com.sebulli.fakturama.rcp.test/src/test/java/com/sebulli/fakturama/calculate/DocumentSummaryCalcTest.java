package com.sebulli.fakturama.calculate;

import java.text.NumberFormat;
import java.util.Locale;

import javax.money.Monetary;
import javax.money.MonetaryAmount;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.preference.IPreferenceStore;
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
import com.sebulli.fakturama.dto.DocumentSummaryManager;
import com.sebulli.fakturama.dto.Price;
import com.sebulli.fakturama.dto.PriceBuilder;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.model.ShippingVatType;
import com.sebulli.fakturama.model.VAT;

public class DocumentSummaryCalcTest {

	private static final double DOUBLE_DELTA = 0.001;

	private static final boolean DEBUG_PRICES = false;

	private IEclipseContext ctx;

	@Mock
	private DocumentReceiverDAO documentReceiverDao;

	@Mock
	private IPreferenceStore defaultValuePrefs;
	
	private MonetaryAmount testAmount0EUR = Money.zero(Monetary.getCurrency(Locale.GERMANY));
	private MonetaryAmount testAmount1EUR = Money.of(MoneyUtils.getBigDecimal(1.0), Monetary.getCurrency(Locale.GERMANY));

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		ctx = EclipseContextFactory.getServiceContext(Activator.getContext());
		Mockito.when(documentReceiverDao.isSETEnabled(Mockito.any(Document.class))).thenReturn(Boolean.FALSE);
		ctx.set(DocumentReceiverDAO.class, documentReceiverDao);

		Mockito.when(defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_SALES_EQUALIZATION_TAX))
				.thenReturn(Boolean.FALSE);
		Mockito.when(defaultValuePrefs.getInt(Constants.PREFERENCES_GENERAL_CURRENCY_DECIMALPLACES))
		.thenReturn(Integer.valueOf(2));
		Mockito.when(defaultValuePrefs.getInt(Constants.PREFERENCES_DOCUMENT_USE_NET_GROSS))
		.thenReturn(Integer.valueOf(DocumentSummary.ROUND_NOTSPECIFIED));
		ctx.set(IPreferenceStore.class, defaultValuePrefs);
		ContextInjectionFactory.setDefault(ctx);
	}

	/**
	 * Test simple document with one item:<br>
	 * <ul>
	 * <li>use net price
	 * <li>1 item with 10% VAT at 10 EUR (net)
	 * <li>no shipping costs or discounts
	 * </ul>
	 * 
	 */
	@Test
	public void testOneItemInADocument() {
		DocumentItem documentItem = createDocumentItem(1, Double.valueOf(1.0), Double.valueOf(10.0),
				Double.valueOf(0.1));
		Invoice invoice = FakturamaModelPackage.MODELFACTORY.createInvoice();
		invoice.addToItems(documentItem);
		invoice.setNetGross(DocumentSummary.ROUND_NOTSPECIFIED); // this is the default

		DocumentSummaryManager calc = ContextInjectionFactory.make(DocumentSummaryManager.class, ctx);
		DocumentSummary summary = calc.calculate(invoice);
		Assert.assertEquals(1.0, summary.getTotalQuantity(), 0.0);
		Assert.assertEquals(10.0, summary.getTotalNet().getNumber().doubleValue(), 0);
		Assert.assertEquals(11.0, summary.getTotalGross().getNumber().doubleValue(), 0);
		Assert.assertEquals(1.0, summary.getTotalVat().getNumber().doubleValue(), 0);

		Assert.assertEquals(1, calc.getVatSummary(invoice).size());
		Assert.assertEquals(testAmount1EUR, calc.getVatSummaryItemForTaxValue(0.1).get(0).getVat());
	}
	
	/* TODO Tests for different shipping calculation methods, see ShippingVatType
	 */
	

	/**
	 * <ul>
	 * <li>use gross prices
	 * <li>25 items with 7% VAT at 2.20 EUR (gross)
	 * <li>5 items with 7% VAT at 2.50 EUR (gross)
	 * <li>1 item with 19% VAT at 10 EUR (gross)
	 * </ul>
	 * 
	 * see https://www.fakturama.info/community/postid/10704/
	 */
	@Test
	public void testFullSizeDocument_001() {
		int id = 1;
		Invoice invoice = FakturamaModelPackage.MODELFACTORY.createInvoice();
		invoice.addToItems(createDocumentItem(id++, Double.valueOf(25.0), Double.valueOf(2.20/1.07),
				Double.valueOf(0.07)));
		invoice.addToItems(createDocumentItem(id++, Double.valueOf(5.0), Double.valueOf(2.50/1.07),
				Double.valueOf(0.07)));
		invoice.addToItems(createDocumentItem(id++, Double.valueOf(1.0), Double.valueOf(10/1.19),
				Double.valueOf(0.19)));
		invoice.setNetGross(DocumentSummary.ROUND_GROSS_VALUES);

		DocumentSummaryManager calc = ContextInjectionFactory.make(DocumentSummaryManager.class, ctx);
		DocumentSummary summary = calc.calculate(invoice);
		Assert.assertEquals(31.0, summary.getTotalQuantity(), 0.0);
		Assert.assertEquals(71.48, summary.getTotalNet().getNumber().doubleValue(), 0);
		Assert.assertEquals(77.5, summary.getTotalGross().getNumber().doubleValue(), 0);
		Assert.assertEquals(6.02, summary.getTotalVatRounded().getNumber().doubleValue(), 0);
		
		Assert.assertEquals(71.48, calc.getVatSummary(invoice).getTotalNet().getNumber().doubleValue(), 0.01);

		Assert.assertEquals(2, calc.getVatSummary(invoice).size());
		Assert.assertEquals(Money.of(MoneyUtils.getBigDecimal(4.42), "EUR"),
				calc.getVatSummaryItemForTaxValue(0.07).get(0).getVatRounded());
		Assert.assertEquals(Money.of(MoneyUtils.getBigDecimal(1.60), "EUR"),
				calc.getVatSummaryItemForTaxValue(0.19).get(0).getVatRounded());
	}

	/**
	 * <ul>
	 * <li>use net prices
	 * <li>25 items with 7% VAT at 2.20 EUR (gross)
	 * <li>5 items with 7% VAT at 2.50 EUR (gross)
	 * <li>1 item with 19% VAT at 10 EUR (gross)
	 * <li>shipping costs: 5 EUR
	 * <li>global discount: 5%
	 * </ul>
	 */
	@Test
	public void testFullSizeDocument_002() {
		int id = 1;
		Invoice invoice = FakturamaModelPackage.MODELFACTORY.createInvoice();
		invoice.addToItems(createDocumentItem(id++, Double.valueOf(1.0), Double.valueOf(10.0),
				Double.valueOf(0.1)));
		DocumentItem documentItem = createDocumentItem(id++, Double.valueOf(1.0), Double.valueOf(10.0),
				Double.valueOf(0.1));
		documentItem.setItemRebate(-0.03);
		invoice.addToItems(documentItem);
		invoice.addToItems(createDocumentItem(id++, Double.valueOf(1.0), Double.valueOf(10.0),
				Double.valueOf(0.05)));
		invoice.setNetGross(DocumentSummary.ROUND_NOTSPECIFIED); // this is the default
		Shipping testShipping = createTestShipping();
		invoice.setShipping(testShipping);
		
		DocumentSummaryManager calc = ContextInjectionFactory.make(DocumentSummaryManager.class, ctx);
		DocumentSummary summary = calc.calculate(invoice);
		Assert.assertEquals(3.0, summary.getTotalQuantity(), 0.0);
		
		// net value including shipping net value
		Assert.assertEquals(34.66, summary.getTotalNet().getNumber().doubleValue(), 0);
		Assert.assertEquals(38.07, summary.getTotalGross().getNumber().doubleValue(), 0.002);
		Assert.assertEquals(3.41, summary.getTotalVat().getNumber().doubleValue(), 0.002);
		Assert.assertEquals(34.66, calc.getVatSummary(invoice).getTotalNet().getNumber().doubleValue(), 0.0);

		Assert.assertEquals(3, calc.getVatSummary(invoice).size());
		Assert.assertEquals(Money.of(MoneyUtils.getBigDecimal(1.97), "EUR"),
				calc.getVatSummaryItemForTaxValue(0.1).get(0).getVatRounded());
		Assert.assertEquals(Money.of(MoneyUtils.getBigDecimal(0.5), "EUR"),
				calc.getVatSummaryItemForTaxValue(0.05).get(0).getVatRounded());
		
		// (shipping VAT)
		Assert.assertEquals(Money.of(MoneyUtils.getBigDecimal(0.94), "EUR"),
				calc.getVatSummaryItemForTaxValue(0.19).get(0).getVatRounded());
	}
	
	// with SET
	@Test
	public void testFullSizeDocument_003() {
		Mockito.when(defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_SALES_EQUALIZATION_TAX))
				.thenReturn(Boolean.TRUE);
		ctx.set(IPreferenceStore.class, defaultValuePrefs);
		Mockito.when(documentReceiverDao.isSETEnabled(Mockito.any(Document.class))).thenReturn(Boolean.TRUE);
		ctx.set(DocumentReceiverDAO.class, documentReceiverDao);
		ContextInjectionFactory.setDefault(ctx);

		int id = 1;
		Invoice invoice = FakturamaModelPackage.MODELFACTORY.createInvoice();
		DocumentItem documentItem1 = createDocumentItem(id++, Double.valueOf(1.0), Double.valueOf(10.0), Double.valueOf(0.19));
		documentItem1.getItemVat().setSalesEqualizationTax(0.052);
		invoice.addToItems(documentItem1);
		
		DocumentItem documentItem2 = createDocumentItem(id++, Double.valueOf(1.0), Double.valueOf(10.0),
				Double.valueOf(0.07));
		documentItem2.setItemRebate(-0.03);
		invoice.addToItems(documentItem2);
		
		invoice.addToItems(createDocumentItem(id++, Double.valueOf(1.0), Double.valueOf(10.0), Double.valueOf(0)));
		invoice.setNetGross(DocumentSummary.ROUND_NOTSPECIFIED); // this is the default
		Shipping testShipping = createTestShipping();
		invoice.setShipping(testShipping);

		DocumentSummaryManager calc = ContextInjectionFactory.make(DocumentSummaryManager.class, ctx);
		DocumentSummary summary = calc.calculate(invoice);
		Assert.assertEquals(3.0, summary.getTotalQuantity(), 0.0);

		// net value
		Assert.assertEquals(34.66, summary.getTotalNet().getNumber().doubleValue(), 0);
		Assert.assertEquals(34.66, calc.getVatSummary(invoice).getTotalNet().getNumber().doubleValue(), 0.0);
		Assert.assertEquals(testAmount0EUR, summary.getDiscountNet());
		Assert.assertEquals(38.7, summary.getTotalGross().getNumber().doubleValue(), DOUBLE_DELTA);
		Assert.assertEquals(3.52, summary.getTotalVat().getNumber().doubleValue(), DOUBLE_DELTA);
		Assert.assertEquals(0.52, summary.getTotalSET().getNumber().doubleValue(), DOUBLE_DELTA);

		Assert.assertEquals(32.8, summary.getItemsGross().getNumber().doubleValue(), DOUBLE_DELTA);
//		Assert.assertEquals(32.28, summary.getItemsGrossDiscounted().getNumber().doubleValue(), DOUBLE_DELTA);
		
		Assert.assertEquals(29.7, summary.getItemsNet().getNumber().doubleValue(), DOUBLE_DELTA);
		Assert.assertEquals(29.7, summary.getItemsNetDiscounted().getNumber().doubleValue(), DOUBLE_DELTA);
		
		Assert.assertEquals(3, calc.getVatSummary(invoice).size());
		Assert.assertEquals(Money.of(MoneyUtils.getBigDecimal(2.84), "EUR"),
				calc.getVatSummaryItemForTaxValue(0.19).get(0).getVatRounded());
//		Assert.assertEquals(Money.of(MoneyUtils.getBigDecimal(14.957983193277311), "EUR"),
//				calc.getVatSummaryItemForTaxValue(0.19).get(0).getNet());
		
		Assert.assertEquals(Money.of(MoneyUtils.getBigDecimal(0.68), "EUR"),
				calc.getVatSummaryItemForTaxValue(0.07).get(0).getVatRounded());
		Assert.assertEquals(Money.of(MoneyUtils.getBigDecimal(0), "EUR"),
				calc.getVatSummaryItemForTaxValue(0.0).get(0).getVatRounded());
	}	
	
	@Test
	public void testMultipleItemsWithSamePrice() {
		int id = 1;
		Invoice invoice = FakturamaModelPackage.MODELFACTORY.createInvoice();

		for (int i = 1; i < 11; i++) {
			invoice.addToItems(
					createDocumentItem(id++, Double.valueOf(1.0), Double.valueOf(35.0 / 1.07), Double.valueOf(0.07)));
		}
		invoice.setNetGross(DocumentSummary.ROUND_NET_VALUES);

		DocumentSummaryManager calc = ContextInjectionFactory.make(DocumentSummaryManager.class, ctx);
		DocumentSummary summary = calc.calculate(invoice);
		Assert.assertEquals(10.0, summary.getTotalQuantity(), 0.0);
		Assert.assertEquals(327.1, summary.getTotalNet().getNumber().doubleValue(), 0);
		Assert.assertEquals(350.0, summary.getTotalGross().getNumber().doubleValue(), 0);
		Assert.assertEquals(22.9, summary.getTotalVatRounded().getNumber().doubleValue(), 0);

		Assert.assertEquals(327.1, calc.getVatSummary(invoice).getTotalNet().getNumber().doubleValue(), 0.01);

		Assert.assertEquals(1, calc.getVatSummary(invoice).size());
		Assert.assertEquals(Money.of(MoneyUtils.getBigDecimal(22.9), "EUR"),
				calc.getVatSummaryItemForTaxValue(0.07).get(0).getVatRounded());
	}

	@Test
	public void testFullSizeDocumentWithAutoVATShipping() {
		int id = 1;
		Invoice invoice = FakturamaModelPackage.MODELFACTORY.createInvoice();
		invoice.addToItems(createDocumentItem(id++, Double.valueOf(1.0), Double.valueOf(10.0),
				Double.valueOf(0.1)));
		DocumentItem documentItem = createDocumentItem(id++, Double.valueOf(1.0), Double.valueOf(10.0),
				Double.valueOf(0.1));
		documentItem.setItemRebate(-0.03);
		invoice.addToItems(documentItem);
		invoice.addToItems(createDocumentItem(id++, Double.valueOf(1.0), Double.valueOf(10.0),
				Double.valueOf(0.05)));
		invoice.setNetGross(DocumentSummary.ROUND_NET_VALUES);
		Shipping testShipping = createTestShipping();
		testShipping.setShippingValue(5.9);
		
		// change shipping Auto VAT
		testShipping.setAutoVat(ShippingVatType.SHIPPINGVATGROSS);
		invoice.setShipping(testShipping);
		
		DocumentSummaryManager calc = ContextInjectionFactory.make(DocumentSummaryManager.class, ctx);
		DocumentSummary summary = calc.calculate(invoice);
		Assert.assertEquals(3.0, summary.getTotalQuantity(), 0.0);
		
		// net value including shipping net value
		double totalNet = summary.getTotalNet().getNumber().doubleValue();
		Assert.assertEquals(35.15, totalNet, 0);
		Assert.assertEquals(38.07, summary.getTotalGross().getNumber().doubleValue(), DOUBLE_DELTA);
		double totalVat = summary.getTotalVat().getNumber().doubleValue();
		Assert.assertEquals(2.92, totalVat, DOUBLE_DELTA);
		
		// has to be same as summary.getTotalNet()
		Assert.assertEquals(totalNet, calc.getVatSummary(invoice).getTotalNet().getNumber().doubleValue(), 0.01);

		Assert.assertEquals(Money.of(MoneyUtils.getBigDecimal(2.33), "EUR"),
				calc.getVatSummaryItemForTaxValue(0.1).get(0).getVatRounded());
		Assert.assertEquals(Money.of(MoneyUtils.getBigDecimal(0.59), "EUR"),
				calc.getVatSummaryItemForTaxValue(0.05).get(0).getVatRounded());
		Assert.assertEquals(2, calc.getVatSummary(invoice).size());
		// sum of items has to be equal to summary.getTotalVat()
		Assert.assertEquals(totalVat, calc.getDocumentSummary(invoice).getTotalVatRounded().getNumber().doubleValue(),
				0);
	}
	

	/**
	 * Create a Shipping object with 5.90EUR and 19% VAT.
	 * 
	 * @return Shipping object
	 */
	private Shipping createTestShipping() {
		Shipping testShipping = FakturamaModelPackage.MODELFACTORY.createShipping();
		testShipping.setAutoVat(ShippingVatType.SHIPPINGVATFIX);
		testShipping.setName("A test shipping");
		testShipping.setDescription("A test shipping description");
		// 4.96 EUR net = 5.90 EUR gross
		testShipping.setShippingValue(Double.valueOf(5.9/1.19));
		testShipping.setShippingVat(createVat("Test VAT " + NumberFormat.getPercentInstance().format(0.19), 0.19));
		return testShipping;
	}

	private VAT createVat(String vatName, double taxValue) {
		VAT vat = FakturamaModelPackage.MODELFACTORY.createVAT();
		vat.setName(vatName);
		vat.setTaxValue(taxValue);
		return vat;
	}

	/**
	 * <p>
	 * Test a simple {@link DocumentItem}.
	 * </p>
	 * <p>
	 * <table border=1>
	 * <tr>
	 * <th>quantity</th>
	 * <th>VAT</th>
	 * <th>price</th>
	 * </tr>
	 * <tr>
	 * <td>1</td>
	 * <td>10 %</td>
	 * <td>10 EUR</td>
	 * </tr>
	 * </table>
	 * </p>
	 */
	@Test
	public void testSimpleDocumentItem() {
		DocumentItem documentItem = createDocumentItem(1, Double.valueOf(1.0), Double.valueOf(10.0),
				Double.valueOf(0.1));

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
		itemVat.setName("Test VAT " + NumberFormat.getPercentInstance().format(taxValue));

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
			System.out.println(StringUtils.rightPad("UnitNet:", longestLabel) + testPrice.getUnitNet());
			System.out.println(
					StringUtils.rightPad("UnitNetDiscounted:", longestLabel) + testPrice.getUnitNetDiscounted());
			System.out.println(StringUtils.rightPad("UnitNetDiscountedRounded:", longestLabel)
					+ testPrice.getUnitNetDiscountedRounded());

			System.out.println(StringUtils.rightPad("UnitGross:", longestLabel) + testPrice.getUnitGross());
			System.out
					.println(StringUtils.rightPad("UnitGrossRounded:", longestLabel) + testPrice.getUnitGrossRounded());
			System.out.println(
					StringUtils.rightPad("UnitGrossDiscounted:", longestLabel) + testPrice.getUnitGrossDiscounted());
			System.out.println(StringUtils.rightPad("UnitGrossDiscountedRounded:", longestLabel)
					+ testPrice.getUnitGrossDiscountedRounded());

			System.out.println(StringUtils.rightPad("TotalAllowance:", longestLabel) + testPrice.getTotalAllowance());

			System.out.println(StringUtils.rightPad("TotalNet:", longestLabel) + testPrice.getTotalNet());
			System.out.println(StringUtils.rightPad("TotalNetRounded:", longestLabel) + testPrice.getTotalNetRounded());

			System.out.println(StringUtils.rightPad("VatPercent:", longestLabel) + testPrice.getVatPercentFormatted());
			System.out.println(StringUtils.rightPad("TotalVat:", longestLabel) + testPrice.getTotalVat());
			System.out.println(StringUtils.rightPad("TotalVatRounded:", longestLabel) + testPrice.getTotalVatRounded());

			System.out.println(
					StringUtils.rightPad("SalesEqTaxPercent:", longestLabel) + testPrice.getSalesEqTaxPercent());
			System.out.println(StringUtils.rightPad("TotalSalesEqTax:", longestLabel) + testPrice.getTotalSalesEqTax());
			System.out.println(StringUtils.rightPad("TotalSalesEqTaxRounded:", longestLabel)
					+ testPrice.getTotalSalesEqTaxRounded());

			System.out.println(StringUtils.rightPad("TotalGross:", longestLabel) + testPrice.getTotalGross());
			System.out.println(
					StringUtils.rightPad("TotalGrossRounded:", longestLabel) + testPrice.getTotalGrossRounded());
		}
	}
}
