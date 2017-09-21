/*
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.handlers;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.office.OfficeDocument;
import com.sebulli.fakturama.parts.DocumentEditor;

/**
 * This action starts the OpenOffice exporter. If there is more than one
 * template, a menu appears and the user can select the template.
 * 
 * @author Gerd Bartelt
 */
public class CreateOODocumentHandler {

    private static final String OO_TEMPLATE_FILEEXTENSION = ".ott";

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    protected IEclipseContext context;

    @Inject @Optional
    private IPreferenceStore preferences;

    @Inject
    private ILogger log;

    private List<Path> templates = new ArrayList<>();
    private DocumentEditor documentEditor;

    //T: Text of the action
    public final static String ACTIONTEXT = "Print as OO document";

    //	/**
    //	 * default constructor
    //	 */
    //	public CreateOODocumentHandler() {
    //		this(ACTIONTEXT, 
    //				//T: Text of the action
    //			_("Print/Export this document as an OpenOffice Writer document"));
    //	}
    //
    //	/**
    //	 * constructor
    //	 * 
    //	 * @param text
    //	 *            Action text
    //	 * @param toolTipText
    //	 *            Tool tip text
    //	 */
    //	public CreateOODocumentHandler(String text, String toolTipText) {
    //		super(text);
    //		setToolTipText(toolTipText);
    //		setId(CommandIds.CMD_CREATE_OODOCUMENT);
    //		setActionDefinitionId(CommandIds.CMD_CREATE_OODOCUMENT);
    //		setImageDescriptor(com.sebulli.fakturama.Activator.getImageDescriptor("/icons/32/oowriter_32.png"));
    //	}
    
    @CanExecute
    public boolean canExecute(EPartService partService) {
    	MPart activePart = partService.getActivePart();
        return activePart != null && activePart.getElementId().contentEquals(DocumentEditor.ID);
    }

    /**
     * Scans the template path for all templates. If a template exists, add it
     * to the list of available templates
     * 
     * @param templatePath
     *            path which is scanned
     */
    private void scanPathForTemplates(Path templatePath) {
        try {
            if(Files.exists(templatePath)) {
                templates = Files.list(templatePath).filter(f -> f.getFileName().toString().toLowerCase().endsWith(OO_TEMPLATE_FILEEXTENSION))
                        .sorted(Comparator.comparing((Path p) -> p.getFileName().toString().toLowerCase()))
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            log.error(e, "Error while scanning the templates directory: " + templatePath.toString());
        }
    }

	/**
	 * Run the action Search for all available templates. If there is more than
	 * one, display a menu to select one template. The content of the editor is
	 * saved before exporting it.
	 */
	@Execute
	public void run(Shell shell, EPartService partService) throws InvocationTargetException, InterruptedException {
		Path template;
    	MPart activePart = partService.getActivePart();
		if (activePart != null && StringUtils.equalsIgnoreCase(activePart.getElementId(), DocumentEditor.ID)) {
			// Search in the folder "Templates" and also in the folder with the
			// localized name
			documentEditor = (DocumentEditor) activePart.getObject();

			String workspace = preferences.getString(Constants.GENERAL_WORKSPACE);
			Path templatePath1 = Paths.get(workspace, getRelativeFolder(documentEditor.getDocumentType()));
			Path templatePath2 = Paths.get(workspace, getLocalizedRelativeFolder(documentEditor.getDocumentType()));

			// Clear the list before adding new entries
			templates.clear();

			// If the name of the localized folder is equal to "Templates",
			// don't search 2 times.
			scanPathForTemplates(templatePath1);
			if (!templatePath1.equals(templatePath2))
				scanPathForTemplates(templatePath2);

			// If more than 1 template is found, show a pup up menu
			if (templates.size() > 1) {
				Menu menu = new Menu(shell, SWT.POP_UP);
				for (int i = 0; i < templates.size(); i++) {
					template = templates.get(i);
					MenuItem item = new MenuItem(menu, SWT.PUSH);
					item.setText(StringUtils.substringBeforeLast(template.getFileName().toString(),
							OO_TEMPLATE_FILEEXTENSION));
					item.setData(template);
					item.addListener(SWT.Selection, new Listener() {
						public void handleEvent(Event e) {
							// save the document and open the exporter
							documentEditor.doSave(null);
							openOODocument(documentEditor.getDocument(true), (Path) e.widget.getData(), shell);
							// documentEditor.markAsPrinted();
						}
					});
				}

				// Set the location of the pop up menu near to the upper left
				// corner,
				// but with a gap, so it should be under the tool bar icon of
				// this action.
				int x = shell.getLocation().x;
				int y = shell.getLocation().y;
				menu.setLocation(x + 80, y + 80);
				menu.setVisible(true);

			} else if (templates.size() == 1) {
				// Save the document and open the exporter
				documentEditor.doSave(null);
				openOODocument(documentEditor.getDocument(true), templates.get(0), shell);
				// documentEditor.markAsPrinted();
			} else {
				// Show an information dialog if no template was found
				MessageDialog.openWarning(shell, msg.dialogMessageboxTitleInfo,
						MessageFormat.format(msg.dialogPrintooNotemplate, templatePath1));
			}
		}
	}

    /**
     * Get the relative path of the template
     * 
     * @param doctype
     *            The doctype defines the path
     * @return The path as string
     */
    public String[] getRelativeFolder(DocumentType doctype) {
        return new String[]{"/Templates", StringUtils.capitalize(doctype.getTypeAsString().toLowerCase())};
    }

    /**
     * Get the localized relative path of the template
     * 
     * @param doctype
     *            The doctype defines the path
     * @return The path as string
     */
    public String[] getLocalizedRelativeFolder(DocumentType doctype) {
        return new String[]{msg.configWorkspaceTemplatesName, msg.getMessageFromKey(DocumentType.getString(doctype))};
    }

    private void openOODocument(final Document document, final Path template, Shell shell) {
        OfficeDocument od = ContextInjectionFactory.make(OfficeDocument.class, context);
        try {
			if (od.testOpenAsExisting(document, template)) {
			    // Show an information dialog if the document was already printed
			    String[] dialogButtonLabels = new String[] { IDialogConstants.YES_LABEL, IDialogConstants.NO_LABEL, IDialogConstants.CANCEL_LABEL };
			    MessageDialog md = new MessageDialog(shell, msg.dialogMessageboxTitleInfo, null, msg.dialogPrintooDocumentalreadycreated,
			            MessageDialog.INFORMATION, dialogButtonLabels, 0);
			    int answer = md.open();
			    // Attention: The return code is the *position* of a button, not the button value itself!
			    if (md.getReturnCode() != 2) {
			        od.setDocument(document);
			        od.setTemplate(template);
			        if (answer == 0) {
			            log.debug("open doc");
			            od.createDocument(false);
			        }
			        if (answer == 1) {
			            log.debug("create doc");
			            od.createDocument(true);
			        }
			    }
			} else {
			    od.setDocument(document);
			    od.setTemplate(template);
			    log.debug("******************* open NEW doc");
			    od.createDocument(false);
			}
		} catch (FakturamaStoringException e) {
			log.error(e, "Dokument konnte nicht erstellt werden!");
			MessageDialog.openError(shell, msg.dialogMessageboxTitleError, "Dokument konnte nicht erstellt werden! Weitere Informationen finden Sie im Logfile.");
		}
    }
}
