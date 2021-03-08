package strategies;

import data.RealTimeData;
import positions.SellingInstructions;

public interface ExitStrategy {
    SellingInstructions run(RealTimeData realTimeData);
}
