/**
 * 
 */
package com.sebulli.fakturama.views.datatable.documents;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.widgets.Composite;

import ca.odell.glazedlists.EventList;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.AbstractCategory;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.tree.ui.TopicTreeViewer;

/**
 * @author rheydenr
 *
 */
public class DocumentsListTable extends AbstractViewDataTable<Document, AbstractCategory> {

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private Logger log;

    // ID of this view
    public static final String ID = "fakturama.views.documentTable";     
    /**
     * Event Broker for receiving update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

    @Inject
    @Preference
    private IEclipsePreferences preferences;

    private EventList<VAT> eventList;


    @Override
    protected NatTable createListTable(Composite searchAndTableComposite) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected String getTableId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected TopicTreeViewer<AbstractCategory> createCategoryTreeViewer(Composite top) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected boolean isHeaderLabelEnabled() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.AbstractViewDataTable#getEditorId()
     */
    @Override
    protected String getEditorId() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#setCategoryFilter(java.lang.String, com.sebulli.fakturama.views.datatable.vats.TreeObjectType)
     */
    @Override
    public void setCategoryFilter(String filter, com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType treeObjectType) {
        // TODO Auto-generated method stub
        
    }

    @Override
    protected String getPopupId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeSelectedEntry() {
        // TODO Auto-generated method stub
        
    }

}
