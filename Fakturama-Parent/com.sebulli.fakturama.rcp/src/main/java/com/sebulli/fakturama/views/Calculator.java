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

package com.sebulli.fakturama.views;

import java.text.DecimalFormat;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 * Calculator view
 * 
 * @author Gerd Bartelt
 */
public class Calculator {
    
    @Inject
    @Preference
    protected IEclipsePreferences defaultValuePrefs;
   
    @Inject
    protected ILogger log;

	private Composite top;
	
	// ID of this view
	public static final String ID = "com.sebulli.fakturama.views.calculator";

	// Initialize variables needed for this class.
	private Text displayText;
	// The three calculator registers.
	private String displayString = "0.";
	private String operatorString = new String();
	// A variable to store the pending calculation
	private char calcChar = ' ';

	// Error strings
	private final String ERROR_STRING = "E:";
	private final String NAN_STRING = "NaN";
	private final String INFINITY_STRING = "Infinity";
	// A flag to check if display should be cleared on the next keystroke
	private boolean clearDisplay = true;

	/**
	 * Creates the SWT controls for this workbench part.
	 * 
	 */
    @PostConstruct
    public void createComposite(Composite parent) {
		// Create the top Composite
		top = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(1).applyTo(top);
		
		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, ContextHelpConstants.CALCULATOR_VIEW);

		// Top container
		Composite container = new Composite(top, SWT.NONE);
		final GridLayout calculatorGridLayout = new GridLayout();
		calculatorGridLayout.marginRight = 10;
		calculatorGridLayout.marginLeft = 10;
		calculatorGridLayout.marginBottom = 15;
		calculatorGridLayout.marginTop = 5;
		calculatorGridLayout.marginWidth = 0;
		calculatorGridLayout.marginHeight = 0;
		calculatorGridLayout.numColumns = 4;
		calculatorGridLayout.makeColumnsEqualWidth = true;
		calculatorGridLayout.verticalSpacing = 5;
		calculatorGridLayout.horizontalSpacing = 10;
		container.setLayout(calculatorGridLayout);
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).applyTo(container);


		// The display. Note that it has a limit of 30 characters,
		// much greater than the length of a double-precision number.
		displayText = new Text(container, SWT.RIGHT | SWT.BORDER);
		displayText.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
		displayText.setEditable(false);
		displayText.setDoubleClickEnabled(false);
		displayText.setTextLimit(10);
		displayText.setText(displayString);
		displayText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 4, 1));

		
		// Set the font of the display
		FontData fD = displayText.getFont().getFontData()[0];
		fD.setHeight(24);
		Font font = new Font(null, fD);
		displayText.setFont(font);

		// Clear button
        createButton(container, 'C', false, Icon.CALC_C, 1);

        // CE Button
        createButton(container, 'E', false, Icon.CALC_CE, 1);

        // Back Button
        createButton(container, 'B', false, Icon.CALC_BACK, 1);

        // Multiply Button
        createButton(container, '*', true, Icon.CALC_X, 1);

        // Inverse Button
        createButton(container, 'I', false, Icon.CALC_INV, 1);

        // percent Button
        createButton(container, '%', false, Icon.CALC_PERCENT, 1);

        // Divide Button
        createButton(container, '/', true, Icon.CALC_DIV, 1);

        // Subtract Button
        createButton(container, '-', true, Icon.CALC_MINUS, 1);

        // Button Icon.CALC_7
        createButton(container, '7', false, Icon.CALC_7, 1);

        // Button Icon.CALC_8
        createButton(container, '8', false, Icon.CALC_8, 1);

        // Button Icon.CALC_9
        createButton(container, '9', false, Icon.CALC_9, 1);

        // Addition Button
        createButton(container, '+', true, Icon.CALC_PLUS, 2);

        // Button Icon.CALC_4
        createButton(container, '4', false, Icon.CALC_4, 1);

        // Button Icon.CALC_5
        createButton(container, '5', false, Icon.CALC_5, 1);

        // Button Icon.CALC_6
        createButton(container, '6', false, Icon.CALC_6, 1);

        // Button Icon.CALC_1
        createButton(container, '1', false, Icon.CALC_1, 1);

        // Button Icon.CALC_2
        createButton(container, '2', false, Icon.CALC_2, 1);

        // Button Icon.CALC_3
        createButton(container, '3', false, Icon.CALC_3, 1);

        // Button Icon.CALC_=
        createButton(container, '=', true, Icon.CALC_SUM, 2);

        // Button Icon.CALC_0
        createButton(container, '0', false, Icon.CALC_0, 1);

        // Button Icon.CALC_.
        createButton(container, '.', false, Icon.CALC_POINT, 1);

        // Sign Button (therefore we use the Character 's')
        createButton(container, 's', false, Icon.CALC_PLUSMINUS, 1);

		// Set the focus to the display Text and add a key listener.
		// So, you can use the mouse or the keyboard
		displayText.setFocus();
		displayText.setSelection(0);
		displayText.addKeyListener(new KeyAdapter() {

			/**
			 * Sent when a key is pressed on the system keyboard
			 * 
			 * @see org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.KeyEvent)
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				switch (e.character) {
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case 'C':
				case 'E':
				case 'B':
				case 'I':
				case '%':
				case '.':
				case 's':
					updateDisplay(e.character);
					break;
				case '*':
				case '/':
				case '-':
				case '+':
				case '=':
					updateCalc(e.character);
					break;
				case ',':
					updateDisplay('.');
					break;
				case '\n':
					updateCalc('=');
					break;
				case '\r':
					updateCalc('=');
					break;
				}
				switch (e.keyCode) {
				case 8:
					updateDisplay('B');
					break;
				case 27:
					updateDisplay('C');
					break;
				}
			}
		});

	}

	/**
	 * Set the LCD string and format zeros, decimal points and the sign
	 * 
	 * @param displayString
	 *            The (unformated) input string
	 * @param isResult
	 */
	private void setDisplayString(String displayString, boolean isResult) {
		String LCDString = displayString;

		// always display a ".", at least at the end
		if (!LCDString.contains("."))
			LCDString += ".";

		// never start with a "."
		if (LCDString.startsWith("."))
			LCDString = "0" + LCDString;

		// never start with a "-."
		if (LCDString.startsWith("-."))
			LCDString = LCDString.replaceFirst("-\\.", "-0.");

		// truncate to 10 digits
		if (LCDString.length() >= 10)
			LCDString = LCDString.substring(0, 10);

		// Remove trailing zeros
		if (isResult)
			while (LCDString.endsWith("0"))
				LCDString = LCDString.substring(0, LCDString.length() - 1);

		// Set the display text
		displayText.setText(LCDString);
	}

	/**
	 * Asks this part to take focus within the workbench.
	 */
	@Focus
	public void setFocus() {
//		displayText.setFocus();
		if(top != null) 
			top.setFocus();

	}

	/**
	 * This method updates the display text based on user input.
	 */
	private void updateDisplay(final char keyPressed) {
		char keyVal = keyPressed;
		String tempString = new String();
		boolean doClear = false;

		if (!clearDisplay) {
			tempString = displayString;
		}

		switch (keyVal) {
		case 'B': // Backspace
			if (tempString.length() < 2) {
				tempString = "";
			}
			else {
				tempString = tempString.substring(0, tempString.length() - 1);
			}
			break;

		case 'C': // Clear
			tempString = "0.";
			operatorString = "";
			calcChar = ' ';
			doClear = true;
			break;

		case 'E': // Clear Entry
			tempString = "0.";
			doClear = true;
			break;

		case 'I': // Inverse
			tempString = doCalc(displayString, "", keyVal);
			doClear = true;
			break;

		case '%': // Percent
			tempString = doCalc(displayString, operatorString, keyVal);
			doClear = true;
			break;

		case 's': // Change Sign
		    keyVal = '-';
			if (tempString.startsWith("-")) {
				tempString = tempString.substring(1, tempString.length());
			}
			else {
				tempString = keyVal + tempString;
			}
			break;

		case '.': // Can't have two decimal points.
			if (tempString.indexOf(".") == -1 && tempString.length() < 9) {
				tempString = tempString + keyVal;
			}
			break;

		case '0': // Don't want 00 to be entered.
			if (!tempString.equals("0") && tempString.length() < 9) {
				tempString = tempString + keyVal;
			}
			break;

		default: // Default case is for the digits 1 through 9.
			if (tempString.length() < 9) {
				tempString = tempString + keyVal;
			}
			break;
		}

		clearDisplay = doClear;
		if (!displayString.equals(tempString)) {
			displayString = tempString;
			setDisplayString(displayString, keyVal == '%' || keyVal == 'I');
		}
	}

	/**
	 * This method converts the operator and display strings to double values
	 * and performs the calculation.
	 * 
	 * @param valAString
	 *            First operand
	 * @param valBString
	 *            Second operand
	 * @param opChar
	 *            Operator
	 * @return The result of the calculation
	 */
	private String doCalc(final String valAString, final String valBString, final char opChar) {
		String resultString = ERROR_STRING + NAN_STRING;
		Double valA = 0.0;
		Double valB = 0.0;
		Double valAnswer = 0.0;

		// Make sure register strings are numbers
		if (valAString.length() > 0) {
			try {
				valA = Double.parseDouble(valAString);
			}
			catch (NumberFormatException e) {
				return resultString;
			}
		}
		else {
			return resultString;
		}

		if (opChar != '%' && opChar != 'I') {
			if (valBString.length() > 0) {
				try {
					valB = Double.parseDouble(valBString);
				}
				catch (NumberFormatException e) {
					return resultString;
				}
			}
			else {
				return resultString;
			}
		}

		if (opChar == '%') {
			if (valBString.length() > 0) {
				try {
					valB = Double.parseDouble(valBString);
				}
				catch (NumberFormatException e) {
					return resultString;
				}
			}
			else {
				valB = 1.0;
			}
		}

		switch (opChar) {
		case '%': // Percent
			valAnswer = valB * (valA / 100);
			break;

		case 'I': // Inverse
			valB = 1.0;
			valAnswer = valB / valA;
			break;

		case '+': // Addition
			valAnswer = valA + valB;
			break;

		case '-': // Subtraction
			valAnswer = valA - valB;
			break;

		case '/': // Division
			valAnswer = valA / valB;
			break;

		case '*': // Multiplication
			valAnswer = valA * valB;
			break;

		default: // Do nothing - this should never happen
			break;

		}
		// Convert answer to string and format it before return.

		DecimalFormat format = new DecimalFormat("0.000000000000");
		// resultString = valAnswer.toString();
		resultString = format.format(valAnswer);
		resultString = resultString.replace(',', '.');
		resultString = trimString(resultString);
		return resultString;
	}

	/**
	 * 
	 * This method updates the operator and display strings, and the pending
	 * calculation flag.
	 * 
	 * @param keyPressed
	 *            The operator
	 */
	private void updateCalc(char keyPressed) {
		char keyVal = keyPressed;
		String tempString = displayString;

		// If there is no display value, the keystroke is deemed invalid and
		// nothing is done.
		if (tempString.length() == 0) { return; }

		/*
		 * If there is no operator value, only calculation key presses are
		 * considered valid. Check that the display value is valid and if so,
		 * move the display value to the operator. No calculation is done.
		 */
		if (operatorString.length() == 0) {
			if (keyVal != '=') {
				tempString = trimString(tempString);
				if (tempString.startsWith(ERROR_STRING)) {
					clearDisplay = true;
					operatorString = "";
					calcChar = ' ';
				}
				else {
					operatorString = tempString;
					calcChar = keyVal;
					clearDisplay = true;
				}
			}
			return;
		}

		// There is an operator and a display value, so do the calculation.
		displayString = doCalc(operatorString, tempString, calcChar);

		/*
		 * If '=' was pressed or result was invalid, reset pending calculation
		 * flag and operator value. Otherwise, set new calculation flag so
		 * calculations can be chained.
		 */
		if (keyVal == '=' || displayString.startsWith(ERROR_STRING)) {
			calcChar = ' ';
			operatorString = "";
		}
		else {
			calcChar = keyVal;
			operatorString = displayString;
		}

		// Set the clear display flag and show the result.
		clearDisplay = true;
		setDisplayString(displayString, true);
	}

	/**
	 * This method formats a string.
	 */
	private String trimString(final String newString) {
		String tempString = newString;

		// Value is not a number
		if (tempString.equals("NaN")) {
			tempString = ERROR_STRING + NAN_STRING;
			return tempString;
		}
		// Value is infinity
		if (tempString.equals("Infinity") || tempString.equals("-Infinity")) {
			tempString = ERROR_STRING + INFINITY_STRING;
			return tempString;
		}
		// Value is -0
		if (tempString.equals("-0.0")) {
			tempString = "0";
			return tempString;
		}
		// Trim unnecessary trailing .0
		if (tempString.endsWith(".0")) {
			tempString = tempString.substring(0, tempString.length() - 2);
		}
		/*
		 * // String is too long to display if (tempString.length() > 8) {
		 * //tempString = ERROR_STRING + LONG_STRING; tempString =
		 * tempString.substring(0, 9); }
		 */
		return tempString;
	}

	/**
	 * Creates a button and assign a mouse listener
	 * 
	 * @param parent
	 *            The parent SWT composite
	 * @param c
	 *            The character of the button
	 * @param isCalc
	 *            True, if it is an operator
	 * @param commandIcon
	 *            The icon's substring
	 * @param vspan
	 *            Number of rows
	 * @return A reference to the new button
	 */
	private Label createButton(Composite parent, final char c, boolean isCalc, Icon commandIcon, int vspan) {
		// Create a button
		Label button = new Label(parent, SWT.NONE);
		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDown(MouseEvent e) {
				
				
				switch (c) {
				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':
				case '8':
				case '9':
				case 'C':
				case 'E':
				case 'B':
				case 'I':
				case '%':
				case '.':
				case 's':
					updateDisplay(c);
					break;
				case '*':
				case '/':
				case '-':
				case '+':
				case '=':
					updateCalc(c);
				}
			}
		});

		// Set the icon
		try {
			button.setImage(commandIcon.getImage(IconSize.CalcIconSize));
			button.setToolTipText(String.valueOf(c));
		}
		catch (Exception e) {
			log.error(e, "Icon not found");
		}
		GridDataFactory.swtDefaults().span(1, vspan).align(SWT.CENTER, SWT.CENTER).applyTo(button);

		return button;
	}

}
