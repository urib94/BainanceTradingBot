package Strategies;

import Data.RealTimeData;

import java.math.BigDecimal;

public interface ExitStrategy {
    BigDecimal run(RealTimeData realTimeData);

}
