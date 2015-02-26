/**
 * 
 */
package com.sebulli.fakturama.views.datatable.documents;

import java.io.Serializable;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.impl.HandledToolItemImpl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
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
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDateDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DisplayConverter;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.action.ViewportSelectRowAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DummyStringCategory;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.util.ContactUtil;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.CellImagePainter;
import com.sebulli.fakturama.views.datatable.ListViewGridLayer;
import com.sebulli.fakturama.views.datatable.ListViewHeaderDataProvider;
import com.sebulli.fakturama.views.datatable.MoneyDisplayConverter;
import com.sebulli.fakturama.views.datatable.impl.NoHeaderRowOnlySelectionBindings;
import com.sebulli.fakturama.views.datatable.tree.model.TreeObject;
import com.sebulli.fakturama.views.datatable.tree.ui.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeCategoryLabelProvider;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

/**
 * Builds the Document list table.
 */
public class DocumentsListTable extends AbstractViewDataTable<Document, DummyStringCategory> {

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private Logger log;

    //  this is for synchronizing the UI thread
    @Inject
    private UISynchronize sync;

    // ID of this view
    public static final String ID = "fakturama.views.documentTable";     
    
    protected static final String POPUP_ID = "com.sebulli.fakturama.document.popup";
    /**
     * Event Broker for receiving update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

    @Inject
    @Preference
    private IEclipsePreferences preferences;

    private EventList<Document> documentListData;
    private EventList<DummyStringCategory> categories;

    private Control top;
    
    @Inject
    private DocumentsDAO documentsDAO;
    
    @Inject
    private ContactsDAO contactsDAO;

    @Inject
    private MApplication application;
    
    @Inject
    private EModelService modelService;
   
    private static final String ICON_CELL_LABEL = "Icon_Cell_LABEL";
    private static final String MONEYVALUE_CELL_LABEL = "MoneyValue_Cell_LABEL";
    private static final String DATE_CELL_LABEL = "DateValue_Cell_LABEL";
    private static final String STATE_CELL_LABEL = "StateValue_Cell_LABEL";
    
    private ListViewGridLayer<Document> gridLayer;
    
    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    protected FilterList<Document> treeFilteredIssues;
    private SelectionLayer selectionLayer;

    private ContactUtil contactUtil;

    private MPart listTablePart;

    @PostConstruct
    public Control createPartControl(Composite parent, IEclipseContext context) {
        log.info("create Document list part");
        top = super.createPartControl(parent, Document.class, false, true, ID);
        listTablePart = (MPart) modelService.find(ID, application);
//        this.context = context;
        // Listen to double clicks
        hookDoubleClickCommand(natTable, gridLayer);
        topicTreeViewer.setTable(this);
        
        // On creating, set the unpaid invoices
        topicTreeViewer.selectItemByName(
                String.format("%s/%s", 
                        msg.getMessageFromKey(DocumentType.INVOICE.getPluralDescription()), 
                        msg.documentOrderStateUnpaid)
                );

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);        
        contactUtil = new ContactUtil(preferences);            
        
        return top;
    }

    @Override
    protected void postConfigureNatTable(NatTable natTable) {
        //as the autoconfiguration of the NatTable is turned off, we have to add the 
        //DefaultNatTableStyleConfiguration and the ConfigRegistry manually 
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new NoHeaderRowOnlySelectionBindings());
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new DocumentTableConfiguration());
        addCustomStyling(natTable);
        // nur für das Headermenü, falls das mal irgendwann gebraucht werden sollte
        //      natTable.addConfiguration(new HeaderMenuConfiguration(n6));

        // Change the default sort key bindings. Note that 'auto configure' was turned off
        // for the SortHeaderLayer (setup in the GlazedListsGridLayer)
        natTable.addConfiguration(new SingleClickSortConfiguration());
        
        /*
         * add feedback behavior to nattable (i.e., if a cell is selected, inform the
         * TreeTable about it) 
         */
        natTable.addLayerListener(new ILayerListener() {
            // Default selection behavior selects cells by default.
            public void handleLayerEvent(ILayerEvent event) {
                if (event instanceof CellSelectionEvent) {
                    CellSelectionEvent cellEvent = (CellSelectionEvent) event;
                    
                    //transform the NatTable row position to the row position of the body layer stack
                    int bodyRowPos = LayerUtil.convertRowPosition(natTable, cellEvent.getRowPosition(), gridLayer.getBodyDataLayer());
                    if(bodyRowPos > -1) {
                        // extract the selected Object
                        Document selectedObject = gridLayer.getBodyDataProvider().getRowObject(bodyRowPos);
                        
                        // Set the transaction and the contact filter
                        if (selectedObject != null) {
                            if(selectedObject.getTransactionId() != null) {
                                topicTreeViewer.setTransaction(selectedObject.getTransactionId());
                            }
                            topicTreeViewer.setContact(selectedObject.getAddressFirstLine(), selectedObject.getContact());
                            changePopupEntries(null);
                        }
                    }
                }
            }
        });

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
                        if(!selectionLayer.isRowPositionSelected(rowPosition)) {
                            selectRowAction.run(natTable, event);
                            changePopupEntries(null);
                        }                   
                    }
                });

        natTable.configure();
    }
    
    
    protected NatTable createListTable(Composite searchAndTableComposite) {       
        // fill the underlying data source (GlazedList)
        documentListData = GlazedLists.eventList(documentsDAO.findAll(true));

        // get the visible properties to show in list view
        String[] propertyNames = documentsDAO.getVisibleProperties();

        final IColumnPropertyAccessor<Document> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<Document>(propertyNames);
        final SpecialCellValueProvider specialCellValueProvider = new SpecialCellValueProvider(msg);
        // Add derived 'default' column
        final IColumnPropertyAccessor<Document> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<Document>() {

            public Object getDataValue(Document rowObject, int columnIndex) {
                DocumentListDescriptor descriptor = DocumentListDescriptor.getDescriptorFromColumn(columnIndex);
                switch (descriptor) {
                case ICON:
                case STATE:
                case PRINTED:
                    return specialCellValueProvider.getDataValue(rowObject, descriptor, columnIndex);
                case DATE:
                    return columnPropertyAccessor.getDataValue(rowObject, columnIndex);
                case DOCUMENT:
                    return columnPropertyAccessor.getDataValue(rowObject, columnIndex-1);
                case NAME:
                    return columnPropertyAccessor.getDataValue(rowObject, 1);
                case TOTAL:
                    // alternative: return rowObject.getFirstName();
                    return columnPropertyAccessor.getDataValue(rowObject, 3);
                default:
                    break;
                }
                return null;
            }

            public void setDataValue(Document rowObject, int columnIndex, Object newValue) {
                throw new UnsupportedOperationException("you can't change a value in list view!");
            }

            public int getColumnCount() {
                return DocumentListDescriptor.getDocumentPropertyNames().length;
            }

            public String getColumnProperty(int columnIndex) {
                DocumentListDescriptor descriptor = DocumentListDescriptor.getDescriptorFromColumn(columnIndex);
                return msg.getMessageFromKey(descriptor.getMessageKey());
            }

            public int getColumnIndex(String propertyName) {
                if (DocumentListDescriptor.ICON.getPropertyName().equals(propertyName)) {
                    return DocumentListDescriptor.ICON.getPosition();
                } else {
                    return columnPropertyAccessor.getColumnIndex(propertyName) + 1;
                }
            }
        };

        //build the column header layer
        // Column header data provider includes derived properties
        IDataProvider columnHeaderDataProvider = new ListViewHeaderDataProvider<Document>(propertyNames, derivedColumnPropertyAccessor); 

        // matcher input Search text field 
        final MatcherEditor<Document> textMatcherEditor = new TextWidgetMatcherEditor<Document>(searchText, new DocumentFilterator());
        
        // Filtered list for Search text field filter
        final FilterList<Document> textFilteredIssues = new FilterList<Document>(documentListData, textMatcherEditor);

        // build the list for the tree-filtered values (i.e., the value list which is affected by
        // tree selection)
        treeFilteredIssues = new FilterList<Document>(textFilteredIssues);

        //create the body layer stack
        final IRowDataProvider<Document> firstBodyDataProvider = 
                new GlazedListsDataProvider<Document>(treeFilteredIssues, columnPropertyAccessor);
        
        //build the grid layer
        gridLayer = new ListViewGridLayer<Document>(treeFilteredIssues, derivedColumnPropertyAccessor, columnHeaderDataProvider, configRegistry, true);
        DataLayer tableDataLayer = gridLayer.getBodyDataLayer();
        tableDataLayer.setColumnPercentageSizing(true);
        for (DocumentListDescriptor descriptor : DocumentListDescriptor.values()) {
            tableDataLayer.setColumnWidthPercentageByPosition(descriptor.getPosition(), descriptor.getDefaultWidth());
        }
        GlazedListsEventLayer<Document> paymentListEventLayer = new GlazedListsEventLayer<Document>(tableDataLayer, documentListData);

        // add a label accumulator to be able to register converter
        // this is crucial for using custom values display
        paymentListEventLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
        
        // Custom selection configuration
        selectionLayer = gridLayer.getBodyLayerStack().getSelectionLayer();
        
        // for further use, if we need it...
        //      ILayer columnHeaderLayer = gridLayer.getColumnHeaderLayer();
        //      ILayer rowHeaderLayer = gridLayer.getRowHeaderLayer();

        IRowIdAccessor<Document> rowIdAccessor = new IRowIdAccessor<Document>() {
            @Override
            public Serializable getRowId(Document rowObject) {
                return rowObject.getId();
            }
        };
        
        //use a RowSelectionModel that will perform row selections and is able to identify a row via unique ID
        RowSelectionModel<Document> selectionModel = new RowSelectionModel<Document>(selectionLayer, firstBodyDataProvider, rowIdAccessor, false);
        selectionLayer.setSelectionModel(selectionModel);
        selectionModel.setMultipleSelectionAllowed(true);
//         Select complete rows
        selectionLayer.addConfiguration(new RowOnlySelectionConfiguration<Document>());

        // now is the time where we can create the NatTable itself

        // Create a label accumulator - adds custom labels to all cells which we
        // wish to render differently. In this case render as an image.
        ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(gridLayer.getBodyLayerStack());
        columnLabelAccumulator.registerColumnOverrides(DocumentListDescriptor.ICON.getPosition(), ICON_CELL_LABEL);
        columnLabelAccumulator.registerColumnOverrides(DocumentListDescriptor.STATE.getPosition(), STATE_CELL_LABEL);
        columnLabelAccumulator.registerColumnOverrides(DocumentListDescriptor.PRINTED.getPosition(), ICON_CELL_LABEL);
        columnLabelAccumulator.registerColumnOverrides(DocumentListDescriptor.TOTAL.getPosition(), MONEYVALUE_CELL_LABEL);
        columnLabelAccumulator.registerColumnOverrides(DocumentListDescriptor.DATE.getPosition(), DATE_CELL_LABEL);

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
    protected ListViewGridLayer<Document> getGridLayer() {
        return gridLayer;
    }

    @Override
    protected TopicTreeViewer<DummyStringCategory> createCategoryTreeViewer(Composite top) {
        topicTreeViewer = new TopicTreeViewer<DummyStringCategory>(top, msg, true, false);
        categories = GlazedLists.eventList(documentsDAO.getCategoryStrings());
        topicTreeViewer.setInput(categories);
        topicTreeViewer.setLabelProvider(new TreeCategoryLabelProvider());
        
        return topicTreeViewer;
    }
    
    /**
     * Handle an incoming refresh command. This could be initiated by an editor 
     * which has just saved a new element (document, Document, payment etc). Here we ONLY
     * listen to "DocumentEditor" events.<br />
     * The tree of Categories is not updated because it is a (more or less) static tree.
     * 
     * @param message an incoming message
     */
    @Inject
    @Optional
    public void handleRefreshEvent(@EventTopic("DocumentEditor") String message) {
        sync.syncExec(new Runnable() {

            @Override
            public void run() {
                top.setRedraw(false);
            }
        });
        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
        documentListData.clear();
        documentListData.addAll(documentsDAO.findAll(true));
        sync.syncExec(new Runnable() {

            @Override
            public void run() {
                top.setRedraw(true);
            }
        });
    }

    /**
     * We have to style the table a little bit...
     * 
     * @param natTable
     *            the {@link NatTable} to style
     */
    private void addCustomStyling(NatTable natTable) {
        DefaultSelectionStyleConfiguration selectionStyle = createDefaultSelectionStyle();

        // Add all style configurations to NatTable
        natTable.addConfiguration(selectionStyle);
    }

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
        if (filter.equals("$shownothing")) {
            filterLabel.setText("");
        } else {
        // Display the localized list names.
        // or the document type
            if (isHeaderLabelEnabled()) {
                // TreeObjectType.CONTACTS_ROOTNODE and TreeObjectType.TRANSACTIONS_ROOTNODE
                // both have the same default name, therefore we only test for one of these node types.
                if(filter.endsWith(TreeObjectType.CONTACTS_ROOTNODE.getDefaultName())) {
                    filterLabel.setText(" ");
                } else {
                    if (treeObjectType == TreeObjectType.TRANSACTIONS_ROOTNODE) {
                        filterLabel.setText(msg.topictreeLabelThistransaction);
                    } else {
                        filterLabel.setText(StringUtils.removeStart(filter, "/"));
                    }
                }
            }
 
        // Reset transaction and contact filter, set category filter
        treeFilteredIssues.setMatcher(new DocumentMatcher(filter, 
                treeObjectType,
                msg));
       }

       filterLabel.pack(true);

        //Refresh is done automagically...
    }
    
    
    public void changeToolbarItem(TreeObject treeObject) {
        MToolBar toolbar = listTablePart.getToolbar();
        for (MToolBarElement tbElem : toolbar.getChildren()) {
            if(tbElem.getElementId().contentEquals("com.sebulli.fakturama.listview.document.add")) {
                HandledToolItemImpl toolItem = (HandledToolItemImpl)tbElem;
                if(treeObject.getNodeType() == TreeObjectType.DEFAULT_NODE) {
                    toolItem.setTooltip(msg.commandNewTooltip + " " + msg.getMessageFromKey(treeObject.getDocType().getSingularKey()));
                } else {
                    toolItem.setTooltip(msg.commandNewTooltip + " " + msg.getMessageFromKey(DocumentType.ORDER.getSingularKey()));
                }
            }
        }
        changePopupEntries(treeObject.getDocType());
    }

    /**
     * @param documentType
     * @param part
     */
    protected void changePopupEntries(DocumentType documentType) {
        BillingType selectedObjectType = (getSelectedObject() != null) ? getSelectedObject().getBillingType() : BillingType.NONE;

        // for controlling of the visibility of popup commands
        // according to the supplementary information (tag name) the visibility is set for the
        // appropriate commands.
        listTablePart.getMenus()
                .stream()
                .filter(menu -> menu.getElementId().contentEquals("com.sebulli.fakturama.document.popup"))
                .forEach(
                        popupMenu -> popupMenu.getChildren().stream().filter(entry -> entry.getTags().contains("orderActive"))
                                .forEach(foundEntry -> foundEntry.setVisible(documentType == DocumentType.ORDER
                                || selectedObjectType == BillingType.ORDER)));
        listTablePart.getMenus()
                .stream()
                .filter(menu -> menu.getElementId().contentEquals("com.sebulli.fakturama.document.popup"))
                .forEach(
                        popupMenu -> popupMenu.getChildren().stream().filter(entry -> entry.getTags().contains("deliveryActive"))
                                .forEach(foundEntry -> foundEntry.setVisible(documentType == DocumentType.DELIVERY
                                || selectedObjectType == BillingType.DELIVERY)));
        
        boolean canBePaid = java.util.Optional.ofNullable(documentType).orElse(DocumentType.NONE).canBePaid()
                || DocumentType.findByKey(selectedObjectType.getValue()).canBePaid();
        listTablePart.getMenus()
                .stream()
                .filter(menu -> menu.getElementId().contentEquals("com.sebulli.fakturama.document.popup"))
                .forEach(
                        popupMenu -> popupMenu.getChildren().stream().filter(entry -> entry.getTags().contains("canBePaidActive"))
                                .forEach(foundEntry -> foundEntry.setVisible(canBePaid)));
    }
    
    @Override
    public void setContactFilter(long filter) {
        // Set the label with the filter string
      Contact contact = contactsDAO.findById(filter);
      if(contact != null) {
        setCategoryFilter(contactUtil.getNameWithCompany(contact), TreeObjectType.CONTACTS_ROOTNODE);
//          filterLabel.setText(contactUtil.getNameWithCompany(contact));
//          filterLabel.pack(true);
      }
//
        // Reset transaction and category filter, set contact filter
      
//      contentProvider.setContactFilter(filter);
//      contentProvider.setTransactionFilter(-1);
//      contentProvider.setCategoryFilter("");

//      // Reset the addNew action. 
//      if (addNewAction != null) {
//          addNewAction.setCategory("");
//      }

//        this.refresh();
    }
    
    @Override
    public void setTransactionFilter(long filter,  TreeObject treeObject) {
        setCategoryFilter(Long.toString(filter), TreeObjectType.TRANSACTIONS_ROOTNODE);

//      // Set the label with the filter string
//      filterLabel.setText("Dieser Vorgang");
//      filterLabel.pack(true);

        // Reset category and contact filter, set transaction filter
//      contentProvider.setTransactionFilter(filter);
//      contentProvider.setContactFilter(-1);
//      contentProvider.setCategoryFilter("");

//      // Reset the addNew action. 
//      if (addNewAction != null) {
//          addNewAction.setCategory("");
//      }
//      this.refresh();
    }
    
    protected boolean isHeaderLabelEnabled() {
        return true;
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
        return DocumentEditor.EDITOR_ID;
    }

    class DocumentTableConfiguration extends AbstractRegistryConfiguration {

        @Override
        public void configureRegistry(IConfigRegistry configRegistry) {
            Style styleLeftAligned = new Style();
            styleLeftAligned.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
            Style styleRightAligned = new Style();
            styleRightAligned.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
            Style styleCentered = new Style();
            styleCentered.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
            CellPainterWrapper painter = new PaddingDecorator(new TextPainter(), 0, 7, 0, 7);

            // default style for most of the cells
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE, // attribute to apply
                    styleLeftAligned,                // value of the attribute
                    DisplayMode.NORMAL,              // apply during normal rendering i.e not during selection or edit
                    GridRegion.BODY.toString());     // apply the above for all cells with this label
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    painter,
                    DisplayMode.NORMAL,
                    GridRegion.BODY.toString());

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, 
                    new CellImagePainter(),
                    DisplayMode.NORMAL, ICON_CELL_LABEL);
//            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
//                    styleCentered,      
//                    DisplayMode.NORMAL,             
//                    ICON_CELL_LABEL); 

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, 
                    new CellPainterDecorator(new TextPainter(), CellEdgeEnum.LEFT, new CellImagePainter()),
                    DisplayMode.NORMAL, STATE_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new StateDisplayConverter(),
                    DisplayMode.NORMAL,
                    STATE_CELL_LABEL);
            
//            configRegistry.registerConfigAttribute(
//                    CellConfigAttributes.CELL_PAINTER,
//                    painter,
//                    DisplayMode.NORMAL,
//                    MONEYVALUE_CELL_LABEL);
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL,             
                    MONEYVALUE_CELL_LABEL ); 
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new MoneyDisplayConverter(),
                    DisplayMode.NORMAL,
                    MONEYVALUE_CELL_LABEL);

            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL,             
                    DATE_CELL_LABEL ); 
            SimpleDateFormat dateFormat = (SimpleDateFormat) SimpleDateFormat.getDateInstance(DateFormat.MEDIUM, LocaleUtil.getInstance().getDefaultLocale());
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultDateDisplayConverter(dateFormat.toPattern()),
                    DisplayMode.NORMAL,
                    DATE_CELL_LABEL);
        }
    }

    public void removeSelectedEntry() {
        if(selectionLayer.getFullySelectedRowPositions().length > 0) {
            Document objToDelete = gridLayer.getBodyDataProvider().getRowObject(selectionLayer.getFullySelectedRowPositions()[0]);
            try {
                // don't delete the entry because it could be referenced
                // from another entity
                objToDelete.setDeleted(Boolean.TRUE);
                documentsDAO.save(objToDelete);
            }
            catch (SQLException e) {
                log.error(e, "can't save the current Document: " + objToDelete.toString());
            }
    
            // Refresh the table view of all Document
            evtBroker.post("DocumentEditor", "update");
        } else {
            log.debug("no rows selected!");
        }
    }
    
    @Override
    public Document[] getSelectedObjects() {
        List<Document> selectedObjects = new ArrayList<>();
        int[] fullySelectedRowPositions = selectionLayer.getFullySelectedRowPositions();
        if(fullySelectedRowPositions.length > 0 && fullySelectedRowPositions[0] > -1) {
            for (int i = 0; i < fullySelectedRowPositions.length; i++) {
                selectedObjects.add(gridLayer.getBodyDataProvider().getRowObject(fullySelectedRowPositions[i]));
            }
        } else {
            log.debug("no rows selected!");
        }
        return selectedObjects.toArray(new Document[selectedObjects.size()]);
    }
    
    
    @Override
    public Document getSelectedObject() {
        Document[] selectedObjects = getSelectedObjects();
        return selectedObjects != null && selectedObjects.length > 0 ? selectedObjects[0] : null;
    }

    protected String getPopupId() {
        return POPUP_ID;
    }

    class StateDisplayConverter extends DisplayConverter {

        @Override
        public Object canonicalToDisplayValue(Object canonicalValue) {
            String retval = "";
            if (canonicalValue != null) {
                Icon value = (Icon) canonicalValue;
                switch (value) {
                case COMMAND_ORDER_PENDING:
                    retval = msg.documentOrderStateOpen;
                    break;
                case COMMAND_ORDER_SHIPPED:
                    retval = msg.documentOrderStateShipped;
                    break;
                case COMMAND_ORDER_PROCESSING:
                    retval = msg.documentOrderStateInprogress;
                    break;
                case COMMAND_CHECKED:
                    retval = msg.documentOrderStatePaid;
//                    retval = msg.documentOrderStateClosed;
                    break;
                case COMMAND_ERROR:
                    retval = msg.documentOrderStateUnpaid;
                    break;
                default:
                    break;
                }
            }
            return retval;
        }

        public Object displayToCanonicalValue(Object displayValue) {
            throw new UnsupportedOperationException("can't change the state in a list view!");
        }
    }
}
