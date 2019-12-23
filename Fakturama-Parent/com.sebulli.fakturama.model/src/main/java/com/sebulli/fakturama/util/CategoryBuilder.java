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
package com.sebulli.fakturama.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.model.AbstractCategory;

/**
 * Builder for all the categories used by {@link MigrationManager}. This builder builds category objects 
 * from old category string values. It is used for migration of old values.
 * 
 */
public class CategoryBuilder<T extends AbstractCategory> {

	@Inject
    private ILogger log;
    	
	/**
     * @param log
     */
	@Inject
    public CategoryBuilder(ILogger log) {
        this.log = log;
    }
	
	/**
	 * Builds a new Category Map. The Map has the old category as key and the new category (as class) as value.
	 * 
	 * @param oldCategoriesList
	 *            categories read from old database
	 * @param categoryClazz
	 *            class of new category
	 * @return map with new categories
	 */
	public Map<String, T> buildCategoryMap(List<String> oldCategoriesList, Class<T> categoryClazz) {
		Map<String, T> newCategories = new HashMap<>();
		for (String oldCategory : oldCategoriesList) {
			T newCategory = null;
			T parentCategory = null;
			String[] splittedCategories = oldCategory.split("/");
			for (String subCat : splittedCategories) {
				if(newCategories.containsKey(subCat)) {
					newCategory = newCategories.get(subCat);
				} else {
					newCategory = createCategory(subCat, categoryClazz, parentCategory);
					newCategories.put(subCat, newCategory);
				}
				parentCategory = newCategory;
			}
			newCategories.put(oldCategory, newCategory);
		}
		return newCategories;
	}
	
	/**
	 * Builds a (hierarchical) category from the given String.
	 *  
	 * @param category string with "/" separated categories
	 * @param categoryClazz the category class to process
	 * @return deepest category (rightmost category entry from the String)
	 */
	public T buildCategoryFromString(String category, Class<T> categoryClazz) {
	    // build a List with one entry
	    List<String> paramList = new ArrayList<>();
	    paramList.add(category);
	    Map<String, T> newCategories = buildCategoryMap(paramList, categoryClazz);
	    // what is the rightmost entry?
//	    String deepestCat = category.substring(StringUtils.lastIndexOf(category, "/") == -1 ? 0 : StringUtils.lastIndexOf(category, "/"));
	    return newCategories.get(category);
	}

	/**
	 * Creates a new Category.
	 * 
	 * @param categoryName the name of this category
	 * @param categoryClazz the category class (extends {@link AbstractCategory})
	 * @param parentCategory the parent category (or <code>null</code> if no parent is available)
	 * @return the new category
	 * 
	 * @throws InstantiationException of no category could be created
	 * @throws IllegalAccessException of no category could be created
	 */
	private T createCategory(String categoryName, Class<T> categoryClazz, T parentCategory) {
		T newCategory = null;
		try {
			newCategory = categoryClazz.newInstance();
			newCategory.setName(categoryName);
			newCategory.setParent(parentCategory);
			newCategory.setValidFrom(new Date());
		} catch (InstantiationException | IllegalAccessException e) {
			log.error(e, "couldn't create new Category of type " + categoryClazz.getName());
		}
		return newCategory;
	}
}
