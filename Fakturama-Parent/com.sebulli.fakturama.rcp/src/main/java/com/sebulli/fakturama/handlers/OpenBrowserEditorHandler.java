/*
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2012 Gerd Bartelt
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Gerd Bartelt - initial API and implementation
 */

package com.sebulli.fakturama.handlers;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.jface.preference.IPreferenceStore;

import com.sebulli.fakturama.Activator;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.parts.BrowserEditor;

/**
 * This action opens the project website in an editor.
 * 
 * @author Gerd Bartelt
 */
public class OpenBrowserEditorHandler {

    //T: Text of the action to open the webbrowser
    public final static String ACTIONTEXT = "Web Browser";

    public static final String PARAM_URL = "com.sebulli.fakturama.command.browser.url";
    public static final String PARAM_USE_PROJECT_URL = "com.sebulli.fakturama.command.browser.useprojecturl";

    // URL of the Fakturama project site
    public final static String FAKTURAMA_PROJECT_URL = "http://www.fakturama.info/";

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private EPartService partService;
    
    @Inject @Optional
    private IPreferenceStore preferences;

    /**
     * Run the action
     * 
     * Set the URL and open the editor.
     */
    @Execute
    public void run(@Optional @Named(PARAM_URL) String paramUrl, @Optional @Named(PARAM_USE_PROJECT_URL) String useFakturamaProjectURL,
            final MApplication application, final EModelService modelService) throws ExecutionException {
        // If we had a selection lets open the editor
        MPartStack documentPartStack = (MPartStack) modelService.find(CallEditor.DETAIL_PARTSTACK_ID, application);

        // Sets the URL
        String url;
        if (BooleanUtils.toBoolean(useFakturamaProjectURL))
            url = FAKTURAMA_PROJECT_URL;
        else {
            url = preferences.getString(Constants.PREFERENCES_GENERAL_WEBBROWSER_URL);

            // In case of an empty URL: use the start page
            if (url.isEmpty() || url.equals("http://www.fakturama.org/"))
                url = "file://" + preferences.getString(Constants.GENERAL_WORKSPACE) + "/" + msg.configWorkspaceTemplatesName + "/Start/start.html";

        }

        // In case of an URL with only "-" do not show an editor
        if (url.equals("-"))
            return;

        // Add the "http://" or "file://"
        if ((!url.toLowerCase().startsWith("http://")) && (!url.toLowerCase().startsWith("file://")))
            url = "http://" + url;

        // Check, if the URL is the Fakturama project
        boolean isFakturamaProjectUrl = url.equalsIgnoreCase(FAKTURAMA_PROJECT_URL);

        // Add version and language a a GET parameter
        // The language is uses, if the project website can generate
        // localized content.
        if (isFakturamaProjectUrl) {
            url += "?version=" + Activator.getContext().getBundle().getVersion();
            url += "&lang=" + Locale.getDefault().getCountry();
        }

        // Open the editor
        partService.showPart(createEditorPart(documentPartStack, isFakturamaProjectUrl, url), PartState.ACTIVATE);
    }

    private MPart createEditorPart(MPartStack stack, boolean isFakturamaProjectUrl, String url) {
        // at first we look for an existing Part
        MPart myPart = partService.findPart(BrowserEditor.ID);

        // if not found then we create a new one from a part descriptor
        if (myPart == null) {
            myPart = partService.createPart(CallEditor.DOCVIEW_PARTDESCRIPTOR_ID);
            myPart.setElementId(BrowserEditor.ID);
            myPart.setContext(EclipseContextFactory.create());
            myPart.getProperties().put(PARAM_URL, url);
            myPart.setContributionURI(CallEditor.BASE_CONTRIBUTION_URI + BrowserEditor.class.getName());
            // Sets the URL as input for the editor.
            if (isFakturamaProjectUrl)
                //T: Short description of start page 
                myPart.setLabel(msg.commandBrowserOpenUrllabel);
            else
                //T: Short description of start page 
                myPart.setLabel(msg.commandBrowserOpenStartpage);

            myPart.getProperties().put(PARAM_USE_PROJECT_URL, Boolean.toString(isFakturamaProjectUrl));
            if(stack == null) {
                stack = (MPartStack) partService.createPart(CallEditor.DOCVIEW_PART_ID);
            }
                stack.getChildren().add(myPart);
        }
        else if(myPart.getObject() != null) {
            // If the browser editor is already open, reset the URL
            ((BrowserEditor) myPart.getObject()).resetUrl(url);
        }
        return myPart;
    }
}
