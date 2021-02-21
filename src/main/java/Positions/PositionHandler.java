package Positions;
import Data.PrivateConfig;
import Data.RealTimeData;
import Strategies.ExitStrategy;
import com.binance.client.api.RequestOptions;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.trade.Order;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PositionHandler {
    private String clientOrderId;
    private BigDecimal qty;
    private String symbol;
    private String side;
    private BigDecimal purchasePrice;
    private boolean isActive;
    private BigDecimal pNLPercentage;
    private BigDecimal pNL;
    private BigDecimal liquidationPrice;
    private BigDecimal distanceToLiquidation;
    private ArrayList<ExitStrategy> exitStrategies;

    //private Order baseOrder;
    //private Position position;

    public PositionHandler(Order order){

    }

    public synchronized boolean isSoldOut(){
        return true;
    } //TODO: return whether the position entry is sold out.

    public synchronized void run(RealTimeData realTimeData){
        for (ExitStrategy exitStrategy: exitStrategies){
            PositionAction positionAction = exitStrategy.run(realTimeData);
            if (positionAction != null){
                //TODO: check exit strategies and sell if needed.
            }
        }
    }

    public synchronized void update() {
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY,
                options);
        Order order = syncRequestClient.getOrder("BTCUSDT", 37886301L, null);
        //TODO: complete this function
    }
}
