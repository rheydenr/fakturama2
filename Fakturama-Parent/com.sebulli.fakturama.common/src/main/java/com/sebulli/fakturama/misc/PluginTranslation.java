package com.sebulli.fakturama.misc;
public class PluginTranslation {
 
	public static final String SYMBOLIC_NAME = "com.sebulli.fakturama.misc";
	public static final String CONTRIBUTOR_URI = "platform:/plugin/"+SYMBOLIC_NAME;
 
	/**
	 * Translate the given key.
	 * @param key key to translate
	 * @param args variable replacements (will replace {0}, {1},... placeholders)
	 * @return translated value
	 */
	public static String translate(String key, Object... args) {
		return E4Utils.translate(key, CONTRIBUTOR_URI, args);
	}
}