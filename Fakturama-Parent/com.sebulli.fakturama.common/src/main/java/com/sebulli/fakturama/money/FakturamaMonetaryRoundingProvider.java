/**
 * 
 */
package com.sebulli.fakturama.money;

import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.money.CurrencyUnit;
import javax.money.MonetaryRounding;
import javax.money.Monetary;
import javax.money.RoundingQuery;
import javax.money.spi.RoundingProviderSpi;

import org.apache.commons.lang3.BooleanUtils;

/**
 *
 */
public class FakturamaMonetaryRoundingProvider implements RoundingProviderSpi {

    public static final String DEFAULT_ROUNDING_ID = "fakturama-rounding-provider";
    private Set<String> roundingsIds = new HashSet<>();

    public FakturamaMonetaryRoundingProvider() {
        roundingsIds.add(DEFAULT_ROUNDING_ID);
        roundingsIds = Collections.unmodifiableSet(roundingsIds);
    }

    @Override
    public String getProviderName() {
        return DEFAULT_ROUNDING_ID;
    }

    /**
     * Evaluate the rounding that match the given query.
     *
     * @return the (shared) default rounding instances matching, never null.
     */
    public MonetaryRounding getRounding(RoundingQuery roundingQuery) {
        RoundingMode roundingMode;
//        if (roundingQuery.getTimestamp() != null) {
//            return null;
//        }
        CurrencyUnit currency = roundingQuery.getCurrency();
        if (currency != null) {
            if (BooleanUtils.isTrue(roundingQuery.getBoolean("cashRounding"))) {
                if (currency.getCurrencyCode().equals("CHF")) {
                    return new FakturamaCashRounding(currency, RoundingMode.HALF_UP, 5);
                } else {
                    return new FakturamaCashRounding(currency, 1);
                }
            }
//           return new FakturamaRounding(currency, roundingMode);
        }
        Integer scale = roundingQuery.getScale();
        if (scale == null) {
            scale = 2;
        }
        MathContext mc = roundingQuery.get(MathContext.class/*, null*/);
        roundingMode = java.util.Optional.ofNullable(roundingQuery.get(RoundingMode.class)).orElse(RoundingMode.HALF_UP);
        if (roundingMode != null || mc != null) {
            if (mc != null) {
                return new FakturamaRounding(scale, mc.getRoundingMode());
            }
            if (roundingMode == null) {
                roundingMode = RoundingMode.HALF_EVEN;
            }
            return new FakturamaRounding(scale, roundingMode);
        }
        if (roundingQuery.getRoundingName() != null && DEFAULT_ROUNDING_ID.equals(roundingQuery.getRoundingName())) {
            return Monetary.getDefaultRounding();
        }
        return null;
    }


    @Override
    public Set<String> getRoundingNames() {
        return roundingsIds;
    }

}
