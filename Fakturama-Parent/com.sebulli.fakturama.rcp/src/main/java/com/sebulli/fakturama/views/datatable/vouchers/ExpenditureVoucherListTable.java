/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gerd Bartelt - initial API and implementation
 */


package com.sebulli.fakturama.views.datatable.vouchers;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.action.ViewportSelectRowAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.ExpendituresDAO;
import com.sebulli.fakturama.dao.VoucherCategoriesDAO;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.model.Voucher;
import com.sebulli.fakturama.model.Voucher_;
import com.sebulli.fakturama.model.VoucherCategory;
import com.sebulli.fakturama.parts.Editor;
import com.sebulli.fakturama.parts.ExpenditureVoucherEditor;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.EntityGridListLayer;
import com.sebulli.fakturama.views.datatable.impl.NoHeaderRowOnlySelectionBindings;
import com.sebulli.fakturama.views.datatable.tree.model.TreeObject;
import com.sebulli.fakturama.views.datatable.tree.ui.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeCategoryLabelProvider;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;



/**
 * View with the table of all expenditures
 * 
 * @author Gerd Bartelt
 * 
 */
public class ExpenditureVoucherListTable extends AbstractViewDataTable<Voucher, VoucherCategory>/* extends AbstractVoucherListTable*/ {
	
	// ID of this view
	public static final String ID = "fakturama.views.expenditureVoucherTable";
	    
    protected static final String POPUP_ID = "com.sebulli.fakturama.expenditurelist.popup";

    /**    this is for synchronizing the UI thread */
    @Inject    
    private UISynchronize sync;
    
    @Inject
    private ExpendituresDAO expendituresDAO;

    @Inject
    private VoucherCategoriesDAO voucherCategoriesDAO;

    private EventList<Voucher> expenditureListData;
    private EventList<VoucherCategory> categories;

    private EntityGridListLayer<Voucher> gridListLayer;

    private MPart listTablePart;

    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    protected FilterList<Voucher> treeFilteredIssues;

	private VoucherMatcher currentFilter = null;

	/**
	 * Creates the SWT controls for this workbench part.
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@PostConstruct
	public Control createPartControl(Composite parent, MPart listTablePart) {
        log.info("create ExpenditureVoucher list part");
        this.listTablePart = listTablePart;
        super.createPartControl(parent, Voucher.class, true, ID);
        // Listen to double clicks
        hookDoubleClickCommand2(natTable, gridListLayer);
        topicTreeViewer.setTable(this);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        return top;

//		// Text of the column "name"
//		customerSupplier = 	DataSetExpenditureVoucher.CUSTOMERSUPPLIER;
//		
//		// Set the input of the table viewer and the tree viewer
//		tableViewer.setInput(Data.INSTANCE.getExpenditureVouchers());
	}

    @Override
    protected void postConfigureNatTable(NatTable natTable) {
        //as the autoconfiguration of the NatTable is turned off, we have to add the 
        //DefaultNatTableStyleConfiguration and the ConfigRegistry manually 
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new NoHeaderRowOnlySelectionBindings());
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new VoucherTableConfiguration());
        natTable.setBackground(GUIHelper.COLOR_WHITE);
        // nur für das Headermenü, falls das mal irgendwann gebraucht werden sollte
        //      natTable.addConfiguration(new HeaderMenuConfiguration(n6));

        // Change the default sort key bindings. Note that 'auto configure' was turned off
        // for the SortHeaderLayer (setup in the GlazedListsGridLayer)
        natTable.addConfiguration(new SingleClickSortConfiguration());
        
        // register right click as a selection event for the whole row
        natTable.getUiBindingRegistry().registerMouseDownBinding(
                new MouseEventMatcher(SWT.NONE, GridRegion.BODY, MouseEventMatcher.RIGHT_BUTTON),

                new IMouseAction() {

                    ViewportSelectRowAction selectRowAction = new ViewportSelectRowAction(false, false);
                                
                    @Override
                    public void run(NatTable natTable, MouseEvent event) {
                        int rowPosition = natTable.getRowPositionByY(event.y);
                        if(!gridListLayer.getSelectionLayer().isRowPositionSelected(rowPosition)) {
                            selectRowAction.run(natTable, event);
                        }                   
                    }
                });
        natTable.configure();
    }

    private IColumnPropertyAccessor<Voucher> createColumnPropertyAccessor(String[] propertyNames) {
        final IColumnPropertyAccessor<Voucher> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<Voucher>(propertyNames);
        
        // Add derived 'default' column
        final IColumnPropertyAccessor<Voucher> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<Voucher>() {

            public Object getDataValue(Voucher rowObject, int columnIndex) {
                ReceiptvoucherListDescriptor descriptor = ReceiptvoucherListDescriptor.getDescriptorFromColumn(columnIndex);
                switch (descriptor) {
                    // alternative: return rowObject.getFirstName();
                default:
                    return columnPropertyAccessor.getDataValue(rowObject, columnIndex);
                }
            }

            public void setDataValue(Voucher rowObject, int columnIndex, Object newValue) {
                throw new UnsupportedOperationException("you can't change a value in list view!");
            }

            public int getColumnCount() {
                return ExpenditureListDescriptor.getVoucherPropertyNames().length;
            }

            public String getColumnProperty(int columnIndex) {
                ExpenditureListDescriptor descriptor = ExpenditureListDescriptor.getDescriptorFromColumn(columnIndex);
                return msg.getMessageFromKey(descriptor.getMessageKey());
            }

            public int getColumnIndex(String propertyName) {
                return columnPropertyAccessor.getColumnIndex(propertyName);
            }
        };
        return derivedColumnPropertyAccessor;
    }
    
    public NatTable createListTable(Composite searchAndTableComposite) {

        expenditureListData = GlazedLists.eventList(expendituresDAO.findAll(true));

        // get the visible properties to show in list view
        String[] propertyNames = expendituresDAO.getVisibleProperties();

        final IColumnPropertyAccessor<Voucher> derivedColumnPropertyAccessor = createColumnPropertyAccessor(propertyNames);

        /*
        // Mark the columns that are used by the search function.
        searchColumns = new String[4];
        searchColumns[0] = "name";
        searchColumns[1] = "nr";
        searchColumns[2] = "documentnr";
        searchColumns[3] = "date";
 */
        final MatcherEditor<Voucher> textMatcherEditor = new TextWidgetMatcherEditor<Voucher>(searchText, 
                GlazedLists.textFilterator(Voucher.class, Voucher_.name.getName(), Voucher_.voucherNumber.getName(),
                        Voucher_.voucherDate.getName(), Voucher_.documentNumber.getName()));
        
        // Filtered list for Search text field filter
        final FilterList<Voucher> textFilteredIssues = new FilterList<Voucher>(expenditureListData, textMatcherEditor);

        // build the list for the tree-filtered values (i.e., the value list which is affected by
        // tree selection)
        treeFilteredIssues = new FilterList<Voucher>(textFilteredIssues);
       
        gridListLayer = new EntityGridListLayer<>(treeFilteredIssues, propertyNames, derivedColumnPropertyAccessor, configRegistry);
        
        DataLayer tableDataLayer = gridListLayer.getBodyDataLayer();
        tableDataLayer.setColumnPercentageSizing(true);
        int col = 0;
        tableDataLayer.setColumnWidthPercentageByPosition(col++, 5);
        tableDataLayer.setColumnWidthPercentageByPosition(col++, 15);
        tableDataLayer.setColumnWidthPercentageByPosition(col++, 75);
        tableDataLayer.setColumnWidthPercentageByPosition(col++, 5);

        ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(gridListLayer.getBodyLayerStack());
        columnLabelAccumulator.registerColumnOverrides(ExpenditureListDescriptor.DONOTBOOK.getPosition(), VoucherTableConfiguration.DONOTBOOK_LABEL);
        columnLabelAccumulator.registerColumnOverrides(ExpenditureListDescriptor.TOTAL.getPosition(), MONEYVALUE_CELL_LABEL);
        columnLabelAccumulator.registerColumnOverrides(ExpenditureListDescriptor.DATE.getPosition(), DATE_CELL_LABEL);

        // Register label accumulator
        gridListLayer.getBodyLayerStack().setConfigLabelAccumulator(columnLabelAccumulator);

        //turn the auto configuration off as we want to add our header menu configuration
        NatTable natTable = new NatTable(searchAndTableComposite, gridListLayer.getGridLayer(), false);
       
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));

        return natTable;
    }
    
    /**
     * @return the gridLayer
     */
    protected EntityGridListLayer<Voucher> getGridLayer() {
        return gridListLayer;
    }

    @Override
    protected TopicTreeViewer<VoucherCategory> createCategoryTreeViewer(Composite top) {
//    	topicTreeViewer = (TopicTreeViewer<VoucherCategory>)ContextInjectionFactory.make(TopicTreeViewer.class, context);
        topicTreeViewer = new TopicTreeViewer<VoucherCategory>(top, msg, false, true);
        categories = GlazedLists.eventList(voucherCategoriesDAO.findAll());
        topicTreeViewer.setInput(categories);
        topicTreeViewer.setLabelProvider(new TreeCategoryLabelProvider());
        return topicTreeViewer;
    }
    
    /**
     * Handle an incoming refresh command. This could be initiated by an editor 
     * which has just saved a new element (document, Voucher, payment etc). Here we ONLY
     * listen to "VatEditor" events.<br />
     * The tree of {@link VoucherCategory}s is updated because we use a GlazedList for
     * the source of the tree. The tree has a listener to the GlazedLists object (<code>categories</code> in this case) which will
     * react on every change of the underlying list (here in the field <code>categories</code>).
     * If the content of <code>categories</code> changes, the change event is fired and the 
     * {@link TopicTreeViewer} is updated.
     * 
     * @param message an incoming message
     */
    @Inject @Optional
    public void handleRefreshEvent(@EventTopic(ExpenditureVoucherEditor.EDITOR_ID) String message) {
    	if(StringUtils.equals(message, Editor.UPDATE_EVENT)) {
	        sync.syncExec(() -> top.setRedraw(false));
	        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
	        GlazedLists.replaceAll(expenditureListData, GlazedLists.eventList(expendituresDAO.findAll(true)), false);
	        GlazedLists.replaceAll(categories, GlazedLists.eventList(voucherCategoriesDAO.findAll(true)), false);
	        treeFilteredIssues.setMatcher(currentFilter);
	        sync.syncExec(() -> top.setRedraw(true));
    	}
    }

    /**
     * Set the category filter with a given {@link TreeObjectType}.
     * 
     * @param filter
     *            The new filter string
     * @param treeObjectType
     *            the {@link TreeObjectType}
     */
    public void setCategoryFilter(String filter, TreeObjectType treeObjectType) {
        currentFilter = new VoucherMatcher(filter, treeObjectType, ((TreeObject) topicTreeViewer.getTree().getTopItem().getData()).getName());
		treeFilteredIssues.setMatcher(currentFilter);

        //Refresh is done automagically...
    }

    protected boolean isHeaderLabelEnabled() {
        return false;
    }

    @Override
    public String getTableId() {
        return ID;
    }
    
    @Override
    protected MToolBar getMToolBar() {
        return listTablePart.getToolbar();
    }

    @Override
    protected String getToolbarAddItemCommandId() {
        return CommandIds.LISTTOOLBAR_ADD_EXPENDITURE;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.AbstractViewDataTable#getEditorId()
     */
    @Override
    protected String getEditorId() {
        return ExpenditureVoucherEditor.ID;
    }

    @Override
    protected String getEditorTypeId() {
        return ExpenditureVoucherEditor.EDITOR_ID;
    }
    
    protected String getPopupId() {
        return POPUP_ID;
    }

    @Override
    protected AbstractDAO<Voucher> getEntityDAO() {
        return expendituresDAO;
    }
    
    @Override
    protected Class<Voucher> getEntityClass() {
    	return Voucher.class;
    }
}

