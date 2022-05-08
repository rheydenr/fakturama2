/*******************************************************************************
 * Copyright (c) 2014 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package com.sebulli.fakturama.views.datatable.products;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.DefaultDoubleDisplayConverter;
import org.eclipse.nebula.widgets.nattable.extension.e4.selection.E4SelectionListener;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.action.ViewportSelectRowAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.ProductCategoriesDAO;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.model.ProductCategory;
import com.sebulli.fakturama.model.Product_;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.parts.Editor;
import com.sebulli.fakturama.parts.ProductEditor;
import com.sebulli.fakturama.parts.converter.VatDisplayConverter;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.common.CommonListItemMatcher;
import com.sebulli.fakturama.views.datatable.common.ListSelectionStyleConfiguration;
import com.sebulli.fakturama.views.datatable.common.MoneyDisplayConverter;
import com.sebulli.fakturama.views.datatable.common.NoHeaderRowOnlySelectionBindings;
import com.sebulli.fakturama.views.datatable.layer.EntityGridListLayer;
import com.sebulli.fakturama.views.datatable.tree.ui.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeCategoryLabelProvider;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

/**
 * Builds the Product list table.
 *
 */
public class ProductListTable extends AbstractViewDataTable<Product, ProductCategory> {

    // ID of this view
    public static final String ID = "fakturama.views.productTable";
    
    protected static final String POPUP_ID = "com.sebulli.fakturama.productlist.popup";
    public static final String SELECTED_PRODUCT_ID = "fakturama.productlist.selectedproductid";

/**    this is for synchronizing the UI thread */
    @Inject    
    private UISynchronize sync;
    
    @Inject
    protected IEclipseContext context;
    
    @Inject
    private ProductsDAO productsDAO;

    @Inject
    private ProductCategoriesDAO productCategoriesDAO;
	
    private EventList<Product> productListData;
    private EventList<ProductCategory> categories;

    private EntityGridListLayer<Product> gridListLayer;

    private MPart listTablePart;
    private Product selectedObject;

    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    protected FilterList<Product> treeFilteredIssues;

	private CommonListItemMatcher<Product> currentFilter;
	private BidiMap<Integer, ProductListDescriptor> prodListDescriptors;
	private ViewDataTableMode viewDataTableMode;

    @PostConstruct
    public Control createPartControl(Composite parent, MPart listTablePart) {
    	log.debug("create Product list part");
        super.createPartControl(parent, Product.class, true, ID);
        this.listTablePart = listTablePart;
//        this.application = application;
        // Listen to double clicks
        Object commandId = this.listTablePart.getTransientData().get(Constants.PROPERTY_PRODUCTS_CLICKHANDLER);
        if(commandId != null) { // exactly would it be Constants.COMMAND_SELECTITEM
            hookDoubleClickCommand(natTable, getGridLayer(), (String) commandId);
            viewDataTableMode = ViewDataTableMode.DIALOG;
        } else {
            hookDoubleClickCommand2(natTable, getGridLayer());
            viewDataTableMode = ViewDataTableMode.LIST;
        }
        topicTreeViewer.setTable(this);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(top);
        return top;
    }
    
    @Override
    public Product getSelectedObject() {
        return selectedObject;
    }
    
    @Override
    public Product[] getSelectedObjects() {
        return getSelectedObjects(false);
    }
    
    @Override
    public Product[] getSelectedObjects(boolean selectIfSingleRow) {
        List<Product> selectedObjects = new ArrayList<>();
        int[] fullySelectedRowPositions = getGridLayer().getSelectionLayer().getFullySelectedRowPositions();
        if(fullySelectedRowPositions.length > 0 && fullySelectedRowPositions[0] > -1) {
            for (int i = 0; i < fullySelectedRowPositions.length; i++) {
                selectedObjects.add(getGridLayer().getBodyDataProvider().getRowObject(fullySelectedRowPositions[i]));
            }
        } 
        
        if(gridListLayer.getGridLayer().getBodyLayer().getRowCount() == 1) {
            int rowPos = natTable.getRowPositionByY(1);
            int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPos, getGridLayer().getBodyDataLayer());
            // TODO Why is selectedObject set? 
            selectedObject = getGridLayer().getBodyDataProvider().getRowObject(bodyRowPos);
            selectedObjects = Arrays.asList(selectedObject);
        }
        Product[] retArr = selectedObjects.toArray(new Product[selectedObjects.size()]);
        selectionService.setSelection(selectedObjects);
        return retArr;
    }
    

    @Override
    protected void hookDoubleClickCommand2(final NatTable nattable, final EntityGridListLayer<Product> gridLayer) {
        hookDoubleClickCommand(nattable, gridLayer, null);
    }
    
    private void hookDoubleClickCommand(final NatTable nattable, final EntityGridListLayer<Product> gridLayer, String commandId) {
        
        if (commandId != null) {
            // if we are in "selectproduct" mode we have to register a single click mouse event
            nattable.getUiBindingRegistry().registerFirstSingleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), new IMouseAction() {

				public void run(NatTable natTable, MouseEvent event) {
                    int rowPos = natTable.getRowPositionByY(event.y);
                    int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPos, gridLayer.getBodyDataLayer());
                    selectedObject = gridLayer.getBodyDataProvider().getRowObject(bodyRowPos);
                }
            });
        }
        // Add a double click listener
        nattable.getUiBindingRegistry().registerDoubleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), new IMouseAction() {

            @Override
            public void run(NatTable natTable, MouseEvent event) {
                //get the row position for the click in the NatTable
                int rowPos = natTable.getRowPositionByY(event.y);
                //transform the NatTable row position to the row position of the body layer stack
                int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPos, gridLayer.getBodyDataLayer());
                selectedObject = gridLayer.getBodyDataProvider().getRowObject(bodyRowPos);
                // Call the corresponding editor. The editor is set
                // in the variable "editor", which is used as a parameter
                // when calling the editor command.
                // in E4 we create a new Part (or use an existing one with the same ID)
                // from PartDescriptor
                
                if(commandId != null) {
                    fireClosingEvent();
                } else {
                    Map<String, Object> params = new HashMap<>();
                    // if we come from the list view then we should open a new editor 
                    params.put(CallEditor.PARAM_OBJ_ID, Long.toString(selectedObject.getId()));
                    params.put(CallEditor.PARAM_EDITOR_TYPE, getEditorId());
                    
                    context.getParent().get(ESelectionService.class).setSelection(null);
                    params.put(CallEditor.PARAM_FOLLOW_UP, null);  // could be set from a previous call
                    ParameterizedCommand parameterizedCommand = commandService.createCommand(CommandIds.CMD_CALL_EDITOR, params);
                    handlerService.executeHandler(parameterizedCommand);
                }
            }
        });
    }

    private void fireClosingEvent() {
        // If we don't give a target document number the event will  be caught by *all*
        // open editors which listens to this event. This is (obviously :-) ) not
        // the intended behavior...
        Map<String, Object> eventParams = new HashMap<>();
        // the transientData HashMap contains the target document number
        // (was set in MouseEvent handler)
        eventParams.put(DocumentEditor.DOCUMENT_ID, context.get(DocumentEditor.DOCUMENT_ID));
        eventParams.put(SELECTED_PRODUCT_ID, Arrays.asList(Long.valueOf(selectedObject.getId())));
//            // alternatively use the Selection Service
        // ==> no! Because this SelectionService has another context than 
        // the receiver of this topic. Therefore the receiver's SelectionService
        // is empty :-(
//            selectionService.setSelection(selectedObject);
        
        // selecting an entry and closing the dialog are two different actions.
        // the "CloseProduct" event is caught by SelectProductDialog#handleDialogDoubleClickClose. 
        evtBroker.post("DialogSelection/Product", eventParams);
        evtBroker.post("DialogAction/CloseProduct", eventParams);
    }

    @Override
    protected void postConfigureNatTable(NatTable natTable) {
        //as the autoconfiguration of the NatTable is turned off, we have to add the 
        //DefaultNatTableStyleConfiguration and the ConfigRegistry manually	
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new NoHeaderRowOnlySelectionBindings());
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new ProductTableConfiguration());
        natTable.addConfiguration(new ListSelectionStyleConfiguration());
        natTable.setBackground(GUIHelper.COLOR_WHITE);
        // nur für das Headermenü, falls das mal irgendwann gebraucht werden sollte
        //		natTable.addConfiguration(new HeaderMenuConfiguration(n6));
        gridListLayer.getSelectionLayer().getSelectionModel().setMultipleSelectionAllowed(true);

        E4SelectionListener<Product> esl = new E4SelectionListener<>(selectionService, gridListLayer.getSelectionLayer(), gridListLayer.getBodyDataProvider());
        gridListLayer.getSelectionLayer().addLayerListener(esl);
        
        // Change the default sort key bindings. Note that 'auto configure' was turned off
        // for the SortHeaderLayer (setup in the GlazedListsGridLayer)
        natTable.addConfiguration(new SingleClickSortConfiguration());

        // register right click as a selection event for the whole row
        natTable.getUiBindingRegistry().registerMouseDownBinding(
                new MouseEventMatcher(SWT.NONE, GridRegion.BODY, MouseEventMatcher.RIGHT_BUTTON),

                new IMouseAction() {

                    ViewportSelectRowAction selectRowAction = new ViewportSelectRowAction(false, false);
                                
                    @Override
                    public void run(NatTable natTable, MouseEvent event) {
                        int rowPosition = natTable.getRowPositionByY(event.y);
                        if(!gridListLayer.getSelectionLayer().isRowPositionSelected(rowPosition)) {
                            selectRowAction.run(natTable, event);

                            int rowPos = natTable.getRowPositionByY(event.y);
                            int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPos, getGridLayer().getBodyDataLayer());
                            selectedObject = getGridLayer().getBodyDataProvider().getRowObject(bodyRowPos);
                        }                   
                    }
                });
        natTable.configure();
    }
    
    /**
     * @param propertyNames
     * @return
     */
	private IColumnPropertyAccessor<Product> createColumnPropertyAccessor(String[] propertyNames) {
		Map<Integer, ProductListDescriptor> tmpMap = new HashMap<>();
		
		// columns are dynamic because of some properties settings (use quantity, use vat etc)
		for (int i = 0; i < propertyNames.length; i++) {
			String string = propertyNames[i];
			java.util.Optional<ProductListDescriptor> descriptorForProperty = ProductListDescriptor.getDescriptorForProperty(string);
			if(descriptorForProperty.isPresent()) {
				tmpMap.put(i, descriptorForProperty.get());
			}
		}
		
		prodListDescriptors = new DualHashBidiMap<>(tmpMap);
		
		final IColumnPropertyAccessor<Product> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<Product>(
				propertyNames);
//		final SpecialCellValueProvider specialCellValueProvider = new SpecialCellValueProvider(msg);

		// Add derived 'default' column
		final IColumnPropertyAccessor<Product> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<Product>() {

			public Object getDataValue(Product rowObject, int columnIndex) {
				ProductListDescriptor descriptor = prodListDescriptors.get(columnIndex);
				switch (descriptor) {
				case PRICE:
					// Fill the price column with the net or the gross price (
					// for quantity = 1)
//					String priceKey = "";
					if (getEclipsePrefs().getInt(Constants.PREFERENCES_PRODUCT_USE_NET_GROSS) == Constants.PRODUCT_USE_NET) {
//						priceKey = "$Price1Gross";
//                    cell.setText(new Price(product.getDoubleValueByKey("price1"), product.getDoubleValueByKeyFromOtherTable("vatid.VATS:value")).getUnitNet()
//                            .asFormatedString());
						return rowObject.getPrice1();
						// return Money.of(rowObject.getPrice1(), DataUtils.getInstance().getDefaultCurrencyUnit()).multiply(1+rowObject.getVat().getTaxValue());
					} else {
//						priceKey = "price1";
						return DataUtils.getInstance().CalculateGrossFromNet(rowObject.getPrice1(), rowObject.getVat().getTaxValue());
					}
				default:
					return columnPropertyAccessor.getDataValue(rowObject, columnIndex);
				}
			}

			public void setDataValue(Product rowObject, int columnIndex, Object newValue) {
				throw new UnsupportedOperationException("you can't change a value in list view!");
			}

			public int getColumnCount() {
				return prodListDescriptors.size();
			}

			public String getColumnProperty(int columnIndex) {
				ProductListDescriptor descriptor = prodListDescriptors.get(columnIndex);
				return msg.getMessageFromKey(descriptor.getMessageKey());
			}

			public int getColumnIndex(String propertyName) {
				return columnPropertyAccessor.getColumnIndex(propertyName);
			}
		};
		return derivedColumnPropertyAccessor;
	}
    
    public NatTable createListTable(Composite searchAndTableComposite) {

        productListData = GlazedLists.eventList(productsDAO.findAll(true));

        // get the visible properties to show in list view
        String[] propertyNames = productsDAO.getVisibleProperties();
        
        final IColumnPropertyAccessor<Product> derivedColumnPropertyAccessor = createColumnPropertyAccessor(propertyNames);

        /*
        // Mark the columns that are used by the search function.
        searchColumns = new String[3];
        searchColumns[0] = "name";
        searchColumns[1] = "description";
        searchColumns[2] = "value";
 */
        final MatcherEditor<Product> textMatcherEditor = new TextWidgetMatcherEditor<Product>(getSearchControl().getTextControl(), 
                GlazedLists.textFilterator(Product.class, Product_.itemNumber.getName(), Product_.name.getName(), Product_.description.getName()));
        
        // Filtered list for Search text field filter
        final FilterList<Product> textFilteredIssues = new FilterList<Product>(productListData, textMatcherEditor);
        // build the list for the tree-filtered values (i.e., the value list which is affected by
        // tree selection)
        treeFilteredIssues = new FilterList<Product>(textFilteredIssues);
        
        textFilteredIssues.addListEventListener(e -> {
            if(viewDataTableMode == ViewDataTableMode.DIALOG && textFilteredIssues.size() == 1) {
                selectedObject = textFilteredIssues.get(0);
                fireClosingEvent();
            }
        });
       
        gridListLayer = new EntityGridListLayer<>(treeFilteredIssues, propertyNames, derivedColumnPropertyAccessor, configRegistry);
        
        DataLayer tableDataLayer = gridListLayer.getBodyDataLayer();
        tableDataLayer.setColumnPercentageSizing(true);

        ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(gridListLayer.getBodyLayerStack());
        columnLabelAccumulator.registerColumnOverrides(prodListDescriptors.getKey(ProductListDescriptor.PRICE), MONEYVALUE_CELL_LABEL);
        if(prodListDescriptors.getKey(ProductListDescriptor.QUANTITY) != null) {
        	columnLabelAccumulator.registerColumnOverrides(prodListDescriptors.getKey(ProductListDescriptor.QUANTITY), NUMBER_CELL_LABEL);
        }
        if(prodListDescriptors.getKey(ProductListDescriptor.VAT) != null) {
        	columnLabelAccumulator.registerColumnOverrides(prodListDescriptors.getKey(ProductListDescriptor.VAT), VAT_CELL_LABEL);
        }

        // Register label accumulator
        gridListLayer.getBodyLayerStack().setConfigLabelAccumulator(columnLabelAccumulator);

        //turn the auto configuration off as we want to add our header menu configuration
        NatTable natTable = new NatTable(searchAndTableComposite, gridListLayer.getGridLayer(), false);
       
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));

        return natTable;
    }
    
    /**
     * @return the gridLayer
     */
    protected EntityGridListLayer<Product> getGridLayer() {
        return gridListLayer;
    }

    @Override
    protected TopicTreeViewer<ProductCategory> createCategoryTreeViewer(Composite top) {
        context.set(TopicTreeViewer.PARENT_COMPOSITE, top);
        context.set(TopicTreeViewer.USE_DOCUMENT_AND_CONTACT_FILTER, false);
        context.set(TopicTreeViewer.USE_ALL, true);
    	topicTreeViewer = (TopicTreeViewer<ProductCategory>)ContextInjectionFactory.make(TopicTreeViewer.class, context);
        categories = GlazedLists.eventList(productCategoriesDAO.findAll());
        topicTreeViewer.setInput(categories);
        // TODO boolean useDocumentAndContactFilter, boolean useAll könnte man eigentlich zusammenfassen.
        // Eins von beiden muß es doch geben, oder?
        topicTreeViewer.setLabelProvider(new TreeCategoryLabelProvider());
        return topicTreeViewer;
    }
    
    /**
     * Handle an incoming refresh command. This could be initiated by an editor 
     * which has just saved a new element (document, Product, payment etc). Here we ONLY
     * listen to "ProductEditor" events.<br />
     * The tree of {@link ProductCategory}s is updated because we use a GlazedList for
     * the source of the tree. The tree has a listener to the GlazedLists object (<code>categories</code> in this case) which will
     * react on every change of the underlying list (here in the field <code>categories</code>).
     * If the content of <code>categories</code> changes, the change event is fired and the 
     * {@link TopicTreeViewer} is updated.
     * 
     * @param message an incoming message
     */
    @Inject @Optional
    public void handleRefreshEvent(@UIEventTopic(ProductEditor.EDITOR_ID) String message) {
    	if(StringUtils.equals(message, Editor.UPDATE_EVENT) && !top.isDisposed()) {
	        sync.syncExec(() -> top.setRedraw(false));
	        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
	        GlazedLists.replaceAll(productListData, GlazedLists.eventList(productsDAO.findAll(true)), false);
	        GlazedLists.replaceAll(categories, GlazedLists.eventList(productCategoriesDAO.findAll(true)), false);
	        treeFilteredIssues.setMatcher(currentFilter);
	        sync.syncExec(() -> top.setRedraw(true));
    	}
    }

	@Override
	protected void handleAfterDeletion(Product objToDelete) {
		// check if we can delete the old category (if it's empty)
		if (objToDelete != null) {
			try {
				long countOfEntriesInCategory = productsDAO.countByCategory(objToDelete.getCategories());
				if (countOfEntriesInCategory == 0) {
					/* the category has to be set to null since the objToDelete isn't "really" deleted
				     * but only marked as "invisible". The reference to the category still remains,
					 * therefore we have to update it.
					 */
					productCategoriesDAO.deleteEmptyCategory(objToDelete.getCategories());
				}
			} catch (FakturamaStoringException e) {
				log.error(e, "can't delete empty category from object " + objToDelete.getName());
			}
		}
	}

    /**
     * Set the category filter with a given {@link TreeObjectType}.
     * 
     * @param filter
     *            The new filter string
     * @param treeObjectType
     *            the {@link TreeObjectType}
     */
    public void setCategoryFilter(String filter, TreeObjectType treeObjectType) {
        // Reset transaction and contact filter, set category filter
    	currentFilter = new CommonListItemMatcher<Product>(filter, treeObjectType, createRootNodeDescriptor(filter));
		treeFilteredIssues.setMatcher(currentFilter);
		
        //Refresh is done automagically...
    }

    protected boolean isHeaderLabelEnabled() {
        return false;
    }

    @Override
    public String getTableId() {
        return ID;
    }
    
    @Override
    protected MToolBar getMToolBar() {
        return listTablePart.getToolbar();
    }

    @Override
    protected String getToolbarAddItemCommandId() {
        return CommandIds.LISTTOOLBAR_ADD_PRODUCT;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.AbstractViewDataTable#getEditorId()
     */
    @Override
    protected String getEditorId() {
        return ProductEditor.ID;
    }

    @Override
    protected String getEditorTypeId() {
        return ProductEditor.class.getSimpleName();
    }
    
    class ProductTableConfiguration extends AbstractRegistryConfiguration {

		@Override
		public void configureRegistry(IConfigRegistry configRegistry) {
			Style styleLeftAligned = new Style();
			styleLeftAligned.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
			Style styleRightAligned = new Style();
			styleRightAligned.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
			Style styleCentered = new Style();
			styleCentered.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);
			
			MoneyDisplayConverter moneyDisplayConverter = ContextInjectionFactory.make(MoneyDisplayConverter.class, context);

			// default style for the most of the cells
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, // attribute to apply
			                                       styleLeftAligned, 				// value of the attribute
			                                       DisplayMode.NORMAL, 				// apply during normal rendering i.e not during selection or edit
			                                       GridRegion.BODY.toString()); 	// apply the above for all cells with this label
 
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL,             
                    MONEYVALUE_CELL_LABEL ); 
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    moneyDisplayConverter,
                    DisplayMode.NORMAL,
                    MONEYVALUE_CELL_LABEL);
            
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL,             
                    VAT_CELL_LABEL); 
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    new VatDisplayConverter(),
                    DisplayMode.NORMAL,
                    VAT_CELL_LABEL);
 
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
                    styleRightAligned,      
                    DisplayMode.NORMAL,             
                    NUMBER_CELL_LABEL); 
            DefaultDoubleDisplayConverter doubleDisplayConverter = new DefaultDoubleDisplayConverter(true);
            doubleDisplayConverter.setMaximumFractionDigits(eclipsePrefs.getInt(Constants.PREFERENCES_GENERAL_QUANTITY_DECIMALPLACES));
			configRegistry.registerConfigAttribute(
                    CellConfigAttributes.DISPLAY_CONVERTER,
                    doubleDisplayConverter,
                    DisplayMode.NORMAL,
                    NUMBER_CELL_LABEL);
		}
	}

    protected String getPopupId() {
        return POPUP_ID;
    }

    @Override
    protected AbstractDAO<Product> getEntityDAO() {
        return productsDAO;
    }
    
    @Focus
    public void focus() {
        if(natTable != null) {
            natTable.setFocus();
        }
    }

    @Override
    protected Class<Product> getEntityClass() {
        return Product.class;
    }
}
