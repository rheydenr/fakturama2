package com.sebulli.fakturama.views.datatable.tree.ui;

import java.util.function.Function;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.DummyStringCategory;
import com.sebulli.fakturama.util.CategoryBuilder;
import com.sebulli.fakturama.views.datatable.tree.model.TreeObject;

/**
 * This class provides the labels for the category tree
 *
 */
public class TreeCategoryLabelProvider extends CellLabelProvider {

    @Inject
    private IEclipseContext context;

    private Function<DummyStringCategory, String> categorySummarizer; 
    
    public TreeCategoryLabelProvider(Function<DummyStringCategory, String> categorySummarizer) {
        this.categorySummarizer = categorySummarizer;
    }
    
    public TreeCategoryLabelProvider() {
        this(null);
    }

    public String getText(Object obj) {

        // Display the localized list names.
        //			if (this.rheNatTable.natTable instanceof NatTable)
        //				return "(localized) " + obj.toString();
        //			else
        return obj.toString();
    }
    
    @Override
    public String getToolTipText(Object element) {
        TreeObject treeObject = (TreeObject)element;
        DocumentType docType = treeObject.getDocType(); // INVOICE
        if(categorySummarizer != null && docType == DocumentType.INVOICE) {
            @SuppressWarnings("unchecked")
            CategoryBuilder<DummyStringCategory> cb = ContextInjectionFactory.make(CategoryBuilder.class, context);
            DummyStringCategory fullCategory = cb.buildCategoryFromString(treeObject.getFullPathName(), DummyStringCategory.class);
            fullCategory.setDocType(docType);
            return categorySummarizer.apply(fullCategory);
        }
        
    	return treeObject.getToolTip();
    }

    @Override
    public void update(ViewerCell cell) {
        cell.setText(getText(cell.getElement()));
        //			cell.setImage(getImage(cell.getElement()));
    }

}