/**
 * 
 */
package com.sebulli.fakturama.views.datatable.contacts;

import javax.inject.Inject;

import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.CreditorsDAO;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.Address_;
import com.sebulli.fakturama.model.Creditor;
import com.sebulli.fakturama.model.Creditor_;
import com.sebulli.fakturama.parts.CreditorEditor;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

/**
 * View with the table of all creditors
 * 
 */
public class CreditorListTable extends ContactListTable<Creditor> {

	// ID of this view
	public static final String ID = "fakturama.views.creditorTable";

	private static final String POPUP_ID = "com.sebulli.fakturama.creditorlist.popup";
	public static final String SELECTED_CREDITOR_ID = "fakturama.creditorlist.selectedcreditorid";

	@Inject
	private CreditorsDAO creditorDAO;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#getTableId()
	 */
	@Override
	public String getTableId() {
		return ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#getEditorId(
	 * )
	 */
	@Override
	protected String getEditorId() {
		return CreditorEditor.ID;
	}

	@Override
	protected String getEditorTypeId() {
		return CreditorEditor.EDITOR_ID;
	}
	
    @Override
    protected String getToolbarAddItemCommandId() {
        return CommandIds.LISTTOOLBAR_ADD_CREDITOR;
    }

	@Inject
	@Optional
	public void handleRefreshEvent(@UIEventTopic(CreditorEditor.EDITOR_ID) String message) {
		super.handleRefreshEvent(message);
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
		 * searchColumns[0] = "nr"; searchColumns[1] = "firstname"; searchColumns[2] =
		 * "name"; searchColumns[3] = "company"; searchColumns[4] = "zip";
		 * searchColumns[5] = "city";
		 */
		return new TextWidgetMatcherEditor<Creditor>(searchText.getTextControl(),
				new ContactListFilterator<Creditor>(
						BeanProperties.value(Creditor.class, Creditor_.customerNumber.getName()),
						BeanProperties.value(Creditor.class, Creditor_.firstName.getName()),
						BeanProperties.value(Creditor.class, Creditor_.name.getName()),
						BeanProperties.value(Creditor.class, Creditor_.company.getName()),
						BeanProperties.list(Creditor.class, Creditor_.addresses.getName(), Address.class)
								.values(Address_.zip.getName()),
						BeanProperties.list(Creditor.class, Creditor_.addresses.getName(), Address.class)
								.values(Address_.city.getName())));
	}

	@Override
	protected AbstractDAO<Creditor> getEntityDAO() {
		return creditorDAO;
	}

	protected Class<Creditor> getEntityClass() {
		return Creditor.class;
	};
}
