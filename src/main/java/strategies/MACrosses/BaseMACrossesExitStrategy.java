package strategies.MACrosses;

import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import strategies.ExitStrategy;

public abstract class BaseMACrossesExitStrategy implements ExitStrategy {
    protected final PositionSide positionSide;

    public BaseMACrossesExitStrategy(PositionSide positionSide){
        this.positionSide = positionSide;
    }

    protected boolean crossedSma(DataHolder realTimeData, DataHolder.IndicatorType indicatorType, DataHolder.CrossType crossType){
        int closeIndex = realTimeData.getLastCloseIndex();
        double smaCurrValue = realTimeData.getSmaValueAtIndex(closeIndex);
        double smaPrevValue = realTimeData.getSmaValueAtIndex(closeIndex - 1);
        double prev = 0, curr = 0;
        switch(indicatorType){
            case RSI:
                curr = realTimeData.getRSIValueAtIndex(closeIndex);
                prev = realTimeData.getRSIValueAtIndex(closeIndex - 1);
                break;
            case MFI:
                curr = realTimeData.getMFIValue(closeIndex);
                prev = realTimeData.getMFIValue(closeIndex - 1);
                break;
            default:
                break;
        }
        switch(crossType){
            case UP:
                return prev <= smaPrevValue && curr > smaCurrValue;
            case DOWN:
                return prev >= smaPrevValue && curr < smaCurrValue;
        }
        return false;
    }
}
