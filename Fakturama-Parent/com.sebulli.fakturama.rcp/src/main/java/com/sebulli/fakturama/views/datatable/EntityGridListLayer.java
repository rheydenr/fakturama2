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

package com.sebulli.fakturama.views.datatable;

import java.io.Serializable;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.IEntity;

import ca.odell.glazedlists.EventList;

/**
 *
 */
public class EntityGridListLayer<T extends IEntity> {
    private BodyLayerStack<T> bodyLayerStack;
    private GridLayer gridLayer;
    
    public EntityGridListLayer(EventList<T> eventList, String[] propertyNames, IColumnPropertyAccessor<T> columnPropertyAccessor, 
            IRowIdAccessor<T> rowIdAccessor, IConfigRegistry configRegistry, Messages msg, boolean withRowHeader) {

        // 1. create BodyLayerStack
        bodyLayerStack = new BodyLayerStack<T>(eventList, columnPropertyAccessor, rowIdAccessor);        

        //2. build the column header layer
        IDataProvider columnHeaderDataProvider = new ListViewColumnHeaderDataProvider<T>(propertyNames, columnPropertyAccessor);
        GlazedListsColumnHeaderLayerStack<T> columnHeaderLayer = new GlazedListsColumnHeaderLayerStack<>(columnHeaderDataProvider, columnPropertyAccessor, configRegistry, bodyLayerStack);
        
        // 3. build the row header layer
        IDataProvider rowHeaderDataProvider = new ListViewRowHeaderDataProvider(bodyLayerStack.getBodyDataProvider(), withRowHeader);
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
        gridLayer = new GridLayer(bodyLayerStack, columnHeaderLayer, rowHeaderLayer, cornerLayer);
    }
    
    public EntityGridListLayer(EventList<T> eventList, String[] propertyNames, IColumnPropertyAccessor<T> columnPropertyAccessor, 
            IRowIdAccessor<T> rowIdAccessor, IConfigRegistry configRegistry, boolean withRowHeader) {
    	this(eventList, propertyNames, columnPropertyAccessor, rowIdAccessor, configRegistry, null, withRowHeader);
    }
    
    public EntityGridListLayer(EventList<T> eventList, String[] propertyNames, IColumnPropertyAccessor<T> columnPropertyAccessor, 
            IRowIdAccessor<T> rowIdAccessor, IConfigRegistry configRegistry) {
        this(eventList, propertyNames, columnPropertyAccessor, rowIdAccessor, configRegistry, false);
    }

    /**
     * 
     */
    public EntityGridListLayer(EventList<T> eventList, String[] propertyNames, IColumnPropertyAccessor<T> columnPropertyAccessor, IConfigRegistry configRegistry) {
        this(eventList, propertyNames, columnPropertyAccessor, new IRowIdAccessor<T>() {
            @Override
            public Serializable getRowId(T rowObject) {
                // default implementation uses entity id as row id
                return rowObject.getId();
            }
        }, configRegistry);
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
    public BodyLayerStack<T> getBodyLayerStack() {
        return bodyLayerStack;
    }

	/**
	 * @return the viewportLayer
	 */
	public ViewportLayer getViewportLayer() {
		return bodyLayerStack.getViewportLayer();
	}
}
