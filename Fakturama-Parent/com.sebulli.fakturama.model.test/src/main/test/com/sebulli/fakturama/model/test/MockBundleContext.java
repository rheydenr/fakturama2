/* 
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2019 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     The Fakturama Team - initial API and implementation
 */
 
package com.sebulli.fakturama.model.test;

import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Dictionary;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkListener;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceObjects;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;

/**
 * @author rheydenreich
 *
 */
public class MockBundleContext implements BundleContext {

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#getProperty(java.lang.String)
	 */
	@Override
	public String getProperty(String key) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#getBundle()
	 */
	@Override
	public Bundle getBundle() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#installBundle(java.lang.String, java.io.InputStream)
	 */
	@Override
	public Bundle installBundle(String location, InputStream input) throws BundleException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#installBundle(java.lang.String)
	 */
	@Override
	public Bundle installBundle(String location) throws BundleException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#getBundle(long)
	 */
	@Override
	public Bundle getBundle(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#getBundles()
	 */
	@Override
	public Bundle[] getBundles() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#addServiceListener(org.osgi.framework.ServiceListener, java.lang.String)
	 */
	@Override
	public void addServiceListener(ServiceListener listener, String filter) throws InvalidSyntaxException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#addServiceListener(org.osgi.framework.ServiceListener)
	 */
	@Override
	public void addServiceListener(ServiceListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#removeServiceListener(org.osgi.framework.ServiceListener)
	 */
	@Override
	public void removeServiceListener(ServiceListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#addBundleListener(org.osgi.framework.BundleListener)
	 */
	@Override
	public void addBundleListener(BundleListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#removeBundleListener(org.osgi.framework.BundleListener)
	 */
	@Override
	public void removeBundleListener(BundleListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#addFrameworkListener(org.osgi.framework.FrameworkListener)
	 */
	@Override
	public void addFrameworkListener(FrameworkListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#removeFrameworkListener(org.osgi.framework.FrameworkListener)
	 */
	@Override
	public void removeFrameworkListener(FrameworkListener listener) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#registerService(java.lang.String[], java.lang.Object, java.util.Dictionary)
	 */
	@Override
	public ServiceRegistration<?> registerService(String[] clazzes, Object service, Dictionary<String, ?> properties) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#registerService(java.lang.String, java.lang.Object, java.util.Dictionary)
	 */
	@Override
	public ServiceRegistration<?> registerService(String clazz, Object service, Dictionary<String, ?> properties) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#registerService(java.lang.Class, java.lang.Object, java.util.Dictionary)
	 */
	@Override
	public <S> ServiceRegistration<S> registerService(Class<S> clazz, S service, Dictionary<String, ?> properties) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#registerService(java.lang.Class, org.osgi.framework.ServiceFactory, java.util.Dictionary)
	 */
	@Override
	public <S> ServiceRegistration<S> registerService(Class<S> clazz, ServiceFactory<S> factory,
			Dictionary<String, ?> properties) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#getServiceReferences(java.lang.String, java.lang.String)
	 */
	@Override
	public ServiceReference<?>[] getServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#getAllServiceReferences(java.lang.String, java.lang.String)
	 */
	@Override
	public ServiceReference<?>[] getAllServiceReferences(String clazz, String filter) throws InvalidSyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#getServiceReference(java.lang.String)
	 */
	@Override
	public ServiceReference<?> getServiceReference(String clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#getServiceReference(java.lang.Class)
	 */
	@Override
	public <S> ServiceReference<S> getServiceReference(Class<S> clazz) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#getServiceReferences(java.lang.Class, java.lang.String)
	 */
	@Override
	public <S> Collection<ServiceReference<S>> getServiceReferences(Class<S> clazz, String filter)
			throws InvalidSyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#getService(org.osgi.framework.ServiceReference)
	 */
	@Override
	public <S> S getService(ServiceReference<S> reference) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#ungetService(org.osgi.framework.ServiceReference)
	 */
	@Override
	public boolean ungetService(ServiceReference<?> reference) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#getServiceObjects(org.osgi.framework.ServiceReference)
	 */
	@Override
	public <S> ServiceObjects<S> getServiceObjects(ServiceReference<S> reference) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#getDataFile(java.lang.String)
	 */
	@Override
	public File getDataFile(String filename) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#createFilter(java.lang.String)
	 */
	@Override
	public Filter createFilter(String filter) throws InvalidSyntaxException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleContext#getBundle(java.lang.String)
	 */
	@Override
	public Bundle getBundle(String location) {
		// TODO Auto-generated method stub
		return null;
	}

}
