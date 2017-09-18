package com.sebulli.fakturama.model;

import java.util.Comparator;

import com.sebulli.fakturama.converter.CommonConverter;

public final class CategoryComparator<T extends AbstractCategory> implements Comparator<T> {
	
	@Override
	public int compare(T cat1, T cat2) {
		// oh no... the names could be equal in different branches,
		// therefore we have to compare with an another attribute
		int result = cat1.getName().compareTo(cat2.getName());
		if(result == 0) {
			result = CommonConverter.getCategoryName(cat1, "").compareTo(CommonConverter.getCategoryName(cat2, ""));
		}
		return result;
	}
}
