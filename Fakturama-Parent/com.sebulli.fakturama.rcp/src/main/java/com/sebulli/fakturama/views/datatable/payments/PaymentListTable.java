/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2015 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.views.datatable.payments;

import java.io.Serializable;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultIntegerDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.PercentageDisplayConverter;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionConfiguration;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.dao.VoucherCategoriesDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.VoucherCategory;
import com.sebulli.fakturama.parts.PaymentEditor;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.DefaultCheckmarkPainter;
import com.sebulli.fakturama.views.datatable.ListViewGridLayer;
import com.sebulli.fakturama.views.datatable.ListViewHeaderDataProvider;
import com.sebulli.fakturama.views.datatable.impl.NoHeaderRowOnlySelectionBindings;
import com.sebulli.fakturama.views.datatable.tree.model.TreeObject;
import com.sebulli.fakturama.views.datatable.tree.ui.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeCategoryLabelProvider;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

/**
 * Builds the Payment list table.
 */
public class PaymentListTable extends AbstractViewDataTable<Payment, VoucherCategory> {

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private Logger log;

//    this is for synchronizing the UI thread
//    @Inject    
//    private UISynchronize synch;

    // ID of this view
    public static final String ID = "fakturama.views.paymentTable";
    
    protected static final String POPUP_ID = "com.sebulli.fakturama.paymentlist.popup";
    
    /**
     * Event Broker for receiving update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

    @Inject
    @Preference
    private IEclipsePreferences preferences;
    
    private EventList<Payment> paymentListData;
    private EventList<VoucherCategory> categories;

    private Control top;
    
    @Inject
    private PaymentsDAO paymentsDAO;

    @Inject
    private VoucherCategoriesDAO accountDAO;
    
    private static final String DEFAULT_CELL_LABEL = "Standard_Cell_LABEL";
    private static final String PERCENTVALUE_CELL_LABEL = "PercentValue_Cell_LABEL";
    private static final String INTEGERVALUE_CELL_LABEL = "IntegerValue_Cell_LABEL";

    private ListViewGridLayer<Payment> gridLayer;
    
    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    protected FilterList<Payment> treeFilteredIssues;
    private SelectionLayer selectionLayer;

    @PostConstruct
    public Control createPartControl(Composite parent) {
        log.info("create Payment list part");
        top = super.createPartControl(parent, Payment.class, true, ID);
        // Listen to double clicks
        hookDoubleClickCommand(natTable, gridLayer);
        topicTreeViewer.setTable(this);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        return top;
    }

    @Override
    protected void postConfigureNatTable(NatTable natTable) {
        //as the autoconfiguration of the NatTable is turned off, we have to add the 
        //DefaultNatTableStyleConfiguration and the ConfigRegistry manually 
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new NoHeaderRowOnlySelectionBindings());
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new PaymentTableConfiguration());
        // nur für das Headermenü, falls das mal irgendwann gebraucht werden sollte
        //      natTable.addConfiguration(new HeaderMenuConfiguration(n6));

        // Change the default sort key bindings. Note that 'auto configure' was turned off
        // for the SortHeaderLayer (setup in the GlazedListsGridLayer)
        natTable.addConfiguration(new SingleClickSortConfiguration());
        
        /*
         * Set the background color for this table. Could only set here, because otherwise 
         * it would be overwritten with default configurations.
         */
        natTable.setBackground(GUIHelper.COLOR_WHITE);
        natTable.configure();
    }
    
    protected NatTable createListTable(Composite searchAndTableComposite) {       
        // fill the underlying data source (GlazedList)
        paymentListData = GlazedLists.eventList(paymentsDAO.findAll(true));

        // get the visible properties to show in list view
        String[] propertyNames = paymentsDAO.getVisibleProperties();

        final IColumnPropertyAccessor<Payment> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<Payment>(propertyNames);
        
        // Add derived 'default' column
        final IColumnPropertyAccessor<Payment> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<Payment>() {

            public Object getDataValue(Payment rowObject, int columnIndex) {
                PaymentListDescriptor descriptor = PaymentListDescriptor.getDescriptorFromColumn(columnIndex);
                switch (descriptor) {
                case DEFAULT:
                    return rowObject.getId() == getDefaultPaymentId();
                case NAME:
                case DESCRIPTION:
                case DISCOUNT:
                case DISCDAYS:
                case NETDAYS:
                    // alternative: return rowObject.getFirstName();
                    return columnPropertyAccessor.getDataValue(rowObject, columnIndex - 1);
                default:
                    break;
                }
                return null;
            }

            public void setDataValue(Payment rowObject, int columnIndex, Object newValue) {
                throw new UnsupportedOperationException("you can't change a value in list view!");
            }

            public int getColumnCount() {
                return PaymentListDescriptor.getPaymentPropertyNames().length;
            }

            public String getColumnProperty(int columnIndex) {
                PaymentListDescriptor descriptor = PaymentListDescriptor.getDescriptorFromColumn(columnIndex);
                return msg.getMessageFromKey(descriptor.getMessageKey());
            }

            public int getColumnIndex(String propertyName) {
                if (PaymentListDescriptor.DEFAULT.getPropertyName().equals(propertyName)) {
                    return PaymentListDescriptor.DEFAULT.getPosition();
                } else {
                    return columnPropertyAccessor.getColumnIndex(propertyName) + 1;
                }
            }
        };

        //build the column header layer
        // Column header data provider includes derived properties
        IDataProvider columnHeaderDataProvider = new ListViewHeaderDataProvider<Payment>(propertyNames, derivedColumnPropertyAccessor); 

        // matcher input Search text field 
        final MatcherEditor<Payment> textMatcherEditor = new TextWidgetMatcherEditor<Payment>(searchText, new PaymentFilterator());
        
        // Filtered list for Search text field filter
        final FilterList<Payment> textFilteredIssues = new FilterList<Payment>(paymentListData, textMatcherEditor);

        // build the list for the tree-filtered values (i.e., the value list which is affected by
        // tree selection)
        treeFilteredIssues = new FilterList<Payment>(textFilteredIssues);

        //create the body layer stack
        final IRowDataProvider<Payment> firstBodyDataProvider = 
                new GlazedListsDataProvider<Payment>(treeFilteredIssues, derivedColumnPropertyAccessor);
        
        //build the grid layer
        gridLayer = new ListViewGridLayer<Payment>(treeFilteredIssues, derivedColumnPropertyAccessor, columnHeaderDataProvider, configRegistry, true);
        DataLayer tableDataLayer = gridLayer.getBodyDataLayer();
        tableDataLayer.setColumnPercentageSizing(true);
        for (PaymentListDescriptor descriptor : PaymentListDescriptor.values()) {
            tableDataLayer.setColumnWidthPercentageByPosition(descriptor.getPosition(), descriptor.getDefaultWidth());
        }
        GlazedListsEventLayer<Payment> paymentListEventLayer = new GlazedListsEventLayer<Payment>(tableDataLayer, paymentListData);

        // add a label accumulator to be able to register converter
        // this is crucial for using custom values display
        paymentListEventLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
        
        // Custom selection configuration
        selectionLayer = gridLayer.getBodyLayerStack().getSelectionLayer();
        
        // for further use, if we need it...
        //      ILayer columnHeaderLayer = gridLayer.getColumnHeaderLayer();
        //      ILayer rowHeaderLayer = gridLayer.getRowHeaderLayer();

        IRowIdAccessor<Payment> rowIdAccessor = new IRowIdAccessor<Payment>() {
            @Override
            public Serializable getRowId(Payment rowObject) {
                return rowObject.getId();
            }
        };
        
        //use a RowSelectionModel that will perform row selections and is able to identify a row via unique ID
        RowSelectionModel<Payment> selectionModel = new RowSelectionModel<Payment>(selectionLayer, firstBodyDataProvider, rowIdAccessor, false);
        selectionLayer.setSelectionModel(selectionModel);
//         Select complete rows
        selectionLayer.addConfiguration(new RowOnlySelectionConfiguration<Payment>());

        // now is the time where we can create the NatTable itself

        // Create a label accumulator - adds custom labels to all cells which we
        // wish to render differently. In this case render as an image.
        ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(gridLayer.getBodyLayerStack());
        columnLabelAccumulator.registerColumnOverrides(PaymentListDescriptor.DEFAULT.getPosition(), DEFAULT_CELL_LABEL);
        columnLabelAccumulator.registerColumnOverrides(PaymentListDescriptor.DISCOUNT.getPosition(), PERCENTVALUE_CELL_LABEL);
        columnLabelAccumulator.registerColumnOverrides(PaymentListDescriptor.DISCDAYS.getPosition(), INTEGERVALUE_CELL_LABEL);
        columnLabelAccumulator.registerColumnOverrides(PaymentListDescriptor.NETDAYS.getPosition(), INTEGERVALUE_CELL_LABEL);

        final NatTable natTable = new NatTable(searchAndTableComposite/*, 
                SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | SWT.BORDER*/, gridLayer, false);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));
        
        // Register label accumulator
        gridLayer.getBodyLayerStack().setConfigLabelAccumulator(columnLabelAccumulator);

        // Register your custom cell painter, cell style, against the label applied to the cell.
        //      addImageTextToColumn(configRegistry, natTable, gridLayer.getBodyDataProvider());
        return natTable;
    }
    
    /**
     * @return the gridLayer
     */
    protected ListViewGridLayer<Payment> getGridLayer() {
        return gridLayer;
    }

    @Override
    protected TopicTreeViewer<VoucherCategory> createCategoryTreeViewer(Composite top) {
        topicTreeViewer = new TopicTreeViewer<VoucherCategory>(top, msg, false, true);
        categories = GlazedLists.eventList(accountDAO.findAll());
        topicTreeViewer.setInput(categories);
        topicTreeViewer.setLabelProvider(new TreeCategoryLabelProvider());
        return topicTreeViewer;
    }

    /**
     * reads the default Payment value from preference store and returns the
     * appropriate Payment-ID value from database
     * 
     * @return
     */
    private long getDefaultPaymentId() {
        return preferences.getLong(Constants.DEFAULT_PAYMENT, 1L);
    }
    
//    /**
//     * Handle an incoming refresh command. This could be initiated by an editor 
//     * which has just saved a new element (document, Payment, payment etc). Here we ONLY
//     * listen to "PaymentEditor" events.<br />
//     * The tree of {@link VoucherCategory}s is updated because we use a GlazedList for
//     * the source of the tree. The tree has a listener to the GlazedLists object (<code>categories</code> in this case) which will
//     * react on every change of the underlying list (here in the field <code>categories</code>).
//     * If the content of <code>categories</code> changes, the change event is fired and the 
//     * {@link TopicTreeViewer} is updated.
//     * 
//     * @param message an incoming message
//     */
//    @Inject
//    @Optional
//    public void handleRefreshEvent(@EventTopic(PaymentEditor.EDITOR_ID) String message) {
//        synch.syncExec(new Runnable() {
//
//            @Override
//            public void run() {
//                top.setRedraw(false);
//            }
//        });
//        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
//        paymentListData.clear();
//        paymentListData.addAll(paymentsDAO.findAll());
//        categories.clear();
//        categories.addAll(accountDAO.findAll());
//        synch.syncExec(new Runnable() {
//
//            @Override
//            public void run() {
//                top.setRedraw(true);
//            }
//        });
//    }

    /**
     * Set the category filter
     * 
     * @param filter
     *            The new filter string
     * 
     * @deprecated use {@link #setCategoryFilter(String, TreeObjectType)}
     *             instead
     */
    public void setCategoryFilter(String filter) {
        setCategoryFilter(filter, TreeObjectType.DEFAULT_NODE);
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
        // Set the label with the filter string
        if (filter.equals("$shownothing"))
            filterLabel.setText("");
        else
        // Display the localized list names.
        // or the document type
        if (isHeaderLabelEnabled())
            filterLabel.setText("DataSetListNames.NAMES.getLocalizedName: " + filter);

        filterLabel.pack(true);

        // Reset transaction and contact filter, set category filter
        treeFilteredIssues.setMatcher(new PaymentMatcher(filter, treeObjectType,((TreeObject)topicTreeViewer.getTree().getTopItem().getData()).getName()));
        //   contentProvider.setTreeObject(treeObject);

        //Refresh is done automagically...
    }
    
    protected boolean isHeaderLabelEnabled() {
        return false;
    }

    @Override
    public String getTableId() {
        return ID;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.AbstractViewDataTable#getEditorId()
     */
    @Override
    protected String getEditorId() {
        return PaymentEditor.ID;
    }

    class PaymentTableConfiguration extends AbstractRegistryConfiguration {

        @Override
        public void configureRegistry(IConfigRegistry configRegistry) {
            Style styleLeftAligned = new Style();
            styleLeftAligned.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
            Style styleRightAligned = new Style();
            styleRightAligned.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
            Style styleCentered = new Style();
            styleCentered.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);

            // default style for the most of the cells
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, // attribute to apply
                                                   styleLeftAligned,                // value of the attribute
                                                   DisplayMode.NORMAL,              // apply during normal rendering i.e not during selection or edit
                                                   GridRegion.BODY.toString());     // apply the above for all cells with this label

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, 
                    new DefaultCheckmarkPainter(),
                    DisplayMode.NORMAL, DEFAULT_CELL_LABEL);
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                    styleCentered,      
                    DisplayMode.NORMAL,             
                    DEFAULT_CELL_LABEL); 

            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL,             
                    PERCENTVALUE_CELL_LABEL ); 
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new PercentageDisplayConverter(),
                    DisplayMode.NORMAL,
                    PERCENTVALUE_CELL_LABEL);

            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL,             
                    INTEGERVALUE_CELL_LABEL ); 
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultIntegerDisplayConverter(),
                    DisplayMode.NORMAL,
                    INTEGERVALUE_CELL_LABEL);
        }
    }

    public void removeSelectedEntry() {
        if(selectionLayer.getFullySelectedRowPositions().length > 0) {
            Payment objToDelete = gridLayer.getBodyDataProvider().getRowObject(selectionLayer.getFullySelectedRowPositions()[0]);
            try {
                // don't delete the entry because it could be referenced
                // from another entity
                objToDelete.setDeleted(Boolean.TRUE);
                paymentsDAO.save(objToDelete);
            }
            catch (SQLException e) {
                log.error(e, "can't save the current Payment: " + objToDelete.toString());
            }
    
            // Refresh the table view of all Payment
            evtBroker.post(PaymentEditor.EDITOR_ID, "update");
        } else {
            log.debug("no rows selected!");
        }
    }

    protected String getPopupId() {
        return POPUP_ID;
    }

}
