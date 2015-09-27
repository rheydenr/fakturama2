/**
 * 
 */
package com.sebulli.fakturama.resources.core;

/**
 * contains all possible images that are used in Fakturama
 * (which aren't icons)
 */
public enum ProgramImages {
	NO_PICTURE("/icons/product/nopicture.png"),
	NOT_FOUND_PICTURE("/icons/product/picturenotfound.png");
	
	private String path;

	/**
	 * @param path
	 */
	private ProgramImages(String path) {
		this.path = path;
	}

	/**
	 * @return the path
	 */
	public String getPath() {
		return path;
	}
	
}
