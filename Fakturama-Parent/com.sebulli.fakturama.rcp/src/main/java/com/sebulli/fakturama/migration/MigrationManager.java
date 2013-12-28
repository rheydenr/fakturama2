/*
 * Fakturama - Free Invoicing Software - http://fakturama.sebulli.com
 * 
 * Copyright (C) 2013 Ralf Heydenreich
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Ralf Heydenreich - initial API and implementation
 */
package com.sebulli.fakturama.migration;

import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.core.services.log.Logger;
import org.eclipse.e4.ui.services.IServiceConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.BackingStoreException;

import com.sebulli.fakturama.dao.ContactDAO;
import com.sebulli.fakturama.dao.CountryCodesDAO;
import com.sebulli.fakturama.dao.PropertiesDAO;
import com.sebulli.fakturama.dao.ShippingsDAO;
import com.sebulli.fakturama.dao.TextsDAO;
import com.sebulli.fakturama.dao.VatsDAO;
import com.sebulli.fakturama.dbconnector.OldTableinfo;
import com.sebulli.fakturama.model.CountryCode;
import com.sebulli.fakturama.model.Shipping;
import com.sebulli.fakturama.model.ShippingCategory;
import com.sebulli.fakturama.model.TextCategory;
import com.sebulli.fakturama.model.TextModule;
import com.sebulli.fakturama.model.UserProperty;
import com.sebulli.fakturama.model.VAT;
import com.sebulli.fakturama.model.VATCategory;
import com.sebulli.fakturama.olddao.OldEntitiesDAO;
import com.sebulli.fakturama.oldmodel.OldList;
import com.sebulli.fakturama.oldmodel.OldProperties;
import com.sebulli.fakturama.oldmodel.OldShippings;
import com.sebulli.fakturama.oldmodel.OldTexts;
import com.sebulli.fakturama.oldmodel.OldVats;
import com.sebulli.fakturama.startup.ConfigurationManager;

/**
 * Migration Tool for converting old data to new one. This affects only database
 * data, not templates or other plain files.
 * 
 * @author R. Heydenreich
 * 
 */
public class MigrationManager {
	@Inject
	private IEclipseContext context;

	@Inject
	private Logger log;

	@Inject
	@Preference
	private IEclipsePreferences preferences;

	/*
	 * all available DAO classes
	 */
	private ContactDAO contactDAO;
	private PropertiesDAO propertiesDAO;
	private ShippingsDAO shippingsDAO;
	private TextsDAO textDAO;
	private VatsDAO vatsDAO;
	private CountryCodesDAO countryCodesDAO;
	
	/*
	 * there's only one DAO for old data
	 */
	private OldEntitiesDAO oldDao;

	/**
	 * entry point for migration of old data (db only)
	 * 
	 * @param parent the current {@link Shell}
	 * @throws BackingStoreException
	 */
	@Execute
	public void migrateOldData(@Named(IServiceConstants.ACTIVE_SHELL) Shell parent) throws BackingStoreException {
		// build jdbc connection string; can't be null at this point since we've checked it above (within the caller)
		String oldWorkDir = preferences.get(ConfigurationManager.MIGRATE_OLD_DATA, null);
		// hard coded old database name
		String hsqlConnectionString = "jdbc:hsqldb:file:" + oldWorkDir + "/Database/Database";
		preferences.put("OLD_JDBC_URL", hsqlConnectionString);
		preferences.flush();

		// initialize DAOs via EclipseContext
		// new Entities have their own dao ;-)
		//		contactDAO = ContextInjectionFactory.make(ContactDAO.class, context);
		propertiesDAO = ContextInjectionFactory.make(PropertiesDAO.class, context);
		shippingsDAO = ContextInjectionFactory.make(ShippingsDAO.class, context);
		vatsDAO = ContextInjectionFactory.make(VatsDAO.class, context);
		textDAO = ContextInjectionFactory.make(TextsDAO.class, context);
		countryCodesDAO = ContextInjectionFactory.make(CountryCodesDAO.class, context);

		// old entities only have one DAO for all entities
		oldDao = ContextInjectionFactory.make(OldEntitiesDAO.class, context);

		// now start a ProgressMonitorDialog for tracing the progress of migration
		ProgressMonitorDialog progressMonitorDialog = new ProgressMonitorDialog(parent);
		try {
			IRunnableWithProgress op = new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					// we have to keep a certain order
					OldTableinfo orderedTasks[] = new OldTableinfo[]{
							OldTableinfo.Properties,
							OldTableinfo.Vats,
							OldTableinfo.Shippings,
							OldTableinfo.Texts,
//							OldTableinfo.List,
							OldTableinfo.Payments,
							OldTableinfo.Expenditures,
							OldTableinfo.Receiptvouchers,
							OldTableinfo.Contact,
							OldTableinfo.Documents
					};
					//  orderedTasks[] now contains all tables which have to be converted
					monitor.beginTask("Main task running ...", orderedTasks.length);
					for (OldTableinfo tableinfo : orderedTasks) {
						monitor.subTask("converting " + tableinfo.name());
						checkCancel(monitor);
						runMigration(new SubProgressMonitor(monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK), tableinfo);
//						monitor.worked(1);
					}
					monitor.done();
				}
			};
			;
			progressMonitorDialog.run(true, true, op);
		}
		catch (InvocationTargetException e) {
			log.error("Fehler: ", e);
		}
		catch (InterruptedException e) {
			// handle cancellation
			throw new OperationCanceledException();
		}
		finally {
			log.info("fäddich!");
		}
	}
	

	/**
	 * This method switches to the certain migration methods.
	 * 
	 * @param subProgressMonitor {@link SubProgressMonitor}
	 * @param tableinfo tableinfo which table has to be migrated
	 * @throws InterruptedException
	 */
	private void runMigration(SubProgressMonitor subProgressMonitor, OldTableinfo tableinfo) throws InterruptedException {
		switch (tableinfo) {
		case Properties:
			runMigratePropertiesSubTask(subProgressMonitor);
			break;
		case Shippings:
			runMigrateShippingsSubTask(subProgressMonitor);
			break;
		case Vats:
			runMigrateVatsSubTask(subProgressMonitor);
			break;
//		case List:
			// as Country Codes are no user definable data they will be read at a later stage
			// from ISO-3166-CountryCodes.txt.
//			runMigrateCountryCodeListsSubTask(subProgressMonitor);
//			break;
		case Texts:
			runMigrateTextsSubTask(subProgressMonitor);
			break;
		case Expenditures:
			break;
		case Contact:
			break;
		case Payments:
//			runMigratePaymentsSubTask(subProgressMonitor);
			break;
		case Products:
			break;
		case Receiptvouchers:
			break;
		default:
			break;
		}
	}


	/**
	 * Migration of the Texts table (DataSetText)
	 * 
	 * @param subProgressMonitor the {@link SubProgressMonitor}
	 */
	private void runMigrateTextsSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllTexts();
		subProgressMonitor.beginTask("Bearbeite insgesamt", countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(" " + countOfEntitiesInTable + " Sätze");
		// use a HashMap as a simple cache
		Map<String, TextCategory> textCategoriesMap = buildTextCategoryMap();
		for (OldTexts oldTexts : oldDao.findAllTexts()) {
			try {
				TextModule text = new TextModule();
				text.setName(oldTexts.getName());
				text.setText(oldTexts.getText());
				if(StringUtils.isNotBlank(oldTexts.getCategory()) && textCategoriesMap.containsKey(oldTexts.getCategory())) {
					// add it to the new entity
					text.addToCategories(textCategoriesMap.get(oldTexts.getCategory()));
				}
				textDAO.save(text);
				subProgressMonitor.worked(1);
			}
			catch (SQLException e) {
				log.error("error while migrating VAT. (old) ID=" + oldTexts.getId());
			}
		}
		subProgressMonitor.done();
	}


	/**
	 * Migration of the Vats table (DataSetVAT)
	 * 
	 * @param subProgressMonitor the {@link SubProgressMonitor}
	 */
	private void runMigrateVatsSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllVats();
		subProgressMonitor.beginTask("Bearbeite insgesamt", countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(" " + countOfEntitiesInTable + " Sätze");
		// use a HashMap as a simple cache
		
		Map<String, VATCategory> vatCategoriesMap = buildVATCategoryMap();
		for (OldVats oldVats : oldDao.findAllVats()) {
			try {
				VAT vat = new VAT();
				vat.setName(oldVats.getName());
				vat.setValue(oldVats.getValue());
				vat.setDescription(oldVats.getDescription());
				if(StringUtils.isNotBlank(oldVats.getCategory()) && vatCategoriesMap.containsKey(oldVats.getCategory())) {
					// add it to the new entity
					vat.addToCategories(vatCategoriesMap.get(oldVats.getCategory()));
				}
				vatsDAO.save(vat);
				subProgressMonitor.worked(1);
			}
			catch (SQLException e) {
				log.error("error while migrating VAT. (old) ID=" + oldVats.getId());
			}
		}
		subProgressMonitor.done();
	}

	/**
	 * Migration of the Shipping table (DataSetShipping)
	 * 
	 * @param subProgressMonitor the {@link SubProgressMonitor}
	 */
	private void runMigrateShippingsSubTask(SubProgressMonitor subProgressMonitor) {
		Long countOfEntitiesInTable = oldDao.countAllShippings();
		subProgressMonitor.beginTask("Bearbeite insgesamt", countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask(" " + countOfEntitiesInTable + " Sätze");
		// use a HashMap as a simple cache
		Map<String, ShippingCategory> shippingCategoriesMap = buildShippingCategoryMap();
		for (OldShippings oldShippings : oldDao.findAllShippings()) {
			try {
				Shipping shipping = new Shipping();
				shipping.setName(oldShippings.getName());
				shipping.setValue(oldShippings.getValue());
				shipping.setAutoVat(BooleanUtils.toBooleanObject(oldShippings.getAutovat()));
				shipping.setDescription(oldShippings.getDescription());
				if(StringUtils.isNotBlank(oldShippings.getCategory()) && shippingCategoriesMap.containsKey(oldShippings.getCategory())) {
					// add it to the new entity
					shipping.addToCategories(shippingCategoriesMap.get(oldShippings.getCategory()));
				}
				shippingsDAO.save(shipping);
				subProgressMonitor.worked(1);
			}
			catch (SQLException e) {
				log.error("error while migrating Shipping. (old) ID=" + oldShippings.getId());
			}
		}
		subProgressMonitor.done();
	}


	/**
	 * @param categoriesFromEntity TODO
	 * @return
	 */
	private Map<String, TextCategory> buildTextCategoryMap() {
		Map<String, TextCategory> textCategoriesMap = new HashMap<String, TextCategory>();
		for (String oldTextCategories : oldDao.findAllTextCategories()) {
			TextCategory textCategory = new TextCategory();
			textCategory.setName(oldTextCategories);
			textCategoriesMap.put(oldTextCategories, textCategory);
		}
		return textCategoriesMap;
	}
	
	/**
	 * @param categoriesFromEntity TODO
	 * @return
	 */
	private Map<String, ShippingCategory> buildShippingCategoryMap() {
		Map<String, ShippingCategory> shippingCategoriesMap = new HashMap<String, ShippingCategory>();
		for (String oldShippingCategories : oldDao.findAllShippingCategories()) {
			ShippingCategory shippingCategory = new ShippingCategory();
			shippingCategory.setName(oldShippingCategories);
			shippingCategoriesMap.put(oldShippingCategories, shippingCategory);
		}
		return shippingCategoriesMap;
	}
	
	/**
	 * @param categoriesFromEntity TODO
	 * @return
	 */
	private Map<String, VATCategory> buildVATCategoryMap() {
		Map<String, VATCategory> vatCategoriesMap = new HashMap<String, VATCategory>();
		for (String oldVatCategories : oldDao.findAllVatCategories()) {
			VATCategory vatCategory = new VATCategory();
			vatCategory.setName(oldVatCategories);
			vatCategoriesMap.put(oldVatCategories, vatCategory);
		}
		return vatCategoriesMap;
	}


	/**
	 * Runs the subtask which converts a single table.
	 * 
	 * @param subProgressMonitor
	 * @param countOfEntitiesInTable
	 * @throws InterruptedException
	 * @throws
	 */
	private void runMigratePropertiesSubTask(SubProgressMonitor subProgressMonitor) throws InterruptedException {
		Long countOfEntitiesInTable = oldDao.countAllProperties();
		subProgressMonitor.beginTask("Converting Properties", countOfEntitiesInTable.intValue());
		subProgressMonitor.subTask("(" + countOfEntitiesInTable + " records total)");
		for (OldProperties oldProperties : oldDao.findAllProperties()) {
			try {
				UserProperty prop = new UserProperty();
				prop.setName(oldProperties.getName());
				prop.setValue(oldProperties.getValue());
				propertiesDAO.save(prop);
				subProgressMonitor.worked(1);
			}
			catch (SQLException e) {
				log.error("error while migrating UserProperty. (old) ID=" + oldProperties.getId());
			}
		}
		subProgressMonitor.done();
	}

	/**
	 * Checks for cancellation of the long running migration job
	 * 
	 * @param monitor
	 * @throws InterruptedException
	 */
	private void checkCancel(IProgressMonitor monitor) throws InterruptedException {
		if (monitor.isCanceled()) { throw new InterruptedException(); }
	}

}
