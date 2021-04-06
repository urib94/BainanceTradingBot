package strategies;

import data.DataHolder;
import positions.Instructions;
import positions.SellingInstructions;

public interface ExitStrategy {
    public void updateExitStrategy();
    Instructions run(DataHolder realTimeData);
}
