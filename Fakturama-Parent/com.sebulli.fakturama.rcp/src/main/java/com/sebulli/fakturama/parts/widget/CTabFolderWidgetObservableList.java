/**
 * 
 */
package com.sebulli.fakturama.parts.widget;

import java.util.Arrays;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.AbstractObservableList;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

/**
 *
 */
public class CTabFolderWidgetObservableList<T extends CTabItem> extends AbstractObservableList<T> {
	private Object elementType;

	/**
	 * Flag to prevent infinite recursion in {@link #doSetValue(Object)}.
	 */
	protected boolean updating = false;

	/**
	 * The "old" selection before a selection event is fired.
	 */
	protected T currentSelection;

	/**
	 * The Control being observed here.
	 */
	private CTabFolder cTabFolder;

	public CTabFolderWidgetObservableList(CTabFolder customWidget, Object elementType) {
		this(customWidget, elementType, Realm.getDefault());
	}

	public CTabFolderWidgetObservableList(CTabFolder customWidget, Object elementType, Realm realm) {
		super(realm);
		this.cTabFolder = customWidget;
		this.elementType = elementType;
		this.currentSelection = (T) customWidget.getSelection();
	}

	@Override
	public void add(int index, T element) {
//		CTabItem c = new CTabItem((CTabFolder)cTabFolder, SWT.NONE);
		System.out.println("");
//		c.setControl(element);
	}

	@Override
	public void clear() {
		Arrays.stream(cTabFolder.getItems()).forEach(a -> a.dispose());
	}

	@Override
	public Object getElementType() {
		return elementType;
	}

	@Override
	protected int doGetSize() {
		return cTabFolder.getItemCount();
	}

	@Override
	public T get(int index) {
		return (T) cTabFolder.getItem(index);
	}
}
