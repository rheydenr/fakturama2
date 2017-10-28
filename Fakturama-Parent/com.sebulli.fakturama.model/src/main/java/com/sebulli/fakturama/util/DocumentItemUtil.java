/**
 * 
 */
package com.sebulli.fakturama.util;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;

import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.ItemType;
import com.sebulli.fakturama.model.Product;

/**
 *
 */
public class DocumentItemUtil {

    @Inject
    private IEclipseContext context;

    protected FakturamaModelFactory modelFactory =  FakturamaModelPackage.MODELFACTORY;
    private ProductUtil productUtil;
    
    @PostConstruct
    public void init() {
        this.productUtil = ContextInjectionFactory.make(ProductUtil.class, context);
    }

    /**
     * Creates a new {@link DocumentItem} from a given {@link Product} and DocumentType.
     * 
     * @param product
     * @param documentType 
     * @return
     */
    public DocumentItem from(Product product, DocumentType documentType) {
	    DocumentItem newItem = null;
	    if(product != null) {
	    	newItem = modelFactory.createDocumentItem();
		    newItem.setName(product.getName());
		    newItem.setProduct(product);
		    newItem.setItemNumber(product.getItemNumber());
		    newItem.setItemType(ItemType.POSITION);
		    newItem.setQuantity(documentType.getSign() * Double.valueOf(1));
		    newItem.setQuantityUnit(product.getQuantityUnit());
		    newItem.setDescription(product.getDescription());
		    newItem.setPrice(productUtil.getPriceByQuantity(product, newItem.getQuantity()));
		    newItem.setItemVat(product.getVat());
		    newItem.setPicture(product.getPicture());
		    newItem.setWeight(product.getWeight());
	    }
	    return newItem;
    }
    

}
