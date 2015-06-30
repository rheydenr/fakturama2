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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
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
import org.javamoney.moneta.Money;
import org.osgi.service.event.Event;

import com.sebulli.fakturama.calculate.CustomerStatistics;
import com.sebulli.fakturama.calculate.DocumentSummaryCalculator;
import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.dao.ShippingsDAO;
import com.sebulli.fakturama.dao.TextsDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.dialogs.SelectContactDialog;
import com.sebulli.fakturama.dto.DocumentItemDTO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.misc.OrderState;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Debitor;
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
import com.sebulli.fakturama.parts.widget.contentprovider.EntityComboProvider;
import com.sebulli.fakturama.parts.widget.contentprovider.HashMapContentProvider;
import com.sebulli.fakturama.parts.widget.formatter.MoneyFormatter;
import com.sebulli.fakturama.parts.widget.labelprovider.EntityLabelProvider;
import com.sebulli.fakturama.parts.widget.labelprovider.NumberLabelProvider;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;
import com.sebulli.fakturama.util.ContactUtil;
import com.sebulli.fakturama.util.DocumentTypeUtil;
import com.sebulli.fakturama.util.ProductUtil;
import com.sebulli.fakturama.views.datatable.contacts.ContactListTable;
import com.sebulli.fakturama.views.datatable.texts.TextListTable;


/**
 * The document editor for all types of document like letter, order,
 * confirmation, invoice, delivery, credit and dunning
 * 
 */
public class DocumentEditor extends Editor<Document> {

	public static final String DOCUMENT_RECALCULATE = "DOCUMENT.RECALCULATE";
    /** Editor's ID */
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
    protected EHandlerService handlerService;

    @Inject
    protected ECommandService commandService;
    
    @Inject
    private EModelService modelService;
    
//    @Inject
//    private EHelpService helpService;
    
    @Inject
    private MApplication application;
    
    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private IEclipseContext context;

    @Inject
    private Logger log;

    @Inject
    private IPreferenceStore preferences;

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
    private TextsDAO textsDAO;
    
    /**
     * the model factory
     */
    private final FakturamaModelFactory modelFactory = FakturamaModelPackage.MODELFACTORY;

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
//	private TableViewer tableViewerItems;
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
	private FormattedText itemsSum;
	private FormattedText itemsDiscount;
	private ComboViewer comboViewerShipping;
	private FormattedText shippingValue;
	//private Text depositValue;
	private FormattedText vatValue;
	private FormattedText totalValue;
	private Composite addressAndIconComposite;
	private Label differentDeliveryAddressIcon;
	private Label netLabel;

	// These flags are set by the preference settings.
	// They define, if elements of the editor are displayed, or not.
	private boolean useGross;

	// The type of this document
	private DocumentType documentType;

	// These are (non visible) values of the document
	
	/*
	 * Since the contact could be either a delivery or a billing contact we have to use an extra field for it.
	 */
	private Contact addressId = null;
	private boolean noVat;
	private String noVatName;
//	private String noVatDescription;
	private Payment payment;
//	private MonetaryAmount paidValue = Money.of(Double.valueOf(0.0), DataUtils.getInstance().getDefaultCurrencyUnit());
//	private int shippingId;
	private Shipping shipping = null;
//	private VAT shippingVat = null;
//	private String shippingVatDescription = "";
//	private ShippingVatType shippingAutoVat = ShippingVatType.SHIPPINGVATGROSS;
	private MonetaryAmount total = Money.of(Double.valueOf(0.0), DataUtils.getInstance().getDefaultCurrencyUnit());
	private MonetaryAmount deposit = Money.of(Double.valueOf(0.0), DataUtils.getInstance().getDefaultCurrencyUnit());
//	private MonetaryAmount finalPayment = FastMoney.MIN_VALUE;
	private int dunningLevel = Integer.valueOf(0);
	private int duedays;
	private String billingAddress = "";
	private String deliveryAddress = "";
//	private DocumentEditor thisDocumentEditor;
	private int netgross = DocumentSummary.ROUND_NOTSPECIFIED;

	// Action to print this document's content.
	// Print means: Export the document in an OpenOffice document
//	CreateOODocumentAction printAction;

	// defines, if the contact is new created
	private boolean newDocument;

	// If the customer is changed and this document displays no payment text,
	// use this variable to store the payment and due days
//	private Payment newPayment = null;
	private String newPaymentDescription = "";
	
	// Imported delivery notes. This list is used to
	// set an reference to this document, if it's an invoice.
	// The reference is not set during the import but later when the
	// document is saved. Because the the  document has an id to reference to.
	private List<Long> importedDeliveryNotes = new ArrayList<>();

    private ProductUtil productUtil;
    private DocumentItemListTable itemListTable;
    private CurrencyUnit currencyUnit;
    private DocumentSummary documentSummary;
    private ContactUtil contactUtil;
	
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

		boolean wasDirty = getMDirtyablePart().isDirty();

		if (newDocument) {
			// Check if the document number is the next one
			if (documentType != DocumentType.LETTER) {
				int result = setNextNr(txtName.getText(), Document_.name.getName());

				// It's not the next free ID
				if (result == ERROR_NOT_NEXT_ID) {
					// Display an error message
					MessageDialog.openError(top.getShell(),

					//T: Title of the dialog that appears if the document number is not valid.
					msg.editorDocumentErrorDocnumberTitle,
					
					//T: Text of the dialog that appears if the customer number is not valid.
					msg.editorDocumentErrorDocnumberNotnextfree + " " + getNextNr() + "\n" + 
					//T: Text of the dialog that appears if the number is not valid.
					msg.editorContactHintSeepreferences);
				}
			}
		}

		// Exit save if there is a document with the same number
		if (thereIsOneWithSameNumber()) {
			return;
		}

		// Always set the editor's data set to "undeleted"
		document.setDeleted(Boolean.FALSE);

		// Set the document type
		document.setBillingType(BillingType.get(documentType.getKey()));

		// If this is an order, use the date as order date
		if (documentType == DocumentType.ORDER) {
			document.setOrderDate(dtDate.getSelection());
		} else {
			document.setOrderDate(dtOrderDate.getSelection());
		}

	      String addressById = "";

		// Test, if the txtAddress field was modified
		// and write the content of the txtAddress to the documents address or
		// delivery address
		boolean addressModified = false;
		// if it's a delivery note, compare the delivery address
		if (documentType == DocumentType.DELIVERY) {
            if (!DataUtils.getInstance().MultiLineStringsAreEqual(contactUtil.getAddressAsString(document.getDeliveryContact()), txtAddress.getText())) {
				addressModified = true;
			}
			document.getDeliveryContact().getAddress().setManualAddress(DataUtils.getInstance().removeCR(txtAddress.getText()));

			// Use the delivery address if the billing address is empty
			if (billingAddress.isEmpty()) {
				billingAddress = DataUtils.getInstance().removeCR(txtAddress.getText());
			}
			if (addressId.getCustomerNumber() != null) {
				addressById = contactUtil.getAddressAsString(document.getDeliveryContact());
//    			document.setDeliveryContact(addressId);   // done by Databinding
			} else {
			    /*
			     * If no addressId was given (no contact selected) then we use
			     * the text field content for the manual address.
			     */
    			document.getBillingContact().getAddress().setManualAddress(billingAddress);
			}
		}
		else {
			if (!DataUtils.getInstance().MultiLineStringsAreEqual(contactUtil.getAddressAsString(document.getBillingContact()), txtAddress.getText())) {
				addressModified = true;
			}

			// Use the billing address if the delivery address is empty
			if (deliveryAddress.isEmpty()) {
				deliveryAddress = DataUtils.getInstance().removeCR(txtAddress.getText());
			}

			if (addressId.getCustomerNumber() != null) {
				addressById = contactUtil.getAddressAsString(document.getBillingContact());
//				document.setContact(addressId);  // done by Databinding
			} else {
	            document.getBillingContact().getAddress().setManualAddress(DataUtils.getInstance().removeCR(txtAddress.getText()));
	            document.getDeliveryContact().getAddress().setManualAddress(deliveryAddress);
			}
		}

		// Show a warning if the entered address is not similar to the address
		// of the document which is set by the address ID.
		if (addressId.getCustomerNumber() != null && addressModified) {
			if (DataUtils.getInstance().similarity(addressById, DataUtils.getInstance().removeCR(txtAddress.getText())) < 0.75) {
				MessageDialog.openWarning(top.getShell(),

				//T: Title of the dialog that appears if the document is assigned to  an other address.
				msg.editorDocumentErrorWrongcontactTitle,
				
				//T: Text of the dialog that appears if the document is assigned to  an other address.
				msg.editorDocumentErrorWrongcontactMsg1 + "\n\n" + addressById + "\n\n" + 
				msg.editorDocumentErrorWrongcontactMsg2);
			}
		}

		// Set the payment values depending on if the document is paid or not
		if (comboPayment != null) {
		    // this is done by databinding
//			document.setStringValueByKey("paymentdescription", comboPayment.getText());
		}
		// If this document contains no payment widgets, but..
		else {
			// the customer changed and so there is a new payment. Set it.
			if (!newPaymentDescription.isEmpty()) {
				document.getAdditionalInfo().setPaymentDescription(newPaymentDescription);
			}
		}

		if (bPaid != null) {
			String paymentText = "";

            if (bPaid.getSelection()) {
//                document.setPaid(Boolean.TRUE);
                //				document.setPayDate(dtPaidDate.getSelection());   // done by databinding
                //				document.setPaidValue(paidValue.getNumber().doubleValue());   // done by databinding
                deposit = Money.of(Double.valueOf(0.0), currencyUnit);
                if (document.getPaidValue() < total.getNumber().doubleValue()) {
                    deposit = Money.of(document.getPaidValue(), currencyUnit);
                    document.setDeposit(Boolean.TRUE);
                    document.setPaid(Boolean.FALSE);
                }
                if (documentType == DocumentType.INVOICE) {
                    // update dunnings
                    updateDunnings();
                }

                // Use the text for "paid" from the current payment
                if (document.getPayment() != null) {
                    if (document.getPaid()) {
                        paymentText = document.getPayment().getPaidText();
                    } else if (document.getDeposit()) {
                        paymentText = document.getPayment().getDepositText();
                    }
                }
            } else {
                document.setPaid(Boolean.FALSE);
                document.setPaidValue(Double.valueOf(0.0));
                document.setDeposit(Boolean.FALSE);

                // Use the text for "unpaid" from the current payment
                if (document.getPayment() != null) {
                    paymentText = document.getPayment().getUnpaidText();
                }
            }
			document.setDueDays(duedays);
			document.getAdditionalInfo().setPaymentText(paymentText);
		}
		// If this document contains no payment widgets, but..
		else {
			// the customer changed and so there is a new payment. Set it.
			if (!newPaymentDescription.isEmpty() && document.getPayment() != null) {
			    document.setDueDays(duedays);
			    document.setPaid(Boolean.FALSE);
			    document.setPaidValue(Double.valueOf(0.0));

				// Use the text for "unpaid" from the current payment
                if (document.getPayment() != null) {
                    document.getAdditionalInfo().setPaymentText(document.getPayment().getUnpaidText());
                }
			}
		}

//		// Set the shipping values     // done by databinding
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
		
		// Set the dunning level
		if(documentType == DocumentType.DUNNING) {
		    ((Dunning)document).setDunningLevel(dunningLevel);
		}

		// Create a new document ID, if this is a new document
		if (newDocument) {
		    try {
                document = documentsDAO.save(document);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		}
//		long documentId = document.getId();
		
		// Update the invoice references in all documents within the same transaction
		if(documentType.equals(DocumentType.INVOICE)) {
		    documentsDAO.updateInvoiceReferences((Invoice) document);
		
    		// Update the references in the delivery notes
    		documentsDAO.updateDeliveries(importedDeliveryNotes, (Invoice) document);
		}
		importedDeliveryNotes.clear();
		
		List<DocumentItem> items = itemListTable.getDocumentItemsListData()
		    .stream()
		    .map(dto -> dto.getDocumentItem())
		    .sorted(Comparator.comparing(DocumentItem::getPosNr))
		    .collect(Collectors.toList());
		document.setItems(new ArrayList<>(items));

		// Set the "addressfirstline" value to the first line of the
		// contact address
		if (addressId != null) {
			document.setAddressFirstLine(contactUtil.getNameWithCompany(document.getBillingContact()));
		}
		else {
			String s = DataUtils.getInstance().removeCR(txtAddress.getText());
			
			// Remove the "\n" if it was a "\n" as line break.
			s = s.split("\n")[0];
			
			document.setAddressFirstLine(s);
		}

		// Mark the (modified) document as "not printed"
		if (wasDirty) {
			document.setPrinted(false);
		}

		// If it is a new document,
		if (newDocument) {

//			// Create this in the data base
//			document = Data.INSTANCE.getDocuments().addNewDataSet(document);
//
//			// If it's an invoice, set the "invoiceid" to the ID.
//			// So all documents will inherit this ID
//			if ((documentType == DocumentType.INVOICE) && (document.getIntValueByKey("id") != document.getIntValueByKey("invoiceid"))) {
//				document.setIntValueByKey("invoiceid", document.getIntValueByKey("id"));
//				Data.INSTANCE.getDocuments().updateDataSet(document);
//			}

			// Now it is no longer new.
			newDocument = false;

			// Create a new editor input.
			// So it's no longer the parent data
//			this.setInput(new UniDataSetEditorInput(document));
		}
		else {

			// Do not create a new data set - just update the old one
//			Data.INSTANCE.getDocuments().updateDataSet(document);
		}
		
        try {
            document = documentsDAO.save(document);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

		//Set the editor's name
		this.part.setLabel(document.getName());

        // Refresh the table view of all documents
        evtBroker.post(EDITOR_ID, "update");
        
        // reset dirty flag
        getMDirtyablePart().setDirty(false);
	}

	/**
	 * Updates all {@link Dunning}s which are related to the current invoice.
	 */
	private void updateDunnings() {
	    documentsDAO.updateDunnings(document, bPaid.getSelection(), dtPaidDate.getSelection(), document.getPaidValue());
	}

	/**
	 * Initializes the editor. If an existing data set is opened, the local
	 * variable "document" is set to this data set. If the editor is opened to
	 * create a new one, a new data set is created and the local variable
	 * "document" is set to this one.
	 * 
	 * @param parent
	 *            The editor's parent Composite
	 */
    @PostConstruct
    public void init(Composite parent) {
        this.part = (MPart) parent.getData("modelElement");
//        this.context = part.getContext();
        this.productUtil = ContextInjectionFactory.make(ProductUtil.class, context);
        this.contactUtil = ContextInjectionFactory.make(ContactUtil.class, context);
        this.currencyUnit = DataUtils.getInstance().getCurrencyUnit(LocaleUtil.getInstance().getCurrencyLocale());
                
        String tmpObjId = (String) part.getProperties().get(CallEditor.PARAM_OBJ_ID);
        if (StringUtils.isNumeric(tmpObjId)) {
            Long objId = Long.valueOf(tmpObjId);
            // Set the editor's data set to the editor's input
            this.document = documentsDAO.findById(objId);
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
			if (documentType == DocumentType.NONE) {
				documentType = DocumentType.ORDER;
			}
			document = DocumentTypeUtil.createDocumentByType(documentType);
			if (duplicated) {
				document.setSourceDocument(parentDoc);
			}
            document.setBillingType(billingType);

			// Copy the entry "message", or reset it to ""
			if (!preferences.getBoolean(Constants.PREFERENCES_DOCUMENT_COPY_MESSAGE_FROM_PARENT)) {
				document.setMessage("");
				document.setMessage2("");
				document.setMessage3("");
			}

			// get the parents document type
			if (parentDoc != null) {
				documentTypeParent = DocumentTypeUtil.findByBillingType(parentDoc.getBillingType());
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
				shipping = lookupDefaultShippingValue();
//				shippingVat = shipping.getShippingVat();
//				shippingAutoVat = shipping.getAutoVat();
//				shippingVatDescription = shipping.getDescription();
				netgross = DocumentSummary.ROUND_NOTSPECIFIED;
				
				document.setShipping(shipping);
//				document.setDoubleValueByKey("shippingvat", shippingVat);
//				document.setStringValueByKey("shippingdescription", stdShipping.getStringValueByKey("description"));
				document.setShippingAutoVat(shipping.getAutoVat());
//				document.setStringValueByKey("shippingvatdescription", shippingVatDescription);
				
				// Default payment
				int paymentId = preferences.getInt(Constants.DEFAULT_PAYMENT);
                payment = paymentsDao.findById(paymentId);
                document.setPayment(payment);
//				document.setStringValueByKey("paymentdescription", Data.INSTANCE.getPayments().getDatasetById(paymentId).getStringValueByKey("description"));
				document.setDueDays(payment.getNetDays());
			}
			else {
				payment = document.getPayment();
				shipping = document.getShipping();
				total = Money.of(document.getTotalValue(), currencyUnit);
			}
			
			// set some dates
			Date today = Calendar.getInstance().getTime();
			document.setOrderDate(today);
			document.setDocumentDate(today);
//			document.setPayDate(today);
			document.setServiceDate(today);
//			document.setPaid(Boolean.FALSE);
			
			document.setNetGross(DocumentSummary.ROUND_NET_VALUES);

			// Get the next document number
			document.setName(getNextNr());

		}
		// If an existing document was opened ..
		else {

			// Get document type, set editorID
			documentType =  DocumentTypeUtil.findByBillingType(document.getBillingType());
			setEditorID(documentType.getTypeAsString());

			payment = document.getPayment();
			shipping = document.getShipping();

			// and the editor's part name
			part.setLabel(document.getName());

		}

		// These variables contain settings that are not in
		// visible SWT widgets.
		duedays = document.getDueDays();
		addressId = document.getBillingContact();
		
		noVat = document.getNoVatReference() != null;
		if(noVat) {
		    noVatName = document.getNoVatReference().getName();
//		    noVatDescription = document.getNoVatReference().getDescription();
		}
		netgross = document.getNetGross() != null ? document.getNetGross() : DocumentSummary.ROUND_NET_VALUES;
		
//		paidValue = document.getPaidValue() != null ? Money.of(document.getPaidValue(), currencyUnit) : Money.of(Double.valueOf(0.0), currencyUnit);
		if (dunningLevel <= 0) {
            if (document.getBillingType() == BillingType.DUNNING) {
            	dunningLevel = ((Dunning)document).getDunningLevel();
            } else {
                dunningLevel = 1;
            }
        }

        billingAddress = contactUtil.getAddressAsString(document.getBillingContact());
		deliveryAddress = contactUtil.getAddressAsString(document.getDeliveryContact());

        showOrderStatisticDialog(parent);
        
        // Get some settings from the preference store
        if (netgross == DocumentSummary.ROUND_NOTSPECIFIED) {
            useGross = (preferences.getInt(Constants.PREFERENCES_DOCUMENT_USE_NET_GROSS/*, DocumentSummary.ROUND_NET_VALUES*/) == DocumentSummary.ROUND_NET_VALUES);
        } else { 
            useGross = (netgross == DocumentSummary.ROUND_GROSS_VALUES);
        }
  
        createPartControl(parent);
	}

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
		if (document.getProgress() == OrderState.NONE.getState()) {
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
		if (!documentType.hasPrice() && !forceCalc) {
			return;
		}
		
		// Get the sign of this document ( + or -)
//		int sign = DocumentTypeUtil.findByBillingType(document.getBillingType()).getSign();
		
		// Get the discount value from the control element
		Double discount = Double.valueOf(0.0);
		if (itemsDiscount != null) {
			discount = (Double) itemsDiscount.getValue();
		}
		
		DocumentSummaryCalculator documentSummaryCalculator = new DocumentSummaryCalculator();
		// unwrap DocumentItemDTOs at first
		List<DocumentItem> docItems = new ArrayList<>();
		if(itemListTable != null) {
		    // itemListTable could be null if a document with hasPrice()=false is displayed
		    // (although it *could* have prices, but for this dialog they don't have to be 
		    // displayed). This is e.g. for Dunnings.
    		// don't use Lambdas because the List isn't initialized yet.
    		for (DocumentItemDTO item : itemListTable.getDocumentItemsListData()) {
    		    docItems.add(item.getDocumentItem());
            }
		} else {
		    docItems.addAll(document.getItems());
		}

		// Do the calculation
        documentSummary = documentSummaryCalculator.calculate(null, docItems,
                document.getShipping() != null ? document.getShipping().getShippingValue() : document.getShippingValue()/* * sign*/,
                shipping.getShippingVat(), document.getShipping() != null ? document.getShipping().getAutoVat() : document.getShippingAutoVat(), discount,
                document.getNoVatReference(), Double.valueOf(1.0), netgross, deposit);

		// Get the total result
		total = documentSummary.getTotalGross();

		// Set the items sum
		if (itemsSum != null) {
			if (useGross) {
				itemsSum.setValue(documentSummary.getItemsGross());
			} else {
				itemsSum.setValue(documentSummary.getItemsNet());
			}
		}

		// Set the shipping
        if (shippingValue != null) {
            // shippingValue is the only field which could be modified manually *and* per calculation
            // therefore we have to disable the ModifyListener at first.
            shippingValue.getControl().setData(CALCULATING_STATE, true);
            if (useGross) {
                shippingValue.setValue(documentSummary.getShippingGross());
            } else {
                shippingValue.setValue(documentSummary.getShippingNet());
            }
            // don't set it to "false" because the condition in ModifyListener checks for a null value!
            shippingValue.getControl().setData(CALCULATING_STATE, null);
        }

		// Set the VAT
		if (vatValue != null) {
			vatValue.setValue(documentSummary.getTotalVat());
		}

		// Set the total value
		if (totalValue != null) {
			totalValue.setValue(documentSummary.getTotalGross());
			totalValue.getControl().setToolTipText(msg.documentOrderStatePaid + ": " + document.getPaidValue());
		}
	}

	/**
	 * Get the total text, net or gross
	 * 
	 * @return
	 * 		The total text
	 */
	private String getTotalText () {
		if (useGross) {
			return msg.editorDocumentTotalgross;
		} else {
			//T: Document Editor - Label Total net 
			return msg.editorDocumentTotalnet;
		}
	}
	
	/**
	 * Change the document from net to gross or backwards 
	 */
	private void updateUseGross(boolean address_changed) {
		
		boolean oldUseGross = useGross;
		
		// Get some settings from the preference store
        if (netgross == DocumentSummary.ROUND_NOTSPECIFIED) {
            useGross = preferences.getInt(Constants.PREFERENCES_DOCUMENT_USE_NET_GROSS) == 1;
        } else {
            useGross = (netgross == DocumentSummary.ROUND_GROSS_VALUES);
        }
		
		// Use the customers settings instead, if they are set
		if (addressId != null && address_changed) {
			
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

		// Show a warning if the customer uses a different setting for net or gross
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
	 * Returns if this editor used net or gross values.
	 * 
	 * @return <code>true</code> if the document uses gross values.
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
		if (newShippingValue < 0) {
			newShippingValue = -newShippingValue;
		}

		// If the shipping value has changed:
		// Set the shippingAutoVat to net or gross, depending on the
		// settings of this editor.
		if (!DataUtils.getInstance().DoublesAreEqual(newShippingValue, shipping.getShippingValue())) {
			document.setShippingAutoVat(useGross ? ShippingVatType.SHIPPINGVATGROSS : ShippingVatType.SHIPPINGVATNET);
		}

		// Recalculate the sum
//		shipping = newShippingValue;
		document.setShippingValue(newShippingValue);
		document.setShipping(null);   // because we changed the Shipping value manually
		calculate();
	}

	/**
	 * Create a SWT composite witch contains other SWT widgets like the payment
	 * date or the paid value. Depending on the parameter "paid" widgets are
	 * created to set the due values or the paid values.
	 * 
	 * @param paid
	 *            If <code>true</code>, the widgets for "paid" are generated
	 */
	private void createPaidComposite(boolean paid, boolean isDeposit, boolean clickedByUser) {

		// If this widget exists, remove it to create a new one.
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
		} else if (isDeposit) {
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
//			paidValue = Money.of(Double.valueOf(0.0), currencyUnit);
		    document.setPaidValue(Double.valueOf(0.0));

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
			spDueDays.setSelection(duedays);
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
			dtIssueDate.setFormat(CDT.DATE_MEDIUM);
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
		dtPaidDate.setFormat(CDT.DATE_MEDIUM);
		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(dtPaidDate);

		// Set the paid date to the documents "paydate" parameter
		dtPaidDate.setSelection(document.getPayDate());

		bindModelValue(document, dtPaidDate, Document_.payDate.getName());

		// Create the widget for the value
		Label paidValueLabel = new Label(paidDataContainer, SWT.NONE);
		
		//T: Label in the document editor
		paidValueLabel.setText(msg.commonFieldValue);
		paidValueLabel.setToolTipText(msg.editorDocumentPaidvalue);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(paidValueLabel);

		// If it's the first time that this document is marked as paid
		// (if the value is 0.0), then also set the date to "today"
		if ((document.getPaidValue() == null || document.getPaidValue() == 0) && clickedByUser) {
		    document.setPaidValue(total.getNumber().doubleValue());
			dtPaidDate.setSelection(Calendar.getInstance().getTime());
		}
		FormattedText txtPayValue = new FormattedText(paidDataContainer, SWT.BORDER | SWT.RIGHT);
		txtPayValue.setFormatter(new MoneyFormatter());
		txtPayValue.getControl().setToolTipText(paidValueLabel.getToolTipText());
		bindModelValue(document, txtPayValue, Document_.paidValue.getName(), 32);
//		txtPayValue.setValue(paidValue);
		GridDataFactory.swtDefaults().hint(60, SWT.DEFAULT).applyTo(txtPayValue.getControl());
	}
	
    /**
     * Update the Issue Date widget with the date that corresponds to the due
     * date
     */
    void updateIssueDate() {
        // Add date and due days and set the issue date to the sum.
        if (dtIssueDate != null && dtDate.getSelection() != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(dtDate.getSelection());
            //	    LocalDate localDate = LocalDate.of(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            //	    localDate.plusDays(spDueDays.getSelection());
            //	    Date.from(localDate.???)
            //		GregorianCalendar calendar = new GregorianCalendar(dtDate.getYear(), dtDate.getMonth(), dtDate.getDay());
            calendar.add(Calendar.DAY_OF_MONTH, spDueDays.getSelection());
            dtIssueDate.setSelection(calendar.getTime());
        }
    }
	
	/**
	 * Show or hide the warning icon
	 */
    private void showHideWarningIcon() {

        // Check, whether the delivery address is the same as the billing address
        boolean differentDeliveryAddress;

        if (documentType == DocumentType.DELIVERY) {
            differentDeliveryAddress = !billingAddress.isEmpty() && !billingAddress.equalsIgnoreCase(DataUtils.getInstance().removeCR(txtAddress.getText()));
            differentDeliveryAddressIcon.setToolTipText(msg.editorDocumentWarningDifferentaddress + '\n' + billingAddress);
        } else {
            differentDeliveryAddress = !deliveryAddress.isEmpty() && !deliveryAddress.equalsIgnoreCase(DataUtils.getInstance().removeCR(txtAddress.getText()));
            differentDeliveryAddressIcon.setToolTipText(msg.editorDocumentWarningDifferentdeliveryaddress + '\n' + deliveryAddress);
        }

        if (differentDeliveryAddress) {
            // Show the icon
            GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(differentDeliveryAddressIcon);
        } else {
            // Hide the icon
            GridDataFactory.swtDefaults().hint(0, 0).align(SWT.END, SWT.CENTER).applyTo(differentDeliveryAddressIcon);
        }
    }
	
	/**
	 * Fill the address label with a contact 
	 * 
	 * @param contact
	 * 		The contact
	 */
	private void setAddress(Contact contact) {
		// Use delivery address, if it's a delivery note
		if (documentType == DocumentType.DELIVERY) {
		    txtAddress.setText(contactUtil.getAddressAsString(contact.getAlternateContacts()));
		} else {
		    txtAddress.setText(contactUtil.getAddressAsString(contact));
		}
		
		billingAddress = contactUtil.getAddressAsString(contact);
		deliveryAddress = contactUtil.getAddressAsString(contact.getAlternateContacts());

		this.addressId = contact;

		if (preferences.getBoolean(Constants.PREFERENCES_DOCUMENT_USE_DISCOUNT_ALL_ITEMS) && itemsDiscount != null) {
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
//		newPayment = dataSetPayment;
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
		labelDate.setToolTipText(msg.editorDocumentDateTooltip);
		
		GridDataFactory.swtDefaults().indent(20, 0).align(SWT.END, SWT.CENTER).applyTo(labelDate);

		// Document date
		dtDate = new CDateTime(nrDateNetGrossComposite, CDT.BORDER | CDT.DROP_DOWN);
		dtDate.setToolTipText(labelDate.getToolTipText());
		dtDate.setFormat(CDT.DATE_MEDIUM);
		dtDate.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// If the date is modified, also modify the issue date.
				// (Let the due days constant).
			    updateIssueDate();
			}
		});
		GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT).applyTo(dtDate);
		
		// Set the dtDate widget to the documents date
		bindModelValue(document, dtDate, Document_.documentDate.getName());
		
		// combo list to select between net or gross
		comboNetGross = new ComboViewer(documentType.hasPrice() ? nrDateNetGrossComposite : invisible, SWT.BORDER);
		comboNetGross.getCombo().setToolTipText(msg.editorDocumentNetgrossTooltip);
		
		Map<Integer, String> netGrossContent = new HashMap<>();
		// empty if nothing is selected
		netGrossContent.put(0, "---"); 
		//T: Text in combo box
		netGrossContent.put(1, msg.productDataNet);
		netGrossContent.put(2, msg.productDataGross);
        
		comboNetGross.setContentProvider(new HashMapContentProvider<Integer, String>());
		comboNetGross.setLabelProvider(new NumberLabelProvider<Integer, String>(netGrossContent));
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
		labelServiceDate.setToolTipText(msg.editorDocumentFieldServicedateTooltip);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelServiceDate);

		// Service date
		dtServiceDate = new CDateTime(useOrderDate ? xtraSettingsComposite : invisible, CDT.BORDER | CDT.DROP_DOWN);
		dtServiceDate.setToolTipText(labelServiceDate.getToolTipText());
		dtServiceDate.setFormat(CDT.DATE_MEDIUM);
		GridDataFactory.fillDefaults().minSize(80, SWT.DEFAULT).grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(dtServiceDate);
		// Set the dtDate widget to the documents date
		bindModelValue(document, dtServiceDate, Document_.serviceDate.getName());

		// Order date
		Label labelOrderDate = new Label(useOrderDate ? xtraSettingsComposite : invisible, SWT.NONE);
		if (documentType == DocumentType.OFFER) {
			//T: Label in the document editor
			labelOrderDate.setText(msg.editorDocumentFieldRequestdate);
			labelOrderDate.setToolTipText(msg.editorDocumentFieldRequestdateTooltip);
		} else {
			//T: Label in the document editor
			labelOrderDate.setText(msg.editorDocumentFieldOrderdate);
			labelOrderDate.setToolTipText(msg.editorDocumentFieldOrderdateTooltip);
		}

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelOrderDate);
		
		// Order date
		dtOrderDate = new CDateTime(useOrderDate ? xtraSettingsComposite : invisible, CDT.BORDER | CDT.DROP_DOWN);
		dtOrderDate.setToolTipText(labelOrderDate.getToolTipText());
		dtOrderDate.setFormat(CDT.DATE_MEDIUM);
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
//						noVatDescription = dataSetVat.getDescription();
					}
					else {
						noVat = false;
						noVatName = "";
//						noVatDescription = "";
					}

					// set all items to 0%
					itemListTable.setItemsNoVat(noVat);
//					tableViewerItems.refresh();

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
        createCopyToolbar(copyGroup);
		
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
			     * 
			     * You have to clone the dialog because else there's no valid parent if you open the dialog 
			     * a second time. Look at https://www.eclipse.org/forums/index.php/t/370078.
			     */
			    context.set(DOCUMENT_ID, document.getName());
			    SelectContactDialog dlg = ContextInjectionFactory.make(SelectContactDialog.class, context);
			    dlg.open();
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
				// Open a new Contact Editor 
                Map<String, Object> params = new HashMap<>();
                params.put(CallEditor.PARAM_EDITOR_TYPE, ContactEditor.ID);
                params.put(CallEditor.PARAM_CALLING_DOC, document.getName());
                ParameterizedCommand parameterizedCommand = commandService.createCommand(CommandIds.CMD_CALL_EDITOR, params);
                handlerService.executeHandler(parameterizedCommand);
			    
			    document.getBillingContact().getAddress().setManualAddress(null);
                getMDirtyablePart().setDirty(true);
			}
		});

		// Composite that contains the address and the warning icon
		addressAndIconComposite = new Composite(top, SWT.NONE | SWT.RIGHT);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(addressAndIconComposite);
		GridDataFactory.fillDefaults().minSize(180, 80).align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(addressAndIconComposite);

		// The address field
		txtAddress = new Text(addressAndIconComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		if (documentType == DocumentType.DELIVERY) {
			txtAddress.setText(contactUtil.getAddressAsString(document.getDeliveryContact()));
		} else {
			txtAddress.setText(contactUtil.getAddressAsString(document.getBillingContact()));
		}
		
		/*
		 * TODO Wenn die Adresse händisch geändert wird, muß sie in manualAddress kopiert werden (am 
		 * besten nach dem Verlassen des Widgets). Falls ein Kontakt ausgewählt wurde, muß dieses
		 * Feld wieder auf null gesetzt werden und die Contact-Verknüpfung aktualisiert werden.
		 * Von daher geht hier ein simples "superviceControl" nicht.
		 * ==> Das wird aber schon alles im doSave() gemacht, daher brauchen wir hier nur checken,
		 * ob überhaupt etwas geändert wurde.
		 */
//		superviceControl(txtAddress, 250);
		
		txtAddress.addModifyListener(new ModifyListener() {
            
            @Override
            public void modifyText(ModifyEvent e) {
//		        if(!contactUtil.getAddressAsString(document.getBillingContact()).contentEquals(txtAddress.getText())) {
//		            document.setManualAddress(txtAddress.getText());
//		            document.setContact(null);
		            getMDirtyablePart().setDirty(true);
//		        }
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
                MTrimmedWindow dialog = (MTrimmedWindow) modelService.find("fakturama.dialog.select.text", application);
                dialog = (MTrimmedWindow) modelService.cloneElement(dialog, (MSnippetContainer) modelService.find("com.sebulli.fakturama.application", application));
                dialog.setToBeRendered(true);
                dialog.setVisible(true);
                dialog.setOnTop(true);
                dialog.getTransientData().put(DOCUMENT_ID, document.getName());
                modelService.bringToTop(dialog);

                // handling of adding a new list item is done via event handling in DocumentEditor
			}
		});

		int noOfMessageFields = preferences.getInt(Constants.PREFERENCES_DOCUMENT_MESSAGES);
		
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
		bindModelValue(document, txtMessage, Document_.message.getName(), 10_000);

		if (noOfMessageFields >= 2) {
			// Add a multi line text field for the message.
			txtMessage2 = new Text(messageFieldsComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
//			txtMessage2.setText(DataUtils.getInstance().makeOSLineFeeds(document.getMessage2()));
			
			GridDataFactory.defaultsFor(txtMessage2).minSize(80, 50).applyTo(txtMessage2);
			txtMessage2.setToolTipText(messageLabel.getToolTipText());
//			GridDataFactory.fillDefaults().grab(true, true).applyTo(txtMessage2);
			bindModelValue(document, txtMessage2, Document_.message2.getName(), 10_000);
		}
		if (noOfMessageFields >= 3) {
			// Add a multi line text field for the message.
			txtMessage3 = new Text(messageFieldsComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
//			txtMessage3.setText(DataUtils.getInstance().makeOSLineFeeds(document.getMessage3()));
			txtMessage3.setToolTipText(messageLabel.getToolTipText());
			
			GridDataFactory.defaultsFor(txtMessage3).minSize(80, 50).applyTo(txtMessage3);
//			GridDataFactory.fillDefaults().grab(true, true).applyTo(txtMessage3);
			bindModelValue(document, txtMessage3, Document_.message3.getName(), 10_000);
		}
		
		// Set the tab order
		if (documentType.hasInvoiceReference())
			setTabOrder(txtAddress, txtInvoiceRef);
		else if (documentType.hasPrice())
			setTabOrder(txtAddress, comboViewerNoVat.getControl());
		else if (documentType.hasItems())
			setTabOrder(txtAddress, itemListTable.getNatTable());
		else
			setTabOrder(txtAddress, txtMessage);

		// Depending on if the document has price values.
		if (!documentType.hasPrice()) {

			// If not, fill the columns for the price with the message field.
			if (documentType.hasItems()) {
				GridDataFactory.fillDefaults().hint(SWT.DEFAULT, noOfMessageFields*65).span(3, 1).grab(true, false).applyTo(messageFieldsComposite);
			} else {
				GridDataFactory.fillDefaults().span(3, 1).grab(true, true).applyTo(messageFieldsComposite);
			}

			createTotalComposite(documentType.hasPrice());
			
			// Get the documents'shipping values.
			shipping = document.getShipping();
//			shippingVat = document.getShipping().getShippingVat();
//			shippingAutoVat = document.getShippingAutoVat();
//			shippingVatDescription = document.getShipping().getShippingVat().getDescription();

//			calculate(Data.INSTANCE.getDocuments().getDatasetById(invoiceId));
			calculate(true);
		
		} else {  // document *has* a price

			if (documentType.canBePaid()) {
				GridDataFactory.fillDefaults().span(2, 1).hint(100, noOfMessageFields*65).grab(true, false).applyTo(messageFieldsComposite);
			} else {
				GridDataFactory.fillDefaults().span(2, 1).hint(100, noOfMessageFields*65).grab(true, true).applyTo(messageFieldsComposite);
			}
			
			// Create a column for the documents subtotal, shipping and total
			createTotalComposite(documentType.hasPrice());

			// Create the "paid"-controls, only if the document type allows
			// this.
			if (documentType.canBePaid()) {
				createPaidControls();
			}
		}

		updateUseGross(false);

		// Calculate the total sum
		if(documentType != DocumentType.DUNNING) {
			calculate();
		}
	}

    /**
     * Create the ToolBar for duplicate / copy a document into another.
     * 
     * @param copyGroup
     */
    private void createCopyToolbar(Group copyGroup) {
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
		    String action = String.format("%d. %s", (dunningLevel + 1), msg.toolbarNewDocumentDunningName);
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
    }

    /**
     * Create the "paid"-controls
     */
    private void createPaidControls() {
        // The paid label
        bPaid = new Button(top, SWT.CHECK | SWT.LEFT);
        if (BooleanUtils.toBoolean(document.getPaid())) {
        	bPaid.setSelection(document.getPaid());
        }
        if (BooleanUtils.toBoolean(document.getDeposit())) {
        	bPaid.setSelection(document.getDeposit());
        	deposit = Money.of(document.getPaidValue(), currencyUnit);
        }
        
        //T: Mark a paid document with this text.
        bPaid.setText(msg.documentOrderStatePaid);
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
        // Set the combo
        bindModelValue(document, comboViewerPayment.getCombo(), Document_.payment.getName(), target2PaymentModel, paymentModel2Target);

        // Create a default paid composite with the document's
        // state for "paid"
        createPaidComposite(BooleanUtils.toBoolean(document.getPaid()), BooleanUtils.toBoolean(document.getDeposit()), false);
    }

    /**
     * 
     */
    private void createTotalComposite(boolean hasPrice) {
        Composite totalComposite = new Composite(top, SWT.NONE);
        GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(2).applyTo(totalComposite);
        GridDataFactory.fillDefaults().align(SWT.END, SWT.TOP).grab(true, false).span(1, 2).applyTo(totalComposite);

        if(hasPrice) {
            // Label sub total
            netLabel = new Label(totalComposite, SWT.NONE);
            // Set the total text
            netLabel.setText(getTotalText());
            GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(netLabel);
    
            // Sub total
            itemsSum = new FormattedText(totalComposite, SWT.NONE | SWT.RIGHT);
            itemsSum.setFormatter(new MoneyFormatter());
            itemsSum.getControl().setEnabled(false);
    //			itemsSum.setText("---");
            GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.TOP).applyTo(itemsSum.getControl());
    
            if (preferences.getBoolean(Constants.PREFERENCES_DOCUMENT_USE_DISCOUNT_ALL_ITEMS) ||
            		!DataUtils.getInstance().DoublesAreEqual(document.getItemsRebate(), 0.0)) {
            	
            	// Label discount
            	Label discountLabel = new Label(totalComposite, SWT.NONE);
            	//T: Document Editor - Label discount 
            	discountLabel.setText(msg.commonFieldDiscount);
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
            shippingLabel.setToolTipText(msg.editorDocumentFieldShippingTooltip);
    
            // Shipping combo
            comboViewerShipping = new ComboViewer(shippingComposite, SWT.BORDER);
            comboViewerShipping.getCombo().setToolTipText(msg.editorDocumentFieldShippingTooltip);
            comboViewerShipping.setContentProvider(new EntityComboProvider());
            comboViewerShipping.setLabelProvider(new EntityLabelProvider());
            GridDataFactory.swtDefaults().hint(90, SWT.DEFAULT).align(SWT.BEGINNING, SWT.CENTER).applyTo(comboViewerShipping.getCombo());
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
            			shipping = (Shipping) structuredSelection.getFirstElement();
    
            			// Update the shipping VAT
    //						shippingVat = shipping.getShippingVat();
    //						shippingVatDescription = shippingVat.getDescription();
    //						shippingAutoVat = shipping.getAutoVat();
            			calculate();
            		}
            	}
            });
    
            // Fill the shipping combo with the shipping values.
            List<Shipping> allShippings = shippingsDAO.findAll();
            comboViewerShipping.setInput(allShippings);
    
            // Get the documents'shipping values.
            shipping = document.getShipping();
    //			shippingVat = document.getShipping().getShippingVat();
    //			shippingAutoVat = document.getShippingAutoVat();
    //			shippingVatDescription = shippingVat.getDescription();
    
            UpdateValueStrategy shippingModel2Target = new UpdateValueStrategy();
            shippingModel2Target.setConverter(new EntityConverter<Shipping>(Shipping.class));
            
            UpdateValueStrategy target2ShippingModel = new UpdateValueStrategy();
            target2ShippingModel.setConverter(new StringToEntityConverter<Shipping>(allShippings, Shipping.class, true));
            // Set the combo
            bindModelValue(document, comboViewerShipping.getCombo(), Document_.shipping.getName(), target2ShippingModel, shippingModel2Target);
    
            // Shipping value field
            shippingValue = new FormattedText(totalComposite, SWT.BORDER | SWT.RIGHT);
            shippingValue.setValue(document.getShippingValue() != null ? document.getShippingValue() : shipping.getShippingValue());
            shippingValue.setFormatter(new MoneyFormatter());
            shippingValue.getControl().setToolTipText(shippingLabel.getToolTipText());
            bindModelValue(document, shippingValue, Document_.shippingValue.getName(), 30);
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
            vatLabel.setText(msg.commonFieldVat);
            GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(vatLabel);
    
            // VAT value
            vatValue = new FormattedText(totalComposite, SWT.NONE | SWT.RIGHT);
            vatValue.setFormatter(new MoneyFormatter());
            vatValue.getControl().setEditable(false);
    //			vatValue.setText("---");
// TODO ???            bindModelValue(documentSummary, vatValue.getControl(), "totalVat", 30);
            GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.TOP).applyTo(vatValue.getControl());
        }

        // Total label
        Label totalLabel = new Label(totalComposite, SWT.NONE);
        //T: Document Editor - Total sum of this document 
        totalLabel.setText(msg.commonFieldTotal);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(totalLabel);

        // Total value
        totalValue = new FormattedText(totalComposite, SWT.NONE | SWT.RIGHT);
        totalValue.setFormatter(new MoneyFormatter());
        totalValue.getControl().setEditable(false);
        GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.TOP).applyTo(totalValue.getControl());
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
        if (documentType == DocumentType.ORDER && preferences.getBoolean(Constants.PREFERENCES_DOCUMENT_CUSTOMER_STATISTICS_DIALOG)) {
			CustomerStatistics customerStaticstics = ContextInjectionFactory.make(CustomerStatistics.class, context);
			
			customerStaticstics.setContact(document.getBillingContact());
			if (preferences.getInt(Constants.PREFERENCES_DOCUMENT_CUSTOMER_STATISTICS_COMPARE_ADDRESS_FIELD) == 1) {
				customerStaticstics.setAddress(document.getBillingContact().getAddress().getManualAddress());
	            customerStaticstics.makeStatistics(true);
			} else {	
                customerStaticstics.makeStatistics(false);
			}
			
			if (customerStaticstics.hasPaidInvoices()) {

				//T: Message Dialog
				MessageDialog.openInformation(parent.getShell(), 
						//T: Title of the customer statistics dialog
						msg.dialogMessageboxTitleInfo,
						//T: Part of the customer statistics dialog
						// the unescapeJava is because of the Newlines in the message format string
						MessageFormat.format(StringEscapeUtils.unescapeJava(msg.dialogCustomerStatisticsPart1), 
						        document.getAddressFirstLine(),
						        customerStaticstics.getOrdersCount(),
						        customerStaticstics.getLastOrderDate(),
						        customerStaticstics.getInvoices(),
						        DataUtils.getInstance().doubleToFormattedPrice(customerStaticstics.getTotal())));
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
                Long contactId = (Long) event.getProperty(ContactListTable.SELECTED_CONTACT_ID);
                Contact contact = contactDAO.findById(contactId);
//
//                // we can use the Selection Service
//                Contact contact = (Contact) selectionService.getSelection();
                setAddress(contact);
                // If a Contact is selected the manualAddress field has to be set to null!
//                document.getBillingContact().getAddress().setManualAddress(null);
                document.setBillingContact(contact);
                addressId = contact;
                txtAddress.setText(contactUtil.getAddressAsString(contact));
                getMDirtyablePart().setDirty(true);
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
                    newItem.setQuantity(documentType.getSign() * Double.valueOf(1));
                    newItem.setQuantityUnit(product.getQuantityUnit());
                    newItem.setDescription(product.getDescription());
                    newItem.setPrice(productUtil.getPriceByQuantity(product, newItem.getQuantity()));
                    newItem.setItemVat(product.getVat());
                    newItem.setPictureName(product.getPictureName());
                    newItem.setWeight(product.getWeight());

                    // Use the products description, or clear it
                    if (!preferences.getBoolean(Constants.PREFERENCES_DOCUMENT_COPY_PRODUCT_DESCRIPTION_FROM_PRODUCTS_DIALOG)) {
                        newItem.setDescription("");
                    }
                    itemListTable.addNewItem(new DocumentItemDTO(newItem));
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
                            itemsString.forEach(item -> itemListTable.addNewItem(new DocumentItemDTO(newItem)));
                            
                            // Put the number of the delivery note in a new line of the message field
                            if (preferences.getBoolean(Constants.PREFERENCES_DOCUMENT_ADD_NR_OF_IMPORTED_DELIVERY_NOTE)) {
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
                                documentsDAO.mergeTwoTransactions(document, deliveryNote);
                            }
                            else
                                importedDeliveryNotes.add(deliveryNote.getId());
                }
//                    tableViewerItems.refresh();
//                    if (newItem!= null)
//                        tableViewerItems.reveal(newItem);
                    calculate();
                break;
            case "TextModule":
                Long textModuleId = (Long) event.getProperty(TextListTable.SELECTED_TEXT_ID);
              TextModule text = textsDAO.findById(textModuleId);
                  
                  // Get the message field with the focus
                  Text selectedMessageField = txtMessage;

                  if (txtMessage2 != null && txtMessage2.isFocusControl())
                    selectedMessageField = txtMessage2;
                  if (txtMessage3 != null && txtMessage3.isFocusControl())
                    selectedMessageField = txtMessage3;
                  
                  // Insert the selected text in the message text
                  if (text != null && selectedMessageField != null) {
                      int begin = selectedMessageField.getSelection().x;
                      int end = selectedMessageField.getSelection().y;
                      String s = selectedMessageField.getText();
                      String s1 = s.substring(0, begin);
                      String s2 = text.getText();

                      selectedMessageField.setText(String.format("%s%s%s", s1, s2, s.substring(end, s.length())));
                      selectedMessageField.setSelection(s1.length() + s2.length());
                  }
                break;
            default:
                break;
            }
        }
    } 

    /**
     * Searches for the standard {@link Shipping} entry.
     */
    private Shipping lookupDefaultShippingValue() {
        long stdID = 1L;
        Shipping retval = null;

        // Get the ID of the standard entity from preferences
        stdID = preferences.getLong(Constants.DEFAULT_SHIPPING);
        retval = shippingsDAO.findById(stdID);
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
	public boolean thereIsOneWithSameNumber() {
		// Letters do not have to be checked
		if (documentType == DocumentType.LETTER)
			return false;

		// Cancel, if there is already a document with the same ID
		if (documentsDAO.existsOther(document)) {
			// Display an error message
		    MessageDialog.openError(top.getShell(), msg.editorDocumentErrorDocnumberTitle, msg.editorDocumentDialogWarningDocumentexists+ " " + txtName.getText());
			return true;
		}

		return false;
	}

	/**
	 * Returns, if save is allowed
	 * 
	 * @return TRUE, if save is allowed
	 * 
	 * @see com.sebulli.fakturama.editors.Editor#saveAllowed()
	 */
	protected boolean saveAllowed() {
		// Save is allowed, if there is no document with the same number
		return !thereIsOneWithSameNumber();
	}

    
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
     * if e.g. the items list has changed.
     * (could be sent from DocumentListTable)
     */
    @Inject
    @org.eclipse.e4.core.di.annotations.Optional
    protected void handleItemChanged(@UIEventTopic(EDITOR_ID + "/itemChanged") Event event) {
        if (event != null) {
            // the event has already all given params in it since we created them as Map
            String targetDocumentName= (String) event.getProperty(DOCUMENT_ID);
            // at first we have to check if the message is for us
            if(!StringUtils.equals(targetDocumentName, document.getName())) {
                // if not, silently ignore this event
                return; 
            }
            // (re)calculate summary
            // TODO check if this has to be done in a synchronous or asynchronous call
            // within UISynchronize
            if((Boolean)event.getProperty(DOCUMENT_RECALCULATE)) {
                calculate();
            }
            getMDirtyablePart().setDirty(true);
    }}
//    
//    @Inject
//    @org.eclipse.e4.core.di.annotations.Optional
//    protected void handleRecalculationRequest(@UIEventTopic(EDITOR_ID + "/recalculation") Event event) {
//    }    
    
    @Override
    protected Class<Document> getModelClass() {
        return Document.class;
    }
}
