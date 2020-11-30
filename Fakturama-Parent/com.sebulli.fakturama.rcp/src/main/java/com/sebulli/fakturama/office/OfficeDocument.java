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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Shell;
import org.odftoolkit.odfdom.dom.element.table.TableCoveredTableCellElement;
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
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ibm.icu.text.NumberFormat;
import com.sebulli.fakturama.Activator;
import com.sebulli.fakturama.calculate.DocumentSummaryCalculator;
import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.dao.DocumentReceiverDAO;
import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.dto.Price;
import com.sebulli.fakturama.dto.VatSummaryItem;
import com.sebulli.fakturama.dto.VatSummarySetManager;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DataUtils;
import com.sebulli.fakturama.misc.IDateFormatterService;
import com.sebulli.fakturama.misc.INumberFormatterService;
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
    
    @Inject
    private DocumentReceiverDAO documentReceiverDao;

    @Inject
    private ILocaleService localeUtil;
    
    @Inject
    private INumberFormatterService numberFormatterService;

    @Inject
    private IDateFormatterService dateFormatterService;

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
     * Checks if the current editor uses sales equalization tax (this is only needed for some customers).
     */
    private boolean useSET = false;

    /**
     * background processing, default is FALSE
     */
    private boolean silentMode = false;

    private Path documentPath;

    private Path generatedPdf;
    
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
                Set<PathOption> pathOptions = Stream.of(PathOption.values()).collect(Collectors.toSet());
                template = fo.getDocumentPath(pathOptions, TargetFormat.ODT, document);
            }

            // Stop here and do not fill the document's placeholders, if it's an existing document
            if (openExisting) {
                documentPath = Paths.get(document.getOdtPath());
                if(document.getPdfPath() != null) {
                    generatedPdf = Paths.get(document.getPdfPath());
                }
                openDocument();
                return;
            }
            
            // check if we have to use sales equalization tax
            setUseSalesEquationTaxForDocument(document);

            // remove previous images            
            cleanup();
            
            // Recalculate the sum of the document before exporting
            DocumentSummaryCalculator documentSummaryCalculator = ContextInjectionFactory.make(DocumentSummaryCalculator.class, context);
            documentSummary = documentSummaryCalculator.calculate(this.document);

            // Get the VAT summary of the UniDataSet document
            VatSummarySetManager vatSummarySetManager = ContextInjectionFactory.make(VatSummarySetManager.class, context);
            vatSummarySetManager.add(this.document, Double.valueOf(1.0));

            /* Get the placeholders of the OpenOffice template.
             * The scanning of all placeholders to find the item and the vat table
             * is also done here.
             */
            TextDocument textdoc = (TextDocument) org.odftoolkit.simple.Document.loadDocument(template.toFile());
            textdoc.changeMode(TextDocument.OdfMediaType.TEXT);
            PlaceholderNavigation navi = new PlaceholderNavigation()
                            .of(textdoc)
                            .withDelimiters(true)
                            .withTableIdentifiers(
                                    PlaceholderTableType.ITEMS_TABLE, 
                                    PlaceholderTableType.VATLIST_TABLE, 
                                    PlaceholderTableType.SALESEQUALIZATIONTAX_TABLE)
                            .build();
            List<PlaceholderNode> placeholderNodes = Collections.unmodifiableList(navi.getPlaceHolders());

            // Create a new ArrayList with all placeholders
            // Collect all placeholders
            allPlaceholders = placeholderNodes.stream().map(pn -> pn.getNodeText()).collect(Collectors.toList());
            
//          // TEST ONLY *********************************************
//          for (PlaceholderNode placeholderNode : placeholderNodes) {
//                System.out.println(placeholderNode.getNodeText() + " is child of " + placeholderNode.getNode().getParentNode());
//            }
            
            // Fill the property list with the placeholder values
            properties = new Properties();
            setCommonProperties();

            // A reference to the item and vat table
            Set<String> processedTables = new HashSet<>();

            // Get the items of the UniDataSet document
            List<DocumentItem> itemDataSets = document.getItems().stream().sorted((i1, i2) -> {return i1.getPosNr().compareTo(i2.getPosNr());})
                    .collect(Collectors.toList());
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
                        
                        // ah, but wait: sometimes the DEPOSIT placeholder isn't placed in a table...
                        if(row != null) {
                            nodesMarkedForRemoving.add(row);
                        }
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
                            fillVatTableWithData(vatSummarySetManager, pTable, pRowTemplate,
                                    placeholderNode.getTableType(), false);
                            break;
                          
                        case SALESEQUALIZATIONTAX_TABLE:
                            fillVatTableWithData(vatSummarySetManager, pTable, pRowTemplate, placeholderNode.getTableType(), true);
                            break;
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

            // Save the document
            if(saveOODocument(textdoc)) {
                openDocument();
            }
        }
        catch (Exception e) {
            log.error(e, "Error starting OpenOffice with " + template.getFileName());
            throw new FakturamaStoringException("Error starting OpenOffice with " + template.getFileName(), e);
        }
    }

    /**
     * Opens the finally created document(s). Depends on preferences (ODT, PDF or both of them).
     */
    private void openDocument() {
        List<String> messages = new ArrayList<>();
        if(!silentMode) {
            if(preferences.getString(Constants.PREFERENCES_OPENOFFICE_ODT_PDF).contains(TargetFormat.ODT.getPrefId())) {
                if(preferences.getBoolean(Constants.PREFERENCES_OPENOFFICE_START_IN_NEW_THREAD) 
                && documentPath != null) {
                    sync.asyncExec(() -> {
                        if(!Program.launch(documentPath.toString())) {
                            MessageDialog.openError(shell, msg.dialogMessageboxTitleError, "Document was created but can't find a viewer for OpenOffice document.");
                        }
                    });
                } else {
                    messages.add(msg.dialogPrintooSuccessful);
                }
            }
            
            if(preferences.getString(Constants.PREFERENCES_OPENOFFICE_ODT_PDF).contains(TargetFormat.PDF.getPrefId())) {
                if(generatedPdf != null) {
                    if (preferences.getBoolean(Constants.PREFERENCES_OPENPDF)) {
                        sync.asyncExec(() -> {
                            if(!Program.launch(generatedPdf.toString())) {
                                MessageDialog.openError(shell, msg.dialogMessageboxTitleError, "Document was created but can't find a viewer for PDF.");
                            }
                        });
                    } else {
                        messages.add(msg.dialogPrintooPdfsuccessful);
                    }
                }
            }
            
            if (!messages.isEmpty() && !preferences.getString(Constants.DISPLAY_SUCCESSFUL_PRINTING).contentEquals(MessageDialogWithToggle.ALWAYS)) {
                MessageDialogWithToggle.openInformation(shell, msg.dialogMessageboxTitleInfo, String.join("\n", messages), 
                        null, false, preferences, Constants.DISPLAY_SUCCESSFUL_PRINTING);
            }
        }
    }

    /**
     * Save an OpenOffice document as *.odt and as *.pdf
     * 
     * @param textdoc
     *            The document
     */
    private boolean saveOODocument(TextDocument textdoc) throws FakturamaStoringException {
        generatedPdf = null;
        Set<PathOption> pathOptions = new HashSet<>(Arrays.asList(PathOption.values()));

        boolean wasSaved = false;
        textdoc.getOfficeMetadata().setCreator(msg.applicationName);
        textdoc.getOfficeMetadata().setTitle(String.format("%s - %s", document.getBillingType().getName(), document.getName()));

        documentPath = fo.getDocumentPath(pathOptions, TargetFormat.ODT, document);
        Path origFileName = documentPath.getFileName();
        if (preferences.getString(Constants.PREFERENCES_OPENOFFICE_ODT_PDF).contains(TargetFormat.ODT.getPrefId())) {

            // Create the directories, if they don't exist.
            createOutputDirectory(documentPath.getParent());

            try (OutputStream fs = Files.newOutputStream(documentPath);) {

                // Save the document
                textdoc.save(fs);
                wasSaved = true;
            } catch (Exception e) {
                log.error(e, "Error saving the OpenOffice document");
                throw new FakturamaStoringException("Error saving the OpenOffice document with template " + template.getFileName() + ". Check if target file is opened.", e);
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
                throw new FakturamaStoringException("Error saving the temporary OpenOffice document with template " + template.getFileName() + ". Check if target file is opened.", e);
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
            }
        }
        
        // copy the PDF to the additional directory
        if (generatedPdf != null && !preferences.getString(Constants.PREFERENCES_ADDITIONAL_OPENOFFICE_PDF_PATH_FORMAT).isEmpty()) {
            documentPath = fo.getDocumentPath(pathOptions, TargetFormat.ADDITIONAL_PDF, document);
            try {
                if (Files.notExists(documentPath.getParent())) {
                    Files.createDirectories(documentPath.getParent());
                }
                Files.copy(generatedPdf, documentPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                log.error(e);
            }
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

            document = documentsDAO.save(document);                

            // run PDF post processors, if any
            postProcess();
            
            // Refresh the table view of all documents
            evtBroker.post(DocumentEditor.EDITOR_ID, "update");
        }
        
        return wasSaved;
    }

    private void postProcess() {
        boolean result = true;
        if (document.getPdfPath() != null && Files.exists(Paths.get(document.getPdfPath()))) {
            try {
                Collection<ServiceReference<IPdfPostProcessor>> serviceReferences = Activator.getContext().getServiceReferences(IPdfPostProcessor.class, null);
                if(serviceReferences.isEmpty()) {
                    log.info("no post processors found");
                }
                
                context.set(Shell.class, shell);
                for (ServiceReference<IPdfPostProcessor> serviceReference : serviceReferences) {
                    // enrich post processor service with available Eclipse services
                    IPdfPostProcessor currentProcessor = Activator.getContext().getService(serviceReference);
                    ContextInjectionFactory.inject(currentProcessor, context);
                    if (currentProcessor.canProcess()) {
                        result = result && currentProcessor.processPdf(Optional.ofNullable(document));
                    }
                }
                
                if(!result) {
                    generatedPdf = null; // so that a message is displayed that something was wrong
                }
            } catch (InvalidSyntaxException e) {
                log.error(String.format("PDF post processor couldn't be started. Reason: %s", e.getMessage()));
            }
        }
    }

    private void cleanup() throws IOException {
        // remove temp images
        final PathMatcher pathMatcher = FileSystems.getDefault().getPathMatcher(
                "glob:"+preferences.getString(Constants.GENERAL_WORKSPACE).replaceAll("\\\\", "/")+"/tmpImage*");
        
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
     * @throws FakturamaStoringException 
     */
    private Path createPdf(Path documentPath, Path origFileName, TargetFormat targetFormat) throws FakturamaStoringException {
        Path pdfFilename = null;

        // Create the directories, if they don't exist.
        createOutputDirectory(targetFormat);

        try {

            // Save the document
            OfficeStarter ooStarter = ContextInjectionFactory.make(OfficeStarter.class, context);
            Path ooPath = ooStarter.getCheckedOOPath(silentMode);
            if (ooPath != null ) {

                // now, if the file name templates are different, we have to
                // rename the pdf
                Set<PathOption> pathOptions = Stream.of(PathOption.values()).collect(Collectors.toSet());
                pdfFilename = fo.getDocumentPath(pathOptions, targetFormat, document);

                ProcessBuilder pb = new ProcessBuilder(ooPath.toString(),"--headless", 
                        "--convert-to", "pdf:writer_pdf_Export", 
                        "--outdir", pdfFilename.getParent().toString(), // this is the PDF path
                        documentPath.toAbsolutePath().toString());

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
                if(!silentMode){
                    MessageDialog.openError(shell, msg.dialogMessageboxTitleError, "Can't create PDF!");
                } else {
                    log.warn("Can't create PDF! Did you set the right OpenOffice path?");
                }
            }
        } catch (FileSystemException e) {
            throw new FakturamaStoringException("kann PDF nicht schreiben. Ist die Datei evtl. geöffnet?", e);
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
     * @param placeholderTableType current placeholder type
     * @param skipIfEmpty skips the row creation if value of sales equalization tax is empty (only for this case!)
     */
    private void fillVatTableWithData(VatSummarySetManager vatSummarySetManager, Table pTable, Row pRowTemplate,
            PlaceholderTableType placeholderTableType, boolean skipIfEmpty) {
        // Get all items
        int cellCount = pRowTemplate.getCellCount();
        for (VatSummaryItem vatSummaryItem : vatSummarySetManager.getVatSummaryItems()) {
            if(skipIfEmpty && (!this.useSET || vatSummaryItem.getSalesEqTaxPercent() == null || vatSummaryItem.getSalesEqTaxPercent().equals(Double.valueOf(0.0)))) { // skip empty rows
                continue;
            }
            
            // clone one row from template
            TableTableRowElement newRowElement = (TableTableRowElement) pRowTemplate.getOdfElement().cloneNode(true);
            // we always insert only ONE row to the table
            Row tmpRow = pTable.insertRowsBefore(pRowTemplate.getRowIndex(), 1).get(0);
//          Row tmpRow = pTable.appendRow();  // don't know yet why the row was appended instead of inserted...
            pTable.getOdfElement().replaceChild(newRowElement, tmpRow.getOdfElement());
            Row newRow = Row.getInstance(newRowElement);
            // find all placeholders within row
            for (int j = 0; j < cellCount; j++) {
                // System.out.print(".");
                // a template cell
                Cell currentCell;
                
                // temp index for columns
                int tmpIdx = j;
                do {
                    // Attention: Skip covered (spanned) cells!
                    currentCell = newRow.getCellByIndex(tmpIdx++);
                } while(currentCell.getOdfElement() instanceof TableCoveredTableCellElement);
                // correct for later use
                tmpIdx--;
                
                // make a copy of the template cell
                Element cellNode = (TableTableCellElementBase) currentCell.getOdfElement().cloneNode(true);

                // find all placeholders in a cell
                NodeList cellPlaceholders = cellNode
                        .getElementsByTagName(TextPlaceholderElement.ELEMENT_NAME.getQName());

                /*
                 * The appended row only has default cells (without styles
                 * etc.). Therefore we have to take the template cell and
                 * replace the current cell (the real cell!) with it.
                 */
                newRow.getOdfElement().replaceChild(cellNode, newRow.getCellByIndex(tmpIdx).getOdfElement());
                // replace placeholders in this cell with current content
                int countOfPlaceholders = cellPlaceholders.getLength();
                for (int k = 0; k < countOfPlaceholders; k++) {
                    Node item = cellPlaceholders.item(0);
                    PlaceholderNode cellPlaceholder = new PlaceholderNode(item);
                    switch (placeholderTableType) {
                    case VATLIST_TABLE:
                        fillVatTableWithData(vatSummaryItem, cellPlaceholder);
                        break;
                    case SALESEQUALIZATIONTAX_TABLE:
                        fillSalesEqualizationTaxTableWithData(vatSummaryItem, cellPlaceholder);
                    default:
                        break;
                    }
                }
            }
            // System.out.println();
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
        String value = numberFormatterService.formatCurrency(vatSummaryItem.getVat());
        // Get the text of the column. This is to determine, if it is the column
        // with the VAT description or with the VAT value
        String textValue = "";
    
        // It's the VAT description
        if (placeholder.equals("VATLIST.DESCRIPTIONS")) {
            textValue = key;
        }
        // It's the VAT value
        else if (placeholder.equals("VATLIST.VALUES")) {
            textValue = value;
        }
        else if (placeholder.equals("VATLIST.PERCENT")) {
            textValue = numberFormatterService.DoubleToFormatedPercent(vatSummaryItem.getVatPercent());
        }
        else if (placeholder.equals("VATLIST.VATSUBTOTAL")) {
            textValue = numberFormatterService.formatCurrency(vatSummaryItem.getNet());
        }
        else {
            return null;
        }

        // Set the text
        return cellPlaceholder.replaceWith(textValue);//Matcher.quoteReplacement(textValue)

        // And also add it to the user defined text fields in the OpenOffice
        // Writer document.
//      addUserTextField(textKey, textValue, index);

    }
    

    private Node fillSalesEqualizationTaxTableWithData(VatSummaryItem vatSummaryItem, PlaceholderNode cellPlaceholder) {

        String placeholderDisplayText = cellPlaceholder.getNodeText().toUpperCase();
        String placeholder = placeholderDisplayText.substring(1, placeholderDisplayText.length() - 1);

        // Get the text of the column. This is to determine, if it is the column
        // with the VAT description or with the VAT value
        String textValue = "";

        if (this.useSET && vatSummaryItem.getSalesEqTax() != null) {
            if (placeholder.equals("SALESEQUALIZATIONTAX.VALUES")) {
                textValue = numberFormatterService.formatCurrency(vatSummaryItem.getSalesEqTax());
            } else if (placeholder.equals("SALESEQUALIZATIONTAX.PERCENT")) {
                textValue = numberFormatterService.DoubleToFormatedPercent(vatSummaryItem.getSalesEqTaxPercent());
            } else if (placeholder.equals("SALESEQUALIZATIONTAX.SUBTOTAL")) {
                textValue = numberFormatterService.formatCurrency(vatSummaryItem.getNet());
            } else {
                return null;
            }
        }

        // Set the text
        return cellPlaceholder.replaceWith(textValue);  //Matcher.quoteReplacement(textValue)
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
        for (int row = 0; row < itemDataSets.size(); row++) {
//               clone one row from template
            TableTableRowElement newRowElement = (TableTableRowElement) pRowTemplate.getOdfElement().cloneNode(true);
            // we always insert only ONE row to the table
            Row tmpRow = pTable.insertRowsBefore(pRowTemplate.getRowIndex(), 1).get(0);
            pTable.getOdfElement().replaceChild(newRowElement, tmpRow.getOdfElement());
            Row newRow = Row.getInstance(newRowElement);
            // find all placeholders within row
            int cellCount = newRowElement.getChildNodes().getLength();
            for (int j = 0; j < cellCount; j++) {
                // a template cell
                Cell currentCell = newRow.getCellByIndex(j);
                
                // skip unnecessary cells
                if(currentCell.getOdfElement() instanceof TableCoveredTableCellElement) continue;
                
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

        Price price = new Price(item, useSET);
        boolean isReplaceOptionalPrice = item.getOptional() && preferences.getBoolean(Constants.PREFERENCES_OPTIONALITEMS_REPLACE_PRICE);

        // Get the item quantity
        if (key.equals("ITEM.QUANTITY")) {
            NumberFormat numberInstance = NumberFormat.getNumberInstance(localeUtil.getDefaultLocale());
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
        
        // Get the item number
        else if (key.equals("ITEM.SUPPLIERNUMBER")) {
            value = item.getSupplierItemNumber();
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
            value = item.getVestingPeriodStart() != null ? dateFormatterService.getFormattedLocalizedDate(item.getVestingPeriodStart()) : "";
        }
        else if (key.equals("ITEM.VESTINGPERIOD.END")) {
            value = item.getVestingPeriodEnd() != null ? dateFormatterService.getFormattedLocalizedDate(item.getVestingPeriodEnd()) : "";
        }

        // Get the item description
        else if (key.equals("ITEM.DESCRIPTION")) {
            value = item.getDescription();
            // Remove pre linebreak if description is empty to avoid empty lines
            if( StringUtils.defaultString(value).isEmpty() ) {
                placeholderDisplayText = placeholderDisplayText.replaceFirst("\n<ITEM.DESCRIPTION>", "<ITEM.DESCRIPTION>");
            }
        }

        // Get the item discount in percent
        else if (key.equals("ITEM.DISCOUNT.PERCENT")) {
            value = numberFormatterService.DoubleToFormatedPercent(item.getItemRebate());
        }

        // Get the absolute item discount (gross=
        else if (key.equals("ITEM.GROSS.DISCOUNT.VALUE")) {
            value = numberFormatterService.formatCurrency(price.getUnitGrossDiscountedRounded());
        }
        
        else if (key.equals("ITEM.SALESEQUALIZATIONTAX.PERCENT") && this.useSET) {
            value = numberFormatterService.DoubleToFormatedPercent(item.getItemVat().getSalesEqualizationTax());
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
            value = numberFormatterService.formatCurrency(price.getUnitNetRounded());
        }

        // Get the item VAT
        else if (key.equals("ITEM.UNIT.VAT")) {
            value = numberFormatterService.formatCurrency(price.getUnitVatRounded());
        }

        // Get the item gross value
        else if (key.equals("ITEM.UNIT.GROSS")) {
            value = numberFormatterService.formatCurrency(price.getUnitGrossRounded());
        }

        // Get the discounted item net value
        else if (key.equals("ITEM.UNIT.NET.DISCOUNTED")) {
            value = numberFormatterService.formatCurrency(price.getUnitNetDiscountedRounded());
        }

        // Get the discounted item VAT
        else if (key.equals("ITEM.UNIT.VAT.DISCOUNTED")) {
            value = numberFormatterService.formatCurrency(price.getUnitVatDiscountedRounded());
        }

        // Get the discounted item gross value
        else if (key.equals("ITEM.UNIT.GROSS.DISCOUNTED")) {
            value = numberFormatterService.formatCurrency(price.getUnitGrossDiscountedRounded());
        }

        // Get the total net value
        else if (key.equals("ITEM.TOTAL.NET")) {
            if (isReplaceOptionalPrice ) {
                value = preferences.getString(Constants.PREFERENCES_OPTIONALITEMS_PRICE_REPLACEMENT);
                if(value.contains("{}")) {
                    value = value.replaceAll("\\{\\}", numberFormatterService.formatCurrency(price.getUnitNetDiscounted().multiply(item.getQuantity())));
                }
            } else {
                value = numberFormatterService.formatCurrency(price.getTotalNetRounded());
            }
        }

        // Get the total VAT
        else if (key.equals("ITEM.TOTAL.VAT")) {
            if (isReplaceOptionalPrice) {
                value = preferences.getString(Constants.PREFERENCES_OPTIONALITEMS_PRICE_REPLACEMENT);
                if(value.contains("{}")) {
                    value = value.replaceAll("\\{\\}", numberFormatterService.formatCurrency(price.getUnitVatDiscounted().multiply(item.getQuantity())));
                }
            } else {
                value = numberFormatterService.formatCurrency(price.getTotalVatRounded());
            }
        }

        // Get the total gross value
        else if (key.equals("ITEM.TOTAL.GROSS")) {
            if (isReplaceOptionalPrice) {
                value = preferences.getString(Constants.PREFERENCES_OPTIONALITEMS_PRICE_REPLACEMENT);
                if(value.contains("{}")) {
                    value = value.replaceAll("\\{\\}", numberFormatterService.formatCurrency(price.getUnitGrossDiscounted().multiply(item.getQuantity())));
                }
            } else {
                value = numberFormatterService.formatCurrency(price.getTotalGrossRounded());
            }
        }
        
        // Get the absolute item discount (net)
        else if (key.equals("ITEM.NET.DISCOUNT.VALUE")) {
            if (isReplaceOptionalPrice) {
                value = preferences.getString(Constants.PREFERENCES_OPTIONALITEMS_PRICE_REPLACEMENT);
                if(value.contains("{}")) {
                    value = value.replaceAll("\\{\\}", numberFormatterService.formatCurrency(price.getUnitNet().subtract(price.getUnitNetDiscounted())));
                }
            } else {
                value = numberFormatterService.formatCurrency(price.getUnitNet().subtract(price.getUnitNetDiscounted()));
            }
        }
        
        // Get the absolute item discount (gross)
        else if (key.equals("ITEM.GROSS.DISCOUNT.VALUE")) {
            if (isReplaceOptionalPrice) {
                value = preferences.getString(Constants.PREFERENCES_OPTIONALITEMS_PRICE_REPLACEMENT);
                if(value.contains("{}")) {
                    value = value.replaceAll("\\{\\}", numberFormatterService.formatCurrency(price.getUnitGross().subtract(price.getUnitGrossDiscountedRounded())));
                }
            } else {
                value = numberFormatterService.formatCurrency(price.getUnitGross().subtract(price.getUnitGrossDiscountedRounded()));
            }
        }
        
        // Get the item's VAT
        else if (key.equals("ITEM.VAT.PERCENT")) {
            value = numberFormatterService.DoubleToFormatedPercent(item.getItemVat().getTaxValue());
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
                Path workDir = null;

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
                    String imageName = "tmpImage"+RandomStringUtils.randomAlphanumeric(8);
                
                    /*
                     * Workaround: As long as the ODF toolkit can't handle images from a ByteStream
                     * we have to convert it to a temporary image and insert that into the document.
                     */
                    workDir = Paths.get(preferences.getString(Constants.GENERAL_WORKSPACE), imageName);
                    
                    // FIXME Scaling doesn't work! :-(
                    // Therefore we "scale" the image manually by setting width and height inside result document
                    
//                  java.awt.Image scaledInstance = image.getScaledInstance(pixelWidth, pixelHeight, 0);
//                  BufferedImage bi = new BufferedImage(scaledInstance.getWidth(null),
//                          scaledInstance.getHeight(null),
//                          BufferedImage.TYPE_4BYTE_ABGR);
//
//                  Graphics2D grph = (Graphics2D) bi.getGraphics();
//                  grph.scale(pictureRatio, pictureRatio);
//
//                  // everything drawn with grph from now on will get scaled.
//                  grph.drawImage(image, 0, 0, null);
//                  grph.dispose();
                    // ============================================

                    ImageIO.write(image, "jpg", workDir.toFile());
                    
                    // with NoaLibre:
//                  GraphicInfo graphicInfo = null;
//                  graphicInfo = new GraphicInfo(new FileInputStream(imagePath),
//                          pixelWidth,
//                          true,
//                          pixelHeight,
//                          true,
//                          VertOrientation.TOP,
//                          HoriOrientation.LEFT,
//                          TextContentAnchorType.AT_PARAGRAPH);
//
//                  ITextContentService textContentService = textDocument.getTextService().getTextContentService();
//                  ITextDocumentImage textDocumentImage = textContentService.constructNewImage(graphicInfo);
//                  textContentService.insertTextContent(iText.getTextCursorService().getTextCursor().getEnd(), textDocumentImage);

                    // replace the placeholder
                    return cellPlaceholder.replaceWith(workDir.toUri(), pixelWidth, pixelHeight);

                }
                catch (IOException e) {
                    log.error("Can't create temporary image file. Reason: " + e);
                } finally {
                    if(workDir != null) {
                        try {
                            Files.deleteIfExists(workDir);
                        } catch (IOException e) {
                            log.error("Can't delete temporary image file. Reason: " + e);
                        }
                    }
                }
            }
            
            value = "";
        }
        
        else if (item.getProduct() != null) {
            Product product = item.getProduct();
            // Get the item's category
            if(key.equals("ITEM.UNIT.CATEGORY")) {
                value = CommonConverter.getCategoryName(product.getCategories(), "/");
            } else if(key.equals("ITEM.UNIT.UDF01")) {
                value = product.getCdf01();
            } else if(key.equals("ITEM.UNIT.UDF02")) {
                value = product.getCdf02();
            } else if(key.equals("ITEM.UNIT.UDF03")) {
                value = product.getCdf03();
            } else if(key.equals("ITEM.UNIT.COSTPRICE")) {
                value = numberFormatterService.DoubleToFormatedPriceRound(Optional.ofNullable(product.getCostPrice()).orElse(Double.valueOf(0.0)));
            } else {
                value = "";
            }
        } else {
            value = "";
        }

        // Interpret all parameters
        value = placeholders.interpretParameters(placeholder,value);
        
        // Convert CRLF to LF 
        value = DataUtils.getInstance().convertCRLF2LF(value);

        // If iText's string is not empty, use that string instead of the template
//      String iTextString = iText.getText();
//      if (!iTextString.isEmpty()) {
//          cellText = iTextString;
//      }
        
        // Set the text of the cell
//      placeholderDisplayText = Matcher.quoteReplacement(placeholderDisplayText).replaceAll("\\{", "\\\\{").replaceAll("\\}", "\\\\}");
        return cellPlaceholder.replaceWith(value); // Matcher.quoteReplacement(value) ???

        // And also add it to the user defined text fields in the OpenOffice
        // Writer document.
//      addUserTextField(key, value, index);
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
//      addUserTextField(key, value);
        
        // Extract parameters
        for (String placeholder : allPlaceholders) {
            if ( (placeholder.equals(PlaceholderNavigation.PLACEHOLDER_PREFIX + key+ PlaceholderNavigation.PLACEHOLDER_SUFFIX)) || 
                    placeholder.startsWith(PlaceholderNavigation.PLACEHOLDER_PREFIX + key+"$") && placeholder.endsWith(PlaceholderNavigation.PLACEHOLDER_SUFFIX) ) {

                // Set the placeholder
                properties.setProperty(placeholder.toUpperCase(), placeholders.interpretParameters(placeholder, value));
            }
        }
    }
    
    /**
     * Set a common property
     * 
     * @param key
     *  Name of the placeholder
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
    *   generate one by the data, but open the existing one.
    */
    public boolean testOpenAsExisting(Document document, Path template) {
        Set<PathOption> pathOptions = Stream.of(PathOption.values()).collect(Collectors.toSet());
        Path oODocumentFile = fo.getDocumentPath(pathOptions, TargetFormat.ODT, document);
        
        boolean ignorePdf = true;
        if (preferences.getString(Constants.PREFERENCES_OPENOFFICE_ODT_PDF).contains(TargetFormat.PDF.getPrefId()) && document.getPdfPath() == null) {
            // if PDF should be created but the path in the document object is null then it has to be re-created.
            ignorePdf = false;
        }

        return (Files.exists(oODocumentFile) && BooleanUtils.isTrue(document.getPrinted()) &&
                filesAreEqual(document.getPrintTemplate(),template) && ignorePdf);
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
     *      <code>true</code>, if both are equal
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
        
        setUseSalesEquationTaxForDocument(document);
    }

    private void setUseSalesEquationTaxForDocument(Document document) {
        this.useSET = documentReceiverDao.isSETEnabled(document);
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
