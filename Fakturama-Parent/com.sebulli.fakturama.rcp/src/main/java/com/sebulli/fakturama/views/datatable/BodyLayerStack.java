package com.sebulli.fakturama.views.datatable;

import java.io.Serializable;

import org.eclipse.nebula.widgets.nattable.copy.command.CopyDataCommandHandler;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ColumnLabelAccumulator;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionConfiguration;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TransformedList;

import com.sebulli.fakturama.model.IEntity;

/**
 * Always encapsulate the body layer stack in an AbstractLayerTransform to
 * ensure that the index transformations are performed in later commands.
 * 
 * @param <T>
 */
public class BodyLayerStack<T extends IEntity> extends AbstractLayerTransform {

    private GlazedListsDataProvider<T> bodyDataProvider;
    private DataLayer bodyDataLayer;
    private final SelectionLayer selectionLayer;
    private SortedList<T> sortedList;
    private RowReorderLayer rowReorderLayer;
    private GlazedListsEventLayer<T> glazedListsEventLayer;
	private ViewportLayer viewportLayer;

    public BodyLayerStack(EventList<T> eventList, IColumnPropertyAccessor<T> columnPropertyAccessor) {
        this(eventList, columnPropertyAccessor, new IRowIdAccessor<T>() {
            @Override
            public Serializable getRowId(T rowObject) {
                // default implementation uses entity id as row id
                return rowObject.getId();
            }
        });
    }

    public BodyLayerStack(EventList<T> eventList, IColumnPropertyAccessor<T> columnPropertyAccessor, IRowIdAccessor<T> rowIdAccessor) {

        //wrapping of the list to show into GlazedLists
        //see http://publicobject.com/glazedlists/ for further information
        //        EventList<T> eventList = GlazedLists.eventList(values);
        TransformedList<T, T> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);

        //use the SortedList constructor with 'null' for the Comparator because the Comparator
        //will be set by configuration
        this.sortedList = new SortedList<T>(rowObjectsGlazedList, null);

        this.bodyDataProvider = new GlazedListsDataProvider<T>(sortedList, columnPropertyAccessor);
        this.bodyDataLayer = new DataLayer(bodyDataProvider);

//        HoverLayer hoverLayer = new HoverLayer(bodyDataLayer);

        glazedListsEventLayer = new GlazedListsEventLayer<T>(bodyDataLayer, sortedList);
        
        // add a label accumulator to be able to register converter
        // this is crucial for using custom values display
        glazedListsEventLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
        rowReorderLayer = new RowReorderLayer(glazedListsEventLayer);
        this.selectionLayer = new SelectionLayer(rowReorderLayer);

        //use a RowSelectionModel that will perform row selections and is able to identify a row via unique ID
        RowSelectionModel<T> selectionModel = new RowSelectionModel<T>(selectionLayer, bodyDataProvider, rowIdAccessor, false);
        selectionLayer.setSelectionModel(selectionModel);
        // Select complete rows
        selectionLayer.addConfiguration(new RowOnlySelectionConfiguration<T>());
        
        viewportLayer = new ViewportLayer(selectionLayer);
        // as the selection mouse bindings are registered for the region label
        // GridRegion.BODY we need to set that region label to the viewport so
        // the selection via mouse is working correctly
//        viewportLayer.setRegionName(GridRegion.BODY);

        setUnderlyingLayer(viewportLayer);

        registerCommandHandler(new CopyDataCommandHandler(this.selectionLayer));
    }

    /**
     * @return the sortedList
     */
    public SortedList<T> getSortedList() {
        return sortedList;
    }

    protected SelectionLayer getSelectionLayer() {
        return selectionLayer;
    }

    protected GlazedListsDataProvider<T> getBodyDataProvider() {
        return bodyDataProvider;
    }

    /**
     * @return the bodyDataLayer
     */
    protected DataLayer getBodyDataLayer() {
        return bodyDataLayer;
    }

    /**
     * @return the rowReorderLayer
     */
    public RowReorderLayer getRowReorderLayer() {
        return rowReorderLayer;
    }

    /**
     * @return the glazedListsEventLayer
     */
    public GlazedListsEventLayer<T> getGlazedListsEventLayer() {
        return glazedListsEventLayer;
    }

	/**
	 * @return the viewportLayer
	 */
	public final ViewportLayer getViewportLayer() {
		return viewportLayer;
	}
}