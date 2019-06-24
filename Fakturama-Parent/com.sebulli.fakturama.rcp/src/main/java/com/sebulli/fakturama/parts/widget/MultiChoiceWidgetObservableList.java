package com.sebulli.fakturama.parts.widget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.eclipse.core.databinding.observable.Diffs;
import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.AbstractObservableList;
import org.eclipse.nebula.widgets.opal.multichoice.MultiChoice;
import org.eclipse.nebula.widgets.opal.multichoice.MultiChoiceSelectionListener;
import org.eclipse.swt.widgets.Shell;

import com.ibm.icu.util.Currency;

public class MultiChoiceWidgetObservableList<T> extends AbstractObservableList<T> {
	private Object elementType;

	/**
	 * Flag to prevent infinite recursion in {@link #doSetValue(Object)}.
	 */
	protected boolean updating = false;

	/**
	 * The "old" selection before a selection event is fired.
	 */
	protected List<T> currentSelection = new ArrayList<>();

	/**
	 * The Control being observed here.
	 */
	private MultiChoice<T> multiChoiceWidget;

	public MultiChoiceWidgetObservableList(MultiChoice<T> customWidget, Object elementType) {
		this(customWidget, elementType, Realm.getDefault());
	}

	public MultiChoiceWidgetObservableList(MultiChoice<T> customWidget, Object elementType, Realm realm) {
		super(realm);
		this.multiChoiceWidget = customWidget;
		this.elementType = elementType;
		this.currentSelection = multiChoiceWidget.getSelection();
		MultiChoiceSelectionListener<T> listener = new MultiChoiceSelectionListener<T>(multiChoiceWidget) {

			@Override
			public void handle(MultiChoice<T> parent, T receiver, boolean selected, Shell popup) {
				if (!updating) {
//					currentSelection = multiChoiceWidget.getSelection();
					List<T> newSelection = new ArrayList<>(currentSelection);
					if(selected) {
						// a value was added to the list
						newSelection.add(receiver);
					} else {
						newSelection.remove(receiver);
					}
					
					if (((newSelection != null) && !newSelection.equals(currentSelection))
							|| ((currentSelection != null) && !currentSelection.equals(newSelection))) {

						fireListChange(Diffs.computeListDiff(currentSelection, newSelection));
					}
					currentSelection = newSelection;
				}
			}
		};
		this.multiChoiceWidget.setSelectionListener(listener);
	}
	
	
	
	@Override
	public void add(int index, T element) {
		multiChoiceWidget.selectAt(index);
		currentSelection.add(element);
	}
	
	@Override
	public boolean remove(Object o) {
		multiChoiceWidget.deselect((T) o);
		currentSelection.remove(o);
		return true;
	}
	
	@Override
	public T remove(int index) {
		T retval = multiChoiceWidget.getItem(index);
		multiChoiceWidget.deselectAt(index);
		return retval;
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
