package com.sebulli.fakturama.views.datatable.tree.ui;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;

import com.sebulli.fakturama.views.datatable.tree.model.TreeObject;

/**
 * This class provides the labels for the category tree
 *
 */
public class TreeCategoryLabelProvider extends CellLabelProvider {

    public String getText(Object obj) {

        // Display the localized list names.
        //			if (this.rheNatTable.natTable instanceof NatTable)
        //				return "(localized) " + obj.toString();
        //			else
        return obj.toString();
    }
    
    @Override
    public String getToolTipText(Object element) {
    	return ((TreeObject)element).getToolTip();
    }

    @Override
    public void update(ViewerCell cell) {
        cell.setText(getText(cell.getElement()));
        //			cell.setImage(getImage(cell.getElement()));
    }

}