package com.binance.client.api.model.trade;

import com.binance.client.api.constant.BinanceApiConstants;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;

public class Loan {

    private String coin;

    private BigDecimal amount;

    private String collateralCoin;

    private BigDecimal collateralAmount;

    private Long time;

    private Long borrowId;

    public String getCoin() {
        return coin;
    }

    public void setCoin(String coin) {
        this.coin = coin;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCollateralCoin() {
        return collateralCoin;
    }

    public void setCollateralCoin(String collateralCoin) {
        this.collateralCoin = collateralCoin;
    }

    public BigDecimal getCollateralAmount() {
        return collateralAmount;
    }

    public void setCollateralAmount(BigDecimal collateralAmount) {
        this.collateralAmount = collateralAmount;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Long getBorrowId() {
        return borrowId;
    }

    public void setBorrowId(Long borrowId) {
        this.borrowId = borrowId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
                .append("coin", coin).append("amount", amount).append("collateralCoin", collateralCoin)
                .append("collateralAmount", collateralAmount).append("time", time).append("borrowId", borrowId).toString();
    }
}
