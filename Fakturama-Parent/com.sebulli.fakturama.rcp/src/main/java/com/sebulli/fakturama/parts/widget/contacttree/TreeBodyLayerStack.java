package com.sebulli.fakturama.parts.widget.contacttree;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeData;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeRowModel;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
import org.eclipse.nebula.widgets.nattable.tree.TreeLayer;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

import com.sebulli.fakturama.dao.DebitorAddress;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;
import ca.odell.glazedlists.TreeList;

/**
 * Always encapsulate the body layer stack in an AbstractLayerTransform to
 * ensure that the index transformations are performed in later commands.
 *
 * @param <T>
 */
class TreeBodyLayerStack<T extends DebitorAddress> extends AbstractLayerTransform {

    private final TreeList<T> treeList;
    private final IDataProvider bodyDataProvider;
    private final SelectionLayer selectionLayer;
    private final TreeLayer treeLayer;
	private DataLayer bodyDataLayer;

    public TreeBodyLayerStack(List<T> values,
            IColumnPropertyAccessor<T> columnPropertyAccessor,
            TreeList.Format<T> treeFormat) {
        // wrapping of the list to show into GlazedLists
        // see http://publicobject.com/glazedlists/ for further information
        EventList<T> eventList = GlazedLists.eventList(values);
        TransformedList<T, T> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);

        // use the SortedList constructor with 'null' for the Comparator
        // because the Comparator will be set by configuration
        SortedList<T> sortedList = new SortedList<>(rowObjectsGlazedList, null);
        
        // wrap the SortedList with the TreeList
        this.treeList = new TreeList<T>(sortedList, treeFormat, TreeList.NODES_START_COLLAPSED);

        this.bodyDataProvider = new ListDataProvider<T>(this.treeList, columnPropertyAccessor);
        bodyDataLayer = new DataLayer(this.bodyDataProvider);

        // simply apply labels for every column by index
        bodyDataLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

        // layer for event handling of GlazedLists and PropertyChanges
        GlazedListsEventLayer<T> glazedListsEventLayer = new GlazedListsEventLayer<>(bodyDataLayer, this.treeList);

        GlazedListTreeData<T> treeData = new GlazedListTreeData<>(this.treeList);
        ITreeRowModel<T> treeRowModel = new GlazedListTreeRowModel<>(treeData);

        this.selectionLayer = new SelectionLayer(glazedListsEventLayer);

        this.treeLayer = new TreeLayer(this.selectionLayer, treeRowModel);
        treeLayer.setRegionName(GridRegion.BODY);
        setUnderlyingLayer(new ViewportLayer(this.treeLayer));
    }

    public SelectionLayer getSelectionLayer() {
        return this.selectionLayer;
    }

    public TreeLayer getTreeLayer() {
        return this.treeLayer;
    }

    public IDataProvider getBodyDataProvider() {
        return this.bodyDataProvider;
    }
}