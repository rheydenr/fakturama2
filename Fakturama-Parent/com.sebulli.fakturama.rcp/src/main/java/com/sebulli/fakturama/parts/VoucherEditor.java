/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2016 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.parts;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.cdatetime.CDateTime;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.javamoney.moneta.Money;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.VoucherCategoriesDAO;
import com.sebulli.fakturama.dao.VoucherItemsDAO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Voucher;
import com.sebulli.fakturama.model.VoucherCategory;
import com.sebulli.fakturama.model.VoucherItem;
import com.sebulli.fakturama.model.VoucherType;
import com.sebulli.fakturama.model.Voucher_;
import com.sebulli.fakturama.parts.converter.CategoryConverter;
import com.sebulli.fakturama.parts.converter.StringToCategoryConverter;
import com.sebulli.fakturama.parts.voucheritems.VoucherItemListBuilder;
import com.sebulli.fakturama.parts.voucheritems.VoucherItemListTable;
import com.sebulli.fakturama.parts.widget.formatter.MoneyFormatter;
import com.sebulli.fakturama.resources.core.Icon;

/**
 *
 */
public abstract class VoucherEditor extends Editor<Voucher>{
	
	public static final String PART_ID = "TEMP_ID";

	@Inject
	protected VoucherCategoriesDAO voucherCategoriesDAO;
	@Inject
	protected IPreferenceStore preferences;
	@Inject
	protected EHandlerService handlerService;
	@Inject
	protected ECommandService commandService;
	@Inject
	protected EPartService partService;
	protected MPart part;
	protected Composite top;
	protected Combo comboCategory;
	protected CDateTime dtDate;
	@Inject
	protected VoucherItemsDAO voucherItemsDAO;
	@Inject
	protected IEclipseContext context;
	protected Text textName;
	protected Text textNr;
	protected Text textDocumentNr;
	protected FormattedText textPaidValue;
	protected FormattedText textTotalValue;
	protected MonetaryAmount paidValue;
	protected MonetaryAmount totalValue = Money.of(Double.valueOf(0.0), DataUtils.getInstance().getDefaultCurrencyUnit());
	protected Button bPaidWithDiscount;
	protected Button bBook;
	protected VoucherItemListTable itemListTable;
	protected CurrencyUnit currencyUnit;
	protected boolean useGross;
	protected VoucherType voucherType;
	protected String customerSupplier = "-";

    // This UniDataSet represents the editor's input
    protected Voucher voucher;
    // defines, if the payment is new created
    protected boolean newVoucher;

    protected Label labelPaidValue;

	/**
	 * Get all items from the voucher
	 * 
	 * @return
	 * 		All voucher items
	 */
	protected List<VoucherItem> getVoucherItems() {
		return ((Voucher)voucher).getItems();
	}
	protected VoucherCategory getLastUsedCategory() {
	    return voucherCategoriesDAO.getLastUsedCategoryForExpenditure();
	}
	/**
	 * Creates the SWT controls for this workbench part
	 * 
	 * @param the
	 *            parent control
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	protected void createPartControl(Composite parent) {
	
	        // Get the some settings from the preference store
	        useGross = (preferences.getInt(Constants.PREFERENCES_DOCUMENT_USE_NET_GROSS) == DocumentSummary.ROUND_NET_VALUES);
	
	        // Create the top Composite
	        top = new Composite(parent, SWT.NONE);
	        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(top);
	
	        // Add context help reference 
	//      PlatformUI.getWorkbench().getHelpSystem().setHelp(top, helpID);
	
	        // Create the top Composite
	        Composite titlebar = new Composite(top, SWT.NONE);
	        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(titlebar);
	        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).span(2, 1).applyTo(titlebar);
	
	        // Large title
	        Label labelTitle = new Label(titlebar, SWT.NONE);
	        
	        //T: VoucherEditor - Title
	        labelTitle.setText(getEditorTitle());
	        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).applyTo(labelTitle);
	        makeLargeLabel(labelTitle);
	        
	        // The "book" label
	        bBook = new Button(titlebar, SWT.CHECK | SWT.RIGHT);
	        bBook.setSelection(!BooleanUtils.toBoolean(voucher.getDoNotBook()));
	//        bindModelValue(voucher, bBook, Voucher_.doNotBook.getName());
	
	        //T: Label voucher editor
	        bBook.setText(msg.voucherFieldBookName);
	        bBook.setToolTipText(msg.voucherFieldBookTooltip);
	        
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.BOTTOM).applyTo(bBook);
	        
	        // If the book check box is selected ...
	        bBook.addSelectionListener(new SelectionAdapter() {
	
	            public void widgetSelected(SelectionEvent e) {
	                if (!bBook.getSelection()) {
	                    //T: Dialog in the voucher editor to uncheck the book field 
	                    if (MessageDialog.openConfirm(parent.getShell(), msg.voucherDialogBookConfirmHeader,
	                            msg.voucherDialogBookConfirmWarning)) {
	                        bBook.setSelection(false);
	                        getMDirtyablePart().setDirty(true);
	                    }
	                }
	            }
	        });
	        
	        // Voucher category
	        Label labelCategory = new Label(top, SWT.NONE);
	
	        //T: Label in the voucher editor
	        labelCategory.setText(msg.commonFieldAccount);
	        labelCategory.setToolTipText(msg.commonFieldAccountTooltip);
	        
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCategory);
	
	        createCategoryCombo();
	        GridDataFactory.fillDefaults().grab(true, false).applyTo(comboCategory);
	
	        // Document date
	        Label labelDate = new Label(top, SWT.NONE);
	        //T: Label in the voucher editor
	        labelDate.setText(msg.commonFieldDate);
	        labelDate.setToolTipText(msg.voucherFieldDateTooltip);
	
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDate);
	
	        // Document date
	        dtDate = new CDateTime(top, CDT.BORDER | CDT.DROP_DOWN);
	        dtDate.setToolTipText(labelDate.getToolTipText());
	        dtDate.setFormat(CDT.DATE_MEDIUM);
	        GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT).applyTo(dtDate);
	        bindModelValue(voucher, dtDate, Voucher_.voucherDate.getName());
	
	        // Number
	        Label labelNr = new Label(top, SWT.NONE);
	        //T: Label in the voucher editor
	        labelNr.setText(msg.voucherFieldNumberName);
	        labelNr.setToolTipText(msg.voucherFieldNumberTooltip);
	
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelNr);
	        textNr = new Text(top, SWT.BORDER);
	        textNr.setToolTipText(labelNr.getToolTipText());
	        bindModelValue(voucher, textNr, Voucher_.voucherNumber.getName(), 32);
	        GridDataFactory.fillDefaults().grab(true, false).applyTo(textNr);
	
	        // Document number
	        Label labelDocumentNr = new Label(top, SWT.NONE);
	        //T: Label in the voucher editor
	        labelDocumentNr.setText(msg.voucherFieldDocumentnumberName);
	        labelDocumentNr.setToolTipText(msg.voucherFieldDocumentnumberTooltip);
	
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDocumentNr);
	        textDocumentNr = new Text(top, SWT.BORDER);
	        bindModelValue(voucher, textDocumentNr, Voucher_.documentNumber.getName(), 32);
	        textDocumentNr.setToolTipText(msg.voucherFieldDocumentnumberTooltip);
	        GridDataFactory.fillDefaults().grab(true, false).applyTo(textDocumentNr);
	
	        // Supplier name
	        Label labelName = new Label(top, SWT.NONE);
	
	        labelName.setText(customerSupplier);
	        labelName.setToolTipText(msg.voucherFieldCustomersupplierName + " " + customerSupplier.toLowerCase());
	        
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);
	        textName = new Text(top, SWT.BORDER);
	        bindModelValue(voucher, textName, Voucher_.name.getName(), 100);
	        textName.setToolTipText(labelName.getToolTipText());
	        GridDataFactory.fillDefaults().grab(true, false).applyTo(textName);
	
	        // Add the suggestion listener
	//      textName.addVerifyListener(new Suggestion(textName, getVouchers().getStrings("name")));
	
	
	/* * * * * * * * * * * * *  here the items list table is created * * * * * * * * * * * * */ 
	            VoucherItemListBuilder itemListBuilder = ContextInjectionFactory.make(VoucherItemListBuilder.class, context);
	            itemListTable = itemListBuilder
	                .withParent(top)
	                .withVoucher(voucher)
	//                .withNetGross(netgross)
	                .withUseGross(useGross)
	                .build();
	/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *  * * * * * * * * * * * */ 
	
	        // Create the bottom Composite
	        Composite bottom = new Composite(top, SWT.NONE);
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).span(2,1).applyTo(bottom);
	        
	        GridLayoutFactory.swtDefaults().numColumns(5).applyTo(bottom);
	
	        // The paid label
	        bPaidWithDiscount = new Button(bottom, SWT.CHECK | SWT.RIGHT);
	        //T: Mark a voucher, if the paid value is not equal to the total value.
	        bPaidWithDiscount.setText(msg.voucherFieldWithdiscountName);
	        bPaidWithDiscount.setToolTipText(msg.voucherFieldWithdiscountTooltip);
	        bindModelValue(voucher, bPaidWithDiscount, Voucher_.discounted.getName());
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(bPaidWithDiscount);
	
	        // If the bPaidWithDiscount check box is selected ...
	        bPaidWithDiscount.addSelectionListener(new SelectionAdapter() {
	
	            // check dirty
	            public void widgetSelected(SelectionEvent e) {
	                if (textPaidValue != null) {
	                    boolean selection = bPaidWithDiscount.getSelection();
	                    
	                    // If selected and the paid value was not already set,
	                    // use the total value
	                    if (selection && paidValue.isZero()) {
	                        paidValue = totalValue;
	                        textPaidValue.setValue(paidValue);
	                    }
	
	                    textPaidValue.getControl().setVisible(selection);
	                    labelPaidValue.setVisible(selection);
	                }
	            }
	        });
	
	        
	        labelPaidValue = new Label(bottom, SWT.NONE);
	        //T: Label in the voucher editor
	        labelPaidValue.setText(msg.voucherFieldPaidvalueName + ":");
	        labelPaidValue.setToolTipText(msg.voucherFieldPaidvalueTooltip);
	        labelPaidValue.setVisible(bPaidWithDiscount.getSelection());
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelPaidValue);
	
	        paidValue = Money.of(voucher.getPaidValue(), currencyUnit);
	
	        textPaidValue = new FormattedText(bottom, SWT.BORDER | SWT.RIGHT);
	        textPaidValue.setFormatter(new MoneyFormatter());
	        textPaidValue.getControl().setVisible(bPaidWithDiscount.getSelection());
	        textPaidValue.getControl().setToolTipText(labelPaidValue.getToolTipText());
	        bindModelValue(voucher, textPaidValue, Voucher_.paidValue.getName(), 32);
	        GridDataFactory.swtDefaults().hint(80, SWT.DEFAULT).align(SWT.END, SWT.CENTER).applyTo(textPaidValue.getControl());
	
	        // Total value
	        Label labelTotalValue = new Label(bottom, SWT.NONE);
	        //T: Label in the voucher editor
	        labelTotalValue.setText(msg.voucherFieldTotalvalueName + ":");
	        //T: Tool Tip Text
	        labelTotalValue.setToolTipText(msg.voucherFieldTotalvalueTooltip);
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelTotalValue);
	
	        totalValue = Money.of(voucher.getTotalValue(), currencyUnit);
	
	        textTotalValue = new FormattedText(bottom, SWT.BORDER | SWT.RIGHT);
	        textTotalValue.setFormatter(new MoneyFormatter());
	        textTotalValue.getControl().setEditable(false);
	        textTotalValue.getControl().setToolTipText(labelTotalValue.getToolTipText());
	        bindModelValue(voucher, textTotalValue, Voucher_.totalValue.getName(), 32);
	        GridDataFactory.swtDefaults().hint(80, SWT.DEFAULT).align(SWT.END, SWT.CENTER).applyTo(textTotalValue.getControl());
	    }
	/**
	 * @return
	 */
	protected String getEditorTitle() {
		return "VoucherEditor";
	}

    /**
     * creates the combo box for the VAT category
     */
    void createCategoryCombo() {
        // Collect all category strings as a sorted Set
        final TreeSet<VoucherCategory> categories = new TreeSet<VoucherCategory>(new Comparator<VoucherCategory>() {
            @Override
            public int compare(VoucherCategory cat1, VoucherCategory cat2) {
                return cat1.getName().compareTo(cat2.getName());
            }
        });
        categories.addAll(voucherCategoriesDAO.findAll());

        comboCategory = new Combo(top, SWT.BORDER);
        comboCategory.setToolTipText(msg.commonFieldAccountTooltip);
        
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

        UpdateValueStrategy vatCatModel2Target = new UpdateValueStrategy();
        vatCatModel2Target.setConverter(new CategoryConverter<VoucherCategory>(VoucherCategory.class));
        
        UpdateValueStrategy target2VatcatModel = new UpdateValueStrategy();
        target2VatcatModel.setConverter(new StringToCategoryConverter<VoucherCategory>(categories, VoucherCategory.class));
        bindModelValue(voucher, comboCategory, Voucher_.account.getName(), target2VatcatModel, vatCatModel2Target);
    }
	/**
	 * Creates a new voucher
	 * 
	 * @param input
	 * 	The editors input
	 * @return
	 * 	The created voucher
	 */
	public Voucher createNewVoucher() {
		Voucher newExpenditure = modelFactory.createVoucher();
		newExpenditure.setVoucherType(getVoucherType());
		newExpenditure.setPaidValue(Double.valueOf(0.0));
		newExpenditure.setTotalValue(Double.valueOf(0.0));
	    return newExpenditure;
	}
	
	/**
	 * @return
	 */
	protected abstract VoucherType getVoucherType();
	
	/**
	 * Add a voucher to the list of all vouchers
	 * 
	 * @param voucher
	 * 	The new voucher to add
	 * @return
	 *  A Reference to the added voucher
	 */
	public Voucher addVoucher(Voucher pVoucher) {
		try {
	        voucher = getModelRepository().save(pVoucher);
	    } catch (FakturamaStoringException e) {
	        log.error(e);
	    }
		return voucher;
	}
	
	/**
	 * @return
	 */
	protected abstract AbstractDAO<Voucher> getModelRepository();

	/**
	 * @return "Customer" or "Supplier"
	 */
	protected abstract String getCustomerSupplierString();
	
	/**
	 * Creates a new array for voucher items
	 * 
	 * @return
	 * 	Array with all voucher items
	 */
	public VoucherItem createNewVoucherItems() {
		return modelFactory.createVoucherItem();
	}
	/**
	 * Updates a voucher
	 * 
	 * @param voucher
	 * 		The voucher to update
	 * @return 
	 */
	public Voucher updateVoucher(Voucher pVoucher) {
		try {
	        voucher = getModelRepository().update(pVoucher);
	    } catch (FakturamaStoringException e) {
	        log.error(e);
	    }
		return voucher;
	}
	/**
	 * Initializes the editor. If an existing data set is opened, the local
	 * variable "voucher" is set to this data set. If the editor is opened to
	 * create a new one, a new data set is created and the local variable
	 * "voucher" is set to this one.
	 * 
	 * @param parent
	 *            The editor's parent Composite
	 */
	@PostConstruct
	public void init(Composite parent) {
	    this.part = (MPart) parent.getData("modelElement");
	    this.part.setIconURI(Icon.COMMAND_EXPENDITURE.getIconURI());
	    this.currencyUnit = DataUtils.getInstance().getCurrencyUnit(LocaleUtil.getInstance().getCurrencyLocale());
	
	    String tmpObjId = (String) part.getProperties().get(CallEditor.PARAM_OBJ_ID);
	    if (StringUtils.isNumeric(tmpObjId)) {
	        Long objId = Long.valueOf(tmpObjId);
	        // Set the editor's data set to the editor's input
	        this.voucher = getModelRepository().findById(objId);
	    }
	    String tmpVoucherType = (String) part.getProperties().get(CallEditor.PARAM_VOUCHERTYPE);
	    if(tmpVoucherType != null) {
	    	voucherType = VoucherType.getByName(tmpVoucherType);
	    }
	
	    // test if the editor is opened to create a new data set. This is,
	    // if there is no input set.
	    newVoucher = (voucher == null);
	
	    // If new ..
	    if (newVoucher) {
	
	        // Create a new data set
	        voucher = createNewVoucher();
	        
	        //T: Voucher Editor: Name of a new Voucher
	        part.setLabel(msg.editorVoucherHeader);
	        
	        /*
	         * Since a newly created voucher doesn't has a unique name we have to distinguish the editor parts
	         * another way. Therefore we use a timestamp. This is necessary while handling the item change event.
	         */
	        part.getProperties().put(PART_ID, java.time.LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
	
	        // Use the last category
	        VoucherCategory lastCategory = getLastUsedCategory();
	        if(lastCategory != null) {
	            voucher.setAccount(lastCategory);
	        }
	    }
	    else {
	
	        // Set the Editor's name to the voucher name.
	        part.setLabel(voucher.getName());
	        part.getProperties().put(PART_ID, voucher.getName());
	    }
	    customerSupplier = getCustomerSupplierString();
	    createPartControl(parent);
	}

}
