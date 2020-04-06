package com.sebulli.fakturama.parts.widget;

import org.eclipse.jface.databinding.swt.WidgetValueProperty;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;

import com.sebulli.fakturama.model.IEntity;

public class CTabItemSelectionProperty<E extends IEntity> extends WidgetValueProperty<CTabFolder, E> {
	CTabItemSelectionProperty(int event) {
		super(event);
	}

	CTabItemSelectionProperty(int[] events) {
		super(events);
	}

	CTabItemSelectionProperty(int[] events, int[] staleEvents) {
		super(events, staleEvents);
	}

	@Override
	public Object getValueType() {
		return String.class;
	}

	@Override
	protected E doGetValue(CTabFolder source) {
		int index = source.getSelectionIndex();
		if (index >= 0) {
			CTabItem item = source.getItem(index);
			return (E) item.getData();
		}
		return null;
	}

	@Override
	protected void doSetValue(CTabFolder source, E value) {
		CTabItem[] items = source.getItems();
		int index = -1;
		if (items != null && value != null) {
			for (int i = 0; i < items.length; i++) {
				if (value.equals(items[i])) {
					index = i;
					break;
				}
			}
			source.setSelection(index);
		}
	}

	public CTabItemSelectionProperty() {
		super(SWT.Selection);
	}

	@Override
	public String toString() {
		return "List.selection <String>"; //$NON-NLS-1$
	}

//
//	@Override
//	public Object getElementType() {
//		return DocumentReceiver.class;
//	}
//
//	@Override
//	protected List<DocumentReceiver> doGetList(CTabFolder source) {
//		// TODO Auto-generated method stub
//		System.out.println("huhu");
//		return null;
//	}
//
//	@Override
//	protected void doSetList(CTabFolder source, List<DocumentReceiver> list, ListDiff<DocumentReceiver> diff) {
//		System.out.println("huhu");
//		// TODO Auto-generated method stub
//
//	}
//
//	@Override
//	public INativePropertyListener<CTabFolder> adaptListener(
//			ISimplePropertyListener<CTabFolder, ListDiff<DocumentReceiver>> listener) {
//		System.out.println("huhu");
//		// TODO Auto-generated method stub
//		return null;
//	}
////	
////	@Override
////	public IObservableList<DocumentReceiver> observe(CTabFolder source) {
////		if (source instanceof Widget) {
////			return observe((Widget) source);
////		}
////		return super.observe(source);
////	}
//
//	@Override
//	public IObservableList<DocumentReceiver> observe(Realm realm, CTabFolder source) {
//		return new SWTObservableListDecorator(super.observe(realm, source),
//				source);
//	}
//
//	@Override
//	public ISWTObservableList observe(CTabFolder widget) {
//		return (ISWTObservableList) observe(DisplayRealm.getRealm(widget
//				.getDisplay()), widget);
//	}
}
