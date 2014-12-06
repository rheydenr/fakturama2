/**
 * 
 */
package com.sebulli.fakturama.parts.converter;

import java.util.TreeSet;

import org.eclipse.core.databinding.conversion.Converter;

import com.sebulli.fakturama.model.AbstractCategory;

/**
 *
 */
public class StringToCategoryConverter<T extends AbstractCategory> extends Converter {

    private final TreeSet<T> categories;
    
    public StringToCategoryConverter(TreeSet<T> categories, Class<T> clazz) {
        super(String.class, clazz);
        this.categories = categories;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
     */
    @Override
    public Object convert(Object fromObject) {
        // in: "Umsatzsteuer"
        // out: VATCategory
        // TODO Look for a better approach! ==> ComboBoxLabelProvider??
        String searchString = (String)fromObject;
        for (T category : categories) {
            if(CommonConverter.getCategoryName(category, "").equals(searchString)) {
                return category;
            }
        }
        return null;
    }

}
