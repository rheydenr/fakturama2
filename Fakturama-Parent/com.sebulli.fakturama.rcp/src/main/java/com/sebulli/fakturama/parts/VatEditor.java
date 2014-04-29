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

import static com.sebulli.fakturama.Translate._;

import java.util.Comparator;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.xml.crypto.Data;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.sebulli.fakturama.dao.VatCategoriesDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.VATCategory;

/**
 * The VAT editor
 * 
 * @author Ralf Heydenreich
 */
public class VatEditor extends Editor<VAT> {
	
	@Inject
	protected VatsDAO vatDao;

	@Inject
	protected VatCategoriesDAO vatCategoriesDAO;
	
	// Editor's ID
	public static final String ID = "com.sebulli.fakturama.editors.vatEditor";

//	// This UniDataSet represents the editor's input 
//	private DataSetVAT vat;

	// SWT widgets of the editor
	private Composite top;
	private Text textName;
	private Text textDescription;
	private Text textValue;
	private Combo comboCategory;

	// defines, if the payment is new created
	private boolean newVat;

	private MPart part;
	private VAT editorVat = null;
	

//	/**
//	 * Constructor
//	 * 
//	 * Associate the table view with the editor
//	 */
//	public VatEditor() {
//		tableViewID = ViewVatTable.ID;
//		editorID = "vat";
//	}
//
//	/**
//	 * Saves the contents of this part
//	 * 
//	 * @param monitor
//	 *            Progress monitor
//	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
//	 */
//	@Override
//	public void doSave(IProgressMonitor monitor) {
//
//		/*
//		 * the following parameters are not saved: 
//		 * - id (constant)
//		 */
//
//		// Always set the editor's data set to "undeleted"
//		vat.setBooleanValueByKey("deleted", false);
//
//		// Set the payment data
//		vat.setStringValueByKey("name", textName.getText());
//		vat.setStringValueByKey("category", comboCategory.getText());
//		vat.setStringValueByKey("description", textDescription.getText());
//		vat.setDoubleValueByKey("value", DataUtils.StringToDouble(textValue.getText() + "%"));
//
//		// If it is a new VAT, add it to the VAT list and
//		// to the data base
//		if (newVat) {
//			vat = Data.INSTANCE.getVATs().addNewDataSet(vat);
//			newVat = false;
//			stdComposite.stdButton.setEnabled(true);
//		}
//		// If it's not new, update at least the data base
//		else {
//			Data.INSTANCE.getVATs().updateDataSet(vat);
//		}
//
//		// Set the Editor's name to the payment name.
//		setPartName(vat.getStringValueByKey("name"));
//
//		// Refresh the table view of all payments
//		refreshView();
//		checkDirty();
//	}
//
	/**
	 * There is no saveAs function
	 */
//	@Override
	public void doSaveAs() {
	}

//	/**
//	 * Initializes the editor. If an existing data set is opened, the local
//	 * variable "vat" is set to This data set. If the editor is opened to create
//	 * a new one, a new data set is created and the local variable "vat" is set
//	 * to this one.
//	 * 
//	 * @param input
//	 *            The editor's input
//	 * @param site
//	 *            The editor's site
//	 */
//	@Override
//	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
//
//		// Set the site and the input
//		setSite(site);
//		setInput(input);
//
//		// Set the editor's data set to the editor's input
//		vat = (DataSetVAT) ((UniDataSetEditorInput) input).getUniDataSet();
//
//		// test, if the editor is opened to create a new data set. This is,
//		// if there is no input set.
//		newVat = (vat == null);
//
//		// If new ..
//		if (newVat) {
//
//			// Create a new data set
//			vat = new DataSetVAT(((UniDataSetEditorInput) input).getCategory());
//
//			//T: VAT Editor: Part Name of a new VAT Entry
//			setPartName(_("New TAX Rate"));
//
//		}
//		else {
//
//			// Set the Editor's name to the payment name.
//			setPartName(vat.getStringValueByKey("name"));
//		}
//	}
//
//	/**
//	 * Returns whether the contents of this part have changed since the last
//	 * save operation
//	 * 
//	 * @see org.eclipse.ui.part.EditorPart#isDirty()
//	 */
//	@Override
//	public boolean isDirty() {
//		/*
//		 * the following parameters are not checked:
//		 *  - id (constant)
//		 */
//
//		if (vat.getBooleanValueByKey("deleted")) { return true; }
//		if (newVat) { return true; }
//
//		if (!vat.getStringValueByKey("name").equals(textName.getText())) { return true; }
//		if (!vat.getStringValueByKey("description").equals(textDescription.getText())) { return true; }
//		if (!DataUtils.DoublesAreEqual(vat.getDoubleValueByKey("value"), DataUtils.StringToDouble(textValue.getText() + "%"))) { return true; }
//		if (!vat.getStringValueByKey("category").equals(comboCategory.getText())) { return true; }
//
//		return false;
//	}
//
//	/**
//	 * Returns whether the "Save As" operation is supported by this part.
//	 * 
//	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
//	 * @return False, SaveAs is not allowed
//	 */
//	@Override
//	public boolean isSaveAsAllowed() {
//		return false;
//	}

	/**
	 * Creates the SWT controls for this workbench part
	 * 
	 * @param the
	 *            parent control
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	@PostConstruct
	public void createPartControl(Composite parent) {
		Long objId = null;
		this.part = (MPart) parent.getData("modelElement");
		String tmpObjId = (String) part.getContext().get("com.sebulli.fakturama.rcp.editor.objId");
		if(StringUtils.isNumeric(tmpObjId)) {
			objId = Long.valueOf(tmpObjId);
		// Set the editor's data set to the editor's input
			editorVat = vatDao.findById(objId);
		}

		// test, if the editor is opened to create a new data set. This is,
		// if there is no input set.
		newVat = (editorVat == null);

		// If new ..
		if (newVat) {

			// Create a new data set
//			vat = new DataSetVAT(((UniDataSetEditorInput) input).getCategory());

			//T: VAT Editor: Part Name of a new VAT Entry
//			setPartName(_("New TAX Rate"));
			part.setLabel(_("New TAX Rate"));

		}
		else {

			// Set the Editor's name to the payment name.
//			setPartName(vat.getStringValueByKey("name"));
			part.setLabel(editorVat.getName());
		}

		// Create the top Composite
		top = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(top);
//
//		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, ContextHelpConstants.VAT_EDITOR);

		// Large VAT label
		Label labelTitle = new Label(top, SWT.NONE);
		//T: VAT Editor: Title VAT Entry
		labelTitle.setText(_("TAX Rate"));
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).span(2, 1).applyTo(labelTitle);
		makeLargeLabel(labelTitle);

		// Name of the VAT
		Label labelName = new Label(top, SWT.NONE);
		labelName.setText(_("Name"));
		//T: Tool Tip Text
		labelName.setToolTipText(_("Name of the tax rate. This is also the identifier used by the shop system."));

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);
		textName = new Text(top, SWT.BORDER);
		textName.setText(editorVat.getName());
		textName.setToolTipText(labelName.getToolTipText());
		superviceControl(textName, 64);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(textName);

		// Category of the VAT
		Label labelCategory = new Label(top, SWT.NONE);
		labelCategory.setText(_("Category"));
		//T: Tool Tip Text
		labelCategory.setToolTipText(_("You can set a category to classify the tax rates"));

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCategory);

		comboCategory = new Combo(top, SWT.BORDER);
//		comboCategory.setText(editorVat.getCategory());
		comboCategory.setToolTipText(labelCategory.getToolTipText());
		superviceControl(comboCategory);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(comboCategory);

		// Collect all category strings
		TreeSet<VATCategory> categories = new TreeSet<VATCategory>(new Comparator<VATCategory>() {
			@Override
			public int compare(VATCategory o1, VATCategory o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		categories.addAll(vatCategoriesDAO.findAll());

		// Add all category strings to the combo
		for (VATCategory category : categories) {
			comboCategory.add(category.getName());
		}

		
		// The description
		Label labelDescription = new Label(top, SWT.NONE);
		labelDescription.setText(_("Description"));
		//T: Tool Tip Text
		labelDescription.setToolTipText(_("The description is the text used in the documents"));

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDescription);
		textDescription = new Text(top, SWT.BORDER);
		textDescription.setText(editorVat.getDescription());
		textDescription.setToolTipText(labelDescription.getToolTipText());
		superviceControl(textDescription, 250);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(textDescription);

		// The value
		Label labelValue = new Label(top, SWT.NONE);
		labelValue.setText(_("Value"));
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelValue);
		textValue = new Text(top, SWT.BORDER);
		textValue.setText(DataUtils.DoubleToFormatedPercent(editorVat.getTaxValue()));
		superviceControl(textValue, 16);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(textValue);

		// Create the composite to make this payment to the standard payment. 
		Label labelStdVat = new Label(top, SWT.NONE);
		labelStdVat.setText(_("Standard"));
		//T: Tool Tip Text
		labelStdVat.setToolTipText(_("Name of the tax rate that is the standard"));

		VAT stdVat = null;
		int stdID = 0;

		// Get the ID of the standard unidataset
		try {
			// TODO check if 1 is a valid default ID
			stdID = defaultValuePrefs.getInt("standardvat", 1);
			stdVat = vatDao.findById(stdID);
		}
		catch (NumberFormatException | NullPointerException e) {
			stdID = 0;
		}

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelStdVat);
		//T: VAT Editor: Button description to make this as standard VAT.
		stdComposite = new StdComposite(top, editorVat, stdVat, _("This TAX Rate"), 1);
		//T: Tool Tip Text
		stdComposite.setToolTipText(_("Make this tax rate to the standard"));

		// Disable the Standard Button, if this is a new VAT
		if (!newVat)
			stdComposite.stdButton.setEnabled(true);


	}
	
	/**
	 * Set the focus to the top composite.
	 * 
	 * @see com.sebulli.fakturama.editors.Editor#setFocus()
	 */
	@Override
	public void setFocus() {
		if(top != null) 
			top.setFocus();
	}

	@Override
	protected MDirtyable getMDirtyablePart() {
		// TODO Auto-generated method stub
		return null;
	}

}
