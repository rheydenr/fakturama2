package com.sebulli.fakturama.dialogs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;

public class TreeItem<T> implements Iterable<TreeItem<T>>, IAdaptable {
	
	private List<TreeItem<T>> children = new ArrayList<>();
	private TreeItem<T> parent;
	private T item;
	private Class<T> myClass;
	
	@SuppressWarnings("unchecked")
	public TreeItem(T item) {
		this.item = item;
		this.myClass = (Class<T>) item.getClass();
	}
	
	public T getItem() {
		return item;
	}

	public TreeItem<T> getParent() {
		return parent;
	}
	
	public void setParent(TreeItem<T> parent) {
		this.parent = parent;
	}
	
	public void add(TreeItem<T> child) {
		child.setParent(this);
		children.add(child);
	}
	
	public boolean hasChildren() {
		return !children.isEmpty();
	}
	
	public void remove(TreeItem<T> child) {
		child.setParent(null);
		children.remove(child);
	}

	@Override
	public Iterator<TreeItem<T>> iterator() {
		return children.iterator();
	}
	
	@Override
	public String toString() {
		return String.valueOf(getItem());
	}
	
	public Class<T> getClassType() {
		return myClass;
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		return null;
	}
}