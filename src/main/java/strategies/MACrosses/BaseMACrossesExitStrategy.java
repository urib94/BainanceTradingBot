package strategies.MACrosses;

import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import strategies.ExitStrategy;

public abstract class BaseMACrossesExitStrategy implements ExitStrategy {
    private boolean slowCondition = false;
    protected boolean fastManagement = true;
    protected final PositionSide positionSide;

    public BaseMACrossesExitStrategy(PositionSide positionSide) {
        this.positionSide = positionSide;
    }

    protected boolean crossedSma(DataHolder realTimeData, DataHolder.IndicatorType indicatorType, DataHolder.CrossType crossType, int CundleCount) {
        int closeIndex = realTimeData.getLastCloseIndex();
        double prev = 0, curr = 0, smaCurrValue = 0, smaPrevValue = 0;
        switch (indicatorType) {
            case RSI:
                curr = realTimeData.getRSIValueAtIndex(closeIndex);
                prev = realTimeData.getRSIValueAtIndex(closeIndex - 1);
                if(CundleCount == MACrossesConstants.SMA_OVER_RSI_BAR_COUNT) {
                    smaCurrValue = realTimeData.getSmaOverRSIValue(closeIndex);
                    smaPrevValue = realTimeData.getSmaOverRSIValue(closeIndex - 1);
                } else {
                    if (CundleCount == MACrossesConstants.FAST_SMA_OVER_RSI_BAR_COUNT) {
                        smaCurrValue = realTimeData.getFastSmaOverRSIValue(closeIndex);
                        smaPrevValue = realTimeData.getFastSmaOverRSIValue(closeIndex - 1);
                    }
                }
                break;
            case MFI:
                curr = realTimeData.getMFIValue(closeIndex);
                prev = realTimeData.getMFIValue(closeIndex - 1);
                smaCurrValue = realTimeData.getSmaOverMFIValue(closeIndex);
                smaPrevValue = realTimeData.getSmaOverMFIValue(closeIndex - 1);
                break;
            default:
                break;
        }
        switch (crossType) {
            case UP:
                return prev <= smaPrevValue && curr > smaCurrValue + MACrossesConstants.EXIT_THRESHOLD;
            case DOWN:
                return prev >= smaPrevValue && curr < smaCurrValue - MACrossesConstants.EXIT_THRESHOLD;
        }
        return false;
    }

    protected void setFastManagement(boolean fastManagement) {
        this.fastManagement = fastManagement;
    }

    protected void updateManagement(DataHolder realTimeData) {
        switch (positionSide) {

            case SHORT:
                if (!MACrossesEntryStrategy.priceIsAboveSMA(realTimeData)) {
                    if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.DOWN, MACrossesConstants.SMA_OVER_RSI_BAR_COUNT)/* ||
                            (crossedSma(realTimeData, DataHolder.IndicatorType.MFI, DataHolder.CrossType.DOWN) && !rsiAboveSMA(realTimeData))*/) {
                        slowCondition = true;
                        if (realTimeData.closePriceCrossed(DataHolder.CrossType.DOWN, DataHolder.CandleType.CLOSE, realTimeData.getLowerBollingerAtIndex(realTimeData.getLastCloseIndex()))){
                            fastManagement = false;
                        }
                    } else if(realTimeData.closePriceCrossed(DataHolder.CrossType.UP, DataHolder.CandleType.CLOSE, realTimeData.getUpperBollingerAtIndex(realTimeData.getLastCloseIndex()))){
                        slowCondition = false;
                    }
                }
                break;

            case LONG:
                if (MACrossesEntryStrategy.priceIsAboveSMA(realTimeData)) {
                    if (crossedSma(realTimeData, DataHolder.IndicatorType.RSI, DataHolder.CrossType.UP, MACrossesConstants.SMA_OVER_RSI_BAR_COUNT)) {  /*||
                            (crossedSma(realTimeData, DataHolder.IndicatorType.MFI, DataHolder.CrossType.UP) && rsiAboveSMA(realTimeData))*/

                        if(realTimeData.closePriceCrossed(DataHolder.CrossType.UP, DataHolder.CandleType.CLOSE, realTimeData.getUpperBollingerAtIndex(realTimeData.getLastCloseIndex()))){
                        fastManagement = false;
                        }
                    } else if(realTimeData.closePriceCrossed(DataHolder.CrossType.DOWN, DataHolder.CandleType.CLOSE, realTimeData.getUpperBollingerAtIndex(realTimeData.getLastCloseIndex()))) {
                        slowCondition = false;
                    }
                    break;
                }
        }
    }

}
