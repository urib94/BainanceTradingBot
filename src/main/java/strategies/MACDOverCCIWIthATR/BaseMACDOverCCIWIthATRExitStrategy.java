package strategies.MACDOverCCIWIthATR;

import TradingTools.DCA.BaseDCA;
import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.Instructions;
import strategies.ExitStrategy;

public abstract class BaseMACDOverCCIWIthATRExitStrategy extends BaseDCA implements ExitStrategy {


    public BaseMACDOverCCIWIthATRExitStrategy(double initiallPrice, double maxDCACount, double initialAmount, double amountFactor,
                                              PositionSide positionSide, double[] exitPrices,String symbol, boolean useTP,DataHolder dataHolder) {
        super(initiallPrice, maxDCACount, initialAmount, amountFactor, positionSide, exitPrices,symbol, useTP, dataHolder);
    }

    public Instructions run(DataHolder realTimeData){
        return  null;
    }

    @Override
    public void updateExitStrategy() {

    }
}

