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

package com.sebulli.fakturama.parts;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The Voucher editor (parent class)
 * 
 * @author Gerd Bartelt
 */
public abstract class VoucherEditor extends Editor {

	protected String titleText = "Voucher";
	protected String customerSupplier = "-";
	
	// This UniDataSet represents the editor's input
//	protected Voucher voucher;
    private MPart part;

	// SWT widgets of the editor
	private Composite top;
	private Combo comboCategory;
	private DateTime dtDate;
	private Text textName;
	private Text textNr;
	private Text textDocumentNr;
	protected TableViewer tableViewerItems;
	private FormattedText textPaidValue;
	private FormattedText textTotalValue;
//	private UniData paidValue; 
//	private UniData totalValue = new UniData(UniDataType.DOUBLE, 0.0);
//	private Button bPaidWithDiscount;
//	private Button bBook;
//	
//	private List<UniDataSetTableColumn> itemTableColumns = new ArrayList<UniDataSetTableColumn>();
//	private CellNavigation cellNavigation;
//
//	// The items of this document
//	protected DataSetArray<DataSetVoucherItem> voucherItems;
//
//	// Flag, if item editing is active
//	VoucherItemEditingSupport itemEditingSupport = null;

	// These flags are set by the preference settings.
	// They define, if elements of the editor are displayed, or not.
	private boolean useGross;

	// defines, if the payment is new created
	private boolean newVoucher;

//	/**
//	 * Constructor
//	 * 
//	 * Associate the table view with the editor
//	 */
//	public VoucherEditor() {
//		cellNavigation = new CellNavigation(itemTableColumns);
//	}

	/**
	 * Saves the contents of this part
	 * 
	 * @param monitor
	 *            Progress monitor
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
    @Persist
	public void doSave(IProgressMonitor monitor) {
		/*
		 * the following parameters are not saved: - id (constant)
		 */
//
//		// Always set the editor's data set to "undeleted"
//		voucher.setBooleanValueByKey("deleted", false);

//		// Set all the items
//		ArrayList<DataSetVoucherItem> itemDatasets = voucherItems.getActiveDatasets();
//		String itemsString = "";
//
//		for (DataSetVoucherItem itemDataset : itemDatasets) {
//
//			// Get the ID of this voucher item and
//			int id = itemDataset.getIntValueByKey("id");
//
//			DataSetVoucherItem item = null;
//
//			// Get an existing item, or use the temporary item
//			if (id >= 0) {
//				item = (DataSetVoucherItem) getVoucherItems().getDatasetById(id);
//
//				// Copy the values to the existing voucher item.
//				item.setStringValueByKey("name", itemDataset.getStringValueByKey("name"));
//				item.setStringValueByKey("category", itemDataset.getStringValueByKey("category"));
//				item.setDoubleValueByKey("price", itemDataset.getDoubleValueByKey("price"));
//				item.setIntValueByKey("vatid", itemDataset.getIntValueByKey("vatid"));
//			}
//			else
//				item = itemDataset;
//
//			// Updates the list of billing account
//			updateBillingAccount(item);
//			
//			
//			// If the ID of this item is -1, this was a new item.
//			// In this case, update the existing one
//			if (id >= 0) {
//				updateVoucherItem(item);
//			}
//			else {
//				// Create a new voucher item
//				DataSetVoucherItem itemDatasetTemp = addVoucherItem(itemDataset);
//				id = itemDatasetTemp.getIntValueByKey("id");
//				itemDataset.setIntValueByKey("id", id);
//			}
//
//			// Collect all item IDs in a sting and separate them by a comma
//			if (itemsString.length() > 0)
//				itemsString += ",";
//			itemsString += Integer.toString(id);
//		}
//		// Set the string value
//		voucher.setStringValueByKey("items", itemsString);
//		
//		// Set total and paid value
//		voucher.setDoubleValueByKey("total",totalValue.getValueAsDouble() );
//
//		// The the voucher was paid with a discount, use the paid value
//		if (bPaidWithDiscount.getSelection()) {
//			voucher.setBooleanValueByKey("discounted", true);
//			voucher.setDoubleValueByKey("paid",paidValue.getValueAsDouble() );
//		}
//		// else use the total value
//		else {
//			voucher.setBooleanValueByKey("discounted", false);
//			voucher.setDoubleValueByKey("paid",totalValue.getValueAsDouble() );
//		}
//
//		// The selection "book" is inverted
//		voucher.setBooleanValueByKey("donotbook", !bBook.getSelection());
//
//		// If it is a new voucher, add it to the voucher list and
//		// to the data base
//		if (newVoucher) {
//			voucher = addVoucher(voucher);
//			newVoucher = false;
//		}
//		// If it's not new, update at least the data base
//		else {
//			updateVoucher(voucher);
//		}
//
//		// Set the Editor's name to the voucher name.
//		setPartName(voucher.getStringValueByKey("name"));
//
//		// Refresh the table view of all vouchers
//		ApplicationWorkbenchAdvisor.refreshView(ViewListTable.ID);
//		refreshView();
//		checkDirty();
	}
//
//	/**
//	 * Updates the list of billing account. Check, whether there is already
//	 * an entry with the same name 
//	 * 
//	 * @param vatID
//	 * 			VAT id of the voucher item
//	 * @param category
//	 * 			Category of the voucher item
//	 */
//	public static void updateBillingAccount(DataSetVoucherItem item) {
//		
//		// Get list of all billing accounts
//		ArrayList<DataSetList> billing_accounts = Data.INSTANCE.getListEntries().getActiveDatasetsByCategory("billing_accounts");
//
//		boolean found = false;
//
//		// Get the VAT of this item
//		int vatID  = item.getIntValueByKey("vatid");
//		DataSetVAT vat = null;
//		String vatName = "";
//		if (vatID >= 0) {
//			vat = Data.INSTANCE.getVATs().getDatasetById(vatID);
//			vatName = vat.getStringValueByKey("name");
//		}
//
//		// Search for the billing account with the same name as the item
//		for (DataSetList billing_account : billing_accounts) {
//			if (billing_account.getStringValueByKey("name").equals(item.getStringValueByKey("category"))) {
//
//				found = true;
//
//				// Get the VAT value from the billing account list
//				String billingVatName = billing_account.getStringValueByKey("value");
//
//				// If the vat is set to another value, refresh it.
//				if (!vatName.equalsIgnoreCase(billingVatName)) {
//					billing_account.setStringValueByKey("value", vatName);
//					Data.INSTANCE.getListEntries().updateDataSet(billing_account);
//				}
//				break;
//			}
//		}
//
//		// Entry not found in the billing accounts list, so create a new
//		// one.
//		if (!found) {
//			DataSetList billing_account;
//			billing_account = new DataSetList("billing_accounts", item.getStringValueByKey("category"), vatName);
//			Data.INSTANCE.getListEntries().addNewDataSet(billing_account);
//		}
//
//	}
	
	/**
	 * Initializes the editor. If an existing data set is opened, the local
	 * variable "payment" is set to This data set. If the editor is opened to
	 * create a new one, a new data set is created and the local variable
	 * "payment" is set to this one.
     * 
     * @param parent
     *            The editor's parent Composite
     */
    @PostConstruct
    public void init(Composite parent) {
//		// Set the editor's data set to the editor's input
//		voucher = (DataSetVoucher) ((UniDataSetEditorInput) input).getUniDataSet();
//
//		// test, if the editor is opened to create a new data set. This is,
//		// if there is no input set.
//		newVoucher = (voucher == null);
//
//		// If new ..
//		if (newVoucher) {
//
//            // Create a new data set
//			voucher = createNewVoucher(((UniDataSetEditorInput) input));
//
//			
//			//T: Voucher Editor: Name of a new Voucher
//			setPartName(_("New Voucher"));
//
//			// Us the last category
//			int lastVoucherSize = getVouchers().getActiveDatasets().size();
//			if (lastVoucherSize > 0) {
//				DataSetVoucher lastVoucher = (( DataSetArray<DataSetVoucher>)getVouchers()).getActiveDatasets().get(lastVoucherSize - 1);
//				voucher.setStringValueByKey("category", lastVoucher.getStringValueByKey("category"));
//
//			}
//
//		}
//		else {
//
//			// Set the Editor's name to the voucher name.
//			setPartName(voucher.getStringValueByKey("name"));
//		}
//
//		// Create a set of new temporary items.
//		// These items exist only in the memory.
//		// If the editor is opened, the items from the document are
//		// copied to this item set. If the editor is closed or saved,
//		// these items are copied back to the document and to the data base.
//		voucherItems = (DataSetArray<DataSetVoucherItem>) createNewVoucherItems();
//
//		// Get all items by ID from the item string
//		String itemsString = voucher.getStringValueByKey("items");
//		String[] itemsStringParts = itemsString.split(",");
//
//		// Parse the item string ..
//		for (String itemsStringPart : itemsStringParts) {
//			int id;
//			if (itemsStringPart.length() > 0) {
//				try {
//					id = Integer.parseInt(itemsStringPart);
//				}
//				catch (NumberFormatException e) {
//					Logger.logError(e, "Error parsing item string");
//					id = 0;
//				}
//
//				// And copy the item to a new one
//				DataSetVoucherItem item = createNewVoucherItem((DataSetVoucherItem)(getVoucherItems().getDatasetById(id))); 
//				voucherItems.getDatasets().add(item);
//			}
//		}
//
//		// If the voucher list is empty, add one entry
//		if (voucherItems.getDatasets().isEmpty())
//			addNewItem();
//
//		paidValue = new UniData(UniDataType.DOUBLE, 0.0);
//		
	}
        
        @Override
        protected MDirtyable getMDirtyablePart() {
            return part;
        }
//
//	/**
//	 * Returns whether the contents of this part have changed since the last
//	 * save operation
//	 * 
//	 * @see org.eclipse.ui.part.EditorPart#isDirty()
//	 */
//	@Override
//	public boolean isDirty() {
//		/*
//		 * the following parameters are not checked: 
//		 * - id (constant)
//		 * - total (this value is calculated during the idDirty() method
//		 */
//
//		// Calculate the total sum of all items
//		calculateTotal();
//		
//		// Check, if a cell is being modified at this moment
//		if (tableViewerItems != null)
//			if (tableViewerItems.isCellEditorActive() && (itemEditingSupport != null))
//				return true;
//
//		if (voucher.getBooleanValueByKey("deleted")) { return true; }
//		if (newVoucher) { return true; }
//
//		if (!voucher.getStringValueByKey("name").equals(textName.getText())) { return true; }
//		if (!voucher.getStringValueByKey("category").equals(comboCategory.getText())) { return true; }
//		if (!voucher.getStringValueByKey("nr").equals(textNr.getText())) { return true; }
//		if (!voucher.getStringValueByKey("documentnr").equals(textDocumentNr.getText())) { return true; }
//		if (!voucher.getStringValueByKey("date").equals(DataUtils.getDateTimeAsString(dtDate))) { return true; }
//
//		// The selection "book" is inverted
//		if (voucher.getBooleanValueByKey("donotbook") != (!bBook.getSelection())) { return true; }
//
//		
//		// Test all the voucher items
//		String itemsString = "";
//		ArrayList<DataSetVoucherItem> itemDatasets = voucherItems.getActiveDatasets();
//		for (DataSetVoucherItem itemDataset : itemDatasets) {
//			int id = itemDataset.getIntValueByKey("id");
//
//			// There is no existing item
//			if (id < 0)
//				return true;
//
//			@SuppressWarnings("unchecked")
//			DataSetVoucherItem item = ((DataSetArray <DataSetVoucherItem>)getVoucherItems()).getDatasetById(id);
//
//			if (!item.getStringValueByKey("name").equals(itemDataset.getStringValueByKey("name"))) { return true; }
//			if (!item.getStringValueByKey("category").equals(itemDataset.getStringValueByKey("category"))) { return true; }
//			if (!DataUtils.DoublesAreEqual(item.getDoubleValueByKey("price"), itemDataset.getDoubleValueByKey("price"))) { return true; }
//			if (item.getIntValueByKey("vatid") != itemDataset.getIntValueByKey("vatid")) { return true; }
//
//			if (itemsString.length() > 0)
//				itemsString += ",";
//			itemsString += Integer.toString(id);
//		}
//
//		// Compare also the items string.
//		// So the voucher is dirty, if new items are added or items have
//		// been deleted.
//		if (!voucher.getStringValueByKey("items").equals(itemsString)) { return true; }
//
//		// Compare paid value
//		// The the voucher was paid with a discount, use the paid value
//		if (bPaidWithDiscount.getSelection()) {
//			if (!DataUtils.DoublesAreEqual(voucher.getDoubleValueByKey("paid"), paidValue.getValueAsDouble() )) { return true; }
//		}
//		// else use the total value
//		else {
//			if (!DataUtils.DoublesAreEqual(voucher.getDoubleValueByKey("paid"), totalValue.getValueAsDouble() )) { return true; }
//		}
//		
//		if (voucher.getBooleanValueByKey("discounted") != bPaidWithDiscount.getSelection()) { return true; }
//		
//		return false;
//	}
//
//	/**
//	 * Calculate the total sum of all voucher items
//	 */
//	private void calculateTotal() {
//		
//		// Do the calculation
//		voucher.calculate(voucherItems, false, 0.0, totalValue.getValueAsDouble(),false );
//
//		// Get the total result
//		Double total = voucher.getSummary().getTotalGross().asDouble();
//
//		// Update the text widget
//		totalValue.setValue(total);
//		textTotalValue.update();
//	}

	/**
	 * Returns, if this editor used net or gross values.
	 * 
	 * @return True, if the document uses gross values.
	 */
	public boolean getUseGross() {
		return useGross;
	}
	
//	/**
//	 * Adds an empty voucher item
//	 */
//	private DataSetVoucherItem addNewItem() {
//
//		DataSetVoucherItem newItem = createNewVoucherItem("Name", "", 0.0, 0);
//
//		// Use the standard VAT value
//		newItem.setIntValueByKey("id", -(voucherItems.getDatasets().size() + 1));
//		newItem.setIntValueByKey("vatid", Integer.parseInt(Data.INSTANCE.getProperty("standardvat")));
//		voucherItems.getDatasets().add(newItem);
//		return newItem;
//	}
//
//	/**
//	 * Creates the SWT controls for this workbench part
//	 * 
//	 * @param the
//	 *            parent control
//	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
//	 */
//	protected void createPartControl(Composite parent, String helpID) {
//
//		// Get the some settings from the preference store
//		useGross = (Activator.getDefault().getPreferenceStore().getInt("DOCUMENT_USE_NET_GROSS") == 1);
//
//		// Create the top Composite
//		top = new Composite(parent, SWT.NONE);
//		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(top);
//
//		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, helpID);
//
//		
//		// There is no invisible component, so no container has to be created
//		// Composite invisible = new Composite(top, SWT.NONE);
//		// invisible.setVisible(false);
//		// GridDataFactory.fillDefaults().hint(0, 0).span(2, 1).applyTo(invisible);
//
//		// Create the top Composite
//		Composite titlebar = new Composite(top, SWT.NONE);
//		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(titlebar);
//		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).span(2, 1).applyTo(titlebar);
//
//		// Large title
//		Label labelTitle = new Label(titlebar, SWT.NONE);
//		
//		//T: VoucherEditor - Title
//		labelTitle.setText(titleText);
//		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(labelTitle);
//		makeLargeLabel(labelTitle);
//
//		
//		// The "book" label
//		bBook = new Button(titlebar, SWT.CHECK | SWT.RIGHT);
//		bBook.setSelection(voucher.getBooleanValueByKey("donotbook"));
//		//T: Label voucher edito
//		bBook.setText(_("book"));
//		//T: Tool Tip Text
//		bBook.setToolTipText(_("Uncheck this, if the voucher should not be booked."));
//		
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.BOTTOM).applyTo(bBook);
//		bBook.setSelection(!voucher.getBooleanValueByKey("donotbook"));
//		
//		
//		// If the book check box is selected ...
//		bBook.addSelectionListener(new SelectionAdapter() {
//
//			// check dirty
//			public void widgetSelected(SelectionEvent e) {
//				checkDirty();
//				if (!bBook.getSelection()) {
//					//T: Dialog in the voucher editor to uncheck the book field 
//					if (Workspace.showMessageBox(SWT.OK | SWT.CANCEL, _("Please confirm"),
//					//T: Dialog in the voucher editor to uncheck the book field 
//							_("If you uncheck this, the voucher won't be booked !")) != SWT.OK) {
//						bBook.setSelection(true);
//					}
//				}
//				checkDirty();
//				
//			}
//		});
//
//
//
//		
//		// Voucher category
//		Label labelCategory = new Label(top, SWT.NONE);
//
//		//T: Label in the voucher editor
//		labelCategory.setText(_("Account"));
//		//T: Tool Tip Text
//		labelCategory.setToolTipText(_("Account of this voucher. E.g. 'Bank', 'Cash', 'Credit Card'"));
//		
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCategory);
//		comboCategory = new Combo(top, SWT.BORDER);
//		comboCategory.setToolTipText(labelCategory.getToolTipText());
//		comboCategory.setText(voucher.getStringValueByKey("category"));
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboCategory);
//		
//		// Collect all category strings
//		TreeSet<String> categories = new TreeSet<String>();
//		categories.addAll(Data.INSTANCE.getPayments().getCategoryStrings());
//		categories.addAll(Data.INSTANCE.getReceiptVouchers().getCategoryStrings());
//		categories.addAll(Data.INSTANCE.getExpenditureVouchers().getCategoryStrings());
//
//		// Add all category strings to the combo
//		for (Object category : categories) {
//			comboCategory.add(category.toString());
//		}
//
//		superviceControl(comboCategory);
//
//		// Document date
//		Label labelDate = new Label(top, SWT.NONE);
//		//T: Label in the voucher editor
//		labelDate.setText(_("Date"));
//		//T: Tool Tip Text
//		labelDate.setToolTipText(_("Date of the voucher"));
//
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDate);
//
//		// Document date
//		dtDate = new DateTime(top, SWT.DROP_DOWN);
//		dtDate.setToolTipText(labelDate.getToolTipText());
//		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(dtDate);
//		superviceControl(dtDate);
//
//		
//
//		// Set the dtDate widget to the vouchers date
//		GregorianCalendar calendar = new GregorianCalendar();
//		calendar = DataUtils.getCalendarFromDateString(voucher.getStringValueByKey("date"));
//		dtDate.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//
//		// Number
//		Label labelNr = new Label(top, SWT.NONE);
//		//T: Label in the voucher editor
//		labelNr.setText(_("Voucher No."));
//		//T: Tool Tip Text
//		labelNr.setToolTipText(_("Consecutive number of all vouchers"));
//
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelNr);
//		textNr = new Text(top, SWT.BORDER);
//		textNr.setText(voucher.getStringValueByKey("nr"));
//		textNr.setToolTipText(labelNr.getToolTipText());
//		superviceControl(textNr, 32);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(textNr);
//
//		// Document number
//		Label labelDocumentNr = new Label(top, SWT.NONE);
//		//T: Label in the voucher editor
//		labelDocumentNr.setText(_("Document No."));
//		//T: Tool Tip Text
//		labelDocumentNr.setToolTipText(_("Number found on the voucher. (Document No of the supplier)"));
//
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDocumentNr);
//		textDocumentNr = new Text(top, SWT.BORDER);
//		textDocumentNr.setText(voucher.getStringValueByKey("documentnr"));
//		textDocumentNr.setToolTipText(labelDocumentNr.getToolTipText());
//		superviceControl(textDocumentNr, 32);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(textDocumentNr);
//
//		// Supplier name
//		Label labelName = new Label(top, SWT.NONE);
//
//		labelName.setText(customerSupplier);
//		//T: Tool Tip Text
//		labelName.setToolTipText(_("Name of the") + " " + customerSupplier.toLowerCase());
//		
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);
//		textName = new Text(top, SWT.BORDER);
//		textName.setText(voucher.getStringValueByKey("name"));
//		textName.setToolTipText(labelName.getToolTipText());
//		superviceControl(textName, 100);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(textName);
//
//		// Add the suggestion listener
//		textName.addVerifyListener(new Suggestion(textName, getVouchers().getStrings("name")));
//
//
//		// Container for the label and the add and delete button.
//		Composite addButtonComposite = new Composite(top, SWT.NONE | SWT.RIGHT);
//		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(addButtonComposite);
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addButtonComposite);
//
//		// Items label
//		Label labelItems = new Label(addButtonComposite, SWT.NONE | SWT.RIGHT);
//		//T: VoucherEditor - Label Items
//		labelItems.setText(_("Items", "VOUCHER"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(labelItems);
//
//		// Item add button
//		Label addButton = new Label(addButtonComposite, SWT.NONE);
//		try {
//			addButton.setImage((Activator.getImageDescriptor("/icons/16/plus_16.png").createImage()));
//			//T: Tool Tip Text
//			addButton.setToolTipText(_("Add a new item"));
//
//		}
//		catch (Exception e) {
//			Logger.logError(e, "Icon not found");
//		}
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addButton);
//		addButton.addMouseListener(new MouseAdapter() {
//
//			// Add a new item with default properties
//			public void mouseDown(MouseEvent e) {
//				tableViewerItems.cancelEditing();
//
//				DataSetVoucherItem newItem = addNewItem();
//
//				tableViewerItems.refresh();
//				tableViewerItems.reveal(newItem);
//				checkDirty();
//			}
//		});
//
//		// Item delete button
//		Label deleteButton = new Label(addButtonComposite, SWT.NONE);
//		try {
//			deleteButton.setImage((Activator.getImageDescriptor("/icons/16/delete_16.png").createImage()));
//			//T: Tool Tip Text
//			deleteButton.setToolTipText(_("Delete the selected item"));
//		}
//		catch (Exception e) {
//			Logger.logError(e, "Icon not found");
//		}
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(deleteButton);
//		deleteButton.addMouseListener(new MouseAdapter() {
//
//			// Delete the selected item
//			public void mouseDown(MouseEvent e) {
//				ISelection selection = tableViewerItems.getSelection();
//				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
//				if (!structuredSelection.isEmpty()) {
//					// get first element ...
//					Object firstElement = structuredSelection.getFirstElement();
//					UniDataSet uds = (UniDataSet) firstElement;
//					// Delete it (mark it as deleted)
//					uds.setBooleanValueByKey("deleted", true);
//					tableViewerItems.refresh();
//					checkDirty();
//				}
//			}
//		});
//
//		// Composite that contains the table
//		Composite tableComposite = new Composite(top, SWT.NONE);
//		GridDataFactory.fillDefaults().grab(true, true).applyTo(tableComposite);
//		TableColumnLayout tableColumnLayout = new TableColumnLayout();
//		tableComposite.setLayout(tableColumnLayout);
//
//		// The table viewer
//		tableViewerItems = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION);
//		tableViewerItems.getTable().setLinesVisible(true);
//		tableViewerItems.getTable().setHeaderVisible(true);
//		tableViewerItems.setContentProvider(new ViewDataSetTableContentProvider(tableViewerItems));
//
//		// Get the column width from the preferences
//		int cw_text = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_VOUCHERITEMS_TEXT");
//		int cw_accounttype = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_VOUCHERITEMS_ACCOUNTTYPE");
//		int cw_vat = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_VOUCHERITEMS_VAT");
//		int cw_net = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_VOUCHERITEMS_NET");
//		int cw_gross = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_VOUCHERITEMS_GROSS");
//
//		// Create the table columns
//		//T: Used as heading of a table. Keep the word short.
//		itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.LEFT, _("Text"), cw_text, false, "name", new VoucherItemEditingSupport(this,
//				tableViewerItems, 1)));
//		//T: Used as heading of a table. Keep the word short.
//		itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.LEFT, _("Account Type"), cw_accounttype, true, "category", new VoucherItemEditingSupport(this,
//				tableViewerItems, 2)));
//		//T: Used as heading of a table. Keep the word short.
//		itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.RIGHT, _("VAT"), cw_vat, true, "$vatnamebyid",
//						new VoucherItemEditingSupport(this, tableViewerItems, 3)));
////		new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.RIGHT, _("VAT"), 50, 0, true, "$VoucherItemVatPercent",
////				vatnamebyid				new VoucherItemEditingSupport(this, tableViewerItems, 3));
//		//T: Used as heading of a table. Keep the word short.
//		itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.RIGHT, _("Net"), cw_net, true, "price", new VoucherItemEditingSupport(this,
//				tableViewerItems, 4)));
//		//T: Used as heading of a table. Keep the word short.
//		itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.RIGHT, _("Gross"), cw_gross, true, "$VoucherItemGrossPrice",
//				new VoucherItemEditingSupport(this, tableViewerItems, 5)));
//
//		// Create the top Composite
//		Composite bottom = new Composite(top, SWT.NONE);
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).span(2,1).applyTo(bottom);
//		
//		GridLayoutFactory.swtDefaults().numColumns(5).applyTo(bottom);
//
//		// The paid label
//		bPaidWithDiscount = new Button(bottom, SWT.CHECK | SWT.RIGHT);
//		bPaidWithDiscount.setSelection(voucher.getBooleanValueByKey("discounted"));
//		//T: Mark an voucher, if the paid value is not equal to the total value.
//		bPaidWithDiscount.setText(_("Paid with discount"));
//		//T: Tool Tip Text
//		bPaidWithDiscount.setToolTipText(_("Check this, if not the total value was paid. Then enter the paid value."));
//		
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(bPaidWithDiscount);
//
//		// If the bPaidWithDiscount check box is selected ...
//		bPaidWithDiscount.addSelectionListener(new SelectionAdapter() {
//
//			// check dirty
//			public void widgetSelected(SelectionEvent e) {
//				checkDirty();
//				if (textPaidValue != null) {
//					boolean selection = bPaidWithDiscount.getSelection();
//					
//					// If selected and the paid value was not already set,
//					// use the total value
//					if (selection && DataUtils.DoublesAreEqual(paidValue.getValueAsDouble(), 0.0)) {
//						paidValue.setValue(totalValue.getValueAsDouble());
//						textPaidValue.update();
//					}
//
//					textPaidValue.getText().setVisible(selection);
//					
//				}
//			}
//		});
//
//		
//		// Paid value
//		Label labelPaidValue = new Label(bottom, SWT.NONE);
//		//T: Label in the voucher editor
//		labelPaidValue.setText(_("Paid Value") + ":");
//		//T: Tool Tip Text,  xgettext:no-c-format
//		labelPaidValue.setToolTipText(_("The paid value (e.g. 97$, if the total value was 100$ with 3% discount)."));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelPaidValue);
//
//		paidValue.setValue(voucher.getStringValueByKey("paid"));
//
//		textPaidValue = new CurrencyText(this, bottom, SWT.BORDER | SWT.RIGHT, paidValue);
//		textPaidValue.getText().setVisible(bPaidWithDiscount.getSelection());
//		textPaidValue.setToolTipText(labelPaidValue.getToolTipText());
//		superviceControl(textPaidValue.getText(), 32);
//		GridDataFactory.swtDefaults().hint(80, SWT.DEFAULT).align(SWT.END, SWT.CENTER).applyTo(textPaidValue.getText());
//
//		// Total value
//		Label labelTotalValue = new Label(bottom, SWT.NONE);
//		//T: Label in the voucher editor
//		labelTotalValue.setText(_("Total Value") + ":");
//		//T: Tool Tip Text
//		labelTotalValue.setToolTipText(_("The total value of the voucher (without discount)."));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelTotalValue);
//
//		totalValue.setValue(voucher.getStringValueByKey("total"));
//
//		textTotalValue = new CurrencyText(this, bottom, SWT.BORDER | SWT.RIGHT, totalValue);
//		textTotalValue.getText().setEditable(false);
//		textTotalValue.setToolTipText(labelTotalValue.getToolTipText());
//		GridDataFactory.swtDefaults().hint(80, SWT.DEFAULT).align(SWT.END, SWT.CENTER).applyTo(textTotalValue.getText());
//
//
//	}
//
//	/**
//	 * Return the table items
//	 * 
//	 * @return The table items
//	 */
//	public TableViewer getTableViewerItems() {
//		return tableViewerItems;
//	}
	
	/**
	 * Set the focus to the top composite.
	 * 
	 * @see com.sebulli.fakturama.editors.Editor#setFocus()
	 */
	@Focus
	public void setFocus() {
		if(top != null) 
			top.setFocus();
	}

}
