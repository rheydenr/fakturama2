/**
 * 
 */
package com.sebulli.fakturama.calculate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.dao.PropertiesDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.UserProperty;

/**
 * Generator for various types of numbers (for customers, invoices etc.).
 *
 */
public class NumberGenerator {
	private static final int ERROR_NOT_NEXT_ID = 1;
	private static final int NO_ERROR = 0;
    private String editorID = "";

	private FakturamaModelFactory modelFactory =  FakturamaModelPackage.MODELFACTORY;

    @Inject
    private IPreferenceStore defaultValuePrefs;
    
    @Inject
    private ILogger log;

    @Inject
    private PropertiesDAO propertiesDao;
    
    public int setNextFreeNumberInPrefStore(String value) {
    	return setNextFreeNumberInPrefStore(value, editorID);
    }

	/**
	 * Set the next free document number in the preference store. But check if
	 * the documents number is the next free one.
	 * 
	 * @param s
	 *            The document number as string.
	 * @return Errorcode, if the document number is correctly set to the next
	 *         free number.
	 */
	public int setNextFreeNumberInPrefStore(String value, String editorId) {

		// Create the string of the preference store for format and number
		String prefStrFormat = "NUMBERRANGE_" + editorId.toUpperCase() + "_FORMAT";
		String prefStrNr = "NUMBERRANGE_" + editorId.toUpperCase() + "_NR";
		String format;
		String s = "";
		int nr;
		int result = ERROR_NOT_NEXT_ID;
		Integer nextnr;

		// Get the next document number from the preferences, increased by one.
		format = defaultValuePrefs.getString(prefStrFormat);
		Optional<String> propVal = propertiesDao.findPropertyValue(prefStrNr, true);
		nextnr = Integer.parseInt(propVal.orElse("1")) + 1;

		// Exit, if format is empty
		if (format.trim().isEmpty())
			return NO_ERROR;

		// Fill the replacements with dummy values
		format = format.replace("{yyyy}", "0000");
		format = format.replace("{yy}", "00");
		format = format.replace("{mm}", "00");
		format = format.replace("{dd}", "00");
		format = format.replace("{YYYY}", "0000");
		format = format.replace("{YY}", "00");
		format = format.replace("{MM}", "00");
		format = format.replace("{DD}", "00");

		
		// Find the placeholder for a decimal number with n digits
		// with the format "{Xnr}", "X" is the number of digits.
		Pattern p = Pattern.compile("\\{\\d*nr\\}");
		Matcher m = p.matcher(format);
		
		// Get the next number
		if (m.find()) {
			
			// Exit, if the value is too short
			if (value.length() < m.start() 
					|| (value.length() - format.length() + m.end()) <= m.start() )
				return ERROR_NOT_NEXT_ID;

			// Extract the number string
			s = value.substring(m.start(), value.length() - format.length() + m.end());

			try {
				// Convert it to an integer and increase it by one.
				nr = Integer.parseInt(s) + 1;

				// Update the value of the last document number, but only,
				// If the number of this document is the next free number
				if (nr == nextnr) {
					
					// Store the number to the preference store
					setNextNumber(prefStrNr, nr, editorId);
					result = NO_ERROR;
				}
			}
			catch (NumberFormatException e) {
				log.error(e, "Document number invalid");
			}
		}

		// The result of the validation
		return result;
	}
	
	public void setNextNumber(String prefStrNr, int nr) {
		setNextNumber(prefStrNr, nr, editorID);
	}

	public void setNextNumber(String prefStrNr, int nr, String editorId) {
//		defaultValuePrefs.setValue(prefStrNr, nr);

		// Store the date of now to a property
		LocalDate now = LocalDate.now();
		try {
//			defaultValuePrefs.setValue("last_setnextnr_date_" + getEditorID().toLowerCase(), now.format(DateTimeFormatter.ISO_DATE));
//			((IPersistentPreferenceStore)defaultValuePrefs).save();
			UserProperty nextNumber = propertiesDao.findByName(prefStrNr);
			if(nextNumber == null) {
			    nextNumber = modelFactory.createUserProperty();
			    nextNumber.setName(prefStrNr);
			}
			nextNumber.setValue(Integer.toString(nr));
			
			String entityName = "last_setnextnr_date_" + editorId.toLowerCase();
            UserProperty lastSetNextNrDate = propertiesDao.findByName(entityName);
            if(lastSetNextNrDate == null) {
                lastSetNextNrDate = modelFactory.createUserProperty();
                lastSetNextNrDate.setName(entityName);
            }
			lastSetNextNrDate.setValue(now.format(DateTimeFormatter.ISO_DATE));
			propertiesDao.save(lastSetNextNrDate);
			propertiesDao.save(nextNumber);
			defaultValuePrefs.setValue(prefStrNr, nr);
        } catch (FakturamaStoringException e1) {
            log.error(e1, "Error while flushing default value preferences.");
        }
	}
	
	public int getCurrentNumber() {
		return getCurrentNumber(editorID);
	}
	
	/**
	 * Gets the current number of the specified entity type.
	 * 
	 * @param editorId the entity type (invoice, product etc.)
	 * @return
	 */
	public int getCurrentNumber(String editorId) {
		String prefStrNr = "NUMBERRANGE_" + editorId.toUpperCase() + "_NR";
		Optional<String> propVal = propertiesDao.findPropertyValue(prefStrNr);
		String nextNumberString = propVal.orElse("1");
		return Integer.parseInt(nextNumberString);
	}
	
	public String getNextNr() {
		return getNextNr(editorID);
	}

	/**
	 * Get the next document number
	 * 
	 * @return The next document number
	 */
	public String getNextNr(String editorId) {
		// Create the string of the preference store for format and number
		String prefStrFormat = "NUMBERRANGE_" + editorId.toUpperCase() + "_FORMAT";
		String prefStrNr = "NUMBERRANGE_" + editorId.toUpperCase() + "_NR";
		String format;
		String nrExp = "";
		String nextNr;
		int nr;

		// Store the date of now to a property
		LocalDate now = LocalDate.now();
		int yyyy = now.getYear();
		int mm = now.getMonthValue();
		int dd = now.getDayOfMonth();

		int last_yyyy = 0; 
		int last_mm = 0; 
		int last_dd = 0; 

		Optional<String> propVal = propertiesDao.findPropertyValue("last_setnextnr_date_" + editorId.toLowerCase());
		String lastSetNextNrDate = propVal.orElse(DateTimeFormatter.ISO_DATE.format(now));
				 
        // Get the year, month and date of a string like "2011-12-24"
        if (lastSetNextNrDate.length() == 10) {
            LocalDate localDate = LocalDate.parse(lastSetNextNrDate);
            last_yyyy = localDate.getYear();
            last_mm = localDate.getMonthValue();
            last_dd = localDate.getDayOfMonth();
        }

		// Get the last (it's the next free) document number from the preferences
		format = defaultValuePrefs.getString(prefStrFormat);
//		nr = defaultValuePrefs.getInt(prefStrNr);
		nr = getCurrentNumber(editorId);

		// Check, whether the date string is a new one
		boolean startNewCounting = false;
		if ((format.contains("{yyyy}") || format.contains("{yy}")) && yyyy != last_yyyy
				|| format.contains("{mm}") && mm != last_mm
				|| format.contains("{dd}") && dd != last_dd)
			startNewCounting = true;
		
		// Reset the counter
		if (startNewCounting) {
			nr = 1;
		}
		setNextNumber(prefStrNr, nr, editorId); 
		
		// Replace the date information
		format = format.replace("{yyyy}", String.format("%04d", yyyy));
		format = format.replace("{yy}", String.format("%04d", yyyy).substring(2, 4));
		format = format.replace("{mm}", String.format("%02d", mm));
		format = format.replace("{dd}", String.format("%02d", dd));
		format = format.replace("{YYYY}", String.format("%04d", yyyy));
		format = format.replace("{YY}", String.format("%04d", yyyy).substring(2, 4));
		format = format.replace("{MM}", String.format("%02d", mm));
		format = format.replace("{DD}", String.format("%02d", dd));
		
		// Find the placeholder for a decimal number with n digits
		// with the format "{Xnr}", "X" is the number of digits.
		Pattern p = Pattern.compile("\\{\\d*nr\\}");
		Matcher m = p.matcher(format);

		// replace "{Xnr}" with "%0Xd"
		if (m.find()) {
			nrExp = format.substring(m.start(), m.end());
			int nrExpLength = nrExp.length();
			nrExp = nrExpLength > 4 ? "%0" + nrExp.substring(1, nrExp.length() - 3) + "d" : "%d";
			format = m.replaceFirst(nrExp);
		}

		// Replace the "%0Xd" with the decimal number
		nextNr = String.format(format, nr);

		// Return the string with the next free document number
		return nextNr;
	}

	/**
	 * @return the editorID
	 */
	public final String getEditorID() {
		return editorID;
	}

	/**
	 * @param editorID the editorID to set
	 */
	public final void setEditorID(String editorID) {
		this.editorID = editorID;
	}
}
