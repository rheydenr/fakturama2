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


import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Text;

import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.Contact;

/**
 * The contact editor
 * 
 * @author Gerd Bartelt
 */

public class ContactEditor { // extends Editor {
	
	@Inject
	@Translation
	protected Messages _;

	// Editor's ID
	public static final String ID = "com.sebulli.fakturama.editors.contactEditor";

	// This UniDataSet represents the editor's input 
	private Contact contact;

	// SWT widgets of the editor
	private TabFolder tabFolder;
	private Text textNote;
	private Combo comboGender;
	private Text txtTitle;
	private Text txtFirstname;
	private Text txtName;
	private Text txtCompany;
	private Text txtStreet;
	private Text txtZip;
	private Text txtCity;
	private Text txtCountry;
	private DateTime dtBirthday;
	private Combo comboDeliveryGender;
	private Text txtDeliveryTitle;
	private Text txtDeliveryFirstname;
	private Text txtDeliveryName;
	private DateTime dtDeliveryBirthday;
	private Text txtDeliveryCompany;
	private Text txtDeliveryStreet;
	private Text txtDeliveryZip;
	private Text txtDeliveryCity;
	private Text txtDeliveryCountry;
	private Text txtAccountHolder;
	private Text txtAccount;
	private Text txtBankCode;
	private Text txtBankName;
	private Text txtIBAN;
	private Text txtBIC;
	private Text txtNr;
	private Combo comboPayment;
	private ComboViewer comboPaymentViewer;
	private Combo comboReliability;
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
	private Combo comboUseNetGross;

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
	private Composite parent;
	private MPart part;
	
	@Inject
	private ContactsDAO contactDAO;
	
	@Inject
	@Preference(nodePath = "/configuration/contactPreferences")
	protected IEclipsePreferences contactPreferences;

	private String objId;
//
//	/**
//	 * Constructor
//	 * 
//	 * Associate the table view with the editor
//	 */
//	@Inject
//	public ContactEditor(Composite parent) {
//		tableViewID = ViewContactTable.ID;
//		editorID = "contact";
//		this.parent = parent;
////		this.dirtyable = dirtyable;
//		this.part = (MPart) parent.getData("modelElement");
//		this.objId = (String) part.getContext().get("com.sebulli.fakturama.rcp.editor.objId");
//	}
//
//	/**
//	 * Saves the contents of this part
//	 * 
//	 * @param monitor
//	 *            Progress monitor
//	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
//	 */
//	@Persist
//	public void doSave(IProgressMonitor monitor) {
//
//		/*
//		 * the following parameters are not saved: 
//		 * - id (constant)
//		 * - date_added (constant)
//		 */
//
//		if (newContact) {
//
//			// Check, if the contact number is the next one
//			int result = setNextNr(txtNr.getText(), "nr", contactDAO.findAll());
//
//			// It's not the next free ID
//			if (result == ERROR_NOT_NEXT_ID) {
//				// Display an error message
//				MessageBox messageBox = new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);
//
//				//T: Title of the dialog that appears if the item/product number is not valid.
//				messageBox.setText(msg("Error in customer ID"));
//
//				//T: Text of the dialog that appears if the customer number is not valid.
//				messageBox.setMessage(msg("Customer ID is not the next free one:") + " " + txtNr.getText() + "\n" + 
//						//T: Text of the dialog that appears if the number is not valid.
//						msg("See Preferences/Number Range."));
//				messageBox.open();
//			}
//
//		}
//
//		// If the Check Box "Address equals delivery address" is set,
//		// all the address data is copied to the delivery addres.s
//		if (bDelAddrEquAddr.getSelection())
//			copyAddressToDeliveryAdress();
//
//		// Always set the editor's data set to "undeleted"
//		contact.setDeleted(false);
//
////		// Set the address data
////		contact.setGender(comboGender.getSelectionIndex());
////		contact.setTitle(txtTitle.getText());
////		contact.setFirstName(txtFirstname.getText());
////		contact.setName(txtName.getText());
////		contact.setCompany(DataUtils.removeCR(txtCompany.getText()));
////		contact.setStreet(txtStreet.getText());
////		contact.setZip(txtZip.getText());
////		contact.setCity(txtCity.getText());
////		contact.setCountry(txtCountry.getText());
////		contact.setBirthday(DataUtils.getDateTimeAsString(dtBirthday));
////
////		// Set the delivery address data
////		contact.setDeliveryGender(comboDeliveryGender.getSelectionIndex());
////		contact.setDeliveryTitle(txtDeliveryTitle.getText());
////		contact.setDeliveryFirstname(txtDeliveryFirstname.getText());
////		contact.setDeliveryName(txtDeliveryName.getText());
////		contact.setDeliveryCompany(DataUtils.removeCR(txtDeliveryCompany.getText()));
////		contact.setDeliveryStreet(txtDeliveryStreet.getText());
////		contact.setDeliveryZip(txtDeliveryZip.getText());
////		contact.setDeliveryCity(txtDeliveryCity.getText());
////		contact.setDeliveryCountry(txtDeliveryCountry.getText());
////		contact.setDeliveryBirthday(DataUtils.getDateTimeAsString(dtDeliveryBirthday));
////
////		// Set the bank data
////		contact.setAccountHolder(txtAccountHolder.getText());
////		contact.setAccount(txtAccount.getText());
////		contact.setBankCode(txtBankCode.getText());
////		contact.setBankName(txtBankName.getText());
//		contact.setIban(txtIBAN.getText());
//		contact.setBic(txtBIC.getText());
//
//		// Set the customer number
//		contact.setCustomerNumber(txtNr.getText());
//
//		// Set the payment ID
//		IStructuredSelection structuredSelection = (IStructuredSelection) comboPaymentViewer.getSelection();
//		if (!structuredSelection.isEmpty()) {
////			contact.setPayment(((Payments) structuredSelection.getFirstElement()).getId());
//		}
//
//		// Set the miscellaneous data
//		contact.setReliability(comboReliability.getSelectionIndex());
//		contact.setPhone(txtPhone.getText());
//		contact.setFax(txtFax.getText());
//		contact.setMobile(txtMobile.getText());
//		contact.setSupplierNumber(txtSupplierNr.getText());
//		contact.setEmail(txtEmail.getText());
//		contact.setWebsite(txtWebsite.getText());
////		contact.setVatnr(txtVatNr.getText());
////		contact.setDiscount(DataUtils.StringToDoubleDiscount(txtDiscount.getText()));
////		contact.setCategory(comboCategory.getText());
////		contact.setUseNetGross(comboUseNetGross.getSelectionIndex());
//
//		// Set the note
//		contact.setNote(DataUtils.removeCR(textNote.getText()));
//
//			try {
//		// If it is a new contact, add it to the contact list and
//		// to the data base
//		if (newContact) {
//				contact = contactDAO.save(contact);
//			newContact = false;
//		}
//		// If it's not new, update at least the data base
//		else {
//			contact = contactDAO.update(contact);
//		}
//			} catch (SQLException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//
////		// Sets the address
////		if (documentEditor != null) {
////			documentEditor.setAddress(contact);
////		}
//		
//		// Set the Editor's name to the first name and last name of the contact.
//		
////		String nameWithCompany = contact.getNameWithCompany(false);
////		if(nameWithCompany.contains("\r")) {
////			nameWithCompany = nameWithCompany.split("\\r")[0];
////		}
////		else if (nameWithCompany.contains("\n")) {
////			nameWithCompany = nameWithCompany.split("\\n")[0];
////		}
////		part.setLabel(nameWithCompany);
//
//		// Refresh the table view of all contacts
//		refreshView();
//		checkDirty();
//
//	}
//
//	/**
//	 * Initializes the editor. If an existing data set is opened, the local
//	 * variable "contact" is set to this data set. If the editor is opened to
//	 * create a new one, a new data set is created and the local variable
//	 * "contact" is set to this one.
//	 * 
//	 * @param input
//	 *            The editor's input
//	 * @param site
//	 *            The editor's site
//	 */
//	@PostConstruct
//	public void initialize(IWorkbenchHelpSystem help) {
//		
////		// Set the site and the input
////		setSite(site);
////		setInput(input);
//
//		// Set the editor's data set to the editor's input
//		try {
//			this.contact = contactDAO.findById(Integer.parseInt(objId));
//		} catch (NumberFormatException e) {
//			this.contact = null;
//		}
//
//		// Get the document that requests a new address
////		documentEditor = ((UniDataSetEditorInput) input).getDocumentEditor();
//		
//		// Test, if the editor is opened to create a new data set. This is,
//		// if there is no input set.
//		newContact = (contact == null);
//
//		// If new ..
//		if (newContact) {
//
//			// Create a new data set
////			contact = new Contact(((UniDataSetEditorInput) input).getCategory());
//			//T: Contact Editor Title of the editor if the data set is a new one.
//			part.setLabel(msg("New Contact"));
//
//			// Set the payment to the standard value
//			contact.setPayment(contactPreferences.getInt("standardpayment", 1));
//
//			// Get the next contact number
//	//		contact.setNr(getNextNr());
//
//		}
//		else {
//
//			// Set the Editor's name to the first name and last name of the contact.
//	//		part.setLabel(contact.getNameWithCompany(false));
//		}
//		
//		createPartControl();
//	}
//
//	/**
//	 * Returns whether the contents of this part have changed since the last
//	 * save operation
//	 * 
//	 * @see org.eclipse.ui.part.EditorPart#isDirty()
//	 */
//
//	public boolean isDirty() {
//
//		/*
//		 * the following parameters are not checked: 
//		 * - id (constant) 
//		 * - date_added (constant) 
//		 * - servicedate:
//		 */
//
//		if (contact.getDeleted()) { return true; }
//		if (newContact) { return true; }
//
//		return part.isDirty();
//	}
//
//	/**
//	 * Defines, if the delivery address is equal to the billing address
//	 * 
//	 * @param isEqual
//	 */
//	private void deliveryAddressIsEqual(boolean isEqual) {
//		deliveryGroup.setVisible(!isEqual);
//		if (isEqual)
//			copyAddressToDeliveryAdress();
//
//	}
//
//	/**
//	 * Copy all the address data to the delivery address
//	 */
//	private void copyAddressToDeliveryAdress() {
//		comboDeliveryGender.select(comboGender.getSelectionIndex());
//		txtDeliveryTitle.setText(txtTitle.getText());
//		txtDeliveryFirstname.setText(txtFirstname.getText());
//		txtDeliveryName.setText(txtName.getText());
//		txtDeliveryCompany.setText(txtCompany.getText());
//		txtDeliveryStreet.setText(txtStreet.getText());
//		txtDeliveryZip.setText(txtZip.getText());
//		txtDeliveryCity.setText(txtCity.getText());
//		txtDeliveryCountry.setText(txtCountry.getText());
//	}
//
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
//
//	/**
//	 * Creates the SWT controls for this workbench part
//	 * 
//	 * @param the
//	 *            parent control
//	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
//	 */
//	public void createPartControl() {
//
//		// Some of this editos's control elements can be hidden.
//		// Get the these settings from the preference store
//		useDelivery = contactPreferences.getBoolean("CONTACT_USE_DELIVERY", false);
//		useBank = contactPreferences.getBoolean("CONTACT_USE_BANK", false );
//		useMisc = contactPreferences.getBoolean("CONTACT_USE_MISC", false);
//		useNote = contactPreferences.getBoolean("CONTACT_USE_NOTE", false);
//		useGender = contactPreferences.getBoolean("CONTACT_USE_GENDER", false);
//		useTitle = contactPreferences.getBoolean("CONTACT_USE_TITLE", false);
//		useLastNameFirst = (contactPreferences.getInt("CONTACT_NAME_FORMAT", 1) == 1);
//		useCompany = contactPreferences.getBoolean("CONTACT_USE_COMPANY", false);
//		useCountry = contactPreferences.getBoolean("CONTACT_USE_COUNTRY", false);
//
//		// Create the parent Composite
//		
//		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(parent);
//
//		// Create an invisible container for all hidden components	
//		Composite invisible = new Composite(parent, SWT.NONE);
//		invisible.setVisible(false);
//		GridDataFactory.fillDefaults().hint(0, 0).applyTo(invisible);
//
//		// Add context help reference 
////		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, ContextHelpConstants.CONTACT_EDITOR);
//
//		// Create the address tab
//		Composite tabAddress;
//		if (useDelivery || useBank || useMisc || useNote) {
//			tabFolder = new TabFolder(parent, SWT.NONE);
//			GridDataFactory.fillDefaults().grab(true, true).applyTo(tabFolder);
//
//			TabItem item1 = new TabItem(tabFolder, SWT.NONE);
//			//T: Label in the contact editor
//			item1.setText(msg("Address"));
//			tabAddress = new Composite(tabFolder, SWT.NONE);
//			item1.setControl(tabAddress);
//		}
//		else {
//			tabAddress = new Composite(parent, SWT.NONE);
//		}
//		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(tabAddress);
//
//		// Create the bank tab
//		Composite tabBank;
//		if (useBank) {
//			TabItem item3 = new TabItem(tabFolder, SWT.NONE);
//			//T: Label in the contact editor
//			item3.setText(msg("Bank Account"));
//			tabBank = new Composite(tabFolder, SWT.NONE);
//			item3.setControl(tabBank);
//		}
//		else {
//			tabBank = new Composite(invisible, SWT.NONE);
//		}
//		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(tabBank);
//
//		// Create the miscellaneous tab
//		Composite tabMisc;
//		if (useMisc) {
//			TabItem item4 = new TabItem(tabFolder, SWT.NONE);
//			//T: Label in the contact editor
//			item4.setText(msg("Miscellaneous"));
//			tabMisc = new Composite(tabFolder, SWT.NONE);
//			item4.setControl(tabMisc);
//		}
//		else {
//			tabMisc = new Composite(invisible, SWT.NONE);
//		}
//		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(tabMisc);
//
//		// Create to note tab
//		TabItem item5 = null;
//		Composite tabNote;
//		if (useNote) {
//			item5 = new TabItem(tabFolder, SWT.NONE);
//			//T: Label in the contact editor
//			item5.setText(msg("Notice"));
//			tabNote = new Composite(tabFolder, SWT.NONE);
//			item5.setControl(tabNote);
//		}
//		else {
//			tabNote = new Composite(invisible, SWT.NONE);
//		}
//		tabNote.setLayout(new FillLayout());
//
//		// Composite for the customer's number
//		Composite customerNrComposite = new Composite(tabAddress, SWT.NONE);
//		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(customerNrComposite);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(customerNrComposite);
//
//		// Customer's number
//		Label labelNr = new Label(customerNrComposite, SWT.NONE);
//		//T: Label in the contact editor
//		labelNr.setText(msg("Customer ID"));
//		//T: Tool Tip Text
//		labelNr.setToolTipText(msg("Next contact ID and the format can be set unter preferences/number range"));
//
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelNr);
//		txtNr = new Text(customerNrComposite, SWT.BORDER);
//		txtNr.setText(contact.getCustomerNumber());
//		txtNr.setToolTipText(labelNr.getToolTipText());
//		superviceControl(txtNr, 32);
//		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(txtNr);
//
//		// Check button: delivery address equals address
//		bDelAddrEquAddr = new Button(tabAddress, SWT.CHECK);
//		//T: Label in the contact editor
//		bDelAddrEquAddr.setText(msg("Delivery Address equals Invoice Address"));
//		GridDataFactory.swtDefaults().applyTo(bDelAddrEquAddr);
//		bDelAddrEquAddr.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				deliveryAddressIsEqual(bDelAddrEquAddr.getSelection());
//				checkDirty();
//			}
//		});
//
//		// Group: address
//		Group addressGroup = new Group(tabAddress, SWT.NONE);
//		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(addressGroup);
//		GridDataFactory.fillDefaults().grab(true, true).applyTo(addressGroup);
//		//T: Label in the contact editor
//		addressGroup.setText(msg("Address"));
//
//		// Controls in the group "address"
//
//		// The title and gender's label
//		Label labelTitle = new Label((useGender || useTitle) ? addressGroup : invisible, SWT.NONE);
//		if (useGender)
//			labelTitle.setText(msg("Gender"));
//		if (useGender && useTitle)
//			labelTitle.setText(labelTitle.getText() + ", ");
//		if (useTitle)
//			//T: "Title" ( part of an address)
//			labelTitle.setText(labelTitle.getText() + msg("Title","ADDRESS"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelTitle);
//
//		// Gender
//		comboGender = new Combo(useGender ? addressGroup : invisible, SWT.BORDER);
//		for (int i = 0; i < 4; i++)
//			comboGender.add(getGenderString(i), i);
//		comboGender.select(contact.getGender());
//		GridDataFactory.fillDefaults().grab(false, false).hint(100, SWT.DEFAULT).span(useTitle ? 1 : 2, 1).applyTo(comboGender);
//		superviceControl(comboGender);
//
//		// Title
//		txtTitle = new Text(useTitle ? addressGroup : invisible, SWT.BORDER);
//		txtTitle.setText(contact.getTitle());
//		GridDataFactory.fillDefaults().grab(true, false).span(useGender ? 1 : 2, 1).applyTo(txtTitle);
//		superviceControl(txtTitle, 32);
//
//		// First and last name		
//		Label labelName = new Label(addressGroup, SWT.NONE);
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);
//		if (useLastNameFirst) {
//			//T: Format of the name in an address
//			labelName.setText(msg("Last Name, First Name"));
//			txtName = new Text(addressGroup, SWT.BORDER);
//			GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(txtName);
//			txtFirstname = new Text(addressGroup, SWT.BORDER);
//			GridDataFactory.fillDefaults().grab(true, false).applyTo(txtFirstname);
//		}
//		else {
//			//T: Format of the name in an address
//			labelName.setText(msg("First Name Last Name"));
//			txtFirstname = new Text(addressGroup, SWT.BORDER);
//			GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(txtFirstname);
//			txtName = new Text(addressGroup, SWT.BORDER);
//			GridDataFactory.fillDefaults().grab(true, false).applyTo(txtName);
//		}
//		txtFirstname.setText(contact.getFirstName());
//		txtName.setText(contact.getName());
//		superviceControl(txtFirstname, 64);
//		superviceControl(txtName, 64);
//
//		// Company
//		Label labelCompany = new Label(useCompany ? addressGroup : invisible, SWT.NONE);
//		//T: Label in the contact editor
//		labelCompany.setText(msg("Company"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCompany);
//		txtCompany = new Text(useCompany ? addressGroup : invisible, SWT.BORDER | SWT.MULTI);
//		txtCompany.setText(DataUtils.makeOSLineFeeds(contact.getCompany()));
//		superviceControl(txtCompany, 64);
//		GridDataFactory.fillDefaults().hint(210, 40).grab(true, false).span(2, 1).applyTo(txtCompany);
//
////		// Street
////		Label labelStreet = new Label(addressGroup, SWT.NONE);
////		//T: Label in the contact editor
////		labelStreet.setText(msg("Street"));
////		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelStreet);
////		txtStreet = new Text(addressGroup, SWT.BORDER);
////		txtStreet.setText(contact.getStreet());
////		superviceControl(txtStreet, 64);
////		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(txtStreet);
////		setTabOrder(txtCompany, txtStreet);
////
////		// City
////		Label labelCity = new Label(addressGroup, SWT.NONE);
////		//T: Label in the contact editor
////		labelCity.setText(msg("ZIP, City"));
////		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCity);
////		txtZip = new Text(addressGroup, SWT.BORDER);
////		txtZip.setText(contact.getZip());
////		superviceControl(txtZip, 16);
////		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(txtZip);
////		txtCity = new Text(addressGroup, SWT.BORDER);
////		txtCity.setText(contact.getCity());
////		superviceControl(txtCity, 32);
////		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtCity);
////
////		// Country
////		Label labelCountry = new Label(useCountry ? addressGroup : invisible, SWT.NONE);
////		//T: Label in the contact editor
////		labelCountry.setText(msg("Country"));
////		//T: Tool Tip Text
////		labelCountry.setToolTipText(msg("Set also your home county. Under preferences/contacts you can set those country names that are not displayed on the address label"));
////		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCountry);
////		txtCountry = new Text(useCountry ? addressGroup : invisible, SWT.BORDER);
////		txtCountry.setText(contact.getCountry());
////		txtCountry.setToolTipText(labelCountry.getToolTipText());
////		superviceControl(txtCountry, 32);
////		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(txtCountry);
//		
//		// Birthday
//		Label labelBirthday = new Label(addressGroup, SWT.NONE);
//		//T: Label in the contact editor
//		labelBirthday.setText(msg("Birthday"));
//		//T: Tool Tip Text
//		labelBirthday.setToolTipText(msg("The contact's birthday"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelBirthday);		
//		dtBirthday = new DateTime(addressGroup, SWT.DROP_DOWN);
//		dtBirthday.setToolTipText(labelBirthday.getToolTipText());
//		GridDataFactory.swtDefaults().applyTo(dtBirthday);
//		superviceControl(dtBirthday);
//		
////		// Set the dtBirthday widget to the contact's birthday date
////		GregorianCalendar calendar = new GregorianCalendar();
////		if(!"".equals(contact.getBirthday())) {
////			calendar = DataUtils.getCalendarFromDateString(contact.getBirthday());
////		}
////		dtBirthday.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
//
//		// Group: delivery address
//		deliveryGroup = new Group(tabAddress, SWT.NONE);
//		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(deliveryGroup);
//		GridDataFactory.fillDefaults().grab(true, true).applyTo(deliveryGroup);
//		//T: Label in the contact editor
//		deliveryGroup.setText(msg("Delivery Address"));
//
//		// Controls in the group "Delivery"
//
//		// Delivery gender and titel's label
//		Label labelDeliveryTitle = new Label((useGender || useTitle) ? deliveryGroup : invisible, SWT.NONE);
//		if (useGender)
//			labelDeliveryTitle.setText(msg("Gender"));
//		if (useGender && useTitle)
//			labelDeliveryTitle.setText(labelDeliveryTitle.getText() + ", ");
//		if (useTitle)
//			//T: "Title" (part of an address)
//			labelDeliveryTitle.setText(labelDeliveryTitle.getText() + msg("Title", "ADDRESS"));
//
////		// Delivery Gender
////		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDeliveryTitle);
////		comboDeliveryGender = new Combo(useGender ? deliveryGroup : invisible, SWT.BORDER);
////		for (int i = 0; i < 4; i++)
////			comboDeliveryGender.add(getGenderString(i), i);
////		comboDeliveryGender.select(contact.getDeliveryGender());
////		GridDataFactory.fillDefaults().grab(false, false).hint(100, SWT.DEFAULT).span(useTitle ? 1 : 2, 1).applyTo(comboDeliveryGender);
////		superviceControl(comboDeliveryGender);
////
////		// Delivery Title
////		txtDeliveryTitle = new Text(useTitle ? deliveryGroup : invisible, SWT.BORDER);
////		txtDeliveryTitle.setText(contact.getDeliveryTitle());
////		superviceControl(txtDeliveryTitle, 32);
////		GridDataFactory.fillDefaults().grab(true, false).span(useGender ? 1 : 2, 1).applyTo(txtDeliveryTitle);
////
////		// Delivery first and last name
////		Label labelDeliveryName = new Label(deliveryGroup, SWT.NONE);
////		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDeliveryName);
////		if (useLastNameFirst) {
////			//T: Format of the name in an address
////			labelDeliveryName.setText(msg("Last name, First Name"));
////			txtDeliveryName = new Text(deliveryGroup, SWT.BORDER);
////			GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(labelDeliveryName);
////			txtDeliveryFirstname = new Text(deliveryGroup, SWT.BORDER);
////			GridDataFactory.fillDefaults().grab(true, false).applyTo(txtDeliveryFirstname);
////		}
////		else {
////			//T: Format of the name in an address
////			labelDeliveryName.setText(msg("First Name Last name"));
////			txtDeliveryFirstname = new Text(deliveryGroup, SWT.BORDER);
////			GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(txtDeliveryFirstname);
////			txtDeliveryName = new Text(deliveryGroup, SWT.BORDER);
////			GridDataFactory.fillDefaults().grab(true, false).applyTo(txtDeliveryName);
////
////		}
////		txtDeliveryFirstname.setText(contact.getDeliveryFirstname());
////		txtDeliveryName.setText(contact.getDeliveryName());
////		superviceControl(txtDeliveryFirstname, 64);
////		superviceControl(txtDeliveryName, 64);
////
////		// Delivery company
////		Label labelDeliveryCompany = new Label(useCompany ? deliveryGroup : invisible, SWT.NONE);
////		//T: Label in the contact editor
////		labelDeliveryCompany.setText(msg("Company"));
////		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDeliveryCompany);
////		txtDeliveryCompany = new Text(useCompany ? deliveryGroup : invisible, SWT.BORDER | SWT.MULTI);
////		txtDeliveryCompany.setText(DataUtils.makeOSLineFeeds(contact.getDeliveryCompany()));
////		superviceControl(txtDeliveryCompany, 64);
////		GridDataFactory.fillDefaults().hint(210, 40).grab(true, false).span(2, 1).applyTo(txtDeliveryCompany);
////
////		// Delivery street
////		Label labelDeliveryStreet = new Label(deliveryGroup, SWT.NONE);
////		//T: Label in the contact editor
////		labelDeliveryStreet.setText(msg("Street"));
////		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDeliveryStreet);
////		txtDeliveryStreet = new Text(deliveryGroup, SWT.BORDER);
////		txtDeliveryStreet.setText(contact.getDeliveryStreet());
////		superviceControl(txtDeliveryStreet, 64);
////		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(txtDeliveryStreet);
////		setTabOrder(txtDeliveryCompany, txtDeliveryStreet);
////
////		// Delivery city
////		Label labelDeliveryCity = new Label(deliveryGroup, SWT.NONE);
////		//T: Label in the contact editor
////		labelDeliveryCity.setText(msg("ZIP, City"));
////		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDeliveryCity);
////		txtDeliveryZip = new Text(deliveryGroup, SWT.BORDER);
////		txtDeliveryZip.setText(contact.getDeliveryZip());
////		superviceControl(txtDeliveryZip, 16);
////		GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(txtDeliveryZip);
////		txtDeliveryCity = new Text(deliveryGroup, SWT.BORDER);
////		txtDeliveryCity.setText(contact.getDeliveryCity());
////		superviceControl(txtDeliveryCity, 32);
////		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtDeliveryCity);
////
////		// Delivery country
////		Label labelDeliveryCountry = new Label(useCountry ? deliveryGroup : invisible, SWT.NONE);
////		//T: Label in the contact editor
////		labelDeliveryCountry.setText(msg("Country"));
////		labelDeliveryCountry.setToolTipText(labelCountry.getToolTipText());
////		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDeliveryCountry);
////		txtDeliveryCountry = new Text(useCountry ? deliveryGroup : invisible, SWT.BORDER);
////		txtDeliveryCountry.setText(contact.getDeliveryCountry());
////		txtDeliveryCountry.setToolTipText(labelCountry.getToolTipText());
////		superviceControl(txtDeliveryZip, 32);
////		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(txtDeliveryCountry);
////		
////		// Deliverer's Birthday
////		Label labelDelivererBirthday = new Label(deliveryGroup, SWT.NONE);
////		//T: Label in the deliverer editor
////		labelDelivererBirthday.setText(msg("Birthday"));
////		//T: Tool Tip Text
////		labelDelivererBirthday.setToolTipText(msg("The deliverer's birthday"));
////		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDelivererBirthday);		
////		dtDeliveryBirthday = new DateTime(deliveryGroup, SWT.DROP_DOWN);
////		dtDeliveryBirthday.setToolTipText(labelDelivererBirthday.getToolTipText());
////		GridDataFactory.swtDefaults().applyTo(dtDeliveryBirthday);
////		superviceControl(dtDeliveryBirthday);
////		
////		// Set the dtDeliveryBirthday widget to the deliverer's birthday date
////		if(!"".equals(contact.getDeliveryBirthday())) {
////			calendar = DataUtils.getCalendarFromDateString(contact.getDeliveryBirthday());
////		} else {
////			calendar = new GregorianCalendar();
////		}
////		dtDeliveryBirthday.setDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
////
//		// Controls in the tab "Bank"
//
//		// Account holder
//		Label labelAccountHolder = new Label(tabBank, SWT.NONE);
//		//T: Label in the contact editor
//		labelAccountHolder.setText(msg("Account Holder"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelAccountHolder);
//		txtAccountHolder = new Text(tabBank, SWT.BORDER);
//		txtAccountHolder.setText(contact.getAccountHolder());
//		superviceControl(txtAccountHolder, 64);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtAccountHolder);
//
//		// Account number
//		Label labelAccount = new Label(tabBank, SWT.NONE);
//		//T: Label in the contact editor
//		labelAccount.setText(msg("Account Number"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelAccount);
//		txtAccount = new Text(tabBank, SWT.BORDER);
//		txtAccount.setText(contact.getAccount());
//		superviceControl(txtAccount, 32);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtAccount);
//
////		// Bank code
////		Label labelBankCode = new Label(tabBank, SWT.NONE);
////		//T: Label in the contact editor
////		labelBankCode.setText(msg("Bank Code"));
////		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelBankCode);
////		txtBankCode = new Text(tabBank, SWT.BORDER);
////		txtBankCode.setText(contact.getBankCode());
////		superviceControl(txtBankCode, 32);
////		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtBankCode);
//
//		// Name of the bank
//		Label labelBankName = new Label(tabBank, SWT.NONE);
//		//T: Label in the contact editor
//		labelBankName.setText(msg("Name of the Bank"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelBankName);
//		txtBankName = new Text(tabBank, SWT.BORDER);
//		txtBankName.setText(contact.getBankName());
//		superviceControl(txtBankName, 64);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtBankName);
//
//		// IBAN Bank code
//		Label labelIBAN = new Label(tabBank, SWT.NONE);
//		//T: Bank code
//		labelIBAN.setText(msg("IBAN"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelIBAN);
//		txtIBAN = new Text(tabBank, SWT.BORDER);
//		txtIBAN.setText(contact.getIban());
//		superviceControl(txtIBAN, 32);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtIBAN);
//
//		// BIC
//		Label labelBIC = new Label(tabBank, SWT.NONE);
//		//T: Bank code
//		labelBIC.setText(msg("BIC"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelBIC);
//		txtBIC = new Text(tabBank, SWT.BORDER);
//		txtBIC.setText(contact.getBic());
//		superviceControl(txtBIC, 32);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtBIC);
//
//		// Controls in tab "Misc"
//
//		// Category 
//		Label labelCategory = new Label(tabMisc, SWT.NONE);
//		//T: Label in the contact editor
//		labelCategory.setText(msg("Category"));
//		//T: Tool Tip Text
//		labelCategory.setToolTipText(msg("Choose a category like 'Customer', 'Customer Web Shop' or 'Supplier'"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCategory);
//
////		comboCategory = new Combo(tabMisc, SWT.BORDER);
////		comboCategory.setText(contact.getCategory());
////		comboCategory.setToolTipText(labelCategory.getToolTipText());
////		superviceControl(comboCategory);
////		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboCategory);
//
//		// Collect all category strings
//		TreeSet<String> categories = new TreeSet<String>();
//		categories.addAll(contactDAO.getCategoryStrings());
//
//		// Add all category strings to the combo
//		for (Object category : categories) {
//			comboCategory.add(category.toString());
//		}
//
//		// Suppliernumber
//		Label labelSupplier = new Label(tabMisc, SWT.NONE);
//		//T: Label in the contact editor
//		labelSupplier.setText(msg("Supplier Number"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelSupplier);
//		txtSupplierNr = new Text(tabMisc, SWT.BORDER);
//		txtSupplierNr.setText(StringUtils.defaultString(contact.getSupplierNumber()));
//		superviceControl(txtSupplierNr, 64);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtSupplierNr);
//		
//		
//		// EMail
//		Label labelEmail = new Label(tabMisc, SWT.NONE);
//		//T: Label in the contact editor
//		labelEmail.setText(msg("E-Mail"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelEmail);
//		txtEmail = new Text(tabMisc, SWT.BORDER);
//		txtEmail.setText(StringUtils.defaultString(contact.getEmail()));
//		superviceControl(txtEmail, 64);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtEmail);
//
//		// Telephone
//		Label labelTel = new Label(tabMisc, SWT.NONE);
//		//T: Label in the contact editor
//		labelTel.setText(msg("Telephone"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelTel);
//		txtPhone = new Text(tabMisc, SWT.BORDER);
//		txtPhone.setText(StringUtils.defaultString(contact.getPhone()));
//		superviceControl(txtPhone, 32);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtPhone);
//
//		// Telefax
//		Label labelFax = new Label(tabMisc, SWT.NONE);
//		//T: Label in the contact editor
//		labelFax.setText(msg("Telefax"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelFax);
//		txtFax = new Text(tabMisc, SWT.BORDER);
//		txtFax.setText(contact.getFax());
//		superviceControl(txtFax, 32);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtFax);
//
//		// Mobile
//		Label labelMobile = new Label(tabMisc, SWT.NONE);
//		//T: Label in the contact editor
//		labelMobile.setText(msg("Mobile"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelMobile);
//		txtMobile = new Text(tabMisc, SWT.BORDER);
//		txtMobile.setText(contact.getMobile());
//		superviceControl(txtMobile, 32);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtMobile);
//
//		// Web Site
//		Label labelWebsite = new Label(tabMisc, SWT.NONE);
//		//T: Label in the contact editor
//		labelWebsite.setText(msg("Web Site"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelWebsite);
//		txtWebsite = new Text(tabMisc, SWT.BORDER);
//		txtWebsite.setText(contact.getWebsite());
//		superviceControl(txtWebsite, 64);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtWebsite);
//
//		// Payment
//		Label labelPayment = new Label(tabMisc, SWT.NONE);
//		//T: Label in the contact editor
//		labelPayment.setText(msg("Payment"));
//		//T: Tool Tip Text
//		labelPayment.setToolTipText(msg("This payment method is used when creating a new document"));
//
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelPayment);
//		comboPayment = new Combo(tabMisc, SWT.BORDER);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboPayment);
//		comboPayment.setToolTipText(labelPayment.getToolTipText());
//		comboPaymentViewer = new ComboViewer(comboPayment);
////		comboPaymentViewer.setContentProvider(new UniDataSetContentProvider());
////		comboPaymentViewer.setLabelProvider(new UniDataSetLabelProvider());
////		comboPaymentViewer.setInput(Data.INSTANCE.getPayments().getDatasets());
//
//		int paymentId = contact.getPayment();
//		try {
////			if (paymentId >= 0)
////				comboPaymentViewer.setSelection(new StructuredSelection(Data.INSTANCE.getPayments().getDatasetById(paymentId)), true);
////			else
//				comboPayment.setText("");
//		}
//		catch (IndexOutOfBoundsException e) {
//			comboPayment.setText("invalid");
//		}
//		superviceControl(comboPayment);
//
//		// Reliability
//		Label labelReliability = new Label(tabMisc, SWT.NONE);
//		//T: Label in the contact editor
//		labelReliability.setText(msg("Reliability"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelReliability);
//		comboReliability = new Combo(tabMisc, SWT.BORDER);
//
////		comboReliability.add(DataSetContact.getReliabilityString(0), 0);
////		comboReliability.add(DataSetContact.getReliabilityString(1), 1);
////		comboReliability.add(DataSetContact.getReliabilityString(2), 2);
////		comboReliability.add(DataSetContact.getReliabilityString(3), 3);
//
//		comboReliability.select(contact.getReliability());
//		superviceControl(comboReliability);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboReliability);
//
//		
//		// VAT number
//		Label labelVatNr = new Label(tabMisc, SWT.NONE);
//		//T: Label in the contact editor
//		labelVatNr.setText(msg("VAT Number"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelVatNr);
//		txtVatNr = new Text(tabMisc, SWT.BORDER);
//		txtVatNr.setText(contact.getVatNumber());
//		superviceControl(txtVatNr, 32);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtVatNr);
//
//		// Customer's discount
//		Label labelDiscount = new Label(tabMisc, SWT.NONE);
//		//T: Customer's discount
//		labelDiscount.setText(msg("Discount","CUSTOMER"));
//		//T: Tool Tip Text
//		labelDiscount.setToolTipText(msg("This customer's discount is used when creating a new document"));
//
////		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDiscount);
////		txtDiscount = new Text(tabMisc, SWT.BORDER);
////		txtDiscount.setText(DataUtils.DoubleToFormatedPercent(contact.getDiscount()));
////		GridDataFactory.fillDefaults().grab(true, false).applyTo(txtDiscount);
////		txtDiscount.setToolTipText(labelDiscount.getToolTipText());
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
//
//		// Use net or gross
//		Label labelNetGross = new Label(tabMisc, SWT.NONE);
//		//T: Label in the contact editor
//		labelNetGross.setText(msg("Net or Gross"));
//		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelNetGross);
//		comboUseNetGross = new Combo(tabMisc, SWT.BORDER);
//
//		comboUseNetGross.add("---");
//		//T: Entry in a combo box of the the contact editor. Use Net or Gross 
//		comboUseNetGross.add(msg("Net"));
//		//T: Entry in a combo box of the the contact editor. Use Net or Gross 
//		comboUseNetGross.add(msg("Gross"));
////
////		// If the value is -1, use 0 instead
////		if (contact.getUseNetGross()<0)
////			contact.setUseNetGross(0); 
////		comboUseNetGross.select(contact.getUseNetGross());
//		
//		superviceControl(comboUseNetGross);
//		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboUseNetGross);
//		
//
//		
//		
//		// Controls in tab "Note"
//
//		// The note
//		String note = DataUtils.makeOSLineFeeds(contact.getNote());
//		textNote = new Text(tabNote, SWT.BORDER | SWT.MULTI);
//		textNote.setText(note);
//		superviceControl(textNote, 10000);
//
//		// If the note is not empty, display it,
//		// when opening the editor.
//		if (useNote && !note.isEmpty())
//			tabFolder.setSelection(item5);
//
//		// Test, if the address and the delivery address
//		// are equal. If they are, set the checkbox and
//		// hide the delivery address
//		Boolean isEqual = isAddressEqualToDeliveryAdress();
//		bDelAddrEquAddr.setSelection(isEqual);
//		deliveryGroup.setVisible(!isEqual);
//	}
//
//	/**
//	 * Set the focus to the parent composite.
//	 * 
//	 * @see com.sebulli.fakturama.editors.Editor#setFocus()
//	 */
//	@Focus
//	public void setFocus() {
//		if(parent != null) 
//			parent.setFocus();
//	}
//
//
//	/**
//	 * Test, if there is a document with the same number
//	 * 
//	 * @return TRUE, if one with the same number is found
//	 */
//	public boolean thereIsOneWithSameNumber() {
//
////		// Cancel, if there is already a document with the same ID
////		if (Data.INSTANCE.getDocuments().isExistingDataSet(contact, "nr", txtNr.getText())) {
////			// Display an error message
////			MessageBox messageBox = new MessageBox(parent.getShell(), SWT.ICON_ERROR | SWT.OK);
////
////			//T: Title of the dialog that appears if the item/product number is not valid.
////			messageBox.setText(msg("Error in customer ID"));
////
////			//T: Text of the dialog that appears if the customer number is not valid.
////			messageBox.setMessage(msg("There is already a customer with the number:") + " " + txtNr.getText());
////			messageBox.open();
////
////			return true;
////		}
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
//		// Save is allowed, if there is no product with the same number
//		return !thereIsOneWithSameNumber();
//	}
//	
//	/**
//	 * Get the gender String by the gender number
//	 * 
//	 * @param i
//	 *            Gender number
//	 * @return Gender as string
//	 */
//	public static String getGenderString(int i) {
//		return getGenderString(i, true);
//	}
//
//	/**
//	 * Get the gender String by the gender number
//	 * 
//	 * @param i
//	 *            Gender number
//	 * @param translate
//	 *            TRUE, if the string should be translated
//	 * @return Gender as string
//	 */
//	public static String getGenderString(int i, boolean translate) {
//		switch (i) {
//		case 0:
//			return "---";
//		case 1:
//			//T: Gender
//			return msg("Mr", translate);
//		case 2:
//			//T: Gender
//			return msg("Ms", translate);
//		case 3:
//			return msg("Company", translate);
//		}
//		return "";
//	}
//
//	/**
//	 * Get the gender number by the string
//	 * 
//	 * @param s
//	 *          Gender string
//	 * @return
//	 * 			The number
//	 */
//	public static int getGenderID(String s) {
//		// Test all strings
//		for (int i = 0;i < 4 ; i++) {
//			if (getGenderString(i,false).equalsIgnoreCase(s)) return i;
//			if (getGenderString(i,true).equalsIgnoreCase(s)) return i;
//		}
//		// Default = "---"
//		return 0;
//	}
//
///**
// * Get the reliability String by the number
// * 
// * @param i
// *            Gender number
// * @return Gender as string
// */
//public static String getReliabilityString(int i) {
//	return getReliabilityString(i, true);
//}
//
///**
// * Get the reliability String by the number
// * 
// * @param i
// *            Gender number
// * @param translate
// *            TRUE, if the string should be translated
// * @return Gender as string
// */
//public static String getReliabilityString(int i, boolean translate) {
//	switch (i) {
//	case 0:
//		return "---";
//	case 1:
//		//T: Reliability
//		return msg("poor", "RELIABILITY", translate);
//	case 2:
//		//T: Reliability
//		return msg("medium", "RELIABILITY", translate);
//	case 3:
//		//T: Reliability
//		return msg("good", "RELIABILITY", translate);
//	}
//	return "";
//}
//
///**
// * Get the reliability number by the string
// * 
// * @param s
// *          Reliability string
// * @return
// * 			The number
// */
//public static int getReliabilityID(String s) {
//	// Test all strings
//	for (int i = 0;i < 4 ; i++) {
//		if (getReliabilityString(i,false).equalsIgnoreCase(s)) return i;
//		if (getReliabilityString(i,true).equalsIgnoreCase(s)) return i;
//	}
//	// Default = "---"
//	return 0;
//}
//
//@Override
//protected MDirtyable getMDirtyablePart() {
//	return part;
//}
//
}

