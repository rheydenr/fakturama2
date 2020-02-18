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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.commons.text.similarity.JaroWinklerDistance;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffectFactory;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.WidgetSideEffects;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.util.Util;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.formattedtext.DoubleFormatter;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.nebula.widgets.formattedtext.PercentFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
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
import com.sebulli.fakturama.dialogs.SelectTextDialog;
import com.sebulli.fakturama.dialogs.SelectTreeContactDialog;
import com.sebulli.fakturama.dto.AddressDTO;
import com.sebulli.fakturama.dto.DocumentItemDTO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.misc.INumberFormatterService;
import com.sebulli.fakturama.misc.OrderState;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.model.DocumentReceiver_;
import com.sebulli.fakturama.model.Document_;
import com.sebulli.fakturama.model.DummyStringCategory;
import com.sebulli.fakturama.model.Dunning;
import com.sebulli.fakturama.model.IDocumentAddressManager;
import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.model.ObjectDuplicator;
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
import com.sebulli.fakturama.parts.widget.contacttree.ContactTreeListTable;
import com.sebulli.fakturama.parts.widget.contentprovider.EntityComboProvider;
import com.sebulli.fakturama.parts.widget.contentprovider.HashMapContentProvider;
import com.sebulli.fakturama.parts.widget.formatter.MoneyFormatter;
import com.sebulli.fakturama.parts.widget.labelprovider.EntityLabelProvider;
import com.sebulli.fakturama.parts.widget.labelprovider.NumberLabelProvider;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;
import com.sebulli.fakturama.util.ContactUtil;
import com.sebulli.fakturama.util.DocumentItemUtil;
import com.sebulli.fakturama.util.DocumentTypeUtil;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.documents.DocumentsListTable;
import com.sebulli.fakturama.views.datatable.products.ProductListTable;
import com.sebulli.fakturama.views.datatable.texts.TextListTable;


/**
 * The document editor for all types of document like letter, order,
 * confirmation, invoice, delivery, credit and dunning
 * 
 */
public class DocumentEditor extends Editor<Document> {

	private static final String ADDRESS_TAB_BILLINGTYPE = "ADDRESS_FOR_BILLING_TAB";
	public static final String DOCUMENT_RECALCULATE = "DOCUMENT.RECALCULATE";
	public static final String PARAM_SILENT_MODE = "org.fakturama.documenteditor.silentmode";
	
    /** Editor's ID */
    public static final String EDITOR_ID = "DocumentEditor";
	public static final String ID = "com.sebulli.fakturama.editors.documentEditor";
	
	/**
	 * The key for the document id parameter (used for differentiating the editors
	 * for the Event Broker).
	 */
	public static final String DOCUMENT_ID = "com.sebulli.fakturama.editors.document.id";
    
    private static final String TOOLITEM_COMMAND = "toolitem_command";
	private static final String ORIGIN_RECEIVER = "ORIGIN_RECEIVER";
//	private static final String CURRENT_RECEIVER = "CURRENT_RECEIVER";

    @Inject
    protected EHandlerService handlerService;

    @Inject
    protected ECommandService commandService;
    
    @Inject
    protected ESelectionService selectionService;
    
    @Inject
    private EPartService partService;
    
//    @Inject
//    private EHelpService helpService;

    @Inject
    private IEclipseContext context;

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
    
	@Inject
	private ILocaleService localeUtil;
	
	@Inject
	private INumberFormatterService numberFormatterService;
	
	@Inject
	private IDialogSettings settings;
	
	@Inject
	private IDocumentAddressManager addressManager;

	// SWT components of the editor
	private Composite top;
	private Text txtName;
	private CDateTime dtDate;
	private CDateTime dtOrderDate, dtVestingPeriodStart, dtVestingPeriodEnd;
	private CDateTime dtServiceDate;
	private Text txtCustomerRef;
	private Text txtConsultant;
	
	private CTabFolder addressAndIconComposite;
	private List<Text> txtAddresses = new ArrayList<>();
	
	private ComboViewer comboViewerNoVat;
	private ComboViewer comboNetGross;
	private Text txtInvoiceRef;
	private Text txtMessage, txtMessage2, txtMessage3;
	private Button bPaid;
	private Composite paidContainer;
	private Composite paidDataContainer = null;
	private Combo comboPayment;
	private Label warningDepositIcon;
	private Label warningDepositText;
	private Spinner spDueDays;
	private CDateTime dtIssueDate;
	private CDateTime dtPaidDate;
	private FormattedText itemsSum;
	private FormattedText itemsDiscount;
	private Combo comboShipping;
	private FormattedText shippingValue;
	private FormattedText vatValue;
	private FormattedText totalValue;
	private Label netLabel;
	private FormattedText tara; 

	// These flags are set by the preference settings.
	// They define, if elements of the editor are displayed, or not.
	private boolean useGross;

	private boolean noVat;
	private String noVatName;
	private Shipping shipping = null;
	private MonetaryAmount total =  Money.zero(DataUtils.getInstance().getDefaultCurrencyUnit());
	private MonetaryAmount deposit =  Money.zero(DataUtils.getInstance().getDefaultCurrencyUnit());
	private int dunningLevel = Integer.valueOf(0);
	
	/*
	 * Map for selected contacts (used for comparing and detecting changed addresses).
	 */
	private Map<BillingType, DocumentReceiver> selectedAddresses = new HashMap<>();
	
	private int netgross = DocumentSummary.ROUND_NOTSPECIFIED;

	// defines if the document is new created
	private boolean newDocument;

	// If the customer is changed and this document displays no payment text,
	// use this variable to store the payment and due days
	@Deprecated
	private String newPaymentDescription = "";
	
	// Imported delivery notes. This list is used to
	// set an reference to this document, if it's an invoice.
	// The reference is not set during the import but later when the
	// document is saved. Because the the  document has an id to reference to.
	private List<Long> importedDeliveryNotes = new ArrayList<>();

    private DocumentItemUtil documentItemUtil;
    private DocumentItemListTable itemListTable;
    private CurrencyUnit currencyUnit;
    private DocumentSummary documentSummary;
    private ContactUtil contactUtil;
	private Text selectedMessageField;
	private Group copyGroup;
	private List<Document> pendingDeliveryMerges;
	private Label netWeight;
	private Label totalWeight;
	
	/**
	 * Mark this document as printed
	 */
	public void markAsPrinted() {
		document.setPrinted(Boolean.TRUE);
		// Refresh the table view
		//refreshView();
	}
	
	/**
	 * Saves the contents of this part.
	 * 
	 * @param monitor
	 *            Progress monitor
	 * @return {@link Boolean#TRUE} if saving was ok, {@link Boolean#FALSE} otherwise.  
	 */
    @Persist
	public Boolean doSave(IProgressMonitor monitor) {
    	
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
		
		// set focus outside of address tab
		txtCustomerRef.setFocus();

		if (newDocument) {
			// Check if the document number is the next one
			if (!document.getBillingType().isLETTER()) {
				int result = getNumberGenerator().setNextFreeNumberInPrefStore(txtName.getText(), Document_.name.getName());

				// It's not the next free ID
				if (result == ERROR_NOT_NEXT_ID) {
					// Display an error message
					MessageDialog.openError(top.getShell(),

					//T: Title of the dialog that appears if the document number is not valid.
					msg.editorDocumentErrorDocnumberTitle,
					
					//T: Text of the dialog that appears if the customer number is not valid.
					MessageFormat.format(msg.editorDocumentErrorDocnumberNotnextfree, getNumberGenerator().getNextNr(getEditorID())) + "\n" + 
					//T: Text of the dialog that appears if the number is not valid.
					msg.editorContactHintSeepreferences);
					return Boolean.FALSE;
				}
			}
		}

		// Exit save if there is a document with the same number
		// already checked in saveAllowed()
		if (thereIsOneWithSameNumber()) {
			return Boolean.FALSE;
		}

		// Always set the editor's data set to "undeleted"
		document.setDeleted(Boolean.FALSE);

		// If this is an order, use the date as order date
		if (document.getBillingType().isORDER()) {
			document.setOrderDate(dtDate.getSelection());
		} else {
			document.setOrderDate(dtOrderDate.getSelection());
		}

	    checkForChangedAddresses();
	    reassignDocumentReceiver();

		if (StringUtils.isNotBlank(newPaymentDescription)) {
			document.getAdditionalInfo().setPaymentDescription(newPaymentDescription);
		}

		if (bPaid != null) {
			String paymentText = "";

            if (bPaid.getSelection()) {
                deposit =  Money.zero(currencyUnit);
				// check if the paid value is only a deposit or the whole invoice amount
				// not: a discount could decrease the invoice amount!
				
				if(isInvoiceDeposited()){
                    deposit = Money.of(document.getPaidValue(), currencyUnit);
                    document.setDeposit(Boolean.TRUE);
                    document.setPaid(Boolean.FALSE);
                    bPaid.setGrayed(true);
                } else {
            		// set the deposit flag
           			document.setDeposit(Boolean.FALSE);
                    document.setPaid(Boolean.TRUE);
                }
                if (document.getBillingType().isINVOICE()) {
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
			document.getAdditionalInfo().setPaymentText(paymentText);
		}
		// If this document contains no payment widgets, but..
		else {
			// the customer changed and so there is a new payment. Set it.
			if (!newPaymentDescription.isEmpty() && document.getPayment() != null) {
			    document.setPaid(Boolean.FALSE);
			    document.setPaidValue(Double.valueOf(0.0));

				// Use the text for "unpaid" from the current payment
                if (document.getPayment() != null) {
                    document.getAdditionalInfo().setPaymentText(document.getPayment().getUnpaidText());
                }
			}
		}
		
		document.setTotalValue(total.getNumber().doubleValue());
		
		// Set the dunning level
		if(document.getBillingType().isDUNNING()) {
		    ((Dunning)document).setDunningLevel(dunningLevel);
		}

		// Create a new document ID, if this is a new document
		if (newDocument) {
		    try {
                document = documentsDAO.save(document);
            } catch (FakturamaStoringException e) {
                log.error(e);
            }
		}

		// update pending delivery notes
		for (Document deliveryNote : pendingDeliveryMerges) {
			// Change also the transaction id of the imported delivery note
			documentsDAO.mergeTwoTransactions(document, deliveryNote);
		}
		
		// Update the invoice references in all documents within the same transaction
		if(document.getBillingType().isINVOICE()) {
		    documentsDAO.updateInvoiceReferences((Invoice) document);
		
    		// Update the references in the delivery notes
    		documentsDAO.updateDeliveries(importedDeliveryNotes, (Invoice) document);
		}
		importedDeliveryNotes.clear();
		
		if(itemListTable != null) {
			List<DocumentItem> items = itemListTable.getDocumentItemsListData()
			    .stream()
			    .map(dto -> dto.getDocumentItem())
			    .sorted(Comparator.comparing(DocumentItem::getPosNr))
			    .collect(Collectors.toList());
			document.setItems(items);
		}

		// Mark the (modified) document as "not printed"
		if (wasDirty) {
			document.setPrinted(false);
		}

		// If it is a new document => Now it is no longer new.
		newDocument = false;
		
        try {
            document = documentsDAO.save(document);
        } catch (FakturamaStoringException e) {
            log.error(e);
        }

		//Set the editor's name
		this.part.setLabel(document.getName());
		// Update Document Editor's object id
		// FIXME Doesn't work for newly created documents :-( "You must modify UIEventPublisher appropriately" 
//		this.part.getProperties().put(CallEditor.PARAM_OBJ_ID, Long.toString(document.getId()));

		setCopyGroupEnabled(true);
		
        // Refresh the table view of all documents
        evtBroker.post(EDITOR_ID, Editor.UPDATE_EVENT);
        if(BooleanUtils.toBoolean(document.getDeposit())) {
        	createDepositWarningIcon();
        }
        
        bindModel();
        
        // reset dirty flag
        setDirty(false);
        ((MPart)getMDirtyablePart()).getTransientData().put(CallEditor.PARAM_OBJ_ID, Long.toString(document.getId()));
        return Boolean.TRUE;
	}
    
    private void reassignDocumentReceiver() {
		document.getReceiver().clear();
		document.getReceiver().addAll(selectedAddresses.values());
	}

	/**
     * Checks the address field(s) for changed entries.
     */
	private void checkForChangedAddresses() {

		// Show a warning if the entered address is not similar to the address
		// of the document which is set by the address ID.
		// Compare only if current address is from the same origin as the stored adress.
		// (Else the user has selected another address from Contact list.)
		JaroWinklerDistance jaroWinklerDistance = new JaroWinklerDistance();
		for (CTabItem tabItem : addressAndIconComposite.getItems()) {
			AddressDTO addressDTO = (AddressDTO) tabItem.getControl().getData(ORIGIN_RECEIVER);
			DocumentReceiver documentReceiver = selectedAddresses.get(tabItem.getData(ADDRESS_TAB_BILLINGTYPE));
			String addressAsString = contactUtil.getAddressAsString(addressDTO, System.lineSeparator());
			if (!addressAsString.isEmpty()
					&& addressDTO != null 
					&& addressDTO.getAddressId() == documentReceiver.getOriginAddressId() 
					&& jaroWinklerDistance.apply(DataUtils.getInstance().removeCR(addressAsString),
					    DataUtils.getInstance().removeCR(((Text) tabItem.getControl()).getText())) < 0.75) {
				MessageDialog.openWarning(top.getShell(),
						// T: Title of the dialog that appears if the document is assigned to an other
						// address.
						msg.editorDocumentErrorWrongcontactTitle,

						// T: Text of the dialog that appears if the document is assigned to an other
						// address.
						MessageFormat.format(msg.editorDocumentErrorWrongcontactMsg, addressAsString));
			}
		}
	}

	@Override
    protected void bindModel() {
		
		// for the (very ugly!) Linux bug which posts an event after each binding
		part.getTransientData().put(BIND_MODE_INDICATOR, Boolean.TRUE);
    	
    	int noOfMessageFields = getNumberOfMessageFields();
		bindModelValue(document, txtName, Document_.name.getName(), 80);
		bindModelValue(document, dtDate, Document_.documentDate.getName());
		bindModelValue(document, comboNetGross, Document_.netGross.getName());
		bindModelValue(document, txtCustomerRef, Document_.customerRef.getName(), 250);
		bindModelValue(document, tara, Document_.tara.getName(), 50);
		
		DocumentReceiver mainTypeReceiver = addressManager.getAdressForBillingType(document, document.getBillingType());
		
		// the first document's receiver is the main receiver. That consultant field is
		// bound to document's consultant field.
		bindModelValue(mainTypeReceiver, txtConsultant, DocumentReceiver_.consultant.getName(), 250);
		
		CTabItem[] items = addressAndIconComposite.getItems();
		for (int i = 0; i < items.length; i++) {
			bindAddressWidgetForIndex(i);
		}
		
		bindModelValue(document, dtServiceDate, Document_.serviceDate.getName());
		bindModelValue(document, dtOrderDate, Document_.orderDate.getName());

		UpdateValueStrategy<IStatus, Date> strategy = new UpdateValueStrategy<IStatus, Date>();
		strategy.setBeforeSetValidator(new IValidator<Date>() {

		    @Override
		    public IStatus validate(Date vestingPeriodStart) {
		    	if(vestingPeriodStart == null || dtVestingPeriodEnd.getSelection() == null) return ValidationStatus.ok();
		        if(vestingPeriodStart.after(dtVestingPeriodEnd.getSelection())) {
		        	return ValidationStatus.error("Startdatum liegt nach dem Endedatum!");
		        } else {
		        	return ValidationStatus.ok();
		        }
		    }
		});
		
		Binding binding = bindModelValue(document, dtVestingPeriodStart, Document_.vestingPeriodStart.getName(), strategy, null);
		ControlDecorationSupport.create(binding, SWT.TOP | SWT.LEFT);
		
		bindModelValue(document, dtVestingPeriodEnd, Document_.vestingPeriodEnd.getName());
		
		bindModelValue(document, txtMessage, Document_.message.getName(), 10_000);
		if (noOfMessageFields >= 2) {
			bindModelValue(document, txtMessage2, Document_.message2.getName(), 10_000);
		}
		if (noOfMessageFields >= 3) {
			bindModelValue(document, txtMessage3, Document_.message3.getName(), 10_000);
		}
		if(itemsDiscount != null) {
			bindModelValue(document, itemsDiscount, Document_.itemsRebate.getName(), 5);			
		}
		if(comboShipping != null) {
			fillAndBindShippingCombo();
		}
		
		DocumentType documentType = DocumentTypeUtil.findByBillingType(document.getBillingType());
		if(documentType.canBePaid()) {
			bindModelValue(document, bPaid, Document_.paid.getName());
			if(isInvoiceDeposited()) {
				bPaid.setSelection(true);
				bPaid.setGrayed(true);
			}
			if(dtPaidDate != null && !dtPaidDate.isDisposed()) {
				bindModelValue(document, dtPaidDate, Document_.payDate.getName());
			}
			fillAndBindPaymentCombo();
			
			if(spDueDays != null && !spDueDays.isDisposed()) {
				// value is set by dueDays variable, not directly by binding
				bindModelValue(document, spDueDays, Document_.dueDays.getName(), 
						new UpdateValueStrategy<Spinner, Integer>(),
						new UpdateValueStrategy<Integer, Spinner>());
				spDueDays.setSelection(document.getDueDays());
			}
			
			updateIssueDate();

		}
        
        // now remove the "bind mode" from part
		part.getTransientData().remove(BIND_MODE_INDICATOR);
        
    }

	/**
	 * This is the "manual" implementation of the UpdateValueStrategy, since it
	 * seems not to be possible for updating a list of receivers which are displayed
	 * in a CTabFolder widget. The UpdateValueStrategy is for displaying a
	 * DocumentReceiver's address in an address tab. Since the address tab only
	 * contains a plain Text field, the values from DocumentReceiver has to be
	 * converted. Furthermore, the value in the Text field can be overwritten. In
	 * this case, the values from DocumentReceiver has to be cleared an the
	 * manualAddress field has to be set. For comparing the original Documentreceiver's 
	 * address with the currently selected (or entered) one it is stored in the data 
	 * field of the text widget.
	 */
	private void bindAddressWidgetForIndex(int index) {
		final Text currentAddressTabWidget = txtAddresses.get(index);
		ISideEffectFactory sideEffectFactory = WidgetSideEffects.createFactory(currentAddressTabWidget);
		ISWTObservableValue<String> observedText = WidgetProperties.text(SWT.FocusOut).observe(currentAddressTabWidget);
		// react on changes inside the Text widget (which contains the String
		// representation of an address)
		sideEffectFactory.create(observedText::getValue, addressString -> {
			BillingType billingType = (BillingType) addressAndIconComposite.getItem(index).getData(ADDRESS_TAB_BILLINGTYPE);
			DocumentReceiver currentReceiver = selectedAddresses.get(billingType);
//			DocumentReceiver currentReceiver = (DocumentReceiver) currentAddressTabWidget.getData(CURRENT_RECEIVER);
			if (currentReceiver == null) {
				// should not occur
				return;
				// throw new RuntimeException("can't get DocumentReceiver from current CTabItem.");
			}
			if (((MPart) getMDirtyablePart()).getTransientData().get(BIND_MODE_INDICATOR) == null) {
				// only if not in bind mode
				
				// Test if the txtAddress field was modified
				// ("modified" means that the content of the text field differs from the address
				// from current DocumentReceiver and was manually(!) changed)
				boolean addressModified = !DataUtils.getInstance().MultiLineStringsAreEqual(
						contactUtil.getAddressAsString(currentReceiver),
						observedText.getValue());
				// TODO check if FAK-276 is working!
	
				if (addressModified) {
					// DocumentReceiver was changed manually
					currentReceiver = clearAddressFields(currentReceiver);
					currentReceiver.setManualAddress(observedText.getValue());
				
				/*
				 * possible cases:
				 * 
				 * Document | Adresse alt        | Adresse neu        | Aktion
				 * neu      | --                 | Adr. aus Kontakten | neuer DocumentReceiver mit dieser Adresse  (v) ==> firstLine wird nicht angezeigt
				 * neu      | --                 | manuelle Adr.      | neuer DocumentReceiver mit dieser Adresse  (v)
				 * vorh.    | Adr. aus Kontakten | Adr. aus Kontakten | DocumentReceiver mit neuer Adresse füllen, manualAddress null setzen  (v) ==> firstLine wird nicht angezeigt
				 * vorh.    | manuelle Eingabe   | Adr. aus Kontakten | DocumentReceiver mit neuer Adresse füllen, manualAddress null setzen  (v)
				 * vorh.    | Adr. aus Kontakten | manuelle Eingabe 1)| DocumentReceiver leeren, manualAddress setzen  (v)
				 * vorh.    | manuelle Eingabe   | manuelle Eingabe   | DocumentReceiver leeren, manualAddress setzen  (v)
				 * 
				 * 1) "manuelle Eingabe" kann hier auch heißen, daß die bestehende Adresse einfach geändert wurde.
				 */
					setDirty(true);
				}
				updateAddressFirstLine(currentReceiver, currentAddressTabWidget, index);
			} else {
				// if in bind mode, fill address Text widget with DocumentReceiver's value
				observedText.setValue(contactUtil.getAddressAsString(currentReceiver));
			}
		});
	}

	private void updateAddressFirstLine(DocumentReceiver currentReceiver, Text currentAddressTabWidget, int index) {
		// Set the "addressFirstLine" value to the first line of the
		// contact address (in case of setting a new address from selection
		// the addressFirstLine property wouldn't be updated).
		// !!! only if this is the first CTab!!!
		if(index == 0) {
			String addressFirstLine = currentReceiver.getCustomerNumber() != null
					? contactUtil.getNameWithCompany(currentReceiver)
					: createAddressFirstLineFromString(currentAddressTabWidget);
			document.setAddressFirstLine(addressFirstLine);
		}
	}

	/**
	 * Clear all fields of a DocumentReceiver.
	 * 
	 * @param originReceiver
	 */
	final private DocumentReceiver clearAddressFields(DocumentReceiver originReceiver) {
		originReceiver.setCompany(null);
		originReceiver.setStreet(null);
		originReceiver.setCityAddon(null);
		originReceiver.setCity(null);
		originReceiver.setZip(null);
		originReceiver.setCountryCode(null);
		originReceiver.setManualAddress(null);
		originReceiver.setTitle(null);
		originReceiver.setFirstName(null);
		originReceiver.setCustomerNumber(null);
		return originReceiver;
	}

	private String createAddressFirstLineFromString(Text currentText) {
		String s = currentText.getText();
		
		// Remove the "\n" if it was a "\n" as line break.
		return s.split(System.lineSeparator())[0];
	}
	
	protected DocumentReceiver getOrCreateAddressByIndexFromContact(int i) {
		// get last address and fill up the address list
		int lastAddressIndex = document.getReceiver().size() - 1;
		if(lastAddressIndex < i) {
			do {
				DocumentReceiver address = modelFactory.createDocumentReceiver();
				// add no ContactType means this address is a default address for this contact
				document.getReceiver().add(address);
			} while(++lastAddressIndex < i);
		}
		return document.getReceiver().get(i);
	}

	private void fillAndBindPaymentCombo() {
		Payment tmpPayment = document.getPayment();
		ComboViewer comboViewerPayment;
        comboViewerPayment = new ComboViewer(comboPayment);
        comboViewerPayment.setContentProvider(new EntityComboProvider());
        comboViewerPayment.setLabelProvider(new EntityLabelProvider());
        GridDataFactory.swtDefaults().hint(200, SWT.DEFAULT).align(SWT.END, SWT.CENTER).applyTo(comboPayment);

        // If a new payment is selected ...
        comboViewerPayment.addSelectionChangedListener(new ISelectionChangedListener() {

        	// change the paymentId to the selected element
        	public void selectionChanged(SelectionChangedEvent event) {

        		// Get the selected element
        		IStructuredSelection structuredSelection = event.getStructuredSelection();
        		if (!structuredSelection.isEmpty()) {
        			// Get first selected element.
        			Object firstElement = structuredSelection.getFirstElement();
        			Payment dataSetPayment = (Payment) firstElement;
        			usePayment(dataSetPayment);
        		}
        		getMDirtyablePart().setDirty(true);
        	}
        });

        // Fill the payment combo with the payments
        List<Payment> allPayments = paymentsDao.findAll();
        comboViewerPayment.setInput(allPayments);
        document.setPayment(tmpPayment);
        
        UpdateValueStrategy<Payment, String> paymentModel2Target = UpdateValueStrategy.create(new EntityConverter<Payment>(Payment.class));
        UpdateValueStrategy<String, Payment> target2PaymentModel = UpdateValueStrategy.create(new StringToEntityConverter<Payment>(allPayments, Payment.class));
        // Set the combo
        bindModelValue(document, comboViewerPayment.getCombo(), Document_.payment.getName(), target2PaymentModel, paymentModel2Target);
	}

	private void fillAndBindShippingCombo() {
		
		Shipping tmpShipping = document.getShipping();
        ComboViewer comboViewerShipping = new ComboViewer(comboShipping);
        comboViewerShipping.setContentProvider(new EntityComboProvider());
        comboViewerShipping.setLabelProvider(new EntityLabelProvider());
        comboViewerShipping.addSelectionChangedListener(new ISelectionChangedListener() {
           
        	// If a new shipping is selected, recalculate the total
        	// sum and update the shipping VAT.
        	public void selectionChanged(SelectionChangedEvent event) {
        		// Get the selected element.
        		IStructuredSelection structuredSelection = event.getStructuredSelection();
        		if (!structuredSelection.isEmpty()) {
        			// Get first selected element.
        			shipping = (Shipping) structuredSelection.getFirstElement();
        			clearManualShipping(document);
        			document.setShipping(shipping);
        			getMDirtyablePart().setDirty(true);

        			// Update the shipping VAT
//						shippingVat = shipping.getShippingVat();
//						shippingVatDescription = shippingVat.getDescription();
//						shippingAutoVat = shipping.getAutoVat();
        			calculate();
        		}
        	}

			private void clearManualShipping(Document document) {
				document.getAdditionalInfo().setShippingDescription(null);
//				document.getAdditionalInfo().setShippingVatValue(null);
			}
        });
        
      comboShipping.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				// if the shipping combo box value is changed manually we have to cut off the shipping from document
				// and set it to the additionalInfos object
				if(e != null) {
					ISelection selection = comboViewerShipping.getSelection();
					// an empty selection signals that the user has typed an own value
					if(selection.isEmpty()) {						
						// set additional infos
						document.getAdditionalInfo().setShippingDescription(((Combo)e.getSource()).getText());
						// cut off shipping
						document.setShipping(null);
						getMDirtyablePart().setDirty(true);
					}
				}
			}
		});
      

        // Fill the shipping combo with the shipping values.
        List<Shipping> allShippings = shippingsDAO.findAll();
        comboViewerShipping.setInput(allShippings);
        document.setShipping(tmpShipping);
        if(tmpShipping == null && document.getAdditionalInfo().getShippingDescription() != null) {
        	// shipping was set manually => we have to do some magic :-)
        	comboShipping.setText(document.getAdditionalInfo().getShippingDescription());
        }
        
        // Get the documents'shipping values.
        UpdateValueStrategy<Shipping, String> shippingModel2Target = UpdateValueStrategy.create(new EntityConverter<Shipping>(Shipping.class));
        UpdateValueStrategy<String, Shipping> target2ShippingModel = UpdateValueStrategy.create(new StringToEntityConverter<Shipping>(allShippings, Shipping.class, true));
        // Set the combo
        bindModelValue(document, comboViewerShipping.getCombo(), Document_.shipping.getName(), target2ShippingModel, shippingModel2Target);
	}

	/**
	 * Checks if the given paid amount is only a deposit. Cares of the discount.
	 * 
     * @return <code>true</code> if it's only a deposit
     */
    private boolean isInvoiceDeposited() {
    	if(document.getPayment() == null) {
    		return false;
    	}
    	// see FAK-485
    	Double discount = 1 - document.getPayment().getDiscountValue();
    	MonetaryAmount paidValue = Money.of(document.getPaidValue(), currencyUnit);
		return paidValue.isGreaterThan(Money.zero(currencyUnit)) && paidValue.isLessThan(total.multiply((java.util.Optional.ofNullable(discount).orElse(NumberUtils.DOUBLE_ZERO)))
				.with(DataUtils.getInstance().getDefaultRounding()));
    }

	/**
	 * Updates all {@link Dunning}s which are related to the current invoice.
	 */
	private void updateDunnings() {
	    documentsDAO.updateDunnings(document, bPaid.getSelection(), dtPaidDate.getSelection());
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
    public void init(Composite parent, @Optional @Named(PARAM_SILENT_MODE) Boolean silentMode) {
        String tmpObjId;
		Boolean tmpDuplicate;
    	if(BooleanUtils.isTrue(silentMode)) {
    		tmpObjId = (String) context.get(CallEditor.PARAM_OBJ_ID);
    		tmpDuplicate = (Boolean) context.get(CallEditor.PARAM_FOLLOW_UP);
    	} else {
    		this.part = (MPart) parent.getData("modelElement");
    		tmpObjId = (String) part.getTransientData().get(CallEditor.PARAM_OBJ_ID);
    		tmpDuplicate = (Boolean) part.getTransientData().get(CallEditor.PARAM_FOLLOW_UP);
    	}
        this.documentItemUtil = ContextInjectionFactory.make(DocumentItemUtil.class, context);
        this.contactUtil = ContextInjectionFactory.make(ContactUtil.class, context);
        this.currencyUnit = numberFormatterService.getCurrencyUnit(localeUtil.getCurrencyLocale());
        pendingDeliveryMerges = new ArrayList<>();
        
        if (StringUtils.isNumeric(tmpObjId)) {
            Long objId = Long.valueOf(tmpObjId);
            // Set the editor's data set to the editor's input
            this.document = documentsDAO.findById(objId, true);
            
		    // if a copy should be created, create one and take the objId as a "template"
		    if(BooleanUtils.toBoolean((String)part.getTransientData().get(CallEditor.PARAM_COPY))) {
		    	// clone the product and use it as new one
		    	switch (this.document.getBillingType()) {
				case OFFER:
					this.document = new ObjectDuplicator().duplicateDocument(this.document);
					
					// Get the next document number
					document.setName(getNumberGenerator().getNextNr(getEditorID()));
			    	
			    	// in this case the document is NOT a follow-up of another!
			    	tmpDuplicate = Boolean.FALSE;
					break;

				default:
					break;
				}
		    	getMDirtyablePart().setDirty(true);
		    	
		    }
        }

		// If the document is a duplicate of an other document,
		// the input is the parent document.
		Document parentDoc = document;
		boolean duplicated = BooleanUtils.toBoolean(tmpDuplicate);

		// The document is new, if there is no document, or if the
		// flag for duplicated was set.
		newDocument = (document == null) || duplicated;

		// If new ..
		if (newDocument) {

			// .. get the document type (=the category) to ..
			String category = BooleanUtils.isTrue(silentMode) 
					? (String) context.get(CallEditor.PARAM_CATEGORY) 
					: (String) part.getTransientData().get(CallEditor.PARAM_CATEGORY);
			BillingType billingType = category != null ? BillingType.get(category) : BillingType.NONE;
			if (billingType.isNONE()) {
				billingType = BillingType.ORDER;
			}
			
			// if this document should be a copy of an existing document, create it
			if (duplicated) {
				document = copyFromSourceDocument(parentDoc, billingType);
				if(BooleanUtils.isNotTrue(silentMode)) {
					setDirty(true);
				}
			} else {
				// create a blank new document
				document = DocumentTypeUtil.createDocumentByBillingType(billingType);
			}

			// Copy the entry "message", or reset it to ""
			if (!defaultValuePrefs.getBoolean(Constants.PREFERENCES_DOCUMENT_COPY_MESSAGE_FROM_PARENT)) {
				document.setMessage("");
				document.setMessage2("");
				document.setMessage3("");
			}

			// If it's a dunning, increase the dunning level by 1
			if (billingType.isDUNNING()) {
				// get the parents document type
				BillingType billingTypeParent = parentDoc != null ? parentDoc.getBillingType() : BillingType.NONE;
				
				if (billingTypeParent.isDUNNING()) {
					dunningLevel = ((Dunning)parentDoc).getDunningLevel() + 1;
				} else {
					dunningLevel = 1;
				}
				// set the paid date to null because it's not paid
				// (this is in the unlikely case if one creates a dunning from a paid invoice)
				document.setPayDate(null);
			}

			// If it's a credit or a dunning, set it to unpaid
			if (billingType.isCREDIT() || billingType.isDUNNING()) {
				document.setPaid(false);
			}
			
			// Set the editors name
//            part.setLabel(documentType.getNewText());

			// In a new document, set some standard values
			Date today = Calendar.getInstance().getTime();
			if (!duplicated) {
				// Default shipping
				shipping = lookupDefaultShippingValue();
//				shippingVat = shipping.getShippingVat();
//				shippingAutoVat = shipping.getAutoVat();
//				shippingVatDescription = shipping.getDescription();
				
				document.setShipping(shipping);
				if(shipping == null) {
					// set a default
					document.setShippingAutoVat(ShippingVatType.SHIPPINGVATGROSS);
				} else {
					document.setShippingAutoVat(shipping.getAutoVat());
				}
				
				// Default payment
				long paymentId = defaultValuePrefs.getLong(Constants.DEFAULT_PAYMENT);
                Payment payment = paymentsDao.findById(paymentId);
                document.setPayment(payment);
                if(payment != null) {
                	document.setDueDays(payment.getNetDays());
                }
				
				document.setOrderDate(today);
				document.setServiceDate(today);
				document.setNetGross(defaultValuePrefs.getInt(Constants.PREFERENCES_DOCUMENT_USE_NET_GROSS));
			}
			else {
				if(document.getPayment() == null) {
					// Default payment
					long paymentId = defaultValuePrefs.getLong(Constants.DEFAULT_PAYMENT);
	                Payment payment = paymentsDao.findById(paymentId);
	                document.setPayment(payment);
				}
				
				if (document.getShipping() != null)
					shipping = document.getShipping();
				else {
					shipping = lookupDefaultShippingValue();
					document.setShipping(shipping);
				}
				total = Money.of(document.getTotalValue(), currencyUnit);
				
				if(billingType.isDUNNING()) {
					document.setOrderDate(parentDoc.getOrderDate()); // see FAK-490
					document.setServiceDate(parentDoc.getServiceDate());  // see FAK-472
				}
			}
			
			// set some dates
			document.setDocumentDate(today);
//			document.setPayDate(today);
//			document.setPaid(Boolean.FALSE);

			// Get the next document number
			document.setName(getNumberGenerator().getNextNr(getEditorID()));

		}
		// If an existing document was opened ..
		else {

			// Get document type, set editorID
			setEditorID(document.getBillingType().toString());
			shipping = document.getShipping();

			// and the editor's part name
			part.setLabel(document.getName());

		}

		noVat = document.getNoVatReference() != null;
		if(noVat) {
		    noVatName = document.getNoVatReference().getName();
		}
		
		netgross = document.getNetGross() != null ? document.getNetGross() : DocumentSummary.ROUND_NET_VALUES;
		if (dunningLevel <= 0) {
            if (document.getBillingType().isDUNNING()) {
            	dunningLevel = ((Dunning)document).getDunningLevel();
            } else {
                dunningLevel = Integer.valueOf(1);
            }
        }
		
    	fillSelectedAddresses();

		if(BooleanUtils.isNotTrue(silentMode) && !newDocument) {
			showOrderStatisticDialog(parent);
		}
        
        // Get some settings from the preference store
        if (netgross == DocumentSummary.ROUND_NOTSPECIFIED) {
            useGross = (defaultValuePrefs.getInt(Constants.PREFERENCES_DOCUMENT_USE_NET_GROSS) == DocumentSummary.ROUND_NET_VALUES);
        } else { 
            useGross = (netgross == DocumentSummary.ROUND_GROSS_VALUES);
        }
        
        // in silent mode we don't need a UI, just saving and exiting
        if(BooleanUtils.isTrue(silentMode)) {
        	try {
        		calculate();
        		document = documentsDAO.save(document);
				getNumberGenerator().setNextFreeNumberInPrefStore(document.getName(), Document_.name.getName());
            } catch (FakturamaStoringException e) {
                log.error(e);
			}
        } else {
        	createPartControl(parent);
        }
	}

    /**
     * Helper method to fill the lookup hash map with all the selected contacts (for addresses resulting in DocumentReceivers).
     */
    private void fillSelectedAddresses() {
    	document.getReceiver().forEach(rcv -> selectedAddresses.put(rcv.getBillingType(), rcv));
	}

	/**
     * Creates a copy of the given {@link Document}.
     * 
     * @param parentDoc the source document
     * @param pTargetType
     * @return a copy of the source document
     */
	private Document copyFromSourceDocument(Document parentDoc, BillingType pTargetType) {
		DocumentType documentType = DocumentTypeUtil.findByBillingType(pTargetType);
		
		// TODO Check if parentDoc is equal to field "document"
		Document retval = DocumentTypeUtil.createDocumentByBillingType(pTargetType);
		retval.setSourceDocument(parentDoc);
		createReceiverInformationFromParentDoc(retval);
		// what about additionalInfo?
		retval.setShipping(parentDoc.getShipping());
		retval.setShippingValue(parentDoc.getShipping() != null ? parentDoc.getShipping().getShippingValue() : parentDoc.getShippingValue());
		retval.setShippingAutoVat(parentDoc.getShippingAutoVat());
		
		Payment parentPayment = parentDoc.getPayment();
		retval.setPaidValue(parentDoc.getPaidValue());
		retval.setPaid(parentDoc.getPaid());
		retval.setPayDate(parentDoc.getPayDate());
		retval.setDueDays(parentDoc.getDueDays());
		retval.setDeposit(parentDoc.getDeposit());
		if((parentPayment == null && !DocumentTypeUtil.findByBillingType(parentDoc.getBillingType()).canBePaid()) && documentType.canBePaid()) {
			// set payment method to default payment if parent document is not a payable document (e.g., an offer or delivery document)
			long paymentId = defaultValuePrefs.getLong(Constants.DEFAULT_PAYMENT);
			parentPayment = paymentsDao.findById(paymentId);
			
			// reset some payment-related values
			retval.setPaidValue(Double.valueOf(0.0));
			retval.setPaid(Boolean.FALSE);
			retval.setPayDate(null);
			retval.setDueDays(parentPayment.getNetDays());
			retval.setDeposit(Boolean.FALSE);
			
		}
		retval.setPayment(parentPayment);
		
		retval.setTotalValue(parentDoc.getTotalValue());
		if(parentDoc.getTransactionId() != null) {
			retval.setTransactionId(parentDoc.getTransactionId());
		}
		
		retval.setCustomerRef(parentDoc.getCustomerRef());
		retval.setServiceDate(parentDoc.getServiceDate());
		retval.setOrderDate(parentDoc.getOrderDate());
		if(parentDoc.getBillingType().isINVOICE()) {
			retval.setInvoiceReference((Invoice) parentDoc);
		} else if(parentDoc.getInvoiceReference() != null) {
			retval.setInvoiceReference(parentDoc.getInvoiceReference());
		}

		// copy items
		for (DocumentItem item : parentDoc.getItems()) {
			// ok, looks a bit odd, but I've generated a (very simple!) copy method which
			// returns a new object. TODO refactor the generation method (see Template!)
			DocumentItem newItem = item.clone();
			retval.addToItems(newItem);
		}
		retval.setItemsRebate(parentDoc.getItemsRebate());
		
		retval.setNoVatReference(parentDoc.getNoVatReference());
		retval.setNetGross(parentDoc.getNetGross());
		retval.setMessage(parentDoc.getMessage());
		retval.setMessage2(parentDoc.getMessage2());
		retval.setMessage3(parentDoc.getMessage3());
		return retval;
	}

	/**
	 * Create the receiver information for this document.
	 * 
	 * @param resultingDoc the document for which the receiver's information should be created
	 */
	private void createReceiverInformationFromParentDoc(Document resultingDoc) {
		Document parentDoc = resultingDoc.getSourceDocument();
		
		// determine parentDoc's Contact
		DocumentReceiver addressFromParentDoc = addressManager.getAdressForBillingType(parentDoc, parentDoc.getBillingType());

		// lookup origin receiver for an additional address which fits to this billing type
		Contact contactFromReceiver = contactDAO.findById(addressFromParentDoc.getOriginContactId());
		DocumentReceiver receiver;
		if(contactFromReceiver != null) {
			receiver = addressManager.createDocumentReceiverForBillingType(contactFromReceiver, resultingDoc.getBillingType());
		} else {
			// if no contact was found (mostly for manually added addresses) we copy the address from origin document receiver
			receiver = addressFromParentDoc.clone();
			// change type
			receiver.setBillingType(resultingDoc.getBillingType());
		}
		resultingDoc.setAddressFirstLine(contactUtil.getNameWithCompany(receiver));
		resultingDoc.getReceiver().add(receiver);
	}

	/**
	 * Returns the document
	 * 
	 * @return The document
	 */
	public Document getDocument() {
		return document;
	}

	public Document getDocument(boolean refresh) {
		if(refresh && document != null && document.getId() != 0L) {
			// refresh document (some associations could have been changed)
			document = documentsDAO.findById(document.getId(), refresh);
//			bindModel();
		}
		return document;
	}
	
	/**
	 * Returns the document type
	 * 
	 * @return The document type
	 * 
	 */
	public DocumentType getDocumentType() {
		return DocumentTypeUtil.findByBillingType(document.getBillingType());
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
		if (!getDocumentType().hasPrice() && !forceCalc) {
			return;
		}
		
		// Get the sign of this document ( + or -)
//		int sign = DocumentTypeUtil.findByBillingType(document.getBillingType()).getSign();
		
		// Get the discount value from the document or (if exists) from control element
		Double rebate = java.util.Optional.ofNullable(document.getItemsRebate()).orElse(Double.valueOf(0.0));
		if (itemsDiscount != null) {
	        // Convert it to negative values
	        rebate = (Double)itemsDiscount.getValue();
			if (rebate > 0) {
				rebate *= Integer.valueOf(-1);
				itemsDiscount.setValue(rebate);
	        }
		}
		
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
		// if - for what reason ever - the document shipping has no value, I've decided to set it to default shipping.
		if(shipping == null && document.getShippingValue() == null) {
			shipping = lookupDefaultShippingValue();
		}
		
		DocumentSummaryCalculator documentSummaryCalculator = new DocumentSummaryCalculator(
				defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_SALES_EQUALIZATION_TAX));
        if(document.getShipping() == null) {
    		documentSummary = documentSummaryCalculator.calculate(null, docItems,
    				document.getShippingValue()/* * sign*/,
                    null, 
                    document.getShippingAutoVat(), 
                    rebate, document.getNoVatReference(), Double.valueOf(1.0), netgross, deposit);
        } else {
			documentSummary = documentSummaryCalculator.calculate(null, docItems,
	                document.getShipping().getShippingValue(),
	                document.getShipping().getShippingVat(), 
	                document.getShipping().getAutoVat(), 
	                rebate, document.getNoVatReference(), Double.valueOf(1.0), netgross, deposit);
        }

		// Get the total result
		total = documentSummary.getTotalGross();

		// Set the items sum
		if (itemsSum != null) {
			itemsSum.setValue(useGross ? documentSummary.getItemsGross() : documentSummary.getItemsNet());
		}

		// Set the shipping
        if (shippingValue != null) {
            // shippingValue is the only field which could be modified manually *and* per calculation
            // therefore we have to disable the ModifyListener at first.
            shippingValue.getControl().setData(CALCULATING_STATE, true);
            shippingValue.setValue(useGross ? documentSummary.getShippingGross() : documentSummary.getShippingNet());
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
		
		if (defaultValuePrefs.getBoolean(Constants.PREFERENCES_PRODUCT_USE_WEIGHT)) {
			// set weight widgets
			double netWeightValue = itemListTable.getDocumentItemsListData().stream()
					.mapToDouble(DocumentItemDTO::getWeight).sum();
			netWeight.setText(numberFormatterService.doubleToFormattedQuantity(netWeightValue));
			Double taraValue = document.getTara() != null ? document.getTara() : Double.valueOf(0.0);
			totalWeight.setText(numberFormatterService.doubleToFormattedQuantity(netWeightValue + taraValue));
		}
	}

	/**
	 * Get the total text, net or gross
	 * 
	 * @return
	 * 		The total text
	 */
	private String getTotalText () {
		return useGross ? msg.editorDocumentTotalgross : msg.editorDocumentTotalnet;
	}
	
	/**
	 * Change the document from net to gross or backwards 
	 */
	private void updateUseGross(boolean address_changed) {
		
		boolean oldUseGross = useGross;
		
		// Get some settings from the preference store
        if (netgross == DocumentSummary.ROUND_NOTSPECIFIED) {
            useGross = defaultValuePrefs.getInt(Constants.PREFERENCES_DOCUMENT_USE_NET_GROSS) == 1;
        } else {
            useGross = netgross == DocumentSummary.ROUND_GROSS_VALUES;
        }
		
		// Use the customers settings instead, if they are set
        DocumentReceiver addr = selectedAddresses.get(BillingType.INVOICE);
		if (addr != null && address_changed && addr.getOriginContactId() != null) {
			// useNetGross can be null (from database!)
			Contact contact = contactDAO.findById(addr.getOriginContactId());
			if (DocumentSummary.ROUND_NET_VALUES == contact.getUseNetGross()) {
				useGross = false;
				netgross = DocumentSummary.ROUND_NET_VALUES;
			} else if (addr == null 
					|| DocumentSummary.ROUND_GROSS_VALUES == contact.getUseNetGross()) {
				useGross = true;
				netgross = DocumentSummary.ROUND_GROSS_VALUES;
			}
			StructuredSelection sel = new StructuredSelection(netgross);
			comboNetGross.setSelection(sel, true);
		}

		// Show a warning if the customer uses a different setting for net or gross
		if ((useGross != oldUseGross) && getDocumentType().hasPrice()) {
			
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
			if (itemListTable != null ) {
				if (useGross) {
//					if (unitPriceColumn >= 0)
//						itemTableColumns.get(unitPriceColumn).setDataKey("$ItemGrossPrice");
//					if (totalPriceColumn >= 0)
//						itemTableColumns.get(totalPriceColumn).setDataKey("$ItemGrossTotal");
				}
				else {
//					if (unitPriceColumn >= 0)
//						itemTableColumns.get(unitPriceColumn).setDataKey("price");
//					if (totalPriceColumn >= 0)
//						itemTableColumns.get(totalPriceColumn).setDataKey("$ItemNetTotal");
				}

				// for deliveries there's no netLabel...
				if(netLabel != null) {
					// Update the total text
					netLabel.setText(getTotalText());
				}

				itemListTable.refresh();
			}
			
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
	protected void changeShippingValue() {

		// Get the new value and take the absolute value
		Double newShippingValue = (Double) shippingValue.getValue();
		if (newShippingValue < 0) {
			newShippingValue = -newShippingValue;
		}

		// If the shipping value has changed:
		// Set the shippingAutoVat to net or gross, depending on the
		// settings of this editor.
		// sometimes the shipping value has many fraction digits so that we have to round it
		// to a reasonable value
		// but at first we have to decide which value is the correct shipping value (net or gross)
		MonetaryAmount currentShippingValue = useGross ? documentSummary.getShippingGross() : documentSummary.getShippingNet();
		if (shipping != null && !DataUtils.getInstance().DoublesAreEqual(newShippingValue, 
				currentShippingValue.getNumber().doubleValue())) {
		    document.getAdditionalInfo().setShippingDescription(shipping.getDescription());
		    document.getAdditionalInfo().setShippingName(shipping.getName());
		    document.getAdditionalInfo().setShippingVatValue(shipping.getShippingVat().getTaxValue());
		    
		    document.setShippingValue(newShippingValue);
			document.setShippingAutoVat(useGross ? ShippingVatType.SHIPPINGVATGROSS : ShippingVatType.SHIPPINGVATNET);
		} else {
			// no change occurred, we can return and leave the values unchanged
			return;
		}

		// Recalculate the sum
		shipping = null;
		document.setShipping(null);   // because we changed the Shipping value manually
		
		// now the document gets dirty
		getMDirtyablePart().setDirty(true);
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
		GridLayoutFactory.swtDefaults().margins(0, 5).numColumns(6).applyTo(paidDataContainer);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.BOTTOM).applyTo(paidDataContainer);

		// Should this container have the widgets for the state "paid" ?
		if (paid) {
			createDepositContainer(clickedByUser);
		} else if (isDeposit) {
			createDepositContainer(clickedByUser);
//			bPaid.setGrayed(true);
			createDepositWarningIcon();
		}
		// The container is created with the widgets that are shown
		// if the invoice is not paid.
		else {

			// Reset the paid value to 0
//			paidValue =  Money.zero(currencyUnit);
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
			spDueDays.setIncrement(1);
			spDueDays.setPageIncrement(10);
			spDueDays.setToolTipText(dueDaysLabel.getToolTipText());
			GridDataFactory.swtDefaults().hint(90, SWT.DEFAULT).applyTo(spDueDays);

			// If the spinner's value changes, add the due days to the
			// day of today.
			spDueDays.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { 
				    Calendar calendar = Calendar.getInstance();
				    calendar.setTime(dtDate.getSelection());
//					duedays = spDueDays.getSelection();
					calendar.add(Calendar.DAY_OF_MONTH, spDueDays.getSelection());
					dtIssueDate.setSelection(calendar.getTime());
					setDirty(true);
			}));

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
			GridDataFactory.swtDefaults().hint(200, SWT.DEFAULT).grab(true, false).applyTo(dtIssueDate);
			dtIssueDate.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { 
				// Calculate the difference between the date of the
				// issue date widget and the documents date,
				// calculate is in "days" and set the due day spinner
				Date calendarIssue = dtIssueDate.getSelection();
				Date calendarDocument = java.util.Optional.ofNullable(dtDate.getSelection()).orElse(Calendar.getInstance().getTime());
				long difference = calendarIssue.getTime() - calendarDocument.getTime();
				// Calculate from milliseconds to days
				int days = (int) (difference / (1000 * 60 * 60 * 24));
				spDueDays.setSelection(days);
				// spinner doesn't throw an event if updating...
				document.setDueDays(spDueDays.getSelection());
				setDirty(true);
			}));
			if(document.getDueDays() != null && document.getDocumentDate() != null) {
				dtIssueDate.setSelection(DateUtils.addDays(document.getDocumentDate(), document.getDueDays()));
			}
			
			updateIssueDate();
		}

		// Resize the container
		paidContainer.layout(changed);
		paidContainer.pack(changed);
	}

	/**
	 * 
	 */
	private void createDepositWarningIcon() {
		if(!paidDataContainer.isDisposed()) {
			if (warningDepositIcon == null || warningDepositIcon.isDisposed() || warningDepositIcon.getImage() == null) { // if the editor is about to close...
				// Add the attention sign if its a deposit
				warningDepositIcon = new Label(paidDataContainer, SWT.NONE);
				warningDepositIcon.setImage(Icon.ICON_WARNING.getImage(IconSize.ToolbarIconSize));
				warningDepositText = new Label(paidDataContainer, SWT.NONE);
				warningDepositText.setText(msg.editorDocumentFieldDeposit);
			} else {
				warningDepositText.setVisible(true);
				warningDepositIcon.setVisible(true);
			}
		}
	}

	/**
	 * @param clickedByUser
	 */
	private void createDepositContainer(boolean clickedByUser) {
		// Create the widget for the date, when the invoice was paid
		Label paidDateLabel = new Label(paidDataContainer, SWT.NONE);
		paidDateLabel.setText(msg.editorDocumentPaidat);
		paidDateLabel.setToolTipText(msg.editorDocumentDateofpayment);

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(paidDateLabel);

		dtPaidDate = new CDateTime(paidDataContainer, CDT.BORDER | CDT.DROP_DOWN);
		dtPaidDate.setToolTipText(paidDateLabel.getToolTipText());
		dtPaidDate.setFormat(CDT.DATE_MEDIUM);
		GridDataFactory.swtDefaults().hint(130, SWT.DEFAULT).applyTo(dtPaidDate);

		// Create the widget for the value
		Label paidValueLabel = new Label(paidDataContainer, SWT.NONE);
		
		paidValueLabel.setText(msg.commonFieldValue);
		paidValueLabel.setToolTipText(msg.editorDocumentPaidvalue);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(paidValueLabel);
		final FormattedText txtPayValue = new FormattedText(paidDataContainer, SWT.BORDER | SWT.RIGHT);
		txtPayValue.setFormatter(ContextInjectionFactory.make(MoneyFormatter.class, context));
		txtPayValue.getControl().setToolTipText(paidValueLabel.getToolTipText());
		
		// TODO bind later!
		bindModelValue(document, txtPayValue, Document_.paidValue.getName(), 32);
		bindModelValue(document, dtPaidDate, Document_.payDate.getName());
		
		txtPayValue.getControl().addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if(isInvoiceDeposited()) {
					createDepositWarningIcon();
					// Resize the container
					paidContainer.layout();
					paidContainer.pack();
				} else {
					if(warningDepositIcon != null && !warningDepositIcon.isDisposed()) {
						warningDepositIcon.setVisible(false);
						warningDepositText.setVisible(false);
					}
					bPaid.setSelection(true);
				}
				bPaid.setGrayed(isInvoiceDeposited());
			}
		});
		
		txtPayValue.getControl().addKeyListener(new KeyAdapter() {
    		public void keyPressed(KeyEvent e) {
    			if (e.keyCode == 13 || e.keyCode == SWT.KEYPAD_CR) {
    				txtPayValue.getControl().traverse(SWT.TRAVERSE_TAB_NEXT);
    			}
    		}
    	});
		GridDataFactory.swtDefaults().hint(60, SWT.DEFAULT).applyTo(txtPayValue.getControl());

		// If it's the first time that this document is marked as paid
		// (if the value is 0.0), then also set the date to "today"
		if ((document.getPaidValue() == null || document.getPaidValue() == 0) && clickedByUser) {
			txtPayValue.setValue(total.getNumber().doubleValue());
			dtPaidDate.setSelection(Calendar.getInstance().getTime());
			
			// FIXME This is only because the CDateTime widget doesn't fire an event on setSelection
			document.setPayDate(dtPaidDate.getSelection());
		}
		if(document.getPayDate() != null) {
			dtPaidDate.setSelection(document.getPayDate());
		}
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
			top.getParent().getDisplay().syncExec(() -> {
			    if(!spDueDays.isDisposed()) {
    				calendar.add(Calendar.DAY_OF_MONTH, spDueDays.getSelection());
    				dtIssueDate.setSelection(calendar.getTime());
			    }
			});
        }
    }
	
	/**
	 * Show or hide the warning icon
	 */
    private void showHideWarningIcon() {
    	
    	// FIXME implement!

//        // Check, whether the delivery address is the same as the billing address
//        boolean hasDifferentDeliveryAddress;
//
//        if (document.getBillingType().isDELIVERY) {
//            hasDifferentDeliveryAddress = !DataUtils.getInstance().MultiLineStringsAreEqual(addressString, txtAddress.getText());
//            // see also https://bugs.eclipse.org/bugs/show_bug.cgi?id=188271
//            differentDeliveryAddressIcon.setToolTipText(MessageFormat.format(msg.editorDocumentWarningDifferentaddress, addressString.replaceAll("&", "&&")));
//        } else {
//            hasDifferentDeliveryAddress = !DataUtils.getInstance().MultiLineStringsAreEqual(addressString, txtAddress.getText());
//            differentDeliveryAddressIcon.setToolTipText(MessageFormat.format(msg.editorDocumentWarningDifferentdeliveryaddress, addressString.replaceAll("&", "&&")));
//        }
//        
//        if (hasDifferentDeliveryAddress) {
//            // Show the icon
//            GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(differentDeliveryAddressIcon);
//        } else {
//            // Hide the icon
//            GridDataFactory.swtDefaults().hint(0, 0).align(SWT.END, SWT.CENTER).applyTo(differentDeliveryAddressIcon);
//        }
    }
	
	/**
	 * Fill the address {@link CTabItem} with a contact 
	 * @param address 
	 * 
	 * @param contact
	 * 		The contact
	 */
	private void setAddress(Address address, DocumentReceiver documentReceiver) {
		Contact contact = address.getContact();
		// set the DocumentReceiver in the currently active address tab
		selectedAddresses.put(document.getBillingType(), documentReceiver);

		// select the correct address tab
		CTabItem addressTab = addressAndIconComposite.getSelection();
		
		bindModelValue(documentReceiver, txtConsultant, DocumentReceiver_.consultant.getName(), 250);
		setAddressInTab(addressTab, documentReceiver, addressAndIconComposite.getSelectionIndex());
		addOtherAddressesIfNotExisting(contact);

		if (defaultValuePrefs.getBoolean(Constants.PREFERENCES_DOCUMENT_USE_DISCOUNT_ALL_ITEMS) && itemsDiscount != null) {
        	itemsDiscount.setValue(contact.getDiscount());
        	document.setItemsRebate(contact.getDiscount());
        }
		// Check, if the payment is valid
		Payment paymentid = contact.getPayment();
		
		if (paymentid != null) {
			//Use the payment method of the customer
			document.setPayment(paymentid);
			if (comboPayment != null) {
				comboPayment.setText(paymentid.getName());
			}

			usePayment(paymentid);
		}
		
		showHideWarningIcon();
		addressAndIconComposite.layout(true);
		updateUseGross(true);
	}
	
	private void setAddressInTab(CTabItem addressTab, DocumentReceiver documentReceiver, int position) {
		if (addressTab != null) {
			Text currenCTabItem = (Text) addressTab.getControl();
			String addressAsString = contactUtil.getAddressAsString(documentReceiver);
			currenCTabItem.setText(addressAsString);
			updateAddressFirstLine(documentReceiver, currenCTabItem, position);

			part.getTransientData().put(BIND_MODE_INDICATOR, Boolean.TRUE);
			bindAddressWidgetForIndex(addressAndIconComposite.getSelectionIndex());
			part.getTransientData().remove(BIND_MODE_INDICATOR);
		}
	}

	/**
	 * If current document is an invoice or a delivery note and there's no according
	 * address we add the appropriate address from that contact (if only one address fits).
	 * 
	 * @param contact currently selected {@link Contact}
	 */
	private void addOtherAddressesIfNotExisting(Contact contact) {
		if (document.getBillingType().isINVOICE() || document.getBillingType().isDELIVERY()) {
			BillingType billingTypeToCheck = document.getBillingType().isINVOICE() ? BillingType.DELIVERY
					: BillingType.INVOICE;
			ContactType contactType = contactUtil.convertToContactType(billingTypeToCheck);
			if (contactType != null && !selectedAddresses.containsKey(billingTypeToCheck)) {
				java.util.Optional<Address> alternateAddress = contact.getAddresses().parallelStream()
						.filter(a -> a.getContactTypes().contains(contactType)).findAny();
				if (alternateAddress.isPresent()) {
					DocumentReceiver documentReceiver = addressManager
							.createDocumentReceiverFromAddress(alternateAddress.get(), billingTypeToCheck);
					document = addressManager.addOrReplaceReceiverToDocument(document, documentReceiver);
					java.util.Optional<CTabItem> addressTabForAlternativeAddress = lookupAddressTabForBillingType(
							billingTypeToCheck);
						// only set an additional tab if haven't a tab for this billing type yet
					if(!addressTabForAlternativeAddress.isPresent()) {
						CTabItem currenCTabItem = createAddressTabItem(documentReceiver);
						setAddressInTab(currenCTabItem, documentReceiver, addressAndIconComposite.getItemCount());
					}
				}
			}
		}

	}

	private java.util.Optional<CTabItem> lookupAddressTabForBillingType(BillingType billingType) {
		return  Arrays.stream(addressAndIconComposite.getItems()).filter(t -> t.getData(ADDRESS_TAB_BILLINGTYPE).equals(billingType)).findFirst();
	}

	/**
	 * Use this payment and update the duedays
	 * 
	 * @param dataSetPayment the payment
	 */
	private void usePayment(Payment dataSetPayment) {
		
		// Return, if no payment is set
		if (dataSetPayment == null)
			return;
		
//		payment = dataSetPayment;

		// Get the due days and description of this payment
//		duedays = payment.getNetDays();
//		newPayment = dataSetPayment;
		newPaymentDescription = dataSetPayment.getDescription();

		if (spDueDays !=null && !spDueDays.isDisposed()) {
			spDueDays.setSelection(dataSetPayment.getNetDays().intValue());
			updateIssueDate();
		}
	}
	
	
	/**
	 * Creates the SWT controls for this workbench part
	 * 
	 * @param the
	 *            parent control
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	private void createPartControl(Composite parent) {
		// Create the ScrolledComposite to scroll horizontally and vertically
	    ScrolledComposite scrollcomposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);

		// Create the top Composite
		top = new Composite(scrollcomposite, SWT.SCROLLBAR_OVERLAY | SWT.NONE );  //was parent before 
		GridLayoutFactory.fillDefaults().numColumns(4).applyTo(top);

		scrollcomposite.setContent(top);
		scrollcomposite.setMinSize(1200, 700);   // 2nd entry should be adjusted to higher value when new fields will be added to composite 
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

		// for letters the "No." label has to be changed, see FAK-437
		if(document.getBillingType().isLETTER()) {
			//T: Letter Editor - Label Document Subject
			labelName.setText(msg.editorDocumentLetterSubject);
			labelName.setToolTipText(msg.editorDocumentLetterSubjectTooltip);
		} else {
			//T: Document Editor - Label Document Number
			labelName.setText(msg.commonFieldNumber);
			labelName.setToolTipText(msg.editorDocumentRefnumberTooltip);
		}
		
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);
		
		// Container for the document number and the date
		Composite nrDateNetGrossComposite = new Composite(top, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(4).applyTo(nrDateNetGrossComposite);
		GridDataFactory.fillDefaults().minSize(540, SWT.DEFAULT).align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(nrDateNetGrossComposite);

		// The document number is the document name
		// but for letters it is the subject, see above
		if(document.getBillingType().isLETTER()) {
			txtName = new Text(nrDateNetGrossComposite, SWT.BORDER);
			txtName.setSize(400, SWT.DEFAULT);
		} else {
			txtName = new Text(nrDateNetGrossComposite, SWT.BORDER);
		}
		txtName.setToolTipText(labelName.getToolTipText());
		GridDataFactory.swtDefaults().minSize(200, SWT.DEFAULT).grab(true, false).applyTo(txtName);
        
		// Document date
		//T: Document Editor
		//T: Label Document Date
		Label labelDate = new Label(nrDateNetGrossComposite, SWT.NONE);
		labelDate.setText(msg.commonFieldDate);
		labelDate.setToolTipText(msg.editorDocumentDateTooltip);
		GridDataFactory.swtDefaults().indent(15, 0).align(SWT.END, SWT.CENTER).applyTo(labelDate);

		// Document date
		dtDate = new CDateTime(nrDateNetGrossComposite, CDT.BORDER | CDT.DROP_DOWN);
		dtDate.setToolTipText(labelDate.getToolTipText());
		dtDate.setFormat(CDT.DATE_MEDIUM);
		dtDate.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { 
				// If the date is modified, also modify the issue date.
				// (Let the due days constant).
			    updateIssueDate();
		}));
		GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT).align(SWT.END, SWT.CENTER).applyTo(dtDate);
		
		// combo list to select between net or gross
		comboNetGross = new ComboViewer(getDocumentType().hasPrice() ? nrDateNetGrossComposite : invisible, SWT.BORDER | SWT.READ_ONLY);
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
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).indent(20, 0).minSize(80, SWT.DEFAULT).grab(true, false).applyTo(comboNetGross.getControl());
		
		comboNetGross.getCombo().addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				StructuredSelection selection = (StructuredSelection)comboNetGross.getSelection();
				netgross = selection.isEmpty() ? netgross : (int) selection.toList().get(0);
				// recalculate the total sum
//				calculate();
				updateUseGross(false);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
	
		// The titleComposite contains the title and the document icon
		Composite titleComposite = new Composite(top, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(titleComposite);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.BOTTOM).span(2, 1).grab(true, false).applyTo(titleComposite);

		// Set the title in large letters
		Label labelDocumentType = new Label(titleComposite, SWT.NONE);
		String documentTypeString = msg.getMessageFromKey(getDocumentType().getSingularKey());
		if (document.getBillingType().isDUNNING()) {
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
	 	GridDataFactory.fillDefaults().hint(300, SWT.DEFAULT).applyTo(txtCustomerRef);
				
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
		
		boolean useOrderDate = !document.getBillingType().isORDER();

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

		// Order date
		Label labelOrderDate = new Label(useOrderDate ? xtraSettingsComposite : invisible, SWT.NONE);
		if (document.getBillingType().isOFFER()) {
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

		// If "orderdate" is not set, use "webshopdate"
		Date orderDateString = document.getOrderDate() == null ? document.getWebshopDate() : document.getOrderDate();
		dtOrderDate.setSelection(orderDateString);
		
		// Vesting period
		Label labelVestingPeriodStart = new Label(defaultValuePrefs.getInt(Constants.PREFERENCES_DOCUMENT_USE_VESTINGPERIOD) > 0 ? xtraSettingsComposite : invisible, SWT.NONE);
		labelVestingPeriodStart.setText(msg.editorDocumentFieldVestingperiodStart);
		labelVestingPeriodStart.setToolTipText(msg.editorDocumentFieldVestingperiodStartTooltip);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelVestingPeriodStart);
		
		dtVestingPeriodStart = new CDateTime(defaultValuePrefs.getInt(Constants.PREFERENCES_DOCUMENT_USE_VESTINGPERIOD) > 0 ? xtraSettingsComposite : invisible, CDT.BORDER | CDT.DROP_DOWN);
		dtVestingPeriodStart.setToolTipText(labelVestingPeriodStart.getToolTipText());
		dtVestingPeriodStart.setFormat(CDT.DATE_MEDIUM);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(dtVestingPeriodStart);
        
		Label labelVestingPeriodEnd = new Label(defaultValuePrefs.getInt(Constants.PREFERENCES_DOCUMENT_USE_VESTINGPERIOD) > 1 ? xtraSettingsComposite : invisible, SWT.NONE);
		labelVestingPeriodEnd.setText(msg.editorDocumentFieldVestingperiodEnd);
		labelVestingPeriodEnd.setToolTipText(msg.editorDocumentFieldVestingperiodEndTooltip);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelVestingPeriodEnd);
		
		dtVestingPeriodEnd = new CDateTime(defaultValuePrefs.getInt(Constants.PREFERENCES_DOCUMENT_USE_VESTINGPERIOD) > 1 ? xtraSettingsComposite : invisible, CDT.BORDER | CDT.DROP_DOWN);
		dtVestingPeriodEnd.setToolTipText(labelVestingPeriodEnd.getToolTipText());
		dtVestingPeriodEnd.setFormat(CDT.DATE_MEDIUM);
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(dtVestingPeriodEnd);

		// A reference to the invoice
		Label labelInvoiceRef = new Label(getDocumentType().hasInvoiceReference() ? xtraSettingsComposite : invisible, SWT.NONE);
		//T: Label in the document editor
		labelInvoiceRef.setText(msg.editorDocumentFieldInvoice);
		labelInvoiceRef.setToolTipText(msg.editorDocumentFieldInvoiceTooltip);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.BOTTOM).applyTo(labelInvoiceRef);
		
		txtInvoiceRef = new Text(getDocumentType().hasInvoiceReference() ? xtraSettingsComposite : invisible, SWT.BORDER);
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
		Label labelNoVat = new Label(getDocumentType().hasPrice() ? xtraSettingsComposite : invisible, SWT.NONE);
		//T: Label in the document editor
		labelNoVat.setText(msg.commonFieldVat);
		labelNoVat.setToolTipText(msg.editorDocumentZerovatTooltip);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelNoVat);

		// combo list with all 0% VATs
		comboViewerNoVat = new ComboViewer(getDocumentType().hasPrice() ? xtraSettingsComposite : invisible, SWT.BORDER  | SWT.READ_ONLY);
		comboViewerNoVat.getCombo().setToolTipText(labelNoVat.getToolTipText());
		comboViewerNoVat.setContentProvider(new EntityComboProvider());
		comboViewerNoVat.setLabelProvider(new EntityLabelProvider());
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.FILL, SWT.CENTER).applyTo(comboViewerNoVat.getCombo());
		
		comboViewerNoVat.addSelectionChangedListener(new ISelectionChangedListener() {

			// A combo entry is selected
			public void selectionChanged(SelectionChangedEvent event) {
        		IStructuredSelection structuredSelection = event.getStructuredSelection();
				if (!structuredSelection.isEmpty()) {

					// get first element ...
					Object firstElement = structuredSelection.getFirstElement();
					VAT dataSetVat = (VAT) firstElement;

					// get the "no-VAT" values
					if (dataSetVat.getId() > 0) {
						noVat = true;
						noVatName = dataSetVat.getName();
//						noVatDescription = dataSetVat.getDescription();
					}
					else {
						noVat = false;
						noVatName = "";
						/* because later on we have to
						 * to decide if noVAT is set based on null or not null 
						 */
						dataSetVat = null;  
//						noVatDescription = "";
					}

					// set all items to 0%
					itemListTable.setItemsNoVat(noVat, dataSetVat);
					// update NoVat reference
					document.setNoVatReference(dataSetVat);
					itemListTable.refresh();

					// recalculate the total sum
					calculate();
					
					setDirty(true);
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

		copyGroup = new Group(top, SWT.SHADOW_ETCHED_OUT);
		
		//T: Document Editor
		//T: Label Group box to create a new document based on this one.
		copyGroup.setText(msg.editorDocumentCreateduplicate);
		GridLayoutFactory.fillDefaults().applyTo(copyGroup);
		GridDataFactory.fillDefaults().span(1, 2).align(SWT.FILL, SWT.BOTTOM).grab(true, false).applyTo(copyGroup);
		
		// Toolbar
        createCopyToolbar(copyGroup);
        if(document.getId() == 0) {
        	setCopyGroupEnabled(false);
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
		selectAddressButton.setToolTipText(msg.dialogSelectaddressTooltip);
		selectAddressButton.setImage(Icon.DOCEDIT_CONTACT_LIST.getImage(IconSize.DocumentIconSize));
		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(selectAddressButton);
		selectAddressButton.addMouseListener(new MouseAdapter() {

			// Open the address dialog if the icon is clicked.
			@SuppressWarnings("unchecked")
			public void mouseDown(MouseEvent e) {
			    /*
			     * This code searches for the dialog part in the Application model
			     * and opens it. The content of this dialog is taken from ContactTreeListTable.
			     * The part in the Application model has an additional context entry
			     * "fakturama.datatable.contacts.clickhandler" which is for the ContactTreeListTable
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
            	// save MPart
            	MPart myPart = context.get(MPart.class);
                // FIXME Workaround (quick & dirty), please use enums or an extra button
            	SelectTreeContactDialog<Address> dlg = null;
		    	context.set("ADDRESS_TYPE", document.getBillingType());
			    if((e.stateMask & SWT.CTRL) != 0) {
				    context.set("CONTACT_TYPE", "CREDITOR");
				    dlg = ContextInjectionFactory.make(SelectTreeContactDialog.class, context);
			    } else {
			    	context.set("CONTACT_TYPE", "DEBITOR");
			    	dlg = ContextInjectionFactory.make(SelectTreeContactDialog.class, context);
			    }
			    dlg.setDialogBoundsSettings(getDialogSettings("SelectTreeContactDialog"), Dialog.DIALOG_PERSISTSIZE | Dialog.DIALOG_PERSISTLOCATION);
			    dlg.open();
			    context.set(MPart.class, myPart);
			    // the result is set via event DialogSelection/Contact
			}
		});

		// Address icon
		Label newAddressButton = new Label(addressComposite, SWT.NONE | SWT.RIGHT);
		newAddressButton.setToolTipText(msg.commandOpenContactTooltip);
		newAddressButton.setImage(Icon.DOCEDIT_CONTACT_PLUS.getImage(IconSize.DocumentIconSize));
		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(newAddressButton);
		newAddressButton.addMouseListener(new MouseAdapter() {

			// Open the address dialog, if the icon is clicked.
			public void mouseDown(MouseEvent e) {
				// Open a new Contact Editor 
                Map<String, Object> params = new HashMap<>();
                params.put(CallEditor.PARAM_EDITOR_TYPE, ContactEditor.ID);
                // since we need a reference to the document where the address has to put in :-)
                params.put(CallEditor.PARAM_CALLING_DOC, document.getName());
                ParameterizedCommand parameterizedCommand = commandService.createCommand(CommandIds.CMD_CALL_EDITOR, params);
                handlerService.executeHandler(parameterizedCommand);
			    
                addressManager.getBillingAdress(document).setManualAddress(null);
                setDirty(true);
			}
		});
		
		// Export address to CSV
		Label exportToCSV = new Label(defaultValuePrefs.getBoolean(Constants.PREFERENCES_EXPORT_CSV4DHL) ? addressComposite : invisible, SWT.NONE | SWT.RIGHT);
		exportToCSV.setToolTipText(msg.commandDocumentsExportAddresscsv4dpDescription);
		exportToCSV.setImage(Icon.DOCUMENT_DHL_CSV.getImage(IconSize.DocumentIconSize));
		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(exportToCSV);
		exportToCSV.addMouseListener(new MouseAdapter() {

			// Open the address dialog, if the icon is clicked.
			public void mouseDown(MouseEvent e) {
				// Open a new Contact Editor 
                Map<String, Object> params = new HashMap<>();
                params.put(CallEditor.PARAM_CALLING_DOC, document.getName());
                ParameterizedCommand parameterizedCommand = commandService.createCommand(CommandIds.CMD_EXPORT_CSV4DP, params);
                handlerService.executeHandler(parameterizedCommand);
			}
		});

		// Composite that contains the addresses
		addressAndIconComposite = new CTabFolder(top, SWT.NONE);
		addressAndIconComposite.setSimple(false);
		// create main document receiver
		DocumentReceiver mainReceiver = createOrGetMainReceiver();
		CTabItem addressTab = createAddressTabItem(mainReceiver);
		addressAndIconComposite.setSelection(addressTab);
		if(document.getReceiver().size() > 1) {
			Iterator<DocumentReceiver> it = document.getReceiver().iterator();
			while (it.hasNext()) {
				DocumentReceiver currentDocumentReceiver = it.next();
				if(mainReceiver.equals(currentDocumentReceiver)) {
					continue;
				}
				createAddressTabItem(currentDocumentReceiver);
			}
		}
		
		GridDataFactory.fillDefaults().minSize(100, 80).align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(addressAndIconComposite);
//		addressAndIconComposite.setSelection(0);

		DocumentType documentType = getDocumentType();
/* * * * * * * * * * * * *  here the items list table is created * * * * * * * * * * * * */ 
		// Add the item table, if the document is one with items.
		if (documentType.hasItems()) {
		    ItemListBuilder itemListBuilder = ContextInjectionFactory.make(ItemListBuilder.class, context);
		    itemListTable = itemListBuilder
		        .withParent(top)
		        .withDocument(document)
		        .withNetGross(netgross)
//		        .withUseGross(useGross)
		        .withContainer(this)
		        .build();
		}
/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  * * * * * * * * * * * */ 
		
		Composite taraComposite = new Composite(defaultValuePrefs.getBoolean(Constants.PREFERENCES_PRODUCT_USE_WEIGHT) ? top : invisible, SWT.NONE | SWT.RIGHT);
		GridLayoutFactory.fillDefaults().applyTo(taraComposite);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(taraComposite);
		Label taraLabel = new Label(taraComposite, SWT.NONE);
		taraLabel.setText(msg.editorDocumentFieldTara);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(taraLabel);
		
		Composite weightComposite = new Composite(defaultValuePrefs.getBoolean(Constants.PREFERENCES_PRODUCT_USE_WEIGHT) ? top : invisible, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(5).applyTo(weightComposite);
		GridDataFactory.swtDefaults().span(3, 1).applyTo(weightComposite);
		tara = new FormattedText(weightComposite, SWT.BORDER | SWT.RIGHT);
		tara.setFormatter(new DoubleFormatter());
		tara.getControl().addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				calculate();
			}
		});
		GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT).applyTo(tara.getControl());
		
		Label netWeightLabel = new Label(weightComposite, SWT.NONE);
		netWeightLabel.setText(msg.editorDocumentFieldNetweight);
		netWeight = new Label(weightComposite, SWT.BORDER | SWT.SHADOW_IN);
		netWeight.setAlignment(SWT.RIGHT);
		GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT).applyTo(netWeight);
		
		Label totalWeightLabel = new Label(weightComposite, SWT.NONE);
		totalWeightLabel.setText(msg.editorDocumentFieldTotalweight);
		totalWeight = new Label(weightComposite, SWT.BORDER | SWT.SHADOW_IN);
		totalWeight.setAlignment(SWT.RIGHT);
		GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT).applyTo(totalWeight);
		
		// Container for the message label and the add button
		Composite addMessageButtonComposite = new Composite(top, SWT.NONE | SWT.RIGHT);
		GridLayoutFactory.fillDefaults().applyTo(addMessageButtonComposite);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addMessageButtonComposite);

		// The message label
		Label messageLabel = new Label(addMessageButtonComposite, SWT.NONE);
		if (documentType.hasItems()) {
			//T: Document Editor Label for the text field under the item table.
			messageLabel.setText(msg.editorDocumentFieldRemarks);
			messageLabel.setToolTipText(msg.editorDocumentFieldRemarksTooltip);

		}	
		else {
			//T: Document Editor Label for the text field, if there is no item table
			messageLabel.setText(msg.commonFieldText);
			messageLabel.setToolTipText(msg.editorDocumentFieldCommentTooltip);
		}

		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(messageLabel);

		// The add message button
		Label addMessageButton = new Label(addMessageButtonComposite, SWT.NONE);
		addMessageButton.setToolTipText(msg.editorDocumentSelecttemplateTooltip);
		addMessageButton.setImage(Icon.DOCEDIT_LIST.getImage(IconSize.DocumentIconSize));
		GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(addMessageButton);
		addMessageButton.addMouseListener(new MouseAdapter() {

			// Open the text dialog and select a text
			public void mouseDown(MouseEvent e) {
//                MTrimmedWindow dialog = (MTrimmedWindow) modelService.find("fakturama.dialog.select.text", application);
//                dialog = (MTrimmedWindow) modelService.cloneElement(dialog, (MSnippetContainer) modelService.find("com.sebulli.fakturama.application", application));
//                dialog.setToBeRendered(true);
//                dialog.setVisible(true);
//                dialog.setOnTop(true);
//                dialog.getTransientData().put(DOCUMENT_ID, document.getName());
//                modelService.bringToTop(dialog);
                
                selectedMessageField = txtMessage;

                if (txtMessage2 != null && txtMessage2.isFocusControl())
                  selectedMessageField = txtMessage2;
                if (txtMessage3 != null && txtMessage3.isFocusControl())
                  selectedMessageField = txtMessage3;
			
			    context.set(DocumentEditor.DOCUMENT_ID, document.getName());
			    context.set(ESelectionService.class, selectionService);
			    SelectTextDialog dlg = ContextInjectionFactory.make(SelectTextDialog.class, context);
	            dlg.setDialogBoundsSettings(getDialogSettings("SelectTextDialog"), Dialog.DIALOG_PERSISTSIZE | Dialog.DIALOG_PERSISTLOCATION);
			    dlg.open();
                // handling of adding a new list item is done via event handling in DocumentEditor
			}
		});

		int noOfMessageFields = getNumberOfMessageFields();
		
		// Container for 1..3 message fields
		Composite messageFieldsComposite = new Composite(top,SWT.NONE );
		GridLayoutFactory.fillDefaults().applyTo(messageFieldsComposite);
		
		// Add a multi line text field for the message.
		txtMessage = new Text(messageFieldsComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
		txtMessage.setToolTipText(messageLabel.getToolTipText());
		
		GridDataFactory.defaultsFor(txtMessage).minSize(80, 50).applyTo(txtMessage);

		if (noOfMessageFields >= 2) {
			// Add a multi line text field for the message.
			txtMessage2 = new Text(messageFieldsComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
			
			GridDataFactory.defaultsFor(txtMessage2).minSize(80, 50).applyTo(txtMessage2);
			txtMessage2.setToolTipText(messageLabel.getToolTipText());
		}
		if (noOfMessageFields >= 3) {
			// Add a multi line text field for the message.
			txtMessage3 = new Text(messageFieldsComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.WRAP);
			txtMessage3.setToolTipText(messageLabel.getToolTipText());
			
			GridDataFactory.defaultsFor(txtMessage3).minSize(80, 50).applyTo(txtMessage3);
		}

		// Depending on if the document has price values.
		if (!documentType.hasPrice()) {

			// If not, fill the columns for the price with the message field.
			if (documentType.hasItems()) {
				GridDataFactory.fillDefaults().hint(SWT.DEFAULT, noOfMessageFields*65).span(3, 1).grab(true, false).applyTo(messageFieldsComposite);
			} else {
				GridDataFactory.fillDefaults().span(3, 1).grab(true, true).applyTo(messageFieldsComposite);
			}

			// Get the documents'shipping values.
			shipping = document.getShipping();

			calculate(true);
		
		} else {  // document *has* a price

			if (documentType.canBePaid()) {
				GridDataFactory.fillDefaults().span(2, 1).hint(50, noOfMessageFields*65).grab(true, false).applyTo(messageFieldsComposite);
			} else {
				GridDataFactory.fillDefaults().span(2, 1).hint(50, noOfMessageFields*65).grab(true, true).applyTo(messageFieldsComposite);
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
		
		bindModel();
		
		// Set the tab order
		if (documentType.hasInvoiceReference())
			setTabOrder((Text) addressAndIconComposite.getItem(0).getControl(), txtInvoiceRef);
		else if (documentType.hasPrice())
			setTabOrder((Text) addressAndIconComposite.getItem(0).getControl(), comboViewerNoVat.getControl());
		else if (documentType.hasItems())
			setTabOrder((Text) addressAndIconComposite.getItem(0).getControl(), itemListTable.getNatTable());
		else
			setTabOrder((Text) addressAndIconComposite.getItem(0).getControl(), txtMessage);
	}

	private DocumentReceiver createOrGetMainReceiver() {
		// always add the receiver for the current document type at the first position
		DocumentReceiver mainReceiver = addressManager.getAdressForBillingType(document, document.getBillingType());
		if(mainReceiver == null) {
			mainReceiver = modelFactory.createDocumentReceiver();
			mainReceiver.setBillingType(document.getBillingType());
			document.getReceiver().add(mainReceiver);
		}

		return mainReceiver;
	}

	/**
	 * Create a single tab for a {@link DocumentReceiver}. For recognizing a changed
	 * address the origin {@link DocumentReceiver} is stored in data field.
	 * 
	 * @param documentReceiver the {@link DocumentReceiver} to set
	 */
	final private CTabItem createAddressTabItem(DocumentReceiver documentReceiver) {
		CTabItem addressTabItem = new CTabItem(addressAndIconComposite, SWT.NONE);
		addressTabItem.setData(ADDRESS_TAB_BILLINGTYPE, documentReceiver.getBillingType());
		addressTabItem.setText(documentReceiver.getBillingType().getName());

		// The address field
		Text currentAddress = new Text(addressAndIconComposite, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);

		// initially both objects are equal
		currentAddress.setData(ORIGIN_RECEIVER, AddressDTO.from(documentReceiver));
		selectedAddresses.put(documentReceiver.getBillingType(), documentReceiver);
//		addressTabItem.setToolTipText("'ne Adresse ");
		
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(currentAddress);
		addressTabItem.setControl(currentAddress);
		txtAddresses.add(currentAddress);
		return addressTabItem;
	}

	private int getNumberOfMessageFields() {
		int noOfMessageFields = defaultValuePrefs.getInt(Constants.PREFERENCES_DOCUMENT_MESSAGES);
		
		if (noOfMessageFields < 1)
			noOfMessageFields = 1;
		if (noOfMessageFields > 3)
			noOfMessageFields = 3;
		return noOfMessageFields;
	}

	/**
	 * @param enabled 
	 * 
	 */
	private void setCopyGroupEnabled(boolean enabled) {
		for (Control long1 : copyGroup.getChildren()) {
			long1.setEnabled(enabled);
		}
	}

    /**
     * Create the ToolBar for creating follow-up documents.
     * 
     * @param copyGroup
     */
    private void createCopyToolbar(Group copyGroup) {
        ToolBar toolBarDuplicateDocument = new ToolBar(copyGroup, SWT.FLAT | SWT.WRAP);
		GridDataFactory.fillDefaults().align(SWT.END, SWT.TOP).applyTo(toolBarDuplicateDocument);

        String tooltipPrefix = msg.commandNewTooltip + " ";
        
		// Add buttons, depending on the document type
		switch (document.getBillingType()) {
		case OFFER:
            createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewConfirmationName,
                    tooltipPrefix + msg.mainMenuNewConfirmation, Icon.ICON_CONFIRMATION_NEW.getImage(IconSize.ToolbarIconSize)
                    , createCommandParams(DocumentType.CONFIRMATION));
	        createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewOrderName,
	                tooltipPrefix + msg.mainMenuNewOrder, Icon.ICON_ORDER_NEW.getImage(IconSize.ToolbarIconSize)
	                , createCommandParams(DocumentType.ORDER));
	        createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewInvoiceName,
	                tooltipPrefix + msg.mainMenuNewInvoice, Icon.ICON_INVOICE_NEW.getImage(IconSize.ToolbarIconSize)
	                , createCommandParams(DocumentType.INVOICE));
	        createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.documentTypeProforma, 
	                tooltipPrefix + msg.mainMenuNewProforma, Icon.ICON_LETTER_NEW.getImage(IconSize.ToolbarIconSize)
	                , createCommandParams(DocumentType.PROFORMA));
			break;
		case ORDER:
	        createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewConfirmationName,
	                tooltipPrefix + msg.mainMenuNewConfirmation, Icon.ICON_CONFIRMATION_NEW.getImage(IconSize.ToolbarIconSize)
	                , createCommandParams(DocumentType.CONFIRMATION));
            createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewInvoiceName,
                    tooltipPrefix + msg.mainMenuNewInvoice, Icon.ICON_INVOICE_NEW.getImage(IconSize.ToolbarIconSize)
                    , createCommandParams(DocumentType.INVOICE));
            createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewDeliveryName,
            		tooltipPrefix + msg.mainMenuNewDeliverynote, Icon.ICON_DELIVERY_NEW.getImage(IconSize.ToolbarIconSize)
            		, createCommandParams(DocumentType.DELIVERY));
            createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.documentTypeProforma, 
                    tooltipPrefix + msg.mainMenuNewProforma, Icon.ICON_LETTER_NEW.getImage(IconSize.ToolbarIconSize)
                    , createCommandParams(DocumentType.PROFORMA));
			break;
		case CONFIRMATION:
            createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewInvoiceName,
                    tooltipPrefix + msg.mainMenuNewInvoice, Icon.ICON_INVOICE_NEW.getImage(IconSize.ToolbarIconSize)
                    , createCommandParams(DocumentType.INVOICE));
            createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.toolbarNewDeliveryName,
            		tooltipPrefix + msg.mainMenuNewDeliverynote, Icon.ICON_DELIVERY_NEW.getImage(IconSize.ToolbarIconSize)
            		, createCommandParams(DocumentType.DELIVERY));
            createToolItem(toolBarDuplicateDocument, CommandIds.CMD_CALL_EDITOR, msg.documentTypeProforma, 
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
		case PROFORMA:
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
        if (BooleanUtils.toBoolean(document.getDeposit())) {
        	// deposit means that not the whole amount is paid
//        	bPaid.setSelection(true);
        	bPaid.setGrayed(true);
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
        bPaid.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { 
	        	// ... Recreate the paid composite
        		createPaidComposite(bPaid.getSelection(), bPaid.getSelection(), true);
        		if(!bPaid.getSelection()) {
        			// remove previously set values
        			document.setPaidValue(null);
        			document.setPayDate(null);
        		}
        		// remove the grayed state
        		bPaid.setGrayed(false);
        }));

        // Combo to select the payment
        comboPayment = new Combo(paidContainer, SWT.BORDER | SWT.READ_ONLY);

        // Create a default paid composite with the document's
        // state for "paid"
        createPaidComposite(BooleanUtils.toBoolean(document.getPaid()), BooleanUtils.toBoolean(document.getDeposit()), false);
    }

    /**
     * 
     */
    private void createTotalComposite(boolean hasPrice) {
        Composite totalComposite = new Composite(top, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(totalComposite);
        GridDataFactory.fillDefaults().align(SWT.END, SWT.TOP).grab(true, false).span(1, 2).applyTo(totalComposite);

        if(hasPrice) {
            // Label sub total
            netLabel = new Label(totalComposite, SWT.NONE);
            // Set the total text
            netLabel.setText(getTotalText());
            GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(netLabel);
    
            // Sub total
            itemsSum = new FormattedText(totalComposite, SWT.NONE | SWT.RIGHT);
            context.set(ILocaleService.class, localeUtil);
    		MoneyFormatter formatter = ContextInjectionFactory.make(MoneyFormatter.class, context);
            itemsSum.setFormatter(formatter);
            itemsSum.getControl().setEnabled(false);
    //			itemsSum.setText("---");
            GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.TOP).applyTo(itemsSum.getControl());
    
            if (defaultValuePrefs.getBoolean(Constants.PREFERENCES_DOCUMENT_USE_DISCOUNT_ALL_ITEMS) ||
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
            		}
            	});
    
            	// Recalculate, if the discount is modified.
            	itemsDiscount.getControl().addKeyListener(new KeyAdapter() {
            		public void keyPressed(KeyEvent e) {
            			if (e.keyCode == 13 || e.keyCode == SWT.KEYPAD_CR) {
            				itemsDiscount.getControl().traverse(SWT.TRAVERSE_TAB_NEXT);
            			}
            		}
            	});
            }
        
            createShippingInfoFields(totalComposite);
    
            // VAT label
            Label vatLabel = new Label(totalComposite, SWT.NONE);
            //T: Document Editor - Label VAT 
            vatLabel.setText(msg.commonFieldVat);
            GridDataFactory.swtDefaults().align(SWT.END, SWT.TOP).applyTo(vatLabel);
    
            // VAT value
            vatValue = new FormattedText(totalComposite, SWT.NONE | SWT.RIGHT);
            vatValue.setFormatter(ContextInjectionFactory.make(MoneyFormatter.class, context));
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
        totalValue.setFormatter(ContextInjectionFactory.make(MoneyFormatter.class, context));
        totalValue.getControl().setEditable(false);
        GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.TOP).applyTo(totalValue.getControl());
    }

	private void createShippingInfoFields(Composite totalComposite) {
        // Shipping composite contains label and combo.
        Composite shippingComposite = new Composite(totalComposite, SWT.NONE);
        GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(3).applyTo(shippingComposite);
        GridDataFactory.fillDefaults().align(SWT.END, SWT.TOP).grab(true, false).applyTo(shippingComposite);

		// Shipping label
		Label shippingLabel = new Label(shippingComposite, SWT.NONE);
		//T: Document Editor - Label shipping 
		shippingLabel.setText(msg.editorDocumentFieldShipping);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(shippingLabel);
		shippingLabel.setToolTipText(msg.editorDocumentFieldShippingTooltip);
   
		// Shipping combo		
		comboShipping = new Combo(shippingComposite, SWT.BORDER);
		comboShipping.setToolTipText(msg.editorDocumentFieldShippingTooltip);
		GridDataFactory.swtDefaults().hint(250, SWT.DEFAULT).grab(true, false).align(SWT.END, SWT.TOP).applyTo(comboShipping);
   
		// Shipping value field
		shippingValue = new FormattedText(totalComposite, SWT.BORDER | SWT.RIGHT);
		shippingValue.setValue(document.getShippingValue() != null ? document.getShippingValue() : shipping.getShippingValue());
		shippingValue.setFormatter(ContextInjectionFactory.make(MoneyFormatter.class, context));
		shippingValue.getControl().setToolTipText(shippingLabel.getToolTipText());
		
		// since the shipping value can be changed also by comboNetGross we have to store
		// the shipping value "manually"
//            bindModelValue(document, shippingValue, Document_.shippingValue.getName(), 30);
		GridDataFactory.swtDefaults().hint(70, SWT.DEFAULT).align(SWT.END, SWT.CENTER).applyTo(shippingValue.getControl());
   
		// Recalculate, if the shipping field looses the focus.
		/*
		 * Note: We have to re-sort the FocusOut listeners because otherwise the display value isn't updated.
		 * (The origin listener gets "overwritten" by the new one, although it isn't. Crazy.) 
		 */
		Listener[] originFocusOutListener = shippingValue.getControl().getListeners(SWT.FocusOut);
		for (Listener listener2 : originFocusOutListener) {
			shippingValue.getControl().removeListener(SWT.FocusOut, listener2);
		}
		shippingValue.getControl().addFocusListener(new FocusAdapter() {
   
			public void focusLost(FocusEvent e) {
				changeShippingValue();
			}
		});
		for (Listener listener : originFocusOutListener) {
			shippingValue.getControl().addListener(SWT.FocusOut, listener);
		}
   
		// Recalculate, if the shipping is modified
		shippingValue.getControl().addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.KEYPAD_CR || e.keyCode == 13) {
					changeShippingValue();
					shippingValue.getControl().traverse(SWT.TRAVERSE_TAB_NEXT);
				}
			}
		});
	}

    /**
     * 
     */
    protected Icon createDocumentIcon() {
        Icon icon = null;
        try {
		    switch (document.getBillingType()) {
            case INVOICE:
                part.setIconURI(Icon.COMMAND_INVOICE.getIconURI());
                icon = Icon.ICON_INVOICE;
                break;
            case OFFER:
                part.setIconURI(Icon.COMMAND_OFFER.getIconURI());
                icon = Icon.ICON_OFFER;
                break;
            case ORDER:
                part.setIconURI(Icon.COMMAND_ORDER.getIconURI());
                icon = Icon.ICON_ORDER;
                break;
            case CREDIT:
                part.setIconURI(Icon.COMMAND_CREDIT.getIconURI());
                icon = Icon.ICON_CREDIT;
                break;
            case DUNNING:
                part.setIconURI(Icon.COMMAND_DUNNING.getIconURI());
                icon = Icon.ICON_DUNNING;
                break;
            case PROFORMA:
                part.setIconURI(Icon.COMMAND_PROFORMA.getIconURI());
                icon = Icon.ICON_PROFORMA;
                break;
            case LETTER:
                part.setIconURI(Icon.COMMAND_LETTER.getIconURI());
                icon = Icon.ICON_LETTER;
                break;
            case CONFIRMATION:
                part.setIconURI(Icon.COMMAND_CONFIRMATION.getIconURI());
                icon = Icon.ICON_CONFIRMATION;
                break;
            case DELIVERY:
                part.setIconURI(Icon.COMMAND_DELIVERY.getIconURI());
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
        if (document.getBillingType().isORDER() && defaultValuePrefs.getBoolean(Constants.PREFERENCES_DOCUMENT_CUSTOMER_STATISTICS_DIALOG)) {
			CustomerStatistics customerStaticstics = ContextInjectionFactory.make(CustomerStatistics.class, context);
			
			DocumentReceiver documentReceiver = addressManager.getBillingAdress(document);
			customerStaticstics.setContact(documentReceiver);
			if(documentReceiver.getOriginContactId() != null) {
				// only relevant if a "real" contact was selected
				if (defaultValuePrefs.getInt(Constants.PREFERENCES_DOCUMENT_CUSTOMER_STATISTICS_COMPARE_ADDRESS_FIELD) == 1) {
					customerStaticstics.setAddress(contactUtil.getAddressAsString(documentReceiver));
		            customerStaticstics.makeStatistics(true);
				} else {	
	                customerStaticstics.makeStatistics(false);
				}
			}
			
			if (customerStaticstics.hasPaidInvoices()) {
				//T: Message Dialog
				MessageDialog.openInformation(parent.getShell(), 
						//T: Title of the customer statistics dialog
						msg.dialogMessageboxTitleInfo,
						//T: Part of the customer statistics dialog
						// the unescapeJava is because of the Newlines in the message format string
						MessageFormat.format(msg.dialogCustomerStatisticsPart1, 
						        document.getAddressFirstLine(),
						        customerStaticstics.getOrdersCount(),
						        customerStaticstics.getLastOrderDate(),
						        customerStaticstics.getInvoices(),
						        numberFormatterService.doubleToFormattedPrice(customerStaticstics.getTotal())));
			}
		}
    }

    private Map<String, Object> createCommandParams(DocumentType docType) {
        Map<String, Object> params = new HashMap<>();
        params.put(CallEditor.PARAM_EDITOR_TYPE, DocumentEditor.ID);
        params.put(CallEditor.PARAM_CATEGORY, docType.name());
        params.put(CallEditor.PARAM_FOLLOW_UP, Boolean.TRUE);
        return params;
    }


    @Inject
    @org.eclipse.e4.core.di.annotations.Optional
    protected void handleDialogSelection(@UIEventTopic("DialogSelection/*") Event event) {
        if (event != null) {
            // the event has already all given params in it since we created them as Map
            String targetDocumentName = (String) event.getProperty(DOCUMENT_ID);
            // at first we have to check if the message is for us
            if(!StringUtils.equals(targetDocumentName, document.getName())) {
                // silently ignore this event if it's not for this document
                return; 
            }
            
            boolean isChanged = false;
            String topic = StringUtils.defaultString(event.getTopic());
            String subTopic = "";
            String[] topicName = topic.split(UIEvents.TOPIC_SEP);
            if (topicName.length > 1) {
                subTopic = topicName[1];
            }

            switch (subTopic) {
            case "Contact":
                Long addressId = (Long) event.getProperty(ContactTreeListTable.SELECTED_ADDRESS_ID);
                
                Address address = contactDAO.findByAddressId(addressId);
                if(address == null) {
                	log.error(String.format("Something weird happened. Selected Address with ID %ld couldn't be found in your database.", addressId));
                	return;
                }

                // this selected contact is from now on the main receiver for this document
                DocumentReceiver documentReceiver = addressManager.createDocumentReceiverFromAddress(address, document.getBillingType());
                
                /*
                 * If a Contact is selected as DocumentReceiver it has to be added to the current Document. But if another
                 * {@link DocumentReceiver} for the same {@link BillingType} exists it has to be replaced.
                 */
                document = addressManager.addOrReplaceReceiverToDocument(document, documentReceiver); 
                setAddress(address, documentReceiver);
                isChanged = true;
                break;
            case "Product":
                // select a product (for an item entry)
                // Get the array list of all selected elements
                @SuppressWarnings("unchecked")
                List<Long> selectedIds = (List<Long>)event.getProperty(ProductListTable.SELECTED_PRODUCT_ID);
                if(!selectedIds.isEmpty()) {
	                List<Product> selectedProducts = productsDAO.findSelectedProducts(selectedIds);
	                addItemsToItemList(selectedProducts);
	                isChanged = true;
                }
                break;
            case "Delivery":
                // select a delivery note for creating a collective invoice 
                Document[] selectedDeliveries = (Document[]) event.getProperty(DocumentsListTable.SELECTED_DELIVERY_ID);
                // Get the array list of all selected elements
                for (Document deliveryNote : selectedDeliveries) {
                    // Get all items by ID from the item string
                    List<DocumentItem> itemsString = deliveryNote.getItems();
                    for (DocumentItem documentItem : itemsString) {
	                    // And copy the item to a new one
//	                    DocumentItem newItem = modelFactory.createDocumentItem();
	                    DocumentItem newItem = documentItem.clone();
	                    // Add the new item
	                    itemListTable.addNewItem(new DocumentItemDTO(newItem));
					}
                    
                    // Put the number of the delivery note in a new line of the message field
                    if (defaultValuePrefs.getBoolean(Constants.PREFERENCES_DOCUMENT_ADD_NR_OF_IMPORTED_DELIVERY_NOTE)) {
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
                        pendingDeliveryMerges.add(deliveryNote);
                    }
                    else
                        importedDeliveryNotes.add(deliveryNote.getId());
                }
//                    tableViewerItems.refresh();
//                    if (newItem!= null)
//                        tableViewerItems.reveal(newItem);
                calculate();
                isChanged = selectedDeliveries.length > 0;
                break;
            case "TextModule":
                Long textModuleId = (Long) event.getProperty(TextListTable.SELECTED_TEXT_ID);
                TextModule text = textsDAO.findById(textModuleId);
                  
              // Insert the selected text in the message text (selected widget is set in the calling method)
              // look at addMessageButton
                  if (text != null && selectedMessageField != null) {
                      int begin = selectedMessageField.getSelection().x;
                      int end = selectedMessageField.getSelection().y;
                      String s = selectedMessageField.getText();
                      String s1 = s.substring(0, begin);
                      String s2 = text.getText();

                      selectedMessageField.setText(String.format("%s%s%s", s1, s2, s.substring(end, s.length())));
                      selectedMessageField.setSelection(s1.length() + s2.length());
                      isChanged = true;
                  }
                break;
            default:
                break;
            }
            setDirty(isChanged);
        }
    }

	/**
	 * @param selectedProducts
	 */
    private void addItemsToItemList(Collection<Product> selectedProducts) {
    	DocumentType documentType = DocumentTypeUtil.findByBillingType(document.getBillingType());
		for (Product product : selectedProducts) {
			DocumentItem newItem = documentItemUtil.from(product, documentType);

		    // Use the products description, or clear it
		    if (!defaultValuePrefs.getBoolean(Constants.PREFERENCES_DOCUMENT_COPY_PRODUCT_DESCRIPTION_FROM_PRODUCTS_DIALOG)) {
		        newItem.setDescription("");
		    }
		    itemListTable.addNewItem(new DocumentItemDTO(newItem));
		}
		setDirty(true);
		calculate();
	} 

    /**
     * Searches for the standard {@link Shipping} entry.
     */
    private Shipping lookupDefaultShippingValue() {
        long stdID = 1L;
        Shipping retval = null;

        // Get the ID of the standard entity from preferences
        stdID = defaultValuePrefs.getLong(Constants.DEFAULT_SHIPPING);
        retval = shippingsDAO.findById(stdID);
        if(retval == null) {
	        // Panic! Something went extremely wrong! Show an error dialog.
	        MessageDialog.openError(this.part.getContext().get(Shell.class), msg.dialogMessageboxTitleError, MessageFormat.format(msg.editorDocumentDialogNodefaultvalue, msg.commandShippingsName));
        }
        return retval;
    }

    private void createToolItem(final ToolBar toolBar, final String commandId, 
            final String commandName, final String tooltip, final Image iconImage,
            Map<String, Object> params) {
        
        ToolItem item = new ToolItem(toolBar, SWT.PUSH);
        final ParameterizedCommand pCmd = commandService.createCommand(commandId, params);
        try {
            item.setText(commandName != null ? commandName : pCmd.getCommand().getName());
            item.setToolTipText((tooltip != null) ? tooltip : pCmd.getCommand().getDescription());
            item.setEnabled(pCmd.getCommand().isEnabled());
            item.setData(TOOLITEM_COMMAND, pCmd);
        }
        catch (NotDefinedException e1) {
            log.error(e1, "Unknown command or creation of a parameterized command failed!");
        }
        item.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { 
            	// at first try to save if document wasn't saved
            	if(getMDirtyablePart().isDirty()) {
            		doSave(null);
            	}
            	
            	BillingType targetType = BillingType.get((String) params.get(CallEditor.PARAM_CATEGORY));
            	if(!copyExists(document, targetType)) {            	
	                params.put(CallEditor.PARAM_OBJ_ID, Long.toString(document.getId()));
	                ParameterizedCommand pCmdCopy = commandService.createCommand(commandId, params);
	                if (handlerService.canExecute(pCmdCopy)) {
	                    handlerService.executeHandler(pCmdCopy);
	                } else {
	                    MessageDialog.openInformation(toolBar.getShell(),
	                            msg.dialogMessageboxTitleError, "current action can't be executed!");
	                }
            	}
        }));
        item.setImage(iconImage);
    }
    
    /**
     * Checks if a follow-up document already exists. If so, the user is asked
     * to confirm the copy. 
     * 
     * @param document the {@link Document} to be checked (source document)
     * @param targetype the target to which this document should be copied
     * @return
     */
	final private boolean copyExists(final Document document, final BillingType targetype) {
		boolean retval = false;
		// under Linux the focus is removed and we get an exception (see issue #601)	
		if (!Util.isLinux() && document != null && document.getTransactionId() != null) {
			Document copyDoc = null;
			// dunning is an extra case
			if (targetype.isDUNNING()) {
				// if the given document is also a dunning, increase the
				// dunning level
				int lookupDunningLevel = (document.getBillingType().isDUNNING())
						? ((Dunning) document).getDunningLevel() + 1 : 1;
				copyDoc = documentsDAO.findDunningByTransactionId(document.getTransactionId(), lookupDunningLevel);
			} else {
				// lookup for a document with the same transaction id and
				// the given target type
				copyDoc = documentsDAO.findExistingDocumentByTransactionIdAndBillingType(document.getTransactionId(), targetype);
			}
			if (copyDoc != null) {
				// the retval has to be inverted because the question asks
				// if you want to create another copy
				retval = !MessageDialog.openQuestion(top.getShell(), msg.dialogMessageboxTitleWarning,
						MessageFormat.format(msg.editorDocumentDialogWarningCopyexists, copyDoc.getName()));
			}
		}
		return retval;
	}    

	/**
	 * Set the focus to the top composite.
	 * 
	 * @see com.sebulli.fakturama.editors.Editor#setFocus()
	 */
	@Focus
	public void setFocus() {
		if(addressAndIconComposite != null) 
			addressAndIconComposite.setFocus();
	}

	/**
	 * Test, if there is a document with the same number
	 * 
	 * @return TRUE, if one with the same number is found
	 */
	private boolean thereIsOneWithSameNumber() {
		// Letters do not have to be checked
		if (document.getBillingType().isLETTER())
			return false;

		// Cancel, if there is already a document with the same ID
		if (documentsDAO.existsOther(document)) {
			// Display an error message
		    MessageDialog.openError(top.getShell(), msg.editorDocumentErrorDocnumberTitle, MessageFormat.format(msg.editorDocumentDialogWarningDocumentexists, txtName.getText()));
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

	public AbstractViewDataTable<DocumentItemDTO, DummyStringCategory> getItemsList() {
		return itemListTable;
	}
    
	@Override
	protected String getEditorID() {
	    return document.getBillingType().getName();
	}
	
    @Override
    protected MDirtyable getMDirtyablePart() {
        return part;
    }
    
    public void setDirty(boolean isDirty) {
    	getMDirtyablePart().setDirty(isDirty);
    }

    /**
     * This method is for setting the dirty state to <code>true</code>. This
     * happens if e.g. the items list has changed. (could be sent from
     * DocumentListTable)
     */
    @Inject
    @org.eclipse.e4.core.di.annotations.Optional
    protected void handleItemChanged(@UIEventTopic(EDITOR_ID + "/itemChanged") Event event) {
        if (event != null) {
            // the event has already all given params in it since we created them as Map
            String targetDocumentName = (String) event.getProperty(DOCUMENT_ID);
            // at first we have to check if the message is for us
            if (!StringUtils.equals(targetDocumentName, document.getName())) {
                // if not, silently ignore this event
                return;
            }
            // (re)calculate summary
            // TODO check if this has to be done in a synchronous or asynchronous call
            // within UISynchronize
            if ((Boolean) event.getProperty(DOCUMENT_RECALCULATE)) {
                calculate();
            }
            setDirty(true);
        }
    }    
    
    /**
     * If an entity is deleted via list view we have to close a possibly open
     * editor window. Since this is triggered by a UIEvent we named this method
     * "handle*".
     */
    @Inject
    @Optional
    public void handleForceClose(@UIEventTopic(DocumentEditor.EDITOR_ID + "/forceClose") Event event) {
        //      sync.syncExec(() -> top.setRedraw(false));
        // the event has already all given params in it since we created them as Map
        String targetDocumentName = (String) event.getProperty(DOCUMENT_ID);
        // at first we have to check if the message is for us
        if (!StringUtils.equals(targetDocumentName, document.getName())) {
            // if not, silently ignore this event
            return;
        }
        partService.hidePart(part, true);
        //  sync.syncExec(() -> top.setRedraw(true));
    }

//    
//    @Inject
//    @org.eclipse.e4.core.di.annotations.Optional
//    protected void handleRecalculationRequest(@UIEventTopic(EDITOR_ID + "/recalculation") Event event) {
//    }    
    
    @Override
    protected Class<Document> getModelClass() {
        return Document.class;
    }

    /**
     * @param section 
     * @return 
     * 
     */
    private IDialogSettings getDialogSettings(String section) {
        if(settings.getSection(section) == null) {
            settings.addNewSection(section);
        }
        return settings.getSection(section);
    }
}
