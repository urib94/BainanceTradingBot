package Positions;
import Data.AccountBalance;
import Data.PrivateConfig;
import Data.RealTimeData;
import Strategies.ExitStrategy;
import com.binance.client.api.RequestOptions;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.trade.Leverage;
import com.binance.client.api.model.trade.Order;
import com.binance.client.api.model.trade.Position;

import java.math.BigDecimal;
import java.util.ArrayList;

public class PositionHandler {
    private String clientOrderId;
    private Long orderID;
    private BigDecimal qty;
    private String symbol;
    private String side;
    private BigDecimal purchasePrice;
    private boolean isActive;
    private String status;
    private BigDecimal pNLPercentage;
    private BigDecimal pNL;
    private BigDecimal liquidationPrice;
    private BigDecimal distanceToLiquidation;
    private BigDecimal leverage;
    private ArrayList<ExitStrategy> exitStrategies;

    public PositionHandler(Order order, BigDecimal leverage, ArrayList<ExitStrategy> exitStrategies){
        clientOrderId = order.getClientOrderId();
        orderID = order.getOrderId();
        qty = BigDecimal.ZERO; //order.getOrigQty(); TODO: originalQty or executedQty
        symbol = order.getSymbol();
        side = order.getSide();
        purchasePrice = order.getPrice();
        isActive = false;
        pNL = BigDecimal.ZERO;
        pNLPercentage = BigDecimal.ZERO;
        liquidationPrice = BigDecimal.ZERO;
        distanceToLiquidation = BigDecimal.ZERO;
        this.leverage = leverage;
        this.exitStrategies = exitStrategies;
        status = PrivateConfig.NEW;
    }

    public synchronized boolean isSoldOut(){ return isActive && (qty.compareTo(BigDecimal.ZERO) <= 0);}

    public synchronized void run(RealTimeData realTimeData){
        for (ExitStrategy exitStrategy: exitStrategies){
            PositionAction positionAction = exitStrategy.run(realTimeData);
            if (positionAction != null){
                //TODO: check exit strategies and sell if needed.
            }
        }
    }

    public synchronized void update() {
        Position position = AccountBalance.getAccountBalance().getPosition(symbol);
        RequestOptions options = new RequestOptions();
        SyncRequestClient syncRequestClient = SyncRequestClient.create(PrivateConfig.API_KEY, PrivateConfig.SECRET_KEY, options);
        Order order = syncRequestClient.getOrder(symbol, orderID , clientOrderId);
        status = order.getStatus();
        pNL = position.getUnrealizedProfit();
        qty = position.getPositionAmt();
        if (!status.equals(PrivateConfig.NEW)) isActive(order);
        //TODO: complete this function and change isActive
    }

    private void isActive(Order order) {
        if (status.equals(PrivateConfig.PARTIALLY_FILLED)){
            //TODO: time job
        }
        else {
            isActive = true;
        }
    }
}
