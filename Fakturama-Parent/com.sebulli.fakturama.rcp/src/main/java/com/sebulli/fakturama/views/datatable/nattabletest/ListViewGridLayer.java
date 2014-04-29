package com.sebulli.fakturama.views.datatable.nattabletest;

import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.IUniqueIndexLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnOverrideLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.sort.command.SortColumnCommand;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;

/**
 * Factory for assembling GridLayer and the child layers - with support for
 *    GlazedLists and sorting
 *    
 * @see {@linkplain http://www.glazedlists.com/}
 */
public class ListViewGridLayer<T> extends GridLayer {

	private ColumnOverrideLabelAccumulator columnLabelAccumulator;
	private DataLayer bodyDataLayer;
	private DefaultBodyLayerStack bodyLayerStack;
	private ListDataProvider<T> bodyDataProvider;
	private GlazedListsColumnHeaderLayerStack<T> columnHeaderLayerStack;

	public ListViewGridLayer(EventList<T> eventList,
									String[] propertyNames,
									Map<String, String> propertyToLabelMap,
									IConfigRegistry configRegistry) {
		this(eventList, propertyNames, propertyToLabelMap, configRegistry, true);
	}

	/**
	 * The underlying {@link DataLayer} created is able to handle Events raised by GlazedLists
	 * and fire corresponding NatTable events.
	 *
	 * The {@link SortHeaderLayer} triggers sorting on the the underlying SortedList when
	 * a {@link SortColumnCommand} is received.
	 */
	public ListViewGridLayer(EventList<T> eventList,
									String[] propertyNames,
									Map<String, String> propertyToLabelMap,
									IConfigRegistry configRegistry,
									boolean useDefaultConfiguration) {
		
		this(eventList,
				new ReflectiveColumnPropertyAccessor<T>(propertyNames),
				new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap),
				configRegistry,
				useDefaultConfiguration);
	}
	
	public ListViewGridLayer(EventList<T> eventList,
			IColumnPropertyAccessor<T> columnPropertyAccessor,
			IDataProvider columnHeaderDataProvider,
			IConfigRegistry configRegistry,
			boolean useDefaultConfiguration) {
		
		super(useDefaultConfiguration);
		
		// Body - with list event listener
		//	NOTE: Remember to use the SortedList constructor with 'null' for the Comparator
		SortedList<T> sortedList = new SortedList<T>(eventList, null);
		bodyDataProvider = new ListDataProvider<T>(sortedList, columnPropertyAccessor);

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
	 * @author rheydenr
	 *
	 */
	class ListViewRowHeaderDataProvider implements IDataProvider  {

		protected final IDataProvider bodyDataProvider;

		public ListViewRowHeaderDataProvider(IDataProvider bodyDataProvider) {
			this.bodyDataProvider = bodyDataProvider;
		}
		@Override
		public Object getDataValue(int columnIndex, int rowIndex) {
			return null;
		}

		@Override
		public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
			throw new UnsupportedOperationException();
		}

		@Override
		public int getColumnCount() {
			return 0;
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
}
