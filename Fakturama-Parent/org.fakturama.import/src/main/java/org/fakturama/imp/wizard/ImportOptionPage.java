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

package org.fakturama.imp.wizard;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.fakturama.imp.ImportMessages;
import org.fakturama.wizards.IFakturamaWizardService;

import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.resources.core.ProgramImages;

/**
 * Create a page of the import wizard. This page is
 * used to select some options.
 * 
 */
public class ImportOptionPage extends WizardPage {
    public static final String WIZARD_TITLE = "title";
	public static final String WIZARD_DESCRIPTION = "description";
	public static final String WIZARD_PREVIEW_IMAGE = "previewimage";
	
	@Inject
	@Translation
	protected ImportMessages importMessages;
	
	@Inject
	private ILogger log;

	//Control elements
	/**
	 * Return whether existing entries should be overwritten
	 */
	private Button buttonUpdateExisting;
	
	/**
	 * Return whether empty cells should be imported
	 */
	private Button buttonUpdateWithEmptyValues;
	private Image previewImage = null;
	private Text quoteChar, separator;
	
	private ImportOptions options;
	private Text pictureBasePath;
	
	/**
	 * Constructor Create the page and set title and message.
	 */
	public ImportOptionPage(String title, String label, ProgramImages image) {
		super("ImportOptionPage");
		//T: Title of the Import Wizard Page 1
		setTitle(title);
		setMessage(label );
	}
	
	/**
	 * Default constructor. Used only for injection. <br /> 
	 * WARNING: Use <b>only</b> with injection since some
	 * initial values are set in initialize method.
	 */
	public ImportOptionPage() {
		super("ImportOptionPage");
	}
	
	@PostConstruct
	public void initialize(IEclipseContext ctx) {
		setTitle((String) ctx.get(WIZARD_TITLE));
		this.previewImage = (Image) ctx.get(IFakturamaWizardService.WIZARD_PREVIEW_IMAGE);
	}

	/**
	 * Creates the top level control for this dialog page under the given parent
	 * composite.
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		options = new ImportOptions(getDialogSettings());

		// Create the top composite
		Composite top = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(top);
		GridDataFactory.swtDefaults().align(SWT.BEGINNING, SWT.CENTER).applyTo(top);
		setControl(top);

		// Preview image
		if (previewImage != null) {
			Label preview = new Label(top, SWT.BORDER);
			preview.setText(importMessages.wizardCommonPreviewLabel);
			GridDataFactory.swtDefaults().span(3, 1).align(SWT.BEGINNING, SWT.CENTER).applyTo(preview);
			try {
				preview.setImage(previewImage);
			}
			catch (Exception e) {
				log.error(e, "Icon not found");
			}
		}
		
		// Create the label with the help text
		Label labelDescription = new Label(top, SWT.NONE);
		
		//T: Import Wizard Page 1 - Long description.
		labelDescription.setText(importMessages.wizardImportOptionsSet);
		GridDataFactory.swtDefaults().span(3, 1).align(SWT.BEGINNING, SWT.CENTER).indent(0, 10).applyTo(labelDescription);

		buttonUpdateExisting = new Button (top, SWT.CHECK);
		buttonUpdateExisting.setText(importMessages.wizardImportOptionsUpdate);
		buttonUpdateExisting.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				options.setUpdateExisting(((Button)e.getSource()).getSelection());
			}
		});
		buttonUpdateExisting.setSelection(options.getUpdateExisting());
		GridDataFactory.swtDefaults().span(3, 1).applyTo(buttonUpdateExisting);

		buttonUpdateWithEmptyValues = new Button (top, SWT.CHECK);
		buttonUpdateWithEmptyValues.setText(importMessages.wizardImportOptionsEmptyupdate);
		buttonUpdateWithEmptyValues.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				options.setUpdateWithEmptyValues(((Button)e.getSource()).getSelection());
			}
		});
		buttonUpdateWithEmptyValues.setSelection(options.getUpdateWithEmptyValues());
		GridDataFactory.swtDefaults().span(3, 1).applyTo(buttonUpdateWithEmptyValues);
		
		Label quoteCharLbl = new Label(top, SWT.NONE);
		quoteCharLbl.setText(importMessages.wizardImportOptionsQuotechar);
		GridDataFactory.swtDefaults().hint(190, SWT.DEFAULT).grab(false, false).applyTo(quoteCharLbl);
		
		quoteChar = new Text(top, SWT.BORDER);
		quoteChar.setText(options.getQuoteChar());
		quoteChar.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				options.setQuoteChar(((Text)e.getSource()).getText());
			}
		});
		GridDataFactory.swtDefaults().span(2, 1).hint(10, SWT.DEFAULT).grab(false, false).applyTo(quoteChar);
		
		Label separatorLbl = new Label(top, SWT.NONE);
		separatorLbl.setText(importMessages.wizardImportOptionsSeparator);
		separator = new Text(top, SWT.BORDER);
		separator.setText(options.getSeparator());
		separator.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				options.setSeparator(((Text)e.getSource()).getText());
			}
		});
		GridDataFactory.swtDefaults().span(2, 1).hint(10, SWT.DEFAULT).grab(false, false).applyTo(separator);
		
		Label basePathLbl = new Label(top, SWT.NONE);
		basePathLbl.setText(importMessages.wizardImportOptionsPicturebasepath);
		
		pictureBasePath = new Text(top, SWT.BORDER);
		pictureBasePath.addModifyListener(new ModifyListener() {
            
            @Override
            public void modifyText(ModifyEvent e) {
                options.setBasePath(((Text)e.getSource()).getText());
            }
        });
		if(options.getBasePath() != null) {
		    pictureBasePath.setText(options.getBasePath());
		}
        GridDataFactory.fillDefaults().grab(true, false).applyTo(pictureBasePath);
		
		Button buttonSelectDir = new Button(top, SWT.PUSH);
	    buttonSelectDir.setText("...");
	    buttonSelectDir.addSelectionListener(new SelectionAdapter() {
	    	public void widgetSelected(SelectionEvent e) {
	        DirectoryDialog directoryDialog = new DirectoryDialog(top.getShell());
	        
	        directoryDialog.setFilterPath("");
	        directoryDialog.setMessage("Please select a directory and click OK");
	        
	        String dir = directoryDialog.open();
	        if(dir != null) {
	        	pictureBasePath.setText(dir);
	        }
	      }		
	    });
	}
	
    private IDialogSettings getCurrentDialogSettings() {
        IDialogSettings section = getDialogSettings().getSection(ImportOptions.IMPORT_SETTING_OPTIONS);
        if (section == null) {
            section = getDialogSettings().addNewSection(ImportOptions.IMPORT_SETTING_OPTIONS);
        }
        return section;
    }

    public ImportOptions getImportOptions() {
        return options;
    }
    
    /**
     * Save the current dialog settings for further use.
     */
    public void saveSettings() {
        getCurrentDialogSettings().put(ImportOptions.PICTURE_BASE_PATH, pictureBasePath.getText());
        getCurrentDialogSettings().put(ImportOptions.IMPORT_CSV_SEPARATOR, separator.getText());
        getCurrentDialogSettings().put(ImportOptions.IMPORT_CSV_QUOTECHAR, quoteChar.getText());
        getCurrentDialogSettings().put(ImportOptions.IMPORT_CSV_FILENAME, options.getCsvFile());
        getCurrentDialogSettings().put(ImportOptions.IMPORT_CSV_UPDATEEXISTING, buttonUpdateExisting.getSelection());
        getCurrentDialogSettings().put(ImportOptions.IMPORT_CSV_UPDATEWITHEMPTYVALUES, buttonUpdateWithEmptyValues.getSelection());
    }
	
}
