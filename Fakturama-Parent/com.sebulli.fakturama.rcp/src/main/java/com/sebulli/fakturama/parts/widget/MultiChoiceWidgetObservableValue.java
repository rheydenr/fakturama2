package com.sebulli.fakturama.parts.widget;

import java.util.HashSet;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.AbstractObservableList;
import org.eclipse.nebula.widgets.opal.multichoice.MultiChoice;

public class MultiChoiceWidgetObservableValue<T> extends AbstractObservableList<T> {
	private Object elementType;

	private MultiChoice<T> multiChoiceWidget;

	public MultiChoiceWidgetObservableValue(MultiChoice<T> customWidget, Object elementType) {
		this(customWidget, elementType, Realm.getDefault());
	}

	public MultiChoiceWidgetObservableValue(MultiChoice<T> customWidget, Object elementType, Realm realm) {
		super(realm);
		this.multiChoiceWidget = customWidget;
		this.elementType = elementType;
	}

	@Override
	public Object getElementType() {
		return elementType;
	}

	@Override
	protected int doGetSize() {
		return multiChoiceWidget.getItemCount();
	}

	@Override
	public T get(int index) {
		return multiChoiceWidget.getItem(index);
	}
	
	@Override
	public void clear() {
		multiChoiceWidget.setSelection(new HashSet<T>());
	}
}
