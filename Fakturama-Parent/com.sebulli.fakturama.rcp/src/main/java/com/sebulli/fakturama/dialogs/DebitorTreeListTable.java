package com.sebulli.fakturama.dialogs;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeData;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeRowModel;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.swt.widgets.Composite;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.DebitorAddress;
import com.sebulli.fakturama.dao.DebitorsDAO;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.model.Debitor;
import com.sebulli.fakturama.parts.DebitorEditor;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.matchers.MatcherEditor;

public class DebitorTreeListTable extends ContactTreeListTable<TreeItem<DebitorAddress>, Debitor>{
    
    @Inject
    private IEclipseContext context;

	@Inject
	private DebitorsDAO debitorDAO;
	

    @Override
    protected EventList<TreeItem<DebitorAddress>> getListData(boolean forceRead) {
        return GlazedLists
				.eventList(debitorDAO.findForTreeListView(null));
    }

	@PostConstruct
	public void createComposite(Composite parent) {
		// Properties of the DebitorAddress items inside the TreeItems
		String[] propertyNames = { "item.name", "item.firstName" };

		IColumnPropertyAccessor<TreeItem<DebitorAddress>> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<TreeItem<DebitorAddress>>(
				propertyNames);
		
		BillingType currentBillingType = (BillingType) context.get("ADDRESS_TYPE");
		ContactType contactType;
		switch (currentBillingType) {
		case INVOICE:
			contactType = ContactType.BILLING;
			break;
		case DELIVERY:
			contactType = ContactType.DELIVERY;
			break;
		default:
			contactType = ContactType.BILLING;
			break;
		}

		EventList<TreeItem<DebitorAddress>> eventList = GlazedLists
				.eventList(debitorDAO.findForTreeListView(contactType));
		TreeList<TreeItem<DebitorAddress>> treeList = new TreeList<TreeItem<DebitorAddress>>(eventList,
				new TreeItemFormat(), TreeList.nodesStartExpanded());
		ListDataProvider<TreeItem<DebitorAddress>> dataProvider = new ListDataProvider<>(treeList,
				columnPropertyAccessor);
		DataLayer dataLayer = new DataLayer(dataProvider);
		setColumWidthPercentage(dataLayer);

		GlazedListTreeData<TreeItem<DebitorAddress>> glazedListTreeData = new GlazedListTreeData<>(treeList);
		GlazedListTreeRowModel<TreeItem<DebitorAddress>> glazedListTreeRowModel = new GlazedListTreeRowModel<>(
				glazedListTreeData);

		TreeLayer treeLayer = new TreeLayer(dataLayer, glazedListTreeRowModel);
		treeLayer.setRegionName(GridRegion.BODY);

		new NatTable(parent, treeLayer, true);

		GridLayoutFactory.fillDefaults().generateLayout(parent);
	}

	private void setColumWidthPercentage(DataLayer dataLayer) {
		dataLayer.setColumnPercentageSizing(true);
		dataLayer.setColumnWidthPercentageByPosition(0, 50);
		dataLayer.setColumnWidthPercentageByPosition(1, 50);
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
    
    @Override
    protected String getEditorTypeId() {
        return DebitorEditor.EDITOR_ID;
    }

	@Override
	protected MatcherEditor<TreeItem<DebitorAddress>> createTextWidgetMatcherEditor() {
		// TODO Auto-generated method stub
		return null;
	}


}
