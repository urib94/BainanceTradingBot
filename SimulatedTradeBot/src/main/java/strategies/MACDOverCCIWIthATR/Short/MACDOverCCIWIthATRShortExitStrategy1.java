package main.java.strategies.MACDOverCCIWIthATR.Short;

import TradingTools.Trailers.SkippingExitTrailer;
import data.DataHolder;
import positions.SellingInstructions;
import strategies.ExitStrategy;

public class MACDOverCCIWIthATRShortExitStrategy1 implements ExitStrategy {
    public double ATRValue;
    private boolean cancelledTP=false;
    private SkippingExitTrailer skippingExitTrailer;
    private boolean isTrailing=false;




    @Override
    public SellingInstructions run(DataHolder realTimeData) {
        return null;
    }
}
