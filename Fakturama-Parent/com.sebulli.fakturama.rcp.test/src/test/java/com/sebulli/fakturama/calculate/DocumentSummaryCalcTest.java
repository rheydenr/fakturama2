package com.sebulli.fakturama.calculate;

import java.util.spi.ResourceBundleProvider;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.services.nls.IMessageFactoryService;
import org.javamoney.moneta.Money;
import org.javamoney.moneta.spi.MoneyUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.sebulli.fakturama.dto.Price;
import com.sebulli.fakturama.dto.PriceBuilder;
import com.sebulli.fakturama.i18n.Messages;

public class DocumentSummaryCalcTest {

    private IEclipseContext ctx;

    @Mock
    private IMessageFactoryService factoryService;
    
    @Mock
    private ResourceBundleProvider provider;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ctx = EclipseContextFactory.create("test-context");
        Mockito.when(factoryService.getMessageInstance(Mockito.any(), Mockito.any(), Mockito.any())).thenReturn(new Messages());
        
        ctx.set(IMessageFactoryService.class, factoryService);
        ctx.set(ResourceBundleProvider.class, provider);
        ContextInjectionFactory.setDefault(ctx);
    }

    @Test
    public void testCalculateSummary() {
        Price testPrice = new PriceBuilder()
                .withAmount(Money.of(MoneyUtils.getBigDecimal(25), "EUR"))
                .withVatPercent(0.19).build();
//        Price p = new Price(Money.of(MoneyUtils.getBigDecimal(25), "EUR"));
        System.out.println(testPrice);

    }

}
