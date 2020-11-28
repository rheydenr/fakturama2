package org.fakturama.imp.wizard.csv.common;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;

import org.apache.commons.collections4.ListValuedMap;
import org.fakturama.imp.wizard.csv.contacts.CustomCsvBoolean;

import com.opencsv.bean.AbstractCsvConverter;
import com.opencsv.bean.BeanFieldSingleValue;
import com.opencsv.bean.CsvConverter;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import com.opencsv.bean.customconverter.ConvertGermanToBoolean;
import com.opencsv.exceptions.CsvBadConverterException;

public class CustomHeaderColumnNameTranslateMappingStrategy<T> extends HeaderColumnNameTranslateMappingStrategy<T> {
    @Override
    protected CsvConverter determineConverter(Field field, Class<?> elementType, String locale, String writeLocale,
            Class<? extends AbstractCsvConverter> customConverter) throws CsvBadConverterException {
        if (customConverter == null && elementType == Double.class) {
            customConverter = CustomCsvDoubleValueConverter.class;
        } else if(customConverter == null && (field.isAnnotationPresent(CsvDate.class))) {
            // intercept default behavior
            customConverter = CustomConverterDate.class;
        }
        return super.determineConverter(field, elementType, locale, writeLocale, customConverter);
    }
    
    @Override
    protected void loadUnadornedFieldMap(ListValuedMap<Class<?>, Field> fields) {
        // extends HeaderColumnNameTranslateMappingStrategy
        for(Map.Entry<Class<?>, Field> classFieldEntry : fields.entries()) {
            if(!(Serializable.class.isAssignableFrom(classFieldEntry.getKey()) && "serialVersionUID".equals(classFieldEntry.getValue().getName()))) {
                CsvConverter converter = determineConverter(classFieldEntry.getValue(), classFieldEntry.getValue().getType(), null, null, null);
                if(classFieldEntry.getValue().isAnnotationPresent(CustomCsvBoolean.class)) {
                    ConvertGermanToBoolean<T, String> conv = new ConvertGermanToBoolean<>();
                    conv.setField(classFieldEntry.getValue());
                    conv.setType(classFieldEntry.getKey());
                    conv.setErrorLocale(errorLocale);
                    fieldMap.put(classFieldEntry.getValue().getName().toUpperCase(), conv);
                    
                } else {
                fieldMap.put(classFieldEntry.getValue().getName().toUpperCase(), new BeanFieldSingleValue<>(
                        classFieldEntry.getKey(), classFieldEntry.getValue(),
                        false, errorLocale, converter, null, null));
                }
            }
        }
    }
}
