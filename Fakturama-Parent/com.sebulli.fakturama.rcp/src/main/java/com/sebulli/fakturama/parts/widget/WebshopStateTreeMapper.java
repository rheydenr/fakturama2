/**
 * 
 */
package com.sebulli.fakturama.parts.widget;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.LabelProvider;
//import org.eclipse.nebula.widgets.treemapper.ISemanticTreeMapperSupport;
//import org.eclipse.nebula.widgets.treemapper.TreeMapper;
//import org.eclipse.nebula.widgets.treemapper.TreeMapperUIConfigProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

import com.sebulli.fakturama.parts.widget.contentprovider.SimpleTreeContentProvider;

/**
 * 
 *
 */
public class WebshopStateTreeMapper {
	
// COMMENTED OUT SINCE TreeMapper DEPENDS ON org.eclipse.ui :-(
	
// so long, we can't use it...	

//	@PostConstruct
//	public void createControls(Composite parent) {
//		Display display = parent.getDisplay();
//
//		Color gray = display.getSystemColor(SWT.COLOR_GRAY);
//		Color blue = display.getSystemColor(SWT.COLOR_BLUE);
//		TreeMapperUIConfigProvider uiConfig = new TreeMapperUIConfigProvider(gray, 1, blue, 3);
//		final List<WebshopOrderStateMapping> mappings = new ArrayList<>();
//		// TODO: Anzeige der vorhandenen Zuordnungen fehlt noch!
//		// die müssen hier in mappings reingebaut werden!
//		ISemanticTreeMapperSupport<WebshopOrderStateMapping, WebshopOrderState, FtkOrderState> semanticSupport = new ISemanticTreeMapperSupport<WebshopStateTreeMapper.WebshopOrderStateMapping, WebshopStateTreeMapper.WebshopOrderState, WebshopStateTreeMapper.FtkOrderState>() {
//			@Override
//			public WebshopOrderStateMapping createSemanticMappingObject(WebshopOrderState leftItem, FtkOrderState rightItem) {
//				// create only one mapping (for the left item); delete old mapping if it was created before!
//				for (WebshopOrderStateMapping orderStatesMapping : mappings) {
//					if(orderStatesMapping.getLeftItem().equals(leftItem)) {
//						mappings.remove(orderStatesMapping);
//						break;
//					}
//				}
//				return new WebshopOrderStateMapping(leftItem, rightItem);
//			}
//
//			@Override
//			public WebshopOrderState resolveLeftItem(WebshopOrderStateMapping semanticMappingObject) {
//				return semanticMappingObject.getLeftItem();
//			}
//
//			@Override
//			public FtkOrderState resolveRightItem(WebshopOrderStateMapping semanticMappingObject) {
//				return semanticMappingObject.getRightItem();
//			}
//		};
//		TreeMapper<WebshopOrderStateMapping, WebshopOrderState, FtkOrderState> tm = new TreeMapper<>(parent, semanticSupport,
//				uiConfig);
//
//		List<WebshopOrderState> leftTreeInput = createDummyContent(0, 10, "left_");
//		List<FtkOrderState> rightTreeInput = Arrays.asList(FtkOrderState.values());
//		tm.setContentProviders(new WebshopStateContentProvider(), new FtkOrderStateContentProvider());
//		tm.setLabelProviders(new ViewLabelProvider(null), new ViewLabelProvider(null));
//		tm.setInput(leftTreeInput, rightTreeInput, mappings);
//		Canvas cv;
//		Control[] controls = tm.getControl().getChildren();
//		// it's not possible to access the Canvas directly :-(
//		for (Control control : controls) {
//			if(control instanceof Canvas) {
//				cv = (Canvas) control;
//				cv.addKeyListener(new KeyAdapter() {
//					@Override
//					public void keyPressed(KeyEvent e) {
//						switch (e.keyCode) {
//						case SWT.DEL:
//							WebshopOrderStateMapping selectedMapping = (WebshopOrderStateMapping) tm.getSelection().getFirstElement();
//							if(selectedMapping != null) {
//								mappings.remove(selectedMapping);
//								tm.refresh();
//							}
//							break;
//		
//						default:
//							break;
//						}
//						super.keyPressed(e);
//					}
//				});
//				break;
//			}
//		}
//		
//		Button b = new Button(parent, SWT.PUSH);
//		b.setText("Übernehmen");
//		b.addSelectionListener(new SelectionAdapter() {
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				System.out.println("Folgende Zuordnungen wurden getroffen:");
//				for (WebshopOrderStateMapping orderStatesMapping : mappings) {
//					// TODO hier müßten die Dinger dann in die DB wandern.
//					/*
//					 * Struktur:
//					 * id (klar)
//					 * webshopId ... ID of the webshop for which this assignment is for
//					 * (hier ist bereits vorgesehen, evtl. mal mehrere Webshops zu haben)
//					 * webshopStateId ... ID of the state from web shop
//					 * webshopStateName ... description (or name) of the state from web shop
//					 * orderState .... OrderState from Fakturama, to which the web shop state is mapped.
//					 * 
//					 * am besten mit insertOrUpdate, weil man ja auch mal die Status anpassen möchte.
//					 * 
//					 * ==> aus jedem WebshopOrderStateMapping muß ein neues Entity erzeugt werden!
//					 */
//					System.out.println(orderStatesMapping);
//				}
//			}
//		});
//	}
//
//	private List<WebshopOrderState> createDummyContent(int start, int end, String prefix) {
//		List<WebshopOrderState> treeInput = new ArrayList<>();
//		for (int i = start; i <= end; i++) {
//			WebshopOrderState os = new WebshopOrderState(i, prefix + i);
//			treeInput.add(os);
//		}
//		return treeInput;
//	}
//
//	/* **************************************************************************************************/
//	class FtkOrderStateContentProvider extends SimpleTreeContentProvider {
//
//		@Override
//		public Object[] getElements(Object inputElement) {
//			return FtkOrderState.values();
//		}
//	}
//
//	class WebshopStateContentProvider extends SimpleTreeContentProvider {
//		@Override
//		public Object[] getElements(Object inputElement) {
//			return ((List<WebshopOrderState>) inputElement).toArray();
//		}
//	}
//
//	class ViewLabelProvider extends LabelProvider {
//
//		private ImageDescriptor directoryImage;
//		private ResourceManager resourceManager;
//
//		public ViewLabelProvider(ImageDescriptor directoryImage) {
//			this.directoryImage = directoryImage;
//		}
//		
//		@Override
//		public String getText(Object element) {
//			String retval = "";
//			if(element instanceof WebshopOrderState) {
//				WebshopOrderState wsOrderState = (WebshopOrderState)element;
//				retval = String.format("ID: %d (%s)", wsOrderState.getId(), wsOrderState.getText());
//			} else {
//				retval = super.getText(element);
//			}
//			return retval;
//		}
//
//		@Override
//		public void dispose() {
//			// garbage collect system resources
//			if (resourceManager != null) {
//				resourceManager.dispose();
//				resourceManager = null;
//			}
//		}
//
//		protected ResourceManager getResourceManager() {
//			if (resourceManager == null) {
//				resourceManager = new LocalResourceManager(JFaceResources.getResources());
//			}
//			return resourceManager;
//		}
//	}
//
//	/* **************************************************************************************************/
//
//	public class WebshopOrderStateMapping {
//		WebshopOrderState leftItem;
//		FtkOrderState rightItem;
//
//		public WebshopOrderStateMapping(WebshopOrderState left, FtkOrderState right) {
//			this.leftItem = left;
//			this.rightItem = right;
//		}
//
//		/**
//		 * @return the leftItem
//		 */
//		public final WebshopOrderState getLeftItem() {
//			return leftItem;
//		}
//
//		/**
//		 * @param leftItem
//		 *            the leftItem to set
//		 */
//		public final void setLeftItem(WebshopOrderState leftItem) {
//			this.leftItem = leftItem;
//		}
//
//		/**
//		 * @return the rightItem
//		 */
//		public final FtkOrderState getRightItem() {
//			return rightItem;
//		}
//
//		/**
//		 * @param rightItem
//		 *            the rightItem to set
//		 */
//		public final void setRightItem(FtkOrderState rightItem) {
//			this.rightItem = rightItem;
//		}
//
//		@Override
//		public String toString() {
//			if (leftItem != null && rightItem != null) {
//				return String.format("%s -> %s", leftItem.getText(), rightItem.name());
//			}
//			return "buh!";
//		}
//	}
//
//	public enum FtkOrderState {
//		BEZAHLT, OFFEN, MAHNUNG, SONSTWAS
//	}
//
//	public class WebshopOrderState {
//		long id;
//		String text;
//
//		public WebshopOrderState(long id, String text) {
//			this.id = id;
//			this.text = text;
//		}
//
//		/**
//		 * @return the id
//		 */
//		public final long getId() {
//			return id;
//		}
//
//		/**
//		 * @param id
//		 *            the id to set
//		 */
//		public final void setId(long id) {
//			this.id = id;
//		}
//
//		/**
//		 * @return the text
//		 */
//		public final String getText() {
//			return text;
//		}
//
//		/**
//		 * @param text
//		 *            the text to set
//		 */
//		public final void setText(String text) {
//			this.text = text;
//		}
//
//	}
//
}
