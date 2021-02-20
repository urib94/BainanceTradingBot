package Strategies;
import Data.AccountBalance;
import Data.RealTimeData;
import Data.PositionAction;
import Strategies.ExitStrategy;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PositionEntry {
    private String positionId;
    private BigDecimal balance;
    private BigDecimal expectedBalance;
    private String  asset;
    private ArrayList<ExitStrategy> exitStrategies;

    public PositionEntry(String _positionId, BigDecimal _balance, String _asset, ArrayList<ExitStrategy> _exitStrategies){
        positionId = _positionId;
        balance = _balance;
        asset = _asset;
        exitStrategies = _exitStrategies;
        expectedBalance = balance;
    }

    public BigDecimal getBalance(){
        return balance;
    }

    public void run(RealTimeData realTimeData){
        for (ExitStrategy exitStrategy: exitStrategies){
            PositionAction positionAction = exitStrategy.run(realTimeData);
            if (positionAction != null){

            }
        }
    }

    public void update() {

    }
}
