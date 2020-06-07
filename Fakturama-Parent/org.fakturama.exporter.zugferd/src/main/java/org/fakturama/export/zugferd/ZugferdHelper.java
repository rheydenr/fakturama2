/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 Ralf Heydenreich
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   This code was copied with friendly permission from gnuaccounting.org. 
 *   - Jochen Staerk
 */
package org.fakturama.export.zugferd;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.Optional;

import javax.xml.transform.TransformerException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentCatalog;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDDocumentNameDictionary;
import org.apache.pdfbox.pdmodel.PDEmbeddedFilesNameTreeNode;
import org.apache.pdfbox.pdmodel.common.PDMetadata;
import org.apache.pdfbox.pdmodel.common.filespecification.PDComplexFileSpecification;
import org.apache.pdfbox.pdmodel.common.filespecification.PDEmbeddedFile;
import org.apache.pdfbox.pdmodel.documentinterchange.logicalstructure.PDMarkInfo;
import org.apache.xmpbox.XMPMetadata;
import org.apache.xmpbox.XmpConstants;
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAExtensionSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.schema.XmpSchemaException;
import org.apache.xmpbox.type.ArrayProperty;
import org.apache.xmpbox.type.Attribute;
import org.apache.xmpbox.type.Cardinality;
import org.apache.xmpbox.type.ChoiceType;
import org.apache.xmpbox.type.DefinedStructuredType;
import org.apache.xmpbox.type.IntegerType;
import org.apache.xmpbox.type.PDFAPropertyType;
import org.apache.xmpbox.type.PDFASchemaType;
import org.apache.xmpbox.type.TextType;
import org.apache.xmpbox.type.TypeMapping;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;
import org.apache.xmpbox.xml.XmpSerializer;


/**
 *
 */
public class ZugferdHelper {

	private static final String ZUGFERD_PREFIX = "zf";
    private static final String ZUGFERD_URN = "urn:ferd:pdfa:invoice:rc#";
    private static final String PDFA_EXTENSION_SCHEMA_PREFIX = "pdfaSchema";
    private static final String PDFA_EXTENSION_SCHEMA_NAMESPACE = "http://www.aiim.org/pdfa/ns/schema#";

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
	public static PDDocument makeA3Acompliant(String pdfFileName, ConformanceLevel level) throws IOException, TransformerException, XmpParsingException, XmpSchemaException {
	    Path pdfFile = Paths.get(pdfFileName);
	    if(Files.notExists(pdfFile)) {
	        return null;
	    }
	    
		PDDocument doc = PDDocument.load(Files.newInputStream(pdfFile));
        PDDocumentCatalog catalog = doc.getDocumentCatalog();
        PDMetadata metadata = catalog.getMetadata();

        DomXmpParser xmpParser = new DomXmpParser();
        XMPMetadata xmp = xmpParser.parse(metadata.createInputStream());
        TypeMapping tm = new TypeMapping(xmp);
        
        DublinCoreSchema dcSchema = Optional.ofNullable(xmp.getDublinCoreSchema()).orElse(xmp.createAndAddDublinCoreSchema());
        String creator = System.getProperty("user.name"); // set current (operating system) user name as (mandatory) human creator
        dcSchema.addCreator(creator);
        dcSchema.setAboutAsSimple("");

        XMPBasicSchema basicSchema = Optional.ofNullable(xmp.getXMPBasicSchema()).orElse(xmp.createAndAddXMPBasicSchema());
        basicSchema.setAboutAsSimple("");
        basicSchema.setCreatorTool("Fakturama invoicing software");
        basicSchema.setCreateDate(GregorianCalendar.getInstance());
        
        PDDocumentInformation pdi = doc.getDocumentInformation();
        String producer = "Fakturama.org"; // (mandatory) producer application is Fakturama 
        pdi.setProducer(producer);
        pdi.setAuthor(creator);
        doc.setDocumentInformation(pdi);

        AdobePDFSchema pdfSchema = Optional.ofNullable(xmp.getAdobePDFSchema()).orElse(xmp.createAndAddAdobePDFSchema());
        pdfSchema.setProducer(producer);
        pdfSchema.setAboutAsSimple("");

        /*
         * Mandatory: PDF/A3-a is tagged PDF which has to be expressed using a
         * MarkInfo dictionary (PDF A/3 Standard sec. 6.7.2.2)*/
        PDMarkInfo markinfo = new PDMarkInfo();
        markinfo.setMarked(true);
        doc.getDocumentCatalog().setMarkInfo(markinfo);

        PDFAIdentificationSchema pdfaid = Optional.ofNullable(xmp.getPDFIdentificationSchema()).orElse(xmp.createAndAddPFAIdentificationSchema());
        TextType conf2 = (TextType) pdfaid.getProperty("conformance");
        conf2.setValue("B");
        IntegerType pdfConformance = (IntegerType) pdfaid.getProperty("part");
        pdfConformance.setValue(3);
        
        createExtensionSchema(xmp, tm);
        
        /*
         * This is what needs to be added to the RDF metadata - basically the name
         * of the embedded ZUGFeRD file
         */
        XMPSchema xmpBasicSchema = tm.getSchemaFactory("http://www.aiim.org/pdfa/ns/extension/").createXMPSchema(xmp, ZUGFERD_PREFIX);
        xmpBasicSchema.addNamespace(ZUGFERD_URN, ZUGFERD_PREFIX);
        xmpBasicSchema.setAboutAsSimple("");

        String conformanceLevel = "COMFORT";
//        if (conformanceLevel == null) {
//            conformanceLevel = CONFORMANCELEVEL;
//        }

        xmpBasicSchema.setTextPropertyValue("ConformanceLevel", conformanceLevel);
        xmpBasicSchema.setTextPropertyValue("DocumentType", "INVOICE");
        xmpBasicSchema.setTextPropertyValue("DocumentFileName", "ZUGFeRD-invoice.xml");
        xmpBasicSchema.setTextPropertyValue("Version", "1.0");
        
        XmpSerializer serializer = new XmpSerializer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        serializer.serialize(xmp, baos, true);

        PDMetadata pdMetadata = new PDMetadata(doc);
        pdMetadata.importXMPMetadata(baos.toByteArray());
        doc.getDocumentCatalog().setMetadata(pdMetadata);

        return doc;
    }

	/**
	 * Additionally to adding a RDF namespace with a indication which file
	 * attachment if ZUGFeRD, this namespace has to be described in a PDFA
	 * Extension Schema.
	 */
    private static void createExtensionSchema(XMPMetadata xmp, TypeMapping tm) {
        PDFAExtensionSchema extSchema = Optional.ofNullable(xmp.getPDFExtensionSchema()).orElse(xmp.createAndAddPDFAExtensionSchemaWithDefaultNS());
        extSchema.addNamespace(PDFA_EXTENSION_SCHEMA_NAMESPACE, PDFA_EXTENSION_SCHEMA_PREFIX);
        extSchema.addNamespace("http://www.aiim.org/pdfa/ns/property#", "pdfaProperty");

       /*
        * What we attach is basically this:
        * pdfaExtension:schemas-node
        * +--bag
        *    +--rdf:li
        *       +--some text node (multiple)
        *       +--property node
        *          +--rdf:Seq
        *             +--rdf:li (multiple) attribute node
        *                +--some attribute property description text node (multiple)
        */
        ArrayProperty newBag = extSchema.createArrayProperty("schemas", Cardinality.Bag);
        DefinedStructuredType li = new DefinedStructuredType(xmp, ZUGFERD_URN, ZUGFERD_PREFIX, XmpConstants.LIST_NAME);
        li.setAttribute(new Attribute(ZUGFERD_URN, XmpConstants.PARSE_TYPE, XmpConstants.RESOURCE_NAME));

        newBag.addProperty(li);
        extSchema.addProperty(newBag);

        TextType pdfa1 = tm.createText(PDFA_EXTENSION_SCHEMA_NAMESPACE, PDFA_EXTENSION_SCHEMA_PREFIX, PDFASchemaType.SCHEMA, "ZUGFeRD PDFA Extension Schema");
        li.addProperty(pdfa1);

        TextType obj = tm.createText(PDFA_EXTENSION_SCHEMA_NAMESPACE, PDFA_EXTENSION_SCHEMA_PREFIX, "namespaceURI", ZUGFERD_URN);
        li.addProperty(obj);
        
        TextType obj1 = tm.createText(PDFA_EXTENSION_SCHEMA_NAMESPACE, PDFA_EXTENSION_SCHEMA_PREFIX, "prefix", ZUGFERD_PREFIX);
        li.addProperty(obj1);

        ArrayProperty newSeq = tm.createArrayProperty(PDFA_EXTENSION_SCHEMA_NAMESPACE, PDFA_EXTENSION_SCHEMA_PREFIX, PDFASchemaType.PROPERTY, Cardinality.Seq);
        li.addProperty(newSeq);

        newSeq.addProperty(createProperty(xmp, "DocumentFileName", "Text", "external", "name of the embedded XML invoice file"));
        newSeq.addProperty(createProperty(xmp, "DocumentType", "Text", "external", "INVOICE"));
        newSeq.addProperty(createProperty(xmp, "Version", "Text", "external", "The actual version of the ZUGFeRD XML schema"));
        newSeq.addProperty(createProperty(xmp, "ConformanceLevel", "Text", "external", "The conformance level of the embedded ZUGFeRD data"));

        ArrayProperty newValType = tm.createArrayProperty(PDFA_EXTENSION_SCHEMA_NAMESPACE, PDFA_EXTENSION_SCHEMA_PREFIX, PDFASchemaType.VALUE_TYPE,
                Cardinality.Seq);
        li.addProperty(newValType);
    }
	
    private static PDFAPropertyType createProperty(XMPMetadata metadata, String name, String type, String category, String description) {

        TypeMapping tm = new TypeMapping(metadata);
        PDFAPropertyType li = new PDFAPropertyType(metadata);
        li.setAttribute(new Attribute( PDFA_EXTENSION_SCHEMA_NAMESPACE, XmpConstants.PARSE_TYPE, XmpConstants.RESOURCE_NAME));
        
        ChoiceType pdfa2 = tm.createChoice(li.getNamespace(), li.getPreferedPrefix(), PDFAPropertyType.NAME, name);
        li.addProperty(pdfa2);
        
        pdfa2 = tm.createChoice(li.getNamespace(), li.getPreferedPrefix(), PDFAPropertyType.VALUETYPE, type);
        li.addProperty(pdfa2);

        pdfa2 = tm.createChoice(li.getNamespace(), li.getPreferedPrefix(), PDFAPropertyType.CATEGORY, category);
        li.addProperty(pdfa2);

        pdfa2 = tm.createChoice(li.getNamespace(), li.getPreferedPrefix(), PDFAPropertyType.DESCRIPTION, description);
        li.addProperty(pdfa2);

        return li;
    }  

	/**
	 * embed the ZUGFeRD XML structure in a file named ZUGFeRD-invoice.xml
	 * @throws IOException 
	 * */
    public static PDDocument attachZugferdFile(PDDocument doc, ByteArrayOutputStream baos) throws IOException {
        
        if(doc == null) {
            return null;
        }

		String filename="ZUGFeRD-invoice.xml";
		// first create the file specification, which holds the embedded file
		PDComplexFileSpecification fs = new PDComplexFileSpecification();
		fs.setFile(filename);
        fs.setFileUnicode(filename);
        fs.setFileDescription("electronical invoice according to ZUGFeRD standard");

        COSDictionary dict = fs.getCOSObject();
		// Relation "Source" for linking with eg. catalog
		dict.setName("AFRelationship", "Alternative"); // as defined in ZUGFeRD standard
		dict.setString("UF", filename);

		// create a data stream from given byte array
		byte[] zugferdData = baos.toByteArray();
        ByteArrayInputStream fileData = new ByteArrayInputStream(zugferdData);
		PDEmbeddedFile ef = new PDEmbeddedFile(doc, fileData);
		// now lets some of the optional parameters
		ef.setSubtype("text/xml");// as defined in ZUGFeRD standard
		ef.setSize(zugferdData.length);
		ef.setCreationDate(GregorianCalendar.getInstance());
		ef.setModDate(GregorianCalendar.getInstance());

        // use both methods for backwards, cross-platform and cross-language compatibility.
        fs.setEmbeddedFile(ef);
        fs.setEmbeddedFileUnicode(ef);

        // embedded files are stored in a named tree
        PDEmbeddedFilesNameTreeNode efTree = new PDEmbeddedFilesNameTreeNode();

		// now add the entry to the embedded file tree and set in the document.
		efTree.setNames(Collections.singletonMap(filename, fs));
		PDDocumentNameDictionary names = new PDDocumentNameDictionary(
				doc.getDocumentCatalog());
		names.setEmbeddedFiles(efTree);
		doc.getDocumentCatalog().setNames(names);
		
		// AF entry (Array) in catalog with the FileSpec
		COSArray cosArray = new COSArray();
		cosArray.add(fs);
		doc.getDocumentCatalog().getCOSObject().setItem("AF", cosArray);
		return doc;
	}

}
