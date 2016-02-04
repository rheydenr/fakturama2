/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2016 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
package org.fakturama.export.wizard;

import java.util.List;

import javax.inject.Inject;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.fakturama.export.AbstractWizardNode;
import org.fakturama.export.IFakturamaExportService;

/**
 * Each export wizard is "registered" here to get available in the selection tree. 
 *
 */
public class FakturamaExporterWizards extends WizardSelectionPage {
	
	private IFakturamaExportService exportService;
	
	private AbstractWizardNode selectedWizardNode;

	@Inject
	public FakturamaExporterWizards(IFakturamaExportService exportService) {
        super("First Page: Wizard Selection Page");
        this.exportService = exportService;
        setTitle("First Page: Wizard Selection Page");
        setDescription("This page is the actual wizard selection page. This"
                + " is the page where the user chooses the wizard which will"
                + " be appended to this wizard as of it was a single wizard"
                + " all along.");
    }

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        
        // Name, you can create your form like normally
        Label l = new Label(composite, SWT.NONE);
        l.setText("Name");
        new Text(composite, SWT.BORDER);
        
        // Project type
        l = new Label(composite, SWT.NONE);
        l.setText("Type");
        
        TableViewer projectType = new TableViewer(composite);
        projectType.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
        projectType.addSelectionChangedListener(new ISelectionChangedListener() {
            @Override
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                if(!selection.isEmpty() && selection instanceof IStructuredSelection) {
                    Object o = ((IStructuredSelection) selection).getFirstElement();
                    if(o instanceof IWizardNode) {
                        // Now we set our selected node, which toggles the next button
                        selectedWizardNode = (AbstractWizardNode) o;
                        setTitle(selectedWizardNode.getName());
                        setSelectedNode(selectedWizardNode);
                    }
                }
            }
        });
        projectType.setContentProvider(new ArrayContentProvider());
        projectType.setLabelProvider(new LabelProvider() {
            @Override
            public String getText(Object element) {
                if(element instanceof IWizardNode) {
                    return ((AbstractWizardNode) element).getName();
                }
                return super.getText(element);
            }
        });
        List<AbstractWizardNode> wizardNodes = exportService.getExporterList();
        projectType.setInput(wizardNodes);
        
        GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(composite);
        
        setControl(composite);
    }


}