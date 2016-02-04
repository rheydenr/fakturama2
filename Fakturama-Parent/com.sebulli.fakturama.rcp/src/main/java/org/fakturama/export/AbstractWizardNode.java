package org.fakturama.export;

import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.swt.graphics.Point;

public abstract class AbstractWizardNode implements IWizardNode {

	public AbstractWizardNode() {
		super();
	}

	public abstract String getName();
	public abstract String getCategory();
	
	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public Point getExtent() {
		 return new Point(-1, -1);
	}

}