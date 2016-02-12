package org.fakturama.export.wizard;
import java.util.List;

import org.fakturama.export.AbstractWizardNode;

import com.sebulli.fakturama.parts.widget.contentprovider.SimpleTreeContentProvider;

public class TreeContentProvider extends SimpleTreeContentProvider {

	List<AbstractWizardNode> inputElements;
	@Override
	public Object[] getElements(Object arg0) {
		this.inputElements = (List<AbstractWizardNode>) arg0;
		return this.inputElements.toArray();
	}
}