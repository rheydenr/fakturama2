/**
 * 
 */
package com.sebulli.fakturama.parts.widget.contacttree;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.e4.selection.E4SelectionListener;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionConfiguration;
import org.eclipse.nebula.widgets.nattable.sort.SortStatePersistor;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

import com.sebulli.fakturama.Activator;
import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.ContactCategoriesDAO;
import com.sebulli.fakturama.dao.DebitorAddress;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.parts.DebitorEditor;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.parts.Editor;
import com.sebulli.fakturama.parts.widget.search.TextSearchControl;
import com.sebulli.fakturama.views.datatable.contacts.ContactListDescriptor;
import com.sebulli.fakturama.views.datatable.tree.ui.TopicTreeViewer;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;

/**
 * View with the table of all contacts
 * @param <K>
 * @param <A>
 * 
 */
@SuppressWarnings("unchecked")
public abstract class ContactTreeListTable<K extends DebitorAddress> {
    @Inject
	private IPreferenceStore eclipsePrefs;

    @Inject
    protected UISynchronize sync;
    
    @Inject
    protected IEclipseContext context;
    
    @Inject
    protected ESelectionService selectionService;
    
    @Inject
    protected ILogger log;

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    protected EHandlerService handlerService;

    @Inject
    protected ECommandService commandService;
    
    /**
     * Event Broker for sending update events from the list table
     */
    @Inject
    protected IEventBroker evtBroker;

    @Inject
    protected EMenuService menuService;

	//The top composite
	protected Composite top;
	
	protected TableColumnLayout tableColumnLayout;

	/**
	 * a new sophisticated search control which displays a magnifying glass and an eraser icon.
	 * This is the default under Linux and Mac OS, but not under Windows. Here we have a nice
	 * widget for all platforms.
	 */
	protected TextSearchControl searchText;

    // ID of this view
    public static final String ID = "fakturama.views.contactTreeTable";

    private static final String POPUP_ID = "com.sebulli.fakturama.contactlist.popup";
    public static final String SELECTED_CONTACT_ID = "fakturama.treecontactlist.selectedcontactid";
	public static final String SELECTED_ADDRESS_ID = "fakturama.treecontactlist.selectedaddressid";
    
    protected EventList<ContactCategory> categories;
    
    @Inject
    protected ContactCategoriesDAO contactCategoriesDAO;
    
    protected MPart listTablePart;

    private K selectedObject;
    private ContactType contactType;

	// The topic tree viewer displays the categories of the UniDataSets
	protected TopicTreeViewer<ContactCategory> topicTreeViewer;

	// The standard UniDataSet
	protected String stdPropertyKey = null;
	
	protected NatTable natTable;

    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    protected FilterList<K> treeFilteredIssues;

	private TempBodyLayerStack<K> bodyLayerStack;

	@PostConstruct
	public Control createPartControl(Composite parent, MPart listTablePart) {
	    // Create the top composite
		top = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(2).applyTo(top);

		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, contextHelpId);
        
        Composite searchAndTableComposite = top;
        // Create the composite that contains the search field and the table
			searchAndTableComposite = createSearchAndTableComposite(top);
        natTable = createListTable(searchAndTableComposite);
        
        addCustomStyling(natTable);
        
        natTable.addDisposeListener(new DisposeListener() {
            
            @Override
            public void widgetDisposed(DisposeEvent e) {
            onStop(natTable);
            }
        });
        
        // call hook for post configure steps, if any
        postConfigureNatTable(natTable);

        onStart(natTable);  // as late as possible! Otherwise sorting doesn't work! (don't ask why!)
       
//        natTable.setTheme(new ModernNatTableThemeConfiguration());

		this.listTablePart = listTablePart;
		// if another click handler is set we use it
		// Listen to double clicks
		Object commandId = this.listTablePart.getTransientData().get(Constants.PROPERTY_CONTACTS_CLICKHANDLER);
		if (commandId != null) { // exactly would it be Constants.COMMAND_SELECTITEM
			hookDoubleClickCommand(natTable, /* gridLayer.getGridLayer(), */ (String) commandId);
		} else {
			hookDoubleClickCommand(natTable, /* gridLayer, */null);
		}

		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(top);
		return top;
	}

    /**
     * Component for the Search field and the item table
     * 
     * @param parent the parent {@link Composite} of this Component
     * @return {@link Composite}
     */
    private Composite createSearchAndTableComposite(Composite parent) {
        Composite searchAndTableComposite = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(1).applyTo(searchAndTableComposite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(searchAndTableComposite);

        // Create the composite that contains the search field and the toolbar
        Composite searchAndToolbarComposite = new Composite(searchAndTableComposite, SWT.NONE);
        GridLayoutFactory.fillDefaults().numColumns(3).applyTo(searchAndToolbarComposite);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(searchAndToolbarComposite);

        // The search composite
        Composite searchComposite = new Composite(searchAndToolbarComposite, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(searchComposite);
        GridDataFactory.fillDefaults().grab(true, true).align(SWT.END, SWT.CENTER).applyTo(searchComposite);

        // Search label an search field
        Label searchLabel = new Label(searchComposite, SWT.NONE);
        searchLabel.setText(msg.commonLabelSearchfield);
        GridDataFactory.swtDefaults().applyTo(searchLabel);
        
        searchText = new TextSearchControl(searchComposite, false, msg);

        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).hint(150, -1).applyTo(searchText);
        return searchAndTableComposite;
    }

    /**
     * We have to style the table a little bit...
     * 
     * @param natTable the {@link NatTable} to style
     */
    private void addCustomStyling(NatTable natTable) {
        DefaultSelectionStyleConfiguration selectionStyle = createDefaultSelectionStyle();

        // Add all style configurations to NatTable
        natTable.addConfiguration(selectionStyle);
    }

    /**
     * @return
     */
    protected DefaultSelectionStyleConfiguration createDefaultSelectionStyle() {
        // NOTE: Getting the colors and fonts from the GUIHelper ensures that
        // they are disposed properly (required by SWT)
        // Setup selection styling
        DefaultSelectionStyleConfiguration selectionStyle = new DefaultSelectionStyleConfiguration();
   //     selectionStyle.selectionFont = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL));
        selectionStyle.selectionBgColor = GUIHelper.getColor(217, 232, 251);
        selectionStyle.selectionFgColor = GUIHelper.COLOR_BLACK;
        selectionStyle.anchorBorderStyle = new BorderStyle(1, GUIHelper.COLOR_DARK_GRAY, LineStyleEnum.SOLID);
        selectionStyle.anchorBgColor = GUIHelper.getColor(217, 232, 251);
        selectionStyle.selectedHeaderBgColor = GUIHelper.getColor(169, 212, 235);
        return selectionStyle;
    }

	public TextSearchControl getSearchControl() {
		return searchText;
	}

	protected String createRootNodeDescriptor(String filter) {
		String rootNode = ""; 
		String[] splittedString = filter.split("/");
		if(splittedString.length > 1) {
			rootNode = splittedString[1];
		}
		return rootNode;
	}

	private void setColumWidthPercentage(DataLayer dataLayer) {
		dataLayer.setColumnPercentageSizing(true);
		dataLayer.setColumnWidthPercentageByPosition(0, 5);
		dataLayer.setColumnWidthPercentageByPosition(1, 15);
		dataLayer.setColumnWidthPercentageByPosition(2, 75);
		dataLayer.setColumnWidthPercentageByPosition(3, 5);
	}

	private void hookDoubleClickCommand(final NatTable nattable/* , final TreeLayer gridLayer */, String commandId) {
        
        if (commandId != null) {
            // if we are in "selectaddress" mode we have to register a single click mouse event
            nattable.getUiBindingRegistry().registerFirstSingleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), 
        		(NatTable natTable, MouseEvent event) -> {
                int rowPos = natTable.getRowPositionByY(event.y);
                int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPos, bodyLayerStack);
                selectedObject = ((ListDataProvider<K>) bodyLayerStack.getBodyDataProvider()).getRowObject(bodyRowPos);
            });
        }
        // Add a double click listener
        nattable.getUiBindingRegistry().registerDoubleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), new IMouseAction() {

            @Override
            public void run(NatTable natTable, MouseEvent event) {
                //get the row position for the click in the NatTable
                int rowPos = natTable.getRowPositionByY(event.y);
                //transform the NatTable row position to the row position of the body layer stack
                int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPos, bodyLayerStack.getSelectionLayer());
                selectedObject = ((ListDataProvider<K>) bodyLayerStack.getBodyDataProvider()).getRowObject(bodyRowPos);
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
                    eventParams.put(SELECTED_CONTACT_ID, Long.valueOf(selectedObject.getContactId()));
                    // alternatively use the Selection Service
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
    
    public K getSelectedObject() {
        return selectedObject;
    }
    
    protected void postConfigureNatTable(NatTable natTable) {
        //as the autoconfiguration of the NatTable is turned off, we have to add the 
        //DefaultNatTableStyleConfiguration and the ConfigRegistry manually 
        natTable.addConfiguration(new ContactTreeTableConfiguration());
        // nur für das Headermenü, falls das mal irgendwann gebraucht werden sollte
        //      natTable.addConfiguration(new HeaderMenuConfiguration(n6));

        E4SelectionListener<DebitorAddress> esl = new E4SelectionListener<DebitorAddress>(selectionService, bodyLayerStack.getSelectionLayer(), (IRowDataProvider<DebitorAddress>) bodyLayerStack.getBodyDataProvider());
        bodyLayerStack.getSelectionLayer().addLayerListener(esl);
        
        // TODO for later use (if we're using Tree Table)
//		ISelectionProvider selectionProvider = new RowSelectionProvider<>(bodyLayerStack.getSelectionLayer(),
//				bodyLayerStack.getBodyDataProvider(), false); // Provides rows where any cell in the row is selected
//
//		selectionProvider.addSelectionChangedListener(new ISelectionChangedListener() {
//
//			@Override
//			public void selectionChanged(SelectionChangedEvent event) {
//				System.out.println("Selection changed:");
//selectionService.getSelection()
//		IStructuredSelection structuredSelection = event.getStructuredSelection();
//		selectedObject = (K) structuredSelection.getFirstElement();
////				@SuppressWarnings("rawtypes")
////				Iterator it = selection.iterator();
////				while (it.hasNext()) {
////					System.out.println("  " + it.next());
////				}
//			}

//		});

        // Change the default sort key bindings. Note that 'auto configure' was turned off
        // for the SortHeaderLayer (setup in the GlazedListsGridLayer)
        natTable.addConfiguration(new SingleClickSortConfiguration());
        natTable.configure();
    }

    protected IColumnPropertyAccessor<K> createColumnPropertyAccessor(String[] propertyNames) {
        final IColumnPropertyAccessor<K> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<K>(propertyNames);
        IColumnPropertyAccessor<K> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<K>() {

            public Object getDataValue(K rowObject, int columnIndex) {
                ContactListDescriptor descriptor = ContactListDescriptor.getDescriptorFromColumn(columnIndex);
                // For the address always the first entry is displayed (if any)
                switch (descriptor) {
                case TYPE:
                	List<ContactType> dataList =(List) columnPropertyAccessor.getDataValue(rowObject, columnIndex);
                	return dataList.isEmpty() ? "" : dataList.stream().map(t -> t.getName()).collect(Collectors.joining(","));
                case NO:
                case FIRSTNAME:
                case LASTNAME:
                case ZIP:
                case CITY:
                case NAMEADDON: /* GS/ */
                case LOCALCONSULTANT: /* GS/ */
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

			public void setDataValue(K rowObject, int columnIndex, Object newValue) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#
	 * createListTable(org.eclipse.swt.widgets.Composite)
	 */
	protected NatTable createListTable(Composite searchAndTableComposite) {

		ContactType contactType;
		BillingType currentBillingType = (BillingType) context.get("ADDRESS_TYPE");
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
		EventList<K> contactListData = getListData(contactType);

		// Properties of the DebitorAddress items inside the TreeItems
		String[] propertyNames = ContactListDescriptor.getContactPropertyNames();

		final IColumnPropertyAccessor<K> columnPropertyAccessor = createColumnPropertyAccessor(propertyNames);

		// matcher input Search text field
		final MatcherEditor<K> textMatcherEditor = createTextWidgetMatcherEditor();

		// Filtered list for Search text field filter
		// build the list for the tree-filtered values (i.e., the value list which is
		// affected by tree selection)
		treeFilteredIssues = new FilterList<K>(contactListData, textMatcherEditor);
		
		
		DebitorAddressGridListLayer<K> tempDebitorAddressGridListLayer = new DebitorAddressGridListLayer<K>(
				treeFilteredIssues, propertyNames, columnPropertyAccessor, configRegistry, new DebitorAddressTreeFormat<K>());
		
		bodyLayerStack = tempDebitorAddressGridListLayer.getBodyLayerStack();

		// turn the auto configuration off as we want to add our header menu
		// configuration
		final NatTable natTable = new NatTable(searchAndTableComposite, tempDebitorAddressGridListLayer.getGridLayer(), false);

		// as the autoconfiguration of the NatTable is turned off, we have to
		// add the DefaultNatTableStyleConfiguration and the ConfigRegistry
		// manually
		natTable.setConfigRegistry(configRegistry);
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
		natTable.setBackground(GUIHelper.COLOR_WHITE);

//		// adds the key bindings that allows pressing space bar to
//		// expand/collapse tree nodes
//		natTable.addConfiguration(new TreeLayerExpandCollapseKeyBindings(bodyLayerStack.getTreeLayer(),
//				bodyLayerStack.getSelectionLayer()));

		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
		setColumWidthPercentage(bodyLayerStack.getBodyDataLayer());

		// use a RowSelectionModel that will perform row selections and is able to
		// identify a row via unique ID
		RowSelectionModel<K> selectionModel = new RowSelectionModel<K>(bodyLayerStack.getSelectionLayer(),
				(IRowDataProvider<K>) bodyLayerStack.getBodyDataProvider(), new IRowIdAccessor<K>() {

					@Override
					public Serializable getRowId(K rowObject) {
						return rowObject.getAddress().getId();
					}
				}, false);
		bodyLayerStack.getSelectionLayer().setSelectionModel(selectionModel);
		// Select complete rows
		bodyLayerStack.getSelectionLayer().addConfiguration(new RowOnlySelectionConfiguration<K>());
		return natTable;
	}

    protected abstract MatcherEditor<K> createTextWidgetMatcherEditor();
    protected abstract String getEditorTypeId();
    protected abstract EventList<K> getListData(ContactType contactType);
	protected abstract Class<K> getEntityClass();
	protected abstract AbstractDAO<? extends Contact> getEntityDAO();

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#getTableId()
     */
    public String getTableId() {
        return ID;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#getEditorId()
     */
    protected String getEditorId() {
        return DebitorEditor.ID;
    }
    
    public void handleRefreshEvent(String message) {
    	if(StringUtils.equals(message, Editor.UPDATE_EVENT) && !top.isDisposed()) {
	        sync.syncExec(() -> top.setRedraw(false));
	        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
	        GlazedLists.replaceAll(treeFilteredIssues, getListData(contactType), false);
	        GlazedLists.replaceAll(categories, GlazedLists.eventList(contactCategoriesDAO.findAll(true)), false);
	        sync.syncExec(() -> top.setRedraw(true));
    	}
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#isHeaderLabelEnabled()
     */
    protected boolean isHeaderLabelEnabled() {
        return false;
    }
    
	/**
	 * Loads the table settings (layout and such stuff) from a properties file.
	 * @param natTable
	 */
    public void onStart(NatTable natTable) {
        Properties properties = new Properties();
        String requestedWorkspace = getEclipsePrefs().getString(Constants.GENERAL_WORKSPACE);
        Path propertiesFile = Paths.get(requestedWorkspace, Constants.VIEWTABLE_PREFERENCES_FILE);

        try (InputStream propertiesInputStream = Files.newInputStream(propertiesFile);) {
            properties.load(propertiesInputStream);
            log.debug("Loading NatTable state from " + Constants.VIEWTABLE_PREFERENCES_FILE);
            properties.load(Files.newInputStream(propertiesFile, StandardOpenOption.READ));
            natTable.loadState(getTableId(), properties);
        } catch (IOException e) {
            // No file found, oh well, move along
            log.warn(Constants.VIEWTABLE_PREFERENCES_FILE + " not found, skipping load");
        }
    }
	
    /**
     * Before Nattable is disposed, all the settings for this table are stored in a properties file.
     * 
     * @param natTable
     */
    public void onStop(NatTable natTable) {
        Properties properties = new Properties();
        String requestedWorkspace = getEclipsePrefs().getString(Constants.GENERAL_WORKSPACE);
        Path propertiesFile = Paths.get(requestedWorkspace, Constants.VIEWTABLE_PREFERENCES_FILE);
        if(Files.notExists(propertiesFile)) {
            try {
                Files.createFile(propertiesFile);
            } catch (IOException ioex) {
                log.error(ioex, Constants.VIEWTABLE_PREFERENCES_FILE + " could not be created.");
            }
        }
        try (InputStream propertiesInputStream = Files.newInputStream(propertiesFile);) {
            properties.load(propertiesInputStream);
            natTable.saveState(getTableId(), properties);
            
            // removing superfluous entries (i.e., count of rows)
            final Iterator<Object> mapIter = properties.keySet().iterator();
            String[] prefixes = new String[]{
            		getTableId() + "." + GridRegion.BODY + RowReorderLayer.PERSISTENCE_KEY_ROW_INDEX_ORDER,
            		getTableId() + "." + GridRegion.COLUMN_HEADER + SortStatePersistor.PERSISTENCE_KEY_SORTING_STATE,
            	};
            String elem;
			while (mapIter.hasNext()) {
				elem = (String) mapIter.next();
				if (StringUtils.containsAny(elem, prefixes)) {
					mapIter.remove();
				}
			}
            
            log.info("Saving NatTable state to " + Constants.VIEWTABLE_PREFERENCES_FILE);
            properties.store(Files.newOutputStream(propertiesFile, StandardOpenOption.CREATE), "NatTable state");
        } catch (IOException ioex) {
            log.error(ioex, Constants.VIEWTABLE_PREFERENCES_FILE + " could not be written.");
        }
    }
	
    
    class ContactTreeTableConfiguration extends AbstractRegistryConfiguration {

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

    protected String getToolbarAddItemCommandId() {
        return CommandIds.LISTTOOLBAR_ADD_CONTACT;
    }

    protected MToolBar getMToolBar() {
        return listTablePart.getToolbar();
    }

    @Focus
    public void focus() {
        if(natTable != null) {
            natTable.setFocus();
        }
    }

    protected ConfigRegistry getConfigRegistry() {
		return configRegistry;
	}

	protected void setConfigRegistry(ConfigRegistry configRegistry) {
		this.configRegistry = configRegistry;
	}
	
	/**
	 * @return the eclipsePrefs
	 */
	protected IPreferenceStore getEclipsePrefs() {
		if(eclipsePrefs == null) {
			eclipsePrefs = EclipseContextFactory.getServiceContext(Activator.getContext()).get(IPreferenceStore.class);
		}
		return eclipsePrefs;
	}

}
