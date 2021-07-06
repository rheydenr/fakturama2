/*
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2021 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation
 */

package org.fakturama.export.wizard.texts;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.dialogs.MessageDialog;
import org.fakturama.export.ExportMessages;
import org.fakturama.export.wizard.OOCalcExporter;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.dao.TextsDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.TextModule;

/**
 *
 */
public class TextExporter extends OOCalcExporter {

    @Inject
    @Translation
    protected Messages msg;
    
    @Inject
    @Translation
    protected ExportMessages exportMessages;
    
    @Inject
    private TextsDAO textsDAO;

    public boolean export() {

        // Try to generate a spreadsheet
        if (!createSpreadSheet())
            return false;
        
        List<TextModule> texts = textsDAO.findAll();
        
        // if no data, return immediately
        if(texts.isEmpty()) {
            MessageDialog.openInformation(shell, msg.dialogMessageboxTitleInfo, exportMessages.wizardCommonNodata);
            return true;
        }
        
        // Fill the first 4 rows with the company information
        fillCompanyInformation(0);
        
        // Counter for the current row and columns in the Calc document
        int row = 9;
        int col = 0;

        setCellTextInBold(row, col++, exportMessages.wizardExportTextCategory);
        setCellTextInBold(row++, col, exportMessages.wizardExportTextHeading);
        
        for (TextModule textModule : texts) {
            col = 0;
            setCellText(row, col++, CommonConverter.getCategoryName(textModule.getCategories(), "/"));
            setCellText(row++, col, textModule.getText());
        }

        save();

        // True = Export was successful
        return true;
    }
    
    
    @Override
    protected String getOutputFileName() {
        return exportMessages.wizardExportTextFilename;
    }

}
