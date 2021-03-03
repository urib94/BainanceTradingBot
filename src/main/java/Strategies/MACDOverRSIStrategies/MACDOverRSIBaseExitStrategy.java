package Strategies.MACDOverRSIStrategies;

import Data.RealTimeData;
import Strategies.ExitStrategy;

public abstract class MACDOverRSIBaseExitStrategy implements ExitStrategy {

    public boolean urisRuleOfExit(RealTimeData realTimeData) {
        double currentMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex());
        double prevMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-1);
        double prevPrevMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-2);
        boolean rule2 = Math.abs(currentMacdOverRsiValue) < Math.abs(prevMacdOverRsiValue);
        boolean rule3 = Math.abs(prevMacdOverRsiValue) < Math.abs(prevPrevMacdOverRsiValue);
        return rule2 && rule3;
    }
    public boolean currentCandleBiggerThanPrev(RealTimeData realTimeData) {
        double now = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex());
        double prev = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-1);
        return Math.abs(prev) <= Math.abs(now);
    }

}
