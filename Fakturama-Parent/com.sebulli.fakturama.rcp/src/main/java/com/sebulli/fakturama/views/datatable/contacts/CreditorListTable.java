/**
 * 
 */
package com.sebulli.fakturama.views.datatable.contacts;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.CreditorsDAO;
import com.sebulli.fakturama.model.Address_;
import com.sebulli.fakturama.model.Creditor;
import com.sebulli.fakturama.model.Creditor_;
import com.sebulli.fakturama.parts.ContactEditor;
import com.sebulli.fakturama.parts.CreditorEditor;
import com.sebulli.fakturama.parts.Editor;

/**
 * View with the table of all contacts
 * 
 */
public class CreditorListTable extends ContactListTable<Creditor> {

    // ID of this view
    public static final String ID = "fakturama.views.creditorTable";

    private static final String POPUP_ID = "com.sebulli.fakturama.creditorlist.popup";
    public static final String SELECTED_CREDITOR_ID = "fakturama.creditorlist.selectedcreditorid";

    @Inject
    private CreditorsDAO creditorDAO;

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#getTableId()
     */
    @Override
    public String getTableId() {
        return ID;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#getEditorId()
     */
    @Override
    protected String getEditorId() {
        return ContactEditor.ID;
    }
    
    @Inject @Optional
    public void handleRefreshEvent(@EventTopic(CreditorEditor.EDITOR_ID) String message) {
    	if(StringUtils.equals(message, Editor.UPDATE_EVENT)) {
	        sync.syncExec(() -> top.setRedraw(false));
	        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
	        GlazedLists.replaceAll(contactListData, getListData(true), false);
	        GlazedLists.replaceAll(categories, GlazedLists.eventList(contactCategoriesDAO.findAll(true)), false);
	        sync.syncExec(() -> top.setRedraw(true));
    	}
    }

    protected String getPopupId() {
        return POPUP_ID;
    }

    @Override
    protected EventList<Creditor> getListData(boolean forceRead) {
        return GlazedLists.eventList(creditorDAO.findAll(forceRead));
    }

    @Override
    protected MatcherEditor<Creditor> createTextWidgetMatcherEditor() {
        /*
        searchColumns[0] = "nr";
        searchColumns[1] = "firstname";
        searchColumns[2] = "name";
        searchColumns[3] = "company";
        searchColumns[4] = "zip";
        searchColumns[5] = "city";
 */
        return new TextWidgetMatcherEditor<Creditor>(searchText, 
                GlazedLists.textFilterator(Creditor.class, 
                        Creditor_.customerNumber.getName(),
                        Creditor_.firstName.getName(),
                        Creditor_.name.getName(),
                        Creditor_.company.getName(),
                        Creditor_.address.getName() + "." + Address_.zip.getName(),
                        Creditor_.address.getName() + "." + Address_.city.getName()
                        ));
    }

    @Override
    protected AbstractDAO<Creditor> getEntityDAO() {
        return creditorDAO;
    }
}
