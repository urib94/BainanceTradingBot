package Positions;
import Data.*;
import Strategies.ExitStrategy;
import Utils.Utils;
import com.binance.client.api.SyncRequestClient;
import com.binance.client.api.model.enums.*;
import com.binance.client.api.model.trade.Order;
import com.binance.client.api.model.trade.Position;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;

public class PositionHandler {
    private final String clientOrderId;
    private final Long orderID;
    private final String stopLossClientOrderId;
    private final Long stopLossOrderID;
    private final String takeProfitClientOrderId;
    private final Long takeProfitOrderID;
    private BigDecimal qty;
    private final String symbol;
    private boolean isActive;
    private String status;
    private ArrayList<ExitStrategy> exitStrategies;
    private Long baseTime;
    private boolean isSelling = false;
    private BigDecimal unrealizedProfit = null;



    public PositionHandler(Order order, Integer leverage, ArrayList<ExitStrategy> exitStrategies){
        clientOrderId = order.getClientOrderId();
        this.stopLossClientOrderId = stopLossClientOrderId;
        this.stopLossOrderID = stopLossOrderID;
        this.takeProfitClientOrderId = takeProfitClientOrderId;
        this.takeProfitOrderID = takeProfitOrderID;
        orderID = order.getOrderId();
        qty = null;
        symbol = order.getSymbol().toLowerCase();
        isActive = false;
        this.exitStrategies = exitStrategies;
        status = Config.NEW;
        this.baseTime = 0L;

    }

    public PositionHandler(BigDecimal qty, String clientOrderId, ArrayList<ExitStrategy> exitStrategies){
        symbol = Config.SYMBOL;
        this.qty = qty;
        this.clientOrderId = clientOrderId;
        this.stopLossClientOrderId = null;
        this.stopLossOrderID = null;
        this.takeProfitClientOrderId = null;
        this.takeProfitOrderID = null;
        orderID = null;
        isActive = false;
        this.exitStrategies = exitStrategies;
        status = Config.NEW;
        this.baseTime = 0L;
    }

    public boolean isSoldOut(){ return false;}//isActive && (qty.compareTo(BigDecimal.ZERO) <= 0);}//TODO: fix

    public void run(RealTimeData realTimeData){
        for (ExitStrategy exitStrategy: exitStrategies){
            //BigDecimal sellingQtyPercentage  = exitStrategy.run(realTimeData);
            BigDecimal sellingQtyPercentage = BigDecimal.valueOf(100.0);//TODO: change test
            if (sellingQtyPercentage != null && status.equals(Config.FILLED) && qty != null && !isSelling){
                isSelling = true;
                if (unrealizedProfit != null && unrealizedProfit.compareTo(BigDecimal.ZERO) <= 0){
                    //TODO: exit with stop loss;
                }
                else if (unrealizedProfit != null){
                    //TODO: exit with take profit;
                }
                System.out.println("selling order: " + clientOrderId);
                String sellingQty = BinanceInfo.formatQty(percentageOfQuantity(sellingQtyPercentage), symbol);
                SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
            }
        }
    }

    public void update(CandlestickInterval interval) {
        AccountBalance.getAccountBalance().aggresiveUpdateBalance();
        Position position = AccountBalance.getAccountBalance().getPosition(symbol);
        System.out.println(position);
        SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
        Order order = syncRequestClient.getOrder(symbol, orderID , clientOrderId);
        status = order.getStatus();
        qty = position.getPositionAmt();
        unrealizedProfit = position.getUnrealizedProfit();
        if (!status.equals(Config.NEW)) isActive(order,interval);
    }

    private void isActive(Order order,CandlestickInterval interval) {
        if (status.equals(Config.PARTIALLY_FILLED)){
            Long updateTime = order.getUpdateTime();
            if (baseTime.equals(0L)) {
                baseTime = updateTime;
            } else {
               long difference = updateTime - baseTime;
               Long intervalInMilliSeconds = Utils.candleStickIntervalToMilliseconds(interval);
               if  (difference >= (intervalInMilliSeconds/2.0)) {
                   SyncRequestClient syncRequestClient = RequestClient.getRequestClient().getSyncRequestClient();
                   syncRequestClient.cancelOrder(symbol,orderID,clientOrderId);
                   isActive = true;
               }
            }
        }
        else {
            isActive = true;
        }
    }

    private BigDecimal percentageOfQuantity(BigDecimal percentage) {
        return qty.multiply(percentage);
    }
}
