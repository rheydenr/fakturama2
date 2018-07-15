/*
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.parts;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.money.MonetaryAmount;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.javamoney.moneta.Money;
import org.osgi.service.event.Event;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.dao.ShippingCategoriesDAO;
import com.sebulli.fakturama.dao.ShippingsDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.CategoryComparator;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.model.ShippingCategory;
import com.sebulli.fakturama.model.ShippingVatType;
import com.sebulli.fakturama.model.Shipping_;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.parts.converter.CategoryConverter;
import com.sebulli.fakturama.parts.converter.EntityConverter;
import com.sebulli.fakturama.parts.converter.StringToCategoryConverter;
import com.sebulli.fakturama.parts.converter.StringToEntityConverter;
import com.sebulli.fakturama.parts.widget.GrossText;
import com.sebulli.fakturama.parts.widget.NetText;
import com.sebulli.fakturama.parts.widget.contentprovider.EntityComboProvider;
import com.sebulli.fakturama.parts.widget.contentprovider.ShippingVatTypeContentProvider;
import com.sebulli.fakturama.parts.widget.labelprovider.EntityLabelProvider;
import com.sebulli.fakturama.parts.widget.labelprovider.ShippingVatTypeLabelProvider;
import com.sebulli.fakturama.resources.core.Icon;

/**
 * The Shipping editor
 */
public class ShippingEditor extends Editor<Shipping> {

    @Inject
    protected ShippingsDAO shippingDao;
    
    @Inject
    protected VatsDAO vatsDao;

    @Inject
    protected ShippingCategoriesDAO shippingCategoriesDAO;
    
    @Inject
    private EPartService partService;
    
    @Inject
    protected IEclipseContext context;

    // Editor's ID
    public static final String EDITOR_ID = "ShippingEditor";

    public static final String ID = "com.sebulli.fakturama.editors.shippingEditor";

    // SWT widgets of the editor
    private Composite top;
    private Text textName;
    private Text textDescription;
    private Combo comboVat;
    private ComboViewer comboViewer;
    private ComboViewer comboAutoVat;
    private NetText netText;
    private GrossText grossText;
    private Combo comboCategory;
    private ShippingCategory oldCat;

    // defines if the shipping is just created
    private boolean newShipping;

    // These flags are set by the preference settings.
    // They define, if elements of the editor are displayed, or not.
    private boolean useNet;
    private boolean useGross;

    // These are (non visible) values of the document
    private MonetaryAmount net;
//    private Double vat = NumberUtils.DOUBLE_ZERO;
//    private VAT vat = null;
    private ShippingVatType autoVat = ShippingVatType.SHIPPINGVATGROSS;

    /**
     * This field can't be injected since the part is created from
     * a PartDescriptor (see createPartControl).
     */
    private MPart part;

    // This UniDataSet represents the editor's input 
    private Shipping editorShipping = null;

    /**
     * Saves the contents of this part
     * 
     * @param monitor
     *            Progress monitor
     */
    @Persist
    public void doSave(IProgressMonitor monitor) {

        /*
         * the following parameters are not saved: 
         * - id (constant)
         */

        // Always set the editor's data to "undeleted"
        editorShipping.setDeleted(Boolean.FALSE);

   		// Set the Shipping data
        // ... done through databinding...
        // except value (since it could be from gross or from net        
//        editorShipping.setShippingValue(netText.getNetValue().getNumber().doubleValue());

   		// save the new or updated Shipping
        try {
            // at first, check the category for a new entry
            // (the user could have written a new one into the combo field)
            String testCat = comboCategory.getText();
            // if there's no category we can skip this step
            if(StringUtils.isNotBlank(testCat)) {
                ShippingCategory parentCategory = shippingCategoriesDAO.getCategory(testCat, true);
                // parentCategory now has the last found Category
                editorShipping.setCategories(parentCategory);
            }
            
            /*
             * If we DON'T use update, the category is saved again and again and again
             * because we have CascadeType.PERSIST. If we use update and save the new ShippingCategory before,
             * all went ok. That's the point...
             */
            // we have to truncate the shipping value (because of calculations between gross and net)
            MathContext mc = new MathContext(16, RoundingMode.HALF_UP);
            BigDecimal val = BigDecimal.valueOf(editorShipping.getShippingValue()).round(mc).setScale(5, RoundingMode.HALF_UP);
            editorShipping.setShippingValue(val.doubleValue());
            editorShipping = shippingDao.update(editorShipping);

            // check if we can delete the old category (if it's empty)
            if(oldCat != null && oldCat != editorShipping.getCategories()) {
            	long countOfEntriesInCategory = shippingDao.countByCategory(oldCat);
            	if(countOfEntriesInCategory == 0) {
            		shippingCategoriesDAO.deleteEmptyCategory(oldCat);
            	}
            }

            oldCat = editorShipping.getCategories();
        }
        catch (FakturamaStoringException e) {
            log.error(e, "can't save the current Shipping: " + editorShipping.toString());
        }

        if (newShipping) {
			newShipping = false;
			stdComposite.stdButton.setEnabled(true);
    	}

       	// Set the Editor's name to the payment name...
        part.setLabel(editorShipping.getName());
        
        // ...and "mark" it with current objectId (though it can be find by 
        // CallEditor if one tries to open it immediately from list view)
        part.getTransientData().put(CallEditor.PARAM_OBJ_ID, Long.toString(editorShipping.getId()));
     
		// Refresh the table view of all Shippings (this also refreshes the tree of categories)
        evtBroker.post(EDITOR_ID, Editor.UPDATE_EVENT);
        
        bindModel();

        // reset dirty flag
		getMDirtyablePart().setDirty(false);
    }
	
    /**
     * If an entity is deleted via list view we have to close a possibly open
     * editor window. Since this is triggered by a UIEvent we named this method
     * "handle*".
     */
    @Inject
    @Optional
    public void handleForceClose(@UIEventTopic(ShippingEditor.EDITOR_ID + "/forceClose") Event event) {
        // the event has already all given params in it since we created them as Map
        String targetDocumentName = (String) event.getProperty(DocumentEditor.DOCUMENT_ID);
        // at first we have to check if the message is for us
        if (!StringUtils.equals(targetDocumentName, editorShipping.getName())) {
            // if not, silently ignore this event
            return;
        }
        partService.hidePart(part, true);
    }

    /**
     * Initializes the editor. If an existing data set is opened, the local
     * variable "shipping" is set to this data set. If the editor is opened to create
     * a new one, a new data set is created and the local variable "shipping" is set
     * to this one.<br />
     * If we get an ID from the opening command we try to open the given
     * {@link Shipping}.
     * 
     * @param parent
     *            the parent control
     */
    @PostConstruct
    public void createPartControl(Composite parent) {
        Long objId = null;
        Shipping stdShipping = null;
        long stdID = 1L;
        this.part = (MPart) parent.getData("modelElement");
        this.part.setIconURI(Icon.COMMAND_SHIPPING.getIconURI());
        String tmpObjId = (String) part.getProperties().get(CallEditor.PARAM_OBJ_ID);
        if (StringUtils.isNumeric(tmpObjId)) {
            objId = Long.valueOf(tmpObjId);
            // Set the editor's data set to the editor's input
            editorShipping = shippingDao.findById(objId, true);
        }
        
        // Some of this editos's control elements can be hidden.
        // Get the these settings from the preference store
        useNet = defaultValuePrefs.getInt(Constants.PREFERENCES_PRODUCT_USE_NET_GROSS) != 2;
        useGross = defaultValuePrefs.getInt(Constants.PREFERENCES_PRODUCT_USE_NET_GROSS) != 1;

        // test, if the editor is opened to create a new data set. This is,
        // if there is no input set.
        newShipping = (editorShipping == null);

        // If new...
        if (newShipping) {
            // Create a new data set
            editorShipping = modelFactory.createShipping();
            String category = (String) part.getProperties().get(CallEditor.PARAM_CATEGORY);
            if(StringUtils.isNotEmpty(category)) {
                ShippingCategory newCat = shippingCategoriesDAO.findCategoryByName(category);
                editorShipping.setCategories(newCat);
            }
            editorShipping.setAutoVat(ShippingVatType.SHIPPINGVATGROSS);
            editorShipping.setShippingValue(Double.valueOf(0.0));
            int vatId = defaultValuePrefs.getInt(Constants.DEFAULT_VAT);
            VAT vat = vatsDao.findById(vatId);  // initially set default VAT
            editorShipping.setShippingVat(vat);
            
            editorShipping.setValidFrom(new Date());

            //T: Shipping Editor: Part Name of a new Shipping Entry
            part.setLabel(msg.mainMenuNewShipping);
            getMDirtyablePart().setDirty(true);
        }
        else {
            // Set the Editor's name to the Shipping name.
            part.setLabel(editorShipping.getName());
        }

        // Get the auto VAT setting
        autoVat = editorShipping.getAutoVat();
        if (autoVat == ShippingVatType.SHIPPINGVATGROSS)
            useGross = true;
        if (autoVat == ShippingVatType.SHIPPINGVATNET)
            useNet = true;

        // Get the VAT ID
        // Get the VAT by the VAT ID
//        vat = editorShipping.getShippingVat();

        // Create the top Composite
        top = new Composite(parent, SWT.NONE);
        GridLayoutFactory.swtDefaults().numColumns(2).applyTo(top);
        
		// Add context help reference 
        //		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, ContextHelpConstants.SHIPPING_EDITOR);

        // Create the title
        Label labelTitle = new Label(top, SWT.NONE);
        labelTitle.setText(msg.editorShippingTitle);
        GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, false).span(2, 1).applyTo(labelTitle);
        makeLargeLabel(labelTitle);

        // Shipping name
        Label labelName = new Label(top, SWT.NONE);
        labelName.setText(msg.commonFieldName);
        labelName.setToolTipText(msg.editorShippingNameTooltip);

        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelName);
        textName = new Text(top, SWT.BORDER);
        textName.setToolTipText(labelName.getToolTipText());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textName);

        // Shipping category
        Label labelCategory = new Label(top, SWT.NONE);
        labelCategory.setText(msg.commonFieldCategory);
        labelCategory.setToolTipText(msg.editorShippingCategoryTooltip);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCategory);

        comboCategory = new Combo(top, SWT.BORDER);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(comboCategory);

        // Shipping description
        Label labelDescription = new Label(top, SWT.NONE);
        labelDescription.setText(msg.commonFieldDescription);
        labelDescription.setToolTipText(msg.editorVatDescriptionTooltip);

        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDescription);
        textDescription = new Text(top, SWT.BORDER);
        //		textDescription.setText(editorShipping.getDescription());
        textDescription.setToolTipText(labelDescription.getToolTipText());
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textDescription);

        // Shipping value
        Label labelValue = new Label(top, SWT.NONE);
        labelValue.setText(msg.commonFieldValue);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.BOTTOM).applyTo(labelValue);

        // Variable to store the net value
		net = Money.of(editorShipping.getShippingValue(), DataUtils.getInstance().getDefaultCurrencyUnit());

        // Create a composite that contains a widget for the net and gross value
        Composite netGrossComposite = new Composite(top, SWT.NONE);
        GridLayoutFactory.swtDefaults().margins(0, 0).numColumns((useNet && !useGross) ? 4 : 2).applyTo(netGrossComposite);

        // Create a net label
        if (useNet) {
            Label netValueLabel = new Label(netGrossComposite, SWT.NONE);
            netValueLabel.setText(msg.productDataNet);
            GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(netValueLabel);
        }

        // Create a gross label
        if (useGross) {
            Label grossValueLabel = new Label(netGrossComposite, SWT.NONE);
            grossValueLabel.setText(msg.productDataGross);
            GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).applyTo(grossValueLabel);
        }

		context.set(Constants.CONTEXT_STYLE, SWT.BORDER | SWT.RIGHT);
		context.set(Constants.CONTEXT_VATVALUE, editorShipping.getShippingVat().getTaxValue());
		context.set(Constants.CONTEXT_CANVAS, netGrossComposite);
		context.set(Constants.CONTEXT_NETVALUE, net);
        // Create a net text widget
        if (useNet) {
            netText = ContextInjectionFactory.make(NetText.class, context);
        }

        // Create a gross text widget
        if (useGross) {
			if(!useNet) {
				// create hidden net field if no one is given
				netText = ContextInjectionFactory.make(NetText.class, context);
				netText.getNetText().getControl().setVisible(false);
				GridDataFactory.swtDefaults().hint(0, SWT.DEFAULT).applyTo(netText.getNetText().getControl());
			}
			grossText = ContextInjectionFactory.make(GrossText.class, context);
			grossText.setNetText(netText);
        }

        // If net and gross were created, link both together
        // so, if one is modified, the other will be recalculated.
        if (useNet && useGross) {
            netText.setGrossText(grossText);
            grossText.setNetText(netText);
        }

        // Apply the gross text widget
        if (useGross) {
            GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).align(SWT.CENTER, SWT.TOP).applyTo(grossText.getGrossText().getControl());
        }
        // Apply the net text widget
        if (useNet) {
            GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).align(SWT.CENTER, SWT.TOP).applyTo(netText.getNetText().getControl());
        }
        
        // VAT Label
        Label labelVat = new Label(top, SWT.NONE);
        labelVat.setText(msg.commonFieldVat);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelVat);

        // VAT combo list
        comboVat = new Combo(top, SWT.BORDER | SWT.READ_ONLY);
        comboViewer = new ComboViewer(comboVat);
        comboViewer.setContentProvider(new EntityComboProvider());
        comboViewer.setLabelProvider(new EntityLabelProvider());
        
        // Create a label for the automatic VAT calculation
        Label labelAutoVat = new Label(top, SWT.NONE);
        //T: Shipping Editor: Label VAT Calculation
        labelAutoVat.setText(msg.editorShippingFieldAutovatcalculationName);
        //T: Tool Tip Text
        labelAutoVat.setToolTipText(msg.editorShippingFieldAutovatcalculationTooltip);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelAutoVat);

        // Create a combo list box for the automatic VAT calculation
        comboAutoVat = new ComboViewer(top, SWT.BORDER | SWT.READ_ONLY);
        //T: Shipping Editor: list entry for "constant VAT calculation"
        List<ShippingVatType> shippingVatTypes = new ArrayList<>();
        shippingVatTypes.add(ShippingVatType.SHIPPINGVATFIX);
        comboAutoVat.getCombo().setToolTipText(labelAutoVat.getToolTipText());
        if (useGross)
            //T: Shipping Editor: list entry for "Calculate VAT from goods VAT - constant Gross"
            shippingVatTypes.add(ShippingVatType.SHIPPINGVATGROSS);
        if (useNet)
            //T: Shipping Editor: list entry for "Calculate VAT from goods VAT - constant Net"
//            comboAutoVat.add();
            shippingVatTypes.add(ShippingVatType.SHIPPINGVATNET);
        comboAutoVat.setContentProvider(new ShippingVatTypeContentProvider());
        comboAutoVat.setLabelProvider(new ShippingVatTypeLabelProvider(msg));
        comboAutoVat.setInput(shippingVatTypes);

        comboAutoVat.addSelectionChangedListener(new ISelectionChangedListener() {
            
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                // Get the selected list entry
                autoVat = (ShippingVatType) ((IStructuredSelection)comboAutoVat.getSelection()).getFirstElement();
                // If no gross values are used, do not allow to select
                // the entry "SHIPPINGVATGROSS"
                if (!useGross && (autoVat.isSHIPPINGVATGROSS()))
                    autoVat = ShippingVatType.SHIPPINGVATNET;

                // Display or hide the net and gross widgets
                autoVatChanged();

                // Check, if the document has changed.
//                checkDirty();
            }
        });
        
        // Create the composite to make this Shipping to the standard Shipping. 
        Label labelStdShipping = new Label(top, SWT.NONE);
        labelStdShipping.setText(msg.commonLabelDefault);
        labelStdShipping.setToolTipText(msg.editorShippingDefaultTooltip);

        // Get the ID of the standard entity from preferences
        try {
            stdID = defaultValuePrefs.getLong(getDefaultEntryKey());
        } catch (NumberFormatException | NullPointerException e) {
            stdID = 1L;
        } finally {
            stdShipping = shippingDao.findById(stdID);
        }

        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelStdShipping);
        //T: Shipping Editor: Button description to make this as standard Shipping.
        stdComposite = new StdComposite(top, editorShipping, stdShipping, msg.editorShippingDefaultButton, 1);
        stdComposite.setToolTipText(msg.editorShippingDefaultButtonTooltip);

        // Disable the Standard Button if this is a new Shipping;
        // the Button is disabled by default, see Editor.StdComposite
        if (!newShipping) {
            stdComposite.stdButton.setEnabled(true);
        }
        
        bindModel();
        
        // add listener always _after_ bind, else you would get a dirty editor instantly after opening
        if(grossText != null) {
            grossText.getGrossText().getControl().addModifyListener(e -> {
            	if(((MPart) getMDirtyablePart()).getTransientData().get(BIND_MODE_INDICATOR) == null) {
            		setDirty(true);
            	}
            });
        }
        
        if(netText != null) {
            netText.getNetText().getControl().addModifyListener(e -> {
            	if(((MPart) getMDirtyablePart()).getTransientData().get(BIND_MODE_INDICATOR) == null) {
            		setDirty(true);
            	}
            });
        }
    }

	private void fillAndBindVatCombo() {
		List<VAT> allVATs = vatsDao.findAll();

        comboViewer.addSelectionChangedListener(new ISelectionChangedListener() {

            public void selectionChanged(SelectionChangedEvent event) {

                // Handle selection changed event 
                ISelection selection = event.getSelection();
                IStructuredSelection structuredSelection = (IStructuredSelection) selection;

                // If one element is selected
                if (!structuredSelection.isEmpty()) {

                    // Get the first element ...
                    Object firstElement = structuredSelection.getFirstElement();

                    // Get the selected VAT
                    VAT uds = (VAT) firstElement;

//                    // Store the old value
//                    Double oldVat = editorShipping.getShippingVat().getTaxValue();
//
//                    // Get the new value
//                    vat = uds;
//
//                    // Recalculate the price values if gross is selected,
//                    // So the gross value will stay constant.
//                    if (!useNet) {
////                        net = new Double(editorShipping.getShippingValue() * ((1 + oldVat) / (1 + vat.getTaxValue())));
//                    }

                    // Update net and gross text widget
                    if (netText != null)
                        netText.setVatValue(uds.getTaxValue());
                    if (grossText != null)
                        grossText.setVatValue(uds.getTaxValue());

                    // Check, if the document has changed.
//                    checkDirty();
                }
            }
        });

        // Create a JFace combo viewer for the VAT list
        
        VAT tmpShippingVat = editorShipping.getShippingVat();
        comboViewer.setInput(allVATs);
        editorShipping.setShippingVat(tmpShippingVat);
        
        UpdateValueStrategy vatModel2Target = new UpdateValueStrategy();
        vatModel2Target.setConverter(new EntityConverter<VAT>(VAT.class));
        
        UpdateValueStrategy target2VatModel = new UpdateValueStrategy();
        target2VatModel.setConverter(new StringToEntityConverter<VAT>(allVATs, VAT.class));
        bindModelValue(editorShipping, comboVat, Shipping_.shippingVat.getName(),
                target2VatModel, vatModel2Target);
	}
    
    protected void bindModel() {
		part.getTransientData().put(BIND_MODE_INDICATOR, Boolean.TRUE);

		bindModelValue(editorShipping, textName, Shipping_.name.getName(), 64);
        fillAndBindCategoryCombo();
        bindModelValue(editorShipping, textDescription, Shipping_.description.getName(), 250);
        fillAndBindVatCombo();
        
        if(useGross && !useNet) {
            bindModelValue(editorShipping, grossText.getNetText().getNetText(), Shipping_.shippingValue.getName(), 64);
        } else {
            bindModelValue(editorShipping, netText.getNetText(), Shipping_.shippingValue.getName(), 64);
        }
        
        // On creating this editor, select the entry of the autoVat list,
        // that is set by the shipping.
        try {
            bindModelValue(editorShipping, comboAutoVat, Shipping_.autoVat.getName());
            autoVatChanged();
        }
        catch (IndexOutOfBoundsException e) {
            autoVat = ShippingVatType.SHIPPINGVATGROSS;
        }
        
		part.getTransientData().remove(BIND_MODE_INDICATOR);
    }

    /**
     * creates the combo box for the Shipping category
     */
    private void fillAndBindCategoryCombo() {
        // Collect all category strings as a sorted Set
        final TreeSet<ShippingCategory> categories = new TreeSet<ShippingCategory>(new CategoryComparator<>());
        categories.addAll(shippingCategoriesDAO.findAll());

        ComboViewer viewer = new ComboViewer(comboCategory);
        viewer.setContentProvider(new ArrayContentProvider() {
            @Override
            public Object[] getElements(Object inputElement) {
                return categories.toArray();
            }
        });
        
        // Add all categories to the combo
        // FIXME see comment in VatEditor
        ShippingCategory tmpCat = editorShipping.getCategories();
        viewer.setInput(categories);
        viewer.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                return element instanceof ShippingCategory ? CommonConverter.getCategoryName((ShippingCategory)element, "") : null;
            }
        });
        editorShipping.setCategories(tmpCat);

        UpdateValueStrategy shippingCatModel2Target = new UpdateValueStrategy();
        shippingCatModel2Target.setConverter(new CategoryConverter<ShippingCategory>(ShippingCategory.class));
        
        UpdateValueStrategy target2ShippingcatModel = new UpdateValueStrategy();
        target2ShippingcatModel.setConverter(new StringToCategoryConverter<ShippingCategory>(categories, ShippingCategory.class));
        bindModelValue(editorShipping, comboCategory, Shipping_.categories.getName(), target2ShippingcatModel, shippingCatModel2Target);
    }

    /**
     * Show or hide the netText and grossText widget, depending on the setting
     * "autoVat".
     */
    private void autoVatChanged() {
        switch (autoVat) {

        // The gross value is based on the net value by using
        // a constant VAT factor
        case SHIPPINGVATFIX:
            comboVat.setVisible(true);
            if (useNet && netText != null) {
                netText.setVisible(true);
                netText.setVatValue(editorShipping.getShippingVat().getTaxValue());
            }
            if (grossText != null) {
                grossText.setVisible(true);
                grossText.setVatValue(editorShipping.getShippingVat().getTaxValue());
            }
            break;

        // The shipping net value is based on the gross value using the
        // same VAT factor as the items. The gross value is kept constant.
        case SHIPPINGVATGROSS:
            comboVat.setVisible(false);
            if (netText != null) {
                netText.setVisible(false);
                netText.setVatValue(Double.valueOf(0.0));
            }
            if (grossText != null) {
                grossText.setVisible(true);
                grossText.setVatValue(Double.valueOf(0.0));
            }
            break;

        // The shipping gross value is based on the net value using the
        // same VAT factor as the items. The net value is kept constant.
        case SHIPPINGVATNET:
            comboVat.setVisible(false);
            if (netText != null) {
                netText.setVisible(true);
                netText.setVatValue(Double.valueOf(0.0));
            }
            if (grossText != null) {
                grossText.setVisible(false);
                grossText.setVatValue(Double.valueOf(0.0));
            }
            break;
        }

    }
//
//    @PreDestroy
//    public void beforeClose() {
//        // Refresh the table view of all Shippings. This is necessary because if you change an entity
//        // and don't save it, the list view gets updated (with the unsaved entity!). This call updates the
//        // list view from database.
//        evtBroker.post(EDITOR_ID, Editor.UPDATE_EVENT);
//        editorShipping = null;
//        top = null;
//    }
   
    @Override
    protected String getDefaultEntryKey() {
        return Constants.DEFAULT_SHIPPING;
    }
    
    @Override
    protected MDirtyable getMDirtyablePart() {
        return part;
    }
        
    public void setDirty(boolean isDirty) {
    	getMDirtyablePart().setDirty(isDirty);
    }

    @Override
    protected Class<Shipping> getModelClass() {
        return Shipping.class;
    }
    
    @Override
    protected String getEditorID() {
    	return EDITOR_ID;
    }
}
