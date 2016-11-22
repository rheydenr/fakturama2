/**
 * 
 */
package com.sebulli.fakturama.dialogs;

import java.sql.Date;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.nebula.widgets.treemapper.ISemanticTreeMapperSupport;
import org.eclipse.nebula.widgets.treemapper.TreeMapper;
import org.eclipse.nebula.widgets.treemapper.TreeMapperUIConfigProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;

import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.dao.DocumentsDAO;
import com.sebulli.fakturama.dao.PaymentsDAO;
import com.sebulli.fakturama.dao.ProductCategoriesDAO;
import com.sebulli.fakturama.dao.ProductsDAO;
import com.sebulli.fakturama.dao.ShippingCategoriesDAO;
import com.sebulli.fakturama.dao.ShippingsDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.dao.WebshopStateMappingDAO;
import com.sebulli.fakturama.exception.FakturamaStoringException;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.misc.OrderState;
import com.sebulli.fakturama.model.WebshopStateMapping;
import com.sebulli.fakturama.parts.widget.contentprovider.SimpleTreeContentProvider;
import com.sebulli.fakturama.webshopimport.ExecutionResult;
import com.sebulli.fakturama.webshopimport.IWebshopConnection;
import com.sebulli.fakturama.webshopimport.WebShopStatusImport;
import com.sebulli.fakturama.webshopimport.type.StatusType;
import com.sebulli.fakturama.webshopimport.type.Webshopexport;

/**
 * Settings for Webshop import (dialog within Webshop preferences)
 */
public class WebShopStatusSettingsDialog extends TitleAreaDialog implements IWebshopConnection {
	
	private static final long FIX_WEBSHOP_ID = 0L;

	@Inject
	@Translation
	protected Messages msg;

	@Inject
	IPreferenceStore preferences;
	
	@Inject
	private WebshopStateMappingDAO webshopStateMappingDAO;

	@Inject
	private IEclipseContext context;
    
    @Inject
    protected Logger log;

    /**
     * contains the result from Web shop connector execution
     */
    private Object data;
	private Control top;

	int worked;

	private Text connectorVersion, shopInfoString;

	// The result of this import process
	private String runResult = "";

	private TreeMapper<WebshopOrderStateMapping, WebshopOrderState, OrderState> treeMapper;

	private List<WebshopOrderStateMapping> mappings;

	@Inject
	public WebShopStatusSettingsDialog(Shell shell, @Translation Messages msg) {
		super(shell);
		setShellStyle(SWT.RESIZE | SWT.BORDER | SWT.TITLE | SWT.APPLICATION_MODAL);
		this.msg = msg;
	}

	@PostConstruct
	public void initialize(Shell shell) {
		this.top = (Control) context.get(Composite.class);
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		// super.createContents(parent);
		// Set the title
		// parent.getShell().setSize(500, 500);
		parent.getShell().setText(msg.preferencesWebshopSettings);
		// setTitle(msg.preferencesWebshopSettings);
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		GridLayout gl_container = new GridLayout(2, false);
		gl_container.horizontalSpacing = 2;
		container.setLayout(gl_container);
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		setMessage(msg.preferencesWebshopSettingsDescription, IMessageProvider.INFORMATION);
		// final Text text1 = new Text(parent, SWT.MULTI | SWT.WRAP |
		// SWT.READ_ONLY);
		// text1.setText(msg.preferencesWebshopSettingsDescription);
		// // With this in addition, it will wrap
		// text1.setLayoutData( new GridData( GridData.FILL_HORIZONTAL ));
		// text1.setBounds(0, 0, 350, 150);

		Button stateBtn = new Button(container, SWT.PUSH);
		stateBtn.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 2, 1));

		stateBtn.setText(msg.preferencesWebshopSettingsGetallstates);
		stateBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				execute(parent);

				Webshopexport expenseObj = (Webshopexport) getData();
				if(expenseObj != null) {
					// set the values from web shop
					shopInfoString.setText(String.format("%s (%s)", expenseObj.getWebshop().getShop(), expenseObj.getCompleteVersion()));
					connectorVersion.setText(expenseObj.getVersion());
					
					// fill WebshopStateTreeMapper
					if(expenseObj.getStatus() == null) {
						MessageDialog.openError(getParentShell(), msg.dialogMessageboxTitleError, msg.preferencesWebshopSettingsStateError);
					} else {
						List<StatusType> statusList = expenseObj.getStatus();
						List<WebshopOrderState> leftTreeInput = new ArrayList<>(statusList.size());
						for (StatusType state : statusList) {
							WebshopOrderState webshopOrderState = new WebshopOrderState(state.getId(), 
									state.getName());
							leftTreeInput.add(webshopOrderState);
						}
						// throw away all mappings
						mappings.clear();
						setInputAndActivateTreeMapperWidget(leftTreeInput, true);
					}
				}
			}
		});

		Label lblShopInfo = new Label(container, SWT.READ_ONLY);
		lblShopInfo.setText(msg.preferencesWebshopSettingsShopinfo);
		shopInfoString = new Text(container, SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(shopInfoString);

		Label lblConnectorVersion = new Label(container, SWT.READ_ONLY);
		lblConnectorVersion.setText(msg.preferencesWebshopSettingsVersion);
		connectorVersion = new Text(container, SWT.READ_ONLY);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(connectorVersion);

		// WebshopStateTreeMapper -> disabled if no web shop states are available! 
		createTreeMapperWidget(container);
//		GridDataFactory.fillDefaults().grab(true, true).applyTo(treeMapper.getControl());

		GridDataFactory.fillDefaults().grab(true, true).applyTo(top);
		
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

		treeMapper.setContentProviders(new WebshopStateContentProvider(), new FtkOrderStateContentProvider());
		treeMapper.setLabelProviders(new ViewLabelProvider(null), new ViewLabelProvider(null));
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
			// at first clear any pre-existing mappings
			webshopStateMappingDAO.clearOldMappings(FIX_WEBSHOP_ID);
			
			for (WebshopOrderStateMapping orderStatesMapping : mappings) {
				WebshopStateMapping webshopStateMapping = new WebshopStateMapping();
				webshopStateMapping.setWebshopId(FIX_WEBSHOP_ID);
				webshopStateMapping.setWebshopStateId(orderStatesMapping.getLeftItem().getId());
				webshopStateMapping.setName(orderStatesMapping.getLeftItem().getStateName());
				webshopStateMapping.setOrderState(orderStatesMapping.getRightItem().name());
				webshopStateMapping.setValidFrom(Date.from(Instant.now()));
				webshopStateMappingDAO.save(webshopStateMapping);
			}
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
			webshopOrderStatesMapping = webshopStateMappingDAO.findAllForWebshop(FIX_WEBSHOP_ID);
		}
		List<OrderState> rightTreeInput = Arrays.asList(OrderState.values());
		if(!webshopOrderStatesMapping.isEmpty()) {
			for (WebshopStateMapping webshopStateMapping : webshopOrderStatesMapping) {
				WebshopOrderState webshopOrderState = new WebshopOrderState(webshopStateMapping.getWebshopStateId(), 
						webshopStateMapping.getName());
				leftTreeInput.add(webshopOrderState);
				OrderState orderState = OrderState.valueOf(webshopStateMapping.getOrderState());
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
	class FtkOrderStateContentProvider extends SimpleTreeContentProvider {

		@Override
		public Object[] getElements(Object inputElement) {
			return OrderState.values();
		}
	}

	class WebshopStateContentProvider extends SimpleTreeContentProvider {
		@Override
		public Object[] getElements(Object inputElement) {
			return ((List<WebshopOrderState>) inputElement).toArray();
		}
	}

	class ViewLabelProvider extends LabelProvider {
		private ImageDescriptor directoryImage;
		private ResourceManager resourceManager;

		public ViewLabelProvider(ImageDescriptor directoryImage) {
			this.directoryImage = directoryImage;
		}
		
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
	public ExecutionResult execute(Composite parent) {
		ExecutionResult executionResult = null;
		ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(parent.getShell());
		IRunnableWithProgress op = new WebShopStatusImport(this);
		try {
			// get all order status from web shop
			progressMonitorDialog.run(true, true, op);
			executionResult = new ExecutionResult(getRunResult(), getRunResult().isEmpty() ? 0 : 1);
		} catch (Exception ex) {
			log.error(ex);
		}

		if (executionResult.getErrorCode() != Constants.RC_OK) {
			// If there is an error - display it in a message box
			String errorMessage = StringUtils.abbreviate(executionResult.getErrorMessage(), 400);
			MessageDialog.openError(parent.getShell(), msg.importWebshopActionError, errorMessage);
			log.error(errorMessage);
		}
		return executionResult;
	}

	/**
	 * @return the runResult
	 */
	public String getRunResult() {
		return runResult;
	}

	/**
	 * @param runResult
	 *            the runResult to set
	 */
	public void setRunResult(String runResult) {
		this.runResult = runResult;
	}

	@Override
	public Messages getMsg() {
		return msg;
	}

	@Override
	public IPreferenceStore getPreferences() {
		return preferences;
	}
	
	@Override
	public Logger getLog() {
		return log;
	}

	@Override
	public IEclipseContext getContext() {
		return context;
	}
	
	/**
	 * @return the data
	 */
	public final Object getData() {
		return data;
	}
	
	/**
	 * @param data the data to set
	 */
	public final void setData(Object data) {
		this.data = data;
	}

	
	/* * * * * * * * [only dummy implementations] * * * * * * * * * * * * * * * * * * * */

	@Override
	public ExecutionResult execute(Shell parent, String prepareGetProductsAndOrders) {
		return null;
	}

	@Override
	public void readOrdersToSynchronize() {
	}

	@Override
	public boolean isGetOrders() {
		return false;
	}

	@Override
	public boolean isGetProducts() {
		return false;
	}

	@Override
	public Properties getOrderstosynchronize() {
		return null;
	}

	@Override
	public void setOrderstosynchronize(Properties orderstosynchronize) {
	}

	@Override
	public void saveOrdersToSynchronize() {
	}

	@Override
	public ProductCategoriesDAO getProductCategoriesDAO() {
		return null;
	}

	@Override
	public PaymentsDAO getPaymentsDAO() {
		return null;
	}

	@Override
	public ShippingsDAO getShippingsDAO() {
		return null;
	}

	@Override
	public ShippingCategoriesDAO getShippingCategoriesDAO() {
		return null;
	}

	@Override
	public ContactsDAO getContactsDAO() {
		return null;
	}

	@Override
	public ProductsDAO getProductsDAO() {
		return null;
	}

	@Override
	public DocumentsDAO getDocumentsDAO() {
		return null;
	}

	@Override
	public VatsDAO getVatsDAO() {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.sebulli.fakturama.webshopimport.IWebshopConnection#getWebshopStateMappingDAO()
	 */
	public WebshopStateMappingDAO getWebshopStateMappingDAO() {
		return null;
	}

}
