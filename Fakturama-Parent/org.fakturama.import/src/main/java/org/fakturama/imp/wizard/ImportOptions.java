/**
 * 
 */
package org.fakturama.imp.wizard;

import javax.inject.Inject;

import org.eclipse.jface.dialogs.IDialogSettings;

/**
 *
 */
public class ImportOptions {
    public static final String IMPORT_SETTING_OPTIONS = "org.fakturama.import.options";
    public static final String PICTURE_BASE_PATH = "import.picture.base.path";
    public static final String IMPORT_CSV_UPDATEEXISTING = "import.csv.updateexisting";
    public static final String IMPORT_CSV_UPDATEWITHEMPTYVALUES = "import.csv.withemptyvalues";
    public static final String IMPORT_CSV_SEPARATOR = "import.csv.separator";
    public static final String IMPORT_CSV_QUOTECHAR = "import.csv.quotechar";
    public static final String IMPORT_CSV_FILENAME = "import.csv.filename";

	// initialize with some predefined values
	private Boolean updateExisting = false;
	private Boolean updateWithEmptyValues = false;
	private String quoteChar = "\"";
	private String separator = ";";
	private String basePath = "", csvFile = "";

	public ImportOptions() {
		
	}

	public ImportOptions(Boolean updateExisting, Boolean updateWithEmptyValues, String quoteChar, String separator) {
		this.updateExisting = updateExisting;
		this.updateWithEmptyValues = updateWithEmptyValues;
		this.quoteChar = quoteChar;
		this.separator = separator;
	}
    
	@Inject
    public ImportOptions(IDialogSettings settings) {
		if(settings != null) {
		    IDialogSettings importSettings = settings.getSection(IMPORT_SETTING_OPTIONS);
		    if(importSettings == null) {
		        settings.addNewSection(IMPORT_SETTING_OPTIONS);
		    }
	        if (importSettings.get(PICTURE_BASE_PATH) != null) {
	            basePath = importSettings.get(PICTURE_BASE_PATH);
	        }
	
	        if (importSettings.get(IMPORT_CSV_SEPARATOR) != null) {
	            separator = importSettings.get(IMPORT_CSV_SEPARATOR);
	        }
	
	        if (importSettings.get(IMPORT_CSV_QUOTECHAR) != null) {
	            quoteChar = importSettings.get(IMPORT_CSV_QUOTECHAR);
	        }
	
	        if (importSettings.get(IMPORT_CSV_FILENAME) != null) {
	            csvFile = importSettings.get(IMPORT_CSV_FILENAME);
	        }
	        
	        if (importSettings.get(IMPORT_CSV_UPDATEEXISTING) != null) {
	            updateExisting = importSettings.getBoolean(IMPORT_CSV_UPDATEEXISTING);
	        }
	        
	        if (settings.get(IMPORT_CSV_UPDATEWITHEMPTYVALUES) != null) {
	            updateWithEmptyValues = settings.getBoolean(IMPORT_CSV_UPDATEWITHEMPTYVALUES);
	        }
		}
    }
	/**
	 * @return the updateExisting
	 */
	public final Boolean getUpdateExisting() {
		return updateExisting;
	}


	/**
	 * @param updateExisting the updateExisting to set
	 */
	public final void setUpdateExisting(Boolean updateExisting) {
		this.updateExisting = updateExisting;
	}


	/**
	 * @return the updateWithEmptyValues
	 */
	public final Boolean getUpdateWithEmptyValues() {
		return updateWithEmptyValues;
	}


	/**
	 * @param updateWithEmptyValues the updateWithEmptyValues to set
	 */
	public final void setUpdateWithEmptyValues(Boolean updateWithEmptyValues) {
		this.updateWithEmptyValues = updateWithEmptyValues;
	}


	/**
	 * @return the quoteChar
	 */
	public final String getQuoteChar() {
		return quoteChar;
	}


	/**
	 * @param quoteChar the quoteChar to set
	 */
	public final void setQuoteChar(String quoteChar) {
		this.quoteChar = quoteChar;
	}


	/**
	 * @return the separator
	 */
	public final String getSeparator() {
		return separator;
	}


	/**
	 * @param separator the separator to set
	 */
	public final void setSeparator(String separator) {
		this.separator = separator;
	}

	/**
	 * @return the basePath
	 */
	public String getBasePath() {
		return basePath;
	}

	/**
	 * @param basePath the basePath to set
	 */
	public void setBasePath(String basePath) {
		this.basePath = basePath;
	}

    /**
     * @return the csvfile
     */
    public String getCsvFile() {
        return csvFile;
    }

    /**
     * @param csvfile the csvfile to set
     */
    public void setCsvFile(String csvfile) {
        this.csvFile = csvfile;
    }
}
