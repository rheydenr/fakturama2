/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2016 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
package org.fakturama.export.wizard;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.dialogs.filteredtree.FilteredTree;
import org.eclipse.e4.ui.dialogs.filteredtree.PatternFilter;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.fakturama.export.AbstractWizardNode;
import org.fakturama.export.IFakturamaExportService;

import com.sebulli.fakturama.i18n.Messages;

/**
 * Each export wizard is "registered" here to get available in the selection
 * tree. This wizard is the main "entry point" to all export wizards. It
 * presents a selection tree which contains all available export wizards. This
 * component replaces the old "org.eclipse.ui.exportWizards" extension point.
 */
public class FakturamaExporterWizards extends WizardSelectionPage {

	private final static int SIZING_VIEWER_WIDTH = 300;

	private FilteredTree filteredTree;
	private PatternFilter filteredTreeFilter;

	// Keep track of the wizards we have previously selected
	private Hashtable selectedWizards = new Hashtable();

	/**
	 * all wizards which are NOT only a category entry are called "primary".
	 */
	private List<AbstractWizardNode> primaryWizards;
	private Messages msg;

	/**
	 * The export service is injected by OSGi container. It resides in a bundle
	 * which contains several exporters.
	 */
	private IFakturamaExportService exportService;

	private AbstractWizardNode selectedWizardNode;

	@Inject
	public FakturamaExporterWizards(IFakturamaExportService exportService, @Translation Messages msg) {
		super(msg.wizardExportCommonTitle);
		this.msg = msg;
		this.exportService = exportService;
		setTitle(msg.wizardExportCommonHeadline);
		setDescription(msg.wizardExportCommonDescription);
		// setImageDescriptor(image);
		// IWorkbenchGraphicConstants.IMG_WIZBAN_EXPORT_WIZ
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.
	 * widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		parent.getShell().setText(msg.wizardExportCommonTitle);
		// Composite composite = new Composite(parent, SWT.NONE);

		// // Name, you can create your form like normally
		// Label l = new Label(composite, SWT.NONE);
		// l.setText("Name");
		// new Text(composite, SWT.BORDER);
		//
		// // Project type
		// l = new Label(composite, SWT.NONE);
		// l.setText("Type");
		//
		// TableViewer projectType = new TableViewer(composite);
		// projectType.getTable().setLayoutData(new
		// GridData(GridData.FILL_BOTH));
		// projectType.addSelectionChangedListener(new
		// ISelectionChangedListener() {
		// @Override
		// public void selectionChanged(SelectionChangedEvent event) {
		// ISelection selection = event.getSelection();
		// if(!selection.isEmpty() && selection instanceof IStructuredSelection)
		// {
		// Object o = ((IStructuredSelection) selection).getFirstElement();
		// if(o instanceof IWizardNode) {
		// // Now we set our selected node, which toggles the next button
		// selectedWizardNode = (AbstractWizardNode) o;
		// setTitle(selectedWizardNode.getName());
		// setSelectedNode(selectedWizardNode);
		// }
		// }
		// }
		// });
		// projectType.setContentProvider(new ArrayContentProvider());
		// projectType.setLabelProvider(new LabelProvider() {
		// @Override
		// public String getText(Object element) {
		// if(element instanceof IWizardNode) {
		// return ((AbstractWizardNode) element).getName();
		// }
		// return super.getText(element);
		// }
		// });
		// List<AbstractWizardNode> wizardNodes =
		// exportService.getExporterList();
		// projectType.setInput(wizardNodes);
		//
		// GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(composite);
		//

		Font wizardFont = parent.getFont();
		// top level group
		Composite outerContainer = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		outerContainer.setLayout(layout);

		Label wizardLabel = new Label(outerContainer, SWT.NONE);
		GridData data = new GridData(SWT.BEGINNING, SWT.FILL, false, true);
		outerContainer.setLayoutData(data);
		wizardLabel.setFont(wizardFont);
		wizardLabel.setText(msg.wizardExportCommonFilterlabel);

		Composite innerContainer = new Composite(outerContainer, SWT.NONE);
		layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		innerContainer.setLayout(layout);
		innerContainer.setFont(wizardFont);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		innerContainer.setLayoutData(data);

		filteredTree = createFilteredTree(innerContainer);
		setControl(parent);
	}

	/**
	 * Create a new FilteredTree in the parent.
	 * 
	 * @param parent
	 *            the parent <code>Composite</code>.
	 * @since 3.0
	 */
	private FilteredTree createFilteredTree(Composite parent) {
		List<AbstractWizardNode> wizardNodes = exportService.getExporterList();

		// at first some housekeeping...
		this.primaryWizards = wizardNodes;
		if (this.primaryWizards.size() > 0) {
//			 if (allPrimary(wizardCategories)) {
//			 this.wizardCategories = null; // dont bother considering the categories as all wizards are primary
//			 needShowAll = false;
//			 } else {
//			 needShowAll = !allActivityEnabled(wizardCategories);
//			 }
//			 } else {
//			 needShowAll = !allActivityEnabled(wizardCategories);
		}

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.widthHint = SIZING_VIEWER_WIDTH;
		data.horizontalSpan = 2;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;

		// boolean needsHint = DialogUtil.inRegularFontMode(parent);
		//
		// //Only give a height hint if the dialog is going to be too small
		// if (needsHint) {
		// data.heightHint = SIZING_LISTS_HEIGHT;
		// }
		composite.setLayoutData(data);

		filteredTreeFilter = new PatternFilter();
		FilteredTree filterTree = new FilteredTree(composite, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER,
				filteredTreeFilter);

		final TreeViewer treeViewer = filterTree.getViewer();
		treeViewer.setContentProvider(new TreeContentProvider());
		treeViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IWizardNode) {
					return ((AbstractWizardNode) element).getName();
				}
				return super.getText(element);
			}
			
			@Override
			public Image getImage(Object element) {
				if (element instanceof IWizardNode) {
					return ((AbstractWizardNode) element).getImage();
				}
				return super.getImage(element);
			}
		});
		// treeViewer.setComparator(NewWizardCollectionComparator.INSTANCE);

		List inputArray = new ArrayList();

		for (int i = 0; i < primaryWizards.size(); i++) {
			inputArray.add(primaryWizards.get(i));
		}

		boolean expandTop = false;

		// if (wizardCategories != null) {
		// if (wizardCategories.getParent() == null) {
		// IWizardCategory [] children = wizardCategories.getCategories();
		// for (int i = 0; i < children.length; i++) {
		// inputArray.add(children[i]);
		// }
		// } else {
		// expandTop = true;
		// inputArray.add(wizardCategories);
		// }
		// }

		// ensure the category is expanded. If there is a remembered expansion
		// it will be set later.
		if (expandTop) {
			treeViewer.setAutoExpandLevel(2);
		}

		treeViewer.setInput(wizardNodes);

		// filterTree.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		// treeViewer.getTree().setFont(parent.getFont());

		treeViewer.addDoubleClickListener(new IDoubleClickListener() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see
			 * org.eclipse.jface.viewers.IDoubleClickListener#doubleClick(org.
			 * eclipse.jface.viewers.DoubleClickEvent)
			 */
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection s = (IStructuredSelection) event.getSelection();
				selectionChanged(new SelectionChangedEvent(event.getViewer(), s));

				Object element = s.getFirstElement();
				if (treeViewer.isExpandable(element)) {
					treeViewer.setExpandedState(element, !treeViewer.getExpandedState(element));
					// } else if (element instanceof WorkbenchWizardElement) {
					// page.advanceToNextPageOrFinish();
				}
			}
		});
		treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				ISelection selection = event.getSelection();
				if (!selection.isEmpty() && selection instanceof IStructuredSelection) {
					Object o = ((IStructuredSelection) selection).getFirstElement();
					if (o instanceof IWizardNode) {
						// Now we set our selected node, which toggles the next
						// button
						selectedWizardNode = (AbstractWizardNode) o;
						setDescription(selectedWizardNode.getDescription());
						setSelectedNode(selectedWizardNode);
					}
				}
			}
		});
		//
		// treeViewer.addFilter(filter);
		//
		// if (projectsOnly) {
		// treeViewer.addFilter(projectFilter);
		// }
		//
		Dialog.applyDialogFont(filterTree);
		return filterTree;
	}

	/**
	 * The user selected either new wizard category(s) or wizard element(s).
	 * Proceed accordingly.
	 * 
	 * @param selectionEvent
	 *            ISelection
	 */
	public void selectionChanged(SelectionChangedEvent selectionEvent) {
		// page.setErrorMessage(null);
		// page.setMessage(null);

		Object selectedObject = getSingleSelection((IStructuredSelection) selectionEvent.getSelection());

//		 if (selectedObject instanceof IWizardDescriptor) {
//		 if (selectedObject == selectedElement) {
//		 return;
//		 }
//		 updateWizardSelection((IWizardDescriptor) selectedObject);
//		 } else {
//		 selectedElement = null;
//		 page.setHasPages(false);
//		 page.setCanFinishEarly(false);
//		 page.selectWizardNode(null);
//		 updateDescription(null);
//		 }
	}

	/**
	 * Returns the single selected object contained in the passed
	 * selectionEvent, or <code>null</code> if the selectionEvent contains
	 * either 0 or 2+ selected objects.
	 */
	protected Object getSingleSelection(IStructuredSelection selection) {
		return selection.size() == 1 ? selection.getFirstElement() : null;
	}

	/**
	 * Update the current description controls.
	 * 
	 * @param selectedObject
	 *            the new wizard
	 * @since 3.0
	 */
	private void updateDescription(IWizardDescriptor selectedObject) {
		String string = ""; //$NON-NLS-1$
		if (selectedObject != null) {
			string = selectedObject.getDescription();
		}
		//
		// page.setDescription(string);
		//
		// if (hasImage(selectedObject)) {
		// ImageDescriptor descriptor = null;
		// if (selectedObject != null) {
		// descriptor = selectedObject.getDescriptionImage();
		// }
		//
		// if (descriptor != null) {
		// GridData data = (GridData)descImageCanvas.getLayoutData();
		// data.widthHint = SWT.DEFAULT;
		// data.heightHint = SWT.DEFAULT;
		// Image image = (Image) imageTable.get(descriptor);
		// if (image == null) {
		// image = descriptor.createImage(false);
		// imageTable.put(descriptor, image);
		// }
		// descImageCanvas.setImage(image);
		// }
		// } else {
		// GridData data = (GridData)descImageCanvas.getLayoutData();
		// data.widthHint = 0;
		// data.heightHint = 0;
		// descImageCanvas.setImage(null);
		// }
		//
		// descImageCanvas.getParent().layout(true);
		// filteredTree.getViewer().getTree().showSelection();
		//
		// IWizardContainer container = page.getWizard().getContainer();
		// if (container instanceof IWizardContainer2) {
		// ((IWizardContainer2) container).updateSize();
		// }
	}

	/**
	 * Tests whether the given wizard has an associated image.
	 * 
	 * @param selectedObject
	 *            the wizard to test
	 * @return whether the given wizard has an associated image
	 */
	private boolean hasImage(IWizardDescriptor selectedObject) {
		if (selectedObject == null) {
			return false;
		}

		if (selectedObject.getDescriptionImage() != null) {
			return true;
		}

		return false;
	}

	/**
	 * @param category
	 *            the wizard category
	 * @return whether all wizards in the category are considered primary
	 */
	private boolean allPrimary(IWizardCategory category) {
		IWizardDescriptor[] wizards = category.getWizards();
		for (int i = 0; i < wizards.length; i++) {
			IWizardDescriptor wizard = wizards[i];
			if (!isPrimary(wizard)) {
				return false;
			}
		}

		IWizardCategory[] children = category.getCategories();
		for (int i = 0; i < children.length; i++) {
			if (!allPrimary(children[i])) {
				return false;
			}
		}

		return true;
	}

	/**
	 * @param wizard
	 * @return whether the given wizard is primary
	 */
	private boolean isPrimary(IWizardDescriptor wizard) {
		for (int j = 0; j < primaryWizards.size(); j++) {
			if (primaryWizards.get(j).equals(wizard)) {
				return true;
			}
		}

		return false;
	}

}