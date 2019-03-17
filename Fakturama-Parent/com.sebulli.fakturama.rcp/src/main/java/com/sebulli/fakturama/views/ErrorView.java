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

import java.util.Deque;
import java.util.LinkedList;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;

/**
 * This class represents the error view of the workbench
 * 
 */
public class ErrorView {

    // Maximum lines of the logfile / error view
    private static final int MAXLINES = 200;
    
    @Inject
    @Preference
    protected IEclipsePreferences defaultValuePrefs;
    
    @Inject
    @Translation
    protected Messages msg;
    
    @Inject
    protected ILogger log;
	
	// The top composite
	private Composite top;

	// ID of this view
	public static final String ID = "part:com.sebulli.fakturama.views.errorView";

	// The text of the view
	private Text errorText;

	/**
	 * Creates the SWT controls for this workbench part.
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
    @PostConstruct
	public void createPartControl(Composite parent) {

		// Create top composite
		top = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(top);

		// Add context help reference 
//		PlatformUI.getWorkbench().getHelpSystem().setHelp(top, ContextHelpConstants.ERROR_VIEW);

		// fill the rest of the view with the text field
		errorText = new Text(top, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(errorText);
		
		parent.setVisible(false);
	}

	/**
	 * Set the focus to the top composite.
	 * 
	 * @see com.sebulli.fakturama.editors.Editor#setFocus()
	 */
    @Focus
	public void setFocus() {
		if(top != null) 
			top.setFocus();
	}

	/**
	 * Set the error text
	 * 
	 * @param errorMessage
	 */
	public void setErrorText(String errorMessage) {
        StringBuilder newErrorString = new StringBuilder();
        // Read the existing text entries and store it in a buffer
        // with a fixed size. Only the newest lines are kept.
        String[] oldEntries;
        if(errorText.getText().isEmpty()) {
            oldEntries = new String[]{};
        } else {
            oldEntries = errorText.getText().split("\\n");
        }
        Deque<String> neu = new LinkedList<>();
        for (int i = 0; i < oldEntries.length; i++) {
            String string = StringUtils.chomp(oldEntries[i]);
            if(i > MAXLINES) {
                neu.removeFirst();
            }
            neu.add(string);
        } 
        if(!neu.isEmpty()) {
            newErrorString = new StringBuilder(StringUtils.join(neu, '\n')).append('\n');
        }
        newErrorString.append(errorMessage);
	    errorText.setText(newErrorString.toString());
	}
}
