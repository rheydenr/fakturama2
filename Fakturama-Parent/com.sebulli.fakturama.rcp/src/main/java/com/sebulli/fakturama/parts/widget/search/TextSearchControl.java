/*******************************************************************************
 * Copyright (c) 2010 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package com.sebulli.fakturama.parts.widget.search;

import java.util.*;
import java.util.List;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;

import com.sebulli.fakturama.i18n.Messages;

/**
 * @author Shawn Minto
 */
public class TextSearchControl extends Composite {

	private static final String FIND_TEXT_MEMENTO_ELEMENT = "text"; //$NON-NLS-1$

	private static final String FIND_MEMENTO_ELEMENT = "historyItem"; //$NON-NLS-1$

	private static final String FIND_MEMENTO_TYPE = "filteredTreeFindHistory"; //$NON-NLS-1$

	private static final int MAX_HISTORY_ITEMS = 50;

	/**
	 * Image descriptor for enabled clear button.
	 */
	private static final String CLEAR_ICON = "org.eclipse.ui.internal.dialogs.CLEAR_ICON"; //$NON-NLS-1$

	/**
	 * Image descriptor for enabled clear button.
	 */
	private static final String FIND_ICON = "org.eclipse.ui.internal.dialogs.FIND_ICON"; //$NON-NLS-1$

	/**
	 * Get image descriptors for the clear button.
	 */
	static {
		ImageDescriptor descriptor = ImageDescriptor.createFromURL(TextSearchControl.class.getResource("clear_co.gif")); //$NON-NLS-1$
		if (descriptor != null) {
			JFaceResources.getImageRegistry().put(CLEAR_ICON, descriptor);
		}

		descriptor = ImageDescriptor.createFromURL(TextSearchControl.class.getResource("find.png"));
		if (descriptor != null) {
			JFaceResources.getImageRegistry().put(FIND_ICON, descriptor);
		}
	}

	private final Text textControl;
	private Control clearControl;
	private Control findControl;
	
	private final boolean automaticFind;
	private final Messages msg;

	private final Set<SelectionListener> selectionListeners = new HashSet<SelectionListener>();

	private Collection<String> searchHistory = new LinkedHashSet<String>();

	private boolean hasHistorySupport;

	private static Boolean useNativeSearchField;

	public TextSearchControl(Composite parent, boolean automaticFind, Messages msg) {
		super(parent, getCompositeStyle(automaticFind, parent));
		this.automaticFind = automaticFind;
		this.msg = msg;
		
		int textStyle = SWT.SINGLE;
		int numColumns = 1;
		if (useNativeSearchField(automaticFind, parent)) {
			if (automaticFind) {
				textStyle |= SWT.SEARCH | SWT.ICON_CANCEL;
			} else {
				textStyle |= SWT.SEARCH | SWT.ICON_SEARCH | SWT.ICON_CANCEL;
			}
		} else {
			super.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
			super.setFont(parent.getFont());
			numColumns = 2;
			if (!automaticFind) {
				numColumns += 1;
			}
		}
		GridLayoutFactory.swtDefaults().margins(0, 0).extendedMargins(0, 0, 0, 0).spacing(0, 1).numColumns(numColumns)
				.applyTo(this);

		textControl = new Text(this, textStyle);

		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		textControl.setLayoutData(gridData);

		if (useNativeSearchField == null || !useNativeSearchField) {
			findControl = createLabelButtonControl(this, textControl,
					JFaceResources.getImageRegistry().getDescriptor(FIND_ICON), "Find", /* main.menu.help.search */"", SWT.ICON_SEARCH);
			clearControl = createLabelButtonControl(this, textControl,
					JFaceResources.getImageRegistry().getDescriptor(CLEAR_ICON), "Tooltip", 
					this.msg.mainMenuEditDeleteName, SWT.ICON_CANCEL);
			addModifyListener(new ModifyListener() {

				public void modifyText(ModifyEvent e) {
					updateButtonVisibilityAndEnablement();

				}
			});
			updateButtonVisibilityAndEnablement();
		}

		registerListeners();
	}

	public boolean hasHistorySupport() {
		return hasHistorySupport;
	}

	private void registerListeners() {
		if(hasHistorySupport()) {
			addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> { 
					if (textControl != null && !textControl.isDisposed()) {
						addToSearchHistory(textControl.getText());
	//					System.out.println("SUCHE läuft! (selected)");
						// TODO: Suche auslösen!
					}
			}));
			textControl.addFocusListener(new FocusAdapter() {
	
				@Override
				public void focusLost(FocusEvent e) {
					if (textControl != null && !textControl.isDisposed()) {
						addToSearchHistory(textControl.getText());
	//System.out.println("SUCHE läuft! (Focus.out)");
						// TODO: Suche auslösen!
					}
				}
			});
		}
	}

	private static int getCompositeStyle(boolean automaticFind, Composite parent) {
		if (useNativeSearchField(automaticFind, parent)) {
			return SWT.NONE;
		}
		return SWT.BORDER;
	}

	private static boolean useNativeSearchField(boolean automaticFind, Composite parent) {
		if (parent != null) {
			if (useNativeSearchField == null) {
				useNativeSearchField = Boolean.FALSE;
				Text testText = null;
				try {
					int style = SWT.SEARCH | SWT.ICON_CANCEL;
					if (automaticFind) {
						style |= SWT.ICON_SEARCH;
					}
					testText = new Text(parent, style);
					useNativeSearchField = Boolean.valueOf((testText.getStyle() & SWT.ICON_CANCEL) != 0
							&& (!automaticFind || (testText.getStyle() & SWT.ICON_SEARCH) != 0));
				} finally {
					if (testText != null) {
						testText.dispose();
					}
				}

			}
		} else {
			useNativeSearchField = Boolean.FALSE;
		}
		return useNativeSearchField.booleanValue();
	}

	private Control createLabelButtonControl(Composite parent, final Text textControl,
			ImageDescriptor activeImageDescriptor, final String accessibilityText, String toolTipText,
			final int detail) {

		final Image nativeImage = parent.getDisplay().getSystemImage(detail);

		final Image activeImage = nativeImage != null ? nativeImage : activeImageDescriptor.createImage();
		final Image inactiveImage = new Image(parent.getDisplay(), activeImage, SWT.IMAGE_GRAY);
		final Image pressedImage = inactiveImage;

		final Label labelButton = new Label(parent, SWT.NONE);
		labelButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		labelButton.setImage(inactiveImage);
		labelButton.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		labelButton.setToolTipText(toolTipText);
		labelButton.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				if (nativeImage == null && activeImage != null && !activeImage.isDisposed()) {
					activeImage.dispose();
				}
				if (inactiveImage != null && !inactiveImage.isDisposed()) {
					inactiveImage.dispose();
				}
				if (pressedImage != null && !pressedImage.isDisposed()) {
					pressedImage.dispose();
				}
			}
		});
		labelButton.addMouseListener(new MouseAdapter() {
			private MouseMoveListener fMoveListener;

			@Override
			public void mouseDown(MouseEvent e) {
				labelButton.setImage(pressedImage);
				fMoveListener = new MouseMoveListener() {
					private boolean fMouseInButton = true;

					public void mouseMove(MouseEvent e) {
						boolean mouseInButton = isMouseInButton(e);
						if (mouseInButton != fMouseInButton) {
							fMouseInButton = mouseInButton;
							labelButton.setImage(mouseInButton ? pressedImage : inactiveImage);
						}
					}
				};
				labelButton.addMouseMoveListener(fMoveListener);
			}

			@Override
			public void mouseUp(MouseEvent e) {
				if (fMoveListener != null) {
					labelButton.removeMouseMoveListener(fMoveListener);
					fMoveListener = null;
					boolean mouseInButton = isMouseInButton(e);
					labelButton.setImage(mouseInButton ? activeImage : inactiveImage);
					if (mouseInButton) {
						if (textControl.isEnabled() && textControl.getText().length() > 0) {
							notifySelectionChanged(detail);
						}
					}
				}
			}

			private boolean isMouseInButton(MouseEvent e) {
				Point buttonSize = labelButton.getSize();
				return 0 <= e.x && e.x < buttonSize.x && 0 <= e.y && e.y < buttonSize.y;
			}
		});

		labelButton.addMouseTrackListener(new MouseTrackListener() {
			public void mouseEnter(MouseEvent e) {
				if (labelButton.getImage() != activeImage) {
					labelButton.setImage(activeImage);
				}
			}

			public void mouseExit(MouseEvent e) {
				if (labelButton.getImage() != inactiveImage) {
					labelButton.setImage(inactiveImage);
				}
			}

			public void mouseHover(MouseEvent e) {
			}
		});

		labelButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = accessibilityText;
			}
		});
		labelButton.getAccessible().addAccessibleControlListener(new AccessibleControlAdapter() {
			@Override
			public void getRole(AccessibleControlEvent e) {
				e.detail = ACC.ROLE_PUSHBUTTON;
			}
		});
		return labelButton;
	}

	public void addSelectionListener(SelectionListener listener) {
		textControl.addSelectionListener(listener);
		selectionListeners.add(listener);
	}

	public void removeSelectionListener(SelectionListener listener) {
		textControl.removeSelectionListener(listener);
		selectionListeners.remove(listener);
	}

	public void addModifyListener(ModifyListener listener) {
		textControl.addModifyListener(listener);
		clearControl.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				textControl.clearSelection();
				textControl.setText("");
				super.mouseDown(e);
			}
		});
	}

	public void removeModifyListener(ModifyListener listener) {
		textControl.removeModifyListener(listener);
	}

	private void notifySelectionChanged(int detail) {
		if (useNativeSearchField != null && useNativeSearchField) {
			// notification should happen via the widgets selection listener
			return;
		}
		Event event = new Event();
		event.detail = detail;
		event.widget = textControl;
		event.display = textControl.getDisplay();

		SelectionEvent e = new SelectionEvent(event);
		for (Object element : selectionListeners) {
			((SelectionListener) element).widgetDefaultSelected(e);
		}
	}

	private void updateButtonVisibilityAndEnablement() {
		if (textControl != null && !textControl.isDisposed()) {
			boolean hasText = textControl.getText().length() > 0;

			setFindButtonVisibility(!(hasText && automaticFind));
			setClearButtonVisibility(hasText);
		}
	}

	private void setFindButtonVisibility(boolean visible) {
		if (findControl != null && !findControl.isDisposed()) {
			findControl.setVisible(visible);
			if (findControl.getLayoutData() instanceof GridData) {
				((GridData) findControl.getLayoutData()).exclude = !visible;
				findControl.getParent().layout();
			}
		}
	}

	private void setClearButtonVisibility(boolean visible) {
		if (clearControl != null && !clearControl.isDisposed()) {
			clearControl.setVisible(visible);
			if (clearControl.getLayoutData() instanceof GridData) {
				((GridData) clearControl.getLayoutData()).exclude = !visible;
				clearControl.getParent().layout();
			}
		}
	}

	public Text getTextControl() {
		return textControl;
	}

	@Override
	public void setBackground(Color color) {
		if (useNativeSearchField != null && useNativeSearchField) {
			super.setBackground(color);
		}
	}

	public Collection<String> getSearchHistory() {
		return Collections.unmodifiableCollection(searchHistory);
	}

	void addToSearchHistory(String searchString) {
		if (searchString != null && searchString.trim().length() > 0) {
			searchHistory.remove(searchString);
			searchHistory.add(searchString);
		}
	}

	public void saveState(IMemento memento) {
		if (memento == null) {
			return;
		}

		IMemento rootMemento = memento.createChild(FIND_MEMENTO_TYPE);
		int i = 0;
		for (String text : searchHistory) {
			IMemento child = rootMemento.createChild(FIND_MEMENTO_ELEMENT);
			child.putString(FIND_TEXT_MEMENTO_ELEMENT, text);
			i++;
			if (i > MAX_HISTORY_ITEMS) {
				break;
			}
		}
	}

	public void restoreState(IMemento memento) {
		if (memento == null) {
			return;
		}
		List<String> history = new ArrayList<String>();

		IMemento rootMemento = memento.getChild(FIND_MEMENTO_TYPE);
		if (rootMemento != null) {
			IMemento[] children = rootMemento.getChildren(FIND_MEMENTO_ELEMENT);
			if (children != null && children.length > 0) {
				for (IMemento child : children) {
					String historyItem = child.getString(FIND_TEXT_MEMENTO_ELEMENT);
					// add to the end of the list
					history.add(history.size(), historyItem);
				}
			}
		}
		if (history != null) {
			searchHistory = history;
		}
	}
}
