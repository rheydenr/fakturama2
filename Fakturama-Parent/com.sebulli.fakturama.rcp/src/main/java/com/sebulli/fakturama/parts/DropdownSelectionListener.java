package com.sebulli.fakturama.parts;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang3.BooleanUtils;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;

import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.resources.core.IconSize;

public class DropdownSelectionListener extends SelectionAdapter {

    @Inject
    private EHandlerService handlerService;

    @Inject
    private ECommandService commandService;
    
    @Inject
    private ILogger log;

    private Menu menu;

    private String defaultCommandId;
    
    public DropdownSelectionListener() {
    }
    
    @Inject
    public DropdownSelectionListener(Shell shell) {
        menu = new Menu(shell);
    }

    public void add(ContactTypeMenuItem contactType) {
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
    
    private void callHandler(String editorType) {
        Map<String, Object> params = new HashMap<>();
        params.put(CallEditor.PARAM_EDITOR_TYPE, editorType);
        params.put(CallEditor.PARAM_FORCE_NEW, BooleanUtils.toStringTrueFalse(true));
        ParameterizedCommand parameterizedCommand = commandService.createCommand(CommandIds.CMD_CALL_EDITOR, params);
        handlerService.executeHandler(parameterizedCommand);        
    }

    
    public void widgetSelected(SelectionEvent event) {
        // If they clicked the arrow, show the list
        if (event.detail == SWT.ARROW) {
            // Determine where to put the dropdown list
            ToolItem item = (ToolItem) event.widget;
            Rectangle rect = item.getBounds();
            Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
            menu.setLocation(pt.x, pt.y + rect.height);
            menu.setVisible(true);
        } else {
            // default action if clicked directly on the button
            // 
            if(event.widget.getData() == null) {
                if(getDefaultCommandId() != null) {
                    callHandler(getDefaultCommandId());        
                } else {
                    log.warn("no default command set for chevron coolitem");
                }
            } else {
                callHandler((String) event.widget.getData());
            }
        }
    }

    public String getDefaultCommandId() {
        return defaultCommandId;
    }

    public void setDefaultCommandId(String defaultCommandId) {
        this.defaultCommandId = defaultCommandId;
    }
}
