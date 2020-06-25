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
 
package com.sebulli.fakturama.parts.voucheritems;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.money.MonetaryAmount;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
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
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDisplayConverter;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDoubleDisplayConverter;
import org.eclipse.nebula.widgets.nattable.edit.EditConfigAttributes;
import org.eclipse.nebula.widgets.nattable.edit.editor.ComboBoxCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.IComboBoxDataProvider;
import org.eclipse.nebula.widgets.nattable.edit.editor.MultiLineTextCellEditor;
import org.eclipse.nebula.widgets.nattable.edit.editor.TextCellEditor;
import org.eclipse.nebula.widgets.nattable.extension.e4.selection.E4SelectionListener;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.ComboBoxPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.reorder.config.DefaultRowReorderLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.reorder.event.RowReorderEvent;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionProvider;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.javamoney.moneta.Money;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.ItemAccountTypeDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.dto.Price;
import com.sebulli.fakturama.dto.VoucherItemDTO;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.INumberFormatterService;
import com.sebulli.fakturama.model.IEntity;
import com.sebulli.fakturama.model.ItemAccountType;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.Voucher;
import com.sebulli.fakturama.model.VoucherCategory;
import com.sebulli.fakturama.model.VoucherItem;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.parts.ExpenditureVoucherEditor;
import com.sebulli.fakturama.parts.VoucherEditor;
import com.sebulli.fakturama.parts.converter.ItemAccountTypeDisplayConverter;
import com.sebulli.fakturama.parts.converter.VatDisplayConverter;
import com.sebulli.fakturama.parts.itemlist.ItemAccountTypeValueComboProvider;
import com.sebulli.fakturama.parts.itemlist.MoveEntryDownMenuItem;
import com.sebulli.fakturama.parts.itemlist.MoveEntryUpMenuItem;
import com.sebulli.fakturama.parts.itemlist.VatValueComboProvider;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.EntityGridListLayer;
import com.sebulli.fakturama.views.datatable.MoneyDisplayConverter;
import com.sebulli.fakturama.views.datatable.tree.model.TreeObject;
import com.sebulli.fakturama.views.datatable.tree.ui.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

/**
 *
 */
public class VoucherItemListTable extends AbstractViewDataTable<VoucherItemDTO, VoucherCategory> {
    
    @Inject
    private VatsDAO vatsDAO;
    
    @Inject
    private ItemAccountTypeDAO itemAccountTypeDAO;
    
	@Inject
	private INumberFormatterService numberFormatterService;
    
    @Inject
    private IEclipseContext context;

    // ID of this view
    public static final String ID = "fakturama.document.voucherItemTable";
    
    private Voucher voucher;
    private EventList<VoucherItemDTO> voucherItemsListData;
    private Set<IEntity> markedForDeletion = new HashSet<>();
    private VoucherEditor container;

    // Flag if there are items with an discount set
//    private boolean containsDiscountedItems = false;
    private boolean useGross;
//    private int netgross = DocumentSummary.NOTSPECIFIED;

    private Control top;
    
    private static final String PERCENT_CELL_LABEL = "Percent_Cell_LABEL";
    private static final String TOTAL_MONEYVALUE_CELL_LABEL = "TotalMoneyValue_Cell_LABEL";
    private static final String TEXT_CELL_LABEL = "Text_Cell_LABEL";
    private static final String DECIMAL_CELL_LABEL = "Decimal_Cell_LABEL";
    private static final String ACCOUNTTYPE_CELL_LABEL = "Accounttype_Cell_LABEL";
    private static final String DESCRIPTION_CELL_LABEL = "Description_Cell_LABEL";
//    private static final String POSITIONNUMBER_CELL_LABEL = "Positionnumber_Cell_LABEL";

    protected static final String POPUP_ID = "com.sebulli.fakturama.voucheritemlist.popup";

    private EntityGridListLayer<VoucherItemDTO> gridListLayer;

    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    private SelectionLayer selectionLayer;

	private VoucherItemTableConfiguration voucherItemsTableConfig;
    
    /**
     * Entry point for this class. Here the whole Composite is built.
     * 
     * @param parent
     * @param document
     * @param documentSummary 
     * @param useGross 
     * @return
     */
    public Control createPartControl(Composite parent, Voucher voucher, boolean useGross,
            int netgross, VoucherEditor container) {
        log.info("create VoucherItem list part");
        this.voucher = voucher;
        this.useGross = useGross;
        this.container = container;

        this.top = super.createPartControl(parent, VoucherItemDTO.class, false, ID);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);        
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
			    				return natEventData.getRowPosition() < gridListLayer.getBodyDataProvider().getRowCount();
			    			}
			    		})
				.build();

		return retval;
	}

    protected NatTable createListTable(Composite tableComposite) {  
        Integer columnIndex = Integer.valueOf(0);
        // fill the underlying data source (GlazedLists)
        initItemsList();

//        // Get the column width from the preferences
//        int cw_text = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_VOUCHERITEMS_TEXT");
//        int cw_accounttype = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_VOUCHERITEMS_ACCOUNTTYPE");
//        int cw_vat = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_VOUCHERITEMS_VAT");
//        int cw_net = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_VOUCHERITEMS_NET");
//        int cw_gross = Activator.getDefault().getPreferenceStore().getInt("COLUMNWIDTH_VOUCHERITEMS_GROSS");

        // Create the table columns
        final BidiMap<Integer, VoucherItemListDescriptor> propertyNamesList = new DualHashBidiMap<>();
//        propertyNamesList.put(columnIndex++, VoucherItemListDescriptor.POSITION);
        propertyNamesList.put(columnIndex++, VoucherItemListDescriptor.TEXT);
        propertyNamesList.put(columnIndex++, VoucherItemListDescriptor.ACCOUNTTYPE);
        propertyNamesList.put(columnIndex++, VoucherItemListDescriptor.VAT);
        propertyNamesList.put(columnIndex++, VoucherItemListDescriptor.PRICE);
        propertyNamesList.put(columnIndex++, VoucherItemListDescriptor.TOTAL);
        List<VoucherItemListDescriptor> tmpList2 = new ArrayList<>(propertyNamesList.values());
        String[] propertyNames = tmpList2.stream().map(VoucherItemListDescriptor::getPropertyName).collect(Collectors.toList()).toArray(new String[]{});
        final IColumnPropertyAccessor<VoucherItem> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<>(propertyNames);
        // Add derived column
        final IColumnPropertyAccessor<VoucherItemDTO> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<VoucherItemDTO>() {

            /**
             * Get the value to set to the editor
             */
            public Object getDataValue(VoucherItemDTO rowObject, int columnIndex) {
                Object retval = "???";
                VoucherItemListDescriptor descriptor = propertyNamesList.get(columnIndex);
                switch (descriptor) {
//                case POSITION:
                case ACCOUNTTYPE:
                    retval = (ItemAccountType) columnPropertyAccessor.getDataValue(rowObject.getVoucherItem(), columnIndex);
                    break;
                case DISCOUNT:
                case TEXT:
                    retval = columnPropertyAccessor.getDataValue(rowObject.getVoucherItem(), columnIndex);
                    break;
                case VAT:
                    retval = (VAT) columnPropertyAccessor.getDataValue(rowObject.getVoucherItem(), columnIndex);
                    break;
                case TOTAL:
                    retval = DataUtils.getInstance().CalculateGrossFromNet(
                    		Money.of(rowObject.getVoucherItem().getPrice(), DataUtils.getInstance().getDefaultCurrencyUnit()), 
                    		rowObject.getVoucherItem().getVat().getTaxValue());
                    break;
                case PRICE:
                    if (useGross) { // "$VoucherItemGrossPrice"
                        // Fill the cell with the total gross value of the item
                        retval = rowObject.getVoucherItem().getPrice()/*.getTotalGrossRounded()*/;
                    } else { 
                        // Fill the cell with the total net value of the item
                        retval = rowObject.getVoucherItem().getPrice()/*.getTotalNetRounded()*/;
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
            public void setDataValue(VoucherItemDTO rowObject, int columnIndex, Object newValue) {
                VoucherItemListDescriptor descriptor = propertyNamesList.get(columnIndex);
                boolean calculate = true;
                switch (descriptor) {
                case TEXT:
                    rowObject.getVoucherItem().setName((String) newValue);
                    calculate = false; // no recalculation needed
                    break;
                case VAT:
                    // Set the VAT
                    VAT vat = (VAT) newValue; //columnPropertyAccessor.getDataValue(rowObject.getDocumentItem(), columnIndex);
                    if (vat != null) {
                        rowObject.getVoucherItem().setVat(vat);
                        // if this item has an accountType change its value
                        if(rowObject.getVoucherItem().getAccountType() != null) {
                        	rowObject.getVoucherItem().getAccountType().setValue(vat.getName());
                        }
                    }
                    break;
                case ACCOUNTTYPE:
                    ItemAccountType accountType = (ItemAccountType) newValue;
                    if(accountType != null) {
                    	// enrich a new accountType with VAT name
                    	if(accountType.getId() == 0 && rowObject.getVoucherItem().getVat() != null) {
                    		accountType.setValue(rowObject.getVoucherItem().getVat().getName());
                    	}
                        rowObject.getVoucherItem().setAccountType(accountType);
                    }
                    break;
                case PRICE:
                	String priceString = ((String) newValue).toLowerCase();
                	rowObject.getVoucherItem().setPrice(DataUtils.getInstance().StringToDouble(priceString));
                	break;
                case TOTAL:
                    // only net values are stored.
                	priceString = ((String) newValue).toLowerCase();
                    MonetaryAmount amount = Money.of(DataUtils.getInstance().StringToDouble(priceString), DataUtils.getInstance().getDefaultCurrencyUnit());
                    Price newPrice = new Price(amount, rowObject.getVoucherItem().getVat().getTaxValue(), false, true);
                    rowObject.getVoucherItem().setPrice(newPrice.getUnitNet().getNumber().doubleValue());
                    break;
                default:
                    break;
                }
                notifyChangeListener(calculate);
            }

            public int getColumnCount() {
                return propertyNames.length;
            }

            public String getColumnProperty(int columnIndex) {
                VoucherItemListDescriptor descriptor = (VoucherItemListDescriptor) propertyNamesList.get(columnIndex);
                return msg.getMessageFromKey(descriptor.getMessageKey());
            }

            public int getColumnIndex(String propertyName) {
                    return columnPropertyAccessor.getColumnIndex(propertyName);
            }
        };
        

        IRowIdAccessor<VoucherItemDTO> rowIdAccessor = new IRowIdAccessor<VoucherItemDTO>() {
            @Override
            public Serializable getRowId(VoucherItemDTO rowObject) {
                return rowObject.getVoucherItem().getPosNr();
            }
        };

        //build the grid layer
        // as long as there's no row header we leave the last param at "false"
        gridListLayer = new EntityGridListLayer<>(getVoucherItemsListData(), propertyNames, derivedColumnPropertyAccessor, rowIdAccessor, configRegistry, false);
        DataLayer tableDataLayer = gridListLayer.getBodyDataLayer();
//        FilterRowDataLayer<ExpenditureItem> filterDataLayer = new FilterRowDataLayer<>(filterStrategy, columnHeaderLayer, columnHeaderDataProvider, configRegistry);
        
        // set default percentage width 
        tableDataLayer.setColumnPercentageSizing(true);
//        tableDataLayer.setColumnPercentageSizing(position, percentageSizing);
        
        // Custom selection configuration
        selectionLayer = gridListLayer.getSelectionLayer();
        
        //set ISelectionProvider
        final RowSelectionProvider<VoucherItemDTO> selectionProvider = 
                new RowSelectionProvider<VoucherItemDTO>(selectionLayer, gridListLayer.getBodyDataProvider());
        
        //add a listener to the selection provider, in an Eclipse application you would do this
        //e.g. getSite().getPage().addSelectionListener()
        selectionProvider.addSelectionChangedListener(new ISelectionChangedListener() {
            
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
//                log.debug("Selection changed:");
                
        		IStructuredSelection structuredSelection = event.getStructuredSelection();
                @SuppressWarnings("unchecked")
                List<VoucherItemDTO> selectedElements = structuredSelection.toList();
                selectionService.setSelection(selectedElements);
            }
            
        });
        
        // for further use, if we need it...
        //      ILayer columnHeaderLayer = gridLayer.getColumnHeaderLayer();
        //      ILayer rowHeaderLayer = gridLayer.getRowHeaderLayer();
        
        // TODO geht nich!
//        ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);
//        viewportLayer.registerCommandHandler(
//                new MoveCellSelectionCommandHandler(selectionLayer, ITraversalStrategy.TABLE_TRAVERSAL_STRATEGY));
        
        // Create a label accumulator - adds custom labels to all cells which we
        // wish to render differently.
        BidiMap<VoucherItemListDescriptor, Integer> reverseMap = propertyNamesList.inverseBidiMap();
        ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(gridListLayer.getBodyLayerStack());
//        registerColumnOverrides(reverseMap, columnLabelAccumulator, VoucherItemListDescriptor.POSITION, POSITIONNUMBER_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, VoucherItemListDescriptor.VAT, VAT_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, VoucherItemListDescriptor.ACCOUNTTYPE, ACCOUNTTYPE_CELL_LABEL);
//        registerColumnOverrides(reverseMap, columnLabelAccumulator, VoucherItemListDescriptor.DISCOUNT, PERCENT_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, VoucherItemListDescriptor.PRICE, MONEYVALUE_CELL_LABEL);
        registerColumnOverrides(reverseMap, columnLabelAccumulator, VoucherItemListDescriptor.TOTAL, MONEYVALUE_CELL_LABEL);
        
        // "normal" columns are always editable
        registerColumnOverrides(reverseMap, columnLabelAccumulator, VoucherItemListDescriptor.TEXT, DESCRIPTION_CELL_LABEL);
//        registerColumnOverrides(reverseMap, columnLabelAccumulator, VoucherItemListDescriptor.ITEMNUMBER, TEXT_CELL_LABEL);
//        registerColumnOverrides(reverseMap, columnLabelAccumulator, VoucherItemListDescriptor.NAME, TEXT_CELL_LABEL);
//        registerColumnOverrides(reverseMap, columnLabelAccumulator, VoucherItemListDescriptor.QUANTITY, DECIMAL_CELL_LABEL);
//        registerColumnOverrides(reverseMap, columnLabelAccumulator, VoucherItemListDescriptor.QUNIT, TEXT_CELL_LABEL);

        final NatTable natTable = new NatTable(tableComposite /*, 
                SWT.NO_REDRAW_RESIZE| SWT.DOUBLE_BUFFERED | SWT.BORDER*/, gridListLayer.getGridLayer(), false);
        // Register label accumulator
        gridListLayer.getBodyLayerStack().setConfigLabelAccumulator(columnLabelAccumulator);
        
        // if a re-ordering of rows occurs we have to renumber the items
        gridListLayer.getBodyLayerStack().getRowReorderLayer().addLayerListener((ILayerEvent event) -> {
                if (event instanceof RowReorderEvent) {
                    RowReorderEvent evt = (RowReorderEvent) event;
                    evt.convertToLocal(gridListLayer.getBodyLayerStack().getRowReorderLayer());
                    int newIdx = 0;
                    for (Integer rowIndex : gridListLayer.getBodyLayerStack().getRowReorderLayer().getRowIndexOrder()) {
                        VoucherItemDTO objToRenumber = gridListLayer.getBodyDataProvider().getRowObject(rowIndex);
                        objToRenumber.getVoucherItem().setPosNr(++newIdx);
                    }
                    getContainer().setDirty(true);
              }
            }
        );
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));
        
        return natTable;
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

						if (!selectionLayer.isRowPositionFullySelected(rowPosition)) {
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
    private void registerColumnOverrides(BidiMap<VoucherItemListDescriptor, Integer> reverseMap, ColumnOverrideLabelAccumulator columnLabelAccumulator, 
            VoucherItemListDescriptor descriptor, String... cellLabel) {
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
        voucherItemsTableConfig = new VoucherItemTableConfiguration();
		natTable.addConfiguration(voucherItemsTableConfig);
        natTable.addConfiguration(new DefaultRowReorderLayerConfiguration());
//        natTable.addConfiguration(new DebugMenuConfiguration(natTable));
        natTable.setBackground(GUIHelper.COLOR_WHITE);
        // nur für das Headermenü, falls das mal irgendwann gebraucht werden sollte
        //      natTable.addConfiguration(new HeaderMenuConfiguration(n6));
        selectionLayer.getSelectionModel().setMultipleSelectionAllowed(true);
        
        E4SelectionListener<VoucherItemDTO> esl = new E4SelectionListener<>(selectionService, selectionLayer, gridListLayer.getBodyDataProvider());
        selectionLayer.addLayerListener(esl);

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
        List<VoucherItemDTO> wrappedItems = voucher.getItems().stream()
        		.sorted(Comparator.comparing((VoucherItem v) -> v.getPosNr()))
        		.map(VoucherItemDTO::new).collect(Collectors.toList());
        voucherItemsListData = GlazedLists.eventList(wrappedItems);
        markedForDeletion.clear();
        
//        renumberItems();
    }
    
    @Override
    public String getTableId() {
        return ID;
    }

    @Override
    protected String getEditorId() {
        return ExpenditureVoucherEditor.ID;
    }
    
    @Override
    protected String getEditorTypeId() {
        return ExpenditureVoucherEditor.class.getSimpleName();
    }

    @Override
    protected TopicTreeViewer<VoucherCategory> createCategoryTreeViewer(Composite top) {
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

    /**
     * @return the documentItemsListData
     */
    public EventList<VoucherItemDTO> getVoucherItemsListData() {
        return voucherItemsListData;
    }

    /**
     * @return the markedForDeletion
     */
    public final Set<IEntity> getMarkedForDeletion() {
        return markedForDeletion;
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
    protected EntityGridListLayer<VoucherItemDTO> getGridLayer() {
        throw new UnsupportedOperationException("Wrong call for a GridLayer.");
    }

    @Override
    protected AbstractDAO<VoucherItemDTO> getEntityDAO() {
        throw new UnsupportedOperationException("Inside a list table there's no extra DAO.");
    }

    /**
     * Renumber all items
     */
    protected void renumberItems() {
        int no = 1;
        for (VoucherItemDTO voucherItemDTO : voucherItemsListData) {
            if(voucherItemDTO.getVoucherItem().getDeleted()) {
                continue;
            }
            voucherItemDTO.getVoucherItem().setPosNr(no++);
        }
    }
    
    public void updateVoucherItemsAndAccountTypes(Voucher voucher) {
    	this.voucher = voucher;
//    	initItemsList();
    	voucherItemsTableConfig.registerAndFillAccountTypeComboBox();
    }

    class VoucherItemTableConfiguration extends AbstractRegistryConfiguration {

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
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.ALWAYS_EDITABLE, 
                    DisplayMode.EDIT, DESCRIPTION_CELL_LABEL);
            
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
            IComboBoxDataProvider dataProvider = new VatValueComboProvider(vatsDAO.findAll());
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
            
            //register a combobox editor for Accounttype values 
            configRegistry.registerConfigAttribute(
                    EditConfigAttributes.CELL_EDITABLE_RULE, 
                    IEditableRule.ALWAYS_EDITABLE, 
                    DisplayMode.EDIT, ACCOUNTTYPE_CELL_LABEL);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_STYLE,
                    styleLeftAligned,      
                    DisplayMode.NORMAL, ACCOUNTTYPE_CELL_LABEL); 
            configRegistry.registerConfigAttribute( 
                    CellConfigAttributes.CELL_PAINTER, 
                    new ComboBoxPainter(), 
                    DisplayMode.NORMAL, ACCOUNTTYPE_CELL_LABEL);
            
            registerAndFillAccountTypeComboBox();
            
            configRegistry.registerConfigAttribute( 
                    CellConfigAttributes.DISPLAY_CONVERTER, 
                    ContextInjectionFactory.make(ItemAccountTypeDisplayConverter.class, context), 
                    DisplayMode.NORMAL, ACCOUNTTYPE_CELL_LABEL); 
            
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
         }
		
		void registerAndFillAccountTypeComboBox() {
			IComboBoxDataProvider dataProviderItemAccountTypes = new ItemAccountTypeValueComboProvider(itemAccountTypeDAO.findAll());
			ComboBoxCellEditor itemAccountTypeValueCombobox = new ComboBoxCellEditor(dataProviderItemAccountTypes);
			itemAccountTypeValueCombobox.setFreeEdit(true);
            configRegistry.registerConfigAttribute( 
                    EditConfigAttributes.CELL_EDITOR, 
                    itemAccountTypeValueCombobox, 
                    DisplayMode.NORMAL, ACCOUNTTYPE_CELL_LABEL); 
		}
    }


    @Override
	public void removeSelectedEntry() {
    	@SuppressWarnings("unchecked")
		List<VoucherItem> selectedEntries = (List<VoucherItem>) selectionService.getSelection();
		if (selectedEntries != null) {
        	// at first, close an open cell editor, if any
        	if(natTable.getActiveCellEditor() != null) {
        		natTable.getActiveCellEditor().close();
        	}
			
        	boolean isRemoved = getVoucherItemsListData().removeAll(selectedEntries);
            if(isRemoved) {
            	markedForDeletion.addAll(selectedEntries);
            	renumberItems();
            	notifyChangeListener(true);
			}
		} else {
			log.debug("no rows selected!");
		}
	}

    /**
     * @param calculate
     */
    private void notifyChangeListener(boolean calculate) {
        // Recalculate the total sum of the document if necessary
        // do it via the messaging system and send a message to VoucherEditor
        Map<String, Object> event = new HashMap<>();
        event.put(DocumentEditor.DOCUMENT_ID, ((MPart)top.getParent().getParent().getData("modelElement")).getTransientData().get(VoucherEditor.PART_ID));
        event.put(DocumentEditor.DOCUMENT_RECALCULATE, calculate);
        evtBroker.post(VoucherEditor.EDITOR_ID + "/itemChanged", event);
    }

    /**
     * Adds an empty item
     * 
     * @param newItem
     *            The new item
     */
    public void addNewItem(VoucherItemDTO newItem) {
        getVoucherItemsListData().add(newItem);
        notifyChangeListener(false);
        getContainer().setDirty(true);
        ILayerCommand scrollToLastPositionCommand = new SelectRowsCommand(gridListLayer.getGridLayer(), 1, newItem.getVoucherItem().getPosNr(), false, false);
		natTable.doCommand(scrollToLastPositionCommand);
    }
    
    @Override
    protected Class<VoucherItemDTO> getEntityClass() {
    	return VoucherItemDTO.class;
    }

	private VoucherEditor getContainer() {
		return container;
	}

}
