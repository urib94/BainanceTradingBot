package Strategies.RSIStrategies;

import Data.RealTimeData;
import Strategies.EntryStrategy;

import java.math.BigDecimal;

public class RSIStrategy {
    double takeProfitPercentage;
    private final double stopLossPercentage;
    private final int rsiCandleNum;
    private final int leverage;
    private final String symbol;
    private final  BigDecimal requestedBuyingAmount;

    public RSIStrategy(double takeProfitPercentage, double stopLossPercentage, int rsiCandleNum, int leverage, String symbol, BigDecimal requestedBuyingAmount) {
        this.takeProfitPercentage = takeProfitPercentage;
        this.stopLossPercentage = stopLossPercentage;
        this.rsiCandleNum = rsiCandleNum;
        this.leverage = leverage;
        this.symbol = symbol;
        this.requestedBuyingAmount = requestedBuyingAmount;
    }


    public void executeStrategy(){


    }
}
