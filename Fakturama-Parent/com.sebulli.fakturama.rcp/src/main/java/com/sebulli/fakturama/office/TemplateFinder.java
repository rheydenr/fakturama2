/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2021 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.office;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.DocumentType;

/**
 *
 */
public class TemplateFinder {
    public static final String OO_TEMPLATE_FILEEXTENSION = ".ott";
    public static final String TXT_TEMPLATE_FILEEXTENSION = ".txt";

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private ILogger log;

    @Inject @Optional
    private IPreferenceStore preferences;

    /**
     * Scans the template path for all templates. If a template exists, add it
     * to the list of available templates
     * 
     * @param templatePath
     *            path which is scanned
     * @param extension 
     */
    private List<Path> scanPathForTemplates(Path templatePath, String extension) {
        List<Path> templates = new ArrayList<>();
        try {
            if(Files.exists(templatePath)) {
                templates = Files.list(templatePath).filter(f -> f.getFileName().toString().toLowerCase().endsWith(extension))
                        .sorted(Comparator.comparing((Path p) -> p.getFileName().toString().toLowerCase()))
                        .collect(Collectors.toList());
            }
        } catch (IOException e) {
            log.error(e, "Error while scanning the templates directory: " + templatePath.toString());
        }
        return templates;
    }
    
    /**
     * Collects all templates for a given {@link DocumentType}.
     * @return List of template paths
     */
    public List<Path> collectTemplates(DocumentType documentType) {
        return collectTemplates(documentType, OO_TEMPLATE_FILEEXTENSION);
    }
    
    public List<Path> collectTemplates(DocumentType documentType, String extension) {
            String workspace = preferences.getString(Constants.GENERAL_WORKSPACE);
            Path templatePath1 = Paths.get(workspace, getRelativeFolder(documentType));
            Path templatePath2 = Paths.get(workspace, getLocalizedRelativeFolder(documentType));

            // If the name of the localized folder is equal to "Templates",
            // don't search 2 times.
            List<Path> templates = scanPathForTemplates(templatePath1, extension);
            if (!templatePath1.equals(templatePath2))
                templates.addAll(scanPathForTemplates(templatePath2, extension));
            
        return templates;
    }

    /**
     * Get the relative path of the template
     * 
     * @param doctype
     *            The doctype defines the path
     * @return The path as string
     */
    public String[] getRelativeFolder(DocumentType doctype) {
        return new String[]{"/Templates", StringUtils.capitalize(doctype.getTypeAsString().toLowerCase())};
    }

    /**
     * Get the localized relative path of the template
     * 
     * @param doctype
     *            The doctype defines the path
     * @return The path as string
     */
    public String[] getLocalizedRelativeFolder(DocumentType doctype) {
        return new String[]{msg.configWorkspaceTemplatesName, msg.getMessageFromKey(DocumentType.getString(doctype))};
    }

}
