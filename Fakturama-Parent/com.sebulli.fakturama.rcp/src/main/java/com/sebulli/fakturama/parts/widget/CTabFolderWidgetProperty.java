package com.sebulli.fakturama.parts.widget;

import java.util.List;

import org.eclipse.core.databinding.observable.Realm;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.property.INativePropertyListener;
import org.eclipse.core.databinding.property.ISimplePropertyListener;
import org.eclipse.core.databinding.property.list.SimpleListProperty;
import org.eclipse.jface.databinding.swt.DisplayRealm;
import org.eclipse.jface.databinding.swt.ISWTObservableList;
import org.eclipse.jface.internal.databinding.swt.SWTObservableListDecorator;
import org.eclipse.swt.custom.CTabFolder;

import com.sebulli.fakturama.model.DocumentReceiver;

public class CTabFolderWidgetProperty extends SimpleListProperty<CTabFolder, DocumentReceiver> {

	
	@Override
	public Object getElementType() {
		return DocumentReceiver.class;
	}

	@Override
	protected List<DocumentReceiver> doGetList(CTabFolder source) {
		// TODO Auto-generated method stub
		System.out.println("huhu");
		return null;
	}

	@Override
	protected void doSetList(CTabFolder source, List<DocumentReceiver> list, ListDiff<DocumentReceiver> diff) {
		System.out.println("huhu");
		// TODO Auto-generated method stub

	}

	@Override
	public INativePropertyListener<CTabFolder> adaptListener(
			ISimplePropertyListener<CTabFolder, ListDiff<DocumentReceiver>> listener) {
		System.out.println("huhu");
		// TODO Auto-generated method stub
		return null;
	}
//	
//	@Override
//	public IObservableList<DocumentReceiver> observe(CTabFolder source) {
//		if (source instanceof Widget) {
//			return observe((Widget) source);
//		}
//		return super.observe(source);
//	}

	@Override
	public IObservableList<DocumentReceiver> observe(Realm realm, CTabFolder source) {
		return new SWTObservableListDecorator(super.observe(realm, source),
				source);
	}

	@Override
	public ISWTObservableList observe(CTabFolder widget) {
		return (ISWTObservableList) observe(DisplayRealm.getRealm(widget
				.getDisplay()), widget);
	}
}
