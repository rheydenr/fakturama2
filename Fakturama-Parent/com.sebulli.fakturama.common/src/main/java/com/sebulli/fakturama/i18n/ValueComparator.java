package com.sebulli.fakturama.i18n;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;
import java.util.Map;

class ValueComparator implements Comparator<String> {

	private Map<String, String> base;
	private Collator collator;
	
    public ValueComparator(Map<String, String> base, Locale defaultLocale) {
        this.base = base;
        this.collator = Collator.getInstance(defaultLocale);
    }

    // Note: this comparator imposes orderings that are inconsistent with equals.    
    public int compare(String a, String b) {
        return collator.compare(base.get(a), base.get(b));
    }
}