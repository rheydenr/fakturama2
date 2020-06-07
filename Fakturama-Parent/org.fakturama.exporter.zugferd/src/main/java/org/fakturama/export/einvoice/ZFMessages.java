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

package org.fakturama.export.einvoice;

import org.eclipse.e4.core.services.nls.Message;

/**
 * This class contains all the message keys from
 * OSGI-INF/l10n/bundle*.properties for a convenient access to translated
 * strings.
 * 
 */
@Message
public class ZFMessages {
	public String zugferdExportCommandTitle;
	public String zugferdExportErrorWrongpath;
	public String zugferdExportErrorCancelled;
	public String zugferdExportInfoSuccessfully;
	public String zugferdExportWarningChooseinvoice;
	public String zugferdExportWarningOverwrite;
	public String zugferdExportLabelRebate;

	public String zugferdPreferencesProfile;
	public String zugferdPreferencesTitle;
	public String zugferdPreferencesVersion;
	public String zugferdPreferencesTestmode;
    public String zugferdPreferencesIsActive;
}
