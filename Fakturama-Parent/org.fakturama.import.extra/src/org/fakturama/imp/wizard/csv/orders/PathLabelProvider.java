/**
 * 
 */
package org.fakturama.imp.wizard.csv.orders;

import java.nio.file.Path;

import org.eclipse.jface.viewers.LabelProvider;

/**
 *
 */
public class PathLabelProvider extends LabelProvider {

	@Override
	public String getText(Object element) {
        String retval = "";
        if (element != null && element instanceof Path) {
		    retval = ((Path)element).getFileName().toString();
		}
        return retval;
	}
}
