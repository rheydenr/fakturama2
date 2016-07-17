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
package com.sebulli.fakturama.views.datatable.lists;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
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

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.ItemAccountTypeDAO;
import com.sebulli.fakturama.dao.ItemListTypeCategoriesDAO;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.model.ItemAccountType;
import com.sebulli.fakturama.model.ItemAccountType_;
import com.sebulli.fakturama.model.ItemListTypeCategory;
import com.sebulli.fakturama.parts.Editor;
import com.sebulli.fakturama.parts.ListEditor;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.EntityGridListLayer;
import com.sebulli.fakturama.views.datatable.impl.NoHeaderRowOnlySelectionBindings;
import com.sebulli.fakturama.views.datatable.tree.model.TreeObject;
import com.sebulli.fakturama.views.datatable.tree.ui.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeCategoryLabelProvider;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

/**
 * Builds the ItemAccountType list table.
 *
 */
public class ItemAccountTypeListTable extends AbstractViewDataTable<ItemAccountType, ItemListTypeCategory> {
 
	@Inject
    protected IEclipseContext context;
    
    // ID of this view
    public static final String ID = "fakturama.views.listTable";
    
    protected static final String POPUP_ID = "com.sebulli.fakturama.listTable.popup";

/**    this is for synchronizing the UI thread */
    @Inject    
    private UISynchronize sync;

    @Inject
    private ItemAccountTypeDAO itemAccountTypeDAO;

    @Inject
    private ItemListTypeCategoriesDAO itemListTypeCategoriesDAO;
    
    private EventList<ItemAccountType> itemAccountTypeData;
    private EventList<ItemListTypeCategory> categories;
    
    private EntityGridListLayer<ItemAccountType> gridListLayer;

    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    protected FilterList<ItemAccountType> treeFilteredIssues;

	private ItemAccountTypeMatcher currentFilter;

    @PostConstruct
    public Control createPartControl(Composite parent, MPart listTablePart) {
    	log.info("create ItemAccountType list part");
        super.createPartControl(parent, ItemAccountType.class, true, ID);
        // Listen to double clicks
        hookDoubleClickCommand2(natTable, gridListLayer);
        topicTreeViewer.setTable(this);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        return top;
    }

    @Override
    protected void postConfigureNatTable(NatTable natTable) {
        //as the autoconfiguration of the NatTable is turned off, we have to add the 
        //DefaultNatTableStyleConfiguration and the ConfigRegistry manually	
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new NoHeaderRowOnlySelectionBindings());
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new ItemAccountTypeTableConfiguration());
        natTable.setBackground(GUIHelper.COLOR_WHITE);
        // nur für das Headermenü, falls das mal irgendwann gebraucht werden sollte
        //		natTable.addConfiguration(new HeaderMenuConfiguration(n6));

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
                        }                   
                    }
                });
        natTable.configure();
    }

    /**
     * @param propertyNames
     * @return
     */
    private IColumnPropertyAccessor<ItemAccountType> createColumnPropertyAccessor(String[] propertyNames) {
        final IColumnPropertyAccessor<ItemAccountType> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<ItemAccountType>(propertyNames);
        
        // Add derived 'default' column
        final IColumnPropertyAccessor<ItemAccountType> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<ItemAccountType>() {

            public Object getDataValue(ItemAccountType rowObject, int columnIndex) {
                ItemAccountTypeListDescriptor descriptor = ItemAccountTypeListDescriptor.getDescriptorFromColumn(columnIndex);
                switch (descriptor) {
                case NAME:
                case VALUE:
                    // alternative: return rowObject.getFirstName();
                    return columnPropertyAccessor.getDataValue(rowObject, columnIndex);
                default:
                    break;
                }
                return null;
            }

            public void setDataValue(ItemAccountType rowObject, int columnIndex, Object newValue) {
                throw new UnsupportedOperationException("you can't change a value in list view!");
            }

            public int getColumnCount() {
                return ItemAccountTypeListDescriptor.getItemAccountTypePropertyNames().length;
            }

            public String getColumnProperty(int columnIndex) {
                ItemAccountTypeListDescriptor descriptor = ItemAccountTypeListDescriptor.getDescriptorFromColumn(columnIndex);
                return msg.getMessageFromKey(descriptor.getMessageKey());
            }

            public int getColumnIndex(String propertyName) {
                    return columnPropertyAccessor.getColumnIndex(propertyName) + 1;
            }
        };
        return derivedColumnPropertyAccessor;
    }
    
    public NatTable createListTable(Composite searchAndTableComposite) {

        itemAccountTypeData = GlazedLists.readOnlyList(GlazedLists.eventList(itemAccountTypeDAO.findAll(true)));

        // get the visible properties to show in list view
        String[] propertyNames = itemAccountTypeDAO.getVisibleProperties();

        final IColumnPropertyAccessor<ItemAccountType> derivedColumnPropertyAccessor = createColumnPropertyAccessor(propertyNames);

        /*
        // Mark the columns that are used by the search function.
        searchColumns = new String[2];
        searchColumns[0] = "name";
        searchColumns[1] = "value";
 */
        final MatcherEditor<ItemAccountType> textMatcherEditor = new TextWidgetMatcherEditor<ItemAccountType>(searchText, 
                GlazedLists.textFilterator(ItemAccountType.class, ItemAccountType_.name.getName(), ItemAccountType_.value.getName()));
        
        // Filtered list for Search text field filter
        final FilterList<ItemAccountType> textFilteredIssues = new FilterList<ItemAccountType>(itemAccountTypeData, textMatcherEditor);

        // build the list for the tree-filtered values (i.e., the value list which is affected by
        // tree selection)
        treeFilteredIssues = new FilterList<ItemAccountType>(textFilteredIssues);
       
        gridListLayer = new EntityGridListLayer<>(treeFilteredIssues, propertyNames, derivedColumnPropertyAccessor, configRegistry);
        
        DataLayer tableDataLayer = gridListLayer.getBodyDataLayer();
        tableDataLayer.setColumnPercentageSizing(true);
        tableDataLayer.setColumnWidthPercentageByPosition(0, 25);
        tableDataLayer.setColumnWidthPercentageByPosition(1, 25);

        ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(gridListLayer.getBodyLayerStack());
       
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
    protected EntityGridListLayer<ItemAccountType> getGridLayer() {
        return gridListLayer;
    }

    @Override
    protected TopicTreeViewer<ItemListTypeCategory> createCategoryTreeViewer(Composite top) {
//    	topicTreeViewer = (TopicTreeViewer<ItemListTypeCategory>)ContextInjectionFactory.make(TopicTreeViewer.class, context);
        topicTreeViewer = new TopicTreeViewer<ItemListTypeCategory>(top, msg, false, true);
        List<ItemListTypeCategory> categoryList = itemListTypeCategoriesDAO.findAll();
        categories = GlazedLists.eventList(categoryList);
        categories.forEach(cat -> msg.getMessageFromKey(cat.getName()));
        topicTreeViewer.setInput(categories);
        topicTreeViewer.setLabelProvider(new TreeCategoryLabelProvider() {
            public String getText(Object itemListTypeCategory) {
                if(itemListTypeCategory instanceof TreeObject && ((TreeObject)itemListTypeCategory).getNodeType() == TreeObjectType.ALL_NODE) {
                    return super.getText(itemListTypeCategory);
                } else {
                    return msg.getMessageFromKey(((TreeObject)itemListTypeCategory).getName());
                }
            };
        });
        return topicTreeViewer;
    }
    
    /**
     * Handle an incoming refresh command. This could be initiated by an editor 
     * which has just saved a new element (document, ItemAccountType, payment etc). Here we ONLY
     * listen to "VatEditor" events.<br />
     * The tree of {@link ItemListTypeCategory}s is updated because we use a GlazedList for
     * the source of the tree. The tree has a listener to the GlazedLists object (<code>categories</code> in this case) which will
     * react on every change of the underlying list (here in the field <code>categories</code>).
     * If the content of <code>categories</code> changes, the change event is fired and the 
     * {@link TopicTreeViewer} is updated.
     * 
     * @param message an incoming message
     */
    @Inject @Optional
    public void handleRefreshEvent(@EventTopic(ListEditor.EDITOR_ID) String message) {
    	if(StringUtils.equals(message, Editor.UPDATE_EVENT)) {
	        sync.syncExec(() -> top.setRedraw(false));
	        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
	        GlazedLists.replaceAll(itemAccountTypeData, GlazedLists.eventList(itemAccountTypeDAO.findAll(true)), false);
	        GlazedLists.replaceAll(categories, GlazedLists.eventList(itemListTypeCategoriesDAO.findAll(true)), false);
	        treeFilteredIssues.setMatcher(currentFilter);
	        sync.syncExec(() -> top.setRedraw(true));
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
        currentFilter = new ItemAccountTypeMatcher(filter, treeObjectType, ((TreeObject) topicTreeViewer.getTree().getTopItem().getData()).getName());
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

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.AbstractViewDataTable#getEditorId()
     */
    @Override
    protected String getEditorId() {
        return ListEditor.ID;
    }

    @Override
    protected String getEditorTypeId() {
        return ListEditor.class.getSimpleName();
    }
    
    class ItemAccountTypeTableConfiguration extends AbstractRegistryConfiguration {

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
			                                       styleLeftAligned, 				// value of the attribute
			                                       DisplayMode.NORMAL, 				// apply during normal rendering i.e not during selection or edit
			                                       GridRegion.BODY.toString()); 	// apply the above for all cells with this label
		}
	}

    protected String getPopupId() {
        return POPUP_ID;
    }

    @Override
    protected AbstractDAO<ItemAccountType> getEntityDAO() {
        return itemAccountTypeDAO;
    }
}
