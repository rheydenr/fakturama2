/**
 * 
 */
package com.sebulli.fakturama.dto;

import java.util.Iterator;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

import org.javamoney.moneta.FastMoney;

import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.CustomDocument;
import com.sebulli.fakturama.model.DocumentItem;

/**
 * Calculator for the document summaries.
 *
 */
public class ItemTotalCalculator {
	private CurrencyUnit currencyCode;

	public ItemTotalCalculator(CurrencyUnit currencyCode) {
		this.currencyCode = currencyCode;
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
	 * @param noVat
	 *            TRUE, if all VAT values are set to 0.
	 * @param noVatDescription
	 *            Name of the VAT, which is 0.
	 * @param scaleFactor
	 * 
	 * @param deposit
	 */
	public DocumentSummary calculate(CustomDocument document) {
		DocumentSummary retval = new DocumentSummary();
		Double vatPercent;
		String vatDescription;
		Double scaleFactor = 1.0;

		// This Vat summary contains only the VAT entries of this document,
		// whereas the the parameter vatSummaryItems is a global VAT summary
		// and contains entries from this document and from others.
		VatSummarySet documentVatSummaryItems = new VatSummarySet();

		// Set the values to 0.0
//		resetValues();

		// Use all non-deleted items
		for (DocumentItem item : document.getItems()) {

			// Get the data from each item
			vatDescription = item.getItemVat().getDescription();
			vatPercent = item.getItemVat().getTaxValue();
			Price price = new Price(item, scaleFactor);  // scaleFactor was always 1.0 in the old application
			MonetaryAmount itemVat = price.getTotalVat();

			// Add the total net value of this item to the sum of net items
			retval.setItemsNet(retval.getItemsNet().add(price.getTotalNet()));

			// If noVat is set, the VAT is 0%
			if (document.getNoVatReference() != null) {
				vatDescription = document.getNoVatReference().getDescription();
				vatPercent = 0.0;
				itemVat = FastMoney.of(0.0, currencyCode);
			}

			// Add the VAT to the sum of VATs
			retval.setTotalVat(retval.getTotalVat().add(itemVat));

			// Add the VAT summary item to the ... 
			VatSummaryItem vatSummaryItem = new VatSummaryItem(vatDescription, vatPercent, price.getTotalNet(), itemVat);

			// .. VAT summary of the document ..
			documentVatSummaryItems.add(vatSummaryItem);

		}

		// *** round sum of items
		
		// round to full net cents
//		if (netgross == DocumentSummary.ROUND_NET_VALUES) {
//			this.itemsNet.round();
//		} 
		
		// Gross value is the sum of net and VAT value
		retval.setTotalNet(retval.getItemsNet());
		retval.setTotalGross(retval.getItemsNet());
		retval.setTotalGross(retval.getTotalGross().add(retval.getTotalVat()));
		
		// round to full gross cents
//		if (netgross == DocumentSummary.ROUND_GROSS_VALUES) {
//			this.itemsGross.round();
//			this.itemsNet.set(this.itemsGross.asDouble() - this.totalVat.asDouble() );
//			this.totalNet.set(this.itemsNet.asDouble());
//		}
//		this.totalGross.set(this.itemsGross.asDouble());
		
		MonetaryAmount itemsNet = retval.getItemsNet();
		MonetaryAmount itemsGross = retval.getItemsGross();

		
		// *** DISCOUNT ***
		
		// Calculate the absolute discount values
		retval.setDiscountNet(itemsNet.multiply(document.getItemsRebate()));
		retval.setDiscountGross(itemsGross.multiply(document.getItemsRebate()));

		// Calculate discount
		if (!DataUtils.DoublesAreEqual(document.getItemsRebate(), 0.0)) {

			// Discount value = discount percent * Net value
			MonetaryAmount discountNet = itemsNet.multiply(document.getItemsRebate());

			// Calculate the vat value in percent from the gross value of all items
			// and the net value of all items. So the discount's vat is the average 
			// value of the item's vat
			Double discountVatPercent;
			if (itemsNet.isZero())
				discountVatPercent = (itemsGross.getNumber().doubleValueExact()/itemsNet.getNumber().doubleValueExact()) - 1;
			else
				// do not divide by zero
				discountVatPercent = 0.0;

			// If noVat is set, the VAT is 0%
			if (document.getNoVatReference() != null) {
				discountVatPercent = 0.0;
			}

			// Reduce all the VAT entries in the VAT Summary Set by the discount 
			MonetaryAmount discountVatValue = FastMoney.MIN_VALUE;
			String discountVatDescription = "";
			for (Iterator<VatSummaryItem> iterator = documentVatSummaryItems.iterator(); iterator.hasNext();) {

				// Get the data from each entry
				VatSummaryItem vatSummaryItem = iterator.next();
				discountVatDescription = vatSummaryItem.getVatName();
				discountVatPercent = vatSummaryItem.getVatPercent();

				// If noVat is set, the VAT is 0%
				if (document.getNoVatReference() != null) {
					discountVatDescription = document.getNoVatReference().getDescription();
					discountVatPercent = 0.0;
				}

				// Calculate the ratio of this vat summary item and all items.
				// The discountNetPart is proportional to this ratio.
				MonetaryAmount discountNetPart = FastMoney.MIN_VALUE;
				if (!itemsNet.isZero())
					discountNetPart = discountNet.multiply(vatSummaryItem.getNet().getNumber().doubleValueExact() / itemsNet.getNumber().doubleValueExact());

				// Add discountNetPart to the sum "discountVatValue"  
				Price discountPart = new Price(discountNetPart, discountVatPercent);
				discountVatValue = discountVatValue.add(discountPart.getUnitVat());

				VatSummaryItem discountVatSummaryItem = new VatSummaryItem(discountVatDescription, discountVatPercent, discountPart.getUnitNet(),
						discountPart.getUnitVat());

				// Adjust the vat summary item by the discount part
				documentVatSummaryItems.add(discountVatSummaryItem);

			}

			// adjust the documents sum by the discount
			retval.setTotalVat(retval.getTotalVat().add(discountVatValue));
			retval.setTotalNet(retval.getTotalNet().add(discountNet));
			
//			// round to full net cents
//			if (netgross == DocumentSummary.ROUND_NET_VALUES) {
//				this.discountNet.round();
//				this.totalNet.round();
//			} 
//			
//			if (netgross != DocumentSummary.ROUND_GROSS_VALUES) {
//				this.totalGross.set(this.totalNet.asDouble() + this.totalVat.asDouble());
//			}
//			
//			// round to full gross cents
//			if (netgross == DocumentSummary.ROUND_GROSS_VALUES) {
//				this.discountGross.round();
//				this.totalGross.add(this.discountGross.asDouble());
//				this.totalGross.round();
//				this.discountNet.set(this.discountGross.asDouble() - discountVatValue);
//				this.totalNet.set(this.totalGross.asDouble() - this.totalVat.asDouble());
//			}
		}

		// calculate shipping

		// Scale the shipping
		MonetaryAmount shippingValue = FastMoney.of(document.getShippingValue() * scaleFactor, currencyCode);
		Double shippingVatPercent = 0.0;
		String shippingVatDescription = null;
		final MonetaryAmount zero = FastMoney.of(0.0, currencyCode);

		// If shippingAutoVat is not fix, the shipping vat is 
		// an average value of the vats of the items.
		if (!document.getShippingAutoVat().isSHIPPINGVATFIX()) {

			// If the shipping is set as gross value, calculate the net value.
			// Use the average vat of all the items.
			if (document.getShippingAutoVat().isSHIPPINGVATGROSS()) {
				if (!itemsGross.isEqualTo(zero)) {
					// shippingValue * itemsNet / itemsGross
					retval.setShippingNet(shippingValue.multiply(itemsNet.divide(itemsGross.getNumber()).getNumber()));
				} else {
					retval.setShippingNet(shippingValue);
				}
			}

			// If the shipping is set as net value, use the net value.
			if (document.getShippingAutoVat().isSHIPPINGVATNET())
				retval.setShippingNet(shippingValue);

			// Use the average vat of all the items.
			if (!itemsNet.isEqualTo(zero)) {
				shippingVatPercent = (itemsGross.divide(itemsNet.getNumber())).getNumber().doubleValue() - 1;
			} else {
				shippingVatPercent = Double.valueOf(0.0);
			}
			
			// Increase the vat summary entries by the shipping ratio

			// Calculate the sum of all VatSummary entries
			MonetaryAmount netSumOfAllVatSummaryItems = FastMoney.from(zero);
//			documentVatSummaryItems.stream().mapToDouble(mapper)
			for (VatSummaryItem vatSummaryItem : documentVatSummaryItems) {
				netSumOfAllVatSummaryItems.add(vatSummaryItem.getNet());
			}

			for (VatSummaryItem vatSummaryItem : documentVatSummaryItems) {

				// Get the data from each entry
				shippingVatDescription = vatSummaryItem.getVatName();
				shippingVatPercent = vatSummaryItem.getVatPercent();

				// If noVat is set, the VAT is 0%
				if (document.getNoVatReference() != null) {
					shippingVatDescription = document.getNoVatReference().getDescription();
					shippingVatPercent = Double.valueOf(0.0);
				}

				// Calculate the ratio of this vat summary item and all items.
				// The shippingNetPart is proportional to this ratio.
				MonetaryAmount shippingNetPart = FastMoney.from(zero);
				if (!netSumOfAllVatSummaryItems.isEqualTo(zero)) {
					shippingNetPart = retval.getShippingNet().multiply(vatSummaryItem.getNet().divide(netSumOfAllVatSummaryItems.getNumber()).getNumber());
				}

				// Add shippingNetPart to the sum "shippingVatValue"  
				Price shippingPart = new Price(shippingNetPart, shippingVatPercent);
				retval.getShippingVat().add(shippingPart.getUnitVat());

				VatSummaryItem shippingVatSummaryItem = new VatSummaryItem(shippingVatDescription, shippingVatPercent, shippingPart.getUnitNet(),
						shippingPart.getUnitVat());

				// Adjust the vat summary item by the shipping part
				documentVatSummaryItems.add(shippingVatSummaryItem);

			}

		}

		// If shippingAutoVat is fix set, the shipping vat is 
		// a constant percent value.
		else {

			retval.setShippingNet(shippingValue);

			// If noVat is set, the VAT is 0%
			if (document.getNoVatReference() != null) {
				shippingVatDescription = document.getNoVatReference().getDescription();
				shippingVatPercent = Double.valueOf(0.0);
			}

			// use shippingVatPercent as fix percent value for the shipping
			retval.setShippingVat(retval.getShippingNet().multiply(shippingVatPercent));

			VatSummaryItem shippingVatSummaryItem = new VatSummaryItem(shippingVatDescription, shippingVatPercent, retval.getShippingNet(),
					retval.getShippingVat());

			// Adjust the vat summary item by the shipping part
			documentVatSummaryItems.add(shippingVatSummaryItem);

		}

//		// round to full net cents
//		if (netgross == DocumentSummary.ROUND_NET_VALUES) {
//			this.shippingNet.round();
//			this.totalNet.round();
//		} 
//		
		retval.setShippingGross(retval.getShippingNet().add(retval.getShippingVat()));
		
//		// round to full gross cents
//		if (netgross == DocumentSummary.ROUND_GROSS_VALUES) {
//			this.shippingGross.round();
//			this.totalGross.round();
//			this.totalNet.set(this.totalGross.asDouble() - this.totalVat.asDouble());
//			this.shippingNet.set(this.shippingGross.asDouble() - this.shippingVat.asDouble());
//		}

		
		// Add the shipping to the documents sum.
		retval.setTotalVat(retval.getTotalVat().add(retval.getShippingVat()));
		retval.setTotalNet(retval.getTotalNet().add(retval.getShippingNet()));
		retval.setTotalGross(retval.getTotalNet().add(retval.getTotalVat()));

//		// Finally, round the values
//		if (netgross == DocumentSummary.ROUND_NET_VALUES) {
//			this.totalNet.round();
//			this.totalVat.round();
//			this.totalGross.set(this.totalNet.asDouble() + this.totalVat.asDouble());
//		} else if (netgross == DocumentSummary.ROUND_GROSS_VALUES) {
//			this.totalGross.round();
//			this.totalVat.round();
//			this.totalNet.set(this.totalGross.asDouble() - this.totalVat.asDouble());
//		} else {
//			this.totalNet.round();
//			this.totalGross.round();
//			this.totalVat.set(this.totalGross.asDouble() - this.totalNet.asDouble());
//		}
//
//		this.discountNet.round();
//		this.discountGross.round();
//
//		this.itemsNet.round();
//		this.itemsGross.round();
//
//		// Finally, round the values
//		if (netgross == DocumentSummary.ROUND_NET_VALUES) {
//			this.shippingNet.round();
//			this.shippingVat.round();
//			this.shippingGross.set(this.shippingNet.asDouble() + this.shippingVat.asDouble());
//		} else if (netgross == DocumentSummary.ROUND_GROSS_VALUES) {
//			this.shippingGross.round();
//			this.shippingVat.round();
//			this.shippingNet.set(this.shippingGross.asDouble() - this.shippingVat.asDouble());
//		} else {
//			this.shippingNet.round();
//			this.shippingGross.round();
//			this.shippingVat.set(this.shippingGross.asDouble() - this.shippingVat.asDouble());
//		}

		//calculate the final payment
		retval.setDeposit(FastMoney.of(document.getDeposit(), currencyCode));
		retval.setFinalPayment(retval.getTotalGross().subtract(retval.getDeposit()));

		// Round also the Vat summaries
//		documentVatSummaryItems.roundAllEntries();

		// Add the entries of the document summary set also to the global one
		// FIXME is not used at the moment
//		if (document.globalVatSummarySet() != null)
//			globalVatSummarySet.addVatSummarySet(documentVatSummaryItems);

		return retval;
	}

}
