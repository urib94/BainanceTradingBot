package Strategies.MACDOverRSIStrategies;

import Data.RealTimeData;
import Strategies.EntryStrategy;

public abstract class MACDOverRSIBaseEntryStrategy implements EntryStrategy {

    public boolean absoluteDecliningPyramid(RealTimeData realTimeData) {
        double currentMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-1);
        double prevMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-2);
        double prevPrevMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-3);
        boolean rule1 = Math.abs(currentMacdOverRsiValue) < Math.abs(prevMacdOverRsiValue);
        boolean rule2 = Math.abs(prevMacdOverRsiValue) < Math.abs(prevPrevMacdOverRsiValue);
        return rule1 && rule2;
    }

    public boolean currentCandleBiggerThanPrev(RealTimeData realTimeData) {
        double now = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex());
        double prev = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-1);
        return Math.abs(prev) <= Math.abs(now);
    }

}
