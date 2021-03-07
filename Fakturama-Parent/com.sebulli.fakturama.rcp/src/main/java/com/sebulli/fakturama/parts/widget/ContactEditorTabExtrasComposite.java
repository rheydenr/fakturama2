package com.sebulli.fakturama.parts.widget;
//TODO GS/i18n - check all -
import java.util.Hashtable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TypedListener;

import com.sebulli.fakturama.misc.EmbeddedProperties;
import com.sebulli.fakturama.misc.Util;


public class ContactEditorTabExtrasComposite extends Composite {
// TODO GS/i18n temporary(?), should be managed by i18n
	private final Hashtable<String, String> i18n;
// temporary -end-
	private EmbeddedProperties theProperties;
	private Text txtInfo;
	private CCombo comboEInvoice;
	private CCombo comboBT10;
	private Text txtMailTo;
	private Text txtNotice;
	private Text txtUrl;
	private CCombo comboBT13;
	private CCombo comboBT12;
	private CCombo comboBT11;
	private Text txtBuyerId;
	private Text txtBuyerGlobalId;
	private Text txtBuyerGlobalIdSchemeId;

	
	private boolean isDirty = false;
	public boolean isDirty() {
		return isDirty;
	}
	public void setDirty(boolean isDirty) {
		this.isDirty = isDirty;
	}
	protected void setDirty() {
		if (!isDirty) {
			this.isDirty = true;
			notifyListeners(SWT.Modify, new Event());
		}
	}

	private String oldValue = null;
	private FocusListener focusListenerText = new FocusListener() {
		@Override
		public void focusLost(FocusEvent e) {
			if (!isDirty && !((Text) e.widget).getText().equals(oldValue)) {
				setDirty();
			}
		}
		@Override
		public void focusGained(FocusEvent e) {
			oldValue = ((Text) e.widget).getText();
		}
	};
	private FocusListener focusListenerCCombo = new FocusListener() {
		@Override
		public void focusLost(FocusEvent e) {
			if (!isDirty && !((CCombo) e.widget).getText().equals(oldValue)) {
				setDirty();
			}
		}
		@Override
		public void focusGained(FocusEvent e) {
			oldValue = ((CCombo) e.widget).getText();
		}
	};
	private SelectionListener selectionListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			CCombo theCombo = (CCombo) e.widget;
			theCombo.setEditable(theCombo.getText().length() == 0);
			setDirty();
		}
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	};

	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public ContactEditorTabExtrasComposite(Composite parent, int style) {
		super(parent, style);

		theProperties = new EmbeddedProperties();

// TODO GS/i18n temporary, should be managed by i18n
		i18n = new Hashtable<String, String>();
		i18n.put(EmbeddedProperties.VALUE_NULL, "");
		i18n.put(EmbeddedProperties.VALUE_FIELD_CUSTREF, "Kundenreferenz");
		i18n.put(EmbeddedProperties.VALUE_FIELD_CUSTREF_LEFT, "Kundenreferenz, links von ' - '");
		i18n.put(EmbeddedProperties.VALUE_FIELD_ORDER_NAME, "Nummer der Bestellung (wenn Dokument vorhanden)");
// temporary -end-

		setLayout(new GridLayout(2, false));
		
		Label lblInfo = new Label(this, SWT.NONE);
		lblInfo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblInfo.setToolTipText("wird im Dokument-Editor über der Adresse angezeigt.");
		lblInfo.setText("Info");
		
		txtInfo = new Text(this, SWT.BORDER);
		txtInfo.addFocusListener(focusListenerText);
		txtInfo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Group grpEInvoice = new Group(this, SWT.NONE);
		grpEInvoice.setText("E-Rechnung");
		grpEInvoice.setLayout(new GridLayout(4, false));
		grpEInvoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		Label lblEInvoice = new Label(grpEInvoice, SWT.NONE);
		lblEInvoice.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblEInvoice.setText("Format");
		
		comboEInvoice = new CCombo(grpEInvoice, SWT.BORDER);
		addItemToCombo(comboEInvoice, EmbeddedProperties.VALUE_NULL, "- keine E-Rechnung -");
		addItemToCombo(comboEInvoice, EmbeddedProperties.VALUE_XRECHNUNG);
		addItemToCombo(comboEInvoice, EmbeddedProperties.VALUE_FACTURX_COMFORT);
		addItemToCombo(comboEInvoice, EmbeddedProperties.VALUE_ZUGFERD_V1_COMFORT);
		addItemToCombo(comboEInvoice, EmbeddedProperties.VALUE_ZUGFERD_V2_COMFORT);
		addItemToCombo(comboEInvoice, EmbeddedProperties.VALUE_ZUGFERD_V2_EN16931);
		comboEInvoice.addFocusListener(focusListenerCCombo);
		comboEInvoice.addSelectionListener(selectionListener);
		comboEInvoice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblNotice = new Label(grpEInvoice, SWT.NONE);
		lblNotice.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNotice.setToolTipText("Anzeige nach Erstellung der E-Rechnung");
		lblNotice.setText("Hinweis");
		
		txtNotice = new Text(grpEInvoice, SWT.BORDER);
		txtNotice.addFocusListener(focusListenerText);
		txtNotice.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblMailTo = new Label(grpEInvoice, SWT.NONE);
		lblMailTo.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMailTo.setText("per E-Mail an");
		
		txtMailTo = new Text(grpEInvoice, SWT.BORDER);
		txtMailTo.addFocusListener(focusListenerText);
		txtMailTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblUrl = new Label(grpEInvoice, SWT.NONE);
		lblUrl.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblUrl.setText("Portal/URL");
		
		txtUrl = new Text(grpEInvoice, SWT.BORDER);
		txtUrl.addFocusListener(focusListenerText);
		txtUrl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblBuyerId = new Label(grpEInvoice, SWT.NONE);
		lblBuyerId.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblBuyerId.setToolTipText("Vorgabe: Kundennummer");
		lblBuyerId.setText("Käufer ID");
		
		txtBuyerId = new Text(grpEInvoice, SWT.BORDER);
		txtBuyerId.addFocusListener(focusListenerText);
		txtBuyerId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblBuyerGlobalId = new Label(grpEInvoice, SWT.NONE);
		lblBuyerGlobalId.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblBuyerGlobalId.setToolTipText("Käufer, GlobalID + SchemeID lt. ISO/IEC 6523 (Vorgabe: GLN + schemeID: 0088)");
		lblBuyerGlobalId.setText("GlobalID, schemeID");
		
		Group grpGlobalId = new Group(grpEInvoice, SWT.NONE);
		grpGlobalId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		GridLayout gl_grpGlobalId = new GridLayout(2, false);
		gl_grpGlobalId.verticalSpacing = 0;
		gl_grpGlobalId.marginWidth = 0;
		gl_grpGlobalId.marginHeight = 0;
		grpGlobalId.setLayout(gl_grpGlobalId);
		
		txtBuyerGlobalId = new Text(grpGlobalId, SWT.BORDER);
		txtBuyerGlobalId.addFocusListener(focusListenerText);
		txtBuyerGlobalId.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		txtBuyerGlobalIdSchemeId = new Text(grpGlobalId, SWT.BORDER);
		txtBuyerGlobalIdSchemeId.addFocusListener(focusListenerText);
		GridData gd_txtBuyerGlobalIdSchemeId = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_txtBuyerGlobalIdSchemeId.widthHint = 40;
		txtBuyerGlobalIdSchemeId.setLayoutData(gd_txtBuyerGlobalIdSchemeId);
		
		Label lblBR13 = new Label(grpEInvoice, SWT.NONE);
		lblBR13.setToolTipText("");
		lblBR13.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblBR13.setText("Leitweg-ID (BT-10)");
		
		comboBT10 = new CCombo(grpEInvoice, SWT.BORDER);
		addItemToCombo(comboBT10, EmbeddedProperties.VALUE_NULL);
		addItemToCombo(comboBT10, EmbeddedProperties.VALUE_FIELD_CUSTREF);
		addItemToCombo(comboBT10, EmbeddedProperties.VALUE_FIELD_CUSTREF_LEFT);
		comboBT10.addFocusListener(focusListenerCCombo);
		comboBT10.addSelectionListener(selectionListener);
		comboBT10.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblBT13 = new Label(grpEInvoice, SWT.NONE);
		lblBT13.setToolTipText("");
		lblBT13.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblBT13.setText("Bestellnummer (BT-13)");

		comboBT13 = new CCombo(grpEInvoice, SWT.BORDER);
		addItemToCombo(comboBT13, EmbeddedProperties.VALUE_NULL);
		addItemToCombo(comboBT13, EmbeddedProperties.VALUE_FIELD_CUSTREF);
		addItemToCombo(comboBT13, EmbeddedProperties.VALUE_FIELD_CUSTREF_LEFT);
		addItemToCombo(comboBT13, EmbeddedProperties.VALUE_FIELD_ORDER_NAME);
		comboBT13.addFocusListener(focusListenerCCombo);
		comboBT13.addSelectionListener(selectionListener);
		comboBT13.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblBT11 = new Label(grpEInvoice, SWT.NONE);
		lblBT11.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblBT11.setText("Projektreferenz (BT-11)");
		
		comboBT11 = new CCombo(grpEInvoice, SWT.BORDER);
		addItemToCombo(comboBT11, EmbeddedProperties.VALUE_NULL);
		addItemToCombo(comboBT11, EmbeddedProperties.VALUE_FIELD_CUSTREF);
		addItemToCombo(comboBT11, EmbeddedProperties.VALUE_FIELD_CUSTREF_LEFT);
		comboBT11.addFocusListener(focusListenerCCombo);
		comboBT11.addSelectionListener(selectionListener);
		comboBT11.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label lblBT12 = new Label(grpEInvoice, SWT.NONE);
		lblBT12.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblBT12.setText("Vertragsnummer (BT-12)");
		
		comboBT12 = new CCombo(grpEInvoice, SWT.BORDER);
		addItemToCombo(comboBT12, EmbeddedProperties.VALUE_NULL);
		addItemToCombo(comboBT12, EmbeddedProperties.VALUE_FIELD_CUSTREF);
		addItemToCombo(comboBT12, EmbeddedProperties.VALUE_FIELD_CUSTREF_LEFT);
		comboBT12.addFocusListener(focusListenerCCombo);
		comboBT12.addSelectionListener(selectionListener);
		comboBT12.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
	
	// fire modify event when data changed
	public void addModifyListener(ModifyListener listener) {
		addListener(SWT.Modify, new TypedListener(listener));
	}
	public void removeModifyListener(ModifyListener listener) {
		removeListener(SWT.Modify, listener);
	}

	protected String getI18NLabel(String key) {
		if (key == null)
			return "";
		return i18n.getOrDefault(key, key);
	}
	protected void addItemToCombo(CCombo theCombo, String itemValue) {
		theCombo.add(getI18NLabel(itemValue));
		theCombo.setData(""+(theCombo.getItemCount()-1), itemValue);
	}
	protected void addItemToCombo(CCombo theCombo, String itemValue, String itemLabel) {
		theCombo.add(itemLabel);
		theCombo.setData(""+(theCombo.getItemCount()-1), itemValue);
	}
	protected String getComboValue(CCombo theCombo) {
		if (theCombo.getSelectionIndex() < 0) {
			return theCombo.getText();
		} else {
			String data = (String)theCombo.getData(""+theCombo.getSelectionIndex());
			if (EmbeddedProperties.VALUE_NULL.equals(data))
				return null;
			else
				return data;
		}
	}
	protected int getComboIndexOfValue(CCombo theCombo, String theValue) {
		if (theValue == null)
			theValue = EmbeddedProperties.VALUE_NULL;
		for (int i = 0; i < theCombo.getItemCount(); i++) {
			if (theValue.equals(theCombo.getData(""+i)))
				return i;
		}
		return -1;
	}
	protected void setOrSelectComboValue(CCombo theCombo, String theValue) {
		int idx = getComboIndexOfValue(theCombo, theValue);
		if (idx > 0) {
			theCombo.select(idx);
			theCombo.setEditable(false);
		} else {
			theCombo.select(0);
			theCombo.setEditable(theCombo.getItemCount() == 0 || theCombo.getItem(0).length() == 0);
			theCombo.setText(Util.defaultIfNull(theValue, ""));
		}
	}
	
	public void extractData(Text sourceField) {
		if (sourceField != null) {
			sourceField.setText(Util.defaultIfNull(theProperties.extract(sourceField.getText()), ""));
		} else {
			theProperties.clear();
		}
		txtInfo.setText(theProperties.getProperty(EmbeddedProperties.PROPERTY_INFO, "", ""));
		setOrSelectComboValue(comboEInvoice, theProperties.getProperty(EmbeddedProperties.PROPERTY_EINVOICE));
		txtNotice.setText(theProperties.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_NOTICE, "", ""));
		txtMailTo.setText(theProperties.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_MAILTO, "", ""));
		txtUrl.setText(theProperties.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_URL, "", ""));
		txtBuyerId.setText(theProperties.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_BUYER_ID, "", ""));
		txtBuyerGlobalId.setText(theProperties.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_BUYER_GLOBALID, "", ""));
		txtBuyerGlobalIdSchemeId.setText(theProperties.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_BUYER_GLOBALID_SCHEMEID, "", ""));
		setOrSelectComboValue(comboBT10, theProperties.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_BT10));
		setOrSelectComboValue(comboBT11, theProperties.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_BT11));
		setOrSelectComboValue(comboBT12, theProperties.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_BT12));
		setOrSelectComboValue(comboBT13, theProperties.getProperty(EmbeddedProperties.PROPERTY_EINVOICE_BT13));
		isDirty = false;
	}

	public void appendData(Text sourceField) {
		theProperties.clear();
		theProperties.setProperty(EmbeddedProperties.PROPERTY_INFO, txtInfo.getText());
		theProperties.setProperty(EmbeddedProperties.PROPERTY_EINVOICE, getComboValue(comboEInvoice));
		theProperties.setProperty(EmbeddedProperties.PROPERTY_EINVOICE_NOTICE, txtNotice.getText());
		theProperties.setProperty(EmbeddedProperties.PROPERTY_EINVOICE_MAILTO, txtMailTo.getText());
		theProperties.setProperty(EmbeddedProperties.PROPERTY_EINVOICE_URL, txtUrl.getText());
		theProperties.setProperty(EmbeddedProperties.PROPERTY_EINVOICE_BUYER_ID, txtBuyerId.getText());
		theProperties.setProperty(EmbeddedProperties.PROPERTY_EINVOICE_BUYER_GLOBALID, txtBuyerGlobalId.getText());
		theProperties.setProperty(EmbeddedProperties.PROPERTY_EINVOICE_BUYER_GLOBALID_SCHEMEID, txtBuyerGlobalIdSchemeId.getText());
		theProperties.setProperty(EmbeddedProperties.PROPERTY_EINVOICE_BT10, getComboValue(comboBT10));
		theProperties.setProperty(EmbeddedProperties.PROPERTY_EINVOICE_BT11, getComboValue(comboBT11));
		theProperties.setProperty(EmbeddedProperties.PROPERTY_EINVOICE_BT12, getComboValue(comboBT12));
		theProperties.setProperty(EmbeddedProperties.PROPERTY_EINVOICE_BT13, getComboValue(comboBT13));
		if (sourceField != null) {
			sourceField.setText(theProperties.appendTo(sourceField.getText()));
		}
	}
}
