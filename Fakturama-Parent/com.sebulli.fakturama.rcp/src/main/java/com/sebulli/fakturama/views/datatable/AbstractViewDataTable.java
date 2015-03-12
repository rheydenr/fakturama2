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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.AbstractCategory;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.IEntity;
import com.sebulli.fakturama.views.datatable.tree.model.TreeObject;
import com.sebulli.fakturama.views.datatable.tree.ui.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

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
    
    @Inject
    private EMenuService menuService;

	//The top composite
	protected Composite top;
	
	protected TableColumnLayout tableColumnLayout;

	// Filter the table 
	protected Label filterLabel;
	protected Text searchText;

	// The topic tree viewer displays the categories of the UniDataSets
	protected TopicTreeViewer<C> topicTreeViewer;

	// Name of the editor to edit the UniDataSets
	protected String editor = "";

	// The standard UniDataSet
	protected String stdPropertyKey = null;

	// The selected tree object
	private TreeObject treeObject = null;
	
	protected NatTable natTable;

	/**
	 * Creates the SWT controls for this workbench part.
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
        addCustomStyling(natTable);
        
        // call hook for post configure steps, if any
        postConfigureNatTable(natTable);
        createDefaultContextMenu();
        
//        Menu contextMenu = createBodyMenu(natTable, getGridLayer()).build();  // works!
//        natTable.setMenu(contextMenu);
//        natTable.getUiBindingRegistry().registerMouseDownBinding(
//                new MouseEventMatcher(SWT.NONE, null, MouseEventMatcher.RIGHT_BUTTON),
//                new PopupMenuAction(contextMenu));
        
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
     * We have to style the table a little bit...
     * 
     * @param natTable the {@link NatTable} to style
     */
    private void addCustomStyling(NatTable natTable) {
        // NOTE: Getting the colors and fonts from the GUIHelper ensures that
        // they are disposed properly (required by SWT)
        // Setup selection styling
        DefaultSelectionStyleConfiguration selectionStyle = new DefaultSelectionStyleConfiguration();
        selectionStyle.selectionFont = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL));
        selectionStyle.selectionBgColor = GUIHelper.getColor(217, 232, 251);
        selectionStyle.selectionFgColor = GUIHelper.COLOR_BLACK;
        selectionStyle.anchorBorderStyle = new BorderStyle(1, GUIHelper.COLOR_DARK_GRAY, LineStyleEnum.SOLID);
        selectionStyle.anchorBgColor = GUIHelper.getColor(217, 232, 251);
        selectionStyle.selectedHeaderBgColor = GUIHelper.getColor(169, 212, 235);

        // Add all style configurations to NatTable
        natTable.addConfiguration(selectionStyle);
    }

	/**
	 * Loads the table settings (layout and such stuff) from a properties file.
	 * @param natTable
	 */
    public void onStart(NatTable natTable) {
        Properties properties = new Properties();
        String requestedWorkspace = eclipsePrefs.get(Constants.GENERAL_WORKSPACE, null);
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
        String requestedWorkspace = eclipsePrefs.get(Constants.GENERAL_WORKSPACE, null);
        Path propertiesFile = Paths.get(requestedWorkspace, Constants.VIEWTABLE_PREFERENCES_FILE);

        try (InputStream propertiesInputStream = Files.newInputStream(propertiesFile);) {
            properties.load(propertiesInputStream);
            natTable.saveState(getTableId(), properties);
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
	abstract public String getTableId();
	

    /**
     * Returns the editor id which corresponds to the current list table (relevant
     * for creating a suitable command)
     * 
     * @return
     */
    protected abstract String getEditorId();

	
	protected void postConfigureNatTable(NatTable natTable) {
	    // per default this method is empty
	}
	
	abstract protected TopicTreeViewer<C> createCategoryTreeViewer(Composite top);
	abstract protected String getPopupId();

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
//                log.debug("Selected Object: " + selectedObject.getName());
                // Call the corresponding editor. The editor is set
                // in the variable "editor", which is used as a parameter
                // when calling the editor command.
                // in E4 we create a new Part (or use an existing one with the same ID)
                // from PartDescriptor
                Map<String, Object> params = new HashMap<>();
                params.put(CallEditor.PARAM_OBJ_ID, Long.toString(selectedObject.getId()));
                params.put(CallEditor.PARAM_EDITOR_TYPE, getEditorId());
                if(selectedObject instanceof Document) {
                    params.put(CallEditor.PARAM_CATEGORY, ((Document)selectedObject).getBillingType().getName());
                }
                ParameterizedCommand parameterizedCommand = commandService.createCommand(CommandIds.CMD_CALL_EDITOR, params);
                handlerService.executeHandler(parameterizedCommand);
            }
        });
    }

    /**
     * Returns the actually marked rows in a table. Can be overwritten.
     * 
     * @return selected rows in a list table
     */
    public T[] getSelectedObjects() { return null; }
    
    /**
     * Returns the actually marked document in a table. Can be overwritten. May return <code>null</code>!
     * 
     * @return selected row in a list table
     */
    public T getSelectedObject() { return null;}
    
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

        // The toolbar is created via Application model (Application.e4xmi)
        
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

//	/**
//	 * Returns the topic tree viewer
//	 * 
//	 * @return The topic tree viewer
//	 */
//	public TopicTreeViewer getTopicTreeViewer() {
//		return topicTreeViewer;
//	}

	/**
	 * Create the menu manager for the context menu
	 */
	protected void createMenuManager() {
	    menuService.registerContextMenu(natTable, getPopupId());
	}

    
//    protected PopupMenuBuilder createBodyMenu(final NatTable natTable, final ListViewGridLayer<T> gridLayer) {
//        return new PopupMenuBuilder(natTable)
//                .withMenuItemProvider(new IMenuItemProvider() {
//                    @Override
//                    public void addMenuItem(final NatTable natTable, Menu popupMenu) {
//                        MenuItem menuItem = new MenuItem(popupMenu, SWT.PUSH);
//                        menuItem.setText("Toggle auto spanning");
//                        menuItem.setEnabled(true);
//
//                        menuItem.addSelectionListener(new SelectionAdapter() {
//                            @Override
//                            public void widgetSelected(SelectionEvent event) {
//                                NatEventData natEventData = MenuItemProviders.getNatEventData(event);
//                                //get the row position for the click in the NatTable
//                                int rowPosition = natEventData.getRowPosition();
//                               // natTable.getDataValueByPosition(columnPosition, rowPosition);
//
//                                //transform the NatTable row position to the row position of the body layer stack
//                                int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPosition, gridLayer.getBodyDataLayer());
//                                // extract the selected Object
//                                T selectedObject = gridLayer.getBodyDataProvider().getRowObject(bodyRowPos);
//                                log.debug("Selected Object: " + selectedObject.getName());
//
//                                natTable.doCommand(new VisualRefreshCommand());
//                            }
//                        });
//                    }
//                })
//                .withStateManagerMenuItemProvider();
//    }
    
	/**
	 * Create the default context menu with one addNew and one Delete action
	 */
	protected void createDefaultContextMenu() {
		createMenuManager();
//		if (addNewAction != null)
//			menuManager.add(addNewAction);
//		menuManager.add(new DeleteDataSetAction());
	}

	/**
	 * Refresh the table and the tree viewer
	 * @deprecated
	 */
	public void refresh() {

		// Refresh the standard entry
		refreshStdId();

//		// Refresh the table
//		if (tableViewer != null)
//			tableViewer.refresh();

		// Refresh the tree viewer
		if (topicTreeViewer != null) {
//			topicTreeViewer.refreshTree();
		}
	}

	/**
	 * Asks this part to take focus within the workbench.
	 */
	@Focus
	public void setFocus() {
	    natTable.setFocus();
	}

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
	public void setTransactionFilter(long filter, TreeObject treeObject) {
	    // per default this method does nothing
	}

	/**
	 * Set the contact filter
	 * 
	 * @param filter
	 *            The new filter string
	 */
	public void setContactFilter(long filter) {
	    // per default this method does nothing
	}

	/**
	 * Refresh the standard ID. Sets the new standard ID to the standard icon
	 * column of the table
	 * @deprecated
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
	
    /**
     * controls if the header label for the list view should be shown
     * 
     * @return the headerLabelEnabled
     */
	abstract protected boolean isHeaderLabelEnabled();
	abstract public void removeSelectedEntry();

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
}
