/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.views.datatable.vats;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider;

/**
 * @author rheydenr
 *
 */
public class ListViewHeaderDataProvider<T> extends DefaultColumnHeaderDataProvider {
    
    private IColumnPropertyAccessor<T> derivedColumnPropertyAccessor;

    /**
     * @param columnLabels
     */
    public ListViewHeaderDataProvider(String[] columnLabels) {
        super(columnLabels);
    }
    
    /**
     * @param propertyNames
     * @param derivedColumnPropertyAccessor
     */
    public ListViewHeaderDataProvider(String[] propertyNames, IColumnPropertyAccessor<T> derivedColumnPropertyAccessor) {
        super(propertyNames);
        this.derivedColumnPropertyAccessor = derivedColumnPropertyAccessor;
    }

    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        return derivedColumnPropertyAccessor.getColumnProperty(columnIndex);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.nebula.widgets.nattable.grid.data.DefaultColumnHeaderDataProvider#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return derivedColumnPropertyAccessor.getColumnCount();
    }

}
