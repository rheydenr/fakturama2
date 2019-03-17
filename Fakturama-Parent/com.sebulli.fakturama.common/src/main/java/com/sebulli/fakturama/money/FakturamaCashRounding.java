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
import javax.money.MonetaryOperator;
import javax.money.MonetaryRounding;
import javax.money.RoundingContext;
import javax.money.RoundingContextBuilder;

/**
 * @author G527032
 *
 */
public class FakturamaCashRounding implements MonetaryRounding {

    /**
     * The scale key to be used.
     */
    private static final String SCALE_KEY = "scale";
    /**
     * The minimal minors key to be used.
     */
    private static final String MINMINORS_KEY = "minimalMinors";

    /**
     * The provider class key to be used.
     */
    private static final String PROVCLASS_KEY = "providerClass";

    /**
     * The cash rounding flag key to be used.
     */
    private static final String CASHROUNDING_KEY = "cashRounding";

    private RoundingContext context;


    /**
     * Creates an rounding instance.
     *
     * @param roundingMode The {@link RoundingMode} to be used, not {@code null}.
     * @param scale        The target scale.
     */
    FakturamaCashRounding(int scale, RoundingMode roundingMode, int minimalMinors) {
        if (scale < 0) {
            throw new IllegalArgumentException("scale < 0");
        }
        this.context = RoundingContextBuilder.of("default", "default").set(CASHROUNDING_KEY, true).
                set(PROVCLASS_KEY, getClass().getName()).set(MINMINORS_KEY, minimalMinors).set(SCALE_KEY, scale)
                .set(Optional.ofNullable(roundingMode)
                        .orElseThrow(() -> new IllegalArgumentException("roundingMode missing"))).build();
    }

    /**
     * Creates an {@link DefaultCashRounding} for rounding
     * {@link MonetaryAmount} instances given a currency.
     *
     * @param currency The currency, which determines the required precision. As
     *                 {@link RoundingMode}, by default, {@link RoundingMode#HALF_UP}
     *                 is used.
     */
    FakturamaCashRounding(CurrencyUnit currency, RoundingMode roundingMode, int minimalMinors) {
        this(currency.getDefaultFractionDigits(), roundingMode, minimalMinors);
    }

    /**
     * Creates an {@link MonetaryOperator} for rounding {@link MonetaryAmount}
     * instances given a currency.
     *
     * @param currency The currency, which determines the required precision. As
     *                 {@link RoundingMode}, by default, {@link RoundingMode#HALF_UP}
     *                 is used.
     */
    FakturamaCashRounding(CurrencyUnit currency, int minimalMinors) {
        this(currency, RoundingMode.HALF_UP, minimalMinors);
    }

    /*
     * (non-Javadoc)
     *
     * @see javax.money.MonetaryFunction#apply(java.lang.Object)
     */
    @Override
    public MonetaryAmount apply(MonetaryAmount value) {
        Objects.requireNonNull(value, "Amount required.");
        // 1 extract BD value, round according the default fraction units
        int scale = this.context.getInt(SCALE_KEY);
        RoundingMode roundingMode = this.context.get(RoundingMode.class);
        BigDecimal num = value.getNumber().numberValue(BigDecimal.class).setScale(scale, roundingMode);
        // 2 evaluate minor units and remainder
        long minors = num.movePointRight(num.scale()).longValueExact();
        int minimalMinors = this.context.getInt(MINMINORS_KEY);
        long factor = minors / minimalMinors;
        long low = minimalMinors * factor;
        long high = minimalMinors * (factor + 1);
        if (minors - low > high - minors) {
            minors = high;
        } else if (minors - low < high - minors) {
            minors = low;
        } else {
            switch (roundingMode) {
                case HALF_UP:
                case UP:
                case HALF_EVEN:
                    minors = high;
                    break;
                default:
                    minors = low;
            }
        }
        return value.getFactory().setCurrency(value.getCurrency())
                .setNumber(BigDecimal.valueOf(minors).movePointLeft(scale)).create();
    }

    @Override
    public RoundingContext getRoundingContext() {
        return context;
    }

}
