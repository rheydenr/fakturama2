/**
 * 
 */
package com.sebulli.fakturama.views.datatable.contacts;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.extension.e4.selection.E4SelectionListener;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.sebulli.fakturama.dao.ContactCategoriesDAO;
import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.parts.DebitorEditor;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.parts.Editor;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.EntityGridListLayer;
import com.sebulli.fakturama.views.datatable.impl.NoHeaderRowOnlySelectionBindings;
import com.sebulli.fakturama.views.datatable.tree.ui.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeCategoryLabelProvider;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;

/**
 * View with the table of all contacts
 * 
 */
public abstract class ContactListTable<T extends Contact> extends AbstractViewDataTable<T, ContactCategory> {
    @Inject
    protected UISynchronize sync;
    
    @Inject
    protected IEclipseContext context;
    
    @Inject
    protected ESelectionService selectionService;

    // ID of this view
    public static final String ID = "fakturama.views.contactTable";

    private static final String POPUP_ID = "com.sebulli.fakturama.contactlist.popup";
    public static final String SELECTED_CONTACT_ID = "fakturama.contactlist.selectedcontactid";

    protected EventList<ContactCategory> categories;
    
    @Inject
    private ContactsDAO contactDAO;
    
    @Inject
    protected ContactCategoriesDAO contactCategoriesDAO;
    
    protected MPart listTablePart;

    private T selectedObject;

    private EntityGridListLayer<T> gridLayer;
    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    protected FilterList<T> treeFilteredIssues;

	private ContactMatcher currentFilter;

    @PostConstruct
    public Control createPartControl(Composite parent, MPart listTablePart) {
//        log.info("create Contact list part");
        super.createPartControl(parent, Contact.class, true, ID);
        this.listTablePart = listTablePart;
        // if another click handler is set we use it
        // Listen to double clicks
        Object commandId = this.listTablePart.getTransientData().get(Constants.PROPERTY_CONTACTS_CLICKHANDLER);
        if(commandId != null) { // exactly would it be Constants.COMMAND_SELECTITEM
            hookDoubleClickCommand(natTable, getGridLayer(), (String) commandId);
        } else {
            hookDoubleClickCommand2(natTable, getGridLayer());
        }
        topicTreeViewer.setTable(this);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(top);
        return top;
    }

    private void hookDoubleClickCommand(final NatTable nattable, final EntityGridListLayer<T> gridLayer, String commandId) {
        
        if (commandId != null) {
            // if we are in "selectaddress" mode we have to register a single click mouse event
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
                    eventParams.put(SELECTED_CONTACT_ID, Long.valueOf(selectedObject.getId()));
//                    // alternatively use the Selection Service
                    // ==> no! Because this SelectionService has another context than 
                    // the receiver of this topic. Therefore the receiver's SelectionService
                    // is empty :-(
//                    selectionService.setSelection(selectedObject);
                    
                    // selecting an entry and closing the dialog are two different actions.
                    // the "CloseContact" event is caught by SelectContactDialog#handleDialogDoubleClickClose. 
                    evtBroker.post("DialogSelection/Contact", eventParams);
                    evtBroker.post("DialogAction/CloseContact", eventParams);
                } else {
                    // if we come from the list view then we should open a new editor 
                    params.put(CallEditor.PARAM_OBJ_ID, Long.toString(selectedObject.getId()));
                    params.put(CallEditor.PARAM_EDITOR_TYPE, getEditorId());
                    context.getParent().get(ESelectionService.class).setSelection(null);
                    parameterizedCommand = commandService.createCommand(CommandIds.CMD_CALL_EDITOR, params);
                    handlerService.executeHandler(parameterizedCommand);
                }
            }
        });
    }
    
    @Override
    public T getSelectedObject() {
        return selectedObject;
    }
    
    @Override
    protected void hookDoubleClickCommand2(final NatTable nattable, final EntityGridListLayer<T> gridLayer) {
        hookDoubleClickCommand(nattable, gridLayer, null);
    }
    
    @Override
    protected void postConfigureNatTable(NatTable natTable) {
        //as the autoconfiguration of the NatTable is turned off, we have to add the 
        //DefaultNatTableStyleConfiguration and the ConfigRegistry manually 
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new NoHeaderRowOnlySelectionBindings());
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new ContactTableConfiguration());
        // nur für das Headermenü, falls das mal irgendwann gebraucht werden sollte
        //      natTable.addConfiguration(new HeaderMenuConfiguration(n6));

        gridLayer.getSelectionLayer().getSelectionModel().setMultipleSelectionAllowed(true);

        E4SelectionListener<T> esl = new E4SelectionListener<>(selectionService, gridLayer.getSelectionLayer(), gridLayer.getBodyDataProvider());
        gridLayer.getSelectionLayer().addLayerListener(esl);

        // Change the default sort key bindings. Note that 'auto configure' was turned off
        // for the SortHeaderLayer (setup in the GlazedListsGridLayer)
        natTable.addConfiguration(new SingleClickSortConfiguration());
        natTable.configure();
    }

    private IColumnPropertyAccessor<T> createColumnPropertyAccessor(String[] propertyNames) {
        final IColumnPropertyAccessor<T> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<T>(propertyNames);
        IColumnPropertyAccessor<T> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<T>() {

            public Object getDataValue(T rowObject, int columnIndex) {
                ContactListDescriptor descriptor = ContactListDescriptor.getDescriptorFromColumn(columnIndex);
                // For the address always the first entry is displayed (if any)
                switch (descriptor) {
                case NO:
                case FIRSTNAME:
                case LASTNAME:
                    return columnPropertyAccessor.getDataValue(rowObject, columnIndex);
                case ZIP:
                	if(!rowObject.getAddresses().isEmpty()) {
                		// display only the first address's values
                		Optional<Address> firstAddress = getFirstAddress(rowObject);
						return firstAddress.isPresent() ? firstAddress.get().getZip() : "";
                	}
                	break;
                case CITY:
                	if(!rowObject.getAddresses().isEmpty()) {
                		Optional<Address> firstAddress = getFirstAddress(rowObject);
						return firstAddress.isPresent() ? firstAddress.get().getCity() : "";
                	}
                	break;
                case COMPANY:
                	String value = (String) columnPropertyAccessor.getDataValue(rowObject, columnIndex);
                	if(value != null) {
                		return StringUtils.substringBefore(value, StringUtils.CR);
                	}
                default:
                    break;
                }
                return null;
            }

            private Optional<Address> getFirstAddress(T rowObject) {
            	return rowObject.getAddresses().stream().min(Comparator.comparingLong(Address::getId));
			}

			public void setDataValue(Contact rowObject, int columnIndex, Object newValue) {
                throw new UnsupportedOperationException("you can't change a value in list view!");
            }

            public int getColumnCount() {
                return columnPropertyAccessor.getColumnCount();
            }

            public String getColumnProperty(int columnIndex) {
                ContactListDescriptor descriptor = ContactListDescriptor.getDescriptorFromColumn(columnIndex);
                return msg.getMessageFromKey(descriptor.getMessageKey());
            }

            public int getColumnIndex(String propertyName) {
                    return columnPropertyAccessor.getColumnIndex(propertyName);
            }
        };
        return derivedColumnPropertyAccessor;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#createListTable(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected NatTable createListTable(Composite searchAndTableComposite) {
        // fill the underlying data source (GlazedList)
    	EventList<T> contactListData = getListData(true);

        // get the visible properties to show in list view
        String[] propertyNames = contactDAO.getVisibleProperties();

        final IColumnPropertyAccessor<T> derivedColumnPropertyAccessor = createColumnPropertyAccessor(propertyNames);

        // matcher input Search text field 
        final MatcherEditor<T> textMatcherEditor = createTextWidgetMatcherEditor();
        
        // Filtered list for Search text field filter
        // build the list for the tree-filtered values (i.e., the value list which is affected by
        // tree selection)
        treeFilteredIssues = new FilterList<T>(contactListData, textMatcherEditor);
        
        //build the grid layer
        setGridLayer(new EntityGridListLayer<T>(treeFilteredIssues, propertyNames, derivedColumnPropertyAccessor, configRegistry));
        DataLayer tableDataLayer = gridLayer.getBodyDataLayer();
        tableDataLayer.setColumnPercentageSizing(true);
        tableDataLayer.setColumnWidthPercentageByPosition(0, 5);
        tableDataLayer.setColumnWidthPercentageByPosition(1, 15);
//        tableDataLayer.setColumnWidthPercentageByPosition(2, 75);
//        tableDataLayer.setColumnWidthPercentageByPosition(3, 5);

        final NatTable natTable = new NatTable(searchAndTableComposite/*, 
                SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | SWT.BORDER*/, gridLayer.getGridLayer(), false);
        natTable.setBackground(GUIHelper.COLOR_WHITE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));
        
        return natTable;
    }

    protected abstract MatcherEditor<T> createTextWidgetMatcherEditor();

    protected abstract EventList<T> getListData(boolean forceRead);
    
    @Override
    protected T handleCascadeDelete(T objToDelete) {
    	// set all addresses to deleted
    	objToDelete.getAddresses().forEach(adr -> adr.setDeleted(true));
    	return objToDelete;
    }

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
        return DebitorEditor.ID;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#createCategoryTreeViewer(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected TopicTreeViewer<ContactCategory> createCategoryTreeViewer(Composite top) {
//        topicTreeViewer = new TopicTreeViewer<ContactCategory>(top, msg, false, true);

        context.set(TopicTreeViewer.PARENT_COMPOSITE, top);
        context.set(TopicTreeViewer.USE_DOCUMENT_AND_CONTACT_FILTER, false);
        context.set(TopicTreeViewer.USE_ALL, true);
        
    	topicTreeViewer = (TopicTreeViewer<ContactCategory>)ContextInjectionFactory.make(TopicTreeViewer.class, context);
        categories = GlazedLists.eventList(contactCategoriesDAO.findAll());
        topicTreeViewer.setInput(categories);
        topicTreeViewer.setLabelProvider(new TreeCategoryLabelProvider());
        return topicTreeViewer;
    }
    
    public void handleRefreshEvent(String message) {
    	if(StringUtils.equals(message, Editor.UPDATE_EVENT) && !top.isDisposed()) {
	        sync.syncExec(() -> top.setRedraw(false));
	        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
	        GlazedLists.replaceAll(treeFilteredIssues, getListData(true), false);
	        GlazedLists.replaceAll(categories, GlazedLists.eventList(contactCategoriesDAO.findAll(true)), false);
	        sync.syncExec(() -> top.setRedraw(true));
    	}
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#setCategoryFilter(java.lang.String, com.sebulli.fakturama.views.datatable.vats.TreeObjectType)
     */
    @Override
    public void setCategoryFilter(String filter, TreeObjectType treeObjectType) {
        currentFilter = new ContactMatcher(filter, treeObjectType, createRootNodeDescriptor(filter));
		treeFilteredIssues.setMatcher(currentFilter);
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#isHeaderLabelEnabled()
     */
    @Override
    protected boolean isHeaderLabelEnabled() {
        return false;
    }

    /**
     * @return the gridLayer
     */
    public EntityGridListLayer<T> getGridLayer() {
        return gridLayer;
    }
    
    class ContactTableConfiguration extends AbstractRegistryConfiguration {

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
        }
    }

    protected String getPopupId() {
        return POPUP_ID;
    }

    @Override
    protected String getToolbarAddItemCommandId() {
        return CommandIds.LISTTOOLBAR_ADD_CONTACT;
    }

    @Override
    protected MToolBar getMToolBar() {
        return listTablePart.getToolbar();
    }

    @Focus
    public void focus() {
        if(natTable != null) {
            natTable.setFocus();
        }
    }
    /**
     * @param gridLayer the gridLayer to set
     */
    private void setGridLayer(EntityGridListLayer<T> gridLayer) {
        this.gridLayer = gridLayer;
    }
}
