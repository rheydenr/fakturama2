package com.sebulli.fakturama.browser;

import java.io.IOException;
import java.net.URL;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.util.Util;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Display;

import com.sebulli.fakturama.ui.dialogs.WorkbenchMessages;

/**
 * The default implementation of the web browser instance.
 * <p>
 * This class is used when no alternative implementation is plugged in via the
 * 'org.eclipse.ui.browserSupport' extension point.
 * </p>
 *
 * @since 3.1
 */
public class DefaultWebBrowser extends AbstractWebBrowser {
	private DefaultWorkbenchBrowserSupport support;

	private String webBrowser;

	private boolean webBrowserOpened;

	/**
	 * Creates the browser instance.
	 *
	 * @param support
	 * @param id
	 */
	public DefaultWebBrowser(DefaultWorkbenchBrowserSupport support, String id) {
		super(id);
		this.support = support;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.browser.IWebBrowser#openURL(java.net.URL)
	 */
	@Override
	public void openURL(URL url) throws E4PartInitException {
		// format the href for an html file (file:///<filename.html>
		// required for Mac only.
		String href = url.toString();
		if (href.startsWith("file:")) { //$NON-NLS-1$
			href = href.substring(5);
			while (href.startsWith("/")) { //$NON-NLS-1$
				href = href.substring(1);
			}
			href = "file:///" + href; //$NON-NLS-1$
		}
		final String localHref = href;

		final Display d = Display.getCurrent();

		if (Util.isWindows()) {
			Program.launch(localHref);
		} else if (Util.isMac()) {
			try {
				Runtime.getRuntime().exec("/usr/bin/open " + localHref); //$NON-NLS-1$
			} catch (IOException e) {
				throw new E4PartInitException(
						WorkbenchMessages.ProductInfoDialog_unableToOpenWebBrowser,
						e);
			}
		} else {
			Thread launcher = new Thread("About Link Launcher") {//$NON-NLS-1$
				@Override
				public void run() {
					try {
						/*
						 * encoding the href as the browser does not open if
						 * there is a space in the url. Bug 77840
						 */
						String encodedLocalHref = urlEncodeForSpaces(localHref
								.toCharArray());
						if (webBrowserOpened) {
							Runtime
									.getRuntime()
									.exec(
											webBrowser
													+ " -remote openURL(" + encodedLocalHref + ")"); //$NON-NLS-1$ //$NON-NLS-2$
						} else {
							Process p = openWebBrowser(encodedLocalHref);
							webBrowserOpened = true;
							try {
								if (p != null) {
									p.waitFor();
								}
							} catch (InterruptedException e) {
								openWebBrowserError(d);
							} finally {
								webBrowserOpened = false;
							}
						}
					} catch (IOException e) {
						openWebBrowserError(d);
					}
				}
			};
			launcher.start();
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.browser.IWebBrowser#close()
	 */
	@Override
	public boolean close() {
		support.unregisterBrowser(this);
		return super.close();
	}

	/**
	 * This method encodes the url, removes the spaces from the url and replaces
	 * the same with <code>"%20"</code>. This method is required to fix Bug
	 * 77840.
	 *
	 */
	private String urlEncodeForSpaces(char[] input) {
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

	// TODO: Move browser support from Help system, remove this method
	private Process openWebBrowser(String href) throws IOException {
		Process p = null;
		if (webBrowser == null) {
			try {
				webBrowser = "firefox"; //$NON-NLS-1$
				p = Runtime.getRuntime().exec(webBrowser + "  " + href); //$NON-NLS-1$;
			} catch (IOException e) {
				p = null;
				webBrowser = "mozilla"; //$NON-NLS-1$
			}
		}

		if (p == null) {
			try {
				p = Runtime.getRuntime().exec(webBrowser + " " + href); //$NON-NLS-1$;
			} catch (IOException e) {
				p = null;
				webBrowser = "netscape"; //$NON-NLS-1$
			}
		}

		if (p == null) {
			try {
				p = Runtime.getRuntime().exec(webBrowser + " " + href); //$NON-NLS-1$;
			} catch (IOException e) {
				p = null;
				throw e;
			}
		}

		return p;
	}

	/**
	 * display an error message
	 */
	private void openWebBrowserError(Display display) {
		display.asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog
						.openError(
								null,
								WorkbenchMessages.ProductInfoDialog_errorTitle,
								WorkbenchMessages.ProductInfoDialog_unableToOpenWebBrowser);
			}
		});
	}
}
