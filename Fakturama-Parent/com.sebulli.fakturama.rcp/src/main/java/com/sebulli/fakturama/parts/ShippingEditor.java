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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

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

import com.sebulli.fakturama.dao.ShippingCategoriesDAO;
import com.sebulli.fakturama.dao.ShippingsDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.model.ShippingCategory;
import com.sebulli.fakturama.model.ShippingVatType;
import com.sebulli.fakturama.model.Shipping_;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.parts.converter.CategoryConverter;
import com.sebulli.fakturama.parts.converter.CommonConverter;
import com.sebulli.fakturama.parts.converter.EntityConverter;
import com.sebulli.fakturama.parts.converter.StringToCategoryConverter;
import com.sebulli.fakturama.parts.converter.StringToEntityConverter;
import com.sebulli.fakturama.parts.widget.GrossText;
import com.sebulli.fakturama.parts.widget.NetText;
import com.sebulli.fakturama.parts.widget.contentprovider.EntityComboProvider;
import com.sebulli.fakturama.parts.widget.contentprovider.ShippingVatTypeContentProvider;
import com.sebulli.fakturama.parts.widget.labelprovider.EntityLabelProvider;
import com.sebulli.fakturama.parts.widget.labelprovider.ShippingVatTypeLabelProvider;

/**
 * The Shipping editor
 */
public class ShippingEditor extends Editor<Shipping> {

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    protected ShippingsDAO shippingDao;
    
    @Inject
    protected VatsDAO vatsDao;
    
    @Inject
    @Preference  //(nodePath = "/configuration/contactPreferences")
    protected IEclipsePreferences defaultPreferences;
    
    /**
     * Event Broker for sending update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

    @Inject
    protected ShippingCategoriesDAO shippingCategoriesDAO;
    
    @Inject
    protected ILogger log;

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

    // defines if the shipping is just created
    private boolean newShipping;

    // These flags are set by the preference settings.
    // They define, if elements of the editor are displayed, or not.
    private boolean useNet;
    private boolean useGross;

    // These are (non visible) values of the document
//    private Double net;
//    private Double vat = NumberUtils.DOUBLE_ZERO;
    private VAT vat = null;
    private ShippingVatType autoVat = ShippingVatType.SHIPPINGVATFIX;

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
        }
        catch (SQLException e) {
            log.error(e, "can't save the current Shipping: " + editorShipping.toString());
        }

        if (newShipping) {
			newShipping = false;
			stdComposite.stdButton.setEnabled(true);
    	}

       	// Set the Editor's name to the payment name.
        part.setLabel(editorShipping.getName());
        
		// Refresh the table view of all Shippings (this also refreshes the tree of categories)
        evtBroker.post(EDITOR_ID, "update");
        
        // reset dirty flag
		getMDirtyablePart().setDirty(false);
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
        String tmpObjId = (String) part.getProperties().get(CallEditor.PARAM_OBJ_ID);
        if (StringUtils.isNumeric(tmpObjId)) {
            objId = Long.valueOf(tmpObjId);
            // Set the editor's data set to the editor's input
            editorShipping = shippingDao.findById(objId, true);
        }

        // test, if the editor is opened to create a new data set. This is,
        // if there is no input set.
        newShipping = (editorShipping == null);

        // If new...
        if (newShipping) {
            // Create a new data set
            editorShipping = new Shipping();

            //T: Shipping Editor: Part Name of a new Shipping Entry
            part.setLabel(msg.mainMenuNewShipping);
        }
        else {
            // Set the Editor's name to the Shipping name.
            part.setLabel(editorShipping.getName());
        }
        
        // Some of this editos's control elements can be hidden.
        // Get the these settings from the preference store
        useNet = defaultPreferences.getInt(Constants.PREFERENCES_PRODUCT_USE_NET_GROSS, 0) != 2;
        useGross = defaultPreferences.getInt(Constants.PREFERENCES_PRODUCT_USE_NET_GROSS, 0) != 1;

        // Get the auto VAT setting
        autoVat = editorShipping.getAutoVat();
        if (autoVat == ShippingVatType.SHIPPINGVATGROSS)
            useGross = true;
        if (autoVat == ShippingVatType.SHIPPINGVATNET)
            useNet = true;

        // Get the VAT ID
        // Get the VAT by the VAT ID
        vat = editorShipping.getShippingVat();

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

        bindModelValue(editorShipping, textName, Shipping_.name.getName(), 64);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textName);

        // Shipping category
        Label labelCategory = new Label(top, SWT.NONE);
        labelCategory.setText(msg.commonFieldCategory);
        labelCategory.setToolTipText(msg.editorShippingCategoryTooltip);

        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelCategory);

        createCategoryCombo();
        GridDataFactory.fillDefaults().grab(true, false).applyTo(comboCategory);

        // Shipping description
        Label labelDescription = new Label(top, SWT.NONE);
        labelDescription.setText(msg.commonFieldDescription);
        labelDescription.setToolTipText(msg.editorVatDescriptionTooltip);

        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelDescription);
        textDescription = new Text(top, SWT.BORDER);
        //		textDescription.setText(editorShipping.getDescription());
        textDescription.setToolTipText(labelDescription.getToolTipText());
        bindModelValue(editorShipping, textDescription, Shipping_.description.getName(), 250);
        GridDataFactory.fillDefaults().grab(true, false).applyTo(textDescription);

        // Shipping value
        Label labelValue = new Label(top, SWT.NONE);
        labelValue.setText(msg.commonFieldValue);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelValue);

        // Variable to store the net value
//        net = editorShipping.getShippingValue();

        // Create a composite that contains a widget for the net and gross value
        Composite netGrossComposite = new Composite(top, SWT.NONE);
        GridLayoutFactory.swtDefaults().margins(0, 0).numColumns((useNet && useGross) ? 2 : 1).applyTo(netGrossComposite);

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

        // Create a net text widget
        if (useNet) {
            netText = new NetText(netGrossComposite, SWT.BORDER | SWT.RIGHT, editorShipping.getShippingValue(), vat.getTaxValue());
            bindModelValue(editorShipping, netText.getNetText(), Shipping_.shippingValue.getName(), 16);
        }

        // Create a gross text widget
        if (useGross) {
            grossText = new GrossText(netGrossComposite, SWT.BORDER | SWT.RIGHT, editorShipping.getShippingValue(), vat.getTaxValue());
        }

        // If net and gross were created, link both together
        // so, if one is modified, the other will be recalculated.
        if (useNet && useGross) {
            netText.setGrossText(grossText.getGrossText());
            grossText.setNetText(netText);
        }

        // Apply the gross text widget
        if (useGross) {
            GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(grossText.getGrossText().getControl());
        }
        // Apply the net text widget
        if (useNet) {
            GridDataFactory.swtDefaults().hint(100, SWT.DEFAULT).applyTo(netText.getNetText().getControl());
        }

        
        // VAT Label
        Label labelVat = new Label(top, SWT.NONE);
        labelVat.setText(msg.commonFieldVat);
        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelVat);

        // VAT combo list
        List<VAT> allVATs = vatsDao.findAll();
        comboVat = new Combo(top, SWT.BORDER);
        comboViewer = new ComboViewer(comboVat);
        comboViewer.setContentProvider(new EntityComboProvider());
        comboViewer.setLabelProvider(new EntityLabelProvider());

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

                    // Store the old value
                    Double oldVat = editorShipping.getShippingVat().getTaxValue();

                    // Get the new value
//                    vatId = uds.getId();
                    vat = uds;

                    // Recalculate the price values if gross is selected,
                    // So the gross value will stay constant.
                    if (!useNet) {
//                        net = new Double(editorShipping.getShippingValue() * ((1 + oldVat) / (1 + vat.getTaxValue())));
                    }

                    // Update net and gross text widget
                    if (netText != null)
                        netText.setVatValue(vat.getTaxValue());
                    if (grossText != null)
                        grossText.setVatValue(vat.getTaxValue());

                    // Check, if the document has changed.
//                    checkDirty();
                }
            }
        });

        // Create a JFace combo viewer for the VAT list
        comboViewer.setInput(allVATs);

        UpdateValueStrategy vatModel2Target = new UpdateValueStrategy();
        vatModel2Target.setConverter(new EntityConverter<VAT>(VAT.class));
        
        UpdateValueStrategy target2VatModel = new UpdateValueStrategy();
        target2VatModel.setConverter(new StringToEntityConverter<VAT>(allVATs, VAT.class));
//        try {
        bindModelValue(editorShipping, comboVat, Shipping_.shippingVat.getName()/* + "." + VAT_.name.getName()*/,
                target2VatModel, vatModel2Target);
//        }
//        catch (IndexOutOfBoundsException e) {
//            comboVat.setText("invalid");
//            vatId = -1;
//        }

        
        // Create a label for the automatic VAT calculation
        Label labelAutoVat = new Label(top, SWT.NONE);
        //T: Shipping Editor: Label VAT Calculation
        labelAutoVat.setText(msg.editorShippingFieldAutovatcalculationName);
        //T: Tool Tip Text
        labelAutoVat.setToolTipText(msg.editorShippingFieldAutovatcalculationTooltip);

        GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).applyTo(labelAutoVat);

        List<ShippingVatType> shippingVatTypes = new ArrayList<>();
        // Create a combo list box for the automatic VAT calculation
        comboAutoVat = new ComboViewer(top, SWT.BORDER);
        //T: Shipping Editor: list entry for "constant VAT calculation"
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
        
        // On creating this editor, select the entry of the autoVat list,
        // that is set by the shipping.
        try {
            bindModelValue(editorShipping, comboAutoVat, Shipping_.autoVat.getName());
//            comboAutoVat.select(autoVat.getValue());
            autoVatChanged();
        }
        catch (IndexOutOfBoundsException e) {
//            comboAutoVat.setText("invalid");
            autoVat = ShippingVatType.SHIPPINGVATGROSS;
        }
        
        // Create the composite to make this Shipping to the standard Shipping. 
        Label labelStdShipping = new Label(top, SWT.NONE);
        labelStdShipping.setText(msg.commonLabelDefault);
        labelStdShipping.setToolTipText(msg.editorShippingDefaultTooltip);

        // Get the ID of the standard entity from preferences
        try {
            stdID = defaultValuePrefs.getLong(getDefaultEntryKey(), 1L);
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
    }

    /**
     * creates the combo box for the Shipping category
     */
    private void createCategoryCombo() {
        // Collect all category strings as a sorted Set
        final TreeSet<ShippingCategory> categories = new TreeSet<ShippingCategory>(new Comparator<ShippingCategory>() {
            @Override
            public int compare(ShippingCategory cat1, ShippingCategory cat2) {
                return cat1.getName().compareTo(cat2.getName());
            }
        });
        categories.addAll(shippingCategoriesDAO.findAll());

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
                return element instanceof ShippingCategory ? CommonConverter.getCategoryName((ShippingCategory)element, "") : null;
            }
        });

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
        // a constant Vat factor
        case SHIPPINGVATFIX:
            comboVat.setVisible(true);
            if (netText != null) {
                netText.setVisible(true);
                netText.setVatValue(vat.getTaxValue());
            }
            if (grossText != null) {
                grossText.setVisible(true);
                grossText.setVatValue(vat.getTaxValue());
            }
            break;

        // The shipping net value is based on the gross value using the
        // same VAT factor as the items. The gross value is kept constant.
        case SHIPPINGVATGROSS:
            comboVat.setVisible(false);
            if (netText != null) {
                netText.setVisible(false);
                netText.setVatValue(0.0);
            }
            if (grossText != null) {
                grossText.setVisible(true);
                grossText.setVatValue(0.0);
            }
            break;

        // The shipping gross value is based on the net value using the
        // same VAT factor as the items. The net value is kept constant.
        case SHIPPINGVATNET:
            comboVat.setVisible(false);
            if (netText != null) {
                netText.setVisible(true);
                netText.setVatValue(0.0);
            }
            if (grossText != null) {
                grossText.setVisible(false);
                grossText.setVatValue(0.0);
            }
            break;
        }

    }

    @PreDestroy
    public void beforeClose() {
        shippingDao.findById(editorShipping.getId(), true);
        editorShipping = null;
        top = null;
//        textName = null;
//        textDescription = null;
//         comboVat = null;
//         comboViewer = null;
//         comboAutoVat = null;
//         netText = null;
//         grossText = null;
//         comboCategory = null;
        
    }
   
    @Override
    protected String getDefaultEntryKey() {
        return Constants.DEFAULT_SHIPPING;
    }
    
    @Override
    protected MDirtyable getMDirtyablePart() {
        return part;
    }
}
