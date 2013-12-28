/*
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2013 Ralf Heydenreich
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Ralf Heydenreich - initial API and implementation
 */
package com.sebulli.fakturama.widgets;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * @author rheydenr
 * 
 */
public class FocusAwareInputField {
	private final Text textField;
	
	public FocusAwareInputField(final Composite parent, String label) {
		textField = new Text(parent, SWT.BORDER);
		textField.setText(label);
		textField.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				textField.setBackground(null);
			}

			@Override
			public void focusGained(FocusEvent e) {
				textField.setBackground(JFaceResources.getColorRegistry().get("bgyellow"));
			}
		});
	}

	/**
	 * @return the textField
	 */
	public final Text getTextField() {
		return textField;
	}

}
