package main.java.strategies.BasicMaStrategyBNB;

import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.PositionHandler;
import positions.SellingInstructions;
import strategies.ExitStrategy;

public class BasicMaBNBExitStrategy1 implements ExitStrategy {
    private PositionSide positionSide;

    public BasicMaBNBExitStrategy1(PositionSide positionSide) {
        this.positionSide = positionSide;
    }


    @Override
    public SellingInstructions run(DataHolder realTimeData) {
        int closeIndex = realTimeData.getLastCloseIndex();
        double currentPrice = realTimeData.getClosePriceAtIndex(closeIndex);
        switch (positionSide) {


            case SHORT:
                boolean currentPriceCrossedMaUp = currentPrice > realTimeData.getSlowSmaValueAtIndex(closeIndex);
                if (currentPriceCrossedMaUp) {
                    return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET, BasicMaBNBConsts.EXIT_SELLING_PERCENTAGE);
                }
                break;
            case LONG:
                boolean currentPriceCrossedMaDown = currentPrice < realTimeData.getSlowSmaValueAtIndex(closeIndex);
                if (currentPriceCrossedMaDown) {
                    return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET, BasicMaBNBConsts.EXIT_SELLING_PERCENTAGE);
                }
                break;
        }
        return null;
    }
}
