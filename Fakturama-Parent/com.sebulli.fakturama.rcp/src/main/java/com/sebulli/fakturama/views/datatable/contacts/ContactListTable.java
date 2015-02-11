/**
 * 
 */
package com.sebulli.fakturama.views.datatable.contacts;

import java.io.Serializable;
import java.sql.SQLException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.di.extensions.EventTopic;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractRegistryConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.ConfigRegistry;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.data.ExtendedReflectiveColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IColumnPropertyAccessor;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowDataProvider;
import org.eclipse.nebula.widgets.nattable.data.IRowIdAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.RowSelectionModel;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.config.RowOnlySelectionConfiguration;
import org.eclipse.nebula.widgets.nattable.sort.config.SingleClickSortConfiguration;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.HorizontalAlignmentEnum;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.util.GUIHelper;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.GlazedLists;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swt.TextWidgetMatcherEditor;

import com.sebulli.fakturama.dao.ContactCategoriesDAO;
import com.sebulli.fakturama.dao.ContactsDAO;
import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.model.Contact;
import com.sebulli.fakturama.model.ContactCategory;
import com.sebulli.fakturama.parts.ContactEditor;
import com.sebulli.fakturama.views.datatable.AbstractViewDataTable;
import com.sebulli.fakturama.views.datatable.ListViewGridLayer;
import com.sebulli.fakturama.views.datatable.ListViewHeaderDataProvider;
import com.sebulli.fakturama.views.datatable.impl.NoHeaderRowOnlySelectionBindings;
import com.sebulli.fakturama.views.datatable.tree.model.TreeObject;
import com.sebulli.fakturama.views.datatable.tree.ui.TopicTreeViewer;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeCategoryLabelProvider;
import com.sebulli.fakturama.views.datatable.tree.ui.TreeObjectType;

/**
 * View with the table of all contacts
 * 
 */
public class ContactListTable extends AbstractViewDataTable<Contact, ContactCategory> {

    @Inject
    @Translation
    protected Messages msg;

    @Inject
    private Logger log;
    
    @Inject    
    private UISynchronize synch;

    // ID of this view
    public static final String ID = "fakturama.views.contactTable";

    private static final String POPUP_ID = "com.sebulli.fakturama.contactlist.popup";
     
    /**
     * Event Broker for receiving update events to the list table
     */
    @Inject
    protected IEventBroker evtBroker;

    @Inject
    @Preference
    private IEclipsePreferences preferences;

    private EventList<Contact> contactListData;
    private EventList<ContactCategory> categories;

    private Control top;
    
    @Inject
    private ContactsDAO contactDAO;
    
    @Inject
    private ContactCategoriesDAO contactCategoriesDAO;
    
    private ListViewGridLayer<Contact> gridLayer;
    //create a new ConfigRegistry which will be needed for GlazedLists handling
    private ConfigRegistry configRegistry = new ConfigRegistry();
    protected FilterList<Contact> treeFilteredIssues;
    private SelectionLayer selectionLayer;

    @PostConstruct
    public Control createPartControl(Composite parent) {
        log.info("create Contact list part");
        top = super.createPartControl(parent, Contact.class, false, true, ID);
        // Listen to double clicks
        hookDoubleClickCommand(natTable, gridLayer);
        topicTreeViewer.setTable(this);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        return top;
    }
    
    @Override
    protected void postConfigureNatTable(NatTable natTable) {
        //as the autoconfiguration of the NatTable is turned off, we have to add the 
        //DefaultNatTableStyleConfiguration and the ConfigRegistry manually 
        natTable.setConfigRegistry(configRegistry);
        natTable.addConfiguration(new NoHeaderRowOnlySelectionBindings());
        natTable.addConfiguration(new DefaultNatTableStyleConfiguration());
        natTable.addConfiguration(new ContactTableConfiguration());
//        addCustomStyling(natTable);
        // nur für das Headermenü, falls das mal irgendwann gebraucht werden sollte
        //      natTable.addConfiguration(new HeaderMenuConfiguration(n6));

        // Change the default sort key bindings. Note that 'auto configure' was turned off
        // for the SortHeaderLayer (setup in the GlazedListsGridLayer)
        natTable.addConfiguration(new SingleClickSortConfiguration());
        natTable.configure();
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#createListTable(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected NatTable createListTable(Composite searchAndTableComposite) {
        // fill the underlying data source (GlazedList)
        contactListData = GlazedLists.eventList(contactDAO.findAll());

        // get the visible properties to show in list view
        String[] propertyNames = contactDAO.getVisibleProperties();

        final IColumnPropertyAccessor<Contact> columnPropertyAccessor = new ExtendedReflectiveColumnPropertyAccessor<Contact>(propertyNames);
        final IColumnPropertyAccessor<Contact> derivedColumnPropertyAccessor = new IColumnPropertyAccessor<Contact>() {

            public Object getDataValue(Contact rowObject, int columnIndex) {
                ContactListDescriptor descriptor = ContactListDescriptor.getDescriptorFromColumn(columnIndex);
                switch (descriptor) {
                case NO:
                case FIRSTNAME:
                case LASTNAME:
                case COMPANY:
                case ZIP:
                case CITY:
                    return columnPropertyAccessor.getDataValue(rowObject, columnIndex);
                default:
                    break;
                }
                return null;
            }

            public void setDataValue(Contact rowObject, int columnIndex, Object newValue) {
                throw new UnsupportedOperationException("you can't change a value in list view!");
            }

            public int getColumnCount() {
                return columnPropertyAccessor.getColumnCount();
            }

            public String getColumnProperty(int columnIndex) {
                ContactListDescriptor descriptor = ContactListDescriptor.getDescriptorFromColumn(columnIndex);
                return msg.getMessageFromKey(descriptor.getMessageKey());
            }

            public int getColumnIndex(String propertyName) {
                    return columnPropertyAccessor.getColumnIndex(propertyName);
            }
        };

        //build the column header layer
        // Column header data provider includes derived properties
        IDataProvider columnHeaderDataProvider = new ListViewHeaderDataProvider<Contact>(propertyNames, derivedColumnPropertyAccessor); 

        // matcher input Search text field 
        final MatcherEditor<Contact> textMatcherEditor = new TextWidgetMatcherEditor<Contact>(searchText, new ContactFilterator());
        
        // Filtered list for Search text field filter
        final FilterList<Contact> textFilteredIssues = new FilterList<Contact>(contactListData, textMatcherEditor);

        // build the list for the tree-filtered values (i.e., the value list which is affected by
        // tree selection)
        treeFilteredIssues = new FilterList<Contact>(textFilteredIssues);

        //create the body layer stack
        final IRowDataProvider<Contact> firstBodyDataProvider = 
                new GlazedListsDataProvider<Contact>(treeFilteredIssues, columnPropertyAccessor);
        
        //build the grid layer
        gridLayer = new ListViewGridLayer<Contact>(treeFilteredIssues, derivedColumnPropertyAccessor, columnHeaderDataProvider, configRegistry, true);
        DataLayer tableDataLayer = gridLayer.getBodyDataLayer();
        tableDataLayer.setColumnPercentageSizing(true);
        tableDataLayer.setColumnWidthPercentageByPosition(0, 5);
        tableDataLayer.setColumnWidthPercentageByPosition(1, 15);
        tableDataLayer.setColumnWidthPercentageByPosition(2, 75);
        tableDataLayer.setColumnWidthPercentageByPosition(3, 5);
//        GlazedListsEventLayer<Contact> vatListEventLayer = new GlazedListsEventLayer<Contact>(tableDataLayer, contactListData);

        // add a label accumulator to be able to register converter
        // this is crucial for using custom values display
//        vatListEventLayer.setConfigLabelAccumulator(new ColumnLabelAccumulator());
        
        // Custom selection configuration
        selectionLayer = gridLayer.getBodyLayerStack().getSelectionLayer();

        IRowIdAccessor<Contact> rowIdAccessor = new IRowIdAccessor<Contact>() {
            @Override
            public Serializable getRowId(Contact rowObject) {
                return rowObject.getId();
            }
        };
        
        //use a RowSelectionModel that will perform row selections and is able to identify a row via unique ID
        RowSelectionModel<Contact> selectionModel = new RowSelectionModel<Contact>(selectionLayer, firstBodyDataProvider, rowIdAccessor, false);
        selectionLayer.setSelectionModel(selectionModel);
//         Select complete rows
        selectionLayer.addConfiguration(new RowOnlySelectionConfiguration<Contact>());

        final NatTable natTable = new NatTable(searchAndTableComposite/*, 
                SWT.NO_REDRAW_RESIZE | SWT.DOUBLE_BUFFERED | SWT.BORDER*/, gridLayer, false);
        natTable.setBackground(GUIHelper.COLOR_WHITE);
        GridDataFactory.fillDefaults().grab(true, true).applyTo(natTable);
        natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));

        // Register your custom cell painter, cell style, against the label applied to the cell.
        //      addImageTextToColumn(configRegistry, natTable, gridLayer.getBodyDataProvider());
        return natTable;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#getTableId()
     */
    @Override
    public String getTableId() {
        return ID;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#getEditorId()
     */
    @Override
    protected String getEditorId() {
        return ContactEditor.ID;
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#createCategoryTreeViewer(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected TopicTreeViewer<ContactCategory> createCategoryTreeViewer(Composite top) {
        topicTreeViewer = new TopicTreeViewer<ContactCategory>(top, msg, false, true);
        categories = GlazedLists.eventList(contactCategoriesDAO.findAll());
        topicTreeViewer.setInput(categories);
        topicTreeViewer.setLabelProvider(new TreeCategoryLabelProvider());
        return topicTreeViewer;
    }
    
    @Inject @Optional
    public void handleRefreshEvent(@EventTopic("ContactEditor") String message) {
        synch.syncExec(new Runnable() {
            
            @Override
            public void run() {
                top.setRedraw(false);
            }
        });
        // As the eventlist has a GlazedListsEventLayer this layer reacts on the change
        contactListData.clear();
        contactListData.addAll(contactDAO.findAll());
        categories.clear();
        categories.addAll(contactCategoriesDAO.findAll());
        synch.syncExec(new Runnable() {
           
            @Override
            public void run() {
                top.setRedraw(true);
            }});
    }

    public void removeSelectedEntry() {
        if(selectionLayer.getFullySelectedRowPositions().length > 0) {
            Contact objToDelete = gridLayer.getBodyDataProvider().getRowObject(selectionLayer.getFullySelectedRowPositions()[0]);
            try {
                // don't delete the entry because it could be referenced
                // from another entity
                objToDelete.setDeleted(Boolean.TRUE);
                contactDAO.save(objToDelete);
            }
            catch (SQLException e) {
                log.error(e, "can't save the current Contact: " + objToDelete.toString());
            }
    
            // Refresh the table view of all VATs
            evtBroker.post("ContactEditor", "update");
        } else {
            log.debug("no rows selected!");
        }
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#setCategoryFilter(java.lang.String, com.sebulli.fakturama.views.datatable.vats.TreeObjectType)
     */
    @Override
    public void setCategoryFilter(String filter, TreeObjectType treeObjectType) {
        treeFilteredIssues.setMatcher(new ContactMatcher(filter, treeObjectType,((TreeObject)topicTreeViewer.getTree().getTopItem().getData()).getName()));
    }

    /* (non-Javadoc)
     * @see com.sebulli.fakturama.views.datatable.vats.AbstractViewDataTable#isHeaderLabelEnabled()
     */
    @Override
    protected boolean isHeaderLabelEnabled() {
        return false;
    }

    /**
     * @return the gridLayer
     */
    public ListViewGridLayer<Contact> getGridLayer() {
        return gridLayer;
    }
    
    class ContactTableConfiguration extends AbstractRegistryConfiguration {

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

    protected String getPopupId() {
        return POPUP_ID;
    }

}
