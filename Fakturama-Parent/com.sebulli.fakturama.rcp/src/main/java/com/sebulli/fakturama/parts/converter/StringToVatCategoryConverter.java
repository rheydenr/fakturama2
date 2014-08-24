/**
 * 
 */
package com.sebulli.fakturama.parts.converter;

import java.util.TreeSet;

import org.eclipse.core.databinding.conversion.Converter;

import com.sebulli.fakturama.model.VATCategory;

/**
 * @author rheydenr
 *
 */
public class StringToVatCategoryConverter extends Converter {

    private final TreeSet<VATCategory> categories;
    
    public StringToVatCategoryConverter(TreeSet<VATCategory> categories) {
        super(String.class, VATCategory.class);
        this.categories = categories;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
     */
    @Override
    public Object convert(Object fromObject) {
        // in: "Umsatzsteuer"
        // out: VATCategory
        // TODO Look for a better approach!
        String searchString = (String)fromObject;
        for (VATCategory vatCategory : categories) {
            if(CommonConverter.getCategoryName(vatCategory, "").equals(searchString)) {
                return vatCategory;
            }
        }
        return null;
    }

}
