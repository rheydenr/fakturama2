/**
 * 
 */
package com.sebulli.fakturama.views.datatable.contacts;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.impl.HandledToolItemImpl;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

import com.sebulli.fakturama.dao.DebitorsDAO;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.model.Address_;
import com.sebulli.fakturama.model.Debitor;
import com.sebulli.fakturama.model.Debitor_;
import com.sebulli.fakturama.views.datatable.tree.model.TreeObject;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

/**
 * View with the table of all contacts
 * 
 */
public class DebitorListTable extends ContactListTable<Debitor> {

    // ID of this view
    public static final String ID = "fakturama.views.debitorTable";

    private static final String POPUP_ID = "com.sebulli.fakturama.debitorlist.popup";
    public static final String SELECTED_CREDITOR_ID = "fakturama.debitorlist.selecteddebitorid";

    private EventList<Debitor> debitorListData;

    private Control top;
    
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
    public void handleRefreshEvent(@EventTopic("ContactEditor") String message) {
        sync.syncExec(new Runnable() {
            
            @Override
            public void run() {
                top.setRedraw(false);
            }
        });
        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
        GlazedLists.replaceAll(debitorListData, getListData(true), false);
        GlazedLists.replaceAll(categories, GlazedLists.eventList(contactCategoriesDAO.findAll(true)), false);
        sync.syncExec(new Runnable() {
           
            @Override
            public void run() {
                top.setRedraw(true);
            }});
    }

    @Override
    public void changeToolbarItem(TreeObject treeObject) {
        MToolBar toolbar = listTablePart.getToolbar();
        if(toolbar != null) {
            for (MToolBarElement tbElem : toolbar.getChildren()) {
                if (tbElem.getElementId().contentEquals(getToolbarAddItemCommandId())) {
                    HandledToolItemImpl toolItem = (HandledToolItemImpl) tbElem;
                    ParameterizedCommand wbCommand = toolItem.getWbCommand();
                    @SuppressWarnings("unchecked")
                    Map<String, Object> parameterMap = wbCommand != null ? wbCommand.getParameterMap() : new HashMap<>();
                    if (treeObject.getNodeType() == TreeObjectType.DEFAULT_NODE) {
                        toolItem.setTooltip(msg.commandNewTooltip + " " + msg.getMessageFromKey(treeObject.getDocType().getSingularKey()));
                        parameterMap.put(CallEditor.PARAM_CATEGORY, treeObject.getDocType().name());
                    }
                }
            }
        }
    }

    protected String getPopupId() {
        return POPUP_ID;
    }

    @Override
    protected EventList<Debitor> getListData(boolean forceRead) {
        return GlazedLists.eventList(debitorDAO.findAll(forceRead));
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
}
