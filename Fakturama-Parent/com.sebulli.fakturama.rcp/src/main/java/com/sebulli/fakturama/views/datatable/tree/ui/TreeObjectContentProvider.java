/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2014 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.views.datatable.tree.ui;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;

import com.sebulli.fakturama.converter.CommonConverter;
import com.sebulli.fakturama.model.AbstractCategory;
import com.sebulli.fakturama.model.DummyStringCategory;
import com.sebulli.fakturama.views.datatable.tree.model.TreeObject;

/**
	 * Content provider for the tree view
	 * 
	 * @author Gerd Bartelt
	 */
class TreeObjectContentProvider<T extends AbstractCategory> implements ITreeContentProvider {

		/**
         * 
         */
        private final TopicTreeViewer<T> topicTreeViewer;

        /**
         * @param topicTreeViewer
         */
        TreeObjectContentProvider(TopicTreeViewer<T> topicTreeViewer) {
            this.topicTreeViewer = topicTreeViewer;
        }

        /**
		 * Returns the elements to display in the viewer when its input is set
		 * to the given element.
		 */
		@Override
		public Object[] getElements(Object parent) {
			int entryCnt = 0;

			// Get the elements
			if (parent == this.topicTreeViewer.root) {

				// Rebuild the elements, if some strings have changed
				// => this replaces "getCategoryStringsChanged()"
				// FIXME zur Zeit ändert sich hier nichts...
//				inputElement.addListEventListener(new ListEventListener<MyRowObject>() {
//
//					@Override
//					public void listChanged(ListEvent<MyRowObject> listChanges) {
					// Clear the tree
					this.topicTreeViewer.clear();
					if (this.topicTreeViewer.inputElement instanceof List) {
						// Get all category strings
						for (T entry : this.topicTreeViewer.inputElement) {
							// Start with the "root" or "all" element
						    addEntry(this.topicTreeViewer.all != null ? this.topicTreeViewer.all : this.topicTreeViewer.root, entry);
						}
					}
//						
//				}});
			}
			
			// Count the category strings
			if (this.topicTreeViewer.inputElement instanceof List) {
				entryCnt = this.topicTreeViewer.inputElement.size();
			}

			// Hide the Tree viewer, if there is no tree element
			if (entryCnt != 0) {
			    this.topicTreeViewer.getTree().setVisible(true);
				GridDataFactory.fillDefaults().hint(150, -1).grab(false, true).applyTo(this.topicTreeViewer.getTree());
				this.topicTreeViewer.getTree().getParent().layout(true);
			} else {
				this.topicTreeViewer.getTree().setVisible(false);
				GridDataFactory.fillDefaults().hint(1, -1).grab(false, true).applyTo(this.topicTreeViewer.getTree());
				this.topicTreeViewer.getTree().getParent().layout(true);
			}

			// Return the children elements
			return getChildren(parent);
		}


	    /**
	     * Add a new entry to the tree. Builds the categories (if needed) recursive bottom up.
	     * 
	     * @param entry
	     *            The new entry to add
	     * @return the added (or current) {@link TreeObject}
	     */
	    private TreeObject addEntry(TreeObject currentNode, AbstractCategory entry) {
	        String fullPath = currentNode.getFullPathName();
	        String categoryName = CommonConverter.getCategoryName(entry, "");
	        TreeObject retval;
	        if(StringUtils.equals(fullPath, categoryName)) {
	            retval = currentNode;
	        } else {
	            TreeObject node = addEntry(currentNode, entry.getParent());
	            String checkPath = (this.topicTreeViewer.all != null ? "/" + this.topicTreeViewer.all.getName() : this.topicTreeViewer.root.getName()) + "/" + categoryName;
	            TreeObject childNode = node.findNode(checkPath);
	            if(childNode == null) {
	                childNode = new TreeObject(entry.getName());
	                childNode.setNodeType(TreeObjectType.DEFAULT_NODE);
	                if(entry instanceof DummyStringCategory) {
	                    childNode.setDocType(((DummyStringCategory)entry).getDocType());
	                }
	                node.addChild(childNode);
	            }
	            retval = childNode;
	        }
	        return retval;
	    }

	    /**
		 * Returns the parent element
		 */
		@Override
		public Object getParent(Object child) {
			if (child instanceof TreeObject) { return ((TreeObject) child).getParent(); }
			return null;
		}

		/**
		 * Returns the children elements
		 */
		@Override
		public Object[] getChildren(Object parent) {
			if (parent instanceof TreeObject) { return ((TreeObject) parent).getChildren(); }
			return new Object[0];
		}

		/**
		 * Returns, if the element has children
		 */
		@Override
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeObject)
				return ((TreeObject) parent).hasChildren();
			return false;
		}

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#dispose()
         */
        @Override
        public void dispose() { 
            // this.topicTreeViewer ... ?
        }

        /* (non-Javadoc)
         * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
         */

        @Override
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            this.topicTreeViewer.internalTreeViewer = (TreeViewer)viewer;
        }
	}