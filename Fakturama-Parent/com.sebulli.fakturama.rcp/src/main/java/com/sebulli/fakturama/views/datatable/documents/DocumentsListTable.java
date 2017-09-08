/**
 * 
 */
package com.sebulli.fakturama.views.datatable.documents;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.persistence.PersistenceException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractParameterValueConverter;
import org.eclipse.core.commands.CommandManager;
import org.eclipse.core.commands.ParameterType;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBarElement;
import org.eclipse.e4.ui.model.application.ui.menu.impl.HandledToolItemImpl;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDateDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.IDisplayConverter;
import org.eclipse.nebula.widgets.nattable.extension.e4.selection.E4SelectionListener;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
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

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.handlers.paramconverter.LongParameterValueConverter;
import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.i18n.MessageRegistry;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.Document_;
import com.sebulli.fakturama.model.DummyStringCategory;
import com.sebulli.fakturama.model.Dunning;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.parts.Editor;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.startup.ConfigurationManager;
import com.sebulli.fakturama.util.ContactUtil;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.CellImagePainter;
import com.sebulli.fakturama.views.datatable.EntityGridListLayer;
import com.sebulli.fakturama.views.datatable.MoneyDisplayConverter;
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
 * Builds the Document list table.
 */
public class DocumentsListTable extends AbstractViewDataTable<Document, DummyStringCategory> {

    //  this is for synchronizing the UI thread
    @Inject
    private UISynchronize sync;

    // ID of this view
    public static final String ID = "fakturama.views.documentTable";     
    
    protected static final String POPUP_ID = "com.sebulli.fakturama.document.popup";
    public static final String SELECTED_DELIVERY_ID = "fakturama.deliverylist.selecteddeliveryid";

    @Inject
    private IEclipseContext context;
	
    @Inject
    @Preference   //(value=InstanceScope.SCOPE)
    private IEclipsePreferences eclipsePrefs;
    
    private EventList<Document> documentListData;
    private EventList<DummyStringCategory> categories;
    
    @Inject
    private DocumentsDAO documentsDAO;
    
    @Inject
    private ContactsDAO contactsDAO;
    
    private EntityGridListLayer<Document> gridLayer;
    
//    @Inject
//    private EHelpService helpService;
    
    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    protected FilterList<Document> treeFilteredIssues;

    private ContactUtil contactUtil;

    private MPart listTablePart;

    private Document selectedObject;
    
    @Inject
    private CommandManager cmdMan;    	

    @Inject
    protected MessageRegistry registry;

	private DocumentMatcher currentFilter;
    
    @PostConstruct
    public Control createPartControl(Composite parent, MPart listTablePart) {
        log.info("create Document list part");
        this.listTablePart = listTablePart;
        if(!eclipsePrefs.get(ConfigurationManager.GENERAL_WORKSPACE_REQUEST, "").isEmpty()) {
        	return null;
        }
        // This is for text only!!!
        ParameterType parameterType = cmdMan.getParameterType("myParam");
    	AbstractParameterValueConverter parameterTypeConverter = new LongParameterValueConverter();
		parameterType.define("myParam", parameterTypeConverter);
        // +++ END TEST +++
        
        super.createPartControl(parent, Document.class, true, ID);

        // if another click handler is set we use it
        // Listen to double clicks
        Object commandId = this.listTablePart.getProperties().get(Constants.PROPERTY_DELIVERIES_CLICKHANDLER);
        if(commandId != null) { // exactly would it be Constants.COMMAND_SELECTITEM
            hookDoubleClickCommand(natTable, getGridLayer(), (String) commandId);
        } else {
            hookDoubleClickCommand2(natTable, getGridLayer());
	        topicTreeViewer.setTable(this);
	        
	        // On creating, set the unpaid invoices
	        topicTreeViewer.selectItemByName(
	                String.format("%s/%s", 
	                        msg.getMessageFromKey(DocumentType.INVOICE.getPluralDescription()), 
	                        msg.documentOrderStateUnpaid)
	                );
        }

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);        
        GridDataFactory.fillDefaults().grab(true, true).applyTo(top);
        contactUtil = ContextInjectionFactory.make(ContactUtil.class, context);
        
        return top;
    }
    
    private void hookDoubleClickCommand(final NatTable nattable, final EntityGridListLayer<Document> gridLayer, String commandId) {
        
        if (commandId != null) {
            // if we are in "selectdelivery" mode we have to register a single click mouse event
            nattable.getUiBindingRegistry().registerFirstSingleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), new IMouseAction() {
                public void run(NatTable natTable, MouseEvent event) {
                    int rowPos = natTable.getRowPositionByY(event.y);
                    int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPos, gridLayer.getBodyDataLayer());
                    selectedObject = gridLayer.getBodyDataProvider().getRowObject(bodyRowPos);
//                    selectionService.setSelection(selectionService);
                }
            });
        }
        // Add a double click listener
        nattable.getUiBindingRegistry().registerDoubleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), new IMouseAction() {

            @Override
            public void run(NatTable natTable, MouseEvent event) {
                //get the row position for the click in the NatTable
                int rowPos = natTable.getRowPositionByY(event.y);
                //transform the NatTable row position to the row position of the body layer stack
                int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPos, gridLayer.getBodyDataLayer());
                selectedObject = gridLayer.getBodyDataProvider().getRowObject(bodyRowPos);
                // Call the corresponding editor. The editor is set
                // in the variable "editor", which is used as a parameter
                // when calling the editor command.
                // in E4 we create a new Part (or use an existing one with the same ID)
                // from PartDescriptor
                Map<String, Object> params = new HashMap<>();
                ParameterizedCommand parameterizedCommand;
                if(commandId != null) {
                    // If we don't give a target document number the event will  be catched by *all*
                    // open editors which listens to this event. This is (obviously :-) ) not
                    // the intended behavior...
                    Map<String, Object> eventParams = new HashMap<>();
                    // the transientData HashMap contains the target document number
                    // (was set in MouseEvent handler)
                    eventParams.put(DocumentEditor.DOCUMENT_ID, context.get(DocumentEditor.DOCUMENT_ID));
                    // TODO how about multiple selections?
                    List<Document> resultList = Arrays.asList(getSelectedObjects());
                    eventParams.put(SELECTED_DELIVERY_ID, resultList);
//                    // alternatively use the Selection Service
                    // ==> no! Because this SelectionService has another context than 
                    // the receiver of this topic. Therefore the receiver's SelectionService
                    // is empty :-(
//                    selectionService.setSelection(selectedObject);
                    
                    // selecting an entry and closing the dialog are two different actions.
                    // the "CloseContact" event is caught by SelectContactDialog#handleDialogDoubleClickClose. 
                    evtBroker.post("DialogAction/CloseDelivery", eventParams);
                } else {
                    // if we come from the list view then we should open a new editor 
                    params.put(CallEditor.PARAM_OBJ_ID, Long.toString(selectedObject.getId()));
                    params.put(CallEditor.PARAM_EDITOR_TYPE, getEditorId());
                    params.putAll(getAdditionalParameters());
                    parameterizedCommand = commandService.createCommand(CommandIds.CMD_CALL_EDITOR, params);
                    handlerService.executeHandler(parameterizedCommand);
                }
            }
        });
    }
    
    @Override
    protected void hookDoubleClickCommand2(final NatTable nattable, final EntityGridListLayer<Document> gridLayer) {
        hookDoubleClickCommand(nattable, gridLayer, null);
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.AbstractViewDataTable#getAdditionalParameters()
     */
    @Override
    protected Map<String, Object> getAdditionalParameters() {
    	Map<String, Object> params = new HashMap<>();
        params.put(CallEditor.PARAM_CATEGORY, ((Document)selectedObject).getBillingType().getName());
        return params;
    }
    
    @Override
    protected void postConfigureNatTable(NatTable natTable) {
        //as the autoconfiguration of the NatTable is turned off, we have to add the 
        //DefaultNatTableStyleConfiguration and the ConfigRegistry manually 
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new NoHeaderRowOnlySelectionBindings());
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new DocumentTableConfiguration());

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
                        if (selectedObject != null && topicTreeViewer != null) {
                            if(selectedObject.getTransactionId() != null) {
                                topicTreeViewer.setTransaction(selectedObject.getTransactionId());
                            } else {
                            	// reset transaction id
                            	topicTreeViewer.setTransaction(Long.valueOf(-1));
                            }
                            topicTreeViewer.setContactFromDocument(selectedObject);
                            changePopupEntries(null);
                        }
                    }
                }
            }
        });
        
        gridLayer.getSelectionLayer().getSelectionModel().setMultipleSelectionAllowed(true);
        
        E4SelectionListener<Document> esl = new E4SelectionListener<>(selectionService, gridLayer.getSelectionLayer(), gridLayer.getBodyDataProvider());
        gridLayer.getSelectionLayer().addLayerListener(esl);

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
                            changePopupEntries(null);
                        }                   
                    }
                });

        natTable.configure();
    }
    
    @Override
    protected Class<Document> getEntityClass() {
    	return Document.class;
    }
    
    private IColumnPropertyAccessor<Document> createColumnPropertyAccessor(String[] propertyNames) {
        final IColumnPropertyAccessor<Document> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<Document>(propertyNames);
        final SpecialCellValueProvider specialCellValueProvider = new SpecialCellValueProvider(msg);
        final IColumnPropertyAccessor<Document> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<Document>() {

            public Object getDataValue(Document rowObject, int columnIndex) {
                DocumentListDescriptor descriptor = DocumentListDescriptor.getDescriptorFromColumn(columnIndex);
                switch (descriptor) {
                case ICON:
                case STATE:
                case PRINTED:
                    return specialCellValueProvider.getDataValue(rowObject, descriptor);
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
        return derivedColumnPropertyAccessor;
    }
    
    protected NatTable createListTable(Composite searchAndTableComposite) {
        // fill the underlying data source (GlazedList)
    	if(this.listTablePart.getProperties().get(Constants.PROPERTY_DELIVERIES_CLICKHANDLER) != null) {
    		// if a click handler is set we are in "dialog" mode which only uses delivery notes.
    		documentListData = GlazedLists.eventList(documentsDAO.findAllDeliveriesWithoutInvoice());
    	} else {
    		documentListData = GlazedLists.eventList(documentsDAO.findAll(true));
    	}

        // get the visible properties to show in list view
        String[] propertyNames = documentsDAO.getVisibleProperties();
        // Add derived 'default' column
        final IColumnPropertyAccessor<Document> derivedColumnPropertyAccessor = createColumnPropertyAccessor(propertyNames);

//        //build the column header layer
//        // Column header data provider includes derived properties
//        IDataProvider columnHeaderDataProvider = new ListViewColumnHeaderDataProvider<Document>(propertyNames, derivedColumnPropertyAccessor); 

        /*
        // Mark the columns that are used by the search function.
        searchColumns = new String[4];
        searchColumns[0] = "name";
        searchColumns[1] = "date";
        searchColumns[2] = "addressfirstline";
        searchColumns[3] = "total";
 */
        final MatcherEditor<Document> textMatcherEditor = new TextWidgetMatcherEditor<Document>(searchText, GlazedLists.textFilterator(Document.class,
                Document_.name.getName(), Document_.addressFirstLine.getName()));
        
        // Filtered list for Search text field filter
        final FilterList<Document> textFilteredIssues = new FilterList<Document>(documentListData, textMatcherEditor);

        // build the list for the tree-filtered values (i.e., the value list which is affected by
        // tree selection)
        treeFilteredIssues = new FilterList<Document>(textFilteredIssues);
        
        //build the grid layer
        gridLayer = new EntityGridListLayer<>(treeFilteredIssues, propertyNames, derivedColumnPropertyAccessor, configRegistry);
        DataLayer tableDataLayer = gridLayer.getBodyDataLayer();
        tableDataLayer.setColumnPercentageSizing(true);
//        Arrays.stream(DocumentListDescriptor.values()).forEach(
//                descriptor -> tableDataLayer.setColumnWidthPercentageByPosition(descriptor.getPosition(), descriptor.getDefaultWidth()));

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
                SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | SWT.BORDER*/, gridLayer.getGridLayer(), false);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));
        
        // Register label accumulator
        gridLayer.getBodyLayerStack().setConfigLabelAccumulator(columnLabelAccumulator);

        return natTable;
    }
    
    /**
     * @return the gridLayer
     */
    protected EntityGridListLayer<Document> getGridLayer() {
        return gridLayer;
    }

    @Override
    protected TopicTreeViewer<DummyStringCategory> createCategoryTreeViewer(Composite top) {
        Object commandId = this.listTablePart.getProperties().get(Constants.PROPERTY_DELIVERIES_CLICKHANDLER);
        if(commandId != null) { // exactly would it be Constants.COMMAND_SELECTITEM
        	topicTreeViewer = null;
        } else {
	        context.set("useDocumentAndContactFilter", true);
	        context.set("useAll", false);
	        //    	topicTreeViewer = (TopicTreeViewer<DummyStringCategory>)ContextInjectionFactory.make(TopicTreeViewer.class, context);
	        try {
				categories = GlazedLists.eventList(documentsDAO.getCategoryStrings());
				topicTreeViewer = new TopicTreeViewer<DummyStringCategory>(top, msg, true, false);
				topicTreeViewer.setInput(categories);
				topicTreeViewer.setLabelProvider(new TreeCategoryLabelProvider());
			} catch (PersistenceException e) {
				// if no database is created an exception occurs at this point
				log.warn("Category tree couldn't be created, perhaps because of initially startup?");
			}
        }
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
    public void handleRefreshEvent(@EventTopic(DocumentEditor.EDITOR_ID) String message) {
    	if(StringUtils.equals(message, Editor.UPDATE_EVENT)) {
	        sync.syncExec(() -> top.setRedraw(false));
	        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
	        GlazedLists.replaceAll(documentListData, GlazedLists.eventList(documentsDAO.findAll(true)), false);
	        // the tree is static, so here we don't have to update it (only re-read the filter)
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
        // Set the label with the filter string
        if (!filter.equals(NO_CATEGORY_LABEL)) {
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
                        // bind myFirstLabel via method reference
                        registry.register(filterLabel::setText, (msg) -> msg.topictreeLabelThistransaction);
                    } else {
                        filterLabel.setText(StringUtils.removeStart(filter, "/"));
                    }
                }
            }
 
        currentFilter = new DocumentMatcher(filter, 
			        treeObjectType,
			        msg);
		treeFilteredIssues.setMatcher(currentFilter);
//        filterLabel.setToolTipText("ouch!");
        }

       filterLabel.pack(true);

        //Refresh is done automagically...
    }
    
    public void changeToolbarItem(TreeObject treeObject) {
        MToolBar toolbar = listTablePart.getToolbar();
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
                else {
                    // default "add" document type is "Order"
                    toolItem.setTooltip(msg.commandNewTooltip + " " + msg.getMessageFromKey(DocumentType.ORDER.getSingularKey()));
                    parameterMap.put(CallEditor.PARAM_CATEGORY, DocumentType.ORDER.name());
                }
                if (wbCommand != null) {
                    wbCommand = ParameterizedCommand.generateCommand(wbCommand.getCommand(), parameterMap);
                }
                else {
                    // during the initialization phase the command is null, therefore we have to create a 
                    // new command
                    parameterMap.put(CallEditor.PARAM_EDITOR_TYPE, DocumentEditor.ID);
                    wbCommand = commandService.createCommand(CommandIds.CMD_CALL_EDITOR, parameterMap);
                }
                toolItem.setWbCommand(wbCommand);
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
                .filter(menu -> menu.getElementId().contentEquals(POPUP_ID))
                .forEach(popupMenu -> popupMenu.getChildren().stream()
                		.filter(entry -> entry.getTags().contains("orderActive"))
                        .forEach(foundEntry -> foundEntry.setVisible(documentType == DocumentType.ORDER
                                || selectedObjectType == BillingType.ORDER)));
        listTablePart.getMenus()
                .stream()
                .filter(menu -> menu.getElementId().contentEquals(POPUP_ID))
                .forEach(
                        popupMenu -> popupMenu.getChildren().stream().filter(entry -> entry.getTags().contains("deliveryActive"))
                                .forEach(foundEntry -> foundEntry.setVisible(documentType == DocumentType.DELIVERY
                                || selectedObjectType == BillingType.DELIVERY)));
        
        boolean canBePaid = java.util.Optional.ofNullable(documentType).orElse(DocumentType.NONE).canBePaid()
                || DocumentType.findByKey(selectedObjectType.getValue()).canBePaid();
        listTablePart.getMenus()
                .stream()
                .filter(menu -> menu.getElementId().contentEquals(POPUP_ID))
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
        return DocumentEditor.ID;
    }
    
    @Override
    protected String getEditorTypeId() {
        return DocumentEditor.class.getSimpleName();
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
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                    styleCentered,      
                    DisplayMode.NORMAL,             
                    ICON_CELL_LABEL); 

            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, 
                    new CellPainterDecorator(new TextPainter(), CellEdgeEnum.LEFT, new CellImagePainter()),
                    DisplayMode.NORMAL, STATE_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new StateDisplayConverter(),
                    DisplayMode.NORMAL,
                    STATE_CELL_LABEL);

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
    
    @Override
    public Document[] getSelectedObjects() {
        List<Document> selectedObjects = new ArrayList<>();
        int[] fullySelectedRowPositions = gridLayer.getSelectionLayer().getFullySelectedRowPositions();
        if(fullySelectedRowPositions.length > 0 && fullySelectedRowPositions[0] > -1) {
            for (int i = 0; i < fullySelectedRowPositions.length; i++) {
                selectedObjects.add(gridLayer.getBodyDataProvider().getRowObject(fullySelectedRowPositions[i]));
            }
        } else {
            log.debug("no rows selected!");
        }
        Document[] retArr = selectedObjects.toArray(new Document[selectedObjects.size()]);
        selectionService.setSelection(selectedObjects);
        return retArr;
    }
    
    
    @Override
    public Document getSelectedObject() {
        Document[] selectedObjects = getSelectedObjects();
        return selectedObjects != null && selectedObjects.length > 0 ? selectedObjects[0] : null;
    }

    protected String getPopupId() {
        return POPUP_ID;
    }

    class StateDisplayConverter implements IDisplayConverter {

        @Override
        public Object canonicalToDisplayValue(Object canonicalValue) {
            return "!!!CHECK StateDisplayConverter " + canonicalValue;
        }

        public Object displayToCanonicalValue(Object displayValue) {
            throw new UnsupportedOperationException("can't change the state in a list view!");
        }

        @Override
        public Object canonicalToDisplayValue(ILayerCell cell,
                IConfigRegistry configRegistry, Object canonicalValue) {
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
                    /* only for dunnings: We have to show the count of current dunning.
                     * Therefore we have to extract the currently displayed value and 
                     * look at the dunning level.
                     */
                    Document rowObject = gridLayer.getBodyDataProvider().getRowObject(cell.getRowIndex());
                    if(rowObject.getBillingType() == BillingType.DUNNING) {
                        int dunningLevel = ((Dunning)rowObject).getDunningLevel();
                        //T: Marking of a dunning in the document table.
                        //T: Format: "Dunning No. xx"
                        retval = MessageFormat.format(msg.documentDunningStatemarkerName, dunningLevel);
                    } else {
                        retval = msg.documentOrderStateUnpaid;
                    }
                    break;
                default:
                    break;
                }
            }
            return retval;
        }

        @Override
        public Object displayToCanonicalValue(ILayerCell cell,
                IConfigRegistry configRegistry, Object displayValue) {
            return displayToCanonicalValue(displayValue);
        }
    }

    @Override
    protected String getToolbarAddItemCommandId() {
        return CommandIds.LISTTOOLBAR_ADD_DOCUMENT;
    }

    @Override
    protected MToolBar getMToolBar() {
        return listTablePart.getToolbar();
    }

    @Override
    protected AbstractDAO<Document> getEntityDAO() {
        return documentsDAO;
    }
}
