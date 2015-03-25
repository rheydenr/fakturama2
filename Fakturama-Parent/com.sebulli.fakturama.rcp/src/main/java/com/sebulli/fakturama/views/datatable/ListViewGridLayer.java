package com.sebulli.fakturama.views.datatable;

import java.io.Serializable;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionConfiguration;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.command.SortColumnCommand;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;

import com.sebulli.fakturama.model.IEntity;

/**
 * Factory for assembling GridLayer and the child layers - with support for
 *    GlazedLists and sorting
 *    
 * @see {@linkplain http://www.glazedlists.com/}
 */
public class ListViewGridLayer<T extends IEntity> extends GridLayer {

	private ColumnOverrideLabelAccumulator columnLabelAccumulator;
	private DataLayer bodyDataLayer;
	private DefaultBodyLayerStack bodyLayerStack;
    private GlazedListsDataProvider<T> bodyDataProvider;
    private GlazedListsColumnHeaderLayerStack<T> columnHeaderLayerStack;
    private ViewportLayer viewportLayer;
    private SelectionLayer selectionLayer;

    public ListViewGridLayer(EntityGridListLayer<T> entityGridListLayer) {
        super(false);
//        this.columnLabelAccumulator = entityGridListLayer.
        this.bodyDataLayer = entityGridListLayer.getBodyDataLayer();
//        this.bodyLayerStack = entityGridListLayer.getBodyLayerStack();
        this.bodyDataProvider = entityGridListLayer.getBodyDataProvider();
    }
    
    
    
    public ListViewGridLayer(EventList<T> eventList,
            IColumnPropertyAccessor<T> columnPropertyAccessor,
            IDataProvider columnHeaderDataProvider,
            IConfigRegistry configRegistry,
            boolean useDefaultConfiguration,
            boolean showRowHeader) {
        
        super(useDefaultConfiguration);
        
        // Body - with list event listener
        TransformedList<T, T> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);
        
        //use the SortedList constructor with 'null' for the Comparator because the Comparator
        //will be set by configuration
        SortedList<T> sortedList = new SortedList<T>(rowObjectsGlazedList, null);
        //  NOTE: Remember to use the SortedList constructor with 'null' for the Comparator
        bodyDataProvider = new GlazedListsDataProvider<T>(sortedList, columnPropertyAccessor);

        bodyDataLayer = new DataLayer(getBodyDataProvider());
        GlazedListsEventLayer<T> glazedListsEventLayer = new GlazedListsEventLayer<T>(getBodyDataLayer(), eventList);
//        bodyLayerStack = new DefaultBodyLayerStack(glazedListsEventLayer);
        RowReorderLayer rowReorderLayer = new RowReorderLayer(glazedListsEventLayer); 
        selectionLayer = new SelectionLayer(rowReorderLayer);
//       Select complete rows
      selectionLayer.addConfiguration(new RowOnlySelectionConfiguration<T>());

        IRowIdAccessor<T> rowIdAccessor = new IRowIdAccessor<T>() {
            @Override
            public Serializable getRowId(T rowObject) {
                return rowObject.getId();
            }
        };

      //use a RowSelectionModel that will perform row selections and is able to identify a row via unique ID
      RowSelectionModel<T> selectionModel = new RowSelectionModel<T>(getSelectionLayer(), getBodyDataProvider(), rowIdAccessor, false);
      selectionModel.setMultipleSelectionAllowed(true);
      selectionLayer.setSelectionModel(selectionModel);
      viewportLayer = new ViewportLayer(getSelectionLayer());
        
        // Column header
        columnHeaderLayerStack = new GlazedListsColumnHeaderLayerStack<T>(columnHeaderDataProvider, 
                                                        sortedList, 
                                                        columnPropertyAccessor, 
                                                        configRegistry, 
                                                        viewportLayer, getSelectionLayer());

        // Row header
        IDataProvider rowHeaderDataProvider = new ListViewRowHeaderDataProvider(getBodyDataProvider(), showRowHeader);
        IUniqueIndexLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, getSelectionLayer());

        // Corner
        IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
        IUniqueIndexLayer cornerDataLayer = new DataLayer(cornerDataProvider);
        CornerLayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayerStack);

        // Grid
//      GridLayer gridLayer = new GridLayer(viewportLayer, columnHeaderLayerStack, rowHeaderLayer, cornerLayer);
        setBodyLayer(viewportLayer);
        setColumnHeaderLayer(columnHeaderLayerStack);
        setRowHeaderLayer(rowHeaderLayer);
        setCornerLayer(cornerLayer);
        
    }
    
    
	/**
	 * The underlying {@link DataLayer} created is able to handle Events raised by GlazedLists
	 * and fire corresponding NatTable events.
	 *
	 * The {@link SortHeaderLayer} triggers sorting on the the underlying SortedList when
	 * a {@link SortColumnCommand} is received.
	 */
    @Deprecated
	public ListViewGridLayer(EventList<T> eventList,
			IColumnPropertyAccessor<T> columnPropertyAccessor,
			IDataProvider columnHeaderDataProvider,
			IConfigRegistry configRegistry,
			boolean useDefaultConfiguration) {
		
		super(useDefaultConfiguration);
		
		// Body - with list event listener
		//	NOTE: Remember to use the SortedList constructor with 'null' for the Comparator
		SortedList<T> sortedList = new SortedList<T>(eventList, null);
		bodyDataProvider = new GlazedListsDataProvider<T>(sortedList, columnPropertyAccessor);

		bodyDataLayer = new DataLayer(bodyDataProvider);
		GlazedListsEventLayer<T> glazedListsEventLayer = new GlazedListsEventLayer<T>(bodyDataLayer, eventList);
		bodyLayerStack = new DefaultBodyLayerStack(glazedListsEventLayer);

		// Column header
		columnHeaderLayerStack = new GlazedListsColumnHeaderLayerStack<T>(columnHeaderDataProvider, 
														sortedList, 
														columnPropertyAccessor, 
														configRegistry, 
														bodyLayerStack);

		// Row header
		IDataProvider rowHeaderDataProvider = new ListViewRowHeaderDataProvider(bodyDataProvider);
		IUniqueIndexLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());

		// Corner
		IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderLayerStack.getDataProvider(), rowHeaderDataProvider);
		IUniqueIndexLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		CornerLayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayerStack);

		// Grid
		setBodyLayer(bodyLayerStack);
		setColumnHeaderLayer(columnHeaderLayerStack);
		setRowHeaderLayer(rowHeaderLayer);
		setCornerLayer(cornerLayer);
	}
	
	/**
	 * This class is for viewing row headers.
	 *
	 */
    class ListViewRowHeaderDataProvider implements IDataProvider  {

        protected final IDataProvider bodyDataProvider;
        
        /** 
         * A row header has either one column (and is visible then) or zero column (and is invisible then).
         */
        private final int columnCount;
        
        public ListViewRowHeaderDataProvider(IDataProvider bodyDataProvider) {
            this(bodyDataProvider, false);
        }

        public ListViewRowHeaderDataProvider(IDataProvider bodyDataProvider, boolean showRowHeader) {
            this.bodyDataProvider = bodyDataProvider;
            this.columnCount = showRowHeader ? 1 : 0;
        }
        
        @Override
        public Object getDataValue(int columnIndex, int rowIndex) {
            return Integer.valueOf(rowIndex + 1);
        }

        @Override
        public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getColumnCount() {
            return columnCount;
        }

        public int getRowCount() {
            return bodyDataProvider.getRowCount();
        }
        
    }
	
	public ColumnOverrideLabelAccumulator getColumnLabelAccumulator() {
		return columnLabelAccumulator;
	}

	@Override
	public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
		super.setClientAreaProvider(clientAreaProvider);
	}

	public DataLayer getBodyDataLayer() {
		return bodyDataLayer;
	}

	public ListDataProvider<T> getBodyDataProvider() {
		return bodyDataProvider;
	}

	public GlazedListsColumnHeaderLayerStack<T> getColumnHeaderLayerStack() {
		return columnHeaderLayerStack;
	}

	public DefaultBodyLayerStack getBodyLayerStack() {
		return bodyLayerStack;
	}

    /**
     * @return the viewportLayer
     */
    public ViewportLayer getViewportLayer() {
        return viewportLayer;
    }

    /**
     * @return the selectionLayer
     */
    public SelectionLayer getSelectionLayer() {
        return selectionLayer;
    }
}
