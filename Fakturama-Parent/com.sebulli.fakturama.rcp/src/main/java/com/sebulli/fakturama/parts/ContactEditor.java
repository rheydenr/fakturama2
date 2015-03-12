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
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

import com.sebulli.fakturama.dao.ContactCategoriesDAO;
import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.Address_;
import com.sebulli.fakturama.model.BankAccount_;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.model.Contact_;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.ReliabilityType;
import com.sebulli.fakturama.parts.converter.CategoryConverter;
import com.sebulli.fakturama.parts.converter.CommonConverter;
import com.sebulli.fakturama.parts.converter.EntityConverter;
import com.sebulli.fakturama.parts.converter.StringToCategoryConverter;
import com.sebulli.fakturama.parts.converter.StringToEntityConverter;
import com.sebulli.fakturama.parts.widget.ComboBoxLabelProvider;
import com.sebulli.fakturama.parts.widget.EntityComboProvider;
import com.sebulli.fakturama.parts.widget.EntityLabelProvider;
import com.sebulli.fakturama.parts.widget.HashMapContentProvider;
import com.sebulli.fakturama.parts.widget.StringComboBoxLabelProvider;
import com.sebulli.fakturama.parts.widget.StringHashMapContentProvider;
import com.sebulli.fakturama.util.ContactUtil;

/**
 * The contact editor
 * 
 * @author Gerd Bartelt
 */

public class ContactEditor extends Editor<Contact> {
	
    @Inject
    @Translation
    protected Messages msg;

	// Editor's ID
	public static final String ID = "com.sebulli.fakturama.editors.contactEditor";

	// This UniDataSet represents the editor's input 
	private Contact editorContact;

	// SWT widgets of the editor
    private Composite top;

    private TabFolder tabFolder;
	private Text textNote;
//	private Combo comboGender;
	private ComboViewer comboGender;
	private Text txtTitle;
	private Text txtFirstname;
	private Text txtName;
	private Text txtCompany;
	private Text txtStreet;
	private Text txtZip;
	private Text txtCity;
	private ComboViewer comboCountry;
	private CDateTime dtBirthday;
//	private DateTime dtBirthday;
	private ComboViewer comboDeliveryGender;
	private Text txtDeliveryTitle;
	private Text txtDeliveryFirstname;
	private Text txtDeliveryName;
	private CDateTime dtDeliveryBirthday;
	private Text txtDeliveryCompany;
	private Text txtDeliveryStreet;
	private Text txtDeliveryZip;
	private Text txtDeliveryCity;
	private ComboViewer comboDeliveryCountry;
	private Text txtAccountHolder;
	private Text txtAccount;
	private Text txtBankCode;
	private Text txtBankName;
	private Text txtIBAN;
	private Text txtBIC;
    private Text txtMandatRef;
	private Text txtNr;
	private ComboViewer comboPaymentViewer;
	private ComboViewer comboReliability;
	private Text txtPhone;
	private Text txtFax;
	private Text txtMobile;
	private Text txtSupplierNr;
	private Text txtEmail;
	private Text txtWebsite;
	private Text txtVatNr;
	private Text txtDiscount;
	private Combo comboCategory;
	private Group deliveryGroup;
	private Button bDelAddrEquAddr;
	private ComboViewer comboUseNetGross;

	// These flags are set by the preference settings.
	// They define, if elements of the editor are displayed, or not.
	private boolean useDelivery;
	private boolean useBank;
	private boolean useMisc;
	private boolean useNote;
	private boolean useGender;
	private boolean useTitle;
	private boolean useLastNameFirst;
	private boolean useCompany;
	private boolean useCountry;

	// defines, if the contact is new created
	private boolean newContact;
	
	// a reference to a document editor that requests a new address
//	private DocumentEditor documentEditor = null;
	
	/*
	 * Window and Part informations
	 */
	private MPart part;
    
    /**
     * Event Broker for sending update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;
	
	@Inject
	private ContactsDAO contactDAO;
    
    @Inject
    private ContactCategoriesDAO contactCategoriesDAO;
    
    @Inject
    private PaymentsDAO paymentsDao;
	
	@Inject
	@Preference  //(nodePath = "/configuration/contactPreferences")
	protected IEclipsePreferences contactPreferences;

    private ContactUtil contactUtil;

	/**
	 * Saves the contents of this part
	 * 
	 * @param monitor
	 *            Progress monitor
	 */
	@Persist
	public void doSave(IProgressMonitor monitor, @Named(IServiceConstants.ACTIVE_SHELL) Shell parent) {

		/*
		 * the following parameters are not saved: 
		 * - id (constant)
		 * - date_added (constant)
		 */

	    // check for a new contact
		if (newContact) {

			// Check, if the contact number is the next one
			int result = setNextNr(txtNr.getText(), Contact_.customerNumber.getName());

			// It's not the next free ID
			if (result == ERROR_NOT_NEXT_ID) {
				// Display an error message
				MessageBox messageBox = new MessageBox(parent, SWT.ICON_ERROR | SWT.OK);

				//T: Title of the dialog that appears if the item/product number is not valid.
				messageBox.setText(msg.editorContactErrorCustomerid);

				//T: Text of the dialog that appears if the customer number is not valid.
				messageBox.setMessage(msg.editorContactErrorNotnextfreenumber + " " + txtNr.getText() + "\n" + 
						msg.editorContactHintSeepreferences);
				messageBox.open();
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
        // ... done through databinding...

		// Set the customer number
        // ... done through databinding...

		// Set the payment ID
//		IStructuredSelection structuredSelection = (IStructuredSelection) comboPaymentViewer.getSelection();
//		if (!structuredSelection.isEmpty()) {
//			contact.setPayment(((Payment) structuredSelection.getFirstElement()));
//		}

		// Set the miscellaneous data
        // ... done through databinding...
//		contact.setReliability(comboReliability.getSelectionIndex());

		// Set the note
        // ... done through databinding...
// TODO ?		contact.setNote(DataUtils.removeCR(textNote.getText()));

        try {
            // save the new or updated Contact
            editorContact = contactDAO.update(editorContact);
        }
        catch (SQLException e) {
            log.error(e, "can't save the current Contact: " + editorContact.toString());
        }
		newContact = false;

//		// Sets the address
//		if (documentEditor != null) {
//			documentEditor.setAddress(contact);
//		}
		
		// Set the Editor's name to the first name and last name of the contact.
		
		String nameWithCompany = contactUtil.getNameWithCompany(editorContact);
		if(nameWithCompany.contains("\r")) {
			nameWithCompany = nameWithCompany.split("\\r")[0];
		} else if (nameWithCompany.contains("\n")) {
			nameWithCompany = nameWithCompany.split("\\n")[0];
		}
		part.setLabel(nameWithCompany);

//		// Refresh the table view of all contacts
        evtBroker.post("ContactEditor", "update");
        
        // reset dirty flag
        getMDirtyablePart().setDirty(false);
	}

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
	    contactUtil = ContactUtil.getInstance(contactPreferences);

        Long objId = null;
        this.part = (MPart) parent.getData("modelElement");
        String tmpObjId = (String) part.getProperties().get(CallEditor.PARAM_OBJ_ID);
        if (StringUtils.isNumeric(tmpObjId)) {
            objId = Long.valueOf(tmpObjId);
            // Set the editor's data set to the editor's input
            editorContact = contactDAO.findById(objId);
        }


		// Get the document that requests a new address
//		documentEditor = ((UniDataSetEditorInput) input).getDocumentEditor();
		
		// Test, if the editor is opened to create a new data set. This is,
		// if there is no input set.
		newContact = (editorContact == null);

		// If new ..
		if (newContact) {

			// Create a new data set
			editorContact = new Contact();  // TODO category?
			//T: Contact Editor Title of the editor if the data set is a new one.
			part.setLabel(msg.commandNewContactName);

			// Set the payment to the standard value
// TODO			contact.setPayment(contactPreferences.getInt("standardpayment", 1));

			// Get the next contact number
			editorContact.setCustomerNumber(getNextNr());

		}
		else {

			// Set the Editor's name to the first name and last name of the contact.
			part.setLabel(contactUtil.getNameWithCompany(editorContact));
		}
		
		createPartControl(parent);
	}

	/**
	 * Defines, if the delivery address is equal to the billing address
	 * 
	 * @param isEqual
	 */
	private void deliveryAddressIsEqual(boolean isEqual) {
		deliveryGroup.setVisible(!isEqual);
		if (isEqual)
			copyAddressToDeliveryAdress();
	}

	/**
	 * Copy all the address data to the delivery address
	 */
	private void copyAddressToDeliveryAdress() {
//		comboDeliveryGender.select(comboGender.getSelectionIndex());
		txtDeliveryTitle.setText(txtTitle.getText());
		txtDeliveryFirstname.setText(txtFirstname.getText());
		txtDeliveryName.setText(txtName.getText());
		txtDeliveryCompany.setText(txtCompany.getText());
		txtDeliveryStreet.setText(txtStreet.getText());
		txtDeliveryZip.setText(txtZip.getText());
		txtDeliveryCity.setText(txtCity.getText());
//		txtDeliveryCountry.setText(comboCountry.getText());
	}

//	/**
//	 * Returns, if the address is equal to the delivery address
//	 * 
//	 * @return True, if both are equal
//	 */
//	private boolean isAddressEqualToDeliveryAdress() {
//		if (comboDeliveryGender.getSelectionIndex() != comboGender.getSelectionIndex()) { return false; }
//		if (!txtDeliveryTitle.getText().equals(txtTitle.getText())) { return false; }
//		if (!txtDeliveryFirstname.getText().equals(txtFirstname.getText())) { return false; }
//		if (!txtDeliveryName.getText().equals(txtName.getText())) { return false; }
//		if (!txtDeliveryCompany.getText().equals(txtCompany.getText())) { return false; }
//		if (!txtDeliveryStreet.getText().equals(txtStreet.getText())) { return false; }
//		if (!txtDeliveryZip.getText().equals(txtZip.getText())) { return false; }
//		if (!txtDeliveryCity.getText().equals(txtCity.getText())) { return false; }
//		if (!txtDeliveryCountry.getText().equals(txtCountry.getText())) { return false; }
//
//		return true;
//	}

	/**
	 * Creates the SWT controls for this workbench part
	 * 
	 * @param the
	 *            parent control
	 */
	public void createPartControl(Composite parent) {

		// Some of this editos's control elements can be hidden.
		// Get the these settings from the preference store
		useDelivery = contactPreferences.getBoolean("CONTACT_USE_DELIVERY", false);
		useBank = contactPreferences.getBoolean("CONTACT_USE_BANK", false );
		useMisc = contactPreferences.getBoolean("CONTACT_USE_MISC", false);
		useNote = contactPreferences.getBoolean("CONTACT_USE_NOTE", false);
		useGender = contactPreferences.getBoolean("CONTACT_USE_GENDER", false);
		useTitle = contactPreferences.getBoolean("CONTACT_USE_TITLE", false);
		useLastNameFirst = (contactPreferences.getInt("CONTACT_NAME_FORMAT", 1) == 1);
		useCompany = contactPreferences.getBoolean("CONTACT_USE_COMPANY", false);
		useCountry = contactPreferences.getBoolean("CONTACT_USE_COUNTRY", false);
		
		// now do some helpful initializations (needed for combo boxes)
        Map<String, String> countryNames = LocaleUtil.getInstance().getLocaleCountryMap();
		
		Map<Integer, String> genderList = new HashMap<>();
		for (int i = 0; i < 4; i++) {
		    genderList.put(i, getGenderString(i));
		} 

		// Create the parent Composite
        top = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(top);
//
		// Create an invisible container for all hidden components	
		Composite invisible = new Composite(top, SWT.NONE);
		invisible.setVisible(false);
		GridDataFactory.fillDefaults().hint(0, 0).applyTo(invisible);

		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ContextHelpConstants.CONTACT_EDITOR);

		// Create the address tab
		Composite tabAddress;
		if (useDelivery || useBank || useMisc || useNote) {
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
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(tabAddress);

		// Create the bank tab
		Composite tabBank;
		if (useBank) {
			TabItem item3 = new TabItem(tabFolder, SWT.NONE);
			//T: Label in the contact editor
			item3.setText(msg.editorContactLabelBankaccount);
			tabBank = new Composite(tabFolder, SWT.NONE);
			item3.setControl(tabBank);
		}
		else {
			tabBank = new Composite(invisible, SWT.NONE);
		}
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(tabBank);

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

		// Composite for the customer's number
		Composite customerNrComposite = new Composite(tabAddress, SWT.NONE);
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
		bindModelValue(editorContact, txtNr, Contact_.customerNumber.getName(), 32);
		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(txtNr);

		// Check button: delivery address equals address
		bDelAddrEquAddr = new Button(tabAddress, SWT.CHECK);
		//T: Label in the contact editor
		bDelAddrEquAddr.setText(msg.editorContactFieldDeliveryaddressequalsName);
		GridDataFactory.swtDefaults().applyTo(bDelAddrEquAddr);
		bDelAddrEquAddr.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				deliveryAddressIsEqual(bDelAddrEquAddr.getSelection());
//				checkDirty();
			}
		});

		// Group: address
		Group addressGroup = new Group(tabAddress, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(addressGroup);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(addressGroup);
		//T: Label in the contact editor
		addressGroup.setText(msg.editorContactLabelAddress);

		// Controls in the group "address"

		// The title and gender's label
		Label labelTitle = new Label((useGender || useTitle) ? addressGroup : invisible, SWT.NONE);
		if (useGender) {
			labelTitle.setText(msg.commonFieldGender);
		}
		if (useGender && useTitle) {
			labelTitle.setText(labelTitle.getText() + ", ");
		}
		if (useTitle) {
			//T: "Title" ( part of an address)
			labelTitle.setText(labelTitle.getText() + msg.commonFieldTitle);
		}
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelTitle);

		// Gender
		comboGender = new ComboViewer(useGender ? addressGroup : invisible, SWT.BORDER);
		comboGender.setContentProvider(new HashMapContentProvider<Integer, String>());
		comboGender.setInput(genderList);
		comboGender.setLabelProvider(new ComboBoxLabelProvider<Integer, String>(genderList));
		bindModelValue(editorContact, comboGender, Contact_.gender.getName());
		GridDataFactory.fillDefaults().grab(false, false).hint(100, SWT.DEFAULT).span(useTitle ? 1 : 2, 1).applyTo(comboGender.getControl());

		// Title
		txtTitle = new Text(useTitle ? addressGroup : invisible, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(useGender ? 1 : 2, 1).applyTo(txtTitle);
		bindModelValue(editorContact, txtTitle, Contact_.title.getName(), 32);

		// First and last name		
		Label labelName = new Label(addressGroup, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);
		if (useLastNameFirst) {
			//T: Format of the name in an address
			labelName.setText(msg.editorContactFieldLastnamefirstnameName);
			txtName = new Text(addressGroup, SWT.BORDER);
			GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(txtName);
			txtFirstname = new Text(addressGroup, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(txtFirstname);
		}
		else {
			//T: Format of the name in an address
			labelName.setText(msg.editorContactFieldFirstnamelastnameName);
			txtFirstname = new Text(addressGroup, SWT.BORDER);
			GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(txtFirstname);
			txtName = new Text(addressGroup, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(txtName);
		}
		bindModelValue(editorContact, txtFirstname, Contact_.firstName.getName(), 64);
		bindModelValue(editorContact, txtName, Contact_.name.getName(), 64);

		// Company
		Label labelCompany = new Label(useCompany ? addressGroup : invisible, SWT.NONE);
		//T: Label in the contact editor
		labelCompany.setText(msg.commonFieldCompany);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCompany);
		txtCompany = new Text(useCompany ? addressGroup : invisible, SWT.BORDER | SWT.MULTI);
//		txtCompany.setText(DataUtils.makeOSLineFeeds(editorContact.getCompany()));
		bindModelValue(editorContact, txtCompany, Contact_.company.getName(), 64);
		GridDataFactory.fillDefaults().hint(210, 40).grab(true, false).span(2, 1).applyTo(txtCompany);

		// Street
		Label labelStreet = new Label(addressGroup, SWT.NONE);
		//T: Label in the contact editor
		labelStreet.setText(msg.commonFieldStreet);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelStreet);
		txtStreet = new Text(addressGroup, SWT.BORDER);
		bindModelValue(editorContact, txtStreet, Contact_.address.getName() + "." + Address_.street.getName(), 64);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(txtStreet);
		setTabOrder(txtCompany, txtStreet);

		// City
		Label labelCity = new Label(addressGroup, SWT.NONE);
		//T: Label in the contact editor
		labelCity.setText(msg.editorContactFieldZipcityName);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCity);
		txtZip = new Text(addressGroup, SWT.BORDER);
		bindModelValue(editorContact, txtZip, Contact_.address.getName() + "." + Address_.zip.getName(), 16);
		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(txtZip);
		txtCity = new Text(addressGroup, SWT.BORDER);
		bindModelValue(editorContact, txtCity, Contact_.address.getName() + "." + Address_.city.getName(), 32);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtCity);

		// Country
		Label labelCountry = new Label(useCountry ? addressGroup : invisible, SWT.NONE);
		//T: Label in the contact editor
		labelCountry.setText(msg.commonFieldCountry);
		//T: Tool Tip Text
		labelCountry.setToolTipText(msg.editorContactHintSethomecountry);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCountry);

		comboCountry = new ComboViewer(useCountry ? addressGroup : invisible, SWT.BORDER);
		comboCountry.getCombo().setToolTipText(labelCountry.getToolTipText());
		comboCountry.setContentProvider(new StringHashMapContentProvider());
		comboCountry.setInput(countryNames);
		comboCountry.setLabelProvider(new StringComboBoxLabelProvider(countryNames));
		bindModelValue(editorContact, comboCountry, Contact_.address.getName() + "." + Address_.country.getName());
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(comboCountry.getCombo());
		
		// Birthday
		Label labelBirthday = new Label(addressGroup, SWT.NONE);
		//T: Label in the contact editor
		labelBirthday.setText(msg.editorContactFieldBirthdayName);
		//T: Tool Tip Text
		labelBirthday.setToolTipText(msg.editorContactFieldBirthdayTooltip);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelBirthday);		

        dtBirthday = new CDateTime(addressGroup, CDT.BORDER | CDT.DROP_DOWN);
        dtBirthday.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
        dtBirthday.setFormat(CDT.DATE_MEDIUM);
		dtBirthday.setToolTipText(labelBirthday.getToolTipText());
		GridDataFactory.swtDefaults().applyTo(dtBirthday);
		// Set the dtBirthday widget to the contact's birthday date
		bindModelValue(editorContact, dtBirthday, Contact_.birthday.getName());

		// Group: delivery address
		deliveryGroup = new Group(tabAddress, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(deliveryGroup);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(deliveryGroup);
		//T: Label in the contact editor
		deliveryGroup.setText(msg.commonFieldDeliveryaddress);

		// Controls in the group "Delivery"

		// Delivery gender and titel's label
		Label labelDeliveryTitle = new Label((useGender || useTitle) ? deliveryGroup : invisible, SWT.NONE);
		if (useGender)
			labelDeliveryTitle.setText(msg.commonFieldGender);
		if (useGender && useTitle)
			labelDeliveryTitle.setText(labelDeliveryTitle.getText() + ", ");
		if (useTitle)
			//T: "Title" (part of an address)
			labelDeliveryTitle.setText(labelDeliveryTitle.getText() + msg.commonFieldTitle);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDeliveryTitle);

		// Delivery Gender
		comboDeliveryGender = new ComboViewer(useGender ? deliveryGroup : invisible, SWT.BORDER);
		comboDeliveryGender.setContentProvider(new HashMapContentProvider<Integer, String>());
        comboDeliveryGender.setInput(genderList);
        comboDeliveryGender.setLabelProvider(new ComboBoxLabelProvider(genderList));
		bindModelValue(editorContact, comboDeliveryGender, Contact_.deliveryContacts.getName() +"." +Contact_.gender.getName());
		GridDataFactory.fillDefaults().grab(false, false).hint(100, SWT.DEFAULT).span(useTitle ? 1 : 2, 1).applyTo(comboDeliveryGender.getCombo());
		
		// Delivery Title
		txtDeliveryTitle = new Text(useTitle ? deliveryGroup : invisible, SWT.BORDER);
		bindModelValue(editorContact, txtDeliveryTitle, Contact_.deliveryContacts.getName() +"." +Contact_.title.getName(), 32);
		GridDataFactory.fillDefaults().grab(true, false).span(useGender ? 1 : 2, 1).applyTo(txtDeliveryTitle);

		// Delivery first and last name
		Label labelDeliveryName = new Label(deliveryGroup, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDeliveryName);
		if (useLastNameFirst) {
			//T: Format of the name in an address
			labelDeliveryName.setText(msg.editorContactFieldLastnamefirstnameName);
			txtDeliveryName = new Text(deliveryGroup, SWT.BORDER);
			GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(labelDeliveryName);
			txtDeliveryFirstname = new Text(deliveryGroup, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(txtDeliveryFirstname);
		}
		else {
			//T: Format of the name in an address
			labelDeliveryName.setText(msg.editorContactFieldFirstnamelastnameName);
			txtDeliveryFirstname = new Text(deliveryGroup, SWT.BORDER);
			GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(txtDeliveryFirstname);
			txtDeliveryName = new Text(deliveryGroup, SWT.BORDER);
			GridDataFactory.fillDefaults().grab(true, false).applyTo(txtDeliveryName);

		}
		bindModelValue(editorContact, txtDeliveryFirstname, Contact_.deliveryContacts.getName() +"." +Contact_.firstName.getName(), 64);
		bindModelValue(editorContact, txtDeliveryName, Contact_.deliveryContacts.getName() +"." +Contact_.name.getName(), 64);

		// Delivery company
		Label labelDeliveryCompany = new Label(useCompany ? deliveryGroup : invisible, SWT.NONE);
		//T: Label in the contact editor
		labelDeliveryCompany.setText(msg.commonFieldCompany);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDeliveryCompany);
		txtDeliveryCompany = new Text(useCompany ? deliveryGroup : invisible, SWT.BORDER | SWT.MULTI);
		bindModelValue(editorContact, txtDeliveryCompany, Contact_.deliveryContacts.getName() +"." +Contact_.company.getName(), 64);
		GridDataFactory.fillDefaults().hint(210, 40).grab(true, false).span(2, 1).applyTo(txtDeliveryCompany);

		// Delivery street
		Label labelDeliveryStreet = new Label(deliveryGroup, SWT.NONE);
		//T: Label in the contact editor
		labelDeliveryStreet.setText(msg.commonFieldStreet);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDeliveryStreet);
		txtDeliveryStreet = new Text(deliveryGroup, SWT.BORDER);
		bindModelValue(editorContact, txtDeliveryStreet, Contact_.deliveryContacts.getName() +"." +Contact_.address.getName() +"." + Address_.street.getName(), 64);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(txtDeliveryStreet);
		setTabOrder(txtDeliveryCompany, txtDeliveryStreet);

		// Delivery city
		Label labelDeliveryCity = new Label(deliveryGroup, SWT.NONE);
		//T: Label in the contact editor
		labelDeliveryCity.setText(msg.editorContactFieldZipcityName);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDeliveryCity);
		txtDeliveryZip = new Text(deliveryGroup, SWT.BORDER);
		bindModelValue(editorContact, txtDeliveryZip, Contact_.deliveryContacts.getName() +"." +Contact_.address.getName() +"." + Address_.zip.getName(), 16);
		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(txtDeliveryZip);
		txtDeliveryCity = new Text(deliveryGroup, SWT.BORDER);
		bindModelValue(editorContact, txtDeliveryCity, Contact_.deliveryContacts.getName() +"." +Contact_.address.getName() +"." + Address_.city.getName(), 32);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtDeliveryCity);

		// Delivery country
		Label labelDeliveryCountry = new Label(useCountry ? deliveryGroup : invisible, SWT.NONE);
		//T: Label in the contact editor
		labelDeliveryCountry.setText(msg.commonFieldCountry);
		labelDeliveryCountry.setToolTipText(labelCountry.getToolTipText());
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDeliveryCountry);
		comboDeliveryCountry = new ComboViewer(useCountry ? deliveryGroup : invisible, SWT.BORDER);
		comboDeliveryCountry.getCombo().setToolTipText(labelCountry.getToolTipText());
        comboDeliveryCountry.setContentProvider(new StringHashMapContentProvider());
        comboDeliveryCountry.setInput(countryNames);
        comboDeliveryCountry.setLabelProvider(new StringComboBoxLabelProvider(countryNames));
		bindModelValue(editorContact, comboDeliveryCountry, Contact_.deliveryContacts.getName() +"." +Contact_.address.getName() +"." + Address_.country.getName());
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(comboDeliveryCountry.getCombo());
		
		// Deliverer's Birthday
		Label labelDelivererBirthday = new Label(deliveryGroup, SWT.NONE);
		//T: Label in the deliverer editor
		labelDelivererBirthday.setText(msg.editorContactFieldBirthdayName);
		//T: Tool Tip Text
		labelDelivererBirthday.setToolTipText(msg.editorContactFieldDeliverersbirthdayTooltip);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDelivererBirthday);		

		dtDeliveryBirthday = new CDateTime(deliveryGroup, CDT.BORDER | CDT.DROP_DOWN);
		dtDeliveryBirthday.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		dtDeliveryBirthday.setFormat(CDT.DATE_MEDIUM);
		dtDeliveryBirthday.setToolTipText(labelDelivererBirthday.getToolTipText());
		GridDataFactory.swtDefaults().applyTo(dtDeliveryBirthday);
		bindModelValue(editorContact, dtDeliveryBirthday, Contact_.deliveryContacts.getName() +"." +Contact_.birthday.getName());

		// Controls in the tab "Bank"

		// Account holder
		Label labelAccountHolder = new Label(tabBank, SWT.NONE);
		//T: Label in the contact editor
		labelAccountHolder.setText(msg.commonFieldAccountholder);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelAccountHolder);
		txtAccountHolder = new Text(tabBank, SWT.BORDER);
		bindModelValue(editorContact, txtAccountHolder, Contact_.bankAccount.getName() +"." +BankAccount_.accountHolder.getName(), 64);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtAccountHolder);

		// Account number
		Label labelAccount = new Label(tabBank, SWT.NONE);
		//T: Label in the contact editor
		labelAccount.setText(msg.editorContactFieldAccountnumberName);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelAccount);
		txtAccount = new Text(tabBank, SWT.BORDER);
		bindModelValue(editorContact, txtAccount, Contact_.bankAccount.getName() +"." +BankAccount_.account.getName(), 32);
		txtAccount.setEnabled(false);
		txtAccount.setToolTipText(msg.editorContactFieldAccountnumberDisabledinfo);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtAccount);

		// Bank code
		Label labelBankCode = new Label(tabBank, SWT.NONE);
		//T: Label in the contact editor
		labelBankCode.setText(msg.editorContactFieldBankcodeName);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelBankCode);
		txtBankCode = new Text(tabBank, SWT.BORDER);
		txtBankCode.setToolTipText(msg.editorContactFieldBankcodeDisabledinfo);
		txtBankCode.setEnabled(false);
		bindModelValue(editorContact, txtBankCode, Contact_.bankAccount.getName() +"." +BankAccount_.bankCode.getName(), 32);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtBankCode);

		// Name of the bank
		Label labelBankName = new Label(tabBank, SWT.NONE);
		//T: Label in the contact editor
		labelBankName.setText(msg.editorContactFieldBankName);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelBankName);
		txtBankName = new Text(tabBank, SWT.BORDER);
		bindModelValue(editorContact, txtBankName, Contact_.bankAccount.getName() +"." +BankAccount_.bankName.getName(), 64);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtBankName);

		// IBAN Bank code
		Label labelIBAN = new Label(tabBank, SWT.NONE);
		//T: Bank code
		labelIBAN.setText(msg.exporterDataIban);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelIBAN);
		txtIBAN = new Text(tabBank, SWT.BORDER);
		bindModelValue(editorContact, txtIBAN, Contact_.bankAccount.getName() +"." +BankAccount_.iban.getName(), 32);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtIBAN);

		// BIC
		Label labelBIC = new Label(tabBank, SWT.NONE);
		//T: Bank code
		labelBIC.setText(msg.exporterDataBic);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelBIC);
		txtBIC = new Text(tabBank, SWT.BORDER);
		bindModelValue(editorContact, txtBIC, Contact_.bankAccount.getName() +"." +BankAccount_.bic.getName(), 32);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtBIC);
        
        // Customer's Mandat reference
        Label labelMandate = new Label(tabBank, SWT.NONE);
        //T: Mandate reference
        labelMandate.setText(msg.editorContactFieldMandaterefName);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelMandate);
        txtMandatRef = new Text(tabBank, SWT.BORDER);
        bindModelValue(editorContact,txtMandatRef, Contact_.mandateReference.getName(), 32);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(txtMandatRef); 

		// Controls in tab "Misc"

		// Category 
		Label labelCategory = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelCategory.setText(msg.commonFieldCategory);
		//T: Tool Tip Text
		labelCategory.setToolTipText(msg.editorContactFieldCategoryTooltip);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCategory);

		createCategoryCombo(tabMisc);
        comboCategory.setToolTipText(labelCategory.getToolTipText());

		// Suppliernumber
		Label labelSupplier = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelSupplier.setText(msg.editorContactFieldSuppliernumberName);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelSupplier);
		txtSupplierNr = new Text(tabMisc, SWT.BORDER);
		bindModelValue(editorContact, txtSupplierNr, Contact_.supplierNumber.getName(), 64);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtSupplierNr);
		
		// EMail
		Label labelEmail = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelEmail.setText(msg.exporterDataEmail);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelEmail);
		txtEmail = new Text(tabMisc, SWT.BORDER);
		bindModelValue(editorContact, txtEmail, Contact_.email.getName(), 64);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtEmail);

		// Telephone
		Label labelTel = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelTel.setText(msg.exporterDataTelephone);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelTel);
		txtPhone = new Text(tabMisc, SWT.BORDER);
		bindModelValue(editorContact, txtPhone, Contact_.phone.getName(), 32);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtPhone);

		// Telefax
		Label labelFax = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelFax.setText(msg.exporterDataTelefax);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelFax);
		txtFax = new Text(tabMisc, SWT.BORDER);
		bindModelValue(editorContact, txtFax, Contact_.fax.getName(), 32);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtFax);

		// Mobile
		Label labelMobile = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelMobile.setText(msg.exporterDataMobile);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelMobile);
		txtMobile = new Text(tabMisc, SWT.BORDER);
		bindModelValue(editorContact, txtMobile, Contact_.mobile.getName(), 32);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtMobile);

		// Web Site
		Label labelWebsite = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelWebsite.setText(msg.exporterDataWebsite);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelWebsite);
		txtWebsite = new Text(tabMisc, SWT.BORDER);
		bindModelValue(editorContact, txtWebsite, Contact_.website.getName(), 64);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtWebsite);

		// Payment
		Label labelPayment = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelPayment.setText(msg.editorContactFieldPaymentName);
		//T: Tool Tip Text
		labelPayment.setToolTipText(msg.editorContactFieldPaymentTooltip);

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelPayment);
		comboPaymentViewer = new ComboViewer(tabMisc, SWT.BORDER);
		comboPaymentViewer.getCombo().setToolTipText(labelPayment.getToolTipText());
		List<Payment> allPayments = paymentsDao.findAll();
		comboPaymentViewer.setContentProvider(new EntityComboProvider());
		comboPaymentViewer.setLabelProvider(new EntityLabelProvider());
		comboPaymentViewer.setInput(allPayments);
        UpdateValueStrategy paymentModel2Target = new UpdateValueStrategy();
        paymentModel2Target.setConverter(new EntityConverter<Payment>(Payment.class));
        UpdateValueStrategy target2PaymentModel = new UpdateValueStrategy();
        target2PaymentModel.setConverter(new StringToEntityConverter<Payment>(allPayments, Payment.class));
		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboPaymentViewer.getCombo());
        bindModelValue(editorContact, comboPaymentViewer.getCombo(), Contact_.payment.getName(),
                target2PaymentModel, paymentModel2Target);

		// Reliability
		Label labelReliability = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelReliability.setText(msg.editorContactFieldReliabilityName);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelReliability);
		comboReliability = new ComboViewer(tabMisc, SWT.BORDER);
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

		bindModelValue(editorContact, comboReliability, Contact_.reliability.getName());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboReliability.getControl());
		
		// VAT number
		Label labelVatNr = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelVatNr.setText(msg.exporterDataVatno);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelVatNr);
		txtVatNr = new Text(tabMisc, SWT.BORDER);
		bindModelValue(editorContact, txtVatNr, Contact_.vatNumber.getName(), 32);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtVatNr);

		// Customer's discount
		Label labelDiscount = new Label(tabMisc, SWT.NONE);
		//T: Customer's discount
		labelDiscount.setText(msg.exporterDataRebate);
		//T: Tool Tip Text
		labelDiscount.setToolTipText(msg.editorContactFieldDiscountTooltip);

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDiscount);
		txtDiscount = new Text(tabMisc, SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtDiscount);
		txtDiscount.setToolTipText(labelDiscount.getToolTipText());
////		txtDiscount.addFocusListener(new FocusAdapter() {
////			public void focusLost(FocusEvent e) {
////				txtDiscount.setText(DataUtils.DoubleToFormatedPercent(DataUtils.StringToDoubleDiscount(txtDiscount.getText())));
////				checkDirty();
////			}
////		});
//		txtDiscount.addKeyListener(new KeyAdapter() {
//			public void keyPressed(KeyEvent e) {
//				if (e.keyCode == 13) {
//					txtDiscount.setText(DataUtils.DoubleToFormatedPercent(DataUtils.StringToDoubleDiscount(txtDiscount.getText())));
//					checkDirty();
//				}
//			}
//		});

		// Use net or gross
		Label labelNetGross = new Label(tabMisc, SWT.NONE);
		//T: Label in the contact editor
		labelNetGross.setText(msg.editorContactFieldNetgrossName);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelNetGross);
		comboUseNetGross = new ComboViewer(tabMisc, SWT.BORDER);
		comboUseNetGross.setContentProvider(ArrayContentProvider.getInstance());
//		comboUseNetGross.setInput(new String[]{"---", msg.productDataNet, msg.productDataGross});
		comboUseNetGross.setInput(new Short[]{0, 1, 2});
		comboUseNetGross.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                Short type = (Short)element;
                switch (type) {
                case 0:
                    return "---";
                case 1:
		//T: Entry in a combo box of the the contact editor. Use Net or Gross 
                    return msg.productDataNet;
                case 2:
		//T: Entry in a combo box of the the contact editor. Use Net or Gross 
                    return msg.productDataGross;
                default:
                    return null;
                }
            }
        });
		
		// If the value is -1, use 0 instead
		if (editorContact.getUseNetGross()== null || editorContact.getUseNetGross()<0) {
		    editorContact.setUseNetGross((short)0);
		}

        bindModelValue(editorContact, comboUseNetGross, Contact_.useNetGross.getName());
		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboUseNetGross.getCombo());

		// Controls in tab "Note"

		// The note
//		String note = DataUtils.makeOSLineFeeds(editorContact.getNote());
		textNote = new Text(tabNote, SWT.BORDER | SWT.MULTI);
		bindModelValue(editorContact, textNote, Contact_.note.getName(), 10000);

//		// If the note is not empty, display it,
//		// when opening the editor.
//		if (useNote && !note.isEmpty())
//			tabFolder.setSelection(item5);

		// Test, if the address and the delivery address
		// are equal. If they are, set the checkbox and
		// hide the delivery address
		Boolean isEqual = editorContact.getDeliveryContacts() == null;
		bDelAddrEquAddr.setSelection(isEqual);
		deliveryGroup.setVisible(!isEqual);
	}

    /**
     * creates the combo box for the VAT category
     * @param tabMisc 
     */
    private void createCategoryCombo(Composite tabMisc) {
        // Collect all category strings as a sorted Set
        final TreeSet<ContactCategory> categories = new TreeSet<ContactCategory>(new Comparator<ContactCategory>() {
            @Override
            public int compare(ContactCategory cat1, ContactCategory cat2) {
                return cat1.getName().compareTo(cat2.getName());
            }
        });
        categories.addAll(contactCategoriesDAO.findAll());

        comboCategory = new Combo(tabMisc, SWT.BORDER);
        ComboViewer viewer = new ComboViewer(comboCategory);
        viewer.setContentProvider(new ArrayContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                return categories.toArray();
            }
        });
        
        // Add all categories to the combo
        viewer.setInput(categories);
        viewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return element instanceof ContactCategory ? CommonConverter.getCategoryName((ContactCategory)element, "") : null;
            }
        });

        UpdateValueStrategy catModel2Target = new UpdateValueStrategy();
        catModel2Target.setConverter(new CategoryConverter<ContactCategory>(ContactCategory.class));
        
        UpdateValueStrategy target2CatModel = new UpdateValueStrategy();
        target2CatModel.setConverter(new StringToCategoryConverter<ContactCategory>(categories, ContactCategory.class));
        bindModelValue(editorContact, comboCategory, Contact_.categories.getName(), target2CatModel, catModel2Target);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(comboCategory);
    }

	/**
	 * Test, if there is a document with the same number
	 * 
	 * @return TRUE, if one with the same number is found
	 */
	public boolean thereIsOneWithSameNumber() {

//		// Cancel, if there is already a document with the same ID
//		if (Data.INSTANCE.getDocuments().isExistingDataSet(contact, "nr", txtNr.getText())) {
//			// Display an error message
//			MessageBox messageBox = new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);
//
//			//T: Title of the dialog that appears if the item/product number is not valid.
//			messageBox.setText(msg("Error in customer ID"));
//
//			//T: Text of the dialog that appears if the customer number is not valid.
//			messageBox.setMessage(msg("There is already a customer with the number:") + " " + txtNr.getText());
//			messageBox.open();
//
//			return true;
//		}

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
//		// Save is allowed, if there is no product with the same number
//		return !thereIsOneWithSameNumber();
//	}
//	

    /**
     * Get the gender String by the gender number
     * 
     * @param i
     *            Gender number
     * @return Gender as string
     */
    public String getGenderString(int i) {
        return getGenderString(i, true);
    }

    /**
     * Get the gender String by the gender number
     * 
     * @param i
     *            Gender number
     * @param translate
     *            TRUE, if the string should be translated
     * @return Gender as string
     */
    public String getGenderString(int i, boolean translate) {
        switch (i) {
        case 0:
            return "---";
        case 1:
            //T: Gender
            return msg.contactFieldMrName;
        case 2:
            //T: Gender
            return msg.contactFieldMsName;
        case 3:
            return msg.commonFieldCompany;
        }
        return "";
    }

	/**
	 * Get the gender number by the string
	 * 
	 * @param s
	 *          Gender string
	 * @return
	 * 			The number
	 */
	public int getGenderID(String s) {
		// Test all strings
		for (int i = 0;i < 4 ; i++) {
			if (getGenderString(i,false).equalsIgnoreCase(s)) return i;
			if (getGenderString(i,true).equalsIgnoreCase(s)) return i;
		}
		// Default = "---"
		return 0;
	}

/**
 * Get the reliability String by the number
 * 
 * @param i
 *            Gender number
 * @return Gender as string
 */
public String getReliabilityString(ReliabilityType type) {
	return getReliabilityString(type, true);
}

/**
 * Get the reliability String by the number
 * 
 * @param type
 *            Gender number
 * @param translate
 *            TRUE, if the string should be translated
 * @return Gender as string
 */
public String getReliabilityString(ReliabilityType type, boolean translate) {
	switch (type) {
	case NONE:
		return "---";
	case POOR:
		//T: Reliability
		return msg.contactFieldReliabilityPoorName;
	case MEDIUM:
		//T: Reliability
		return msg.contactFieldReliabilityMediumName;
	case GOOD:
		//T: Reliability
		return msg.contactFieldReliabilityGoodName;
	}
	return "";
}

///**
// * Get the reliability number by the string
// * 
// * @param s
// *          Reliability string
// * @return
// * 			The number
// */
//public int getReliabilityID(String s) {
//	// Test all strings
//	for (int i = 0;i < 4 ; i++) {
//		if (getReliabilityString(i,false).equalsIgnoreCase(s)) return i;
//		if (getReliabilityString(i,true).equalsIgnoreCase(s)) return i;
//	}
//	// Default = "---"
//	return 0;
//}

@Override
protected MDirtyable getMDirtyablePart() {
	return part;
}

@Override
protected String getEditorID() {
    return "Contact";
}
}

