/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package com.sebulli.fakturama.ui.dialogs.about.internal.e3;

import java.awt.Desktop;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.FrameworkUtil;

import com.sebulli.fakturama.ui.dialogs.WorkbenchMessages;

/**
 * Manages links in styled text.
 */

public class AboutUtils {

	private final static String ERROR_LOG_COPY_FILENAME = "log"; //$NON-NLS-1$

	/**
	 * Scan the contents of the about text
	 * 
	 * @param s
	 * @return
	 */
	public static AboutItem scan(String s) {
		List<Object> linkRanges = new ArrayList<Object>();
		List<Object> links = new ArrayList<Object>();

		// slightly modified version of jface url detection
		// see org.eclipse.jface.text.hyperlink.URLHyperlinkDetector

		int urlSeparatorOffset = s.indexOf("://"); //$NON-NLS-1$
		while (urlSeparatorOffset >= 0) {

			boolean startDoubleQuote = false;

			// URL protocol (left to "://")
			int urlOffset = urlSeparatorOffset;
			char ch;
			do {
				urlOffset--;
				ch = ' ';
				if (urlOffset > -1)
					ch = s.charAt(urlOffset);
				startDoubleQuote = ch == '"';
			} while (Character.isUnicodeIdentifierStart(ch));
			urlOffset++;

			// Right to "://"
			StringTokenizer tokenizer = new StringTokenizer(s.substring(urlSeparatorOffset + 3), " \t\n\r\f<>", false); //$NON-NLS-1$
			if (!tokenizer.hasMoreTokens())
				return null;

			int urlLength = tokenizer.nextToken().length() + 3 + urlSeparatorOffset - urlOffset;

			if (startDoubleQuote) {
				int endOffset = -1;
				int nextDoubleQuote = s.indexOf('"', urlOffset);
				int nextWhitespace = s.indexOf(' ', urlOffset);
				if (nextDoubleQuote != -1 && nextWhitespace != -1)
					endOffset = Math.min(nextDoubleQuote, nextWhitespace);
				else if (nextDoubleQuote != -1)
					endOffset = nextDoubleQuote;
				else if (nextWhitespace != -1)
					endOffset = nextWhitespace;
				if (endOffset != -1)
					urlLength = endOffset - urlOffset;
			}

			linkRanges.add(new int[] { urlOffset, urlLength });
			links.add(s.substring(urlOffset, urlOffset + urlLength));

			urlSeparatorOffset = s.indexOf("://", urlOffset + urlLength + 1); //$NON-NLS-1$
		}
		return new AboutItem(s, (int[][]) linkRanges.toArray(new int[linkRanges.size()][2]),
				(String[]) links.toArray(new String[links.size()]));
	}

	/**
	 * Open a browser with the argument title on the argument url. If the url
	 * refers to a resource within a bundle, then a temp copy of the file will
	 * be extracted and opened.
	 * 
	 * @see <code>Platform.asLocalUrl</code>
	 * @param url
	 *            The target url to be displayed, null will be safely ignored
	 * @return true if the url was successfully displayed and false otherwise
	 */
	public static boolean openBrowser(Shell shell, URL url) {
		if (url != null) {
			try {
				url = FileLocator.toFileURL(url);
			} catch (IOException e) {
				return false;
			}
		}
		if (url == null) {
			return false;
		}
		openLink(shell, url.toString());
		return true;
	}

	/**
	 * Open a link
	 */
	public static void openLink(Shell shell, String href) {
		// format the href for an html file (file:///<filename.html>
		// required for Mac only.
		if (href.startsWith("file:")) { //$NON-NLS-1$
			href = href.substring(5);
			while (href.startsWith("/")) { //$NON-NLS-1$
				href = href.substring(1);
			}
			href = "file:///" + href; //$NON-NLS-1$
		}
		try {
			Desktop.getDesktop().browse(new URL(urlEncodeForSpaces(href.toCharArray())).toURI());
		} catch (IOException ioe) {
			openWebBrowserError(shell, href, ioe);
		} catch (URISyntaxException urie) {
			openWebBrowserError(shell, href, urie);
		}

	}

	/**
	 * This method encodes the url, removes the spaces from the url and replaces
	 * the same with <code>"%20"</code>. This method is required to fix Bug
	 * 77840.
	 * 
	 * @since 3.0.2
	 */
	private static String urlEncodeForSpaces(char[] input) {
		StringBuffer retu = new StringBuffer(input.length);
		for (int i = 0; i < input.length; i++) {
			if (input[i] == ' ') {
				retu.append("%20"); //$NON-NLS-1$
			} else {
				retu.append(input[i]);
			}
		}
		return retu.toString();
	}

	/**
	 * Returns the result of converting a list of comma-separated tokens into an
	 * array. Used as a replacement for <code>String.split(String)</code>, to
	 * allow compilation against JCL Foundation (bug 80053).
	 * 
	 * @param prop
	 *            the initial comma-separated string
	 * @param separator
	 *            the separator characters
	 * @return the array of string tokens
	 * @since 3.1
	 */
	public static String[] getArrayFromList(String prop, String separator) {
		if (prop == null || prop.trim().equals("")) { //$NON-NLS-1$
			return new String[0];
		}
		List<String> list = new ArrayList<String>();
		StringTokenizer tokens = new StringTokenizer(prop, separator);
		while (tokens.hasMoreTokens()) {
			String token = tokens.nextToken().trim();
			if (!token.equals("")) { //$NON-NLS-1$
				list.add(token);
			}
		}
		return list.isEmpty() ? new String[0] : (String[]) list.toArray(new String[list.size()]);
	}

	public static void handleStatus(String status) {
		handleStatus(status, IStatus.ERROR, null);
	}

	public static void handleStatus(String status, Exception e) {
		handleStatus(status, IStatus.ERROR, e);
	}

	public static void handleStatus(String status, int level) {
		handleStatus(status, level, null);
	}

	public static void handleStatus(String status, int level, Exception e) {
		ILog log = Platform.getLog(FrameworkUtil.getBundle(AboutUtils.class));
		log.log(new Status(level, DialogPlugin.ID, status, e));
	}

	/**
	 * display an error message
	 */
	private static void openWebBrowserError(Shell shell, final String href, final Throwable t) {
		String title = WorkbenchMessages.ProductInfoDialog_errorTitle;
		String msg = NLS.bind(WorkbenchMessages.ProductInfoDialog_unableToOpenWebBrowser, href);

		AboutUtils.handleStatus(title + ": " + msg);
	}

	public static void openErrorLogBrowser(Shell shell) {
		String filename = Platform.getLogFileLocation().toOSString();

		Path log = Paths.get(filename);
		if (Files.exists(log)) {
			// Make a copy of the file with a temporary name.
			// Working around an issue with windows file associations/browser
			// malfunction whereby the browser doesn't open on ".log" and we
			// aren't returned an error.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=97783
			Path logCopy = makeDisplayCopy(log);
			if (logCopy != null) {
				AboutUtils.openLink(shell, logCopy.toUri().toString()); //$NON-NLS-1$
				return;
			}
			// Couldn't make copy, try to open the original log.
			// We try the original in this case rather than putting up an error,
			// because the copy could fail due to an I/O or out of space
			// problem.
			// In that case we may still be able to show the original log,
			// depending on the platform. The risk is that users with
			// configurations that have bug #97783 will still get nothing
			// (vs. an error) but we'd rather
			// try again than put up an error dialog on platforms where the
			// ability to view the original log works just fine.
			AboutUtils.openLink(shell, "file:///" + filename); //$NON-NLS-1$
			return;
		}
		MessageDialog.openInformation(shell, WorkbenchMessages.AboutSystemDialog_noLogTitle,
				NLS.bind(WorkbenchMessages.AboutSystemDialog_noLogMessage, filename));
	}

	/**
	 * Returns a copy of the given file to be used for display in a browser.
	 * 
	 * @return the file, or <code>null</code>
	 */
	private static Path makeDisplayCopy(Path file) {

		IPath path = Platform.getStateLocation(FrameworkUtil.getBundle(AboutUtils.class));

		// TODO tut das selbe wie in der Plugin Methode
		// IPath path = WorkbenchPlugin.getDefault().getDataLocation();

		if (path == null) {
			return null;
		}
		path = path.append(ERROR_LOG_COPY_FILENAME);
		Path copy = path.toFile().toPath();
		
		try {
			return Files.copy(file, copy, StandardCopyOption.REPLACE_EXISTING);
		} catch (FileNotFoundException e) {
			return null;
		} catch (IOException e) {
			return null;
		}
	}
}
