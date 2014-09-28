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
	/**
	 * 10x10
	 */
	MiniIconSize("10x10"),
	/**
	 * 16x16
	 */
	DefaultIconSize("16x16"),
	/**
	 * 20x20
	 */
	BrowserIconSize("20x20"),
	/**
	 * 20x20
	 */
	DocumentIconSize("20x20"),
	/**
	 * 32x32
	 */
	ToolbarIconSize("32x32"),
	/**
	 * 48x48
	 */
	BigIconSize("48x48"),
	/**
	 * Icons for Calculator
	 */
	CalcIconSize("47x47"),
	/**
	 * 48x48
	 */
	OverlayIconSize("48x48"),
	
	AppIconSize("app");

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
