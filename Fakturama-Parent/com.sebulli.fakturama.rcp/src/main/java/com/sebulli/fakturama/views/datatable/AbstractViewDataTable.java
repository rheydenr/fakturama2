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

import javax.inject.Inject;

import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.AbstractCategory;
import com.sebulli.fakturama.views.datatable.tree.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.TreeObject;
import com.sebulli.fakturama.views.datatable.tree.TreeObjectType;

/**
 * This is the abstract parent class for all views that show a table with
 * UniDataSets and a tree viewer
 * 
 * @author Gerd Bartelt
 * 
 */
public abstract class AbstractViewDataTable<T, C extends AbstractCategory> {
    
    @Inject
    @Translation
    protected Messages msg;

	//The top composite
	protected Composite top;
	
	protected TableColumnLayout tableColumnLayout;
	protected ViewDataTableContentProvider contentProvider;
	protected DataTableColumn stdIconColumn = null;

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
	
	abstract protected NatTable createListTable(Composite searchAndTableComposite);
	
	protected void postConfigureNatTable(NatTable natTable) {
	    // per default this method is empty
	}
	
	abstract protected TopicTreeViewer<C> createCategoryTreeViewer(Composite top);

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
        ToolBar toolBar = new ToolBar(searchAndToolbarComposite, SWT.FLAT);
        GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(toolBar);
//      ToolBarManager tbm = new ToolBarManager(toolBar);

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
		contentProvider.setTransactionFilter(filter);
		contentProvider.setContactFilter(-1);
		contentProvider.setCategoryFilter("");

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
		contentProvider.setContactFilter(filter);
		contentProvider.setTransactionFilter(-1);
		contentProvider.setCategoryFilter("");

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
		if (stdIconColumn == null)
			return;

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
