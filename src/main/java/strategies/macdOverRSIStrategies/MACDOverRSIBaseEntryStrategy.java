package strategies.macdOverRSIStrategies;

import data.RealTimeData;
import strategies.EntryStrategy;

public abstract class MACDOverRSIBaseEntryStrategy implements EntryStrategy {

    public boolean decliningPyramid(RealTimeData realTimeData, DecliningType type) {
        boolean rule1;
        boolean rule2;
        double currentMacdOverRsiValue = realTimeData.getMacdOverRsiCloseValue();
        double prevMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -1);
        double prevPrevMacdOverRsiValue = realTimeData.getMacdOverRsiValueAtIndex(MACDOverRSIConstants.LAST_CLOSE_INDEX -2);
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
