package Strategies.MACDOverRSIStrategies;

import Data.RealTimeData;
import Strategies.ExitStrategy;

public abstract class MACDOverRSIBaseExitStrategy implements ExitStrategy {
    public boolean decliningPyramid(RealTimeData realTimeData, DecliningType type) {
        boolean rule1;
        boolean rule2;
        double currentMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-1);
        double prevMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-2);
        double prevPrevMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-3);
        if (type == DecliningType.NEGATIVE){
            rule1 = currentMacdOverRsiValue > prevMacdOverRsiValue;
            rule2 = prevMacdOverRsiValue > prevPrevMacdOverRsiValue;
        }
        else{
            rule1 = currentMacdOverRsiValue < prevMacdOverRsiValue;
            rule2 = prevMacdOverRsiValue < prevPrevMacdOverRsiValue;
        }
        return rule1 && rule2;
    }

    public boolean currentCandleBiggerThanPrev(RealTimeData realTimeData) {
        double now = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-1);
        double prev = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-2);
        return Math.abs(prev) <= Math.abs(now);
    }

    public enum DecliningType{
        NEGATIVE,
        POSITIVE;
    }

}
