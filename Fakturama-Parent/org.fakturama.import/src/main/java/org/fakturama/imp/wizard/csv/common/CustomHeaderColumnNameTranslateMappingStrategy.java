package org.fakturama.imp.wizard.csv.common;

import java.lang.reflect.Field;

import com.opencsv.bean.AbstractCsvConverter;
import com.opencsv.bean.CsvConverter;
import com.opencsv.bean.CsvDate;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
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
}
