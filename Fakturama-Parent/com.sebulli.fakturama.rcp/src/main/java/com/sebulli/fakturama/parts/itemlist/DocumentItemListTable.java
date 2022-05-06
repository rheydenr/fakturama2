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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.money.MonetaryAmount;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.nebula.widgets.cdatetime.CDT;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
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
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditBindings;
import org.eclipse.nebula.widgets.nattable.edit.config.DefaultEditConfiguration;
import org.eclipse.nebula.widgets.nattable.edit.editor.CheckBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.MultiLineTextCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.gui.CellEditDialog;
import org.eclipse.nebula.widgets.nattable.edit.gui.ICellEditDialog;
import org.eclipse.nebula.widgets.nattable.extension.e4.selection.E4SelectionListener;
import org.eclipse.nebula.widgets.nattable.extension.nebula.cdatetime.CDateTimeCellEditor;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
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
import org.eclipse.nebula.widgets.nattable.selection.EditTraversalStrategy;
import org.eclipse.nebula.widgets.nattable.selection.ITraversalStrategy;
import org.eclipse.nebula.widgets.nattable.selection.MoveCellSelectionCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.command.SelectRowsCommand;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.NatEventData;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemProvider;
import org.eclipse.nebula.widgets.nattable.ui.menu.IMenuItemState;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuAction;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.action.ViewportSelectRowAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.javamoney.moneta.Money;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.DocumentReceiverDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.dto.DocumentItemDTO;
import com.sebulli.fakturama.dto.Price;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.misc.INumberFormatterService;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.DummyStringCategory;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.parts.converter.DateDisplayConverter;
import com.sebulli.fakturama.parts.converter.DoublePercentageDisplayConverter;
import com.sebulli.fakturama.parts.converter.VatDisplayConverter;
import com.sebulli.fakturama.resources.ITemplateResourceManager;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;
import com.sebulli.fakturama.util.DocumentItemUtil;
import com.sebulli.fakturama.util.DocumentTypeUtil;
import com.sebulli.fakturama.util.ProductUtil;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.common.CellImagePainter;
import com.sebulli.fakturama.views.datatable.common.ListSelectionStyleConfiguration;
import com.sebulli.fakturama.views.datatable.common.MoneyDisplayConverter;
import com.sebulli.fakturama.views.datatable.layer.EntityGridListLayer;
import com.sebulli.fakturama.views.datatable.tree.model.TreeObject;
import com.sebulli.fakturama.views.datatable.tree.ui.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

/**
 * The table of items inside a Document (invoice, order etc). 
 */
public class DocumentItemListTable extends AbstractViewDataTable<DocumentItemDTO, DummyStringCategory> {

    @Inject
    private ESelectionService selectionService;
    
    @Inject
    private EMenuService menuService;
    
    @Inject
    private IEclipseContext context;
    
    @Inject
    private VatsDAO vatsDAO;
	
	@Inject
	private ILocaleService localeUtil;
	
	@Inject
	private INumberFormatterService numberFormatterService;
	
	@Inject
	private DocumentReceiverDAO documentReceiverDao;
	   
    @Inject
    private ITemplateResourceManager resourceManager;

    // ID of this view
    public static final String ID = "fakturama.document.itemTable";
    
    private EventList<DocumentItemDTO> documentItemsListData;

    private Document document;
    private DocumentType documentType;
    
    /** for accessing the surrounding {@link DocumentEditor} we have to pull it in */
	private DocumentEditor container;

    // Flag if there are items with property "optional" set
    private boolean containsOptionalItems = false;

    // Flag if there are items with an discount set
    private boolean containsDiscountedItems = false;
//    private boolean useGross;
//    private int netgross = DocumentSummary.NOTSPECIFIED;
    
    /**
     * {@link VAT} entry for use in conjunction with "no VAT" entry
     */
    private VAT noVatReference = null;
    
    private static final String OPTIONAL_CELL_LABEL = "Optional_Cell_LABEL";
    private static final String PERCENT_CELL_LABEL = "Percent_Cell_LABEL";
    private static final String MONEYVALUE_CELL_LABEL = "MoneyValue_Cell_LABEL";
    private static final String TOTAL_MONEYVALUE_CELL_LABEL = "TotalMoneyValue_Cell_LABEL";
    private static final String PICTURE_CELL_LABEL = "Picture_Cell_LABEL";
    private static final String TEXT_CELL_LABEL = "Text_Cell_LABEL";
    private static final String DECIMAL_CELL_LABEL = "Decimal_Cell_LABEL";
    private static final String DATE_CELL_LABEL = "Date_Cell_LABEL";
    private static final String VAT_CELL_LABEL = "VAT_Cell_LABEL";
    private static final String DESCRIPTION_CELL_LABEL = "Description_Cell_LABEL";
    private static final String POSITIONNUMBER_CELL_LABEL = "Positionnumber_Cell_LABEL";

    protected static final String POPUP_ID = "com.sebulli.fakturama.documentitemlist.popup";

    private EntityGridListLayer<DocumentItemDTO> gridListLayer;

    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
//    private SelectionLayer selectionLayer;
    
    private ProductUtil productUtil;
    private DocumentItemUtil documentItemUtil;

    /**
     * Checks if the current editor uses sales equalization tax (this is only needed for some customers).
     */
    private boolean useSET = false;
    
    /**
     * Entry point for this class. Here the whole Composite is built.
     * 
     * @param parent
     * @param document
     * @param documentSummary 
     * @param useGross 
     * @return
     */
    public Control createPartControl(Composite parent, Document document,/* boolean useGross,*/DocumentEditor container,
            int netgross) {
        log.debug("create DocumentItem list part");
        this.document = document;
        this.documentType = DocumentType.findByKey(document.getBillingType().getValue());
        this.container = container;
        this.productUtil = ContextInjectionFactory.make(ProductUtil.class, context);
        this.documentItemUtil = ContextInjectionFactory.make(DocumentItemUtil.class, context);
        this.useSET = documentReceiverDao.isSETEnabled(document);
//        // Get some settings from the preference store
//        if (netgross == DocumentSummary.ROUND_NOTSPECIFIED) {
//            useGross = (eclipsePrefs.getInt(Constants.PREFERENCES_DOCUMENT_USE_NET_GROSS) == DocumentSummary.ROUND_NET_VALUES);
//        } else {
//            useGross = (netgross == DocumentSummary.ROUND_GROSS_VALUES);
//        }
        
        super.createPartControl(parent, DocumentItemDTO.class, false, ID);
        // Listen to double clicks (omitted at the moment, perhaps at a later time
//        hookDoubleClickCommand(natTable, gridLayer);
        return top;
    }
    

	/**
	 * Create the default context menu
	 */
	private Menu createContextMenu() {
		// Add up/down and delete actions
		MoveEntryUpMenuItem moveEntryUpHandler = ContextInjectionFactory.make(MoveEntryUpMenuItem.class, context);
		MoveEntryDownMenuItem moveEntryDownHandler = ContextInjectionFactory.make(MoveEntryDownMenuItem.class, context);
		
		IMenuItemProvider deleteMenuItem = new IMenuItemProvider() {

			@Override
			public void addMenuItem(NatTable natTable, Menu popupMenu) {
		        final MenuItem deleteItem = new MenuItem(popupMenu, SWT.PUSH);
		        deleteItem.setText(msg.mainMenuEditDeleteName);
				deleteItem.setImage(Icon.COMMAND_DELETE.getImage(IconSize.DefaultIconSize));
				deleteItem.setEnabled(true);
				deleteItem.setAccelerator(SWT.MOD1 + 'D');  // doesn't work :-(
				deleteItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						removeSelectedEntry();
					}
				});
			}
			
		};
		
		MenuManager menuManager = new MenuManager();
		Menu retval = new PopupMenuBuilder(natTable, menuManager)
				.withMenuItemProvider(CommandIds.CMD_MOVE_UP, moveEntryUpHandler)
				.withMenuItemProvider(CommandIds.CMD_MOVE_DOWN, moveEntryDownHandler)
				.withMenuItemProvider(CommandIds.CMD_DELETE_DATASET, deleteMenuItem)
			    .withEnabledState(
			    		CommandIds.CMD_MOVE_UP,
			            new IMenuItemState() {
			     
			                @Override
			                public boolean isActive(NatEventData natEventData) {
			                    return natEventData.getRowPosition() > 1;
			                }
			        })
			    .withEnabledState(
			    		CommandIds.CMD_MOVE_DOWN,
			    		new IMenuItemState() {
			    			
			    			@Override
			    			public boolean isActive(NatEventData natEventData) {
			    				return natEventData.getRowPosition() < getGridLayer().getBodyDataProvider().getRowCount();
			    			}
			    		})
				.build();

		return retval;
	}
	
	/**
	 * This is an alternative implementation of the context menu. It uses the definition from Application.e4xmi. 
	 * At the moment, it's unused because the {@link MenuItem}s aren't real handler implementations. 
	 * This could be changed in the future. 
	 * @return
	 */
	@SuppressWarnings("unused")
	private Menu createContextMenuFromE4Applicationmodel() {
		Menu retval = null;
		menuService.registerContextMenu(natTable, "com.sebulli.fakturama.documentitemlist.popup");
		// get the menu registered by EMenuService
		final Menu e4Menu = natTable.getMenu();
		
		// remove the menu reference from NatTable instance
		natTable.setMenu(null);
		natTable.addConfiguration(
		        new AbstractUiBindingConfiguration() {
		 
		    @Override
		    public void configureUiBindings(
		            UiBindingRegistry uiBindingRegistry) {
		        // add NatTable menu items
		        // and register the DisposeListener
		        new PopupMenuBuilder(natTable, e4Menu)
		            .build();
		 
		        // register the UI binding
		        uiBindingRegistry.registerMouseDownBinding(
		                new MouseEventMatcher(
		                        SWT.NONE,
		                        GridRegion.BODY,
		                        MouseEventMatcher.RIGHT_BUTTON),
		                new PopupMenuAction(e4Menu));
		    }
		});
		
		retval = new PopupMenuBuilder(natTable, e4Menu)
				.withEnabledState(CommandIds.CMD_MOVE_UP, new IMenuItemState() {

					@Override
					public boolean isActive(NatEventData natEventData) {
						return natEventData.getRowPosition() > 1;
					}
				}).withEnabledState(CommandIds.CMD_MOVE_DOWN, new IMenuItemState() {

					@Override
					public boolean isActive(NatEventData natEventData) {
						return natEventData.getRowPosition() < getGridLayer().getBodyDataProvider().getRowCount();
					}
				}).build();		
		return retval;
	}
    
/*
 * Move an item up or down
 * ==> done through NatTable mechanics
 */
    
    protected NatTable createListTable(Composite tableComposite) {  
        // fill the underlying data source (GlazedLists)
        initItemsList();

        final BidiMap<Integer, DocumentItemListDescriptor> propertyNamesList = createColumns();
        
        List<DocumentItemListDescriptor> tmpList2 = new ArrayList<DocumentItemListDescriptor>(propertyNamesList.values());
        String[] propertyNames = tmpList2.stream().map(DocumentItemListDescriptor::getPropertyName).collect(Collectors.toList()).toArray(new String[]{});
        final IColumnPropertyAccessor<DocumentItem> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<>(propertyNames);
        
        // Add derived column
        final IColumnPropertyAccessor<DocumentItemDTO> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<DocumentItemDTO>() {

            /**
             * Get the value to set to the editor
             */
            public Object getDataValue(DocumentItemDTO rowObject, int columnIndex) {
                Object retval;
                DocumentItemListDescriptor descriptor;
                if(columnIndex < 0) {
                	descriptor = DocumentItemListDescriptor.POSITION;
                } else {
                	descriptor = (DocumentItemListDescriptor) propertyNamesList.get(columnIndex);
                }
                try {
					switch (descriptor) {
	                case POSITION:
	//                    retval = eclipsePrefs.getBoolean(Constants.PREFERENCES_DOCUMENT_USE_ITEM_POS) ? rowObject.getDocumentItem().getPosNr() : -1.0;
	                	// we ALWAYS use a position number!
	                    retval = rowObject.getDocumentItem().getPosNr();
	                    break;
	                case QUANTITY:
	                    retval = numberFormatterService.doubleToFormattedQuantity(rowObject.getDocumentItem().getQuantity());
	                    break;
	                case OPTIONAL:
	                case QUNIT:
	                case WEIGHT:
	                case ITEMNUMBER:
	                case NAME:
	                case DESCRIPTION:
	                case DISCOUNT:
	                    retval = columnPropertyAccessor.getDataValue(rowObject.getDocumentItem(), columnIndex);
	                    break;
	                case VESTINGDATESTART:
	                case VESTINGDATEEND:
	                    retval = columnPropertyAccessor.getDataValue(rowObject.getDocumentItem(), columnIndex);
	                	if(retval == null) {
	                		retval = Calendar.getInstance().getTime();
	                	}
	                    break;
	                case PICTURE:
	                    // we have to build the picture path
	                    // opening the picture dialog (preview) occurs in the PictureViewEditor (via configuration)
	//                    String imgPath = (String) columnPropertyAccessor.getDataValue(rowObject.getDocumentItem(), columnIndex);
	//                    if (StringUtils.isNotBlank(imgPath)) {
	//                        String picturePath = eclipsePrefs.getString(Constants.GENERAL_WORKSPACE) + Constants.PRODUCT_PICTURE_FOLDER;
	//                        retval = picturePath + imgPath;
	//                    } else {
	//                        retval = null;
	//                    }
	                	retval = rowObject.getDocumentItem().getPicture();
	                    break;
	                case VAT:
	                    retval = noVatReference != null ? noVatReference 
	                    		: (VAT) columnPropertyAccessor.getDataValue(rowObject.getDocumentItem(), columnIndex);
	                    break;
	                case SALESEQUALIZATIONTAX:
	                    Price price = rowObject.getPrice(useSET);
//	                	Double tmpVat = (Double)columnPropertyAccessor.getDataValue(rowObject.getDocumentItem(), columnIndex);
//	                	retval = tmpVat != null ? DataUtils.getInstance().round(tmpVat, 3) : NumberUtils.DOUBLE_ZERO;
	                    retval = price.getTotalSalesEqTaxRounded();
	                	break;
	                case UNITPRICE:
	                    price = rowObject.getPrice(useSET);
						retval = container.getUseGross() 
	                			? price.getUnitGrossRounded() 
	                			: price.getUnitNetRounded();
	                    break;
	                case TOTALPRICE:
	                    int sign = container.getDocumentType().getSign();
	                    price = rowObject.getPrice(useSET);
	                    price.multiply(sign);
	                    if (container.getUseGross()) { // "$ItemGrossTotal"
	                        // Fill the cell with the total gross value of the item
	                        retval = price.getTotalGrossRounded();
	                    } else { // "$ItemNetTotal"
	                        // Fill the cell with the total net value of the item
	                        retval = price.getTotalNetRounded();
	                    }
	                    break;
	                default:
	                    retval = "???";
	                    break;
	                }
                } catch (Exception ex) {
                	retval = "ERROR!";
            		log.error("Error while displaying a value from DocumentItem (name=["+rowObject.getDocumentItem().getName()+"]) at column position ["+columnIndex+"]. Reason: " + ex.getMessage());
                }
                return retval;
            }

            /**
             * Sets the new value on the given element.
             * <h3>HINT</h3>
             * <p>Saving a new document causes to set the technical fields (dateAdded or modifiedBy) for the DocumentItem.
             * Therefore we have to check the "real" changes and ignore the only technical ones. This is achieved by 
             * a tiny PropertyChangeListener inside DocumentItemDTO, which tracks all "real" changes. The status
             * can be get with "isDocumentItemDirty()" method from DocumentItemDTO.</p>
             */
            public void setDataValue(DocumentItemDTO rowObject, int columnIndex, Object newValue) {
                DocumentItemListDescriptor descriptor = (DocumentItemListDescriptor) propertyNamesList.get(columnIndex);
                boolean calculate = true;
                switch (descriptor) {
                case OPTIONAL:
                    rowObject.getDocumentItem().setOptional((Boolean) newValue);
                    break;
                case QUANTITY:
                    Double oldQuantity = rowObject.getDocumentItem().getQuantity();
                    // Set the quantity
                    rowObject.getDocumentItem().setQuantity(Optional.ofNullable((Double) newValue).orElse(Double.valueOf(0.0)));
                    Product product = rowObject.getDocumentItem().getProduct();

                    // If the item is coupled with a product, get the graduated price
                    if (product != null) {

                        // Compare the price. Is it equal to the price of the product,
                        // then use the product price.
                        // If the price is not equal, it was modified. In this case, do not
                        // modify the price value.
                        Double oldPrice = rowObject.getDocumentItem().getPrice();
                        Double oldPriceByQuantity = productUtil.getPriceByQuantity(product, oldQuantity);

                        Double newPrice = productUtil.getPriceByQuantity(product, (Double) newValue);

                        if (DataUtils.getInstance().DoublesAreEqual(oldPrice, oldPriceByQuantity)) {
                            // Do not use 0.00€
                            //if (!DataUtils.DoublesAreEqual(newPrice, 0.0))
                            rowObject.getDocumentItem().setPrice(newPrice);
                        }
                        
                        rowObject.getDocumentItem().setQuantityUnit(documentItemUtil.getProductQuantityUnit(product, rowObject.getDocumentItem().getQuantity()));
                    }
                    break;
                case QUNIT:
                    rowObject.getDocumentItem().setQuantityUnit((String) newValue);
                    calculate = false; // no recalculation needed
                    break;
                case WEIGHT:
                	Double newWeight = ObjectUtils.defaultIfNull((Double) newValue, Double.valueOf(0.0));
                	rowObject.getDocumentItem().setWeight(newWeight);
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
                    Double discountValue = ObjectUtils.defaultIfNull((Double) newValue, Double.valueOf(0.0));
                    // Convert it to negative values
                    if (discountValue > 0) {
                        discountValue *= -1;
                    }
                    rowObject.getDocumentItem().setItemRebate(discountValue);
                    break;
                case PICTURE:
                    // setting a new picture isn't allowed in this context!
                    calculate = false; // no recalculation needed
                    break;
                case VAT:
                    // Set the VAT
                    if (newValue != null) {
            			// Set the vat and store the vat value before and after the modification.
            			Double oldVat = 1.0 + rowObject.getDocumentItem().getItemVat().getTaxValue();
                        rowObject.getDocumentItem().setItemVat((VAT) newValue);

            			// Modify the net value that the gross value stays constant.
            			if (container.getUseGross()) {
            				rowObject.getDocumentItem().setPrice(oldVat / (1 + ((VAT) newValue).getTaxValue()) * rowObject.getDocumentItem().getPrice());
            			}
                    }
                    break;
                case UNITPRICE:
                    String priceString = StringUtils.defaultString((String) newValue, "0").toLowerCase();
                    boolean useGross = container.getUseGross();
                    
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
                    
                    if(priceString.startsWith(",")) {
                    	priceString = "0" + priceString;
                    }

                    // Set the price as gross or net value.
                    // If the editor displays gross values, calculate the net value,
                    // because only net values are stored.
                    MonetaryAmount amount = Money.of(DataUtils.getInstance().StringToDouble(priceString), DataUtils.getInstance().getDefaultCurrencyUnit());
                    if (useGross) {
                        Price newPrice = new Price(amount, rowObject.getDocumentItem().getItemVat().getTaxValue(), rowObject.getDocumentItem().getNoVat(), useGross);
                        rowObject.getDocumentItem().setPrice(newPrice.getUnitNet().getNumber().doubleValue());
                    } else {
                        rowObject.getDocumentItem().setPrice(amount.getNumber().doubleValue());
                    }
                    break;
                case VESTINGDATESTART:
                    rowObject.getDocumentItem().setVestingPeriodStart((Date)newValue);
                    break;
                case VESTINGDATEEND:
                    rowObject.getDocumentItem().setVestingPeriodEnd((Date)newValue);
                    break;
                default:
                    break;
                }

                if(rowObject.isDocumentItemDirty()) {
                    Map<String, Object> event = new HashMap<>();
                    event.put("source", descriptor);
	                // Recalculate the total sum of the document if necessary
	                // do it via the messaging system and send a message to DocumentEditor
	                event.put(DocumentEditor.DOCUMENT_ID, document.getName());
	                event.put(DocumentEditor.DOCUMENT_RECALCULATE, calculate);
	                rowObject.setDocumentItemDirty(false);
	                
	                evtBroker.post(DocumentEditor.EDITOR_ID + UIEvents.TOPIC_SEP + "itemChanged", event);
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
		gridListLayer = new EntityGridListLayer<>(getDocumentItemsListData(), propertyNames,
				derivedColumnPropertyAccessor, rowIdAccessor, configRegistry, msg, true);
		
        // set default percentage width 
		gridListLayer.getBodyDataLayer().setColumnPercentageSizing(true);

        //set ISelectionProvider
        final RowSelectionProvider<DocumentItemDTO> selectionProvider = 
                new RowSelectionProvider<DocumentItemDTO>(gridListLayer.getSelectionLayer(), gridListLayer.getBodyDataProvider());
        
        //add a listener to the selection provider, in an Eclipse application you would do this
        //e.g. getSite().getPage().addSelectionListener()
        selectionProvider.addSelectionChangedListener((SelectionChangedEvent event) -> {
//                log.debug("Selection changed:");
                
    		IStructuredSelection structuredSelection = event.getStructuredSelection();
                selectionService.setSelection(structuredSelection.toList());
            }
        );
         
        // add some edit configuration
        gridListLayer.getGridLayer().addConfiguration(new DefaultEditBindings());
        gridListLayer.getGridLayer().addConfiguration(new DefaultEditConfiguration());
        gridListLayer.getGridLayer().addConfiguration(new DocumentItemTableConfiguration());
        
        // Create a label accumulator - adds custom labels to all cells which we
        // wish to render differently.
        BidiMap<DocumentItemListDescriptor, Integer> reverseMap = propertyNamesList.inverseBidiMap();
        ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(gridListLayer.getBodyDataLayer());
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.POSITION, POSITIONNUMBER_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.OPTIONAL, OPTIONAL_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.PICTURE, PICTURE_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.VAT, VAT_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.SALESEQUALIZATIONTAX, MONEYVALUE_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.DISCOUNT, PERCENT_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.UNITPRICE, MONEYVALUE_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.TOTALPRICE, TOTAL_MONEYVALUE_CELL_LABEL);
        
        // "normal" columns are always editable
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.DESCRIPTION, DESCRIPTION_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.VESTINGDATESTART, DATE_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.VESTINGDATEEND, DATE_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.ITEMNUMBER, TEXT_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.NAME, TEXT_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.QUANTITY, DECIMAL_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.WEIGHT, DECIMAL_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, DocumentItemListDescriptor.QUNIT, TEXT_CELL_LABEL);

        // Register label accumulator
        gridListLayer.getBodyDataLayer().setConfigLabelAccumulator(columnLabelAccumulator);
        
        // if a re-ordering of rows occurs we have to renumber the items
        gridListLayer.getBodyLayerStack().getRowReorderLayer().addLayerListener((ILayerEvent event) -> {
                if (event instanceof RowReorderEvent) {
                    RowReorderEvent evt = (RowReorderEvent) event;
                    evt.convertToLocal(gridListLayer.getBodyLayerStack().getRowReorderLayer());
                    int newIdx = 0; // documentItemsListData??
                    for (Integer rowIndex : gridListLayer.getBodyLayerStack().getRowReorderLayer().getRowIndexOrder()) {
                        DocumentItemDTO objToRenumber = gridListLayer.getBodyDataProvider().getRowObject(rowIndex);
                        objToRenumber.getDocumentItem().setPosNr(++newIdx);
                    }
                    getContainer().setDirty(true);
                }
            }
        );
        final NatTable natTable = new NatTable(tableComposite , /*
                SWT.NO_REDRAW_RESIZE| SWT.DOUBLE_BUFFERED | SWT.BORDER,
                // FIXME: Doesn't work! 
               ,gridListLayer.getViewportLayer()  */
		 gridListLayer.getGridLayer() , false);
        natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));

        // register a MoveCellSelectionCommandHandler with
        // TABLE_CYCLE_TRAVERSAL_STRATEGY for horizontal traversal
        // and AXIS_CYCLE_TRAVERSAL_STRATEGY for vertical traversal
        gridListLayer.getGridLayer().registerCommandHandler(
                new MoveCellSelectionCommandHandler(gridListLayer.getSelectionLayer(),
                        new EditTraversalStrategy(ITraversalStrategy.TABLE_CYCLE_TRAVERSAL_STRATEGY, natTable),
                        new EditTraversalStrategy(ITraversalStrategy.AXIS_CYCLE_TRAVERSAL_STRATEGY, natTable)));
        
        // register Delete command
        // https://stackoverflow.com/questions/31907288/delete-rows-from-nattable
        gridListLayer.getBodyDataLayer().registerCommandHandler(
                new DeleteRowCommandHandler<DocumentItemDTO>(gridListLayer.getBodyDataProvider().getList()));
        return natTable;
    }


    public void reloadItemList(Document document) {
        this.document = document;
        DocumentType documentType = DocumentTypeUtil.findByBillingType(document.getBillingType());
        
        if (!documentType.hasPrice()) {
            return;
        }
        
        getDocumentItemsListData().clear();
        
        List<DocumentItemDTO> documentItems = document.getItems().stream().map(DocumentItemDTO::new).collect(Collectors.toList());
        getDocumentItemsListData().addAll(documentItems);
    }

	private BidiMap<Integer, DocumentItemListDescriptor> createColumns() {
		// Create the table columns 
        // get the visible properties to show in list view along with their position index
        Integer columnIndex = Integer.valueOf(0);
        final BidiMap<Integer, DocumentItemListDescriptor> propertyNamesList = new DualHashBidiMap<>();

        if (containsOptionalItems || getEclipsePrefs().getBoolean(Constants.PREFERENCES_OPTIONALITEMS_USE) && (documentType == DocumentType.OFFER)) {
           propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.OPTIONAL);
        }

        propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.QUANTITY);
        
        if (getEclipsePrefs().getBoolean(Constants.PREFERENCES_PRODUCT_USE_QUNIT)) {
           propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.QUNIT);
        }
        
        if (getEclipsePrefs().getBoolean(Constants.PREFERENCES_PRODUCT_USE_WEIGHT)) {
        	propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.WEIGHT);
        }
        
        if (getEclipsePrefs().getBoolean(Constants.PREFERENCES_PRODUCT_USE_ITEMNR)) {
           propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.ITEMNUMBER);
        }
        
        if (getEclipsePrefs().getBoolean(Constants.PREFERENCES_PRODUCT_USE_PICTURE)) {
           propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.PICTURE);
        }        

        if (getEclipsePrefs().getInt(Constants.PREFERENCES_DOCUMENT_USE_VESTINGPERIOD) > 0) {
            propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.VESTINGDATESTART);
         }        
        
        if (getEclipsePrefs().getInt(Constants.PREFERENCES_DOCUMENT_USE_VESTINGPERIOD) > 1) {
        	propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.VESTINGDATEEND);
        }        
        
        propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.NAME);
        
        if (getEclipsePrefs().getBoolean(Constants.PREFERENCES_PRODUCT_USE_DESCRIPTION)) {
            propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.DESCRIPTION);
        }
        
        if (documentType.hasPrice() 
        		|| document.getBillingType().isDELIVERY() && getEclipsePrefs().getBoolean(Constants.PREFERENCES_DOCUMENT_DELIVERY_NOTE_ITEMS_WITH_PRICE)) {

            if (getEclipsePrefs().getBoolean(Constants.PREFERENCES_PRODUCT_USE_VAT)) {
                propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.VAT); 
            }
	        
	        if (getEclipsePrefs().getBoolean(Constants.PREFERENCES_CONTACT_USE_SALES_EQUALIZATION_TAX) && useSET) {
	        	propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.SALESEQUALIZATIONTAX);
	        }        
            
            // "$ItemGrossPrice" (if useGross = true) or "price" (if useGross = false)
            propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.UNITPRICE);
            
            if (containsDiscountedItems || getEclipsePrefs().getBoolean(Constants.PREFERENCES_DOCUMENT_USE_DISCOUNT_EACH_ITEM)) {
                propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.DISCOUNT);
            } 
            
            // useGross = true => "$ItemGrossTotal", useGross = false => "$ItemNetTotal"
            propertyNamesList.put(columnIndex++, DocumentItemListDescriptor.TOTALPRICE);
        }
		return propertyNamesList;
	}

	@Override
	protected void createDefaultContextMenu() {

		natTable.addConfiguration(new AbstractUiBindingConfiguration() {

			private final Menu bodyMenu = createContextMenu();

			@Override
			public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
				uiBindingRegistry.registerFirstMouseDownBinding(
						new MouseEventMatcher(SWT.NONE, GridRegion.BODY, MouseEventMatcher.RIGHT_BUTTON),
						new PopupMenuAction(this.bodyMenu) {
					@Override
					public void run(NatTable natTable, MouseEvent event) {
						int columnPosition = natTable.getColumnPositionByX(event.x);
						int rowPosition = natTable.getRowPositionByY(event.y);

						if (!getGridLayer().getSelectionLayer().isRowPositionFullySelected(rowPosition)) {
							natTable.doCommand(
									new SelectRowsCommand(natTable, columnPosition, rowPosition, false, false));
						}

						super.run(natTable, event);
					}
				});
			}

		});
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
        natTable.addConfiguration(new DefaultRowReorderLayerConfiguration());
        natTable.addConfiguration(new DocumentItemTableConfiguration());
        natTable.addConfiguration(new ListSelectionStyleConfiguration());
        natTable.setBackground(GUIHelper.COLOR_WHITE);
        // nur für das Headermenü, falls das mal irgendwann gebraucht werden sollte
        //      natTable.addConfiguration(new HeaderMenuConfiguration(n6));
        
        gridListLayer.getSelectionLayer().getSelectionModel().setMultipleSelectionAllowed(true);
        
        E4SelectionListener<DocumentItemDTO> esl = new E4SelectionListener<>(selectionService, gridListLayer.getSelectionLayer(), gridListLayer.getBodyDataProvider());
        gridListLayer.getSelectionLayer().addLayerListener(esl);

        // register right click as a selection event for the whole row
        natTable.getUiBindingRegistry().registerFirstMouseDownBinding(
                new MouseEventMatcher(SWT.NONE, GridRegion.BODY, MouseEventMatcher.RIGHT_BUTTON),
                new IMouseAction() {

                    ViewportSelectRowAction selectRowAction = new ViewportSelectRowAction(false, false);
                                
                    @Override
                    public void run(NatTable natTable, MouseEvent event) {
                        int rowPosition = natTable.getRowPositionByY(event.y);
                        if(!gridListLayer.getSelectionLayer().isRowPositionSelected(rowPosition)) {
                            selectRowAction.run(natTable, event);
                        }                   
                    }
                });

        natTable.configure();
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);        
    }
    
    private void initItemsList() {
        // Create a set of new temporary items.
        // These items exist only in the memory.
        // If the editor is opened, the items from the document are
        // copied to this item set. If the editor is closed or saved,
        // these items are copied back to the document and to the data base.
        List<DocumentItemDTO> wrappedItems = document.getItems().stream()
        		.sorted(Comparator.comparing((DocumentItem d) -> d.getPosNr()))
        		.map(DocumentItemDTO::new).collect(Collectors.toList());
        documentItemsListData = GlazedLists.eventList(wrappedItems);

//        // Set the sign
//        if (parentSign != documentType.sign())
//            newItem = new DataSetItem(item, -1);
//        else
//            newItem = new DataSetItem(item);

        // Reset the property "optional" from all items if the parent document was an offer
        // the parents document type
        DocumentType documentTypeParent = document.getSourceDocument() != null ? DocumentType.findByKey(document.getSourceDocument().getBillingType()
                .getValue()) : DocumentType.NONE;
        if (documentTypeParent == DocumentType.OFFER) {
            getDocumentItemsListData().forEach(item -> item.getDocumentItem().setOptional(Boolean.FALSE));
        }
        
        // set vesting period if this field is empty
        if(getEclipsePrefs().getInt(Constants.PREFERENCES_DOCUMENT_USE_VESTINGPERIOD) > 0) {
        	getDocumentItemsListData().stream()
        		.filter(item -> item.getDocumentItem().getVestingPeriodStart() == null || item.getDocumentItem().getVestingPeriodEnd() == null)
        		.forEach(item -> {
        			if(item.getDocumentItem().getVestingPeriodStart() == null) item.getDocumentItem().setVestingPeriodStart(new Date());
        			if(item.getDocumentItem().getVestingPeriodEnd() == null) item.getDocumentItem().setVestingPeriodEnd(new Date());
        		});
        }

        // Show the column "optional" if at least one item
        // with this property set was found
        Optional<DocumentItemDTO> optionalValue = getDocumentItemsListData().stream().filter(item -> Optional.ofNullable(item.getDocumentItem().getOptional()).orElse(Boolean.FALSE)).findFirst();
        containsOptionalItems = optionalValue.isPresent();

        // Show the columns discount if at least one item
        // with a discounted price was found
        Optional<DocumentItemDTO> discountedValue = getDocumentItemsListData().stream().filter(item -> item.getDocumentItem().getItemRebate() != null && item.getDocumentItem().getItemRebate().compareTo(Double.valueOf(0.0)) != 0).findFirst();
        containsDiscountedItems = discountedValue.isPresent();
    }
    
    @Override
    public String getTableId() {
    	/*
    	 * Since different document types have different value columns we have to 
    	 * distinguish between them.
    	 */
        return ID + documentType.getKey();
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
    	@SuppressWarnings("unchecked")
		Collection<DocumentItemDTO> selectedEntries = (Collection<DocumentItemDTO>)selectionService.getSelection();
        if(selectedEntries != null && selectedEntries.size() > 0) {
        	
        	// at first, close an open cell editor, if any
        	if(natTable.getActiveCellEditor() != null) {
        		natTable.getActiveCellEditor().close();
        	}
        	
        	boolean isRemoved = documentItemsListData.removeAll(selectedEntries);
            if(isRemoved) {
            	informEditor();
            }
        } else {
            log.debug("no rows selected!");
        }
    }

	private void informEditor() {
		renumberItems();
		// Recalculate the total sum of the document if necessary
		// do it via the messaging system and send a message to DocumentEditor
		Map<String, Object> event = new HashMap<>();
		event.put(DocumentEditor.DOCUMENT_ID, document.getName());
		event.put(DocumentEditor.DOCUMENT_RECALCULATE, true);
		evtBroker.post(DocumentEditor.EDITOR_ID + UIEvents.TOPIC_SEP + "itemChanged", event);
	}

	public void copySelectedEntry() {
    	@SuppressWarnings("unchecked")
        Collection<DocumentItemDTO> selectedEntries = (Collection<DocumentItemDTO>) selectionService.getSelection();
    	if(selectedEntries != null && selectedEntries.size() > 0) {
        	
        	// at first, close an open cell editor, if any
        	if(natTable.getActiveCellEditor() != null) {
        		natTable.getActiveCellEditor().close();
        	}
        	
        	boolean isAdded = false;
        	
        	for (DocumentItemDTO documentItemDTO : selectedEntries) {
        		DocumentItem newDocumentItem = documentItemDTO.getDocumentItem().clone();
        		
        		// some modifications...
        		newDocumentItem.setDateAdded(null);
        		newDocumentItem.setModified(null);
        		newDocumentItem.setModifiedBy(null);
        		
				DocumentItemDTO itemCopy = new DocumentItemDTO(newDocumentItem);
				isAdded = documentItemsListData.add(itemCopy);
			}
        	
            if(isAdded) {
            	informEditor();
            }
        	
        } else {
            log.debug("no rows selected!");
        }
	}

	/**
     * Set the "novat" in all items. If a document is marked as "novat", the {@link VAT}
     * of all items is displayed as "0.0%"
     * @param noVat <code>true</code> if no {@link VAT} should be used
     * @param dataSetVat in case of <em>noVat</em> is <code>true</code> the {@link VAT} entry for
     * the 0% {@link VAT} (i.e, "no VAT" - there could be more than one entry for 0% {@link VAT}); else this parameter is <code>null</code>
     */
    public void setItemsNoVat(Boolean noVat, VAT dataSetVat) {
    	documentItemsListData.forEach(item -> item.getDocumentItem().setNoVat(noVat));
        this.noVatReference = dataSetVat;
    }

    /**
     * Adds an empty item
     * 
     * @param newItem
     *            The new item
     */
    public void addNewItem(DocumentItemDTO newItem) {
    	newItem.getDocumentItem().setPosNr(documentItemsListData.size() + 1);
    	documentItemsListData.add(newItem);
    	natTable.doCommand(new SelectRowsCommand(getGridLayer().getSelectionLayer(), 0, documentItemsListData.size()-1, false, false));
        getContainer().setDirty(true);
    }

    /**
     * Renumber all items
     */
    public void renumberItems() {
        int no = 1;
        List<Integer> rowIndexOrder = getGridLayer().getBodyLayerStack().getRowReorderLayer().getRowIndexOrder();
        for (Integer i : rowIndexOrder) {
            DocumentItem documentItem = documentItemsListData.get(i).getDocumentItem();
            if (documentItem.getDeleted()) {
                continue;
            }
            documentItem.setPosNr(no++);
        }        
    }
    
    /**
     * If an external process wants to update the NatTable we use this method.
     */
    public void refresh() {
    	natTable.refresh();
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
            
            // configure to open the adjacent editor after commit
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.OPEN_ADJACENT_EDITOR,
                    Boolean.TRUE);
            
            // center position number
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    styleCentered,      
                    DisplayMode.NORMAL, POSITIONNUMBER_CELL_LABEL); 
            
            // for number values (e.g., quantity)
            TextCellEditor textCellEditor = new TextCellEditor(true, true);
            textCellEditor.setErrorDecorationEnabled(true);
            textCellEditor.setDecorationPositionOverride(SWT.LEFT | SWT.TOP);
			NumberFormat numberInstance = NumberFormat.getNumberInstance(localeUtil.getDefaultLocale().toLocale());
			numberInstance.setMaximumFractionDigits(10);
			DefaultDoubleDisplayConverter doubleDisplayConverter = new DefaultDoubleDisplayConverter(true);
			doubleDisplayConverter.setNumberFormat(numberInstance);

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
                    doubleDisplayConverter, 
                    DisplayMode.EDIT, DECIMAL_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL, DECIMAL_CELL_LABEL ); 
            
            registerDescriptionColumn(configRegistry, styleLeftAligned);
            registerVATColumn(configRegistry, styleRightAligned); 
            
            registerOptionalColumn(configRegistry);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    styleCentered,      
                    DisplayMode.NORMAL, OPTIONAL_CELL_LABEL); 
            
            // for date cells (e.g., vesting period)
            CDateTimeCellEditor dateCellEditor = new CDateTimeCellEditor(false, CDT.DROP_DOWN | CDT.DATE_SHORT);
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR, 
                    dateCellEditor, 
                    DisplayMode.NORMAL, DATE_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DateDisplayConverter(),
                    DisplayMode.NORMAL, DATE_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.ALWAYS_EDITABLE, 
                    DisplayMode.EDIT, DATE_CELL_LABEL);

            // for discount values
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL, PERCENT_CELL_LABEL ); 
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new DoublePercentageDisplayConverter(localeUtil),
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
                    new MoneyDisplayConverter(numberFormatterService),
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
                    new MoneyDisplayConverter(numberFormatterService),
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
            CellImagePainter cellImagePainter = new CellImagePainter(resourceManager);
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

		/**
		 * Registers the configuration for the description column.
		 * 
		 * @param configRegistry the config registry
		 * @param styleLeftAligned 
		 */
		private void registerDescriptionColumn(IConfigRegistry configRegistry, Style styleLeftAligned) {
			// description column
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR, 
                    new MultiLineTextCellEditor(true),
                    DisplayMode.EDIT, 
                    DESCRIPTION_CELL_LABEL);
            // configure the multi line text editor to always open in a
            // subdialog
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.OPEN_IN_DIALOG,
                    Boolean.TRUE,
                    DisplayMode.EDIT,
                    DESCRIPTION_CELL_LABEL);
            
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    styleLeftAligned,
                    DisplayMode.NORMAL,
                    DESCRIPTION_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    styleLeftAligned,
                    DisplayMode.EDIT,
                    DESCRIPTION_CELL_LABEL);
            
            // configure custom dialog settings
            Display display = Display.getCurrent();
            Map<String, Object> editDialogSettings = new HashMap<>();
            editDialogSettings.put(ICellEditDialog.DIALOG_SHELL_TITLE, msg.dialogItemdescriptionHeader);
            editDialogSettings.put(ICellEditDialog.DIALOG_SHELL_ICON, display.getSystemImage(SWT.ICON_INFORMATION));
            editDialogSettings.put(ICellEditDialog.DIALOG_SHELL_RESIZABLE, Boolean.TRUE);
            
            // calculate the dialog position in relation to main window (doesn't work if main window is moved)
            Point size = new Point(400, 300);
            editDialogSettings.put(ICellEditDialog.DIALOG_SHELL_SIZE, size);
            Rectangle bounds = natTable.getShell().getBounds();
            Point location = new Point(
            		bounds.x + (bounds.width / 2  - size.x / 2),
            		bounds.y + (bounds.height / 2 - size.y / 2));
            editDialogSettings.put(ICellEditDialog.DIALOG_SHELL_LOCATION, location);
            
            // add custom message
            editDialogSettings.put(ICellEditDialog.DIALOG_MESSAGE, msg.dialogItemdescriptionHint);
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.EDIT_DIALOG_SETTINGS,
                    editDialogSettings,
                    DisplayMode.EDIT,
                    DESCRIPTION_CELL_LABEL);		
        }

		/**
		 * Registers the configuration for the optional value column (if an item is optional).
		 * 
		 * @param configRegistry the config registry
		 */
		private void registerOptionalColumn(IConfigRegistry configRegistry) {
			// for optional values
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.ALWAYS_EDITABLE, 
                    DisplayMode.EDIT, OPTIONAL_CELL_LABEL);			
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITOR, 
                    new CheckBoxCellEditor(), 
                    DisplayMode.EDIT, OPTIONAL_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER, 
                    new CheckBoxPainter(Icon.COMMAND_CHECKED.getImage(IconSize.DefaultIconSize), Icon.COMMAND_UNCHECKED.getImage(IconSize.DefaultIconSize)), 
                    DisplayMode.NORMAL, OPTIONAL_CELL_LABEL);  
            //using a CheckBoxCellEditor also needs a Boolean conversion to work correctly
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER, 
                    new DefaultBooleanDisplayConverter(), 
                    DisplayMode.NORMAL, 
                    OPTIONAL_CELL_LABEL);
		}

		/**
		 * Registers the configuration for the {@link VAT} column.
		 * 
		 * @param configRegistry the config registry
		 * @param styleRightAligned a style attribute
		 */
		private void registerVATColumn(IConfigRegistry configRegistry, Style styleRightAligned) {
			//register a combobox editor for VAT values 
			    configRegistry.registerConfigAttribute(
			            EditConfigAttributes.CELL_EDITABLE_RULE, 
			            IEditableRule.ALWAYS_EDITABLE, 
			            DisplayMode.EDIT, VAT_CELL_LABEL);
			    configRegistry.registerConfigAttribute(
			            CellConfigAttributes.CELL_STYLE,
			            styleRightAligned,      
			            DisplayMode.NORMAL, VAT_CELL_LABEL); 
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
        return gridListLayer;
    }

    @Override
    protected AbstractDAO<DocumentItemDTO> getEntityDAO() {
        throw new UnsupportedOperationException("Inside a list table there's no extra DAO.");
    }

	/**
	 * @return the container
	 */
	public final DocumentEditor getContainer() {
		return container;
	}

	/**
	 * @param container the container to set
	 */
	public final void setContainer(DocumentEditor container) {
		this.container = container;
	}
	
	@Override
	protected Class<DocumentItemDTO> getEntityClass() {
		return DocumentItemDTO.class;
	}
}
