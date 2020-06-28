package com.sebulli.fakturama.parts;

import com.sebulli.fakturama.resources.core.Icon;

class DropdownMenuItem {
	String displayText, editorId;
	Icon icon;
	
	public DropdownMenuItem(String displayText, String editorId, Icon icon) {
		this.displayText = displayText;
		this.editorId = editorId;
		this.icon = icon;
	}
	
}