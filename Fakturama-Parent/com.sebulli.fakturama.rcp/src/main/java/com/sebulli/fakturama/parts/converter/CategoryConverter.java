/**
 * 
 */
package com.sebulli.fakturama.parts.converter;

import org.eclipse.core.databinding.conversion.Converter;

import com.sebulli.fakturama.model.AbstractCategory;

/**
 *
 */
public class CategoryConverter<T extends AbstractCategory> extends Converter {
    
    private Class<T> type;
    
    public CategoryConverter(Class<T> type) {
        super(type, String.class);
        this.type = type;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
     */
    @Override
    public Object convert(Object fromObject) {
        String result = null;
        if(type.equals(getFromType())) {
            result = CommonConverter.getCategoryName((AbstractCategory) fromObject, "");
        }
        return result;
    }
}
