package org.fakturama.imp.wizard.csv.common;

import java.lang.reflect.InvocationTargetException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import com.opencsv.bean.ConverterDate;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.sebulli.fakturama.misc.IDateFormatterService;

public class CustomConverterDate extends ConverterDate {
    
    public CustomConverterDate() {
        // these params are irrelevant since we don't use them
        super(Date.class, "de", null, null,  "yyyyMMdd'T'HHmmss",  "yyyyMMdd'T'HHmmss", "ISO", "ISO");
 
    }

    public CustomConverterDate(Class<?> type, String locale, String writeLocale, Locale errorLocale, String readFormat, String writeFormat,
            String readChronology, String writeChronology) {
        super(type, locale, writeLocale, errorLocale, readFormat, writeFormat, readChronology, writeChronology);
    }

    @Override
    public Object convertToRead(String value) throws CsvDataTypeMismatchException {
        // Can't use injection because this class is handled by an external library.
        // So we've to get the OSGi service the classical way...

        BundleContext bc = FrameworkUtil.getBundle(getClass()).getBundleContext();
        ServiceReference<IDateFormatterService> serviceReference = bc.getServiceReference(IDateFormatterService.class);
        IDateFormatterService service = bc.getService(serviceReference);

        Object returnValue = null;
        if (StringUtils.isNotBlank(value)) {

            // Convert Date-based types
            if (Date.class.isAssignableFrom(type)) {
                try {
                    Date d = null;
                    synchronized (service) {
                        Calendar cal = service.getCalendarFromDateString(value);
                        if (cal != null) {
                            d = cal.getTime();
                        }
                    }

                    returnValue = type.getConstructor(Long.TYPE).newInstance(d.getTime());
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                    CsvDataTypeMismatchException csve = new CsvDataTypeMismatchException(value, type);
                    csve.initCause(e);
                    throw csve;
                }
                return returnValue;
            }
        }

        return super.convertToRead(value);
    }

}
