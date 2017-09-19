package com.sebulli.fakturama.browser;

import java.util.Hashtable;
import java.util.Map;

/**
 * Extends the abstract browser support class by providing minimal support for
 * external browsers.
 * <p>
 * This class is used when no alternative implementation is plugged in via the
 * 'org.eclipse.ui.browserSupport' extension point.
 * </p>
 *
 * @since 3.1
 */
public class DefaultWorkbenchBrowserSupport extends
		AbstractWorkbenchBrowserSupport {
	private Map<String, IWebBrowser> browsers;
	private static final String DEFAULT_BROWSER_ID_BASE = "org.eclipse.ui.defaultBrowser"; //$NON-NLS-1$

	/**
	 * The default constructor.
	 */
	public DefaultWorkbenchBrowserSupport() {
		browsers = new Hashtable<>();
	}

	void registerBrowser(IWebBrowser browser) {
		browsers.put(browser.getId(), browser);
	}

	void unregisterBrowser(IWebBrowser browser) {
		browsers.remove(browser.getId());
	}

	IWebBrowser findBrowser(String id) {
		return (IWebBrowser) browsers.get(id);
	}

	protected IWebBrowser doCreateBrowser(int style, String browserId,
			String name, String tooltip)/* throws PartInitException*/ {
		return new DefaultWebBrowser(this, browserId);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.browser.IWorkbenchBrowserSupport#createBrowser(int,
	 *      java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public IWebBrowser createBrowser(int style, String browserId, String name,
			String tooltip) /*throws PartInitException*/ {
		IWebBrowser browser = findBrowser(browserId == null? getDefaultId():browserId);
		if (browser != null) {
			return browser;
		}
		browser = doCreateBrowser(style, browserId, name, tooltip);
		registerBrowser(browser);
		return browser;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.browser.IWorkbenchBrowserSupport#createBrowser(java.lang.String)
	 */
	@Override
	public IWebBrowser createBrowser(String browserId) /*throws PartInitException*/ {
		return createBrowser(AS_EXTERNAL, browserId, null, null);
	}

	private String getDefaultId() {
		String id = null;
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			id = DEFAULT_BROWSER_ID_BASE + i;
			if (browsers.get(id) == null)
				break;
		}
		return id;
	}
}
