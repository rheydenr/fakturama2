package com.sebulli.fakturama.ui.dialogs.about.internal.e3;

/**
 * These constants define the set of properties that the UI expects to
 * be available via <code>IBundleGroup.getProperty(String)</code>.
 *
 * @since 3.0
 * @see org.eclipse.core.runtime.IBundleGroup#getProperty(String)
 */
public interface IBundleGroupConstants {

    /**
     * The text to show in an "about features" dialog.
     */
    public static final String ABOUT_TEXT = "aboutText"; //$NON-NLS-1$

    /**
     * An image which can be shown in an "about features" dialog (32x32).
     * <p>
     * The value is a fully qualified valid URL.
     * </p>
     */
    public static final String FEATURE_IMAGE = "featureImage"; //$NON-NLS-1$

    /**
     * A help reference for the feature's tips and tricks page (optional).
     */
    public static final String TIPS_AND_TRICKS_HREF = "tipsAndTricksHref"; //$NON-NLS-1$

    /**
     * The feature's welcome page (special XML-based format).
     * <p>
     * The value is a fully qualified valid URL.
     * </p>
     * Products designed to run "headless" typically would not have such a page.
     */
    public static final String WELCOME_PAGE = "welcomePage"; //$NON-NLS-1$

    /**
     * The id of a perspective in which to show the welcome page
     * (optional).
     */
    public static final String WELCOME_PERSPECTIVE = "welcomePerspective"; //$NON-NLS-1$

    /**
     * The URL of the license page for the feature (optional).
     * <p>
     * The value is a fully qualified valid URL.
     * </p>
     */
    public static final String LICENSE_HREF = "licenseHref"; //$NON-NLS-1$

	/**
	 * The feature's branding bundle id (optional).
	 *
	 * @since 3.5
	 */
	public static final String BRANDING_BUNDLE_ID = "brandingBundleId"; //$NON-NLS-1$

	/**
	 * The feature's branding bundle version (optional).
	 *
	 * @since 3.5
	 */
	public static final String BRANDING_BUNDLE_VERSION = "brandingBundleVersion"; //$NON-NLS-1$
}
