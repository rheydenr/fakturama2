package com.sebulli.fakturama.parts.widget.contacttree;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.CreditorsDAO;
import com.sebulli.fakturama.dao.DebitorAddress;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.model.Creditor;
import com.sebulli.fakturama.parts.CreditorEditor;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

public class CreditorTreeListTable extends ContactTreeListTable<DebitorAddress> {
// ID of this view
public static final String ID = "fakturama.views.creditorTreeTable";

private static final String POPUP_ID = "com.sebulli.fakturama.creditorlist.popup";
public static final String SELECTED_DEDITOR_ID = "fakturama.creditorlist.selectedcreditorid";

@Inject
private CreditorsDAO creditorsDAO;

@Override
public String getTableId() {
    return ID;
}
@PostConstruct
public Control createPartControl(Composite parent, MPart listTablePart) {
    return super.createPartControl(parent, listTablePart);
}

@Override
protected String getEditorTypeId() {
    return CreditorEditor.EDITOR_ID;
}

protected String getPopupId() {
    return POPUP_ID;
}

@Override
protected EventList<DebitorAddress> getListData(ContactType contactType) {
    return GlazedLists
            .eventList(creditorsDAO.findForTreeListView(contactType));
}

@Override
protected MatcherEditor<DebitorAddress> createTextWidgetMatcherEditor() {
    ContactTreeListFilterator<DebitorAddress> contactTreeListFilterator = new ContactTreeListFilterator<>(
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
protected AbstractDAO<Creditor> getEntityDAO() {
    return creditorsDAO;
}

@Override
protected String getEditorId() {
    return CreditorEditor.ID;
}

@Override
protected Class<DebitorAddress> getEntityClass() {
    return DebitorAddress.class;
}

}
