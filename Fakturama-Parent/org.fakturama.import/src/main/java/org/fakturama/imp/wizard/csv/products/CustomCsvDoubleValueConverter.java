package org.fakturama.imp.wizard.csv.products;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;

import com.opencsv.bean.AbstractCsvConverter;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;

public class CustomCsvDoubleValueConverter extends AbstractCsvConverter {

    private NumberFormat numberFormatInstance;

    public CustomCsvDoubleValueConverter() {
        // do nothing
    }

    @Override
    public Object convertToRead(String value) throws CsvDataTypeMismatchException, CsvConstraintViolationException {
        Object retval = null;
        try {
            retval = getNumberFormatInstance().parse(value);
        } catch (ParseException e) {
            // try other locales
            determineNumberFormat(value);
        }
        return retval;
    }

    private void determineNumberFormat(String value) {
        Locale testLocales[] = new Locale[] {Locale.GERMAN, Locale.ENGLISH, Locale.CHINESE};

        for (Locale locale : testLocales) {
            numberFormatInstance = NumberFormat.getCurrencyInstance(locale);
            try {
                getNumberFormatInstance().parse(value);
                break;
            } catch (ParseException e) {
                // try other locales
                numberFormatInstance = null;
            }
        }
        
        if(numberFormatInstance == null && StringUtils.contains(value, '%')) {
            numberFormatInstance = NumberFormat.getPercentInstance();
        }
    }

    public NumberFormat getNumberFormatInstance() {
        if (numberFormatInstance == null) {
            numberFormatInstance = NumberFormat.getCurrencyInstance(Locale.US);
        }
        return numberFormatInstance;
    }
}