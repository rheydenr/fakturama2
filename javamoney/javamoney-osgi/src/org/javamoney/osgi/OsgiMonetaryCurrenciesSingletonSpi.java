package org.javamoney.osgi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.money.CurrencyQuery;
import javax.money.CurrencyUnit;
import javax.money.spi.Bootstrap;
import javax.money.spi.CurrencyProviderSpi;
import javax.money.spi.MonetaryCurrenciesSingletonSpi;

public final class OsgiMonetaryCurrenciesSingletonSpi implements MonetaryCurrenciesSingletonSpi {
	
//	private CurrencyProviderSpi currencyProvider;
//	
//	public void setCurrencyProvider(CurrencyProviderSpi currencyProvider) {
//		this.currencyProvider = currencyProvider;
//	}
//	
//	public void unsetCurrencyProvider(CurrencyProviderSpi currencyProvider) {
//		this.currencyProvider = null;
//	}
//
//	public void activate() {
//		
//	}
//	
//	public void deactivate() {
//		
//	}

    @Override
    public Set<CurrencyUnit> getCurrencies(CurrencyQuery query) {
        Set<CurrencyUnit> result = new HashSet<>();
        for (CurrencyProviderSpi spi : Bootstrap.getServices(CurrencyProviderSpi.class)) {
            try {
                result.addAll(spi.getCurrencies(query));
            } catch (Exception e) {
                Logger.getLogger(OsgiMonetaryCurrenciesSingletonSpi.class.getName())
                        .log(Level.SEVERE, "Error loading currency provider names for " + spi.getClass().getName(),
                                e);
            }
        }
        return result;
    }

    /**
     * This default implementation simply returns all providers defined in arbitrary order.
     *
     * @return the default provider chain, never null.
     */
    @Override
    public List<String> getDefaultProviderChain() {
        List<String> list = new ArrayList<>();
        list.addAll(getProviderNames());
        Collections.sort(list);
        return list;
    }

    /**
     * Get the names of the currently loaded providers.
     *
     * @return the names of the currently loaded providers, never null.
     */
    @Override
    public Set<String> getProviderNames() {
        Set<String> result = new HashSet<>();
        for (CurrencyProviderSpi spi : Bootstrap.getServices(CurrencyProviderSpi.class)) {
            try {
                result.add(spi.getProviderName());
            } catch (Exception e) {
                Logger.getLogger(OsgiMonetaryCurrenciesSingletonSpi.class.getName())
                        .log(Level.SEVERE, "Error loading currency provider names for " + spi.getClass().getName(),
                                e);
            }
        }
        return result;
    }
}