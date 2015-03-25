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

import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.convert.PercentageDisplayConverter;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.painter.cell.CellPainterWrapper;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.PaddingDecorator;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.persistence.command.DisplayPersistenceDialogCommandHandler;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.menu.HeaderMenuConfiguration;
import org.eclipse.nebula.widgets.nattable.ui.menu.PopupMenuBuilder;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

import com.sebulli.fakturama.dao.VatCategoriesDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.VATCategory;
import com.sebulli.fakturama.parts.VatEditor;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.DefaultCheckmarkPainter;
import com.sebulli.fakturama.views.datatable.EntityGridListLayer;
import com.sebulli.fakturama.views.datatable.ListViewGridLayer;
import com.sebulli.fakturama.views.datatable.ListViewHeaderDataProvider;
import com.sebulli.fakturama.views.datatable.impl.NoHeaderRowOnlySelectionBindings;
import com.sebulli.fakturama.views.datatable.tree.model.TreeObject;
import com.sebulli.fakturama.views.datatable.tree.ui.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeCategoryLabelProvider;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

/**
 * Builds the VAT list table.
 *
 */
public class VATListTable extends AbstractViewDataTable<VAT, VATCategory> {

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private Logger log;

//    this is for synchronizing the UI thread (unused at the moment)
    @Inject    
    private UISynchronize synch;

    // ID of this view
    public static final String ID = "fakturama.views.vatTable";
    
    protected static final String POPUP_ID = "com.sebulli.fakturama.vatlist.popup";
    
    /**
     * Event Broker for receiving update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

    @Inject
    @Preference
    private IEclipsePreferences preferences;
    
    private EventList<VAT> vatListData;
    private EventList<VATCategory> categories;

    private Control top;
    
    @Inject
    private VatsDAO vatsDAO;

    @Inject
    private VatCategoriesDAO vatCategoriesDAO;
    
    private static final String DEFAULT_CELL_LABEL = "Standard_Cell_LABEL";
    private static final String TAXVALUE_CELL_LABEL = "TaxValue_Cell_LABEL";

    private ListViewGridLayer<VAT> gridLayer;
    private EntityGridListLayer<VAT> gridListLayer;

    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    protected FilterList<VAT> treeFilteredIssues;
    private SelectionLayer selectionLayer;

    @PostConstruct
    public Control createPartControl(Composite parent) {
    	log.info("create VAT list part");
        top = super.createPartControl(parent, VAT.class, true, ID);
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

        // Change the default sort key bindings. Note that 'auto configure' was turned off
        // for the SortHeaderLayer (setup in the GlazedListsGridLayer)
        natTable.addConfiguration(new SingleClickSortConfiguration());
        natTable.configure();
    }
	
    @Deprecated
    protected NatTable createListTable2(Composite searchAndTableComposite) {       
        // fill the underlying data source (GlazedList)
        vatListData = GlazedLists.eventList(vatsDAO.findAll());

        // get the visible properties to show in list view
        String[] propertyNames = vatsDAO.getVisibleProperties();

        final IColumnPropertyAccessor<VAT> derivedColumnPropertyAccessor = createColumnPropertyAccessor(propertyNames);

        //build the column header layer
        // Column header data provider includes derived properties
        IDataProvider columnHeaderDataProvider = new ListViewHeaderDataProvider<VAT>(propertyNames, derivedColumnPropertyAccessor); 

        // matcher input Search text field 
        final MatcherEditor<VAT> textMatcherEditor = new TextWidgetMatcherEditor<VAT>(searchText, new VATFilterator());
        
        // Filtered list for Search text field filter
        final FilterList<VAT> textFilteredIssues = new FilterList<VAT>(vatListData, textMatcherEditor);

        // build the list for the tree-filtered values (i.e., the value list which is affected by
        // tree selection)
        treeFilteredIssues = new FilterList<VAT>(textFilteredIssues);

        //create the body layer stack
        //build the grid layer
        gridLayer = new ListViewGridLayer<VAT>(treeFilteredIssues, derivedColumnPropertyAccessor, columnHeaderDataProvider, 
                configRegistry, false, false);
        DataLayer tableDataLayer = gridLayer.getBodyDataLayer();
        tableDataLayer.setColumnPercentageSizing(true);
        tableDataLayer.setColumnWidthPercentageByPosition(0, 5);
        tableDataLayer.setColumnWidthPercentageByPosition(1, 15);
        tableDataLayer.setColumnWidthPercentageByPosition(2, 75);
        tableDataLayer.setColumnWidthPercentageByPosition(3, 5);
//        GlazedListsEventLayer<VAT> vatListEventLayer = new GlazedListsEventLayer<VAT>(tableDataLayer, vatListData);

        // add a label accumulator to be able to register converter
        // this is crucial for using custom values display
//        vatListEventLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
        
        // Custom selection configuration
        selectionLayer = gridLayer.getSelectionLayer();
        
        // for further use, if we need it...
        //      ILayer columnHeaderLayer = gridLayer.getColumnHeaderLayer();
        //      ILayer rowHeaderLayer = gridLayer.getRowHeaderLayer();

//        IRowIdAccessor<VAT> rowIdAccessor = new IRowIdAccessor<VAT>() {
//            @Override
//            public Serializable getRowId(VAT rowObject) {
//                return rowObject.getId();
//            }
//        };
//        
//        //use a RowSelectionModel that will perform row selections and is able to identify a row via unique ID
//        RowSelectionModel<VAT> selectionModel = new RowSelectionModel<VAT>(selectionLayer, firstBodyDataProvider, rowIdAccessor, false);
//        selectionLayer.setSelectionModel(selectionModel);
////         Select complete rows
//        selectionLayer.addConfiguration(new RowOnlySelectionConfiguration<VAT>());

        // now is the time where we can create the NatTable itself

        // Label accumulator - adds labels to all cells with the given data value
        //      CellOverrideLabelAccumulator<VAT> cellLabelAccumulator =
        //          new CellOverrideLabelAccumulator<VAT>(gridLayer.getBodyDataProvider());
        //      cellLabelAccumulator.registerOverride(defaultVat, STANDARD_COLUMN_POSITION, DEFAULT_CELL_LABEL);

        // Create a label accumulator - adds custom labels to all cells which we
        // wish to render differently. In this case render as an image.
        ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(gridLayer.getBodyLayerStack());
        columnLabelAccumulator.registerColumnOverrides(VATListDescriptor.DEFAULT.getPosition(), DEFAULT_CELL_LABEL);
        columnLabelAccumulator.registerColumnOverrides(VATListDescriptor.VALUE.getPosition(), TAXVALUE_CELL_LABEL);

        final NatTable natTable = new NatTable(searchAndTableComposite/*, 
                SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | SWT.BORDER*/, gridLayer, false);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));
        
        // Register label accumulator
        gridLayer.getBodyLayerStack().setConfigLabelAccumulator(columnLabelAccumulator);

        // Register your custom cell painter, cell style, against the label applied to the cell.
        //      addImageTextToColumn(configRegistry, natTable, gridLayer.getBodyDataProvider());
        return natTable;
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

        vatListData = GlazedLists.eventList(vatsDAO.findAll());

        // get the visible properties to show in list view
        String[] propertyNames = vatsDAO.getVisibleProperties();

        final IColumnPropertyAccessor<VAT> derivedColumnPropertyAccessor = createColumnPropertyAccessor(propertyNames);

        // matcher input Search text field 
        final MatcherEditor<VAT> textMatcherEditor = new TextWidgetMatcherEditor<VAT>(searchText, new VATFilterator());
        
        // Filtered list for Search text field filter
        final FilterList<VAT> textFilteredIssues = new FilterList<VAT>(vatListData, textMatcherEditor);

        // build the list for the tree-filtered values (i.e., the value list which is affected by
        // tree selection)
        treeFilteredIssues = new FilterList<VAT>(textFilteredIssues);
       
        gridListLayer = new EntityGridListLayer<>(treeFilteredIssues, propertyNames, derivedColumnPropertyAccessor, configRegistry);
        
        DataLayer tableDataLayer = gridListLayer.getBodyDataLayer();
        tableDataLayer.setColumnPercentageSizing(true);
        tableDataLayer.setColumnWidthPercentageByPosition(0, 5);
        tableDataLayer.setColumnWidthPercentageByPosition(1, 15);
        tableDataLayer.setColumnWidthPercentageByPosition(2, 75);
        tableDataLayer.setColumnWidthPercentageByPosition(3, 5);

        ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(gridListLayer.getBodyLayerStack());
        columnLabelAccumulator.registerColumnOverrides(VATListDescriptor.DEFAULT.getPosition(), DEFAULT_CELL_LABEL);
        columnLabelAccumulator.registerColumnOverrides(VATListDescriptor.VALUE.getPosition(), TAXVALUE_CELL_LABEL);
       
        // Register label accumulator
        gridListLayer.getBodyLayerStack().setConfigLabelAccumulator(columnLabelAccumulator);

        //turn the auto configuration off as we want to add our header menu configuration
        NatTable natTable = new NatTable(searchAndTableComposite, gridListLayer.getGridLayer(), false);
        
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));
        
        postConfigureNatTable(natTable);
        
        natTable.registerCommandHandler(new DisplayPersistenceDialogCommandHandler(natTable));
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
        topicTreeViewer = new TopicTreeViewer<VATCategory>(top, msg, false, true);
        categories = GlazedLists.eventList(vatCategoriesDAO.findAll());
        topicTreeViewer.setInput(categories);
        // TODO boolean useDocumentAndContactFilter, boolean useAll könnte man eigentlich zusammenfassen.
        // Eins von beiden muß es doch geben, oder?
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
    public void handleRefreshEvent(@EventTopic(VatEditor.EDITOR_ID) String message) {
        synch.syncExec(new Runnable() {
            
            @Override
            public void run() {
                top.setRedraw(false);
            }
        });
        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
        vatListData.clear();
        vatListData.addAll(vatsDAO.findAll());
        categories.clear();
        categories.addAll(vatCategoriesDAO.findAll());
        synch.syncExec(new Runnable() {
           
            @Override
            public void run() {
        top.setRedraw(true);
            }});
    }

    /**
     * Set the category filter
     * 
     * @param filter
     *            The new filter string
     * 
     * @deprecated use {@link #setCategoryFilter(String, TreeObjectType)}
     *             instead
     */
    public void setCategoryFilter(String filter) {
        setCategoryFilter(filter, TreeObjectType.DEFAULT_NODE);
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
        // Set the label with the filter string
        if (filter.equals("$shownothing"))
            filterLabel.setText("");
        else
        // Display the localized list names.
        // or the document type
        if (isHeaderLabelEnabled())
            filterLabel.setText("DataSetListNames.NAMES.getLocalizedName: " + filter);

        filterLabel.pack(true);

        // Reset transaction and contact filter, set category filter
        treeFilteredIssues.setMatcher(new VATMatcher(filter, treeObjectType,((TreeObject)topicTreeViewer.getTree().getTopItem().getData()).getName()));
        //   contentProvider.setTreeObject(treeObject);

        // Set category to the addNew action. So a new data set is created
        // with the selected category
        //   if (addNewAction != null) {
        //       addNewAction.setCategory(filter);
        //   }

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
        return VatEditor.ID;
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
					new PercentageDisplayConverter(),
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

    public void removeSelectedEntry() {
        if(selectionLayer.getFullySelectedRowPositions().length > 0) {
            VAT objToDelete = gridLayer.getBodyDataProvider().getRowObject(selectionLayer.getFullySelectedRowPositions()[0]);
            try {
                // don't delete the entry because it could be referenced
                // from another entity
                objToDelete.setDeleted(Boolean.TRUE);
                vatsDAO.save(objToDelete);
            }
            catch (SQLException e) {
                log.error(e, "can't save the current VAT: " + objToDelete.toString());
            }
    
            // Refresh the table view of all VATs
            evtBroker.post("VatEditor", "update");
        } else {
            log.debug("no rows selected!");
        }
    }

    protected String getPopupId() {
        return POPUP_ID;
    }
}

