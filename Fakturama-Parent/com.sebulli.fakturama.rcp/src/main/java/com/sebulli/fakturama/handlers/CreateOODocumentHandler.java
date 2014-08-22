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

package com.sebulli.fakturama.handlers;

import javax.inject.Inject;

import org.eclipse.e4.core.services.nls.Translation;

import com.sebulli.fakturama.i18n.Messages;

/**
 * This action starts the OpenOffice exporter. If there is more than one
 * template, a menu appears and the user can select the template.
 * 
 * @author Gerd Bartelt
 */
public class CreateOODocumentHandler {
    
    @Inject
    @Translation
    protected Messages msg;

//	private List<DocumentFilename> templates = new ArrayList<DocumentFilename>();
//	private DocumentFilename template;
//	private DocumentEditor documentEditor;

	//T: Text of the action
	public final static String ACTIONTEXT = "Print as OO document"; 
//
//	
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
//
//	/**
//	 * Scans the template path for all templates. If a template exists, add it
//	 * to the list of available templates
//	 * 
//	 * @param templatePath
//	 *            path which is scanned
//	 */
//	private void scanPathForTemplates(String templatePath) {
//		File dir = new File(templatePath);
//		String[] children = dir.list();
//		if (children != null) {
//			for (int i = 0; i < children.length; i++) {
//				// Get filename of file or directory
//				DocumentFilename oOTemplateFilename = new DocumentFilename(templatePath, children[i]);
//				if (oOTemplateFilename.getExtension().equalsIgnoreCase(".ott"))
//					templates.add(oOTemplateFilename);
//			}
//		}
//	}
//
//	/**
//	 * Run the action Search for all available templates. If there is more than
//	 * one, display a menu to select one template. The content of the editor is
//	 * saved before exporting it.
//	 */
//	@Override
//	public void run() {
//
//		// cancel, if the data base is not connected.
//		if (!DataBaseConnectionState.INSTANCE.isConnected())
//			return;
//
//		// Get the active workbench window
//		final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//
//		Editor editor = (Editor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
//		if (editor != null)
//			if (editor instanceof DocumentEditor) {
//
//				// Search in the folder "Templates" and also in the folder with the localized  name
//				documentEditor = (DocumentEditor) editor;
//
//				// Exit, if there is a document with the same number
//				if (documentEditor.thereIsOneWithSameNumber())
//					return;
//
//				String workspace = Activator.getDefault().getPreferenceStore().getString("GENERAL_WORKSPACE");
//				String templatePath1 = workspace + DocumentFilename.getRelativeFolder(documentEditor.getDocumentType());
//				String templatePath2 = workspace + DocumentFilename.getLocalizedRelativeFolder(documentEditor.getDocumentType());
//
//				// Clear the list before adding new entries
//				templates.clear();
//
//				// If the name of the localized folder is equal to "Templates", don't search 2 times.
//				scanPathForTemplates(templatePath1);
//				if (!templatePath1.equals(templatePath2))
//					scanPathForTemplates(templatePath2);
//
//				// If more than 1 template is found, show a pup up menu
//				if (templates.size() > 1) {
//					Menu menu = new Menu(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), SWT.POP_UP);
//					for (int i = 0; i < templates.size(); i++) {
//						template = templates.get(i);
//						MenuItem item = new MenuItem(menu, SWT.PUSH);
//						item.setText(template.getName());
//						item.setData(template.getPathAndFilename());
//						item.addListener(SWT.Selection, new Listener() {
//							public void handleEvent(Event e) {
//								// save the document and open the exporter
//								documentEditor.doSave(null);
//								openOODocument(documentEditor.getDocument(), (String) e.widget.getData(),workbenchWindow.getShell());
////								documentEditor.markAsPrinted();
//							}
//						});
//					}
//
//					// Set the location of the pup up menu near to the upper left corner,
//					// but with an gap, so it should be under the tool bar icon of this action.
//					int x = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getLocation().x;
//					int y = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getLocation().y;
//					menu.setLocation(x + 80, y + 80);
//					menu.setVisible(true);
//
//				}
//				else if (templates.size() == 1) {
//					// Save the document and open the exporter
//					documentEditor.doSave(null);
//					openOODocument(documentEditor.getDocument(), templates.get(0).getPathAndFilename(), workbenchWindow.getShell());
////					documentEditor.markAsPrinted();
//				}
//				else {
//					// Show an information dialog, if no template was found
//					Workspace.showMessageBox(SWT.ICON_WARNING | SWT.OK,
//							//T: Title of the dialog 
//							_("Information"),
//							//T: Text of the dialog
//							"No OpenOffice.org *.ott template found in:\n" + templatePath1);
//				}
//			}
//	}
//	
//	
//	private void openOODocument(final DataSetDocument document, final String template, Shell shell) {
//
//		
//		if (OfficeDocument.testOpenAsExisting(document, template)) {
//			// Show an information dialog, if no template was found
//			int answer = Workspace.showMessageBox(SWT.ICON_QUESTION | SWT.CANCEL | SWT.YES | SWT.NO,
//			//T: Title of the dialog 
//			_("Information"),
//			//T: Text of the dialog
//			_("Document has been already printed. Should it be reopened ?\nYES: Reopen it.\nNO: Create a new one.")
//			);
//
//			if (answer == SWT.YES)
//				OfficeManager.INSTANCE.openOODocument(document, template, false);
//			if (answer == SWT.NO)
//				OfficeManager.INSTANCE.openOODocument(document, template, true);
//		}
//		else
//			OfficeManager.INSTANCE.openOODocument(document, template, false);
//	
//	}
}
