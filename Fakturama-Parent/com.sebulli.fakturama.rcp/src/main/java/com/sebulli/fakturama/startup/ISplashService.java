package com.sebulli.fakturama.startup;

/**
 * 
 * @see http://www.eclipse.org/forums/index.php/mv/msg/328812/857153/#msg_857153
 *
 */
public interface ISplashService {
	/**
	 * Tell the Service where to find the Splash-Image
	 * @param pluginId ID of the Plugin where the Image resides
	 */
	public void setSplashPluginId(String pluginId);
	
	/**
	 * Tell the service the path and name of the Splash-Image
	 * @param path Path and filename of the Splash-Image
	 */
	public void setSplashImagePath(String path);
	
	/**
	 * Open the Splash-Screen
	 */
	public void open();
	
	/**
	 * Close the Splash Screen
	 */
	public void close();
	
	/**
	 * Set the displayed message on the Splash-Screen
	 * @param message Text-Message to be displayed.
	 */
	public void setMessage(String message);
}