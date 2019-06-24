/**
 * 
 */
package com.sebulli.fakturama.views.datatable.contacts;

import java.util.List;

import com.sebulli.fakturama.model.Debitor;
import com.sebulli.fakturama.model.Debitor_;

import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;

/**
 *
 */
public class ContactListFilterator implements TextFilterator<Debitor> {

	@Override
	public void getFilterStrings(List<String> baseList, Debitor element) {
      GlazedLists.textFilterator(Debitor.class, 
	      Debitor_.customerNumber.getName(),
	      Debitor_.firstName.getName(),
	      Debitor_.name.getName(),
	      Debitor_.company.getName() // ,
	//      Address_.zip.getName(),
	//      Address_.city.getName()
      );
	}

}
