package com.sebulli.fakturama.parts;
//TODO GS/ activate missing fields (name-/addressAddon) when available in DB/DAO
//TODO GS/i18n - check all -

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Util;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.parts.widget.contentprovider.HashMapContentProvider;
import com.sebulli.fakturama.parts.widget.contentprovider.StringHashMapContentProvider;
import com.sebulli.fakturama.parts.widget.labelprovider.NumberLabelProvider;
import com.sebulli.fakturama.parts.widget.labelprovider.StringComboBoxLabelProvider;
import com.sebulli.fakturama.util.ContactUtil;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.layout.RowData;

public class DocumentReceiverEditorDialog extends TitleAreaDialog {

	@Inject
	private ILocaleService localeUtil;
	@Inject
	protected IPreferenceStore defaultValuePrefs;
	@Inject
	protected IEclipseContext context;
	@Inject
	@Translation
	protected Messages msg;
	@Inject
	private ContactUtil contactUtil;

	private DocumentReceiver theReceiver;

	private Text txtCompany;
	private Map<Integer, String> salutationMap;
	private ComboViewer comboGender;
	private Text txtTitle;
	private Text txtFirstname;
	private Text txtName;
	private Text txtCustomerNumber;
	private Text txtManualAddress;
	private Button chkAddressGenerate;
	private Text txtNameAddon;
	private Text txtStreet;
	private Text txtAddressAddon;
	private Text txtCityAddon;
	private Text txtZip;
	private Text txtCity;
	private ComboViewer comboCountrycode;
	private Text txtConsultant;
	private Text txtEMail;
	private Text txtPhone;
	private Text txtFax;
	private Text txtMobile;
	private Text txtSupplierNumber;
	private Text txtAlias;
	private Text txtVatNumber;
	private Text txtGLN;
	private Text txtMandateReference;
	private Text txtDescription;

	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public DocumentReceiverEditorDialog(Shell parentShell, DocumentReceiver receiver) {
		super(parentShell);
		setHelpAvailable(false);
		setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
		theReceiver = receiver;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Empfängerdaten bearbeiten");
//        setMessage("Änderungen wirken sich nur auf das aktuelle Dokument aus.", IMessageProvider.INFORMATION);
		setMessage("Änderungen wirken sich nur auf das aktuelle Dokument aus.");
	}

	private String oldValue = null;
	private DocumentReceiver tempReceiver = new DocumentReceiver();
	private void generateAddress() {
		if (chkAddressGenerate.getSelection()) {
			tempReceiver.setCompany(Util.defaultIfEmpty(txtCompany.getText(), null));
			tempReceiver.setGender((Integer) comboGender.getStructuredSelection().getFirstElement());
			tempReceiver.setTitle(Util.defaultIfEmpty(txtTitle.getText(), null));
			tempReceiver.setFirstName(Util.defaultIfEmpty(txtFirstname.getText(), null));
			tempReceiver.setName(Util.defaultIfEmpty(txtName.getText(), null));
//			tempReceiver.setNameAddon(Util.defaultIfEmpty(txtNameAddon.getText(), null));
			tempReceiver.setStreet(Util.defaultIfEmpty(txtStreet.getText(), null));
//			tempReceiver.setAddressAddon(Util.defaultIfEmpty(txtAddressAddon.getText(), null));
			tempReceiver.setCityAddon(Util.defaultIfEmpty(txtCityAddon.getText(), null));
			tempReceiver.setCity(Util.defaultIfEmpty(txtCity.getText(), null));
			tempReceiver.setZip(Util.defaultIfEmpty(txtZip.getText(), null));
			tempReceiver.setCountryCode((String) comboCountrycode.getStructuredSelection().getFirstElement());
			tempReceiver.setConsultant(Util.defaultIfEmpty(txtConsultant.getText(), null));
			txtManualAddress.setText(contactUtil.getAddressAsString(tempReceiver));
		}
	}
	private FocusListener updateAddressListener = new FocusListener() {
		@Override
		public void focusLost(FocusEvent e) {
			if (!((Text) e.widget).getText().equals(oldValue)) {
				generateAddress();
			}
		}

		@Override
		public void focusGained(FocusEvent e) {
			oldValue = ((Text) e.widget).getText();
		}
	};
	private Group grpAddress;
	private Composite pnlGenderTitle;
	private Composite pnlFirstnameName;
	private Composite pnlZipCity;
	private Group grpDescription;

	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
// GS/ ATM we don't care abaout these settings
//		boolean useSalutation = defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_GENDER);
//		boolean useTitle = defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_TITLE);
//		boolean useLastNameFirst = (defaultValuePrefs.getInt(Constants.PREFERENCES_CONTACT_NAME_FORMAT) == 1);
//		boolean useCompany = defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_COMPANY);
//		boolean useCountry = defaultValuePrefs.getBoolean(Constants.PREFERENCES_CONTACT_USE_COUNTRY);

		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		GridLayout gl_container = new GridLayout(4, false);
		gl_container.verticalSpacing = 2;
		container.setLayout(gl_container);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label lblCustomerNumber = new Label(container, SWT.NONE);
		lblCustomerNumber.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCustomerNumber.setText("Kundennummer");

		txtCustomerNumber = new Text(container, SWT.BORDER);
		txtCustomerNumber.setEnabled(false);
		txtCustomerNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		grpAddress = new Group(container, SWT.NONE);
		grpAddress.setText("Anschrift");
		GridLayout gl_grpAddress = new GridLayout(1, false);
		gl_grpAddress.marginHeight = 0;
		gl_grpAddress.marginWidth = 0;
		gl_grpAddress.verticalSpacing = 0;
		gl_grpAddress.horizontalSpacing = 0;
		grpAddress.setLayout(gl_grpAddress);
		grpAddress.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 5));

		chkAddressGenerate = new Button(grpAddress, SWT.CHECK);
		chkAddressGenerate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				generateAddress();
				txtManualAddress.setEnabled(!chkAddressGenerate.getSelection());
			}
		});
		chkAddressGenerate.setSelection(true);
		chkAddressGenerate.setText("automatisch erzeugen");

		txtManualAddress = new Text(grpAddress, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.CANCEL | SWT.MULTI);
		txtManualAddress.setEnabled(false);
		GridData gd_txtManualAddress = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_txtManualAddress.verticalIndent = 5;
		txtManualAddress.setLayoutData(gd_txtManualAddress);
		txtManualAddress.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					e.doit = true;
				}
			}
		});

		// Company
		Label labelCompany = new Label(container, SWT.NONE);
		labelCompany.setText("Firma");
		labelCompany.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		txtCompany = new Text(container, SWT.BORDER | SWT.MULTI);
		GridData gd_txtCompany = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtCompany.heightHint = 40;
		txtCompany.setLayoutData(gd_txtCompany);
		txtCompany.addFocusListener(this.updateAddressListener);
		txtCompany.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					e.doit = true;
				}
			}
		});

		// The title and gender's label
		Label labelTitle = new Label(container, SWT.NONE);
//		labelTitle.setText(msg.commonFieldGender + ", " + msg.commonFieldTitle);
		labelTitle.setText("Anrede, Titel");
		labelTitle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		pnlGenderTitle = new Composite(container, SWT.NONE);
		pnlGenderTitle.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		RowLayout rl_salutationPanel = new RowLayout(SWT.HORIZONTAL);
		rl_salutationPanel.marginTop = 0;
		rl_salutationPanel.marginRight = 0;
		rl_salutationPanel.marginLeft = 0;
		rl_salutationPanel.marginBottom = 0;
		rl_salutationPanel.fill = true;
		pnlGenderTitle.setLayout(rl_salutationPanel);
		// Salutation
		comboGender = new ComboViewer(pnlGenderTitle, SWT.BORDER | SWT.READ_ONLY);
		Combo combo = comboGender.getCombo();
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				generateAddress();
			}
		});
		comboGender.setContentProvider(new HashMapContentProvider<Integer, String>());
		comboGender.setInput(getSalutationMap());
		comboGender.setLabelProvider(new NumberLabelProvider<Integer, String>(getSalutationMap()));
		// Title
		txtTitle = new Text(pnlGenderTitle, SWT.BORDER);
		txtTitle.setTextLimit(32);
		txtTitle.setLayoutData(new RowData(100, SWT.DEFAULT));
		txtTitle.addFocusListener(this.updateAddressListener);

		// First and last name
		Label labelName = new Label(container, SWT.NONE);
//        labelName.setText(msg.editorContactFieldFirstnamelastnameName);
		labelName.setText("Vorname, Nachname");
		labelName.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));

		pnlFirstnameName = new Composite(container, SWT.NONE);
		pnlFirstnameName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout gl_namePanel = new GridLayout(2, true);
		gl_namePanel.marginWidth = 0;
		gl_namePanel.marginHeight = 0;
		gl_namePanel.verticalSpacing = 0;
		gl_namePanel.horizontalSpacing = 0;
		pnlFirstnameName.setLayout(gl_namePanel);
		txtFirstname = new Text(pnlFirstnameName, SWT.BORDER);
		txtFirstname.setTextLimit(128);
		txtFirstname.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		txtFirstname.addFocusListener(this.updateAddressListener);
		txtName = new Text(pnlFirstnameName, SWT.BORDER);
		txtName.setTextLimit(255);
		GridData gd_txtName = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_txtName.horizontalIndent = 3;
		txtName.setLayoutData(gd_txtName);
		txtName.addFocusListener(this.updateAddressListener);

		Label lblNameaddon = new Label(container, SWT.NONE);
		lblNameaddon.setEnabled(false);
		lblNameaddon.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNameaddon.setText("Namenszusatz");
		txtNameAddon = new Text(container, SWT.BORDER);
		txtNameAddon.setTextLimit(255);
		txtNameAddon.setEnabled(false);
		txtNameAddon.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtNameAddon.addFocusListener(this.updateAddressListener);

		Label lblStreet = new Label(container, SWT.NONE);
		lblStreet.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStreet.setText("Straße");
		txtStreet = new Text(container, SWT.BORDER);
		txtStreet.setTextLimit(255);
		txtStreet.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtStreet.addFocusListener(this.updateAddressListener);

		Label lblConsultant = new Label(container, SWT.NONE);
		lblConsultant.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblConsultant.setText("Ansprechpartner");
		txtConsultant = new Text(container, SWT.BORDER);
		txtConsultant.setTextLimit(255);
		txtConsultant.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtConsultant.addFocusListener(this.updateAddressListener);

		Label lblAddressAddon = new Label(container, SWT.NONE);
		lblAddressAddon.setEnabled(false);
		lblAddressAddon.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblAddressAddon.setText("Adresszusatz");
		txtAddressAddon = new Text(container, SWT.BORDER);
		txtAddressAddon.setTextLimit(255);
		txtAddressAddon.setEnabled(false);
		txtAddressAddon.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtAddressAddon.addFocusListener(this.updateAddressListener);

		Label lblEMail = new Label(container, SWT.NONE);
		lblEMail.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblEMail.setText("E-Mail");
		txtEMail = new Text(container, SWT.BORDER);
		txtEMail.setTextLimit(128);
		txtEMail.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblCityAddon = new Label(container, SWT.NONE);
		lblCityAddon.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCityAddon.setText("Ortsteil");
		txtCityAddon = new Text(container, SWT.BORDER);
		txtCityAddon.setTextLimit(255);
		txtCityAddon.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtCityAddon.addFocusListener(this.updateAddressListener);

		Label lblPhone = new Label(container, SWT.NONE);
		lblPhone.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblPhone.setText("Telefon");
		txtPhone = new Text(container, SWT.BORDER);
		txtPhone.setTextLimit(64);
		txtPhone.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblZipCity = new Label(container, SWT.NONE);
		lblZipCity.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblZipCity.setText("PLZ, Ort");
		pnlZipCity = new Composite(container, SWT.NONE);
		pnlZipCity.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		pnlFirstnameName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		GridLayout gl_pnlZipCity = new GridLayout(2, false);
		gl_pnlZipCity.marginWidth = 0;
		gl_pnlZipCity.marginHeight = 0;
		gl_pnlZipCity.verticalSpacing = 0;
		gl_pnlZipCity.horizontalSpacing = 0;
		pnlZipCity.setLayout(gl_pnlZipCity);
		txtZip = new Text(pnlZipCity, SWT.BORDER);
		txtZip.setTextLimit(16);
		GridData gd_txtZip = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		gd_txtZip.widthHint = 80;
		txtZip.setLayoutData(gd_txtZip);
		txtZip.addFocusListener(this.updateAddressListener);
		txtCity = new Text(pnlZipCity, SWT.BORDER);
		txtCity.setTextLimit(255);
		GridData gd_txtCity = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd_txtCity.horizontalIndent = 3;
		txtCity.setLayoutData(gd_txtCity);
		txtCity.addFocusListener(this.updateAddressListener);

		Label lblFax = new Label(container, SWT.NONE);
		lblFax.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFax.setText("Telefax");
		txtFax = new Text(container, SWT.BORDER);
		txtFax.setTextLimit(64);
		txtFax.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblCountrycode = new Label(container, SWT.NONE);
		lblCountrycode.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblCountrycode.setText("Land");
		comboCountrycode = new ComboViewer(container, SWT.NONE | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboCountrycode.getCombo());
		comboCountrycode.setContentProvider(new StringHashMapContentProvider());
		comboCountrycode.setInput(localeUtil.getLocaleCountryMap());
		StringComboBoxLabelProvider stringComboBoxLabelProvider = ContextInjectionFactory
				.make(StringComboBoxLabelProvider.class, context);
		stringComboBoxLabelProvider.setCountryNames(localeUtil.getLocaleCountryMap());
		comboCountrycode.setLabelProvider(stringComboBoxLabelProvider);
		Combo comboCC = comboCountrycode.getCombo();
		comboCC.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				generateAddress();
			}
		});

		Label lblMobile = new Label(container, SWT.NONE);
		lblMobile.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMobile.setText("Mobilnummer");
		txtMobile = new Text(container, SWT.BORDER);
		txtMobile.setTextLimit(64);
		txtMobile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblSupplierNumber = new Label(container, SWT.NONE);
		GridData gd_lblSupplierNumber = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblSupplierNumber.verticalIndent = 7;
		lblSupplierNumber.setLayoutData(gd_lblSupplierNumber);
		lblSupplierNumber.setText("Lieferanten-Nummer");
		txtSupplierNumber = new Text(container, SWT.BORDER);
		txtSupplierNumber.setTextLimit(32);
		GridData gd_txtSupplierNumber = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtSupplierNumber.verticalIndent = 7;
		txtSupplierNumber.setLayoutData(gd_txtSupplierNumber);

		Label lblMandateReference = new Label(container, SWT.NONE);
		GridData gd_lblMandateReference = new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1);
		gd_lblMandateReference.verticalIndent = 7;
		lblMandateReference.setLayoutData(gd_lblMandateReference);
		lblMandateReference.setText("Mandatsreferenz");
		txtMandateReference = new Text(container, SWT.BORDER);
		txtMandateReference.setTextLimit(128);
		GridData gd_txtMandateReference = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_txtMandateReference.verticalIndent = 7;
		txtMandateReference.setLayoutData(gd_txtMandateReference);

		Label lblTAlias = new Label(container, SWT.NONE);
		lblTAlias.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTAlias.setText("Alias-Name");
		txtAlias = new Text(container, SWT.BORDER);
		txtAlias.setTextLimit(255);
		txtAlias.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		grpDescription = new Group(container, SWT.NONE);
		grpDescription.setText("Vermerk");
		grpDescription.setLayout(new FillLayout(SWT.HORIZONTAL));
		grpDescription.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 3));
		txtDescription = new Text(grpDescription, SWT.BORDER | SWT.MULTI);
		txtDescription.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_TAB_NEXT && e.stateMask == SWT.CTRL) {
					e.doit = false;
				} else if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
					e.doit = true;
				}
			}
		});

		Label lblVatNumber = new Label(container, SWT.NONE);
		lblVatNumber.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblVatNumber.setText("UStId.-Nr.");
		txtVatNumber = new Text(container, SWT.BORDER);
		txtVatNumber.setTextLimit(32);
		txtVatNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Label lblGln = new Label(container, SWT.NONE);
		lblGln.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblGln.setText("GLN");
		txtGLN = new Text(container, SWT.BORDER);
		txtGLN.setTextLimit(20);
		txtGLN.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		setTabOrder(container);
		
		updateUI();
		
		if (chkAddressGenerate.getSelection()) {
			txtCompany.setFocus();
		} else {
			txtManualAddress.setFocus();
		}

		return area;
	}
	
	protected void setTabOrder(Composite container) {
		Control[] controls = new Control[] {
				grpAddress, /* chkAddressGenerate, txtManualAddress */
				txtCompany,
				pnlGenderTitle, /* comboGender, txtTitle */
				pnlFirstnameName, /* txtFirstname, txtName */
				txtNameAddon,
				txtStreet,
				txtAddressAddon,
				txtCityAddon,
				pnlZipCity, /* txtZip, txtCity */
				comboCountrycode.getCombo(),
				txtConsultant,
				txtEMail,
				txtPhone,
				txtFax,
				txtMobile,
				txtSupplierNumber,
				txtAlias,
				txtVatNumber,
				txtGLN,
				txtMandateReference,
				grpDescription /* txtDescription */
			};
		container.setTabList(controls);
	}

	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize() {
		return new Point(900, 620);
	}

	@Override
	protected void okPressed() {
		if (updateReceiverData()) {
			super.okPressed();
		}
	}

	private Map<Integer, String> getSalutationMap() {
		if (salutationMap == null || salutationMap.isEmpty()) {
			salutationMap = new HashMap<>();
			for (int i = 0; i <= ContactUtil.MAX_SALUTATION_COUNT; i++) {
				salutationMap.put(i, contactUtil.getSalutationString(i));
			}
		}
		return salutationMap;
	}

	private void updateUI() {
		txtCustomerNumber.setText(Util.defaultIfNull(theReceiver.getCustomerNumber(), ""));
		txtCompany.setText(Util.defaultIfNull(theReceiver.getCompany(), ""));
		if (theReceiver.getGender() != null)
			comboGender.setSelection(new StructuredSelection(theReceiver.getGender()));
		txtTitle.setText(Util.defaultIfNull(theReceiver.getTitle(), ""));
		txtFirstname.setText(Util.defaultIfNull(theReceiver.getFirstName(), ""));
		txtName.setText(Util.defaultIfNull(theReceiver.getName(), ""));
//		txtNameAddon.setText(Util.defaultIfNull(theReceiver.getNameAddon(), ""));
		txtStreet.setText(Util.defaultIfNull(theReceiver.getStreet(), ""));
//		txtAddressAddon.setText(Util.defaultIfNull(theReceiver.getAddressAddon(), ""));
		txtCityAddon.setText(Util.defaultIfNull(theReceiver.getCityAddon(), ""));
		txtZip.setText(Util.defaultIfNull(theReceiver.getZip(), ""));
		txtCity.setText(Util.defaultIfNull(theReceiver.getCity(), ""));
		if (theReceiver.getCountryCode() != null)
			comboCountrycode.setSelection(new StructuredSelection(theReceiver.getCountryCode()));

		chkAddressGenerate.setSelection(theReceiver.getManualAddress() == null);
		txtManualAddress.setText(contactUtil.getAddressAsString(theReceiver));
		txtManualAddress.setEnabled(!chkAddressGenerate.getSelection());
		txtConsultant.setText(Util.defaultIfNull(theReceiver.getConsultant(), ""));
		txtEMail.setText(Util.defaultIfNull(theReceiver.getEmail(), ""));
		txtPhone.setText(Util.defaultIfNull(theReceiver.getPhone(), ""));
		txtFax.setText(Util.defaultIfNull(theReceiver.getFax(), ""));
		txtMobile.setText(Util.defaultIfNull(theReceiver.getMobile(), ""));

		txtSupplierNumber.setText(Util.defaultIfNull(theReceiver.getSupplierNumber(), ""));
		txtAlias.setText(Util.defaultIfNull(theReceiver.getAlias(), ""));
		txtVatNumber.setText(Util.defaultIfNull(theReceiver.getVatNumber(), ""));
		txtGLN.setText(theReceiver.getGln() != null ? theReceiver.getGln().toString() : "");

		txtMandateReference.setText(Util.defaultIfNull(theReceiver.getMandateReference(), ""));
		txtDescription.setText(Util.defaultIfNull(theReceiver.getDescription(), ""));
	}

	private boolean updateReceiverData() {
		// Validation
		if (Util.defaultIfEmpty(txtCompany.getText(), null) == null
				&& Util.defaultIfEmpty(txtName.getText(), null) == null
				&& Util.defaultIfEmpty(txtManualAddress.getText(), null) == null) {
			MessageDialog.openError(getShell(), "Fehlende Angaben", "Firma,\nName\noder Adresseingabe\nist erforderlich.");
			return false;
		}
		theReceiver.setCompany(Util.defaultIfEmpty(txtCompany.getText(), null));
		theReceiver.setGender((Integer) comboGender.getStructuredSelection().getFirstElement());
		theReceiver.setTitle(Util.defaultIfEmpty(txtTitle.getText(), null));
		theReceiver.setFirstName(Util.defaultIfEmpty(txtFirstname.getText(), null));
		theReceiver.setName(Util.defaultIfEmpty(txtName.getText(), null));
//		theReceiver.setNameAddon(Util.defaultIfEmpty(txtNameAddon.getText(), null));
		theReceiver.setStreet(Util.defaultIfEmpty(txtStreet.getText(), null));
//		theReceiver.setAddressAddon(Util.defaultIfEmpty(txtAddressAddon.getText(), null));
		theReceiver.setCityAddon(Util.defaultIfEmpty(txtCityAddon.getText(), null));
		theReceiver.setZip(Util.defaultIfEmpty(txtZip.getText(), null));
		theReceiver.setCity(Util.defaultIfEmpty(txtCity.getText(), null));
		theReceiver.setCountryCode((String) comboCountrycode.getStructuredSelection().getFirstElement());

		if (!chkAddressGenerate.getSelection()) {
			theReceiver.setManualAddress(Util.defaultIfEmpty(txtManualAddress.getText(), null));
		} else {
			theReceiver.setManualAddress(null);
		}
		theReceiver.setConsultant(Util.defaultIfEmpty(txtConsultant.getText(), null));
		theReceiver.setEmail(Util.defaultIfEmpty(txtEMail.getText(), null));
		theReceiver.setPhone(Util.defaultIfEmpty(txtPhone.getText(), null));
		theReceiver.setFax(Util.defaultIfEmpty(txtFax.getText(), null));
		theReceiver.setMobile(Util.defaultIfEmpty(txtMobile.getText(), null));

		theReceiver.setSupplierNumber(Util.defaultIfEmpty(txtSupplierNumber.getText(), null));
		theReceiver.setAlias(Util.defaultIfEmpty(txtAlias.getText(), null));
		theReceiver.setVatNumber(Util.defaultIfEmpty(txtVatNumber.getText(), null));
		try {
			theReceiver.setGln(Long.valueOf(txtGLN.getText()));
		} catch (Exception e) {
			theReceiver.setGln(null);
		}

		theReceiver.setMandateReference(Util.defaultIfEmpty(txtMandateReference.getText(), null));
		theReceiver.setDescription(Util.defaultIfEmpty(txtDescription.getText(), null));
		return true;
	}
}
