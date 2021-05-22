package strategies;

import data.DataHolder;
import positions.SellingInstructions;

public interface ExitStrategy {
    SellingInstructions run(DataHolder realTimeData);
}
