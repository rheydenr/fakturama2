/**
 * 
 */
package org.fakturama.imp.wizard;

/**
 *
 */
public class ImportOptions {
	private Boolean updateExisting;
	private Boolean updateWithEmptyValues;
	private String quoteChar;
	private String separator;

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


	public ImportOptions(Boolean updateExisting, Boolean updateWithEmptyValues, String quoteChar, String separator) {
		super();
		this.updateExisting = updateExisting;
		this.updateWithEmptyValues = updateWithEmptyValues;
		this.quoteChar = quoteChar;
		this.separator = separator;
	}


	public static class ImportOptionsBuilder {
		private Boolean updateExisting;
		private Boolean updateWithEmptyValues;
		private String quoteChar;
		private String separator;

		public ImportOptionsBuilder withUpdateExisting(Boolean updateExisting) {
			this.updateExisting = updateExisting;
			return this;
		}

		public ImportOptionsBuilder withUpdateWithEmptyValues(Boolean updateWithEmptyValues) {
			this.updateWithEmptyValues = updateWithEmptyValues;
			return this;
		}

		public ImportOptionsBuilder withQuoteChar(String quoteChar) {
			this.quoteChar = quoteChar;
			return this;
		}

		public ImportOptionsBuilder withSeparator(String separator) {
			this.separator = separator;
			return this;
		}

		public ImportOptions build() {
			return new ImportOptions(updateExisting, updateWithEmptyValues, quoteChar, separator);
		}
	}


	public static ImportOptionsBuilder importOptions() {
		return new ImportOptionsBuilder();
	}

}
