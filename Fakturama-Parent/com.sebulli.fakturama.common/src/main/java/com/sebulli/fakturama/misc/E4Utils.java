package com.sebulli.fakturama.misc;

import java.text.MessageFormat;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.translation.TranslationService;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;

public class E4Utils {
 
	public static final TranslationService TRANSLATIONS = getTopContext().get(TranslationService.class);
 
	/**
	 * Finds the top Eclipse context.
	 * @return the eclipse context.
	 */
	public static IEclipseContext getTopContext() {
		return E4Workbench.getServiceContext();
 
	}
 
	/**
	 * Translate the given key.
	 * @param key key to translate
	 * @param contributorUri the contributor URI of the translation
	 * @param args variable replacements (will replace {0}, {1},... placeholders)
	 */
	public static String translate(final String key, final String contributorUri, Object... args) {
		if (key == null) return "";
		String msgKey = key;
		if (msgKey.charAt(0) != '%') msgKey = '%'+msgKey;
		String rc = TRANSLATIONS.translate(msgKey, contributorUri);
		if ((args == null) || (args.length == 0)) return rc;
		return MessageFormat.format(rc, args);
	}
}