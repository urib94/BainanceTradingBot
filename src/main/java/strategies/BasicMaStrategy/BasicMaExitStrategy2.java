package strategies.BasicMaStrategy;

import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import positions.PositionHandler;
import positions.SellingInstructions;
import strategies.ExitStrategy;

public class BasicMaExitStrategy2 implements ExitStrategy {

    private PositionSide positionSide;
    private double open;
    private boolean once = true;

    public BasicMaExitStrategy2(PositionSide positionSide, double open) {
        this.positionSide = positionSide;
        this.open = open;
    }
    @Override
    public SellingInstructions run(DataHolder realTimeData) {
        boolean isActive = open != realTimeData.getOpenPrice(realTimeData.getLastCloseIndex());
        if(isActive && once){
            int closeIndex = realTimeData.getLastCloseIndex();
            boolean isBullish = realTimeData.getClosePriceAtIndex(closeIndex) > realTimeData.getOpenPrice(closeIndex);
            boolean volumeLarge = realTimeData.getVolumeAtIndex(closeIndex) > 1000;
            switch (positionSide){

                case SHORT:
                    if(!isBullish && volumeLarge){
                        once = false;
                        return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET, BasicMaConsts.EXIT_2_SELLING_PERCENTAGE);
                    }
                    break;
                case LONG:
                    if(isBullish && volumeLarge){
                        once = false;
                        return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET, BasicMaConsts.EXIT_2_SELLING_PERCENTAGE);
                    }
                    break;
            }
        }
        return null;
    }
}
