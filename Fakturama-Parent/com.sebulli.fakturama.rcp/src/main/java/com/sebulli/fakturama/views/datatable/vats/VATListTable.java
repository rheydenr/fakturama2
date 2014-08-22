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

import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.coordinate.Range;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.PercentageDisplayConverter;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.ColumnSelectionEvent;
import org.eclipse.nebula.widgets.nattable.selection.event.RowSelectionEvent;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

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
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.DefaultCheckmarkPainter;
import com.sebulli.fakturama.views.datatable.ListViewGridLayer;
import com.sebulli.fakturama.views.datatable.impl.NoHeaderRowOnlySelectionBindings;
import com.sebulli.fakturama.views.datatable.tree.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.TreeCategoryLabelProvider;
import com.sebulli.fakturama.views.datatable.tree.TreeObjectType;

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

    // ID of this view
    public static final String ID = "com.sebulli.fakturama.views.vatTable";
    
    /**
     * Event Broker for receiving update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

    @Inject
    @Preference
    private IEclipsePreferences preferences;

    private EventList<VAT> eventList;

    @Inject
    private VatsDAO vatsDAO;

    @Inject
    private VatCategoriesDAO vatCategoriesDAO;
    
    private static final String DEFAULT_CELL_LABEL = "Standard_Cell_LABEL";
    private static final String TAXVALUE_CELL_LABEL = "TaxValue_Cell_LABEL";

    /**
     * controls if the header label for the list view should be shown
     */
    private final boolean headerLabelEnabled = false;

    private ListViewGridLayer<VAT> gridLayer;
    
    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();

    protected FilterList<VAT> treeFilteredIssues;

    private SelectionLayer selectionLayer;

    private DataLayer tableDataLayer;
    private int lastSelectedRow = -1;

    @PostConstruct
    public Control createPartControl(Composite parent) {
        Control top = super.createPartControl(parent, VAT.class, false, true, ID);

        // Listen to double clicks
        hookDoubleClickCommand(natTable, gridLayer);
        topicTreeViewer.setTable(this);

        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        return top;
    }

    @Override
    protected void postConfigureNatTable(NatTable natTable) {

        //as the autoconfiguration of the NatTable is turned off, we have to add the 
        //DefaultNatTableStyleConfiguration and the ConfigRegistry manually	
        addCustomStyling(natTable);
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new NoHeaderRowOnlySelectionBindings());
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new VATTableConfiguration());
        // nur für das Headermenü, falls das mal irgendwann gebraucht werden sollte
        //		natTable.addConfiguration(new HeaderMenuConfiguration(n6));

        // Change the default sort key bindings. Note that 'auto configure' was turned off
        // for the SortHeaderLayer (setup in the GlazedListsGridLayer)
        natTable.addConfiguration(new SingleClickSortConfiguration());
        natTable.configure();
    }
	
    protected NatTable createListTable(Composite searchAndTableComposite) {
        //mapping from property to label, needed for column header labels
        final Map<String, String> propertyToLabelMap = vatsDAO.getVisiblePropertyToLabelMap();
        
        // fill the underlying data source (GlazedList)
        eventList = GlazedLists.eventList(vatsDAO.findAll());

        // get the visible properties to show in list view
        String[] propertyNames = vatsDAO.getVisibleProperties();

        final IColumnPropertyAccessor<VAT> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<VAT>(propertyNames);
        
        // Add derived 'fullName' column
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
                switch (descriptor) {
                case DEFAULT:
                    return msg.commonLabelDefault;
                case NAME:
                case DESCRIPTION:
                case VALUE:
                    return propertyToLabelMap.get(columnPropertyAccessor.getColumnProperty(columnIndex - 1));
                default:
                    break;
                }
                return null;
            }

            public int getColumnIndex(String propertyName) {
                if (VATListDescriptor.DEFAULT.getPropertyName().equals(propertyName)) {
                    return VATListDescriptor.DEFAULT.getPosition();
                } else {
                    return columnPropertyAccessor.getColumnIndex(propertyName) + 1;
                }
            }
        };

        //build the column header layer
        // Column header data provider includes derived properties
        IDataProvider columnHeaderDataProvider = new IDataProvider() {

            public Object getDataValue(int columnIndex, int rowIndex) {
                return derivedColumnPropertyAccessor.getColumnProperty(columnIndex);
            }

            public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
                // noop
            }

            public int getColumnCount() {
                return derivedColumnPropertyAccessor.getColumnCount();
            }

            public int getRowCount() {
                return 1;
            }

        };

        // matcher input Search text field 
        final MatcherEditor<VAT> textMatcherEditor = new TextWidgetMatcherEditor<VAT>(searchText, new VATFilterator());
        
        // Filtered list for Search text field filter
        final FilterList<VAT> textFilteredIssues = new FilterList<VAT>(eventList, textMatcherEditor);

        // build the list for the tree-filtered values (i.e., the value list which is affected by
        // tree selection)
        treeFilteredIssues = new FilterList<VAT>(textFilteredIssues);

        //create the body layer stack
        final IRowDataProvider<VAT> firstBodyDataProvider = 
                new GlazedListsDataProvider<VAT>(treeFilteredIssues, columnPropertyAccessor);
        
        //build the grid layer
        gridLayer = new ListViewGridLayer<VAT>(treeFilteredIssues, derivedColumnPropertyAccessor, columnHeaderDataProvider, configRegistry, true);
        tableDataLayer = gridLayer.getBodyDataLayer();        

        GlazedListsEventLayer<VAT> vatListEventLayer = new GlazedListsEventLayer<VAT>(tableDataLayer, eventList);
//        DefaultBodyLayerStack bodyLayer = new DefaultBodyLayerStack(vatListEventLayer);

        // add a label accumulator to be able to register converter
        // this is crucial for using custom values display
        tableDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

        //example for mixed percentage sizing in a grid
        //configure not every column with the exact percentage value, this way the columns for which
        //no exact values are set will use the remaining space
//        tableDataLayer.setColumnWidthByPosition(VATListDescriptor.DEFAULT.getPosition(), 5);
//        tableDataLayer.setColumnWidthByPosition(VATListDescriptor.NAME.getPosition(), 20);
//        tableDataLayer.setColumnWidthByPosition(VATListDescriptor.VALUE.getPosition(), 10);
//        tableDataLayer.setColumnPercentageSizing(true);
        
        selectionLayer = new SelectionLayer(vatListEventLayer);
        
        // for further use, if we need it...
        //      ILayer columnHeaderLayer = gridLayer.getColumnHeaderLayer();
        //      ILayer rowHeaderLayer = gridLayer.getRowHeaderLayer();

        IRowIdAccessor<VAT> rowIdAccessor = new IRowIdAccessor<VAT>() {
            @Override
            public Serializable getRowId(VAT rowObject) {
                return rowObject.getId();
            }
        };
        
        //use a RowSelectionModel that will perform row selections and is able to identify a row via unique ID
        RowSelectionModel<VAT> selectionModel = new RowSelectionModel<VAT>(selectionLayer, firstBodyDataProvider, rowIdAccessor, false);
        selectionLayer.setSelectionModel(selectionModel);
//         Select complete rows
//        selectionLayer.addConfiguration(new RowOnlySelectionConfiguration<VAT>());

        // now is the time where we can create the NatTable itself

        //      // Label accumulator - adds labels to all cells with the given data value
        //      CellOverrideLabelAccumulator<VAT> cellLabelAccumulator =
        //          new CellOverrideLabelAccumulator<VAT>(gridLayer.getBodyDataProvider());
        //      cellLabelAccumulator.registerOverride(defaultVat, STANDARD_COLUMN_POSITION, DEFAULT_CELL_LABEL);

        // Create a label accumulator - adds custom labels to all cells which we
        // wish to render differently. In this case render as an image.
        ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(gridLayer.getBodyLayerStack());
        columnLabelAccumulator.registerColumnOverrides(VATListDescriptor.DEFAULT.getPosition(), DEFAULT_CELL_LABEL);
        columnLabelAccumulator.registerColumnOverrides(VATListDescriptor.VALUE.getPosition(), TAXVALUE_CELL_LABEL);

        final NatTable natTable = new NatTable(searchAndTableComposite, gridLayer, false);

        natTable.addLayerListener(new ILayerListener() {

            // Default selection behavior selects cells by default.
            @Override
            public void handleLayerEvent(ILayerEvent event) {
                if (event instanceof CellSelectionEvent) {
                    CellSelectionEvent cellEvent = (CellSelectionEvent) event;
                    lastSelectedRow = cellEvent.getRowPosition();
//                    System.out.println("Selected cell: [" +
//                            cellEvent.getRowPosition() + ", " +
//                            cellEvent.getColumnPosition() + "], " +
//                            natTable.getDataValueByPosition(cellEvent.getColumnPosition(), cellEvent.getRowPosition()));
                }
                else if (event instanceof ColumnSelectionEvent) {
                    ColumnSelectionEvent columnEvent = (ColumnSelectionEvent) event;
                    System.out.println("Selected Column: " + columnEvent.getColumnPositionRanges());
                }
                else if (event instanceof RowSelectionEvent) {
                    //directly ask the SelectionLayer about the selected rows and access the data via IRowDataProvider
                    Collection<Range> selections = selectionLayer.getSelectedRowPositions();
                    StringBuilder builder = new StringBuilder("Selected Persons: ")
                        .append(selectionLayer.getSelectedRowPositions())
                        .append("[");
                    for (Range r : selections) {
                        for (int i = r.start; i < r.end; i++) {
                            VAT p = gridLayer.getBodyDataProvider().getRowObject(i);
                            if (p != null) {
                                if (!builder.toString().endsWith("[")) {
                                    builder.append(", ");
                                }
                                builder.append(p.getName());
                            }
                        }
                    }
                    builder.append("]");
                    System.out.println(builder.toString());
                }
            }
        });
        
        // Register label accumulator
        gridLayer.getBodyLayerStack().setConfigLabelAccumulator(columnLabelAccumulator);

        // Register your custom cell painter, cell style, against the label applied to the cell.
        //      addImageTextToColumn(configRegistry, natTable, gridLayer.getBodyDataProvider());
        return natTable;
    }

    @Override
    protected TopicTreeViewer<VATCategory> createCategoryTreeViewer(Composite top) {
        TopicTreeViewer<VATCategory> topicTreeViewer = new TopicTreeViewer<VATCategory>(top, msg, false, true);
        topicTreeViewer.setInput(GlazedLists.eventList(vatCategoriesDAO.findAll()));
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
    
    @Inject @Optional
    public void handleRefreshEvent(@EventTopic("VatEditor") String message) {
        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
        eventList.clear();
        eventList.addAll(vatsDAO.findAll());
    }


    private void addImageTextToColumn(IConfigRegistry configRegistry, Composite parent, IDataProvider iDataProvider) {
        ICellPainter imageTextPainter = new CellPainterDecorator(new TextPainter(), CellEdgeEnum.RIGHT, new MyImagePainter(iDataProvider));

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER, imageTextPainter, DisplayMode.NORMAL, CUSTOM_CELL_LABEL);

        // Set the color of the cell. This is picked up by the button painter to style the button
        Style style = new Style();
        style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_WHITE);

        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.NORMAL, CUSTOM_CELL_LABEL);
        configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, style, DisplayMode.SELECT, CUSTOM_CELL_LABEL);
    }

    //My image painter
    class MyImagePainter extends ImagePainter {

        private IDataProvider data = null;

        public MyImagePainter(IDataProvider iDataProvider) {
            this.data = iDataProvider; //this would be useful, what image has to displayed on call.
        }

        @Override
        protected Image getImage(ILayerCell cell, IConfigRegistry configRegistry) {
            //return specific image on data object and it's properties.
            // cell.getDataValue();
            return GUIHelper.getImage("preferences");
        }
    }

    /**
     * We have to style the table a little bit...
     * 
     * @param natTable
     *            the {@link NatTable} to style
     */
    private void addCustomStyling(NatTable natTable) {
        // NOTE: Getting the colors and fonts from the GUIHelper ensures that
        // they are disposed properly (required by SWT)
        // Setup selection styling
        DefaultSelectionStyleConfiguration selectionStyle = new DefaultSelectionStyleConfiguration();
        selectionStyle.selectionFont = GUIHelper.getFont(new FontData("Verdana", 8, SWT.NORMAL));
        selectionStyle.selectionBgColor = GUIHelper.getColor(217, 232, 251);
        selectionStyle.selectionFgColor = GUIHelper.COLOR_BLACK;
        selectionStyle.anchorBorderStyle = new BorderStyle(1, GUIHelper.COLOR_DARK_GRAY, LineStyleEnum.SOLID);
        selectionStyle.anchorBgColor = GUIHelper.getColor(217, 232, 251);
        selectionStyle.selectedHeaderBgColor = GUIHelper.getColor(169, 212, 235);

        // Add all style configurations to NatTable
        natTable.addConfiguration(selectionStyle);
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
        treeFilteredIssues.setMatcher(new VATMatcher(filter, treeObjectType));
        //   contentProvider.setTreeObject(treeObject);

        // Set category to the addNew action. So a new data set is created
        // with the selected category
        //   if (addNewAction != null) {
        //       addNewAction.setCategory(filter);
        //   }

        //Refresh
        //   this.refresh();

    }
	
	/**
     * @return the headerLabelEnabled
     */
    protected boolean isHeaderLabelEnabled() {
        return headerLabelEnabled;
    }

    @Override
    protected String getTableId() {
        return ID;
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
		}
	}

    public void removeSelectedEntry() {
        VAT objToDelete = gridLayer.getBodyDataProvider().getRowObject(lastSelectedRow);
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
    }

}

