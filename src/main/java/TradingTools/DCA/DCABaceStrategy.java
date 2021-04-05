package TradingTools.DCA;

import data.DataHolder;
import positions.DCAInstructions;
import positions.PositionHandler;
import strategies.DCAStrategy;

public class DCABaceStrategy implements DCAStrategy {
    PositionHandler positionHandler;

    public DCABaceStrategy (PositionHandler positionHandler){
        this.positionHandler=positionHandler;
    }
    @Override
    public void setTimeToDCA(DataHolder dataHolder) {

    }

    @Override
    public DCAInstructions getDCAInstructions() {
        return null;
    }

    @Override
    public void setDCAInstructions(DCAInstructions dcaInstructions) {

    }

    @Override
    public DCAInstructions run(DataHolder realTimeData) {
        return null;
    }
}
