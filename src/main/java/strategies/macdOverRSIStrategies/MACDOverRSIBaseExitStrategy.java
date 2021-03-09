package strategies.macdOverRSIStrategies;

import data.RealTimeData;
import org.ta4j.core.BaseBarSeries;
import strategies.ExitStrategy;

import java.math.BigDecimal;

public abstract class MACDOverRSIBaseExitStrategy implements ExitStrategy {

    public boolean currentCandleBiggerThanPrev(RealTimeData realTimeData) {
        double now = realTimeData.getMacdOverRsiCloseValue();
        double prev = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-2);
        return Math.abs(prev) <= Math.abs(now);
    }

    public boolean upwardsPyramid(RealTimeData realTimeData) {
        double now = realTimeData.getMacdOverRsiCloseValue();
        double prev = realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -1);
        double prevprev = realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -2);
        return Math.abs(prevprev) <= Math.abs(prev) && Math.abs(prev) <= Math.abs(now);
    }
    public boolean downwardsPyramid(RealTimeData realTimeData) {
        double now = realTimeData.getMacdOverRsiCloseValue();
        double prev = realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -1);
        double prevprev = realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -2);
        return Math.abs(prevprev) >= Math.abs(prev) && Math.abs(prev) >= Math.abs(now);
    }

    public boolean negativeThreeHistograms(RealTimeData realTimeData) {
        return realTimeData.getMacdOverRsiCloseValue() < 0
                && realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -1) < 0
                && realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -2) < 0;
    }
    public boolean positiveThreeHistograms(RealTimeData realTimeData) {
        return realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex()) > 0
                && realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -1) > 0
                && realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -2) > 0;
    }

    public boolean changedDirection(RealTimeData realTimeData){
        double now = realTimeData.getMacdOverRsiCloseValue();
        double prev = realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -1);
        return Math.abs(prev) >= Math.abs(now);
    }

    public boolean changedDirectionAndPositiveThreeHistogram(RealTimeData realTimeData){
        double now = realTimeData.getMacdOverRsiCloseValue();
        double prev = realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -1);
        return now > 0 && prev > 0 && realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -2) > 0
                && Math.abs(prev) >= Math.abs(now);
    }

    public boolean changedDirectionAndNegativeThreeHistogram(RealTimeData realTimeData){
        double now = realTimeData.getMacdOverRsiCloseValue();
        double prev = realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -1);
        return now < 0 && prev < 0 && realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -2) < 0
                && Math.abs(prev) >= Math.abs(now);
    }

    public boolean stayInTrackAndThreePositiveHistograms(RealTimeData realTimeData){
        double now = realTimeData.getMacdOverRsiCloseValue();
        double prev = realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX-1);
        return Math.abs(prev) <= Math.abs(now) && now > 0 && prev > 0
                && realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -2) > 0;
    }

    public boolean stayInTrackAndThreeNegativeHistograms(RealTimeData realTimeData){
        double now = realTimeData.getMacdOverRsiCloseValue();
        double prev = realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX-1);
        return Math.abs(prev) <= Math.abs(now) && now < 0 && prev < 0
                && realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -2) < 0;
    }

    public boolean upwardsPyramidAndThreeNegativeHistograms(RealTimeData realTimeData){
        double now = realTimeData.getMacdOverRsiCloseValue();
        double prev = realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -1);
        double prevprev = realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -2);
        return Math.abs(prevprev) <= Math.abs(prev) && Math.abs(prev) <= Math.abs(now) && now < 0
                && prev < 0 && prevprev < 0;
    }

    public boolean currentPriceCrossedBollinger(RealTimeData realTimeData ,RealTimeData.CrossType crossType, BollingerType bollingerType){
        BigDecimal bollingerValue;
        BaseBarSeries baseBarSeries = realTimeData.getRealTimeData();
        BigDecimal curr = BigDecimal.valueOf(baseBarSeries.getBar(MACDOverRSIConstants.LAST_CLOSE_INDEX).getClosePrice().doubleValue());
        BigDecimal prev = BigDecimal.valueOf(baseBarSeries.getBar(MACDOverRSIConstants.LAST_CLOSE_INDEX -1).getClosePrice().doubleValue());
        if (bollingerType == BollingerType.UPPER){
            bollingerValue = BigDecimal.valueOf(realTimeData.getUpperBollingerAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -1));
        }
        else{
            bollingerValue = BigDecimal.valueOf(realTimeData.getLowerBollingerAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -1));
        }
        if (crossType == RealTimeData.CrossType.UP){
            return curr.compareTo(bollingerValue) >= 0 && prev.compareTo(bollingerValue) <= 0;
        }
        return curr.compareTo(bollingerValue) <= 0 && prev.compareTo(bollingerValue) >= 0;
    }



    public enum DecliningType{
        NEGATIVE,
        POSITIVE;
    }

    public enum BollingerType{
        UPPER,
        LOWER;
    }
}
