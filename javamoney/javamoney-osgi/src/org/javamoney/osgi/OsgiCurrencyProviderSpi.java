package org.javamoney.osgi;

import java.util.Currency;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.money.CurrencyQuery;
import javax.money.CurrencyUnit;
import javax.money.spi.CurrencyProviderSpi;

public class OsgiCurrencyProviderSpi implements CurrencyProviderSpi {

    /** Internal shared cache of {@link javax.money.CurrencyUnit} instances. */
    private static final Map<String, CurrencyUnit> CACHED = new HashMap<>();

    public OsgiCurrencyProviderSpi() {
        for (Currency jdkCurrency : Currency.getAvailableCurrencies()) {
            CurrencyUnit cu = new JDKCurrencyAdapter(jdkCurrency);
            CACHED.put(cu.getCurrencyCode(), cu);
        }
    }

    @Override
    public String getProviderName(){
        return "osgi-default";
    }

    /**
     * Return a {@link CurrencyUnit} instances matching the given
     * {@link javax.money.CurrencyContext}.
     *
     * @param currencyQuery the {@link javax.money.CurrencyContext} containing the parameters determining the query. not null.
     * @return the corresponding {@link CurrencyUnit}, or null, if no such unit
     * is provided by this provider.
     */
    public Set<CurrencyUnit> getCurrencies(CurrencyQuery currencyQuery){
        Set<CurrencyUnit> result = new HashSet<>();
        if(!currencyQuery.getCurrencyCodes().isEmpty()) {
            for (String code : currencyQuery.getCurrencyCodes()) {
                CurrencyUnit cu = CACHED.get(code);
                if (cu != null) {
                    result.add(cu);
                }
            }
            return result;
        }
        if(!currencyQuery.getCountries().isEmpty()) {
            for (Locale country : currencyQuery.getCountries()) {
                CurrencyUnit cu = getCurrencyUnit(country);
                if (cu != null) {
                    result.add(cu);
                }
            }
            return result;
        }
        result.addAll(CACHED.values());
        return result;
    }

    private CurrencyUnit getCurrencyUnit(Locale locale) {
        Currency cur;
        try {
            cur = Currency.getInstance(locale);
            if (Objects.nonNull(cur)) {
                return CACHED.get(cur.getCurrencyCode());
            }
        } catch (Exception e) {
            if (Logger.getLogger(getClass().getName()).isLoggable(Level.FINEST)) {
                Logger.getLogger(getClass().getName()).finest(
                        "No currency for locale found: " + locale);
            }
        }
        return null;
    }

}
