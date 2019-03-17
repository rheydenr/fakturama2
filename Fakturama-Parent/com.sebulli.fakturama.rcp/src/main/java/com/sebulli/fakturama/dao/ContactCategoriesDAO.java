package com.sebulli.fakturama.dao;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.model.Contact_;

@Creatable
public class ContactCategoriesDAO extends AbstractCategoriesDAO<ContactCategory> {

    protected Class<ContactCategory> getEntityClass() {
    	return ContactCategory.class;
    }
    
    @Override
    protected void updateObsoleteEntities(ContactCategory oldCat) {
		// at first update all (deleted) entries in this category and set the category entry to null
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaUpdate<Contact> updateContacts = getEntityManager().getCriteriaBuilder().createCriteriaUpdate(Contact.class);
        Root<Contact> root = updateContacts.from(Contact.class);	        
		updateContacts.set(root.get(Contact_.categories),(ContactCategory) null);
		updateContacts.where(cb.and(
				cb.equal(root.get(Contact_.categories), oldCat),
				cb.isTrue(root.get(Contact_.deleted))));
		getEntityManager().createQuery(updateContacts).executeUpdate();
    }

//    /**
//     * Find a {@link ContactCategory} by its name. If one of the part categories doesn't exist we create it 
//     * (if {@code withPersistOption} is set to <code>true</code>).
//     * 
//     * @param testCat the category to find
//     * @param withPersistOption persist a (part) category if it doesn't exist
//     * @return found category
//     */
//    public ContactCategory getCategory(String testCat, boolean withPersistOption) {
//        // to find the complete category we have to start with the topmost category
//        // and then lookup each of the child categories in the given path
//        String[] splittedCategories = testCat.split("/");
//        ContactCategory parentCategory = null;
//        String category = "";
//        try {
//            for (int i = 0; i < splittedCategories.length; i++) {
//            	if(StringUtils.isBlank(splittedCategories[i])) {
//            		continue;
//            	}
//                category += "/" + splittedCategories[i];
//                ContactCategory searchCat = findCategoryByName(category);
//                if (searchCat == null) {
//                    // not found? Then create a new one.
//                    ContactCategory newCategory = new ContactCategory();
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
