package com.sebulli.fakturama.parts.widget.contacttree;

import java.io.Serializable;

import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.data.ListDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.DetailGlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeData;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.tree.GlazedListTreeRowModel;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.AbstractIndexLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionConfiguration;
import org.eclipse.nebula.widgets.nattable.tree.ITreeRowModel;
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
public class TempBodyLayerStack<T extends DebitorAddress> extends AbstractIndexLayerTransform {

    private ListDataProvider<T> bodyDataProvider;
    private DataLayer bodyDataLayer;
    private final SelectionLayer selectionLayer;
    private final TreeList<T> treeList;
    private SortedList<T> sortedList;
    private DetailGlazedListsEventLayer<T> glazedListsEventLayer;
//    private final TreeLayer treeLayer;
	private ViewportLayer viewportLayer;

    public TempBodyLayerStack(EventList<T> eventList, 
    		IColumnPropertyAccessor<T> columnPropertyAccessor,
            TreeList.Format<T> treeFormat) {
        this(eventList, columnPropertyAccessor, new IRowIdAccessor<T>() {
            @Override
            public Serializable getRowId(T rowObject) {
                // default implementation uses entity id as row id
                return rowObject.getCustomerNumber();
            }
        }, treeFormat);
    }

    public TempBodyLayerStack(EventList<T> eventList, 
    		IColumnPropertyAccessor<T> columnPropertyAccessor, 
    		IRowIdAccessor<T> rowIdAccessor,
            TreeList.Format<T> treeFormat) {

        //wrapping of the list to show into GlazedLists
        //see http://publicobject.com/glazedlists/ for further information
        TransformedList<T, T> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);

        //use the SortedList constructor with 'null' for the Comparator because the Comparator
        //will be set by configuration
        this.sortedList = new SortedList<T>(rowObjectsGlazedList, null);
        
        // wrap the SortedList with the TreeList
        this.treeList = new TreeList<T>(sortedList, treeFormat, TreeList.NODES_START_COLLAPSED);

        this.bodyDataProvider = new ListDataProvider<T>(sortedList, columnPropertyAccessor);
        this.bodyDataLayer = new DataLayer(bodyDataProvider);

        // layer for event handling of GlazedLists and PropertyChanges
        glazedListsEventLayer = new DetailGlazedListsEventLayer<T>(bodyDataLayer, sortedList);
        
        // add a label accumulator to be able to register converter
        // this is crucial for using custom values display
        glazedListsEventLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());

//        GlazedListTreeData<T> treeData = new GlazedListTreeData<>(this.treeList);
//        ITreeRowModel<T> treeRowModel = new GlazedListTreeRowModel<>(treeData);
        
        this.selectionLayer = new SelectionLayer(glazedListsEventLayer);

        //use a RowSelectionModel that will perform row selections and is able to identify a row via unique ID
        RowSelectionModel<T> selectionModel = new RowSelectionModel<T>(selectionLayer, bodyDataProvider, rowIdAccessor, false);
        selectionLayer.setSelectionModel(selectionModel);
        // Select complete rows
        selectionLayer.addConfiguration(new RowOnlySelectionConfiguration<T>());
        
//        this.treeLayer = new TreeLayer(this.selectionLayer, treeRowModel);
//        treeLayer.setRegionName(GridRegion.BODY);
        
        viewportLayer = new ViewportLayer(selectionLayer);
        // as the selection mouse bindings are registered for the region label
        // GridRegion.BODY we need to set that region label to the viewport so
        // the selection via mouse is working correctly
        viewportLayer.setRegionName(GridRegion.BODY);

        setUnderlyingLayer(viewportLayer);

        registerCommandHandler(new CopyDataCommandHandler(this.selectionLayer));
    }

    /**
     * @return the sortedList
     */
    public SortedList<T> getSortedList() {
        return sortedList;
    }
    
    public TreeList<T> getTreeList() {
		return treeList;
	}

	protected SelectionLayer getSelectionLayer() {
        return selectionLayer;
    }

    protected ListDataProvider<T> getBodyDataProvider() {
        return bodyDataProvider;
    }

    /**
     * @return the bodyDataLayer
     */
    protected DataLayer getBodyDataLayer() {
        return bodyDataLayer;
    }

    /**
     * @return the glazedListsEventLayer
     */
    public DetailGlazedListsEventLayer<T> getGlazedListsEventLayer() {
        return glazedListsEventLayer;
    }

	/**
	 * @return the viewportLayer
	 */
	public final ViewportLayer getViewportLayer() {
		return viewportLayer;
	}
}