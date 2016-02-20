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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.nebula.widgets.formattedtext.NumberFormatter;
import org.eclipse.nebula.widgets.formattedtext.PercentFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.dao.VoucherCategoriesDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Payment;
import com.sebulli.fakturama.model.Payment_;
import com.sebulli.fakturama.model.VoucherCategory;
import com.sebulli.fakturama.parts.converter.CategoryConverter;
import com.sebulli.fakturama.parts.converter.StringToCategoryConverter;
import com.sebulli.fakturama.resources.core.Icon;

/**
 * The payment editor
 * 
 */
public class PaymentEditor extends Editor<Payment> {

	// Editor's ID
    public static final String EDITOR_ID = "PaymentEditor";
    
//    private EventList<Payment> paymentListData;
//    private EventList<Account> categories;

    @Inject
    private PaymentsDAO paymentsDAO;

    @Inject
    private VoucherCategoriesDAO accountDAO;

	public static final String ID = "com.sebulli.fakturama.editors.paymentEditor";

	// This UniDataSet represents the editor's input 
	private Payment payment;

	// SWT widgets of the editor
	private Composite top;
	private Text textName;
	private Text textDescription;
	private FormattedText textDiscountValue;
	private FormattedText textDiscountDays;
	private FormattedText textNetDays;
	private Text textPaid;
	private Text textDepositPaid;
	private Text textUnpaid;
	private Combo comboCategory;

	// defines if the payment is newly created
	private boolean newPayment;
    private MPart part;

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
		 */

		// Always set the editor's data set to "undeleted"
		payment.setDeleted(Boolean.FALSE);

		// Set the payment data
        // ... done through databinding...

        // save the new or updated payment
        try {
            // at first, check the category for a new entry
            // (the user could have written a new one into the combo field)
            String testCat = comboCategory.getText();
            // if there's no category we can skip this step
            if(StringUtils.isNotBlank(testCat)) {
                VoucherCategory parentCategory = accountDAO.getOrCreateCategory(testCat, true);
                // parentCategory now has the last found Category
                payment.setCategory(parentCategory);
            }
            
            // we have to truncate the shipping value (because of calculations between gross and net)
            MathContext mc = new MathContext(16, RoundingMode.HALF_UP);
            BigDecimal val = BigDecimal.valueOf(payment.getDiscountValue()).round(mc).setScale(5, RoundingMode.HALF_UP);
            payment.setDiscountValue(val.doubleValue());
            payment = paymentsDAO.update(payment);
        }
        catch (FakturamaStoringException e) {
            log.error(e, "can't save the current Payment: " + payment.toString());
        }
		
		// If it is a new payment, add it to the payment list and
		// to the data base
		if (newPayment) {
			newPayment = false;
            String category = (String) part.getProperties().get(CallEditor.PARAM_CATEGORY);
            if(StringUtils.isNotEmpty(category)) {
                VoucherCategory newCat = accountDAO.findVoucherCategoryByName(category);
                payment.setCategory(newCat);
            }
			stdComposite.stdButton.setEnabled(true);
		}

		// Set the Editor's name to the payment name.
        part.setLabel(payment.getName());

		// Refresh the table view of all payments
        evtBroker.post(EDITOR_ID, "update");
        
        // reset dirty flag
        getMDirtyablePart().setDirty(false);
	}

	/**
	 * Initializes the editor. If an existing data set is opened, the local
	 * variable "payment" is set to this data set. If the editor is opened to
	 * create a new one, a new data set is created and the local variable
	 * "payment" is set to this one.
     * 
     * @param parent
     *            the parent control
     */
    @PostConstruct
	public void createPartControl(Composite parent) {
        Long objId = null;
        Payment stdPayment = null;
        long stdID = 1L;
        this.part = (MPart) parent.getData("modelElement");
        this.part.setIconURI(Icon.COMMAND_PAYMENT.getIconURI());
        String tmpObjId = (String) part.getProperties().get(CallEditor.PARAM_OBJ_ID);
        if (StringUtils.isNumeric(tmpObjId)) {
            objId = Long.valueOf(tmpObjId);
            // Set the editor's data set to the editor's input
            payment = paymentsDAO.findById(objId, true);
        }

		// test, if the editor is opened to create a new data set. This is,
		// if there is no input set.
		newPayment = (payment == null);

		// If new ..
		if (newPayment) {

			// Create a new data set
			payment = new Payment();
            String category = (String) part.getProperties().get(CallEditor.PARAM_CATEGORY);
            if(StringUtils.isNotEmpty(category)) {
                VoucherCategory newCat = accountDAO.findVoucherCategoryByName(category);
                payment.setCategory(newCat);
            }
			
			//T: Payment Editor: Part Name of a new payment
			part.setLabel(msg.mainMenuNewPayment);
            getMDirtyablePart().setDirty(true);
		}
		else {

			// Set the Editor's name to the payment name.
		    part.setLabel(payment.getName());
		}

		// Create the top Composite
		top = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(4).applyTo(top);

		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, ContextHelpConstants.PAYMENT_EDITOR);

		// Large payment label
		Label labelTitle = new Label(top, SWT.NONE);
		//T: Payment Editor: Title
		labelTitle.setText(msg.editorContactFieldPaymentName);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).span(4, 1).applyTo(labelTitle);
		makeLargeLabel(labelTitle);

		// Payment name
		Label labelName = new Label(top, SWT.NONE);
		labelName.setText(msg.commonFieldName);
		//T: Tool Tip Text
		labelName.setToolTipText(msg.editorPaymentNameTooltip);
		
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);
		textName = new Text(top, SWT.BORDER);
//		textName.setText(StringUtils.defaultString(payment.getName()));
		textName.setToolTipText(labelName.getToolTipText());

        bindModelValue(payment, textName, Payment_.name.getName(), 32);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(textName);

		// Payment category
		Label labelCategory = new Label(top, SWT.NONE);
		//T: Payment Editor - category
		labelCategory.setText(msg.commonFieldAccount);
		//T: Payment Editor - category Tool Tip Text
		labelCategory.setToolTipText(msg.editorPaymentAccountTooltip);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCategory);
		
        // Collect all category strings
        createCategoryCombo();
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(comboCategory);

		// Payment description
		Label labelDescription = new Label(top, SWT.NONE);
		labelDescription.setText(msg.commonFieldDescription);
		//T: Tool Tip Text
		labelDescription.setToolTipText(msg.editorVatDescriptionTooltip);
		
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDescription);
		textDescription = new Text(top, SWT.BORDER);
		textDescription.setToolTipText(labelDescription.getToolTipText());
        bindModelValue(payment, textDescription, Payment_.description.getName(), 64);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(textDescription);

		// Payment discount value
		Label labelDiscountValue = new Label(top, SWT.NONE);
		labelDiscountValue.setText(msg.editorPaymentDiscount);
		//T: Tool Tip Text
		labelDiscountValue.setToolTipText(msg.editorPaymentDiscountTooltip);

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDiscountValue);
		textDiscountValue = new FormattedText(top, SWT.BORDER | SWT.SINGLE);
		textDiscountValue.setFormatter(new PercentFormatter());
		textDiscountValue.getControl().setToolTipText(labelDiscountValue.getToolTipText());
        bindModelValue(payment, textDiscountValue, Payment_.discountValue.getName(), 12);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(textDiscountValue.getControl());

		// Payment days to pay the discount
		Label labelDiscountDays = new Label(top, SWT.NONE);
		//T: Label in the payment editor
		labelDiscountDays.setText(msg.editorPaymentDiscountDays);
		//T: Tool Tip Text
		labelDiscountDays.setToolTipText(msg.editorPaymentDiscountDaysTooltip);

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDiscountDays);
		textDiscountDays = new FormattedText(top, SWT.BORDER | SWT.SINGLE);
		textDiscountDays.setFormatter(new NumberFormatter());
		textDiscountDays.getControl().setToolTipText(labelDiscountDays.getToolTipText());
        bindModelValue(payment, textDiscountDays, Payment_.discountDays.getName(), 8);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(textDiscountDays.getControl());

		// Payment days to pay the net value
		Label labelNetDays = new Label(top, SWT.NONE);
		//T: Label in the payment editor
		labelNetDays.setText(msg.commonFieldNetDays);
		//T: Tool Tip Text
		labelNetDays.setToolTipText(msg.editorPaymentNetdaysTooltip);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelNetDays);
		textNetDays = new FormattedText(top, SWT.BORDER | SWT.SINGLE);
		textNetDays.setFormatter(new NumberFormatter());
		textNetDays.getControl().setToolTipText(labelNetDays.getToolTipText());
        bindModelValue(payment, textNetDays, Payment_.netDays.getName(), 8);
		GridDataFactory.fillDefaults().grab(true, false).span(3, 1).applyTo(textNetDays.getControl());

		// Label for the "paid" text message
		Label labelPaid = new Label(top, SWT.NONE);
		//T: Payment Editor: Label for the text paid
		labelPaid.setText(msg.editorPaymentPaidName);
		//T: Tool Tip Text
		labelPaid.setToolTipText(msg.editorPaymentPaidTooltip);		
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelPaid);

		// Create text field for the "paid" text message
		textPaid = new Text(top, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		textPaid.setToolTipText(labelPaid.getToolTipText());
        bindModelValue(payment, textPaid, Payment_.paidText.getName(), 500);
		GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 200).grab(true, true).applyTo(textPaid);

		// Label for the "depositpaid" text message
		Label labelDepositPaid = new Label(top, SWT.NONE);
		//T: Payment Editor: Label for the text paid
		labelDepositPaid.setText(msg.editorPaymentDepositName);
		//T: Tool Tip Text
		labelDepositPaid.setToolTipText(msg.editorPaymentDepositTooltip);
			
		GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDepositPaid);

		// Create text field for the "depositpaid" text message
		textDepositPaid = new Text(top, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		textDepositPaid.setToolTipText(labelDepositPaid.getToolTipText());
        bindModelValue(payment, textDepositPaid, Payment_.depositText.getName(), 500);
		GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 200).grab(true, true).applyTo(textDepositPaid);
		
		// Label for the "unpaid" text message
		Label labelUnpaid = new Label(top, SWT.NONE);
		//T: Payment Editor: Label for the text unpaid
		labelUnpaid.setText(msg.editorPaymentUnpaidName);
		//T: Tool Tip Text
		labelUnpaid.setToolTipText(msg.editorPaymentUnpaidTooltip);
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(labelUnpaid);

		// Create text field for "unpaid" text message
		textUnpaid = new Text(top, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
		textUnpaid.setToolTipText(labelUnpaid.getToolTipText());
        bindModelValue(payment, textUnpaid, Payment_.unpaidText.getName(), 500);
		GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 200).grab(true, true).applyTo(textUnpaid);

		// Empty label
		new Label(top, SWT.NONE);

		// Info label with the possible placeholders
		Label labelPlaceholderInfo1 = new Label(top, SWT.WRAP);
		//T: Label in the payment editor
		labelPlaceholderInfo1.setText(msg.editorPaymentPlaceholderInfo + ": <PAID.VALUE>, <PAID.DATE>");
		makeSmallLabel(labelPlaceholderInfo1);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).applyTo(labelPlaceholderInfo1);

		// Empty label
		new Label(top, SWT.NONE);

		// Info label with the possible placeholders
		Label labelPlaceholderInfo2 = new Label(top, SWT.WRAP);
		//T: Label in the payment editor
		labelPlaceholderInfo2.setText(msg.editorPaymentPlaceholderInfo + ": <DUE.DAYS>, <DUE.DATE>, <DUE.DISCOUNT.PERCENT>, <DUE.DISCOUNT.DAYS>, <DUE.DISCOUNT.VALUE>, <DUE.DISCOUNT.DATE>,\n" +
				"<BANK.ACCOUNT.HOLDER>, <BANK.ACCOUNT>, <BANK.ACCOUNT.CENSORED>, <BANK.IBAN>, <BANK.IBAN.CENSORED>, <BANK.BIC>, <BANK.CODE>,\n" +
				"<BANK.NAME>, <DEBITOR.BANK.ACCOUNT.HOLDER>, <DEBITOR.BANK.IBAN>, <DEBITOR.BANK.IBAN.CENSORED>, <DEBITOR.BANK.BIC>,\n" +
				"<DEBITOR.BANK.NAME>, <DEBITOR.MANDATREF>, <DOCUMENT.TOTAL>, <YOURCOMPANY.CREDITORID>");
		makeSmallLabel(labelPlaceholderInfo2);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(3, 1).applyTo(labelPlaceholderInfo2);

		// Create the composite to make this payment to the standard payment. 
		Label labelStd = new Label(top, SWT.NONE);
		//T: Label in the payment editor
		labelStd.setText(msg.commonLabelDefault);
		//T: Tool Tip Text
		labelStd.setToolTipText(msg.editorPaymentDefaultTooltip);

        // Get the ID of the standard entity from preferences
        try {
            stdID = defaultValuePrefs.getLong(getDefaultEntryKey());
            stdPayment = paymentsDAO.findById(stdID);
        } catch (NumberFormatException | NullPointerException e) {
            stdID = 1L;
            stdPayment = paymentsDAO.findById(stdID);
        }

		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelStd);
		//T: Payment Editor: Button description to make this as standard payment.
		stdComposite = new StdComposite(top, payment, stdPayment, msg.editorPaymentDefaultButtonName, 3);
		
		//T: Tool Tip Text
		stdComposite.setToolTipText(msg.editorPaymentDefaultButtonHint);
		
		// disable the Standard Button, if this is a new payment
		if (!newPayment)
			stdComposite.stdButton.setEnabled(true);

	}

    /**
     * creates the combo box for the VAT category
     */
    private void createCategoryCombo() {
        // Collect all category strings as a sorted Set
        final TreeSet<VoucherCategory> categories = new TreeSet<VoucherCategory>(new Comparator<VoucherCategory>() {
            @Override
            public int compare(VoucherCategory cat1, VoucherCategory cat2) {
                return cat1.getName().compareTo(cat2.getName());
            }
        });
        categories.addAll(accountDAO.findAll());

        comboCategory = new Combo(top, SWT.BORDER);
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
                return element instanceof VoucherCategory ? CommonConverter.getCategoryName((VoucherCategory)element, "") : null;
            }
        });

        UpdateValueStrategy paymentCatModel2Target = new UpdateValueStrategy();
        paymentCatModel2Target.setConverter(new CategoryConverter<VoucherCategory>(VoucherCategory.class));
        
        UpdateValueStrategy target2PaymentCatModel = new UpdateValueStrategy();
        target2PaymentCatModel.setConverter(new StringToCategoryConverter<VoucherCategory>(categories, VoucherCategory.class));
        bindModelValue(payment, comboCategory, Payment_.category.getName(), target2PaymentCatModel, paymentCatModel2Target);
    }

    @Override
    protected String getDefaultEntryKey() {
        return Constants.DEFAULT_PAYMENT;
    }
    
    @Override
    protected MDirtyable getMDirtyablePart() {
        return part;
    }
    
    @Override
    protected String getEditorID() {
    	return EDITOR_ID;
    }

    @Override
    protected Class<Payment> getModelClass() {
        return Payment.class;
    }
}
