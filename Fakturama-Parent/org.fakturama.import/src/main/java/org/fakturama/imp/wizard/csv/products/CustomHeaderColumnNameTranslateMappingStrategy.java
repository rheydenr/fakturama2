package org.fakturama.imp.wizard.csv.products;

import java.lang.reflect.Field;

import com.opencsv.bean.AbstractCsvConverter;
import com.opencsv.bean.CsvConverter;
import com.opencsv.bean.HeaderColumnNameTranslateMappingStrategy;
import com.opencsv.exceptions.CsvBadConverterException;

public class CustomHeaderColumnNameTranslateMappingStrategy<T> extends HeaderColumnNameTranslateMappingStrategy<T> {
    @Override
    protected CsvConverter determineConverter(Field field, Class<?> elementType, String locale, String writeLocale,
            Class<? extends AbstractCsvConverter> customConverter) throws CsvBadConverterException {
        if (customConverter == null && elementType == Double.class) {
            // TODO annotate model attributes (fields) with a kind of "Money" annotation
            // (generation of any additional annotation didn't work at the moment)
            customConverter = CustomCsvDoubleValueConverter.class;
        }
        return super.determineConverter(field, elementType, locale, writeLocale, customConverter);
    }
}
