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

package com.sebulli.fakturama.views.datatable;

import javax.inject.Inject;

import org.eclipse.e4.tools.services.IResourcePool;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TableColumn;

import com.sebulli.fakturama.model.IEntity;

/**
 * This class represents a column of a table that contains UniDataSets
 * 
 * @author Gerd Bartelt
 * @deprecated
 */
public class DataTableColumn<T extends IEntity> {

    @Inject
    private IResourcePool resourcePool;

	// All used images are loaded by default and when the table row is
	// displayed. This makes the loading of the table faster.
//	private static final Image CHECKED = Activator.getImageDescriptor("icons/16/checked_16.png").createImage();
//	private static final Image PRINTER = Activator.getImageDescriptor("icons/16/printer_16.png").createImage();
//	private static final Image PRINTERGREY = Activator.getImageDescriptor("icons/16/printer_grey_16.png").createImage();
//	private static final Image CHECKED48 = Activator.getImageDescriptor("icons/48/checked_48x64.png").createImage();
//	private static final Image OFFER = Activator.getImageDescriptor("icons/16/offer_16.png").createImage();
//	private static final Image CONFIRMATION = Activator.getImageDescriptor("icons/16/confirmation_16.png").createImage();
//	private static final Image ORDER = Activator.getImageDescriptor("icons/16/order_16.png").createImage();
//	private static final Image INVOICE = Activator.getImageDescriptor("icons/16/invoice_16.png").createImage();
//	private static final Image DELIVERY = Activator.getImageDescriptor("icons/16/delivery_16.png").createImage();
//	private static final Image CREDIT = Activator.getImageDescriptor("icons/16/credit_16.png").createImage();
//	private static final Image DUNNING = Activator.getImageDescriptor("icons/16/dunning_16.png").createImage();
//	private static final Image LETTER = Activator.getImageDescriptor("icons/16/letter_16.png").createImage();
//	private static final Image PROFORMA = Activator.getImageDescriptor("icons/16/proforma_16.png").createImage();
//	private static final Image ERROR = Activator.getImageDescriptor("icons/16/error_16.png").createImage();
//	private static final Image ORDER_PENDING = Activator.getImageDescriptor("icons/16/order_pending_16.png").createImage();
//	private static final Image ORDER_PROCESSING = Activator.getImageDescriptor("icons/16/order_processing_16.png").createImage();
//	private static final Image ORDER_SHIPPED = Activator.getImageDescriptor("icons/16/order_shipped_16.png").createImage();
//	private static final Image REDPOINT = Activator.getImageDescriptor("icons/16/redpoint_16.png").createImage();
	private int stdId = 0;
	
	private Display display = null;

	private String dataKey = "";
	
	// Editing support of this column
	EditingSupport editingSupport;

	/**
	 * Constructor Creates a UniDatSet table column with no editing support
	 * 
	 * @param tableColumnLayout
	 *            The layout of the table column
	 * @param tableViewer
	 *            The table viewer
	 * @param style
	 *            SWT style of the column
	 * @param header
	 *            The header of the column
	 * @param weight
	 *            Width of the column
	 * @param minimumWidth
	 *            The minimum width
	 * @param fixsize
	 *            Set the width to a fix value
	 * @param dataKey
	 *            Key that represents the column's data
	 */
	public DataTableColumn(TableColumnLayout tableColumnLayout, TableViewer tableViewer, int style, String header, int weight,
			boolean fixsize, String dKey) {
		this(tableColumnLayout, tableViewer, style, header, weight, fixsize, dKey, null);
	}
	/**
	 * Constructor Creates a UniDatSet table column with no editing support
	 * 
	 * @param tableColumnLayout
	 *            The layout of the table column
	 * @param tableViewer
	 *            The table viewer
	 * @param style
	 *            SWT style of the column
	 * @param header
	 *            The header of the column
	 * @param weight
	 *            Width of the column
	 * @param minimumWidth
	 *            The minimum width
	 * @param fixsize
	 *            Set the width to a fix value
	 * @param dataKey
	 *            Key that represents the column's data
	 */
	public DataTableColumn(Display display, TableColumnLayout tableColumnLayout, TableViewer tableViewer, int style, String header, int weight,
			boolean fixsize, String dKey, EditingSupport editingSupport) {
		this(tableColumnLayout, tableViewer, style, header, weight, fixsize, dKey, editingSupport);
		this.display = display;
	}

	public void setDataKey (String dataKey) {
		this.dataKey = dataKey;
	}
	
	/**
	 * Constructor Creates a UniDatSet table column with editing support
	 * 
	 * @param tableColumnLayout
	 *            The layout of the table column
	 * @param tableViewer
	 *            The table viewer
	 * @param style
	 *            SWT style of the column
	 * @param header
	 *            The header of the column
	 * @param weight
	 *            Width of the column
	 * @param minimumWidth
	 *            The minimum width
	 * @param fixsize
	 *            Set the width to a fix value
	 * @param dataKey
	 *            Key that represents the column's data
	 * @param editingSupport
	 *            The editing support of the cell
	 * @param columnNr
	 *            column of the table
	 */
	public DataTableColumn(TableColumnLayout tableColumnLayout, final TableViewer tableViewer, int style, String header, int weight,
			boolean fixsize, String dKey, EditingSupport editingSupport) {

		this.dataKey = dKey;
		
		// Create a TableViewerColum for the column
		TableViewerColumn viewerNameColumn = new TableViewerColumn(tableViewer, style);

		// Set the editing support
		this.editingSupport = editingSupport;
		
		// Create a column and set the header text
		final TableColumn column = viewerNameColumn.getColumn();
		column.setText(header);

		// Add a selection listener
		column.addSelectionListener(new SelectionAdapter() {

			// The table column was selected
			@Override
			public void widgetSelected(SelectionEvent e) {

				if (tableViewer.getSorter() == null)
					return;
				
//				// Get the data key of the column
//				DataSetArray<?> datasets = (DataSetArray<?>) tableViewer.getInput();
//				((TableSorter) tableViewer.getSorter()).setDataKey(datasets.getTemplate(), dataKey);

				// Get the sort order (direction)
				int dir = tableViewer.getTable().getSortDirection();

				// Toggle the direction
				if (tableViewer.getTable().getSortColumn() == column) {
					if (dir == SWT.UP)
						dir = SWT.DOWN;
					else
						dir = SWT.UP;
				}
				else {
					dir = SWT.DOWN;
				}

				// Set the new sort order (direction)
				tableViewer.getTable().setSortDirection(dir);
				tableViewer.getTable().setSortColumn(column);
				tableViewer.refresh();
			}
		});

		// Set the column width as fix or variable width
		if (fixsize) {
			tableColumnLayout.setColumnData(viewerNameColumn.getColumn(), new ColumnPixelData(weight));
		} else {
			tableColumnLayout.setColumnData(viewerNameColumn.getColumn(), new ColumnWeightData(weight, weight / 4, true));
		}

		// Add the editing support
		if (editingSupport != null) {
			viewerNameColumn.setEditingSupport(editingSupport);
		}

		// Add the label provider
		viewerNameColumn.setLabelProvider(new CellLabelProvider() {

			@Override
			public void update(ViewerCell cell) {
				T uds = (T) cell.getElement();

				// Fill the cell with a UniData value, if the dataKey
				// does not start with a "$"
				if (!dataKey.startsWith("$")) {
					cell.setText(getText(uds, dataKey));
				}

//				// Fill the cell with a mark, if optional is set
//				else if (dataKey.equals("$Row")) {
//					int i = ((IDataSetItem)uds).row;
//					cell.setText(Integer.toString(i));
//				}
//
//				// Fill the cell with an mark, if optional is set
//				else if (dataKey.equals("$Optional")) {
//					if (uds.getBooleanValueByKey("optional")){
//
//						// Set the 48pixel icon, if product pictures are used
//						if (Activator.getDefault().getPreferenceStore().getBoolean("DOCUMENT_USE_PREVIEW_PICTURE")) {
//							cell.setImage(CHECKED48);
//						} 
//						else {
//							cell.setImage(CHECKED);
//						}
//						
//					}
//					else
//						cell.setImage(null);
//				}
//
//				
//				
//				// Fill the cell with the icon for standard ID
//				else if (dataKey.equals("$stdId")) {
//					if (stdId == uds.getIntValueByKey("id"))
//						cell.setImage(CHECKED);
//					else
//						cell.setImage(null);
//					/*
//					if (stdId == uds.getIntValueByKey("id")) {
//						Color color = new Color(null, 0xff, 0xb8, 0x00);
//						int columns = cell.getViewerRow().getColumnCount();
//						for (int i=0; i<columns; i++)
//							 cell.getViewerRow().setBackground(i, color);
//						color.dispose();
//
//					}*/
//
//				}
//
//				// Fill the cell with the icon for standard ID
//				else if (dataKey.equals("$donotbook")) {
//					if (!uds.getBooleanValueByKey("donotbook"))
//						cell.setImage(null);
//					else  {
//						cell.setImage(REDPOINT);
//						/*
//						Color color = new Color(null, 0xc0, 0x80, 0x80);
//						int columns = cell.getViewerRow().getColumnCount();
//						for (int i=0; i<columns; i++) {
//							 cell.getViewerRow().setForeground(i, color);
//						}
//						color.dispose();
//						*/
//					}
//
//				}
//
//				// Fill the cell with the icon for status
//				// eg. "paid/unpaid" for invoices
//				else if (dataKey.equals("$status")) {
//					switch (DocumentType.getType(uds.getIntValueByKey("category"))) {
//					case INVOICE:
//					case CREDIT:
//						if (uds.getBooleanValueByKey("paid")) {
//							cell.setImage(CHECKED);
//						}
//						else {
//							cell.setImage(ERROR);
//						}
//						break;
//					case DUNNING:
//						if (uds.getBooleanValueByKey("paid")) {
//							cell.setImage(CHECKED);
//						}
//						else {
//							cell.setImage(ERROR);
//						}
//						break;
//					case ORDER:
//						switch (uds.getIntValueByKey("progress")) {
//						case 0:
//						case MarkOrderAsAction.PENDING:
//							cell.setImage(ORDER_PENDING);
//							break;
//						case MarkOrderAsAction.PROCESSING:
//							cell.setImage(ORDER_PROCESSING);
//							break;
//						case MarkOrderAsAction.SHIPPED:
//							cell.setImage(ORDER_SHIPPED);
//							break;
//						case MarkOrderAsAction.COMPLETED:
//							cell.setImage(CHECKED);
//							break;
//						}
//						break;
//					default:
//						cell.setImage(null);
//					}
//					cell.setText(getText(uds, dataKey));
//				}
//				
//				
//				// Fill the cell with the icon for printed status
//				else if (dataKey.equals("$printed")) {
//
//					if (uds.getBooleanValueByKey("printed")) {
//						cell.setImage(PRINTER);
//					}
//					else if (!uds.getStringValueByKey("odtpath").isEmpty() || !uds.getStringValueByKey("pdfpath").isEmpty()) {
//						cell.setImage(PRINTERGREY);
//					}
//				}
//				
//				
//				// Fill the cell with the icon of the document type
//				else if (dataKey.equals("$documenttype")) {
//					DocumentType documentType = DocumentType.getType(((UniDataSet) cell.getElement()).getIntValueByKey("category"));
//					switch (documentType) {
//					case LETTER:
//						cell.setImage(LETTER);
//						break;
//					case OFFER:
//						cell.setImage(OFFER);
//						break;
//					case CONFIRMATION:
//						cell.setImage(CONFIRMATION);
//						break;
//					case ORDER:
//						cell.setImage(ORDER);
//						break;
//					case INVOICE:
//						cell.setImage(INVOICE);
//						break;
//					case DELIVERY:
//						cell.setImage(DELIVERY);
//						break;
//					case CREDIT:
//						cell.setImage(CREDIT);
//						break;
//					case DUNNING:
//						cell.setImage(DUNNING);
//					case PROFORMA:
//						cell.setImage(PROFORMA);
//						break;
//					}
//				}
//
//				// Fill the cell a small preview picture
//				else if (dataKey.equals("$ProductPictureSmall")) {
//					String pictureName = ((UniDataSet) cell.getElement()).getStringValueByKey("picturename");
//					
//					try {
//						// Display the picture, if a product picture is set.
//						if (!pictureName.isEmpty()) {
//
//
//							// Load the image, based on the picture name
//							Image image = new Image(display,  Workspace.INSTANCE.getWorkspace() + Workspace.productPictureFolderName + pictureName);
//
//							// Get the pictures size
//							int width = image.getBounds().width;
//							int height = image.getBounds().height;
//
//							// Scale the image to 64x48 Pixel
//							if ((width != 0) && (height != 0)) {
//
//								// Picture is more width than height.
//								if (width >= ((64*height)/48)) {
//									height = (height * 64) / width;
//									width = 64;
//								}
//								else { //if (height > ((48*width)/64)) {
//									width = (width * 48) / height;
//									height = 48;
//								}
//
//							}
//							
//							// Scale the product picture and place it into the 64x48 pixel image
//							Image baseImage = new Image(display, 64, 48);
//							GC gc = new GC(baseImage);
//							gc.drawImage(new Image(display, image.getImageData().scaledTo(width, height)), (64-width)/2, (48-height)/2);
//						    gc.dispose();
//
//							cell.setImage(baseImage);
//
//						}
//					}
//					catch (Exception e) {
//					}
//				}
//
//				// Fill the cell with the VAT value
//				else if (dataKey.equals("$vatbyid")) {
//					cell.setText(((UniDataSet) cell.getElement()).getFormatedStringValueByKeyFromOtherTable("vatid.VATS:value"));
//				}
//
//				// Fill the cell with the VAT value
//				else if (dataKey.equals("$vatnamebyid")) {
//					cell.setText(((UniDataSet) cell.getElement()).getStringValueByKeyFromOtherTable("vatid.VATS:name"));
//				}
//
//				// Fill the cell with the total net value of the item
//				else if (dataKey.equals("$ItemNetTotal")) {
//					cell.setText(new Price(((DataSetItem) cell.getElement())).getTotalNetRounded().asFormatedString());
//				}
//
//				// Fill the cell with the VAT (percent) value of the item
//				else if (dataKey.equals("$ItemVatPercent")) {
//					cell.setText(new Price(((DataSetItem) cell.getElement())).getVatPercent());
//				}
//
//				// Fill the cell with the VAT (percent) value of the item
//				else if (dataKey.equals("$VoucherItemVatPercent")) {
//					cell.setText(new Price(((DataSetVoucherItem) cell.getElement())).getVatPercent());
//				}
//
//				// Fill the cell with the total gross value of the item
//				else if (dataKey.equals("$ItemGrossTotal")) {
//					cell.setText(new Price(((DataSetItem) cell.getElement())).getTotalGrossRounded().asFormatedString());
//				}
//
//				// Fill the cell with the gross price of the item
//				else if (dataKey.equals("$ItemGrossPrice")) {
//					cell.setText(new Price(((DataSetItem) cell.getElement())).getUnitGross().asFormatedString());
//				}
//
//				// Fill the cell with the gross price of the item
//				else if (dataKey.equals("$VoucherItemGrossPrice")) {
//					cell.setText(new Price(((DataSetVoucherItem) cell.getElement())).getUnitGross().asFormatedString());
//				}
//
//				// Fill the cell with the net price of the product (quantity = 1)
//				else if (dataKey.equals("$Price1Net")) {
//					DataSetProduct product = (DataSetProduct) cell.getElement();
//					cell.setText(new Price(product.getDoubleValueByKey("price1"), product.getDoubleValueByKeyFromOtherTable("vatid.VATS:value")).getUnitNet()
//							.asFormatedString());
//				}
//
//				// Fill the cell with the gross price of the product (quantity = 1)
//				else if (dataKey.equals("$Price1Gross")) {
//					DataSetProduct product = (DataSetProduct) cell.getElement();
//					cell.setText(new Price(product.getDoubleValueByKey("price1"), product.getDoubleValueByKeyFromOtherTable("vatid.VATS:value")).getUnitGross()
//							.asFormatedString());
//				}

			}
		});

	}

	/**
	 * Get the value to fill the cell as text.
	 * 
	 * @param uds
	 *            The UniDataSet that contains the text
	 * @param dataKey
	 *            The data key to access the UniDataSet
	 * @return The value as text string
	 */
	public String getText(T uds, String dataKey) {
		// Fill the cell direct with a UniData value, if the dataKey
		// does not start with a "$"
		if (!dataKey.startsWith("$")) {
			String firstline;
			try {
				firstline = uds.getFormatedStringValueByKey(dataKey).split("\n")[0];
			} catch (Exception e) {
				
				// Return an empty string, if there is only one line feed
				return "";
			}
			return firstline;
		}

		// Fill the cell with the status
		// e.g. "paid/unpaid" for invoices
//		else if (dataKey.equals("$status")) {
//			switch (DocumentType.getType(uds.getIntValueByKey("category"))) {
//			case INVOICE:
//			case CREDIT:
//				if (uds.getBooleanValueByKey("paid")) {
//					//T: Mark a paid document with this text.
//					return _("paid");
//				}
//				else {
//					//T: Mark an unpaid document with this text.
//					return _("unpaid");
//				}
//			case DUNNING:
//				if (uds.getBooleanValueByKey("paid")) {
//					//T: Mark a paid document with this text.
//					return _("paid");
//				}
//				else {
//					//T: Marking of a dunning in the document table.
//					//T: Format: "Dunning No. xx"
//					return _("Dunning No.") + " " + uds.getStringValueByKey("dunninglevel");
//				}
//			case ORDER:
//				switch (uds.getIntValueByKey("progress")) {
//				case 0:
//				case MarkOrderAsAction.PENDING:
//					//T: Mark an order with this text.
//					return _("open");
//				case MarkOrderAsAction.PROCESSING:
//					//T: Mark an order with this text.
//					return _("in work");
//				case MarkOrderAsAction.SHIPPED:
//					//T: Mark an order with this text.
//					return _("shipped");
//				case MarkOrderAsAction.COMPLETED:
//					//T: Mark an order with this text.
//					return _("closed");
//				}
//				break;
//			default:
//				return "";
//			}
//
//		}

//		// Fill the cell with the document type
//		else if (dataKey.equals("$documenttype")) {
//			DocumentType documentType = DocumentType.getType(uds.getIntValueByKey("category"));
//			return documentType.getString();
//		}
//
//		// Fill the cell with the VAT value
//		else if (dataKey.equals("$vatbyid")) { return uds.getFormatedStringValueByKeyFromOtherTable("vatid.VATS:value"); }

		return "";
	}
//
//	/**
//	 * Get the value to fill the cell as number.
//	 * 
//	 * @param uds
//	 *            The UniDataSet that contains the text
//	 * @param dataKey
//	 *            The data key to access the UniDataSet
//	 * @return The value as double
//	 */
//	public static Double getDoubleValue(UniDataSet uds, String dataKey) {
//
//		// Get the UniData value, if the dataKey
//		// does not start with a "$"
//		if (!dataKey.startsWith("$")) {
//			return uds.getDoubleValueByKey(dataKey);
//		}
//
//		// Get the value of the status
//		// eg. "paid/unpaid" for invoices
//		else if (dataKey.equals("$status")) {
//			switch (DocumentType.getType(uds.getIntValueByKey("category"))) {
//			case INVOICE:
//			case CREDIT:
//				return uds.getDoubleValueByKey("paid");
//			case DUNNING:
//				if (uds.getBooleanValueByKey("paid")) {
//					return 0.0;
//				}
//				else {
//					return uds.getDoubleValueByKey("dunninglevel");
//				}
//			case ORDER:
//				return uds.getDoubleValueByKey("progress");
//			default:
//				return 0.0;
//			}
//
//		}
//
//		// Get the number of the document type
//		else if (dataKey.equals("$documenttype")) {
//			return uds.getDoubleValueByKey("category");
//		}
//
//		// Get the VAT
//		else if (dataKey.equals("$vatbyid")) { return DataUtils.StringToDouble(uds.getFormatedStringValueByKeyFromOtherTable("vatid.VATS:value")); }
//
//		return 0.0;
//	}
//
//	/**
//	 * Returns TRUE, if the dataKey describes a numeric value
//	 * 
//	 * @param uds
//	 *            The UnidataSet to test
//	 * @param dataKey
//	 *            The data key
//	 * @return TRUE, if the dataKey describes a numeric value
//	 */
//	public static boolean isNumeric(UniDataSet uds, String dataKey) {
//
//		// If it does not start with a "$", test, if the UniDataSet contains
//		// a numeric value.
//		if (!dataKey.startsWith("$")) {
//			return uds.getUniDataTypeByKey(dataKey).isNumeric();
//		}
//
//		// Status is not numeric
//		else if (dataKey.equals("$status")) {
//			return false;
//		}
//
//		// Document type is not numeric
//		else if (dataKey.equals("$documenttype")) {
//			return false;
//		}
//
//		// VAT is numeric
//		else if (dataKey.equals("$vatbyid")) {
//			return true;
//		}
//
//		// Price is numeric
//		else if (dataKey.equals("$Price1Net")) {
//			return true;
//		}
//		else if (dataKey.equals("$Price1Gross")) { return true; }
//
//		return false;
//	}
//
//	/**
//	 * Returns TRUE, if the dataKey describes a date value
//	 * 
//	 * @param uds
//	 *            The UnidataSet to test
//	 * @param dataKey
//	 *            The data key
//	 * @return TRUE, if the dataKey describes a date value
//	 */
//	public static boolean isDate(UniDataSet uds, String dataKey) {
//		if (!dataKey.startsWith("$")) { return uds.getUniDataTypeByKey(dataKey).isDate(); }
//		return false;
//	}

	/**
	 * Set the standard entry
	 * 
	 * @param stdId
	 *            ID of the standard entry
	 */
	public void setStdEntry(int stdId) {
		this.stdId = stdId;
	}

//	/**
//	 * Getter for the editing support object
//	 * 
//	 * @return editingSupport
//	 * 			The editing support object.
//	 */
//	public EditingSupport getEditingSupport() {
//		return editingSupport;
//	}
}
