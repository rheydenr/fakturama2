/**
 * 
 */
package com.sebulli.fakturama.util;

import java.text.ChoiceFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
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
		    // if quantity unit has a special meaning we have to consider that
		    newItem.setQuantityUnit(getProductQuantityUnit(product, newItem.getQuantity()));
		    newItem.setDescription(product.getDescription());
		    newItem.setPrice(productUtil.getPriceByQuantity(product, newItem.getQuantity()));
		    newItem.setItemVat(product.getVat());
		    newItem.setPicture(product.getPicture());
		    newItem.setWeight(product.getWeight());
		    newItem.setGtin(product.getGtin());
	    }
	    return newItem;
    }

    /**
     * Creates the correct spelled form of the quantity unit if configured. The quantity unit has to be in the following form:
     * <pre>
     * n#name1|n#name2|n#name3
     * </pre>
     * 
     * Example:
     * <pre>
     * 1#Stück|2#Stücke|100#Stück|101#Stücke
     * </pre>
     * 
     * I.e., if you have a quantity of "1" the quantity unit is "Stück". For 2 and up to 99 articles it is "Stücke".
     * 
     * @param product
     * @return
     */
	public String getProductQuantityUnit(Product product, Double quantity) {
		String retval = product.getQuantityUnit();
		if (StringUtils.defaultString(retval).contains("|")) {
			String[] choices = retval.split("\\|");
			List<Double> limitList = new ArrayList<>();
			List<String> names = new ArrayList<>();
			for (String string : choices) {
				String[] tmpSplit = string.split("#");
				limitList.add(Double.valueOf(tmpSplit[0]));
				names.add(tmpSplit[1]);
			}
			ChoiceFormat form3 = new ChoiceFormat(ArrayUtils.toPrimitive(limitList.toArray(new Double[] {})),
					names.toArray(new String[] {}));
			retval = form3.format(quantity);
		}
		return retval;
	}    
}
