/**
 * 
 */
package com.sebulli.fakturama.money;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.Optional;

import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;
import javax.money.MonetaryRounding;
import javax.money.RoundingContext;
import javax.money.RoundingContextBuilder;

/**
 * @author G527032
 *
 */
public class FakturamaRounding implements MonetaryRounding {

    /**
     * The scale key to be used.
     */
    private static final String SCALE_KEY = "scale";

    /**
     * The provider class key to be used.
     */
    private static final String PROVCLASS_KEY = "providerClass";

    /**
     * The {@link RoundingMode} used.
     */
    private final RoundingContext context;

    /**
     * Creates an rounding instance.
     *
     * @param roundingMode The {@link java.math.RoundingMode} to be used, not {@code null}.
     */
    FakturamaRounding(int scale, RoundingMode roundingMode) {
        Objects.requireNonNull(roundingMode, "RoundingMode required.");
        if (scale < 0) {
            scale = 0;
        }
        this.context = RoundingContextBuilder.of("default", "default").
                set(PROVCLASS_KEY, getClass().getName()).set(SCALE_KEY, scale).set(Optional.ofNullable(roundingMode)
                .orElseThrow(
                        () -> new
                                IllegalArgumentException(
                                "roundingMode missing")))
                .build();
    }

    /**
     * Creates an {@link DefaultRounding} for rounding {@link MonetaryAmount}
     * instances given a currency.
     *
     * @param currency The currency, which determines the required precision. As
     *                 {@link RoundingMode}, by default, {@link RoundingMode#HALF_UP}
     *                 is used.
     */
    FakturamaRounding(CurrencyUnit currency, RoundingMode roundingMode) {
        this(currency.getDefaultFractionDigits(), roundingMode);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.money.MonetaryFunction#apply(java.lang.Object)
     */
    @Override
    public MonetaryAmount apply(MonetaryAmount amount) {
        return amount.getFactory().setCurrency(amount.getCurrency()).setNumber(
                amount.getNumber().numberValue(BigDecimal.class)
                        .setScale(this.context.getInt(SCALE_KEY), this.context.get(RoundingMode.class))).create();
    }

    @Override
    public RoundingContext getRoundingContext() {
        return context;
    }
}
