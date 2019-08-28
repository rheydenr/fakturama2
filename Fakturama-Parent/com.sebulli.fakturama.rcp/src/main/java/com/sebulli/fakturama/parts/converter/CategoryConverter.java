/**
 * 
 */
package com.sebulli.fakturama.parts.converter;

import org.eclipse.core.databinding.conversion.Converter;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.AbstractCategory;
import com.sebulli.fakturama.model.ItemListTypeCategory;

/**
 *
 */
public class CategoryConverter<T extends AbstractCategory> extends Converter<T, String> {

    protected Messages msg;
    
    private Class<T> type;
    
    public CategoryConverter(Class<T> type) {
        this(type, null);
    }
    
    public CategoryConverter(Class<T> type, Messages msg) {
        super(type, String.class);
        this.type = type;
        this.msg = msg;
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
     */
    @Override
    public String convert(AbstractCategory fromObject) {
        String result = null;
        if(type.equals(getFromType())) {
            result = CommonConverter.getCategoryName(fromObject, "");
            if(type.getSimpleName().equals(ItemListTypeCategory.class.getSimpleName())) {
                // special case, it's a key here.
                result = msg.getMessageFromKey(result);
            }
        }
        return result;
    }
}
