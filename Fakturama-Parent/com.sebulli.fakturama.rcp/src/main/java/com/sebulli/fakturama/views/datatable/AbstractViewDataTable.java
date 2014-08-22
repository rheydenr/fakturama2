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

package com.sebulli.fakturama.views.datatable;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.VisualRefreshCommand;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.AbstractCategory;
import com.sebulli.fakturama.model.IEntity;
import com.sebulli.fakturama.views.datatable.tree.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.TreeObject;
import com.sebulli.fakturama.views.datatable.tree.TreeObjectType;

/**
 * This is the abstract parent class for all views that show a table with
 * UniDataSets and a tree viewer.
 * 
 * @author Gerd Bartelt
 * 
 */
public abstract class AbstractViewDataTable<T extends IEntity, C extends AbstractCategory> {
    public static final String ROOT_NODE_NAME = "all";

    /**
     * show now category label as header for the list view
     */
    protected static final String NO_CATEGORY_LABEL = "$shownothing";

    protected static final String NO_SORT_LABEL = "noSortLabel";

    protected static final String CUSTOM_CELL_LABEL = "Cell_LABEL";

    @Inject
    @Preference
    private IEclipsePreferences eclipsePrefs;

    @Inject
    private Logger log;

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private EHandlerService handlerService;

    @Inject
    private ECommandService commandService;

	//The top composite
	protected Composite top;
	
	protected TableColumnLayout tableColumnLayout;
//	protected ViewDataTableContentProvider contentProvider;
//	protected DataTableColumn stdIconColumn = null;

	// Filter the table 
	protected Label filterLabel;
	protected Text searchText;

	// The topic tree viewer displays the categories of the UniDataSets
	protected TopicTreeViewer<C> topicTreeViewer;

	// Name of the editor to edit the UniDataSets
	protected String editor = "";

//	// Action to create new dataset in the editor
//	protected NewEditorAction addNewAction = null;
//
//	// Menu manager of the context menu
//	protected MenuManager menuManager;

	// The standard UniDataSet
	protected String stdPropertyKey = null;

	// The selected tree object
	private TreeObject treeObject = null;
	
	protected NatTable natTable;

	/**
	 * Creates the SWT controls for this workbench part.
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createPartControl(Composite parent, Class<?> elementClass, boolean useDocumentAndContactFilter, boolean useAll, String contextHelpId) {
		// Create the top composite
		top = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(2).applyTo(top);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(top);

		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, contextHelpId);
        
        // Create the tree viewer
        topicTreeViewer = createCategoryTreeViewer(top); 
//		GridDataFactory.swtDefaults().hint(10, -1).applyTo(topicTreeViewer.getTree());

        // Create the composite that contains the search field and the table
        Composite searchAndTableComposite = createSearchAndTableComposite(top);
        natTable = createListTable(searchAndTableComposite);
        
        // call hook for post configure steps, if any
        postConfigureNatTable(natTable);
        
        onStart(natTable);
        natTable.addDisposeListener(new DisposeListener() {
            
            @Override
            public void widgetDisposed(DisposeEvent e) {
                onStop(natTable);
            }
        });

		// Workaround
		// At startup the browser editor is the active part of the workbench.
		// If now an element of this view is selected, the view does not get active.
		// So we check, if we are active, and if not: we activate this view.
//		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
//
//			@Override
//			public void selectionChanged(SelectionChangedEvent event) {
//
//				IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//				IWorkbenchPage page = workbenchWindow.getActivePage();
//
//				if (page != null) {
//					// Activate the part of the workbench page.
//					if (!page.getActivePart().equals(me))
//						page.activate(me);
//				}
//			}
//		});

//		// Set selection provider
//		getSite().setSelectionProvider(tableViewer);

		return top;
	}
	
	/**
	 * Loads the table settings (layout and such stuff) from a properties file.
	 * @param natTable
	 */
    public void onStart(NatTable natTable) {
        Properties properties = new Properties();
        String requestedWorkspace = eclipsePrefs.get(Constants.GENERAL_WORKSPACE, null);
        Path propertiesFile = Paths.get(requestedWorkspace + FileSystems.getDefault().getSeparator()+Constants.VIEWTABLE_PREFERENCES_FILE);

        try {
            log.debug("Loading NatTable state from " + Constants.VIEWTABLE_PREFERENCES_FILE);
            properties.load(Files.newInputStream(propertiesFile, StandardOpenOption.READ));
            natTable.loadState(getTableId(), properties);
        } catch (IOException e) {
            // No file found, oh well, move along
            log.warn(Constants.VIEWTABLE_PREFERENCES_FILE + " not found, skipping load");
        }

//        example.onStart();
    }
	
    /**
     * Before Nattable is disposed, all the settings for this table are stored in a properties file.
     * 
     * @param natTable
     */
    public void onStop(NatTable natTable) {
//        example.onStop();

        Properties properties = new Properties();
        String requestedWorkspace = eclipsePrefs.get(Constants.GENERAL_WORKSPACE, null);
        Path propertiesFile = Paths.get(requestedWorkspace + FileSystems.getDefault().getSeparator()+Constants.VIEWTABLE_PREFERENCES_FILE);
        natTable.saveState(getTableId(), properties);

        try {
            log.info("Saving NatTable state to " + Constants.VIEWTABLE_PREFERENCES_FILE);
            properties.store(Files.newOutputStream(propertiesFile, StandardOpenOption.CREATE), "NatTable state");
        } catch (IOException ioex) {
            log.error(ioex, Constants.VIEWTABLE_PREFERENCES_FILE + " could not be created. ");
        }
    }
	
	abstract protected NatTable createListTable(Composite searchAndTableComposite);
	
	/**
	 * Gets a unique identifier for the implementing table (for using in conjunction with storing preferences)
	 *  
	 * @return unique identifier
	 */
	abstract protected String getTableId();
	
	protected void postConfigureNatTable(NatTable natTable) {
	    // per default this method is empty
	}
	
	abstract protected TopicTreeViewer<C> createCategoryTreeViewer(Composite top);
//	abstract protected void createListViewToolbar(Composite parent);
	

    /**
     * On double click: open the corresponding editor
     * 
     * @param nattable
     * @param gridLayer
     */
    protected void hookDoubleClickCommand(final NatTable nattable, final ListViewGridLayer<T> gridLayer) {
        // Add a double click listener
        nattable.getUiBindingRegistry().registerDoubleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), new IMouseAction() {

            @Override
            public void run(NatTable natTable, MouseEvent event) {
                //get the row position for the click in the NatTable
                int rowPos = natTable.getRowPositionByY(event.y);
                //transform the NatTable row position to the row position of the body layer stack
                int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPos, gridLayer.getBodyDataLayer());
                // extract the selected Object
                T selectedObject = gridLayer.getBodyDataProvider().getRowObject(bodyRowPos);
                log.debug("Selected Object: " + selectedObject.getName());
                // Call the corresponding editor. The editor is set
                // in the variable "editor", which is used as a parameter
                // when calling the editor command.
                // in E4 we create a new Part (or use an existing one with the same ID)
                // from PartDescriptor
                Command callEditor = commandService.getCommand("com.sebulli.fakturama.command.callEditor");
                Map<String, String> params = new HashMap<>();
                params.put("com.sebulli.fakturama.rcp.cmdparam.objId", Long.toString(selectedObject.getId()));
                params.put("com.sebulli.fakturama.editors.editortype", getTableId());
                ParameterizedCommand parameterizedCommand = ParameterizedCommand.generateCommand(callEditor, params);
                handlerService.executeHandler(parameterizedCommand);
            }
        });
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

        // The toolbar
//        createListViewToolbar(searchAndToolbarComposite);
        
        filterLabel = new Label(searchAndToolbarComposite, SWT.NONE);
        FontData[] fD = filterLabel.getFont().getFontData();
        fD[0].setHeight(20);
        Font font = new Font(null, fD[0]);
        filterLabel.setFont(font);
        font.dispose();
        GridDataFactory.fillDefaults().grab(true, false).align(SWT.CENTER, SWT.CENTER).applyTo(filterLabel);

        // The search composite
        Composite searchComposite = new Composite(searchAndToolbarComposite, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(searchComposite);
        GridDataFactory.fillDefaults().grab(true, true).align(SWT.END, SWT.CENTER).applyTo(searchComposite);

        // Search label an search field
        Label searchLabel = new Label(searchComposite, SWT.NONE);
        searchLabel.setText(msg.commonLabelSearchfield);
        GridDataFactory.swtDefaults().applyTo(searchLabel);
        searchText = new Text(searchComposite, SWT.BORDER | SWT.SEARCH | SWT.CANCEL | SWT.ICON_SEARCH);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).hint(150, -1).applyTo(searchText);
        return searchAndTableComposite;
    }

	/**
	 * Returns the topic tree viewer
	 * 
	 * @return The topic tree viewer
	 */
//	public TopicTreeViewer getTopicTreeViewer() {
//		return topicTreeViewer;
//	}
//
//	/**
//	 * Create the menu manager for the context menu
//	 */
//	protected void createMenuManager() {
//		menuManager = new MenuManager();
//		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
//		tableViewer.getTable().setMenu(menuManager.createContextMenu(tableViewer.getTable()));
//
//		getSite().registerContextMenu("com.sebulli.fakturama.views.datasettable.popup", menuManager, tableViewer);
//		getSite().setSelectionProvider(tableViewer);
//
//	}

    
    protected PopupMenuBuilder createBodyMenu(final NatTable natTable) {
        return new PopupMenuBuilder(natTable)
                .withMenuItemProvider(new IMenuItemProvider() {
                    @Override
                    public void addMenuItem(final NatTable natTable, Menu popupMenu) {
                        MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
                        menuItem.setText("Toggle auto spanning");
                        menuItem.setEnabled(true);

                        menuItem.addSelectionListener(new SelectionAdapter() {
                            @Override
                            public void widgetSelected(SelectionEvent event) {
//                                if (dataProvider.isAutoColumnSpan()) {
//                                    dataProvider.setAutoColumnSpan(false);
//                                    dataProvider.setAutoRowSpan(true);
//                                }
//                                else if (dataProvider.isAutoRowSpan()) {
//                                    dataProvider.setAutoRowSpan(false);
//                                }
//                                else {
//                                    dataProvider.setAutoColumnSpan(true);
//                                }
                                natTable.doCommand(new VisualRefreshCommand());
                            }
                        });
                    }
                })
                .withStateManagerMenuItemProvider();
    }
    
    //
	/**
	 * Create the default context menu with one addNew and one Delete action
	 */
	protected void createDefaultContextMenu() {
//		createMenuManager();
//		if (addNewAction != null)
//			menuManager.add(addNewAction);
//		menuManager.add(new DeleteDataSetAction());
	}

	/**
	 * Refresh the table and the tree viewer
	 */
	public void refresh() {

		// Refresh the standard entry
		refreshStdId();

//		// Refresh the table
//		if (tableViewer != null)
//			tableViewer.refresh();

		// Refresh the tree viewer
		if (topicTreeViewer != null) {
			topicTreeViewer.refreshTree();
		}
	}
//
//	/**
//	 * Asks this part to take focus within the workbench.
//	 * 
//	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
//	 */
//	@Override
//	public void setFocus() {
//		tableViewer.getControl().setFocus();
//	}

	/**
	 * Set a reference to the tree object
	 * 
	 * @param treeObject
	 * 		The tree object
	 */
	public void setTreeObject(TreeObject treeObject){
		this.treeObject = treeObject;
	}

	/**
	 * Set the category filter
	 * 
	 * @param filter
	 *            The new filter string
	 */
	abstract public void setCategoryFilter(String filter, TreeObjectType treeObjectType);
	
	/**
	 * Set the transaction filter
	 * 
	 * @param filter
	 *            The new filter string
	 */
	public void setTransactionFilter(int filter, TreeObjectType treeObjectType) {

		// Set the label with the filter string
		filterLabel.setText("Dieser Vorgang");
		filterLabel.pack(true);

		// Reset category and contact filter, set transaction filter
//		contentProvider.setTransactionFilter(filter);
//		contentProvider.setContactFilter(-1);
//		contentProvider.setCategoryFilter("");

//		// Reset the addNew action. 
//		if (addNewAction != null) {
//			addNewAction.setCategory("");
//		}
		this.refresh();
	}

	/**
	 * Set the contact filter
	 * 
	 * @param filter
	 *            The new filter string
	 */
	public void setContactFilter(int filter) {

		// Set the label with the filter string
//		filterLabel.setText(Data.INSTANCE.getContacts().getDatasetById(filter).getName(false));
		filterLabel.pack(true);

		// Reset transaction and category filter, set contact filter
//		contentProvider.setContactFilter(filter);
//		contentProvider.setTransactionFilter(-1);
//		contentProvider.setCategoryFilter("");

//		// Reset the addNew action. 
//		if (addNewAction != null) {
//			addNewAction.setCategory("");
//		}

		this.refresh();
	}

	/**
	 * Refresh the standard ID. Sets the new standard ID to the standard icon
	 * column of the table
	 */
	public void refreshStdId() {

		if (stdPropertyKey == null)
			return;
//		if (stdIconColumn == null)
//			return;

		try {
			// Set the the new standard ID to the standard icon column
			// TODO
			//stdIconColumn.setStdEntry(Integer.parseInt(Data.INSTANCE.getProperty(stdPropertyKey)));
		}
		catch (NumberFormatException e) {
		}

	}
	
	abstract protected boolean isHeaderLabelEnabled();
}
