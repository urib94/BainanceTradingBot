package strategies.MACDOverCCIWIthATR;

import TradingTools.DCA.BaseDCA;
import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.DCAInstructions;
import positions.Instructions;
import strategies.ExitStrategy;

public abstract class BaseMACDOverCCIWIthATRExitStrategy extends BaseDCA implements ExitStrategy {


    public BaseMACDOverCCIWIthATRExitStrategy(double initiallPrice, double maxDCACount, double initialAmount, double amountFactor,
                                              PositionSide positionSide, double TPPrice, double DCAPrices, String symbol, double step, double stepFactor, DataHolder dataHolder
            , DCAInstructions dcaInstructions) {
        super(initiallPrice, maxDCACount, initialAmount, amountFactor, positionSide, TPPrice,DCAPrices,symbol, step ,stepFactor,dcaInstructions);
    }

    public Instructions run(DataHolder realTimeData){
        return null;
    }

    @Override
    public void updateExitStrategy() {

    }
}

