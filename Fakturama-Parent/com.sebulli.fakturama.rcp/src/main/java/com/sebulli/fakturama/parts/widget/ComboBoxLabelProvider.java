package com.sebulli.fakturama.parts.widget;

import java.util.Map;

import org.eclipse.jface.viewers.LabelProvider;

/**
 * An <code>ILabelProvider</code> that assists in rendering labels.
 */
public class ComboBoxLabelProvider<K, V> extends LabelProvider {

    /**
     * The values.
     */
    private Map<K, V> values;

    /**
     * @param values the possible label values that this 
     * <code>ILabelProvider</code> may return.
     */
    public ComboBoxLabelProvider(Map<K, V> values) {
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
        Object index;
        if (element instanceof Integer) {
            index = element != null ? element : Integer.valueOf(0);
            retval = (String) values.get(index);
        } else if(element instanceof Short) {
            index = element != null ? element : Short.valueOf((short)0);
            retval = (String) values.get(index);
        }
        return retval;
    }
}
