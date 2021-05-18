package strategies.MACDOverCCIWIthATR.Short;

import TradingTools.Trailers.SkippingExitTrailer;
import TradingTools.Trailers.TrailingExit;
import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.DCAInstructions;
import positions.Instructions;
import strategies.MACDOverCCIWIthATR.BaseMACDOverCCIWIthATRExitStrategy;

public class MACDOverCCIWIthATRShortExitStrategy1 extends BaseMACDOverCCIWIthATRExitStrategy {
    public double ATRValue;
    private boolean cancelledTP=false;
    private SkippingExitTrailer skippingExitTrailer;
    private boolean isTrailing=false;
    public MACDOverCCIWIthATRShortExitStrategy1(double initiallPrice, int maxDCACount, double initialAmount, double amountFactor,
                                                PositionSide positionSide, double TPPrice, double DCAPrices, String symbol, double step, double stepFactor, DataHolder dataHolder, TrailingExit trailingExit, DCAInstructions dcaInstructions) {
        super(initiallPrice, maxDCACount, initialAmount, amountFactor, positionSide, TPPrice,DCAPrices,symbol, step,stepFactor, dataHolder, dcaInstructions );        this.tPPrice = TPPrice;
        this.ATRValue = ATRValue;
        this.skippingExitTrailer=(SkippingExitTrailer) trailingExit;
    }

    @Override
    public void updateExitStrategy() {

    }

    @Override
    public Instructions run(DataHolder realTimeData) {
        return null;
    }
}
