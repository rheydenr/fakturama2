package com.sebulli.fakturama.i18n;
/******************************************************************************
 * Copyright (c) 1998, 2004 Jackwind Li Guojie
 * All right reserved. 
 * 
 * Created on Dec 28, 2003 7:56:40 PM by JACK
 * $Id$
 * 
 * visit: http://www.asprise.com/swt
 *****************************************************************************/

import java.util.Vector;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

public class CategoryTree {
  Display display = new Display();
  Shell shell = new Shell(display);
  
  final Tree tree = new Tree(shell, SWT.BORDER);
  
  Vector categories = new Vector();
  
  public CategoryTree() {
    Category category = new Category("Java libraries", null);
    categories.add(category);
    
    category = new Category("UI Toolkits", category);
    new Category("AWT", category);
    new Category("Swing", category);
    new Category("SWT/JFace", category);
    
    category = new Category("Java IDEs", null);
    categories.add(category);
    
    new Category("Eclipse", category);
    new Category("JBuilder", category);
  }
  
  /**
   * Builds up the tree with traditional approach. 
   *
   */
  public void traditional() {
    for(int i=0; categories != null && i < categories.size(); i++) {
      Category category = (Category)categories.elementAt(i);
      addCategory(null, category);
    }
  }
  
  /**
   * Adds a category to the tree (recursively).
   * @param parentItem
   * @param category
   */
  private void addCategory(TreeItem parentItem, Category category) {
    TreeItem item = null;
    if(parentItem == null) 
      item = new TreeItem(tree, SWT.NONE);
    else
      item = new TreeItem(parentItem, SWT.NONE);
    
    item.setText(category.getName());
    
    Vector subs = category.getSubCategories();
    for(int i=0; subs != null && i < subs.size(); i++)
      addCategory(item, (Category)subs.elementAt(i));
  }
  
  /**
   * Builds up the tree with MVC approach. 
   *
   */
  public void MVC() {
    
    TreeViewer treeViewer = new TreeViewer(tree);
    
    treeViewer.setContentProvider(new ITreeContentProvider() {
      public Object[] getChildren(Object parentElement) {
        Vector subcats = ((Category)parentElement).getSubCategories();
        return subcats == null ? new Object[0] : subcats.toArray();
      }
      
      public Object getParent(Object element) {
        return ((Category)element).getParent();
      }
      
      public boolean hasChildren(Object element) {
        return ((Category)element).getSubCategories() != null;
      }
      
      public Object[] getElements(Object inputElement) {
        if(inputElement != null && inputElement instanceof Vector) {
          return ((Vector)inputElement).toArray();
        }
        return new Object[0];
      }
      
      public void dispose() {
        // 
      }
      
      public void inputChanged(Viewer viewer,
                   Object oldInput,
                   Object newInput) {
        // 
      }
    });
    
    treeViewer.setLabelProvider(new LabelProvider() {
      public String getText(Object element) {
        return ((Category)element).getName();
      }
    });
    
    treeViewer.setInput(categories);
    
  }
  
  public void show() {
    tree.setSize(300, 200);
    shell.setSize(300, 200);
    
    shell.open();
    
    // Set up the event loop.
    while (!shell.isDisposed()) {
      if (!display.readAndDispatch()) {
        // If no more entries in event queue
        display.sleep();
      }
    }

    display.dispose();
  }
  
  public static void main(String[] args) {
    CategoryTree tree = new CategoryTree();
    //tree.traditional();
    tree.MVC();
    tree.show();
  }

  /**
   * Represents a category of items. 
   * The max level of categories is 2 only.
   *
   */
  class Category {
    private String name;
    private Vector subCategories;
    private Category parent;
    
    public Category(String name, Category parent) {
      this.name = name;
      this.parent = parent;
      if(parent != null)
        parent.addSubCategory(this);
    }
    
    public Vector getSubCategories() {
      return subCategories;
    }
    
    private void addSubCategory(Category subcategory) {
      if(subCategories == null)
        subCategories = new Vector();
      if(! subCategories.contains(subcategory))
        subCategories.add(subcategory);
    }
    
    public String getName() {
      return name;
    }
    
    public Category getParent() {
      return parent;
    }
  }  
  
}