package org.fakturama.export.wizard;
import org.eclipse.jface.viewers.LabelProvider;

public class TreeLabelProvider extends LabelProvider {
	@Override
	public String getText(Object element) {
		String name = (String) element;
		return name;
	}
}
