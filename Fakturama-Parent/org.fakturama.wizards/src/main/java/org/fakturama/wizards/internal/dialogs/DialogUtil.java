package org.fakturama.wizards.internal.dialogs;

import org.eclipse.swt.widgets.Composite;

public class DialogUtil {

    /**
     * Return the number of rows available in the current display using the
     * current font.
     * @param parent The Composite whose Font will be queried.
     * @return int The result of the display size divided by the font size.
     */
    public static int availableRows(Composite parent) {

        int fontHeight = (parent.getFont().getFontData())[0].getHeight();
        int displayHeight = parent.getDisplay().getClientArea().height;

        return displayHeight / fontHeight;
    }

}
