package com.sebulli.fakturama.dialogs.about;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceColors;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.ui.IE4ApplicationInfo;

/**
 * Displays information about the product.
 */
public class E4AboutDialog extends TrayDialog {
	private final static int TEXT_MARGIN = 5;

//	@Inject
//	private IProductPreferencesService productPreferencesService;
	
//	@Inject
//	private IBundleGroupProvider bundleGroupProvider;
	
	@Inject
	protected EMenuService menuService;
	
//	@Inject
//	protected MMenuFactory mf;

	@Inject
	@Translation
	protected Messages msg;
	
	protected IE4ApplicationInfo applicationInfo;

    private final static int MAX_IMAGE_WIDTH_FOR_TEXT = 250;

    private final static int DETAILS_ID = IDialogConstants.CLIENT_ID + 1;

    private String productName;

    private E4AboutBundleGroupData[] bundleGroupInfos;

    private List<Image> images = new ArrayList<Image>();

    private AboutFeaturesButtonManager buttonManager = new AboutFeaturesButtonManager();

    private StyledText text;

    private AboutTextManager aboutTextManager;
    private AboutItem item;
    
    /**
     * Create an instance of the AboutDialog for the given window.
     * @param parentShell The parent of the dialog.
     */
    @Inject
    public E4AboutDialog(Shell parentShell, IE4ApplicationInfo applicationInfo) {
        super(parentShell);
        this.applicationInfo = applicationInfo;
		productName = applicationInfo.getProductName();
        if (productName == null) {
			productName = msg.helpAboutdialogDefaultproductname; // WorkbenchMessages.AboutDialog_defaultProductName;
		}

        // create a descriptive object for each BundleGroup
        IBundleGroupProvider[] providers = Platform.getBundleGroupProviders();
		LinkedList<E4AboutBundleGroupData> groups = new LinkedList<E4AboutBundleGroupData>();
        if (providers != null) {
			for (IBundleGroupProvider provider : providers) {
                IBundleGroup[] bundleGroups = provider.getBundleGroups();
                for (IBundleGroup bundleGroup : bundleGroups) {
					groups.add(new E4AboutBundleGroupData(bundleGroup));
				}
            }
		}
        bundleGroupInfos = groups
                .toArray(new E4AboutBundleGroupData[0]);
    }

    @Override
	protected void buttonPressed(int buttonId) {
        switch (buttonId) {
        case DETAILS_ID:
//			BusyIndicator.showWhile(getShell().getDisplay(), new Runnable() {
//				@Override
//				public void run() {
//					IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
//					InstallationDialog dialog = new InstallationDialog(getShell(), workbenchWindow);
//					dialog.setModalParent(AboutDialog.this);
//					dialog.open();
//				}
//			});
            break;
        default:
            super.buttonPressed(buttonId);
            break;
        }
    }

    @Override
	public boolean close() {
        // dispose all images
        for (int i = 0; i < images.size(); ++i) {
            Image image = images.get(i);
            image.dispose();
        }

        return super.close();
    }

    @Override
	protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(MessageFormat.format(msg.helpAboutdialogShelltitle, productName));
//        PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell,
//				IWorkbenchHelpContextIds.ABOUT_DIALOG);
    }

    /**
     * Add buttons to the dialog's button bar.
     *
     * Subclasses should override.
     *
     * @param parent
     *            the button bar composite
     */
    @Override
	protected void createButtonsForButtonBar(Composite parent) {
        parent.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

//        createButton(parent, DETAILS_ID, msg.helpAboutdialogDetailsbutton, false);

        Label l = new Label(parent, SWT.NONE);
        l.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        GridLayout layout = (GridLayout) parent.getLayout();
        layout.numColumns++;
        layout.makeColumnsEqualWidth = false;

        Button b = createButton(parent, IDialogConstants.OK_ID,
                IDialogConstants.OK_LABEL, true);
        b.setFocus();
    }

    @Override
	protected Control createDialogArea(Composite parent) {
         // brand the about box if there is product info
        Image aboutImage = null;
        item = null;
        if (applicationInfo != null) {
//        	ProductFileAdvice adv = createProductAdvice();
        	
            ImageDescriptor imageDescriptor = applicationInfo.getAboutImage();
            if (imageDescriptor != null) {
				aboutImage = imageDescriptor.createImage();
			}

            // if the about image is small enough, then show the text
            if (aboutImage == null
                    || aboutImage.getBounds().width <= MAX_IMAGE_WIDTH_FOR_TEXT) {
                String aboutText = applicationInfo.getAboutText();
                if (aboutText != null) {
					item = AboutTextManager.scan(aboutText);
				}
            }

            if (aboutImage != null) {
				images.add(aboutImage);
			}
        }

        // create a composite which is the parent of the top area and the bottom
        // button bar, this allows there to be a second child of this composite with
        // a banner background on top but not have on the bottom
        Composite workArea = new Composite(parent, SWT.NONE);
        GridLayout workLayout = new GridLayout();
        workLayout.marginHeight = 0;
        workLayout.marginWidth = 0;
        workLayout.verticalSpacing = 0;
        workLayout.horizontalSpacing = 0;
        workArea.setLayout(workLayout);
        workArea.setLayoutData(new GridData(GridData.FILL_BOTH));

        // page group
        Color background = JFaceColors.getBannerBackground(parent.getDisplay());
        Color foreground = JFaceColors.getBannerForeground(parent.getDisplay());
        Composite top = (Composite) super.createDialogArea(workArea);

        // override any layout inherited from createDialogArea
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        top.setLayout(layout);
        top.setLayoutData(new GridData(GridData.FILL_BOTH));
        top.setBackground(background);
        top.setForeground(foreground);

        // the image & text
        final Composite topContainer = new Composite(top, SWT.NONE);
        topContainer.setBackground(background);
        topContainer.setForeground(foreground);

        layout = new GridLayout();
        layout.numColumns = (aboutImage == null || item == null ? 1 : 2);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.verticalSpacing = 0;
        layout.horizontalSpacing = 0;
        topContainer.setLayout(layout);


        GC gc = new GC(parent);
        // arbitrary default
        int topContainerHeightHint = 100;
        try {
			// default height enough for 6 lines of text
			topContainerHeightHint = Math.max(topContainerHeightHint, gc
					.getFontMetrics().getHeight() * 6);
        }
        finally {
        	gc.dispose();
        }

        //image on left side of dialog
        if (aboutImage != null) {
            Label imageLabel = new Label(topContainer, SWT.NONE);
            imageLabel.setBackground(background);
            imageLabel.setForeground(foreground);

            GridData data = new GridData();
            data.horizontalAlignment = GridData.FILL;
            data.verticalAlignment = GridData.BEGINNING;
            data.grabExcessHorizontalSpace = false;
            imageLabel.setLayoutData(data);
            imageLabel.setImage(aboutImage);
            topContainerHeightHint = Math.max(topContainerHeightHint, aboutImage.getBounds().height);
        }

        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        data.verticalAlignment = GridData.FILL;
        data.grabExcessHorizontalSpace = true;
        data.grabExcessVerticalSpace = true;
        data.heightHint = topContainerHeightHint;
        topContainer.setLayoutData(data);

        if (item != null) {
			final int minWidth = 400; // This value should really be calculated
        	// from the computeSize(SWT.DEFAULT,
        	// SWT.DEFAULT) of all the
        	// children in infoArea excluding the
        	// wrapped styled text
        	// There is no easy way to do this.
        	final ScrolledComposite scroller = new ScrolledComposite(topContainer,
    				SWT.V_SCROLL | SWT.H_SCROLL);
        	data = new GridData(GridData.FILL_BOTH);
        	data.widthHint = minWidth;
    		scroller.setLayoutData(data);

    		final Composite textComposite = new Composite(scroller, SWT.NONE);
    		textComposite.setBackground(background);

    		layout = new GridLayout();
    		textComposite.setLayout(layout);

    		text = new StyledText(textComposite, SWT.MULTI | SWT.WRAP | SWT.READ_ONLY);
    		configureText(topContainer);

    		// Adjust the scrollbar increments
    		scroller.getHorizontalBar().setIncrement(20);
    		scroller.getVerticalBar().setIncrement(20);

    		final boolean[] inresize = new boolean[1]; // flag to stop unneccesary
    		// recursion
    		textComposite.addControlListener(new ControlAdapter() {
    			@Override
				public void controlResized(ControlEvent e) {
    				if (inresize[0]) {
						return;
					}
    				inresize[0] = true;
    				// required because of bugzilla report 4579
    				textComposite.layout(true);
    				// required because you want to change the height that the
    				// scrollbar will scroll over when the width changes.
    				int width = textComposite.getClientArea().width;
    				Point p = textComposite.computeSize(width, SWT.DEFAULT);
    				scroller.setMinSize(minWidth, p.y);
    				inresize[0] = false;
    			}
    		});

    		scroller.setExpandHorizontal(true);
    		scroller.setExpandVertical(true);
    		Point p = textComposite.computeSize(minWidth, SWT.DEFAULT);
    		textComposite.setSize(p.x, p.y);
    		scroller.setMinWidth(minWidth);
    		scroller.setMinHeight(p.y);

    		scroller.setContent(textComposite);
        }

        // horizontal bar
        Label bar = new Label(workArea, SWT.HORIZONTAL | SWT.SEPARATOR);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        bar.setLayoutData(data);

        // add image buttons for bundle groups that have them
        Composite bottom = (Composite) super.createDialogArea(workArea);
        // override any layout inherited from createDialogArea
        layout = new GridLayout();
        bottom.setLayout(layout);
        data = new GridData();
        data.horizontalAlignment = SWT.FILL;
        data.verticalAlignment = SWT.FILL;
        data.grabExcessHorizontalSpace = true;

        bottom.setLayoutData(data);

        createFeatureImageButtonRow(bottom);

        // spacer
        bar = new Label(bottom, SWT.NONE);
        data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        bar.setLayoutData(data);

        return workArea;
    }

	void configureText(final Composite parent) {
		// Don't set caret to 'null' as this causes
		// https://bugs.eclipse.org/293263.
		// text.setCaret(null);
		Color background = JFaceColors.getBannerBackground(parent.getDisplay());
		Color foreground = JFaceColors.getBannerForeground(parent.getDisplay());

		text.setFont(parent.getFont());
		text.setText(item.getText());
		text.setCursor(null);
		text.setBackground(background);
		text.setForeground(foreground);
		text.setMargins(TEXT_MARGIN, TEXT_MARGIN, TEXT_MARGIN, 0);

		aboutTextManager = new AboutTextManager(text);
		aboutTextManager.setItem(item);

		createTextMenu();

		GridData gd = new GridData();
		gd.verticalAlignment = GridData.BEGINNING;
		gd.horizontalAlignment = GridData.FILL;
		gd.grabExcessHorizontalSpace = true;
		text.setLayoutData(gd);
	}

    /**
	 * Create the context menu for the text widget.
	 *
	 * @since 3.4
	 */
	private void createTextMenu() {
//		MPopupMenu popupMenu = mf.createPopupMenu();
//		popupMenu.
//		menuService.registerContextMenu(parent, menuId);
//		final MenuManager textManager = new MenuManager();
//		textManager.add(new CommandContributionItem(
//				new CommandContributionItemParameter(PlatformUI
//						.getWorkbench(), null, IWorkbenchCommandConstants.EDIT_COPY,
//						CommandContributionItem.STYLE_PUSH)));
//		textManager.add(new CommandContributionItem(
//				new CommandContributionItemParameter(PlatformUI
//						.getWorkbench(), null, IWorkbenchCommandConstants.EDIT_SELECT_ALL,
//						CommandContributionItem.STYLE_PUSH)));
//		text.setMenu(textManager.createContextMenu(text));
//		text.addDisposeListener(new DisposeListener() {
//
//			@Override
//			public void widgetDisposed(DisposeEvent e) {
//				textManager.dispose();
//			}
//		});

	}

	private void createFeatureImageButtonRow(Composite parent) {
        Composite featureContainer = new Composite(parent, SWT.NONE);
        RowLayout rowLayout = new RowLayout();
        rowLayout.wrap = true;
        featureContainer.setLayout(rowLayout);
        GridData data = new GridData();
        data.horizontalAlignment = GridData.FILL;
        featureContainer.setLayoutData(data);

        for (E4AboutBundleGroupData bundleGroupInfo : bundleGroupInfos) {
			createFeatureButton(featureContainer, bundleGroupInfo);
		}
    }

    private Button createFeatureButton(Composite parent,
            final E4AboutBundleGroupData info) {
        if (!buttonManager.add(info)) {
			return null;
		}

        ImageDescriptor desc = info.getFeatureImage();
        Image featureImage = null;

        Button button = new Button(parent, SWT.FLAT | SWT.PUSH);
        button.setData(info);
        featureImage = desc.createImage();
        images.add(featureImage);
        button.setImage(featureImage);
        button.setToolTipText(info.getProviderName());

        button.getAccessible().addAccessibleListener(new AccessibleAdapter(){
			@Override
			public void getName(AccessibleEvent e) {
				e.result = info.getProviderName();
			}
        });
        button.addSelectionListener(new SelectionAdapter() {
            @Override
			public void widgetSelected(SelectionEvent event) {
                E4AboutBundleGroupData[] groupInfos = buttonManager
                        .getRelatedInfos(info);
                E4AboutBundleGroupData selection = (E4AboutBundleGroupData) event.widget
                        .getData();

//                AboutFeaturesDialog d = new AboutFeaturesDialog(getShell(),
//                        productName, groupInfos, selection);
//                d.open();
            }
        });

        return button;
    }

	@Override
	protected boolean isResizable() {
		return true;
	}
}
