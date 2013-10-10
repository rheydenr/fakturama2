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

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;

import com.sebulli.fakturama.views.datatable.TopicTreeViewer.TreeObject;
import com.sebulli.fakturama.views.datatable.filter.TableFilter;

/**
 * This is the abstract parent class for all views that show a table with
 * UniDataSets and a tree viewer
 * 
 * @author Gerd Bartelt
 * 
 */
public abstract class AbstractViewDataTable<T> {

	//The top composite
	Composite top;
	
	// The table with the UniDataSets
	protected TableViewer tableViewer;
	protected TableColumnLayout tableColumnLayout;
	protected ViewDataTableContentProvider contentProvider;
	protected DataTableColumn stdIconColumn = null;

	// The columns that are used for the text search
	protected String searchColumns[];

	// Filter the table 
	protected TableFilter<T> tableFilter;
	protected Label filterLabel;

	// The topic tree viewer displays the categories of the UniDataSets
	protected TopicTreeViewer topicTreeViewer;

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
	
//	private ViewDataTable me;

	/**
	 * Creates the SWT controls for this workbench part.
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent, Class<?> elementClass, boolean useDocumentAndContactFilter, boolean useAll, String contextHelpId) {
//		me = this;

		// Create the top composite
		top = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(2).applyTo(top);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(top);

		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, contextHelpId);
		
		// Create the tree viewer
//		topicTreeViewer = new TopicTreeViewer(top, SWT.BORDER, elementClass, useDocumentAndContactFilter, useAll);
//		GridDataFactory.swtDefaults().hint(10, -1).applyTo(topicTreeViewer.getTree());

		// Create the composite that contains the search field and the table
		Composite searchAndTableComposite = new Composite(top, SWT.NONE);
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(1).applyTo(searchAndTableComposite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(searchAndTableComposite);

		// Create the composite that contains the search field and the toolbar
		Composite searchAndToolbarComposite = new Composite(searchAndTableComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(searchAndToolbarComposite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(searchAndToolbarComposite);

//		// The toolbar 
		ToolBar toolBar = new ToolBar(searchAndToolbarComposite, SWT.FLAT);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(toolBar);
		ToolBarManager tbm = new ToolBarManager(toolBar);

//		tbm.add(new DeleteDataSetAction());

//		if (addNewAction != null) {
//			addNewAction.setImageDescriptor(Activator.getImageDescriptor("/icons/16/plus_16.png"));
//			tbm.add(addNewAction);
//		}

		tbm.update(true);

		// The filter label
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
		searchLabel.setText("Search:");
		GridDataFactory.swtDefaults().applyTo(searchLabel);
		final Text searchText = new Text(searchComposite, SWT.BORDER | SWT.SEARCH | SWT.CANCEL | SWT.ICON_SEARCH);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).hint(150, -1).applyTo(searchText);
		searchText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				tableFilter.setSearchText(searchText.getText());
				tableViewer.refresh();
			}
		});

		// The table composite
		Composite tableComposite = new Composite(searchAndTableComposite, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableComposite);

		// Set the table layout
		tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);

		// Create a table viewer
		tableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.getTable().setLinesVisible(true);
		tableViewer.getTable().setHeaderVisible(true);

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

		// Listen to double clicks
		hookDoubleClickCommand();

		// Set the content provider
		contentProvider = new ViewDataTableContentProvider(tableViewer);
		tableViewer.setContentProvider(contentProvider);

		// Set the table
//		topicTreeViewer.setTable(this);
//
//		// Set sorter and filter
		tableViewer.setComparator(new ViewerComparator() {
			
			@Override
			public int compare(Viewer viewer, Object e1, Object e2) {
				// TODO Auto-generated method stub
				return super.compare(viewer, e1, e2);
			}
		});
		tableFilter = new TableFilter<T>(searchColumns);
		tableViewer.addFilter(tableFilter);
		
		// Activate the tooltip support for the viewer
		ColumnViewerToolTipSupport.enableFor(tableViewer, ToolTip.NO_RECREATE); 
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
	 * On double click: open the corresponding editor
	 */
	private void hookDoubleClickCommand() {

		// Add a double click listener
		tableViewer.addDoubleClickListener(new IDoubleClickListener() {

			public void doubleClick(DoubleClickEvent event) {
//				IHandlerService handlerService = (IHandlerService) getSite().getService(IHandlerService.class);
//				ICommandService commandService = (ICommandService) getSite().getService(ICommandService.class);
//
//				try {
//
//					// Call the corresponding editor. The editor is set
//					// in the variable "editor", which is used as a parameter
//					// when calling the editor command.
//					Command callEditor = commandService.getCommand("com.sebulli.fakturama.editors.callEditor");
//					Map<String, String> params = new HashMap<>();
//					params.put("com.sebulli.fakturama.editors.callEditorParameter", editor);
//					ParameterizedCommand parameterizedCommand = ParameterizedCommand.generateCommand(callEditor, params);
//					handlerService.executeCommand(parameterizedCommand, null);
//
//				}
//				catch (Exception e) {
////					Logger.logError(e, "Editor not found: " + editor);
//				}
			}
		});
	}

	/**
	 * Refresh the table and the tree viewer
	 */
	public void refresh() {

		// Refresh the standard entry
		refreshStdId();

		// Refresh the table
		if (tableViewer != null)
			tableViewer.refresh();

//		// Refresh the tree viewer
//		if (topicTreeViewer != null) {
//			topicTreeViewer.refresh();
//		}
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
	public void setCategoryFilter(String filter) {

		// Set the label with the filter string
//		if (filter.equals("$shownothing"))
//			filterLabel.setText("");
//		else
//		// Display the localizes list names.
//		// or the document type
//		if ( (this instanceof ViewDocumentTable) || (this instanceof ViewListTable) )
//			filterLabel.setText(DataSetListNames.NAMES.getLocalizedName(filter));
//
//		filterLabel.pack(true);
//
//		// Reset transaction and contact filter, set category filter
//		contentProvider.setTransactionFilter(-1);
//		contentProvider.setContactFilter(-1);
//		contentProvider.setCategoryFilter(filter);
//		contentProvider.setTreeObject(treeObject);
//
//		// Set category to the addNew action. So a new data set is created
//		// with the selected category
//		if (addNewAction != null) {
//			addNewAction.setCategory(filter);
//		}

		//Refresh
		this.refresh();
	}

	/**
	 * Set the transaction filter
	 * 
	 * @param filter
	 *            The new filter string
	 */
	public void setTransactionFilter(int filter) {

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
}
