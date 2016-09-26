package com.sebulli.fakturama.browser;

/**
 * Implements <code>IWorkbenchBrowserSupport</code> while leaving some methods
 * to the implementors. Classes that extend this abstract class are meant to be
 * contributed via 'org.eclipse.ui.browserSupport' extension point.
 *
 * @since 3.1
 */
public abstract class AbstractWorkbenchBrowserSupport implements
		IWorkbenchBrowserSupport {

	private static final String SHARED_EXTERNAL_BROWSER_ID = "org.eclipse.ui.externalBrowser"; //$NON-NLS-1$

	/**
	 * The default constructor.
	 */
	public AbstractWorkbenchBrowserSupport() {
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.browser.IWorkbenchBrowserSupport#getExternalBrowser()
	 */
	@Override
	public IWebBrowser getExternalBrowser()/* throws PartInitException */{
		return createBrowser(AS_EXTERNAL, SHARED_EXTERNAL_BROWSER_ID, null,
				null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.browser.IWorkbenchBrowserSupport#isInternalWebBrowserAvailable()
	 */
	@Override
	public boolean isInternalWebBrowserAvailable() {
		return false;
	}
}