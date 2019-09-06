package com.sebulli.fakturama.parts.widget.contacttree;

import javax.inject.Inject;

import org.eclipse.core.databinding.beans.BeanProperties;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.DebitorAddress;
import com.sebulli.fakturama.dao.DebitorsDAO;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.model.Debitor;
import com.sebulli.fakturama.parts.DebitorEditor;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

public class DebitorTreeListTable extends ContactTreeListTable<DebitorAddress>{

    // ID of this view
    public static final String ID = "fakturama.views.debitorTreeTable";

    private static final String POPUP_ID = "com.sebulli.fakturama.debitorlist.popup";
    public static final String SELECTED_DEDITOR_ID = "fakturama.debitorlist.selecteddebitorid";

	@Inject
	private DebitorsDAO debitorDAO;
	
    @Override
    public String getTableId() {
        return ID;
    }
    
    @Override
    protected String getEditorTypeId() {
        return DebitorEditor.EDITOR_ID;
    }
    
    protected String getPopupId() {
        return POPUP_ID;
    }

    @Override
    protected EventList<DebitorAddress> getListData(ContactType contactType) {
        return GlazedLists
				.eventList(debitorDAO.findForTreeListView(contactType));
    }

	@Override
	protected MatcherEditor<DebitorAddress> createTextWidgetMatcherEditor() {
		ContactTreeListFilterator contactTreeListFilterator = new ContactTreeListFilterator(
				BeanProperties.value(DebitorAddress.class, "customerNumber"),
				BeanProperties.value(DebitorAddress.class, "firstName"),
				BeanProperties.value(DebitorAddress.class, "name"),
				BeanProperties.value(DebitorAddress.class, "company"),
				BeanProperties.value(DebitorAddress.class, "zipCode"),
				BeanProperties.value(DebitorAddress.class, "city")
				);
        return new TextWidgetMatcherEditor<DebitorAddress>(searchText.getTextControl(), 
        		contactTreeListFilterator);
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
    protected Class<DebitorAddress> getEntityClass() {
    	return DebitorAddress.class;
    }
}
