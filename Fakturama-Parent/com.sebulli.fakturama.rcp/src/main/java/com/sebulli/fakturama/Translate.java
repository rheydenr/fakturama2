package com.sebulli.fakturama;

import com.sebulli.fakturama.misc.E4Utils;

/**
 * Translation Helper for all localized strings. Translation is read from bundle.properties
 * (located in OSGI-INF). This is the default location. 
 * 
 * @author R. Heydenreich
 *
 */
public class Translate {
 
	public static final String SYMBOLIC_NAME = "com.sebulli.fakturama.rcp";
	public static final String CONTRIBUTOR_URI = "platform:/plugin/"+SYMBOLIC_NAME;
 
	/**
	 * Translate the given key.
	 * @param key key to translate
	 * @param args variable replacements (will replace {0}, {1},... placeholders)
	 * @return translated value
	 */
	public static String _(String key, Object... args) {
		return E4Utils.translate(key, CONTRIBUTOR_URI, args);
	}
}