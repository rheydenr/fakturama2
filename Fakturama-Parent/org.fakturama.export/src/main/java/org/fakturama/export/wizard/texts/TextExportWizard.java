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
 
package org.fakturama.export.wizard.texts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.workbench.IWorkbench;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.fakturama.export.ExportMessages;
import org.fakturama.export.wizard.EmptyWizardPage;
import org.fakturama.wizards.IExportWizard;
import org.fakturama.wizards.IFakturamaWizardService;

import com.sebulli.fakturama.i18n.Messages;

/**
 * Exporter for text blocks.
 */
public class TextExportWizard extends Wizard implements IExportWizard{

    @Inject
    @Translation
    protected Messages msg;
    
    @Inject
    @Translation
    protected ExportMessages exportMessages;
    
    // The first (and only) page of this wizard
    private EmptyWizardPage page1;

    @Inject
    private IEclipseContext ctx;

    @PostConstruct
    @Override
    public void init(IWorkbench workbench, @Optional IStructuredSelection selection) {
        setWindowTitle(msg.pageExport);
        ctx.set(IFakturamaWizardService.WIZARD_PREVIEW_IMAGE, null);

        ctx.set(IFakturamaWizardService.WIZARD_TITLE, exportMessages.wizardExportTextHeading);
        ctx.set(IFakturamaWizardService.WIZARD_DESCRIPTION, exportMessages.wizardExportTextDescription);
        page1 = ContextInjectionFactory.make(EmptyWizardPage.class, ctx);
        addPage(page1);
    }

    @Override
    public boolean performFinish() {
        TextExporter exporter = ContextInjectionFactory.make(TextExporter.class, ctx);
        return exporter.export();
    }

}
