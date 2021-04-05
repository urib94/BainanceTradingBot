package strategies;

import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import org.ta4j.core.BaseStrategy;
import org.ta4j.core.Rule;
import positions.DCAInstructions;
import positions.PositionHandler;

public interface DCAStrategy {
    boolean timeToDCA=false;
    DCAInstructions DCA_INSTRUCTIONS = null;


    public void setTimeToDCA(DataHolder dataHolder);

    public DCAInstructions getDCAInstructions ();
    public void setDCAInstructions(DCAInstructions dcaInstructions);


    DCAInstructions run(DataHolder realTimeData);
}
