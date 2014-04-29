/*
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2013 Ralf Heydenreich
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Ralf Heydenreich - initial API and implementation
 */
package com.sebulli.fakturama.migration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sebulli.fakturama.model.AbstractCategory;

/**
 * Builder for all the categories used by {@link MigrationManager}.
 * 
 * @author Ralf Heydenreich
 * 
 */
public class CategoryBuilder<T extends AbstractCategory> {

	/**
	 * Builds a new Category Map
	 * 
	 * @param oldCategoriesList
	 *            categories read from old database
	 * @param categoryClazz
	 *            class of new category
	 * @return map with new categories
	 */
	public Map<String, T> buildCategoryMap(List<String> oldCategoriesList, Class<T> categoryClazz) {
		Map<String, T> newCategories = new HashMap<String, T>();
		for (String oldCategory : oldCategoriesList) {
			T newCategory = null;
			try {
				T parentCategory = null;
				String[] splittedCategories = oldCategory.split("/");
				for (String string : splittedCategories) {
					if(newCategories.containsKey(string)) {
						newCategory = newCategories.get(string);
					} else {
						newCategory = createCategory(string, categoryClazz, parentCategory);
					}
					parentCategory = newCategory;
				}
				newCategories.put(oldCategory, newCategory);
			}
			catch (InstantiationException | IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		return newCategories;
	}

	private T createCategory(String oldCategory, Class<T> categoryClazz, T parentCategory) throws InstantiationException, IllegalAccessException {
		T newCategory = categoryClazz.newInstance();
		newCategory.setName(oldCategory);
		newCategory.setParent(parentCategory);
		return newCategory;

	}

}
