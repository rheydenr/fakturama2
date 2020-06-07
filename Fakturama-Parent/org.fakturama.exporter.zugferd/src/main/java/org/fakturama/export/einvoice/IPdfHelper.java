package org.fakturama.export.einvoice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.xmpbox.schema.XmpSchemaException;
import org.apache.xmpbox.xml.XmpParsingException;

public interface IPdfHelper {

    String DOCTYPE_INVOICE = "INVOICE";
    String PDFA_EXTENSION_SCHEMA_PREFIX = "pdfaSchema";
    String PDFA_EXTENSION_SCHEMA_NAMESPACE = "http://www.aiim.org/pdfa/ns/schema#";

    /**
     * Makes A PDF/A-3a-compliant document from a PDF/A-1 compliant document (on
     * the metadata level, this will not e.g. convert graphics to JPG-2000)
     * 
     * @return 
     * @throws TransformerException 
     * @throws IOException 
     * @throws XmpParsingException 
     * @throws XmpSchemaException 
     * */
    PDDocument makeA3Acompliant(String pdfFileName, ConformanceLevel level) throws IOException, TransformerException, XmpParsingException, XmpSchemaException;

    /**
     * embed the ZUGFeRD XML structure in a file named ZUGFeRD-invoice.xml
     * @throws IOException 
     * */
    PDDocument attachZugferdFile(PDDocument doc, ByteArrayOutputStream baos) throws IOException;

}