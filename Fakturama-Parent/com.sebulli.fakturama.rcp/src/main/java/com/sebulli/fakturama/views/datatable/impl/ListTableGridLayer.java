/**
 * 
 */
package com.sebulli.fakturama.views.datatable.impl;

import java.util.List;
import java.util.Map;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.swt.widgets.Text;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.Filterator;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.TextMatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

/**
 * Provides a table list with all values from the selected entity table.
 * 
 * @author Ralf Heydenreich
 *
 */
public class ListTableGridLayer<T> extends GridLayer {

	private final DataLayer bodyDataLayer;
	private final DataLayer columnHeaderDataLayer;
	private ListTableBodyLayerStack bodyLayer;
	private ListDataProvider<T> bodyDataProvider;
	
	/**
	 * Constructor
	 * 
	 * @param propertyNames which properties should be displayed
	 * @param propertyToLabelMap mapping from property to display name
	 * @param dataList the data list
	 */
	public ListTableGridLayer(
			String[] propertyNames, 
			Map<String, String> propertyToLabelMap, 
			List<T> dataList) {
		this(dataList, new ReflectiveColumnPropertyAccessor<T>(propertyNames), null, null, null);
	}
	
	/**
	 * Constructor
	 * 
	 * @param propertyNames which properties should be displayed
	 * @param propertyToLabelMap mapping from property to display name
	 * @param dataList the data list
	 * @param columnPropertyAccessor how should a single cell value (or entity field) be displayed
	 */
	public ListTableGridLayer(
			String[] propertyNames, 
			Map<String, String> propertyToLabelMap, 
			List<T> dataList, 
			IColumnPropertyAccessor<T> columnPropertyAccessor) {
		this(dataList, columnPropertyAccessor,
				new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap), null, null);
	}
	
	public ListTableGridLayer(
			String[] propertyNames, 
			Map<String, String> propertyToLabelMap, 
			List<T> dataList, 
			IColumnPropertyAccessor<T> columnPropertyAccessor, Text searchTextField, 
			TextFilterator<T> textFilterator) {
		this(dataList, columnPropertyAccessor,
				new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap), searchTextField, textFilterator);
		
	}
	
	/**
	 * Constructor
	 * 
	 * @param propertyNames which properties should be displayed
	 * @param propertyToLabelMap mapping from property to display name
	 * @param dataList the data list
	 * @param columnPropertyAccessor how should a single cell value (or entity field) be displayed
	 * @param columnHeaderDataProvider labels for the column headers
	 * @param searchTextField if data should be filtered you have to provide an appropriate text field...
	 * @param textFilterator ...and a {@link Filterator} which holds all the field which have to be searched
	 */
	public ListTableGridLayer(List<T> dataList, 
			IColumnPropertyAccessor<T> columnPropertyAccessor, IDataProvider columnHeaderDataProvider, Text searchTextField, 
			TextFilterator<T> textFilterator) {
		super(true);
		EventList<T> eventList = GlazedLists.eventList(dataList);
		if(searchTextField != null && textFilterator != null) {
	        TextWidgetMatcherEditor<T> matcherEditor = new TextWidgetMatcherEditor<T>(searchTextField, textFilterator, true);
	        matcherEditor.setMode(TextMatcherEditor.CONTAINS);
			FilterList<T> textFilteredIssues = new FilterList<T>(eventList, 
	        		matcherEditor);
			bodyDataProvider = new ListDataProvider<T>(textFilteredIssues, columnPropertyAccessor);
		} else {
			bodyDataProvider = new ListDataProvider<T>(eventList, columnPropertyAccessor);
		}

		bodyDataLayer = new DataLayer(bodyDataProvider);
		bodyLayer = new ListTableBodyLayerStack(bodyDataLayer);

		// Column header
		columnHeaderDataLayer = new DefaultColumnHeaderDataLayer(columnHeaderDataProvider);
		ColumnHeaderLayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, bodyLayer, bodyLayer.getSelectionLayer());

		// Row header
		DefaultRowHeaderDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyDataProvider);
		DefaultRowHeaderDataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
		RowHeaderLayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayer, bodyLayer.getSelectionLayer());

		// Corner
		DefaultCornerDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		CornerLayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);

		// Grid
		setBodyLayer(bodyLayer);
		setColumnHeaderLayer(columnHeaderLayer);
		setRowHeaderLayer(rowHeaderLayer);
		setCornerLayer(cornerLayer);
	}

	@Override
	public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
		super.setClientAreaProvider(clientAreaProvider);
	}

	public SelectionLayer getSelectionLayer() {
		return bodyLayer.getSelectionLayer();
	}
	
	public DataLayer getBodyDataLayer() {
		return bodyDataLayer;
	}

	public ListDataProvider<T> getBodyDataProvider() {
		return bodyDataProvider;
	}
	
	public DataLayer getColumnHeaderDataLayer() {
		return columnHeaderDataLayer;
	}

}
