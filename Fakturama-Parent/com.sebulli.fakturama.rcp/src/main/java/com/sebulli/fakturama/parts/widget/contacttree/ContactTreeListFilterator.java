/**
 * 
 */
package com.sebulli.fakturama.parts.widget.contacttree;

import java.util.List;

import org.eclipse.core.databinding.beans.IBeanListProperty;
import org.eclipse.core.databinding.beans.IBeanProperty;
import org.eclipse.core.databinding.beans.IBeanValueProperty;

import com.sebulli.fakturama.dao.DebitorAddress;

import ca.odell.glazedlists.TextFilterator;

/**
 *
 */
public class ContactTreeListFilterator<T extends DebitorAddress> implements TextFilterator<T> {

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
	public void getFilterStrings(List<String> baseList, T element) {
		if (element == null)
			return;

		// get the filter strings
		for (int p = 0; p < beanProperties.length; p++) {
			Object propertyValue = null;
			if (beanProperties[p] instanceof IBeanValueProperty) {
				propertyValue = ((IBeanValueProperty)beanProperties[p]).getValue(element);
			} else if (beanProperties[p] instanceof IBeanListProperty && element.getAddress() == null) {
				propertyValue = ((IBeanListProperty)beanProperties[p]).getList(element).get(0);
			}
			if (propertyValue != null)
				baseList.add(propertyValue.toString());
		}
	}
}
