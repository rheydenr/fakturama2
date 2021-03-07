package com.sebulli.fakturama.misc;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Properties;

/**
 * @author gschrick
 * 
 * Class to handle Java properties embedded in a String
 *
 */
public class EmbeddedProperties {
	public static final String MARKER_START = "<[EmbeddedProperties[";
	public static final String MARKER_END = "]]>";
	public static final String MARKER_VALUE_SPLIT = " - ";
	// property keys
	public static final String PROPERTY_INFO = "info";
	public static final String PROPERTY_EINVOICE = "einvoice";
    public static final String PROPERTY_EINVOICE_NOTICE = "einvoice.notice";
    public static final String PROPERTY_EINVOICE_MAILTO = "einvoice.mailto";
    public static final String PROPERTY_EINVOICE_URL = "einvoice.url";
    public static final String PROPERTY_EINVOICE_BT10 = "einvoice.BT10"; // Leitweg-ID
    public static final String PROPERTY_EINVOICE_BT11 = "einvoice.BT11"; // Projektreferenz
    public static final String PROPERTY_EINVOICE_BT12 = "einvoice.BT12"; // Vertragsnummer
    public static final String PROPERTY_EINVOICE_BT13 = "einvoice.BT13"; // BestellNr.
    public static final String PROPERTY_EINVOICE_BUYER_ID = "einvoice.buyer.id"; // Buyer ID
    public static final String PROPERTY_EINVOICE_BUYER_GLOBALID = "einvoice.buyer.globalid"; // Buyer GlobalID
    public static final String PROPERTY_EINVOICE_BUYER_GLOBALID_SCHEMEID = "einvoice.buyer.globalid.schemeid"; // Buyer GlobalID@schemeID
	// property value codes
	public static final String VALUE_XRECHNUNG = "XRECHNUNG";
	public static final String VALUE_FACTURX_COMFORT = "FACTURX_COMFORT";
	public static final String VALUE_ZUGFERD_V2_COMFORT = "ZUGFERD_V2_COMFORT";
	public static final String VALUE_ZUGFERD_V2_EN16931 = "ZUGFERD_V2_EN16931";
	public static final String VALUE_ZUGFERD_V1_COMFORT = "ZUGFERD_V1_COMFORT";
	public static final String VALUE_NULL = "{null}";
	public static final String VALUE_FIELD_CUSTREF = "{custref}";
	public static final String VALUE_FIELD_CUSTREF_LEFT = "{custref:left}";
	public static final String VALUE_FIELD_ORDER_NAME = "{order.name}";

	public static String left(String source) {
		if (source != null) {
			int pos = source.indexOf(MARKER_VALUE_SPLIT);
			if (pos > 0)
	    		return source.substring(0, pos);
		}
		return source;
	}


	
	private Properties theProperties;


	public EmbeddedProperties() {
		theProperties = new Properties();
	}

	public EmbeddedProperties(String source) {
		this();
		read(source);
	}


	public void clear() {
		theProperties.clear();
	}
	
	public String getProperty(String key) {
		return theProperties.getProperty(key);
	}
	public String getProperty(String key, String valueIfNull, String valueIfEmpty) {
		String value = theProperties.getProperty(key);
		if (value == null)
			return valueIfNull;
		else if (value.trim().length() == 0)
			return valueIfEmpty;
		else
			return value;
	}

	/**
	 * sets the property with the given key (must not be null)
	 * if value is null or empty property is removed
	 * @param key
	 * @param value
	 */
	public void setProperty(String key, String value) {
		if (value != null && value.trim().length() > 0) {
			theProperties.setProperty(key, value);
		} else {
			theProperties.remove(key);
		}
	}
	
	public boolean isEmpty() {
		return theProperties.isEmpty();
	}

	/**
	 * Reads java.util.Properties from the source String with default marker
	 * 
	 * @param source
	 * @return
	 */
	public void read(String source) {
		read(source, MARKER_START, MARKER_END, false);
	}

	/**
	 * Reads java.util.Properties from the source String optionally from a separated
	 * section in the string between startMarker and endMarker.
	 * If startMarker is null/empty start is begin of source
	 * If startMarker is not found means: no properties embedded to be read.
	 * If endMarker is null or not found, end is end of source.
	 * If extract is true the source's content w/o the embedded properties block is returned.
	 * 
	 * @param source
	 * @param startMarker
	 * @param endMarker
	 * @param extract
	 * @return if extract is true see above, else null
	 */
	public String read(String source, String startMarker, String endMarker, boolean extract) {
		theProperties.clear();
		if (source != null && source.length() > 0) {
			int startPos = 0, startReadPos = 0;
			int endPos = source.length();
			int endReadPos = endPos;
			if (startMarker != null && startMarker.length() > 0) {
				startPos = source.indexOf(startMarker);
				if (startPos >= 0) {
					startReadPos = startPos + startMarker.length();
					if (endMarker != null && endMarker.length() > 0) {
						endReadPos = source.indexOf(endMarker, startReadPos);
						// if endMarker was given but not found
						if (endReadPos < 0) {
							endReadPos = source.length();
						} else {
							endPos = endReadPos + endMarker.length();
						}
					}
				} else {
					startReadPos = -1;
				}
			}

			if (startReadPos >= 0 && endReadPos > startReadPos) {
				try {
					theProperties.load(new StringReader(source.substring(startReadPos, endReadPos)));
				} catch (IOException e) {
					;
				}
				if (extract) {
					StringBuffer outSB = new StringBuffer();
					if (startPos > 0)
						outSB.append(source.substring(0, startPos - 1));
					if (source.length() > endPos)
						outSB.append(source.substring(endPos));
					return outSB.toString().trim();
				}
			} else if (extract) {
				return source;
			}
		}
		return null;
	}
	
	/**
	 * shortcut for read(source, MARKER_START, MARKER_END, true)
	 * @param source
	 * @return
	 */
	public String extract(String source) {
		return read(source, MARKER_START, MARKER_END, true);
	}
	
	/**
	 * appends the embedded properties block to source
	 * but only if there are properties set
	 * @param source
	 * @return source with properties (if any)
	 */
	public String appendTo(String source) {
		// anything to write/add?
		if (theProperties.size() > 0) {
			StringWriter writer = new StringWriter();
			try {
				theProperties.store(writer, null);
			} catch (IOException e) {
// TODO GS/ this should somehow be reported (msg or log)
//				e.printStackTrace();
			}
			StringBuffer propertiesSB = writer.getBuffer();
			StringBuffer outSB = new StringBuffer();
			if (source != null && source.length() > 0)
				outSB.append(source);
			outSB.append("\n");
			outSB.append(MARKER_START);
			outSB.append("\n");
			outSB.append(propertiesSB);
			outSB.append(MARKER_END);
			return outSB.toString();
		} else {
			return source;
		}
	}
	
}
