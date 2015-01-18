package com.sebulli.fakturama.parts.widget;

import java.util.Map;

import org.eclipse.jface.viewers.LabelProvider;

/**
 * An <code>ILabelProvider</code> that assists in rendering labels.
 */
public class ComboBoxLabelProvider extends LabelProvider {

    /**
     * The values.
     */
    private Map<Integer, String> values;

    /**
     * @param values the possible label values that this 
     * <code>ILabelProvider</code> may return.
     */
    public ComboBoxLabelProvider(Map<Integer, String> values) {
        this.values = values;
    }

    /**
     * Returns the <code>String</code> that maps to the given 
     * <code>Integer</code>.
     * 
     * @param element an <code>Integer</code> object
     * @return a <code>String</code> from the provided values array, or the 
     * empty <code>String</code> 
     * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
     */
    public String getText(Object element) {
        String retval = "";
        if (element != null && element instanceof Integer) {
            retval = values.get(element);
        } else {
            retval = values.get(Integer.valueOf(0));
        }
        
        return retval;
    }
}
