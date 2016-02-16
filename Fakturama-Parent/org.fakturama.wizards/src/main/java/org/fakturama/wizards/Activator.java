/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2016 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package org.fakturama.wizards;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.MissingResourceException;
import java.util.Optional;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;


/**
 *
 */
public class Activator implements BundleActivator {

	
	private static Activator defaultInstance;

	private BundleContext context;

	public Activator() {
		defaultInstance = this;
	}

	public static Activator getDefault() {
		return defaultInstance;
	}

	/*
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext bundleContext) throws Exception {
		context = bundleContext;
	}

	/*
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		context = null;
	}

	/**
	 * provide access to current bundle for resource loading purposes
	 */
	public Bundle getBundle() {
		return context.getBundle();
	}
	
	/**
	 * Creates and returns a new image descriptor for an image file located
	 * within the specified plug-in.
	 * <p>
	 * This is a convenience method that simply locates the image file in within
	 * the plug-in. It will now query the ISharedImages registry first. The path
	 * is relative to the root of the plug-in, and takes into account files
	 * coming from plug-in fragments. The path may include $arg$ elements.
	 * However, the path must not have a leading "." or path separator. Clients
	 * should use a path like "icons/mysample.gif" rather than
	 * "./icons/mysample.gif" or "/icons/mysample.gif".
	 * </p>
	 * 
	 * @param pluginId
	 *            the id of the plug-in containing the image file;
	 *            <code>null</code> is returned if the plug-in does not exist
	 * @param imageFilePath
	 *            the relative path of the image file, relative to the root of
	 *            the plug-in; the path must be legal
	 * @return an image descriptor, or <code>null</code> if no image could be
	 *         found
	 * @since 3.0
	 */
    public ImageDescriptor imageDescriptorFromPlugin(String pluginId,
            String imageFilePath) {
        if (pluginId == null || imageFilePath == null) {
            throw new IllegalArgumentException();
        }

		ImageDescriptor imageDescriptor = JFaceResources.getImageRegistry()
				.getDescriptor(imageFilePath);
		if (imageDescriptor != null)
			return imageDescriptor; // found in the shared images

        // if the bundle is not ready then there is no image
		
        Bundle bundle = findBundle(pluginId);
        if (bundle.getState() != Bundle.ACTIVE) {
			return null;
		}

        // look for the image (this will check both the plugin and fragment folders
        URL fullPathString = FileLocator.find(bundle, new Path(imageFilePath), null);
        if (fullPathString == null) {
            try {
                fullPathString = new URL(imageFilePath);
            } catch (MalformedURLException e) {
                return null;
            }
        }

        return ImageDescriptor.createFromURL(fullPathString);
    }
    

    private Bundle findBundle(String pluginId) {
    	Optional<Bundle> firstFoundBundle = Arrays.stream(context.getBundles()).filter(b -> b.getSymbolicName().equalsIgnoreCase(pluginId)).findFirst();
    	// fallback: return this bundle
		return firstFoundBundle.orElse(getBundle());
	}

	/**
     * Returns an image. Clients do not need to dispose the image, it will be
     * disposed automatically.
     * 
     * @return an {@link Image}
     */
    public Image getImage(String path) {
        Image image = JFaceResources.getImage(path);
        if (image == null) {
            image = addIconImageDescriptor(path);
        }
        return image;
    }

    /**
     * Add an image descriptor for a specific key to the
     * global {@link ImageRegistry}
     * 
     * @param name
     * @param is
     * @return <code>true</code> if successfully added, else <code>false</code>
     */
    private Image addIconImageDescriptor(String path) {
        try {
            URL fileLocation = new File(path).toURI().toURL();
            ImageDescriptor id = ImageDescriptor.createFromURL(fileLocation);
            JFaceResources.getImageRegistry().put(path, id);
        }
        catch (MissingResourceException | MalformedURLException | IllegalArgumentException e) {
            return null;
        }
        return JFaceResources.getImage(path);
    }

}
