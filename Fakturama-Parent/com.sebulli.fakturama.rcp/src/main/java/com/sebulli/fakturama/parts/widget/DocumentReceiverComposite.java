package com.sebulli.fakturama.parts.widget;
// TODO GS/i18n - check all -

import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.window.Window;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TypedListener;

import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.dao.TextsDAO;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.EmbeddedProperties;
import com.sebulli.fakturama.misc.Util;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.Creditor;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentReceiver;
import com.sebulli.fakturama.model.TextModule;
import com.sebulli.fakturama.parts.CreditorEditor;
import com.sebulli.fakturama.parts.DebitorEditor;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.parts.DocumentReceiverEditorDialog;
import com.sebulli.fakturama.util.ContactUtil;
import com.sebulli.fakturama.util.DocumentTypeUtil;

/**
 * @author gschrick
 *
 */
public class DocumentReceiverComposite extends Composite {

	@Inject
	private IEclipseContext context;

	@Inject
	protected EHandlerService handlerService;

	@Inject
	protected ECommandService commandService;

    @Inject
    private ESelectionService selectionService;

    @Inject
	@org.eclipse.e4.core.di.annotations.Optional
	private IPreferenceStore eclipsePrefs;

	@Inject
	@Translation
	protected Messages msg;

	@Inject
	private ContactUtil contactUtil;

	@Inject
	private TextsDAO textsDAO;

	@Inject
	private ContactsDAO contactsDAO;

//    @Inject
//    private DocumentsDAO documentsDAO;

    
	private CTabItem parentTabItem = null;


	private BillingType theBillingType;
	private DocumentEditor theEditor;
	// value of FKT_DOCUMENTRECEIVER.ID (rec id in DB)
	private long recordId;
	private DocumentReceiver theReceiver = null;
	// special: read from property "info" in FKT_CONTACT.NOTE
	private String contactInfo = null;

	public static final String TEXT_NO_VALUE = "---";
// TODO GS/ I18N
	private String text_lblEMailToolTipPrefix = "E-Mail senden an: ";
	private String text_txtAddress_DeliverySameAsInvoice = "\n\t(wie Rechnungsanschrift)";
	private Label lblContactInfo;
	private Text txtAddress;
	private Link lnkCustomerNumber;
	private Link lnkEMail;
	private Label lblRemark;
	private Label lblSupplierNumber;
	private Label lblCustomerNumber;
	private Label lblSupplierNumberValue;
	private Label lblEMail;


	@Inject
	public DocumentReceiverComposite(Composite parent, DocumentEditor editor, BillingType bType) {
		super(parent, SWT.NONE);

		if (editor == null) throw new SWTError(SWT.ERROR_NULL_ARGUMENT, "DocumentEditor must not be null!");
		theEditor = editor;
		if (bType == null) throw new SWTError(SWT.ERROR_NULL_ARGUMENT, "BillingType must not be null!");
		theBillingType = bType;
		
		theReceiver = new DocumentReceiver();

		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);

		lblContactInfo = new Label(this, SWT.NONE);
		lblContactInfo.setAlignment(SWT.CENTER);
		lblContactInfo.setBackground(new Color(new RGB(179, 255, 217)));
		lblContactInfo.setForeground(new Color(new RGB(0, 0, 0)));
		lblContactInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		lblContactInfo.setText("[Contact Info]");

		txtAddress = new Text(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL | SWT.READ_ONLY);
		txtAddress.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		txtAddress.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				editDocumentReceiver();
			}
		});
		txtAddress.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
		        if (e.detail == SWT.TRAVERSE_TAB_NEXT || e.detail == SWT.TRAVERSE_TAB_PREVIOUS) {
		            e.doit = true;
		        } else if (e.detail == SWT.TRAVERSE_RETURN) {
		        	e.doit = false;
					editDocumentReceiver();
		        }
			}
		});
		txtAddress.addKeyListener(new KeyListener() {
			@Override
			public void keyPressed(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e == null) ;
			}
			@Override
			public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub
				if (e == null) ;
			}
		});
		
		Composite sidebar = new Composite(this, SWT.NONE);
		GridLayout gl_sidebar = new GridLayout(1, false);
		gl_sidebar.verticalSpacing = 1;
		sidebar.setLayout(gl_sidebar);
		sidebar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		
		lblCustomerNumber = new Label(sidebar, SWT.NONE);
		lblCustomerNumber.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblCustomerNumber.setText("Kundennummer:");
		
		lnkCustomerNumber = new Link(sidebar, SWT.NONE);
		GridData gd_lnkCustomerNumber = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lnkCustomerNumber.horizontalIndent = 8;
		lnkCustomerNumber.setLayoutData(gd_lnkCustomerNumber);
		lnkCustomerNumber.setToolTipText("Im Kontakteditor öffnen.");
		lnkCustomerNumber.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openContactEditor();
			}
		});
		lnkCustomerNumber.setText("---");
		
		lblSupplierNumber = new Label(sidebar, SWT.NONE);
		GridData gd_lblSupplierNumber = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblSupplierNumber.verticalIndent = 5;
		lblSupplierNumber.setLayoutData(gd_lblSupplierNumber);
		lblSupplierNumber.setText("Lieferanten-Nummer:");
		
		lblSupplierNumberValue = new Label(sidebar, SWT.NONE);
		GridData gd_lblSupplierNumberValue = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblSupplierNumberValue.horizontalIndent = 8;
		lblSupplierNumberValue.setLayoutData(gd_lblSupplierNumberValue);
		lblSupplierNumberValue.setText("---");
		
		lblEMail = new Label(sidebar, SWT.NONE);
		GridData gd_lblEMail = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblEMail.verticalIndent = 5;
		lblEMail.setLayoutData(gd_lblEMail);
		lblEMail.setText("E-Mail:");
		
		lnkEMail = new Link(sidebar, SWT.NONE);
		GridData gd_lnkEMail = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lnkEMail.horizontalIndent = 8;
		lnkEMail.setLayoutData(gd_lnkEMail);
		lnkEMail.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				startEMail();
			}
		});
		lnkEMail.setText("---");
		
		lblRemark = new Label(sidebar, SWT.NONE);
		GridData gd_lblRemark = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_lblRemark.widthHint = 180;
		gd_lblRemark.verticalIndent = 5;
		lblRemark.setLayoutData(gd_lblRemark);
		lblRemark.setAlignment(SWT.CENTER);
		lblRemark.setText("* Vermerk vorhanden *");
		lblRemark.setBackground(new Color(new RGB(179, 255, 217)));
		lblRemark.setForeground(new Color(new RGB(0, 0, 0)));
	}

	protected void editDocumentReceiver() {
		DocumentReceiverEditorDialog dreDialog = new DocumentReceiverEditorDialog(getShell(), theReceiver);
		ContextInjectionFactory.inject(dreDialog, context);
		if (dreDialog.open() == Window.OK) {
			notifyListeners(SWT.Modify, new Event());
			updateView();
		}
		dreDialog = null;
	}
	
	protected void openContactEditor() {
		if (theReceiver.getOriginContactId() != null) {
			Contact c = contactsDAO.findById(theReceiver.getOriginContactId(), true);
			if (c != null) {
				selectionService.setSelection(null);
				Map<String, Object> params = new HashMap<>();
				if (c.getClass().equals(Creditor.class)) {
					params.put(CallEditor.PARAM_EDITOR_TYPE, CreditorEditor.ID);
				} else {
					params.put(CallEditor.PARAM_EDITOR_TYPE, DebitorEditor.ID);
				}
				c = null;
				params.put(CallEditor.PARAM_OBJ_ID, Long.toString(theReceiver.getOriginContactId()));
				ParameterizedCommand parameterizedCommand = commandService.createCommand(CommandIds.CMD_CALL_EDITOR, params);
				handlerService.executeHandler(parameterizedCommand);
			}
		}
	}


	public BillingType getBillingType() {
		return theBillingType;
	}


	public DocumentReceiver getDocumentReceiver() {
		return theReceiver;
	}

	/**
	 * binds the control to this DocumentReceiver object (its record Id)
	 * if null, record Id is set to 0
	 * @param rcvr
	 */
	public void bindDocumentReceiver(DocumentReceiver rcvr) {
		if (rcvr != null) {
			recordId = rcvr.getId();
			theReceiver = rcvr;
			theReceiver.setBillingType(theBillingType);
			setContactInfo();
		} else {
			recordId = 0;
			updateDocumentReceiver(null);
		}
		// update view
		updateView();
	}

	public void setDocumentReceiver(DocumentReceiver documentReceiver) {
		this.updateDocumentReceiver(documentReceiver);
		updateView();
    	notifyListeners(SWT.Modify, new Event());
	}
	void updateDocumentReceiver(DocumentReceiver documentReceiver) {
		this.theReceiver = documentReceiver != null ? documentReceiver : new DocumentReceiver();
		theReceiver.setId(recordId);
		theReceiver.setBillingType(theBillingType);
		if (documentReceiver != null) {
        	theReceiver.setDateAdded(new Date());
        	theReceiver.setValidFrom(theReceiver.getDateAdded());
			theReceiver.setDeleted(Boolean.FALSE);
			if (theReceiver.getManualAddress() != null && theReceiver.getManualAddress().trim().length() == 0) {
				theReceiver.setManualAddress(null);
			}
		} else {
			theReceiver.setDeleted(Boolean.TRUE);
		}
		setContactInfo();
	}
	
	/**
	 * if it's an existing contact, read property "info" from contact
	 */
	void setContactInfo() {
		contactInfo = null;
		if (theReceiver != null && theReceiver.getOriginContactId() != null) {
			Contact c = contactsDAO.findById(theReceiver.getOriginContactId(), true);
			if (c != null) {
				EmbeddedProperties ep = new EmbeddedProperties(c.getNote());
				if (ep != null) {
					contactInfo = ep.getProperty(EmbeddedProperties.PROPERTY_INFO, null, null);
				}
				c = null;
			}
		}
	}


	public boolean removeDocumentReceiver() {
		String addressTypeName = parentTabItem != null ? parentTabItem.getText() : "Adresse";
// TODO GS/i18n
//		boolean doIt = (MessageDialog.openQuestion(top.getShell(), msg.dialogMessageboxTitleWarning, msg.editorContactQuestionRemoveaddress));
		boolean doIt = (MessageDialog.openQuestion(getShell(), addressTypeName+" entfernen", addressTypeName+" von diesem Dokument entfernen?"));
		if (doIt) {
			updateDocumentReceiver(null);
			updateView();
			Event e = new Event();
			e.data = theReceiver;
			notifyListeners(SWT.Modify, e);
		}
		return doIt;
	}
	
	
	/**
	 * If the DocumentReceiver is considered empty
	 * @return true if empty
	 */
	public boolean isEmpty() {
		return theReceiver == null || (
				theReceiver.getOriginAddressId() == null
				&& theReceiver.getCompany() == null
				&& theReceiver.getName() == null
				&& theReceiver.getManualAddress() == null);
	}

	/**
	 * If the DocumentReceiver has data (to persist)
	 * @return true if recordId != 0 or !isEmpty()
	 */
	public boolean hasData() {
		return (recordId != 0 || !isEmpty());
	}

	public Text getAddressTextWidget() {
		return txtAddress;
	}


	/**
	 * updates the Controls' content from the DocumentReceiver object
	 */
	public void updateView() {
		if (theReceiver != null ) {
			txtAddress.setText(contactUtil.getAddressAsString(theReceiver));
			// CustomerNumber (Link to open in editor)
			String data = Util.defaultIfEmpty(theReceiver.getCustomerNumber(), null);
			lnkCustomerNumber.setText(data != null ? "<a>"+data+"</a>" : TEXT_NO_VALUE);
			lnkCustomerNumber.setEnabled(theReceiver.getOriginContactId() != null);
			// SupplierNumber
			data = Util.defaultIfEmpty(theReceiver.getSupplierNumber(), null);
			lblSupplierNumberValue.setText(data != null ? data : TEXT_NO_VALUE);
			lblSupplierNumberValue.setEnabled(data != null);
			// EMail (Link to open E-Mail Client)
			data = Util.defaultIfEmpty(theReceiver.getEmail(), null);
			if (data != null) {
				if (data.length() > 32) {
					lnkEMail.setText("<a>"+data.substring(0, 27)+"...</a>");
				} else {
					lnkEMail.setText("<a>"+data+"</a>");
				}
				lnkEMail.setToolTipText(text_lblEMailToolTipPrefix + data);
			} else {
				lnkEMail.setText(TEXT_NO_VALUE);
				lnkEMail.setToolTipText("");
			}
			lnkEMail.setEnabled(data != null);
			// Remark
			lblRemark.setVisible(theReceiver.getDescription() != null);
		} else {
			txtAddress.setText("");
			lnkCustomerNumber.setText(TEXT_NO_VALUE);
			lnkCustomerNumber.setEnabled(false);
			lblSupplierNumberValue.setText(TEXT_NO_VALUE);
			lblSupplierNumberValue.setEnabled(false);
			lnkEMail.setText(TEXT_NO_VALUE);
			lnkEMail.setEnabled(false);
			lblRemark.setVisible(false);
		}
		lblCustomerNumber.setEnabled(lnkCustomerNumber.getEnabled());
		lblSupplierNumber.setEnabled(lblSupplierNumberValue.getEnabled());
		lblEMail.setEnabled(lnkEMail.getEnabled());
		if (theBillingType.isDELIVERY() && isEmpty()) {
			txtAddress.setText(text_txtAddress_DeliverySameAsInvoice);
		}
		lblContactInfo.setText(Util.defaultIfNull(contactInfo, ""));
		GridData gd = (GridData) lblContactInfo.getLayoutData();
		gd.exclude = contactInfo == null;
		lblContactInfo.setVisible(!gd.exclude);
//		this.requestLayout();
		this.layout(true, true);
		reflectStateInUI();
	}
	
	
	public CTabItem getParentTabItem() {
		return parentTabItem;
	}
	public void setParentTabItem(CTabItem parentTabItem) {
		this.parentTabItem = parentTabItem;
	}


	public void reflectStateInUI() {
		if (parentTabItem != null) {
			if (isEmpty()) {
				parentTabItem.setForeground(GUIHelper.COLOR_GRAY);
			} else {
				parentTabItem.setForeground(GUIHelper.COLOR_WIDGET_FOREGROUND);
			}
		}
	}
	

	// fire modify event when data changed
	public void addModifyListener(ModifyListener listener) {
		addListener(SWT.Modify, new TypedListener(listener));
	}
	public void removeModifyListener(ModifyListener listener) {
		removeListener(SWT.Modify, listener);
	}


	void startEMail() {
		String to = theReceiver.getEmail();
		String mailProgCall = eclipsePrefs.getString(Constants.PREFERENCES_OPENOFFICE_MAILCLIENTCALL);
		Document document = theEditor.getDocument();
		BillingType docBT = document.getBillingType();
		if (mailProgCall != null && mailProgCall.length() > 0) {
			try {
				// required for exec command
				String[] execCommandSegments = mailProgCall.split("_\\|_"); // segments separated by: _|_
				Pattern pattern = Pattern.compile("\\{(.+?)\\}"); // placeholder: { ... }
	
				for (int idx = 0; idx < execCommandSegments.length; idx++) {
					if (execCommandSegments[idx].contains("{")) {
						Matcher matcher = pattern.matcher(execCommandSegments[idx]);
						StringBuffer buffer = new StringBuffer();
						while (matcher.find()) {
							String thePaceholder = matcher.group(1);
							StringBuffer sbValue = new StringBuffer();
							String[] parts = thePaceholder.split("\\|");
							if (parts[0].startsWith("to")) {
								if (parts.length >= 2)
									sbValue.append(parts[1]);
								sbValue.append(to);
								if (parts.length >= 3)
									sbValue.append(parts[2]);
							} else if (parts[0].startsWith("subject")) {
								String theSubject = msg
										.getMessageFromKey(DocumentTypeUtil.findByBillingType(docBT).getSingularKey()) + " "
										+ document.getName() + " - " + document.getCustomerRef();
								if (parts.length >= 2)
									sbValue.append(parts[1]);
								if (parts[0].contains(":e")) {
									// encoded value
									sbValue.append(URLEncoder.encode(theSubject, "UTF-8").replaceAll("\\+", "%20"));
								} else {
									// plain value
									sbValue.append(theSubject);
								}
								if (parts.length >= 3)
									sbValue.append(parts[2]);
							} else if (parts[0].startsWith("body")) {
//TODO GS/ text seems to be cached? how to get the latest/current in DB?
								TextModule textModule = textsDAO.findByName("mailto" + docBT.getName());
								if (textModule != null) {
									String bodyContent = textModule.getText();
									if (parts.length >= 2)
										sbValue.append(parts[1]);
									if (parts[0].contains(":e")) {
										// encoded value
										sbValue.append(URLEncoder.encode(bodyContent, "UTF-8").replaceAll("\\+", "%20"));
									} else {
										// plain value
										sbValue.append(bodyContent);
									}
									if (parts.length >= 3)
										sbValue.append(parts[2]);
								}
							} else if (parts[0].startsWith("attachment")) {
								String thePDF = document.getPdfPath();
								if (thePDF != null && thePDF.length() > 0) {
									if (parts.length >= 2)
										sbValue.append(parts[1]);
									if (parts[0].contains(":e")) {
										// encoded value
										sbValue.append(URLEncoder.encode(thePDF, "UTF-8").replaceAll("\\+", "%20"));
									} else {
										// plain value
										sbValue.append(thePDF);
									}
									if (parts.length >= 3)
										sbValue.append(parts[2]);
								}
							}
							matcher.appendReplacement(buffer, "");
							if (sbValue.length() > 0) {
								buffer.append(sbValue);
							}
						}
						matcher.appendTail(buffer);
						execCommandSegments[idx] = buffer.toString();
					}
				} // for
				Runtime.getRuntime().exec(execCommandSegments);
			} catch (Exception exception) {
//TODO GS/i18n
				MessageDialog.openInformation(getShell(), "MAILTO/Exception",
						"mailProgCall: " + mailProgCall + "\nException:\n" + exception.toString());
			}
		} else {
			// no mail call config
//TODO GS/i18n
			MessageDialog.openInformation(getShell(), "MAILTO", "Aufruf für den Mail-Client ist in den Einstellungen nicht konfiguriert.");
		}
	}
	
}

