package com.sebulli.fakturama.ui.dialogs.about.internal.e3;

import java.io.PrintWriter;
import java.util.Collection;

import org.eclipse.e4.core.services.about.AboutSections;
import org.eclipse.e4.core.services.about.ISystemInformation;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import com.sebulli.fakturama.ui.dialogs.WorkbenchMessages;

/**
 * This class puts basic platform information into the system summary log. This
 * includes sections for the java properties, the ids of all installed features
 * and plugins, as well as a the current contents of the preferences service.
 * 
 * @since 3.0
 */
public class ConfigurationLogDefaultSection implements ISystemSummarySection {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.about.ISystemSummarySection#write(java.io.PrintWriter)
	 */
	@Override
	public void write(PrintWriter writer) {
		appendProperties(writer);
		appendFeatures(writer);
		appendRegistry(writer);
		appendUserPreferences(writer);
	}

	/**
	 * Appends the <code>System</code> properties.
	 */
	private void appendProperties(PrintWriter writer) {
		writer.println();
		writer.println(WorkbenchMessages.SystemSummary_systemProperties);
		appendSection(AboutSections.SECTION_SYSTEM_PROPERTIES, writer);
	}

	/**
	 * Appends the installed and configured features.
	 */
	private void appendFeatures(PrintWriter writer) {
		writer.println();
		writer.println(WorkbenchMessages.SystemSummary_features);
		appendSection(AboutSections.SECTION_INSTALLED_FEATURES, writer);
	}

	/**
	 * Appends the contents of the Plugin Registry.
	 */
	private void appendRegistry(PrintWriter writer) {
		writer.println();
		writer.println(WorkbenchMessages.SystemSummary_pluginRegistry);
		appendSection(AboutSections.SECTION_INSTALLED_BUNDLES, writer);
	}

	/**
	 * Appends the preferences. Reads from System Information Service
	 */
	private void appendUserPreferences(PrintWriter writer) {
		writer.println();
		writer.println(WorkbenchMessages.SystemSummary_userPreferences);
		appendSection(AboutSections.SECTION_USER_PREFERENCES, writer);
	}

	private void appendSection(String section, PrintWriter writer) {
		BundleContext bundleContext = FrameworkUtil.getBundle(getClass()).getBundleContext();
		try {
			Collection<ServiceReference<ISystemInformation>> serviceReferences = bundleContext.getServiceReferences(ISystemInformation.class,
				 		AboutSections.createSectionFilter(section));
			ServiceReference<ISystemInformation> ref = serviceReferences.iterator().next();
			ISystemInformation service = bundleContext.getService(ref);
			try {
				service.append(writer);
			} finally {
				bundleContext.ungetService(ref);
			}
		} catch (InvalidSyntaxException e) {
			writer.println(String.format("Error reading section %s: %s", section, e.toString()));//$NON-NLS-1$		
		}
	}
}
