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

import javax.inject.Inject;
import javax.money.MonetaryAmount;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.nebula.widgets.formattedtext.FormattedText;
import org.eclipse.nebula.widgets.formattedtext.ITextFormatter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;

import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.parts.widget.formatter.MoneyFormatter;

/**
 * Controls a text widget that contains the gross value of a price. This control
 * interacts with a NetText control, that contains the net value. If the value
 * of this control is changes, also the corresponding net control is modified.
 * 
 */
public class GrossText {

	// The  net value
	private MonetaryAmount netValue;

	// VAT value as factor
	private Double vatValue;

	// The corresponding text control that contains the net value
	private NetText netText;

	// The text control 
	private FormattedText grossText;
	
	private GrossText(Composite parent, int style, MonetaryAmount net, Double vat, IEclipseContext context) {

		// Set the local variables
		this.netValue = net;
		this.vatValue = vat;

		// Create the text widget
		this.grossText = new FormattedText(parent, style);
		ITextFormatter formatter;
		if(context != null) {
			formatter = ContextInjectionFactory.make(MoneyFormatter.class, context);
		} else {
			formatter = new MoneyFormatter(null);
		}
		this.grossText.setFormatter(formatter);
		if(netValue != null) {
			grossText.setValue(netValue.multiply(1 + vat));
		}

		// Set the text of the GrossText, based on the NetText's value.
		// Do this, if the text widget is selected (If "ENTER" is pressed).
		grossText.getControl().addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { 
				grossText.setValue(DataUtils.getInstance().CalculateGrossFromNet(netValue, vatValue));
		}));

		// Set the text of the NetText, based on the GrossText's value
		grossText.getControl().addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (grossText != null && grossText.getControl().isFocusControl()) {
					setNetValue(DataUtils.getInstance().calculateNetFromGross(
							grossText.getControl().getText(), 
							vatValue, netValue));

			        // Fill the SWT text field "net" with the result
					if(netText != null) {
						netText.setNetValue(netValue);
					}
				}
			}
		});
		
    	// Focus out on Return key
		grossText.getControl().addKeyListener(new KeyAdapter() {
    		public void keyPressed(KeyEvent e) {
    			if (e.keyCode == 13 || e.keyCode == SWT.KEYPAD_CR) {
    				grossText.getControl().traverse(SWT.TRAVERSE_TAB_NEXT);
    			}
    		}
    	});
	}
	
	@Inject
	public GrossText(IEclipseContext context) {
		this((Composite)context.get(Constants.CONTEXT_CANVAS), 
				(Integer)context.get(Constants.CONTEXT_STYLE), 
				(MonetaryAmount)context.get(Constants.CONTEXT_NETVALUE), 
				(Double)context.get(Constants.CONTEXT_VATVALUE), context);
	}
	

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
	public GrossText(Composite parent, int style, MonetaryAmount net, Double vat) {
		this(parent, style, net, vat, null);
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
	 * @param formattedText
	 *            The net text widget
	 */
	public void setNetText(NetText formattedText) {
		this.netText = formattedText;
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
		grossText.setValue(DataUtils.getInstance().CalculateGrossFromNet(netValue, vatValue));
	}

	/**
	 * @return the vatValue
	 */
	public final Double getVatValue() {
		return vatValue;
	}

	/**
	 * Get a reference of the net text widget
	 * 
	 * @return The net text widget.
	 */
	public NetText getNetText() {
		return netText;
	}

	/**
	 * @return the netValue
	 */
	public final MonetaryAmount getNetValue() {
		return netValue;
	}

	/**
	 * @param netValue the netValue to set
	 */
	public final void setNetValue(MonetaryAmount netValue) {
		this.netValue = netValue;
	}
}
