package com.binance.client.api.model.trade;

import com.binance.client.api.constant.BinanceApiConstants;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.math.BigDecimal;

public class crossCollateral {

    private String loanCoin;

    private String collateralCoin;

    private BigDecimal locked;

    private BigDecimal loanAmount;

    private BigDecimal currentCollateralRate;

    private BigDecimal interestFreeLimitUsed;

    private BigDecimal principalForInterest;

    private BigDecimal interest;

    public String getLoanCoin() {
        return loanCoin;
    }

    public void setLoanCoin(String loanCoin) {
        this.loanCoin = loanCoin;
    }

    public String getCollateralCoin() {
        return collateralCoin;
    }

    public void setCollateralCoin(String collateralCoin) {
        this.collateralCoin = collateralCoin;
    }

    public BigDecimal getLocked() {
        return locked;
    }

    public void setLocked(BigDecimal locked) {
        this.locked = locked;
    }

    public BigDecimal getLoanAmount() {
        return loanAmount;
    }

    public void setLoanAmount(BigDecimal loanAmount) {
        this.loanAmount = loanAmount;
    }

    public BigDecimal getCurrentCollateralRate() {
        return currentCollateralRate;
    }

    public void setCurrentCollateralRate(BigDecimal currentCollateralRate) {
        this.currentCollateralRate = currentCollateralRate;
    }

    public BigDecimal getInterestFreeLimitUsed() {
        return interestFreeLimitUsed;
    }

    public void setInterestFreeLimitUsed(BigDecimal interestFreeLimitUsed) {
        this.interestFreeLimitUsed = interestFreeLimitUsed;
    }

    public BigDecimal getPrincipalForInterest() {
        return principalForInterest;
    }

    public void setPrincipalForInterest(BigDecimal principalForInterest) {
        this.principalForInterest = principalForInterest;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public void setInterest(BigDecimal interest) {
        this.interest = interest;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceApiConstants.TO_STRING_BUILDER_STYLE)
                .append("loanCoin", loanCoin).append("collateralCoin", collateralCoin).append("locked", locked)
                .append("loanAmount", loanAmount).append("currentCollateralRate", currentCollateralRate)
                .append("interestFreeLimitUsed", interestFreeLimitUsed).append("principalForInterest", principalForInterest).append("interest", interest).toString();
    }
}
