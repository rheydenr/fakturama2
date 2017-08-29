/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2015 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.views.datatable.texts;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.extension.e4.selection.E4SelectionListener;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.LayerUtil;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.nebula.widgets.nattable.viewport.action.ViewportSelectRowAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.sebulli.fakturama.dao.AbstractDAO;
import com.sebulli.fakturama.dao.TextCategoriesDAO;
import com.sebulli.fakturama.dao.TextsDAO;
import com.sebulli.fakturama.handlers.CallEditor;
import com.sebulli.fakturama.handlers.CommandIds;
import com.sebulli.fakturama.model.TextCategory;
import com.sebulli.fakturama.model.TextModule;
import com.sebulli.fakturama.model.TextModule_;
import com.sebulli.fakturama.parts.DocumentEditor;
import com.sebulli.fakturama.parts.Editor;
import com.sebulli.fakturama.parts.TextEditor;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.EntityGridListLayer;
import com.sebulli.fakturama.views.datatable.impl.NoHeaderRowOnlySelectionBindings;
import com.sebulli.fakturama.views.datatable.tree.model.TreeObject;
import com.sebulli.fakturama.views.datatable.tree.ui.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeCategoryLabelProvider;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

/**
 * Builds the Texts list table.
 *
 */
public class TextListTable extends AbstractViewDataTable<TextModule, TextCategory> {

    // ID of this view
    public static final String ID = "fakturama.views.viewTextTable";
    
    protected static final String POPUP_ID = "com.sebulli.fakturama.textlist.popup";
    public static final String SELECTED_TEXT_ID = "fakturama.textslist.selected.text.id";

/**    this is for synchronizing the UI thread */
    @Inject    
    private UISynchronize sync;
    
    @Inject
    private TextsDAO textsDAO;
    
    @Inject
    protected IEclipseContext context;

    @Inject
    private TextCategoriesDAO textCategoriesDAO;
    
    protected MPart listTablePart;
    private TextModule selectedObject;

    private EventList<TextModule> textListData;
    private EventList<TextCategory> categories;

    private EntityGridListLayer<TextModule> gridListLayer;

    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    protected FilterList<TextModule> treeFilteredIssues;


    @PostConstruct
    public Control createPartControl(Composite parent, MPart listTablePart) {
        log.info("create TextModule list part");
       super.createPartControl(parent, TextModule.class, true, ID);
       this.listTablePart = listTablePart;
       // if another click handler is set we use it
       // Listen to double clicks
       Object commandId = this.listTablePart.getProperties().get("fakturama.datatable.texts.clickhandler");
       if(commandId != null) { // exactly it would be "com.sebulli.fakturama.command.selectitem"
           hookDoubleClickCommand(natTable, getGridLayer(), (String) commandId);
       } else {
           hookDoubleClickCommand2(natTable, getGridLayer());
       }
        topicTreeViewer.setTable(this);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(top);
        return top;
    }
    
    @Override
    protected void postConfigureNatTable(NatTable natTable) {
        //as the autoconfiguration of the NatTable is turned off, we have to add the 
        //DefaultNatTableStyleConfiguration and the ConfigRegistry manually 
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new NoHeaderRowOnlySelectionBindings());
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new TextModuleTableConfiguration());
        natTable.setBackground(GUIHelper.COLOR_WHITE);
        // nur für das Headermenü, falls das mal irgendwann gebraucht werden sollte
        //      natTable.addConfiguration(new HeaderMenuConfiguration(n6));
        gridListLayer.getSelectionLayer().getSelectionModel().setMultipleSelectionAllowed(true);

        E4SelectionListener<TextModule> esl = new E4SelectionListener<>(selectionService, gridListLayer.getSelectionLayer(), gridListLayer.getBodyDataProvider());
        gridListLayer.getSelectionLayer().addLayerListener(esl);

        // Change the default sort key bindings. Note that 'auto configure' was turned off
        // for the SortHeaderLayer (setup in the GlazedListsGridLayer)
        natTable.addConfiguration(new SingleClickSortConfiguration());
        
        // register right click as a selection event for the whole row
        natTable.getUiBindingRegistry().registerMouseDownBinding(
                new MouseEventMatcher(SWT.NONE, GridRegion.BODY, MouseEventMatcher.RIGHT_BUTTON),

                new IMouseAction() {

                    ViewportSelectRowAction selectRowAction = new ViewportSelectRowAction(false, false);
                                
                    @Override
                    public void run(NatTable natTable, MouseEvent event) {
                        int rowPosition = natTable.getRowPositionByY(event.y);
                        if(!gridListLayer.getSelectionLayer().isRowPositionSelected(rowPosition)) {
                            selectRowAction.run(natTable, event);
                        }                   
                    }
                });
        natTable.configure();
    }

    /**
     * @param propertyNames
     * @return
     */
    private IColumnPropertyAccessor<TextModule> createColumnPropertyAccessor(String[] propertyNames) {
        final IColumnPropertyAccessor<TextModule> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<TextModule>(propertyNames);
        
        // Add derived 'default' column
        final IColumnPropertyAccessor<TextModule> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<TextModule>() {

            public Object getDataValue(TextModule rowObject, int columnIndex) {
                TextModuleListDescriptor descriptor = TextModuleListDescriptor.getDescriptorFromColumn(columnIndex);
                switch (descriptor) {
                case NAME:
                case TEXT:
                    // alternative: return rowObject.getFirstName();
                    return columnPropertyAccessor.getDataValue(rowObject, columnIndex);
                default:
                    break;
                }
                return null;
            }

            public void setDataValue(TextModule rowObject, int columnIndex, Object newValue) {
                throw new UnsupportedOperationException("you can't change a value in list view!");
            }

            public int getColumnCount() {
                return TextModuleListDescriptor.getTextModulePropertyNames().length;
            }

            public String getColumnProperty(int columnIndex) {
                TextModuleListDescriptor descriptor = TextModuleListDescriptor.getDescriptorFromColumn(columnIndex);
                return msg.getMessageFromKey(descriptor.getMessageKey());
            }

            public int getColumnIndex(String propertyName) {
                    return columnPropertyAccessor.getColumnIndex(propertyName);
            }
        };
        return derivedColumnPropertyAccessor;
    }
    
    public NatTable createListTable(Composite searchAndTableComposite) {

        textListData = GlazedLists.eventList(textsDAO.findAll(true));

        // get the visible properties to show in list view
        String[] propertyNames = textsDAO.getVisibleProperties();

        final IColumnPropertyAccessor<TextModule> derivedColumnPropertyAccessor = createColumnPropertyAccessor(propertyNames);

        /*
        // Mark the columns that are used by the search function.
        searchColumns = new String[2];
        searchColumns[0] = "name";
        searchColumns[1] = "text";
 */
        final MatcherEditor<TextModule> textMatcherEditor = new TextWidgetMatcherEditor<TextModule>(searchText, 
                GlazedLists.textFilterator(TextModule.class, TextModule_.name.getName(), TextModule_.text.getName()));
        
        // Filtered list for Search text field filter
        final FilterList<TextModule> textFilteredIssues = new FilterList<TextModule>(textListData, textMatcherEditor);

        // build the list for the tree-filtered values (i.e., the value list which is affected by
        // tree selection)
        treeFilteredIssues = new FilterList<TextModule>(textFilteredIssues);
       
        gridListLayer = new EntityGridListLayer<>(treeFilteredIssues, propertyNames, derivedColumnPropertyAccessor, configRegistry);
        
        DataLayer tableDataLayer = gridListLayer.getBodyDataLayer();
        tableDataLayer.setColumnPercentageSizing(true);
        tableDataLayer.setColumnWidthPercentageByPosition(0, 5);
        tableDataLayer.setColumnWidthPercentageByPosition(1, 15);

        //turn the auto configuration off as we want to add our header menu configuration
        NatTable natTable = new NatTable(searchAndTableComposite, gridListLayer.getGridLayer(), false);
       
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));

        return natTable;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.AbstractViewDataTable#getTableId()
     */
    @Override
    public String getTableId() {
        return ID;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.AbstractViewDataTable#getEditorId()
     */
    @Override
    protected String getEditorId() {
        return TextEditor.ID;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.AbstractViewDataTable#getEditorTypeId()
     */
    @Override
    protected String getEditorTypeId() {
        return TextEditor.EDITOR_ID;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.AbstractViewDataTable#createCategoryTreeViewer(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected TopicTreeViewer<TextCategory> createCategoryTreeViewer(Composite top) {
        topicTreeViewer = new TopicTreeViewer<TextCategory>(top, msg, false, true);
//    	topicTreeViewer = (TopicTreeViewer<TextCategory>)ContextInjectionFactory.make(TopicTreeViewer.class, context);
        categories = GlazedLists.eventList(textCategoriesDAO.findAll());
        topicTreeViewer.setInput(categories);
        topicTreeViewer.setLabelProvider(new TreeCategoryLabelProvider());
        return topicTreeViewer;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.AbstractViewDataTable#getPopupId()
     */
    @Override
    protected String getPopupId() {
        return POPUP_ID;
    }

    @Override
    protected String getToolbarAddItemCommandId() {
        return CommandIds.LISTTOOLBAR_ADD_DOCUMENT;
    }

    @Override
    protected MToolBar getMToolBar() {
        return listTablePart.getToolbar();
    }

    @Inject @Optional
    public void handleRefreshEvent(@EventTopic(TextEditor.EDITOR_ID) String message) {
    	if(StringUtils.equals(message, Editor.UPDATE_EVENT)) {
	        sync.syncExec(() -> top.setRedraw(false));
	        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
	        GlazedLists.replaceAll(textListData, GlazedLists.eventList(textsDAO.findAll(true)), false);
	        GlazedLists.replaceAll(categories, GlazedLists.eventList(textCategoriesDAO.findAll(true)), false);
	        sync.syncExec(() -> top.setRedraw(true));
    	}
    }
    
    private void hookDoubleClickCommand(final NatTable nattable, final EntityGridListLayer<TextModule> gridLayer, String commandId) {
        if (commandId != null) {
            // if we are in "selecttext" mode we have to register a single click mouse event
            nattable.getUiBindingRegistry().registerFirstSingleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), new IMouseAction() {

				public void run(NatTable natTable, MouseEvent event) {
                    int rowPos = natTable.getRowPositionByY(event.y);
                    int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPos, gridLayer.getBodyDataLayer());
                    selectedObject = gridLayer.getBodyDataProvider().getRowObject(bodyRowPos);
                }
            });
        }
        
        // Add a double click listener
        nattable.getUiBindingRegistry().registerDoubleClickBinding(MouseEventMatcher.bodyLeftClick(SWT.NONE), new IMouseAction() {

            @Override
            public void run(NatTable natTable, MouseEvent event) {
                //get the row position for the click in the NatTable
                int rowPos = natTable.getRowPositionByY(event.y);
                //transform the NatTable row position to the row position of the body layer stack
                int bodyRowPos = LayerUtil.convertRowPosition(natTable, rowPos, gridLayer.getBodyDataLayer());
                // extract the selected Object
                selectedObject = gridLayer.getBodyDataProvider().getRowObject(bodyRowPos);
                // Call the corresponding editor. The editor is set
                // in the variable "editor", which is used as a parameter
                // when calling the editor command.
                // in E4 we create a new Part (or use an existing one with the same ID)
                // from PartDescriptor
                Map<String, Object> params = new HashMap<>();
                ParameterizedCommand parameterizedCommand;
                if(commandId != null) {
                    // If we don't give a target document number the event will  be catched by *all*
                    // open editors which listens to this event. This is (obviously :-) ) not
                    // the intended behavior...
                    Map<String, Object> eventParams = new HashMap<>();
                    // the transientData HashMap contains the target document number
                    // (was set in MouseEvent handler)
                    eventParams.put(DocumentEditor.DOCUMENT_ID, context.get(DocumentEditor.DOCUMENT_ID));
                    eventParams.put(SELECTED_TEXT_ID, Long.valueOf(selectedObject.getId()));
//                    // alternatively use the Selection Service
                    // ==> no! Because this SelectionService has another context than 
                    // the receiver of this topic. Therefore the receiver's SelectionService
                    // is empty :-(
//                    selectionService.setSelection(selectedObject);
                    evtBroker.post("DialogSelection/TextModule", eventParams);
                    evtBroker.post("DialogAction/CloseTextModule", eventParams);
                } else {
                    // if we come from the list view then we should open a new editor 
                    params.put(CallEditor.PARAM_OBJ_ID, Long.toString(selectedObject.getId()));
                    params.put(CallEditor.PARAM_EDITOR_TYPE, getEditorId());
                    parameterizedCommand = commandService.createCommand(CommandIds.CMD_CALL_EDITOR, params);
                    handlerService.executeHandler(parameterizedCommand);
                }
            }
        });
    }
    
    @Override
    public TextModule getSelectedObject() {
        return selectedObject;
    }

    @Override
    protected void hookDoubleClickCommand2(final NatTable nattable, final EntityGridListLayer<TextModule> gridLayer) {
        hookDoubleClickCommand(nattable, gridLayer, null);
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.AbstractViewDataTable#setCategoryFilter(java.lang.String, com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType)
     */
    @Override
    public void setCategoryFilter(String filter, TreeObjectType treeObjectType) {
        treeFilteredIssues.setMatcher(new TextModuleMatcher(filter, treeObjectType, ((TreeObject) topicTreeViewer.getTree().getTopItem().getData()).getName()));
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.AbstractViewDataTable#isHeaderLabelEnabled()
     */
    @Override
    protected boolean isHeaderLabelEnabled() {
        return false;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.AbstractViewDataTable#getGridLayer()
     */
    @Override
    protected EntityGridListLayer<TextModule> getGridLayer() {
        return gridListLayer;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.AbstractViewDataTable#getEntityDAO()
     */
    @Override
    protected AbstractDAO<TextModule> getEntityDAO() {
        return textsDAO;
    }
    
    class TextModuleTableConfiguration extends AbstractRegistryConfiguration {

        @Override
        public void configureRegistry(IConfigRegistry configRegistry) {
            Style styleLeftAligned = new Style();
            styleLeftAligned.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.LEFT);
            Style styleRightAligned = new Style();
            styleRightAligned.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.RIGHT);
            Style styleCentered = new Style();
            styleCentered.setAttributeValue(CellStyleAttributes.HORIZONTAL_ALIGNMENT, HorizontalAlignmentEnum.CENTER);

            // default style for the most of the cells
            configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, // attribute to apply
                                                   styleLeftAligned,                // value of the attribute
                                                   DisplayMode.NORMAL,              // apply during normal rendering i.e not during selection or edit
                                                   GridRegion.BODY.toString());     // apply the above for all cells with this label
        }
    }
    
    protected Class<TextModule> getEntityClass() {
    	return TextModule.class;
    };
}
