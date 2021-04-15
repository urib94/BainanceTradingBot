package strategies.MACDOverCCIWIthATR.Short;

import TradingTools.Trailers.SkippingExitTrailer;
import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.DCAInstructions;
import positions.Instructions;
import strategies.ExitStrategy;
import strategies.MACDOverCCIWIthATR.BaseMACDOverCCIWIthATRExitStrategy;

public class MACDOverCCIWIthATRShortExitStrategy1 extends BaseMACDOverCCIWIthATRExitStrategy {
    public MACDOverCCIWIthATRShortExitStrategy1(double initiallPrice, double maxDCACount, double initialAmount, double amountFactor, PositionSide positionSide, double TPPrice, double[] DCAPrices, String symbol, boolean useTP, DataHolder dataHolder, SkippingExitTrailer skippingExitTrailer, DCAInstructions dcaInstructions) {
        super(initiallPrice, maxDCACount, initialAmount, amountFactor, positionSide, TPPrice, DCAPrices, symbol, useTP, dataHolder, dcaInstructions);
    }

    @Override
    public void updateExitStrategy() {

    }

    @Override
    public Instructions run(DataHolder realTimeData) {
        return null;
    }
}
