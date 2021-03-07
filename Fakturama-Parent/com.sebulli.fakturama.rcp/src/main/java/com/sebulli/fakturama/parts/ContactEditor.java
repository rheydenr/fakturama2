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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.metamodel.ListAttribute;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.e4.ui.workbench.UIEvents;
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
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.nebula.widgets.formattedtext.PercentFormatter;
import org.eclipse.nebula.widgets.opal.multichoice.MultiChoice;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolder2Listener;
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
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
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
import com.sebulli.fakturama.misc.EmbeddedProperties;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.model.Address_;
import com.sebulli.fakturama.model.BankAccount_;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.CategoryComparator;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.model.ContactType;
import com.sebulli.fakturama.model.Contact_;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.IEntity;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.ReliabilityType;
import com.sebulli.fakturama.parts.converter.CategoryConverter;
import com.sebulli.fakturama.parts.converter.EntityConverter;
import com.sebulli.fakturama.parts.converter.StringToCategoryConverter;
import com.sebulli.fakturama.parts.converter.StringToEntityConverter;
import com.sebulli.fakturama.parts.widget.ContactEditorTabExtrasComposite;
import com.sebulli.fakturama.parts.widget.contacttree.ContactTreeListTable;
import com.sebulli.fakturama.parts.widget.contentprovider.EntityComboProvider;
import com.sebulli.fakturama.parts.widget.contentprovider.HashMapContentProvider;
import com.sebulli.fakturama.parts.widget.contentprovider.StringHashMapContentProvider;
import com.sebulli.fakturama.parts.widget.labelprovider.ContactTypeLabelProvider;
import com.sebulli.fakturama.parts.widget.labelprovider.EntityLabelProvider;
import com.sebulli.fakturama.parts.widget.labelprovider.NumberLabelProvider;
import com.sebulli.fakturama.parts.widget.labelprovider.StringComboBoxLabelProvider;
import com.sebulli.fakturama.util.ContactUtil;

/**
 * The contact editor.
 * 
 */

public abstract class ContactEditor<C extends Contact> extends Editor<C> {

	private static final String ADDRESS_TAB_WIDGET = "ADDRESS_TAB_WIDGET";

    private static final String ADDRESS_INDEX = "ADDRESS_INDEX";

    /** Editor's ID */
	public static final String ID = "com.sebulli.fakturama.editors.contactEditor";
	
	public static final String EDITOR_ID = "ContactEditor";

	/** This UniDataSet represents the editor's input */ 
	protected C editorContact;
    
    @Inject
    private EPartService partService;
    
	@Inject
	private ILocaleService localeUtil;

	// SWT widgets of the editor
    private Composite top;

    private CTabFolder tabFolder;
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
	private Text txtSupplierNr;
	private Text txtWebsite;
	private Text txtWebshopName, txtAlias, txtRegisterNumber;
	private Text txtVatNr;
	private Text txtGln;
	private FormattedText txtDiscount;
	private CCombo comboCategory;
	private ComboViewer comboUseNetGross;
	
// GS/ tab Extras
	private ContactEditorTabExtrasComposite tabExtras;

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

	private UpdateValueStrategy<IStatus, String> emailValidationStrategy = new UpdateValueStrategy<IStatus, String>();

	private Map<Integer, String> salutationMap;

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

	    // check the number for a new contact (but only if check is not suppressed)
		if (newContact && !defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_SUPPRESS_CONTACTNUMER_CHECK)) {
			// Check, if the contact number is the next one
			int result = getNumberGenerator().setNextFreeNumberInPrefStore(txtNr.getText(), getEditorID());

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
		
// GS/
		// merge data from tabExtras (back) to txtNote
		tabExtras.appendData(textNote);

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
        String callerDocument = (String) part.getTransientData().get(CallEditor.PARAM_CALLING_DOC);
        if(callerDocument != null) {
            eventParams.put(DocumentEditor.DOCUMENT_ID, callerDocument);
            eventParams.put(ContactTreeListTable.SELECTED_CONTACT_ID, Long.valueOf(editorContact.getId()));
            eventParams.put(ContactTreeListTable.SELECTED_ADDRESS_ID, Long.valueOf(editorContact.getAddresses().get(0).getId()));
            selectionService.setSelection(editorContact);
            evtBroker.post("DialogSelection/Contact", eventParams);
        }
        
        bindModel();
        
        // reset dirty flag
        tabExtras.setDirty(false); // GS/
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
	    
		emailValidationStrategy.setBeforeSetValidator((String emailAddress) -> {
			if (StringUtils.isBlank(emailAddress) || EmailValidator.getInstance().isValid(emailAddress)) {
				return ValidationStatus.ok();
			} else {
				return ValidationStatus.error(msg.editorContactFieldEmailValidationerror);
			}
		});
	    
	    
	    
        Long objId = null;
        this.part = (MPart) parent.getData("modelElement");
        this.part.setIconURI(getEditorIconURI());
        String tmpObjId = (String) part.getTransientData().get(CallEditor.PARAM_OBJ_ID);
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
			// Create a new data set and set some defaults
            editorContact = createNewContact(modelFactory);
            
            String category = (String) part.getTransientData().get(CallEditor.PARAM_CATEGORY);
            if(StringUtils.isNotEmpty(category)) {
                ContactCategory newCat = contactCategoriesDAO.findCategoryByName(category);
                editorContact.setCategories(newCat);
            }

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

			// Get the next contact number
			editorContact.setCustomerNumber(getNumberGenerator().getNextNr(getEditorID()));
			
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
    public void handleForceClose(@UIEventTopic(ContactEditor.EDITOR_ID + UIEvents.TOPIC_SEP + "forceClose") Event event) {
        // the event has already all given params in it since we created them as Map
        String targetDocumentName = (String) event.getProperty(Editor.OBJECT_ID);
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
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(parent);
        GridLayoutFactory.fillDefaults().applyTo(parent);

		// Create the ScrolledComposite to scroll horizontally and vertically
	    ScrolledComposite scrollcomposite = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);

		// Create the top Composite
        top = new Composite(scrollcomposite, SWT.SCROLLBAR_OVERLAY | SWT.NONE );  //was parent before 

		GridLayoutFactory.fillDefaults().numColumns(4).margins(10, 20).equalWidth(false).applyTo(top);

		// Create an invisible container for all hidden components	
		Composite invisible = new Composite(parent, SWT.NONE);
		invisible.setVisible(false);
		GridDataFactory.fillDefaults().hint(0, 0).applyTo(invisible);

		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ContextHelpConstants.CONTACT_EDITOR);

		createCustomerNumberWidget(top);

        // Category 
		Composite categoryPanel = new Composite(top, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(categoryPanel);
        GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(categoryPanel);
		
        Label labelCategory = new Label(categoryPanel, SWT.NONE);
        //T: Label in the contact editor
        labelCategory.setText(msg.commonFieldCategory);
        //T: Tool Tip Text
        labelCategory.setToolTipText(msg.editorContactFieldCategoryTooltip);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCategory);

        comboCategory = new CCombo(categoryPanel, SWT.BORDER);
        comboCategory.setToolTipText(labelCategory.getToolTipText());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(comboCategory);

        // Company
        Label labelCompany = new Label(useCompany ? top : invisible, SWT.NONE);
        //T: Label in the contact editor
        labelCompany.setText(msg.commonFieldCompany);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCompany);
        txtCompany = new Text(useCompany ? top : invisible, SWT.BORDER | SWT.MULTI);
//      txtCompany.setText(DataUtils.makeOSLineFeeds(editorContact.getCompany()));
        GridDataFactory.swtDefaults().hint(400, 40).span(3, 1).grab(false, false).applyTo(txtCompany);

        // The title and gender's label
        Label labelTitle = new Label((useSalutation || useTitle) ? top : invisible, SWT.NONE);
        int span = 4;
        if (useSalutation) {
            labelTitle.setText(msg.commonFieldGender);
            span = 3;
        } 
        if (useSalutation && useTitle) {
            labelTitle.setText(labelTitle.getText() + ", ");
            span = 3;
        } 
        if (useTitle) {
            //T: "Title" (part of an address)
            span = 3;
            labelTitle.setText(labelTitle.getText() + msg.commonFieldTitle);
        }
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelTitle);
        
        Composite salutationPanel = new Composite(top, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).span(span, 1).applyTo(salutationPanel);
        GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(salutationPanel);

        // Salutation
        comboSalutationViewer = new ComboViewer(useSalutation ? salutationPanel : invisible, SWT.BORDER | SWT.READ_ONLY);
        comboSalutationViewer.setContentProvider(new HashMapContentProvider<Integer, String>());
/*
        allSalutations = itemListTypeDao.findAllSalutations();
        comboSalutationViewer.setContentProvider(new EntityComboProvider());
        comboSalutationViewer.setLabelProvider(new EntityLabelProvider());
        comboSalutationViewer.setInput(allSalutations);
 */
        comboSalutationViewer.setInput(getSalutationMap());
        comboSalutationViewer.setLabelProvider(new NumberLabelProvider<Integer, String>(getSalutationMap()));
        GridDataFactory.fillDefaults().grab(false, false).hint(100, SWT.DEFAULT).span(useTitle ? 1 : 2, 1).applyTo(comboSalutationViewer.getControl());
        setTabOrder(txtCompany, comboSalutationViewer.getControl());

        // Title
        txtTitle = new Text(useTitle ? salutationPanel : invisible, SWT.BORDER);
        GridDataFactory.swtDefaults().hint(200, SWT.DEFAULT).span(useSalutation ? 1 : 2, 1).applyTo(txtTitle);

        // First and last name      
        Label labelName = new Label(top, SWT.NONE);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);
        
        Composite namePanel = new Composite(top, SWT.NONE);
        GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(namePanel);
        GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(namePanel);
        if (useLastNameFirst) {
            //T: Format of the name in an address
            labelName.setText(msg.editorContactFieldLastnamefirstnameName);
            txtName = new Text(namePanel, SWT.BORDER);
            GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).applyTo(txtName);
            txtFirstname = new Text(namePanel, SWT.BORDER);
            GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).applyTo(txtFirstname);
        }
        else {
            //T: Format of the name in an address
            labelName.setText(msg.editorContactFieldFirstnamelastnameName);
            txtFirstname = new Text(namePanel, SWT.BORDER);
            GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).applyTo(txtFirstname);
            txtName = new Text(namePanel, SWT.BORDER);
            GridDataFactory.fillDefaults().hint(200, SWT.DEFAULT).applyTo(txtName);
        }
        
//      txtFirstname.addFocusListener(new FocusAdapter() {
//          /* (non-Javadoc)
//           * @see org.eclipse.swt.events.FocusAdapter#focusLost(org.eclipse.swt.events.FocusEvent)
//           */
//          @Override
//          public void focusLost(FocusEvent e) {
//              checkDuplicateContact();
//          }
//      });
        txtFirstname.addModifyListener(new ModifyListener() {
            
            @Override
            public void modifyText(ModifyEvent e) {
                if(!((Text)e.getSource()).getText().equals(editorContact.getFirstName())) {
                    isDuplicateWarningShown = false;
                }
            }
        });
        
//      txtName.addFocusListener(new FocusAdapter() {
//          /* (non-Javadoc)
//           * @see org.eclipse.swt.events.FocusAdapter#focusLost(org.eclipse.swt.events.FocusEvent)
//           */
//          @Override
//          public void focusLost(FocusEvent e) {
//              checkDuplicateContact();
//          }
//      });
        txtName.addModifyListener(new ModifyListener() {
            
            @Override
            public void modifyText(ModifyEvent e) {
                if(!((Text)e.getSource()).getText().equals(editorContact.getName())) {
                    isDuplicateWarningShown = false;
                }
            }
        });
        
		// Create the address tab
		Composite tabAddress;
		if (useBank || useMisc || useNote) {
			tabFolder = new CTabFolder(top, SWT.NONE);
			tabFolder.setSimple(false);
			GridDataFactory.fillDefaults().grab(true, true).span(4, 1).applyTo(tabFolder);

			CTabItem item1 = new CTabItem(tabFolder, SWT.NONE);
			//T: Label in the contact editor
			item1.setText(msg.editorContactLabelAddress);
			tabAddress = new Composite(tabFolder, SWT.NONE);
			item1.setControl(tabAddress);
		}
		else {
			tabAddress = new Composite(top, SWT.NONE);
		}
        GridDataFactory.fillDefaults().grab(true, true).span(4, 1).applyTo(tabAddress);
		GridLayoutFactory.swtDefaults().applyTo(tabAddress);

		// Create the bank tab
		Composite tabBank;
		if (useBank) {
			CTabItem item3 = new CTabItem(tabFolder, SWT.NONE);
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
			CTabItem item4 = new CTabItem(tabFolder, SWT.NONE);
			//T: Label in the contact editor
			item4.setText(msg.commandNavigationMisc);
			tabMisc = new Composite(tabFolder, SWT.NONE);
			item4.setControl(tabMisc);
		}
		else {
			tabMisc = new Composite(invisible, SWT.NONE);
		}
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(tabMisc);

		// Create to note tab
		CTabItem item5 = null;
		Composite tabNote;
		if (useNote) {
			item5 = new CTabItem(tabFolder, SWT.NONE);
			//T: Label in the contact editor
			item5.setText(msg.editorContactLabelNotice);
			tabNote = new Composite(tabFolder, SWT.NONE);
			item5.setControl(tabNote);
		}
		else {
			tabNote = new Composite(invisible, SWT.NONE);
		}
		tabNote.setLayout(new FillLayout());
		
// GS/ 
		// Create extras tab
		CTabItem item6 = null;
		item6 = new CTabItem(tabFolder, SWT.NONE);
		//T: Label in the contact editor
		item6.setText(msg.editorContactLabelExtras);
		tabExtras = new ContactEditorTabExtrasComposite(tabFolder, SWT.NONE);
		tabExtras.addModifyListener(new ModifyListener() {			
			@Override
			public void modifyText(ModifyEvent e) {
				setDirty(true);
			}
		});		
		item6.setControl(tabExtras);

		if(tabFolder != null) {
			tabFolder.setSelection(0);
		}
		
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

		// Web Site
		Label labelWebsite = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelWebsite.setText(msg.exporterDataWebsite);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelWebsite);
		txtWebsite = new Text(tabMisc, SWT.BORDER);
		
		final Cursor cursorHand = top.getDisplay().getSystemCursor(SWT.CURSOR_HAND);
		final Cursor cursorIBeam = top.getDisplay().getSystemCursor(SWT.CURSOR_IBEAM);
		
		txtWebsite.addMouseMoveListener((e) -> {
			if (e.stateMask == SWT.CTRL) {
				txtWebsite.setCursor(cursorHand);
			} else {
				txtWebsite.setCursor(cursorIBeam);
			}
		});
		
		// FAK-382 clickable URL
		txtWebsite.addMouseListener(new UrlCallHandler(txtWebsite, log));
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(txtWebsite);

		// Suppliernumber
		Label labelSupplier = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelSupplier.setText(msg.editorContactFieldSuppliernumberName);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelSupplier);
		txtSupplierNr = new Text(tabMisc, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtSupplierNr);
		
		// GLN
		Label labelGln = new Label(tabMisc, SWT.NONE);
		labelGln.setText(msg.contactFieldGln);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelGln);
		txtGln = new Text(tabMisc, SWT.BORDER);
		txtGln.setToolTipText(msg.contactFieldGlnTooltip);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtGln);
		
		// Register Number
		Label labelRegisterNumber = new Label(tabMisc, SWT.NONE);
		labelRegisterNumber.setText(msg.contactFieldRegisterNumber);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelRegisterNumber);
		txtRegisterNumber = new Text(tabMisc, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtRegisterNumber);
		
		// WebShop name  
		Label labelWebshopName = new Label(tabMisc, SWT.NONE);
		labelWebshopName.setText(msg.exporterDataWebshopname);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelWebshopName);
		txtWebshopName = new Text(tabMisc, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtWebshopName);
		
		// Alias for this contact 
		Label labelAlias = new Label(tabMisc, SWT.NONE);
		labelAlias.setText(msg.editorContactFieldAlias);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelAlias);
		txtAlias = new Text(tabMisc, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtAlias);
	      
        // Birthday
        Label labelBirthday = new Label(tabMisc, SWT.NONE);
        //T: Label in the contact editor
        labelBirthday.setText(msg.editorContactFieldBirthdayName);
        //T: Tool Tip Text
        labelBirthday.setToolTipText(msg.editorContactFieldBirthdayTooltip);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelBirthday);        

        // Set the dtBirthday widget to the contact's birthday date
        dtBirthday = new CDateTime(tabMisc, CDT.BORDER | CDT.DROP_DOWN);
        dtBirthday.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
        dtBirthday.setFormat(CDT.DATE_MEDIUM);
        dtBirthday.setToolTipText(labelBirthday.getToolTipText());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(dtBirthday);

		// VAT number
		Label labelVatNr = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelVatNr.setText(msg.exporterDataVatno);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelVatNr);
		txtVatNr = new Text(tabMisc, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtVatNr);

		// Payment
		Label labelPayment = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelPayment.setText(msg.editorContactFieldPaymentName);
		//T: Tool Tip Text
		labelPayment.setToolTipText(msg.editorContactFieldPaymentTooltip);

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelPayment);
        // Combo to select the payment
        comboPayment = new Combo(tabMisc, SWT.BORDER | SWT.READ_ONLY);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(comboPayment);

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
		        return contactUtil.getReliabilityString((ReliabilityType)element);
		    }
		});

		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboReliability.getControl());

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
//		String note = DataUtils.getInstance().makeOSLineFeeds(editorContact.getNote());
		textNote = new Text(tabNote, SWT.BORDER | SWT.MULTI |SWT.WRAP | SWT.V_SCROLL | SWT.H_SCROLL);
        textNote.setData("Notiz");
        
        scrollcomposite.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));
        scrollcomposite.setContent(top);
        scrollcomposite.setMinSize(top.computeSize(SWT.DEFAULT, SWT.DEFAULT));   // 2nd entry should be adjusted to higher value when new fields will be added to composite 
        scrollcomposite.setExpandHorizontal(true);
        scrollcomposite.setExpandVertical(true);
        bindModel();
        
		// If the note is not empty, display it,
		// when opening the editor.
        if (useNote && StringUtils.isNotEmpty(editorContact.getNote())) {
			tabFolder.setSelection(item5);
			textNote.setFocus();
		}
	}

    private void createCustomerNumberWidget(Composite headInfo) {
		// Composite for the customer's number

		// Customer's number
		Label labelNr = new Label(headInfo, SWT.NONE);
		//T: Label in the contact editor
		labelNr.setText(msg.editorContactFieldNumberName);
		//T: Tool Tip Text
		labelNr.setToolTipText(msg.editorContactFieldNumberTooltip);

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelNr);
		txtNr = new Text(headInfo, SWT.BORDER);
		txtNr.setToolTipText(labelNr.getToolTipText());
        GridDataFactory.swtDefaults().hint(200, SWT.DEFAULT).applyTo(txtNr);
	}

    @Focus
    public void setFocus() {
        if (useNote && StringUtils.isNotEmpty(editorContact.getNote())) {
            textNote.setFocus();
        } else {
            txtNr.setFocus();
        }
    }
    
	private void createAddressGroup(Composite invisible, Composite twoPanelLayout) {
		Composite tabAddress = new Composite(twoPanelLayout, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(tabAddress);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(tabAddress);
		
		// Controls in the group "address"

		addressTabFolder = new CTabFolder(twoPanelLayout, SWT.NONE);
		addressTabFolder.setHighlightEnabled(true);
		addressTabFolder.setSimple(false);
		Button plusButton = new Button(addressTabFolder, SWT.NONE);
		plusButton.setText("+");
		plusButton.setToolTipText(msg.editorContactHintAddaddresstooltip);
		plusButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			    // address will be created in bindAddressWidgetForIndex, therefore we use the term editorContact.getAddresses().size() (without "-1")
				CTabItem newAddressTab = createAddressTabForBillingType(msg.editorContactLabelAdditionaladdress, editorContact.getAddresses().size(),
				        addressTabFolder.getItemCount(), invisible, createAddressPanel(), SWT.CLOSE);
				bindAddressWidgetForIndex(newAddressTab, addressTabFolder.getItemCount() - 1);
				
// GS/ copy/duplicate address data by [Ctrl]+Click
//   wouldn't it be nice to have it mentioned in the ToolTip? e.g. "[Strg]+Click = Copy"
				if ((e.stateMask & SWT.CTRL) != 0) {
					CTabItem currentAddressTab = addressTabFolder.getSelection();
					AddressTabWidget currentAddress = (AddressTabWidget)currentAddressTab.getData(ADDRESS_TAB_WIDGET);
					AddressTabWidget newAddress = (AddressTabWidget)newAddressTab.getData(ADDRESS_TAB_WIDGET);
					newAddress.getAdditionalPhone().setText(currentAddress.getAdditionalPhone().getText());
					newAddress.getAddressAddon().setText(currentAddress.getAddressAddon().getText());
					newAddress.getCity().setText(currentAddress.getCity().getText());
					newAddress.getCityAddon().setText(currentAddress.getCityAddon().getText());
					newAddress.getCountryCombo().setSelection(currentAddress.getCountryCombo().getSelection());
					newAddress.getEmail().setText(currentAddress.getEmail().getText());
					newAddress.getFax().setText(currentAddress.getFax().getText());
					newAddress.getLocalConsultant().setText(currentAddress.getLocalConsultant().getText());
					newAddress.getMobile().setText(currentAddress.getMobile().getText());
					newAddress.getNameAddon().setText(currentAddress.getNameAddon().getText());
					newAddress.getPhone().setText(currentAddress.getPhone().getText());
					newAddress.getStreet().setText(currentAddress.getStreet().getText());
					newAddress.getZip().setText(currentAddress.getZip().getText());
				}

				addressTabFolder.setSelection(newAddressTab);
			}
		});
		addressTabFolder.setTopRight(plusButton);

		// create an inner tab for each BillingType
		// hint: The main tab is not closable!
		CTabItem addressTab = createAddressTabForBillingType(msg.editorContactLabelMaintab, null, 0, invisible, createAddressPanel(), SWT.NONE);
		addressTabFolder.setSelection(addressTab);
		int addressCount = editorContact.getAddresses().size();
        if(addressCount > 1) {
            int visibleIndex = 1;
			for (int realIndex = 1; realIndex < addressCount; realIndex++) {
			    // deleted addresses have to be skipped!
			    if(editorContact.getAddresses().get(realIndex).getDeleted()) {
			        continue;
			    }
				createAddressTabForBillingType(msg.editorContactLabelAdditionaladdress, realIndex, visibleIndex, invisible, createAddressPanel(), SWT.CLOSE);
				visibleIndex++;
			}
		}
		
		addressTabFolder.addCTabFolder2Listener(CTabFolder2Listener.closeAdapter(e -> {
		    Object index = ((CTabItem)e.item).getData(ADDRESS_INDEX);
		    boolean dirtyState = true;
	        if(index == null || !(index instanceof Number)) {
	            return;
	        }

	        Integer currentIdx = (Integer)index;
		    if(getMDirtyablePart().isDirty() || editorContact.getAddresses().get(currentIdx).getId() != 0) {
    			e.doit = MessageDialog.openQuestion(top.getShell(), msg.dialogMessageboxTitleWarning, msg.editorContactQuestionRemoveaddress);
		    } else { // just added, but not edited yet
		        dirtyState = false;
		    }
		    
			if(e.doit) {
			    // delete address
			    removeAddressFromContact(index);
			    Object addressTabWidget = ((CTabItem)e.item).getData(ADDRESS_TAB_WIDGET);
			    if(addressTabWidget != null && addressTabWidget instanceof AddressTabWidget) {
			        addressTabWidgets.remove(((AddressTabWidget)addressTabWidget));
			    }
			    setDirty(dirtyState);
			}
		}));

		GridDataFactory.fillDefaults().grab(true, true).applyTo(addressTabFolder);
		addressTabFolder.setFocus();
	}

	/**
	 * Remove the given address (specified by index) from contact. If the address wasn't persisted,
	 * the address is deleted from object only. If the address was persisted (id != 0), then it'll be
	 * set to "deleted".
	 * 
	 * @param data 
	 */
	private void removeAddressFromContact(Object index) {
        if(!(index instanceof Number)) {
            return;
        }

        // for deletion we have to use the "real" address list (not only the visible entries)
        Integer currentIdx = (Integer)index;
        if(editorContact.getAddresses().size() < currentIdx) {
            return;
        }
        
        if(editorContact.getAddresses().get(currentIdx).getId() > 0) {
            editorContact.getAddresses().get(currentIdx).setDeleted(Boolean.TRUE);
            editorContact.getAddresses().get(currentIdx).setValidTo(new Date());
        } else {
            editorContact.getAddresses().remove(currentIdx.intValue());
        }
    }
	
	private List<Address> getVisibleAddresses() {
	    return editorContact.getAddresses().stream().filter(a -> !a.getDeleted()).collect(Collectors.toList());
	}

    private Map<Integer, String> getSalutationMap() {
		if(salutationMap == null || salutationMap.isEmpty()) {
			salutationMap = new HashMap<>();
			for (int i = 0; i <= ContactUtil.MAX_SALUTATION_COUNT; i++) {
			    salutationMap.put(i, contactUtil.getSalutationString(i));
			}
		}
		return salutationMap;
	}

	private Composite createAddressPanel() {
		Composite addressGroup = new Composite(addressTabFolder, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(4).equalWidth(false).applyTo(addressGroup);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(addressGroup);
		return addressGroup;
	}
	
	private CTabItem createAddressTabForBillingType(String name, Integer realIndex, int visibleIndex, Composite invisible, Composite addressGroup, int style) {
		CTabItem addressForBillingType = new CTabItem(addressTabFolder, style);
		AddressTabWidget addressTabWidget = new AddressTabWidget();
		if(realIndex != null) {
		    addressForBillingType.setText(name + " #" + visibleIndex);
		    addressForBillingType.setData(ADDRESS_INDEX, realIndex);
		} else {
		    addressForBillingType.setText(name);
		}
		addressForBillingType.setData(ADDRESS_TAB_WIDGET, addressTabWidget);
        
        // Name Addon
        Label labelNameAddon = new Label(addressGroup, SWT.NONE);
        //T: Label in the contact editor
        labelNameAddon.setText(msg.editorContactFieldNameAddon);
        labelNameAddon.setToolTipText(msg.editorContactFieldNameAddonTooltip);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelNameAddon);
        Text txtNameAddon = new Text(addressGroup, SWT.BORDER);
        txtNameAddon.setToolTipText(msg.editorContactFieldNameAddonTooltip);
        addressTabWidget.setNameAddon(txtNameAddon);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(txtNameAddon);

		// Local Consultant
		Label labelLocalConsultant = new Label(addressGroup, SWT.NONE);
		//T: Label in the contact editor
		labelLocalConsultant.setText(msg.editorContactFieldLocalconsultant);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelLocalConsultant);
		
		Text txtlocalConsultant = new Text(addressGroup, SWT.BORDER);
		addressTabWidget.setLocalConsultant(txtlocalConsultant);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtlocalConsultant);

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
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtStreet);
      
        final Cursor cursorHand = top.getDisplay().getSystemCursor(SWT.CURSOR_HAND);
        final Cursor cursorIBeam = top.getDisplay().getSystemCursor(SWT.CURSOR_IBEAM);
        
        // EMail
        Label labelEmail = new Label(addressGroup, SWT.NONE);
        //T: Label in the contact editor
        labelEmail.setText(msg.exporterDataEmail);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelEmail);
        Text txtEmail = new Text(addressGroup, SWT.BORDER);
        txtEmail.addMouseMoveListener((e) -> {
            if (e.stateMask == SWT.CTRL) {
                txtEmail.setCursor(cursorHand);
            } else {
                txtEmail.setCursor(cursorIBeam);
            }
        });
        txtEmail.addMouseListener(new UrlCallHandler(txtEmail, log));
        addressTabWidget.setEmail(txtEmail);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(txtEmail);
        
        // Address Addon
        Label labelAddressAddon = new Label(addressGroup, SWT.NONE);
        //T: Label in the contact editor
        labelAddressAddon.setText(msg.editorContactFieldAddressAddon);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelAddressAddon);
        Text txtAddressAddon = new Text(addressGroup, SWT.BORDER);
        addressTabWidget.setAddressAddon(txtAddressAddon);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(txtAddressAddon);

        // Telephone
        Label labelTel = new Label(addressGroup, SWT.NONE);
        //T: Label in the contact editor
        labelTel.setText(msg.exporterDataTelephone);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelTel);
        Text txtPhone = new Text(addressGroup, SWT.BORDER);
        addressTabWidget.setPhone(txtPhone);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(txtPhone);

		// City Addon
		Label labelCityAddon = new Label(addressGroup, SWT.NONE);
		//T: Label in the contact editor
		labelCityAddon.setText(msg.editorContactFieldDistrict);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCityAddon);
		Text txtCityAddon = new Text(addressGroup, SWT.BORDER);
		addressTabWidget.setCityAddon(txtCityAddon);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtCityAddon);

        // additional Phone
        Label labelAdditionalPhone = new Label(addressGroup, SWT.NONE);
        //T: Label in the contact editor
        labelAdditionalPhone.setText(msg.editorContactFieldAdditionalPhone);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelAdditionalPhone);
        Text txtAdditionalPhone = new Text(addressGroup, SWT.BORDER);
        addressTabWidget.setAdditionalPhone(txtAdditionalPhone);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(txtAdditionalPhone);

		// City
		Label labelCity = new Label(addressGroup, SWT.NONE);
		//T: Label in the contact editor
		labelCity.setText(msg.editorContactFieldZipcityName);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCity);
		
		Composite zipAndCity = new Composite(addressGroup, SWT.NONE);
		GridDataFactory.fillDefaults().hint(80, SWT.DEFAULT).grab(true, false).applyTo(zipAndCity);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(false).applyTo(zipAndCity);
		Text txtZip = new Text(zipAndCity, SWT.BORDER);
		addressTabWidget.setZip(txtZip);
		GridDataFactory.fillDefaults().hint(50, SWT.DEFAULT).applyTo(txtZip);
		
		Text txtCity = new Text(zipAndCity, SWT.BORDER);
		addressTabWidget.setCity(txtCity);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtCity);

        // Telefax
        Label labelFax = new Label(addressGroup, SWT.NONE);
        //T: Label in the contact editor
        labelFax.setText(msg.exporterDataTelefax);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelFax);
        Text txtFax = new Text(addressGroup, SWT.BORDER);
        addressTabWidget.setFax(txtFax);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(txtFax);

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
		comboCountry.setInput(localeUtil.getLocaleCountryMap());
		StringComboBoxLabelProvider stringComboBoxLabelProvider = ContextInjectionFactory.make(StringComboBoxLabelProvider.class, context);
		stringComboBoxLabelProvider.setCountryNames(localeUtil.getLocaleCountryMap());
		comboCountry.setLabelProvider(stringComboBoxLabelProvider);
		addressTabWidget.setCountryCombo(comboCountry);
		GridDataFactory.fillDefaults().hint(80, SWT.DEFAULT).grab(false, false).applyTo(comboCountry.getCombo());
        
		// Mobile
		Label labelMobile = new Label(addressGroup, SWT.NONE);
		//T: Label in the contact editor
		labelMobile.setText(msg.exporterDataMobile);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelMobile);
		Text txtMobile = new Text(addressGroup, SWT.BORDER);
		addressTabWidget.setMobile(txtMobile);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtMobile);

        setTabOrder(txtNameAddon, txtStreet);
        setTabOrder(txtStreet, txtAddressAddon);
        setTabOrder(txtAddressAddon, txtCityAddon);
        setTabOrder(txtCityAddon, txtZip);
        setTabOrder(txtCity, comboCountry.getCombo());
        setTabOrder(comboCountry.getCombo(), txtlocalConsultant);
        setTabOrder(txtlocalConsultant, txtEmail);
        setTabOrder(txtEmail, txtPhone);
        setTabOrder(txtPhone, txtAdditionalPhone);
        setTabOrder(txtAdditionalPhone, txtFax);
        setTabOrder(txtFax, txtMobile);
		
		Label labelAddressType = new Label(addressGroup, SWT.NONE);
		labelAddressType.setText(msg.editorContactFieldContacttype);
		labelAddressType.setToolTipText(msg.editorContactFieldContacttypeTooltip);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelAddressType);
		
		final MultiChoice<ContactType> mcSimple = new MultiChoice<ContactType>(addressGroup, SWT.None);
		mcSimple.setLabelProvider(new ContactTypeLabelProvider(msg));
		mcSimple.addAll(ContactType.values());
		addressTabWidget.setContactTypeWidget(mcSimple);
		GridDataFactory.fillDefaults().applyTo(mcSimple);
		
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
		bindModelValue(editorContact, txtFirstname, Contact_.firstName.getName(), 255);
		bindModelValue(editorContact, txtName, Contact_.name.getName(), 255);
		bindModelValue(editorContact, txtCompany, Contact_.company.getName(), 255);
		
		CTabItem[] items = addressTabFolder.getItems();
		for (int i = 0; i < items.length; i++) {
			CTabItem addressTabItem = items[i];
			bindAddressWidgetForIndex(addressTabItem, i);
		}
		
		bindModelValue(editorContact, txtAccountHolder, Contact_.bankAccount.getName() +"." +BankAccount_.accountHolder.getName(), 255);
		bindModelValue(editorContact, txtAccount, Contact_.bankAccount.getName() +"." +BankAccount_.name.getName(), 32);
		bindModelValue(editorContact, txtBankCode, Contact_.bankAccount.getName() +"." +BankAccount_.bankCode.getName(), 32);
		bindModelValue(editorContact, txtBankName, Contact_.bankAccount.getName() +"." +BankAccount_.bankName.getName(), 64);
		bindModelValue(editorContact, txtIBAN, Contact_.bankAccount.getName() +"." +BankAccount_.iban.getName(), 255);
		bindModelValue(editorContact, txtBIC, Contact_.bankAccount.getName() +"." +BankAccount_.bic.getName(), 255);
        bindModelValue(editorContact, txtMandatRef, Contact_.mandateReference.getName(), 255);

		fillAndBindCategoryCombo();
		fillAndBindPaymentCombo();
		
		bindModelValue(editorContact, txtSupplierNr, Contact_.supplierNumber.getName(), 32);
		bindModelValue(editorContact, txtWebsite, Contact_.website.getName(), 255);
		bindModelValue(editorContact, txtWebshopName, Contact_.webshopName.getName(), 128);
		bindModelValue(editorContact, txtAlias, Contact_.alias.getName(), 255);
        bindModelValue(editorContact, dtBirthday, Contact_.birthday.getName());

		bindModelValue(editorContact, comboReliability, Contact_.reliability.getName());
		bindModelValue(editorContact, txtVatNr, Contact_.vatNumber.getName(), 64);
		bindModelValue(editorContact, txtGln, Contact_.gln.getName(), 32);
		bindModelValue(editorContact, txtRegisterNumber, Contact_.registerNumber.getName(), 128);
		bindModelValue(editorContact, txtDiscount, Contact_.discount.getName(), 16);
        bindModelValue(editorContact, comboUseNetGross, Contact_.useNetGross.getName());
		bindModelValue(editorContact, textNote, Contact_.note.getName(), -1);
		
// GS/
		// "bind" the Extras tab
		tabExtras.extractData(textNote);
		
		bindAdditionalValues(editorContact);
		
		part.getTransientData().remove(BIND_MODE_INDICATOR);
    }

	protected void bindAddressWidgetForIndex(CTabItem addressTabItem, int index) {
	    Address currentAddress = getOrCreateAddressByIndexFromContact(index);
		// sometimes the country code is null
		// ==> country is determined by locale
	    if(currentAddress.getCountryCode() == null) {
	    	currentAddress.setCountryCode(localeUtil.getDefaultLocale().getCountry());
	    }
	    
	    AddressTabWidget currentAddressTabWidget = addressTabWidgets.get(index);
		bindModelValue(currentAddress, currentAddressTabWidget.getAddressAddon(), Address_.addressAddon.getName(), 255);
		bindModelValue(currentAddress, currentAddressTabWidget.getNameAddon(), Address_.name.getName(), 255);
		bindModelValue(currentAddress, currentAddressTabWidget.getLocalConsultant(), Address_.localConsultant.getName(), 128);
	    bindModelValue(currentAddress, currentAddressTabWidget.getStreet(), Address_.street.getName(), 255);
	    bindModelValue(currentAddress, currentAddressTabWidget.getZip(), Address_.zip.getName(), 16);
	    bindModelValue(currentAddress, currentAddressTabWidget.getCity(), Address_.city.getName(), 128);
	    bindModelValue(currentAddress, currentAddressTabWidget.getCityAddon(), Address_.cityAddon.getName(), 128);
	    bindModelValue(currentAddress, currentAddressTabWidget.getCountryCombo(), Address_.countryCode.getName());

	    bindListWidget(currentAddress, currentAddressTabWidget.getContactTypeWidget(), Address_.contactTypes);
		
		Binding binding = bindModelValue(currentAddress, currentAddressTabWidget.getEmail(), Address_.email.getName(), 64, emailValidationStrategy, null);
		ControlDecorationSupport.create(binding, SWT.TOP | SWT.LEFT);
		
		bindModelValue(currentAddress, currentAddressTabWidget.getPhone(), Address_.phone.getName(), 64);
		bindModelValue(currentAddress, currentAddressTabWidget.getAdditionalPhone(), Address_.additionalPhone.getName(), 64);
		bindModelValue(currentAddress, currentAddressTabWidget.getFax(), Address_.fax.getName(), 64);
		bindModelValue(currentAddress, currentAddressTabWidget.getMobile(), Address_.mobile.getName(), 64);
	}

	@SuppressWarnings("unchecked")
	private <E extends IEntity> void bindListWidget(E listEntity, Control currentWidget,
			ListAttribute<E, ContactType> property) {

		if (currentWidget instanceof MultiChoice) {
			bindModelList(listEntity, ContactType.class, (MultiChoice<ContactType>) currentWidget, property.getName(),
				null, null);
		}
	}

	protected Address getOrCreateAddressByIndexFromContact(int visibleIndex) {
		// get last address and fill up the address list
		long lastAddressIndex = getVisibleAddresses().size() - 1L;
		if(lastAddressIndex < visibleIndex) {
			do {
				Address address = modelFactory.createAddress();
				address.setCountryCode(localeUtil.getDefaultLocale().getCountry());
				// add no ContactType means this address is a default address for this contact
				editorContact.addToAddresses(address);
			} while(++lastAddressIndex < visibleIndex);
		}
		return getVisibleAddresses().get(visibleIndex);
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
	
	public C getCurrentContact() {
	    return editorContact;
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
		    || !getMDirtyablePart().isDirty()
			|| "".equals(txtName.getText())
			|| "".equals(txtFirstname.getText())
			|| "".equals(street)) return;
		
		// Search the list for an existing data set with the specified value
		Contact testContact = contactDAO.checkContactWithSameValues(txtName.getText()
				, txtFirstname.getText()
				, street);
		if(testContact != null) {
				isDuplicateWarningShown = true;
				MessageDialog.openWarning(top.getShell(), msg.editorContactWarningDuplicate,
						MessageFormat.format(msg.editorContactWarningDuplicateStreet, 
								testContact.getFirstName(), testContact.getName(), testContact.getAddresses().get(0).getStreet()));
		}

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

