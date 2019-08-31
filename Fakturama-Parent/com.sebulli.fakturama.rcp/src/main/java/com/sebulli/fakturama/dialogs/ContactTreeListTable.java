/**
 * 
 */
package com.sebulli.fakturama.dialogs;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ParameterizedCommand;
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
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDateDisplayConverter;
import org.eclipse.nebula.widgets.nattable.extension.e4.selection.E4SelectionListener;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeData;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeRowModel;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionConfiguration;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.tree.config.TreeLayerExpandCollapseKeyBindings;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.sebulli.fakturama.dao.ContactCategoriesDAO;
import com.sebulli.fakturama.dao.DebitorAddress;
import com.sebulli.fakturama.dto.DocumentItemDTO;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.parts.DebitorEditor;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.parts.Editor;
import com.sebulli.fakturama.views.datatable.GlazedListsColumnHeaderLayerStack;
import com.sebulli.fakturama.views.datatable.ListViewColumnHeaderDataProvider;
import com.sebulli.fakturama.views.datatable.contacts.ContactListDescriptor;
import com.sebulli.fakturama.views.datatable.contacts.ContactMatcher;
import com.sebulli.fakturama.views.datatable.impl.NoHeaderRowOnlySelectionBindings;
import com.sebulli.fakturama.views.datatable.tree.ui.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeCategoryLabelProvider;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.TreeList;
import ca.odell.glazedlists.matchers.MatcherEditor;

/**
 * View with the table of all contacts
 * @param <K>
 * @param <A>
 * 
 */
public abstract class ContactTreeListTable<K extends DebitorAddress> extends AbstractTreeViewDataTable<K, ContactCategory> {
    @Inject
    protected UISynchronize sync;
    
    @Inject
    protected IEclipseContext context;
    
    @Inject
    protected ESelectionService selectionService;
//    ListDataProvider<K> bodyDataProvider;
    
    // ID of this view
    public static final String ID = "fakturama.views.contactTreeTable";

    private static final String POPUP_ID = "com.sebulli.fakturama.contactlist.popup";
    public static final String SELECTED_CONTACT_ID = "fakturama.contactlist.selectedcontactid";
	public static final String SELECTED_ADDRESS_ID = "fakturama.treecontactlist.selectedcontactid";

    protected EventList<DebitorAddress> contactListData;
    protected EventList<ContactCategory> categories;
    
    @Inject
    protected ContactCategoriesDAO contactCategoriesDAO;
    
    protected MPart listTablePart;

    private K selectedObject;
    private ContactType contactType;

    private TreeLayer gridLayer;
    
    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    protected FilterList<DebitorAddress> treeFilteredIssues;

	private ContactMatcher currentFilter;

//	protected DataLayer bodyDataLayer;

	private TreeBodyLayerStack<DebitorAddress> bodyLayerStack;

	@PostConstruct
	public Control createPartControl(Composite parent, MPart listTablePart) {
		createPartControl(parent, Contact.class, true, ID);

		this.listTablePart = listTablePart;
		// if another click handler is set we use it
		// Listen to double clicks
		Object commandId = this.listTablePart.getProperties().get(Constants.PROPERTY_CONTACTS_CLICKHANDLER);
		if (commandId != null) { // exactly would it be Constants.COMMAND_SELECTITEM
			hookDoubleClickCommand(natTable, getGridLayer(), (String) commandId);
		} else {
			hookDoubleClickCommand2(natTable, getGridLayer());
		}

		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(top);
		return top;
	}

	private void setColumWidthPercentage(DataLayer dataLayer) {
		dataLayer.setColumnPercentageSizing(true);
//		dataLayer.setColumnWidthPercentageByPosition(0, 50);
//		dataLayer.setColumnWidthPercentageByPosition(1, 50);
	}

    private void hookDoubleClickCommand(final NatTable nattable, final TreeLayer gridLayer, String commandId) {
        
        if (commandId != null) {
            // if we are in "selectaddress" mode we have to register a single click mouse event
            nattable.getUiBindingRegistry().registerFirstSingleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), new IMouseAction() {
                public void run(NatTable natTable, MouseEvent event) {
                    int rowPos = natTable.getRowPositionByY(event.y);
                    int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPos, bodyLayerStack.treeLayer);
                    selectedObject = ((ListDataProvider<K>) bodyLayerStack.getBodyDataProvider()).getRowObject(bodyRowPos);
                    /// ???
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
//                int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPos, gridLayer.getBodyDataLayer());
//                selectedObject = gridLayer.getBodyDataProvider().getRowObject(bodyRowPos);
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
                    eventParams.put(SELECTED_ADDRESS_ID, Long.valueOf(selectedObject.getAddress().getId()));
                    eventParams.put(SELECTED_CONTACT_ID, Long.valueOf(selectedObject.getAddress().getContact().getId()));
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
//                    params.put(CallEditor.PARAM_OBJ_ID, Long.toString(selectedObject.getId()));
                    params.put(CallEditor.PARAM_EDITOR_TYPE, getEditorId());
                    parameterizedCommand = commandService.createCommand(CommandIds.CMD_CALL_EDITOR, params);
                    handlerService.executeHandler(parameterizedCommand);
                }
            }
        });
    }
    
    @Override
    public K getSelectedObject() {
        return selectedObject;
    }
    
//    @Override
    protected void hookDoubleClickCommand2(final NatTable nattable, final TreeLayer gridLayer) {
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
//        addCustomStyling(natTable);
        // nur für das Headermenü, falls das mal irgendwann gebraucht werden sollte
        //      natTable.addConfiguration(new HeaderMenuConfiguration(n6));

        E4SelectionListener<DebitorAddress> esl = new E4SelectionListener<DebitorAddress>(selectionService, bodyLayerStack.getSelectionLayer(), (IRowDataProvider<DebitorAddress>) bodyLayerStack.bodyDataProvider);
        bodyLayerStack.getSelectionLayer().addLayerListener(esl);

        // Change the default sort key bindings. Note that 'auto configure' was turned off
        // for the SortHeaderLayer (setup in the GlazedListsGridLayer)
        natTable.addConfiguration(new SingleClickSortConfiguration());
        natTable.configure();
    }

    protected IColumnPropertyAccessor<DebitorAddress> createColumnPropertyAccessor(String[] propertyNames) {
        final IColumnPropertyAccessor<DebitorAddress> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<DebitorAddress>(propertyNames);
        IColumnPropertyAccessor<DebitorAddress> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<DebitorAddress>() {

            public Object getDataValue(DebitorAddress rowObject, int columnIndex) {
                ContactListDescriptor descriptor = ContactListDescriptor.getDescriptorFromColumn(columnIndex);
                // For the address always the first entry is displayed (if any)
                switch (descriptor) {
                case NO:
                case FIRSTNAME:
                case LASTNAME:
                case ZIP:
                case CITY:
                    return columnPropertyAccessor.getDataValue(rowObject, columnIndex);
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

			public void setDataValue(DebitorAddress rowObject, int columnIndex, Object newValue) {
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
 
    	// fill the underlying data source (GlazedList)
        contactListData = getListData(contactType);
		
		// Properties of the DebitorAddress items inside the TreeItems
		String[] propertyNames = ContactListDescriptor.getContactPropertyNames();

        final IColumnPropertyAccessor<DebitorAddress> columnPropertyAccessor = createColumnPropertyAccessor(propertyNames);

        bodyLayerStack = new TreeBodyLayerStack<>(
				contactListData,
		        columnPropertyAccessor, new DebitorAddressTreeFormat());
       
      //2. build the column header layer
      IDataProvider columnHeaderDataProvider =
          new ListViewColumnHeaderDataProvider<DebitorAddress>(propertyNames, columnPropertyAccessor);
      DataLayer columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
      ILayer columnHeaderLayer =
              new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());

      // build the row header layer
      IDataProvider rowHeaderDataProvider =
              new DefaultRowHeaderDataProvider(bodyLayerStack.getBodyDataProvider());
      DataLayer rowHeaderDataLayer =
              new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
      ILayer rowHeaderLayer =
              new RowHeaderLayer(rowHeaderDataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());

      // build the corner layer
      IDataProvider cornerDataProvider =
              new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
      DataLayer cornerDataLayer =
              new DataLayer(cornerDataProvider);
      ILayer cornerLayer =
              new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);
        

      // build the grid layer
      GridLayer gridLayer =
              new GridLayer(bodyLayerStack, columnHeaderLayer, rowHeaderLayer, cornerLayer);

      // turn the auto configuration off as we want to add our header menu
      // configuration
      final NatTable natTable = new NatTable(searchAndTableComposite, gridLayer, false);
        

      // as the autoconfiguration of the NatTable is turned off, we have to
      // add the DefaultNatTableStyleConfiguration and the ConfigRegistry
      // manually
      natTable.setConfigRegistry(configRegistry);
      natTable.addConfiguration(new DefaultNatTableStyleConfiguration());

      // adds the key bindings that allows pressing space bar to
      // expand/collapse tree nodes
      natTable.addConfiguration(
              new TreeLayerExpandCollapseKeyBindings(
                      bodyLayerStack.getTreeLayer(),
                      bodyLayerStack.getSelectionLayer()));

      natTable.configure();

      GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);

        // matcher input Search text field 
        final MatcherEditor<DebitorAddress> textMatcherEditor = createTextWidgetMatcherEditor();
        
        // Filtered list for Search text field filter
        final FilterList<DebitorAddress> textFilteredIssues = new FilterList<DebitorAddress>(contactListData, textMatcherEditor);

        // build the list for the tree-filtered values (i.e., the value list which is affected by
        // tree selection)
        treeFilteredIssues = new FilterList<DebitorAddress>(textFilteredIssues);

        //use a RowSelectionModel that will perform row selections and is able to identify a row via unique ID
        RowSelectionModel<DebitorAddress> selectionModel = new RowSelectionModel<DebitorAddress>(bodyLayerStack.getSelectionLayer(), 
        		(IRowDataProvider<DebitorAddress>) bodyLayerStack.bodyDataProvider, new IRowIdAccessor<DebitorAddress>() {

			@Override
			public Serializable getRowId(DebitorAddress rowObject) {
				return rowObject.getAddress().getId();
			}
		}, false);
        bodyLayerStack.getSelectionLayer().setSelectionModel(selectionModel);
        // Select complete rows
        bodyLayerStack.getSelectionLayer().addConfiguration(new RowOnlySelectionConfiguration< DebitorAddress >());

//        //build the grid layer
//        setGridLayer(treeLayer);
////        DataLayer tableDataLayer = getGridLayer().getBodyDataLayer();
//        bodyDataLayer.setColumnPercentageSizing(true);
////        tableDataLayer.setColumnWidthPercentageByPosition(0, 5);
////        tableDataLayer.setColumnWidthPercentageByPosition(1, 15);
////        tableDataLayer.setColumnWidthPercentageByPosition(2, 75);
////        tableDataLayer.setColumnWidthPercentageByPosition(3, 5);
////
////        final NatTable natTable = new NatTable(searchAndTableComposite/*, 
////                SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | SWT.BORDER*/, getGridLayer().getGridLayer(), false);
//		final NatTable natTable = new NatTable(searchAndTableComposite, treeLayer, false);
//        natTable.setBackground(GUIHelper.COLOR_WHITE);
//        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
//        natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));
////        
        return natTable;
    }

    protected abstract MatcherEditor<DebitorAddress> createTextWidgetMatcherEditor();

    protected abstract EventList<DebitorAddress> getListData(ContactType contactType);

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
        topicTreeViewer = new TopicTreeViewer<ContactCategory>(top, msg, false, true);
//    	topicTreeViewer = (TopicTreeViewer<ContactCategory>)ContextInjectionFactory.make(TopicTreeViewer.class, context);
        categories = GlazedLists.eventList(contactCategoriesDAO.findAll());
        topicTreeViewer.setInput(categories);
        topicTreeViewer.setLabelProvider(new TreeCategoryLabelProvider());
        return topicTreeViewer;
    }
    
    public void handleRefreshEvent(String message) {
    	if(StringUtils.equals(message, Editor.UPDATE_EVENT) && !top.isDisposed()) {
	        sync.syncExec(() -> top.setRedraw(false));
	        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
	        GlazedLists.replaceAll(contactListData, getListData(contactType), false);
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
//		treeFilteredIssues.setMatcher(currentFilter);
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
    public TreeLayer getGridLayer() {
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
    protected void setGridLayer(TreeLayer gridLayer) {
        this.gridLayer = gridLayer;
    }

    protected ConfigRegistry getConfigRegistry() {
		return configRegistry;
	}

	protected void setConfigRegistry(ConfigRegistry configRegistry) {
		this.configRegistry = configRegistry;
	}
	
	
	
	
	

    /**
     * Always encapsulate the body layer stack in an AbstractLayerTransform to
     * ensure that the index transformations are performed in later commands.
     *
     * @param <T>
     */
    class TreeBodyLayerStack<T> extends AbstractLayerTransform {

        private final TreeList<T> treeList;

        private final IDataProvider bodyDataProvider;

        private final SelectionLayer selectionLayer;

        private final TreeLayer treeLayer;

        @SuppressWarnings("unchecked")
        public TreeBodyLayerStack(List<T> values,
                IColumnPropertyAccessor<T> columnPropertyAccessor,
                TreeList.Format<T> treeFormat) {
            // wrapping of the list to show into GlazedLists
            // see http://publicobject.com/glazedlists/ for further information
            EventList<T> eventList = GlazedLists.eventList(values);
            TransformedList<T, T> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);

            // use the SortedList constructor with 'null' for the Comparator
            // because the Comparator will be set by configuration
            SortedList<T> sortedList = new SortedList<>(rowObjectsGlazedList, null);
            // wrap the SortedList with the TreeList
            this.treeList = new TreeList<T>(sortedList, treeFormat, TreeList.NODES_START_EXPANDED);

            this.bodyDataProvider = new ListDataProvider<T>(this.treeList, columnPropertyAccessor);
            DataLayer bodyDataLayer = new DataLayer(this.bodyDataProvider);

            // simply apply labels for every column by index
            bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

            // layer for event handling of GlazedLists and PropertyChanges
            GlazedListsEventLayer<T> glazedListsEventLayer = new GlazedListsEventLayer<>(bodyDataLayer, this.treeList);

            GlazedListTreeData<T> treeData = new GlazedListTreeData<>(this.treeList);
            ITreeRowModel<T> treeRowModel = new GlazedListTreeRowModel<>(treeData);

            this.selectionLayer = new SelectionLayer(glazedListsEventLayer);

            this.treeLayer = new TreeLayer(this.selectionLayer, treeRowModel);
            ViewportLayer viewportLayer = new ViewportLayer(this.treeLayer);

            setUnderlyingLayer(viewportLayer);
        }

        public SelectionLayer getSelectionLayer() {
            return this.selectionLayer;
        }

        public TreeLayer getTreeLayer() {
            return this.treeLayer;
        }

        public TreeList<T> getTreeList() {
            return this.treeList;
        }

        public IDataProvider getBodyDataProvider() {
            return this.bodyDataProvider;
        }
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
