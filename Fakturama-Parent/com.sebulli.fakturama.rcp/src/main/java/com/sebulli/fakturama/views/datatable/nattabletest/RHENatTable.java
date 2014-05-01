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
package com.sebulli.fakturama.views.datatable.nattabletest;


import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.convert.PercentageDisplayConverter;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.painter.cell.ICellPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.ImagePainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.TextPainter;
import org.eclipse.nebula.widgets.nattable.painter.cell.decorator.CellPainterDecorator;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.DefaultSelectionStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionConfiguration;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle;
import org.eclipse.nebula.widgets.nattable.style.BorderStyle.LineStyleEnum;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.ui.util.CellEdgeEnum;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

import com.sebulli.fakturama.dao.VatCategoriesDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.VATCategory;
import com.sebulli.fakturama.views.datatable.impl.NoHeaderRowOnlySelectionBindings;

public class RHENatTable {
    
    @Inject
    @Translation
    protected Messages msg;

	// ID of this view
	public static final String ID = "com.sebulli.fakturama.views.datasettable.viewVatTable";
	
//	@Inject
//	private MPart part;
	
	@Inject
	private EHandlerService handlerService;
	
	@Inject
	private ECommandService commandService;
	
	@Inject
	@Preference
	private IEclipsePreferences preferences;
	
//	//The top composite
//	private Composite top;
	
	private String editor = "com.sebulli.fakturama.DocumentEditor";  	
	
	private EventList<VAT> eventList;
	
	@Inject
	private VatsDAO vatsDAO;
	
	@Inject
	private VatCategoriesDAO vatCategoriesDAO;

//	private static final String CUSTOM_COMPARATOR_LABEL = "customComparatorLabel";
	protected static final String ROOT_NODE_NAME = "all";
	
	// VAT view specific constants
	private static final int DEFAULT_COLUMN_POSITION = 0;
	private static final int NAME_COLUMN_POSITION = 1;
	private static final int DESCRIPTION_COLUMN_POSITION = 2;
	private static final int VALUE_COLUMN_POSITION = 3;
	
	private static final String STANDARD_PROPERTYNAME = "default";
	private static final String NAME_PROPERTYNAME = "name";
	private static final String DESCRIPTION_PROPERTYNAME = "description";
	private static final String VALUE_PROPERTYNAME = "taxValue";

	private static final String[] VAT_PROPERTY_NAMES = {
		STANDARD_PROPERTYNAME, 
		NAME_PROPERTYNAME, 
		DESCRIPTION_PROPERTYNAME, 
		VALUE_PROPERTYNAME};

	
	/**
	 * show now category label as header for the list view
	 */
	protected static final String NO_CATEGORY_LABEL = "$shownothing";
	protected static final String NO_SORT_LABEL = "noSortLabel";
	private static final String CUSTOM_CELL_LABEL = "Cell_LABEL";
	private static final String STATUS_CELL_LABEL = "Status_Cell_LABEL";
	private static final String DEFAULT_CELL_LABEL = "Standard_Cell_LABEL";
	private static final String TAXVALUE_CELL_LABEL = "TaxValue_Cell_LABEL";
	
	/**
	 * controls if the header label for the list view should be shown
	 */
	protected final boolean headerLabelEnabled = false;

	private Text searchText;
	private Label filterLabel;
	private NatTable natTable;
	

	protected FilterList<VAT> treeFilteredIssues;
	
	@PostConstruct
	public Control createPartControl(Composite parent) {
		// Create the top composite
		Composite top = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().margins(0, 0).numColumns(2).applyTo(top);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).applyTo(top);

		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, contextHelpId);
		
		// Create the tree viewer
		final TopicTreeViewer<VATCategory> topicTreeViewer = createCategoryTreeViewer(top);	
		
		// Create the composite that contains the search field and the table
		Composite searchAndTableComposite = createSearchAndTableComposite(top);

		//mapping from property to label, needed for column header labels
		final Map<String, String> propertyToLabelMap = vatsDAO.getVisiblePropertyToLabelMap();

		eventList = GlazedLists.eventList(vatsDAO.findAll());
		
		//create a new ConfigRegistry which will be needed for GlazedLists handling
		ConfigRegistry configRegistry = new ConfigRegistry();
		String[] propertyNames = vatsDAO.getVisibleProperties();

		final IColumnPropertyAccessor<VAT> columnPropertyAccessor = 
				new ExtendedReflectiveColumnPropertyAccessor<VAT>(propertyNames);
		
		// Add derived 'fullName' column
		final IColumnPropertyAccessor<VAT> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<VAT>() {

			public Object getDataValue(VAT rowObject, int columnIndex) {
				switch (columnIndex) {
				case DEFAULT_COLUMN_POSITION:
					// VAT_PROPERTY_NAMES
//					System.out.println(preferences.getBoolean(STANDARD_PROPERTYNAME, false));
					return rowObject.getId() == 1L;
				case NAME_COLUMN_POSITION:
				case DESCRIPTION_COLUMN_POSITION:
				case VALUE_COLUMN_POSITION:
					// alternative: return rowObject.getFirstName();
					return columnPropertyAccessor.getDataValue(rowObject, columnIndex -1 );
				default:
					break;
				}
				return null;
			}

			public void setDataValue(VAT rowObject, int columnIndex, Object newValue) {
				throw new UnsupportedOperationException();
//				columnPropertyAccessor.setDataValue(rowObject, columnIndex+1, newValue);
			}

			public int getColumnCount() {
				return VAT_PROPERTY_NAMES.length;
			}

			public String getColumnProperty(int columnIndex) {
				switch (columnIndex) {
				case DEFAULT_COLUMN_POSITION:
					return msg.commonLabelDefault;
				case NAME_COLUMN_POSITION:
				case DESCRIPTION_COLUMN_POSITION:
				case VALUE_COLUMN_POSITION:
					return propertyToLabelMap.get(columnPropertyAccessor.getColumnProperty(columnIndex-1));
				default:
					break;
				}
				return null;
			}

			public int getColumnIndex(String propertyName) {
				if (STANDARD_PROPERTYNAME.equals(propertyName)) {
					return DEFAULT_COLUMN_POSITION;
				}
				else {
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
		final MatcherEditor<VAT> textMatcherEditor = new TextWidgetMatcherEditor<VAT>(
				searchText, new VATFilterator());
		// Filtered list for Search text field filter
		final FilterList<VAT> textFilteredIssues = new FilterList<VAT>(eventList, textMatcherEditor);
		
		// build the list for the tree-filtered values (i.e., the value list which is affected by
		// tree selection)
		treeFilteredIssues = new FilterList<VAT>(textFilteredIssues);

		//build the grid layer
		ListViewGridLayer<VAT> gridLayer = new ListViewGridLayer<VAT>(treeFilteredIssues, 
				derivedColumnPropertyAccessor, columnHeaderDataProvider, configRegistry, true);
		
		// get the underlying data layer
		final DataLayer tableDataLayer = gridLayer.getBodyDataLayer();
		
		// add a label accumulator to be able to register converter
		// this is crucial for using custom values display
		tableDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
		
		//example for mixed percentage sizing in a grid
		//configure not every column with the exact percentage value, this way the columns for which
		//no exact values are set will use the remaining space
		tableDataLayer.setColumnWidthByPosition(DEFAULT_COLUMN_POSITION, 5);
		tableDataLayer.setColumnWidthByPosition(NAME_COLUMN_POSITION, 20);
		tableDataLayer.setColumnWidthByPosition(VALUE_COLUMN_POSITION, 10);
		tableDataLayer.setColumnPercentageSizing(true);
		
		// Custom selection configuration
		final SelectionLayer selectionLayer = gridLayer.getBodyLayerStack().getSelectionLayer();
		// for further use, if we need it...
//		ILayer columnHeaderLayer = gridLayer.getColumnHeaderLayer();
//		ILayer rowHeaderLayer = gridLayer.getRowHeaderLayer();
		
		// Select complete rows
		selectionLayer.addConfiguration(new RowOnlySelectionConfiguration<VAT>());
		RowSelectionModel<VAT> selectionModel = new RowSelectionModel<VAT>(selectionLayer, gridLayer.getBodyDataProvider(),
				 new IRowIdAccessor<VAT>() {

			public Serializable getRowId(VAT rowObject) {
				return rowObject.getId();
			}
			
		}, false);
		selectionLayer.setSelectionModel(selectionModel);

		// now is the time where we can create the NatTable itself
		natTable = new NatTable(searchAndTableComposite, gridLayer, false);

//		VAT defaultVat = getDefaultVAT();
//		// Label accumulator - adds labels to all cells with the given data value
//		CellOverrideLabelAccumulator<VAT> cellLabelAccumulator =
//			new CellOverrideLabelAccumulator<VAT>(gridLayer.getBodyDataProvider());
//		cellLabelAccumulator.registerOverride(defaultVat, STANDARD_COLUMN_POSITION, DEFAULT_CELL_LABEL);

		// Create a label accumulator - adds custom labels to all cells which we
		// wish to render differently. In this case render as an image.
		ColumnOverrideLabelAccumulator columnLabelAccumulator = new ColumnOverrideLabelAccumulator(gridLayer.getBodyLayerStack());
		columnLabelAccumulator.registerColumnOverrides(DEFAULT_COLUMN_POSITION, DEFAULT_CELL_LABEL);
		columnLabelAccumulator.registerColumnOverrides(VALUE_COLUMN_POSITION, TAXVALUE_CELL_LABEL);

		// Register label accumulator
		gridLayer.getBodyLayerStack().setConfigLabelAccumulator(columnLabelAccumulator);
		
		// Register your custom cell painter, cell style, against the label applied to the cell.
//		addImageTextToColumn(configRegistry, natTable, gridLayer.getBodyDataProvider());

		// Listen to double clicks
		hookDoubleClickCommand(natTable, gridLayer);
		
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
		topicTreeViewer.setTable(this);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
		return top;
	}

	/**
	 * reads the default VAT value from preference store and returns the appropriate VAT value from dtaabase
	 *  
	 * @return
	 */
private VAT getDefaultVAT() {
	// FIXME FOR TEST PURPOSES  HERE'S THE VAT WITH ID 1 RETURNED!
		return vatsDAO.findById(1L);
	}

	private void addImageTextToColumn(IConfigRegistry configRegistry, Composite parent, IDataProvider iDataProvider) {
		ICellPainter imageTextPainter = new CellPainterDecorator(new TextPainter(), CellEdgeEnum.RIGHT, new MyImagePainter(iDataProvider));

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_PAINTER,
				imageTextPainter,
				DisplayMode.NORMAL,
				CUSTOM_CELL_LABEL);

		// Set the color of the cell. This is picked up by the button painter to style the button
		Style style = new Style();
		style.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR, GUIHelper.COLOR_WHITE);

		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,	style, DisplayMode.NORMAL, CUSTOM_CELL_LABEL);
		configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE,	style, DisplayMode.SELECT, CUSTOM_CELL_LABEL);
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
	 * Component for the Search field and the item table
	 * 
	 * @param parent the parent {@link Composite} of this Component
	 * @return {@link Composite}
	 */
	private Composite createSearchAndTableComposite(Composite parent) {
		Composite searchAndTableComposite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().margins(0, 0).numColumns(1).applyTo(searchAndTableComposite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(searchAndTableComposite);

		// Create the composite that contains the search field and the toolbar
		Composite searchAndToolbarComposite = new Composite(searchAndTableComposite, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(searchAndToolbarComposite);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(searchAndToolbarComposite);

		// The toolbar 
		ToolBar toolBar = new ToolBar(searchAndToolbarComposite, SWT.FLAT);
		GridDataFactory.fillDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(toolBar);
//		ToolBarManager tbm = new ToolBarManager(toolBar);

		filterLabel = new Label(searchAndToolbarComposite, SWT.NONE);
		FontData[] fD = filterLabel.getFont().getFontData();
		fD[0].setHeight(20);
		Font font = new Font(null, fD[0]);
		filterLabel.setFont(font);
		font.dispose();
		GridDataFactory.fillDefaults().grab(true, false).align(SWT.CENTER, SWT.CENTER).applyTo(filterLabel);

		// The search composite
		Composite searchComposite = new Composite(searchAndToolbarComposite, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).applyTo(searchComposite);
		GridDataFactory.fillDefaults().grab(true, true).align(SWT.END, SWT.CENTER).applyTo(searchComposite);

		// Search label an search field
		Label searchLabel = new Label(searchComposite, SWT.NONE);
		searchLabel.setText(msg.commonLabelSearchfield);
		GridDataFactory.swtDefaults().applyTo(searchLabel);
		searchText = new Text(searchComposite, SWT.BORDER | SWT.SEARCH | SWT.CANCEL | SWT.ICON_SEARCH);
		GridDataFactory.swtDefaults().align(SWT.END, SWT.CENTER).hint(150, -1).applyTo(searchText);
		return searchAndTableComposite;
	}

	/**
	 * We have to style the table a little bit...
	 * 
	 * @param natTable the {@link NatTable} to style
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
	
	private TopicTreeViewer<VATCategory> createCategoryTreeViewer(Composite top) {
		TopicTreeViewer<VATCategory> topicTreeViewer = new TopicTreeViewer<VATCategory>(top, msg, VATCategory.class, false, true);
		topicTreeViewer.setInput(GlazedLists.eventList(vatCategoriesDAO.findAll()));
		// TODO boolean useDocumentAndContactFilter, boolean useAll könnte man eigentlich zusammenfassen.
		// Eins von beiden muß es doch geben, oder?
		topicTreeViewer.setLabelProvider(new RHEViewTableLabelProvider());
		return topicTreeViewer;
	}
	
    /**
     * Set the category filter with a given {@link TreeObjectType}.
     * 
     * @param filter
     *            The new filter string
     * @param treeObjectType the {@link TreeObjectType}
     */
    public void setCategoryFilter(String filter, TreeObjectType treeObjectType) {
		// Set the label with the filter string
		if (filter.equals("$shownothing"))
			filterLabel.setText("");
		else
		// Display the localizes list names.
		// or the document type
		if (headerLabelEnabled)
			filterLabel.setText("DataSetListNames.NAMES.getLocalizedName: " + filter);

		filterLabel.pack(true);

		// Reset transaction and contact filter, set category filter
		treeFilteredIssues.setMatcher(new VATMatcher(filter, treeObjectType));
//		contentProvider.setTreeObject(treeObject);

		// Set category to the addNew action. So a new data set is created
		// with the selected category
//		if (addNewAction != null) {
//			addNewAction.setCategory(filter);
//		}

//		//Refresh
//		this.refresh();
		

    }
	
	/**
	 * Set the category filter
	 * 
	 * @param filter
	 *            The new filter string
	 *            
	 * @deprecated use {@link #setCategoryFilter(String, TreeObjectType)} instead
	 */
	public void setCategoryFilter(String filter) {
	    setCategoryFilter(filter, TreeObjectType.DEFAULT_NODE);
	}

	/**
	 * Set the transaction filter
	 * 
	 * @param filter
	 *            The new filter string
	 */
	public void setTransactionFilter(int filter) {

//		// Set the label with the filter string
//		filterLabel.setText("Dieser Vorgang");
//		filterLabel.pack(true);
//
//		// Reset category and contact filter, set transaction filter
//		treeFilteredIssues.setMatcher(new VATTransactionMatcher(filter));
//		contentProvider.setTransactionFilter(filter);
//		contentProvider.setContactFilter(-1);
//		contentProvider.setCategoryFilter("");
//
//		// Reset the addNew action. 
//		if (addNewAction != null) {
//			addNewAction.setCategory("");
//		}
//		this.refresh();
	}

	/**
	 * Set the contact filter
	 * 
	 * @param filter
	 *            The new filter string
	 */
	public void setContactFilter(int filter) {

//		// Set the label with the filter string
//		filterLabel.setText(Data.INSTANCE.getContacts().getDatasetById(filter).getName(false));
//		filterLabel.pack(true);
//
//		// Reset transaction and category filter, set contact filter
//		contentProvider.setContactFilter(filter);
//		contentProvider.setTransactionFilter(-1);
//		contentProvider.setCategoryFilter("");
//
//		// Reset the addNew action. 
//		if (addNewAction != null) {
//			addNewAction.setCategory("");
//		}
//
//		this.refresh();
	}
	
	
	
	
	/**
	 * This class provides the labels for the category tree
	 * @author rheydenr
	 *
	 */	
	class RHEViewTableLabelProvider extends CellLabelProvider {
		public String getText(Object obj) {

			// Display the localizes list names.
			if (natTable instanceof NatTable)
				return "(localized) " + obj.toString();
			else
				return obj.toString();
		}

		@Override
		public void update(ViewerCell cell) {
			cell.setText(getText(cell.getElement()));
//			cell.setImage(getImage(cell.getElement()));
		}
		
	}
	
	/**
	 * On double click: open the corresponding editor
	 * @param nattable 
	 * @param gridLayer 
	 */
	private void hookDoubleClickCommand(final NatTable nattable, final ListViewGridLayer<VAT> gridLayer) {
		// Add a double click listener
		nattable.getUiBindingRegistry().registerDoubleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE),
		new IMouseAction() {

			@SuppressWarnings("restriction")
			@Override
			public void run(NatTable natTable, MouseEvent event) {
				//get the row position for the click in the NatTable
				int rowPos = natTable.getRowPositionByY(event.y);
				//transform the NatTable row position to the row position of the body layer stack
				int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPos, gridLayer.getBodyDataLayer());
				// extract the selected Object
				VAT vat  = gridLayer.getBodyDataProvider().getRowObject(bodyRowPos);
				System.out.println("Selected VAT: " + vat.getName());
				// Call the corresponding editor. The editor is set
				// in the variable "editor", which is used as a parameter
				// when calling the editor command.
				// in E4 we create a new Part (or use an existing one with the same ID)
				// from PartDescriptor
				Command callEditor = commandService.getCommand("com.sebulli.fakturama.command.callEditor");
				Map<String, String> params = new HashMap<>();
				params.put("com.sebulli.fakturama.rcp.cmdparam.objId", Long.toString(vat.getId()));
				params.put("com.sebulli.fakturama.editors.editortype", ID);
				ParameterizedCommand parameterizedCommand = ParameterizedCommand.generateCommand(callEditor, params);
				handlerService.executeHandler(parameterizedCommand);
			}
		});
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

}

