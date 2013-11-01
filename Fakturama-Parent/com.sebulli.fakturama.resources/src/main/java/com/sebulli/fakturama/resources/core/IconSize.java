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
package com.sebulli.fakturama.resources.core;

/**
 * Definition of various image sizes for transparent using with Icon class.
 * 
 * @author rheydenr
 *
 */
public enum IconSize {
	MiniIconSize("10x10"),
	DefaultIconSize("16x16"),
	BrowserIconSize("20x20"),
	DocumentIconSize("20x20"),
	ToobarIconSize("32x32"),
	BigIconSize("48x48"),
	OverlayIconSize("48x48");

	/**
	 * the name (directory path) of this image set
	 */
	public String name;

	/**
	 * hidden constructor
	 * 
	 * @param name
	 */
	private IconSize(String name) {
		this.name = name;
	}
}
