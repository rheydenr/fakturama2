package com.sebulli.fakturama.views.datatable;

import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsEventLayer;
import org.eclipse.nebula.widgets.nattable.layer.AbstractLayerTransform;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.reorder.RowReorderLayer;
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

    public BodyLayerStack(EventList<T> eventList, IColumnPropertyAccessor<T> columnPropertyAccessor) {

        //wrapping of the list to show into GlazedLists
        //see http://publicobject.com/glazedlists/ for further information
//        EventList<T> eventList = GlazedLists.eventList(values);
        TransformedList<T, T> rowObjectsGlazedList = GlazedLists.threadSafeList(eventList);

        //use the SortedList constructor with 'null' for the Comparator because the Comparator
        //will be set by configuration
        sortedList = new SortedList<T>(rowObjectsGlazedList, null);

        bodyDataProvider = new GlazedListsDataProvider<T>(sortedList, columnPropertyAccessor);
        bodyDataLayer = new DataLayer(getBodyDataProvider());

        //layer for event handling of GlazedLists and PropertyChanges
        GlazedListsEventLayer<T> glazedListsEventLayer = new GlazedListsEventLayer<T>(bodyDataLayer, sortedList);
        RowReorderLayer rowReorderLayer = new RowReorderLayer(glazedListsEventLayer);

        this.selectionLayer = new SelectionLayer(rowReorderLayer);
        selectionLayer.addConfiguration(new RowOnlySelectionConfiguration<T>());
        ViewportLayer viewportLayer = new ViewportLayer(getSelectionLayer());

        setUnderlyingLayer(viewportLayer);
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
}