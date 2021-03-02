package Strategies.MACDOverRSIStrategies.Long;

import Data.RealTimeData;
import Positions.PositionHandler;
import Strategies.EntryStrategy;
import Strategies.ExitStrategy;
import Strategies.MACDOverRSIStrategies.MACDOverRSIConstants;
import Strategies.RSIStrategies.PositionInStrategy;
import Strategies.RSIStrategies.RSIConstants;

import java.math.BigDecimal;
import java.util.ArrayList;

public class MACDOverRSILongEntryStrategy implements EntryStrategy {

    double takeProfitPercentage = MACDOverRSIConstants.DEFAULT_TAKE_PROFIT_PERCENTAGE;
    private double stopLossPercentage = MACDOverRSIConstants.DEFAULT_STOP_LOSS_PERCENTAGE;
    private int leverage = MACDOverRSIConstants.DEFAULT_LEVERAGE;
    private  BigDecimal requestedBuyingAmount = MACDOverRSIConstants.DEFAULT_BUYING_AMOUNT;

    @Override
    public PositionHandler run(RealTimeData realTimeData, String symbol) {
        if (realTimeData.crossed(RealTimeData.IndicatorType.MACD_OVER_RSI, RealTimeData.CrossType.UP,RealTimeData.CandleType.CLOSE,0)) {
            //TODO: buy stuff
            ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
            exitStrategies.add(new MACDOverRSILongExitStrategy1());
            exitStrategies.add(new MACDOverRSILongExitStrategy2());

        }
        if (realTimeData.getMacdOverRsiSignalLineValueAtIndex(realTimeData.getLastCloseIndex()) < 0) {
            if (realTimeData.urisRulesForEntry()) {
                //TODO: buy stuff
                ArrayList<ExitStrategy> exitStrategies = new ArrayList<>();
                exitStrategies.add(new MACDOverRSILongExitStrategy1());
                exitStrategies.add(new MACDOverRSILongExitStrategy2());

                return null;
            }
        }
        return null;
    }

    @Override
    public void setTakeProfitPercentage(double takeProfitPercentage) {
        this.takeProfitPercentage =takeProfitPercentage;
    }

    @Override
    public void setStopLossPercentage(double stopLossPercentage) {
        this.stopLossPercentage = stopLossPercentage;
    }

    @Override
    public void setLeverage(int leverage) {
        this.leverage = leverage;
    }

    @Override
    public void setRequestedBuyingAmount(BigDecimal requestedBuyingAmount) {
        this.requestedBuyingAmount = requestedBuyingAmount;
    }
}
