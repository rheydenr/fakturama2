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
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.GregorianCalendar;

import javax.xml.transform.TransformerException;

import org.apache.jempbox.xmp.XMPMetadata;
import org.apache.jempbox.xmp.XMPSchemaBasic;
import org.apache.jempbox.xmp.XMPSchemaDublinCore;
import org.apache.jempbox.xmp.XMPSchemaPDF;
import org.apache.jempbox.xmp.pdfa.XMPSchemaPDFAId;
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
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.e4.core.internal.services.ResourceBundleHelper;
import org.osgi.framework.Bundle;

import com.sebulli.fakturama.exception.FakturamaStoringException;


/**
 * @author rheydenr
 *
 */
public class ZugferdHelper {
    private static final String XMP_SCHEMALOCATION = "/ZUGFeRD_Extension-Schema-neu.xmp";

	/**
	 * Makes A PDF/A3a-compliant document from a PDF-A1 compliant document (on
	 * the metadata level, this will not e.g. convert graphics to JPG-2000)
	 * 
	 * @return 
	 * @throws TransformerException 
	 * @throws IOException 
	 * */
	public static PDDocument makeA3Acompliant(String pdfFile, ConformanceLevel level) throws IOException, TransformerException {
		PDDocument doc = PDDocument.load(pdfFile);
			PDDocumentCatalog cat = doc.getDocumentCatalog();
			PDMetadata metadata = cat.getMetadata();
			// we're using the jempbox org.apache.jempbox.xmp.XMPMetadata version,
			// not the xmpbox one
			XMPMetadata xmp = new XMPMetadata();

			XMPSchemaPDFAId pdfaid = new XMPSchemaPDFAId(xmp);
			pdfaid.setAbout(""); //$NON-NLS-1$
			xmp.addSchema(pdfaid);

			XMPSchemaDublinCore dc = xmp.addDublinCoreSchema();
			String creator = System.getProperty("user.name"); // set current (operating system) user name as (mandatory) human creator //$NON-NLS-1$
			String producer = "Fakturama.org"; // (mandatory) producer application is Fakturama 
			dc.addCreator(creator);
			dc.setAbout(""); //$NON-NLS-1$

			XMPSchemaBasic xsb = xmp.addBasicSchema();
			xsb.setAbout(""); //$NON-NLS-1$

			xsb.setCreatorTool("Fakturama invoicing software");
			xsb.setCreateDate(GregorianCalendar.getInstance());
			PDDocumentInformation pdi=doc.getDocumentInformation();
			pdi.setProducer(producer);
			pdi.setAuthor(creator);
			doc.setDocumentInformation(pdi);

			XMPSchemaPDF pdf = xmp.addPDFSchema();
			pdf.setProducer(producer);
			pdf.setAbout(""); //$NON-NLS-1$

			/*
			// Mandatory: PDF/A3-a is tagged PDF which has to be expressed using a
			// MarkInfo dictionary (PDF A/3 Standard sec. 6.7.2.2)*/
			PDMarkInfo markinfo = new PDMarkInfo();
			markinfo.setMarked(true);
			doc.getDocumentCatalog().setMarkInfo(markinfo);
	
	/*
		To be on the safe side, we use level B without Markinfo because we can not 
		guarantee that the user  correctly tagged the templates for the PDF. 
	 * */
			pdfaid.setConformance("B");//$NON-NLS-1$
        /*  * All files are PDF/A-3, setConformance
         * refers to the level conformance, e.g.
         * PDF/A-3-B where B means only visually
         * preservable, U means visually and unicode
         * preservable and A -like in this case-
         * means full compliance, i.e. visually,
         * unicode and structurally preservable
         * 
         */
			pdfaid.setPart(3);

				addZugferdXMP(xmp, level); /*
									 * this is the only line where we do something
									 * Zugferd-specific, i.e. add PDF metadata
									 * specifically for Zugferd, not generically for
									 * an embedded file
									 */

			metadata.importXMPMetadata(xmp);
			
//			XmpSerializer serializer = new XmpSerializer();
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			serializer.serialize(xmp, baos, true);
//			metadata.importXMPMetadata(baos.toByteArray());
//			doc.getDocumentCatalog().setMetadata(metadata);			
			
			
			return doc;
		}

/***
 * This will add both the RDF-indication which embedded file is Zugferd and the 
 * necessary PDF/A schema extension description to be able to add this information to RDF 
 * @param metadata
 */
	private static void addZugferdXMP(XMPMetadata metadata, ConformanceLevel level) {
        Bundle definingBundle = ResourceBundleHelper.getBundleForName(org.fakturama.export.zugferd.Activator.PLUGIN_ID);
        URL fileResource = FileLocator.find(definingBundle, new org.eclipse.core.runtime.Path(
                XMP_SCHEMALOCATION), null);
        if(fileResource == null) {
            // try resource from src
            fileResource = FileLocator.find(definingBundle, new org.eclipse.core.runtime.Path(
                "/src/main/resources/"+XMP_SCHEMALOCATION), null);
        }
//        try(InputStream zfExtensionIs = fileResource.openStream();) {
//            DomXmpParser builder = new DomXmpParser();
//            builder.setStrictParsing(true);
//            
//            XMPMetadata defaultXmp = builder.parse(zfExtensionIs);
//            metadata.addSchema(defaultXmp.getPDFExtensionSchema());
//
        
        XMPSchemaZugferd zf = new XMPSchemaZugferd(metadata, level/*, "1.0"*/);
            zf.setAbout(""); //$NON-NLS-1$
            metadata.addSchema(zf);
//        } catch (IOException | NullPointerException e) {
//            throw new FakturamaStoringException("can't parse XMP file", e);
//        }

		XMPSchemaPDFAExtensions pdfaex = new XMPSchemaPDFAExtensions(metadata);
		pdfaex.setAbout(""); //$NON-NLS-1$
		metadata.addSchema(pdfaex);
	}

	/**
	 * embed the Zugferd XML structure in a file named ZUGFeRD-invoice.xml
	 * @throws IOException 
	 * */
public static PDDocument attachZugferdFile(PDDocument doc, byte[] data) throws IOException {

		// embedded files are stored in a named tree
		PDEmbeddedFilesNameTreeNode efTree = new PDEmbeddedFilesNameTreeNode();

		String filename="ZUGFeRD-invoice.xml"; //$NON-NLS-1$
		// first create the file specification, which holds the embedded file
		PDComplexFileSpecification fs = new PDComplexFileSpecification();
		fs.setFile(filename);

		COSDictionary dict = fs.getCOSDictionary();
		// Relation "Source" for linking with eg. catalog
		dict.setName("AFRelationship", "Alternative"); // as defined in Zugferd standard //$NON-NLS-1$ //$NON-NLS-2$

		dict.setString("UF", filename); //$NON-NLS-1$

		// create a dummy file stream, this would probably normally be a
		// FileInputStream
		
		ByteArrayInputStream fakeFile = new ByteArrayInputStream(data);
		PDEmbeddedFile ef = new PDEmbeddedFile(doc, fakeFile);
		// now lets some of the optional parameters
		ef.setSubtype("text/xml");// as defined in Zugferd standard //$NON-NLS-1$
		ef.setSize(data.length);
		ef.setCreationDate(new GregorianCalendar());

		ef.setModDate(GregorianCalendar.getInstance());

		fs.setEmbeddedFile(ef);

		// now add the entry to the embedded file tree and set in the document.
		efTree.setNames(Collections.singletonMap(filename, fs));
		PDDocumentNameDictionary names = new PDDocumentNameDictionary(
				doc.getDocumentCatalog());
		names.setEmbeddedFiles(efTree);
		doc.getDocumentCatalog().setNames(names);
		// AF entry (Array) in catalog with the FileSpec
		COSArray cosArray = new COSArray();
		cosArray.add(fs);
		doc.getDocumentCatalog().getCOSDictionary().setItem("AF", cosArray); //$NON-NLS-1$
		return doc;

	}

}
