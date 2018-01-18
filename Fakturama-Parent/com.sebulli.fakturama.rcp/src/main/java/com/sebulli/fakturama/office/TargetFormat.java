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
 
package com.sebulli.fakturama.office;

/**
 * Target formats for output documents.
 *
 */
public enum TargetFormat {
	
	/** PDF format */
	PDF(".pdf", "PDF", false), 
	
	/** ODT format */
	ODT(".odt", "ODT", false),
	
	/** additional PDF format */
	ADDITIONAL_PDF(".pdf", "ADDITIONAL_PDF", true);
	
	private String extension, prefId;
	boolean absolutePath;

	/**
	 * The Constructor.
	 *
	 * @param extension the extension
	 * @param prefId the pref id
	 */
	private TargetFormat(String extension, String prefId, boolean absolutePath) {
		this.extension = extension;
		this.prefId = prefId;
		this.absolutePath = absolutePath;
	}

	/**
	 * The extension for this {@link TargetFormat} with leading dot and in lower case.
	 * @return the extension
	 */
	public final String getExtension() {
		return extension;
	}

	/**
	 * Gets the preference id (used for preferences queries).
	 *
	 * @return the prefId
	 */
	public final String getPrefId() {
		return prefId;
	}

	/**
	 * @return the absolutePath
	 */
	public final boolean isAbsolutePath() {
		return absolutePath;
	}
	
}