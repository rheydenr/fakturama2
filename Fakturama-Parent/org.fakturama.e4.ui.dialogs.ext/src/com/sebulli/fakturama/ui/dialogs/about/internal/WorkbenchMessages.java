/*******************************************************************************
 * Copyright (c) 2005, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Sebastian Davids - bug 128529
 * Semion Chichelnitsky (semion@il.ibm.com) - bug 278064
 * Tristan Hume - <trishume@gmail.com> -
 * 		Fix for Bug 2369 [Workbench] Would like to be able to save workspace without exiting
 * 		Implemented workbench auto-save to correctly restore state in case of crash.
 *******************************************************************************/
package com.sebulli.fakturama.ui.dialogs.about.internal;

import org.eclipse.osgi.util.NLS;

/**
 * Message class for workbench messages. These messages are used throughout the
 * workbench.
 * 
 * This is a stripped version from original E3 class.
 *
 */
public class WorkbenchMessages extends NLS {
	private static final String BUNDLE_NAME = "com.sebulli.fakturama.ui.dialogs.about.internal.messages";    //$NON-NLS-1$

	public static String BundleSigningTray_Cant_Find_Service;

	public static String BundleSigningTray_Determine_Signer_For;

	public static String BundleSigningTray_Signing_Certificate;

	public static String BundleSigningTray_Signing_Date;

	public static String BundleSigningTray_Unget_Signing_Service;

	public static String BundleSigningTray_Unknown;

	public static String BundleSigningTray_Unsigned;

	public static String BundleSigningTray_Working;

	// --- Help Menu ---
	public static String AboutAction_text;
	public static String AboutAction_toolTip;
	public static String HelpContentsAction_text;
	public static String HelpContentsAction_toolTip;
	public static String HelpSearchAction_text;
	public static String HelpSearchAction_toolTip;
	public static String DynamicHelpAction_text;
	public static String DynamicHelpAction_toolTip;
	public static String AboutDialog_shellTitle;
	public static String AboutDialog_defaultProductName;

	public static String AboutDialog_DetailsButton;
	public static String ProductInfoDialog_errorTitle;
	public static String ProductInfoDialog_unableToOpenWebBrowser;
	public static String PreferencesExportDialog_ErrorDialogTitle;
	public static String AboutPluginsDialog_shellTitle;
	public static String AboutPluginsDialog_pluginName;
	public static String AboutPluginsDialog_pluginId;
	public static String AboutPluginsDialog_version;
	public static String AboutPluginsDialog_signed;
	public static String AboutPluginsDialog_provider;
	public static String AboutPluginsDialog_state_installed;
	public static String AboutPluginsDialog_state_resolved;
	public static String AboutPluginsDialog_state_starting;
	public static String AboutPluginsDialog_state_stopping;
	public static String AboutPluginsDialog_state_uninstalled;
	public static String AboutPluginsDialog_state_active;
	public static String AboutPluginsDialog_state_unknown;
	public static String AboutPluginsDialog_moreInfo;
	public static String AboutPluginsDialog_signingInfo_show;
	public static String AboutPluginsDialog_signingInfo_hide;
	public static String AboutPluginsDialog_columns;
	public static String AboutPluginsDialog_errorTitle;
	public static String AboutPluginsDialog_unableToOpenFile;
	public static String AboutPluginsDialog_filterTextMessage;
	public static String AboutFeaturesDialog_shellTitle;
	public static String AboutFeaturesDialog_featureName;
	public static String AboutFeaturesDialog_featureId;
	public static String AboutFeaturesDialog_version;
	public static String AboutFeaturesDialog_signed;
	public static String AboutFeaturesDialog_provider;
	public static String AboutFeaturesDialog_moreInfo;
	public static String AboutFeaturesDialog_pluginsInfo;
	public static String AboutFeaturesDialog_columns;
	public static String AboutFeaturesDialog_noInformation;
	public static String AboutFeaturesDialog_pluginInfoTitle;
	public static String AboutFeaturesDialog_pluginInfoMessage;
	public static String AboutFeaturesDialog_noInfoTitle;

	public static String AboutFeaturesDialog_SimpleTitle;
	public static String AboutSystemDialog_browseErrorLogName;
	public static String AboutSystemDialog_copyToClipboardName;
	public static String AboutSystemDialog_noLogTitle;
	public static String AboutSystemDialog_noLogMessage;

	public static String AboutSystemPage_FetchJobTitle;

	public static String AboutSystemPage_RetrievingSystemInfo;

	// ==============================================================================
	// Dialogs
	// ==============================================================================
	public static String Error;
	public static String Information;

	public static String InstallationDialog_ShellTitle;

	public static String Workbench_NeedsClose_Title;
	public static String Workbench_NeedsClose_Message;

	public static String ErrorPreferencePage_errorMessage;

	public static String ListSelection_title;
	public static String ListSelection_message;

	public static String SelectionDialog_selectLabel;
	public static String SelectionDialog_deselectLabel;

	public static String ElementTreeSelectionDialog_nothing_available;

	public static String CheckedTreeSelectionDialog_nothing_available;
	public static String CheckedTreeSelectionDialog_select_all;
	public static String CheckedTreeSelectionDialog_deselect_all;

	// =================================================================
	// System Summary
	// =================================================================
	public static String SystemSummary_title;
	public static String SystemSummary_timeStamp;
	public static String SystemSummary_systemProperties;
	public static String SystemSummary_features;
	public static String SystemSummary_pluginRegistry;
	public static String SystemSummary_userPreferences;
	public static String SystemSummary_sectionTitle;
	public static String SystemSummary_sectionError;

	// parameter 0 is the feature name, parameter 1 is the version and parameter
	// 2 is the Id
	public static String SystemSummary_featureVersion;
	public static String SystemMenuMovePane_PaneName;

	public static String SystemSummary_descriptorIdVersionState;


	public static String WorkbenchPlugin_extension;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, WorkbenchMessages.class);
	}

}
