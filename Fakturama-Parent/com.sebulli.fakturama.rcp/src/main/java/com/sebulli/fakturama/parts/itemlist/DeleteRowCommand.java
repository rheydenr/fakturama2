package com.sebulli.fakturama.parts.itemlist;

import org.eclipse.nebula.widgets.nattable.command.AbstractRowCommand;
import org.eclipse.nebula.widgets.nattable.command.ILayerCommand;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;

// see https://stackoverflow.com/questions/31907288/delete-rows-from-nattable
public class DeleteRowCommand extends AbstractRowCommand {

    public DeleteRowCommand(ILayer layer, int rowPosition) {
        super(layer, rowPosition);
    }

    protected DeleteRowCommand(DeleteRowCommand command) {
        super(command);
    }

    @Override
    public ILayerCommand cloneCommand() {
        return new DeleteRowCommand(this);
    }

}