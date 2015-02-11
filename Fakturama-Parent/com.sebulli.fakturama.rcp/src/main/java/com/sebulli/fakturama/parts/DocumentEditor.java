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


/**
 * The document editor for all types of document like letter, order,
 * confirmation, invoice, delivery, credit and dunning
 * 
 * @author Gerd Bartelt
 */
public class DocumentEditor {

	// Editor's ID
    public static final String EDITOR_ID = "DocumentEditor";
	public static final String ID = "com.sebulli.fakturama.editors.documentEditor";
//
//	// This UniDataSet represents the editor's input
//	private DataSetDocument document;
//
//	// SWT components of the editor
//	private Composite top;
//	private Text txtName;
//	private DateTime dtDate;
//	private DateTime dtOrderDate;
//	private DateTime dtServiceDate;
//	private Text txtCustomerRef;
//	private Text txtConsultant;
//	private Text txtAddress;
//	private Combo comboNoVat;
//	private ComboViewer comboViewerNoVat;
//	private Combo comboNetGross;
//	private Text txtInvoiceRef;
//	private TableViewer tableViewerItems;
//	private Text txtMessage;
//	private Text txtMessage2;
//	private Text txtMessage3;
//	private Button bPaid;
//	private Composite paidContainer;
//	private Composite paidDataContainer = null;
//	private Combo comboPayment;
//	private ComboViewer comboViewerPayment;
//	private Label warningDepositIcon;
//	private Label warningDepositText;
//	private Spinner spDueDays;
//	private DateTime dtIssueDate;
//	private DateTime dtPaidDate;
//	private Label itemsSum;
//	private Text itemsDiscount;
//	private Combo comboShipping;
//	private ComboViewer comboViewerShipping;
//	private Text shippingValue;
//	//private Text depositValue;
//	private Label vatValue;
//	private Label totalValue;
//	private Composite addressAndIconComposite;
//	private Label differentDeliveryAddressIcon;
//	private Label netLabel;
//	private TableColumnLayout tableColumnLayout;
//
//	// Column number of the unit and total price. Use this to update the column
//	private int unitPriceColumn = -1;
//	private int totalPriceColumn = -1;
//	
//	private List<UniDataSetTableColumn> itemTableColumns = new ArrayList<UniDataSetTableColumn>();
//	private CellNavigation cellNavigation;
//
//	
//	// These flags are set by the preference settings.
//	// They define, if elements of the editor are displayed, or not.
//	private boolean useGross;
//
//	// The items of this document
//	private DataSetArray<DataSetItem> items;
//
//	// The type of this document
//	private DocumentType documentType;
//
//	// These are (non visible) values of the document
//	private int addressId = -1;
//	private boolean noVat;
//	private String noVatName;
//	private String noVatDescription;
//	private int paymentId;
//	private UniData paidValue = new UniData(UniDataType.DOUBLE, 0.0);
//	private int shippingId;
//	private Double shipping = 0.0;
//	private Double shippingVat = 0.0;
//	private String shippingVatDescription = "";
//	private int shippingAutoVat = DataSetShipping.SHIPPINGVATGROSS;
//	private Double total = 0.0;
//	private Double deposit = 0.0;
//	//private Double finalPayment = 0.0;
//	private int dunningLevel = 0;
//	private int duedays;
//	private String billingAddress = "";
//	private String deliveryAddress = "";
//	private DocumentEditor thisDocumentEditor;
//	private int netgross = DocumentSummary.NOTSPECIFIED;
//	
//	// Flag, if item editing is active
//	private DocumentItemEditingSupport itemEditingSupport = null;
//
//	// Flag if there are items with property "optional" set
//	private boolean containsOptionalItems = false;
//
//	// Flag if there are items with an discount set
//	private boolean containsDiscountedItems = false;
//
//	// Action to print this document's content.
//	// Print means: Export the document in an OpenOffice document
//	CreateOODocumentAction printAction;
//
//	// defines, if the contact is new created
//	private boolean newDocument;
//
//	// Menu manager of the context menu
//	private MenuManager menuManager;
//
//	// If the customer is changed, and this document displays no payment text,
//	// use this variable to store the payment and due days
//	private int newPaymentID = -1;
//	private String newPaymentDescription = "";
//	
//	// Imported delivery notes. This list is used to
//	// set an reference to this document, if it's an invoice.
//	// The reference is not set during the import but later when the
//	// document is saved. Because the the  document has an id to reference to.
//	private List<Integer> importedDeliveryNotes = new ArrayList<Integer>();
//	
//	/**
//	 * Constructor
//	 * 
//	 * Associate the table view with the editor
//	 */
//	public DocumentEditor() {
//		cellNavigation = new CellNavigation(itemTableColumns);
//		tableViewID = ViewDocumentTable.ID;
//		editorID = "document";
//		thisDocumentEditor = this;
//	}
//
//	/**
//	 * Select the next cell
//	 * @param keyCode
//	 * @param element
//	 * @param itemEditingSupport
//	 */
//	public void selectNextCell(int keyCode, Object element, DocumentItemEditingSupport itemEditingSupport) {
//		cellNavigation.selectNextCell(keyCode, element, itemEditingSupport, items,tableViewerItems);
//	}
//	
//	/**
//	 * Mark this document as printed
//	 */
//	public void markAsPrinted() {
//		document.setBooleanValueByKey("printed", true);
//		// Refresh the table view
//		//refreshView();
//		//checkDirty();
//
//	}
//	
//	/**
//	 * Saves the contents of this part
//	 * 
//	 * @param monitor
//	 *            Progress monitor
//	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
//	 */
//	@Override
//	public void doSave(IProgressMonitor monitor) {
//
//		/*
//		 * the following parameters are not saved: 
//		 * - id (constant) 
//		 * - progress (not modified by editor) 
//		 * - transaction (not modified by editor)
//		 * - webshopid (not modified by editor)
//		 * - webshopdate (not modified by editor)
//		 *  ITEMS:
//		 *  	- id (constant) 
//		 *  	- deleted (is checked by the items string)
//		 *  	- shared (not modified by editor)
//		 */
//
//		// Cancel the item editing
//		if (itemEditingSupport != null)
//			itemEditingSupport.cancelAndSave();
//
//		boolean wasDirty = isDirty();
//
//		if (newDocument) {
//			// Check, if the document number is the next one
//			if (documentType != DocumentType.LETTER) {
//				int result = setNextNr(txtName.getText(), "name", Data.INSTANCE.getDocuments());
//
//				// It's not the next free ID
//				if (result == ERROR_NOT_NEXT_ID) {
//					// Display an error message
//					MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
//
//					//T: Title of the dialog that appears if the document number is not valid.
//					messageBox.setText(_("Error in document number"));
//					
//					//T: Text of the dialog that appears if the customer number is not valid.
//					messageBox.setMessage(_("Document number is not the next free one:") + " " + getNextNr() + "\n" + 
//							//T: Text of the dialog that appears if the number is not valid.
//							_("See Preferences/Number Range."));
//					messageBox.open();
//				}
//			}
//
//		}
//
//		// Exit save if there is a document with the same number
//		if (thereIsOneWithSameNumber())
//			return;
//
//		// Always set the editor's data set to "undeleted"
//		document.setBooleanValueByKey("deleted", false);
//
//		// Set the document type
//		document.setIntValueByKey("category", documentType.getInt());
//
//		// Set name and date
//		document.setStringValueByKey("name", txtName.getText());
//		document.setStringValueByKey("date", DataUtils.getDateTimeAsString(dtDate));
//
//		// If this is an order, use the date as order date
//		if (documentType == DocumentType.ORDER)
//			document.setStringValueByKey("orderdate", DataUtils.getDateTimeAsString(dtDate));
//		else
//			document.setStringValueByKey("orderdate", DataUtils.getDateTimeAsString(dtOrderDate));
//
//		document.setStringValueByKey("servicedate", DataUtils.getDateTimeAsString(dtServiceDate));
//		
//		document.setIntValueByKey("addressid", addressId);
//		String addressById = "";
//
//		// Test, if the txtAddress field was modified
//		// and write the content of the txtAddress to the documents address or
//		// delivery address
//		boolean addressModified = false;
//		// if it's a delivery note, compare the delivery address
//		if (documentType == DocumentType.DELIVERY) {
//			if (!DataUtils.MultiLineStringsAreEqual(document.getStringValueByKey("deliveryaddress"), txtAddress.getText()))
//				addressModified = true;
//			document.setStringValueByKey("deliveryaddress", DataUtils.removeCR(txtAddress.getText()));
//
//			// Use the delivery address, if the billing address is empty
//			if (billingAddress.isEmpty())
//				billingAddress = DataUtils.removeCR(txtAddress.getText());
//			document.setStringValueByKey("address", billingAddress);
//
//			if (addressId >= 0)
//				addressById = Data.INSTANCE.getContacts().getDatasetById(addressId).getAddress(true);
//		}
//		else {
//			if (!document.getStringValueByKey("address").equals(txtAddress.getText()))
//				addressModified = true;
//			document.setStringValueByKey("address", DataUtils.removeCR(txtAddress.getText()));
//
//			// Use the billing address, if the delivery address is empty
//			if (deliveryAddress.isEmpty())
//				deliveryAddress = DataUtils.removeCR(txtAddress.getText());
//			
//			document.setStringValueByKey("deliveryaddress", deliveryAddress);
//
//			if (addressId >= 0)
//				addressById = Data.INSTANCE.getContacts().getDatasetById(addressId).getAddress(false);
//		}
//
//		// Show a warning, if the entered address is not similar to the address
//		// of the document, set by the address ID.
//		if ((addressId >= 0) && (addressModified)) {
//			if (DataUtils.similarity(addressById, DataUtils.removeCR(txtAddress.getText())) < 0.75) {
//				MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_WARNING | SWT.OK);
//
//				//T: Title of the dialog that appears if the document is assigned to  an other address.
//				messageBox.setText(_("Please verify"));
//				
//				//T: Text of the dialog that appears if the document is assigned to  an other address.
//				messageBox.setMessage(_("This document is assiged to the contact:") + "\n\n" + addressById + "\n\n" + 
//						//T: Text of the dialog that appears if the document is assigned to  an other address.
//						_("You have entered a different one."));
//				messageBox.open();
//			}
//		}
//		
//		
//		// Set the customer reference number
//		document.setStringValueByKey("customerref", txtCustomerRef.getText());
//		
//		// Set the consultant value
//		document.setStringValueByKey("consultant", txtConsultant.getText());
//
//		// Set the payment values depending on if the document is paid or not
//		// Set the shipping values
//		if (comboPayment != null) {
//			document.setStringValueByKey("paymentdescription", comboPayment.getText());
//		}
//		// If this document contains no payment widgets, but..
//		else {
//			// the customer changed and so there is a new payment. Set it.
//			if (!newPaymentDescription.isEmpty()) {
//				document.setStringValueByKey("paymentdescription", newPaymentDescription);
//			}
//
//		}
//
//		document.setIntValueByKey("paymentid", paymentId);
//
//		if (bPaid != null) {
//			String paymentText = "";
//
//			if (bPaid.getSelection()) {
//				document.setBooleanValueByKey("paid", true);
//				document.setStringValueByKey("paydate", DataUtils.getDateTimeAsString(dtPaidDate));
//				document.setDoubleValueByKey("payvalue", paidValue.getValueAsDouble());
//				deposit = 0.0;
//				//System.out.println(paidValue.getValueAsString());
//				if(paidValue.getValueAsDouble() < total){
//					deposit = paidValue.getValueAsDouble();
//					document.setBooleanValueByKey("isdeposit", true);
//					document.setBooleanValueByKey("paid", false);
//				}
//				if(documentType == DocumentType.INVOICE) {
//					// update dunnings
//					updateDunnings();
//				}
//
//				// Use the text for "paid" from the current payment
//				if (paymentId >= 0) {
//					if (document.getBooleanValueByKey("paid")){
//					paymentText = Data.INSTANCE.getPayments().getDatasetById(paymentId).getStringValueByKey("paidtext");
//				}
//					else
//						if (document.getBooleanValueByKey("deposit")){
//							paymentText = Data.INSTANCE.getPayments().getDatasetById(paymentId).getStringValueByKey("deposittext");
//						}
//				}
//
//			}
//			else {
//				document.setBooleanValueByKey("paid", false);
//				document.setDoubleValueByKey("payvalue", 0.0);
//				document.setBooleanValueByKey("isdeposit", false);
//
//				// Use the text for "unpaid" from the current payment
//				if (paymentId >= 0) {
//					paymentText = Data.INSTANCE.getPayments().getDatasetById(paymentId).getStringValueByKey("unpaidtext");
//				}
//
//			}
//			document.setIntValueByKey("duedays", duedays);
//			
//			document.setStringValueByKey("paymenttext", paymentText);
//
//		}
//		// If this document contains no payment widgets, but..
//		else {
//			// the customer changed and so there is a new payment. Set it.
//			if (!newPaymentDescription.isEmpty() && (newPaymentID >= 0)) {
//				document.setIntValueByKey("duedays", duedays);
//				document.setBooleanValueByKey("paid", false);
//				document.setDoubleValueByKey("payvalue", 0.0);
//
//				// Use the text for "unpaid" from the current payment
//				document.setStringValueByKey("paymenttext", Data.INSTANCE.getPayments().getDatasetById(newPaymentID).getStringValueByKey("unpaidtext"));
//			}
//		}
//
//
//		// Set the shipping values
//		if (comboShipping != null) {
//			document.setStringValueByKey("shippingdescription", comboShipping.getText());
//		}
//		document.setIntValueByKey("shippingid", shippingId);
//		document.setDoubleValueByKey("shipping", shipping);
//		document.setDoubleValueByKey("shippingvat", shippingVat);
//		document.setStringValueByKey("shippingvatdescription", shippingVatDescription);
//		document.setIntValueByKey("shippingautovat", shippingAutoVat);
//
//		// Set the discount value
//		if (itemsDiscount != null)
//			document.setDoubleValueByKey("itemsdiscount", DataUtils.StringToDoubleDiscount(itemsDiscount.getText()));
//
//		// Set the total value.
//		document.setDoubleValueByKey("total", total);
//
//		//Set the deposit value
//		document.setDoubleValueByKey("deposit", deposit);
//		
//		// Set the message
//		document.setStringValueByKey("message", DataUtils.removeCR(txtMessage.getText()));
//		if (txtMessage2 != null)
//		document.setStringValueByKey("message2", DataUtils.removeCR(txtMessage2.getText()));
//		if (txtMessage3 != null)
//		document.setStringValueByKey("message3", DataUtils.removeCR(txtMessage3.getText()));
//
//		// Set the whole vat of the document to zero
//		document.setBooleanValueByKey("novat", noVat);
//		document.setStringValueByKey("novatname", noVatName);
//		document.setStringValueByKey("novatdescription", noVatDescription);
//
//		// Set whether the document uses net or gross values
//		document.setIntValueByKey("netgross", netgross);
//		
//		// Set the dunning level
//		document.setIntValueByKey("dunninglevel", dunningLevel);
//
//		// Create a new document ID, if this is a new document
//		int documentId = document.getIntValueByKey("id");
//		if (newDocument) {
//			documentId = Data.INSTANCE.getDocuments().getNextFreeId();
//		}
//		
//		// Update the invoice references in all documents within the same transaction
//		if(documentType.equals(DocumentType.INVOICE)) {
//			Transaction trans = new Transaction(document);
//			List<DataSetDocument> docs = trans.getDocuments();
//			for (DataSetDocument doc : docs) {
//				if(doc.getIntValueByKey("invoiceid") < 0) {
//			        doc.setIntValueByKey("invoiceid", documentId );
//			        Data.INSTANCE.updateDataSet(doc);
//				}
//	        }
//		}
//		
//
//		// Update the references in the delivery notes
//		for (Integer importedDeliveryNote : importedDeliveryNotes) {
//			if (importedDeliveryNote >= 0) {
//				DataSetDocument deliveryNote = Data.INSTANCE.getDocuments().getDatasetById(importedDeliveryNote);
//				deliveryNote.setIntValueByKey("invoiceid", documentId );
//				Data.INSTANCE.updateDataSet(deliveryNote);
//				
//				// Change also the transaction id of the imported delivery note
//				Transaction.mergeTwoTransactions(document, deliveryNote);
//
//			}
//				
//		}
//		importedDeliveryNotes.clear();
//		
//		// Set all the items
//		ArrayList<DataSetItem> itemDatasets = items.getActiveDatasets();
//		String itemsString = "";
//
//		for (DataSetItem itemDataset : itemDatasets) {
//
//			// Get the ID of this item and
//			int id = itemDataset.getIntValueByKey("id");
//			// the ID of the owner document
//			int owner = itemDataset.getIntValueByKey("owner");
//
//			boolean saveNewItem = true;
//			DataSetItem item = null;
//
//			// If the ID of this item is -1, this was a new item
//			if (id >= 0) {
//				item = Data.INSTANCE.getItems().getDatasetById(id);
//				// Compare all data of the item in this document editor
//				// with the item in the document.
//				boolean modified = ((!item.getStringValueByKey("name").equals(itemDataset.getStringValueByKey("name")))
//						|| (!item.getStringValueByKey("itemnr").equals(itemDataset.getStringValueByKey("itemnr")))
//						|| (!item.getStringValueByKey("description").equals(itemDataset.getStringValueByKey("description")))
//						|| (!item.getStringValueByKey("category").equals(itemDataset.getStringValueByKey("category")))
//						|| (!DataUtils.DoublesAreEqual(item.getDoubleValueByKey("quantity"), itemDataset.getDoubleValueByKey("quantity")))
//						|| (!DataUtils.DoublesAreEqual(item.getDoubleValueByKey("price"), itemDataset.getDoubleValueByKey("price")))
//						|| (!DataUtils.DoublesAreEqual(item.getDoubleValueByKey("discount"), itemDataset.getDoubleValueByKey("discount")))
//						|| (item.getIntValueByKey("owner") != itemDataset.getIntValueByKey("owner"))
//						|| (item.getIntValueByKey("vatid") != itemDataset.getIntValueByKey("vatid"))
//						|| (!DataUtils.DoublesAreEqual(item.getDoubleValueByKey("vatvalue"), itemDataset.getDoubleValueByKey("vatvalue")))
//						|| (item.getBooleanValueByKey("novat") != itemDataset.getBooleanValueByKey("novat"))
//						|| (!item.getStringValueByKey("vatname").equals(itemDataset.getStringValueByKey("vatname")))
//						|| (!item.getStringValueByKey("vatdescription").equals(itemDataset.getStringValueByKey("vatdescription")))
//						|| (item.getBooleanValueByKey("optional") != itemDataset.getBooleanValueByKey("optional")));
//
//				// If the item was modified and was shared with other documents,
//				// than we should make a copy and save it new.
//				// We also save it, if it was a new item with no owner yet,
//				saveNewItem = ((owner < 0) || (modified && ((owner != document.getIntValueByKey("id")) || item.getBooleanValueByKey("shared"))));
//			}
//			else {
//				// It was a new item with no ID set
//				saveNewItem = true;
//			}
//
//			// Create a new item
//			// The owner of this new item is the document from this editor.
//			// And because it's new, it is not shared with other documents.
//			if (saveNewItem) {
//				itemDataset.setIntValueByKey("owner", documentId);
//				itemDataset.setBooleanValueByKey("shared", false);
//				DataSetItem itemDatasetTemp = Data.INSTANCE.getItems().addNewDataSet(new DataSetItem(itemDataset));
//				id = itemDatasetTemp.getIntValueByKey("id");
//				itemDataset.setIntValueByKey("id", id);
//			}
//			// If it's not new, copy the items's data from the editor to the
//			// items in the data base
//			else {
//				item.setStringValueByKey("name", itemDataset.getStringValueByKey("name"));
//				item.setStringValueByKey("itemnr", itemDataset.getStringValueByKey("itemnr"));
//				item.setStringValueByKey("description", itemDataset.getStringValueByKey("description"));
//				item.setStringValueByKey("category", itemDataset.getStringValueByKey("category"));
//				item.setDoubleValueByKey("quantity", itemDataset.getDoubleValueByKey("quantity"));
//				item.setDoubleValueByKey("price", itemDataset.getDoubleValueByKey("price"));
//				item.setDoubleValueByKey("discount", itemDataset.getDoubleValueByKey("discount"));
//				item.setIntValueByKey("owner", itemDataset.getIntValueByKey("owner"));
//				item.setIntValueByKey("vatid", itemDataset.getIntValueByKey("vatid"));
//				item.setBooleanValueByKey("novat", itemDataset.getBooleanValueByKey("novat"));
//				item.setDoubleValueByKey("vatvalue", itemDataset.getDoubleValueByKey("vatvalue"));
//				item.setStringValueByKey("vatname", itemDataset.getStringValueByKey("vatname"));
//				item.setStringValueByKey("vatdescription", itemDataset.getStringValueByKey("vatdescription"));
//				item.setBooleanValueByKey("optional", itemDataset.getBooleanValueByKey("optional"));
//
//				Data.INSTANCE.getItems().updateDataSet(item);
//			}
//
//			// Collect all item IDs in a sting and separate them by a comma
//			if (itemsString.length() > 0)
//				itemsString += ",";
//			itemsString += Integer.toString(id);
//		}
//		// Set the string value
//		document.setStringValueByKey("items", itemsString);
//
//		// Set the "addressfirstline" value to the first line of the
//		// contact address
//		if (addressId >= 0) {
//			document.setStringValueByKey("addressfirstline", Data.INSTANCE.getContacts().getDatasetById(addressId).getNameWithCompany(false));
//		}
//		else {
//			String s = DataUtils.removeCR(txtAddress.getText());
//			
//			// Remove the "\n" if it was a "\n" as line break.
//			s = s.split("\n")[0];
//			
//			document.setStringValueByKey("addressfirstline", s);
//		}
//
//		// Mark the (modified) document as "not printed"
//		if (wasDirty)
//			document.setBooleanValueByKey("printed", false);
//
//		// If it is a new document,
//		if (newDocument) {
//
//			// Create this in the data base
//			document = Data.INSTANCE.getDocuments().addNewDataSet(document);
//
//			// If it's an invoice, set the "invoiceid" to the ID.
//			// So all documents will inherit this ID
//			if ((documentType == DocumentType.INVOICE) && (document.getIntValueByKey("id") != document.getIntValueByKey("invoiceid"))) {
//				document.setIntValueByKey("invoiceid", document.getIntValueByKey("id"));
//				Data.INSTANCE.getDocuments().updateDataSet(document);
//			}
//
//			// Now, it is no longer new.
//			newDocument = false;
//
//			// Create a new editor input.
//			// So it's no longer the parent data
//			this.setInput(new UniDataSetEditorInput(document));
//		}
//		else {
//
//			// Do not create a new data set - just update the old one
//			Data.INSTANCE.getDocuments().updateDataSet(document);
//		}
//
//		//Set the editor's name
//		setPartName(document.getStringValueByKey("name"));
//		
//		// Refresh the table view
//		refreshView();
//		checkDirty();
//	}
//
//	/**
//	 * Updates all Dunnings which are related to the current invoice.
//	 * TODO: REFACTOR if database layer is switched to JPA (should be a one-liner...)
//	 */
//	private void updateDunnings() {
//		DataSetArray<DataSetDocument> content = Data.INSTANCE.getDocuments();
//		
//		// Create a 2nd list, which will contain only those elements,
//		// that are not deleted and match the filters.
//		ArrayList<DataSetDocument> dunnings = new ArrayList<DataSetDocument>();
//
//		// Check all dunning entries and collect matching dunnings
//		int invoiceId = document.getIntValueByKey("id");
//		for (DataSetDocument uds : content.getActiveDatasetsByCategory(Integer.toString(DocumentType.getInt(DocumentType.DUNNING)))) {
//			if (invoiceId == uds.getIntValueByKey("invoiceid")) {
//				dunnings.add(uds);
//			}
//		}
//		
//		// TODO What if "payvalue" is not the total sum? Is it paid?
//		for (DataSetDocument dunning : dunnings) {
//			dunning.setPaid(bPaid.getSelection());
//			dunning.setStringValueByKey("paydate", DataUtils.getDateTimeAsString(dtPaidDate));
//			dunning.setDoubleValueByKey("payvalue", paidValue.getValueAsDouble());
//			Data.INSTANCE.getDocuments().updateDataSet(dunning);
//		}
//	}
//
//	/**
//	 * There is no saveAs function
//	 */
//	@Override
//	public void doSaveAs() {
//	}
//
//	/**
//	 * Initializes the editor. If an existing data set is opened, the local
//	 * variable "document" is set to This data set. If the editor is opened to
//	 * create a new one, a new data set is created and the local variable
//	 * "contact" is set to this one.
//	 * 
//	 * @param input
//	 *            The editor's input
//	 * @param site
//	 *            The editor's site
//	 */
//	@Override
//	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
//
//		// Set the site and the input
//		setSite(site);
//		setInput(input);
//
//		// Set the editors data set to the editors input
//		document = (DataSetDocument) ((UniDataSetEditorInput) input).getUniDataSet();
//
//		// If the document is a duplicate of an other document,
//		// the input is the parent document.
//		DataSetDocument parent = document;
//		// the parents document type
//		DocumentType documentTypeParent = DocumentType.NONE;
//		boolean duplicated = ((UniDataSetEditorInput) input).getDuplicate();
//
//		// The document is new, if there is no document, or if the
//		// flag for duplicated was set.
//		newDocument = (document == null) || duplicated;
//
//		// If new ..
//		if (newDocument) {
//
//			// .. get the document type (=the category) to ..
//			String category = ((UniDataSetEditorInput) input).getCategory();
//			documentType = DocumentType.getType(category);
//			if (documentType == DocumentType.NONE)
//				documentType = DocumentType.ORDER;
//
//			// create a new data set with this document type
//			if (duplicated)
//				document = new DataSetDocument(documentType, parent);
//			else
//				document = new DataSetDocument(documentType);
//
//			// Copy the entry "message", or reset it to ""
//			if (!Activator.getDefault().getPreferenceStore().getBoolean("DOCUMENT_COPY_MESSAGE_FROM_PARENT")) {
//				document.setStringValueByKey("message", "");
//				document.setStringValueByKey("message2", "");
//				document.setStringValueByKey("message3", "");
//			}
//			
//			// Set the editor ID to the document type
//			editorID = documentType.getTypeAsString();
//
//			// get the parents document type
//			if (parent != null)
//				documentTypeParent = DocumentType.getType(parent.getIntValueByKey("category"));
//
//			// If it's a dunning, increase the dunning level by 1
//			if (documentType == DocumentType.DUNNING) {
//				if (documentTypeParent == DocumentType.DUNNING)
//					dunningLevel = document.getIntValueByKey("dunninglevel") + 1;
//				else
//					dunningLevel = 1;
//			}
//
//			// If it's a credit or a dunning, set it to unpaid
//			if ( (documentType == DocumentType.CREDIT)|| (documentType == DocumentType.DUNNING)) {
//				document.setBooleanValueByKey("paid", false);
//			}
//			
//			// Set the editors name
//			setPartName(documentType.getNewText());
//
//			// In a new document, set some standard values
//			if (!duplicated) {
//				// Default shipping
//				shippingId = Data.INSTANCE.getPropertyAsInt("standardshipping");
//				DataSetShipping stdShipping = Data.INSTANCE.getShippings().getDatasetById(shippingId);
//				shipping = stdShipping.getDoubleValueByKey("value");
//				shippingVat = stdShipping.getDoubleValueByKeyFromOtherTable("vatid.VATS:value");
//				shippingAutoVat = stdShipping.getIntValueByKey("autovat");
//				shippingVatDescription = stdShipping.getStringValueByKey("description");
//				netgross = DocumentSummary.NOTSPECIFIED;
//				
//				document.setDoubleValueByKey("shipping", shipping);
//				document.setDoubleValueByKey("shippingvat", shippingVat);
//				document.setStringValueByKey("shippingdescription", stdShipping.getStringValueByKey("description"));
//				document.setIntValueByKey("shippingautovat", shippingAutoVat);
//				document.setStringValueByKey("shippingvatdescription", shippingVatDescription);
//				
//				
//				// Default payment
//				paymentId = Data.INSTANCE.getPropertyAsInt("standardpayment");
//				document.setStringValueByKey("paymentdescription", Data.INSTANCE.getPayments().getDatasetById(paymentId).getStringValueByKey("description"));
//				document.setIntValueByKey("duedays", Data.INSTANCE.getPayments().getDatasetById(paymentId).getIntValueByKey("netdays"));
//			}
//			else {
//				paymentId = document.getIntValueByKey("paymentid");
//				shippingId = document.getIntValueByKey("shippingid");
//				total = document.getDoubleValueByKey("total");
//			}
//
//			// Get the next document number
//			document.setStringValueByKey("name", getNextNr());
//
//		}
//		// If an existing document was opened ..
//		else {
//
//			// Get document type, set editorID
//			documentType = DocumentType.getType(document.getIntValueByKey("category"));
//			editorID = documentType.getTypeAsString();
//
//			paymentId = document.getIntValueByKey("paymentid");
//			shippingId = document.getIntValueByKey("shippingid");
//
//			// and the editor's part name
//			setPartName(document.getStringValueByKey("name"));
//
//		}
//
//		// These variables contain settings, that are not in
//		// visible SWT widgets.
//		duedays = document.getIntValueByKey("duedays");
//		addressId = document.getIntValueByKey("addressid");
//		
//		noVat = document.getBooleanValueByKey("novat");
//		noVatName = document.getStringValueByKey("novatname");
//		noVatDescription = document.getStringValueByKey("novatdescription");
//		netgross = document.getIntValueByKey("netgross");
//		
//		paidValue.setValue(document.getDoubleValueByKey("payvalue"));
//		if (dunningLevel <= 0)
//			dunningLevel = document.getIntValueByKey("dunninglevel");
//
//		
//		
//		// Create a set of new temporary items.
//		// These items exist only in the memory.
//		// If the editor is opened, the items from the document are
//		// copied to this item set. If the editor is closed or saved,
//		// these items are copied back to the document and to the data base.
//		items = new DataSetArray<DataSetItem>();
//
//		// Get all items by ID from the item string
//		String itemsString = document.getStringValueByKey("items");
//		String[] itemsStringParts = itemsString.split(",");
//
//		billingAddress = document.getStringValueByKey("address");
//		deliveryAddress = document.getStringValueByKey("deliveryaddress");
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
//				int parentSign = DocumentType.getType(parent.getIntValueByKey("category")).sign();
//
//				// And copy the item to a new one
//				DataSetItem item = Data.INSTANCE.getItems().getDatasetById(id);
//				
//				
//				// the new item
//				DataSetItem newItem;
//				
//				// Set the sign
//				if (parentSign != documentType.sign())
//					newItem = new DataSetItem(item, -1);
//				else
//					newItem = new DataSetItem(item);
//
//				// Reset the property "optional" from all items,
//				// if the parent document was an offer
//				if (documentTypeParent == DocumentType.OFFER) {
//					newItem.setBooleanValueByKey("optional", false);
//				}
//
//				// Show the columns "optional" if at least one item
//				// with this property set was found
//				if (newItem.getBooleanValueByKey("optional"))
//					containsOptionalItems = true;
//				
//				// Show the columns discount if at least one item
//				// with a discounted price was found
//				if (!DataUtils.DoublesAreEqual(newItem.getDoubleValueByKey("discount"),0.0))
//					containsDiscountedItems = true;
//
//				// Add the new item
//				items.getDatasets().add(newItem);
//			}
//		}
//		
//		// Renumber all Items
//		RenumberItems();
//	}
//	
//	/**
//	 * Returns whether the contents of this part have changed since the last
//	 * save operation
//	 * 
//	 * @see org.eclipse.ui.part.EditorPart#isDirty()
//	 */
//	@Override
//	public boolean isDirty() {
//
//		/*
//		 * the following parameters are not checked: 
//		 * - id (constant)
//		 * - addressfirstline (generated by editor) 
//		 * - progress (not modified by editor) 
//		 * - transaction (not modified by editor) 
//		 * - webshopid (not modified by editor)
//		 * - webshopdate (not modified by editor)
//		 *  - total (generated by editor)
//		 * 
//		 * ITEMS: 
//		 * - id (constant) 
//		 * - deleted (is checked by the items string)
//		 * - shared (not modified by editor)
//		 */
//
//		// Check, if a cell is being modified at this moment
//		if (tableViewerItems != null)
//			if (tableViewerItems.isCellEditorActive() && (itemEditingSupport != null))
//				return true;
//
//		// Test all the document parameters
//		if (document.getBooleanValueByKey("deleted")) { return true; }
//
//		if (newDocument) { return true; }
//		if (document.getIntValueByKey("category") != documentType.getInt()) { return true; }
//		if (!document.getStringValueByKey("name").equals(txtName.getText())) { return true; }
//
//		if (!document.getStringValueByKey("date").equals(DataUtils.getDateTimeAsString(dtDate))) { return true; }
//
//		// If this is an order, use the dtDate widget and do not check the
//		// dtOrderDate widget
//		if (documentType != DocumentType.ORDER) {
//			String orderDateString = document.getStringValueByKey("orderdate");
//			if (orderDateString.isEmpty()) {
//				orderDateString = document.getStringValueByKey("webshopdate");
//			}
//			if (!orderDateString.equals(DataUtils.getDateTimeAsString(dtOrderDate))) { return true; }
//		}
//		
//		if (!document.getStringValueByKey("servicedate").equals(DataUtils.getDateTimeAsString(dtServiceDate))) { return true; }
//
//		if (document.getIntValueByKey("addressid") != addressId) { return true; }
//		if (documentType == DocumentType.DELIVERY) {
//			if (!DataUtils.MultiLineStringsAreEqual(document.getStringValueByKey("deliveryaddress"), txtAddress.getText())) { return true; }
//			if (!DataUtils.MultiLineStringsAreEqual(document.getStringValueByKey("address"), billingAddress)) { return true; }
//		}
//		else {
//			if (!DataUtils.MultiLineStringsAreEqual(document.getStringValueByKey("address"), txtAddress.getText())) { return true; }
//			if (!DataUtils.MultiLineStringsAreEqual(document.getStringValueByKey("deliveryaddress"), deliveryAddress)) { return true; }
//		}
//
//		if (!document.getStringValueByKey("customerref").equals(txtCustomerRef.getText())) { return true; }
//
//		if (!document.getStringValueByKey("consultant").equals(txtConsultant.getText())) { return true; }
//		
//		
//		if (spDueDays != null)
//			if (document.getBooleanValueByKey("paid") != bPaid.getSelection()
//			&& document.getBooleanValueByKey("isdeposit")
//			&& !DataUtils.DoublesAreEqual(deposit, paidValue.getValueAsDouble())
//			|| document.getBooleanValueByKey("paid") != bPaid.getSelection()
//			&& !document.getBooleanValueByKey("isdeposit")
//			&& !DataUtils.DoublesAreEqual(document.getDoubleValueByKey("payvalue"), paidValue.getValueAsDouble())) { return true; }
//			else {
//				if (document.getBooleanValueByKey("isdeposit") != bPaid.getSelection()) { return true; }
//			}
//		
//		/*
//		 * 				//System.out.println(paidValue.getValueAsString());
//				if(paidValue.getValueAsDouble() < total){
//					deposit = paidValue.getValueAsDouble();
//					document.setBooleanValueByKey("isdeposit", true);
//					document.setBooleanValueByKey("paid", false);
//				}
//
//		 */
//		if (bPaid != null) {
//			if (bPaid.getSelection()) {
//				if (!document.getStringValueByKey("paydate").equals(DataUtils.getDateTimeAsString(dtPaidDate))) { return true; }
//				if (!DataUtils.DoublesAreEqual(paidValue.getValueAsDouble(), document.getDoubleValueByKey("payvalue"))) { return true; }
//			}
//			else {
//				if (document.getIntValueByKey("duedays") != spDueDays.getSelection()) { return true; }
//			}
//		}
//		if (comboPayment != null)
//			if (!document.getStringValueByKey("paymentdescription").equals(comboPayment.getText())) { return true; }
//		if (document.getIntValueByKey("paymentid") != paymentId) { return true; }
//		
//		if (itemsDiscount != null)
//			if (!DataUtils.DoublesAreEqual(DataUtils.StringToDoubleDiscount(itemsDiscount.getText()), document.getDoubleValueByKey("itemsdiscount"))) { return true; }
//
//		if (document.getIntValueByKey("shippingid") != shippingId) { return true; }
//		if (!DataUtils.DoublesAreEqual(shipping, document.getDoubleValueByKey("shipping"))) { return true; }
//		if (!DataUtils.DoublesAreEqual(shippingVat, document.getDoubleValueByKey("shippingvat"))) { return true; }
//		if (comboShipping != null)
//			if (!document.getStringValueByKey("shippingdescription").equals(comboShipping.getText())) { return true; }
//		if (!DataUtils.DoublesAreEqual(deposit, document.getDoubleValueByKey("deposit"))) { return true; }
//		if (!DataUtils.MultiLineStringsAreEqual(document.getStringValueByKey("message"), txtMessage.getText())) { return true; }
//		if (txtMessage2 != null)
//			if (!DataUtils.MultiLineStringsAreEqual(document.getStringValueByKey("message2"), txtMessage2.getText())) { return true; }
//		if (txtMessage3 != null)
//			if (!DataUtils.MultiLineStringsAreEqual(document.getStringValueByKey("message3"), txtMessage3.getText())) { return true; }
//		if (!document.getStringValueByKey("shippingvatdescription").equals(shippingVatDescription)) { return true; }
//		if (document.getIntValueByKey("shippingautovat") != shippingAutoVat) { return true; }
//		if (document.getBooleanValueByKey("novat") != noVat) { return true; }
//		if (!document.getStringValueByKey("novatname").equals(noVatName)) { return true; }
//		if (!document.getStringValueByKey("novatdescription").equals(noVatDescription)) { return true; }
//		if (document.getIntValueByKey("netgross") != netgross) { return true; }
//		
//		// Test all the document items
//		String itemsString = "";
//		ArrayList<DataSetItem> itemDatasets = items.getActiveDatasets();
//		for (DataSetItem itemDataset : itemDatasets) {
//			int id = itemDataset.getIntValueByKey("id");
//
//			// If the owner is -1, it was a new item.
//			// New items are always saved.
//			if (itemDataset.getIntValueByKey("owner") < 0) {
//				return true;
//			}
//			else {
//
//				DataSetItem item = Data.INSTANCE.getItems().getDatasetById(id);
//				if (!item.getStringValueByKey("name").equals(itemDataset.getStringValueByKey("name"))) { return true; }
//				if (!item.getStringValueByKey("itemnr").equals(itemDataset.getStringValueByKey("itemnr"))) { return true; }
//				if (!item.getStringValueByKey("description").equals(itemDataset.getStringValueByKey("description"))) { return true; }
//				if (!item.getStringValueByKey("category").equals(itemDataset.getStringValueByKey("category"))) { return true; }
//				if (!DataUtils.DoublesAreEqual(item.getDoubleValueByKey("quantity"), itemDataset.getDoubleValueByKey("quantity"))) { return true; }
//				if (!DataUtils.DoublesAreEqual(item.getDoubleValueByKey("price"), itemDataset.getDoubleValueByKey("price"))) { return true; }
//				if (!DataUtils.DoublesAreEqual(item.getDoubleValueByKey("discount"), itemDataset.getDoubleValueByKey("discount"))) { return true; }
//				if (item.getIntValueByKey("owner") != itemDataset.getIntValueByKey("owner")) { return true; }
//				if (item.getIntValueByKey("vatid") != itemDataset.getIntValueByKey("vatid")) { return true; }
//				if (item.getBooleanValueByKey("novat") != itemDataset.getBooleanValueByKey("novat")) { return true; }
//				if (!DataUtils.DoublesAreEqual(item.getDoubleValueByKey("vatvalue"), itemDataset.getDoubleValueByKey("vatvalue"))) { return true; }
//				if (!item.getStringValueByKey("vatname").equals(itemDataset.getStringValueByKey("vatname"))) { return true; }
//				if (!item.getStringValueByKey("vatdescription").equals(itemDataset.getStringValueByKey("vatdescription"))) { return true; }
//				if (!item.getStringValueByKey("qunit").equals(itemDataset.getStringValueByKey("qunit"))) { return true; }
//				if (item.getBooleanValueByKey("optional") != itemDataset.getBooleanValueByKey("optional")) { return true; }
//			}
//			if (itemsString.length() > 0)
//				itemsString += ",";
//			itemsString += Integer.toString(id);
//		}
//
//		// Compare also the items string.
//		// So the document is dirty, if new items are added or items have
//		// been deleted.
//		if (!document.getStringValueByKey("items").equals(itemsString)) { return true; }
//
//		return false;
//	}
//
//	/**
//	 * Returns whether the "Save As" operation is supported by this part.
//	 * 
//	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
//	 * @return False, SaveAs is not allowed
//	 */
//	@Override
//	public boolean isSaveAsAllowed() {
//		return false;
//	}
//
//	/**
//	 * Renumber all items
//	 */
//	private void RenumberItems () {
//		
//		int no = 1;
//		// renumber all items
//		for (DataSetItem item : items.getActiveDatasets()) {
//			item.row = no;
//			no++;
//		}
//		
//		// Refresh the table viewer
//		if (tableViewerItems != null)
//			tableViewerItems.refresh();
//		
//	}
//
//	
//	/**
//	 * Sets a flag, if item editing is active
//	 * 
//	 * @param active
//	 *            , TRUE, if editing is active
//	 */
//	public void setItemEditing(DocumentItemEditingSupport itemEditingSupport) {
//		this.itemEditingSupport = itemEditingSupport;
//	}
//
//	/**
//	 * Set the "novat" in all items. If a document is marks as "novat", the VAT
//	 * of all items is set to "0.0%"
//	 */
//	private void setItemsNoVat() {
//		ArrayList<DataSetItem> itemDatasets = items.getActiveDatasets();
//		for (DataSetItem itemDataset : itemDatasets) {
//			itemDataset.setBooleanValueByKey("novat", noVat);
//		}
//	}
//
//	/**
//	 * Adds an empty item
//	 * 
//	 * @param newItem
//	 *            The new item
//	 */
//	private void addNewItem(DataSetItem newItem) {
//		newItem.setIntValueByKey("id", -(items.getDatasets().size() + 1));
//		items.getDatasets().add(newItem);
//	}
//
//	/**
//	 * Returns the document
//	 * 
//	 * @return The document
//	 */
//	public DataSetDocument getDocument() {
//		return document;
//	}
//
//	/**
//	 * Returns the document type
//	 * 
//	 * @return The document type
//	 */
//	public DocumentType getDocumentType() {
//		return documentType;
//	}
//
//	/**
//	 * If this document is duplicated, set the documents progress from 0% to 50%
//	 * 
//	 */
//	public void childDocumentGenerated() {
//		if (document.getIntValueByKey("progress") == 0) {
//			document.setIntValueByKey("progress", MarkOrderAsAction.PROCESSING);
//			Data.INSTANCE.updateDataSet(document);
//		}
//	}
//	
//	public void calculate() {
//		calculate(false);
//	}
//
//	/**
//	 * Recalculate the total sum of this editor and write the result to the
//	 * corresponding fields.
//	 * 
//	 */
//	public void calculate(boolean forceCalc) {
//
//		// Recalculate only documents that contains price values.
//		if (!documentType.hasPrice() && !forceCalc)
//			return;
//
//		// Get the sign of this document ( + or -)
//		int sign = DocumentType.getType(document.getIntValueByKey("category")).sign();
//		
//		// Get the discount value from the control element
//		Double discount = 0.0;
//		if (itemsDiscount != null)
//			discount = DataUtils.StringToDoubleDiscount(itemsDiscount.getText());
//
//		// Do the calculation
//		document.calculate(items, shipping * sign, shippingVat, shippingVatDescription, shippingAutoVat,
//				discount, noVat, noVatDescription, 1.0, netgross, deposit);
//
//		// Get the total result
//		total = document.getSummary().getTotalGross().asDouble();
//
//		// Set the items sum
//		if (itemsSum != null) {
//			if (useGross)
//				itemsSum.setText(document.getSummary().getItemsGross().asFormatedString());
//			else
//				itemsSum.setText(document.getSummary().getItemsNet().asFormatedString());
//		}
//
//		// Set the shipping
//		if (shippingValue != null) {
//			if (useGross)
//				shippingValue.setText(document.getSummary().getShippingGross().asFormatedString());
//			else
//				shippingValue.setText(document.getSummary().getShippingNet().asFormatedString());
//		}
//
//		// Set the VAT
//		if (vatValue != null)
//			vatValue.setText(document.getSummary().getTotalVat().asFormatedString());
//
//		// Set the total value
//		if (totalValue != null) {
//			totalValue.setText(document.getSummary().getTotalGross().asFormatedString());
//			totalValue.setToolTipText(_("paid") + ":" + document.getFormatedStringValueByKey("payvalue"));
//		}
//
//	}
//
//
//	/**
//	 * Get the total text, net or gross
//	 * 
//	 * @return
//	 * 		The total text
//	 */
//	private String getTotalText () {
//		if (useGross)
//			//T: Document Editor - Label Total gross 
//			return _("Total Gross");
//		else
//			//T: Document Editor - Label Total net 
//			return _("Total Net");
//
//	}
//	
//	/**
//	 * Change the document from net to gross or backwards 
//	 */
//	private void updateUseGross(boolean address_changed) {
//		
//		boolean oldUseGross = useGross;
//		
//		// Get some settings from the preference store
//		if (netgross == DocumentSummary.NOTSPECIFIED)
//			useGross = (Activator.getDefault().getPreferenceStore().getInt("DOCUMENT_USE_NET_GROSS") == 1);
//		else 
//			useGross = ( netgross == DocumentSummary.ROUND_GROSS_VALUES );
//		
//		
//		// Use the customers settings instead, if they are set
//		if ((addressId >= 0) && address_changed) {
//			
//			if (Data.INSTANCE.getContacts().getDatasetById(addressId).getIntValueByKey("use_net_gross") == 1) {
//				useGross = false;
//				netgross = DocumentSummary.ROUND_NET_VALUES;
//				comboNetGross.select(netgross);
//			}
//			if (Data.INSTANCE.getContacts().getDatasetById(addressId).getIntValueByKey("use_net_gross") == 2) {
//				useGross = true;
//				netgross = DocumentSummary.ROUND_GROSS_VALUES;
//				comboNetGross.select(netgross);
//			}
//			
//		}
//
//		// Show a warning, if the customer uses a different setting for net or gross
//		if ((useGross != oldUseGross) && documentType.hasItemsPrice()) {
//			
//			if (address_changed) {
//				MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_WARNING | SWT.OK);
//
//				//T: Title of the dialog that appears if customer uses a different setting for net or gross.
//				messageBox.setText(_("Warning"));
//				
//				
//				if (useGross) {
//					//T: Text of the dialog that appears if customer uses a different setting for net or gross.
//					messageBox.setMessage(_("Gross values are used!"));
//				}
//				else {
//					//T: Text of the dialog that appears if customer uses a different setting for net or gross.
//					messageBox.setMessage(_("Net values are used!"));
//				}
//				messageBox.open();
//			}
//
//			// Update the columns
//			if (itemTableColumns != null ) {
//				if (useGross) {
//					if (unitPriceColumn >= 0)
//						itemTableColumns.get(unitPriceColumn).setDataKey("$ItemGrossPrice");
//					if (totalPriceColumn >= 0)
//						itemTableColumns.get(totalPriceColumn).setDataKey("$ItemGrossTotal");
//				}
//				else {
//					if (unitPriceColumn >= 0)
//						itemTableColumns.get(unitPriceColumn).setDataKey("price");
//					if (totalPriceColumn >= 0)
//						itemTableColumns.get(totalPriceColumn).setDataKey("$ItemNetTotal");
//				}
//
//				// for deliveries there's no netLabel...
//				if(netLabel != null) {
//					// Update the total text
//					netLabel.setText(getTotalText());
//				}
//
//				tableViewerItems.refresh();
//			}
//			
//			// Update the shipping value;
//			calculate();
//			
//		}
//		
//	}
//	
//	/**
//	 * Returns, if this editor used net or gross values.
//	 * 
//	 * @return True, if the document uses gross values.
//	 */
//	public boolean getUseGross() {
//		return useGross;
//	}
//
//	/**
//	 * The shipping value has changed. So take the absolute value and
//	 * recalculate the document's total sum.
//	 */
//	private void changeShippingValue() {
//
//		// Get the new value and take the absolute value
//		Double newShippingValue = DataUtils.StringToDouble(shippingValue.getText());
//		if (newShippingValue < 0)
//			newShippingValue = -newShippingValue;
//
//		// If the shipping value has changed:
//		// Set the shippingAutoVat to net or gross, depending on the
//		// settings of this editor.
//		if (!DataUtils.DoublesAreEqual(newShippingValue, shipping)) {
//			shippingAutoVat = useGross ? DataSetShipping.SHIPPINGVATGROSS : DataSetShipping.SHIPPINGVATNET;
//		}
//
//		// Recalculate the sum
//		shipping = newShippingValue;
//		calculate();
//	}
//
//	/**
//	 * Create a SWT composite witch contains other SWT widgets like the payment
//	 * date or the paid value. Depending on the parameter "paid" widgets are
//	 * created to set the due values or the paid values.
//	 * 
//	 * @param paid
//	 *            If true, the widgets for "paid" are generated
//	 */
//	private void createPaidComposite(boolean paid, boolean isdeposit, boolean clickedByUser) {
//
//		// If this widget exists yet, remove it to create it new.
//		boolean changed = false;
//		if (paidDataContainer != null && !paidDataContainer.isDisposed()) {
//			paidDataContainer.dispose();
//			changed = true;
//		}
//
//		// Create the new paid container
//		paidDataContainer = new Composite(paidContainer, SWT.NONE);
//		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(6).applyTo(paidDataContainer);
//		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BOTTOM).applyTo(paidDataContainer);
//
//		// Should this container have the widgets for the state "paid" ?
//		if (paid) {
//			createDepositContainer(clickedByUser);
//		} else if (isdeposit) {
//			createDepositContainer(clickedByUser);
//			
//			// Add the attention sign if its a deposit
//			warningDepositIcon = new Label(paidDataContainer, SWT.NONE);
//				try {
//					warningDepositIcon.setImage((Activator.getImageDescriptor("/icons/32/warning_32.png").createImage()));
//				}
//				catch (IllegalArgumentException e) {
//					Logger.logError(e, "Icon not found");
//				}
//			warningDepositText = new Label(paidDataContainer, SWT.NONE);
//			warningDepositText.setText(_("ANZAHLUNG"));
//		}
//		// The container is created with the widgets that are shown
//		// if the invoice is not paid.
//		else {
//
//			// Reset the paid value to 0
//			paidValue.setValue(0.0);
//
//			// Create the due days label
//			Label dueDaysLabel = new Label(paidDataContainer, SWT.NONE);
//
//			//T: Document Editor - Label before the Text Field "Due Days".
//			//T: Format: THIS LABEL <DAYS> PAYABLE UNTIL <ISSUE DATE>
//			dueDaysLabel.setText(_("Due Days"));
//			//T: Tool Tip Text
//			dueDaysLabel.setToolTipText(_("Please pay the invoice within those days"));
//
//			GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(dueDaysLabel);
//
//			// Creates the due days spinner
//			spDueDays = new Spinner(paidDataContainer, SWT.BORDER | SWT.RIGHT);
//			spDueDays.setMinimum(0);
//			spDueDays.setMaximum(365);
//			spDueDays.setSelection(duedays /* document.getIntValueByKey("duedays") */);
//			spDueDays.setIncrement(1);
//			spDueDays.setPageIncrement(10);
//			spDueDays.setToolTipText(dueDaysLabel.getToolTipText());
//			GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).applyTo(spDueDays);
//
//			// If the spinner's value changes, add the due days to the
//			// day of today.
//			spDueDays.addSelectionListener(new SelectionAdapter() {
//				public void widgetSelected(SelectionEvent e) {
//					GregorianCalendar calendar = new GregorianCalendar(dtDate.getYear(), dtDate.getMonth(), dtDate.getDay());
//					duedays = spDueDays.getSelection();
//					calendar.add(Calendar.DAY_OF_MONTH, duedays );
//					dtIssueDate.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//					checkDirty();
//				}
//			});
//
//			// Create the issue date label
//			Label issueDateLabel = new Label(paidDataContainer, SWT.NONE);
//
//			//T: Document Editor - Label between the Text Field "Due Days" and the Date Field "Issue Date" 
//			//T: Format:  DUE DAYS: <DAYS> THIS LABEL <ISSUE DATE>
//			issueDateLabel.setText(_("Pay Until"));
//			//T: Tool Tip Text
//			issueDateLabel.setToolTipText(_("Please pay the invoice before this date"));
//
//			GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(issueDateLabel);
//
//			// Create the issue date widget
//			dtIssueDate = new DateTime(paidDataContainer, SWT.DROP_DOWN);
//			dtIssueDate.setToolTipText(issueDateLabel.getToolTipText());
//			GridDataFactory.swtDefaults().applyTo(dtIssueDate);
//			dtIssueDate.addSelectionListener(new SelectionAdapter() {
//				public void widgetSelected(SelectionEvent e) {
//					// Calculate the difference between the date of the
//					// issue date widget and the documents date,
//					// calculate is in "days" and set the due day spinner
//					GregorianCalendar calendarIssue = new GregorianCalendar(dtIssueDate.getYear(), dtIssueDate.getMonth(), dtIssueDate.getDay());
//					GregorianCalendar calendarDocument = new GregorianCalendar(dtDate.getYear(), dtDate.getMonth(), dtDate.getDay());
//					long difference = calendarIssue.getTimeInMillis() - calendarDocument.getTimeInMillis();
//					// Calculate from milliseconds to days
//					int days = (int) (difference / (1000 * 60 * 60 * 24));
//					duedays = days;
//					spDueDays.setSelection(days);
//					checkDirty();
//				}
//			});
//
//			updateIssueDate();
//		}
//
//		// Resize the container
//		paidContainer.layout(changed);
//		paidContainer.pack(changed);
//	}
//
//	/**
//	 * @param clickedByUser
//	 */
//	private void createDepositContainer(boolean clickedByUser) {
//		// Create the widget for the date, when the invoice was paid
//		Label paidDateLabel = new Label(paidDataContainer, SWT.NONE);
//		paidDateLabel.setText("am");
//		//T: Tool Tip Text
//		paidDateLabel.setToolTipText(_("Date of the payment"));
//
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(paidDateLabel);
//
//		dtPaidDate = new DateTime(paidDataContainer, SWT.DROP_DOWN);
//		dtPaidDate.setToolTipText(paidDateLabel.getToolTipText());
//		GridDataFactory.swtDefaults().applyTo(dtPaidDate);
//
//		// Set the paid date to the documents "paydate" parameter
//		GregorianCalendar calendar = new GregorianCalendar();
//		calendar = DataUtils.getCalendarFromDateString(document.getStringValueByKey("paydate"));
//		dtPaidDate.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//
//		superviceControl(dtPaidDate);
//
//		// Create the widget for the value
//		Label paidValueLabel = new Label(paidDataContainer, SWT.NONE);
//		
//		//T: Label in the document editor
//		paidValueLabel.setText(_("Value"));
//		//T: Tool Tip Text
//		paidValueLabel.setToolTipText(_("The paid value"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(paidValueLabel);
//
//		// If it's the first time, that this document is marked as paid
//		// (if the value is 0.0), then also set the date to "today"
//		if (paidValue.getValueAsDouble() == 0.0 && clickedByUser) {
//			paidValue.setValue(total);
//			calendar = new GregorianCalendar();
//			dtPaidDate.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//		}
//		CurrencyText txtPayValue = new CurrencyText(this, paidDataContainer, SWT.BORDER | SWT.RIGHT, paidValue);
//		txtPayValue.setToolTipText(paidValueLabel.getToolTipText());
//		GridDataFactory.swtDefaults().hint(60, SWT.DEFAULT).applyTo(txtPayValue.getText());
//	}
//	
//	/**
//	 * Update the Issue Date widget with the date that corresponds to the due date
//	 */
//	void updateIssueDate() {
//		// Add date and due days and set the issue date to the sum.
//		GregorianCalendar calendar = new GregorianCalendar(dtDate.getYear(), dtDate.getMonth(), dtDate.getDay());
//		calendar.add(Calendar.DAY_OF_MONTH, spDueDays.getSelection());
//		dtIssueDate.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//
//	}
//	
//	/**
//	 * Show or hide the warning icon
//	 */
//	void showHideWarningIcon() {
//		
//		// Check, whether the delivery address is the same as the billing address
//		boolean differentDeliveryAddress;
//		
//		if (documentType == DocumentType.DELIVERY) {
//			differentDeliveryAddress = !billingAddress.equalsIgnoreCase(DataUtils.removeCR(txtAddress.getText()));
//			//T: Tool Tip Text
//			differentDeliveryAddressIcon.setToolTipText(_("Different billing address !") +  OSDependent.getNewLine() + billingAddress);
//		}
//		else {
//			differentDeliveryAddress = !deliveryAddress.equalsIgnoreCase(DataUtils.removeCR(txtAddress.getText()));
//			//T: Tool Tip Text
//			differentDeliveryAddressIcon.setToolTipText(_("Different delivery address !") + OSDependent.getNewLine() + deliveryAddress);
//		}
//
//		if (differentDeliveryAddress)
//			// Show the icon
//			GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(differentDeliveryAddressIcon);
//		else
//			// Hide the icon
//			GridDataFactory.swtDefaults().hint(0,0).align(SWT.END, SWT.CENTER).applyTo(differentDeliveryAddressIcon);
//		
//	}
//	
//	/**
//	 * Fill the address label with a contact 
//	 * 
//	 * @param contact
//	 * 		The contact
//	 */
//	public void setAddress(DataSetContact contact) {
//		// Use delivery address, if it's a delivery note
//		if (documentType == DocumentType.DELIVERY)
//			txtAddress.setText(DataUtils.makeOSLineFeeds(contact.getAddress(true)));
//		else
//			txtAddress.setText(DataUtils.makeOSLineFeeds(contact.getAddress(false)));
//		
//		billingAddress = contact.getAddress(false);
//		deliveryAddress = contact.getAddress(true);
//
//		addressId = contact.getIntValueByKey("id");
//
//		// Use the customers discount
//		if (Activator.getDefault().getPreferenceStore().getBoolean("DOCUMENT_USE_DISCOUNT_ALL_ITEMS"))
//			if (itemsDiscount != null)
//				itemsDiscount.setText(DataUtils.DoubleToFormatedPercent(contact.getDoubleValueByKey("discount")));
//
//		// Check, if the payment is valid
//		int paymentid = contact.getIntValueByKey("payment");
//		
//		if (paymentid >= 0) {
//			//Use the payment method of the customer
//			if (comboPayment != null) {
//				comboPayment.setText(contact.getFormatedStringValueByKeyFromOtherTable("payment.PAYMENTS:description"));
//			}
//
//			usePayment(contact.getIntValueByKey("payment"));
//		}
//
//		
//		showHideWarningIcon();
//		addressAndIconComposite.layout(true);
//		updateUseGross(true);
//		
//	}
//	
//	/**
//	 * Use this payment and update the duedays
//	 * 
//	 * @param id
//	 * 	ID of the payment
//	 */
//	private void usePayment(int id) {
//		
//		// Return, if no payment is set
//		if (id < 0)
//			return;
//		
//		paymentId = id;
//		
//		DataSetPayment payment = Data.INSTANCE.getPayments().getDatasetById(id);
//
//		// Get the due days and description of this payment
//		duedays = payment.getIntValueByKey("netdays");
//		newPaymentID = id;
//		newPaymentDescription = payment.getStringValueByKey("description");
//
//		if (spDueDays !=null ) {
//			if (!spDueDays.isDisposed()) {
//				spDueDays.setSelection(duedays);
//				updateIssueDate();
//			}
//		}
//		checkDirty();
//
//	}
//	
//	
//	/**
//	 * Creates the SWT controls for this workbench part
//	 * 
//	 * @param the
//	 *            parent control
//	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
//	 */
//	@Override
//	public void createPartControl(Composite parent) {
//
//		// Printing an document from the document editor means:
//		// Start OpenOffice in the background and export the document as
//		// an OpenOffice document.
//		printAction = new CreateOODocumentAction();
//		getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.PRINT.getId(), printAction);
//
//		// Show an info dialog, if this is a regular customer
//		if ((documentType == DocumentType.ORDER) &&			
//			Activator.getDefault().getPreferenceStore().getBoolean("DOCUMENT_CUSTOMER_STATISTICS_DIALOG")) {
//			CustomerStatistics customerStaticstics;
//			
//			
//			if (Activator.getDefault().getPreferenceStore().getInt("DOCUMENT_CUSTOMER_STATISTICS_COMPARE_ADDRESS_FIELD")==1)
//				customerStaticstics = new CustomerStatistics(document.getIntValueByKey("addressid"), document.getStringValueByKey("address"));
//			else	
//				customerStaticstics = new CustomerStatistics(document.getIntValueByKey("addressid"));
//			
//			if (customerStaticstics.isRegularCustomer()) {
//
//				//T: Message Dialog
//				MessageDialog.openInformation(parent.getShell(), 
//						//T: Title of the customer statistics dialog
//						_("Information"),
//						document.getStringValueByKey("addressfirstline") + " " +
//						//T: Part of the customer statistics dialog
//						_("has already ordered") + " "+ customerStaticstics.getOrdersCount().toString() + " " + 
//						//T: Part of the customer statistics dialog
//						_("times in the past.") + "\n" + 
//						//T: Part of the customer statistics dialog
//						_("Last time:") + " " + customerStaticstics.getLastOrderDate()  + "\n" +
//						//T: Part of the customer statistics dialog
//						_("Invoice numbers:") + " " + customerStaticstics.getInvoices()  + "\n" +
//						//T: Part of the customer statistics dialog
//						_("Total volume:") +" " + DataUtils.DoubleToFormatedPrice(customerStaticstics.getTotal()));
//			
//			}
//		}
//		
//		// Get some settings from the preference store
//		if (netgross == DocumentSummary.NOTSPECIFIED)
//			useGross = (Activator.getDefault().getPreferenceStore().getInt("DOCUMENT_USE_NET_GROSS") == 1);
//		else 
//			useGross = ( netgross == DocumentSummary.ROUND_GROSS_VALUES );
//
//		// Create the ScrolledComposite to scroll horizontally and vertically
//	    ScrolledComposite scrollcomposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
//
//		// Create the top Composite
//		top = new Composite(scrollcomposite, SWT.NONE );  //was parent before 
//		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(top);
//
//		scrollcomposite.setContent(top);
//		scrollcomposite.setMinSize(1000, 600);   // 2nd entry should be adjusted to higher value when new fields will be added to composite 
//		scrollcomposite.setExpandHorizontal(true);
//		scrollcomposite.setExpandVertical(true);
//
//		// Create an invisible container for all hidden components
//		Composite invisible = new Composite(top, SWT.NONE);
//		invisible.setVisible(false);
//		GridDataFactory.fillDefaults().hint(0, 0).span(4, 1).applyTo(invisible);
//
//		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, ContextHelpConstants.DOCUMENT_EDITOR);
//		
//		// Document number label
//		Label labelName = new Label(top, SWT.NONE);
//
//		//T: Document Editor - Label Document Number
//		labelName.setText(_("No."));
//		//T: Tool Tip Text
//		labelName.setToolTipText(_("Reference number of this document. Next document number and the format can be set unter preferences/number range"));
//
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);
//
//		// Container for the document number and the date
//		Composite nrDateNetGrossComposite = new Composite(top, SWT.NONE);
//		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(4).applyTo(nrDateNetGrossComposite);
//		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(nrDateNetGrossComposite);
//
//		// The document number is the document name
//		txtName = new Text(nrDateNetGrossComposite, SWT.BORDER);
//		txtName.setText(document.getStringValueByKey("name"));
//		txtName.setToolTipText(labelName.getToolTipText());
//
//		superviceControl(txtName, 32);
//		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(txtName);
//		
//		// Document date
//		//T: Document Editor
//		//T: Label Document Date
//		Label labelDate = new Label(nrDateNetGrossComposite, SWT.NONE);
//		labelDate.setText(_("Date"));
//		//T: Tool Tip Text
//		labelDate.setToolTipText(_("The document's date"));
//		
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDate);
//		
//		// Document date
//		dtDate = new DateTime(nrDateNetGrossComposite, SWT.DROP_DOWN);
//		dtDate.setToolTipText(labelDate.getToolTipText());
//		dtDate.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				// If the date is modified, also modify the issue date.
//				// (Let the due days constant).
//				if (dtIssueDate != null) {
//					GregorianCalendar calendar = new GregorianCalendar(dtDate.getYear(), dtDate.getMonth(), dtDate.getDay());
//					calendar.add(Calendar.DAY_OF_MONTH, spDueDays.getSelection());
//					dtIssueDate.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//					checkDirty();
//				}
//			}
//		});
//		GridDataFactory.swtDefaults().applyTo(dtDate);
//		superviceControl(dtDate);
//		
//		// Set the dtDate widget to the documents date
//		GregorianCalendar calendar = new GregorianCalendar();
//		calendar = DataUtils.getCalendarFromDateString(document.getStringValueByKey("date"));
//		dtDate.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//
//		
//		// combo list to select between net or gross
//		comboNetGross = new Combo(documentType.hasItemsPrice() ? nrDateNetGrossComposite : invisible, SWT.BORDER);
//		comboNetGross.setToolTipText(_("Specify whether the prices should be rounded to net or gross values"));
//		// empty, if nothing is selected
//		comboNetGross.add("---"); 
//		//T: Text in combo box
//		comboNetGross.add(_("Net"));
//		//T: Text in combo box
//		comboNetGross.add(_("Gross"));
//		
//		
//		//comboViewerNetGross = new ComboViewer(comboNetGross);
//		//comboViewerNetGross.setContentProvider(new NoVatContentProvider());
//		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).grab(false, false).align(SWT.BEGINNING, SWT.CENTER).applyTo(comboNetGross);
//		comboNetGross.addSelectionListener(new SelectionListener() {
//
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				netgross = comboNetGross.getSelectionIndex();
//				// recalculate the total sum
//				calculate();
//				checkDirty();
//				updateUseGross(false);
//			}
//
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				widgetSelected(e);
//			}
//		});
//		
//		comboNetGross.select(netgross);
//	
//		
//		
//		
//		// The titleComposite contains the title and the document icon
//		Composite titleComposite = new Composite(top, SWT.NONE);
//		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(2).applyTo(titleComposite);
//		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.BOTTOM).span(2, 1).grab(true, false).applyTo(titleComposite);
//
//		// Set the title in large letters
//		Label labelDocumentType = new Label(titleComposite, SWT.NONE);
//		String documentTypeString = DocumentType.getString(document.getIntValueByKey("category"));
//		if (documentType == DocumentType.DUNNING)
//			documentTypeString = Integer.toString(dunningLevel) + "." + documentTypeString;
//		labelDocumentType.setText(documentTypeString);
//		makeLargeLabel(labelDocumentType);
//		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(labelDocumentType);
//
//		// Set the document icon
//		Label labelDocumentTypeIcon = new Label(titleComposite, SWT.NONE);
//		try {
//			labelDocumentTypeIcon
//					.setImage((Activator.getImageDescriptor("/icons/32/" + documentType.getTypeAsString().toLowerCase() + "_32.png").createImage()));
//		}
//		catch (IllegalArgumentException e) {
//			Logger.logError(e, "Icon not found");
//		}
//		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.TOP).grab(true, false).applyTo(labelDocumentTypeIcon);
//
//		// Customer reference label
//		Label labelCustomerRef = new Label(top, SWT.NONE);
//		//T: Document Editor - Label Customer Reference
//		labelCustomerRef.setText(_("Cust.Ref."));
//		//T: Tool Tip Text
//		labelCustomerRef.setToolTipText(_("Customer's reference. E.g.: Your order No.0001"));
//
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCustomerRef);
//
//		// Customer reference 
//		txtCustomerRef = new Text(top, SWT.BORDER); 
//		txtCustomerRef.setText(document.getStringValueByKey("customerref")); 
//		txtCustomerRef.setToolTipText(labelCustomerRef.getToolTipText());
//		superviceControl(txtCustomerRef, 250);
//	 	GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(txtCustomerRef);
//				
//				
//		// The extra settings composite contains additional fields like
//		// the no-Vat widget or a reference to the invoice
//		Composite xtraSettingsComposite = new Composite(top, SWT.NONE);
//		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(2).applyTo(xtraSettingsComposite);
//		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BOTTOM).span(1, 2).grab(true, false).applyTo(xtraSettingsComposite);
//		
//		// Consultant label
//		Label labelConsultant = new Label(xtraSettingsComposite, SWT.NONE);
//		//T: Document Editor - Label Consultant
//		labelConsultant.setText(_("Consultant"));
//		//T: Tool Tip Text
//		labelConsultant.setToolTipText(_("Consultant, e.g.: Heinz Mueller"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelConsultant);
//		
//		
//		// Consultant
//		txtConsultant = new Text(xtraSettingsComposite, SWT.BORDER);
//		txtConsultant.setText(document.getStringValueByKey("consultant"));
//		txtConsultant.setToolTipText(labelConsultant.getToolTipText());
//		superviceControl(txtConsultant, 250);
//		
//		
//		boolean useOrderDate = (documentType != DocumentType.ORDER);
//
//		// Service date
//		Label labelServiceDate = new Label(useOrderDate ? xtraSettingsComposite : invisible, SWT.NONE);
//		//T: Label Service Date
//		labelServiceDate.setText(_("ServiceDate"));
//		//T: Tool Tip Text
//		labelServiceDate.setToolTipText(_("The service date"));
//		
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelServiceDate);
//
//		// Service date
//		dtServiceDate = new DateTime(useOrderDate ? xtraSettingsComposite : invisible, SWT.DROP_DOWN);
//		dtServiceDate.setToolTipText(labelServiceDate.getToolTipText());
//		GridDataFactory.swtDefaults().applyTo(dtServiceDate);
//		superviceControl(dtServiceDate);
//
//		// Set the dtDate widget to the documents date
//		calendar = new GregorianCalendar();
//		calendar = DataUtils.getCalendarFromDateString(document.getStringValueByKey("servicedate"));
//		dtServiceDate.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//
//
//		// Order date
//		Label labelOrderDate = new Label(useOrderDate ? xtraSettingsComposite : invisible, SWT.NONE);
//		if (documentType == DocumentType.OFFER) {
//			//T: Label in the document editor
//			labelOrderDate.setText(_("Date of request"));
//			//T: Tool Tip Text
//			labelOrderDate.setToolTipText(_("Date when the offer was requested"));
//		} else {
//			//T: Label in the document editor
//			labelOrderDate.setText(_("Order Date"));
//			//T: Tool Tip Text
//			labelOrderDate.setToolTipText(_("Date when the order was placed"));
//		}
//
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelOrderDate);
//		
//		// Order date
//		dtOrderDate = new DateTime(useOrderDate ? xtraSettingsComposite : invisible, SWT.DROP_DOWN);
//		dtOrderDate.setToolTipText(labelOrderDate.getToolTipText());
//		GridDataFactory.swtDefaults().applyTo(dtOrderDate);
//		superviceControl(dtOrderDate);
//
//		// Set the dtDate widget to the documents date
//		calendar = new GregorianCalendar();
//
//		// If "orderdate" is not set, use "webshopdate"
//		String orderDateString = document.getStringValueByKey("orderdate");
//		if (orderDateString.isEmpty()) {
//			orderDateString = document.getStringValueByKey("webshopdate");
//		}
//
//		calendar = DataUtils.getCalendarFromDateString(orderDateString);
//		dtOrderDate.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//
//		// A reference to the invoice
//		Label labelInvoiceRef = new Label(documentType.hasInvoiceReference() ? xtraSettingsComposite : invisible, SWT.NONE);
//		//T: Label in the document editor
//		labelInvoiceRef.setText(_("Invoice"));
//		//T: Tool Tip Text
//		labelInvoiceRef.setToolTipText(_("Number of the Invoice that belongs to this document."));
//
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.BOTTOM).applyTo(labelInvoiceRef);
//		txtInvoiceRef = new Text(documentType.hasInvoiceReference() ? xtraSettingsComposite : invisible, SWT.BORDER);
//		txtInvoiceRef.setToolTipText(labelInvoiceRef.getToolTipText());
//		int invoiceId = document.getIntValueByKey("invoiceid");
//		if (invoiceId >= 0)
//			txtInvoiceRef.setText(Data.INSTANCE.getDocuments().getDatasetById(invoiceId).getStringValueByKey("name"));
//		else
//			txtInvoiceRef.setText("---");
//		txtInvoiceRef.setEditable(false);
//		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(txtInvoiceRef);
//
//		// This document should use a VAT of 0%
//		Label labelNoVat = new Label(documentType.hasPrice() ? xtraSettingsComposite : invisible, SWT.NONE);
//		//T: Label in the document editor
//		labelNoVat.setText(_("VAT") );
//		//T: Tool Tip Text
//		labelNoVat.setToolTipText(_("If this document is set to a tax rate with 0%, all the items of the document are calculated with 0% tax."));
//
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelNoVat);
//
//		// combo list with all 0% VATs
//		comboNoVat = new Combo(documentType.hasPrice() ? xtraSettingsComposite : invisible, SWT.BORDER);
//		comboNoVat.setToolTipText(labelNoVat.getToolTipText());
//		comboViewerNoVat = new ComboViewer(comboNoVat);
//		comboViewerNoVat.setContentProvider(new NoVatContentProvider());
//		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(comboNoVat);
//		comboViewerNoVat.addSelectionChangedListener(new ISelectionChangedListener() {
//
//			// A combo entry is selected
//			public void selectionChanged(SelectionChangedEvent event) {
//				ISelection selection = event.getSelection();
//				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
//				if (!structuredSelection.isEmpty()) {
//
//					// get first element ...
//					Object firstElement = structuredSelection.getFirstElement();
//					DataSetVAT dataSetVat = (DataSetVAT) firstElement;
//					int id = dataSetVat.getIntValueByKey("id");
//
//					// get the "no-VAT" values
//					if (id >= 0) {
//						noVat = true;
//						noVatName = dataSetVat.getStringValueByKey("name");
//						noVatDescription = dataSetVat.getStringValueByKey("description");
//					}
//					else {
//						noVat = false;
//						noVatName = "";
//						noVatDescription = "";
//					}
//
//					// set all items to 0%
//					setItemsNoVat();
//					tableViewerItems.refresh();
//
//					// recalculate the total sum
//					calculate();
//					checkDirty();
//				}
//			}
//		});
//
//		// Selects the no VAT entry
//		comboViewerNoVat.setInput(Data.INSTANCE.getVATs().getActiveDatasetsPrefereCategory(DataSetVAT.getSalesTaxString()));
//		if (noVat)
//			comboNoVat.setText(noVatName);
//		else
//			comboNoVat.select(0);
//
//
//		// Group with tool bar with buttons to generate
//		// a new document from this document
//		Group copyGroup = new Group(top, SWT.NONE);
//
//		//T: Document Editor
//		//T: Label Group box to create a new document based on this one.
//		copyGroup.setText(_("Create a duplicate"));
//		GridLayoutFactory.fillDefaults().margins(0, 0).applyTo(copyGroup);
//		GridDataFactory.fillDefaults().minSize(200, SWT.DEFAULT).align(SWT.END, SWT.BOTTOM).grab(true, false).span(1, 2).applyTo(copyGroup);
//
//		// Toolbar
//		ToolBar toolBarDuplicateDocument = new ToolBar(copyGroup, SWT.FLAT | SWT.WRAP);
//		GridDataFactory.fillDefaults().align(SWT.END, SWT.TOP).applyTo(toolBarDuplicateDocument);
//		ToolBarManager tbmDuplicate = new ToolBarManager(toolBarDuplicateDocument);
//
//		// Add buttons, depending on the document type
//		switch (documentType) {
//		case OFFER:
//			tbmDuplicate.add(new NewDocumentActionContributionItem(new NewDocumentAction(DocumentType.ORDER, this, 32)));
//			tbmDuplicate.add(new NewDocumentActionContributionItem(new NewDocumentAction(DocumentType.INVOICE, this, 32)));
//			tbmDuplicate.add(new NewDocumentActionContributionItem(new NewDocumentAction(DocumentType.PROFORMA, this, 32)));
//			break;
//		case ORDER:
//			tbmDuplicate.add(new NewDocumentActionContributionItem(new NewDocumentAction(DocumentType.CONFIRMATION, this, 32)));
//			tbmDuplicate.add(new NewDocumentActionContributionItem(new NewDocumentAction(DocumentType.INVOICE, this, 32)));
//			tbmDuplicate.add(new NewDocumentActionContributionItem(new NewDocumentAction(DocumentType.DELIVERY, this, 32)));
//			tbmDuplicate.add(new NewDocumentActionContributionItem(new NewDocumentAction(DocumentType.PROFORMA, this, 32)));
//			break;
//		case CONFIRMATION:
//			tbmDuplicate.add(new NewDocumentActionContributionItem(new NewDocumentAction(DocumentType.INVOICE, this, 32)));
//			tbmDuplicate.add(new NewDocumentActionContributionItem(new NewDocumentAction(DocumentType.DELIVERY, this, 32)));
//			tbmDuplicate.add(new NewDocumentActionContributionItem(new NewDocumentAction(DocumentType.PROFORMA, this, 32)));
//			break;
//		case INVOICE:
//			tbmDuplicate.add(new NewDocumentActionContributionItem(new NewDocumentAction(DocumentType.DELIVERY, this, 32)));
//			tbmDuplicate.add(new NewDocumentActionContributionItem(new NewDocumentAction(DocumentType.CREDIT, this, 32)));
//			tbmDuplicate.add(new NewDocumentActionContributionItem(new NewDocumentAction(DocumentType.DUNNING, this, 32)));
//			break;
//		case DELIVERY:
//			tbmDuplicate.add(new NewDocumentActionContributionItem(new NewDocumentAction(DocumentType.INVOICE, this, 32)));
//			break;
//		case DUNNING:
//			NewDocumentAction action = new NewDocumentAction(DocumentType.DUNNING, this, 32);
//			action.setText(Integer.toString(dunningLevel + 1) + "." + action.getText());
//			tbmDuplicate.add(new NewDocumentActionContributionItem(action));
//			break;
//		case PROFORMA:
//			tbmDuplicate.add(new NewDocumentActionContributionItem(new NewDocumentAction(DocumentType.INVOICE, this, 32)));
//			break;
//		default:
//			copyGroup.setVisible(false);
//		}
//
//		// Resize the toolbar
//		tbmDuplicate.update(true);
//
//		// Composite that contains the address label and the address icon
//		Composite addressComposite = new Composite(top, SWT.NONE | SWT.RIGHT);
//		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(addressComposite);
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addressComposite);
//
//		// Address label
//		Label labelAddress = new Label(addressComposite, SWT.NONE | SWT.RIGHT);
//		//T: Label in the document editor
//		labelAddress.setText(_("Address"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(labelAddress);
//
//		// Address icon
//		Label selectAddressButton = new Label(addressComposite, SWT.NONE | SWT.RIGHT);
//		//T: Tool Tip Text
//		selectAddressButton.setToolTipText(_("Pick an address from the list of all contacts"));
//
//		try {
//			selectAddressButton.setImage((Activator.getImageDescriptor("/icons/20/contact_list_20.png").createImage()));
//		}
//		catch (Exception e) {
//			Logger.logError(e, "Icon not found");
//		}
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(selectAddressButton);
//		selectAddressButton.addMouseListener(new MouseAdapter() {
//
//			// Open the address dialog, if the icon is clicked.
//			public void mouseDown(MouseEvent e) {
//
//				//T: Document Editor
//				//T: Title of the dialog to select the address
//				SelectContactDialog dialog = new SelectContactDialog(_("Select the address"));
//				DataSetContact contact;
//				if (dialog.open() == Dialog.OK) {
//					contact = (DataSetContact) dialog.getSelectedDataSet();
//					if (contact != null) {
//
//						setAddress(contact);
//						
//					}
//				}
//			}
//		});
//
//		// Address icon
//		Label newAddressButton = new Label(addressComposite, SWT.NONE | SWT.RIGHT);
//		//T: Tool Tip Text
//		newAddressButton.setToolTipText(_("Open the contact editor to enter a new address"));
//
//		try {
//			newAddressButton.setImage((Activator.getImageDescriptor("/icons/20/contact_plus_20.png").createImage()));
//		}
//		catch (Exception e) {
//			Logger.logError(e, "Icon not found");
//		}
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(newAddressButton);
//		newAddressButton.addMouseListener(new MouseAdapter() {
//
//			// Open the address dialog, if the icon is clicked.
//			public void mouseDown(MouseEvent e) {
//
//				
//				// Sets the editors input
//				UniDataSetEditorInput input = new UniDataSetEditorInput(thisDocumentEditor);
//
//				// Open a new Contact Editor 
//				try {
//					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, ContactEditor.ID);
//				}
//				catch (PartInitException e1) {
//					Logger.logError(e1, "Error opening Editor: " + ContactEditor.ID);
//				}
//
//				
//
//			}
//		});
//
//		// Composite that contains the address and the warning icon
//		addressAndIconComposite = new Composite(top, SWT.NONE | SWT.RIGHT);
//		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(addressAndIconComposite);
//		GridDataFactory.fillDefaults().minSize(180, 80).align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(addressAndIconComposite);
//
//		// The address field
//		txtAddress = new Text(addressAndIconComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
//		if (documentType == DocumentType.DELIVERY)
//			txtAddress.setText(DataUtils.makeOSLineFeeds(document.getStringValueByKey("deliveryaddress")));
//		else
//			txtAddress.setText(DataUtils.makeOSLineFeeds(document.getStringValueByKey("address")));
//		superviceControl(txtAddress, 250);
//		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(txtAddress);
//
//		// Add the attention sign if the delivery address is not equal to the billing address
//		differentDeliveryAddressIcon = new Label(addressAndIconComposite, SWT.NONE);
//
//		try {
//			differentDeliveryAddressIcon.setImage((Activator.getImageDescriptor("/icons/32/warning_32.png").createImage()));
//		}
//		catch (Exception e) {
//			Logger.logError(e, "Icon not found");
//		}
//		
//		showHideWarningIcon();
//
//		
//		
//		// Add the item table, if the document is one with items.
//		if (documentType.hasItems()) {	
//
//			// Container for the label and the add and delete button.
//			Composite addButtonComposite = new Composite(top, SWT.NONE | SWT.RIGHT);
//			GridLayoutFactory.fillDefaults().numColumns(1).applyTo(addButtonComposite);
//			GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addButtonComposite);
//
//			// Items label
//			Label labelItems = new Label(addButtonComposite, SWT.NONE | SWT.RIGHT);
//			//T: Document Editor
//			//T: Label items
//			labelItems.setText(_("Items"));
//			GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(labelItems);
//
//			// Item add button
//			Label addFromListButton = new Label(addButtonComposite, SWT.NONE);
//			//T: Tool Tip Text
//			addFromListButton.setToolTipText(_("Pick an item from the list of all products"));
//
//			try {
//				addFromListButton.setImage((Activator.getImageDescriptor("/icons/20/product_list_20.png").createImage()));
//			}
//			catch (Exception e) {
//				Logger.logError(e, "Icon not found");
//			}
//			GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addFromListButton);
//			addFromListButton.addMouseListener(new MouseAdapter() {
//
//				// Open the product dialog and add the
//				// selected product as new item.
//				public void mouseDown(MouseEvent e) {
//
//					DataSetItem newItem = null;
//					
//					//T: Document Editor
//					//T: Title of the dialog to select a product
//					SelectProductDialog dialog = new SelectProductDialog(_("Select a product"));
//					if (dialog.open() == Dialog.OK) {
//						
//						// Get the array list of all selected elements
//						for (UniDataSet uds : dialog.getSelectedDataSets()) {
//							
//							// Get one product
//							DataSetProduct product = (DataSetProduct)uds;
//							
//							if (product != null) {
//								newItem = new DataSetItem(documentType.sign() * 1.0, product);
//								
//								// Use the products description, or clear it
//								if (!Activator.getDefault().getPreferenceStore().getBoolean("DOCUMENT_COPY_PRODUCT_DESCRIPTION_FROM_PRODUCTS_DIALOG"))
//									newItem.setStringValueByKey("description", "");
//								
//								addNewItem(newItem);
//							}
//
//						}
//
//						tableViewerItems.refresh();
//						if (newItem!= null)
//							tableViewerItems.reveal(newItem);
//						calculate();
//						checkDirty();
//
//						// Renumber all Items
//						RenumberItems();
//					}
//				}
//			});
//
//			// Add the button to add all items from a delivery note
//			if (documentType.hasAddFromDeliveryNote()) {
//				// Item add button
//				Label addFromDeliveryNoteButton = new Label(addButtonComposite, SWT.NONE);
//				//T: Tool Tip Text
//				addFromDeliveryNoteButton.setToolTipText(_("Insert all items from a delivery note"));
//
//				try {
//					addFromDeliveryNoteButton.setImage((Activator.getImageDescriptor("/icons/20/delivery_note_list_20.png").createImage()));
//				}
//				catch (Exception e) {
//					Logger.logError(e, "Icon not found");
//				}
//				GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addFromDeliveryNoteButton);
//				addFromDeliveryNoteButton.addMouseListener(new MouseAdapter() {
//
//					// Open the product dialog and add the
//					// selected product as new item.
//					public void mouseDown(MouseEvent e) {
//
//						DataSetItem newItem = null;
//						
//						//T: Document Editor
//						//T: Title of the dialog to select a delivery note
//						SelectDeliveryNoteDialog dialog = new SelectDeliveryNoteDialog(_("Select a delivey note"), addressId);
//						if (dialog.open() == Dialog.OK) {
//							
//							// Get the array list of all selected elements
//							for (UniDataSet uds : dialog.getSelectedDataSets()) {
//								
//								// Get one product
//								DataSetDocument deliveryNote = (DataSetDocument)uds;
//								
//								if (deliveryNote != null) {
//									// Get all items by ID from the item string
//									String itemsString = deliveryNote.getStringValueByKey("items");
//									String[] itemsStringParts = itemsString.split(",");
//									
//									// Parse the item string ..
//									for (String itemsStringPart : itemsStringParts) {
//										int id;
//										if (itemsStringPart.length() > 0) {
//											try {
//												id = Integer.parseInt(itemsStringPart);
//											}
//											catch (NumberFormatException e1) {
//												Logger.logError(e1, "Error parsing item string");
//												id = 0;
//											}
//											
//											// And copy the item to a new one
//											DataSetItem item = Data.INSTANCE.getItems().getDatasetById(id);
//											
//											// the new item
//											newItem = new DataSetItem(item);
//
//											// Add the new item
//											items.getDatasets().add(newItem);
//											
//										}
//									}
//									
//									// Put the number of the delivery note in a new line of the message field
//									if (Activator.getDefault().getPreferenceStore().getBoolean("DOCUMENT_ADD_NR_OF_IMPORTED_DELIVERY_NOTE")) {
//										String dNName = deliveryNote.getStringValueByKey("name");
//										
//										if (!txtMessage.getText().isEmpty())
//											dNName = OSDependent.getNewLine() + dNName;
//										txtMessage.setText(txtMessage.getText() + dNName);
//									}
//									
//									// Set the delivery notes reference to this invoice
//									int documentID = document.getIntValueByKey("id");
//									// If the document has no id, collect the imported 
//									// delivery notes in a list.
//									if (documentID >= 0) {
//										
//										// Set the reference of the imported delivery note to
//										// this invoice
//										deliveryNote.setIntValueByKey("invoiceid", documentID );
//										Data.INSTANCE.updateDataSet(deliveryNote);
//										
//										// Change also the transaction id of the imported delivery note
//										Transaction.mergeTwoTransactions(document, deliveryNote);
//									}
//									else
//										importedDeliveryNotes.add(deliveryNote.getIntValueByKey("id"));
//
//								}
//
//							}
//
//							tableViewerItems.refresh();
//							if (newItem!= null)
//								tableViewerItems.reveal(newItem);
//							calculate();
//							checkDirty();
//
//							// Renumber all Items
//							RenumberItems();
//						}
//					}
//				});
//			}
//			
//			// Item add button
//			Label addButton = new Label(addButtonComposite, SWT.NONE);
//			//T: Tool Tip Text
//			addButton.setToolTipText(_("Add a new item with default name and quantity '1'"));
//			try {
//				addButton.setImage((Activator.getImageDescriptor("/icons/16/plus_16.png").createImage()));
//			}
//			catch (Exception e) {
//				Logger.logError(e, "Icon not found");
//			}
//			GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addButton);
//			addButton.addMouseListener(new MouseAdapter() {
//
//				// Add a new item with default properties
//				public void mouseDown(MouseEvent e) {
//
//					// Cancel the item editing
//					if (itemEditingSupport != null)
//						itemEditingSupport.cancelAndSave();
//					
//					//T: Text of a new item
//					DataSetItem newItem = new DataSetItem(_("Name"), 
//							//T: Text of a new item
//							_("Item No."), "", documentType.sign() * 1.0, "", 0.0, 0, "", "");
//
//					// Use the standard VAT value
//					newItem.setVat(Integer.parseInt(Data.INSTANCE.getProperty("standardvat")));
//					addNewItem(newItem);
//
//					tableViewerItems.refresh();
//					tableViewerItems.reveal(newItem);
//					calculate();
//					checkDirty();
//					
//					// Renumber all Items
//					RenumberItems();
//
//				}
//			});
//
//			// Item delete button
//			Label deleteButton = new Label(addButtonComposite, SWT.NONE);
//			//T: Tool Tip Text
//			deleteButton.setToolTipText(_("Delete the selected item from the list of items"));
//
//			try {
//				deleteButton.setImage((Activator.getImageDescriptor("/icons/16/delete_16.png").createImage()));
//			}
//			catch (IllegalArgumentException e) {
//				Logger.logError(e, "Icon not found");
//			}
//			GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(deleteButton);
//			deleteButton.addMouseListener(new MouseAdapter() {
//
//				// Delete the selected item
//				public void mouseDown(MouseEvent e) {
//					ISelection selection = tableViewerItems.getSelection();
//					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
//					if (!structuredSelection.isEmpty()) {
//						IStructuredSelection iselection = ((IStructuredSelection) selection);
//						for (Iterator iterator = iselection.iterator(); iterator.hasNext();) {
//							Object obj = (Object) iterator.next();
//							// If we had a selection, delete it
//							if (obj != null) {
//								UniDataSet uds = (UniDataSet) obj;
//								deleteItem(uds);
//							}
//						}
//
//						// Renumber all Items
//						RenumberItems();
//
//					}
//				}
//			});
//
//			// Composite that contains the table
//			Composite tableComposite = new Composite(top, SWT.NONE);
//			GridDataFactory.fillDefaults().grab(true, true).span(3, 1).applyTo(tableComposite);
//			tableColumnLayout = new TableColumnLayout();
//			tableComposite.setLayout(tableColumnLayout);
//
//			// The table viewer
//			tableViewerItems = new TableViewer(tableComposite, SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
//			tableViewerItems.getTable().setLinesVisible(true);
//			tableViewerItems.getTable().setHeaderVisible(true);
//			tableViewerItems.setContentProvider(new ViewDataSetTableContentProvider(tableViewerItems));
//			
//			// Get the column width from the preferences
//			int cw_pos = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_ITEMS_POS");
//			int cw_opt = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_ITEMS_OPT");
//			int cw_qty = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_ITEMS_QTY");
//			int cw_qunit = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_ITEMS_QUNIT");
//			int cw_itemno = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_ITEMS_ITEMNO");
//			int cw_picture = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_ITEMS_PICTURE");
//			int cw_name = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_ITEMS_NAME");
//			int cw_description = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_ITEMS_DESCRIPTION");
//			int cw_vat = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_ITEMS_VAT");
//			int cw_uprice = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_ITEMS_UPRICE");
//			int cw_discount = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_ITEMS_DISCOUNT");
//			int cw_price = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_ITEMS_PRICE");
//
//			
//			// Workaround for of an Error in Windows JFace
//			// Add an empty column
//			// Mantis #0072
//			if (OSDependent.isWin())
//				itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.CENTER,"", 0, true, "$", null));
//
//			if (Activator.getDefault().getPreferenceStore().getBoolean("DOCUMENT_USE_ITEM_POS"))
//			//T: Used as heading of a table. Keep the word short.
//				itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.CENTER,_("Pos"), cw_pos, true, "$Row", null));
//			
//			// Create the table columns 
//			if (containsOptionalItems || Activator.getDefault().getPreferenceStore().getBoolean("OPTIONALITEMS_USE") && (documentType == DocumentType.OFFER))
//				//T: Used as heading of a table. Keep the word short.
//				itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.CENTER, _("Opt."), cw_opt, true, "$Optional", new DocumentItemEditingSupport(this,
//						tableViewerItems, DocumentItemEditingSupport.Column.OPTIONAL)));
//			//T: Used as heading of a table. Keep the word short.
//			itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.CENTER, _("Qty."), cw_qty, true, "quantity", new DocumentItemEditingSupport(this,
//					tableViewerItems, DocumentItemEditingSupport.Column.QUANTITY)));
//			
//			if (Activator.getDefault().getPreferenceStore().getBoolean("PRODUCT_USE_QUNIT"))
//			//T: Used as heading of a table. Keep the word short.
//			itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.CENTER, _("Q. Unit"), cw_qunit, true, "qunit", new DocumentItemEditingSupport(this,
//					tableViewerItems, DocumentItemEditingSupport.Column.QUNIT)));
//
//			if (Activator.getDefault().getPreferenceStore().getBoolean("PRODUCT_USE_ITEMNR"))
//				//T: Used as heading of a table. Keep the word short.
//				itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.LEFT, _("Item No."), cw_itemno, true, "itemnr", new DocumentItemEditingSupport(this,
//						tableViewerItems, DocumentItemEditingSupport.Column.ITEMNR)));
//			
//			if (Activator.getDefault().getPreferenceStore().getBoolean("DOCUMENT_USE_PREVIEW_PICTURE"))
//				//T: Used as heading of a table. Keep the word short.
//				itemTableColumns.add( new UniDataSetTableColumn(parent.getDisplay() , tableColumnLayout, tableViewerItems, SWT.LEFT, _("Picture"), cw_picture, true, "$ProductPictureSmall", new DocumentItemEditingSupport(this,
//					tableViewerItems, DocumentItemEditingSupport.Column.PICTURE)));
//
//			//T: Used as heading of a table. Keep the word short.
//			itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.LEFT, _("Name"), cw_name, false, "name", new DocumentItemEditingSupport(this,
//					tableViewerItems, DocumentItemEditingSupport.Column.NAME)));
//			//T: Used as heading of a table. Keep the word short.
//			itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.LEFT, _("Description"), cw_description, false, "description", new DocumentItemEditingSupport(
//					this, tableViewerItems, DocumentItemEditingSupport.Column.DESCRIPTION)));
//			if (documentType.hasItemsPrice()) {
//				//T: Used as heading of a table. Keep the word short.
//				itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.RIGHT, _("VAT"), cw_vat, true, "$ItemVatPercent", new DocumentItemEditingSupport(this,
//						tableViewerItems, DocumentItemEditingSupport.Column.VAT)));
//
//				if (useGross) {
//					//T: Unit Price.
//					//T: Used as heading of a table. Keep the word short.
//					itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.RIGHT, _("U.Price"), cw_uprice, true, "$ItemGrossPrice",
//							new DocumentItemEditingSupport(this, tableViewerItems, DocumentItemEditingSupport.Column.PRICE)));
//				}
//				else {
//					//T: Used as heading of a table. Keep the word short.
//					itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.RIGHT, _("U.Price"), cw_uprice, true, "price", new DocumentItemEditingSupport(this,
//							tableViewerItems, DocumentItemEditingSupport.Column.PRICE)));
//				}
//				unitPriceColumn = itemTableColumns.size()-1;
//				
//				if (containsDiscountedItems || Activator.getDefault().getPreferenceStore().getBoolean("DOCUMENT_USE_DISCOUNT_EACH_ITEM"))
//					//T: Used as heading of a table. Keep the word short.
//					itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.RIGHT, _("Discount"), cw_discount, true, "discount", new DocumentItemEditingSupport(this,
//							tableViewerItems, DocumentItemEditingSupport.Column.DISCOUNT)));
//
//				
//				if (useGross){
//					//T: Used as heading of a table. Keep the word short.
//					itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.RIGHT, _("Price"), cw_price, true, "$ItemGrossTotal", new DocumentItemEditingSupport(
//							this, tableViewerItems, DocumentItemEditingSupport.Column.TOTAL)));
//				}
//				else {
//					//T: Used as heading of a table. Keep the word short.
//					itemTableColumns.add( new UniDataSetTableColumn(tableColumnLayout, tableViewerItems, SWT.RIGHT, _("Price"), cw_price, true, "$ItemNetTotal", new DocumentItemEditingSupport(
//							this, tableViewerItems, DocumentItemEditingSupport.Column.TOTAL)));
//				}
//
//				totalPriceColumn = itemTableColumns.size()-1;
//
//			}
//			// Fill the table with the items
//			tableViewerItems.setInput(items);
//			
//			//Create the context menu
//			createContextMenu(tableViewerItems);
//		}
//
//		// Container for the message label and the add button
//		Composite addMessageButtonComposite = new Composite(top, SWT.NONE | SWT.RIGHT);
//		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(addMessageButtonComposite);
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addMessageButtonComposite);
//
//		// The message label
//		Label messageLabel = new Label(addMessageButtonComposite, SWT.NONE);
//		if (documentType.hasItems()) {
//			//T: Document Editor Label for the text field under the item table.
//			messageLabel.setText(_("Remarks"));
//			//T: Tool Tip Text
//			messageLabel.setToolTipText(_("Enter an additional text that will be placed under the list of items."));
//
//		}	
//		else {
//			//T: Document Editor Label for the text field, if there is no item table
//			messageLabel.setText(_("Text"));
//			//T: Tool Tip Text
//			messageLabel.setToolTipText(_("Enter the text that will be placed in the document."));
//		}
//
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(messageLabel);
//
//		// The add message button
//		Label addMessageButton = new Label(addMessageButtonComposite, SWT.NONE);
//		//T: Tool Tip Text
//		addMessageButton.setToolTipText(_("Select one of the text templates in of the list of texts"));
//
//		try {
//			addMessageButton.setImage((Activator.getImageDescriptor("/icons/20/list_20.png").createImage()));
//		}
//		catch (IllegalArgumentException e) {
//			Logger.logError(e, "Icon not found");
//		}
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addMessageButton);
//		addMessageButton.addMouseListener(new MouseAdapter() {
//
//			// Open the text dialog and select a text
//			public void mouseDown(MouseEvent e) {
//				
//				//T: Document Editor
//				//T: Title of the dialog to select a text
//				SelectTextDialog dialog = new SelectTextDialog(_("Select a text"));
//				DataSetText text;
//				if (dialog.open() == Dialog.OK) {
//					text = (DataSetText) dialog.getSelectedDataSet();
//					
//					// Get the message field with the focus
//					Text selecteMessageField = txtMessage;
//
//					// Get the message field with the focus
//					if (txtMessage2 != null)
//						if (txtMessage2.isFocusControl())
//							selecteMessageField = txtMessage2;
//					if (txtMessage3 != null)
//						if (txtMessage3.isFocusControl())
//							selecteMessageField = txtMessage3;
//					
//					// Insert the selected text in the message text
//					if ((text != null) && (selecteMessageField != null)) {
//						int begin = selecteMessageField.getSelection().x;
//						int end = selecteMessageField.getSelection().y;
//						String s = selecteMessageField.getText();
//						String s1 = s.substring(0, begin);
//						String s2 = text.getStringValueByKey("text");
//						String s3 = s.substring(end, s.length());
//
//						selecteMessageField.setText(s1 + s2 + s3);
//
//						selecteMessageField.setSelection(s1.length() + s2.length());
//						checkDirty();
//					}
//				}
//			}
//		});
//
//		int noOfMessageFields = Activator.getDefault().getPreferenceStore().getInt("DOCUMENT_MESSAGES");
//		
//		if (noOfMessageFields < 1)
//			noOfMessageFields = 1;
//		if (noOfMessageFields > 3)
//			noOfMessageFields = 3;
//		
//		// Container for 1..3 message fields
//		Composite messageFieldsComposite = new Composite(top,SWT.NONE );
//		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(messageFieldsComposite);
//		
//		// Add a multi line text field for the message.
//		txtMessage = new Text(messageFieldsComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
//		txtMessage.setText(DataUtils.makeOSLineFeeds(document.getStringValueByKey("message")));
//		txtMessage.setToolTipText(messageLabel.getToolTipText());
//		
//		GridDataFactory.defaultsFor(txtMessage).minSize(80, 50).applyTo(txtMessage);
////		GridDataFactory.fillDefaults().grab(true, true).applyTo(txtMessage);
//		superviceControl(txtMessage, 10000);
//
//		if (noOfMessageFields >= 2) {
//			// Add a multi line text field for the message.
//			txtMessage2 = new Text(messageFieldsComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
//			txtMessage2.setText(DataUtils.makeOSLineFeeds(document.getStringValueByKey("message2")));
//			
//			GridDataFactory.defaultsFor(txtMessage2).minSize(80, 50).applyTo(txtMessage2);
//			txtMessage2.setToolTipText(messageLabel.getToolTipText());
////			GridDataFactory.fillDefaults().grab(true, true).applyTo(txtMessage2);
//			superviceControl(txtMessage2, 10000);
//		}
//		if (noOfMessageFields >= 3) {
//			// Add a multi line text field for the message.
//			txtMessage3 = new Text(messageFieldsComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
//			txtMessage3.setText(DataUtils.makeOSLineFeeds(document.getStringValueByKey("message3")));
//			txtMessage3.setToolTipText(messageLabel.getToolTipText());
//			
//			GridDataFactory.defaultsFor(txtMessage3).minSize(80, 50).applyTo(txtMessage3);
////			GridDataFactory.fillDefaults().grab(true, true).applyTo(txtMessage3);
//			superviceControl(txtMessage3, 10000);
//		}
//		
//		// Set the tab order
//		if (documentType.hasInvoiceReference())
//			setTabOrder(txtAddress, txtInvoiceRef);
//		else if (documentType.hasPrice())
//			setTabOrder(txtAddress, comboNoVat);
//		else if (documentType.hasItems())
//			setTabOrder(txtAddress, tableViewerItems.getTable());
//		else
//			setTabOrder(txtAddress, txtMessage);
//
//		// Depending on if the document has price values.
//		if (!documentType.hasPrice()) {
//
//			// If not, fill the columns for the price with the message field.
//			if (documentType.hasItems())
//				GridDataFactory.fillDefaults().hint(SWT.DEFAULT, noOfMessageFields*65).span(3, 1).grab(true, false).applyTo(messageFieldsComposite);
//			else
//				GridDataFactory.fillDefaults().span(3, 1).grab(true, true).applyTo(messageFieldsComposite);
//
//
////			Composite totalComposite = new Composite(top, SWT.NONE);
////			GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(totalComposite);
////			GridDataFactory.fillDefaults().align(SWT.END, SWT.TOP).grab(true, false).span(4, 5).applyTo(totalComposite);
////			// Total label
////			Label totalLabel = new Label(totalComposite, SWT.NONE);
////			//T: Document Editor - Total sum of this document 
////			totalLabel.setText(_("Total"));
////			GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(totalLabel);
////
////			// Total value
////			totalValue = new Label(totalComposite, SWT.NONE | SWT.RIGHT);
////			totalValue.setText("---");
//
//			// Get the documents'shipping values.
//			shipping = document.getDoubleValueByKey("shipping");
//			shippingVat = document.getDoubleValueByKey("shippingvat");
//			shippingAutoVat = document.getIntValueByKey("shippingautovat");
//			shippingVatDescription = document.getStringValueByKey("shippingvatdescription");
//
////			GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.TOP).applyTo(totalValue);
////			calculate(Data.INSTANCE.getDocuments().getDatasetById(invoiceId));
//			calculate(true);
//		
//		} else {
//
//			if (documentType.hasPaid())
//				GridDataFactory.fillDefaults().span(2, 1).hint(100, noOfMessageFields*65).grab(true, false).applyTo(messageFieldsComposite);
//			else
//				GridDataFactory.fillDefaults().span(2, 1).hint(100, noOfMessageFields*65).grab(true, true).applyTo(messageFieldsComposite);
//
//			// Create a column for the documents subtotal, shipping and total
//			Composite totalComposite = new Composite(top, SWT.NONE);
//			GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(totalComposite);
//			GridDataFactory.fillDefaults().align(SWT.END, SWT.TOP).grab(true, false).span(1, 2).applyTo(totalComposite);
//
//			// Label sub total
//			netLabel = new Label(totalComposite, SWT.NONE);
//			// Set the total text
//			netLabel.setText(getTotalText());
//			GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(netLabel);
//
//			// Sub total
//			itemsSum = new Label(totalComposite, SWT.NONE | SWT.RIGHT);
//			itemsSum.setText("---");
//			GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.TOP).applyTo(itemsSum);
//
//
//			if (Activator.getDefault().getPreferenceStore().getBoolean("DOCUMENT_USE_DISCOUNT_ALL_ITEMS") ||
//					!DataUtils.DoublesAreEqual(document.getDoubleValueByKey("itemsdiscount"), 0.0)) {
//				
//				// Label discount
//				Label discountLabel = new Label(totalComposite, SWT.NONE);
//				//T: Document Editor - Label discount 
//				discountLabel.setText(_("Discount"));
//				//T: Tool Tip Text, xgettext:no-c-format
//				discountLabel.setToolTipText(_("Enter a discount value in % for all items."));
//				GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(discountLabel);
//				
//				// Discount field
//				itemsDiscount = new Text(totalComposite, SWT.NONE | SWT.RIGHT);
//				itemsDiscount.setText(document.getFormatedStringValueByKey("itemsdiscount"));
//				itemsDiscount.setToolTipText(discountLabel.getToolTipText());
//				GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.TOP).applyTo(itemsDiscount);
//
//				// Set the tab order
//				setTabOrder(txtMessage, itemsDiscount);
//
//				// Recalculate, if the discount field looses the focus.
//				itemsDiscount.addFocusListener(new FocusAdapter() {
//					public void focusLost(FocusEvent e) {
//						calculate();
//						checkDirty();
//						itemsDiscount.setText(DataUtils.DoubleToFormatedPercent(DataUtils.StringToDoubleDiscount(itemsDiscount.getText())));
//
//					}
//				});
//
//				// Recalculate, if the discount is modified.
//				itemsDiscount.addKeyListener(new KeyAdapter() {
//					public void keyPressed(KeyEvent e) {
//						if (e.keyCode == 13) {
//							itemsDiscount.setText(DataUtils.DoubleToFormatedPercent(DataUtils.StringToDoubleDiscount(itemsDiscount.getText())));
//							calculate();
//							checkDirty();
//						}
//					}
//				});
//
//			}
//					
//			// Shipping composite contains label and combo.
//			Composite shippingComposite = new Composite(totalComposite, SWT.NONE);
//			GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(shippingComposite);
//			GridDataFactory.fillDefaults().align(SWT.END, SWT.TOP).grab(true, false).applyTo(shippingComposite);
//
//			// Shipping label
//			Label shippingLabel = new Label(shippingComposite, SWT.NONE);
//			//T: Document Editor - Label shipping 
//			shippingLabel.setText(_("Shipping"));
//			GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(shippingLabel);
//			//T: Tool Tip Text
//			shippingLabel.setToolTipText(_("It's possible to enter a different shipping value than those of the selected shipping entry"));
//
//			// Shipping combo
//			comboShipping = new Combo(shippingComposite, SWT.BORDER);
//			comboShipping.setToolTipText(shippingLabel.getToolTipText());
//			comboViewerShipping = new ComboViewer(comboShipping);
//			comboViewerShipping.setContentProvider(new UniDataSetContentProvider());
//			comboViewerShipping.setLabelProvider(new UniDataSetLabelProvider("description"));
//			GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(comboShipping);
//			comboViewerShipping.addSelectionChangedListener(new ISelectionChangedListener() {
//
//				// If a new shipping is selected, recalculate the total
//				// sum,
//				// and update the shipping VAT.
//				public void selectionChanged(SelectionChangedEvent event) {
//					// Get the selected element.
//					ISelection selection = event.getSelection();
//					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
//					if (!structuredSelection.isEmpty()) {
//						// Get first selected element.
//						Object firstElement = structuredSelection.getFirstElement();
//						DataSetShipping dataSetShipping = (DataSetShipping) firstElement;
//						shipping = dataSetShipping.getDoubleValueByKey("value");
//						shippingId = dataSetShipping.getIntValueByKey("id");
//
//						// Update the shipping VAT
//						int shippungVatId = dataSetShipping.getIntValueByKey("vatid");
//						shippingVatDescription = Data.INSTANCE.getVATs().getDatasetById(shippungVatId).getStringValueByKey("description");
//						shippingVat = Data.INSTANCE.getVATs().getDatasetById(shippungVatId).getDoubleValueByKey("value");
//						shippingAutoVat = Data.INSTANCE.getShippings().getDatasetById(shippingId).getIntValueByKey("autovat");
//						calculate();
//						checkDirty();
//					}
//				}
//			});
//
//			// Fill the shipping combo with the shipping values.
//			comboViewerShipping.setInput(Data.INSTANCE.getShippings().getDatasets());
//
//			// Get the documents'shipping values.
//			shipping = document.getDoubleValueByKey("shipping");
//			shippingVat = document.getDoubleValueByKey("shippingvat");
//			shippingAutoVat = document.getIntValueByKey("shippingautovat");
//			shippingVatDescription = document.getStringValueByKey("shippingvatdescription");
//
//			// Set the combo
//			comboShipping.setText(document.getStringValueByKey("shippingdescription"));
//			superviceControl(comboShipping);
//
//			// Shipping value field
//			shippingValue = new Text(totalComposite, SWT.NONE | SWT.RIGHT);
//			shippingValue.setText(DataUtils.DoubleToFormatedPrice(shipping));
//			shippingValue.setToolTipText(shippingLabel.getToolTipText());
//			GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.CENTER).applyTo(shippingValue);
//
//			// Recalculate, if the discount field looses the focus.
//			shippingValue.addFocusListener(new FocusAdapter() {
//
//				public void focusLost(FocusEvent e) {
//					changeShippingValue();
//					checkDirty();
//				}
//			});
//
//			// Recalculate, if the shipping is modified
//			shippingValue.addKeyListener(new KeyAdapter() {
//				public void keyPressed(KeyEvent e) {
//					if (e.keyCode == 13) {
//						changeShippingValue();
//						checkDirty();
//					}
//				}
//			});
//
//			superviceControl(shippingValue, 12);
//
//			// VAT label
//			Label vatLabel = new Label(totalComposite, SWT.NONE);
//			//T: Document Editor - Label VAT 
//			vatLabel.setText(_("VAT"));
//			GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(vatLabel);
//
//			// VAT value
//			vatValue = new Label(totalComposite, SWT.NONE | SWT.RIGHT);
//			vatValue.setText("---");
//			GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.TOP).applyTo(vatValue);
//
//			// Total label
//			Label totalLabel = new Label(totalComposite, SWT.NONE);
//			//T: Document Editor - Total sum of this document 
//			totalLabel.setText(_("Total"));
//			GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(totalLabel);
//
//			// Total value
//			totalValue = new Label(totalComposite, SWT.NONE | SWT.RIGHT);
//			totalValue.setText("---");
//			GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.TOP).applyTo(totalValue);
//
//			// Create the "paid"-controls, only if the document type allows
//			// this.
//			if (documentType.hasPaid()) {
//
//				// The paid label
//				bPaid = new Button(top, SWT.CHECK | SWT.LEFT);
//				if (document.getBooleanValueByKey("paid")) {
//					bPaid.setSelection(document.getBooleanValueByKey("paid"));
//				}
//				if (document.getBooleanValueByKey("isdeposit")) {
//					bPaid.setSelection(document.getBooleanValueByKey("isdeposit"));
//				}
//				deposit = document.getDoubleValueByKey("deposit");
//				
//				//T: Mark a paid document with this text.
//				bPaid.setText(_("paid"));
//				//T: Tool Tip Text
//				bPaid.setToolTipText(_("Check this, if the invoice is paid"));
//				
//				GridDataFactory.swtDefaults().applyTo(bPaid);
//
//				// Container for the payment and the paid state
//				paidContainer = new Composite(top, SWT.NONE);
//				GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(paidContainer);
//				GridDataFactory.swtDefaults().span(2, 1).align(SWT.BEGINNING, SWT.CENTER).applyTo(paidContainer);
//
//				// If the paid check box is selected ...
//				bPaid.addSelectionListener(new SelectionAdapter() {
//
//					// ... Recreate the paid composite
//					public void widgetSelected(SelectionEvent e) {
//						createPaidComposite(bPaid.getSelection(), bPaid.getSelection(), true);
//						checkDirty();
//					}
//				});
//
//				// Combo to select the payment
//				comboPayment = new Combo(paidContainer, SWT.BORDER);
//				comboViewerPayment = new ComboViewer(comboPayment);
//				comboViewerPayment.setContentProvider(new UniDataSetContentProvider());
//				comboViewerPayment.setLabelProvider(new UniDataSetLabelProvider("description"));
//				GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(comboPayment);
//
//				// If a new payment is selected ...
//				comboViewerPayment.addSelectionChangedListener(new ISelectionChangedListener() {
//
//					// change the paymentId to the selected element
//					public void selectionChanged(SelectionChangedEvent event) {
//
//						// Get the selected element
//						ISelection selection = event.getSelection();
//						IStructuredSelection structuredSelection = (IStructuredSelection) selection;
//						if (!structuredSelection.isEmpty()) {
//							// Get first selected element.
//							Object firstElement = structuredSelection.getFirstElement();
//							DataSetPayment dataSetPayment = (DataSetPayment) firstElement;
//							usePayment(dataSetPayment.getIntValueByKey("id"));
//						}
//					}
//				});
//
//				// Fill the payment combo with the payments
//				comboViewerPayment.setInput(Data.INSTANCE.getPayments().getDatasets());
//				superviceControl(comboPayment);
//
//				// Create a default paid composite with the document's
//				// state for "paid"
//				createPaidComposite(document.getBooleanValueByKey("paid"), document.getBooleanValueByKey("isdeposit"), false);
//
//				// Set the combo
//				comboPayment.setText(document.getStringValueByKey("paymentdescription"));
//
//			}
//		}
//
//		updateUseGross(false);
//
//		// Calculate the total sum
//		if(documentType != DocumentType.DUNNING) {
//			calculate();
//		}
//
//	}
//
//	/**
//	 * Set the focus to the top composite.
//	 * 
//	 * @see com.sebulli.fakturama.editors.Editor#setFocus()
//	 */
//	@Override
//	public void setFocus() {
//		if(top != null) 
//			top.setFocus();
//	}
//
//	/**
//	 * Test, if there is a document with the same number
//	 * 
//	 * @return TRUE, if one with the same number is found
//	 */
//	public boolean thereIsOneWithSameNumber() {
//		// Letters do not have to be checked
//		if (documentType == DocumentType.LETTER)
//			return false;
//
//		// Cancel, if there is already a document with the same ID
//		if (Data.INSTANCE.getDocuments().isExistingDataSet(document, "name", txtName.getText())) {
//			// Display an error message
//			MessageBox messageBox = new MessageBox(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.ICON_ERROR | SWT.OK);
//
//			//T: Title of the dialog that appears if the document number is not valid.
//			messageBox.setText(_("Error in document number"));
//
//			//T: Text of the dialog that appears if the customer number is not valid.
//			messageBox.setMessage(_("There is already a document with the number:") + " " + txtName.getText());
//			
//			messageBox.open();
//
//			return true;
//		}
//
//		return false;
//	}
//
//	/**
//	 * Returns, if save is allowed
//	 * 
//	 * @return TRUE, if save is allowed
//	 * 
//	 * @see com.sebulli.fakturama.editors.Editor#saveAllowed()
//	 */
//	@Override
//	protected boolean saveAllowed() {
//		// Save is allowed, if there is no document with the same number
//		return !thereIsOneWithSameNumber();
//	}
//
//
//	/**
//	 * Create the default context menu 
//	 */
//	private void createContextMenu(TableViewer tableViewerItems) {
//		
//		//Cancel, if there are no items
//		if (tableViewerItems == null)
//			return;
//		
//		menuManager = new MenuManager();
//		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
//		tableViewerItems.getTable().setMenu(menuManager.createContextMenu(tableViewerItems.getTable()));
//
//		getSite().registerContextMenu("com.sebulli.fakturama.editors.DocumentEditor.tableViewerItems.contextmenu", menuManager, tableViewerItems);
//		getSite().setSelectionProvider(tableViewerItems);
//		
//		// Add up/down and delete actions
//		menuManager.add(new MoveEntryUpAction());
//		menuManager.add(new MoveEntryDownAction());
//		menuManager.add(new DeleteDataSetAction());
//	}
//
//	/**
//	 * Move an item up or down
//	 */
//	public void moveItem(UniDataSet uds, boolean up) {
//
//		if (!(uds instanceof DataSetItem))
//			return;
//		
//		if (items.getActiveDatasets().contains(uds)) {
//
//			// Get the position of the selected element
//			int prepos = items.getDatasets().indexOf(items.getPreviousDataSet((DataSetItem) uds));
//			int pos = items.getDatasets().indexOf(uds);
//			int nextpos = items.getDatasets().indexOf(items.getNextDataSet((DataSetItem) uds));
//			int size = items.getDatasets().size();
//			int activesize = items.getActiveDatasets().size();
//
//			// Do not move one single item
//			if (activesize >= 2) {
//				// Move up
//				if (up && (prepos >=0 )){
//					items.swapPosition(prepos, pos);
//				}
//
//				// Move down
//				if (!up && (nextpos < size) && nextpos >= 0){
//					items.swapPosition(pos, nextpos);
//				}
//			}
//			
//			
//		}
//
//		//Renumber the items
//		RenumberItems();
//
//		// Refresh the table
//		tableViewerItems.refresh();
//		checkDirty();
//	}
//	
//	/**
//	 * delete an item
//	 */
//	public void deleteItem(UniDataSet uds) {
//
//		if (!(uds instanceof DataSetItem))
//			return;
//		
//		// Delete it (mark it as deleted)
//		uds.setBooleanValueByKey("deleted", true);
//		
//		// Renumber the items
//		RenumberItems();
//
//		tableViewerItems.refresh();
//		calculate();
//		checkDirty();
//	}
//
//
}
