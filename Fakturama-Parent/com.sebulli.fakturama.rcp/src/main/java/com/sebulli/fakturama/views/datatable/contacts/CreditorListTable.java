/**
 * 
 */
package com.sebulli.fakturama.views.datatable.contacts;

import javax.inject.Inject;

import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

import com.sebulli.fakturama.dao.ContactCategoriesDAO;
import com.sebulli.fakturama.dao.CreditorsDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.Address_;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.model.Creditor;
import com.sebulli.fakturama.model.Creditor_;
import com.sebulli.fakturama.parts.ContactEditor;
import com.sebulli.fakturama.views.datatable.ListViewGridLayer;

/**
 * View with the table of all contacts
 * 
 */
public class CreditorListTable extends ContactListTable<Creditor> {

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private Logger log;
    
    @Inject    
    private UISynchronize sync;

    @Inject
    private EHandlerService handlerService;

    @Inject
    private ECommandService commandService;

    // ID of this view
    public static final String ID = "fakturama.views.creditorTable";

    private static final String POPUP_ID = "com.sebulli.fakturama.creditorlist.popup";
    public static final String SELECTED_CREDITOR_ID = "fakturama.creditorlist.selectedcreditorid";
     
    /**
     * Event Broker for receiving update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

    private EventList<Contact> creditorListData;
    private EventList<ContactCategory> categories;

    private Control top;
    
    @Inject
    private CreditorsDAO creditorDAO;
    
    @Inject
    private ContactCategoriesDAO contactCategoriesDAO;
    
    private MPart listTablePart;
    
    private ListViewGridLayer<Contact> gridLayer;
    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    protected FilterList<Contact> treeFilteredIssues;
    private SelectionLayer selectionLayer;
//
//    @PostConstruct
//    public Control createPartControl(Composite parent, MPart listTablePart) {
//        log.info("create Contact list part");
//        top = super.createPartControl(parent, Contact.class, true, ID);
//        this.listTablePart = listTablePart;
//        // if another click handler is set we use it
//        // Listen to double clicks
//        Object commandId = this.listTablePart.getProperties().get("fakturama.datatable.creditors.clickhandler");
//        if(commandId != null) { // exactly it would be "com.sebulli.fakturama.command.selectitem"
//            hookDoubleClickCommand(natTable, gridLayer, (String) commandId);
//        } else {
//            hookDoubleClickCommand(natTable, gridLayer);
//        }
//        topicTreeViewer.setTable(this);
//        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
//        return top;
//    }
//    
//    private void hookDoubleClickCommand(final NatTable nattable, final ListViewGridLayer<Contact> gridLayer, String commandId) {
//        // Add a double click listener
//        nattable.getUiBindingRegistry().registerDoubleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), new IMouseAction() {
//
//            @Override
//            public void run(NatTable natTable, MouseEvent event) {
//                //get the row position for the click in the NatTable
//                int rowPos = natTable.getRowPositionByY(event.y);
//                //transform the NatTable row position to the row position of the body layer stack
//                int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPos, gridLayer.getBodyDataLayer());
//                // extract the selected Object
//                Contact selectedObject = gridLayer.getBodyDataProvider().getRowObject(bodyRowPos);
//                // Call the corresponding editor. The editor is set
//                // in the variable "editor", which is used as a parameter
//                // when calling the editor command.
//                // in E4 we create a new Part (or use an existing one with the same ID)
//                // from PartDescriptor
//                Map<String, Object> params = new HashMap<>();
//                ParameterizedCommand parameterizedCommand;
//                if(commandId != null) {
//                    // If we don't give a target document number the event will  be catched by *all*
//                    // open editors which listens to this event. This is (obviously :-) ) not
//                    // the intended behavior...
//                    Map<String, Object> eventParams = new HashMap<>();
//                    // the transientData HashMap contains the target document number
//                    // (was set in MouseEvent handler)
//                    eventParams.putAll(listTablePart.getParent().getTransientData());
//                    eventParams.put(SELECTED_CREDITOR_ID, Long.valueOf(selectedObject.getId()));
//                    evtBroker.post("DialogSelection/Contact", eventParams);
//                    listTablePart.getParent().setVisible(false);
//                } else {
//                    params.put(CallEditor.PARAM_OBJ_ID, Long.toString(selectedObject.getId()));
//                    params.put(CallEditor.PARAM_EDITOR_TYPE, getEditorId());
//                    parameterizedCommand = commandService.createCommand(CommandIds.CMD_CALL_EDITOR, params);
//                    handlerService.executeHandler(parameterizedCommand);
//                }
//            }
//        });
//    }
//    
//    @Override
//    protected void hookDoubleClickCommand(final NatTable nattable, final ListViewGridLayer<Contact> gridLayer) {
//        hookDoubleClickCommand(nattable, gridLayer, null);
//    }
//    
//    @Override
//    protected void postConfigureNatTable(NatTable natTable) {
//        //as the autoconfiguration of the NatTable is turned off, we have to add the 
//        //DefaultNatTableStyleConfiguration and the ConfigRegistry manually 
//        natTable.setConfigRegistry(configRegistry);
//        natTable.addConfiguration(new NoHeaderRowOnlySelectionBindings());
//        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
//        natTable.addConfiguration(new ContactTableConfiguration());
////        addCustomStyling(natTable);
//        // nur für das Headermenü, falls das mal irgendwann gebraucht werden sollte
//        //      natTable.addConfiguration(new HeaderMenuConfiguration(n6));
//
//        // Change the default sort key bindings. Note that 'auto configure' was turned off
//        // for the SortHeaderLayer (setup in the GlazedListsGridLayer)
//        natTable.addConfiguration(new SingleClickSortConfiguration());
//        natTable.configure();
//    }

//    /* (non-Javadoc)
//     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#createListTable(org.eclipse.swt.widgets.Composite)
//     */
//    @Override
//    protected NatTable createListTable(Composite searchAndTableComposite) {
//        // fill the underlying data source (GlazedList)
//        creditorListData = GlazedLists.eventList(contactDAO.findAll());
//
//        // get the visible properties to show in list view
//        String[] propertyNames = contactDAO.getVisibleProperties();
//
//        final IColumnPropertyAccessor<Contact> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<Contact>(propertyNames);
//        final IColumnPropertyAccessor<Contact> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<Contact>() {
//
//            public Object getDataValue(Contact rowObject, int columnIndex) {
//                ContactListDescriptor descriptor = ContactListDescriptor.getDescriptorFromColumn(columnIndex);
//                switch (descriptor) {
//                case NO:
//                case FIRSTNAME:
//                case LASTNAME:
//                case COMPANY:
//                case ZIP:
//                case CITY:
//                    return columnPropertyAccessor.getDataValue(rowObject, columnIndex);
//                default:
//                    break;
//                }
//                return null;
//            }
//
//            public void setDataValue(Contact rowObject, int columnIndex, Object newValue) {
//                throw new UnsupportedOperationException("you can't change a value in list view!");
//            }
//
//            public int getColumnCount() {
//                return columnPropertyAccessor.getColumnCount();
//            }
//
//            public String getColumnProperty(int columnIndex) {
//                ContactListDescriptor descriptor = ContactListDescriptor.getDescriptorFromColumn(columnIndex);
//                return msg.getMessageFromKey(descriptor.getMessageKey());
//            }
//
//            public int getColumnIndex(String propertyName) {
//                    return columnPropertyAccessor.getColumnIndex(propertyName);
//            }
//        };
//
//        //build the column header layer
//        // Column header data provider includes derived properties
//        IDataProvider columnHeaderDataProvider = new ListViewColumnHeaderDataProvider<Contact>(propertyNames, derivedColumnPropertyAccessor); 
//
//        // matcher input Search text field 
//        final MatcherEditor<Contact> textMatcherEditor = new TextWidgetMatcherEditor<Contact>(searchText, new ContactFilterator());
//        
//        // Filtered list for Search text field filter
//        final FilterList<Contact> textFilteredIssues = new FilterList<Contact>(creditorListData, textMatcherEditor);
//
//        // build the list for the tree-filtered values (i.e., the value list which is affected by
//        // tree selection)
//        treeFilteredIssues = new FilterList<Contact>(textFilteredIssues);
//
//        //create the body layer stack
//        final IRowDataProvider<Contact> firstBodyDataProvider = 
//                new GlazedListsDataProvider<Contact>(treeFilteredIssues, derivedColumnPropertyAccessor);
//        
//        //build the grid layer
//        gridLayer = new ListViewGridLayer<Contact>(treeFilteredIssues, derivedColumnPropertyAccessor, columnHeaderDataProvider, configRegistry, true);
//        DataLayer tableDataLayer = gridLayer.getBodyDataLayer();
//        tableDataLayer.setColumnPercentageSizing(true);
//        tableDataLayer.setColumnWidthPercentageByPosition(0, 5);
//        tableDataLayer.setColumnWidthPercentageByPosition(1, 15);
//        tableDataLayer.setColumnWidthPercentageByPosition(2, 75);
//        tableDataLayer.setColumnWidthPercentageByPosition(3, 5);
////        GlazedListsEventLayer<Contact> vatListEventLayer = new GlazedListsEventLayer<Contact>(tableDataLayer, creditorListData);
//
//        // add a label accumulator to be able to register converter
//        // this is crucial for using custom values display
////        vatListEventLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
//        
//        // Custom selection configuration
//        selectionLayer = gridLayer.getBodyLayerStack().getSelectionLayer();
//
//        IRowIdAccessor<Contact> rowIdAccessor = new IRowIdAccessor<Contact>() {
//            @Override
//            public Serializable getRowId(Contact rowObject) {
//                return rowObject.getId();
//            }
//        };
//        
//        //use a RowSelectionModel that will perform row selections and is able to identify a row via unique ID
//        RowSelectionModel<Contact> selectionModel = new RowSelectionModel<Contact>(selectionLayer, firstBodyDataProvider, rowIdAccessor, false);
//        selectionLayer.setSelectionModel(selectionModel);
////         Select complete rows
//        selectionLayer.addConfiguration(new RowOnlySelectionConfiguration<Contact>());
//
//        final NatTable natTable = new NatTable(searchAndTableComposite/*, 
//                SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | SWT.BORDER*/, gridLayer, false);
//        natTable.setBackground(GUIHelper.COLOR_WHITE);
//        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
//        natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));
//
//        // Register your custom cell painter, cell style, against the label applied to the cell.
//        //      addImageTextToColumn(configRegistry, natTable, gridLayer.getBodyDataProvider());
//        return natTable;
//    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#getTableId()
     */
    @Override
    public String getTableId() {
        return ID;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#getEditorId()
     */
    @Override
    protected String getEditorId() {
        return ContactEditor.ID;
    }

//    /* (non-Javadoc)
//     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#createCategoryTreeViewer(org.eclipse.swt.widgets.Composite)
//     */
//    @Override
//    protected TopicTreeViewer<ContactCategory> createCategoryTreeViewer(Composite top) {
//        topicTreeViewer = new TopicTreeViewer<ContactCategory>(top, msg, false, true);
//        categories = GlazedLists.eventList(contactCategoriesDAO.findAll());
//        topicTreeViewer.setInput(categories);
//        topicTreeViewer.setLabelProvider(new TreeCategoryLabelProvider());
//        return topicTreeViewer;
//    }
    
    @Inject @Optional
    public void handleRefreshEvent(@EventTopic("ContactEditor") String message) {
        sync.syncExec(new Runnable() {
            
            @Override
            public void run() {
                top.setRedraw(false);
            }
        });
        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
        creditorListData.clear();
        creditorListData.addAll(creditorDAO.findAll(true));
        categories.clear();
        categories.addAll(contactCategoriesDAO.findAll(true));
        sync.syncExec(new Runnable() {
           
            @Override
            public void run() {
                top.setRedraw(true);
            }});
    }

//    public void removeSelectedEntry() {
//        if(selectionLayer.getFullySelectedRowPositions().length > 0) {
//            Contact objToDelete = gridLayer.getBodyDataProvider().getRowObject(selectionLayer.getFullySelectedRowPositions()[0]);
//            try {
//                // don't delete the entry because it could be referenced
//                // from another entity
//                objToDelete.setDeleted(Boolean.TRUE);
//                contactDAO.save(objToDelete);
//            }
//            catch (SQLException e) {
//                log.error(e, "can't save the current Contact: " + objToDelete.toString());
//            }
//    
//            // Refresh the table view of all VATs
//            evtBroker.post("ContactEditor", "update");
//        } else {
//            log.debug("no rows selected!");
//        }
//    }
//
//    /* (non-Javadoc)
//     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#setCategoryFilter(java.lang.String, com.sebulli.fakturama.views.datatable.vats.TreeObjectType)
//     */
//    @Override
//    public void setCategoryFilter(String filter, TreeObjectType treeObjectType) {
//        treeFilteredIssues.setMatcher(new ContactMatcher(filter, treeObjectType,((TreeObject)topicTreeViewer.getTree().getTopItem().getData()).getName()));
//    }
//
//    /* (non-Javadoc)
//     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#isHeaderLabelEnabled()
//     */
//    @Override
//    protected boolean isHeaderLabelEnabled() {
//        return false;
//    }
//
//    /**
//     * @return the gridLayer
//     */
//    public ListViewGridLayer<Contact> getGridLayer() {
//        return gridLayer;
//    }
//    
//    class ContactTableConfiguration extends AbstractRegistryConfiguration {
//
//        @Override
//        public void configureRegistry(IConfigRegistry configRegistry) {
//            Style styleLeftAligned = new Style();
//            styleLeftAligned.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
//            Style styleRightAligned = new Style();
//            styleRightAligned.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
//            Style styleCentered = new Style();
//            styleCentered.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
//
//            // default style for the most of the cells
//            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, // attribute to apply
//                                                   styleLeftAligned,                // value of the attribute
//                                                   DisplayMode.NORMAL,              // apply during normal rendering i.e not during selection or edit
//                                                   GridRegion.BODY.toString());     // apply the above for all cells with this label
//        }
//    }
//
//    protected String getPopupId() {
//        return POPUP_ID;
//    }
//
//    @Override
//    protected String getToolbarAddItemCommandId() {
//        return CommandIds.LISTTOOLBAR_ADD_CONTACT;
//    }
//
//    @Override
//    protected MToolBar getMToolBar() {
//        return listTablePart.getToolbar();
//    }

    @Override
    protected EventList<Creditor> getListData(boolean forceRead) {
        return GlazedLists.eventList(creditorDAO.findAll(forceRead));
    }

    @Override
    protected MatcherEditor<Creditor> createTextWidgetMatcherEditor() {
        /*
        searchColumns[0] = "nr";
        searchColumns[1] = "firstname";
        searchColumns[2] = "name";
        searchColumns[3] = "company";
        searchColumns[4] = "zip";
        searchColumns[5] = "city";
 */
        return new TextWidgetMatcherEditor<Creditor>(searchText, 
                GlazedLists.textFilterator(Creditor.class, 
                        Creditor_.customerNumber.getName(),
                        Creditor_.firstName.getName(),
                        Creditor_.name.getName(),
                        Creditor_.company.getName(),
                        Creditor_.address.getName() + "." + Address_.zip.getName(),
                        Creditor_.address.getName() + "." + Address_.city.getName()
                        ));
    }
}
