/**
 * 
 */
package com.sebulli.fakturama.parts.widget.labelprovider;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.nebula.widgets.opal.multichoice.MultiChoiceLabelProvider;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.ContactType;

/**
 * {@link LabelProvider} for {@link ContactType}s.
 *
 */
public class ContactTypeLabelProvider implements MultiChoiceLabelProvider {
    
    private Messages msg;
    
    /**
     * @param msg
     */
    public ContactTypeLabelProvider(Messages msg) {
        this.msg = msg;
    }

    @Override
    public String getText(Object element) {
        String retval = "";
        if(element instanceof ContactType) {
        	ContactType type = (ContactType)element;
            switch (type) {
            case BILLING:
                retval = "Invoice";
                break;
            case DELIVERY:
                retval = "Delivery";
                break;
            default:
                retval = "invalid";
                break;
            }
        }
        return retval;
    }

}
