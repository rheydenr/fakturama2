/**
 * 
 */
package org.fakturama.export.einvoice;

/**
 * Abbildung der Levels für die einzelnen ZUGFeRD-Ausbaustufen
 * 
 * @author R. Heydenreich
 * 
 */
public enum ConformanceLevel {

	BASIC("urn:ferd:invoice:1.0:basic"), COMFORT("urn:ferd:invoice:1.0:comfort"), EXTENDED("urn:ferd:invoice:1.0:extended");

	private String urn;

	/**
	 * @param urn
	 */
	private ConformanceLevel(String urn) {
		this.urn = urn;
	}

	/**
	 * @return the urn
	 */
	public final String getUrn() {
		return urn;
	}

}
