package com.sebulli.fakturama.dialogs;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.odftoolkit.odfdom.dom.element.table.TableTableCellElementBase;
import org.odftoolkit.odfdom.dom.element.table.TableTableRowElement;
import org.odftoolkit.odfdom.dom.element.text.TextPlaceholderElement;
import org.odftoolkit.simple.Document;
import org.odftoolkit.simple.PresentationDocument;
import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.common.navigation.CellSelection;
import org.odftoolkit.simple.common.navigation.PlaceholderNavigation;
import org.odftoolkit.simple.common.navigation.PlaceholderNode;
import org.odftoolkit.simple.common.navigation.PlaceholderNode.PlaceholderTableType;
import org.odftoolkit.simple.common.navigation.TextNavigation;
import org.odftoolkit.simple.common.navigation.TextSelection;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.DefaultCellValueAdapter;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This demo is a simple template application about hotel receipt. By loading
 * the configuration file "consume-data.properties" and navigating the hotel
 * receipt template, this demo could generate the ODF documents (ODT, ODP, and
 * ODS format). In the generated ODF documents, you can see the concrete
 * information about hotel receipt, such as hotel name, customer name, consume
 * time, consume data, total expense, head count, and consume item details
 * including the price, quantity, total expense of each item.
 *
 */
public class NavigationSample {

	private Properties properties = new Properties();
	private Map<String, String> map = new HashMap<String, String>();

	public static void main(String[] args) throws Exception {
		NavigationSample navigationSample = new NavigationSample("consume-data.properties");
//		navigationSample.navigatePlaceholders();
		navigationSample.checkDocument();
//		navigationSample.navigateODT();
//		navigationSample.navigateODP();
//		navigationSample.navigateODS();
	}

	private void navigatePlaceholders() {
		/*
		 * Achtung: Bei Templates gilt:
		 * Document.OdfMediaType.TEXT_TEMPLATE.getMediaTypeString() == document.getMediaTypeString()
		 * 
		 * *** Konvertierung zu PDF:
		 * swriter.exe -convert-to pdf --outdir d:\eclipse44SR1\workspace\RheTest\mpf d:\eclipse44SR1\workspace\RheTest\Document_Test2.ott-Generated.odt
		 * 
		 * swriter ist besser, da sich bei Verwendung von "soffice" der Willkommensbildschirm öffnet
		 */
		String fileName = "Document_Test2.ott";
		Path propFile = Paths.get("placeholders.properties");
		try (InputStream is = Files.newInputStream(propFile);) {
			properties.clear();
			properties.load(is);

			Iterator<Entry<Object, Object>> it = properties.entrySet().iterator();
			System.out.println("Navigate placeholders in ODT document: " + fileName);
			TextDocument textdoc = (TextDocument) Document.loadDocument(fileName);
			textdoc.changeMode(TextDocument.OdfMediaType.TEXT);
			PlaceholderNavigation search;

//			PlaceholderNavigation allNodes = new PlaceholderNavigation(textdoc);
//			allNodes.replaceEachWithValue(properties);
			while (it.hasNext()) {
				Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) it.next();
				String key = (String) entry.getKey();
				String value = (String) entry.getValue();
				search = new PlaceholderNavigation(key, textdoc, true);
				while (search.hasNext()) {
					PlaceholderNode item = search.nextSelection();
					item.replaceWith(value);
				}
			}

			// test for replacing images
			search = new PlaceholderNavigation("ITEM.PICTURE", textdoc, true);
			while(search.hasNext()) {
				PlaceholderNode item = search.nextSelection();
				Path p = Paths.get("ingo-weiss_design.png");
				item.replaceWith(p.toUri());
			}

			// remove ObjectReplacements/ and Thumbnails/
			textdoc.getPackage().removeDocument("ObjectReplacements/");
			textdoc.getPackage().removeDocument("Thumbnails/");
			textdoc.save(fileName +"-Generated.odt");
			System.out.println("...\nNavigation is over, and "+fileName +"-Generated.odt is generated");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void checkDocument() throws Exception {
		System.out.println("Navigate OTT document: Document_Test2.ott");
		TextDocument textdoc = (TextDocument) Document.loadDocument("Document_Test2.ott");

		PlaceholderNavigation navi = new PlaceholderNavigation(textdoc);
		List<PlaceholderNode> placeholders = navi.getPlaceHolders();
		Set<PlaceholderTableType> processedTables = new HashSet<>();
		int i = 0;
		for (PlaceholderNode placeholderNode : placeholders) {
	        switch (placeholderNode.getNodeType()) {
			case NORMAL_NODE:
				placeholderNode.replaceWith("test" + i);
				break;
			case TABLE_NODE:
				// process only if that table wasn't processed
				// but wait: a table (e.g., "ITEM" table) could occur more than once!
				if(!processedTables.contains(placeholderNode.getTableType())) {
					// get the complete row with placeholders and store it as a template
					Row pRowTemplate = navi.getTableRow(placeholderNode);
					int cellCount = pRowTemplate.getCellCount();
					// for each item from items list create a row and replace the placeholders
					Table pTable = pRowTemplate.getTable();
					pTable.setCellStyleInheritance(true);

					for(int m = 0; m < 50; m++) {   // simulate the items list
						System.out.print("Durchlauf: "+m);
						TableTableRowElement newRowElement = (TableTableRowElement) pRowTemplate.getOdfElement().cloneNode(true);
						// Row tmpRow = pTable.appendRow();
						// we always insert only ONE row to the table
						Row tmpRow = pTable.insertRowsBefore(pRowTemplate.getRowIndex(), 1).get(0);
						pTable.getOdfElement().replaceChild(newRowElement, tmpRow.getOdfElement());
						Row newRow = Row.getInstance(newRowElement);
						// find all placeholders within row
						for (int j = 0; j < cellCount; j++) {
						    System.out.print(".");
							// a template cell
			                Cell currentCell = newRow.getCellByIndex(j);
			        		// make a copy of the template cell
			                TableTableCellElementBase cellNode = (TableTableCellElementBase) currentCell.getOdfElement().cloneNode(true);

			                // find all placeholders in a cell
			                NodeList cellPlaceholders = cellNode.getElementsByTagName(TextPlaceholderElement.ELEMENT_NAME.getQName());
			                List<PlaceholderNode> cellPlaceholderList = new ArrayList<>();
			        		for (int k = 0; k < cellPlaceholders.getLength(); k++) {
			                    Node item = cellPlaceholders.item(k);
			                    cellPlaceholderList.add(new PlaceholderNode(item));
			        		}

			        		/*
			        		 * The appended row only has default cells (without styles etc.). Therefore we have to take
			        		 * the template cell and replace the current cell with it.
			        		 */
			        		newRow.getOdfElement().replaceChild(cellNode, newRow.getCellByIndex(j).getOdfElement());
			                // replace placeholders with content
			                for (PlaceholderNode cellPlaceholder : cellPlaceholderList) {
		                        cellPlaceholder.replaceWith(""+m+"/"+j+"/"+cellPlaceholder.getNode().getTextContent().replaceAll("[<>]", "|"));
	                        }
		                }
						System.out.println();
					}

					// delete the template row from table
					pTable.removeRowsByIndex(pRowTemplate.getRowIndex(), 1);

					// determine type of this table and store it
					// irgendwie muß hier noch ein Name oder 'ne ID oder sowas mit ran...
					processedTables.add(placeholderNode.getTableType());
				}
				break;

			default:
				break;
			}
	        i++;
        }
		textdoc.save("Document_Test2-Generated.odt");
		System.out.println("...\nNavigation is over, and Document-Generated.odt is generated");
    }

	public void loadConsumeData(String filepath) throws Exception {
		InputStream is = new FileInputStream(filepath);
		properties.load(is);
		is.close();
		// put ConsumeItem as key and TotalExpenseOfItem as value to map
		Enumeration<String> enu = (Enumeration<String>) properties.propertyNames();
		while (enu.hasMoreElements()) {
			String key = (String) enu.nextElement();
			if (key.contains("ConsumeItem")) {
				String consumeItem = properties.getProperty(key);
				String totalExpenseOfItem = properties.getProperty("TotalExpenseOfItem" + key.charAt(key.length() - 1));
				map.put(consumeItem, totalExpenseOfItem);
			}
		}
	}

	NavigationSample(String filepath) throws Exception {
		loadConsumeData(filepath);
	}

	public void navigateODT() throws Exception {

		Iterator it = properties.entrySet().iterator();
		System.out.println("Navigate ODT document: Navigation-ODT-Templating.odt");
		TextDocument textdoc = (TextDocument) Document.loadDocument("Navigation-ODT-Templating.odt");
		TextNavigation search;
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			search = new TextNavigation(key, textdoc);
			while (search.hasNext()) {
				TextSelection item = (TextSelection) search.nextSelection();
				item.replaceWith(value);
			}
		}
		// remove ObjectReplacements/ and Thumbnails/
		textdoc.getPackage().removeDocument("ObjectReplacements/");
		textdoc.getPackage().removeDocument("Thumbnails/");
		textdoc.save("Navigation-ODT-Generated.odt");
		System.out.println("...\nNavigation is over, and Navigation-ODT-Generated.odt is generated");
	}

	public void navigateODP() throws Exception {
		Enumeration<String> enu = (Enumeration<String>) properties.propertyNames();
		System.out.println("Navigate ODP document: Navigation-ODP-Templating.odp");
		PresentationDocument pdoc = (PresentationDocument) Document.loadDocument("Navigation-ODP-Templating.odp");
		TextNavigation search;
		while (enu.hasMoreElements()) {
			String key = (String) enu.nextElement();
			String value = (String) properties.getProperty(key);
			search = new TextNavigation(key, pdoc);
			while (search.hasNext()) {
				TextSelection item;
				item = (TextSelection) search.nextSelection();
				item.replaceWith(value);
			}
		}
		// set the cell value in the table of embedderdocument
		List<Document> embeddedDocuments = pdoc.getEmbeddedDocuments();
		Document embeddedDocument = embeddedDocuments.get(0);
		Table table = embeddedDocument.getTableList().get(0);
		String consumeItem;
		String totalExpenseOfItem;
		int index = 0;
		Iterator<String> it = map.keySet().iterator();
		while (it.hasNext()) {
			index++;
			consumeItem = (String) it.next();
			totalExpenseOfItem = (String) map.get(consumeItem);
			table.getColumnByIndex(0).getCellByIndex(index).setDisplayText(consumeItem);
			table.getColumnByIndex(1).getCellByIndex(index).setDisplayText(totalExpenseOfItem,
					new DefaultCellValueAdapter());
		}
		// remove ObjectReplacements/ and Thumbnails/
		pdoc.getPackage().removeDocument("ObjectReplacements/");
		pdoc.getPackage().removeDocument("Thumbnails/");
		pdoc.save("Navigation-ODP-Generated.odp");
		System.out.println("...\nNavigation is over, and Navigation-ODP-Generated.odp is generated");

	}

	public void navigateODS() throws Exception {
		Enumeration<String> enu = (Enumeration<String>) properties.propertyNames();
		System.out.println("Navigate ODS document: Navigation-ODS-Templating.ods");
		SpreadsheetDocument ssdoc = (SpreadsheetDocument) Document.loadDocument("Navigation-ODS-Templating.ods");
		TextNavigation search;
		while (enu.hasMoreElements()) {
			String key = (String) enu.nextElement();
			String value = properties.getProperty(key);
			search = new TextNavigation(key, ssdoc);
			while (search.hasNext()) {
				CellSelection item = (CellSelection) search.nextSelection();
				item.advancedReplaceWith(value, new DefaultCellValueAdapter());
			}
		}
		// remove ObjectReplacements/ and Thumbnails/
		ssdoc.getPackage().removeDocument("ObjectReplacements/");
		ssdoc.getPackage().removeDocument("Thumbnails/");
		ssdoc.save("Navigation-ODS-Generated.ods");
		System.out.println("...\nNavigation is over, and Navigation-ODS-Generated.ods is generated");
	}
}
