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

import org.eclipse.nebula.widgets.nattable.data.IDataProvider;

/**
 *
 */
public class ListViewRowHeaderDataProvider implements IDataProvider {
    protected final IDataProvider bodyDataProvider;
    private final boolean withRowHeader;

    public ListViewRowHeaderDataProvider(IDataProvider bodyDataProvider, boolean withRowHeader) {
        this.bodyDataProvider = bodyDataProvider;
        this.withRowHeader = withRowHeader;
    }
    @Override
    public Object getDataValue(int columnIndex, int rowIndex) {
        String retval = null;
        if(withRowHeader) {
            // data Value is already the PosNo
            retval = Integer.toString((Integer) bodyDataProvider.getDataValue(columnIndex, rowIndex));
        }
        return retval;
    }

    @Override
    public void setDataValue(int columnIndex, int rowIndex, Object newValue) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getColumnCount() {
        // if this value is > 0 we get a row header (which is useful e.g. for reordering)
        return withRowHeader ? 1 : 0;
    }

    public int getRowCount() {
        return bodyDataProvider.getRowCount();
    }
}
