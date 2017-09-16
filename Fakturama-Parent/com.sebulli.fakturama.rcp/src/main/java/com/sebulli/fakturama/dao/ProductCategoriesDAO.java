package com.sebulli.fakturama.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.model.ProductCategory;
import com.sebulli.fakturama.model.ProductCategory_;

@Creatable
public class ProductCategoriesDAO extends AbstractCategoriesDAO<ProductCategory> {

    protected Class<ProductCategory> getEntityClass() {
    	return ProductCategory.class;
    }
    
    @Override
    public ProductCategory findByExample(ProductCategory example) {
    	ProductCategory result = null;
		// Two Categories are equal if they have the same name and the same parent string
    	
// von ReadAll()        query.setHierarchicalQueryClause(startWith, connectBy, orderSiblingsExpressions);

		String exampleCategory = CommonConverter.getCategoryName(example, "");
		exampleCategory = StringUtils.prependIfMissing(exampleCategory, "/", "/");
		List<ProductCategory> foundCandidates = findProductCategoryByName(exampleCategory);
		Optional<ProductCategory> s = foundCandidates.stream().filter(prodCategory -> prodCategory.getName().equalsIgnoreCase(example.getName())).findFirst();
		result = s.isPresent() ? s.get() : null;
		return result;
    }
    
    /**
     * Finds a {@link ProductCategory} by its name. Category in this case is a String separated by 
     * slashes, e.g. "/fooCat/barCat". Searching starts with the rightmost value
     * and then check the parent. 
     * 
     * @param productCategory the {@link ProductCategory} to search
     * @return {@link ProductCategory}
     */
    public List<ProductCategory> findProductCategoryByName(String productCategory) {
    	List<ProductCategory> result = new ArrayList<ProductCategory>();
        if(StringUtils.isNotEmpty(productCategory) && !StringUtils.equalsIgnoreCase(productCategory, "/")) {
        	CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
        	CriteriaQuery<ProductCategory> cq = cb.createQuery(getEntityClass());
        	Root<ProductCategory> rootEntity = cq.from(getEntityClass());
        	// extract the rightmost value
            String[] splittedCategories = productCategory.split("/");
        	String leafCategory = splittedCategories[splittedCategories.length - 1];       	
    		CriteriaQuery<ProductCategory> selectQuery = cq.select(rootEntity)
    		        .where(cb.and(
        		                cb.equal(rootEntity.get(ProductCategory_.name), leafCategory),
        		                cb.equal(rootEntity.get(ProductCategory_.deleted), false)));
            try {
                List<ProductCategory> tmpResultList = getEntityManager().createQuery(selectQuery).getResultList();
                // remove leading slash
                String testCat = StringUtils.removeStart(productCategory, "/");
                for (ProductCategory prodCat : tmpResultList) {
                    if(StringUtils.equals(CommonConverter.getCategoryName(prodCat, ""), testCat)) {
                        result.add(prodCat);
                    }
                }
            }
            catch (NoResultException nre) {
                // no result means we return a null value 
            }
        }
        return result;
    }
    

    /**
     * Find a {@link ProductCategory} by its name. If one of the part categories doesn't exist we create it 
     * (if withPersistOption is set).
     * 
     * @param testCat the category to find
     * @param withPersistOption persist a (part) category if it doesn't exist
     * @return found category
     */
    public ProductCategory getCategory(String testCat, boolean withPersistOption) {
        // to find the complete category we have to start with the topmost category
        // and then lookup each of the child categories in the given path
        String[] splittedCategories = testCat.split("/");
        ProductCategory parentCategory = null;
        String category = "";
        try {
        	
            for (int i = 0; i < splittedCategories.length; i++) {
            	if(StringUtils.isBlank(splittedCategories[i])) {
            		continue;
            	}
                category += "/" + splittedCategories[i];
                List<ProductCategory> foundCategories = findProductCategoryByName(category);
                ProductCategory searchCat = null;
                if(!foundCategories.isEmpty()) {
                	searchCat = foundCategories.get(0);
                }
                if (searchCat == null) {
                    // not found? Then create a new one.
                    ProductCategory newCategory = new ProductCategory();
                    newCategory.setName(splittedCategories[i]);
                    newCategory.setParent(parentCategory);
//                    save(newCategory);
                    searchCat = newCategory;
                }
                // save the parent and then dive deeper...
                parentCategory = searchCat;
            } 
            if(parentCategory != null && !getEntityManager().contains(parentCategory)) {
                parentCategory = save(parentCategory);
            }
        }
        catch (FakturamaStoringException e) {
            getLog().error(e);
        }
        return parentCategory;
    }

}
