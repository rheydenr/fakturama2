package com.sebulli.fakturama.parts;

import com.sebulli.fakturama.resources.core.Icon;

class ContactTypeMenuItem {
	String displayText, editorId;
	Icon icon;
	
	public ContactTypeMenuItem(String displayText, String editorId, Icon icon) {
		this.displayText = displayText;
		this.editorId = editorId;
		this.icon = icon;
	}
	
}