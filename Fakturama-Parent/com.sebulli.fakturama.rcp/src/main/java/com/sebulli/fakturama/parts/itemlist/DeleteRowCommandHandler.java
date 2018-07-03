package com.sebulli.fakturama.parts.itemlist;

import java.util.List;

import org.eclipse.nebula.widgets.nattable.command.ILayerCommandHandler;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.layer.event.RowDeleteEvent;

public class DeleteRowCommandHandler<T> implements ILayerCommandHandler<DeleteRowCommand> {

    private List<T> bodyData;

    public DeleteRowCommandHandler(List<T> bodyData) {
        this.bodyData = bodyData;
    }

    @Override
    public Class<DeleteRowCommand> getCommandClass() {
        return DeleteRowCommand.class;
    }

    @Override
    public boolean doCommand(ILayer targetLayer, DeleteRowCommand command) {
        //convert the transported position to the target layer
        if (command.convertToTargetLayer(targetLayer)) {
            //remove the element
            this.bodyData.remove(command.getRowPosition());
            //fire the event to refresh
            targetLayer.fireLayerEvent(new RowDeleteEvent(targetLayer, command.getRowPosition()));
            return true;
        }
        return false;
    }

}