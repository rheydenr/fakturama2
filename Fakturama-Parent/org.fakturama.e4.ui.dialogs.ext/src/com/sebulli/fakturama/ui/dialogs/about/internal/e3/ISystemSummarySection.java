package com.sebulli.fakturama.ui.dialogs.about.internal.e3;

import java.io.PrintWriter;

/**
 * Extensions to <code>org.eclipse.ui.systemSummaryExtensions</code> must
 * provide an implementation of this interface. The class must provide a default
 * constructor. A new instance of the class will be created each time the system
 * summary is created.
 * 
 * @since 3.0
 */
public interface ISystemSummarySection {
	/**
	 * A method that puts the section's information into the system summary's
	 * configuration details log.
	 * 
	 * @param writer
	 *            puts information into the system summary
	 */
	public void write(PrintWriter writer);
}
