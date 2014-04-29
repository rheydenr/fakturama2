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

package com.sebulli.fakturama.parts;

import static com.sebulli.fakturama.Translate._;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.PersistState;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.sebulli.fakturama.dao.ContactDAO;
import com.sebulli.fakturama.model.IEntity;

/**
 * Parent class for all editors
 * 
 * @author Gerd Bartelt
 */
public abstract class Editor<T extends IEntity> {
	
	@Inject
	@Preference(nodePath = "/configuration/defaultValues")
	protected IEclipsePreferences defaultValuePrefs;
	
	@Inject
	protected ContactDAO contactDAO;

	protected StdComposite stdComposite = null;
	protected String tableViewID = "";
	protected String editorID = "";
	protected static final int NO_ERROR = 0;
	protected static final int ERROR_NOT_NEXT_ID = 1;
	
	protected abstract MDirtyable getMDirtyablePart();

	/**
	 * Set the font size of a label to 24pt
	 * 
	 * @param label
	 *            The label that is modified
	 */
	protected void makeLargeLabel(Label label) {
		resizeLabel(label, 24);
	}

	/**
	 * Set the font size of a label to 9pt
	 * 
	 * @param label
	 *            The label that is modified
	 */
	protected void makeSmallLabel(Label label) {
		resizeLabel(label, 9);
	}

	/**
	 * Set the font size of a label to x px
	 * 
	 * @param label
	 *            The label that is modified
	 * @size Size of the label in px
	 */
	protected void resizeLabel(Label label, int size) {
		FontData[] fD = label.getFont().getFontData();
		fD[0].setHeight(size);
		Font font = new Font(null, fD[0]);
		label.setFont(font);
		font.dispose();
	}

	/**
	 * Class to create the widgets to show and set the standard entry.
	 * 
	 */
	protected class StdComposite {

		// Text widgets that displays the standard widget
		private Text txtStd;
		// The button
		public Button stdButton;

		// The property key that defines the standard
		private String propertyKey = null;

		// The unidataset of this editor 
		private final T uds;

		// The label for "This dataset"
		private String thisDataset = null;

		// The data set array with this and the other unidatasets
		private List<T> dataSetArray;

		/**
		 * Constructor Creates the widgets to set this entry as standard entry.
		 * 
		 * @param parent
		 *            The parent widget
		 * @param uds
		 *            The editor's unidataset
		 * @param dataSetArray
		 *            This and the other unidatasets
		 * @param propertyKey
		 *            The property key that defines the standard
		 * @param thisDataset
		 *            Text for "This dataset"
		 * @param hSpan
		 *            Horizontal span
		 */
		public StdComposite(Composite parent, final T uds, final T currentStandardEntry, final String thisDataset, int hSpan) {

			// Set the local variables
			this.uds = uds;
			this.thisDataset = thisDataset;

			// Create a container for the text widget and the button
			Composite stdComposite = new Composite(parent, SWT.NONE);
			GridLayoutFactory.fillDefaults().numColumns(2).applyTo(stdComposite);
			GridDataFactory.fillDefaults().span(hSpan, 1).applyTo(stdComposite);

			// Create the text widget that displays the standard entry
			txtStd = new Text(stdComposite, SWT.BORDER);
			txtStd.setEditable(false);

			GridDataFactory.swtDefaults().hint(200, -1).align(SWT.BEGINNING, SWT.CENTER).applyTo(txtStd);
			setStdText(currentStandardEntry);

			// Create the button to make this entry to the standard
			stdButton = new Button(stdComposite, SWT.BORDER);
			//T: Button text
			stdButton.setText(_("Set as standard"));
			GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(stdButton);
			stdButton.setEnabled(false);
			stdButton.addSelectionListener(new SelectionAdapter() {
				
				/**
				 * Make this entry to the standard
				 * 
				 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
				 */
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (uds.getId() >= 0) {
						defaultValuePrefs.putLong(propertyKey, uds.getId());
						txtStd.setText(thisDataset);
						refreshView();
					}
				}
			});

		}

		/**
		 * Test, if this is the standard entry and set the text of the text
		 * widget.
		 */
		public void setStdText(T stdEntry) {
			if (txtStd != null) {

				// If the editor's unidataset is the standard entry
				if (uds.getId() == stdEntry.getId()) {
					// Mark it as "standard" ..
					txtStd.setText(thisDataset);
				}
				else
					// .. or display the one that is the standard entry.
					txtStd.setText(stdEntry.getName());
			}
		}
		
		/**
		 * Set the tool tip text
		 * 
		 * @param toolTipText
		 * 				The tool tip text
		 */
		public void setToolTipText (String toolTipText) {
			stdButton.setToolTipText(toolTipText);
		}
		
		/**
		 * Sets the focus of this component to the default button.
		 */
		public void focus() {
			stdButton.setFocus();
		}

	}

	/**
	 * Asks this part to take focus within the workbench. Set the focus to the
	 * standard text
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Focus
	public void setFocus() {
		if (stdComposite != null)
//			stdComposite.setStdText();
			stdComposite.focus();
	}

	/**
	 * Get the next document number
	 * 
	 * @return The next document number
	 */
	protected String getNextNr() {

		// Create the string of the preference store for format and number
		String prefStrFormat = "NUMBERRANGE_" + editorID.toUpperCase() + "_FORMAT";
		String prefStrNr = "NUMBERRANGE_" + editorID.toUpperCase() + "_NR";
		String format;
		String nrExp = "";
		String nextNr;
		int nr;

		// Store the date of now to a property
		Calendar calendar = new GregorianCalendar();
		int yyyy = calendar.get(Calendar.YEAR);
		int mm = calendar.get(Calendar.MONTH) + 1;
		int dd = calendar.get(Calendar.DAY_OF_MONTH);
		String lastSetNextNrDate = defaultValuePrefs.get("last_setnextnr_date_" + editorID.toLowerCase(), "2000-01-01");

		int last_yyyy = 0; 
		int last_mm = 0; 
		int last_dd = 0; 

		// Get the year, month and date of a string like "2011-12-24"
		if (lastSetNextNrDate.length() == 10) {
			try {
				/*
				 * TODO use org.apache.commons.convert
						Class DateTimeConverters.StringToDate
				 */
				last_yyyy = Integer.parseInt(lastSetNextNrDate.substring(0, 4));
				last_mm = Integer.parseInt(lastSetNextNrDate.substring(5, 7));
				last_dd = Integer.parseInt(lastSetNextNrDate.substring(8, 10));
			}
			catch (Exception e){ /* TODO give a reasonable Exception! */};
		}
		
		// Get the last (it's the next free) document number from the preferences
		format = defaultValuePrefs.get(prefStrFormat, "NO_DEFAULT_VALUE");
		nr = defaultValuePrefs.getInt(prefStrNr, 1);

		// Check, whether the date string is a new one
		boolean startNewCounting = false;
		if (format.contains("{yyyy}") || format.contains("{yy}"))
			if (yyyy != last_yyyy)
				startNewCounting = true;
		if (format.contains("{mm}"))
			if (mm != last_mm)
				startNewCounting = true;
		if (format.contains("{dd}"))
			if (dd != last_dd)
				startNewCounting = true;
		
		// Reset the counter
		if (startNewCounting) {
			nr = 1;
			setNextNumber(prefStrNr, nr); 
		}
			
		
		// Replace the date information
		format = format.replace("{yyyy}", String.format("%04d", yyyy));
		format = format.replace("{yy}", String.format("%04d", yyyy).substring(2, 4));
		format = format.replace("{mm}", String.format("%02d", mm));
		format = format.replace("{dd}", String.format("%02d", dd));
		format = format.replace("{YYYY}", String.format("%04d", yyyy));
		format = format.replace("{YY}", String.format("%04d", yyyy).substring(2, 4));
		format = format.replace("{MM}", String.format("%02d", mm));
		format = format.replace("{DD}", String.format("%02d", dd));
		
		
		// Find the placeholder for a decimal number with n digits
		// with the format "{Xnr}", "X" is the number of digits.
		Pattern p = Pattern.compile("\\{\\d*nr\\}");
		Matcher m = p.matcher(format);

		// replace "{Xnr}" with "%0Xd"
		if (m.find()) {
			nrExp = format.substring(m.start(), m.end());
			int nrExpLength = nrExp.length();
			if (nrExpLength > 4 ) {
				nrExp = "%0" + nrExp.substring(1, nrExp.length() - 3) + "d";
			}
			else {
				nrExp = "%d";
			}
		
			format = m.replaceFirst(nrExp);
		}

		// Replace the "%0Xd" with the decimal number
		nextNr = String.format(format, nr);

		// Return the string with the next free document number
		return nextNr;
	}

	protected void setNextNumber(String prefStrNr, int nr) {
		defaultValuePrefs.putInt(prefStrNr, nr);

		// Store the date of now to a property
		Calendar calendar = new GregorianCalendar();
		int yyyy = calendar.get(Calendar.YEAR);
		int mm = calendar.get(Calendar.MONTH) + 1;
		int dd = calendar.get(Calendar.DAY_OF_MONTH);
		defaultValuePrefs.put("last_setnextnr_date_" + editorID.toLowerCase(), String.format("%04d-%02d-%02d", yyyy, mm, dd));

	}
	
	
	/**
	 * Set the next free document number in the preference store. But check if
	 * the documents number is the next free one.
	 * 
	 * @param s
	 *            The document number as string.
	 * @return Errorcode, if the document number is correctly set to the next
	 *         free number.
	 */
	protected int setNextNr(String value, String key, List<T> allDataSets) {

		// Create the string of the preference store for format and number
		String prefStrFormat = "NUMBERRANGE_" + editorID.toUpperCase() + "_FORMAT";
		String prefStrNr = "NUMBERRANGE_" + editorID.toUpperCase() + "_NR";
		String format;
		String s = "";
		int nr;
		int result = ERROR_NOT_NEXT_ID;
		Integer nextnr;

		// Get the next document number from the preferences, increased be one.
		format = defaultValuePrefs.get(prefStrFormat, "NO_DEFAULT_VALUE");
		nextnr = defaultValuePrefs.getInt(prefStrNr, 1) + 1;

		// Exit, if format is empty
		if (format.trim().isEmpty())
			return NO_ERROR;

		// Fill the replacements with dummy values
		format = format.replace("{yyyy}", "0000");
		format = format.replace("{yy}", "00");
		format = format.replace("{mm}", "00");
		format = format.replace("{dd}", "00");
		format = format.replace("{YYYY}", "0000");
		format = format.replace("{YY}", "00");
		format = format.replace("{MM}", "00");
		format = format.replace("{DD}", "00");

		
		// Find the placeholder for a decimal number with n digits
		// with the format "{Xnr}", "X" is the number of digits.
		Pattern p = Pattern.compile("\\{\\d*nr\\}");
		Matcher m = p.matcher(format);
		
		// Get the next number
		if (m.find()) {
			
			// Exit, if the value is to short
			if (value.length() < m.start())
				return ERROR_NOT_NEXT_ID;

			// Exit, if the value is to short
			if ((value.length() - format.length() + m.end()) <= m.start() )
				return ERROR_NOT_NEXT_ID;

			// Extract the number string
			s = value.substring(m.start(), value.length() - format.length() + m.end());

			try {
				// Convert it to an integer and increase it by one.
				nr = Integer.parseInt(s) + 1;

				// Update the value of the last document number, but only,
				// If the number of this document is the next free number
				if (nr == nextnr) {
					
					// Store the number to the preference store
					setNextNumber(prefStrNr, nr);
					result = NO_ERROR;
				}
			}
			catch (NumberFormatException e) {
				//Logger.logError(e, "Document number invalid");
			}
		}

		// The result of the validation
		return result;
	}

	/**
	 * Refresh the view that corresponds to this editor
	 * 
	 */
	protected void refreshView() {

		// Refresh the view that corresponds to this editor
		// TODO change it!
		//ApplicationWorkbenchAdvisor.refreshView(tableViewID);

	}


	/**
	 * Request a new validation, if the document is dirty.
	 */
	public void checkDirty() {
		getMDirtyablePart().setDirty(true);
	}

	/**
	 * Supervice this text widget. Set the text limit and request a new
	 * "isDirty" validation, if the content of the text widget is modified.
	 */
	protected void superviceControl(Text text, int limit) {
		text.setTextLimit(limit);
		text.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				checkDirty();

			}
		});
	}

	/**
	 * Supervice this dateTime widget. Set the text limit and request a new
	 * "isDirty" validation, if the content of the text dateTime is modified.
	 */
	protected void superviceControl(DateTime dateTime) {
		dateTime.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				checkDirty();
			}

		});
	}

	/**
	 * Supervice this combo widget. Set the text limit and request a new
	 * "isDirty" validation, if the content of the text combo is modified.
	 */
	protected void superviceControl(Combo combo) {

		combo.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				checkDirty();

			}
		});

		combo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				checkDirty();
			}

		});
	}

	/**
	 * Jump to the next control, if in a multi-line text control the tab key is
	 * pressed. Normally the tab won't jump to the next control, if the current
	 * one is a text control. It will insert a tabulator.
	 * 
	 * @param text
	 *            This (multi-line) text control
	 * @param nextControl
	 *            The next control
	 */
	protected void setTabOrder(Text text, final Control nextControl) {
		text.addKeyListener(new KeyAdapter() {

			/**
			 * Capture the tab key and set the focus to the next control
			 * 
			 * @see org.eclipse.swt.events.KeyAdapter#keyPressed(org.eclipse.swt.events.KeyEvent)
			 */
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == '\t') {
					e.doit = false;
					nextControl.setFocus();
				}
			}

		});

	}

	/**
	 * Test before close, if the document ID is correct
	 * 
	 * @see org.eclipse.ui.ISaveablePart2#promptToSaveOnClose()
	 */
	@PersistState
	public int promptToSaveOnClose(Composite parent) {
		
		//T: Dialog Header
		MessageDialog dialog = new MessageDialog(parent.getShell(), _("Save changes"), null,
				//T: Dialog Text
				_("Save changes ?"), MessageDialog.QUESTION,
				new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL }, 0);

		final int dialogResult = dialog.open();

		if (dialogResult == 0) {
			return 0;
			// Check, if the number is unique
			/*if (thereIsOneWithSameNumber())
				return ISaveablePart2.CANCEL;
			else
				return ISaveablePart2.YES;*/
		}
		else if (dialogResult == 1) {
			return 1; // ISaveablePart2.NO;
		}
		else {
			return 2; // ISaveablePart2.CANCEL;
		}
	}

	/**
	 * Returns, if save is allowed
	 * 
	 * @return TRUE, if save is allowed
	 */
	protected boolean saveAllowed() {
		return true;
	}

}
