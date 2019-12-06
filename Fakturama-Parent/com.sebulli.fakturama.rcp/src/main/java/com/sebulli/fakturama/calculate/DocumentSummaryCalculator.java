/**
 * 
 */
package com.sebulli.fakturama.calculate;

import java.util.List;
import java.util.Optional;

import javax.inject.Inject;
import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.MonetaryRounding;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.javamoney.moneta.Money;

import com.sebulli.fakturama.dao.DocumentReceiverDAO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.dto.Price;
import com.sebulli.fakturama.dto.VatSummaryItem;
import com.sebulli.fakturama.dto.VatSummarySet;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.model.ShippingVatType;
import com.sebulli.fakturama.model.VAT;

/**
 * Calculator for the document summaries.
 *
 */
public class DocumentSummaryCalculator {
	@Inject
	private DocumentReceiverDAO documentReceiverDao;

    /**
     * Checks if the current editor uses sales equalization tax (this is only needed for some customers).
     */
    private boolean useSET = false;
	private CurrencyUnit currencyCode;
	
	public DocumentSummaryCalculator() {
	    this(false, DataUtils.getInstance().getDefaultCurrencyUnit());
	}
	
	public DocumentSummaryCalculator(boolean useSET) {
		this.useSET = useSET;
		this.currencyCode = DataUtils.getInstance().getDefaultCurrencyUnit();
	}
	
	public DocumentSummaryCalculator(CurrencyUnit currencyCode) {
		this(false, currencyCode);
	}
	
	public DocumentSummaryCalculator(boolean useSET, CurrencyUnit currencyCode) {
		this.useSET = useSET;
		this.currencyCode = currencyCode;
	}
	
    public DocumentSummary calculate(Document dataSetDocument) {
    	this.useSET = documentReceiverDao.isSETEnabled(dataSetDocument);
        Double scaleFactor = NumberUtils.DOUBLE_ONE;
        int netGross = dataSetDocument.getNetGross() != null ? dataSetDocument.getNetGross() : 0;
        VAT noVatReference = dataSetDocument.getNoVatReference();
        // only calculate a deposit if the document is really deposited (else, if e.g. we have a fully paid invoice and the deposit 
        // amount is not equal to zero we get unpredictable results in later processing)
        MonetaryAmount deposit = Money.of(BooleanUtils.isTrue(dataSetDocument.getDeposit()) ? dataSetDocument.getPaidValue() : NumberUtils.DOUBLE_ZERO, currencyCode);
        Shipping shipping = dataSetDocument.getShipping();
		return calculate(null, dataSetDocument.getItems(), 
        		shipping != null ? shipping.getShippingValue() : Optional.ofNullable(dataSetDocument.getShippingValue()).orElse(NumberUtils.DOUBLE_ZERO), 
        		shipping != null ? shipping.getShippingVat() : null, 
                shipping != null ? shipping.getAutoVat() : dataSetDocument.getShippingAutoVat(), 
                Optional.ofNullable(dataSetDocument.getItemsRebate()).orElse(NumberUtils.DOUBLE_ZERO), noVatReference, 
                scaleFactor, netGross, deposit);
    }

	public DocumentSummary calculate(List<DocumentItem> items, Shipping shipping, ShippingVatType shippingAutoVat, Double itemsDiscount, VAT noVatReference, 
            int netGross, MonetaryAmount deposit) {
        return calculate(null, items, shipping.getShippingValue(), shipping.getShippingVat(), shippingAutoVat, itemsDiscount, noVatReference, NumberUtils.DOUBLE_ONE, netGross, deposit);
    }
	/**
	 * Calculates the tax, gross and sum of a document
	 * 
	 * @param globalVatSummarySet
	 *            The documents vat is added to this global VAT summary set.
	 * @param items
	 *            Document's items
	 * @param shippingValue
	 *            Document's shipping
	 * @param shippingVatPercent
	 *            Shipping's VAT - This is only used, if the shipping's VAT is
	 *            not calculated based on the items.
	 * @param shippingVatDescription
	 *            Shipping's VAT name
	 * @param shippingAutoVat
	 *            If TRUE, the shipping VAT is based on the item's VAT
	 * @param itemsDiscount
	 *            Discount value
	 * @param VAT noVatReference
	 *            VAT which has a taxValue of 0. If set, it's assumed that noVat is given and all VAT values are set to 0.
	 *            (replacement for noVat flag)
	 * @param scaleFactor
	 * 
	 * @param deposit
	 * @param shippingValue 
	 */
    public DocumentSummary calculate(VatSummarySet globalVatSummarySet, List<DocumentItem> items, Double shippingValue, VAT shippingVat,
            ShippingVatType shippingAutoVat, Double itemsDiscount, VAT noVatReference, Double scaleFactor, 
            int netGross, MonetaryAmount deposit) {
		Double vatPercent;
		String vatDescription;
        CurrencyUnit currencyUnit = DataUtils.getInstance().getDefaultCurrencyUnit();
        DocumentSummary retval = new DocumentSummary(currencyUnit);
        MonetaryRounding rounding = DataUtils.getInstance().getRounding(currencyUnit);  

		// This VAT summary contains only the VAT entries of this document,
		// whereas the the parameter vatSummaryItems is a global VAT summary
		// and contains entries from this document and from others.
		VatSummarySet documentVatSummaryItems = new VatSummarySet();

		// Set the values to 0.0
//		resetValues();

		// Use all non-deleted items
		for (DocumentItem item : items) {

			// Get the data from each item
			VAT itemVat = getItemVat(item);
			vatDescription = itemVat.getDescription();
			vatPercent = itemVat.getTaxValue();
			Price price = new Price(item, scaleFactor, useSET);  // scaleFactor was always 1.0 in the old application, hence we could omit this
			MonetaryAmount itemVatAmount = price.getTotalVat();

			// Add the total net value of this item to the sum of net items
			retval.setItemsNet(retval.getItemsNet().add(price.getTotalNet()));

			// Sums the total amount of item quantities. 
			retval.setTotalQuantity(retval.getTotalQuantity() + item.getQuantity()); 

			// If noVat is set, the VAT is 0%
			if (noVatReference != null) {
				vatDescription = noVatReference.getDescription();
				vatPercent = noVatReference.getTaxValue();
				itemVatAmount = Money.zero(currencyCode);
			}

			// Add the VAT to the sum of VATs
			retval.setTotalVat(retval.getTotalVat().add(itemVatAmount));

			// Add the VAT summary item to the ... 
			VatSummaryItem vatSummaryItem = new VatSummaryItem(vatDescription, vatPercent, price.getTotalNet(), itemVatAmount);
			if(itemVat != null && this.useSET && itemVat.getSalesEqualizationTax() != null) {
				vatSummaryItem.setSalesEqTaxPercent(itemVat.getSalesEqualizationTax());
				retval.setTotalSET(retval.getTotalSET().add(vatSummaryItem.getSalesEqTax()));
			}

			// .. VAT summary of the document ..
			documentVatSummaryItems.add(vatSummaryItem);

		}

		// *** round sum of items
		
		// round to full net cents
//		if (netGross == DocumentSummary.ROUND_NET_VALUES) {
//		    retval.setItemsNet(retval.getItemsNet()/*.with(rounding)*/);
//		} 
		
		// Gross value is the sum of net and VAT and sales equalization tax value 
		retval.setTotalNet(retval.getItemsNet());
		retval.setItemsGross(retval.getItemsNet().add(retval.getTotalVat()).add(retval.getTotalSET()));
		
		// round to full gross cents
		if (netGross == DocumentSummary.ROUND_GROSS_VALUES) {
//			retval.getItemsGross().round();
		    retval.setItemsNet(retval.getItemsGross().subtract(retval.getTotalVat()).subtract(retval.getTotalSET()));
		    retval.setTotalNet(retval.getItemsNet());
		}
		retval.setTotalGross(retval.getItemsGross());
		
		MonetaryAmount itemsNet = retval.getItemsNet();
		MonetaryAmount itemsGross = retval.getItemsGross();

		// *** DISCOUNT ***
		
		// Calculate the absolute discount values
		// Discount value = discount percent * Net value
		MonetaryAmount discountNet = itemsNet.multiply(itemsDiscount);
		retval.setDiscountNet(discountNet);
		retval.setDiscountGross(itemsGross.multiply(itemsDiscount));

		final MonetaryAmount zero = Money.zero(currencyCode);

		// Calculate discount
		if (!DataUtils.getInstance().DoublesAreEqual(itemsDiscount, NumberUtils.DOUBLE_ZERO)) {

			// Calculate the vat value in percent from the gross value of all items
			// and the net value of all items. So the discount's vat is the average 
			// value of the item's vat
			Double discountVatPercent;
			if (!itemsNet.isZero()) {
				discountVatPercent = itemsGross.divide(itemsNet.getNumber()).getNumber().doubleValue() - NumberUtils.DOUBLE_ONE;
			} else {
				// do not divide by zero
				discountVatPercent = NumberUtils.DOUBLE_ZERO;
			}
			
			// If noVat is set, the VAT is 0%
			if (noVatReference != null) {
				discountVatPercent = NumberUtils.DOUBLE_ZERO;
			}

			// Reduce all the VAT entries in the VAT Summary Set by the discount 
			MonetaryAmount discountVatValue = Money.from(zero);
			String discountVatDescription = "";
			// Get the data from each entry
			for (VatSummaryItem vatSummaryItem : documentVatSummaryItems) {

				// If noVat is set, the VAT is 0%
				if (noVatReference != null) {
					discountVatDescription = noVatReference.getDescription();
					discountVatPercent = NumberUtils.DOUBLE_ZERO;
				} else {
    			    discountVatDescription = vatSummaryItem.getVatName();
    				discountVatPercent = vatSummaryItem.getVatPercent();
				}

				// Calculate the ratio of this vat summary item and all items.
				// The discountNetPart is proportional to this ratio.
				MonetaryAmount discountNetPart = Money.from(zero);
				if (!itemsNet.isZero())
					discountNetPart = discountNet.multiply(vatSummaryItem.getNet().divide(itemsNet.getNumber()).getNumber());

				// Add discountNetPart to the sum "discountVatValue"  
				Price discountPart = new Price(discountNetPart, discountVatPercent, Optional.ofNullable(vatSummaryItem.getSalesEqTaxPercent()).orElse(NumberUtils.DOUBLE_ZERO));
				discountVatValue = discountVatValue.add(discountPart.getUnitVat()).add(discountPart.getTotalSalesEqTax());

				VatSummaryItem discountVatSummaryItem = new VatSummaryItem(discountVatDescription, discountVatPercent, discountPart.getUnitNet(),
						discountPart.getUnitVat());
				if(this.useSET && vatSummaryItem.getSalesEqTax() != null) {
					discountVatSummaryItem.setSalesEqTax(vatSummaryItem.getSalesEqTax());
				}

				// Adjust the vat summary item by the discount part
				documentVatSummaryItems.add(discountVatSummaryItem);
			}

			// adjust the documents sum by the discount
			retval.setTotalVat(retval.getTotalVat().add(discountVatValue));
			retval.setTotalNet(retval.getTotalNet().add(discountNet));
			
			// round to full net cents
//			if (netGross == DocumentSummary.ROUND_NET_VALUES) {
//			    retval.setDiscountNet(retval.getDiscountNet().round());
//			    retval.setTotalNet(retval.getTotalNet().round());
//			} 
			
			if (netGross != DocumentSummary.ROUND_GROSS_VALUES) {
				retval.setTotalGross(retval.getTotalNet().add(retval.getTotalVat().add(retval.getTotalSET())));
			} else {
			
			// round to full gross cents
//				this.discountGross.round();
			    retval.setTotalGross(retval.getTotalGross().add(retval.getDiscountGross()));
//				this.totalGross.round();
			    retval.setDiscountNet(retval.getDiscountGross().subtract(discountVatValue));
			    retval.setTotalNet(retval.getTotalGross().subtract(retval.getTotalVat()).subtract(retval.getTotalSET()));
			}
		}

		// calculate shipping

		// Scale the shipping
		MonetaryAmount shippingAmount = Money.of(shippingValue * scaleFactor, currencyCode);
		Double shippingVatPercent = shippingVat != null ? shippingVat.getTaxValue() : NumberUtils.DOUBLE_ZERO;
		String shippingVatDescription = shippingVat != null ? shippingVat.getDescription() : ""; // TODO or get it from additional document info???

		// If shippingAutoVat is not fix, the shipping vat is 
		// an average value of the vats of the items.
		if (shippingAutoVat != null && !shippingAutoVat.isSHIPPINGVATFIX()) {

			// If the shipping is set as gross value, calculate the net value.
			// Use the average vat of all the items.
			if (shippingAutoVat.isSHIPPINGVATGROSS()) {
				if (!itemsGross.isZero()) {
					// shippingValue * itemsNet / itemsGross
					retval.setShippingNet(shippingAmount.multiply(itemsNet.divide(itemsGross.getNumber()).getNumber()));
				} else {
					retval.setShippingNet(shippingAmount);
				}
			}

			// If the shipping is set as net value, use the net value.
			if (shippingAutoVat.isSHIPPINGVATNET()) {
				retval.setShippingNet(shippingAmount);
			}

			// Use the average vat of all the items.
			if (itemsNet.isEqualTo(zero)) {
				shippingVatPercent = NumberUtils.DOUBLE_ZERO;
			} else {
				shippingVatPercent = itemsGross.divide(itemsNet.getNumber()).getNumber().doubleValue() - 1;
			}
			
			// Increase the vat summary entries by the shipping ratio

			// Calculate the sum of all VatSummary entries
			MonetaryAmount netSumOfAllVatSummaryItems = Money.from(zero);

			for (VatSummaryItem vatSummaryItem : documentVatSummaryItems) {
				
				// TODO How do I use Lambdas???
	// 			documentVatSummaryItems.forEach(vatItem -> {
	// 			    netSumOfAllVatSummaryItems = netSumOfAllVatSummaryItems.add(vatItem.getNet());
	// 			});
			    netSumOfAllVatSummaryItems = netSumOfAllVatSummaryItems.add(vatSummaryItem.getNet());
			}

			// at this point we have all relevant VATs (discounted items)
			// and store them into a separate container.
			for (VatSummaryItem vatSummaryItem : documentVatSummaryItems) {
				// Get the data from each entry
				shippingVatDescription = vatSummaryItem.getVatName();
				shippingVatPercent = vatSummaryItem.getVatPercent();

				// If noVat is set, the VAT is 0%
				if (noVatReference != null) {
					shippingVatDescription = noVatReference.getDescription();
					shippingVatPercent = NumberUtils.DOUBLE_ZERO;
				}

				// Calculate the ratio of this vat summary item and all items.
				// The shippingNetPart is proportional to this ratio.
				MonetaryAmount shippingNetPart = Money.from(zero);
				if (!netSumOfAllVatSummaryItems.isZero()) {
					shippingNetPart = retval.getShippingNet().multiply(vatSummaryItem.getNet().divide(netSumOfAllVatSummaryItems.getNumber()).getNumber());
				}

				// Add shippingNetPart to the sum "shippingVatValue"  
				Price shippingPart = new Price(shippingNetPart, shippingVatPercent);
				retval.setShippingVat(retval.getShippingVat().add(shippingPart.getUnitVat()));

				VatSummaryItem shippingVatSummaryItem = new VatSummaryItem(shippingVatDescription, shippingVatPercent, shippingPart.getUnitNet(),
						shippingPart.getUnitVat());

				// Adjust the vat summary item by the shipping part
				documentVatSummaryItems.add(shippingVatSummaryItem);
			}
		}

		// If shippingAutoVat is fix set, the shipping vat is 
		// a constant percent value.
		else {
			retval.setShippingNet(shippingAmount);

			// If noVat is set, the VAT is 0%
			if (noVatReference != null) {
				shippingVatDescription = noVatReference.getDescription();
				shippingVatPercent = NumberUtils.DOUBLE_ZERO;
			}

			// use shippingVatPercent as fix percent value for the shipping
			retval.setShippingVat(retval.getShippingNet().multiply(shippingVatPercent));

			VatSummaryItem shippingVatSummaryItem = new VatSummaryItem(shippingVatDescription, shippingVatPercent, retval.getShippingNet(),
					retval.getShippingVat());

			// Adjust the vat summary item by the shipping part
			documentVatSummaryItems.add(shippingVatSummaryItem);
		}
		
		retval.setShippingGross(retval.getShippingNet().add(retval.getShippingVat()));

		// round to full net cents
		if (netGross == DocumentSummary.ROUND_NET_VALUES) {
		    retval.setShippingNet(retval.getShippingNet().with(rounding));
		    retval.setTotalNet(retval.getTotalNet().with(rounding));
		} 
		
		// round to full gross cents
		if (netGross == DocumentSummary.ROUND_GROSS_VALUES) {
		    retval.setShippingGross(retval.getShippingGross().with(rounding));
		    retval.setTotalGross(retval.getTotalGross().with(rounding));
		    retval.setTotalNet(retval.getTotalGross().subtract(retval.getTotalVat()));
		    retval.setShippingNet(retval.getShippingGross().subtract(retval.getShippingVat()));
		}

		// Add the shipping to the documents sum.
		retval.setTotalVat(retval.getTotalVat().add(retval.getShippingVat()));
		
		retval.setTotalNet(retval.getTotalNet().add(retval.getShippingNet()));
		retval.setTotalGross(retval.getTotalGross().add(retval.getShippingGross()));

		// Finally, round the values
		if (netGross == DocumentSummary.ROUND_NET_VALUES) {
            retval.setTotalNet(retval.getTotalNet().with(rounding));
            retval.setTotalVat(retval.getTotalVat().with(rounding));
		    retval.setTotalGross(retval.getTotalGross().with(rounding));
		} else if (netGross == DocumentSummary.ROUND_GROSS_VALUES) {
		    retval.setTotalGross(retval.getTotalGross().with(rounding));
            retval.setTotalVat(retval.getTotalVat().with(rounding));
		    retval.setTotalNet(retval.getTotalGross().subtract(retval.getTotalVat()).subtract(retval.getTotalSET().with(rounding)));
		} else {
            retval.setTotalNet(retval.getTotalNet().with(rounding));
		    retval.setTotalGross(retval.getTotalGross().with(rounding));
			retval.setTotalVat(retval.getTotalGross().subtract(retval.getTotalNet()));
		}

		retval.setDiscountNet(retval.getDiscountNet().with(rounding));
		retval.setDiscountGross(retval.getDiscountGross().with(rounding));

		retval.setItemsNet(retval.getItemsNet().with(rounding));
		retval.setItemsGross(retval.getItemsGross().with(rounding));

		// round the shipping values
		if (netGross == DocumentSummary.ROUND_NET_VALUES) {
		    retval.setShippingNet(retval.getShippingNet().with(rounding));
		    retval.setShippingVat(retval.getShippingVat().with(rounding));
			retval.setShippingGross(retval.getShippingNet().add(retval.getShippingVat()));
		} else if (netGross == DocumentSummary.ROUND_GROSS_VALUES) {
		    retval.setShippingGross(retval.getShippingGross().with(rounding));
		    retval.setShippingVat(retval.getShippingVat().with(rounding));
		    retval.setShippingNet(retval.getShippingGross().subtract(retval.getShippingVat()));
		} else {
		    retval.setShippingNet(retval.getShippingNet().with(rounding));
		    retval.setShippingGross(retval.getShippingGross().with(rounding));
		    retval.setShippingVat(retval.getShippingGross().subtract(retval.getShippingVat()));
		}

		//calculate the final payment
		retval.setDeposit(deposit);
		retval.setFinalPayment(retval.getTotalGross().subtract(retval.getDeposit()));

		// Round also the Vat summaries
		documentVatSummaryItems.roundAllEntries();

		// Add the entries of the document summary set also to the global one
		if (globalVatSummarySet != null) {
			globalVatSummarySet.addVatSummarySet(documentVatSummaryItems);
		}

		return retval;
	}

    private VAT getItemVat(DocumentItem item) {
        // TODO  HOW CAN I DO THIS WITH Optional ???????
//        return Optional.ofNullable(item.getItemVat()).orElse(Optional.ofNullable(item.getProduct()).get().getVat());
        VAT retval = item.getItemVat();
        if(retval == null && item.getProduct() != null) {
            retval = item.getProduct().getVat();
        }
        return retval;
    }

	/**
	 * @return the useSET
	 */
	public final boolean isUseSET() {
		return useSET;
	}

	/**
	 * @param useSET the useSET to set
	 */
	public final void setUseSET(boolean useSET) {
		this.useSET = useSET;
	}

}
