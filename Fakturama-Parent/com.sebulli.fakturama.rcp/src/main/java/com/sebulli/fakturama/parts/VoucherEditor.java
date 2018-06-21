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

import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.fieldassist.TextContentAdapter;
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
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.javamoney.moneta.Money;
import org.osgi.service.event.Event;

import com.sebulli.fakturama.calculate.VoucherSummaryCalculator;
import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.VoucherCategoriesDAO;
import com.sebulli.fakturama.dao.VoucherItemsDAO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.dto.VoucherItemDTO;
import com.sebulli.fakturama.dto.VoucherSummary;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.CategoryComparator;
import com.sebulli.fakturama.model.IEntity;
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

/**
 * Editor for various kinds of vouchers. Has to be overwritten by concrete implementations.
 */
public abstract class VoucherEditor extends Editor<Voucher>{
	
	public static final String PART_ID = "TEMP_ID";
	public static final String EDITOR_ID = "VoucherEditor";

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
	protected MonetaryAmount paidValue = Money.of(Double.valueOf(0.0), DataUtils.getInstance().getDefaultCurrencyUnit());
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
	        bBook.setText(msg.editorVoucherFieldBookName);
	        bBook.setToolTipText(msg.editorVoucherFieldBookTooltip);
	        
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.BOTTOM).applyTo(bBook);
	        
	        // If the book check box is selected ...
	        bBook.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { 
	                if (!bBook.getSelection()) {
	                    //T: Dialog in the voucher editor to uncheck the book field 
	                    if (MessageDialog.openConfirm(parent.getShell(), msg.editorVoucherDialogBookConfirmHeader,
	                            msg.editorVoucherDialogBookConfirmWarning)) {
	                        bBook.setSelection(false);
	                        getMDirtyablePart().setDirty(true);
	                    }
	                }
	        }));
	        
	        // Voucher category
	        Label labelCategory = new Label(top, SWT.NONE);
	
	        //T: Label in the voucher editor
	        labelCategory.setText(msg.commonFieldAccount);
	        labelCategory.setToolTipText(msg.commonFieldAccountTooltip);
	        
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCategory);
	
	        comboCategory = new Combo(top, SWT.BORDER);
	        comboCategory.setToolTipText(msg.commonFieldAccountTooltip);
//	        fillAndBindCategoryCombo();
	        GridDataFactory.fillDefaults().grab(true, false).applyTo(comboCategory);
	
	        // Document date
	        Label labelDate = new Label(top, SWT.NONE);
	        //T: Label in the voucher editor
	        labelDate.setText(msg.commonFieldDate);
	        labelDate.setToolTipText(msg.editorVoucherFieldDateTooltip);
	
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDate);
	
	        // Document date
	        dtDate = new CDateTime(top, CDT.BORDER | CDT.DROP_DOWN);
	        dtDate.setToolTipText(labelDate.getToolTipText());
	        dtDate.setFormat(CDT.DATE_MEDIUM);
	        GridDataFactory.swtDefaults().hint(150, SWT.DEFAULT).applyTo(dtDate);
	
	        // Number
	        Label labelNr = new Label(top, SWT.NONE);
	        //T: Label in the voucher editor
	        labelNr.setText(msg.editorVoucherFieldNumberName);
	        labelNr.setToolTipText(msg.editorVoucherFieldNumberTooltip);
	
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelNr);
	        textNr = new Text(top, SWT.BORDER);
	        textNr.setToolTipText(labelNr.getToolTipText());
	        GridDataFactory.fillDefaults().grab(true, false).applyTo(textNr);
	
	        // Document number
	        Label labelDocumentNr = new Label(top, SWT.NONE);
	        //T: Label in the voucher editor
	        labelDocumentNr.setText(msg.editorVoucherFieldDocumentnumberName);
	        labelDocumentNr.setToolTipText(msg.editorVoucherFieldDocumentnumberTooltip);
	
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDocumentNr);
	        textDocumentNr = new Text(top, SWT.BORDER);
	        textDocumentNr.setToolTipText(msg.editorVoucherFieldDocumentnumberTooltip);
	        GridDataFactory.fillDefaults().grab(true, false).applyTo(textDocumentNr);
	
	        // Supplier name
	        Label labelName = new Label(top, SWT.NONE);
	
	        labelName.setText(customerSupplier);
	        labelName.setToolTipText(MessageFormat.format(msg.editorVoucherFieldCustomersupplierName, getCustomerSupplierString()));
	        
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);
	        textName = new Text(top, SWT.BORDER);
	        textName.setToolTipText(labelName.getToolTipText());
	        
	        // Add the suggestion listener
	        String[] nameProposals = getNameProposals();
	        if(nameProposals.length > 0) {
		        ControlDecoration decoration = new ControlDecoration(textName, SWT.LEFT | SWT.TOP);
		        Image hintImage = FieldDecorationRegistry.getDefault()
		        		.getFieldDecoration(FieldDecorationRegistry.DEC_CONTENT_PROPOSAL).getImage();
		        		decoration.setImage(hintImage);	        
		        decoration.setImage(hintImage);
		        decoration.setDescriptionText(msg.editorVoucherFieldCustomersupplierHint);
		        decoration.setShowOnlyOnFocus(true);
		        decoration.setShowHover(true);
	        	new AutoCompleteField(textName, new TextContentAdapter(), nameProposals);
	        }
	        
	        GridDataFactory.fillDefaults().grab(true, false).applyTo(textName);	
	
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
	        bPaidWithDiscount.setText(msg.editorVoucherFieldWithdiscountName);
	        bPaidWithDiscount.setToolTipText(msg.editorVoucherFieldWithdiscountTooltip);
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(bPaidWithDiscount);
	
	        // If the bPaidWithDiscount check box is selected ...
	        bPaidWithDiscount.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { 
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
	        }));
	
	        
	        labelPaidValue = new Label(bottom, SWT.NONE);
	        //T: Label in the voucher editor
	        labelPaidValue.setText(msg.editorVoucherFieldPaidvalueName + ":");
	        labelPaidValue.setToolTipText(msg.editorVoucherFieldPaidvalueTooltip);
	        labelPaidValue.setVisible(bPaidWithDiscount.getSelection());
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelPaidValue);
	
	        paidValue = Money.of(java.util.Optional.ofNullable(voucher.getPaidValue()).orElse(Double.valueOf(0.0)), currencyUnit);
	
	        textPaidValue = new FormattedText(bottom, SWT.BORDER | SWT.RIGHT);
	        textPaidValue.setFormatter(ContextInjectionFactory.make(MoneyFormatter.class, context));
	        textPaidValue.getControl().setVisible(bPaidWithDiscount.getSelection());
	        textPaidValue.getControl().setToolTipText(labelPaidValue.getToolTipText());
	        GridDataFactory.swtDefaults().hint(80, SWT.DEFAULT).align(SWT.END, SWT.CENTER).applyTo(textPaidValue.getControl());
	
	        // Total value
	        Label labelTotalValue = new Label(bottom, SWT.NONE);
	        //T: Label in the voucher editor
	        labelTotalValue.setText(msg.editorVoucherFieldTotalvalueName + ":");
	        //T: Tool Tip Text
	        labelTotalValue.setToolTipText(msg.editorVoucherFieldTotalvalueTooltip);
	        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelTotalValue);
	
	        totalValue = Money.of(java.util.Optional.ofNullable(voucher.getTotalValue()).orElse(Double.valueOf(0.0)), currencyUnit);
	
	        textTotalValue = new FormattedText(bottom, SWT.BORDER | SWT.RIGHT);
	        textTotalValue.setFormatter(ContextInjectionFactory.make(MoneyFormatter.class, context));
	        textTotalValue.getControl().setEditable(false);
	        textTotalValue.getControl().setToolTipText(labelTotalValue.getToolTipText());
	        GridDataFactory.swtDefaults().hint(80, SWT.DEFAULT).align(SWT.END, SWT.CENTER).applyTo(textTotalValue.getControl());
	        
	        bindModel();
	    }
	
	@Override
	protected void bindModel() {
		part.getTransientData().put(BIND_MODE_INDICATOR, Boolean.TRUE);

		fillAndBindCategoryCombo();
        bindModelValue(voucher, dtDate, Voucher_.voucherDate.getName());
        bindModelValue(voucher, textNr, Voucher_.voucherNumber.getName(), 32);
        bindModelValue(voucher, textDocumentNr, Voucher_.documentNumber.getName(), 32);
        bindModelValue(voucher, textName, Voucher_.name.getName(), 100);
        bindModelValue(voucher, bPaidWithDiscount, Voucher_.discounted.getName());
        bindModelValue(voucher, textPaidValue, Voucher_.paidValue.getName(), 32);
        bindModelValue(voucher, textTotalValue, Voucher_.totalValue.getName(), 32);
        
		part.getTransientData().remove(BIND_MODE_INDICATOR);
	}
	
		/**
		 * Returns the title for the voucher editor (e.g., expenditure, receipt voucher).
		 * 
		 * @return the title string
		 */
		abstract protected String getEditorTitle();

    /**
     * creates the combo box for the {@link VoucherCategory}
     */
    private void fillAndBindCategoryCombo() {
        // Collect all category strings as a sorted Set
        final TreeSet<VoucherCategory> categories = new TreeSet<VoucherCategory>(new CategoryComparator<>());
        categories.addAll(voucherCategoriesDAO.findAll());
        
        ComboViewer viewer = new ComboViewer(comboCategory);
        viewer.setContentProvider(new ArrayContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                return categories.toArray();
            }
        });
        
        // Add all categories to the combo
        VoucherCategory tmpAccount = voucher.getAccount();
        viewer.setInput(categories);
        viewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return element instanceof VoucherCategory ? CommonConverter.getCategoryName((VoucherCategory)element, "") : null;
            }
        });
        voucher.setAccount(tmpAccount);

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
		Voucher newVoucher = modelFactory.createVoucher();
		newVoucher.setVoucherType(getVoucherType());
		newVoucher.setPaidValue(Double.valueOf(0.0));
		newVoucher.setTotalValue(Double.valueOf(0.0));
	    return newVoucher;
	}
	
	/**
	 * @return
	 */
	protected abstract VoucherType getVoucherType();
	
	/**
	 * Gets the model repository.
	 *
	 * @return the model repository
	 */
	protected abstract AbstractDAO<Voucher> getModelRepository();

	/**
	 * @return "Customer" or "Supplier"
	 */
	protected abstract String getCustomerSupplierString();
	
	/**
	 * Creates a new voucher items
	 * 
	 * @return
	 * 	Array with all voucher items
	 */
	public VoucherItem createNewVoucherItem() {
		return modelFactory.createVoucherItem();
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
	    this.part.setIconURI(getEditorIconURI());
	    this.currencyUnit = DataUtils.getInstance().getDefaultCurrencyUnit();
	
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
	
	abstract protected String getEditorIconURI();
	
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
	        voucher.setDeleted(false);

	        // at first, check the category for a new entry
            // (the user could have written a new one into the combo field)
            String testCat = comboCategory.getText();
            // if there's no category we can skip this step
            if(StringUtils.isNotBlank(testCat)) {
                VoucherCategory account = voucherCategoriesDAO.getOrCreateCategory(testCat, true);
                // parentCategory now has the last found Category
                voucher.setAccount(account);
            }

	        // Set all the items
	        List<VoucherItem> items = itemListTable.getVoucherItemsListData()
	            .stream()
	            .map(dto -> dto.getVoucherItem())
	            /*.sorted(Comparator.comparing(VoucherItem::getId))*/
	            .collect(Collectors.toList());
	        voucher.setItems(new ArrayList<>(items));
	        
	        // delete removed items
	        for (IEntity expenditureItem : itemListTable.getMarkedForDeletion()) {
	            try {
	                voucherItemsDAO.save((VoucherItem)expenditureItem);
	            } catch (FakturamaStoringException e) {
	                log.error(e);
	            }
	        }

	       // The the voucher was paid with a discount, use the paid value
	        if (bPaidWithDiscount.getSelection()) {
	            voucher.setPaidValue((Double) textPaidValue.getValue());
	        }
	        // else use the total value
	        else {
	            voucher.setPaidValue((Double) textTotalValue.getValue());
	        }
	
	      // The selection "book" is inverted
	      voucher.setDoNotBook(!bBook.getSelection());

		try {
			voucher = getModelRepository().update(voucher);
		} catch (FakturamaStoringException e) {
			log.error(e);
		}

	      // Set the Editor's name to the voucher name.
	      this.part.setLabel(voucher.getName());
	
	      // Refresh the table view of all vouchers
	      evtBroker.post(getEditorId(), Editor.UPDATE_EVENT);
	      
	        bindModel();

	      // reset dirty flag
	      getMDirtyablePart().setDirty(false);
	    }
	
	/**
	 * @return
	 */
	protected abstract String getEditorId();
	/**
	 * This method is for setting the dirty state to <code>true</code>. This
	 * happens if e.g. the items list has changed. (could be sent from
	 * DocumentListTable)
	 */
	@Inject
	@org.eclipse.e4.core.di.annotations.Optional
	protected void handleItemChanged(@UIEventTopic(EDITOR_ID + "/itemChanged") Event event) {
	    if (event != null) {
	        // the event has already all given params in it since we created them as Map
	        String targetDocumentName = (String) event.getProperty(DocumentEditor.DOCUMENT_ID);
	        // at first we have to check if the message is for us
	        String voucherTempId =  part.getProperties().get(PART_ID); 
	        if (!StringUtils.equals(targetDocumentName, voucherTempId)) {
	            // if not, silently ignore this event
	            return;
	        }
	        // (re)calculate summary
	        // TODO check if this has to be done in a synchronous or asynchronous call
	        // within UISynchronize
	        if ((Boolean) event.getProperty(DocumentEditor.DOCUMENT_RECALCULATE)) {
	            calculateTotal();
	        }
	        getMDirtyablePart().setDirty(true);
	    }
	}
	/**
	 * If an entity is deleted via list view we have to close a possibly open
	 * editor window. Since this is triggered by a UIEvent we named this method
	 * "handle*".
	 */
	@Inject
	@Optional
	public void handleForceClose(@UIEventTopic(ExpenditureVoucherEditor.EDITOR_ID + "/forceClose") Event event) {
	    //      sync.syncExec(() -> top.setRedraw(false));
	    // the event has already all given params in it since we created them as Map
	    String targetDocumentName = (String) event.getProperty(DocumentEditor.DOCUMENT_ID);
	    // at first we have to check if the message is for us
	    String voucherTempId =  part.getProperties().get(PART_ID); 
	    if (!StringUtils.equals(targetDocumentName, voucherTempId)) {
	        // if not, silently ignore this event
	        return;
	    }
	    partService.hidePart(part, true);
	    //  sync.syncExec(() -> top.setRedraw(true));
	}
	/**
	 * Calculate the total sum of all voucher items
	 */
	protected void calculateTotal() {
	    
	    VoucherSummaryCalculator voucherSummaryCalculator = ContextInjectionFactory.make(VoucherSummaryCalculator.class, context);
	    // unwrap VoucherItemDTOs at first
	    List<VoucherItem> docItems = new ArrayList<>();
	    if(itemListTable != null) {
	        // don't use Lambdas because the List isn't initialized yet.
	        for (VoucherItemDTO item : itemListTable.getVoucherItemsListData()) {
	            docItems.add(item.getVoucherItem());
	        }
	    } else {
	        docItems.addAll(voucher.getItems());
	    }
	    // Do the calculation
	    VoucherSummary voucherSummary = voucherSummaryCalculator.calculate(null, docItems, false, 
	            paidValue, totalValue, false);
	
	    // Get the total result
	    totalValue = voucherSummary.getTotalGross();
	
	    // Update the text widget
	    textTotalValue.setValue(totalValue);
	    voucher.setTotalValue((Double) textTotalValue.getValue());
	}
	
    @Override
    protected MDirtyable getMDirtyablePart() {
        return part;
    }
    
	protected abstract String[] getNameProposals();
}
