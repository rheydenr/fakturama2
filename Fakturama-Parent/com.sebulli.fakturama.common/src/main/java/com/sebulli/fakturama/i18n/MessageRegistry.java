package com.sebulli.fakturama.i18n;

import javax.inject.Inject;

import org.eclipse.e4.core.di.annotations.Creatable;
import org.eclipse.e4.core.services.nls.BaseMessageRegistry;
import org.eclipse.e4.core.services.nls.Translation;

@Creatable
public class MessageRegistry extends BaseMessageRegistry<Messages> {

	@Override
	@Inject
	public void updateMessages(@Translation Messages messages) {
		super.updateMessages(messages);
	}
}

