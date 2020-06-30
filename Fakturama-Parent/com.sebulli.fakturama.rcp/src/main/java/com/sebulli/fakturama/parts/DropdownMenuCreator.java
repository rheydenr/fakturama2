package com.sebulli.fakturama.parts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.resources.core.IconSize;

/**
 * Create the dropdown menu for the chevron buttons in the cool bar. 
 *
 */
public class DropdownMenuCreator implements IMenuCreator {

    @Inject
    private EHandlerService handlerService;

    @Inject
    private ECommandService commandService;

    private Menu menu;

    private String defaultCommandId;
    private List<DropdownMenuItem> menuItems = new ArrayList<>();

    @Inject
    public DropdownMenuCreator(Shell shell) {
        menu = new Menu(shell);
    }

    public void add(DropdownMenuItem contactType) {
        menuItems.add(contactType);
    }

    private void callHandler(String commandId) {
        Map<String, Object> params = new HashMap<>();
        params.put(CallEditor.PARAM_EDITOR_TYPE, commandId);
        params.put(CallEditor.PARAM_FORCE_NEW, BooleanUtils.toStringTrueFalse(true));
        ParameterizedCommand parameterizedCommand = commandService.createCommand(CommandIds.CMD_CALL_EDITOR, params);
        handlerService.executeHandler(parameterizedCommand);
    }

    public String getDefaultCommandId() {
        return defaultCommandId;
    }

    public void setDefaultCommandId(String defaultCommandId) {
        this.defaultCommandId = defaultCommandId;
    }

    @Override
    public void dispose() {
    }

    @Override
    public Menu getMenu(Menu parent) {
        setMenu(new Menu(parent));
        fillMenu(menu);
        initMenu();
        return menu;
    }

    @Override
    public Menu getMenu(Control parent) {
        setMenu(new Menu(parent));
        fillMenu(menu);
        initMenu();
        return menu;
    }

    private void setMenu(Menu menu) {
        this.menu = menu;
    }

    private void initMenu() {
        menu.addMenuListener(new MenuAdapter() {
            @Override
            public void menuShown(MenuEvent e) {
                Menu m = (Menu) e.widget;
                Arrays.stream(m.getItems()).forEach(item -> item.dispose());
                fillMenu(m);
            }
        });
    }

    private void fillMenu(Menu menu) {
        for (DropdownMenuItem contactType : menuItems) {
            MenuItem item = new MenuItem(menu, SWT.NONE);
            item.setText(contactType.displayText);
            item.setData(contactType.editorId);
            item.setImage(contactType.icon.getImage(IconSize.DefaultIconSize));
            item.addSelectionListener(new SelectionAdapter() {
                public void widgetSelected(SelectionEvent event) {
                    callHandler((String) event.widget.getData());
                }
            });
        }
    }
}
