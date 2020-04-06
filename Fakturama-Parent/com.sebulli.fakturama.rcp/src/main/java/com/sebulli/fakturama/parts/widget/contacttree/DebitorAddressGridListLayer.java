/*
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2015 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.parts.widget.contacttree;

import java.io.Serializable;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.grid.cell.AlternatingRowConfigLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.config.DefaultGridLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.grid.layer.config.DefaultRowStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.CompositeLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

import com.sebulli.fakturama.dao.DebitorAddress;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.views.datatable.ListViewColumnHeaderDataProvider;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.TreeList;

/**
 *
 */
public class DebitorAddressGridListLayer<T extends DebitorAddress> {

    private TempBodyLayerStack<T> bodyLayerStack;
    private ContactTreeListColumnHeaderLayerStack<T> columnHeaderLayer;
    private GridLayer gridLayer;
	private ViewportLayer viewportLayer;
    
    public DebitorAddressGridListLayer(EventList<T> eventList, String[] propertyNames, IColumnPropertyAccessor<T> columnPropertyAccessor, 
            IRowIdAccessor<T> rowIdAccessor, IConfigRegistry configRegistry, Messages msg, boolean withRowHeader,
            TreeList.Format<T> treeFormat) {

        // 1. create BodyLayerStack
        bodyLayerStack = new TempBodyLayerStack<T>(eventList, columnPropertyAccessor, rowIdAccessor, treeFormat);        

        //2. build the column header layer
        IDataProvider columnHeaderDataProvider = new ListViewColumnHeaderDataProvider<T>(propertyNames, columnPropertyAccessor);
        columnHeaderLayer = new ContactTreeListColumnHeaderLayerStack<T>(columnHeaderDataProvider, columnPropertyAccessor, configRegistry, bodyLayerStack);

        // 3. build the row header layer
        IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyLayerStack.getBodyDataProvider());
        DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
        ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());

        // 4. build the corner layer
        IDataProvider cornerDataProvider;
        
        /*
         * If we use row headers we label the corner with "pos no.".
         */
        if(withRowHeader) {
        	// for brevity we overwrite the getDataValue method here.
        	cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider) {
        		@Override
        		public Object getDataValue(int columnIndex, int rowIndex) {
        			return msg != null ? msg.editorDocumentFieldPosition : "Pos. No.";
        		}
        	};
        } else {
        	cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
        }
        DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
        ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);

        // 5. build the grid layer
        gridLayer = new GridLayer(bodyLayerStack, columnHeaderLayer, rowHeaderLayer, cornerLayer, false);
        // change the alternating row configuration so it does not change on row
        // reordering or scrolling, see Bug #521990
        gridLayer.addConfiguration(new DefaultGridLayerConfiguration(gridLayer) {

            @Override
            protected void addAlternateRowColoringConfig(CompositeLayer gridLayer) {
                addConfiguration(new DefaultRowStyleConfiguration());
                gridLayer.setConfigLabelAccumulatorForRegion(
                        GridRegion.BODY,
                        new AlternatingRowConfigLabelAccumulator(gridLayer
                                .getChildLayerByRegionName(GridRegion.BODY)));
            }

        });
        
        setViewportLayer(new ViewportLayer(bodyLayerStack.getSelectionLayer()));
        // as the selection mouse bindings are registered for the region label
        // GridRegion.BODY we need to set that region label to the viewport so
        // the selection via mouse is working correctly
        getViewportLayer().setRegionName(GridRegion.BODY);
    }
    
    public DebitorAddressGridListLayer(EventList<T> eventList, String[] propertyNames, IColumnPropertyAccessor<T> columnPropertyAccessor, 
            IRowIdAccessor<T> rowIdAccessor, IConfigRegistry configRegistry, boolean withRowHeader,
            TreeList.Format<T> treeFormat) {
    	this(eventList, propertyNames, columnPropertyAccessor, rowIdAccessor, configRegistry, null, withRowHeader, treeFormat);
    }
    
    public DebitorAddressGridListLayer(EventList<T> eventList, String[] propertyNames, IColumnPropertyAccessor<T> columnPropertyAccessor, 
            IRowIdAccessor<T> rowIdAccessor, IConfigRegistry configRegistry,
            TreeList.Format<T> treeFormat) {
        this(eventList, propertyNames, columnPropertyAccessor, rowIdAccessor, configRegistry, false, treeFormat);
    }

    /**
     * 
     */
    public DebitorAddressGridListLayer(EventList<T> eventList, String[] propertyNames, 
    		IColumnPropertyAccessor<T> columnPropertyAccessor, IConfigRegistry configRegistry, TreeList.Format<T> treeFormat) {
        this(eventList, propertyNames, columnPropertyAccessor, new IRowIdAccessor<T>() {
            @Override
            public Serializable getRowId(T rowObject) {
                // default implementation uses entity id as row id
                return rowObject.getCustomerNumber();
            }
        }, configRegistry, treeFormat);
    }

    /**
     * @return the selectionLayer
     */
    public SelectionLayer getSelectionLayer() {
        return bodyLayerStack.getSelectionLayer();
    }

    /**
     * @return the gridLayer
     */
    public GridLayer getGridLayer() {
        return gridLayer;
    }

    public ListDataProvider<T> getBodyDataProvider() {
        return bodyLayerStack.getBodyDataProvider();
    }

    public DataLayer getBodyDataLayer() {
        return bodyLayerStack.getBodyDataLayer();
    }

    /**
     * @return the bodyLayerStack
     */
    public TempBodyLayerStack<T> getBodyLayerStack() {
        return bodyLayerStack;
    }

	/**
	 * @return the viewportLayer
	 */
	public ViewportLayer getViewportLayer() {
		return viewportLayer;
	}

	/**
	 * @param viewportLayer the viewportLayer to set
	 */
	private void setViewportLayer(ViewportLayer viewportLayer) {
		this.viewportLayer = viewportLayer;
	}

}
