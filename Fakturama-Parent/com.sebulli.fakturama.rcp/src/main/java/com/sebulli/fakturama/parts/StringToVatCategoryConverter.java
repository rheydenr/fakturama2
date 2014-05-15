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
public class StringToVatCategoryConverter extends Converter {

    public StringToVatCategoryConverter() {
        super(String.class, VATCategory.class);
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
     */
    @Override
    public Object convert(Object fromObject) {
        // in: "Umsatzsteuer"
        // out: VATCategory
        // FIXME!
        return null;
    }

}
