package com.sebulli.fakturama.views.datatable;

import java.util.Map;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsSortModel;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultColumnHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.stack.DefaultBodyLayerStack;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.sort.SortHeaderLayer;
import org.eclipse.nebula.widgets.nattable.util.IClientAreaProvider;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

import com.sebulli.fakturama.model.IEntity;

import ca.odell.glazedlists.SortedList;

/**
 * Column header layer stack, with a {@link SortHeaderLayer}.
 * 	Utilizes {@link GlazedListsSortModel} for sorting
 */
public class GlazedListsColumnHeaderLayerStack<T extends IEntity> extends AbstractLayerTransform {
	private IDataProvider dataProvider;
	private DataLayer dataLayer;
	private ILayer columnHeaderLayer;

	public GlazedListsColumnHeaderLayerStack(String[] propertyNames, 
												Map<String, String> propertyToLabelMap, 
												SortedList<T> sortedList,
												IColumnPropertyAccessor<T> columnPropertyAccessor, 
												IConfigRegistry configRegistry,
												DefaultBodyLayerStack bodyLayerStack) {

		this(new DefaultColumnHeaderDataProvider(propertyNames, propertyToLabelMap),
				sortedList,
				columnPropertyAccessor, 
				configRegistry,
				bodyLayerStack);
	}
	public GlazedListsColumnHeaderLayerStack(IDataProvider dataProvider,
			IColumnPropertyAccessor<T> columnPropertyAccessor, 
			IConfigRegistry configRegistry,
			BodyLayerStack<T> bodyLayerStack) {
		
		this.dataProvider = dataProvider;
		dataLayer = new DefaultColumnHeaderDataLayer(dataProvider);
		columnHeaderLayer = new ColumnHeaderLayer(dataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());

		SortHeaderLayer<T> sortHeaderLayer = new SortHeaderLayer<T>(
												columnHeaderLayer, 
												new GlazedListsSortModel<T>(
												        bodyLayerStack.getSortedList(), 
														columnPropertyAccessor,
														configRegistry, 
														dataLayer), 
												false);

		setUnderlyingLayer(sortHeaderLayer);
	    
	}
	
	@Deprecated
	public GlazedListsColumnHeaderLayerStack(IDataProvider dataProvider, 
			SortedList<T> sortedList,
			IColumnPropertyAccessor<T> columnPropertyAccessor, 
			IConfigRegistry configRegistry,
			DefaultBodyLayerStack bodyLayerStack) {
		
		this.dataProvider = dataProvider;
		dataLayer = new DefaultColumnHeaderDataLayer(dataProvider);
		columnHeaderLayer = new ColumnHeaderLayer(dataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());

		SortHeaderLayer<T> sortHeaderLayer = new SortHeaderLayer<T>(
												columnHeaderLayer, 
												new GlazedListsSortModel<T>(
														sortedList, 
														columnPropertyAccessor,
														configRegistry, 
														dataLayer), 
												false);

		setUnderlyingLayer(sortHeaderLayer);
	}
	   
    public GlazedListsColumnHeaderLayerStack(IDataProvider dataProvider, 
            SortedList<T> sortedList,
            IColumnPropertyAccessor<T> columnPropertyAccessor, 
            IConfigRegistry configRegistry,
            ViewportLayer viewportLayer, SelectionLayer selectionLayer) {
        
        this.dataProvider = dataProvider;
        this.dataLayer = new DefaultColumnHeaderDataLayer(dataProvider);
        this.columnHeaderLayer = new ColumnHeaderLayer(dataLayer, viewportLayer, selectionLayer);

        SortHeaderLayer<T> sortHeaderLayer = new SortHeaderLayer<T>(
                                                columnHeaderLayer, 
                                                new GlazedListsSortModel<T>(
                                                        sortedList, 
                                                        columnPropertyAccessor,
                                                        configRegistry, 
                                                        dataLayer), 
                                                false);

        setUnderlyingLayer(sortHeaderLayer);
    }

	@Override
	public void setClientAreaProvider(IClientAreaProvider clientAreaProvider) {
		super.setClientAreaProvider(clientAreaProvider);
	}

	public DataLayer getDataLayer() {
		return dataLayer;
	}

	public IDataProvider getDataProvider() {
		return dataProvider;
	}

	public ILayer getColumnHeaderLayer() {
		return columnHeaderLayer;
	}
}
