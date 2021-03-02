package com.binance.client.api.model.trade;

import com.binance.client.api.constant.BinanceApiConstants;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;
import java.util.List;

public class CrossCollateralWallet {

    private BigDecimal totalCrossCollateral;

    private BigDecimal totalBorrowed;

    private BigDecimal totalInterest;

    private BigDecimal interestFreeLimit;

    private String asset;

    private List<crossCollateral> crossCollaterals;


    public BigDecimal getTotalCrossCollateral() {
        return totalCrossCollateral;
    }

    public void setTotalCrossCollateral(BigDecimal totalCrossCollateral) {
        this.totalCrossCollateral = totalCrossCollateral;
    }

    public BigDecimal getTotalBorrowed() {
        return totalBorrowed;
    }

    public void setTotalBorrowed(BigDecimal totalBorrowed) {
        this.totalBorrowed = totalBorrowed;
    }

    public BigDecimal getTotalInterest() {
        return totalInterest;
    }

    public void setTotalInterest(BigDecimal totalInterest) {
        this.totalInterest = totalInterest;
    }

    public BigDecimal getInterestFreeLimit() {
        return interestFreeLimit;
    }

    public void setInterestFreeLimit(BigDecimal interestFreeLimit) {
        this.interestFreeLimit = interestFreeLimit;
    }

    public String getAsset() {
        return asset;
    }

    public void setAsset(String asset) {
        this.asset = asset;
    }

    public List<crossCollateral> getCrossCollaterals() {
        return crossCollaterals;
    }

    public void setCrossCollaterals(List<crossCollateral> crossCollaterals) {
        this.crossCollaterals = crossCollaterals;
    }

    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
                .append("totalCrossCollateral", totalCrossCollateral).append("totalBorrowed", totalBorrowed).append("totalInterest", totalInterest)
                .append("interestFreeLimit", interestFreeLimit).append("asset", asset).append("crossCollaterals", crossCollaterals).toString();
    }



}
