/**
 * 
 */
package org.fakturama.imp.wizard;

/**
 *
 */
public class ImportOptions {
	
	// initialize with some predefined values
	private Boolean updateExisting = false;
	private Boolean updateWithEmptyValues = false;
	private String quoteChar = "\"";
	private String separator = ";";
	private String basePath = "";

	public ImportOptions() {
		
	}

	public ImportOptions(Boolean updateExisting, Boolean updateWithEmptyValues, String quoteChar, String separator) {
		this.updateExisting = updateExisting;
		this.updateWithEmptyValues = updateWithEmptyValues;
		this.quoteChar = quoteChar;
		this.separator = separator;
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
}
