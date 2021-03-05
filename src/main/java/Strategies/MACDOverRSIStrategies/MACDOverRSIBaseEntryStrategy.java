package Strategies.MACDOverRSIStrategies;

import Data.RealTimeData;
import Strategies.EntryStrategy;

public abstract class MACDOverRSIBaseEntryStrategy implements EntryStrategy {

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

    public enum DecliningType{
        NEGATIVE,
        POSITIVE;
    }

}
