/**
 * 
 */
package com.sebulli.fakturama.views.datatable.shippings;

import java.util.List;

import ca.odell.glazedlists.Filterator;
import ca.odell.glazedlists.TextFilterator;

import com.sebulli.fakturama.model.Shipping;

/**
 * 
 * This {@link Filterator} is for filtering {@link Shipping}s in a list view.
 * 
 */
public class ShippingFilterator implements TextFilterator<Shipping> {

	/**
	 * Which elements are relevant for filtering?
	 */
	@Override
	public void getFilterStrings(List<String> baseList, Shipping element) {
/*
        // Mark the columns that are used by the search function.
        searchColumns[0] = "name";
        searchColumns[1] = "description";
        searchColumns[2] = "value";
 */
//	    baseList.add(element.getShippingValue());
		baseList.add(element.getDescription());
		baseList.add(element.getName());
	}
}
