package com.example.order.processing;

import static com.timgroup.karg.keywords.Keyword.newKeyword;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.example.order.model.TradeOrderType;
import com.timgroup.karg.keywords.Keyword;
import com.timgroup.karg.keywords.KeywordArgument;
import com.timgroup.karg.keywords.KeywordArguments;


@Entity
@Table(name="TRADE_ORDER")
public class TradeOrderRecord {
    
    @Id
    @Column(name="ID", length=32)
    @GeneratedValue
    public String id;

    @Column(name="COMPANY_ID")
    public String companyId;

    @Column(name="FOHF_ID")
    public String fohfId;
    
    @Column(name="ASSET_ID")
    public String assetId;

    @Column(name="CURRENCY_ID")
    public String currencyId;
    
    @Column(name="STATUS", nullable=false)
    public String status;
    
    @Column(name="AMOUNT", nullable=true)
    public BigDecimal amount;
    
    @Column(name="SHARES", nullable=true)
    public BigDecimal shares;

    @Column(name="PERCENTAGE", nullable=true)
    public BigDecimal percentage;
    
    @Column(name="WHOLE_HEDGE_FUND", nullable=false)
    public boolean isWholeHedgeFund;

    @Column(name="TRADE_DATE", nullable=false)
    public Date tradeDate;

    @Column(name="VALUE_DATE", nullable=false)
    public Date valueDate;

    @Column(name="TYPE")
    public TradeOrderType type;
    
    public static class Arguments {
    	public static final Keyword<String> COMPANY_ID = newKeyword();
    	public static final Keyword<String> FOHF_ID = newKeyword();
    	public static final Keyword<String> ASSET_ID = newKeyword();
    	public static final Keyword<String> CURRENCY_ID = newKeyword();
    	public static final Keyword<String> STATUS = newKeyword();
    	public static final Keyword<BigDecimal> AMOUNT = newKeyword();
    	public static final Keyword<BigDecimal> SHARES = newKeyword();
    	public static final Keyword<BigDecimal> PERCENTAGE = newKeyword();
    	public static final Keyword<Boolean> IS_WHOLE_HEDGE_FUND = newKeyword();
    	public static final Keyword<Date> TRADE_DATE = newKeyword();
    	public static final Keyword<Date> VALUE_DATE = newKeyword();
    	public static final Keyword<TradeOrderType> TYPE = newKeyword();
    }
    
    protected TradeOrderRecord() { }
    
    public TradeOrderRecord(KeywordArgument...arguments){
    	this(KeywordArguments.of(arguments));
    }
    
    public TradeOrderRecord(KeywordArguments arguments) {
    	this.companyId = Arguments.COMPANY_ID.from(arguments);
    	this.fohfId = Arguments.FOHF_ID.from(arguments);
    	this.assetId = Arguments.ASSET_ID.from(arguments);
    	this.currencyId = Arguments.CURRENCY_ID.from(arguments);
    	this.status = Arguments.STATUS.from(arguments);
    	this.amount = Arguments.AMOUNT.from(arguments);
    	this.shares = Arguments.SHARES.from(arguments);
    	this.percentage = Arguments.PERCENTAGE.from(arguments);
    	this.isWholeHedgeFund = Arguments.IS_WHOLE_HEDGE_FUND.from(arguments, false);
    	this.tradeDate = Arguments.TRADE_DATE.from(arguments);
    	this.valueDate = Arguments.VALUE_DATE.from(arguments);
    	this.type = Arguments.TYPE.from(arguments);
    }

}