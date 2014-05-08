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
public class IdToVATCategoryConverter extends Converter {

        /**
         * Given an ID as String
         */
    public IdToVATCategoryConverter() {
            super(String.class, VATCategory.class);
        }

        /* (non-Javadoc)
         * @see org.eclipse.core.databinding.conversion.IConverter#convert(java.lang.Object)
         */
        @Override
        public Object convert(Object fromObject) {
            String result = null;
            if(String.class.equals(getFromType())) {
                result = ((String) fromObject);
            }
            return result;
        }


}
