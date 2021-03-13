package strategies.macdOverRSIStrategies;

import data.DataHolder;
import data.RealTimeData;
import org.ta4j.core.BaseBarSeries;
import strategies.ExitStrategy;

import java.math.BigDecimal;

public abstract class MACDOverRSIBaseExitStrategy implements ExitStrategy {

    public boolean changedDirection(DataHolder realTimeData, DataHolder.CandleType type){
        double now, prev;
        if (type == DataHolder.CandleType.CLOSE){
            now = realTimeData.getMacdOverRsiCloseValue();
            prev = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex() -1);
        }
        else {
            now = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex());
            prev = realTimeData.getMacdOverRsiCloseValue();
        }
        return Math.abs(prev) >= Math.abs(now);
    }

    public boolean changedDirectionAndPositiveThreeHistogram(DataHolder realTimeData){
        double now = realTimeData.getMacdOverRsiCloseValue();
        double prev = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex() -1);
        return now > 0 && prev > 0 && realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex() -2) > 0
                && Math.abs(prev) >= Math.abs(now);
    }

    public boolean changedDirectionAndNegativeThreeHistogram(DataHolder realTimeData){
        double now = realTimeData.getMacdOverRsiCloseValue();
        double prev = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex() -1);
        return now < 0 && prev < 0 && realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex() -2) < 0
                && Math.abs(prev) >= Math.abs(now);
    }

    public boolean stayInTrackAndThreePositiveHistograms(DataHolder realTimeData){
        double now = realTimeData.getMacdOverRsiCloseValue();
        double prev = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex()-1);
        return Math.abs(prev) <= Math.abs(now) && now > 0 && prev > 0
                && realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex() -2) > 0;
    }

    public boolean stayInTrackAndThreeNegativeHistograms(DataHolder realTimeData){
        double now = realTimeData.getMacdOverRsiCloseValue();
        double prev = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex()-1);
        return Math.abs(prev) <= Math.abs(now) && now < 0 && prev < 0
                && realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex() -2) < 0;
    }

//    public boolean currentPriceCrossedBollinger(DataHolder realTimeData ,DataHolder.CrossType crossType, BollingerType bollingerType){
//        BigDecimal bollingerValue;
//        BaseBarSeries baseBarSeries = realTimeData.getRealTimeData();
//        BigDecimal curr = BigDecimal.valueOf(baseBarSeries.getBar(MACDOverRSIConstants.LAST_CLOSE_INDEX).getClosePrice().doubleValue());
//        BigDecimal prev = BigDecimal.valueOf(baseBarSeries.getBar(MACDOverRSIConstants.LAST_CLOSE_INDEX -1).getClosePrice().doubleValue());
//        if (bollingerType == BollingerType.UPPER){
//            bollingerValue = BigDecimal.valueOf(realTimeData.getUpperBollingerAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -1));
//        }
//        else{
//            bollingerValue = BigDecimal.valueOf(realTimeData.getLowerBollingerAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -1));
//        }
//        if (crossType == RealTimeData.CrossType.UP){
//            return curr.compareTo(bollingerValue) >= 0 && prev.compareTo(bollingerValue) <= 0;
//        }
//        return curr.compareTo(bollingerValue) <= 0 && prev.compareTo(bollingerValue) >= 0;
//    }
//
//
//
//    public enum DecliningType{
//        NEGATIVE,
//        POSITIVE;
//    }
//
//    public enum BollingerType{
//        UPPER,
//        LOWER;
//    }
}
