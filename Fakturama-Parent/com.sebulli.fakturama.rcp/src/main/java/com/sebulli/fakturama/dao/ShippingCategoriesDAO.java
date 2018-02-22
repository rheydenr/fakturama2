package com.sebulli.fakturama.dao;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.model.ShippingCategory;

@Creatable
public class ShippingCategoriesDAO extends AbstractCategoriesDAO<ShippingCategory> {

    protected Class<ShippingCategory> getEntityClass() {
    	return ShippingCategory.class;
    }
    
//    
//    /**
//     * Find a {@link ShippingCategory} by its name. If one of the part categories doesn't exist we create it 
//     * (if withPersistOption is set).
//     * 
//     * @param testCat the category to find
//     * @param withPersistOption persist a (part) category if it doesn't exist
//     * @return found category
//     */
//    public ShippingCategory getCategory(String testCat, boolean withPersistOption) {
//        // to find the complete category we have to start with the topmost category
//        // and then lookup each of the child categories in the given path
//        String[] splittedCategories = testCat.split("/");
//        ShippingCategory parentCategory = null;
//        String category = "";
//        try {
//            for (int i = 0; i < splittedCategories.length; i++) {
//            	if(StringUtils.isBlank(splittedCategories[i])) {
//            		continue;
//            	}
//                category += "/" + splittedCategories[i];
////                ShippingCategory searchCat = findShippingCategoryByName(category);
//                ShippingCategory searchCat = findCategoryByName(category);
//                if (searchCat == null) {
//                    // not found? Then create a new one.
//                    ShippingCategory newCategory = new ShippingCategory();
//                    newCategory.setName(splittedCategories[i]);
//                    newCategory.setParent(parentCategory);
//                    newCategory = save(newCategory);
//                    searchCat = newCategory;
//                }
//                // save the parent and then dive deeper...
//                parentCategory = searchCat;
//            } 
//        }
//        catch (FakturamaStoringException e) {
//            getLog().error(e);
//        }
//        return parentCategory;
//    }

}
