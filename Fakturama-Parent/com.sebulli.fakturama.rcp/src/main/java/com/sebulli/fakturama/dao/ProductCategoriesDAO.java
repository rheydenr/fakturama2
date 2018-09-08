package com.sebulli.fakturama.dao;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Creatable;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.ProductCategory;
import com.sebulli.fakturama.model.Product_;

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
//		List<ProductCategory> foundCandidates = findProductCategoryByName(exampleCategory);
//		Optional<ProductCategory> s = foundCandidates.stream().filter(prodCategory -> prodCategory.getName().equalsIgnoreCase(example.getName())).findFirst();
//		result = s.isPresent() ? s.get() : null;
		result = findCategoryByName(exampleCategory);
		return result;
    }
    
    @Override
    protected void updateObsoleteEntities(ProductCategory oldCat) {
		// at first update all (deleted) entries in this category and set the category entry to null
		CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
		CriteriaUpdate<Product> updateProducts = getEntityManager().getCriteriaBuilder().createCriteriaUpdate(Product.class);
        Root<Product> root = updateProducts.from(Product.class);	        
		updateProducts.set(root.get(Product_.categories),(ProductCategory) null);
		updateProducts.where(cb.and(
				cb.equal(root.get(Product_.categories), oldCat),
				cb.isTrue(root.get(Product_.deleted))));
		getEntityManager().createQuery(updateProducts).executeUpdate();
    }
}
