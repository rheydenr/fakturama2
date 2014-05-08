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

package com.sebulli.fakturama.views.datatable.impl;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.BodyMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;

import com.sebulli.fakturama.dao.ContactDAO;
import com.sebulli.fakturama.model.Contact;

/**
 * View with the table of all contacts
 * 
 * @author Gerd Bartelt
 * 
 */
public class ViewContactTable { // extends AbstractViewDataTable<Contact> {

	// ID of this view
	public static final String ID = "com.sebulli.fakturama.views.datasettable.viewContactTable";
	
	@Inject
	private Composite parent;
	
	@Inject
	private MPart part;
	
	@Inject
	private EHandlerService handlerService;
	
	@Inject
	private ECommandService commandService;
	
	@Inject
	private ContactDAO contactDAO;
	
	//The top composite
	private Composite top;
	
	// Filter the table 
	protected Label filterLabel;

	private String editor;  	  
	/**
	 * Creates the SWT controls for this workbench part.
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@PostConstruct
	public void createPartControl() {
		// Layout widgets

		// Create the top composite
		top = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(2).applyTo(top);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(top);

		// Add the action to create a new entry
//		addNewAction = new NewContactAction(null);

//		super.createPartControl(parent, Contact.class, false, true, "ContextHelpConstants.CONTACT_TABLE_VIEW");
		
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

		// The toolbar 
		ToolBar toolBar = new ToolBar(searchAndToolbarComposite, SWT.FLAT);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(toolBar);
		ToolBarManager tbm = new ToolBarManager(toolBar);
//
//		tbm.add(new DeleteDataSetAction());
//
//		if (addNewAction != null) {
//			addNewAction.setImageDescriptor(Activator.getImageDescriptor("/icons/16/plus_16.png"));
//			tbm.add(addNewAction);
//		}
//
//		tbm.update(true);

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
		searchLabel.setText("Search:");// TODO i18n
		GridDataFactory.swtDefaults().applyTo(searchLabel);
		final Text searchText = new Text(searchComposite, SWT.BORDER | SWT.SEARCH | SWT.CANCEL | SWT.ICON_SEARCH);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).hint(150, -1).applyTo(searchText);

		part.setLabel("Contact");

		// The table composite
		// Set selection provider
		NatTable natTable = buildTable(searchText);
		// Set the table layout
		natTable.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		// Name of the editor
		editor = "Contact";
//
//		// Get the column width from the preferences
//		int cw_no = 10; // Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_CONTACTS_NO");
//		int cw_firstname = 10; // Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_CONTACTS_FIRSTNAME");
//		int cw_lastname = 10; // Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_CONTACTS_LASTNAME");
//		int cw_company = 10; // Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_CONTACTS_COMPANY");
//		int cw_zip = 10; // Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_CONTACTS_ZIP");
//		int cw_city = 10; // Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_CONTACTS_CITY");
//
//		
//		// Create the table columns
//		// new TableColumn(tableColumnLayout, tableViewer, SWT.RIGHT, "ID", 30, 0, true, "id");
//		
//		//T: Used as heading of a table. Keep the word short.
//		new DataTableColumn<Contact>(tableColumnLayout, tableViewer, SWT.RIGHT, msg("No."), cw_no, true, "nr");
//		//T: Used as heading of a table. Keep the word short.
//		new DataTableColumn<Contact>(tableColumnLayout, tableViewer, SWT.LEFT, msg("First Name"), cw_firstname,  false, "firstname");
//		//T: Used as heading of a table. Keep the word short.
//		new DataTableColumn<Contact>(tableColumnLayout, tableViewer, SWT.LEFT, msg("Last Name"), cw_lastname, false, "name");
//		//T: Used as heading of a table. Keep the word short.
//		new DataTableColumn<Contact>(tableColumnLayout, tableViewer, SWT.LEFT, msg("Company"), cw_company, false, "company");
//		//T: Used as heading of a table. Keep the word short.
//		new DataTableColumn<Contact>(tableColumnLayout, tableViewer, SWT.RIGHT, msg("ZIP"), cw_zip, true, "zip");
//		//T: Used as heading of a table. Keep the word short.
//		new DataTableColumn<Contact>(tableColumnLayout, tableViewer, SWT.LEFT, msg("City"), cw_city, false, "city");
//
//		// Set the input of the table viewer and the tree viewer
// 		tableViewer.setInput(Data.INSTANCE.getContacts());
//		topicTreeViewer.setInput(Data.INSTANCE.getContacts());
	}
	
	protected NatTable buildTable(Text searchTextField) {
		ListTableGridLayer<Contact> gridLayer = new ListTableGridLayer<Contact>(ContactsListColumnAccessor.CONTACT_PROPERTIES, 
				ContactsListColumnAccessor.PROPERTY_TO_LABEL_MAP, contactDAO.findAll(),
				new ContactsListColumnAccessor(), searchTextField, new ContactsTextFilterator());

		NatTable nattable = new NatTable(parent, gridLayer, false);

		nattable.addConfiguration(new DefaultNatTableStyleConfiguration());
		nattable.addConfiguration(new HeaderMenuConfiguration(nattable));
		nattable.addConfiguration(new ListSelectionStyleConfiguration());

		// Custom selection configuration
		SelectionLayer selectionLayer = gridLayer.getSelectionLayer();
		selectionLayer.setSelectionModel(new RowSelectionModel<Contact>(selectionLayer, gridLayer.getBodyDataProvider(), new IRowIdAccessor<Contact>() {

			public Serializable getRowId(Contact rowObject) {
				return rowObject.getId();
			}
			
		}));
		
		selectionLayer.addConfiguration(new RowOnlySelectionConfiguration<Contact>());
		nattable.addConfiguration(new NoHeaderRowOnlySelectionBindings());

		// Listen to double clicks
		hookDoubleClickCommand(nattable, gridLayer);

		// Set the table
//		topicTreeViewer.setTable(this);

		// Set sorter and filter
		// ... done with NatTable

		// Create the context menu
		createDefaultContextMenu(nattable, gridLayer.getBodyLayer());

		nattable.configure();
		return nattable;
	}

	/**
	 * On double click: open the corresponding editor
	 * @param nattable 
	 * @param gridLayer 
	 */
	private void hookDoubleClickCommand(final NatTable nattable, final ListTableGridLayer<Contact> gridLayer) {
		// Add a double click listener
		nattable.getUiBindingRegistry().registerDoubleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE),
		new IMouseAction() {

			@Override
			public void run(NatTable natTable, MouseEvent event) {
				//get the row position for the click in the NatTable
				int rowPos = natTable.getRowPositionByY(event.y);
				//transform the NatTable row position to the row position of the body layer stack
				int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPos, gridLayer.getBodyDataLayer());
				// extract the selected Object
				Contact contact  = gridLayer.getBodyDataProvider().getRowObject(bodyRowPos);
				System.out.println("Selected contact: " + contact.getCustomerNumber());
//				try {
					// Call the corresponding editor. The editor is set
					// in the variable "editor", which is used as a parameter
					// when calling the editor command.
					// in E4 we create a new Part (or use an existing one with the same ID)
					// from PartDescriptor
					Command callEditor = commandService.getCommand("com.sebulli.fakturama.editors.callEditor");
					Map<String, String> params = new HashMap<>();
					params.put("com.sebulli.fakturama.editors.editortype", editor);
					params.put("com.sebulli.fakturama.rcp.cmdparam.objId", Long.toString(contact.getId()));
					ParameterizedCommand parameterizedCommand = ParameterizedCommand.generateCommand(callEditor, params);
					handlerService.executeHandler(parameterizedCommand);
//
//				}
//				catch (Exception e) {
//					Logger.logError(e, "Editor not found: " + editor);
//				}
			}
		});
	}  
	
	/**
	 * Create the default context menu with one addNew and one Delete action
	 * @param natTable 
	 */
	protected void createDefaultContextMenu(NatTable natTable, final ILayer iLayer) {
		createMenuManager(natTable, iLayer);
//		if (addNewAction != null)
//			menuManager.add(addNewAction);
//		menuManager.add(new DeleteDataSetAction());
	}
  
	/**
	 * Create the menu manager for the context menu
	 * @param natTable 
	 */
	protected void createMenuManager(final NatTable natTable, final ILayer iLayer) {

//		getSite().registerContextMenu("com.sebulli.fakturama.views.datasettable.popup", menuManager, tableViewer);
//		getSite().setSelectionProvider(tableViewer);

		
		natTable.addConfiguration(new BodyMenuConfiguration(natTable, iLayer));
		
		
		
		
	}  
	
	/**
	 * Set the focus to the top composite.
	 * 
	 * @see com.sebulli.fakturama.editors.Editor#setFocus()
	 */
	@Focus
	public void setFocus() {
		if(parent != null) 
			parent.setFocus();
	}


}
