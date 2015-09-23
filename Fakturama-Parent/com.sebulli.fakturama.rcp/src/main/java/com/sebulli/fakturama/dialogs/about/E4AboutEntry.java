/**
 * 
 */
package com.sebulli.fakturama.dialogs.about;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * @author G527032
 *
 */
public class E4AboutEntry extends AboutData {
	
	ImageDescriptor image;
	String aboutText;

	/**
	 * @param providerName
	 * @param name
	 * @param version
	 * @param id
	 */
	public E4AboutEntry(String providerName, String name, String version, String id) {
		super(providerName, name, version, id);
	}

	/**
	 * @return the image
	 */
	public final ImageDescriptor getImage() {
		return image;
	}

	/**
	 * @param image the image to set
	 */
	public final void setImage(ImageDescriptor image) {
		this.image = image;
	}

	/**
	 * @return the aboutText
	 */
	public final String getAboutText() {
		return aboutText;
	}

	/**
	 * @param aboutText the aboutText to set
	 */
	public final void setAboutText(String aboutText) {
		this.aboutText = aboutText;
	}
	
	

}
