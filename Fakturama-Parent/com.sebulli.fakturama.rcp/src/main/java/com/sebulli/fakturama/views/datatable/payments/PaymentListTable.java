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

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultIntegerDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.PercentageDisplayConverter;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.action.ViewportSelectRowAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.dao.VoucherCategoriesDAO;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.Payment_;
import com.sebulli.fakturama.model.VoucherCategory;
import com.sebulli.fakturama.parts.PaymentEditor;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.DefaultCheckmarkPainter;
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
 * Builds the Payment list table.
 */
public class PaymentListTable extends AbstractViewDataTable<Payment, VoucherCategory> {

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    protected IEclipseContext context;

    @Inject
    private Logger log;

    //this is for synchronizing the UI thread
    @Inject    
    private UISynchronize sync;

    // ID of this view
    public static final String ID = "fakturama.views.paymentTable";
    
    protected static final String POPUP_ID = "com.sebulli.fakturama.paymentlist.popup";

    @Inject
    @Preference
    private IEclipsePreferences preferences;
    
    private EventList<Payment> paymentListData;
    private EventList<VoucherCategory> categories;
    
    @Inject
    private PaymentsDAO paymentsDAO;

    @Inject
    private VoucherCategoriesDAO accountDAO;
    
    private static final String DEFAULT_CELL_LABEL = "Standard_Cell_LABEL";
    private static final String PERCENTVALUE_CELL_LABEL = "PercentValue_Cell_LABEL";
    private static final String INTEGERVALUE_CELL_LABEL = "IntegerValue_Cell_LABEL";

    private EntityGridListLayer<Payment> gridLayer;

    private MPart listTablePart;
 
    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    protected FilterList<Payment> treeFilteredIssues;

    @PostConstruct
    public Control createPartControl(Composite parent, MPart listTablePart) {
        log.info("create Payment list part");
        this.listTablePart = listTablePart;
        super.createPartControl(parent, Payment.class, true, ID);
        // Listen to double clicks
        hookDoubleClickCommand2(natTable, gridLayer);
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
        
        // register right click as a selection event for the whole row
        natTable.getUiBindingRegistry().registerMouseDownBinding(
                new MouseEventMatcher(SWT.NONE, GridRegion.BODY, MouseEventMatcher.RIGHT_BUTTON),

                new IMouseAction() {

                    ViewportSelectRowAction selectRowAction = new ViewportSelectRowAction(false, false);
                                
                    @Override
                    public void run(NatTable natTable, MouseEvent event) {
                        int rowPosition = natTable.getRowPositionByY(event.y);
                        if(!gridLayer.getSelectionLayer().isRowPositionSelected(rowPosition)) {
                            selectRowAction.run(natTable, event);
                        }                   
                    }
                });
        natTable.configure();
    }
    
    private IColumnPropertyAccessor<Payment> createColumnPropertyAccessor(String[] propertyNames) {

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
       return derivedColumnPropertyAccessor;
    }
    
    protected NatTable createListTable(Composite searchAndTableComposite) {       
        // fill the underlying data source (GlazedList)
        paymentListData = GlazedLists.eventList(paymentsDAO.findAll(true));

        // get the visible properties to show in list view
        String[] propertyNames = paymentsDAO.getVisibleProperties();
        final IColumnPropertyAccessor<Payment> derivedColumnPropertyAccessor = createColumnPropertyAccessor(propertyNames); 
        /*
        // Mark the columns that are used by the search function.
        searchColumns = new String[5];
        searchColumns[0] = "name";
        searchColumns[1] = "description";
        searchColumns[2] = "discountvalue";
        searchColumns[3] = "discountdays";
        searchColumns[4] = "netdays";
 */
        final MatcherEditor<Payment> textMatcherEditor = new TextWidgetMatcherEditor<Payment>(searchText, GlazedLists.textFilterator(Payment.class,
                Payment_.name.getName(), Payment_.description.getName()));
        
        // Filtered list for Search text field filter
        final FilterList<Payment> textFilteredIssues = new FilterList<Payment>(paymentListData, textMatcherEditor);

        // build the list for the tree-filtered values (i.e., the value list which is affected by
        // tree selection)
        treeFilteredIssues = new FilterList<Payment>(textFilteredIssues);

        //build the grid layer
        gridLayer = new EntityGridListLayer<Payment>(treeFilteredIssues, propertyNames, derivedColumnPropertyAccessor, configRegistry);
        DataLayer tableDataLayer = gridLayer.getBodyDataLayer();
        tableDataLayer.setColumnPercentageSizing(true);
        for (PaymentListDescriptor descriptor : PaymentListDescriptor.values()) {
            tableDataLayer.setColumnWidthPercentageByPosition(descriptor.getPosition(), descriptor.getDefaultWidth());
        }

        // now is the time where we can create the NatTable itself

        // Create a label accumulator - adds custom labels to all cells which we
        // wish to render differently. In this case render as an image.
        ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(gridLayer.getBodyLayerStack());
        columnLabelAccumulator.registerColumnOverrides(PaymentListDescriptor.DEFAULT.getPosition(), DEFAULT_CELL_LABEL);
        columnLabelAccumulator.registerColumnOverrides(PaymentListDescriptor.DISCOUNT.getPosition(), PERCENTVALUE_CELL_LABEL);
        columnLabelAccumulator.registerColumnOverrides(PaymentListDescriptor.DISCDAYS.getPosition(), INTEGERVALUE_CELL_LABEL);
        columnLabelAccumulator.registerColumnOverrides(PaymentListDescriptor.NETDAYS.getPosition(), INTEGERVALUE_CELL_LABEL);
        
        // Register label accumulator
        gridLayer.getBodyLayerStack().setConfigLabelAccumulator(columnLabelAccumulator);

        final NatTable natTable = new NatTable(searchAndTableComposite/*, 
                SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | SWT.BORDER*/, gridLayer.getGridLayer(), false);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));

        // Register your custom cell painter, cell style, against the label applied to the cell.
        //      addImageTextToColumn(configRegistry, natTable, gridLayer.getBodyDataProvider());
        return natTable;
    }
    
    /**
     * @return the gridLayer
     */
    protected EntityGridListLayer<Payment> getGridLayer() {
        return gridLayer;
    }

    @Override
    protected TopicTreeViewer<VoucherCategory> createCategoryTreeViewer(Composite top) {
    	context.set("useDocumentAndContactFilter", false);
    	context.set("useAll", true);
        topicTreeViewer = new TopicTreeViewer<VoucherCategory>(top, msg, false, true);
//    	topicTreeViewer = (TopicTreeViewer<VoucherCategory>)ContextInjectionFactory.make(TopicTreeViewer.class, context);
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
    
    /**
     * Handle an incoming refresh command. This could be initiated by an editor 
     * which has just saved a new element (document, Payment, payment etc). Here we ONLY
     * listen to "PaymentEditor" events.<br />
     * The tree of {@link VoucherCategory}s is updated because we use a GlazedList for
     * the source of the tree. The tree has a listener to the GlazedLists object (<code>categories</code> in this case) which will
     * react on every change of the underlying list (here in the field <code>categories</code>).
     * If the content of <code>categories</code> changes, the change event is fired and the 
     * {@link TopicTreeViewer} is updated.
     * 
     * @param message an incoming message
     */
    @Inject
    @Optional
    public void handleRefreshEvent(@EventTopic(PaymentEditor.EDITOR_ID) String message) {
        sync.syncExec(() -> top.setRedraw(false));
        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
        GlazedLists.replaceAll(paymentListData, GlazedLists.eventList(paymentsDAO.findAll(true)), false);
        GlazedLists.replaceAll(categories, GlazedLists.eventList(accountDAO.findAll(true)), false);
        sync.syncExec(() -> top.setRedraw(true));
    }
//
//    /**
//     * Set the category filter
//     * 
//     * @param filter
//     *            The new filter string
//     * 
//     * @deprecated use {@link #setCategoryFilter(String, TreeObjectType)}
//     *             instead
//     */
//    public void setCategoryFilter(String filter) {
//        setCategoryFilter(filter, TreeObjectType.DEFAULT_NODE);
//    }

    /**
     * Set the category filter with a given {@link TreeObjectType}.
     * 
     * @param filter
     *            The new filter string
     * @param treeObjectType
     *            the {@link TreeObjectType}
     */
    public void setCategoryFilter(String filter, TreeObjectType treeObjectType) {

        // Reset transaction and contact filter, set category filter
        treeFilteredIssues.setMatcher(new PaymentMatcher(filter, treeObjectType,((TreeObject)topicTreeViewer.getTree().getTopItem().getData()).getName()));
        //   contentProvider.setTreeObject(treeObject);

        //Refresh is done automagically...
    }
    
    protected boolean isHeaderLabelEnabled() {
        return false;
    }
    
    @Override
    protected MToolBar getMToolBar() {
        return listTablePart.getToolbar();
    }

    @Override
    protected String getToolbarAddItemCommandId() {
        return CommandIds.LISTTOOLBAR_ADD_PAYMENT;
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

    @Override
    protected String getEditorTypeId() {
        return PaymentEditor.class.getSimpleName();
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

    protected String getPopupId() {
        return POPUP_ID;
    }

    @Override
    protected AbstractDAO<Payment> getEntityDAO() {
        return paymentsDAO;
    }
}
