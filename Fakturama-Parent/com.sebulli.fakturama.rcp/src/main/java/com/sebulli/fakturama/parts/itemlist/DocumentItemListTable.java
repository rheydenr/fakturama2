/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2015 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.parts.itemlist;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.money.MonetaryAmount;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.IEditableRule;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultBooleanDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDoubleDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.PercentageDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.MultiLineTextCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.gui.CellEditDialog;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ComboBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.reorder.config.DefaultRowReorderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.reorder.event.RowReorderEvent;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.action.ViewportSelectRowAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.javamoney.moneta.Money;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.dto.DocumentItemDTO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.dto.Price;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.DummyStringCategory;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;
import com.sebulli.fakturama.util.ProductUtil;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.CellImagePainter;
import com.sebulli.fakturama.views.datatable.EntityGridListLayer;
//import com.sebulli.fakturama.views.datatable.ListViewGridLayer;
import com.sebulli.fakturama.views.datatable.MoneyDisplayConverter;
import com.sebulli.fakturama.views.datatable.tree.model.TreeObject;
import com.sebulli.fakturama.views.datatable.tree.ui.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

/**
 *
 */
public class DocumentItemListTable extends AbstractViewDataTable<DocumentItemDTO, DummyStringCategory> {

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private Logger log;

//    this is for synchronizing the UI thread (unused at the moment)
//    @Inject    
//    private UISynchronize synch;
    
    /**
     * Event Broker for sending update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;
    
    @Inject
    private ESelectionService selectionService;
    
    @Inject
    private IPreferenceStore preferences;
    
    @Inject
    private IEclipseContext context;
    
    @Inject
    private VatsDAO vatsDAO;

    // ID of this view
    public static final String ID = "fakturama.document.itemTable";
    
    private EventList<DocumentItemDTO> documentItemsListData;

    private Document document;
    private DocumentType documentType;

    // Flag if there are items with property "optional" set
    private boolean containsOptionalItems = false;

    // Flag if there are items with an discount set
    private boolean containsDiscountedItems = false;
    private boolean useGross;
//    private int netgross = DocumentSummary.NOTSPECIFIED;

    private Control top;
    
    private static final String OPTIONAL_CELL_LABEL = "Optional_Cell_LABEL";
    private static final String PERCENT_CELL_LABEL = "Percent_Cell_LABEL";
    private static final String MONEYVALUE_CELL_LABEL = "MoneyValue_Cell_LABEL";
    private static final String TOTAL_MONEYVALUE_CELL_LABEL = "TotalMoneyValue_Cell_LABEL";
    private static final String PICTURE_CELL_LABEL = "Picture_Cell_LABEL";
    private static final String TEXT_CELL_LABEL = "Text_Cell_LABEL";
    private static final String DECIMAL_CELL_LABEL = "Decimal_Cell_LABEL";
    private static final String VAT_CELL_LABEL = "VAT_Cell_LABEL";
    private static final String DESCRIPTION_CELL_LABEL = "Description_Cell_LABEL";
    private static final String POSITIONNUMBER_CELL_LABEL = "Positionnumber_Cell_LABEL";

    protected static final String POPUP_ID = "com.sebulli.fakturama.documentitemlist.popup";

    private EntityGridListLayer<DocumentItemDTO> gridListLayer;

    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    private SelectionLayer selectionLayer;
    
    private ProductUtil productUtil;
    
    /**
     * Entry point for this class. Here the whole Composite is built.
     * 
     * @param parent
     * @param document
     * @param documentSummary 
     * @param useGross 
     * @return
     */
    public Control createPartControl(Composite parent, Document document, boolean useGross,
            int netgross) {
        log.info("create DocumentItem list part");
        this.document = document;
        this.documentType = DocumentType.findByKey(document.getBillingType().getValue());
        this.useGross = useGross;
        this.productUtil = ContextInjectionFactory.make(ProductUtil.class, context);
        
        // Get some settings from the preference store
        if (netgross == DocumentSummary.ROUND_NOTSPECIFIED) {
            useGross = (preferences.getInt(Constants.PREFERENCES_DOCUMENT_USE_NET_GROSS) == 1);
        } else {
            useGross = (netgross == DocumentSummary.ROUND_GROSS_VALUES);
        }
        
        this.top = super.createPartControl(parent, DocumentItemDTO.class, false, ID);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);        
        // Listen to double clicks (omitted at the moment, perhaps at a later time
//        hookDoubleClickCommand(natTable, gridLayer);
        return top;
    }

/**
 * Create the default context menu 
 */
private Menu createContextMenu(NatTable natTable) {
//    // Add up/down and delete actions
//    menuManager.add(new MoveEntryUpAction());
//    menuManager.add(new MoveEntryDownAction());
//    menuManager.add(new DeleteDataSetAction());
    // add NatTable menu items
    // and register the DisposeListener
    MoveEntryUpMenuItem moveEntryUpHandler = ContextInjectionFactory.make(MoveEntryUpMenuItem.class, context);
    moveEntryUpHandler.setGridListLayer(gridListLayer);
    MoveEntryDownMenuItem moveEntryDownHandler = ContextInjectionFactory.make(MoveEntryDownMenuItem.class, context);
    moveEntryDownHandler.setGridListLayer(gridListLayer);
    Menu retval = new PopupMenuBuilder(natTable)
        .withMenuItemProvider(CommandIds.CMD_MOVE_UP, moveEntryUpHandler)
        .withMenuItemProvider(CommandIds.CMD_MOVE_DOWN, moveEntryDownHandler)
        .build();
    return retval;
}

    
/*
 * Move an item up or down
 * ==> done through NatTable mechanics
 */
    
    protected NatTable createListTable(Composite tableComposite) {  
        Integer columnIndex = Integer.valueOf(0);
        // fill the underlying data source (GlazedLists)
        initItemsList();

        // Create the table columns 
        // get the visible properties to show in list view along with their position index
        final BidiMap<Integer, DocumentItemListDescriptor> propertyNamesList = new DualHashBidiMap<>();
        
        if(preferences.getBoolean(Constants.PREFERENCES_DOCUMENT_USE_ITEM_POS)) {
            propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.POSITION);
        }

        if (containsOptionalItems || preferences.getBoolean(Constants.PREFERENCES_OPTIONALITEMS_USE) && (documentType == DocumentType.OFFER)) {
           propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.OPTIONAL);
        }

        propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.QUANTITY);
        
        if (preferences.getBoolean(Constants.PREFERENCES_PRODUCT_USE_QUNIT)) {
           propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.QUNIT);
        }
        
        if (preferences.getBoolean(Constants.PREFERENCES_PRODUCT_USE_ITEMNR)) {
           propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.ITEMNUMBER);
        }
        
        if (preferences.getBoolean(Constants.PREFERENCES_DOCUMENT_USE_PREVIEW_PICTURE)) {
           propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.PICTURE);
        }        
        
        propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.NAME);
        propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.DESCRIPTION);

        if (documentType.hasPrice()) {
            propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.VAT); 
            
            // "$ItemGrossPrice" (if useGross = true) or "price" (if useGross = false)
            propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.UNITPRICE);
            
            if (containsDiscountedItems || preferences.getBoolean(Constants.PREFERENCES_DOCUMENT_USE_DISCOUNT_EACH_ITEM)) {
                propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.DISCOUNT);
            } 
            
            // useGross = true => "$ItemGrossTotal", useGross = false => "$ItemNetTotal"
            propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.TOTALPRICE);
        }
        
        List<DocumentItemListDescriptor> tmpList2 = new ArrayList<DocumentItemListDescriptor>(propertyNamesList.values());
        String[] propertyNames = tmpList2.stream().map(DocumentItemListDescriptor::getPropertyName).collect(Collectors.toList()).toArray(new String[]{});
        final IColumnPropertyAccessor<DocumentItem> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<>(propertyNames);
        
        // Add derived column
        final IColumnPropertyAccessor<DocumentItemDTO> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<DocumentItemDTO>() {

            /**
             * Get the value to set to the editor
             */
            public Object getDataValue(DocumentItemDTO rowObject, int columnIndex) {
                Object retval = "???";
                DocumentItemListDescriptor descriptor = (DocumentItemListDescriptor) propertyNamesList.get(columnIndex);
                switch (descriptor) {
                case POSITION:
                    retval = rowObject.getDocumentItem().getPosNr();
                    break;
                case OPTIONAL:
                case QUANTITY:
                case QUNIT:
                case ITEMNUMBER:
                case NAME:
                case DESCRIPTION:
                case DISCOUNT:
                    retval = columnPropertyAccessor.getDataValue(rowObject.getDocumentItem(), columnIndex);
                    break;
                case PICTURE:
                    // we have to build the picture path
                    // opening the picture dialog (preview) occurs in the PictureViewEditor (via configuration)
                    String imgPath = (String) columnPropertyAccessor.getDataValue(rowObject.getDocumentItem(), columnIndex);
                    if (StringUtils.isNotBlank(imgPath)) {
                        String picturePath = preferences.getString(Constants.GENERAL_WORKSPACE) + Constants.PRODUCT_PICTURE_FOLDER;
                        retval = picturePath + imgPath;
                    } else {
                        retval = null;
                    }
                    break;
                case VAT:
                    retval = (VAT) columnPropertyAccessor.getDataValue(rowObject.getDocumentItem(), columnIndex);
                    break;
                case UNITPRICE:
/* TODO CHECK! old code:
 * 
            if (documentEditor.getUseGross())
                return new Price(item).getUnitGross().asFormatedString();
            else
                return new Price(item).getUnitNet().asFormatedString();
 *              
 */
                    retval = (Double) columnPropertyAccessor.getDataValue(rowObject.getDocumentItem(), columnIndex);
                    break;
                case TOTALPRICE:
                    if (useGross) { // "$ItemGrossTotal"
                        // Fill the cell with the total gross value of the item
                        retval = rowObject.getPrice().getTotalGrossRounded();
                    } else { // "$ItemNetTotal"
                        // Fill the cell with the total net value of the item
                        retval = rowObject.getPrice().getTotalNetRounded();
                    }
                    break;
                default:
                    retval = "???";
                    break;
                }
                return retval;
            }

            /**
             * Sets the new value on the given element.
             */
            public void setDataValue(DocumentItemDTO rowObject, int columnIndex, Object newValue) {
                DocumentItemListDescriptor descriptor = (DocumentItemListDescriptor) propertyNamesList.get(columnIndex);
                boolean calculate = true;
                switch (descriptor) {
                case OPTIONAL:
                    rowObject.getDocumentItem().setOptional((Boolean) newValue);
                    break;
                case QUANTITY:
                    Double oldQuanity = rowObject.getDocumentItem().getQuantity();
                    // Set the quantity
                    rowObject.getDocumentItem().setQuantity((Double) newValue);
                    Product product = rowObject.getDocumentItem().getProduct();

                    // If the item is coupled with a product, get the graduated price
                    if (product != null) {

                        // Compare the price. Is it equal to the price of the product,
                        // then use the product price.
                        // If the price is not equal, it was modified. In this case, do not
                        // modify the price value.
                        Double oldPrice = rowObject.getDocumentItem().getPrice();
                        Double oldPriceByQuantity = productUtil.getPriceByQuantity(product, oldQuanity);

                        Double newPrice = productUtil.getPriceByQuantity(product, DataUtils.getInstance().StringToDouble(String.valueOf(newValue)));

                        if (DataUtils.getInstance().DoublesAreEqual(oldPrice, oldPriceByQuantity)) {
                            // Do not use 0.00€
                            //if (!DataUtils.DoublesAreEqual(newPrice, 0.0))
                            rowObject.getDocumentItem().setPrice(newPrice);
                        }
                    }
                    break;
                case QUNIT:
                    rowObject.getDocumentItem().setQuantityUnit((String) newValue);
                    calculate = false; // no recalculation needed
                    break;
                case ITEMNUMBER:
                    rowObject.getDocumentItem().setItemNumber((String) newValue);
                    calculate = false; // no recalculation needed
                    break;
                case NAME:
                    rowObject.getDocumentItem().setName((String) newValue);
                    calculate = false; // no recalculation needed
                    break;
                case DESCRIPTION:
                    rowObject.getDocumentItem().setDescription((String) newValue);
                    calculate = false; // no recalculation needed
                    break;
                case DISCOUNT:
                    Double discountValue = (Double) newValue;
                    // Convert it to negative values
                    if (discountValue > 0) {
                        discountValue = -1 * discountValue;
                    }
                    rowObject.getDocumentItem().setItemRebate(discountValue);
                    break;
                case PICTURE:
                    // setting a new picture isn't allowed in this context!
                    calculate = false; // no recalculation needed
                    break;
                case VAT:
                    // Set the VAT
                    VAT vat = (VAT) newValue; //columnPropertyAccessor.getDataValue(rowObject.getDocumentItem(), columnIndex);

                    // // If no VAT is found, use the standard VAT
                    // if (i < 0)
                    //     i = Integer.parseInt(Data.INSTANCE.getProperty("standardvat"));

                    // Set the vat and store the vat value before and after the modification.
                    //                    Double oldVat = 1.0 + item.getDoubleValueByKeyFromOtherTable("vatid.VATS:value");
                    //                    item.setVat(i);
                    //                    Double newVat = 1.0 + item.getDoubleValueByKeyFromOtherTable("vatid.VATS:value");
                    //
                    //                    // Modify the net value that the gross value stays constant.
                    //                    if (documentEditor.getUseGross())
                    //                        item.setDoubleValueByKey("price", oldVat / newVat * item.getDoubleValueByKey("price"));

                    if (vat != null) {
                        rowObject.getDocumentItem().setItemVat(vat);
                        //                    } else {   TODO ???
                        //                        retval = Double.valueOf(0.0);
                    }
                    break;
                case UNITPRICE:
                    String priceString = ((String) newValue).toLowerCase();

                    // If the price is tagged with an "Net" or "Gross", force this
                    // value to a net or gross value
                    //T: Tag to mark a price as net or gross
                    if (priceString.contains((msg.productDataNet).toLowerCase())) {
                        useGross = false;
                    }
                    //T: Tag to mark a price as net or gross
                    if (priceString.contains((msg.productDataGross).toLowerCase())) {
                        useGross = true;
                    }

                    // Set the price as gross or net value.
                    // If the editor displays gross values, calculate the net value,
                    // because only net values are stored.
                    if (useGross) {
                        MonetaryAmount amount = Money.of(DataUtils.getInstance().StringToDouble(priceString), DataUtils.getInstance().getDefaultCurrencyUnit());
                        Price newPrice = new Price(amount, rowObject.getDocumentItem().getItemVat().getTaxValue(), rowObject.getDocumentItem().getNoVat(), true);
                        rowObject.getDocumentItem().setPrice(newPrice.getUnitNet().getNumber().doubleValue());
                    } else {
                        MonetaryAmount amount = Money.of(DataUtils.getInstance().StringToDouble(priceString), DataUtils.getInstance().getDefaultCurrencyUnit());
                        rowObject.getDocumentItem().setPrice(amount.getNumber().doubleValue());
                    }
                    break;
                default:
                    break;
                }

                // Recalculate the total sum of the document if necessary
                // do it via the messaging system and send a message to DocumentEditor
                Map<String, Object> event = new HashMap<>();
                event.put(DocumentEditor.DOCUMENT_ID, document.getName());
                event.put(DocumentEditor.DOCUMENT_RECALCULATE, calculate);
                evtBroker.post(DocumentEditor.EDITOR_ID + "/itemChanged", event);
            }

            public int getColumnCount() {
                return propertyNamesList.size();
            }

            public String getColumnProperty(int columnIndex) {
                DocumentItemListDescriptor descriptor = (DocumentItemListDescriptor) propertyNamesList.get(columnIndex);
                return msg.getMessageFromKey(descriptor.getMessageKey());
            }

            public int getColumnIndex(String propertyName) {
                    return columnPropertyAccessor.getColumnIndex(propertyName);
            }
        };

        IRowIdAccessor<DocumentItemDTO> rowIdAccessor = new IRowIdAccessor<DocumentItemDTO>() {
            @Override
            public Serializable getRowId(DocumentItemDTO rowObject) {
                return rowObject.getDocumentItem().getPosNr();
            }
        };

        //build the grid layer
        gridListLayer = new EntityGridListLayer<>(getDocumentItemsListData(), propertyNames, derivedColumnPropertyAccessor, rowIdAccessor, configRegistry, true);
        DataLayer tableDataLayer = gridListLayer.getBodyDataLayer();
        
        // set default percentage width 
        tableDataLayer.setColumnPercentageSizing(true);
//        tableDataLayer.setColumnPercentageSizing(position, percentageSizing);
        
        // Custom selection configuration
        selectionLayer = gridListLayer.getSelectionLayer();
        
        //set ISelectionProvider
        final RowSelectionProvider<DocumentItemDTO> selectionProvider = 
                new RowSelectionProvider<DocumentItemDTO>(selectionLayer, gridListLayer.getBodyDataProvider());
        
        //add a listener to the selection provider, in an Eclipse application you would do this
        //e.g. getSite().getPage().addSelectionListener()
        selectionProvider.addSelectionChangedListener(new ISelectionChangedListener() {
            
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
//                log.debug("Selection changed:");
                
                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                @SuppressWarnings("unchecked")
                List<DocumentItemDTO> selectedElements = selection.toList();
                selectionService.setSelection(selectedElements);
            }
            
        });
        
        // for further use, if we need it...
        //      ILayer columnHeaderLayer = gridLayer.getColumnHeaderLayer();
        //      ILayer rowHeaderLayer = gridLayer.getRowHeaderLayer();

        // Create a label accumulator - adds custom labels to all cells which we
        // wish to render differently.
        BidiMap<DocumentItemListDescriptor, Integer> reverseMap = propertyNamesList.inverseBidiMap();
        ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(gridListLayer.getBodyLayerStack());
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.POSITION, POSITIONNUMBER_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.OPTIONAL, OPTIONAL_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.PICTURE, PICTURE_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.VAT, VAT_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.DISCOUNT, PERCENT_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.UNITPRICE, MONEYVALUE_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.TOTALPRICE, TOTAL_MONEYVALUE_CELL_LABEL);
        
        // "normal" columns are always editable
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.DESCRIPTION, DESCRIPTION_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.ITEMNUMBER, TEXT_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.NAME, TEXT_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.QUANTITY, DECIMAL_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.QUNIT, TEXT_CELL_LABEL);

        final NatTable natTable = new NatTable(tableComposite /*, 
                SWT.NO_REDRAW_RESIZE| SWT.DOUBLE_BUFFERED | SWT.BORDER*/, gridListLayer.getGridLayer(), false);
        // Register label accumulator
        gridListLayer.getBodyLayerStack().setConfigLabelAccumulator(columnLabelAccumulator);
        
        // if a re-ordering of rows occurs we have to renumber the items
        gridListLayer.getBodyLayerStack().getRowReorderLayer().addLayerListener(new ILayerListener() {
            
            @Override
            public void handleLayerEvent(ILayerEvent event) {
                if (event instanceof RowReorderEvent) {
                    RowReorderEvent evt = (RowReorderEvent) event;
                    evt.convertToLocal(gridListLayer.getBodyLayerStack().getRowReorderLayer());
                    int newIdx = 0;
                    for (Integer rowIndex : gridListLayer.getBodyLayerStack().getRowReorderLayer().getRowIndexOrder()) {
                        DocumentItemDTO objToRenumber = gridListLayer.getBodyDataProvider().getRowObject(rowIndex);
                        objToRenumber.getDocumentItem().setPosNr(++newIdx);
                    }
                }
            }
        });
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));
        
        return natTable;
    }

    @Override
    protected void createDefaultContextMenu() {
        super.createDefaultContextMenu();

        final Menu e4Menu = createContextMenu(natTable);

        // remove the menu reference from NatTable instance
        natTable.setMenu(null);
        natTable.getUiBindingRegistry().registerMouseDownBinding(new MouseEventMatcher(SWT.NONE, GridRegion.BODY, MouseEventMatcher.RIGHT_BUTTON),
                new PopupMenuAction(e4Menu));

    }

    /**
     * @param reverseMap
     * @param columnLabelAccumulator
     * @param cellLabel 
     * @param descriptor 
     */
    private void registerColumnOverrides(BidiMap<DocumentItemListDescriptor, Integer> reverseMap, ColumnOverrideLabelAccumulator columnLabelAccumulator, 
            DocumentItemListDescriptor descriptor, String... cellLabel) {
        // it's null if the column doesn't exist (because of e.g. preferences)
        if(reverseMap.get(descriptor) != null) {
            columnLabelAccumulator.registerColumnOverrides(reverseMap.get(descriptor), cellLabel);
        }
    }
    

    @Override
    protected void postConfigureNatTable(NatTable natTable) {
        //as the autoconfiguration of the NatTable is turned off, we have to add the 
        //DefaultNatTableStyleConfiguration and the ConfigRegistry manually 
        natTable.setConfigRegistry(configRegistry);
//        natTable.addConfiguration(new NoHeaderRowOnlySelectionBindings());
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        // enable sorting on single click on the column header
        natTable.addConfiguration(new SingleClickSortConfiguration());
        natTable.addConfiguration(new DocumentItemTableConfiguration());
        natTable.addConfiguration(new DefaultRowReorderLayerConfiguration());
//        natTable.addConfiguration(new DebugMenuConfiguration(natTable));
        natTable.setBackground(GUIHelper.COLOR_WHITE);
        // nur für das Headermenü, falls das mal irgendwann gebraucht werden sollte
        //      natTable.addConfiguration(new HeaderMenuConfiguration(n6));
        
        // register right click as a selection event for the whole row
        natTable.getUiBindingRegistry().registerFirstMouseDownBinding(
                new MouseEventMatcher(SWT.NONE, GridRegion.BODY, MouseEventMatcher.RIGHT_BUTTON),

                new IMouseAction() {

                    ViewportSelectRowAction selectRowAction = new ViewportSelectRowAction(false, false);
                                
                    @Override
                    public void run(NatTable natTable, MouseEvent event) {
                        int rowPosition = natTable.getRowPositionByY(event.y);
                        if(!selectionLayer.isRowPositionSelected(rowPosition)) {
                            selectRowAction.run(natTable, event);
                        }                   
                    }
                });

        natTable.configure();
    }
    
    private void initItemsList() {

        // Create a set of new temporary items.
        // These items exist only in the memory.
        // If the editor is opened, the items from the document are
        // copied to this item set. If the editor is closed or saved,
        // these items are copied back to the document and to the data base.
        List<DocumentItemDTO> wrappedItems = new ArrayList<>();
//        @SuppressWarnings("unused")
//        DocumentItem dummyItem = document.getItems().get(0);
//        List<DocumentItemDTO> wrappedItems = document.getItems().stream().map(DocumentItemDTO::new).collect(Collectors.toList());
        for (DocumentItem item : document.getItems()) {
            wrappedItems.add(new DocumentItemDTO(item));
        }
        wrappedItems.sort(Comparator.comparing((DocumentItemDTO d) -> d.getDocumentItem().getPosNr()));
        documentItemsListData = GlazedLists.eventList(wrappedItems);

        //            // Set the sign
        //            if (parentSign != documentType.sign())
        //                newItem = new DataSetItem(item, -1);
        //            else
        //                newItem = new DataSetItem(item);

        // Reset the property "optional" from all items if the parent document was an offer
        // the parents document type
        DocumentType documentTypeParent = document.getSourceDocument() != null ? DocumentType.findByKey(document.getSourceDocument().getBillingType()
                .getValue()) : DocumentType.NONE;
        if (documentTypeParent == DocumentType.OFFER) {
            getDocumentItemsListData().forEach(item -> item.getDocumentItem().setOptional(Boolean.FALSE));
        }

        // Show the column "optional" if at least one item
        // with this property set was found
        Optional<DocumentItemDTO> optionalValue = getDocumentItemsListData().stream().filter(item -> Optional.ofNullable(item.getDocumentItem().getOptional()).orElse(Boolean.FALSE)).findFirst();
        containsOptionalItems = optionalValue.isPresent();

        // Show the columns discount if at least one item
        // with a discounted price was found
        Optional<DocumentItemDTO> discountedValue = getDocumentItemsListData().stream().filter(item -> item.getDocumentItem().getItemRebate() != null).findFirst();
        containsDiscountedItems = discountedValue.isPresent();

        // Renumber all Items
        //renumberItems();
    }
    
    @Override
    public String getTableId() {
        return ID;
    }

    @Override
    protected String getEditorId() {
        return DocumentEditor.ID;
    }
    
    @Override
    protected String getEditorTypeId() {
        return DocumentEditor.class.getSimpleName();
    }

    @Override
    protected TopicTreeViewer<DummyStringCategory> createCategoryTreeViewer(Composite top) {
        return null; // no category tree needed at the moment
    }

    @Override
    protected String getPopupId() {
        return POPUP_ID;
    }

    @Override
    public void setCategoryFilter(String filter, TreeObjectType treeObjectType) {
        // nothing to do
    }

    @Override
    protected boolean isHeaderLabelEnabled() {
        return false;
    }

    @Override
    public void removeSelectedEntry() {
        if(selectionLayer.getFullySelectedRowPositions().length > 0) { 
            DocumentItemDTO objToDelete = gridListLayer.getBodyDataProvider().getRowObject(selectionLayer.getFullySelectedRowPositions()[0]);
                List<DocumentItemDTO> tmpList = getDocumentItemsListData().stream()
                        .filter(d -> d != objToDelete)
                        .collect(Collectors.toList());
                // TODO RENUMBER!!!!
//                IntSupplier i = ()-> Integer.MAX_VALUE;
//                tmpList.stream().forEach(dto -> dto.getDocumentItem().setPosNr(3));
                getDocumentItemsListData().clear();
                getDocumentItemsListData().addAll(tmpList);
        } else {
            log.debug("no rows selected!");
        }
    }


    /**
     * Set the "novat" in all items. If a document is marks as "novat", the VAT
     * of all items is set to "0.0%"
     */
    public void setItemsNoVat(Boolean noVat) {
        getDocumentItemsListData().forEach(item -> item.getDocumentItem().setNoVat(noVat));
    }

    /**
     * Adds an empty item
     * 
     * @param newItem
     *            The new item
     */
    public void addNewItem(DocumentItemDTO newItem) {
//      newItem.setIntValueByKey("id", -(items.getDatasets().size() + 1));
        getDocumentItemsListData().add(newItem);
    }

    /**
     * Renumber all items
     */
    private void renumberItems () {
        
        int no = 1;
        // renumber all items
//      for (DocumentItem item : items) {
//          item.row = no;
//          no++;
//      }
    }

    class DocumentItemTableConfiguration extends AbstractRegistryConfiguration {

        @Override
        public void configureRegistry(IConfigRegistry configRegistry) {
            Style styleLeftAligned = new Style();
            styleLeftAligned.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
            Style styleRightAligned = new Style();
            styleRightAligned.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
            Style styleCentered = new Style();
            styleCentered.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
            
//            //add the style configuration for hover
//            Style style = new Style();
//            style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_YELLOW);
//            configRegistry.registerConfigAttribute(
//                    CellConfigAttributes.CELL_STYLE, 
//                    style, 
//                    DisplayMode.HOVER);

            // default style for the most of the cells
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, // attribute to apply
                                                   styleLeftAligned,                // value of the attribute
                                                   DisplayMode.NORMAL,              // apply during normal rendering i.e not during selection or edit
                                                   GridRegion.BODY.toString());     // apply the above for all cells with this label
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.ALWAYS_EDITABLE, 
                    DisplayMode.EDIT, TEXT_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.ALWAYS_EDITABLE, 
                    DisplayMode.EDIT, DESCRIPTION_CELL_LABEL);
            
            // center position number
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    styleCentered,      
                    DisplayMode.NORMAL, POSITIONNUMBER_CELL_LABEL); 
            
            // for number values (e.g., quantity)
            TextCellEditor textCellEditor = new TextCellEditor();
            textCellEditor.setErrorDecorationEnabled(true);
            textCellEditor.setDecorationPositionOverride(SWT.LEFT | SWT.TOP);
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR, 
                    textCellEditor, 
                    DisplayMode.NORMAL, DECIMAL_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.ALWAYS_EDITABLE, 
                    DisplayMode.EDIT, DECIMAL_CELL_LABEL);
            configRegistry.registerConfigAttribute( 
                    CellConfigAttributes.DISPLAY_CONVERTER, 
                    new DefaultDoubleDisplayConverter(), 
                    DisplayMode.EDIT, DECIMAL_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL, DECIMAL_CELL_LABEL ); 
            
            // description column
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR, 
                    new MultiLineTextCellEditor(false),
                    DisplayMode.NORMAL, 
                    DESCRIPTION_CELL_LABEL);
            
          //register a combobox editor for VAT values 
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.ALWAYS_EDITABLE, 
                    DisplayMode.EDIT, VAT_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL, VAT_CELL_LABEL ); 
            configRegistry.registerConfigAttribute( 
                    CellConfigAttributes.CELL_PAINTER, 
                    new ComboBoxPainter(), 
                    DisplayMode.NORMAL, VAT_CELL_LABEL);
            VatValueComboProvider dataProvider = new VatValueComboProvider(vatsDAO.findAll());
            ComboBoxCellEditor vatValueCombobox = new ComboBoxCellEditor(dataProvider);
            vatValueCombobox.setFreeEdit(false);
            configRegistry.registerConfigAttribute( 
                    EditConfigAttributes.CELL_EDITOR, 
                    vatValueCombobox, 
                    DisplayMode.NORMAL, VAT_CELL_LABEL); 
            configRegistry.registerConfigAttribute( 
                    CellConfigAttributes.DISPLAY_CONVERTER, 
                    new VatDisplayConverter(), 
                    DisplayMode.NORMAL, VAT_CELL_LABEL); 
            
            // for optional values
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR, 
                    new CheckBoxCellEditor(), 
                    DisplayMode.EDIT, OPTIONAL_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, 
                    new CheckBoxPainter(Icon.COMMAND_CHECKED.getImage(IconSize.DefaultIconSize), GUIHelper.getImage("arrow_down")), 
                 //   new DefaultCheckmarkPainter(),
                    DisplayMode.NORMAL, OPTIONAL_CELL_LABEL);  
//            configRegistry.registerConfigAttribute(
//                    CellConfigAttributes.CELL_STYLE,
//                    styleCentered,      
//                    DisplayMode.NORMAL, OPTIONAL_CELL_LABEL); 
            //using a CheckBoxCellEditor also needs a Boolean conversion to work correctly
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER, 
                    new DefaultBooleanDisplayConverter(), 
                    DisplayMode.NORMAL, 
                    OPTIONAL_CELL_LABEL);

            // for discount values
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL, PERCENT_CELL_LABEL ); 
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new PercentageDisplayConverter(),
                    DisplayMode.NORMAL, PERCENT_CELL_LABEL);
            configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.ALWAYS_EDITABLE, 
                    DisplayMode.EDIT, PERCENT_CELL_LABEL);
            
            // have a little space between cell border and value
            CellPainterWrapper paddedTextPainter = new PaddingDecorator(new TextPainter(), 0, 5, 0, 0);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    paddedTextPainter,
                    DisplayMode.NORMAL, PERCENT_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    paddedTextPainter,
                    DisplayMode.NORMAL, DECIMAL_CELL_LABEL);

            // for monetary values
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL, MONEYVALUE_CELL_LABEL ); 
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.ALWAYS_EDITABLE, 
                    DisplayMode.EDIT, MONEYVALUE_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new MoneyDisplayConverter(),
                    DisplayMode.NORMAL, MONEYVALUE_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DefaultDisplayConverter(),
                    DisplayMode.EDIT, MONEYVALUE_CELL_LABEL);
            
            // total value is never editable
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL, TOTAL_MONEYVALUE_CELL_LABEL ); 
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new MoneyDisplayConverter(),
                    DisplayMode.NORMAL, TOTAL_MONEYVALUE_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.NEVER_EDITABLE, 
                    DisplayMode.EDIT, TOTAL_MONEYVALUE_CELL_LABEL);
            
            // for product pictures
            // the cell has to be "editable" since you can't launch the dialog else
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE, 
                    new IEditableRule() {
                        
                        @Override
                        public boolean isEditable(int columnIndex, int rowIndex) {
                            return false;
                        }
                        
                        @Override
                        public boolean isEditable(ILayerCell cell, IConfigRegistry configRegistry) {
                            // the picture dialog in the document's item list table
                            // is only visible if a picture is contained in the article
                            return cell.getDataValue() != null;
                        }
                    }, 
                    DisplayMode.EDIT, PICTURE_CELL_LABEL);
            // open dialog in a new window
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.OPEN_IN_DIALOG,
                    Boolean.TRUE,
                    DisplayMode.EDIT,
                    PICTURE_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    styleCentered,      
                    DisplayMode.NORMAL, PICTURE_CELL_LABEL); 
            CellImagePainter cellImagePainter = new CellImagePainter();
            cellImagePainter.setCalculateByHeight(true);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, 
                    cellImagePainter,
                    DisplayMode.NORMAL, PICTURE_CELL_LABEL);                        
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR, 
                    new PictureViewEditor(),
                    DisplayMode.NORMAL, 
                    PICTURE_CELL_LABEL);
            
            //configure custom dialog settings
            Display display = Display.getCurrent();
            Map<String, Object> editDialogSettings = new HashMap<String, Object>();
            editDialogSettings.put(CellEditDialog.DIALOG_SHELL_TITLE, msg.dialogProductPicturePreview);
            editDialogSettings.put(CellEditDialog.DIALOG_SHELL_ICON, display.getSystemImage(SWT.ICON_INFORMATION));
            editDialogSettings.put(CellEditDialog.DIALOG_SHELL_RESIZABLE, Boolean.TRUE);
            
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.EDIT_DIALOG_SETTINGS, 
                    editDialogSettings,
                    DisplayMode.EDIT,
                    PICTURE_CELL_LABEL);
        }
    }

    /**
     * @return the documentItemsListData
     */
    public EventList<DocumentItemDTO> getDocumentItemsListData() {
        return documentItemsListData;
    }

    @Override
    public void changeToolbarItem(TreeObject treeObject) {
        // no action needed since there's no toolbar
    }
    
    @Override
    protected String getToolbarAddItemCommandId() {
        // This error should'nt occur since we've overridden the changeToolbarItem method.
        throw new UnsupportedOperationException("no action needed since there's no toolbar");
    }

    @Override
    protected MToolBar getMToolBar() {
        // This error should'nt occur since we've overridden the changeToolbarItem method.
        throw new UnsupportedOperationException("Inside a list table there's no toolbar.");
    }

    @Override
    protected EntityGridListLayer<DocumentItemDTO> getGridLayer() {
        throw new UnsupportedOperationException("Wrong call for a GridLayer.");
    }

    @Override
    protected AbstractDAO<DocumentItemDTO> getEntityDAO() {
        throw new UnsupportedOperationException("Inside a list table there's no extra DAO.");
    }
}
