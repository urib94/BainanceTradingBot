package Strategies.MACDOverRSIStrategies;

import Data.RealTimeData;
import Strategies.EntryStrategy;

public abstract class MACDOverRSIBaseEntryStrategy implements EntryStrategy {

    public boolean urisRulesOfEntry(RealTimeData realTimeData) {
        double currentMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex());
        double prevMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-1);
        double prevPrevMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastIndex()-2);
        boolean rule1 = currentMacdOverRsiValue < 0;
        boolean rule2 = Math.abs(currentMacdOverRsiValue) < Math.abs(prevMacdOverRsiValue);
        boolean rule3 = Math.abs(prevMacdOverRsiValue) < Math.abs(prevPrevMacdOverRsiValue);
        return rule1 && rule2 && rule3;
    }
}
