package com.sebulli.fakturama.misc;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;

public class E4Utils {
	 
	/**
	 * Finds the top Eclipse context.
	 * @return the eclipse context.
	 */
	public static IEclipseContext getTopContext() {
		return E4Workbench.getServiceContext();
 
	}
}