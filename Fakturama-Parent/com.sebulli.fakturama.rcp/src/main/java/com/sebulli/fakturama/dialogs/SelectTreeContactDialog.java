/**
 * 
 */
package com.sebulli.fakturama.dialogs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.internal.events.EventBroker;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.dialogs.AbstractSelectionDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.event.Event;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.misc.Constants;
import com.sebulli.fakturama.model.Address;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.parts.widget.contacttree.DebitorTreeListTable;

/**
 *
 */
public class SelectTreeContactDialog<T extends Address> extends AbstractSelectionDialog<T> {
    protected static final Point DEFAULT_DIALOG_SIZE = new Point(800, 550);

    protected String editor = "";
    
    @Inject
    @Translation
    protected Messages msg;
   
    @Inject
    private IEventBroker evtBroker;
    
    @Inject
    private EModelService modelService;

    protected String title = "";
    
    @Inject
    private IEclipseContext context;
    
    private Control top;

	private DebitorTreeListTable contactListTable;

    @Inject
    public SelectTreeContactDialog(Shell shell, @Translation Messages msg) {
        super(shell);
        this.msg = msg;
        // Set the title
        setTitle(msg.dialogSelectaddressTitle);
    }
    
    @PostConstruct
    public void init(Shell shell) {
        this.top = (Control) context.get(Composite.class); 
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        // Create the top composite dialog area
        top = (Composite) super.createDialogArea(parent);
        GridLayoutFactory.fillDefaults().numColumns(1).applyTo((Composite) top);

        context.set(Composite.class, (Composite) top);
        context.set(IEventBroker.class, evtBroker);
        MPart part = modelService.createModelElement(MPart.class);
        part.setContext(context);
        part.getTransientData().put(Constants.PROPERTY_CONTACTS_CLICKHANDLER, Constants.COMMAND_SELECTITEM);
        context.set(MPart.class, part);
        // FIXME Workaround (quick & dirty), please use enums
        if(StringUtils.equals((String) context.get("CONTACT_TYPE"), "DEBITOR")) {
        	contactListTable = (DebitorTreeListTable) ContextInjectionFactory.make(DebitorTreeListTable.class, context);
//        } else {
//        	contactListTable = (ContactListTable<T>) ContextInjectionFactory.make(CreditorListTable.class, context);
        }

        GridDataFactory.fillDefaults().grab(true, true).applyTo(top);
    	contactListTable.getSearchControl().getTextControl().setFocus();

        return top;
    }
    /**
     * If an entry is selected it will be put in an Event which will be posted by {@link EventBroker}.
     * After this the dialog closes. The Event is caught by the {@link DocumentEditor} which will use it as
     * billing or delivery address.
     */
    @SuppressWarnings("unchecked")
	@Override
    protected void okPressed() {
        if (contactListTable.getSelectedObject() != null) {
            Map<String, Object> eventParams = new HashMap<>();
            eventParams.put(DocumentEditor.DOCUMENT_ID, context.get(DocumentEditor.DOCUMENT_ID));
            eventParams.put(DebitorTreeListTable.SELECTED_ADDRESS_ID, Long.valueOf(contactListTable.getSelectedObject().getAddress().getId()));
            
            Collection<T> tmpList = new ArrayList<>();
            tmpList.add((T) contactListTable.getSelectedObject().getAddress());
            setResult(tmpList);
            // inform the DocumentEditor (or any other Editor) about the selection
            evtBroker.post("DialogSelection/Contact", eventParams);
            // TODO Unterscheidung zw. Billing / Delivery! siehe Altcode
        }
        super.okPressed();
    }

        
    /**
     * Called if a user doubleclicks on an entry. Then the entry will be selected and the dialog closes. Since these are
     * two different actions it couldn't put into one method.
     *  
     * @param event
     */
    @SuppressWarnings("unchecked")
	@Inject
    @org.eclipse.e4.core.di.annotations.Optional
    protected void handleDialogDoubleClickClose(@UIEventTopic("DialogAction/CloseContact") Event event) {
        if (event != null) {
            if (contactListTable.getSelectedObject() != null) {
                // only for convenience, the result is already set by NatTable on double click and send to the 
                // DocumentEditor.
                Collection<T> tmpList = new ArrayList<>();
                tmpList.add((T) contactListTable.getSelectedObject().getAddress());
                setResult(tmpList);
            }
            super.okPressed();
        }
    }

    /**
     * Set the initial size of the dialogs in pixel
     * 
     * @return Size as Point object
     */
    @Override
    protected Point getInitialSize() {
        Point initalSize = super.getInitialSize();
        if (initalSize != null && (initalSize.x < DEFAULT_DIALOG_SIZE.x || initalSize.y < DEFAULT_DIALOG_SIZE.y)) {
            initalSize = DEFAULT_DIALOG_SIZE;
        }
        return initalSize;
    }
}
