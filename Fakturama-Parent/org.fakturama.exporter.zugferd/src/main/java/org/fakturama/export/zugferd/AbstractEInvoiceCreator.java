package org.fakturama.export.zugferd;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.money.MonetaryAmount;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.xmpbox.schema.XmpSchemaException;
import org.apache.xmpbox.xml.XmpParsingException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.fakturama.export.zugferd.modelgen.CrossIndustryDocument;

import com.sebulli.fakturama.dao.CEFACTCodeDAO;
import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.i18n.ILocaleService;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.IDateFormatterService;
import com.sebulli.fakturama.misc.INumberFormatterService;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.IDocumentAddressManager;

public abstract class AbstractEInvoiceCreator implements IEinvoiceCreator {

    @Inject
    @Translation
    protected ZFMessages msg;
    
    @Inject // node: org.fakturama.export.zugferd
    protected IPreferenceStore preferences;
    
    @Inject @org.eclipse.e4.core.di.annotations.Optional
    @Preference
    protected IEclipsePreferences eclipsePrefs;

    @Inject
    protected IEclipseContext eclipseContext;
    
    @Inject ILogger log;

    @Inject
    protected CEFACTCodeDAO measureUnits;
    
    @Inject
    protected ILocaleService localeUtil;
    
    @Inject
    protected ContactsDAO contactsDAO;
    
    @Inject
    protected INumberFormatterService numberFormatterService;

    @Inject
    protected IDateFormatterService dateFormatterService;
    
    @Inject
    protected IDocumentAddressManager addressManager;

    @Inject @org.eclipse.e4.core.di.annotations.Optional
    protected Shell shell;
    
    /** The Constant DEFAULT_PRICE_SCALE. */
    protected static final int DEFAULT_AMOUNT_SCALE = 4;

    protected static SimpleDateFormat sdfDest = new SimpleDateFormat("yyyyMMdd");
    Map<String, MonetaryAmount> netPricesPerVat = new HashMap<>();;

    /**
     * Erzeugt aus einem bereits gedruckten PDF-Dokument (PDF/A-1) und einem
     * XML-Eingabestream eine ZUGFeRD-Datei (PDF/A-3).
     * 
     * @param invoice
     * @param root
     * @param zugferdProfile
     */
    protected boolean createPdf(Document invoice, Supplier<Object> root, ConformanceLevel zugferdProfile) {
        boolean retval = true;
        String pdfFile = invoice.getPdfPath();
        PDDocument pdfa3 = null;
    
        netPricesPerVat.clear();
    
        try (ByteArrayOutputStream buffo = new ByteArrayOutputStream()) {
            // create XML from structure
            DOMResult res = new DOMResult();
            JAXBContext context = JAXBContext.newInstance(root.get().getClass());
            context.createMarshaller().marshal(root.get(), res);
            org.w3c.dom.Document zugferdXml = (org.w3c.dom.Document) res.getNode();
            printDocument(zugferdXml, buffo);
    
            PDDocument retvalPDFA3 = ZugferdHelper.makeA3Acompliant(pdfFile, zugferdProfile/*, zugferdXml, invoice.getName()*/);
    
            // embed XML
            pdfa3 = ZugferdHelper.attachZugferdFile(retvalPDFA3, buffo);
            
            if (pdfFile != null) {
                pdfa3.save(Paths.get(pdfFile).toFile());
            } else { // dialog cancelled
                retval = false;
            }
        } catch (JAXBException | IOException | TransformerException | XmpParsingException | XmpSchemaException exception) {
            log.error(exception, "error creating ZUGFeRD document: " + exception.getMessage());
            retval = false;
        } finally {
            if (pdfa3 != null) {
                try {
                    pdfa3.close();
                } catch (IOException ioex) {
                    log.error(ioex, "error closing ZUGFeRD PDF document: " + ioex.getMessage());
                }
            }
        }
        return retval;
    }
    
    private void printDocument(org.w3c.dom.Document doc, StreamResult streamResult) throws IOException, TransformerException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        transformer.transform(new DOMSource(doc), streamResult);
    }
    
    protected void printDocument(org.w3c.dom.Document doc, OutputStream out) throws IOException, TransformerException {
        StreamResult streamResult = new StreamResult(new OutputStreamWriter(out, "UTF-8"));
        printDocument(doc, streamResult);
    }
    
    /**
     * tests the generated export file (only for debugging purposes)  
     * 
     * @param root the document 
     */
    @SuppressWarnings("unused")
    private void testOutput(CrossIndustryDocument root) {
        /* * * * * * TEST ONLY!!! * * * * * */
        Path path = Paths.get("d:\\temp\\ZUGTEST.XML");
        try(BufferedWriter newBufferedWriter = Files.newBufferedWriter(path, Charset.defaultCharset(), StandardOpenOption.CREATE);) {
            
            DOMResult res = new DOMResult();
            JAXBContext testContext = JAXBContext.newInstance(root.getClass());
            testContext.createMarshaller().marshal(root, res);
            org.w3c.dom.Document doc = (org.w3c.dom.Document) res.getNode();
            printDocument(doc, new StreamResult(newBufferedWriter));
        }
        catch (JAXBException | IOException | TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /* * * * * *  END TEST ONLY!!! * * * * * */
        
        
    }

}