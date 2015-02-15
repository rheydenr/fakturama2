/**
 * 
 */
package com.sebulli.fakturama.money.internal;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import javax.money.format.AmountFormatContextBuilder;
import javax.money.format.AmountFormatQuery;
import javax.money.format.MonetaryAmountFormat;
import javax.money.spi.MonetaryAmountFormatProviderSpi;

/**
 *
 */
public class FakturamaFormatProviderSpi implements MonetaryAmountFormatProviderSpi {

    public static final String DEFAULT_STYLE = "fakturama-money-format";
    private static final String PROVIDER_NAME = "fakturama-money-formatter";

    private Set<Locale> supportedSets = new HashSet<>();
    private Set<String> formatNames = new HashSet<>();

    public FakturamaFormatProviderSpi(){
        supportedSets.addAll(Arrays.asList(DecimalFormat.getAvailableLocales()));
        supportedSets = Collections.unmodifiableSet(supportedSets);
        formatNames.add(DEFAULT_STYLE);
        formatNames = Collections.unmodifiableSet(formatNames);
    }

    @Override
    public String getProviderName(){
        return PROVIDER_NAME;
    }

    /*
         * (non-Javadoc)
         * @see
         * javax.money.spi.MonetaryAmountFormatProviderSpi#getFormat(javax.money.format.AmountFormatContext)
         */
    @Override
    public Collection<MonetaryAmountFormat> getAmountFormats(AmountFormatQuery amountFormatQuery){
        Objects.requireNonNull(amountFormatQuery, "AmountFormatContext required");
        if(!amountFormatQuery.getProviderNames().isEmpty() &&
                !amountFormatQuery.getProviderNames().contains(getProviderName())){
            return Collections.emptySet();
        }
        if(!(amountFormatQuery.getFormatName() == null || DEFAULT_STYLE.equals(amountFormatQuery.getFormatName()))){
            return Collections.emptySet();
        }
        AmountFormatContextBuilder builder = AmountFormatContextBuilder.of(DEFAULT_STYLE);
        if(amountFormatQuery.getLocale() != null){
            builder.setLocale(amountFormatQuery.getLocale());
        }
        builder.importContext(amountFormatQuery, false);
        builder.setMonetaryAmountFactory(amountFormatQuery.getMonetaryAmountFactory());
        return Arrays.asList(new MonetaryAmountFormat[]{new FakturamaMonetaryAmountFormat(builder.build())});
    }

    @Override
    public Set<Locale> getAvailableLocales(){
        return supportedSets;
    }

    @Override
    public Set<String> getAvailableFormatNames(){
        return formatNames;
    }

}
