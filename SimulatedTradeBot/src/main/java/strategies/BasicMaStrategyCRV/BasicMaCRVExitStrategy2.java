package main.java.strategies.BasicMaStrategyCRV;

import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.PositionHandler;
import positions.SellingInstructions;
import strategies.ExitStrategy;

public class BasicMaCRVExitStrategy2 implements ExitStrategy {

    private PositionSide positionSide;
    private double open;
    private boolean once = true;
    private boolean needToSell = false;

    public BasicMaCRVExitStrategy2(PositionSide positionSide, double open) {
        this.positionSide = positionSide;
        this.open = open;
    }
    @Override
    public SellingInstructions run(DataHolder realTimeData) {
        boolean isActive = open != realTimeData.getOpenPrice(realTimeData.getLastCloseIndex());
        int closeIndex = realTimeData.getLastCloseIndex();
        boolean isBullish = realTimeData.getClosePriceAtIndex(closeIndex) > realTimeData.getOpenPrice(closeIndex);
        if(isActive && once && !needToSell){
            boolean volumeLarge = realTimeData.getVolumeAtIndex(closeIndex) > 400000;
            switch (positionSide){

                case SHORT:
                    if(!isBullish && volumeLarge){
                        once = false;
                        needToSell = true;
                    }
                    break;
                case LONG:
                    if(isBullish && volumeLarge){
                        once = false;
                        needToSell = true;
                    }
                    break;
            }
        }
        if (needToSell){
            double currentPrice = realTimeData.getCurrentPrice();
            switch (positionSide){

                case SHORT:
                    if (isBullish){
                        return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET, BasicMaCRVConsts.EXIT_2_SELLING_PERCENTAGE);
                    }
                    break;
                case LONG:
                    if (!isBullish){
                        return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET, BasicMaCRVConsts.EXIT_2_SELLING_PERCENTAGE);
                    }
                    break;
            }
        }
        return null;
    }
}
