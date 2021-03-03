package Strategies;

import Data.RealTimeData;
import Positions.SellingInstructions;

import java.math.BigDecimal;

public interface ExitStrategy {
    SellingInstructions run(RealTimeData realTimeData);

}
