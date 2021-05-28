package strategies.MACrosses;

import com.binance.client.model.enums.*;
import data.DataHolder;
import positions.PositionHandler;
import positions.SellingInstructions;

public class MACrossesExitStrategy5 extends BaseMACrossesExitStrategy{

    public MACrossesExitStrategy5(PositionSide positionSide) {
        super(positionSide);
    }

    @Override
    public SellingInstructions run(DataHolder realTimeData) {
        int index = realTimeData.getLastCloseIndex();
        double volume = realTimeData.getVolumeAtIndex(index);
        double smaOverVolume = realTimeData.getSmaOverVolumeValueAtIndex(index);
        if (volume > 2.5 * smaOverVolume || volume < 2.5 * smaOverVolume){
            switch (positionSide){

                case SHORT:
                    return new SellingInstructions(PositionHandler.ClosePositionTypes.CLOSE_SHORT_MARKET,
                            MACrossesConstants.EXIT_SELLING_PERCENTAGE);
                case LONG:
                    return new SellingInstructions(PositionHandler.ClosePositionTypes.SELL_MARKET,
                            MACrossesConstants.EXIT_SELLING_PERCENTAGE);
            }
        }
        return null;
    }
}
