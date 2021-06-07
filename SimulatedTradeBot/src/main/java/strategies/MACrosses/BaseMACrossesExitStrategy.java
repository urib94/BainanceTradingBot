package main.java.strategies.MACrosses;

import com.binance.client.model.enums.PositionSide;
import data.DataHolder;
import strategies.ExitStrategy;

public abstract class BaseMACrossesExitStrategy implements ExitStrategy {
    protected boolean slowCondition = false;
    protected boolean fastManagement = true;
    protected final PositionSide positionSide;

    public BaseMACrossesExitStrategy(PositionSide positionSide) {
        this.positionSide = positionSide;
    }


}
