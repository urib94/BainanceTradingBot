package Strategies.MACDOverRSIStrategies;

import Data.RealTimeData;
import Strategies.ExitStrategy;

public abstract class MACDOverRSIBaseExitStrategy implements ExitStrategy {


    public boolean currentCandleBiggerThanPrev(RealTimeData realTimeData) {
        double now = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-1);
        double prev = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-2);
        return Math.abs(prev) <= Math.abs(now);
    }

    public boolean upwardsPyramid(RealTimeData realTimeData) {
        double now = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-1);
        double prev = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-2);
        double prevprev = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-3);
        return Math.abs(prevprev) <= Math.abs(prev) && Math.abs(prev) <= Math.abs(now);
    }
    public boolean downwardsPyramid(RealTimeData realTimeData) {
        double now = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-1);
        double prev = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-2);
        double prevprev = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-3);
        return Math.abs(prevprev) >= Math.abs(prev) && Math.abs(prev) >= Math.abs(now);
    }

    public boolean negativeThreeHistograms(RealTimeData realTimeData) {
        return realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex()) < 0
                && realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex()-1) < 0
                && realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex()-2) < 0;
    }
    public boolean positiveThreeHistograms(RealTimeData realTimeData) {
        return realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex()) > 0
                && realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex()-1) > 0
                && realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex()-2) > 0;
    }

    public enum DecliningType{
        NEGATIVE,
        POSITIVE;
    }

}
