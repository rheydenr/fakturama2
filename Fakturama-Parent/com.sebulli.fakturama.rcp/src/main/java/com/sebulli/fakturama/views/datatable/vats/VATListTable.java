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
package com.sebulli.fakturama.views.datatable.vats;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.extension.e4.selection.E4SelectionListener;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
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
import com.sebulli.fakturama.dao.VatCategoriesDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.VATCategory;
import com.sebulli.fakturama.model.VAT_;
import com.sebulli.fakturama.parts.Editor;
import com.sebulli.fakturama.parts.VatEditor;
import com.sebulli.fakturama.parts.converter.DoublePercentageDisplayConverter;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.DefaultCheckmarkPainter;
import com.sebulli.fakturama.views.datatable.EntityGridListLayer;
import com.sebulli.fakturama.views.datatable.impl.NoHeaderRowOnlySelectionBindings;
import com.sebulli.fakturama.views.datatable.tree.ui.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeCategoryLabelProvider;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

/**
 * Builds the VAT list table.
 *
 */
public class VATListTable extends AbstractViewDataTable<VAT, VATCategory> {

    // ID of this view
    public static final String ID = "fakturama.views.vatTable";
    
    protected static final String POPUP_ID = "com.sebulli.fakturama.vatlist.popup";

/**    this is for synchronizing the UI thread */
    @Inject    
    private UISynchronize sync;
    
    @Inject
    private VatsDAO vatsDAO;
    
    @Inject
    protected IEclipseContext context;

    @Inject
    private VatCategoriesDAO vatCategoriesDAO;

    @Inject
    @Preference
    private IEclipsePreferences preferences;
    
	@Inject
	private ILocaleService localeUtil;

    
    private EventList<VAT> vatListData;
    private EventList<VATCategory> categories;
    
    private static final String DEFAULT_CELL_LABEL = "Standard_Cell_LABEL";
    private static final String TAXVALUE_CELL_LABEL = "TaxValue_Cell_LABEL";

    private EntityGridListLayer<VAT> gridListLayer;

    private MPart listTablePart;

    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    protected FilterList<VAT> treeFilteredIssues;

	private VATMatcher currentFilter;

    @PostConstruct
    public Control createPartControl(Composite parent, MPart listTablePart) {
    	log.info("create VAT list part");
        this.listTablePart = listTablePart;
        super.createPartControl(parent, VAT.class, true, ID);
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
        natTable.addConfiguration(new VATTableConfiguration());
        natTable.setBackground(GUIHelper.COLOR_WHITE);
        // nur für das Headermenü, falls das mal irgendwann gebraucht werden sollte
        //		natTable.addConfiguration(new HeaderMenuConfiguration(n6));
        gridListLayer.getSelectionLayer().getSelectionModel().setMultipleSelectionAllowed(true);

        E4SelectionListener<VAT> esl = new E4SelectionListener<>(selectionService, gridListLayer.getSelectionLayer(), gridListLayer.getBodyDataProvider());
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
                        }                   
                    }
                });
        natTable.configure();
    }

    /**
     * @param propertyNames
     * @return
     */
    private IColumnPropertyAccessor<VAT> createColumnPropertyAccessor(String[] propertyNames) {
        final IColumnPropertyAccessor<VAT> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<VAT>(propertyNames);
        
        // Add derived 'default' column
        final IColumnPropertyAccessor<VAT> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<VAT>() {

            public Object getDataValue(VAT rowObject, int columnIndex) {
                VATListDescriptor descriptor = VATListDescriptor.getDescriptorFromColumn(columnIndex);
                switch (descriptor) {
                case DEFAULT:
                    return rowObject.getId() == getDefaultVATId();
                case NAME:
                case DESCRIPTION:
                case VALUE:
                    // alternative: return rowObject.getFirstName();
                    return columnPropertyAccessor.getDataValue(rowObject, columnIndex - 1);
                default:
                    break;
                }
                return null;
            }

            public void setDataValue(VAT rowObject, int columnIndex, Object newValue) {
                throw new UnsupportedOperationException("you can't change a value in list view!");
            }

            public int getColumnCount() {
                return VATListDescriptor.getVATPropertyNames().length;
            }

            public String getColumnProperty(int columnIndex) {
                VATListDescriptor descriptor = VATListDescriptor.getDescriptorFromColumn(columnIndex);
                return msg.getMessageFromKey(descriptor.getMessageKey());
            }

            public int getColumnIndex(String propertyName) {
                if (VATListDescriptor.DEFAULT.getPropertyName().equals(propertyName)) {
                    return VATListDescriptor.DEFAULT.getPosition();
                } else {
                    return columnPropertyAccessor.getColumnIndex(propertyName) + 1;
                }
            }
        };
        return derivedColumnPropertyAccessor;
    }
    
    public NatTable createListTable(Composite searchAndTableComposite) {

        vatListData = GlazedLists.eventList(vatsDAO.findAll(true));

        // get the visible properties to show in list view
        String[] propertyNames = vatsDAO.getVisibleProperties();

        final IColumnPropertyAccessor<VAT> derivedColumnPropertyAccessor = createColumnPropertyAccessor(propertyNames);

        /*
        // Mark the columns that are used by the search function.
        searchColumns = new String[3];
        searchColumns[0] = "name";
        searchColumns[1] = "description";
        searchColumns[2] = "value";
 */
        final MatcherEditor<VAT> textMatcherEditor = new TextWidgetMatcherEditor<VAT>(searchText.getTextControl(), 
                GlazedLists.textFilterator(VAT.class, VAT_.name.getName(), VAT_.description.getName()));
        
        // Filtered list for Search text field filter
        final FilterList<VAT> textFilteredIssues = new FilterList<VAT>(vatListData, textMatcherEditor);

        // build the list for the tree-filtered values (i.e., the value list which is affected by
        // tree selection)
        treeFilteredIssues = new FilterList<VAT>(textFilteredIssues);
       
        gridListLayer = new EntityGridListLayer<>(treeFilteredIssues, propertyNames, derivedColumnPropertyAccessor, configRegistry);
        
        DataLayer tableDataLayer = gridListLayer.getBodyDataLayer();
        tableDataLayer.setColumnPercentageSizing(true);
//        tableDataLayer.setColumnWidthPercentageByPosition(0, 5);
//        tableDataLayer.setColumnWidthPercentageByPosition(1, 15);
//        tableDataLayer.setColumnWidthPercentageByPosition(2, 75);
//        tableDataLayer.setColumnWidthPercentageByPosition(3, 5);

        ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(gridListLayer.getBodyLayerStack());
        columnLabelAccumulator.registerColumnOverrides(VATListDescriptor.DEFAULT.getPosition(), DEFAULT_CELL_LABEL);
        columnLabelAccumulator.registerColumnOverrides(VATListDescriptor.VALUE.getPosition(), TAXVALUE_CELL_LABEL);
       
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
    protected EntityGridListLayer<VAT> getGridLayer() {
        return gridListLayer;
    }

    @Override
    protected TopicTreeViewer<VATCategory> createCategoryTreeViewer(Composite top) {
        context.set(TopicTreeViewer.PARENT_COMPOSITE, top);
        context.set(TopicTreeViewer.USE_DOCUMENT_AND_CONTACT_FILTER, false);
        context.set(TopicTreeViewer.USE_ALL, true);
    	topicTreeViewer = (TopicTreeViewer<VATCategory>)ContextInjectionFactory.make(TopicTreeViewer.class, context);
        categories = GlazedLists.eventList(vatCategoriesDAO.findAll());
        topicTreeViewer.setInput(categories);
        topicTreeViewer.setLabelProvider(new TreeCategoryLabelProvider());
        return topicTreeViewer;
    }

    /**
     * reads the default VAT value from preference store and returns the
     * appropriate VAT-ID value from database
     * 
     * @return
     */
    private long getDefaultVATId() {
        return preferences.getLong(Constants.DEFAULT_VAT, 1L);
    }
    
    /**
     * Handle an incoming refresh command. This could be initiated by an editor 
     * which has just saved a new element (document, VAT, payment etc). Here we ONLY
     * listen to "VatEditor" events.<br />
     * The tree of {@link VATCategory}s is updated because we use a GlazedList for
     * the source of the tree. The tree has a listener to the GlazedLists object (<code>categories</code> in this case) which will
     * react on every change of the underlying list (here in the field <code>categories</code>).
     * If the content of <code>categories</code> changes, the change event is fired and the 
     * {@link TopicTreeViewer} is updated.
     * 
     * @param message an incoming message
     */
    @Inject @Optional
    public void handleRefreshEvent(@UIEventTopic(VatEditor.EDITOR_ID) String message) {
    	if(StringUtils.startsWith(message, Editor.UPDATE_EVENT) && !top.isDisposed()) {
	        sync.syncExec(() -> top.setRedraw(false));
	        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
	        GlazedLists.replaceAll(vatListData, GlazedLists.eventList(vatsDAO.findAll(true)), false);
	        GlazedLists.replaceAll(categories, GlazedLists.eventList(vatCategoriesDAO.findAll(true)), false);
	        treeFilteredIssues.setMatcher(currentFilter);
	        sync.syncExec(() -> top.setRedraw(true));
    	}
    }

	@Override
	protected void handleAfterDeletion(VAT objToDelete) {
		// check if we can delete the old category (if it's empty)
		if (objToDelete != null) {
			try {
				long countOfEntriesInCategory = vatsDAO.countByCategory(objToDelete.getCategory());
				if (countOfEntriesInCategory == 0) {
					/* the category has to be set to null since the objToDelete isn't "really" deleted
				     * but only marked as "invisible". The reference to the category still remains,
					 * therefore we have to update it.
					 */
					VATCategory oldCat = objToDelete.getCategory();
					objToDelete.setCategory(null);
					getEntityDAO().update(objToDelete);
					vatCategoriesDAO.deleteEmptyCategory(oldCat);
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
        currentFilter = new VATMatcher(filter, treeObjectType, createRootNodeDescriptor(filter));
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
        return CommandIds.LISTTOOLBAR_ADD_VAT;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.AbstractViewDataTable#getEditorId()
     */
    @Override
    protected String getEditorId() {
        return VatEditor.ID;
    }

    @Override
    protected String getEditorTypeId() {
        return VatEditor.class.getSimpleName();
    }
    
    class VATTableConfiguration extends AbstractRegistryConfiguration {

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

			configRegistry.registerConfigAttribute(
					CellConfigAttributes.CELL_PAINTER, 
					new DefaultCheckmarkPainter(),
					DisplayMode.NORMAL, DEFAULT_CELL_LABEL);
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
					styleCentered, 		
					DisplayMode.NORMAL, 			
					DEFAULT_CELL_LABEL); 

			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,
					styleRightAligned, 		
					DisplayMode.NORMAL, 			
					TAXVALUE_CELL_LABEL ); 
			configRegistry.registerConfigAttribute(
					CellConfigAttributes.DISPLAY_CONVERTER,
					new DoublePercentageDisplayConverter(localeUtil),
					DisplayMode.NORMAL,
					TAXVALUE_CELL_LABEL);
            // have a little space between cell border and value
			CellPainterWrapper painter = new PaddingDecorator(new TextPainter(), 0, 5, 0, 0);
            configRegistry.registerConfigAttribute(
                    CellConfigAttributes.CELL_PAINTER,
                    painter,
                    DisplayMode.NORMAL,
                    TAXVALUE_CELL_LABEL);
		}
	}

    protected String getPopupId() {
        return POPUP_ID;
    }

    @Override
    protected AbstractDAO<VAT> getEntityDAO() {
        return vatsDAO;
    }
    
    @Override
    protected Class<VAT> getEntityClass() {
    	return VAT.class;
    }
}
