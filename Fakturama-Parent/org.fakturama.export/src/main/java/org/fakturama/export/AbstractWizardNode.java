package org.fakturama.export;

import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

public abstract class AbstractWizardNode implements IWizardNode {
	
	private Image image = null;
	private String category = null;
	private String description = null;

    /**
     * a descriptive label of the wizard
     */
    private String name;

	public AbstractWizardNode() {
		super();
	}
	
	public String getCategory() {
		return category;
	}
	
	/**
	 * @param category the category to set
	 */
	public void setCategory(String category) {
		this.category = category;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
	}

	@Override
	public Point getExtent() {
		 return new Point(-1, -1);
	}
	
	public Image getImage() {
		return image;
	}

	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public final void setDescription(String description) {
		this.description = description;
	}

}