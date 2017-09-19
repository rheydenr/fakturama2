/*
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2015 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation
 */

package com.sebulli.fakturama.views.datatable.documents;

import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.DocumentType;
import com.sebulli.fakturama.misc.OrderState;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.IEntity;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.util.DocumentTypeUtil;

/**
 *
 */
public class SpecialCellValueProvider {
    protected Messages msg;

    /**
     * @param columnPropertyAccessor
     * @param msg
     */
    public SpecialCellValueProvider(Messages msg) {
        this.msg = msg;
    }

    
        public Object getCellValue(IEntity rowObject, String dataKey) {
            Object retval = null;
    //                // Fill the cell with an mark, if optional is set
    //                else if (dataKey.equals("$Optional")) {
    //                    if (uds.getBooleanValueByKey("optional")){
    //
    //                        // Set the 48pixel icon, if product pictures are used
    //                        if (Activator.getDefault().getPreferenceStore().getBoolean("DOCUMENT_USE_PREVIEW_PICTURE")) {
    //                            cell.setImage(CHECKED48);
    //                        } 
    //                        else {
    //                            cell.setImage(CHECKED);
    //                        }
    //                        
    //                    }
    //                    else
    //                        cell.setImage(null);
    //                }
    //
    //                // Fill the cell with the icon for standard ID
    //                else if (dataKey.equals("$donotbook")) {
    //                    if (!uds.getBooleanValueByKey("donotbook"))
    //                        cell.setImage(null);
    //                    else  {
    //                        cell.setImage(REDPOINT);
    //                        /*
    //                        Color color = new Color(null, 0xc0, 0x80, 0x80);
    //                        int columns = cell.getViewerRow().getColumnCount();
    //                        for (int i=0; i<columns; i++) {
    //                             cell.getViewerRow().setForeground(i, color);
    //                        }
    //                        color.dispose();
    //                        */
    //                    }
    //
    //                }
    //                
    //                
//    
//                    // Fill the cell a small preview picture
//                    /*else*/ if (dataKey.equals("$ProductPictureSmall")) {
//                        String pictureName = ((DocumentItem)rowObject).getPictureName();
//                        
//                        try {
//                            // Display the picture, if a product picture is set.
//                            if (!pictureName.isEmpty()) {
//    
//    
//                                // Load the image, based on the picture name
//                                Image image = new Image(display,  Workspace.INSTANCE.getWorkspace() + Workspace.productPictureFolderName + pictureName);
//    
//                                // Get the pictures size
//                                int width = image.getBounds().width;
//                                int height = image.getBounds().height;
//    
//                                // Scale the image to 64x48 Pixel
//                                if ((width != 0) && (height != 0)) {
//    
//                                    // Picture is more width than height.
//                                    if (width >= ((64*height)/48)) {
//                                        height = (height * 64) / width;
//                                        width = 64;
//                                    }
//                                    else { //if (height > ((48*width)/64)) {
//                                        width = (width * 48) / height;
//                                        height = 48;
//                                    }
//    
//                                }
//                                
//                                // Scale the product picture and place it into the 64x48 pixel image
//                                Image baseImage = new Image(display, 64, 48);
//                                GC gc = new GC(baseImage);
//                                gc.drawImage(new Image(display, image.getImageData().scaledTo(width, height)), (64-width)/2, (48-height)/2);
//                                gc.dispose();
//    
//                                cell.setImage(baseImage);
//    
//                            }
//                        }
//                        catch (Exception e) {
//                        }
//                    }
    //
    //                // Fill the cell with the VAT value
    //                else if (dataKey.equals("$vatbyid")) {
    //                    cell.setText(((UniDataSet) cell.getElement()).getFormatedStringValueByKeyFromOtherTable("vatid.VATS:value"));
    //                }
    //
    //                // Fill the cell with the VAT value
    //                else if (dataKey.equals("$vatnamebyid")) {
    //                    cell.setText(((UniDataSet) cell.getElement()).getStringValueByKeyFromOtherTable("vatid.VATS:name"));
    //                }
    //
    //                // Fill the cell with the VAT (percent) value of the item
    //                else if (dataKey.equals("$ItemVatPercent")) {
    //                    cell.setText(new Price(((DataSetItem) cell.getElement())).getVatPercent());
    //                }
    //
    //                // Fill the cell with the VAT (percent) value of the item
    //                else if (dataKey.equals("$VoucherItemVatPercent")) {
    //                    cell.setText(new Price(((DataSetVoucherItem) cell.getElement())).getVatPercent());
    //                }
     //
    //                // Fill the cell with the gross price of the item
    //                else if (dataKey.equals("$ItemGrossPrice")) {
    //                    cell.setText(new Price(((DataSetItem) cell.getElement())).getUnitGross().asFormatedString());
    //                }
    //
    //                // Fill the cell with the gross price of the item
    //                else if (dataKey.equals("$VoucherItemGrossPrice")) {
    //                    cell.setText(new Price(((DataSetVoucherItem) cell.getElement())).getUnitGross().asFormatedString());
    //                }
    //
    //                // Fill the cell with the net price of the product (quantity = 1)
    //                else if (dataKey.equals("$Price1Net")) {
    //                    DataSetProduct product = (DataSetProduct) cell.getElement();
    //                    cell.setText(new Price(product.getDoubleValueByKey("price1"), product.getDoubleValueByKeyFromOtherTable("vatid.VATS:value")).getUnitNet()
    //                            .asFormatedString());
    //                }
    //
    //                // Fill the cell with the gross price of the product (quantity = 1)
    //                else if (dataKey.equals("$Price1Gross")) {
    //                    DataSetProduct product = (DataSetProduct) cell.getElement();
    //                    cell.setText(new Price(product.getDoubleValueByKey("price1"), product.getDoubleValueByKeyFromOtherTable("vatid.VATS:value")).getUnitGross()
    //                            .asFormatedString());
    //                }
    return retval;
                }

    public Object getDataValue(Document rowObject, DocumentListDescriptor descriptor) {
        switch (descriptor) {
        // Fill the cell with the icon of the document type
        case ICON:
            DocumentType obj = DocumentTypeUtil.findByBillingType(rowObject.getBillingType());
            if (obj != null) {
                switch (obj) {
                case LETTER:
                    return Icon.ICON_LETTER;
                case OFFER:
                    return Icon.ICON_OFFER;
                case ORDER:
                    return Icon.ICON_ORDER;
                case CONFIRMATION:
                    return Icon.ICON_CONFIRMATION;
                case INVOICE:
                    return Icon.ICON_INVOICE;
                case DELIVERY:
                    return Icon.ICON_DELIVERY;
                case CREDIT:
                    return Icon.ICON_CREDIT;
                case DUNNING:
                    return Icon.ICON_DUNNING;
                case PROFORMA:
                    return Icon.ICON_PROFORMA;
                default:
                    break;
                }
            }
            break;
        case STATE:

            // Fill the cell with the icon for status
            // e.g. "paid/unpaid" for invoices
            obj = DocumentTypeUtil.findByBillingType(rowObject.getBillingType());
            if (obj != null) {
                switch (obj) {
                case INVOICE:
                case CREDIT:
                    if (rowObject.getPayDate() != null && BooleanUtils.toBoolean(rowObject.getPaid())) {
                        return Icon.COMMAND_CHECKED;
                    }
                    else {
                        return Icon.COMMAND_ERROR;
                    }
                case DELIVERY:
                    if (rowObject.getInvoiceReference() != null) { return Icon.COMMAND_INVOICE; }
                    break;
                case DUNNING:
                    if (rowObject.getPayDate() != null) {
                        return Icon.COMMAND_CHECKED;
                    }
                    else {
                        return Icon.COMMAND_ERROR;
                    }
                case ORDER:
                    OrderState progress = OrderState.findByProgressValue(Optional.ofNullable(rowObject.getProgress()));
                    
                    // TODO create more order statuses
                    switch (progress) {
                    case NONE:
                    case PENDING:
                        return Icon.COMMAND_ORDER_PENDING;
                    case PROCESSING:
                        return Icon.COMMAND_ORDER_PROCESSING;
                    case SHIPPED:
                        return Icon.COMMAND_ORDER_SHIPPED;
                    case COMPLETED:
                        return Icon.COMMAND_CHECKED;
                    }
                    break;
                default:
                    return null;
                }
            }
            break;
        case PRINTED:
            // Fill the cell with the icon for printed status
            if (BooleanUtils.toBoolean(rowObject.getPrinted())) {
                return Icon.COMMAND_PRINTER;
            }
            else if (StringUtils.isNotBlank(rowObject.getOdtPath()) || StringUtils.isNotBlank(rowObject.getPdfPath())) { return Icon.COMMAND_PRINTER_GREY; }
            break;
        default:
            break;
        }
        return null;
    }

    /*
    ICON("$documenttype", "icon", 0, 20), 
    DOCUMENT(Document_.name.getName(), "common.field.document", 1, 80), 
    DATE(Document_.documentDate.getName(), "common.field.date", 2, 80), 
    NAME(Document_.addressFirstLine.getName(), "common.field.name", 3, 200), 
    STATE("$status", "common.field.state", 4, 100),
    TOTAL(Document_.totalValue.getName(), "common.field.total", 5, 70),
    PRINTED("$printed", "common.field.printed", 6, 60),

     */
}
