/**
 * 
 */
package com.sebulli.fakturama.views.datatable;

import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;

import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 * @author rheydenr
 *
 */
public class ImageDisplayConverter extends DisplayConverter {

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter#canonicalToDisplayValue(java.lang.Object)
	 */
	@Override
	public Object canonicalToDisplayValue(Object canonicalValue) {
		// TODO Auto-generated method stub
		return Icon.COMMAND_CHECKED.getImage(IconSize.DefaultIconSize);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter#displayToCanonicalValue(java.lang.Object)
	 */
	@Override
	public Object displayToCanonicalValue(Object displayValue) {
		// TODO Auto-generated method stub
		return null;
	}

}
