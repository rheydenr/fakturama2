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


import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateListStrategy;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.databinding.fieldassist.ControlDecorationSupport;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.nebula.widgets.formattedtext.PercentFormatter;
import org.eclipse.nebula.widgets.opal.multichoice.MultiChoice;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.osgi.service.event.Event;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.ContactCategoriesDAO;
import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.Address_;
import com.sebulli.fakturama.model.BankAccount_;
import com.sebulli.fakturama.model.CategoryComparator;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.model.Contact_;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.IEntity;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.ReliabilityType;
import com.sebulli.fakturama.parts.converter.CategoryConverter;
import com.sebulli.fakturama.parts.converter.EntityConverter;
import com.sebulli.fakturama.parts.converter.StringToCategoryConverter;
import com.sebulli.fakturama.parts.converter.StringToEntityConverter;
import com.sebulli.fakturama.parts.widget.contentprovider.EntityComboProvider;
import com.sebulli.fakturama.parts.widget.contentprovider.HashMapContentProvider;
import com.sebulli.fakturama.parts.widget.contentprovider.StringHashMapContentProvider;
import com.sebulli.fakturama.parts.widget.labelprovider.ContactTypeLabelProvider;
import com.sebulli.fakturama.parts.widget.labelprovider.EntityLabelProvider;
import com.sebulli.fakturama.parts.widget.labelprovider.NumberLabelProvider;
import com.sebulli.fakturama.parts.widget.labelprovider.StringComboBoxLabelProvider;
import com.sebulli.fakturama.util.ContactUtil;
import com.sebulli.fakturama.views.datatable.contacts.ContactListTable;

/**
 * The contact editor.
 * 
 */

public abstract class ContactEditor<C extends Contact> extends Editor<C> {

	/** Editor's ID */
	public static final String ID = "com.sebulli.fakturama.editors.contactEditor";
	
	/**
	 * The widget property identifier (needed for the widgets in the address tab group)
	 */
	public static final String WIDGET_IDENTIFIER = "WIDGET";
	
	public static final String EDITOR_ID = "ContactEditor";

	/** This UniDataSet represents the editor's input */ 
	protected C editorContact;
    
    @Inject
    private EPartService partService;
    
	@Inject
	private ILocaleService localeUtil;

	// SWT widgets of the editor
    private Composite top;

    private TabFolder tabFolder;
    private CTabFolder addressTabFolder;
    private List<AddressTabWidget> addressTabWidgets = new ArrayList<>();
    
	private Text textNote;
	private ComboViewer comboSalutationViewer;
	private Text txtTitle;
	private Text txtFirstname;
	private Text txtName;
	private Text txtCompany;
	private CDateTime dtBirthday;

	private Text txtAccountHolder;
	private Text txtAccount;
	private Text txtBankCode;
	private Text txtBankName;
	private Text txtIBAN;
	private Text txtBIC;
    private Text txtMandatRef;
	private Text txtNr;
	private Combo comboPayment;
	private ComboViewer comboReliability;
	private Text txtPhone;
	private Text txtFax;
	private Text txtMobile;
	private Text txtSupplierNr;
	private Text txtEmail;
	private Text txtWebsite;
	private Text txtWebshopName;
	private Text txtVatNr;
	private Text txtGln;
	private FormattedText txtDiscount;
	private Combo comboCategory;
	private ComboViewer comboUseNetGross;

	// These flags are set by the preference settings.
	// They define, if elements of the editor are displayed, or not.
	private boolean useBank;
	private boolean useMisc;
	private boolean useNote;
	private boolean useSalutation;
	private boolean useTitle;
	private boolean useLastNameFirst;
	private boolean useCompany;
	private boolean useCountry;
	
	/**
	 * If the "duplicate contact" warning ist shown we won't show 'em again 'til
	 * we change a value in street, name or firstname.
	 */
	private boolean isDuplicateWarningShown = false;

	// defines, if the contact is newly created
	private boolean newContact;
	
	/**
	 * Window and Part informations
	 */
	private MPart part;
    
    @Inject
    private ContactCategoriesDAO contactCategoriesDAO;
    
    @Inject
    private PaymentsDAO paymentsDao;
    
    @Inject
    private ESelectionService selectionService;

    private ContactUtil contactUtil;
    private FakturamaModelFactory modelFactory = new FakturamaModelFactory();

	/**
	 * Saves the contents of this part
	 * 
	 * @param monitor
	 *            Progress monitor
	 */
	@Persist
	public Boolean doSave(IProgressMonitor monitor, @Named(IServiceConstants.ACTIVE_SHELL) Shell parent) {

		/*
		 * the following parameters are not saved: 
		 * - id (constant)
		 * - date_added (constant)
		 */

		// TODO bind combo to model!
		// at first, check the category for a new entry
        // (the user could have written a new one into the combo field)
        String testCat = comboCategory.getText();
        // if there's no category we can skip this step
        if(StringUtils.isNotBlank(testCat)) {
            ContactCategory contactCategory = contactCategoriesDAO.getCategory(testCat, true);
            // parentCategory now has the last found Category
            editorContact.setCategories(contactCategory);
        }
		
		// check if the same number was used.
		if(thereIsOneWithSameNumber()) {
			// Save is only allowed, if there is no contact with the same number
			return Boolean.FALSE;
		}

	    // check for a new contact
		if (newContact) {

			// Check, if the contact number is the next one
			int result = setNextFreeNumberInPrefStore(txtNr.getText(), Contact_.customerNumber.getName());

			// It's not the next free ID
			if (result == ERROR_NOT_NEXT_ID) {
				// Display an error message
				MessageBox messageBox = new MessageBox(parent, SWT.ICON_ERROR | SWT.OK);

				//T: Title of the dialog that appears if the item/product number is not valid.
				messageBox.setText(msg.editorContactErrorCustomerid);

				//T: Text of the dialog that appears if the customer number is not valid.
				messageBox.setMessage(MessageFormat.format(msg.editorContactErrorNotnextfreenumber, txtNr.getText()) + "\n" + 
						msg.editorContactHintSeepreferences);
				messageBox.open();
				throw new RuntimeException(MessageFormat.format(msg.editorContactErrorNotnextfreenumber, txtNr.getText()));
			}

		}

//		// If the Check Box "Address equals delivery address" is set,
//		// all the address data is copied to the delivery address
		// => this is not necessary because we use the contact info if there's no
		// delivery address
//		if (bDelAddrEquAddr.getSelection())
//			copyAddressToDeliveryAdress();

		// Always set the editor's data set to "undeleted"
		editorContact.setDeleted(Boolean.FALSE);
		
		// Set the address data
		// ... done through databinding...		
// TODO?		contact.setCompany(DataUtils.removeCR(txtCompany.getText()));

		// Set the delivery address data
		// ... done through databinding...
// TODO?		contact.setDeliveryCompany(DataUtils.removeCR(txtDeliveryCompany.getText()));

		// Set the bank data
		// Set the customer number
		// Set the payment ID
		// Set the miscellaneous data
		// Set the note
        // ... done through databinding...
// TODO ?		contact.setNote(DataUtils.removeCR(textNote.getText()));
		
		if(editorContact.getDiscount() != null && editorContact.getDiscount().compareTo(Double.valueOf(0.0)) > 0) {
		    editorContact.setDiscount(editorContact.getDiscount() * -1); // discount has to be negative
		}
		
//		// remove any manual added address
//		editorContact.getAddress().setManualAddress(null);
//		if(editorContact.getAlternateContacts() != null && editorContact.getAlternateContacts().getAddress() != null) {
//			editorContact.getAlternateContacts().getAddress().setManualAddress(null);
//		}

        try {
            // save the new or updated Contact
            editorContact = getContactsDao().update(editorContact);
        }
        catch (FakturamaStoringException e) {
            log.error(e, "can't save the current Contact: " + editorContact.toString());
            MessageDialog.openError(parent, msg.dialogMessageboxTitleError, "Can't save data! Please see log file.\n");
            return Boolean.FALSE;
        }
		newContact = false;
		
		// Set the Editor's name to the first name and last name of the contact.
		String nameWithCompany = contactUtil.getNameWithCompany(editorContact);
		if(nameWithCompany.contains("\r")) {
			nameWithCompany = nameWithCompany.split("\\r")[0];
		} else if (nameWithCompany.contains("\n")) {
			nameWithCompany = nameWithCompany.split("\\n")[0];
		}
		part.setLabel(nameWithCompany);

		// Refresh the table view of all contacts
        evtBroker.post(getEditorID(), Editor.UPDATE_EVENT);

//      if the editor was called from DialogEditor we have to 
//      return the new contact
        Map<String, Object> eventParams = new HashMap<>();
        // the transientData HashMap contains the target document number
        // (was set in MouseEvent handler)
        String callerDocument = (String) part.getProperties().get(CallEditor.PARAM_CALLING_DOC);
        if(callerDocument != null) {
            eventParams.put(DocumentEditor.DOCUMENT_ID, callerDocument);
            eventParams.put(ContactListTable.SELECTED_CONTACT_ID, Long.valueOf(editorContact.getId()));
            selectionService.setSelection(editorContact);
            evtBroker.post("DialogSelection/Contact", eventParams);
        }
        
        bindModel();
        
        // reset dirty flag
        getMDirtyablePart().setDirty(false);
        return Boolean.TRUE;
	}

	protected abstract AbstractDAO<C> getContactsDao();

	/**
	 * Initializes the editor. If an existing data set is opened, the local
	 * variable "contact" is set to this data set. If the editor is opened to
	 * create a new one, a new data set is created and the local variable
	 * "contact" is set to this one.
     * If we get an ID from the opening command we try to open the given
     * {@link Contact}.
	 * 
	 * @param input
	 *            The editor's input
	 * @param site
	 *            The editor's site
	 */
	@PostConstruct
	public void init(Composite parent) {
	    contactUtil = ContextInjectionFactory.make(ContactUtil.class, context);
        Long objId = null;
        this.part = (MPart) parent.getData("modelElement");
        this.part.setIconURI(getEditorIconURI());
        String tmpObjId = (String) part.getProperties().get(CallEditor.PARAM_OBJ_ID);
        if (StringUtils.isNumeric(tmpObjId)) {
            objId = Long.valueOf(tmpObjId);
            // Set the editor's data set to the editor's input
            editorContact = getContactsDao().findById(objId);
        }

		// Test, if the editor is opened to create a new data set. This is,
		// if there is no input set.
		newContact = (editorContact == null);

		// If new ... 
		if (newContact) {
//            String category = (String) part.getProperties().get(CallEditor.PARAM_EDITOR_TYPE);

			// Create a new data set and set some defaults
            editorContact = createNewContact(modelFactory);
            
			//T: Contact Editor Title of the editor if the data set is a new one.
			setPartLabelForNewContact(part);

			// Set the payment to the standard value
			long paymentId = defaultValuePrefs.getLong(Constants.DEFAULT_PAYMENT);
			Payment defaultPayment = paymentsDao.findById(paymentId);
			editorContact.setPayment(defaultPayment);
			editorContact.setReliability(ReliabilityType.NONE);
			// it's not known to me why Texo generates a default value of "1" for this field.
			// I'ver never given it to the model!
			// Therefore, to keep it in sync with old version, I've changed it here back to "uncertain" (which is 0).
			editorContact.setUseNetGross((short)0);
			
			// country is determined by locale
//			editorContact.getAddress().setCountryCode(localeUtil.getDefaultLocale().getCountry());

			// Get the next contact number
			editorContact.setCustomerNumber(getNextNr());
			
            // a new contact is always dirty...
            // setDirty(true);

		}
		else {

			// Set the Editor's name to the first name and last name of the contact.
			part.setLabel(contactUtil.getNameWithCompany(editorContact));
		}
		
		createPartControl(parent);
	}

	/**
	 * Sets the label for a new contact (which is either a debtor or a creditor).
	 * @param part2
	 */
	protected abstract void setPartLabelForNewContact(MPart part2);

	/**
	 * Gets the editor icon uri.
	 *
	 * @return the editor icon uri
	 */
	protected abstract String getEditorIconURI();

	/**
	 * Creates the new contact.
	 *
	 * @param modelFactory the model factory
	 * @return the contact
	 */
	protected abstract C createNewContact(FakturamaModelFactory modelFactory2);
	
    /**
     * If an entity is deleted via list view we have to close a possibly open
     * editor window. Since this is triggered by a UIEvent we named this method
     * "handle*".
     */
    @Inject
    @Optional
    public void handleForceClose(@UIEventTopic(ContactEditor.EDITOR_ID + "/forceClose") Event event) {
        // the event has already all given params in it since we created them as Map
        String targetDocumentName = (String) event.getProperty(DocumentEditor.DOCUMENT_ID);
        // at first we have to check if the message is for us
        if (!StringUtils.equals(targetDocumentName, editorContact.getName())) {
            // if not, silently ignore this event
            return;
        }
        partService.hidePart(part, true);
    }


	/**
	 * Creates the SWT controls for this workbench part
	 * 
	 * @param the
	 *            parent control
	 */
	public void createPartControl(Composite parent) {

		// Some of this editors' control elements can be hidden.
		// Get the these settings from the preference store
		useBank = defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_BANK);
		useMisc = defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_MISC);
		useNote = defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_NOTE);
		useSalutation = defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_GENDER);
		useTitle = defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_TITLE);
		useLastNameFirst = (defaultValuePrefs.getInt(Constants.PREFERENCES_CONTACT_NAME_FORMAT) == 1);
		useCompany = defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_COMPANY);
		useCountry = defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_COUNTRY);

		// Create the ScrolledComposite to scroll horizontally and vertically
	    ScrolledComposite scrollcomposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);

		// Create the top Composite
		top = new Composite(scrollcomposite, SWT.SCROLLBAR_OVERLAY | SWT.NONE );  //was parent before 

		scrollcomposite.setContent(top);
		scrollcomposite.setMinSize(1200, 600);   // 2nd entry should be adjusted to higher value when new fields will be added to composite 
		scrollcomposite.setExpandHorizontal(true);
		scrollcomposite.setExpandVertical(true);
        scrollcomposite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,false,true));
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(top);

		// Create an invisible container for all hidden components	
		Composite invisible = new Composite(top, SWT.NONE);
		invisible.setVisible(false);
		GridDataFactory.fillDefaults().hint(0, 0).applyTo(invisible);

		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ContextHelpConstants.CONTACT_EDITOR);

		createCustomerNumberWidget();

		// Create the address tab
		Composite tabAddress;
		if (useBank || useMisc || useNote) {
			tabFolder = new TabFolder(top, SWT.NONE);
			GridDataFactory.fillDefaults().grab(true, true).applyTo(tabFolder);

			TabItem item1 = new TabItem(tabFolder, SWT.NONE);
			//T: Label in the contact editor
			item1.setText(msg.editorContactLabelAddress);
			tabAddress = new Composite(tabFolder, SWT.NONE);
			item1.setControl(tabAddress);
		}
		else {
			tabAddress = new Composite(top, SWT.NONE);
		}
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(tabAddress);

		// Create the bank tab
		Composite tabBank;
		if (useBank) {
			TabItem item3 = new TabItem(tabFolder, SWT.NONE);
			//T: Label in the contact editor
			item3.setText(msg.editorContactLabelBankaccount);
			tabBank = new Composite(tabFolder, SWT.NONE);
			item3.setControl(tabBank);
			if(editorContact.getBankAccount() == null) {
				editorContact.setBankAccount(modelFactory.createBankAccount());
			}
		}
		else {
			tabBank = new Composite(invisible, SWT.NONE);
		}
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(tabBank);

		// Create the miscellaneous tab
		Composite tabMisc;
		if (useMisc) {
			TabItem item4 = new TabItem(tabFolder, SWT.NONE);
			//T: Label in the contact editor
			item4.setText(msg.commandNavigationMisc);
			tabMisc = new Composite(tabFolder, SWT.NONE);
			item4.setControl(tabMisc);
		}
		else {
			tabMisc = new Composite(invisible, SWT.NONE);
		}
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(tabMisc);

		// Create to note tab
		TabItem item5 = null;
		Composite tabNote;
		if (useNote) {
			item5 = new TabItem(tabFolder, SWT.NONE);
			//T: Label in the contact editor
			item5.setText(msg.editorContactLabelNotice);
			tabNote = new Composite(tabFolder, SWT.NONE);
			item5.setControl(tabNote);
		}
		else {
			tabNote = new Composite(invisible, SWT.NONE);
		}
		tabNote.setLayout(new FillLayout());

		// Group: address
		createAddressGroup(invisible, tabAddress);
		
		// Controls in the tab "Bank"

		// Account holder
		Label labelAccountHolder = new Label(tabBank, SWT.NONE);
		//T: Label in the contact editor
		labelAccountHolder.setText(msg.commonFieldAccountholder);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelAccountHolder);
		txtAccountHolder = new Text(tabBank, SWT.BORDER);
		GridDataFactory.fillDefaults().span(3, SWT.DEFAULT).grab(true, false).applyTo(txtAccountHolder);

		// Bank code
		Label labelBankCode = new Label(tabBank, SWT.NONE);
		//T: Label in the contact editor
		labelBankCode.setText(msg.editorContactFieldBankcodeName);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelBankCode);
		txtBankCode = new Text(tabBank, SWT.BORDER);
		txtBankCode.setToolTipText(msg.editorContactFieldBankcodeDisabledinfo);
		txtBankCode.setEnabled(false);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtBankCode);

		// Account number
		Label labelAccount = new Label(tabBank, SWT.NONE);
		//T: Label in the contact editor
		labelAccount.setText(msg.editorContactFieldAccountnumberName);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelAccount);
		txtAccount = new Text(tabBank, SWT.BORDER);
		txtAccount.setEnabled(false);
		txtAccount.setToolTipText(msg.editorContactFieldAccountnumberDisabledinfo);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtAccount);

		// Name of the bank
		Label labelBankName = new Label(tabBank, SWT.NONE);
		//T: Label in the contact editor
		labelBankName.setText(msg.editorContactFieldBankName);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelBankName);
		txtBankName = new Text(tabBank, SWT.BORDER);
		GridDataFactory.fillDefaults().span(3, SWT.DEFAULT).grab(true, false).applyTo(txtBankName);

		// BIC
		Label labelBIC = new Label(tabBank, SWT.NONE);
		//T: Bank code
		labelBIC.setText(msg.exporterDataBic);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelBIC);
		txtBIC = new Text(tabBank, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtBIC);

		// IBAN Bank code
		Label labelIBAN = new Label(tabBank, SWT.NONE);
		//T: Bank code
		labelIBAN.setText(msg.exporterDataIban);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelIBAN);
		txtIBAN = new Text(tabBank, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtIBAN);
        
        // Customer's Mandat reference
        Label labelMandate = new Label(tabBank, SWT.NONE);
        //T: Mandate reference
        labelMandate.setText(msg.editorContactFieldMandaterefName);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelMandate);
        txtMandatRef = new Text(tabBank, SWT.BORDER);
        GridDataFactory.fillDefaults().span(3, SWT.DEFAULT).grab(true, false).applyTo(txtMandatRef); 

		// Controls in tab "Misc"

		// Category 
		Label labelCategory = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelCategory.setText(msg.commonFieldCategory);
		//T: Tool Tip Text
		labelCategory.setToolTipText(msg.editorContactFieldCategoryTooltip);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCategory);

//		createCategoryCombo();
        comboCategory = new Combo(tabMisc, SWT.BORDER);
        comboCategory.setToolTipText(labelCategory.getToolTipText());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(comboCategory);

		// Suppliernumber
		Label labelSupplier = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelSupplier.setText(msg.editorContactFieldSuppliernumberName);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelSupplier);
		txtSupplierNr = new Text(tabMisc, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtSupplierNr);
		
		final Cursor cursorHand = top.getDisplay().getSystemCursor(SWT.CURSOR_HAND);
		final Cursor cursorIBeam = top.getDisplay().getSystemCursor(SWT.CURSOR_IBEAM);
		
		// EMail
		Label labelEmail = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelEmail.setText(msg.exporterDataEmail);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelEmail);
		txtEmail = new Text(tabMisc, SWT.BORDER);
		txtEmail.addMouseMoveListener((e) -> {
			if (e.stateMask == SWT.CTRL) {
				txtEmail.setCursor(cursorHand);
			} else {
				txtEmail.setCursor(cursorIBeam);
			}
		});
		txtEmail.addMouseListener(new UrlCallHandler(txtEmail, log));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtEmail);

		// Telephone
		Label labelTel = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelTel.setText(msg.exporterDataTelephone);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelTel);
		txtPhone = new Text(tabMisc, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtPhone);

		// Telefax
		Label labelFax = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelFax.setText(msg.exporterDataTelefax);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelFax);
		txtFax = new Text(tabMisc, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtFax);

		// Mobile
		Label labelMobile = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelMobile.setText(msg.exporterDataMobile);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelMobile);
		txtMobile = new Text(tabMisc, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtMobile);

		// Web Site
		Label labelWebsite = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelWebsite.setText(msg.exporterDataWebsite);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelWebsite);
		txtWebsite = new Text(tabMisc, SWT.BORDER);
		
		txtWebsite.addMouseMoveListener((e) -> {
			if (e.stateMask == SWT.CTRL) {
				txtWebsite.setCursor(cursorHand);
			} else {
				txtWebsite.setCursor(cursorIBeam);
			}
		});
		
		// FAK-382 clickable URL
		txtWebsite.addMouseListener(new UrlCallHandler(txtWebsite, log));
		
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtWebsite);
		
		// WebShop name  
		Label labelWebshopName = new Label(tabMisc, SWT.NONE);
		labelWebshopName.setText(msg.exporterDataWebshopname);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelWebshopName);
		txtWebshopName = new Text(tabMisc, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtWebshopName);

		// Payment
		Label labelPayment = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelPayment.setText(msg.editorContactFieldPaymentName);
		//T: Tool Tip Text
		labelPayment.setToolTipText(msg.editorContactFieldPaymentTooltip);

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelPayment);
        // Combo to select the payment
        comboPayment = new Combo(tabMisc, SWT.BORDER | SWT.READ_ONLY);

		// Reliability
		Label labelReliability = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelReliability.setText(msg.editorContactFieldReliabilityName);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelReliability);
		comboReliability = new ComboViewer(tabMisc, SWT.BORDER | SWT.READ_ONLY);
		comboReliability.setContentProvider(ArrayContentProvider.getInstance());
		comboReliability.setInput(ReliabilityType.values());
		comboReliability.setLabelProvider(new LabelProvider() {
		    @Override
		    public String getText(Object element) {
		        ReliabilityType type = (ReliabilityType)element;
		        switch (type) {
                case NONE:
                    return "---";
                case POOR:
                    return msg.contactFieldReliabilityPoorName;
                case MEDIUM:
                    return msg.contactFieldReliabilityMediumName;
                case GOOD:
                    return msg.contactFieldReliabilityGoodName;
                default:
                    return null;
                }
		    }
		});

		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboReliability.getControl());
		
		// VAT number
		Label labelVatNr = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelVatNr.setText(msg.exporterDataVatno);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelVatNr);
		txtVatNr = new Text(tabMisc, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtVatNr);
		
		// GLN
		Label labelGln = new Label(tabMisc, SWT.NONE);
		labelGln.setText(msg.contactFieldGln);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelGln);
		txtGln = new Text(tabMisc, SWT.BORDER);
		txtGln.setToolTipText(msg.contactFieldGlnTooltip);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtGln);

		// Customer's discount
		Label labelDiscount = new Label(tabMisc, SWT.NONE);
		//T: Customer's discount
		labelDiscount.setText(msg.exporterDataRebate);
		labelDiscount.setToolTipText(msg.editorContactFieldDiscountTooltip);

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDiscount);
		txtDiscount = new FormattedText(tabMisc, SWT.BORDER);
		txtDiscount.setFormatter(new PercentFormatter());
		txtDiscount.getControl().setToolTipText(labelDiscount.getToolTipText());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtDiscount.getControl());

		// Use net or gross
		Label labelNetGross = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelNetGross.setText(msg.editorContactFieldNetgrossName);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelNetGross);
		comboUseNetGross = new ComboViewer(tabMisc, SWT.BORDER | SWT.READ_ONLY);
		comboUseNetGross.setContentProvider(ArrayContentProvider.getInstance());
		comboUseNetGross.setInput(new Short[]{0, 1, 2});
		comboUseNetGross.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                Short type = (Short)element;
                switch (type) {
                case 0:
                    return "---";
                case 1:
                    return msg.productDataNet;
                case 2:
                    return msg.productDataGross;
                default:
                    return null;
                }
            }
        });
		
		createAdditionalFields(tabMisc);
		
		// If the value is -1, use 0 instead
		if (editorContact.getUseNetGross() == null || editorContact.getUseNetGross() < 0) {
			editorContact.setUseNetGross((short) 0);
		}

		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboUseNetGross.getCombo());

		// Controls in tab "Note"

		// The note
//		String note = DataUtils.makeOSLineFeeds(editorContact.getNote());
		textNote = new Text(tabNote, SWT.BORDER | SWT.MULTI);

		// If the note is not empty, display it,
		// when opening the editor.
		if (useNote && StringUtils.isNotEmpty(editorContact.getNote()))
			tabFolder.setSelection(item5);

        bindModel();
	}

	private void createCustomerNumberWidget() {
		// Composite for the customer's number
		Composite customerNrComposite = new Composite(top, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(customerNrComposite);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(customerNrComposite);

		// Customer's number
		Label labelNr = new Label(customerNrComposite, SWT.NONE);
		//T: Label in the contact editor
		labelNr.setText(msg.editorContactFieldNumberName);
		//T: Tool Tip Text
		labelNr.setToolTipText(msg.editorContactFieldNumberTooltip);

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelNr);
		txtNr = new Text(customerNrComposite, SWT.BORDER);
		txtNr.setToolTipText(labelNr.getToolTipText());
		GridDataFactory.swtDefaults().hint(400, SWT.DEFAULT).applyTo(txtNr);
	}

	private void createAddressGroup(Composite invisible, Composite tabAddress) {
		// now do some helpful initializations (needed for combo boxes)
        Map<String, String> countryNames = localeUtil.getLocaleCountryMap();
		
		Map<Integer, String> salutationList = new HashMap<>();
		for (int i = 0; i <= ContactUtil.MAX_SALUTATION_COUNT; i++) {
		    salutationList.put(i, contactUtil.getSalutationString(i));
		} 

		// Controls in the group "address"

		// Company
		Label labelCompany = new Label(useCompany ? tabAddress : invisible, SWT.NONE);
		//T: Label in the contact editor
		labelCompany.setText(msg.commonFieldCompany);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCompany);
		txtCompany = new Text(useCompany ? tabAddress : invisible, SWT.BORDER | SWT.MULTI);
//		txtCompany.setText(DataUtils.makeOSLineFeeds(editorContact.getCompany()));
		GridDataFactory.fillDefaults().hint(210, 40).grab(true, false).span(2, 1).applyTo(txtCompany);

		// The title and gender's label
		Label labelTitle = new Label((useSalutation || useTitle) ? tabAddress : invisible, SWT.NONE);
		if (useSalutation) {
			labelTitle.setText(msg.commonFieldGender);
		} else if (useSalutation && useTitle) {
			labelTitle.setText(labelTitle.getText() + ", ");
		} else if (useTitle) {
			//T: "Title" ( part of an address)
			labelTitle.setText(labelTitle.getText() + msg.commonFieldTitle);
		}
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelTitle);
		
		// Salutation
		comboSalutationViewer = new ComboViewer(useSalutation ? tabAddress : invisible, SWT.BORDER | SWT.READ_ONLY);
		comboSalutationViewer.setContentProvider(new HashMapContentProvider<Integer, String>());
/*
		allSalutations = itemListTypeDao.findAllSalutations();
		comboSalutationViewer.setContentProvider(new EntityComboProvider());
		comboSalutationViewer.setLabelProvider(new EntityLabelProvider());
		comboSalutationViewer.setInput(allSalutations);
 */
		comboSalutationViewer.setInput(salutationList);
		comboSalutationViewer.setLabelProvider(new NumberLabelProvider<Integer, String>(salutationList));
		GridDataFactory.fillDefaults().grab(false, false).hint(100, SWT.DEFAULT).span(useTitle ? 1 : 2, 1).applyTo(comboSalutationViewer.getControl());

		// Title
		txtTitle = new Text(useTitle ? tabAddress : invisible, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(useSalutation ? 1 : 2, 1).applyTo(txtTitle);

		// First and last name		
		Label labelName = new Label(tabAddress, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);
		if (useLastNameFirst) {
			//T: Format of the name in an address
			labelName.setText(msg.editorContactFieldLastnamefirstnameName);
			txtName = new Text(tabAddress, SWT.BORDER);
			GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).applyTo(txtName);
			txtFirstname = new Text(tabAddress, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(txtFirstname);
		}
		else {
			//T: Format of the name in an address
			labelName.setText(msg.editorContactFieldFirstnamelastnameName);
			txtFirstname = new Text(tabAddress, SWT.BORDER);
			GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).grab(true, false).applyTo(txtFirstname);
			txtName = new Text(tabAddress, SWT.BORDER);
			GridDataFactory.fillDefaults().hint(100, SWT.DEFAULT).grab(true, false).applyTo(txtName);
		}
		
//		txtFirstname.addFocusListener(new FocusAdapter() {
//			/* (non-Javadoc)
//			 * @see org.eclipse.swt.events.FocusAdapter#focusLost(org.eclipse.swt.events.FocusEvent)
//			 */
//			@Override
//			public void focusLost(FocusEvent e) {
//			    checkDuplicateContact();
//			}
//		});
		txtFirstname.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if(!((Text)e.getSource()).getText().equals(editorContact.getFirstName())) {
					isDuplicateWarningShown = false;
				}
			}
		});
		
//		txtName.addFocusListener(new FocusAdapter() {
//			/* (non-Javadoc)
//			 * @see org.eclipse.swt.events.FocusAdapter#focusLost(org.eclipse.swt.events.FocusEvent)
//			 */
//			@Override
//			public void focusLost(FocusEvent e) {
//			    checkDuplicateContact();
//			}
//		});
		txtName.addModifyListener(new ModifyListener() {
			
			@Override
			public void modifyText(ModifyEvent e) {
				if(!((Text)e.getSource()).getText().equals(editorContact.getName())) {
					isDuplicateWarningShown = false;
				}
			}
		});
		
		// Birthday
		Label labelBirthday = new Label(tabAddress, SWT.NONE);
		//T: Label in the contact editor
		labelBirthday.setText(msg.editorContactFieldBirthdayName);
		//T: Tool Tip Text
		labelBirthday.setToolTipText(msg.editorContactFieldBirthdayTooltip);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelBirthday);		

		// Set the dtBirthday widget to the contact's birthday date
        dtBirthday = new CDateTime(tabAddress, CDT.BORDER | CDT.DROP_DOWN);
        dtBirthday.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        dtBirthday.setFormat(CDT.DATE_MEDIUM);
		dtBirthday.setToolTipText(labelBirthday.getToolTipText());
		GridDataFactory.fillDefaults().span(2, SWT.DEFAULT).hint(250, SWT.DEFAULT).grab(true, false).applyTo(dtBirthday);
		
		Label placeholder = new Label(tabAddress, SWT.NONE);
		placeholder.setText(" ");
		GridDataFactory.fillDefaults().span(4, SWT.DEFAULT).grab(true, false).applyTo(placeholder);

		addressTabFolder = new CTabFolder(tabAddress, SWT.NONE);
		addressTabFolder.setHighlightEnabled(true);
		addressTabFolder.setBackground(new Color[] {
				Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW),
				Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW),
		        Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW)
		        }, new int[] { 50, 100 });
		Button plusButton = new Button(addressTabFolder, SWT.NONE);
		plusButton.setText("+");
		plusButton.setToolTipText("add a new address for this contact");
		plusButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Composite addressGroup = new Composite(addressTabFolder, SWT.NONE);
				GridLayoutFactory.swtDefaults().numColumns(3).applyTo(addressGroup);
				GridDataFactory.fillDefaults().grab(true, true).applyTo(addressGroup);

				CTabItem newAddressTab = createAddressTabForBillingType("address #" + (addressTabFolder.getItemCount() + 1),
						invisible, addressGroup, countryNames, salutationList);
				// TODO binding!!!
				
				addressTabFolder.setSelection(newAddressTab);
			}
		});
		addressTabFolder.setTopRight(plusButton);

		Composite addressGroup = new Composite(addressTabFolder, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(addressGroup);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(addressGroup);
		
		// create an inner tab for each BillingType
		CTabItem mainAddressTab = createAddressTabForBillingType("main address", invisible, addressGroup, countryNames, salutationList);
		
		GridDataFactory.fillDefaults().span(3, SWT.DEFAULT).grab(true, true).applyTo(addressTabFolder);
		addressTabFolder.setSelection(mainAddressTab);
		addressTabFolder.setFocus();
	}

	private CTabItem createAddressTabForBillingType(String name, Composite invisible, Composite addressGroup, Map<String, String> countryNames,
			Map<Integer, String> salutationList) {
		CTabItem addressForBillingType = new CTabItem(addressTabFolder, SWT.NONE);
		AddressTabWidget addressTabWidget = new AddressTabWidget();
		addressForBillingType.setText(name);

		// Local Consultant
		Label labelLocalConsultant = new Label(addressGroup, SWT.NONE);
		//T: Label in the contact editor
		labelLocalConsultant.setText("Local consultant");
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelLocalConsultant);
		
		Text txtlocalConsultant = new Text(addressGroup, SWT.BORDER);
		addressTabWidget.setLocalConsultant(txtlocalConsultant);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(txtlocalConsultant);
		
		// Street
		Label labelStreet = new Label(addressGroup, SWT.NONE);
		//T: Label in the contact editor
		labelStreet.setText(msg.commonFieldStreet);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelStreet);
		Text txtStreet = new Text(addressGroup, SWT.BORDER);
		txtStreet.addFocusListener(new FocusAdapter() {
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.FocusAdapter#focusLost(org.eclipse.swt.events.FocusEvent)
			 */
			@Override
			public void focusLost(FocusEvent e) {
				checkDuplicateContact(txtStreet.getText());
			}
		});
		addressTabWidget.setStreet(txtStreet);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(txtStreet);
//		setTabOrder(txtCompany, txtStreet);
		
		// City Addon
		Label labelCityAddon = new Label(addressGroup, SWT.NONE);
		//T: Label in the contact editor
		labelCityAddon.setText("City Addon");
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCityAddon);
		Text txtCityAddon = new Text(addressGroup, SWT.BORDER);
		addressTabWidget.setCityAddon(txtCityAddon);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(txtCityAddon);
//		setTabOrder(txtStreet, txtCityAddon);

		// City
		Label labelCity = new Label(addressGroup, SWT.NONE);
		//T: Label in the contact editor
		labelCity.setText(msg.editorContactFieldZipcityName);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCity);
		
		Text txtZip = new Text(addressGroup, SWT.BORDER);
		addressTabWidget.setZip(txtZip);
		GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).applyTo(txtZip);
		
		Text txtCity = new Text(addressGroup, SWT.BORDER);
		addressTabWidget.setCity(txtCity);
		GridDataFactory.fillDefaults().hint(150, SWT.DEFAULT).grab(true, false).applyTo(txtCity);

		// Country
		Label labelCountry = new Label(useCountry ? addressGroup : invisible, SWT.NONE);
		//T: Label in the contact editor
		labelCountry.setText(msg.commonFieldCountry);
		//T: Tool Tip Text
		labelCountry.setToolTipText(msg.editorContactHintSethomecountry);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCountry);

		ComboViewer comboCountry = new ComboViewer(useCountry ? addressGroup : invisible, SWT.BORDER | SWT.READ_ONLY);
		comboCountry.getCombo().setToolTipText(labelCountry.getToolTipText());
		comboCountry.setContentProvider(new StringHashMapContentProvider());
		comboCountry.setInput(countryNames);
		StringComboBoxLabelProvider stringComboBoxLabelProvider = ContextInjectionFactory.make(StringComboBoxLabelProvider.class, context);
		stringComboBoxLabelProvider.setCountryNames(countryNames);
		comboCountry.setLabelProvider(stringComboBoxLabelProvider);
		addressTabWidget.setCountryCombo(comboCountry);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(comboCountry.getCombo());
		
		Label labelAddressType = new Label(addressGroup, SWT.NONE);
		labelAddressType.setText("address for");
		labelAddressType.setToolTipText("this address can be used for the selected address types");
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelAddressType);
		
		final MultiChoice<ContactType> mcSimple = new MultiChoice<ContactType>(addressGroup, SWT.None);
		mcSimple.setLabelProvider(new ContactTypeLabelProvider(msg));
		mcSimple.addAll(ContactType.values());
		addressTabWidget.setContactTypeWidget(mcSimple);
		GridDataFactory.swtDefaults().hint(400, SWT.DEFAULT).span(2, 1).applyTo(mcSimple);
		
		addressForBillingType.setControl(addressGroup);
		addressTabWidgets.add(addressTabWidget);
		return addressForBillingType;
	}
	
	/**
	 * Hook method for creating additional fields, should be overwritten.
	 * @param tabMisc the parent widget
	 */
    protected void createAdditionalFields(Composite tabMisc) {
		// default is empty
	}

	protected void bindModel() {
		part.getTransientData().put(BIND_MODE_INDICATOR, Boolean.TRUE);

		bindModelValue(editorContact, txtNr, Contact_.customerNumber.getName(), 32);
		bindModelValue(editorContact, comboSalutationViewer, Contact_.gender.getName());
		bindModelValue(editorContact, txtTitle, Contact_.title.getName(), 32);
		bindModelValue(editorContact, txtFirstname, Contact_.firstName.getName(), 64);
		bindModelValue(editorContact, txtName, Contact_.name.getName(), 64);
		bindModelValue(editorContact, txtCompany, Contact_.company.getName(), 64);
		bindModelValue(editorContact, dtBirthday, Contact_.birthday.getName());
		
		CTabItem[] items = addressTabFolder.getItems();
		for (int i = 0; i < items.length; i++) {
			CTabItem addressTabItem = items[i];
		    Address currentAddress = getOrCreateAddressByIndexFromContact(i);
		    
		    bindModelValue(currentAddress, addressTabWidgets.get(i).getLocalConsultant(), Address_.localConsultant.getName(), 64);
		    bindModelValue(currentAddress, addressTabWidgets.get(i).getStreet(), Address_.street.getName(), 64);
		    bindModelValue(currentAddress, addressTabWidgets.get(i).getZip(), Address_.zip.getName(), 16);
		    bindModelValue(currentAddress, addressTabWidgets.get(i).getCity(), Address_.city.getName(), 32);
		    bindModelValue(currentAddress, addressTabWidgets.get(i).getCityAddon(), Address_.cityAddon.getName(), 32);
		    bindModelValue(currentAddress, addressTabWidgets.get(i).getCountryCombo(), Address_.countryCode.getName());

		    bindListWidget(currentAddress, addressTabWidgets.get(i).getContactTypeWidget(), Address_.contactTypes, addressTabItem);
		}
		
		bindModelValue(editorContact, txtAccountHolder, Contact_.bankAccount.getName() +"." +BankAccount_.accountHolder.getName(), 64);
		bindModelValue(editorContact, txtAccount, Contact_.bankAccount.getName() +"." +BankAccount_.name.getName(), 32);
		bindModelValue(editorContact, txtBankCode, Contact_.bankAccount.getName() +"." +BankAccount_.bankCode.getName(), 32);
		bindModelValue(editorContact, txtBankName, Contact_.bankAccount.getName() +"." +BankAccount_.bankName.getName(), 64);
		bindModelValue(editorContact, txtIBAN, Contact_.bankAccount.getName() +"." +BankAccount_.iban.getName(), 32);
		bindModelValue(editorContact, txtBIC, Contact_.bankAccount.getName() +"." +BankAccount_.bic.getName(), 32);
        bindModelValue(editorContact,txtMandatRef, Contact_.mandateReference.getName(), 32);

		fillAndBindCategoryCombo();
		fillAndBindPaymentCombo();
		
		bindModelValue(editorContact, txtSupplierNr, Contact_.supplierNumber.getName(), 64);
		
		UpdateValueStrategy<IStatus, String> strategy = new UpdateValueStrategy<IStatus, String>();
		strategy.setBeforeSetValidator(new IValidator<String>() {

		    @Override
		    public IStatus validate(String emailAddress) {
		        if(StringUtils.isBlank(emailAddress) || EmailValidator.getInstance().isValid(emailAddress)) {
		        	return ValidationStatus.ok();
		        } else {
		        	return ValidationStatus.error(msg.editorContactFieldEmailValidationerror);
		        }
		    }
		});
		
		Binding binding = bindModelValue(editorContact, txtEmail, Contact_.email.getName(), 64, strategy, null);
		ControlDecorationSupport.create(binding, SWT.TOP | SWT.LEFT);
		
		bindModelValue(editorContact, txtPhone, Contact_.phone.getName(), 32);
		bindModelValue(editorContact, txtFax, Contact_.fax.getName(), 32);
		bindModelValue(editorContact, txtMobile, Contact_.mobile.getName(), 32);
		bindModelValue(editorContact, txtWebsite, Contact_.website.getName(), 64);
		bindModelValue(editorContact, txtWebshopName, Contact_.webshopName.getName(), 64);

		bindModelValue(editorContact, comboReliability, Contact_.reliability.getName());
		bindModelValue(editorContact, txtVatNr, Contact_.vatNumber.getName(), 32);
		bindModelValue(editorContact, txtGln, Contact_.gln.getName(), 32);
		bindModelValue(editorContact, txtDiscount, Contact_.discount.getName(), 16);
        bindModelValue(editorContact, comboUseNetGross, Contact_.useNetGross.getName());
		bindModelValue(editorContact, textNote, Contact_.note.getName(), 10000);
		
		bindAdditionalValues(editorContact);
		
		part.getTransientData().remove(BIND_MODE_INDICATOR);
    }

	@SuppressWarnings("unchecked")
	private <E extends IEntity> void bindListWidget(E listEntity, Control currentWidget,
			ListAttribute<E, ContactType> property, CTabItem cTabItem) {

		if (currentWidget instanceof MultiChoice) {
			bindModelList(listEntity, ContactType.class, (MultiChoice<ContactType>) currentWidget, property.getName(),
				null, null);
		}
	}
//
//	/**
//	 * Binds a widget from a {@link CTabFolder}'s {@link CTabItem} to a certain field of an entity. The entity
//	 * hereby is a part (entry) of a list which belongs to a parent entity (e.g., the {@link Address}
//	 * entry from a {@link Contact}'s  list). The widget has to contain an entry in its data property which 
//	 * names the containing property.
//	 * 
//	 * @see ContactEditor#findAddressWidgetFor(SingularAttribute, CTabItem)
//	 * 
//	 * @param listEntity the current entity
//	 * @param property the property to bind
//	 * @param cTabItem the current {@link CTabItem}
//	 * @param length the length of this field (can be 0 for default length)
//	 */
//	private <E extends IEntity> void bindWidget(E listEntity, SingularAttribute<E, String> property, CTabItem cTabItem,
//			int length) {
//
//		// each widget has a "marker" which identifies the widget for a certain property
//		// of an address
//		java.util.Optional<Control> currentWidget = findAddressWidgetFor(property, cTabItem);
//		currentWidget.ifPresent(w -> {
//			if (w instanceof Text) {
//				if (length > 0) {
//					bindModelValue(listEntity, (Text) w, property.getName(), length);
//				} else {
//					bindModelValue(listEntity, (Text) w, property.getName());
//				}
//			}
//		});
//	}

//	private <E extends IEntity> java.util.Optional<Control> findAddressWidgetFor(Attribute<E, ?> attribute, CTabItem addressTabItem) {
////		for (Control w : ((Composite)addressTabItem.getControl()).getChildren()) {
////			if(w.getData(WIDGET_IDENTIFIER) != null && w.getData(WIDGET_IDENTIFIER).equals(attribute)) {
////				System.out.println("found widget: " + w);
////			}
////		}
////		
//		return Arrays.stream(((Composite)addressTabItem.getControl()).getChildren()).filter(w -> w.getData(WIDGET_IDENTIFIER) != null && w.getData(WIDGET_IDENTIFIER).equals(attribute)).findFirst();
//	}

	protected Address getOrCreateAddressByIndexFromContact(int i) {
		// get last address and fill up the address list
		int lastAddressIndex = editorContact.getAddresses().size() - 1;
		if(lastAddressIndex < i) {
			do {
				Address address = modelFactory.createAddress();
				// add no ContactType means this address is a default address for this contact
				editorContact.addToAddresses(address);
			} while(++lastAddressIndex < i);
		}
		return editorContact.getAddresses().get(i);
	}

	private void fillAndBindPaymentCombo() {
		Payment tmpPayment = editorContact.getPayment();
		ComboViewer comboViewerPayment;
        comboViewerPayment = new ComboViewer(comboPayment);
        comboViewerPayment.setContentProvider(new EntityComboProvider());
        comboViewerPayment.setLabelProvider(new EntityLabelProvider());
//        comboViewerPayment.getCombo().setToolTipText(labelPayment.getToolTipText());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboPayment);
//        GridDataFactory.swtDefaults().hint(200, SWT.DEFAULT).align(SWT.END, SWT.CENTER).applyTo(comboViewerPayment.getCombo());
        
        // If a new payment is selected ...
        comboViewerPayment.addSelectionChangedListener(new ISelectionChangedListener() {

        	// change the paymentId to the selected element
        	public void selectionChanged(SelectionChangedEvent event) {
        		getMDirtyablePart().setDirty(true);
        	}
        });

        // Fill the payment combo with the payments
        List<Payment> allPayments = paymentsDao.findAll();
        comboViewerPayment.setInput(allPayments);
        editorContact.setPayment(tmpPayment);

        UpdateValueStrategy<Payment, String> paymentModel2Target = UpdateValueStrategy.create(new EntityConverter<Payment>(Payment.class));
        UpdateValueStrategy<String, Payment> target2PaymentModel = UpdateValueStrategy.create(new StringToEntityConverter<Payment>(allPayments, Payment.class));

        // Set the combo
        bindModelValue(editorContact, comboPayment, Contact_.payment.getName(),
                target2PaymentModel, paymentModel2Target);
	}
	

	/**
	 * Binds additional values. Should be overwritten by subclasses.
	 * 
	 * @param editorContact2 contact editor object
	 */
    protected void bindAdditionalValues(C editorContact2) {
		// default is empty
		
	}

	/**
     * Gets the delivery contact. This is the additional contact if the billing address differs from delivery address.
     *
     * @return the delivery contact
     */
//	abstract protected C getDeliveryContact();

	/**
     * creates the combo box for the VAT category
     */
    private void fillAndBindCategoryCombo() {
        // Collect all category strings as a sorted Set
        final TreeSet<ContactCategory> categories = new TreeSet<ContactCategory>(new CategoryComparator<>());
        categories.addAll(contactCategoriesDAO.findAll(true));

        ComboViewer viewer = new ComboViewer(comboCategory);
        viewer.setContentProvider(new ArrayContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                return categories.toArray();
            }
        });
        
        // FIXME see comment in VatEditor
        ContactCategory tmpCategory = editorContact.getCategories();
        // Add all categories to the combo
        viewer.setInput(categories);
        viewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return element instanceof ContactCategory ? CommonConverter.getCategoryName((ContactCategory)element, "") : null;
            }
        });
        editorContact.setCategories(tmpCategory);
        
        UpdateValueStrategy<ContactCategory, String> catModel2Target = UpdateValueStrategy.create(new CategoryConverter<ContactCategory>(ContactCategory.class));
        UpdateValueStrategy<String, ContactCategory> target2CatModel = UpdateValueStrategy.create(new StringToCategoryConverter<ContactCategory>(categories, ContactCategory.class));
        bindModelValue(editorContact, comboCategory, Contact_.categories.getName(), target2CatModel, catModel2Target);
    }
	
	/**
	 * Checks if the currently entered name and street is already stored.
	 * @param street 
	 * 
	 * @return <code>true</code> if a contact with the same name and street was found
	 */
	private void checkDuplicateContact(String street) {
		// check only if name, firstname and street aren't empty
		if(isDuplicateWarningShown  
			|| "".equals(txtName.getText())
			|| "".equals(txtFirstname.getText())
			|| "".equals(street)) return;
		
		// Search the list for an existing data set with the specified value
		Contact testContact = contactDAO.checkContactWithSameValues(txtName.getText()
				, txtFirstname.getText()
				, street);
//		if(testContact != null) {
//				isDuplicateWarningShown = true;
//				MessageDialog.openWarning(top.getShell(), msg.editorContactWarningDuplicate,
//						MessageFormat.format(msg.editorContactWarningDuplicateStreet, 
//								testContact.getFirstName(), testContact.getName(), testContact.getAddress().getStreet()));
//		}

		// nothing found
	}

	/**
	 * Test, if there is a contact with the same number
	 * 
	 * @return TRUE, if one with the same number is found
	 */
	private boolean thereIsOneWithSameNumber() {
		isDuplicateWarningShown = true;  // so the other check isn't executed
		// Cancel, if there is already a document with the same ID
		Contact testContact = contactDAO.getContactWithSameNumber(txtNr.getText());
		if (testContact != null && testContact.getId() != editorContact.getId()) {
			int contactFormat = defaultValuePrefs.getInt(Constants.PREFERENCES_CONTACT_NAME_FORMAT);
			// Display an error message
			MessageDialog.openError(top.getShell(), msg.dialogMessageboxTitleError, 
					MessageFormat.format(msg.editorContactErrorCustomernumber, txtNr.getText(), 
							(contactFormat == Constants.CONTACT_FORMAT_FIRSTNAME_LASTNAME ? testContact.getFirstName() : testContact.getName() + ","),
							(contactFormat == Constants.CONTACT_FORMAT_FIRSTNAME_LASTNAME ? testContact.getName() : testContact.getFirstName())));
			return true;
		}

		return false;
	}

	@Override
	protected MDirtyable getMDirtyablePart() {
		return part;
	}
	
    public void setDirty(boolean isDirty) {
    	getMDirtyablePart().setDirty(isDirty);
    }

	@Override
	abstract protected String getEditorID();

	/**
		 * 
		 *
		 */
	private static final class UrlCallHandler extends MouseAdapter {
		
		private Text txtField;
		private ILogger log;


		/**
		 * @param txtField
		 * @param log
		 */
		public UrlCallHandler(Text txtField, ILogger log) {
			this.txtField = txtField;
			this.log = log;
		}


		@Override
		public void mouseDown(MouseEvent e) {
			if (e.stateMask == SWT.CTRL) {
				String websiteText = txtField.getText();
				
				// distinguish between URLs and E-Mail addresses
				if(websiteText.indexOf('@') > 0) {
					if(EmailValidator.getInstance().isValid(websiteText)) {
						try {
							websiteText = StringUtils.prependIfMissing(websiteText, "mailto:");
							Desktop.getDesktop().mail(new URI(websiteText));
						} catch (IOException | URISyntaxException e1) {
							log.error(e1, "can't open the e-mail application. Reason: ");
						}
					}
				} else {
					// validate URL and open it in browser
					String[] schemes = { "http", "https" };
					UrlValidator urlValidator = new UrlValidator(schemes);
					if (urlValidator.isValid(websiteText)) {
						try {
							Desktop.getDesktop().browse(new URI(websiteText));
						} catch (IOException | URISyntaxException e1) {
							log.error(e1, "can't open the contact's web site. Reason: ");
						}
					} else {
						// URL is invalid or empty
					}
				}
			}
			super.mouseDown(e);
		}

	}

}

