/**
 * 
 */
package com.sebulli.fakturama.views.datatable.contacts;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.DebitorsDAO;
import com.sebulli.fakturama.model.Address_;
import com.sebulli.fakturama.model.Debitor;
import com.sebulli.fakturama.model.Debitor_;
import com.sebulli.fakturama.parts.DebitorEditor;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

/**
 * View with the table of all contacts
 * 
 */
public class DebitorListTable extends ContactListTable<Debitor> {

    // ID of this view
    public static final String ID = "fakturama.views.debitorTable";

    private static final String POPUP_ID = "com.sebulli.fakturama.debitorlist.popup";
    public static final String SELECTED_CREDITOR_ID = "fakturama.debitorlist.selecteddebitorid";
    
    @Inject
    private DebitorsDAO debitorDAO;

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#getTableId()
     */
    @Override
    public String getTableId() {
        return ID;
    }
    
    @Override
    protected String getEditorTypeId() {
        return DebitorEditor.EDITOR_ID;
    }
    
    
    @Inject @Optional
    public void handleRefreshEvent(@UIEventTopic(DebitorEditor.EDITOR_ID) String message) {
    	super.handleRefreshEvent(message);
    }

    protected String getPopupId() {
        return POPUP_ID;
    }

    @Override
    protected EventList<Debitor> getListData(boolean forceRead) {
        return GlazedLists.eventList(debitorDAO.findForListView());
    }

    @Override
    protected MatcherEditor<Debitor> createTextWidgetMatcherEditor() {
        /*
        searchColumns[0] = "nr";
        searchColumns[1] = "firstname";
        searchColumns[2] = "name";
        searchColumns[3] = "company";
        searchColumns[4] = "zip";
        searchColumns[5] = "city";
 */
        return new TextWidgetMatcherEditor<Debitor>(searchText.getTextControl(), 
                GlazedLists.textFilterator(Debitor.class, 
                        Debitor_.customerNumber.getName(),
                        Debitor_.firstName.getName(),
                        Debitor_.name.getName(),
                        Debitor_.company.getName(),
                        Debitor_.addresses.getName() + "." + Address_.zip.getName(),
                        Debitor_.addresses.getName() + "." + Address_.city.getName()
                        ));
    }

    @Override
    protected AbstractDAO<Debitor> getEntityDAO() {
        return debitorDAO;
    }
    
    @Override
    protected String getEditorId() {
    	return DebitorEditor.ID;
    }
    
    @Override
    protected Class<Debitor> getEntityClass() {
    	return Debitor.class;
    }
}
