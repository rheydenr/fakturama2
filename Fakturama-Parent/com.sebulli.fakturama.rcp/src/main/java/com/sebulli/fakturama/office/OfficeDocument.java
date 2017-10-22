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

package com.sebulli.fakturama.office;

import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileSystemException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.odftoolkit.odfdom.dom.element.table.TableTableCellElementBase;
import org.odftoolkit.odfdom.dom.element.table.TableTableRowElement;
import org.odftoolkit.odfdom.dom.element.text.TextPlaceholderElement;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.common.navigation.PlaceholderNavigation;
import org.odftoolkit.simple.common.navigation.PlaceholderNode;
import org.odftoolkit.simple.common.navigation.PlaceholderNode.PlaceholderTableType;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sebulli.fakturama.calculate.DocumentSummaryCalculator;
import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.dto.Price;
import com.sebulli.fakturama.dto.VatSummaryItem;
import com.sebulli.fakturama.dto.VatSummarySetManager;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.LocaleUtil;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.DocumentItem;
import com.sebulli.fakturama.model.Product;
import com.sebulli.fakturama.office.FileOrganizer.PathOption;
import com.sebulli.fakturama.parts.DocumentEditor;

/**
 * This class fills an OpenOffice template and replaces all the
 * placeholders with the document data.
 */
public class OfficeDocument {

	/** The UniDataSet document, that is used to fill the OpenOffice document */ 
	private Document document;

	/** A list of properties that represents the placeholders of the
	 OpenOffice Writer template */
	private Properties properties;
    
    @Inject
    private IPreferenceStore preferences;

    @Inject
    protected IEclipseContext context;

    @Inject
    private ILogger log;

    @Inject
    @Translation
    protected Messages msg;
    
    @Inject
    private DocumentsDAO documentsDAO;
    
    /**
     * Event Broker for sending update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;
    
 // get UISynchronize injected as field
    @Inject UISynchronize sync;

	/** Template name */
	private Path template;	
	
	private List<String> allPlaceholders;
    private DocumentSummary documentSummary;
    private Placeholders placeholders;
    private FileOrganizer fo;
    private Shell shell;
    
    /**
     * background processing, default is FALSE
     */
    private boolean silentMode = false;
    
    /**
     * Default constructor
     */
    public OfficeDocument() {}
	
    @PostConstruct
    public void init(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
        fo = ContextInjectionFactory.make(FileOrganizer.class, context);
        placeholders = ContextInjectionFactory.make(Placeholders.class, context);
        this.shell = shell;
    }
	
	public void createDocument(boolean forceRecreation) throws FakturamaStoringException {
		//Open an existing document instead of creating a new one
		boolean openExisting = false;

		// Try to generate the OpenOffice document
		try {
			
			// Check whether there is already a document then do not 
			// generate one by the data, but open the existing one.
			if (testOpenAsExisting(document, template) && !forceRecreation) {
				openExisting = true;
	    		Set<PathOption> pathOptions = new HashSet<>();
	            pathOptions.add(PathOption.WITH_FILENAME);
	            pathOptions.add(PathOption.WITH_EXTENSION);
				template = fo.getDocumentPath(pathOptions, TargetFormat.ODT, document);
			}

			// Stop here and do not fill the document's placeholders, if it's an existing document
			if (openExisting)
				return;

			// remove previous images            
            cleanup();
			
            // Recalculate the sum of the document before exporting
			documentSummary = new DocumentSummaryCalculator().calculate(this.document);

            /* Get the placeholders of the OpenOffice template.
             * The scanning of all placeholders to find the item and the vat table
             * is also done here.
             */
            TextDocument textdoc = (TextDocument) org.odftoolkit.simple.Document.loadDocument(template.toFile());
            textdoc.changeMode(TextDocument.OdfMediaType.TEXT);
            PlaceholderNavigation navi = new PlaceholderNavigation()
                            .of(textdoc)
                            .withDelimiters(true)
                            .withTableIdentifiers(PlaceholderTableType.ITEMS_TABLE, 
                                    PlaceholderTableType.VATLIST_TABLE)
                            .build();
            List<PlaceholderNode> placeholderNodes = Collections.unmodifiableList(navi.getPlaceHolders());

            // Create a new ArrayList with all placeholders
			// Collect all placeholders
			allPlaceholders = placeholderNodes.stream().map(pn -> pn.getNodeText()).collect(Collectors.toList());
			
//			// TEST ONLY *********************************************
//			for (PlaceholderNode placeholderNode : placeholderNodes) {
//                System.out.println(placeholderNode.getNodeText() + " is child of " + placeholderNode.getNode().getParentNode());
//            }
			
			// Fill the property list with the placeholder values
			properties = new Properties();
			setCommonProperties();

			// A reference to the item and vat table
	        Set<String> processedTables = new HashSet<>();

			// Get the items of the UniDataSet document
			List<DocumentItem> itemDataSets = document.getItems();
			Set<Node> nodesMarkedForRemoving = new HashSet<>();
			
	        for (PlaceholderNode placeholderNode : placeholderNodes) {
	            if(!StringUtils.startsWith(placeholderNode.getNodeText(), PlaceholderNavigation.PLACEHOLDER_PREFIX)) continue;
	            switch (placeholderNode.getNodeType()) {
	            case NORMAL_NODE:
	    			// Remove the discount cells, if there is no discount set
	    			// Remove the Deposit & the Finalpayment Row if there is no Deposit
	                if (StringUtils.startsWith(placeholderNode.getNodeText().substring(1), PlaceholderTableType.DISCOUNT_TABLE.getKey()) 
	                		&& documentSummary.getDiscountNet().isZero()
	                	|| StringUtils.startsWith(placeholderNode.getNodeText().substring(1), PlaceholderTableType.DEPOSIT_TABLE.getKey())
	                		&& documentSummary.getDeposit().isZero()
	                	) {
						// store parent node for later removing
						// we have to remember the parent node since the current node is replaced (could be orphaned)
						TableTableRowElement row = (TableTableRowElement)placeholderNode.findParentNode(TableTableRowElement.ELEMENT_NAME.getQName(), placeholderNode.getNode());
						nodesMarkedForRemoving.add(row);
					}
	              // Replace all other placeholders
	                replaceText(placeholderNode);
	                break;
	            case TABLE_NODE:
	            	// process only if that table wasn't processed
	            	// but wait: a table (e.g., "ITEM" table) could occur more than once!
	            	if(!processedTables.contains(placeholderNode.getNode().getUserData("TABLE_ID"))) {
		            	// get the complete row with placeholders and store it as a template
		            	Row pRowTemplate = navi.getTableRow(placeholderNode);
		            	Table pTable = pRowTemplate.getTable();
	                    // for each item from items list create a row and replace the placeholders
	                    pTable.setCellStyleInheritance(true);
	                    
	                    // which table?
	                    switch (placeholderNode.getTableType()) {
                        case ITEMS_TABLE:
                			// Fill the item table with the items
                        	/* Attention: Not only the current placeholderNode is replaced in this step,
                        	 * but also *all* other placeholders belonging to this item table!
                        	 * Therefore we have to skip all the other items placeholder in this
                        	 * table template.
                        	 * We distinguish the placeholders for a certain table by its user data field.
                        	 */
    	                    fillItemTableWithData(itemDataSets, pTable, pRowTemplate);
                            break;
                        case VATLIST_TABLE:
                            
                          // Get the VAT summary of the UniDataSet document
                          VatSummarySetManager vatSummarySetManager = new VatSummarySetManager();
                          vatSummarySetManager.add(this.document, 1.0);
                          fillVatTableWithData(vatSummarySetManager, pTable, pRowTemplate);
                          break;
//                        	
                        default:
                            break;
                        }

	                    // delete the template row from table
	                    pTable.removeRowsByIndex(pRowTemplate.getRowIndex(), 1);

	                    // determine type of this table and store it
	                    processedTables.add((String) placeholderNode.getNode().getUserData("TABLE_ID"));
	                }
	                break;

	            default:
	                break;
	            }
	        }
            
			for (Node removeNode : nodesMarkedForRemoving) {
				removeNode.getParentNode().removeChild(removeNode);
			}

//			// Save the document
			if(saveOODocument(textdoc)) {
			    MessageDialog.openInformation(shell, msg.dialogMessageboxTitleInfo, msg.dialogPrintooSuccessful);
			    
			    // TODO open Doc in OO if necessary
			} else {
                MessageDialog.openError(shell, msg.viewErrorlogName, msg.dialogPrintooCantprint);
			}

			// Print and close the OpenOffice document
			
//			textDocument.getFrame().getDispatch(GlobalCommands.PRINT_DOCUMENT_DIRECT).dispatch();
		}
		catch (Exception e) {
		    log.error(e, "Error starting OpenOffice from " + template.getFileName());
		    throw new FakturamaStoringException("Error starting OpenOffice from " + template.getFileName(), e);
		}
	}

	/**
     * Save an OpenOffice document as *.odt and as *.pdf
     * 
     * @param textdoc
     *            The document
     */
	private boolean saveOODocument(TextDocument textdoc) {
		Path generatedPdf = null;
		Set<PathOption> pathOptions = new HashSet<>();
		pathOptions.add(PathOption.WITH_FILENAME);
		pathOptions.add(PathOption.WITH_EXTENSION);

        boolean wasSaved = false;
        textdoc.getOfficeMetadata().setCreator(msg.applicationName);
        textdoc.getOfficeMetadata().setTitle(String.format("%s - %s", document.getBillingType().getName(), document.getName()));

		Path documentPath = fo.getDocumentPath(pathOptions, TargetFormat.ODT, document);
		Path origFileName = documentPath.getFileName();;
        if (preferences.getString(Constants.PREFERENCES_OPENOFFICE_ODT_PDF).contains(TargetFormat.ODT.getPrefId())) {

            // Create the directories, if they don't exist.
            createOutputDirectory(documentPath.getParent());

            try (OutputStream fs = Files.newOutputStream(documentPath);) {

                // Save the document
                textdoc.save(fs);
                wasSaved = true;
                if(preferences.getBoolean(Constants.PREFERENCES_OPENOFFICE_START_IN_NEW_THREAD) && !silentMode) {
	                // perhaps we should open the filled document in Openoffice (if wanted)
	        		try {
						Desktop.getDesktop().open(documentPath.toFile());
					} catch (IOException | IllegalArgumentException e) {
		                log.error(e, MessageFormat.format("Error opening the PDF document {0}: {1}", documentPath.toString(), e.getMessage()));
					}
                }
            } catch (Exception e) {
                log.error(e, "Error saving the OpenOffice document");
            }
        } else {
	    	// create a temporary document because the user doesn't want an ODT
        	OutputStream fs = null;
            try {
            	documentPath = Files.createTempFile(null, null);
            	documentPath.toFile().deleteOnExit();
				fs = Files.newOutputStream(documentPath);
                // Save the document
                textdoc.save(fs);
            } catch (Exception e) {
                log.error(e, "Error saving the OpenOffice document");
            } finally {
            	if(fs != null) {
            		try {
						fs.close();
					} catch (IOException e) {
		                log.error(e, "Error closing temporary OpenOffice file");
					}
            	}
            }
        }

        if (preferences.getString(Constants.PREFERENCES_OPENOFFICE_ODT_PDF).contains(TargetFormat.PDF.getPrefId())) {
        	generatedPdf = createPdf(documentPath, origFileName, TargetFormat.PDF);
        	
            // open the pdf if needed
        	if(generatedPdf != null) {
        		wasSaved = true;
        		if(preferences.getBoolean(Constants.PREFERENCES_OPENPDF) && !silentMode) {
	        		try {
						Desktop.getDesktop().open(generatedPdf.toFile());
					} catch (IOException | IllegalArgumentException e) {
		                log.error(e, MessageFormat.format("Error opening the PDF document {0}: {1}", documentPath.toString(), e.getMessage()));
					}
        		}
        	}
        }
        
        // copy the PDF to the additional directory
        if (generatedPdf != null && !preferences.getString(Constants.PREFERENCES_ADDITIONAL_OPENOFFICE_PDF_PATH_FORMAT).isEmpty()) {
        	documentPath = Paths.get(preferences.getString(Constants.PREFERENCES_ADDITIONAL_OPENOFFICE_PDF_PATH_FORMAT), 
        	        generatedPdf.getFileName().toString());
			try {
				if (Files.notExists(documentPath.getParent())) {
					Files.createDirectories(documentPath.getParent());
				}
				Files.copy(generatedPdf, documentPath, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				log.error(e);
			}
			//        	generatedPdf = createPdf(documentPath, TargetFormat.ADDITIONAL_PDF);
        }
        

        // Mark the document as printed, if it was saved as ODT or PDF
        if (wasSaved) {
            // Mark the document as "printed"
            document.setPrinted(Boolean.TRUE);
            document.setPrintTemplate(template.toString());

            if (preferences.getString(Constants.PREFERENCES_OPENOFFICE_ODT_PDF).contains(TargetFormat.ODT.getPrefId()) && Files.exists(documentPath)) {
                document.setOdtPath(documentPath.toString());
            }

            // Update the document entry "pdfpath"
            if (generatedPdf != null && Files.exists(generatedPdf)) {
                document.setPdfPath(generatedPdf.toString());
            }

            try {
                documentsDAO.update(document);                
            } catch (FakturamaStoringException e) {
                log.error(e);
            }

            // Refresh the table view of all documents
            evtBroker.post(DocumentEditor.EDITOR_ID, "update");
        }
        
        return wasSaved;
    }

	private void cleanup() throws IOException {
		// remove temp images
		final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(
				"glob:"+preferences.getString(Constants.GENERAL_WORKSPACE).replaceAll("\\\\", "/")+"tmpImage*");
		
		Files.walkFileTree(Paths.get(preferences.getString(Constants.GENERAL_WORKSPACE)), new SimpleFileVisitor<Path>() {
			
			@Override
			public FileVisitResult visitFile(Path path,
					BasicFileAttributes attrs) throws IOException {
				if (pathMatcher.matches(path)) {
					try {
						Files.deleteIfExists(path);
					} catch (FileSystemException e) {
						log.warn(String.format("temporary File couldn't be deleted! %s", e.getMessage()));
					}
				}
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFileFailed(Path file, IOException exc)
					throws IOException {
				return FileVisitResult.CONTINUE;
			}
			
			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return FileVisitResult.SKIP_SIBLINGS;
			}
		});
		
	}

	/**
	 * Creates the PDF.
	 *
	 * @param documentPath the path to the ODT document (which will be converted)
	 * @param origFileName 
	 * @param targetFormat 
	 * @return <code>true</code> if the creation was successful
	 */
	private Path createPdf(Path documentPath, Path origFileName, TargetFormat targetFormat) {
		Path pdfFilename = null;

		// Create the directories, if they don't exist.
		createOutputDirectory(targetFormat);

		try {

		    // Save the document
		    OfficeStarter ooStarter = ContextInjectionFactory.make(OfficeStarter.class, context);
		    Path ooPath = ooStarter.getCheckedOOPath();
			if (ooPath != null ) {

				// now, if the file name templates are different, we have to
				// rename the pdf
				Set<PathOption> pathOptions = new HashSet<>();
				pathOptions.add(PathOption.WITH_FILENAME);
				pathOptions.add(PathOption.WITH_EXTENSION);
				pdfFilename = fo.getDocumentPath(pathOptions, targetFormat, document);

				// FIXME How to create a PDF/A1 document?
				// PDFFilter pdfFilter = new PDFFilter();
				// pdfFilter.getPDFFilterProperties().setPdfVersion(1);

				ProcessBuilder pb = new ProcessBuilder(ooPath.toString(),"--headless", 
						"--convert-to", "pdf:writer_pdf_Export", 
						"--outdir", pdfFilename.getParent().toString(), // this is the PDF path
						documentPath.toAbsolutePath().toString());
//				pb.redirectErrorStream(true);
//				pb.inheritIO();
				Process p = pb.start();
				p.waitFor();
				
				// if we convert a temporary document the suffix is changing to ".PDF", therefore
				// we have to change the document name here
				// create a temporary filename as it would be created by PDF writer process
				Path tmpPdf = Paths.get(pdfFilename.getParent().toString(),
						documentPath.getFileName().toString().replaceAll("\\.ODT$|\\.odt$|.tmp$", TargetFormat.PDF.getExtension()));

				if (/* !Files.exists(pdfFilename) && */Files.exists(tmpPdf)) {
					pdfFilename = Files.move(tmpPdf, pdfFilename, StandardCopyOption.REPLACE_EXISTING);
				}
			} else {
				MessageDialog.openError(shell, msg.dialogMessageboxTitleError, "Can't create PDF!");
			}
		} catch (FileSystemException e) {
			System.err.println("is nich!");
		} catch (IOException e) {
		    log.error(e, "Error moving the PDF document");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pdfFilename;
	}
	
	private void createOutputDirectory(Path directory) {
		if (Files.notExists(directory)) {
		    try {
		        Files.createDirectories(directory);
		    } catch (IOException e) {
		    	log.error(e, "could not create output directory: " + directory.toString());
		    }
		}
	}

	/**
	 * Creates the output directory, if necessary.
	 *
	 * @param targetFormat the target document format
	 * @return the path that was created
	 */
	private Path createOutputDirectory(TargetFormat targetFormat) {
		Set<PathOption> pathOptions = Collections.emptySet();
		Path directory = fo.getDocumentPath(pathOptions, targetFormat, document);
		createOutputDirectory(directory);
		return directory;
	}

	
	/**
	 * Fill vat table with data.
	 *
	 * @param vatSummarySetManager the vat summary set manager
	 * @param pTable the p table
	 * @param pRowTemplate the p row template
	 */
	private void fillVatTableWithData(VatSummarySetManager vatSummarySetManager,Table pTable, Row pRowTemplate) {
        // Get all items
        int cellCount = pRowTemplate.getCellCount();
        for (VatSummaryItem vatSummaryItem : vatSummarySetManager.getVatSummaryItems()) {
//               clone one row from template
                TableTableRowElement newRowElement = (TableTableRowElement) pRowTemplate.getOdfElement().cloneNode(true);
                // we always insert only ONE row to the table
                Row tmpRow = pTable.insertRowsBefore(pRowTemplate.getRowIndex(), 1).get(0);
                pTable.getOdfElement().replaceChild(newRowElement, tmpRow.getOdfElement());
                Row newRow = Row.getInstance(newRowElement);
                // find all placeholders within row
                for (int j = 0; j < cellCount; j++) {
//                    System.out.print(".");
                    // a template cell
                    Cell currentCell = newRow.getCellByIndex(j);
                    // make a copy of the template cell
                    Element cellNode = (TableTableCellElementBase) currentCell.getOdfElement().cloneNode(true);

                    // find all placeholders in a cell
                    NodeList cellPlaceholders = cellNode.getElementsByTagName(TextPlaceholderElement.ELEMENT_NAME.getQName());

                    /*
                     * The appended row only has default cells (without styles etc.). Therefore we have to take
                     * the template cell and replace the current cell with it.
                     */
                    newRow.getOdfElement().replaceChild(cellNode, newRow.getCellByIndex(j).getOdfElement());
                    // replace placeholders in this cell with current content
                    int countOfPlaceholders = cellPlaceholders.getLength();
                    for (int k = 0; k < countOfPlaceholders; k++) {
                      Node item = cellPlaceholders.item(0);
                      PlaceholderNode cellPlaceholder = new PlaceholderNode(item);
                      fillVatTableWithData(vatSummaryItem, cellPlaceholder);
                    }
                }
//                System.out.println();
            }
	}

	/**
	 * Fill the cell of the VAT table with the VAT data
	 * 
	 * @param placeholderDisplayText
	 *            Column header
	 * @param key
	 *            VAT key (VAT description)
	 * @param value
	 *            VAT value
	 * @param iText
	 *            The Text that is set
	 * @param index
	 *            Index of the VAT entry
	 * @param cellText
	 *            The cell's text.
	 * @return 
	 */
	private Node fillVatTableWithData(VatSummaryItem vatSummaryItem, PlaceholderNode cellPlaceholder) {
        
        String placeholderDisplayText = cellPlaceholder.getNodeText().toUpperCase();
        String placeholder = placeholderDisplayText.substring(1, placeholderDisplayText.length() - 1);
        
		String key = vatSummaryItem.getVatName();
		String value = DataUtils.getInstance().formatCurrency(vatSummaryItem.getVat());
		// Get the text of the column. This is to determine, if it is the column
		// with the VAT description or with the VAT value
		String textValue;

		// It's the VAT description
		if (placeholder.equals("VATLIST.DESCRIPTIONS")) {
			textValue = key;
		}
		// It's the VAT value
		else if (placeholder.equals("VATLIST.VALUES")) {
			textValue = value;
		}
		else if (placeholder.equals("VATLIST.PERCENT")) {
			textValue = DataUtils.getInstance().DoubleToFormatedPercent(vatSummaryItem.getVatPercent());
		}
		else if (placeholder.equals("VATLIST.VATSUBTOTAL")) {
			textValue = DataUtils.getInstance().formatCurrency(vatSummaryItem.getNet());
		}
		else {
			return null;
		}
		
		// Set the text
		return cellPlaceholder.replaceWith(Matcher.quoteReplacement(textValue));

		// And also add it to the user defined text fields in the OpenOffice
		// Writer document.
//		addUserTextField(textKey, textValue, index);

	}

	/**
	 * Fill all cells of the item table with the item data
	 * 
	 * @param column
	 *            The index of the column
	 * @param itemDataSets
	 *            Item data
	 * @param itemsTable
	 *            The item table
	 * @param lastTemplateRow
	 *            Counts the last row of the table
	 * @param cellText
	 *            The cell's text.
	 */
	private void fillItemTableWithData(List<DocumentItem> itemDataSets, Table pTable, Row pRowTemplate) {
        // Get all items
        int cellCount = pRowTemplate.getCellCount();
        for (int row = 0; row < itemDataSets.size(); row++) {
//               clone one row from template
                TableTableRowElement newRowElement = (TableTableRowElement) pRowTemplate.getOdfElement().cloneNode(true);
                // we always insert only ONE row to the table
                Row tmpRow = pTable.insertRowsBefore(pRowTemplate.getRowIndex(), 1).get(0);
                pTable.getOdfElement().replaceChild(newRowElement, tmpRow.getOdfElement());
                Row newRow = Row.getInstance(newRowElement);
                // find all placeholders within row
                for (int j = 0; j < cellCount; j++) {
//                    System.out.print(".");
                    // a template cell
                    Cell currentCell = newRow.getCellByIndex(j);
                    // make a copy of the template cell
                    Element cellNode = (TableTableCellElementBase) currentCell.getOdfElement().cloneNode(true);

                    // find all placeholders in a cell
                    NodeList cellPlaceholders = cellNode.getElementsByTagName(TextPlaceholderElement.ELEMENT_NAME.getQName());

                    /*
                     * The appended row only has default cells (without styles etc.). Therefore we have to take
                     * the template cell and replace the current cell with it.
                     */
                    newRow.getOdfElement().replaceChild(cellNode, newRow.getCellByIndex(j).getOdfElement());
                    // replace placeholders in this cell with current content
                    int countOfPlaceholders = cellPlaceholders.getLength();
                    for (int k = 0; k < countOfPlaceholders; k++) {
                      Node item = cellPlaceholders.item(0);
                      PlaceholderNode cellPlaceholder = new PlaceholderNode(item);
                      fillItemTableWithData(itemDataSets.get(row), cellPlaceholder);
                    }
                }
            }
	}
	
	/**
	 * Fill the cell of the item table with the item data
	 * 
	 * @param item
	 * @param index
	 *            Index of the item entry
	 * @param cellPlaceholder
	 *            The cell's placeholder.
	 * @return 
	 */
	private Node fillItemTableWithData(DocumentItem item, PlaceholderNode cellPlaceholder) {

		String value = "";
		
        String placeholderDisplayText = cellPlaceholder.getNodeText().toUpperCase();
		String placeholder = placeholderDisplayText.substring(1, placeholderDisplayText.length() - 1);
		String key = placeholder.split("\\$")[0];

		Price price = new Price(item);

		// Get the item quantity
		if (key.equals("ITEM.QUANTITY")) {
			NumberFormat numberInstance = NumberFormat.getNumberInstance(LocaleUtil.getInstance().getDefaultLocale());
			numberInstance.setMaximumFractionDigits(10);
			value = numberInstance.format(item.getQuantity());
		}

		// The position
		else if (key.equals("ITEM.POS")) {
			value = item.getPosNr().toString();
		}

		// The text for optional items
		else if (key.equals("ITEM.OPTIONAL.TEXT")) {
			if (item.getOptional()) {
				value = preferences.getString(Constants.PREFERENCES_OPTIONALITEMS_OPTIONALITEM_TEXT);
				value = value.replaceAll("<br>", "\n");
			}
		}
		
		// Get the item name
		else if (key.equals("ITEM.NAME")) {
			value = item.getName();
		}

		// Get the item number
		else if (key.equals("ITEM.NR")) {
			value = item.getItemNumber();
		}

		// Get the quanity unit
		else if (key.equals("ITEM.QUANTITYUNIT")) {
			value = item.getQuantityUnit();
		}

		// Get the item weight
		else if (key.equals("ITEM.WEIGHT")) {
			value = item.getWeight() != null ? item.getWeight().toString() : "";
		}
		
		// Get the item weight
		else if (key.equals("ITEM.GTIN")) {
			value = item.getGtin() != null ? item.getGtin().toString() : "";
		}
		
		// vesting period
		else if (key.equals("ITEM.VESTINGPERIOD.START")) {
			value = item.getVestingPeriodStart() != null ? DataUtils.getInstance().getFormattedLocalizedDate(item.getVestingPeriodStart()) : "";
		}
		else if (key.equals("ITEM.VESTINGPERIOD.END")) {
			value = item.getVestingPeriodEnd() != null ? DataUtils.getInstance().getFormattedLocalizedDate(item.getVestingPeriodEnd()) : "";
		}

		// Get the item description
		else if (key.equals("ITEM.DESCRIPTION")) {
			value = item.getDescription();
			// Remove pre linebreak if description is empty to avoid empty lines
			if( StringUtils.defaultString(value).isEmpty() ) {
				placeholderDisplayText = placeholderDisplayText.replaceFirst("\n<ITEM.DESCRIPTION>", "<ITEM.DESCRIPTION>");
			}
		}

		// Get the item discount
		else if (key.equals("ITEM.DISCOUNT.PERCENT")) {
			value = DataUtils.getInstance().DoubleToFormatedPercent(item.getItemRebate());
		}

		// Get the item's VAT
		else if (key.equals("ITEM.VAT.PERCENT")) {
			value = DataUtils.getInstance().DoubleToFormatedPercent(item.getItemVat().getTaxValue());
		}

		// Get the item's VAT name
		else if (key.equals("ITEM.VAT.NAME")) {
			value = item.getItemVat().getName();
		}

		// Get the item's VAT description
		else if (key.equals("ITEM.VAT.DESCRIPTION")) {
			value = item.getItemVat().getDescription();
		}
		
		// Get the item net value
		else if (key.equals("ITEM.UNIT.NET")) {
		    value = DataUtils.getInstance().formatCurrency(price.getUnitNetRounded());
		}

		// Get the item VAT
		else if (key.equals("ITEM.UNIT.VAT")) {
			value = DataUtils.getInstance().formatCurrency(price.getUnitVatDiscountedRounded());
		}

		// Get the item gross value
		else if (key.equals("ITEM.UNIT.GROSS")) {
			value = DataUtils.getInstance().formatCurrency(price.getUnitGrossRounded());
		}

		// Get the discounted item net value
		else if (key.equals("ITEM.UNIT.NET.DISCOUNTED")) {
			value = DataUtils.getInstance().formatCurrency(price.getUnitNetDiscountedRounded());
		}

		// Get the discounted item VAT
		else if (key.equals("ITEM.UNIT.VAT.DISCOUNTED")) {
			value = DataUtils.getInstance().formatCurrency(price.getUnitVatDiscountedRounded());
		}

		// Get the discounted item gross value
		else if (key.equals("ITEM.UNIT.GROSS.DISCOUNTED")) {
			value = DataUtils.getInstance().formatCurrency(price.getUnitGrossDiscountedRounded());
		}

		// Get the total net value
		else if (key.equals("ITEM.TOTAL.NET")) {
			value = DataUtils.getInstance().formatCurrency(price.getTotalNetRounded());
			if (item.getOptional()) {
				if (preferences.getBoolean(Constants.PREFERENCES_OPTIONALITEMS_REPLACE_PRICE))
					value = preferences.getString(Constants.PREFERENCES_OPTIONALITEMS_PRICE_REPLACEMENT);
			}
		}

		// Get the total VAT
		else if (key.equals("ITEM.TOTAL.VAT")) {
			value = DataUtils.getInstance().formatCurrency(price.getTotalVatRounded());
			if (item.getOptional()) {
                if (preferences.getBoolean(Constants.PREFERENCES_OPTIONALITEMS_REPLACE_PRICE))
                    value = preferences.getString(Constants.PREFERENCES_OPTIONALITEMS_PRICE_REPLACEMENT);
			}
		}

		// Get the total gross value
		else if (key.equals("ITEM.TOTAL.GROSS")) {
			value = DataUtils.getInstance().formatCurrency(price.getTotalGrossRounded());
            if (item.getOptional()) {
                if (preferences.getBoolean(Constants.PREFERENCES_OPTIONALITEMS_REPLACE_PRICE))
                    value = preferences.getString(Constants.PREFERENCES_OPTIONALITEMS_PRICE_REPLACEMENT);
			}
		}
		
		// Get product picture
		else if (key.startsWith("ITEM.PICTURE")){
			
			String width_s = placeholders.extractParam(placeholder,"WIDTH");
			String height_s = placeholders.extractParam(placeholder,"HEIGHT");

			if (item.getPicture() != null) {
				// Default height and with
				int pixelWidth = 0;
				int pixelHeight = 0;

				// Use the parameter values
				try {
					pixelWidth = Integer.parseInt(width_s);
					pixelHeight = Integer.parseInt(height_s);
				}
				catch (NumberFormatException e) {
				}
				
				// Use default values
				if (pixelWidth < 1)
					pixelWidth = 150;
				if (pixelHeight < 1)
					pixelHeight = 100;

				int pictureHeight = 100;
				int pictureWidth = 100;
				double pictureRatio = 1.0;
				double pixelRatio = 1.0;
			      
				// Read the image a first time to get width and height
				try (ByteArrayInputStream imgStream = new ByteArrayInputStream(item.getPicture());) {
					
					BufferedImage image = ImageIO.read(imgStream);
					pictureHeight = image.getHeight();
					pictureWidth = image.getWidth();

					// Calculate the ratio of the original image
					if (pictureHeight > 0) {
						pictureRatio = (double)pictureWidth/(double)pictureHeight;
					}
					
					// Calculate the ratio of the placeholder
					if (pixelHeight > 0) {
						pixelRatio = (double)pixelWidth/(double)pixelHeight;
					}
					
					// Correct the height and width of the placeholder 
					// to match the original image
					if ((pictureRatio > pixelRatio) &&  (pictureRatio != 0.0)) {
						pixelHeight = (int) Math.round(((double)pixelWidth / pictureRatio));
					}
					if ((pictureRatio < pixelRatio) &&  (pictureRatio != 0.0)) {
						pixelWidth = (int) Math.round(((double)pixelHeight * pictureRatio));
					}
					
					// Generate the image
					
					/*
					 * Workaround: As long as the ODF toolkit can't handle images from a ByteStream
					 * we have to convert it to a temporary image and insert that into the document.
					 */
					Path workDir = Paths.get(preferences.getString(Constants.GENERAL_WORKSPACE), "tmpImage"+item.getDescription().hashCode());
					
					// FIXME Scaling doesn't work! :-(
					// Therefore we "scale" the image manually by setting width and height inside result document
					
//					java.awt.Image scaledInstance = image.getScaledInstance(pixelWidth, pixelHeight, 0);
//					BufferedImage bi = new BufferedImage(scaledInstance.getWidth(null),
//							scaledInstance.getHeight(null),
//							BufferedImage.TYPE_4BYTE_ABGR);
//
//			        Graphics2D grph = (Graphics2D) bi.getGraphics();
//			        grph.scale(pictureRatio, pictureRatio);
//
//			        // everything drawn with grph from now on will get scaled.
//			        grph.drawImage(image, 0, 0, null);
//			        grph.dispose();
					// ============================================

					ImageIO.write(image, "jpg", workDir.toFile());
					
					// with NoaLibre:
//					GraphicInfo graphicInfo = null;
//					graphicInfo = new GraphicInfo(new FileInputStream(imagePath),
//						    pixelWidth,
//						    true,
//						    pixelHeight,
//						    true,
//						    VertOrientation.TOP,
//						    HoriOrientation.LEFT,
//						    TextContentAnchorType.AT_PARAGRAPH);
//
//					ITextContentService textContentService = textDocument.getTextService().getTextContentService();
//					ITextDocumentImage textDocumentImage = textContentService.constructNewImage(graphicInfo);
//					textContentService.insertTextContent(iText.getTextCursorService().getTextCursor().getEnd(), textDocumentImage);

					// replace the placeholder
					return cellPlaceholder.replaceWith(workDir.toUri(), pixelWidth, pixelHeight);

				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			value = "";
		}
		
		else
			return null;

		// Get the item's category
		if (item.getProduct() != null) {
			Product product = item.getProduct();
			if(key.equals("ITEM.UNIT.CATEGORY")) {
				value = product.getCategories().getName();
			} else if(key.equals("ITEM.UNIT.UDF01")) {
				value = product.getCdf01();
			} else if(key.equals("ITEM.UNIT.UDF02")) {
				value = product.getCdf02();
			} else if(key.equals("ITEM.UNIT.UDF03")) {
				value = product.getCdf03();
			}
		}

		// Interpret all parameters
		value = placeholders.interpretParameters(placeholder,value);
		
		// Convert CRLF to LF 
		value = DataUtils.getInstance().convertCRLF2LF(value);

		// If iText's string is not empty, use that string instead of the template
//		String iTextString = iText.getText();
//		if (!iTextString.isEmpty()) {
//			cellText = iTextString;
//		}
		
		// Set the text of the cell
		placeholderDisplayText = Matcher.quoteReplacement(placeholderDisplayText).replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
		return cellPlaceholder.replaceWith(Matcher.quoteReplacement(value));

		// And also add it to the user defined text fields in the OpenOffice
		// Writer document.
//		addUserTextField(key, value, index);
	}

	/**
	 * Set a property and add it to the user defined text fields in the
	 * OpenOffice Writer document.
	 * 
	 * @param key
	 *            The property key
	 * @param value
	 *            The property value
	 */
	private void setProperty(String key, String value) {

		if (key == null || value == null)
			return;
		
		// Convert CRLF to LF 
		value = DataUtils.getInstance().convertCRLF2LF(value);
		
		// Set the user defined text field
//		addUserTextField(key, value);
		
		// Extract parameters
		for (String placeholder : allPlaceholders) {
			if ( (placeholder.equals(PlaceholderNavigation.PLACEHOLDER_PREFIX + key+ PlaceholderNavigation.PLACEHOLDER_SUFFIX)) || 
					( (placeholder.startsWith(PlaceholderNavigation.PLACEHOLDER_PREFIX + key+"$")) && (placeholder.endsWith(PlaceholderNavigation.PLACEHOLDER_SUFFIX)) ) ) {

				// Set the placeholder
				properties.setProperty(placeholder.toUpperCase(), placeholders.interpretParameters(placeholder, value));
			}
		}
	}
	
	/**
	 * Set a common property
	 * 
	 * @param key
	 * 	Name of the placeholder
	 */
	private void setCommonProperty(String key) {
		setProperty(key,placeholders.getDocumentInfo( document, documentSummary, key) );
	}
	
	
	/**
	 * Fill the property list with the placeholder values
	 */
	private void setCommonProperties() {
		if (document == null)
			return;
        
		// Get all available placeholders and set them
		for (String placeholder: placeholders.getPlaceholders()) {
			setCommonProperty(placeholder);
		}
	}

	/**
	 * Replace a placeholder with the content of the property in the property
	 * list.
	 * 
	 * @param placeholder
	 *            The placeholder and the name of the key in the property list
	 */
	private void replaceText(final PlaceholderNode placeholder) {
		// Get the placeholder's text
		String placeholderDisplayText = placeholder.getNodeText().toUpperCase();
		
		// Get the value of the Property list.
		String text = properties.getProperty(placeholderDisplayText);
		
		// If the String is non empty, replace the OS new line with the OpenOffice new line
		if(StringUtils.isNotBlank(text)){
			text = text.replaceAll("\n", "\r");
		}
		// Replace the placeholder with the value of the property list.
		log.debug(String.format("trying to replace %s with [%s]", placeholderDisplayText, text));
		placeholder.replaceWith(text);
	}
	
	/** 
	 * Check whether there is already a document then do not 
	*	generate one by the data, but open the existing one.
	*/
	public boolean testOpenAsExisting(Document document, Path template) {
		Path oODocumentFile = fo.getDocumentPath(
				FileOrganizer.WITH_FILENAME,
				FileOrganizer.WITH_EXTENSION, 
				FileOrganizer.ODT, document);

		return (Files.exists(oODocumentFile) && document.getPrinted() &&
				filesAreEqual(document.getPrintTemplate(),template));
	}

    /**
     * Tests if 2 filenames are equal.
     * Tests only the relative path and use the parameter "folder" to
     * separate the relative path from the absolute one.
     *  
     * @param fileName1
     * @param fileName2
     * @param folder
     *      The folder name to separate the relative path
     * @return
     *      True, if both are equal
     */
    private boolean filesAreEqual(String fileName1, Path fileName2, String folder) {
        
        int pos;
        String otherFileName = fileName2.toString();
        pos = fileName1.indexOf(folder);
        if (pos >= 0)
            fileName1 = fileName1.substring(pos);

        pos = fileName2.toString().indexOf(folder);
        if (pos >= 0)
            otherFileName = fileName2.toString().substring(pos);

        
        return fileName1.equals(otherFileName);
    }
    
    /**
     * Tests if 2 template filenames are equal.
     * The absolute path is ignored.
     * 
     * @param fileName1
     * @param template
     * 
     * @return
     *      True, if both filenames are equal
     */
    private boolean filesAreEqual(String fileName1, Path template) {
    	
    	if(fileName1 == null) {
    		return false;
    	}
        
        // Test, if also the absolute path is equal
        if (fileName1.equals(template.toString()))
            return true;
        
        // If not, use the unlocalized folder names
        if (filesAreEqual(fileName1,template, "/Templates/"))
            return true;

        // Use the localized folder names
        if (filesAreEqual(fileName1,template, "/" + msg.configWorkspaceTemplatesName + "/"))
            return true;
        
        return false;
    }

    /**
     * @return the document
     */
    public Document getDocument() {
        return document;
    }

    /**
     * @param document the document to set
     */
    public void setDocument(Document document) {
        this.document = document;
    }

    /**
     * @return the template
     */
    public Path getTemplate() {
        return template;
    }

    /**
     * @param template the template to set
     */
    public void setTemplate(Path template) {
        this.template = template;
    }

	/**
	 * @return the silentMode
	 */
	public boolean isSilentMode() {
		return silentMode;
	}

	/**
	 * @param silentMode the silentMode to set
	 */
	public void setSilentMode(boolean silentMode) {
		this.silentMode = silentMode;
	}

}
