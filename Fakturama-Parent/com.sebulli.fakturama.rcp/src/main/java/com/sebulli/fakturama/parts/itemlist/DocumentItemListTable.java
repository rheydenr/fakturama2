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

import java.io.File;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.DualHashBidiMap;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
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
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDoubleDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.PercentageDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.CheckBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ComboBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.reorder.config.DefaultRowReorderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.sebulli.fakturama.dto.DocumentItemDTO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.DummyStringCategory;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;
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
     * Event Broker for receiving update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;
    
    @Inject
    private ESelectionService selectionService;
    
    @Inject
    private EModelService modelService;

    @Inject
    @Preference
    private IEclipsePreferences preferences;
    
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

    
    protected static final String POPUP_ID = "com.sebulli.fakturama.documentitemlist.popup";

    private EntityGridListLayer<DocumentItemDTO> gridListLayer;

    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    private SelectionLayer selectionLayer;

    // ID of this view
    public static final String ID = "fakturama.document.itemTable";
    
    /**
     * Entry point for this class. Here the whole Composite is built.
     * 
     * @param parent
     * @param document
     * @param useGross 
     * @return
     */
    public Control createPartControl(Composite parent, Document document, boolean useGross,
            int netgross) {
        log.info("create DocumentItem list part");
        this.document = document;
        this.documentType = DocumentType.findByKey(document.getBillingType().getValue());
        this.useGross = useGross;
        
        // Get some settings from the preference store
        if (netgross == DocumentSummary.ROUND_NOTSPECIFIED) {
            useGross = (preferences.getInt(Constants.PREFERENCES_DOCUMENT_USE_NET_GROSS, 1) == 1);
        } else {
            useGross = (netgross == DocumentSummary.ROUND_GROSS_VALUES);
        }
        
        this.top = super.createPartControl(parent, DocumentItemDTO.class, false, ID);
//
//        DataLayer tableDataLayer = gridLayer.getBodyDataLayer();
//        tableDataLayer.setColumnPercentageSizing(true);
        
//        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);        
        // Listen to double clicks (omitted at the moment, perhaps at a later time
//        hookDoubleClickCommand(natTable, gridLayer);
        return top;
    }


///**
// * Create the default context menu 
// */
//private void createContextMenu(TableViewer tableViewerItems) {
//    
//    //Cancel, if there are no items
//    if (tableViewerItems == null)
//        return;
//    
//    menuManager = new MenuManager();
//    menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
//    tableViewerItems.getTable().setMenu(menuManager.createContextMenu(tableViewerItems.getTable()));
//
//    getSite().registerContextMenu("com.sebulli.fakturama.editors.DocumentEditor.tableViewerItems.contextmenu", menuManager, tableViewerItems);
//    getSite().setSelectionProvider(tableViewerItems);
//    
//    // Add up/down and delete actions
//    menuManager.add(new MoveEntryUpAction());
//    menuManager.add(new MoveEntryDownAction());
//    menuManager.add(new DeleteDataSetAction());
//}
//
    
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
        final BidiMap propertyNamesList = new DualHashBidiMap();
        
        if(preferences.getBoolean(Constants.PREFERENCES_DOCUMENT_USE_ITEM_POS, false)) {
            propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.POSITION);
        }

        if (containsOptionalItems || preferences.getBoolean(Constants.PREFERENCES_OPTIONALITEMS_USE, false) && (documentType == DocumentType.OFFER)) {
           propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.OPTIONAL);
        }

        propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.QUANTITY);
        
        if (preferences.getBoolean(Constants.PREFERENCES_PRODUCT_USE_QUNIT, false)) {
           propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.QUNIT);
        }
        
        if (preferences.getBoolean(Constants.PREFERENCES_PRODUCT_USE_ITEMNR, false)) {
           propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.ITEMNUMBER);
        }
        
        if (preferences.getBoolean(Constants.PREFERENCES_DOCUMENT_USE_PREVIEW_PICTURE, false)) {
           propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.PICTURE);
        }        
        
        propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.NAME);
        propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.DESCRIPTION);

        if (documentType.hasPrice()) {
            propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.VAT); 
            
            // "$ItemGrossPrice" (if useGross = true) or "price" (if useGross = false)
            propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.UNITPRICE);
            
            if (containsDiscountedItems || preferences.getBoolean(Constants.PREFERENCES_DOCUMENT_USE_DISCOUNT_EACH_ITEM, false)) {
                propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.DISCOUNT);
            } 
            
            // useGross = true => "$ItemGrossTotal", useGross = false => "$ItemNetTotal"
            propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.TOTALPRICE);
        }
        
        @SuppressWarnings("unchecked")
        List<DocumentItemListDescriptor> tmpList2 = new ArrayList<DocumentItemListDescriptor>(propertyNamesList.values());
        String[] propertyNames = tmpList2.stream().map(DocumentItemListDescriptor::getPropertyName).collect(Collectors.toList()).toArray(new String[]{});
        final IColumnPropertyAccessor<DocumentItem> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<>(propertyNames);
        
        // Add derived column
        final IColumnPropertyAccessor<DocumentItemDTO> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<DocumentItemDTO>() {

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
                    String imgPath = (String) columnPropertyAccessor.getDataValue(rowObject.getDocumentItem(), columnIndex);
                    String picturePath = preferences.get(Constants.GENERAL_WORKSPACE, "") + Constants.PRODUCT_PICTURE_FOLDER;
                    retval = getScaledImage(picturePath + imgPath);
                    break;
                case VAT:
                    VAT vat = (VAT) columnPropertyAccessor.getDataValue(rowObject.getDocumentItem(), columnIndex);
                    if(vat != null) {
                        retval = vat.getTaxValue();
                    } else {
                        retval = Double.valueOf(0.0);
                    }
                    break;
                case UNITPRICE:
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

            public void setDataValue(DocumentItemDTO rowObject, int columnIndex, Object newValue) {
                DocumentItemListDescriptor descriptor = (DocumentItemListDescriptor) propertyNamesList.get(columnIndex);
                switch (descriptor) {
                case OPTIONAL:
                    rowObject.getDocumentItem().setOptional((Boolean) newValue);
                    break;
                case QUANTITY:
                    rowObject.getDocumentItem().setQuantity((Double) newValue);
                    break;
                case QUNIT:
                    rowObject.getDocumentItem().setQuantityUnit((String) newValue);
                    break;
                case ITEMNUMBER:
                    rowObject.getDocumentItem().setItemNumber((String) newValue);
                    break;
                case NAME:
                    rowObject.getDocumentItem().setName((String) newValue);
                    break;
                case DESCRIPTION:
                    rowObject.getDocumentItem().setDescription((String) newValue);
                    break;
                case DISCOUNT:
                    rowObject.getDocumentItem().setItemRebate((Double) newValue);
                    break;
                case PICTURE:
                    // we have to build the picture path
                    String imgPath = (String) columnPropertyAccessor.getDataValue(rowObject.getDocumentItem(), columnIndex);
//                    String picturePath = preferences.get(Constants.GENERAL_WORKSPACE, "") + Constants.PRODUCT_PICTURE_FOLDER;
//                    retval = getScaledImage(picturePath + imgPath);
                    break;
                case VAT:
                    VAT vat = (VAT) columnPropertyAccessor.getDataValue(rowObject.getDocumentItem(), columnIndex);
//                    if(vat != null) {
//                        retval = vat.getTaxValue();
//                    } else {
//                        retval = Double.valueOf(0.0);
//                    }
                    break;
                case UNITPRICE:
                    rowObject.getDocumentItem().setPrice((Double) newValue);
                    break;
                default:
                    break;
                }
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
        gridListLayer = new EntityGridListLayer<>(getDocumentItemsListData(), propertyNames, derivedColumnPropertyAccessor, rowIdAccessor, configRegistry);
        DataLayer tableDataLayer = gridListLayer.getBodyDataLayer();
        tableDataLayer.setColumnPercentageSizing(true);
        
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
        BidiMap reverseMap = propertyNamesList.inverseBidiMap();
        ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(gridListLayer.getBodyLayerStack());
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.OPTIONAL, OPTIONAL_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.PICTURE, PICTURE_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.VAT, VAT_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.DISCOUNT, PERCENT_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.UNITPRICE, MONEYVALUE_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.TOTALPRICE, TOTAL_MONEYVALUE_CELL_LABEL);
        
        // "normal" columns are always editable
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.DESCRIPTION, TEXT_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.ITEMNUMBER, TEXT_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.NAME, TEXT_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.QUANTITY, DECIMAL_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.QUNIT, TEXT_CELL_LABEL);

        final NatTable natTable = new NatTable(tableComposite /*, 
                SWT.NO_REDRAW_RESIZE| SWT.DOUBLE_BUFFERED | SWT.BORDER*/, gridListLayer.getGridLayer(), false);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));
        
        // Register label accumulator
        gridListLayer.getBodyLayerStack().setConfigLabelAccumulator(columnLabelAccumulator);

        // Register your custom cell painter, cell style, against the label applied to the cell.
        //      addImageTextToColumn(configRegistry, natTable, gridLayer.getBodyDataProvider());
        return natTable;
    }


    /**
     * @param reverseMap
     * @param columnLabelAccumulator
     * @param cellLabel 
     * @param descriptor 
     */
    private void registerColumnOverrides(BidiMap reverseMap, ColumnOverrideLabelAccumulator columnLabelAccumulator, 
            DocumentItemListDescriptor descriptor, String... cellLabel) {
        // it's null if the column doesn't exist (because of e.g. preferences)
        if(reverseMap.get(descriptor) != null) {
            columnLabelAccumulator.registerColumnOverrides((Integer)reverseMap.get(descriptor), cellLabel);
        }
    }
    

    @Override
    protected void postConfigureNatTable(NatTable natTable) {
        //as the autoconfiguration of the NatTable is turned off, we have to add the 
        //DefaultNatTableStyleConfiguration and the ConfigRegistry manually 
        natTable.setConfigRegistry(configRegistry);
//        natTable.addConfiguration(new NoHeaderRowOnlySelectionBindings());
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new DocumentItemTableConfiguration());
        natTable.addConfiguration(new DefaultRowReorderLayerConfiguration());
        natTable.setBackground(GUIHelper.COLOR_WHITE);
        // nur für das Headermenü, falls das mal irgendwann gebraucht werden sollte
        //      natTable.addConfiguration(new HeaderMenuConfiguration(n6));

        natTable.configure();
    }
    
    private void initItemsList() {

        // Create a set of new temporary items.
        // These items exist only in the memory.
        // If the editor is opened, the items from the document are
        // copied to this item set. If the editor is closed or saved,
        // these items are copied back to the document and to the data base.
        List<DocumentItemDTO> wrappedItems = new ArrayList<>();
        for (DocumentItem item : document.getItems()) {
            wrappedItems.add(new DocumentItemDTO(item));
        }
        setDocumentItemsListData(GlazedLists.eventList(wrappedItems));

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
        Optional<DocumentItemDTO> optionalValue = getDocumentItemsListData().stream().filter(item -> item.getDocumentItem().getOptional()).findFirst();
        if (optionalValue.isPresent()) {
            containsOptionalItems = true;
        }

        // Show the columns discount if at least one item
        // with a discounted price was found
        Optional<DocumentItemDTO> discountedValue = getDocumentItemsListData().stream().filter(item -> item.getDocumentItem().getItemRebate() != null).findFirst();
        if (discountedValue.isPresent()) {
            containsDiscountedItems = true;
        }

        // Renumber all Items
        renumberItems();
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
                List<DocumentItemDTO> tmpList = getDocumentItemsListData().stream().filter(d -> d != objToDelete).collect(Collectors.toList());
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
  //    
//      // Refresh the table viewer
//      if (tableViewerItems != null)
//          tableViewerItems.refresh();
        
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

            // default style for the most of the cells
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, // attribute to apply
                                                   styleLeftAligned,                // value of the attribute
                                                   DisplayMode.NORMAL,              // apply during normal rendering i.e not during selection or edit
                                                   GridRegion.BODY.toString());     // apply the above for all cells with this label
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.ALWAYS_EDITABLE, 
                    DisplayMode.EDIT, TEXT_CELL_LABEL);
            
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
            
          //register a combobox editor for VAT values 
            configRegistry.registerConfigAttribute( 
                    CellConfigAttributes.CELL_PAINTER, 
                    new ComboBoxPainter(), 
                    DisplayMode.NORMAL, VAT_CELL_LABEL); 
            configRegistry.registerConfigAttribute( 
                    EditConfigAttributes.CELL_EDITOR, 
                    new ComboBoxCellEditor(Arrays.asList(new String[] {"Value1", "Value2"} )), 
                    DisplayMode.EDIT, VAT_CELL_LABEL); 
            configRegistry.registerConfigAttribute( 
                    EditConfigAttributes.CELL_EDITOR, 
                    new ComboBoxCellEditor(Arrays.asList(new String[] {"Value1", "Value2"} )), 
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

            // for monetary values
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL, MONEYVALUE_CELL_LABEL ); 
            configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.ALWAYS_EDITABLE, 
                    DisplayMode.EDIT, MONEYVALUE_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new MoneyDisplayConverter(),
                    DisplayMode.NORMAL, MONEYVALUE_CELL_LABEL);
            
            // total value is never editable
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL, TOTAL_MONEYVALUE_CELL_LABEL ); 
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new MoneyDisplayConverter(),
                    DisplayMode.NORMAL, TOTAL_MONEYVALUE_CELL_LABEL);
            configRegistry.registerConfigAttribute(EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.NEVER_EDITABLE, 
                    DisplayMode.EDIT, TOTAL_MONEYVALUE_CELL_LABEL);
            
            // for product pictures
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                    styleCentered,      
                    DisplayMode.NORMAL, PICTURE_CELL_LABEL); 
            CellImagePainter cellImagePainter = new CellImagePainter();
            cellImagePainter.setCalculateByHeight(true);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, 
                    cellImagePainter,
                    DisplayMode.NORMAL, PICTURE_CELL_LABEL);
        }
    }

    /**
     * Returns an image. Clients do not need to dispose the image, it will be
     * disposed automatically.
     * 
     * @return an {@link Image}
     */
    private Image getImage(String path) {
        Image image = JFaceResources.getImageRegistry().get(path);
        if (image == null) {
            addIconImageDescriptor(path);
            image = JFaceResources.getImageRegistry().get(path);
        }
        return image;
    }

    /**
     * Scale the given image to {@link CellImagePainter#MAX_IMAGE_PREVIEW_WIDTH}
     * px width. Copied from old ProductPictureDialog class.
     * 
     * @param pictureName
     * @return Image
     */
    private Image getScaledImage(String pictureName) {
        // The scaled image with width and height (used to resize the dialog)
        Image scaledImage = null;
        // Display the picture, if it is set.
        if (!pictureName.isEmpty()) {

            int width = 300;
            int height = 200;

            // Load the image, based on the picture name
            // but at first check if it exists
            if(Files.notExists(Paths.get(pictureName))) {
                return null;
            }
            Image image = getImage(pictureName);

            // Get the pictures size
            width = image.getBounds().width;
            height = image.getBounds().height;
            
            // Scale the image to 64x48 Pixel
            if (width != 0 && height != 0) {
                // Picture is wider than height.
                if (width >= 64*height/48) {
                    height = height * 64 / width;
                    width = 64;
                }
                else { //if (height > ((48*width)/64)) {
                    width = width * 48 / height;
                    height = 48;
                }
            }
            
            scaledImage = new Image(image.getDevice(), image.getImageData().scaledTo(width, height));
            
//            // Scale it to maximum 250px
//            int maxWidth = MAX_IMAGE_PREVIEW_WIDTH;
//
//            // Maximum picture width 
//            if (width > maxWidth) {
//                height = maxWidth * height / width;
//                width = maxWidth;
//
//                // Rescale the picture to the maximum width
//                scaledImage = new Image(image.getDevice(), image.getImageData().scaledTo(width, height));
//            }
//            else {
//                scaledImage = image;
//            }
        }
        return scaledImage;
    }

    /**
     * Add an image descriptor for a specific key and {@link IconSize} to the
     * global {@link ImageRegistry}
     * 
     * @param name
     * @param is
     * @return <code>true</code> if successfully added, else <code>false</code>
     */
    private boolean addIconImageDescriptor(String path) {
        try {
            URL fileLocation = new File(path).toURI().toURL();
            ImageDescriptor id = ImageDescriptor.createFromURL(fileLocation);
            JFaceResources.getImageRegistry().put(path, id);
        }
        catch (MissingResourceException | MalformedURLException | IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    /**
     * @return the documentItemsListData
     */
    public EventList<DocumentItemDTO> getDocumentItemsListData() {
        return documentItemsListData;
    }

    /**
     * @param documentItemsListData the documentItemsListData to set
     */
    public void setDocumentItemsListData(EventList<DocumentItemDTO> documentItemsListData) {
        this.documentItemsListData = documentItemsListData;
    }

    @Override
    public void changeToolbarItem(TreeObject treeObject) {
        // no action needed since there's no toolbar
    }
    
    @Override
    protected String getToolbarAddItemCommandId() {
        // This error should'nt occur since we've overridden the changeToolbarItem method.
        throw new UnsupportedOperationException("Inside a list table you can't create a new document.");
    }

    @Override
    protected MToolBar getMToolBar() {
        // This error should'nt occur since we've overridden the changeToolbarItem method.
        throw new UnsupportedOperationException("Inside a list table there's no toolbar.");
    }

}
