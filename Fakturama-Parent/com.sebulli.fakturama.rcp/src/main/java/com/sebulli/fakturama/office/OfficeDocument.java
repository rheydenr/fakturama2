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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.BooleanUtils;
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
import org.odftoolkit.simple.TextDocument;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sebulli.fakturama.Activator;
import com.sebulli.fakturama.calculate.DocumentSummaryCalculator;
import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dto.DocumentSummary;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.Invoice;
import com.sebulli.fakturama.office.FileOrganizer.PathOption;
import com.sebulli.fakturama.parts.DocumentEditor;

public class OfficeDocument {

    /** The UniDataSet document, that is used to fill the OpenOffice document */ 
    private Document document;
    
    @Inject
    private IPreferenceStore preferences;

    @Inject
    protected IEclipseContext context;
    
    @Inject
    protected DocumentsDAO documentsDAO;

    @Inject
    private ILogger log;

    @Inject
    @Translation
    protected Messages msg;

    /**
     * Event Broker for sending update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;
    
 // get UISynchronize injected as field
    @Inject UISynchronize sync;

    /** Template name */
//    private Path template;  
    
    private DocumentSummary documentSummary;
    private FileOrganizer fo;
    private Shell shell;

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
        this.shell = shell;
    }
    
    public void createDocument(Path template, Document document, boolean forceRecreation) throws FakturamaStoringException {
        //Open an existing document instead of creating a new one
        boolean openExisting = false;
        
        this.document = document;

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

            // remove previously created images            
            cleanup();

//            // Get the VAT summary of the UniDataSet document
//            VatSummarySetManager vatSummarySetManager = ContextInjectionFactory.make(VatSummarySetManager.class, context);
//            vatSummarySetManager.add(this.document, Double.valueOf(1.0));
            
            // Recalculate the sum of the document before exporting
            DocumentSummaryCalculator documentSummaryCalculator = ContextInjectionFactory.make(DocumentSummaryCalculator.class, context);
            documentSummary = documentSummaryCalculator.calculate(this.document);

            /* Get the placeholders of the OpenOffice template.
             * The scanning of all placeholders to find the item and the vat table
             * is also done here.
             */
            TextDocument textdoc = (TextDocument) org.odftoolkit.simple.Document.loadDocument(template.toFile());
            textdoc.changeMode(TextDocument.OdfMediaType.TEXT);
            
            TemplateProcessor templateProcessor = ContextInjectionFactory.make(TemplateProcessor.class, context);
            templateProcessor.processTemplate(textdoc, document, documentSummary);

            // Save the document
            if(saveOODocument(textdoc, template)) {
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
    private boolean saveOODocument(TextDocument textdoc, Path template) throws FakturamaStoringException {
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
                    if (currentProcessor.canProcess() && document instanceof Invoice) {
                        result = result && currentProcessor.processPdf(Optional.ofNullable((Invoice)document));
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
                "glob:"+preferences.getString(Constants.GENERAL_WORKSPACE).replaceAll("\\\\", "/")+"tmpImage*");
        
        Files.walkFileTree(Paths.get(preferences.getString(Constants.GENERAL_WORKSPACE)), new SimpleFileVisitor<Path>() {
            
            @Override
            public FileVisitResult visitFile(Path path,
                    BasicFileAttributes attrs) throws IOException {
                if (pathMatcher.matches(path)) {
                    try {
                        Files.deleteIfExists(path);
                    } catch (FileSystemException e) {
                        log.warn(String.format("temporary file couldn't be deleted! %s", e.getMessage()));
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
