package strategies.MACDOverCCIWIthATR;

import TradingTools.DCA.BaseDCA;
import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.DCAInstructions;
import positions.SellingInstructions;
import strategies.ExitStrategy;

public abstract class BaseMACDOverCCIWIthATRExitStrategy extends BaseDCA implements ExitStrategy {


    public BaseMACDOverCCIWIthATRExitStrategy(double initiallPrice, double maxDCACount, double initialAmount, double amountFactor, PositionSide positionSide, double exitPrice) {
        super(initiallPrice, maxDCACount, initialAmount, amountFactor, positionSide, exitPrice);
    }

    @Override
    public void updateExitStrategy() {

    }
}

