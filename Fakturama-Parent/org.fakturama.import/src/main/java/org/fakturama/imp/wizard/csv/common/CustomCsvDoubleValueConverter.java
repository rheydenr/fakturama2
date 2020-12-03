package org.fakturama.imp.wizard.csv.common;

import com.opencsv.bean.AbstractCsvConverter;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.sebulli.fakturama.misc.DataUtils;

/**
 * Converter to automatically translate Strings which represent a value into that value.
 *
 */
public class CustomCsvDoubleValueConverter extends AbstractCsvConverter {

    public CustomCsvDoubleValueConverter() {
        // do nothing
    }

    @Override
    public Object convertToRead(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        return DataUtils.getInstance().StringToDouble(value);
    }
}