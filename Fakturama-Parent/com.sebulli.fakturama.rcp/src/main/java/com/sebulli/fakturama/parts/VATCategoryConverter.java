/**
 * 
 */
package com.sebulli.fakturama.parts;

import org.eclipse.core.databinding.conversion.Converter;

import com.sebulli.fakturama.model.VATCategory;

/**
 * @author rheydenr
 *
 */
public class VATCategoryConverter extends Converter {

    public VATCategoryConverter() {
        super(VATCategory.class, String.class);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
     */
    @Override
    public Object convert(Object fromObject) {
        String result = null;
        if(VATCategory.class.equals(getFromType())) {
            result = ((VATCategory) fromObject).getName();
        }
        return result;
    }

}
