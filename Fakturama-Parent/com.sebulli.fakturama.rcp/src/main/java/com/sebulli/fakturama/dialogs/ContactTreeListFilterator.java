/**
 * 
 */
package com.sebulli.fakturama.dialogs;

import java.util.List;

import org.eclipse.core.databinding.beans.IBeanListProperty;
import org.eclipse.core.databinding.beans.IBeanProperty;
import org.eclipse.core.databinding.beans.IBeanValueProperty;

import com.sebulli.fakturama.dao.DebitorAddress;
import com.sebulli.fakturama.model.Contact;

import ca.odell.glazedlists.TextFilterator;

/**
 *
 */
public class ContactTreeListFilterator implements TextFilterator<DebitorAddress> {

	/** methods for extracting field values */
	private IBeanProperty[] beanProperties = null;

	/**
	 * Create a BeanTextFilterator that uses the specified property names.
	 */
	public ContactTreeListFilterator(IBeanProperty... propertyNames) {
		this.beanProperties = propertyNames;
	}

	/** {@inheritDoc} */
	@SuppressWarnings({ "unchecked" })
	public void getFilterStrings(List<String> baseList, DebitorAddress element) {
		if (element == null)
			return;

		// get the filter strings
		for (int p = 0; p < beanProperties.length; p++) {
			Object propertyValue = null;
			if (beanProperties[p] instanceof IBeanValueProperty) {
				propertyValue = ((IBeanValueProperty)beanProperties[p]).getValue(element);
//			} else if (beanProperties[p] instanceof IBeanListProperty && element.getAddresses().size() > 0) {
//				propertyValue = ((IBeanListProperty)beanProperties[p]).getList(element).get(0);
			}
			if (propertyValue != null)
				baseList.add(propertyValue.toString());
		}
	}
}
