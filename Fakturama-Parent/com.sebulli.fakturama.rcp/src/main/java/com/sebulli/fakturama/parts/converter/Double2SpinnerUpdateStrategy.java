package com.sebulli.fakturama.parts.converter;

import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;

public class Double2SpinnerUpdateStrategy extends UpdateValueStrategy {
	@Override
	protected IStatus doSet(IObservableValue observableValue, Object value) {
//		int newValue = (int) (((Double) value) * 10);
		return super.doSet(observableValue, value);
	}
}
