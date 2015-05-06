/**
 * 
 */
package com.sebulli.fakturama.parts.converter;

import javax.inject.Inject;

import org.eclipse.core.databinding.conversion.Converter;
import org.eclipse.e4.core.services.nls.Translation;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.AbstractCategory;
import com.sebulli.fakturama.model.ItemAccountType;

/**
 *
 */
public class CategoryConverter<T extends AbstractCategory> extends Converter {

    @Inject
    @Translation
    protected Messages msg;
    
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
            if(type.isInstance(ItemAccountType.class)) {
                // special case, it's a key here.
                msg.getMessageFromKey(result);
            }
        }
        return result;
    }
}
