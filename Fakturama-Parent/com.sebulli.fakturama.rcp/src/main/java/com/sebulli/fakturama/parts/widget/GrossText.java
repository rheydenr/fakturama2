/* 
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.parts.widget;

import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;

import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.parts.widget.formatter.MoneyFormatter;

/**
 * Controls a text widget that contains the gross value of a price. This control
 * interacts with a NetText control, that contains the net value. If the value
 * of this control is changes, also the corresponding net control is modified.
 * 
 * @author Gerd Bartelt
 */
public class GrossText {

	// The  net value
	private Double netValue;

	// VAT value as factor
	private Double vatValue;

	// The corresponding text control that contains the net value
	private NetText netText;

	// The text control 
	private FormattedText grossText;

	/**
	 * Constructor that creates the text widget and connects it with the
	 * corresponding net widget.
	 * 
	 * @param editor
	 *            The editor that contains this widget.
	 * @param parent
	 *            The parent control.
	 * @param style
	 *            Style of the text widget
	 * @param net
	 *            The net value
	 * @param vat
	 *            The vat value ( factor )
	 */
	public GrossText(Composite parent, int style, Double net, Double vat) {

		// Set the local variables
		this.netValue = net;
		this.vatValue = vat;

		// Create the text widget
		this.grossText = new FormattedText(parent, style);
		this.grossText.setFormatter(new MoneyFormatter());
		grossText.setValue(netValue * (1 + vat));

//		// Set the text of the GrossText, based on the NetText's value.
//		// Do this, if the text widget is selected (If "ENTER" is pressed).
//		grossText.getControl().addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {
//				grossText.setValue(DataUtils.getInstance().calculateGrossFromNetAsDouble(netValue, vatValue));
//			}
//		});

		// Set the text of the NetText, based on the GrossText's value
		grossText.getControl().addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (grossText.getControl().isFocusControl()) {
					netValue = DataUtils.getInstance().calculateNetFromGrossAsDouble((Double)grossText.getValue(), vatValue);
					if(netText != null) {
						netText.getNetText().setValue(netValue);
					}
				}
			}
		});

	}

	/**
	 * Set the visibility of the text widget.
	 * 
	 * @param visible
	 *            True, if visible
	 */
	public void setVisible(boolean visible) {
		grossText.getControl().setVisible(visible);
	}

	/**
	 * Get a reference of the text widget
	 * 
	 * @return The text widget.
	 */
	public FormattedText getGrossText() {
		return this.grossText;
	}

	/**
	 * Set a reference to the net text widget
	 * 
	 * @param netT
	 *            The net text widget
	 */
	public void setNetText(NetText netT) {
		this.netText = netT;
		this.netValue = ((Double) netText.getNetText().getValue());
	}

	/**
	 * Update the Vat factor and recalculate the text of the gross text based on
	 * the net text.
	 * 
	 * @param vatValue
	 *            The Vat value as factor.
	 */
	public void setVatValue(Double vatValue) {
		this.vatValue = vatValue;
		grossText.setValue(DataUtils.getInstance().calculateGrossFromNetAsDouble((Double) netText.getNetText().getValue(), vatValue));
	}

	/**
	 * Get a reference of the net text widget
	 * 
	 * @return The net text widget.
	 */
	public NetText getNetText() {
		return netText;
	}

	public void recalculate(FormattedText netText2, Double vatValue2) {
		this.netValue = (Double)netText2.getValue();
		setVatValue(vatValue2);
	}
}
