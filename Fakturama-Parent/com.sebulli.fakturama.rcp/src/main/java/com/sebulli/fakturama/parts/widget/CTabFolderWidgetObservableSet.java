/**
 * 
 */
package com.sebulli.fakturama.parts.widget;

import java.util.Arrays;
import java.util.Set;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.set.AbstractObservableSet;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Widget;

/**
 *
 */
public class CTabFolderWidgetObservableSet<T> extends AbstractObservableSet<T> {
	private Object elementType;
	private IElementComparer elementComparer;

	/**
	 * Flag to prevent infinite recursion in {@link #doSetValue(Object)}.
	 */
	protected boolean updating = false;

	/**
	 * The "old" selection before a selection event is fired.
	 */
	protected CTabItem[] currentSelection;

	/**
	 * The Control being observed here.
	 */
	private Widget cTabFolder;

	public CTabFolderWidgetObservableSet(Widget customWidget, Object elementType) {
		this(customWidget, elementType, Realm.getDefault());
	}

	public CTabFolderWidgetObservableSet(Widget customWidget, Object elementType, Realm realm) {
		super(realm);
		this.cTabFolder = customWidget;
		this.elementType = elementType;
		if (customWidget instanceof CTabFolder) {
			this.currentSelection = ((CTabFolder) customWidget).getItems();
		}
	}

	@Override
	public boolean add(T element) {
//		CTabItem c = new CTabItem((CTabFolder)cTabFolder, SWT.NONE);
		
//		c.setControl(element);
		return true;
	}
	
	@Override
	public boolean remove(Object o) {
//		cTabFolder.deselect((T) o);
//		currentSelection.remove(o);
		return true;
	}
	
//	@Override
//	public T remove(int index) {
//		T retval = cTabFolder.getItem(index);
//		cTabFolder.deselectAt(index);
//		return retval;
//	}
	
	@Override
	public void clear() {
		Arrays.stream(((CTabFolder)cTabFolder).getItems()).forEach(a -> a.dispose());
	}

	@Override
	public Object getElementType() {
		return elementType;
	}
//
//	@Override
//	protected int doGetSize() {
//		return cTabFolder.getItemCount();
//	}
//
//	@Override
//	public T get(int index) {
//		return (T) cTabFolder.getItem(index).getControl().getData();
////		return cTabFolder.getItem(index);
//	}

	@Override
	protected Set<T> getWrappedSet() {
		// TODO Auto-generated method stub
		return null;
	}

//	@Override
//	public void clear() {
//		cTabFolder.setSelection(new HashSet<T>());
//	}

}
