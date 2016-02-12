package org.fakturama.export.wizard.contacts;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.graphics.Image;
import org.fakturama.export.AbstractWizardNode;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.resources.core.Icon;
import com.sebulli.fakturama.resources.core.IconSize;

public class AddressListExportWizardNode extends AbstractWizardNode {
	
	@Inject
	private IEclipseContext ctx;
//		
//	@Inject
//	private ILogger log;


    /**
     * This is the wizard that this IWizardNode represents. One reason to
     * keep this reference is because we can check if the wizard is created at the
     * isContentCreated method.
     */
    private IWizard wizard;
    
    public AddressListExportWizardNode() {
    	setCategory("org.fakturama.export.contacts");
    	initialize();
    }
    
    public AddressListExportWizardNode(String name) {
        setName(name);
    }
    
    @PostConstruct
    public void initialize() {
        Bundle bundle = FrameworkUtil.getBundle(this.getClass());
        BundleContext bundleContext = bundle.getBundleContext();
        try {
			Collection<ServiceReference<IEclipseContext>> serviceReferences = bundleContext.getServiceReferences(IEclipseContext.class, null);
			ServiceReference<IEclipseContext> next = serviceReferences.iterator().next();
			ctx = bundleContext.getService(next);
		} catch (InvalidSyntaxException e) {
//			log.error(e);
		}
        Messages msg = ctx.get(Messages.class);
        if(msg != null) {
        	setName(msg.wizardExportContactsName);
        	setDescription(msg.wizardExportContactsDescription);
        }
    }
    
    @Override
    public IWizard getWizard() {
        wizard = ContextInjectionFactory.make(AddressListExportWizard.class, ctx);
        return wizard;
    }

    /**
     * Returns whether a wizard has been created for this node.
     */
    @Override
    public boolean isContentCreated() {
        return wizard != null;
    }
	
	@Override
	public Image getImage() {
		return Icon.COMMAND_CONTACT.getImage(IconSize.DefaultIconSize);
	}
}