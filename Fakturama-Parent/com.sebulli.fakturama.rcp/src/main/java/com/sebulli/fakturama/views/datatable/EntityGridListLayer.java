/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2015 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.views.datatable;

import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultRowHeaderDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.DefaultRowHeaderDataLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.config.DefaultGridLayerConfiguration;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

import ca.odell.glazedlists.EventList;

import com.sebulli.fakturama.model.IEntity;

/**
 *
 */
public class EntityGridListLayer<T extends IEntity> {

//    private ColumnOverrideLabelAccumulator columnLabelAccumulator;
    private BodyLayerStack<T> bodyLayerStack;
    private GlazedListsColumnHeaderLayerStack<T> columnHeaderLayer;
    private ViewportLayer viewportLayer;
//    private SelectionLayer selectionLayer;
    private GridLayer gridLayer;
    

    /**
     * 
     */
    public EntityGridListLayer(EventList<T> eventList, String[] propertyNames, IColumnPropertyAccessor<T> columnPropertyAccessor,
            IConfigRegistry configRegistry) {
        
        // 1. create BodyLayerStack
         bodyLayerStack = new BodyLayerStack<T>(eventList, columnPropertyAccessor);


         //2. build the column header layer
         IDataProvider columnHeaderDataProvider = new ListViewHeaderDataProvider<T>(propertyNames, columnPropertyAccessor); 
         columnHeaderLayer = new GlazedListsColumnHeaderLayerStack<T>(columnHeaderDataProvider, 
                                                         columnPropertyAccessor, 
                                                         configRegistry, 
                                                         bodyLayerStack);

         // 3. build the row header layer
         IDataProvider rowHeaderDataProvider = new DefaultRowHeaderDataProvider(bodyLayerStack.getBodyDataProvider());
         DataLayer rowHeaderDataLayer = new DefaultRowHeaderDataLayer(rowHeaderDataProvider);
         ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, bodyLayerStack, bodyLayerStack.getSelectionLayer());
         
         // 4. build the corner layer
         IDataProvider cornerDataProvider = new DefaultCornerDataProvider(columnHeaderDataProvider, rowHeaderDataProvider);
         DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
         ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);
         
         // 5. build the grid layer
         gridLayer = new GridLayer(bodyLayerStack, columnHeaderLayer, rowHeaderLayer, cornerLayer);
         gridLayer.addConfiguration(new DefaultGridLayerConfiguration(gridLayer));
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


    public GlazedListsDataProvider<T> getBodyDataProvider() {
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

}
