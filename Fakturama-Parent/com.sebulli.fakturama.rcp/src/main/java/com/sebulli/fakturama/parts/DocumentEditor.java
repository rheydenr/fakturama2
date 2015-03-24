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

import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MDialog;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.nebula.widgets.formattedtext.PercentFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.osgi.service.event.Event;

import com.sebulli.fakturama.calculate.CustomerStatistics;
import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.dao.ShippingsDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.misc.OrderState;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Delivery;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.Document_;
import com.sebulli.fakturama.model.Dunning;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.model.ShippingVatType;
import com.sebulli.fakturama.model.TextModule;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.parts.converter.EntityConverter;
import com.sebulli.fakturama.parts.converter.StringToEntityConverter;
import com.sebulli.fakturama.parts.itemlist.DocumentItemListTable;
import com.sebulli.fakturama.parts.itemlist.ItemListBuilder;
import com.sebulli.fakturama.parts.widget.ComboBoxLabelProvider;
import com.sebulli.fakturama.parts.widget.EntityComboProvider;
import com.sebulli.fakturama.parts.widget.EntityLabelProvider;
import com.sebulli.fakturama.parts.widget.HashMapContentProvider;
import com.sebulli.fakturama.parts.widget.MoneyFormatter;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;
import com.sebulli.fakturama.util.ContactUtil;
import com.sebulli.fakturama.util.ProductUtil;
import com.sebulli.fakturama.views.datatable.contacts.ContactListTable;


/**
 * The document editor for all types of document like letter, order,
 * confirmation, invoice, delivery, credit and dunning
 * 
 */
public class DocumentEditor extends Editor<Document> {

	// Editor's ID
    public static final String EDITOR_ID = "DocumentEditor";
	public static final String ID = "com.sebulli.fakturama.editors.documentEditor";
	
	/**
	 * The key for the document id parameter (used for differentiating the editors
	 * for the Event Broker).
	 */
	public static final String DOCUMENT_ID = "com.sebulli.fakturama.editors.document.id";
    
    private static final String TOOLITEM_COMMAND = "toolitem_command";

    @Inject
    private ECommandService cmdService;
    
    @Inject
    private EHandlerService handlerService;
    
    @Inject
    private EModelService modelService;
    
    @Inject
    private MApplication application;
    
    @Inject
    @Translation
    protected Messages msg;
    
    @Inject
    private IEclipseContext context;

    @Inject
    private Logger log;

	// This UniDataSet represents the editor's input
	private Document document;
	
    private MPart part;
    
    @Inject
    private DocumentsDAO documentsDAO;
    
    @Inject
    private ProductsDAO productsDAO;
    
    @Inject
    private ShippingsDAO shippingsDAO;

    @Inject
    private PaymentsDAO paymentsDao;

    @Inject
    protected VatsDAO vatDao;
    
    @Inject
    @Preference  //(nodePath = "/configuration/preferences")
    protected IEclipsePreferences preferences;
    
    @Inject
    @Preference(nodePath=Constants.DEFAULT_PREFERENCES_NODE)
    private IEclipsePreferences eclipseDefaultPrefs;
    
    /**
     * the model factory
     */
    private FakturamaModelFactory modelFactory;

	// SWT components of the editor
	private Composite top;
	private Text txtName;
	private CDateTime dtDate;
	private CDateTime dtOrderDate;
	private CDateTime dtServiceDate;
	private Text txtCustomerRef;
	private Text txtConsultant;
	private Text txtAddress;
	private ComboViewer comboViewerNoVat;
	private ComboViewer comboNetGross;
	private Text txtInvoiceRef;
	private TableViewer tableViewerItems;
	private Text txtMessage;
	private Text txtMessage2;
	private Text txtMessage3;
	private Button bPaid;
	private Composite paidContainer;
	private Composite paidDataContainer = null;
	private Combo comboPayment;
	private ComboViewer comboViewerPayment;
	private Label warningDepositIcon;
	private Label warningDepositText;
	private Spinner spDueDays;
	private CDateTime dtIssueDate;
	private CDateTime dtPaidDate;
	private Label itemsSum;
	private FormattedText itemsDiscount;
	private ComboViewer comboViewerShipping;
	private FormattedText shippingValue;
	//private Text depositValue;
	private Label vatValue;
	private Label totalValue;
	private Composite addressAndIconComposite;
	private Label differentDeliveryAddressIcon;
	private Label netLabel;

	// These flags are set by the preference settings.
	// They define, if elements of the editor are displayed, or not.
	private boolean useGross;

	// The type of this document
	private DocumentType documentType;

	// These are (non visible) values of the document
	private Contact addressId = null;
	private boolean noVat;
	private String noVatName;
	private String noVatDescription;
	private Payment payment;
	private Double paidValue = Double.valueOf(0.0);
	private int shippingId;
	private Shipping shipping = null;
	private VAT shippingVat = null;
	private String shippingVatDescription = "";
	private ShippingVatType shippingAutoVat = ShippingVatType.SHIPPINGVATGROSS;
	private Double total = Double.valueOf(0.0);
	private Double deposit = Double.valueOf(0.0);
	private Double finalPayment = Double.valueOf(0.0);
	private int dunningLevel = Integer.valueOf(0);
	private int duedays;
	private String billingAddress = "";
	private String deliveryAddress = "";
//	private DocumentEditor thisDocumentEditor;
	private int netgross = DocumentSummary.NOTSPECIFIED;
	
	// Flag, if item editing is active
//	private DocumentItemEditingSupport itemEditingSupport = null;

	// Action to print this document's content.
	// Print means: Export the document in an OpenOffice document
//	CreateOODocumentAction printAction;

	// defines, if the contact is new created
	private boolean newDocument;

	// If the customer is changed, and this document displays no payment text,
	// use this variable to store the payment and due days
	private Payment newPayment = null;
	private String newPaymentDescription = "";
	
	// Imported delivery notes. This list is used to
	// set an reference to this document, if it's an invoice.
	// The reference is not set during the import but later when the
	// document is saved. Because the the  document has an id to reference to.
	private List<Long> importedDeliveryNotes = new ArrayList<>();

    private ProductUtil productUtil;
    private DocumentItemListTable itemListTable;
	
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
	
	/**
	 * Mark this document as printed
	 */
	public void markAsPrinted() {
		document.setPrinted(Boolean.TRUE);
		// Refresh the table view
		//refreshView();

	}
	
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
		 * the following parameters are not saved: 
		 * - id (constant) 
		 * - progress (not modified by editor) 
		 * - transaction (not modified by editor)
		 * - webshopid (not modified by editor)
		 * - webshopdate (not modified by editor)
		 *  ITEMS:
		 *  	- id (constant) 
		 *  	- deleted (is checked by the items string)
		 *  	- shared (not modified by editor)
		 */

//		// Cancel the item editing
//		if (itemEditingSupport != null)
//			itemEditingSupport.cancelAndSave();
//
		boolean wasDirty = getMDirtyablePart().isDirty();

		if (newDocument) {
			// Check if the document number is the next one
			if (documentType != DocumentType.LETTER) {
				int result = setNextNr(txtName.getText(), Document_.name.getName());

				// It's not the next free ID
				if (result == ERROR_NOT_NEXT_ID) {
					// Display an error message
					MessageBox messageBox = new MessageBox(top.getShell(), SWT.ICON_ERROR | SWT.OK);

					//T: Title of the dialog that appears if the document number is not valid.
					messageBox.setText(msg.editorDocumentErrorDocnumberTitle);
					
					//T: Text of the dialog that appears if the customer number is not valid.
					messageBox.setMessage(msg.editorDocumentErrorDocnumberNotnextfree + " " + getNextNr() + "\n" + 
							//T: Text of the dialog that appears if the number is not valid.
							msg.editorContactHintSeepreferences);
					messageBox.open();
				}
			}

		}

		// Exit save if there is a document with the same number
		if (thereIsOneWithSameNumber())
			return;

		// Always set the editor's data set to "undeleted"
		document.setDeleted(Boolean.FALSE);

		// Set the document type
		document.setBillingType(BillingType.get(documentType.getKey()));

		// If this is an order, use the date as order date
		if (documentType == DocumentType.ORDER)
			document.setOrderDate(dtDate.getSelection());
		else
			document.setOrderDate(dtOrderDate.getSelection());

//		document.setIntValueByKey("addressid", addressId);
		String addressById = "";

		// Test, if the txtAddress field was modified
		// and write the content of the txtAddress to the documents address or
		// delivery address
		boolean addressModified = false;
		// if it's a delivery note, compare the delivery address
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
//		// Set the whole vat of the document to zero
//		document.setBooleanValueByKey("novat", noVat);
//		document.setStringValueByKey("novatname", noVatName);
//		document.setStringValueByKey("novatdescription", noVatDescription);
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

		//Set the editor's name
		this.part.setLabel(document.getName());

        // Refresh the table view of all payments
        evtBroker.post(ID, "update");
        
        // reset dirty flag
        getMDirtyablePart().setDirty(false);
	}

	/**
	 * Updates all Dunnings which are related to the current invoice.
	 */
	private void updateDunnings() {
	    documentsDAO.updateDunnings(document, bPaid.getSelection(), dtPaidDate.getSelection(), paidValue);
	}

	/**
	 * Initializes the editor. If an existing data set is opened, the local
	 * variable "document" is set to This data set. If the editor is opened to
	 * create a new one, a new data set is created and the local variable
	 * "contact" is set to this one.
	 * 
	 * @param input
	 *            The editor's input
	 * @param site
	 *            The editor's site
	 */
    @PostConstruct
    public void init(Composite parent) {
        modelFactory = FakturamaModelPackage.MODELFACTORY;
        productUtil = ContextInjectionFactory.make(ProductUtil.class, context);
        Long objId = null;
        this.part = (MPart) parent.getData("modelElement");
                
        String tmpObjId = (String) part.getProperties().get(CallEditor.PARAM_OBJ_ID);
        if (StringUtils.isNumeric(tmpObjId)) {
            objId = Long.valueOf(tmpObjId);
            // Set the editor's data set to the editor's input
            document = documentsDAO.findById(objId);
        }

		// If the document is a duplicate of an other document,
		// the input is the parent document.
		Document parentDoc = document;
		// the parents document type
		DocumentType documentTypeParent = DocumentType.NONE;
		String tmpDuplicate =  (String) part.getProperties().get(CallEditor.PARAM_DUPLICATE);
		boolean duplicated = BooleanUtils.toBoolean(tmpDuplicate);

		// The document is new, if there is no document, or if the
		// flag for duplicated was set.
		newDocument = (document == null) || duplicated;

		// If new ..
		if (newDocument) {

			// .. get the document type (=the category) to ..
			String category = (String) part.getProperties().get(CallEditor.PARAM_CATEGORY);
			BillingType billingType = BillingType.get(category);
			documentType = DocumentType.findByKey(billingType.getValue());
			if (documentType == DocumentType.NONE)
				documentType = DocumentType.ORDER;

			// create a new data set with this document type
			switch (documentType) {
            case INVOICE:
                document = modelFactory.createInvoice();
                break;
            case PROFORMA:
                document = modelFactory.createProforma();
                break;
            case DUNNING:
                document = modelFactory.createDunning();
                break;
            case DELIVERY:
                document = modelFactory.createDelivery();
                break;
            case OFFER:
                document = modelFactory.createOffer();
                break;
            case ORDER:
                document = modelFactory.createOrder();
                break;
            case CONFIRMATION:
                document = modelFactory.createConfirmation();
                break;
            case CREDIT:
                document = modelFactory.createCredit();
                break;
            default:
                document = modelFactory.createOrder();
                break;
            }
			if (duplicated) {
				document.setSourceDocument(parentDoc);
			}
            document.setBillingType(billingType);

			// Copy the entry "message", or reset it to ""
			if (!getBooleanPreference(Constants.PREFERENCES_DOCUMENT_COPY_MESSAGE_FROM_PARENT, false)) {
				document.setMessage("");
				document.setMessage2("");
				document.setMessage3("");
			}

			// get the parents document type
			if (parentDoc != null) {
				documentTypeParent = DocumentType.findDocumentTypeByDescription(parentDoc.getBillingType().getName());
			}

			// If it's a dunning, increase the dunning level by 1
			if (documentType == DocumentType.DUNNING) {
				if (documentTypeParent == DocumentType.DUNNING) {
					dunningLevel = ((Dunning)document).getDunningLevel() + 1;
				} else {
					dunningLevel = 1;
				}
			}

			// If it's a credit or a dunning, set it to unpaid
			if ( (documentType == DocumentType.CREDIT)|| (documentType == DocumentType.DUNNING)) {
				document.setPaid(false);
			}
			
			// Set the editors name
//            part.setLabel(documentType.getNewText());

			// In a new document, set some standard values
			if (!duplicated) {
				// Default shipping
				shippingId = preferences.getInt(Constants.DEFAULT_SHIPPING, 1);
				shipping = shippingsDAO.findById(shippingId);
				shippingVat = shipping.getShippingVat();
				shippingAutoVat = shipping.getAutoVat();
				shippingVatDescription = shipping.getDescription();
				netgross = DocumentSummary.NOTSPECIFIED;
				
				document.setShipping(shipping);
//				document.setDoubleValueByKey("shippingvat", shippingVat);
//				document.setStringValueByKey("shippingdescription", stdShipping.getStringValueByKey("description"));
				document.setShippingAutoVat(shippingAutoVat);
//				document.setStringValueByKey("shippingvatdescription", shippingVatDescription);
				
				// Default payment
				int paymentId = preferences.getInt(Constants.DEFAULT_PAYMENT, 1);
                payment = paymentsDao.findById(paymentId);
                document.setPayment(payment);
//				document.setStringValueByKey("paymentdescription", Data.INSTANCE.getPayments().getDatasetById(paymentId).getStringValueByKey("description"));
				document.setDueDays(payment.getNetDays());
			}
			else {
				payment = document.getPayment();
				shipping = document.getShipping();
				total = document.getTotalValue();
			}

			// Get the next document number
			document.setName(getNextNr());

		}
		// If an existing document was opened ..
		else {

			// Get document type, set editorID
			documentType = DocumentType.findByKey(document.getBillingType().getValue());
			setEditorID(documentType.getTypeAsString());

			payment = document.getPayment();
			shipping = document.getShipping();

			// and the editor's part name
			part.setLabel(document.getName());

		}

		// These variables contain settings, that are not in
		// visible SWT widgets.
		duedays = document.getDueDays();
		addressId = document.getContact();
		
		noVat = document.getNoVatReference() != null;
		if(noVat) {
		    noVatName = document.getNoVatReference().getName();
		    noVatDescription = document.getNoVatReference().getDescription();
		}
		netgross = document.getNetGross() != null ? document.getNetGross() : 0;
		
		paidValue = document.getPaidValue();
		if (dunningLevel <= 0) {
            if (document.getBillingType() == BillingType.DUNNING) {
            	dunningLevel = ((Dunning)document).getDunningLevel();
            } else {
                dunningLevel = 1;
            }
        }

		ContactUtil contactUtil = ContactUtil.getInstance(preferences);
        billingAddress = contactUtil.getAddressAsString(document.getContact());
		deliveryAddress = contactUtil.getAddressAsString(document.getDeliveryContact());
        
        createPartControl(parent);
	}
//
//	
////	/**
////	 * Sets a flag, if item editing is active
////	 * 
////	 * @param active
////	 *            , TRUE, if editing is active
////	 */
////	public void setItemEditing(DocumentItemEditingSupport itemEditingSupport) {
////		this.itemEditingSupport = itemEditingSupport;
////	}

	/**
	 * Returns the document
	 * 
	 * @return The document
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Returns the document type
	 * 
	 * @return The document type
	 */
	public DocumentType getDocumentType() {
		return documentType;
	}

	/**
	 * If this document is duplicated, set the documents progress from 0% to 50%
	 * 
	 */
	public void childDocumentGenerated() {
		if (document.getProgress() == 0) {
			document.setProgress(OrderState.PROCESSING.getState());
//			Data.INSTANCE.updateDataSet(document);
		}
	}
	
	public void calculate() {
		calculate(false);
	}

	/**
	 * Recalculate the total sum of this editor and write the result to the
	 * corresponding fields.
	 * 
	 */
	public void calculate(boolean forceCalc) {

		// Recalculate only documents that contains price values.
		if (!documentType.hasPrice() && !forceCalc)
			return;

		// Get the sign of this document ( + or -)
		int sign = DocumentType.findByKey(document.getBillingType().getValue()).getSign();
		
		// Get the discount value from the control element
		Double discount = 0.0;
		if (itemsDiscount != null)
			discount = (Double) itemsDiscount.getValue();

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
//			totalValue.setToolTipText(msg.documentOrderStatePaid + ":" + document.getPayedValue());
//		}

	}


	/**
	 * Get the total text, net or gross
	 * 
	 * @return
	 * 		The total text
	 */
	private String getTotalText () {
		if (useGross)
			return msg.editorDocumentTotalgross;
		else
			//T: Document Editor - Label Total net 
			return msg.editorDocumentTotalnet;
	}
	
	/**
	 * Change the document from net to gross or backwards 
	 */
	private void updateUseGross(boolean address_changed) {
		
		boolean oldUseGross = useGross;
		
		// Get some settings from the preference store
		if (netgross == DocumentSummary.NOTSPECIFIED)
			useGross = (getIntPreference(Constants.PREFERENCES_DOCUMENT_USE_NET_GROSS, 1) == 1);
		else 
			useGross = ( netgross == DocumentSummary.ROUND_GROSS_VALUES );
		
		
		// Use the customers settings instead, if they are set
		if ((addressId != null) && address_changed) {
			
			if (addressId.getUseNetGross() == 1) {
				useGross = false;
				netgross = DocumentSummary.ROUND_NET_VALUES;
				comboNetGross.getCombo().select(netgross);
			}
			if (addressId.getUseNetGross() == 2) {
				useGross = true;
				netgross = DocumentSummary.ROUND_GROSS_VALUES;
				comboNetGross.getCombo().select(netgross);
			}
			
		}

		// Show a warning, if the customer uses a different setting for net or gross
		if ((useGross != oldUseGross) && documentType.hasPrice()) {
			
			if (address_changed) {
				MessageBox messageBox = new MessageBox(top.getShell(), SWT.ICON_WARNING | SWT.OK);

				//T: Title of the dialog that appears if customer uses a different setting for net or gross.
				messageBox.setText(msg.dialogMessageboxTitleWarning);
				
				
				if (useGross) {
					//T: Text of the dialog that appears if customer uses a different setting for net or gross.
					messageBox.setMessage(msg.editorDocumentDialogGrossvalues);
				}
				else {
					//T: Text of the dialog that appears if customer uses a different setting for net or gross.
					messageBox.setMessage(msg.editorDocumentDialogNetvalues);
				}
				messageBox.open();
			}

			// Update the columns
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
			
			// Update the shipping value;
			calculate();
			
		}
		
	}
	
	/**
	 * Returns, if this editor used net or gross values.
	 * 
	 * @return True, if the document uses gross values.
	 */
	public boolean getUseGross() {
		return useGross;
	}

	/**
	 * The shipping value has changed. So take the absolute value and
	 * recalculate the document's total sum.
	 */
	private void changeShippingValue() {

		// Get the new value and take the absolute value
		Double newShippingValue = (Double) shippingValue.getValue();
		if (newShippingValue < 0)
			newShippingValue = -newShippingValue;

		// If the shipping value has changed:
		// Set the shippingAutoVat to net or gross, depending on the
		// settings of this editor.
		if (!DataUtils.getInstance().DoublesAreEqual(newShippingValue, shipping.getShippingValue())) {
			shippingAutoVat = useGross ? ShippingVatType.SHIPPINGVATGROSS : ShippingVatType.SHIPPINGVATNET;
		}

		// Recalculate the sum
//		shipping = newShippingValue;
		calculate();
	}

	/**
	 * Create a SWT composite witch contains other SWT widgets like the payment
	 * date or the paid value. Depending on the parameter "paid" widgets are
	 * created to set the due values or the paid values.
	 * 
	 * @param paid
	 *            If true, the widgets for "paid" are generated
	 */
	private void createPaidComposite(boolean paid, boolean isdeposit, boolean clickedByUser) {

		// If this widget exists yet, remove it to create it new.
		boolean changed = false;
		if (paidDataContainer != null && !paidDataContainer.isDisposed()) {
			paidDataContainer.dispose();
			changed = true;
		}

		// Create the new paid container
		paidDataContainer = new Composite(paidContainer, SWT.NONE);
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(6).applyTo(paidDataContainer);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BOTTOM).applyTo(paidDataContainer);

		// Should this container have the widgets for the state "paid" ?
		if (paid) {
			createDepositContainer(clickedByUser);
		} else if (isdeposit) {
			createDepositContainer(clickedByUser);
			
			// Add the attention sign if its a deposit
			warningDepositIcon = new Label(paidDataContainer, SWT.NONE);
			warningDepositIcon.setImage(Icon.ICON_WARNING.getImage(IconSize.ToolbarIconSize));
			warningDepositText = new Label(paidDataContainer, SWT.NONE);
			warningDepositText.setText(msg.editorDocumentFieldDeposit);
		}
		// The container is created with the widgets that are shown
		// if the invoice is not paid.
		else {

			// Reset the paid value to 0
			paidValue = Double.valueOf(0.0);

			// Create the due days label
			Label dueDaysLabel = new Label(paidDataContainer, SWT.NONE);

			//T: Document Editor - Label before the Text Field "Due Days".
			//T: Format: THIS LABEL <DAYS> PAYABLE UNTIL <ISSUE DATE>
			dueDaysLabel.setText(msg.editorDocumentDuedays);
			//T: Tool Tip Text
			dueDaysLabel.setToolTipText(msg.editorDocumentDuedaysTooltip);

			GridDataFactory.swtDefaults().indent(20, 0).align(SWT.END, SWT.CENTER).applyTo(dueDaysLabel);

			// Creates the due days spinner
			spDueDays = new Spinner(paidDataContainer, SWT.BORDER | SWT.RIGHT);
			spDueDays.setMinimum(0);
			spDueDays.setMaximum(365);
			spDueDays.setSelection(duedays /* document.getIntValueByKey("duedays") */);
			spDueDays.setIncrement(1);
			spDueDays.setPageIncrement(10);
			spDueDays.setToolTipText(dueDaysLabel.getToolTipText());
			GridDataFactory.swtDefaults().hint(50, SWT.DEFAULT).applyTo(spDueDays);

			// If the spinner's value changes, add the due days to the
			// day of today.
			spDueDays.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
				    Calendar calendar = Calendar.getInstance();
				    calendar.setTime(dtDate.getSelection());
					duedays = spDueDays.getSelection();
					calendar.add(Calendar.DAY_OF_MONTH, duedays );
					dtIssueDate.setSelection(calendar.getTime());
				}
			});

			// Create the issue date label
			Label issueDateLabel = new Label(paidDataContainer, SWT.NONE);

			//T: Document Editor - Label between the Text Field "Due Days" and the Date Field "Issue Date" 
			//T: Format:  DUE DAYS: <DAYS> THIS LABEL <ISSUE DATE>
			issueDateLabel.setText(msg.editorDocumentPayuntil);
			//T: Tool Tip Text
			issueDateLabel.setToolTipText(msg.editorDocumentPayuntilTooltip);

			GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(issueDateLabel);

			// Create the issue date widget
			dtIssueDate = new CDateTime(paidDataContainer, CDT.BORDER | CDT.DROP_DOWN);
			dtIssueDate.setToolTipText(issueDateLabel.getToolTipText());
			GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT).grab(true, false).applyTo(dtIssueDate);
			dtIssueDate.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					// Calculate the difference between the date of the
					// issue date widget and the documents date,
					// calculate is in "days" and set the due day spinner
					Date calendarIssue = dtIssueDate.getSelection();
					Date calendarDocument = Optional.ofNullable(dtDate.getSelection()).orElse(Calendar.getInstance().getTime());
					long difference = calendarIssue.getTime() - calendarDocument.getTime();
					// Calculate from milliseconds to days
					int days = (int) (difference / (1000 * 60 * 60 * 24));
					duedays = days;
					spDueDays.setSelection(days);
				}
			});

			updateIssueDate();
		}

		// Resize the container
		paidContainer.layout(changed);
		paidContainer.pack(changed);
	}

	/**
	 * @param clickedByUser
	 */
	private void createDepositContainer(boolean clickedByUser) {
		// Create the widget for the date, when the invoice was paid
		Label paidDateLabel = new Label(paidDataContainer, SWT.NONE);
		paidDateLabel.setText("am");
		//T: Tool Tip Text
		paidDateLabel.setToolTipText(msg.editorDocumentDateofpayment);

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(paidDateLabel);

		dtPaidDate = new CDateTime(paidDataContainer, CDT.BORDER | CDT.DROP_DOWN);
		dtPaidDate.setToolTipText(paidDateLabel.getToolTipText());
		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(dtPaidDate);

		// Set the paid date to the documents "paydate" parameter
		dtPaidDate.setSelection(document.getPayDate());

		bindModelValue(document, dtPaidDate, Document_.payDate.getName());

		// Create the widget for the value
		Label paidValueLabel = new Label(paidDataContainer, SWT.NONE);
		
		//T: Label in the document editor
		paidValueLabel.setText(msg.commonFieldValue);
		//T: Tool Tip Text
		paidValueLabel.setToolTipText(msg.editorDocumentPaidvalue);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(paidValueLabel);

		// If it's the first time that this document is marked as paid
		// (if the value is 0.0), then also set the date to "today"
		if (paidValue == 0.0 && clickedByUser) {
			paidValue = total;
			dtPaidDate.setSelection(Calendar.getInstance().getTime());
		}
		FormattedText txtPayValue = new FormattedText(paidDataContainer, SWT.BORDER | SWT.RIGHT);
		txtPayValue.setValue(paidValue);
		txtPayValue.setFormatter(new MoneyFormatter());
		txtPayValue.getControl().setToolTipText(paidValueLabel.getToolTipText());
		bindModelValue(document, txtPayValue, Document_.paidValue.getName(), 32);
		GridDataFactory.swtDefaults().hint(60, SWT.DEFAULT).applyTo(txtPayValue.getControl());
	}
	
	/**
	 * Update the Issue Date widget with the date that corresponds to the due date
	 */
	void updateIssueDate() {
		// Add date and due days and set the issue date to the sum.
//		GregorianCalendar calendar = new GregorianCalendar(dtDate.getYear(), dtDate.getMonth(), dtDate.getDay());
//		calendar.add(Calendar.DAY_OF_MONTH, spDueDays.getSelection());
//		dtIssueDate.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

	}
	
	/**
	 * Show or hide the warning icon
	 */
	void showHideWarningIcon() {
		
		// Check, whether the delivery address is the same as the billing address
		boolean differentDeliveryAddress;
		
		if (documentType == DocumentType.DELIVERY) {
			differentDeliveryAddress = !billingAddress.equalsIgnoreCase(DataUtils.getInstance().removeCR(txtAddress.getText()));
			//T: Tool Tip Text
			differentDeliveryAddressIcon.setToolTipText(msg.editorDocumentWarningDifferentaddress + '\n' + billingAddress);
		}
		else {
			differentDeliveryAddress = !deliveryAddress.equalsIgnoreCase(DataUtils.getInstance().removeCR(txtAddress.getText()));
			//T: Tool Tip Text
			differentDeliveryAddressIcon.setToolTipText(msg.editorDocumentWarningDifferentdeliveryaddress + '\n' + deliveryAddress);
		}

		if (differentDeliveryAddress)
			// Show the icon
			GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(differentDeliveryAddressIcon);
		else
			// Hide the icon
			GridDataFactory.swtDefaults().hint(0,0).align(SWT.END, SWT.CENTER).applyTo(differentDeliveryAddressIcon);
		
	}
	
	/**
	 * Fill the address label with a contact 
	 * 
	 * @param contact
	 * 		The contact
	 */
	private void setAddress(Contact contact) {
		// Use delivery address, if it's a delivery note
	    ContactUtil contactUtil = ContactUtil.getInstance(preferences);
		if (documentType == DocumentType.DELIVERY) {
		    txtAddress.setText(contactUtil.getAddressAsString(contact.getDeliveryContacts()));
		} else {
		    txtAddress.setText(contactUtil.getAddressAsString(contact));
		}
		
		billingAddress = contactUtil.getAddressAsString(contact);
		deliveryAddress = contactUtil.getAddressAsString(contact.getDeliveryContacts());

		this.addressId = contact;

		if (getBooleanPreference(Constants.PREFERENCES_DOCUMENT_USE_DISCOUNT_ALL_ITEMS, false) && itemsDiscount != null) {
        	itemsDiscount.setValue(contact.getDiscount());
        }
		// Check, if the payment is valid
		Payment paymentid = contact.getPayment();
		
		if (paymentid != null) {
			//Use the payment method of the customer
			if (comboPayment != null) {
				comboPayment.setText(paymentid.getDescription());
			}

			usePayment(contact.getPayment());
		}
		
		showHideWarningIcon();
		addressAndIconComposite.layout(true);
		updateUseGross(true);
	}
	
	/**
	 * Use this payment and update the duedays
	 * 
	 * @param dataSetPayment
	 * 	ID of the payment
	 */
	private void usePayment(Payment dataSetPayment) {
		
		// Return, if no payment is set
		if (dataSetPayment == null)
			return;
		
		payment = dataSetPayment;
		
//		Payment payment = paymentsDAO.findById(dataSetPayment);

		// Get the due days and description of this payment
		duedays = payment.getNetDays();
		newPayment = dataSetPayment;
		newPaymentDescription = payment.getDescription();

		if (spDueDays !=null ) {
			if (!spDueDays.isDisposed()) {
				spDueDays.setSelection(duedays);
				updateIssueDate();
			}
		}
	}
	
	
	/**
	 * Creates the SWT controls for this workbench part
	 * 
	 * @param the
	 *            parent control
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		// Printing an document from the document editor means:
		// Start OpenOffice in the background and export the document as
		// an OpenOffice document.
//		printAction = new CreateOODocumentAction();
//		getEditorSite().getActionBars().setGlobalActionHandler(ActionFactory.PRINT.getId(), printAction);

		showOrderStatisticDialog(parent);
		
		// Get some settings from the preference store
		if (netgross == DocumentSummary.NOTSPECIFIED) {
			useGross = (getIntPreference(Constants.PREFERENCES_DOCUMENT_USE_NET_GROSS, 1) == 1);
		} else { 
			useGross = ( netgross == DocumentSummary.ROUND_GROSS_VALUES );
		}

		// Create the ScrolledComposite to scroll horizontally and vertically
	    ScrolledComposite scrollcomposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);

		// Create the top Composite
		top = new Composite(scrollcomposite, SWT.SCROLLBAR_OVERLAY | SWT.NONE );  //was parent before 
		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(top);

		scrollcomposite.setContent(top);
		scrollcomposite.setMinSize(1000, 600);   // 2nd entry should be adjusted to higher value when new fields will be added to composite 
		scrollcomposite.setExpandHorizontal(true);
		scrollcomposite.setExpandVertical(true);
        scrollcomposite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,true));

		// Create an invisible container for all hidden components
		Composite invisible = new Composite(top, SWT.NONE);
		invisible.setVisible(false);
		GridDataFactory.fillDefaults().hint(0, 0).span(4, 1).applyTo(invisible);

		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, ContextHelpConstants.DOCUMENT_EDITOR);
		
		// Document number label
		Label labelName = new Label(top, SWT.NONE);

		//T: Document Editor - Label Document Number
		labelName.setText(msg.commonFieldNumber);
		//T: Tool Tip Text
		labelName.setToolTipText(msg.editorDocumentRefnumberTooltip);

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);

		// Container for the document number and the date
		Composite nrDateNetGrossComposite = new Composite(top, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(4).applyTo(nrDateNetGrossComposite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(nrDateNetGrossComposite);

		// The document number is the document name
		txtName = new Text(nrDateNetGrossComposite, SWT.BORDER);
		txtName.setToolTipText(labelName.getToolTipText());

		bindModelValue(document, txtName, Document_.name.getName(), 32);
		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(txtName);
        
		// Document date
		//T: Document Editor
		//T: Label Document Date
		Label labelDate = new Label(nrDateNetGrossComposite, SWT.NONE);
		labelDate.setText(msg.commonFieldDate);
		//T: Tool Tip Text
		labelDate.setToolTipText(msg.editorDocumentDateTooltip);
		
		GridDataFactory.swtDefaults().indent(20, 0).align(SWT.END, SWT.CENTER).applyTo(labelDate);

		// Document date
		dtDate = new CDateTime(nrDateNetGrossComposite, CDT.BORDER | CDT.DROP_DOWN);
		dtDate.setToolTipText(labelDate.getToolTipText());
		dtDate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// If the date is modified, also modify the issue date.
				// (Let the due days constant).
				if (dtIssueDate != null) {
				    // TODO how can we do this?
//					GregorianCalendar calendar = new GregorianCalendar(dtDate.getYear(), dtDate.getMonth(), dtDate.getDay());
//					calendar.add(Calendar.DAY_OF_MONTH, spDueDays.getSelection());
//					dtIssueDate.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
				}
			}
		});
		GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT).applyTo(dtDate);
		
		// Set the dtDate widget to the documents date
		bindModelValue(document, dtDate, Document_.documentDate.getName());
		
		// combo list to select between net or gross
		comboNetGross = new ComboViewer(documentType.hasPrice() ? nrDateNetGrossComposite : invisible, SWT.BORDER);
		comboNetGross.getCombo().setToolTipText(msg.editorDocumentNetgrossTooltip);
		
		Map<Integer, String> netGrossContent = new HashMap<>();
		// empty, if nothing is selected
		netGrossContent.put(0, "---"); 
		//T: Text in combo box
		netGrossContent.put(1, msg.productDataNet);
		//T: Text in combo box
		netGrossContent.put(2, msg.productDataGross);
        
		comboNetGross.setContentProvider(new HashMapContentProvider<Integer, String>());
		comboNetGross.setLabelProvider(new ComboBoxLabelProvider<Integer, String>(netGrossContent));
		comboNetGross.setInput(netGrossContent);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).indent(50, 0).hint(120, SWT.DEFAULT).grab(true, false).applyTo(comboNetGross.getControl());
		
		comboNetGross.getCombo().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				netgross = comboNetGross.getCombo().getSelectionIndex();
				// recalculate the total sum
				calculate();
				updateUseGross(false);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		bindModelValue(document, comboNetGross, Document_.netGross.getName());
	
		// The titleComposite contains the title and the document icon
		Composite titleComposite = new Composite(top, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(titleComposite);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.BOTTOM).span(2, 1).grab(true, false).applyTo(titleComposite);

		// Set the title in large letters
		Label labelDocumentType = new Label(titleComposite, SWT.NONE);
		String documentTypeString = msg.getMessageFromKey(DocumentType.findByKey(document.getBillingType().getValue()).getSingularKey());
		if (documentType == DocumentType.DUNNING) {
			documentTypeString = MessageFormat.format("{0}. {1}", Integer.toString(dunningLevel), documentTypeString);
		}
		labelDocumentType.setText(documentTypeString);
		makeLargeLabel(labelDocumentType);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(labelDocumentType);

		// Set the document icon
		Label labelDocumentTypeIcon = new Label(titleComposite, SWT.NONE);
		Icon icon = createDocumentIcon();
		labelDocumentTypeIcon
					.setImage(icon.getImage(IconSize.ToolbarIconSize));
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.TOP).grab(true, false).applyTo(labelDocumentTypeIcon);

		// Customer reference label
		Label labelCustomerRef = new Label(top, SWT.NONE);
		//T: Document Editor - Label Customer Reference
		labelCustomerRef.setText(msg.editorDocumentFieldCustref);
		//T: Tool Tip Text
		labelCustomerRef.setToolTipText(msg.editorDocumentFieldCustrefTooltip);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCustomerRef);

		// Customer reference 
		txtCustomerRef = new Text(top, SWT.BORDER); 
		txtCustomerRef.setToolTipText(labelCustomerRef.getToolTipText());
		bindModelValue(document, txtCustomerRef, Document_.customerRef.getName(), 250);
	 	GridDataFactory.fillDefaults().hint(400, SWT.DEFAULT).applyTo(txtCustomerRef);
				
		// The extra settings composite contains additional fields like
		// the no-Vat widget or a reference to the invoice
		Composite xtraSettingsComposite = new Composite(top, SWT.BORDER);
		GridLayoutFactory.fillDefaults().margins(10, 10).numColumns(2).applyTo(xtraSettingsComposite);
		GridDataFactory.fillDefaults().span(1, 2).minSize(250, SWT.DEFAULT).align(SWT.FILL, SWT.BOTTOM).grab(true, false).applyTo(xtraSettingsComposite);
		
		// Consultant label
		Label labelConsultant = new Label(xtraSettingsComposite, SWT.NONE);
		//T: Document Editor - Label Consultant
		labelConsultant.setText(msg.editorDocumentFieldConsultant);
		//T: Tool Tip Text
		labelConsultant.setToolTipText(msg.editorDocumentFieldConsultantTooltip);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelConsultant);
		
		// Consultant
		txtConsultant = new Text(xtraSettingsComposite, SWT.BORDER);
		txtConsultant.setToolTipText(labelConsultant.getToolTipText());
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(txtConsultant);
		bindModelValue(document, txtConsultant, Document_.consultant.getName(), 250);
		
		boolean useOrderDate = (documentType != DocumentType.ORDER);

		// Service date
		Label labelServiceDate = new Label(useOrderDate ? xtraSettingsComposite : invisible, SWT.NONE);
		//T: Label Service Date
		labelServiceDate.setText(msg.editorDocumentFieldServicedate);
		//T: Tool Tip Text
		labelServiceDate.setToolTipText(msg.editorDocumentFieldServicedateTooltip);
		
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelServiceDate);

		// Service date
		dtServiceDate = new CDateTime(useOrderDate ? xtraSettingsComposite : invisible, CDT.BORDER | CDT.DROP_DOWN);
		dtServiceDate.setToolTipText(labelServiceDate.getToolTipText());
		GridDataFactory.fillDefaults().minSize(80, SWT.DEFAULT).grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(dtServiceDate);
		// Set the dtDate widget to the documents date
		bindModelValue(document, dtServiceDate, Document_.serviceDate.getName());

		// Order date
		Label labelOrderDate = new Label(useOrderDate ? xtraSettingsComposite : invisible, SWT.NONE);
		if (documentType == DocumentType.OFFER) {
			//T: Label in the document editor
			labelOrderDate.setText(msg.editorDocumentFieldRequestdate);
			//T: Tool Tip Text
			labelOrderDate.setToolTipText(msg.editorDocumentFieldRequestdateTooltip);
		} else {
			//T: Label in the document editor
			labelOrderDate.setText(msg.editorDocumentFieldOrderdate);
			//T: Tool Tip Text
			labelOrderDate.setToolTipText(msg.editorDocumentFieldOrderdateTooltip);
		}

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelOrderDate);
		
		// Order date
		dtOrderDate = new CDateTime(useOrderDate ? xtraSettingsComposite : invisible, CDT.BORDER | CDT.DROP_DOWN);
		dtOrderDate.setToolTipText(labelOrderDate.getToolTipText());
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(dtOrderDate);
		// Set the dtDate widget to the documents date
		bindModelValue(document, dtOrderDate, Document_.orderDate.getName());

		// If "orderdate" is not set, use "webshopdate"
		Date orderDateString = document.getOrderDate() == null ? document.getWebshopDate() : document.getOrderDate();
		dtOrderDate.setSelection(orderDateString);

		// A reference to the invoice
		Label labelInvoiceRef = new Label(documentType.hasInvoiceReference() ? xtraSettingsComposite : invisible, SWT.NONE);
		//T: Label in the document editor
		labelInvoiceRef.setText(msg.editorDocumentFieldInvoice);
		//T: Tool Tip Text
		labelInvoiceRef.setToolTipText(msg.editorDocumentFieldInvoiceTooltip);

		GridDataFactory.swtDefaults().align(SWT.END, SWT.BOTTOM).applyTo(labelInvoiceRef);
		txtInvoiceRef = new Text(documentType.hasInvoiceReference() ? xtraSettingsComposite : invisible, SWT.BORDER);
		txtInvoiceRef.setToolTipText(labelInvoiceRef.getToolTipText());
		Invoice invoiceId = document.getInvoiceReference();
		if (invoiceId != null) {
			txtInvoiceRef.setText(invoiceId.getName());
		} else {
			txtInvoiceRef.setText("---");
		}
		txtInvoiceRef.setEditable(false);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(txtInvoiceRef);

		// This document should use a VAT of 0%
		Label labelNoVat = new Label(documentType.hasPrice() ? xtraSettingsComposite : invisible, SWT.NONE);
		//T: Label in the document editor
		labelNoVat.setText(msg.commonFieldVat);
		//T: Tool Tip Text
		labelNoVat.setToolTipText(msg.editorDocumentZerovatTooltip);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelNoVat);

		// combo list with all 0% VATs
		comboViewerNoVat = new ComboViewer(documentType.hasPrice() ? xtraSettingsComposite : invisible, SWT.BORDER);
		comboViewerNoVat.getCombo().setToolTipText(labelNoVat.getToolTipText());
		comboViewerNoVat.setContentProvider(new EntityComboProvider());
		comboViewerNoVat.setLabelProvider(new EntityLabelProvider());
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(comboViewerNoVat.getCombo());
		
		comboViewerNoVat.addSelectionChangedListener(new ISelectionChangedListener() {

			// A combo entry is selected
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				IStructuredSelection structuredSelection = (IStructuredSelection) selection;
				if (!structuredSelection.isEmpty()) {

					// get first element ...
					Object firstElement = structuredSelection.getFirstElement();
					VAT dataSetVat = (VAT) firstElement;

					// get the "no-VAT" values
					if (dataSetVat != null) {
						noVat = true;
						noVatName = dataSetVat.getName();
						noVatDescription = dataSetVat.getDescription();
					}
					else {
						noVat = false;
						noVatName = "";
						noVatDescription = "";
					}

					// set all items to 0%
					itemListTable.setItemsNoVat(noVat);
					tableViewerItems.refresh();

					// recalculate the total sum
					calculate();
				}
			}
		});

		// Selects the no VAT entry
		comboViewerNoVat.setInput(vatDao.findNoVATEntries());
		if (noVat) {
			comboViewerNoVat.getCombo().setText(noVatName);
		} else {
		    comboViewerNoVat.getCombo().select(0);
		}

		// Group with tool bar with buttons to generate
		// a new document from this document
		Group copyGroup = new Group(top, SWT.SHADOW_ETCHED_OUT);
		
		//T: Document Editor
		//T: Label Group box to create a new document based on this one.
		copyGroup.setText(msg.editorDocumentCreateduplicate);
		GridLayoutFactory.fillDefaults().applyTo(copyGroup);
		GridDataFactory.fillDefaults().align(SWT.END, SWT.BOTTOM).span(1, 2).grab(true, false).applyTo(copyGroup);

		// Toolbar
        ToolBar toolBarDuplicateDocument = new ToolBar(copyGroup, SWT.FLAT | SWT.WRAP);
		GridDataFactory.fillDefaults().align(SWT.END, SWT.TOP).applyTo(toolBarDuplicateDocument);

        String tooltipPrefix = msg.commandNewTooltip + " ";
        
		// Add buttons, depending on the document type
		switch (documentType) {
		case OFFER:
	        createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewOrderName,
	                tooltipPrefix + msg.mainMenuNewOrder, Icon.ICON_ORDER_NEW.getImage(IconSize.ToolbarIconSize)
	                , createCommandParams(DocumentType.ORDER));
	        createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewInvoiceName,
	                tooltipPrefix + msg.mainMenuNewInvoice, Icon.ICON_INVOICE_NEW.getImage(IconSize.ToolbarIconSize)
	                , createCommandParams(DocumentType.INVOICE));
        createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewDocumentProformaName, 
                tooltipPrefix + msg.mainMenuNewProforma, Icon.ICON_LETTER_NEW.getImage(IconSize.ToolbarIconSize)
                , createCommandParams(DocumentType.PROFORMA));
			break;
		case ORDER:
	        createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewConfirmationName,
	                tooltipPrefix + msg.mainMenuNewConfirmation, Icon.ICON_CONFIRMATION_NEW.getImage(IconSize.ToolbarIconSize)
	                , createCommandParams(DocumentType.CONFIRMATION));
	        createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewDeliveryName,
	                tooltipPrefix + msg.mainMenuNewDeliverynote, Icon.ICON_DELIVERY_NEW.getImage(IconSize.ToolbarIconSize)
	                , createCommandParams(DocumentType.DELIVERY));
            createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewInvoiceName,
                    tooltipPrefix + msg.mainMenuNewInvoice, Icon.ICON_INVOICE_NEW.getImage(IconSize.ToolbarIconSize)
                    , createCommandParams(DocumentType.INVOICE));
            createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewDocumentProformaName, 
                    tooltipPrefix + msg.mainMenuNewProforma, Icon.ICON_LETTER_NEW.getImage(IconSize.ToolbarIconSize)
                    , createCommandParams(DocumentType.PROFORMA));
			break;
		case CONFIRMATION:
            createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewDeliveryName,
                    tooltipPrefix + msg.mainMenuNewDeliverynote, Icon.ICON_DELIVERY_NEW.getImage(IconSize.ToolbarIconSize)
                    , createCommandParams(DocumentType.DELIVERY));
            createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewInvoiceName,
                    tooltipPrefix + msg.mainMenuNewInvoice, Icon.ICON_INVOICE_NEW.getImage(IconSize.ToolbarIconSize)
                    , createCommandParams(DocumentType.INVOICE));
            createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewDocumentProformaName, 
                    tooltipPrefix + msg.mainMenuNewProforma, Icon.ICON_LETTER_NEW.getImage(IconSize.ToolbarIconSize)
                    , createCommandParams(DocumentType.PROFORMA));
			break;
		case INVOICE:
            createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewDeliveryName,
                    tooltipPrefix + msg.mainMenuNewDeliverynote, Icon.ICON_DELIVERY_NEW.getImage(IconSize.ToolbarIconSize)
                    , createCommandParams(DocumentType.DELIVERY));
            createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewCreditName, 
                    tooltipPrefix + msg.mainMenuNewCredit, Icon.ICON_CREDIT_NEW.getImage(IconSize.ToolbarIconSize)
                    , createCommandParams(DocumentType.CREDIT));
	        createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewDocumentDunningName, 
	                tooltipPrefix + msg.mainMenuNewDunning, Icon.ICON_DUNNING_NEW.getImage(IconSize.ToolbarIconSize)
	                , createCommandParams(DocumentType.DUNNING));
			break;
		case DELIVERY:
            createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewInvoiceName,
                    tooltipPrefix + msg.mainMenuNewInvoice, Icon.ICON_INVOICE_NEW.getImage(IconSize.ToolbarIconSize)
                    , createCommandParams(DocumentType.INVOICE));
			break;
		case DUNNING:
		    String action = String.format("%s%s.%s", tooltipPrefix, Integer.toString(dunningLevel + 1),msg.mainMenuNewDunning);
	        createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, action, 
	                tooltipPrefix + msg.mainMenuNewDunning, Icon.ICON_DUNNING_NEW.getImage(IconSize.ToolbarIconSize)
	                , createCommandParams(DocumentType.DUNNING));
			break;
		case PROFORMA:
            createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewInvoiceName,
                    tooltipPrefix + msg.mainMenuNewInvoice, Icon.ICON_INVOICE_NEW.getImage(IconSize.ToolbarIconSize)
                    , createCommandParams(DocumentType.INVOICE));
			break;
		default:
			copyGroup.setVisible(false);
			break;
		}
		
		// Composite that contains the address label and the address icon
		Composite addressComposite = new Composite(top, SWT.NONE | SWT.RIGHT);
		GridLayoutFactory.fillDefaults().applyTo(addressComposite);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addressComposite);

		// Address label
		Label labelAddress = new Label(addressComposite, SWT.NONE | SWT.RIGHT);
		//T: Label in the document editor
		labelAddress.setText(msg.editorContactLabelAddress);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(labelAddress);

		// Address icon
		Label selectAddressButton = new Label(addressComposite, SWT.NONE | SWT.RIGHT);
		//T: Tool Tip Text
		selectAddressButton.setToolTipText(msg.dialogSelectaddressTooltip);
		selectAddressButton.setImage(Icon.DOCEDIT_CONTACT_LIST.getImage(IconSize.DocumentIconSize));
		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(selectAddressButton);
		selectAddressButton.addMouseListener(new MouseAdapter() {

			// Open the address dialog if the icon is clicked.
			public void mouseDown(MouseEvent e) {
			    /*
			     * This code searches for the dialog part in the Application model
			     * and opens it. The content of this dialog is taken from ContactListTable.
			     * The part in the Application model has an additional context entry
			     * "fakturama.datatable.contacts.clickhandler" which is for the ContactListTable
			     * part to decide which action should be taken on double click.
			     * Once an entry is selected the address (or, more specific, the Contact object id)
			     * is posted via EventBroker. There the handleDialogSelection method comes on stage.
			     * This method handles the selected address and stores the value in the (intermediate)
			     * Text field.
			     * The (old) SelectContactDialog therefore is obsolete.
			     */
			    MDialog dialog = (MDialog) modelService.find("fakturama.dialog.select.contact", application);
			    dialog.setToBeRendered(true);
			    dialog.setVisible(true);
//			    dialog.setOnTop(true);
			    dialog.getTransientData().put(DOCUMENT_ID, document.getName());
			    modelService.bringToTop(dialog);
			}
		});

		// Address icon
		Label newAddressButton = new Label(addressComposite, SWT.NONE | SWT.RIGHT);
		//T: Tool Tip Text
		newAddressButton.setToolTipText(msg.commandOpenContactTooltip);
		newAddressButton.setImage(Icon.DOCEDIT_CONTACT_PLUS.getImage(IconSize.DocumentIconSize));
		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(newAddressButton);
		newAddressButton.addMouseListener(new MouseAdapter() {

			// Open the address dialog, if the icon is clicked.
			public void mouseDown(MouseEvent e) {

//				// Sets the editors input
//				UniDataSetEditorInput input = new UniDataSetEditorInput(thisDocumentEditor);
//
//				// Open a new Contact Editor 
//				try {
//					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().openEditor(input, ContactEditor.ID);
//				}
//				catch (PartInitException e1) {
//				    log.error(e1, "Error opening Editor: " + ContactEditor.ID);
//				}
			    
			    // TODO Wenn hier eine Adresse ausgewählt wurde, muß manualAddress auf null gesetzt werden!
                getMDirtyablePart().setDirty(true);
                document.setManualAddress(null);
//                document.setContact(...);

			}
		});

		// Composite that contains the address and the warning icon
		addressAndIconComposite = new Composite(top, SWT.NONE | SWT.RIGHT);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(addressAndIconComposite);
		GridDataFactory.fillDefaults().minSize(180, 80).align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(addressAndIconComposite);

		// The address field
		txtAddress = new Text(addressAndIconComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		if (documentType == DocumentType.DELIVERY) {
			txtAddress.setText(ContactUtil.getInstance(preferences).getAddressAsString(document.getDeliveryContact()));
		} else {
			txtAddress.setText(ContactUtil.getInstance(preferences).getAddressAsString(document.getContact()));
		}
		
		/*
		 * TODO Wenn die Adresse händisch geändert wird, muß sie in manualAddress kopiert werden (am 
		 * besten nach dem Verlassen des Widgets). Falls ein Kontakt ausgewählt wurde, muß dieses
		 * Feld wieder auf null gesetzt werden und die Contact-Verknüpfung aktualisiert werden.
		 * Von daher geht hier ein simples "superviceControl" nicht.
		 */
//		superviceControl(txtAddress, 250);
		
		txtAddress.addFocusListener(new FocusAdapter() {
		    @Override
		    public void focusLost(FocusEvent e) {
		        if(!ContactUtil.getInstance(preferences).getAddressAsString(document.getContact()).contentEquals(txtAddress.getText())) {
		            getMDirtyablePart().setDirty(true);
		            document.setManualAddress(txtAddress.getText());
		            document.setContact(null);
		        }
		    }
        });
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(txtAddress);

		// Add the attention sign if the delivery address is not equal to the billing address
		differentDeliveryAddressIcon = new Label(addressAndIconComposite, SWT.NONE);
		differentDeliveryAddressIcon.setImage(Icon.ICON_WARNING.getImage(IconSize.ToolbarIconSize));
		
		showHideWarningIcon();

/* * * * * * * * * * * * *  here the items list table is created * * * * * * * * * * * * */ 
		// Add the item table, if the document is one with items.
		if (documentType.hasItems()) {
		    ItemListBuilder itemListBuilder = ContextInjectionFactory.make(ItemListBuilder.class, context);
		    itemListTable = itemListBuilder
		        .withParent(top)
		        .withDocument(document)
		        .withNetGross(netgross)
		        .withUseGross(useGross)
//		        .withContainer(this)
		        .build();
		}
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  * * * * * * * * * * * */ 

		// Container for the message label and the add button
		Composite addMessageButtonComposite = new Composite(top, SWT.NONE | SWT.RIGHT);
		GridLayoutFactory.fillDefaults().applyTo(addMessageButtonComposite);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addMessageButtonComposite);

		// The message label
		Label messageLabel = new Label(addMessageButtonComposite, SWT.NONE);
		if (documentType.hasItems()) {
			//T: Document Editor Label for the text field under the item table.
			messageLabel.setText(msg.editorDocumentFieldRemarks);
			//T: Tool Tip Text
			messageLabel.setToolTipText(msg.editorDocumentFieldRemarksTooltip);

		}	
		else {
			//T: Document Editor Label for the text field, if there is no item table
			messageLabel.setText(msg.commonFieldText);
			//T: Tool Tip Text
			messageLabel.setToolTipText(msg.editorDocumentFieldCommentTooltip);
		}

		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(messageLabel);

		// The add message button
		Label addMessageButton = new Label(addMessageButtonComposite, SWT.NONE);
		//T: Tool Tip Text
		addMessageButton.setToolTipText(msg.editorDocumentSelecttemplateTooltip);
		addMessageButton.setImage(Icon.DOCEDIT_LIST.getImage(IconSize.DocumentIconSize));
		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addMessageButton);
		addMessageButton.addMouseListener(new MouseAdapter() {

			// Open the text dialog and select a text
			public void mouseDown(MouseEvent e) {
				
				//T: Document Editor
				//T: Title of the dialog to select a text
//				SelectTextDialog dialog = new SelectTextDialog(msg.editorDocumentSelecttextTitle);
                MDialog dialog = (MDialog) modelService.find("fakturama.dialog.selecttext", application);
                dialog.setToBeRendered(true);
                dialog.setVisible(true);
                dialog.setOnTop(true);
                modelService.bringToTop(dialog);

                // handling of adding a new list item is done via event handling in DocumentEditor
			}
		});

		int noOfMessageFields = preferences.getInt(Constants.PREFERENCES_DOCUMENT_MESSAGES, 1);
		
		if (noOfMessageFields < 1)
			noOfMessageFields = 1;
		if (noOfMessageFields > 3)
			noOfMessageFields = 3;
		
		// Container for 1..3 message fields
		Composite messageFieldsComposite = new Composite(top,SWT.NONE );
		GridLayoutFactory.fillDefaults().applyTo(messageFieldsComposite);
		
		// Add a multi line text field for the message.
		txtMessage = new Text(messageFieldsComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
//		txtMessage.setText(DataUtils.getInstance().makeOSLineFeeds(document.getMessage()));
		txtMessage.setToolTipText(messageLabel.getToolTipText());
		
		GridDataFactory.defaultsFor(txtMessage).minSize(80, 50).applyTo(txtMessage);
//		GridDataFactory.fillDefaults().grab(true, true).applyTo(txtMessage);
		bindModelValue(document, txtMessage, Document_.message.getName(), 10000);

		if (noOfMessageFields >= 2) {
			// Add a multi line text field for the message.
			txtMessage2 = new Text(messageFieldsComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
//			txtMessage2.setText(DataUtils.getInstance().makeOSLineFeeds(document.getMessage2()));
			
			GridDataFactory.defaultsFor(txtMessage2).minSize(80, 50).applyTo(txtMessage2);
			txtMessage2.setToolTipText(messageLabel.getToolTipText());
//			GridDataFactory.fillDefaults().grab(true, true).applyTo(txtMessage2);
			bindModelValue(document, txtMessage2, Document_.message2.getName(), 10000);
		}
		if (noOfMessageFields >= 3) {
			// Add a multi line text field for the message.
			txtMessage3 = new Text(messageFieldsComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
//			txtMessage3.setText(DataUtils.getInstance().makeOSLineFeeds(document.getMessage3()));
			txtMessage3.setToolTipText(messageLabel.getToolTipText());
			
			GridDataFactory.defaultsFor(txtMessage3).minSize(80, 50).applyTo(txtMessage3);
//			GridDataFactory.fillDefaults().grab(true, true).applyTo(txtMessage3);
			bindModelValue(document, txtMessage3, Document_.message3.getName(), 10000);
		}
		
		// Set the tab order
		if (documentType.hasInvoiceReference())
			setTabOrder(txtAddress, txtInvoiceRef);
		else if (documentType.hasPrice())
			setTabOrder(txtAddress, comboViewerNoVat.getControl());
		else if (documentType.hasItems())
			setTabOrder(txtAddress, tableViewerItems.getTable());
		else
			setTabOrder(txtAddress, txtMessage);

		// Depending on if the document has price values.
		if (!documentType.hasPrice()) {

			// If not, fill the columns for the price with the message field.
			if (documentType.hasItems())
				GridDataFactory.fillDefaults().hint(SWT.DEFAULT, noOfMessageFields*65).span(3, 1).grab(true, false).applyTo(messageFieldsComposite);
			else
				GridDataFactory.fillDefaults().span(3, 1).grab(true, true).applyTo(messageFieldsComposite);


//			Composite totalComposite = new Composite(top, SWT.NONE);
//			GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(totalComposite);
//			GridDataFactory.fillDefaults().align(SWT.END, SWT.TOP).grab(true, false).span(4, 5).applyTo(totalComposite);
//			// Total label
//			Label totalLabel = new Label(totalComposite, SWT.NONE);
//			//T: Document Editor - Total sum of this document 
//			totalLabel.setText(_("Total"));
//			GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(totalLabel);
//
//			// Total value
//			totalValue = new Label(totalComposite, SWT.NONE | SWT.RIGHT);
//			totalValue.setText("---");

			// Get the documents'shipping values.
			shipping = document.getShipping();
			shippingVat = document.getShipping().getShippingVat();
			shippingAutoVat = document.getShippingAutoVat();
			shippingVatDescription = document.getShipping().getShippingVat().getDescription();

//			GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.TOP).applyTo(totalValue);
//			calculate(Data.INSTANCE.getDocuments().getDatasetById(invoiceId));
			calculate(true);
		
		} else {

			if (documentType.canBePaid())
				GridDataFactory.fillDefaults().span(2, 1).hint(100, noOfMessageFields*65).grab(true, false).applyTo(messageFieldsComposite);
			else
				GridDataFactory.fillDefaults().span(2, 1).hint(100, noOfMessageFields*65).grab(true, true).applyTo(messageFieldsComposite);

			// Create a column for the documents subtotal, shipping and total
			Composite totalComposite = new Composite(top, SWT.NONE);
			GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(totalComposite);
			GridDataFactory.fillDefaults().align(SWT.END, SWT.TOP).grab(true, false).span(1, 2).applyTo(totalComposite);

			// Label sub total
			netLabel = new Label(totalComposite, SWT.NONE);
			// Set the total text
			netLabel.setText(getTotalText());
			GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(netLabel);

			// Sub total
			itemsSum = new Label(totalComposite, SWT.NONE | SWT.RIGHT);
			itemsSum.setText("---");
			GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.TOP).applyTo(itemsSum);


			if (preferences.getBoolean(Constants.PREFERENCES_DOCUMENT_USE_DISCOUNT_ALL_ITEMS, false) ||
					!DataUtils.getInstance().DoublesAreEqual(document.getItemsRebate(), 0.0)) {
				
				// Label discount
				Label discountLabel = new Label(totalComposite, SWT.NONE);
				//T: Document Editor - Label discount 
				discountLabel.setText(msg.commonFieldDiscount);
				//T: Tool Tip Text, xgettext:no-c-format
				discountLabel.setToolTipText(msg.editorDocumentDiscountTooltip);
				GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(discountLabel);
				
				// Discount field
				itemsDiscount = new FormattedText(totalComposite, SWT.BORDER | SWT.RIGHT);
				itemsDiscount.setFormatter(new PercentFormatter());
				itemsDiscount.setValue(document.getItemsRebate());
				itemsDiscount.getControl().setToolTipText(discountLabel.getToolTipText());
				GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.TOP).applyTo(itemsDiscount.getControl());

				// Set the tab order
				setTabOrder(txtMessage, itemsDiscount.getControl());

				// Recalculate, if the discount field looses the focus.
				itemsDiscount.getControl().addFocusListener(new FocusAdapter() {
					public void focusLost(FocusEvent e) {
						calculate();
//						itemsDiscount.setText(DataUtils.DoubleToFormatedPercent(DataUtils.StringToDoubleDiscount(itemsDiscount.getValue())));

					}
				});

				// Recalculate, if the discount is modified.
				itemsDiscount.getControl().addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						if (e.keyCode == 13) {
//							itemsDiscount.getValue();
							calculate();
						}
					}
				});

			}
					
			// Shipping composite contains label and combo.
			Composite shippingComposite = new Composite(totalComposite, SWT.NONE);
			GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(shippingComposite);
			GridDataFactory.fillDefaults().align(SWT.END, SWT.TOP).grab(true, false).applyTo(shippingComposite);

			// Shipping label
			Label shippingLabel = new Label(shippingComposite, SWT.NONE);
			//T: Document Editor - Label shipping 
			shippingLabel.setText(msg.editorDocumentFieldShipping);
			GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(shippingLabel);
			//T: Tool Tip Text
			shippingLabel.setToolTipText(msg.editorDocumentFieldShippingTooltip);

			// Shipping combo
			comboViewerShipping = new ComboViewer(shippingComposite, SWT.BORDER);
			comboViewerShipping.getCombo().setToolTipText(shippingLabel.getToolTipText());
			comboViewerShipping.setContentProvider(new EntityComboProvider());
			comboViewerShipping.setLabelProvider(new EntityLabelProvider());
			GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(comboViewerShipping.getCombo());
			comboViewerShipping.addSelectionChangedListener(new ISelectionChangedListener() {

				// If a new shipping is selected, recalculate the total
				// sum,
				// and update the shipping VAT.
				public void selectionChanged(SelectionChangedEvent event) {
					// Get the selected element.
					ISelection selection = event.getSelection();
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					if (!structuredSelection.isEmpty()) {
						// Get first selected element.
						Object firstElement = structuredSelection.getFirstElement();
						shipping = (Shipping) firstElement;

						// Update the shipping VAT
						shippingVat = shipping.getShippingVat();
						shippingVatDescription = shippingVat.getDescription();
						shippingAutoVat = shipping.getAutoVat();
						calculate();
					}
				}
			});

			// Fill the shipping combo with the shipping values.
			List<Shipping> allShippings = shippingsDAO.findAll();
            comboViewerShipping.setInput(allShippings);

			// Get the documents'shipping values.
			shipping = document.getShipping();
			shippingVat = document.getShipping().getShippingVat();
			shippingAutoVat = document.getShippingAutoVat();
			shippingVatDescription = shippingVat.getDescription();

	        UpdateValueStrategy shippingModel2Target = new UpdateValueStrategy();
	        shippingModel2Target.setConverter(new EntityConverter<Shipping>(Shipping.class));
	        
	        UpdateValueStrategy target2ShippingModel = new UpdateValueStrategy();
	        target2ShippingModel.setConverter(new StringToEntityConverter<Shipping>(allShippings, Shipping.class));
			// Set the combo
	        bindModelValue(document, comboViewerShipping.getCombo(), Document_.shipping.getName(), target2ShippingModel, shippingModel2Target);

			// Shipping value field
			shippingValue = new FormattedText(totalComposite, SWT.BORDER | SWT.RIGHT);
			shippingValue.setValue(shipping.getShippingValue());
			shippingValue.setFormatter(new MoneyFormatter());
			shippingValue.getControl().setToolTipText(shippingLabel.getToolTipText());
			GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.CENTER).applyTo(shippingValue.getControl());

			// Recalculate, if the discount field looses the focus.
			shippingValue.getControl().addFocusListener(new FocusAdapter() {

				public void focusLost(FocusEvent e) {
					changeShippingValue();
				}
			});

			// Recalculate, if the shipping is modified
			shippingValue.getControl().addKeyListener(new KeyAdapter() {
				public void keyPressed(KeyEvent e) {
					if (e.keyCode == 13) {
						changeShippingValue();
					}
				}
			});

// FIXME here the VALUE is meant, not the Name!!!			bindModelValue(document, shippingValue, Document_.shipping.getName(), 12);

			// VAT label
			Label vatLabel = new Label(totalComposite, SWT.NONE);
			//T: Document Editor - Label VAT 
			vatLabel.setText(msg.commonFieldValue);
			GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(vatLabel);

			// VAT value
			vatValue = new Label(totalComposite, SWT.NONE | SWT.RIGHT);
			vatValue.setText("---");
			GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.TOP).applyTo(vatValue);

			// Total label
			Label totalLabel = new Label(totalComposite, SWT.NONE);
			//T: Document Editor - Total sum of this document 
			totalLabel.setText(msg.commonFieldTotal);
			GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(totalLabel);

			// Total value
			totalValue = new Label(totalComposite, SWT.NONE | SWT.RIGHT);
			totalValue.setText("---");
			GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.TOP).applyTo(totalValue);

			// Create the "paid"-controls, only if the document type allows
			// this.
			if (documentType.canBePaid()) {

				// The paid label
				bPaid = new Button(top, SWT.CHECK | SWT.LEFT);
				if (BooleanUtils.toBoolean(document.getPaid())) {
					bPaid.setSelection(document.getPaid());
				}
				if (BooleanUtils.toBoolean(document.getDeposit())) {
					bPaid.setSelection(document.getDeposit());
					deposit = document.getPaidValue();
				}
				
				//T: Mark a paid document with this text.
				bPaid.setText(msg.documentOrderStatePaid);
				//T: Tool Tip Text
				bPaid.setToolTipText(msg.editorDocumentCheckboxPaidTooltip);
				
				GridDataFactory.swtDefaults().applyTo(bPaid);

				// Container for the payment and the paid state
				paidContainer = new Composite(top, SWT.NONE);
				GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(paidContainer);
				GridDataFactory.swtDefaults().span(2, 1).align(SWT.BEGINNING, SWT.CENTER).applyTo(paidContainer);

				// If the paid check box is selected ...
				bPaid.addSelectionListener(new SelectionAdapter() {

					// ... Recreate the paid composite
					public void widgetSelected(SelectionEvent e) {
						createPaidComposite(bPaid.getSelection(), bPaid.getSelection(), true);
					}
				});

				// Combo to select the payment
				comboPayment = new Combo(paidContainer, SWT.BORDER);
				comboViewerPayment = new ComboViewer(comboPayment);
				comboViewerPayment.setContentProvider(new EntityComboProvider());
				comboViewerPayment.setLabelProvider(new EntityLabelProvider());
				GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(comboPayment);

				// If a new payment is selected ...
				comboViewerPayment.addSelectionChangedListener(new ISelectionChangedListener() {

					// change the paymentId to the selected element
					public void selectionChanged(SelectionChangedEvent event) {

						// Get the selected element
						ISelection selection = event.getSelection();
						IStructuredSelection structuredSelection = (IStructuredSelection) selection;
						if (!structuredSelection.isEmpty()) {
							// Get first selected element.
							Object firstElement = structuredSelection.getFirstElement();
							Payment dataSetPayment = (Payment) firstElement;
							usePayment(dataSetPayment);
						}
					}
				});

				// Fill the payment combo with the payments
				List<Payment> allPayments = paymentsDao.findAll();
                comboViewerPayment.setInput(allPayments);

                UpdateValueStrategy paymentModel2Target = new UpdateValueStrategy();
                paymentModel2Target.setConverter(new EntityConverter<Payment>(Payment.class));
                
                UpdateValueStrategy target2PaymentModel = new UpdateValueStrategy();
                target2PaymentModel.setConverter(new StringToEntityConverter<Payment>(allPayments, Payment.class));
                bindModelValue(document, comboViewerPayment.getCombo(), Document_.payment.getName(), target2PaymentModel, paymentModel2Target);

				// Create a default paid composite with the document's
				// state for "paid"
				createPaidComposite(BooleanUtils.toBoolean(document.getPaid()), BooleanUtils.toBoolean(document.getDeposit()), false);

				// Set the combo
//				comboPayment.setText(document.getPayment().getDescription());

			}
		}

		updateUseGross(false);

		// Calculate the total sum
		if(documentType != DocumentType.DUNNING) {
			calculate();
		}
	}

    /**
     * 
     */
    protected Icon createDocumentIcon() {
        Icon icon = null;
        try {
		    switch (documentType) {
            case INVOICE:
                icon = Icon.ICON_INVOICE;
                break;
            case OFFER:
                icon = Icon.ICON_OFFER;
                break;
            case ORDER:
                icon = Icon.ICON_ORDER;
                break;
            case CREDIT:
                icon = Icon.ICON_CREDIT;
                break;
            case DUNNING:
                icon = Icon.ICON_DUNNING;
                break;
            case PROFORMA:
                icon = Icon.ICON_PROFORMA;
                break;
            case LETTER:
                icon = Icon.ICON_LETTER;
                break;
            case CONFIRMATION:
                icon = Icon.ICON_CONFIRMATION;
                break;
            case DELIVERY:
                icon = Icon.ICON_DELIVERY;
                break;
            default:
                icon = Icon.ICON_ORDER;
                break;
            }
		}
		catch (IllegalArgumentException e) {
			log.error(e, "Icon not found");
		}
        return icon;
    }

    /**
     * @param parent
     */
    protected void showOrderStatisticDialog(Composite parent) {
        // Show an info dialog, if this is a regular customer
        if (documentType == DocumentType.ORDER && getBooleanPreference(Constants.PREFERENCES_DOCUMENT_CUSTOMER_STATISTICS_DIALOG, true)) {
			CustomerStatistics customerStaticstics = ContextInjectionFactory.make(CustomerStatistics.class, context);
			
			if (getIntPreference(Constants.PREFERENCES_DOCUMENT_CUSTOMER_STATISTICS_COMPARE_ADDRESS_FIELD, 1) == 1) {
				customerStaticstics.setContact(document.getContact());
				customerStaticstics.setAddress(document.getManualAddress());
	            customerStaticstics.makeStatistics(true);
			} else {	
                customerStaticstics.setContact(document.getContact());
                customerStaticstics.makeStatistics(false);
			}
			
			if (customerStaticstics.isRegularCustomer()) {

				//T: Message Dialog
				MessageDialog.openInformation(parent.getShell(), 
						//T: Title of the customer statistics dialog
						msg.dialogMessageboxTitleInfo,
						document.getAddressFirstLine() + " " +
						//T: Part of the customer statistics dialog
						msg.dialogCustomerStatisticsPart1 + " "+ customerStaticstics.getOrdersCount().toString() + " " + 
						//T: Part of the customer statistics dialog
						msg.dialogCustomerStatisticsPart2 + "\n" + 
						//T: Part of the customer statistics dialog
						msg.dialogCustomerStatisticsPart3 + " " + customerStaticstics.getLastOrderDate()  + "\n" +
						//T: Part of the customer statistics dialog
						msg.dialogCustomerStatisticsPart4 + " " + customerStaticstics.getInvoices() + "\n" +
						//T: Part of the customer statistics dialog
						msg.dialogCustomerStatisticsPart5 +" " + DataUtils.getInstance().doubleToFormattedPrice(customerStaticstics.getTotal()));
			
			}
		}
    }

    private Map<String, Object> createCommandParams(DocumentType docType) {
        Map<String, Object> params = new HashMap<>();
        params.put(CallEditor.PARAM_EDITOR_TYPE, DocumentEditor.ID);
        params.put(CallEditor.PARAM_CATEGORY, docType.name());
        return params;
    }


    @Inject
    @org.eclipse.e4.core.di.annotations.Optional
    protected void handleDialogSelection(@UIEventTopic("DialogSelection/*") Event event) {
        if (event != null) {
            // the event has already all given params in it since we created them as Map
            String targetDocumentName= (String) event.getProperty(DOCUMENT_ID);
            // at first we have to check if the message is for us
            if(!StringUtils.equals(targetDocumentName, document.getName())) {
                // silently ignore this event
                return; 
            }
            
            String topic = StringUtils.defaultString(event.getTopic());
            String subTopic = "";
            String[] topicName = topic.split("/");
            if (topicName.length > 1) {
                subTopic = topicName[1];
            }

            switch (subTopic) {
            case "Contact":
                // in ContactListTable the selected id is put in as Long value
                Contact contact = contactDAO.findById((Long)event.getProperty(ContactListTable.SELECTED_CONTACT_ID));
                setAddress(contact);
                // If a Contact is selected the manualAddress fild has to be set to null!
                // 
                getMDirtyablePart().setDirty(true);
                document.setManualAddress(null);
                document.setContact(contact);
                txtAddress.setText(ContactUtil.getInstance(preferences).getAddressAsString(contact));
                break;
            case "Product":
                // select a product (for an item entry)
                // Get the array list of all selected elements
                @SuppressWarnings("unchecked")
                List<Long> selectedIds = (List<Long>)event.getProperty(ContactListTable.SELECTED_CONTACT_ID);
                List<Product> selectedProducts = productsDAO.findSelectedProducts(selectedIds);
                for (Product product : selectedProducts) {
                    DocumentItem newItem = modelFactory.createDocumentItem();
                    newItem.setName(product.getName());
                    newItem.setProduct(product);
                    newItem.setItemNumber(product.getItemNumber());
                    newItem.setDeleted(Boolean.FALSE);
                    newItem.setQuantity(documentType.getSign() * Double.valueOf(1));
                    newItem.setQuantityUnit(product.getQuantityUnit());
                    newItem.setDescription(product.getDescription());
                    newItem.setPrice(productUtil.getPriceByQuantity(product, newItem.getQuantity()));
                    newItem.setItemVat(product.getVat());
                    newItem.setPictureName(product.getPictureName());
                    newItem.setWeight(product.getWeight());

                    // Use the products description, or clear it
                    if (!getBooleanPreference(Constants.PREFERENCES_DOCUMENT_COPY_PRODUCT_DESCRIPTION_FROM_PRODUCTS_DIALOG, true)) {
                        newItem.setDescription("");
                    }
                    itemListTable.addNewItem(newItem);
                }

                //          if (newItem!= null)
                //              tableViewerItems.reveal(newItem);
                calculate();
                break;
            case "Delivery":
                // select a delivery note for creating a collective invoice 
                @SuppressWarnings("unchecked")
                List<Delivery> selectedDeliveries = documentsDAO.findSelectedDeliveries((List<Long>)event.getProperty(ContactListTable.SELECTED_CONTACT_ID));
                // Get the array list of all selected elements
                for (Delivery deliveryNote : selectedDeliveries) {
                            // Get all items by ID from the item string
                            List<DocumentItem> itemsString = deliveryNote.getItems();
                            // And copy the item to a new one
                            DocumentItem newItem = modelFactory.createDocumentItem();
                            // Add the new item
                            itemsString.forEach(item -> itemListTable.addNewItem(newItem));
                            
                            // Put the number of the delivery note in a new line of the message field
                            if (getBooleanPreference(Constants.PREFERENCES_DOCUMENT_ADD_NR_OF_IMPORTED_DELIVERY_NOTE, true)) {
                                String dNName = deliveryNote.getName();
                                
                                if (!txtMessage.getText().isEmpty())
                                    dNName = System.lineSeparator() + dNName;
                                txtMessage.setText(txtMessage.getText() + dNName);
                            }
                            
                            // Set the delivery notes reference to this invoice
                            long documentID = document.getId();
                            // If the document has no id, collect the imported 
                            // delivery notes in a list.
                            if (documentID > 0) {
                                
                                // Set the reference of the imported delivery note to
                                // this invoice
                                deliveryNote.setInvoiceReference((Invoice) document);
                                try {
                                    documentsDAO.save(deliveryNote);
                                }
                                catch (SQLException e) {
                                    // TODO Auto-generated catch block
                                    e.printStackTrace();
                                }
                                
                                // Change also the transaction id of the imported delivery note
                                // TODO
//                                Transaction.mergeTwoTransactions(document, deliveryNote);
                            }
                            else
                                importedDeliveryNotes.add(deliveryNote.getId());
                }
//                    tableViewerItems.refresh();
//                    if (newItem!= null)
//                        tableViewerItems.reveal(newItem);
                    calculate();
                break;
            case "Text":
              TextModule text;
                  text = (TextModule) event.getProperty(ContactListTable.SELECTED_CONTACT_ID);
                  
                  // Get the message field with the focus
                  Text selecteMessageField = txtMessage;

                  // Get the message field with the focus
                  if (txtMessage2 != null)
                      if (txtMessage2.isFocusControl())
                          selecteMessageField = txtMessage2;
                  if (txtMessage3 != null)
                      if (txtMessage3.isFocusControl())
                          selecteMessageField = txtMessage3;
                  
                  // Insert the selected text in the message text
                  if ((text != null) && (selecteMessageField != null)) {
                      int begin = selecteMessageField.getSelection().x;
                      int end = selecteMessageField.getSelection().y;
                      String s = selecteMessageField.getText();
                      String s1 = s.substring(0, begin);
                      String s2 = text.getText();
                      String s3 = s.substring(end, s.length());

                      selecteMessageField.setText(s1 + s2 + s3);

                      selecteMessageField.setSelection(s1.length() + s2.length());
                  }
                break;
            default:
                break;
            }
        }
    }

    private int getIntPreference(String preference, int defaultValue) {
        return preferences.getInt(preference, eclipseDefaultPrefs.getInt(preference, defaultValue));
    }

    private boolean getBooleanPreference(String preference, boolean defaultValue) {
        return preferences.getBoolean(preference, eclipseDefaultPrefs.getBoolean(preference, defaultValue));
    }
    

    /**
     * Searches for the standard {@link Shipping} entry. 
     */
    public Shipping lookupDefaultShippingValue() {
        long stdID = 1L;
        Shipping retval = null;

        // Get the ID of the standard entity from preferences
        try {
            stdID = preferences.getLong(getDefaultEntryKey(), 1L);
        } catch (NumberFormatException | NullPointerException e) {
            stdID = 1L;
        } finally {
            retval = shippingsDAO.findById(stdID);
        }
        return retval;
    }


    private void createToolItem(final ToolBar toolBar, final String commandId, 
            final String commandName, final String tooltip, final Image iconImage,
            Map<String, Object> params) {
        
        ToolItem item = new ToolItem(toolBar, SWT.PUSH);
        final ParameterizedCommand pCmd = cmdService.createCommand(commandId, params);
        try {
            item.setText(commandName != null ? commandName : pCmd.getCommand().getName());
            item.setToolTipText((tooltip != null) ? tooltip : pCmd.getCommand().getDescription());
            item.setEnabled(pCmd.getCommand().isEnabled());
            item.setData(TOOLITEM_COMMAND, pCmd);
        }
        catch (NotDefinedException e1) {
            log.error(e1, "Fehler! ");
        }
        item.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                if (handlerService.canExecute(pCmd)) {
                    handlerService.executeHandler(pCmd);
                } else {
                    MessageDialog.openInformation(toolBar.getShell(),
                            "Action Info", "current action can't be executed!");
                }
            }
        });
        item.setImage(iconImage);
    }

//	/**
//	 * Set the focus to the top composite.
//	 * 
//	 * @see com.sebulli.fakturama.editors.Editor#setFocus()
//	 */
//	@Focus
//	public void setFocus() {
//		if(top != null) 
//			top.setFocus();
//	}

	/**
	 * Test, if there is a document with the same number
	 * 
	 * @return TRUE, if one with the same number is found
	 */
	private boolean thereIsOneWithSameNumber() {
		// Letters do not have to be checked
		if (documentType == DocumentType.LETTER)
			return false;

		// Cancel, if there is already a document with the same ID
		Document testDoc = documentsDAO.findByName(txtName.getText());
		if (testDoc != null) {
			// Display an error message
		    MessageDialog.openError(top.getShell(), msg.editorDocumentErrorDocnumberTitle, msg.editorDocumentDialogWarningDocumentexists+ " " + txtName.getText());
			return true;
		}

		return false;
	}
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
    
	@Override
	protected String getEditorID() {
	    return documentType.getTypeAsString();
	}
	
    @Override
    protected MDirtyable getMDirtyablePart() {
        return part;
    }

    /**
     * This method is for setting the dirty state to <code>true</code>. This happens
     * if e.g. the items list has changed. It is initiated if a certain event occurs.
     */
    @Inject
    @org.eclipse.e4.core.di.annotations.Optional
    protected void handleAddItemToList(@UIEventTopic(EDITOR_ID + "/itemChanged") Event event) {
        if (event != null) {
            // the event has already all given params in it since we created them as Map
            String targetDocumentName= (String) event.getProperty(DOCUMENT_ID);
            // at first we have to check if the message is for us
            if(!StringUtils.equals(targetDocumentName, document.getName())) {
                // silently ignore this event
                return; 
            }
       getMDirtyablePart().setDirty(true);
    }}
}
