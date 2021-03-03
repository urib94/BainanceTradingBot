package Strategies.MACDOverRSIStrategies.Long;

import Data.RealTimeData;
import Strategies.EntryStrategy;

public abstract class MACDOverRSILongBaseEntryStrategy implements EntryStrategy {

    public boolean urisRulesOfEntry(RealTimeData realTimeData) {
        double currentMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex());
        double prevMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex()-1);
        double prevPrevMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(realTimeData.getLastCloseIndex()-2);
        boolean rule1 = currentMacdOverRsiValue < 0;
        boolean rule2 = Math.abs(currentMacdOverRsiValue) < Math.abs(prevMacdOverRsiValue);
        boolean rule3 = Math.abs(prevMacdOverRsiValue) < Math.abs(prevPrevMacdOverRsiValue);
        return rule1 && rule2 && rule3;
    }
}
