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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Collections;
import java.util.GregorianCalendar;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

import org.apache.pdfbox.cos.COSArray;
import org.apache.pdfbox.cos.COSDictionary;
import org.apache.pdfbox.cos.COSStream;
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
import org.apache.xmpbox.schema.AdobePDFSchema;
import org.apache.xmpbox.schema.DublinCoreSchema;
import org.apache.xmpbox.schema.PDFAIdentificationSchema;
import org.apache.xmpbox.schema.XMPBasicSchema;
import org.apache.xmpbox.schema.XMPSchema;
import org.apache.xmpbox.type.BadFieldValueException;
import org.apache.xmpbox.xml.DomXmpParser;
import org.apache.xmpbox.xml.XmpParsingException;
import org.apache.xmpbox.xml.XmpSerializer;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.core.internal.services.ResourceBundleHelper;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;

/**
 * @author rheydenr
 *
 */
public class ZugferdHelper {

	private static final String XMP_SCHEMALOCATION = "/resources/ZUGFeRD_Extension-Schema-neu.xmp";

	/**
	 * Makes A PDF/A3a-compliant document from a PDF-A1 compliant document (on
	 * the metadata level, this will not e.g. convert graphics to JPG-2000)
	 * 
	 * @return 
	 * @throws TransformerException 
	 * @throws IOException 
	 * @throws BadFieldValueException 
	 * @throws XmpParsingException 
	 * @throws XPathExpressionException 
	 * */
	public static PDDocument makeA3Acompliant(String pdfFile, ConformanceLevel level, Document zugferdXml, String docName) throws IOException, TransformerException, BadFieldValueException, XmpParsingException, XPathExpressionException {
		PDDocument doc = PDDocument.load(new File(pdfFile));
		PDDocumentCatalog cat = doc.getDocumentCatalog();
		PDMetadata metadata = new PDMetadata(doc);
		cat.setMetadata(metadata);
		XMPMetadata xmp = XMPMetadata.createXMPMetadata();

		PDFAIdentificationSchema pdfaid = new PDFAIdentificationSchema(xmp);
		pdfaid.setAboutAsSimple(""); //$NON-NLS-1$
		xmp.addSchema(pdfaid);

		DublinCoreSchema dc = xmp.createAndAddDublinCoreSchema();
		dc.setTitle(docName);
		String creator = System.getProperty("user.name"); // set current (operating system) user name as (mandatory) human creator //$NON-NLS-1$
		String producer = "Fakturama.org"; // (mandatory) producer application is Fakturama 
		dc.addCreator(creator);
		dc.setAboutAsSimple(""); //$NON-NLS-1$

		XMPBasicSchema xsb = xmp.createAndAddXMPBasicSchema();;
		xsb.setAboutAsSimple(""); //$NON-NLS-1$

		xsb.setCreatorTool("Fakturama invoicing software");
		xsb.setCreateDate(GregorianCalendar.getInstance());
		// PDDocumentInformation pdi=doc.getDocumentInformation();
		PDDocumentInformation pdi = new PDDocumentInformation();
		pdi.setProducer(producer);
		pdi.setAuthor(creator);
		doc.setDocumentInformation(pdi);

		AdobePDFSchema pdf = xmp.createAndAddAdobePDFSchema();
		pdf.setProducer(producer);
		pdf.setAboutAsSimple(""); //$NON-NLS-1$

		// Mandatory: PDF/A3-a is tagged PDF which has to be expressed using a
		// MarkInfo dictionary (PDF A/3 Standard sec. 6.7.2.2)
		PDMarkInfo markinfo = new PDMarkInfo();
		markinfo.setMarked(true);
		doc.getDocumentCatalog().setMarkInfo(markinfo);
	/*
	 * 	 
		To be on the safe side, we use level B without Markinfo because we can not 
		guarantee that the user  correctly tagged the templates for the PDF. 

	 * */
		pdfaid.setConformance("B");
		/* //$NON-NLS-1$
		 * All files are PDF/A-3, setConformance
		 * refers to the level conformance, e.g.
		 * PDF/A-3-B where B means only visually
		 * preservable, U means visually and unicode
		 * preservable and A -like in this case-
		 * means full compliance, i.e. visually,
		 * unicode and structurally preservable
		 * 
		 */
		pdfaid.setPart(3);
		
		addZugferdXMP(xmp, pdf, level, zugferdXml); /*
								 * this is the only line where we do something
								 * Zugferd-specific, i.e. add PDF metadata
								 * specifically for Zugferd, not generically for
								 * an embedded file
								 */

		OutputStream outputStreamMeta = metadata.createOutputStream();
		new XmpSerializer().serialize(xmp, outputStreamMeta, true);
		outputStreamMeta.close();
		return doc;
	}

/***
 * This will add both the RDF-indication which embedded file is Zugferd and the 
 * neccessary PDF/A schema extension description to be able to add this information to RDF 
 * @param metadata
 * @throws XmpParsingException 
 */
	private static void addZugferdXMP(XMPMetadata metadata, XMPSchema schema, ConformanceLevel level, Document doc) throws XmpParsingException {
        Bundle definingBundle = ResourceBundleHelper.getBundleForName(org.fakturama.export.zugferd.Activator.PLUGIN_ID);
		URL fileResource = FileLocator.find(definingBundle, new org.eclipse.core.runtime.Path(
				XMP_SCHEMALOCATION), null);
		try(InputStream zfExtensionIs = fileResource.openStream();) {
			DomXmpParser builder = new DomXmpParser();
			builder.setStrictParsing(true);
			XMPMetadata defaultXmp = builder.parse(zfExtensionIs);
			metadata.addSchema(defaultXmp.getPDFExtensionSchema());
			XMPSchemaZugferd zf = new XMPSchemaZugferd(metadata, level, "1.0");
			metadata.addSchema(zf);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * embed the Zugferd XML structure in a file named ZUGFeRD-invoice.xml
	 * @throws IOException 
	 * */
public static PDDocument attachZugferdFile(PDDocument doc, byte[] data) throws IOException {

		// embedded files are stored in a named tree
		PDEmbeddedFilesNameTreeNode efTree = new PDEmbeddedFilesNameTreeNode();

		// first create the file specification, which holds the embedded file
		PDComplexFileSpecification fs = new PDComplexFileSpecification();
		fs.setFile(XMPSchemaZugferd.ZUGFERD_XML_DEFAULT_NAME);

		COSDictionary dict = fs.getCOSObject();
		// Relation "Source" for linking with eg. catalog
		dict.setName("AFRelationship", "Alternative"); // as defined in Zugferd standard //$NON-NLS-1$ //$NON-NLS-2$

		dict.setString("UF", XMPSchemaZugferd.ZUGFERD_XML_DEFAULT_NAME); //$NON-NLS-1$
		dict.setString("Desc", "(ZUGFeRD-Rechnung)"); //$NON-NLS-1$

		// create a dummy file stream, this would probably normally be a
		// FileInputStream
		
		ByteArrayInputStream fakeFile = new ByteArrayInputStream(data);
		PDEmbeddedFile ef = new PDEmbeddedFile(doc, fakeFile);
		// now lets some of the optional parameters
		ef.setSubtype("text/xml");// as defined in Zugferd standard //$NON-NLS-1$
		ef.setSize(data.length);
		ef.setCreationDate(GregorianCalendar.getInstance());

		ef.setModDate(GregorianCalendar.getInstance());

		fs.setEmbeddedFile(ef);

		//Ergänzungen am EmbeddedFile-Dict
		COSDictionary cosDict = (COSDictionary)dict.getDictionaryObject("EF");
		cosDict.setItem("UF", cosDict.getDictionaryObject("F"));
		//Ergänzungen am EmbeddedFile-Stream
		COSStream efStream = (COSStream) cosDict.getDictionaryObject("F");
		cosDict = new COSDictionary();
		cosDict.setDate("ModDate", new GregorianCalendar());
		efStream.setItem("Params", cosDict);
		
		// now add the entry to the embedded file tree and set in the document.
		efTree.setNames(Collections.singletonMap(XMPSchemaZugferd.ZUGFERD_XML_DEFAULT_NAME, fs));
		PDDocumentNameDictionary names = new PDDocumentNameDictionary(
				doc.getDocumentCatalog());
		names.setEmbeddedFiles(efTree);
		doc.getDocumentCatalog().setNames(names);
		// AF entry (Array) in catalog with the FileSpec
		COSArray cosArray = new COSArray();
		cosArray.add(fs);
		doc.getDocumentCatalog().getCOSObject().setItem("AF", cosArray); //$NON-NLS-1$
		return doc;

	}

}
