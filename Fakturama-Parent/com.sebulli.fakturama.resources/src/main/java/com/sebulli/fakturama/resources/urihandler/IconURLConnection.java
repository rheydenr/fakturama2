/*******************************************************************************
 * Copyright (c) 2012 Marco Descher.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marco Descher - initial API and implementation
 ******************************************************************************/
package com.sebulli.fakturama.resources.urihandler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.MissingResourceException;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;

import com.sebulli.fakturama.resources.Activator;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

public class IconURLConnection extends URLConnection {

	String iconName;

	protected IconURLConnection(URL url) {
		super(url);
		iconName = url.getAuthority();
	}

	@Override
	public InputStream getInputStream() throws IOException {
		try {
			// for the application model, the size is set fixed to 16x16...
			Icon selectedIcon = Icon.valueOf(iconName);
			IconSize size = IconSize.DefaultIconSize;
			if(iconName.startsWith("ICON_")) {
				// ... except it starts with ICON_, because then it's a toolbar icon
				size = IconSize.ToolbarIconSize;
			}
			InputStream is = selectedIcon
					.getImageAsInputStream(size);
			return is;
		} catch (MissingResourceException | IllegalArgumentException e) {
			System.out
					.println("[ERROR] " + IconURLConnection.class.getName()
							+ " " + iconName
							+ " not found, replacing with empty icon.");
			return FileLocator.find(Activator.getDefault().getBundle(),
					new Path("icons/empty.png"), null).openStream();
		}
	}

	@Override
	public void connect() throws IOException {
	}
}
