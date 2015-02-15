package org.javamoney.moneta.internal;

//import javax.annotation.Priority;
import javax.money.MonetaryAmountFactory;
import javax.money.MonetaryContext;
import javax.money.spi.MonetaryAmountFactoryProviderSpi;

import org.javamoney.moneta.Money;

/**
 * Implementation of {@link MonetaryAmountFactoryProviderSpi} creating instances of
 * {@link MoneyAmountBuilder}.
 *
 * @author Anatole Tresch
 */
//@Priority(10)
public final class MoneyAmountFactoryProvider implements MonetaryAmountFactoryProviderSpi<Money>{

    @Override
    public Class<Money> getAmountType(){
        return Money.class;
    }

    @Override
    public MonetaryAmountFactory<Money> createMonetaryAmountFactory(){
        return new MoneyAmountBuilder();
    }

    /*
     * (non-Javadoc)
     * @see javax.money.spi.MonetaryAmountFactoryProviderSpi#getQueryInclusionPolicy()
     */
    @Override
    public QueryInclusionPolicy getQueryInclusionPolicy(){
        return QueryInclusionPolicy.ALWAYS;
    }

    @Override
    public MonetaryContext getDefaultMonetaryContext(){
        return MoneyAmountBuilder.DEFAULT_CONTEXT;
    }

    @Override
    public MonetaryContext getMaximalMonetaryContext(){
        return MoneyAmountBuilder.MAX_CONTEXT;
    }
}
