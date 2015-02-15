package org.javamoney.moneta.internal;

import javax.money.MonetaryAmountFactory;
import javax.money.MonetaryContext;
import javax.money.spi.MonetaryAmountFactoryProviderSpi;

import org.javamoney.moneta.FastMoney;

/**
 * Implementation of {@link MonetaryAmountFactoryProviderSpi} creating instances of
 * {@link FastMoneyAmountBuilder}.
 *
 * @author Anatole Tresch
 */
public final class FastMoneyAmountFactoryProvider implements MonetaryAmountFactoryProviderSpi<FastMoney>{

    @Override
    public Class<FastMoney> getAmountType(){
        return FastMoney.class;
    }

    @Override
    public MonetaryAmountFactory<FastMoney> createMonetaryAmountFactory(){
        // TODO ensure context!
        return new FastMoneyAmountBuilder();
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
        return FastMoneyAmountBuilder.DEFAULT_CONTEXT;
    }

    @Override
    public MonetaryContext getMaximalMonetaryContext(){
        return FastMoneyAmountBuilder.MAX_CONTEXT;
    }

}
