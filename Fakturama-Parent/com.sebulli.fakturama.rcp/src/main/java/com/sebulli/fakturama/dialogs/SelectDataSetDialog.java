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

package com.sebulli.fakturama.dialogs;


import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Abstract class for all dialogs to select an UniDataSet entry from a table
 * 
 * @author Gerd Bartelt
 */
public abstract class SelectDataSetDialog {
	protected TableViewer tableViewer;
	protected TableColumnLayout tableColumnLayout;
	protected String editor = "";
	
//	// The first selected item
//	protected UniDataSet selectedDataSet = null;
//	
//	// All selected items
//	protected List<UniDataSet> selectedDataSets = new ArrayList<UniDataSet>()  ;
//
//	// Is it possible to select multiple items 
//	protected boolean multiple;
//	protected String title = "";
//
//	// Filter the table 
//	protected TableFilter tableFilter;
//	// The columns that are used for the text search
//	protected String searchColumns[];
//
//	/**
//	 * Constructor
//	 * 
//	 * @param parentShell
//	 *            The parent shell
//	 */
//	protected SelectDataSetDialog(Shell parentShell) {
//		super(parentShell);
//	}
//
//	/**
//	 * Constructor Creates a new dialog and uses the shell of the active
//	 * workbench window
//	 * 
//	 * @param title
//	 *            Title of the new dialog
//	 */
//	public SelectDataSetDialog(String title, boolean multiple) {
//		this(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
//		this.title = title;
//		this.multiple = multiple;
//	}
//
//	/**
//	 * Create this part of the dialog are that is common in all the different
//	 * types of SelectDataSetDialogs
//	 * 
//	 * @param parent
//	 *            The parent composite
//	 */
//	@Override
//	protected Control createDialogArea(Composite parent) {
//
//		// Create the top composite dialog area
//		Composite top = (Composite) super.createDialogArea(parent);
//		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(top);
//
//		// Set the title
//		this.getShell().setText(title);
//
//		// The search composite
//		Composite searchComposite = new Composite(top, SWT.NONE);
//		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(searchComposite);
//		GridDataFactory.fillDefaults().grab(true, false).align(SWT.END, SWT.CENTER).applyTo(searchComposite);
//
//		// Search label an search field
//		Label searchLabel = new Label(searchComposite, SWT.NONE);
//		
//		//T: Label of the search field
//		searchLabel.setText(_("Search:"));
//		GridDataFactory.swtDefaults().applyTo(searchLabel);
//		final Text searchText = new Text(searchComposite, SWT.BORDER | SWT.SEARCH | SWT.CANCEL | SWT.ICON_SEARCH);
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).hint(150, -1).applyTo(searchText);
//		searchText.addModifyListener(new ModifyListener() {
//
//			@Override
//			public void modifyText(ModifyEvent e) {
//				tableFilter.setSearchText(searchText.getText());
//				tableViewer.refresh();
//			}
//		});
//
//		// Define the SWT layout
//		Composite tableComposite = new Composite(top, SWT.NONE);
//		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableComposite);
//
//		// Define the layout of the table
//		tableColumnLayout = new TableColumnLayout();
//		tableComposite.setLayout(tableColumnLayout);
//
//		// Create the jface table viewer
//		int multipleStyle = 0;
//		if (multiple)
//			multipleStyle = SWT.MULTI;
//		
//		tableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION | multipleStyle);
//		tableViewer.getTable().setLinesVisible(true);
//		tableViewer.getTable().setHeaderVisible(true);
//
//		// Add a selection change listener
//		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
//
//			/**
//			 * Selection Changed method
//			 * 
//			 * @param event
//			 */
//			@SuppressWarnings("unchecked")
//			@Override
//			public void selectionChanged(SelectionChangedEvent event) {
//				ISelection selection = tableViewer.getSelection();
//				if (selection != null && selection instanceof IStructuredSelection) {
//					Object obj = ((IStructuredSelection) selection).getFirstElement();
//					selectedDataSet = (UniDataSet) obj;
//					selectedDataSets = ((IStructuredSelection) selection).toList();
//				}
//			}
//
//		});
//
//		// Add a double click listener.
//		// Close the dialog
//
//		tableViewer.addDoubleClickListener(new IDoubleClickListener() {
//
//			/**
//			 * Double click method
//			 * 
//			 * @param event
//			 */
//			public void doubleClick(DoubleClickEvent event) {
//				close();
//			}
//		});
//
//		tableFilter = new TableFilter(searchColumns);
//		tableViewer.addFilter(tableFilter);
//
//		return top;
//	}
//
//	/**
//	 * Set the initial size of the dialogs in pixel
//	 * 
//	 * @return Size as Point object
//	 */
//	@Override
//	protected Point getInitialSize() {
//		return new Point(800, 550);
//	}
//
//	/**
//	 * Configures the shell
//	 * 
//	 * @param newShell
//	 *            The new shell
//	 */
//	@Override
//	protected void configureShell(Shell newShell) {
//		super.configureShell(newShell);
//	}
//
//	/**
//	 * Get the selected element
//	 * 
//	 * @return The selected element or null, if none is selected
//	 */
//	public UniDataSet getSelectedDataSet() {
//		return selectedDataSet;
//	}
//
//	/**
//	 * Get the selected elements
//	 * 
//	 * @return The selected element or null, if none is selected
//	 */
//	public List<UniDataSet> getSelectedDataSets() {
//		return selectedDataSets;
//	}
	
	
}
