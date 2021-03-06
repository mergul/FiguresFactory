package com.example.order.processing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;

import com.example.order.CurrencyException;
import com.example.order.OrderProcessingException;
import com.example.order.model.Currency;
import com.example.order.model.ExchangeRate;
import com.example.order.model.FundOfFund;
import com.example.order.model.HedgeFundAsset;
import com.example.order.model.Position;
import com.example.order.model.TradeOrderType;
import com.example.order.service.FXService;
import com.example.order.service.PositionFetcher;
import com.example.order.service.PriceFetcher;

public class FiguresFactoryTest {

    private static final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
    
    private Mockery context = new Mockery() {{ setImposteriser(ClassImposteriser.INSTANCE); }};

    private final PriceFetcher priceFetcher = context.mock(PriceFetcher.class);
    private final PositionFetcher positionsFetcher = context.mock(PositionFetcher.class);
    private final FXService fxService = context.mock(FXService.class);
    private final FundOfFund fohf = context.mock(FundOfFund.class);
    private final HedgeFundAsset gbpAsset = context.mock(HedgeFundAsset.class, "asset");
    private final HedgeFundAsset usdAsset = context.mock(HedgeFundAsset.class," usdAsset");
    private final Currency gbp = context.mock(Currency.class, "gbp");
    private final Currency usd = context.mock(Currency.class, "usd");
    private Date firstSeptember;
    
    @Before public void
    setup_prices_and_assets() throws ParseException, CurrencyException {
    	firstSeptember = fmt.parse("01/09/2011");
        context.checking(new Expectations() {{
            // best price for the gbp asset is 5 GBP
            allowing(priceFetcher).fetchBestPriceFor(with(gbpAsset), with(any(Date.class)), with(any(BigDecimal.class))); 
            	will(returnValue(new BigDecimal("5")));

            // best price for the usd asset is 2 USD
            allowing(priceFetcher).fetchBestPriceFor(with(usdAsset), with(any(Date.class)), with(any(BigDecimal.class))); 
            	will(returnValue(new BigDecimal("2")));
            	
            allowing(gbpAsset).getCurrency(); will(returnValue(gbp));
            allowing(usdAsset).getCurrency(); will(returnValue(usd));
            
            allowing(gbp).getSymbol(); will(returnValue("GBP"));
            allowing(usd).getSymbol(); will(returnValue("USD"));
            
            // GBP -> USD exchange rate is 1.5
            allowing(fxService).getExchangeRate(with(gbp), with(usd), with(any(Date.class)));
            	will(returnValue(new ExchangeRate(new BigDecimal("1.5"))));
            	
            // Position on 01/09 in GBP asset is 100 shares (@5 GBP per share)
            allowing(positionsFetcher).getAssetPosition(with(gbpAsset), with(fohf), with(firstSeptember));
            	will(returnValue(new Position(new BigDecimal("100"))));
        }});
    }
    
    @Test public void
    creates_figures_for_a_subscription_order_with_an_amount() throws OrderProcessingException {
    	// Given an order to Buy 100 GBP of the asset
        TradeOrder order = new TradeOrder();
        order.setAmount(new BigDecimal("100"));
        order.setCurrency(gbp);
        order.setAsset(gbpAsset);
        order.setType(TradeOrderType.SUBSCRIPTION);
        order.setFohf(fohf);
        
        // When we create the figures
        Figures figures = new FiguresFactory(priceFetcher, positionsFetcher, fxService).buildFrom(order, firstSeptember);
        
        // Then we expect to get 20 shares @ 5 GBP per share == 100 GBP total
        assertThat(figures.getAmount(), is(new BigDecimal("100")));
        assertThat(figures.getPrice().getValue(), is(new BigDecimal("5")));
        assertThat(figures.getPrice().getCurrency().getSymbol(), is("GBP"));
        assertThat(figures.getShares(), is(new BigDecimal("20")));
    }

    @Test public void
    creates_figures_for_a_subscription_order_with_a_number_of_shares() throws OrderProcessingException {
    	// Given an order to Buy 20 shares of the asset
        TradeOrder order = new TradeOrder();
        order.setShares(new BigDecimal("20"));
        order.setCurrency(gbp);
        order.setAsset(gbpAsset);
        order.setType(TradeOrderType.SUBSCRIPTION);
        order.setFohf(fohf);
        
        // When we create the figures
        Figures figures = new FiguresFactory(priceFetcher, positionsFetcher, fxService).buildFrom(order, firstSeptember);
        
        // Then we expect to get 20 shares @ 5 GBP per share == 100 GBP total
        assertThat(figures.getAmount(), is(new BigDecimal("100")));
        assertThat(figures.getPrice().getValue(), is(new BigDecimal("5")));
        assertThat(figures.getPrice().getCurrency().getSymbol(), is("GBP"));
        assertThat(figures.getShares(), is(new BigDecimal("20")));
    }
    
    @Test public void
    creates_figures_for_a_subscription_order_with_an_amount_in_a_different_currency() throws OrderProcessingException {
    	// Given an order to Buy 100 GBP of the USD asset
        TradeOrder order = new TradeOrder();
        order.setAmount(new BigDecimal("100"));
        order.setCurrency(gbp);
        order.setAsset(usdAsset);
        order.setType(TradeOrderType.SUBSCRIPTION);
        order.setFohf(fohf);
        
        // When we create the figures
        Figures figures = new FiguresFactory(priceFetcher, positionsFetcher, fxService).buildFrom(order, firstSeptember);
        
        // GBP -> USD fx rate is 1.5 so 100 GBP order is 150 USD
        // Then we expect to get 75 shares @ 2 USD per share == 150 USD total
        assertThat(figures.getAmount(), is(new BigDecimal("150")));
        assertThat(figures.getPrice().getValue(), is(new BigDecimal("2")));
        assertThat(figures.getPrice().getCurrency().getSymbol(), is("USD"));
        assertThat(figures.getShares(), is(new BigDecimal("75")));
    }
    
    @Test public void
    creates_figures_for_a_redemption_order_with_an_amount() throws OrderProcessingException {
    	// Given an order to Sell 100 GBP of the asset
        TradeOrder order = new TradeOrder();
        order.setAmount(new BigDecimal("100"));
        order.setCurrency(gbp);
        order.setAsset(gbpAsset);
        order.setType(TradeOrderType.REDEMPTION);
        order.setTradeDate(firstSeptember);
        order.setFohf(fohf);
        
        // When we create the figures
        Figures figures = new FiguresFactory(priceFetcher, positionsFetcher, fxService).buildFrom(order, firstSeptember);
        
        // Then we expect to get 20 shares @ 5 GBP per share == 100 GBP total
        assertThat(figures.getAmount(), is(new BigDecimal("100")));
        assertThat(figures.getPrice().getValue(), is(new BigDecimal("5")));
        assertThat(figures.getPrice().getCurrency().getSymbol(), is("GBP"));
        assertThat(figures.getShares(), is(new BigDecimal("20")));
    }

    @Test(expected=OrderProcessingException.class) public void
    exception_thrown_when_creating_figures_for_redemption_order_greater_than_current_position() throws OrderProcessingException {
    	// Given an order to Sell 2000 GBP of the asset
        TradeOrder order = new TradeOrder();
        order.setAmount(new BigDecimal("2000"));
        order.setCurrency(gbp);
        order.setAsset(gbpAsset);
        order.setType(TradeOrderType.REDEMPTION);
        order.setTradeDate(firstSeptember);
        order.setFohf(fohf);
        
        // When we create the figures
        new FiguresFactory(priceFetcher, positionsFetcher, fxService).buildFrom(order, firstSeptember);
        
        // Then we expect an exception
    }
    
    @Test public void
    creates_figures_for_a_redemption_order_with_a_percentage() throws OrderProcessingException {
    	// Given an order to Sell 50% of the asset (we have 100 shares @ 5 GBP per share)
        TradeOrder order = new TradeOrder();
        order.setPercentage(new BigDecimal("50"));
        order.setCurrency(gbp);
        order.setAsset(gbpAsset);
        order.setType(TradeOrderType.REDEMPTION);
        order.setTradeDate(firstSeptember);
        order.setFohf(fohf);
        
        // When we create the figures
        Figures figures = new FiguresFactory(priceFetcher, positionsFetcher, fxService).buildFrom(order, firstSeptember);
        
        // Then we expect to get 50 shares @ 5 GBP per share == 500 GBP total
        assertThat(figures.getAmount(), is(new BigDecimal("250")));
        assertThat(figures.getPrice().getValue(), is(new BigDecimal("5")));
        assertThat(figures.getPrice().getCurrency().getSymbol(), is("GBP"));
        assertThat(figures.getShares(), is(new BigDecimal("50")));
    }

}
