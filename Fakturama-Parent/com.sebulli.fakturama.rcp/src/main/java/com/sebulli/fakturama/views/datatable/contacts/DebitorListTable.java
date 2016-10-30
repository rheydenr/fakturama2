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
import com.sebulli.fakturama.dao.DebitorsDAO;
import com.sebulli.fakturama.model.Address_;
import com.sebulli.fakturama.model.Debitor;
import com.sebulli.fakturama.model.Debitor_;
import com.sebulli.fakturama.parts.DebitorEditor;

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
    
    @Inject @Optional
    public void handleRefreshEvent(@EventTopic(DebitorEditor.EDITOR_ID) String message) {
    	if(StringUtils.equals(message, "update")) {
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
        return new TextWidgetMatcherEditor<Debitor>(searchText, 
                GlazedLists.textFilterator(Debitor.class, 
                        Debitor_.customerNumber.getName(),
                        Debitor_.firstName.getName(),
                        Debitor_.name.getName(),
                        Debitor_.company.getName(),
                        Debitor_.address.getName() + "." + Address_.zip.getName(),
                        Debitor_.address.getName() + "." + Address_.city.getName()
                        ));
    }

    @Override
    protected AbstractDAO<Debitor> getEntityDAO() {
        return debitorDAO;
    }
}
