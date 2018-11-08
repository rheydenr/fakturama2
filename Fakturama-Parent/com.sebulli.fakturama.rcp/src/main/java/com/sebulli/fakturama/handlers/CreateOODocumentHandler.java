/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.handlers;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.BooleanUtils;
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

import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.model.BillingType;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.FakturamaModelFactory;
import com.sebulli.fakturama.model.FakturamaModelPackage;
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
    private DocumentsDAO documentsDao;

    @Inject
    private ILogger log;

    /**
     * Parameter identifier for the document
     */
	public static final String PARAM_DOCUMENT = "org.fakturama.command.printoo.document";
	
	/**
	 * Parameter identifier for the silent mode
	 */
	public static final String PARAM_SILENTMODE = "org.fakturama.command.printoo.silentmode";

	/**
	 * Parameter identifier for the template path.
	 */
	public static final String PARAM_TEMPLATEPATH = "org.fakturama.command.printoo.templatepath";
    
    @CanExecute
    public boolean canExecute(EPartService partService, @Optional @Named(PARAM_SILENTMODE) String silentMode) {
    	MPart activePart = partService.getActivePart();
        return BooleanUtils.toBoolean(silentMode) || activePart != null && activePart.getElementId().contentEquals(DocumentEditor.ID);
    }

    /**
     * Scans the template path for all templates. If a template exists, add it
     * to the list of available templates
     * 
     * @param templatePath
     *            path which is scanned
     */
    private List<Path> scanPathForTemplates(Path templatePath) {
		List<Path> templates = new ArrayList<>();
        try {
            if(Files.exists(templatePath)) {
                templates = Files.list(templatePath).filter(f -> f.getFileName().toString().toLowerCase().endsWith(OO_TEMPLATE_FILEEXTENSION))
                        .sorted(Comparator.comparing((Path p) -> p.getFileName().toString().toLowerCase()))
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            log.error(e, "Error while scanning the templates directory: " + templatePath.toString());
        }
        return templates;
    }
    
    /**
     * Collects all templates for a given {@link DocumentType}.
     * @return List of template paths
     */
    public List<Path> collectTemplates(DocumentType documentType) {
			String workspace = preferences.getString(Constants.GENERAL_WORKSPACE);
			Path templatePath1 = Paths.get(workspace, getRelativeFolder(documentType));
			Path templatePath2 = Paths.get(workspace, getLocalizedRelativeFolder(documentType));

			// If the name of the localized folder is equal to "Templates",
			// don't search 2 times.
			List<Path> templates = scanPathForTemplates(templatePath1);
			if (!templatePath1.equals(templatePath2))
				templates.addAll(scanPathForTemplates(templatePath2));
			
    	return templates;
    }

	/**
	 * <p>Run the action Search for all available templates. If there is more than
	 * one, display a menu to select one template. The content of the editor is
	 * saved before exporting it.</p>
	 * <p>If a {@link Document} is given and the handler is called with "silent mode" the template selection dialog 
	 * and all other dialogs are suppressed
	 * and the {@link Document} is printed immediately. You have to provide a template in this case.
	 * </p>
	 */
	@Execute
	public void run(Shell shell, EPartService partService, 
			@Optional @Named(PARAM_DOCUMENT) String docId,
			@Optional @Named(PARAM_TEMPLATEPATH) String templatePathString,
			@Optional @Named(PARAM_SILENTMODE) String silentModeString) throws InvocationTargetException, InterruptedException {
		Path template;
		boolean silentMode = BooleanUtils.toBoolean(silentModeString);
		if(silentMode) {
			if (docId == null || templatePathString == null) {
				log.warn("Silent flag set but no template or document given. Aborting.");
				return;
			} else {
				Document doc = documentsDao.findById(Long.parseLong(docId));
				openOODocument(doc, Paths.get(templatePathString), shell, silentMode);				
			}
		}
    	MPart activePart = partService.getActivePart();
		if (activePart != null && StringUtils.equalsIgnoreCase(activePart.getElementId(), DocumentEditor.ID)) {
			// Search in the folder "Templates" and also in the folder with the localized name
			DocumentEditor documentEditor = (DocumentEditor) activePart.getObject();
			
			if(documentEditor != null) {
				List<Path> templates = collectTemplates(documentEditor.getDocumentType());
				final List<DocumentItem> olditemsList = new ArrayList<>();
				
				// new documents need to be saved first, we don't have an id yet
				Document tmpDoc = null;
				if(documentEditor.getDocument().getId() > 0) {
					tmpDoc = documentsDao.findById(documentEditor.getDocument().getId(), true);
				}
				
				if(tmpDoc != null) {
					 olditemsList.addAll(tmpDoc.getItems());
				}
				
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
								openOODocument(documentEditor.getDocument(true), (Path) e.widget.getData(), shell, silentMode);
								// documentEditor.markAsPrinted();
								updateStockQuantity(shell, olditemsList, documentEditor.getDocument());
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
					openOODocument(documentEditor.getDocument(true), templates.get(0), shell, silentMode);
					// documentEditor.markAsPrinted();
					updateStockQuantity(shell, olditemsList, documentEditor.getDocument());
				} else {
					// Show an information dialog if no template was found
					MessageDialog.openWarning(shell, msg.dialogMessageboxTitleInfo,
							MessageFormat.format(msg.dialogPrintooNotemplate, StringUtils.join(getLocalizedRelativeFolder(documentEditor.getDocumentType()), File.separatorChar)));
				}
			} else {
				MessageDialog.openError(shell, msg.dialogMessageboxTitleError, msg.dialogPrintooErrorNoactivepart);
			}
		}
	}

	/**
	 * <p>The stock update works as follows:</p>
	 * <p>You have to collect the
	 * <ul><li>changed,</li><li>new and</li><li>deleted</li></ul>
	 * {@link DocumentItem}s. For this, we hold the previous {@link DocumentItem} list and compare it to
	 * the current one. Here we can find the updated {@link DocumentItem}s (which are available in both lists),
	 * the new {@link DocumentItem}s (which are only available in the current list) and the deleted {@link DocumentItem}s
	 * (which are only available in the old list). The resulting list is a combination of the changed and created {@link DocumentItem}s
	 * from the current list and the deleted {@link DocumentItem}s from the old list. This resulting list contains
	 * {@link DocumentItem}s which each contains a transient field (not stored to database) named <code>quantityOrigin</code>. 
	 * This field is filled like follows:
	 * <ul><li>changed {@link DocumentItem}s &rArr; <code>quantityOrigin</code> contains the old <code>quantity</code>
	 * <li>new {@link DocumentItem}s &rArr; <code>quantityOrigin</code> contains nothing (<code>null</code>)
	 * <li>deleted {@link DocumentItem}s &rArr; <code>quantityOrigin</code> contains the old <code>quantity</code>, but the
	 * <code>quantity</code> of this {@link DocumentItem} is set to <code>null</code>.
	 * </li></p>
	 * <p>
	 * <b><i>This merged list is only for checking the changed, created or deleted {@link DocumentItem}s and is NOT intended to being
	 * stored in database!</i></b>
	 * </p>
	 * @param shell 
	 * 
	 * @param olditemsList
	 * @param currentDocument
	 */
    private void updateStockQuantity(Shell shell, List<DocumentItem> olditemsList, Document currentDocument) {
    	Document tmpDocument = null;
    	boolean found = false;
    	
		// if no quantity should be used or document is not of the right BillingType we
		// can omit the stock update
		if (!isStockUpdateForCurrentDocument(currentDocument)) {
			return;
		}
    	
    	FakturamaModelFactory modelFactory = FakturamaModelPackage.MODELFACTORY;    	
    	
    	// create a temporary document
    	switch (currentDocument.getBillingType()) {
		case DELIVERY:
			tmpDocument = modelFactory.createDelivery();
			tmpDocument.setBillingType(BillingType.DELIVERY);
			break;
		case INVOICE:
			tmpDocument = modelFactory.createInvoice();
			tmpDocument.setBillingType(BillingType.INVOICE);
			break;
		case CREDIT:
			tmpDocument = modelFactory.createCredit();
			tmpDocument.setBillingType(BillingType.CREDIT);
			break;
		default:
			break;
		}
    	
    	//if no reasonable billing type ist given we can return from this method
    	if(tmpDocument == null) {
    		return;
    	}
    	
		// create a set of DocumentItems (if the same DocumentItem is used we have to
		// summarize them)
    	// Note: The key can't be the ID of the  DocumentItem since it couldn't be persisted yet.
		Map<Integer, DocumentItem> tmpDocItems = new HashMap<>();
		for (DocumentItem currentDocumentItem : currentDocument.getItems()) {
			found = false;
			// collect changed items for stock update
			for (DocumentItem oldItem : olditemsList) {
				// only items which have a changed quantity compared to the old one (comparing
				// via itemNumber)
				// Hint: don't use "Optional" class since a quantity of "0" is different from a
				// quantity of "null"!
				if (StringUtils.equalsIgnoreCase(oldItem.getItemNumber(), currentDocumentItem.getItemNumber())) {
					// item was found so we have to mark it as found
					found = true;
					
					// now check if it's changed
					if (currentDocumentItem.getQuantity() != null
							&& currentDocumentItem.getQuantity().compareTo(oldItem.getQuantity()) != 0) {

						currentDocumentItem.setOriginQuantity(oldItem.getQuantity());
						addToTmpItems(tmpDocItems, currentDocumentItem);
					}
				}
			}

			if (!found) {
// "not found" means the currentDocumentItem couldn't be found in the oldDocumentItem's list. It seems to be a new one.
// "found" means that the item was found in both lists but wasn't changed.
				tmpDocItems.put(currentDocumentItem.hashCode(), currentDocumentItem);
			}
		}
		
		// at the end collect the deleted items (but only if the document was printed before, because only then the stock was updated).
		if(currentDocument.getPrinted()) {
			for (DocumentItem oldItem : olditemsList) {
				java.util.Optional<DocumentItem> firstFound = currentDocument.getItems().stream()
						.filter(currentDocumentItem -> StringUtils.equalsIgnoreCase(oldItem.getItemNumber(), currentDocumentItem.getItemNumber()))
						.findFirst();
				if(!firstFound.isPresent()) {
					oldItem.setOriginQuantity(oldItem.getQuantity());
					oldItem.setQuantity(null);
					addToTmpItems(tmpDocItems, oldItem);
				}
			}
		}
		
		tmpDocument.setItems(tmpDocItems.entrySet().stream()
						.map(item -> item.getValue())
						.collect(Collectors.toList()));
        
        // update stock quantity
		// can't be called via HandlerService since the ParameterConverter reads the document from the database :-(
		// Therefore we have to call it manually.
       
        StockUpdateHandler stockUpdateHandler = ContextInjectionFactory.make(StockUpdateHandler.class, context);
        stockUpdateHandler.updateStockQuantity(shell, null, tmpDocument);
	}

	private boolean isStockUpdateForCurrentDocument(Document currentDocument) {
		boolean retval = false;
		switch (preferences.getString(Constants.PREFERENCES_PRODUCT_CHANGE_QTY)) {
		case Constants.PREFERENCES_PRODUCT_CHANGE_QTY_ORDER:
			retval = currentDocument.getBillingType().isORDER();
			break;
		case Constants.PREFERENCES_PRODUCT_CHANGE_QTY_DELIVERY:
			retval = currentDocument.getBillingType().isDELIVERY();
			break;
		case Constants.PREFERENCES_PRODUCT_CHANGE_QTY_INVOICE:
			retval = currentDocument.getBillingType().isINVOICE();
			break;
		default:
			break;
		}
		return retval && preferences.getBoolean(Constants.PREFERENCES_PRODUCT_USE_QUANTITY);
	}

	/**
	 * @param tmpDocItems
	 * @param itemToAdd
	 */
	private void addToTmpItems(Map<Integer, DocumentItem> tmpDocItems, DocumentItem itemToAdd) {
		if (tmpDocItems.get(itemToAdd.getItemNumber().hashCode()) != null) {
			itemToAdd.setOriginQuantity(itemToAdd.getOriginQuantity()
					+ tmpDocItems.get(itemToAdd.getItemNumber().hashCode()).getQuantity());
		}

		tmpDocItems.put(itemToAdd.getItemNumber().hashCode(), itemToAdd);
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

    private void openOODocument(final Document document, final Path template, Shell shell, boolean silentMode) {
        OfficeDocument od = ContextInjectionFactory.make(OfficeDocument.class, context);
        od.setSilentMode(silentMode);
        
        // add silent mode flag (don't put this in context because it couldn't be removed after finishing
        // which leads to unwanted side effects)
        
        try {
			if (!silentMode && od.testOpenAsExisting(document, template)) {
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
			        } else if (answer == 1) {
			            log.debug("create doc");
			            od.createDocument(true);
			        }
			    }
			} else {
			    od.setDocument(document);
			    od.setTemplate(template);
			    log.debug("open NEW doc");
			    od.createDocument(false);
			}
		} catch (FakturamaStoringException e) {
			log.error(e, "Document couldn't be created. Reason: " + e.getDescription());
			if(e.getException() != null && e.getException() instanceof FakturamaStoringException) {
				log.warn("Caused by: " + ((FakturamaStoringException)e.getException()).getDescription());
			} else {
				log.error(e.getException());
			}
			if(!silentMode) {
				MessageDialog.openError(shell, msg.dialogMessageboxTitleError, msg.dialogPrintooCantprint);
			}
		}
    }
}
