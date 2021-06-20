/**
 * 
 */
package com.sebulli.fakturama.dialogs;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.widgets.ButtonFactory;
import org.eclipse.jface.widgets.CompositeFactory;
import org.eclipse.jface.widgets.LabelFactory;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.nebula.widgets.treemapper.ISemanticTreeMapperSupport;
import org.eclipse.nebula.widgets.treemapper.TreeMapper;
import org.eclipse.nebula.widgets.treemapper.TreeMapperUIConfigProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

import com.sebulli.fakturama.dao.WebshopDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.OrderState;
import com.sebulli.fakturama.model.FakturamaModelPackage;
import com.sebulli.fakturama.model.WebShop;
import com.sebulli.fakturama.model.WebshopStateMapping;
import com.sebulli.fakturama.parts.widget.contentprovider.SimpleTreeContentProvider;
import com.sebulli.fakturama.webshopimport.ExecutionResult;
import com.sebulli.fakturama.webshopimport.WebShopConfig;
import com.sebulli.fakturama.webshopimport.WebShopStatusImporter;
import com.sebulli.fakturama.webshopimport.type.StatusType;
import com.sebulli.fakturama.webshopimport.type.StatusType.Status;
import com.sebulli.fakturama.webshopimport.type.Webshopexport;

/**
 * Settings for Webshop import (dialog within Webshop preferences)
 */
public class WebShopStatusSettingsDialog extends TitleAreaDialog {
	
	@Inject
	@Translation
	private Messages msg;

	@Inject
	public IPreferenceStore preferences;
	
	@Inject
	private WebshopDAO webshopDAO;

	@Inject
	private IEclipseContext context;
    
    @Inject
    private ILogger log;
    
	private Text connectorVersion, shopInfoString;

	private TreeMapper<WebshopOrderStateMapping, WebshopOrderState, OrderState> treeMapper;
	private List<WebshopOrderStateMapping> mappings;
	
	@Inject
	public WebShopStatusSettingsDialog(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell) {
		super(shell);
		setShellStyle(SWT.RESIZE | SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		// Set the title
		// parent.getShell().setSize(500, 500);
		parent.getShell().setText(msg.preferencesWebshopSettings);
		// setTitle(msg.preferencesWebshopSettings);
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = CompositeFactory.newComposite(SWT.NONE)
		        .layout(GridLayoutFactory.swtDefaults().equalWidth(true).spacing(2, SWT.DEFAULT).numColumns(2).create())
		        .layoutData(GridDataFactory.fillDefaults().create())
		        .create(area);

		setMessage(msg.preferencesWebshopSettingsDescription, IMessageProvider.INFORMATION);

		ButtonFactory.newButton(SWT.PUSH)
		        .layoutData(GridDataFactory.swtDefaults().align(SWT.LEFT, SWT.TOP).span(2, 1).create())
		        .text(msg.preferencesWebshopSettingsGetallstates)
		        .onSelect(e -> {
		            Webshopexport wsExport = execute(parent.getShell());
		            if (wsExport != null) {
		                // set the values from web shop
		                shopInfoString.setText(String.format("%s (%s: %s)", wsExport.getWebshop().getShop(),
		                        msg.helpAboutfeaturesdialogVersion,
		                        StringUtils.defaultIfBlank(wsExport.getCompleteVersion(), "unknown")));
		                connectorVersion.setText(wsExport.getVersion());

		                // fill WebshopStateTreeMapper
		                if (wsExport.getStatusList() == null || wsExport.getStatusList().getStatus().isEmpty()) {
		                    log.error("can't get status list from web shop, status list is empty!");
		                    MessageDialog.openError(getParentShell(), msg.dialogMessageboxTitleError,
		                            msg.preferencesWebshopSettingsStateError);
		                } else {
		                    StatusType statusList = wsExport.getStatusList();
		                    List<WebshopOrderState> leftTreeInput = new ArrayList<>(statusList.getStatus().size());
		                    // TODO perhaps a naming confusion?
		                    // statusList is a container for several statuses, getStatus() returns a list of
		                    // statuses
		                    for (Status st : statusList.getStatus()) {
		                        WebshopOrderState webshopOrderState = new WebshopOrderState(st.getId(), st.getName());
		                        leftTreeInput.add(webshopOrderState);
		                    }
		                    // throw away all mappings
		                    mappings.clear();
		                    // and create a new one
		                    setInputAndActivateTreeMapperWidget(leftTreeInput, true);
		                }
		            }
		        })
		        .create(container);

		LabelFactory.newLabel(SWT.READ_ONLY)
	        .text(msg.preferencesWebshopSettingsShopinfo)
	        .create(container);
		shopInfoString = TextFactory.newText(SWT.READ_ONLY)
	        .layoutData(GridDataFactory.fillDefaults().grab(true, false).create())
		    .create(container);

		LabelFactory.newLabel(SWT.READ_ONLY)
		    .text(msg.preferencesWebshopSettingsVersion)
		    .create(container);
		connectorVersion = TextFactory.newText(SWT.READ_ONLY)
	        .layoutData(GridDataFactory.fillDefaults().grab(true, false).create())
	        .create(container);

		// WebshopStateTreeMapper -> disabled if no web shop states are available! 
		createTreeMapperWidget(container);

		return area;
	}
	
	private void createTreeMapperWidget(Composite parent) {
		Display display = parent.getDisplay();
		Label hint = new Label(parent, SWT.NONE);
		hint.setText(msg.preferencesWebshopSettingsAssignstate);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(hint);

		Color gray = display.getSystemColor(SWT.COLOR_GRAY);
		Color blue = display.getSystemColor(SWT.COLOR_BLUE);
		TreeMapperUIConfigProvider uiConfig = new TreeMapperUIConfigProvider(gray, 1, blue, 3);
		mappings = new ArrayList<>();
		ISemanticTreeMapperSupport<WebshopOrderStateMapping, WebshopOrderState, OrderState> semanticSupport = new ISemanticTreeMapperSupport<WebshopOrderStateMapping, WebshopOrderState, OrderState>() {
			@Override
			public WebshopOrderStateMapping createSemanticMappingObject(WebshopOrderState leftItem, OrderState rightItem) {
				// create only one mapping (for the left item); delete old mapping if it was created before!
				for (WebshopOrderStateMapping orderStatesMapping : mappings) {
					if(orderStatesMapping.getLeftItem().equals(leftItem)) {
						mappings.remove(orderStatesMapping);
						break;
					}
				}
				return new WebshopOrderStateMapping(leftItem, rightItem);
			}

			@Override
			public WebshopOrderState resolveLeftItem(WebshopOrderStateMapping semanticMappingObject) {
				return semanticMappingObject.getLeftItem();
			}

			@Override
			public OrderState resolveRightItem(WebshopOrderStateMapping semanticMappingObject) {
				return semanticMappingObject.getRightItem();
			}
		};
		treeMapper = new TreeMapper<>(parent, semanticSupport,
				uiConfig);

		treeMapper.setContentProviders(new WebshopStateContentProvider(), new FktOrderStateContentProvider());
		treeMapper.setLabelProviders(new ViewLabelProvider(), new ViewLabelProvider());
		List<WebshopOrderState> leftTreeInput = new ArrayList<>();
		
		Canvas cv;
		Control[] controls = treeMapper.getControl().getChildren();
		// it's not possible to access the Canvas directly :-(
		for (Control control : controls) {
			if(control instanceof Canvas) {
				cv = (Canvas) control;
				cv.addKeyListener(new KeyAdapter() {
					@Override
					public void keyPressed(KeyEvent e) {
						switch (e.keyCode) {
						case SWT.DEL:
							WebshopOrderStateMapping selectedMapping = (WebshopOrderStateMapping) treeMapper.getSelection().getFirstElement();
							if(selectedMapping != null) {
								mappings.remove(selectedMapping);
								treeMapper.refresh();
							}
							break;
		
						default:
							break;
						}
						super.keyPressed(e);
					}
				});
				break;
			}
		}
		
		treeMapper.getControl().setWeights(new int[] { 2, 1, 2} );
		GridDataFactory.fillDefaults().hint(SWT.DEFAULT, 200).grab(true, true).span(2, 1).applyTo(treeMapper.getControl());

		setInputAndActivateTreeMapperWidget(leftTreeInput);
	}
	
	private boolean createMappings() {
		boolean creationOk = false;
		// pre-check: all states assigned?
		TreeItem[] leftItems = treeMapper.getLeftTreeViewer().getTree().getItems();
		List<String> missingMappings = new ArrayList<>();
		for (TreeItem treeItem : leftItems) {
			boolean found = false;
			for (WebshopOrderStateMapping webshopOrderStateMapping : mappings) {
				if (((WebshopOrderState)treeItem.getData()).getStateName().equals(webshopOrderStateMapping.getLeftItem().getStateName())) {
					found = true;
					break;
				}
			}
			if(!found) {
				// nothing found - warning!
				missingMappings.add(treeItem.getText());
			}
		}
		
		// => falls hier Status aus dem Webshop übrig bleiben (nicht gemappt
		// wurden), sollte man noch einen Hinweis ausgeben!
		if(!missingMappings.isEmpty()) {
			creationOk = MessageDialog.openQuestion(getShell(), msg.dialogMessageboxTitleWarning, 
					msg.preferencesWebshopSettingsAssignstateMissing);
			if(!creationOk) {
				return creationOk;
			}
		}
		
		try {
			
			// TODO
			/*
			 * - at first, give the webshop a name, e.g. by extracting some infos from webshop url
			 * (because we could have more than one webshop from a vendor and therefore we couldn't 
			 * distinguish between all them)
			 * - how could we create a unique name for a shop?
			 * - after this, create a new mapping object and assign it to that webshop (clear the old one). 
			WebShop currentWebshop = webShopDAO.findByName(currentShop);
			currentWebshop.
			 */
			String webShopIdentifier = webshopDAO.createWebShopIdentifier(preferences.getString(Constants.PREFERENCES_WEBSHOP_URL));
			WebShop currentWebshop = webshopDAO.findByName(webShopIdentifier);
			if(currentWebshop == null) {
				currentWebshop = FakturamaModelPackage.MODELFACTORY.createWebShop();
				currentWebshop.setName(webShopIdentifier);
				currentWebshop.setWebshopVendor(shopInfoString.getText());
			} else {
				// at first clear any pre-existing mappings
				currentWebshop = webshopDAO.clearOldMappings(webShopIdentifier);
			}
			for (WebshopOrderStateMapping orderStatesMapping : mappings) {
				WebshopStateMapping webshopStateMapping = FakturamaModelPackage.MODELFACTORY.createWebshopStateMapping();
				webshopStateMapping.setWebshopState(orderStatesMapping.getLeftItem().getId());
				webshopStateMapping.setName(orderStatesMapping.getLeftItem().getStateName());
				webshopStateMapping.setFakturamaOrderState(orderStatesMapping.getRightItem().name());
				webshopStateMapping.setValidFrom(Date.from(Instant.now()));
				currentWebshop.getStateMapping().add(webshopStateMapping);
			}
			webshopDAO.save(currentWebshop);
			creationOk = true;
		} catch (FakturamaStoringException exception) {
			MessageDialog.openError(getShell(), msg.dialogMessageboxTitleError, exception.getDescription());
		}
		return creationOk;
	}
	
	@Override
	protected Control createButtonBar(Composite parent) {
		Control buttonBar = super.createButtonBar(parent);
		if(getButton(IDialogConstants.OK_ID) != null) {
			getButton(IDialogConstants.OK_ID).setEnabled(treeMapper.getControl().isEnabled());
		}
		return buttonBar;
	}
			
	@Override
	protected void okPressed() {
		boolean creationOk = createMappings();
		if(creationOk) {
			super.okPressed();
		} // else: suppress closing dialog and stay there
	}

	/**
	 * Sets the input to the TreeMapper widget and activates it. The Widget can only
	 * be activated if mappings are available. Otherwise it remains disabled.
	 *
	 * @param leftTreeInput the left tree input
	 */
	private void setInputAndActivateTreeMapperWidget(List<WebshopOrderState> leftTreeInput) {
		setInputAndActivateTreeMapperWidget(leftTreeInput, false);
	}
	
	/**
	 * Sets the input to the TreeMapper widget and activates it. The Widget can only
	 * be activated if mappings are available. Otherwise it remains disabled.
	 *
	 * @param leftTreeInput the left tree input
	 * @param forceReMapping if we want to throw away any old (or incomplete) mapping
	 */
	private void setInputAndActivateTreeMapperWidget(List<WebshopOrderState> leftTreeInput, boolean forceReMapping) {
		// at first check if mappings are available
		boolean isTreeWidgetEnabled = false;  // by default it is not enabled
		List<WebshopStateMapping> webshopOrderStatesMapping;
		if(forceReMapping) {
			webshopOrderStatesMapping = new ArrayList<>();
		} else {
			String webshopName = webshopDAO.createWebShopIdentifier(preferences.getString(Constants.PREFERENCES_WEBSHOP_URL));
			WebShop webShop = webshopDAO.findByName(webshopName);
			if (webShop != null) {
				webshopOrderStatesMapping = webshopDAO.findAllForWebshop(webshopName);
				shopInfoString.setText(StringUtils.defaultString(webShop.getWebshopVendor()));
			} else {
				webshopOrderStatesMapping = Collections.emptyList(); 
			}
		}
		List<OrderState> rightTreeInput = Arrays.asList(OrderState.values());
		if(!webshopOrderStatesMapping.isEmpty()) {
			for (WebshopStateMapping webshopStateMapping : webshopOrderStatesMapping) {
				WebshopOrderState webshopOrderState = new WebshopOrderState(webshopStateMapping.getWebshopState(), 
						webshopStateMapping.getName());
				leftTreeInput.add(webshopOrderState);
				OrderState orderState = OrderState.valueOf(webshopStateMapping.getFakturamaOrderState());
				if(orderState != null) {
					mappings.add(new WebshopOrderStateMapping(webshopOrderState, orderState));
				}
			}
		}
		
		if(!leftTreeInput.isEmpty()) {
			treeMapper.setInput(leftTreeInput, rightTreeInput, mappings);
			isTreeWidgetEnabled = true;
		}
		treeMapper.getControl().setEnabled(isTreeWidgetEnabled);
		if(getButton(IDialogConstants.OK_ID) != null) {
			getButton(IDialogConstants.OK_ID).setEnabled(isTreeWidgetEnabled);
		}
	}

	/* **************************************************************************************************/
	class FktOrderStateContentProvider extends SimpleTreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return OrderState.values();
		}
	}

	class WebshopStateContentProvider extends SimpleTreeContentProvider {
		@SuppressWarnings("unchecked")
		@Override
		public Object[] getElements(Object inputElement) {
			return ((List<WebshopOrderState>) inputElement).toArray();
		}
	}

	class ViewLabelProvider extends LabelProvider {
		private ResourceManager resourceManager;
		
		@Override
		public String getText(Object element) {
			String retval = "";
			if(element instanceof WebshopOrderState) {
				WebshopOrderState wsOrderState = (WebshopOrderState)element;
				retval = String.format("%s (ID: %s)", wsOrderState.getStateName(), wsOrderState.getId());
			} else {
				retval = super.getText(element);
			}
			return retval;
		}

		@Override
		public void dispose() {
			// garbage collect system resources
			if (resourceManager != null) {
				resourceManager.dispose();
				resourceManager = null;
			}
		}

		protected ResourceManager getResourceManager() {
			if (resourceManager == null) {
				resourceManager = new LocalResourceManager(JFaceResources.getResources());
			}
			return resourceManager;
		}
	}

	/* **************************************************************************************************/

	/**
	 * This is kind of a DTO. This class holds the mappings between a web shop state to the Fakturama {@link OrderState}.
	 * 
	 */
	public class WebshopOrderStateMapping {
		
		/**
		 * The web shop state from remote web shop.
		 */
		private WebshopOrderState leftItem;
		
		/**
		 * The {@link OrderState} which is used in this application.
		 */
		private OrderState rightItem;

		
		/**
		 * The Constructor.
		 *
		 * @param left the remote {@link WebshopOrderState} 
		 * @param right the {@link OrderState} to which the {@link WebshopOrderState} was mapped by.
		 */
		public WebshopOrderStateMapping(WebshopOrderState left, OrderState right) {
			this.leftItem = left;
			this.rightItem = right;
		}

		/**
		 * Gets the left item.
		 *
		 * @return the left item
		 */
		public final WebshopOrderState getLeftItem() {
			return leftItem;
		}

		/**
		 * Sets the left item.
		 *
		 * @param leftItem the left item
		 */
		public final void setLeftItem(WebshopOrderState leftItem) {
			this.leftItem = leftItem;
		}

		/**
		 * Gets the right item.
		 *
		 * @return the right item
		 */
		public final OrderState getRightItem() {
			return rightItem;
		}

		/**
		 * Sets the right item.
		 *
		 * @param rightItem the right item
		 */
		public final void setRightItem(OrderState rightItem) {
			this.rightItem = rightItem;
		}

		@Override
		public String toString() {
			if (leftItem != null && rightItem != null) {
				return String.format("%s -> %s", leftItem.getStateName(), rightItem.name());
			}
			return "(null)";
		}
	}

	/**
	 * Represents a web shop state from a remote web shop. 
	 */
	public class WebshopOrderState {
		private String id;
		private String stateName;

		public WebshopOrderState(String id, String text) {
			this.id = id;
			this.stateName = text;
		}

		/**
		 * @return the id
		 */
		public final String getId() {
			return id;
		}

		/**
		 * @param id
		 *            the id to set
		 */
		public final void setId(String id) {
			this.id = id;
		}

		/**
		 * @return the text
		 */
		public final String getStateName() {
			return stateName;
		}

		/**
		 * @param text
		 *            the text to set
		 */
		public final void setStateName(String stateName) {
			this.stateName = stateName;
		}
	}

	/**
	 * @param parent
	 */
	public Webshopexport execute(Shell parent) {
		ExecutionResult executionResult = null;
		ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(parent);
		
		String shopURL = preferences.getString(Constants.PREFERENCES_WEBSHOP_URL);
		
        // Add "http://" if no protocol is given
        WebShopConfig conn = new WebShopConfig()
        		.withScriptURL(StringUtils.prependIfMissingIgnoreCase(shopURL, "http://", "https://", "file://"))
        		.withUseAuthorization(preferences.getBoolean(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_ENABLED))
        		.withAuthorizationUser(preferences.getString(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_USER))
        		.withAuthorizationPassword(preferences.getString(Constants.PREFERENCES_WEBSHOP_AUTHORIZATION_PASSWORD))
        		.withUser(preferences.getString(Constants.PREFERENCES_WEBSHOP_USER))
        		.withPassword(preferences.getString(Constants.PREFERENCES_WEBSHOP_PASSWORD));
		
        WebShopStatusImporter importOperation = ContextInjectionFactory.make(WebShopStatusImporter.class, context);
        importOperation.setConnector(conn);
		try {
			// get all order status from web shop
			progressMonitorDialog.run(true, true, importOperation);
			executionResult = importOperation.getRunResult();
		} catch (Exception ex) {
			log.error(ex);
		}

		if (executionResult != null && executionResult.getErrorCode() != Constants.RC_OK) {
			// If there is an error - display it in a message box
			String errorMessage = StringUtils.abbreviate(executionResult.getErrorMessage(), 400);
			MessageDialog.openError(parent, msg.importWebshopActionError, errorMessage);
			log.error(executionResult.getException(), errorMessage);
		}
		return importOperation.getWebshopexport();
	}
}
