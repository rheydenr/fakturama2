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

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Ralf
 *
 */
public class SecondSampleWizard extends Wizard {
    
    @Override
    public void addPages() {
        addPage(new SecondNormalWizardPage());
    }
    
    @Override
    public boolean performFinish() {
        return false;
    }
    
    
	
	static class SecondNormalWizardPage extends WizardPage {

        protected SecondNormalWizardPage() {
            super("Second Page: Normal Wizard Page");
            setTitle("Second Page: Normal Wizard Page");
            setDescription("This is the second page, although the user thinks "
                    + "it's still the same wizard this is actually a completely"
                    + "different one!");
        }

        @Override
        public void createControl(Composite parent) {
            Composite composite = new Composite(parent, SWT.NONE);
            
            Label l = new Label(composite, SWT.NONE);
            l.setText("Path");
            new Text(composite, SWT.BORDER);
            
            GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(composite);
        
            setControl(composite);
        }
        
    }

}
